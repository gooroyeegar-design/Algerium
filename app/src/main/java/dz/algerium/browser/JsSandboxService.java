package dz.algerium.browser;

import android.app.Service;
import android.content.Intent;
import android.os.*;
import java.util.*;
import java.util.concurrent.*;

public final class JsSandboxService extends Service {
    static final int MSG_START = 1;
    static final int MSG_CHUNK = 2;
    static final int MSG_STOP = 3;
    static final int MSG_RESULT_CHUNK = 4;
    static final int MSG_RESULT_META = 5;
    static final int CHUNK_CHARS = 180000;
    static final int MAX_REQUEST_CHARS = BrowserPolicy.pageLimit() * 2;
    static final int MAX_CHUNKS = 64;

    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private final HashMap<Integer, Request> requests = new HashMap<>();
    private final Messenger messenger = new Messenger(new Handler(Looper.getMainLooper()) {
        @Override public void handleMessage(Message msg) {
            if (msg.what == MSG_START) startRequest(msg);
            else if (msg.what == MSG_CHUNK) addChunk(msg);
            else if (msg.what == MSG_STOP) requests.remove(msg.arg1);
            else super.handleMessage(msg);
        }
    });

    static final class Request {
        final int id, total;
        final String base;
        final Messenger reply;
        final String[] chunks;
        int received;
        int chars;
        Request(int id,int total,String base,Messenger reply){
            this.id=id;this.total=total;this.base=base;this.reply=reply;
            this.chunks=new String[total];
        }
    }

    @Override public IBinder onBind(Intent intent) { return messenger.getBinder(); }

    private synchronized void startRequest(Message msg) {
        Bundle b=msg.getData();
        int id=msg.arg1, total=b.getInt("total",0);
        String base=b.getString("base","");
        if(total<1||total>MAX_CHUNKS||base.length()>4096||!BrowserPolicy.isHttpUrl(base)){
            replyError(msg.replyTo,id,"Rejected sandbox request");
            return;
        }
        requests.put(id,new Request(id,total,base,msg.replyTo));
    }

    private synchronized void addChunk(Message msg) {
        Request r=requests.get(msg.arg1);
        if(r==null)return;
        Bundle b=msg.getData();
        int index=b.getInt("index",-1);
        String data=b.getString("data","");
        if(index<0||index>=r.total||data.length()>CHUNK_CHARS||r.chunks[index]!=null){
            requests.remove(r.id); replyError(r.reply,r.id,"Rejected sandbox chunk"); return;
        }
        r.chunks[index]=data;r.received++;r.chars+=data.length();
        if(r.chars>MAX_REQUEST_CHARS){requests.remove(r.id);replyError(r.reply,r.id,"Sandbox request too large");return;}
        if(r.received==r.total){
            requests.remove(r.id);
            worker.execute(()->execute(r));
        }
    }

    private void execute(Request r) {
        StringBuilder html=new StringBuilder(r.chars);
        for(String c:r.chunks) html.append(c);
        String out;
        JsRuntimeCore core=new JsRuntimeCore();
        try { out=core.run(html.toString(),r.base); }
        catch(Throwable t){ out=html.toString(); }
        int scripts=core.getScriptsRun(), ms=(int)Math.min(Integer.MAX_VALUE,core.getLastRunMs());
        String err=core.getLastError();
        sendResult(r.reply,r.id,out,scripts,ms,err);
    }

    private void sendResult(Messenger reply,int id,String out,int scripts,int ms,String err) {
        if(out==null)out="";
        try {
            int total=(out.length()+CHUNK_CHARS-1)/CHUNK_CHARS;
            if(total>MAX_CHUNKS){replyError(reply,id,"Sandbox result too large");return;}
            Message meta=Message.obtain(null,MSG_RESULT_META);
            meta.arg1=id;meta.arg2=total;
            Bundle b=new Bundle();b.putInt("scripts",scripts);b.putInt("ms",ms);b.putString("error",err==null?"":err);meta.setData(b);
            reply.send(meta);
            for(int i=0;i<total;i++){
                int end=Math.min(out.length(),(i+1)*CHUNK_CHARS);
                Message m=Message.obtain(null,MSG_RESULT_CHUNK);
                m.arg1=id;m.arg2=i;
                Bundle x=new Bundle();x.putString("data",out.substring(i*CHUNK_CHARS,end));m.setData(x);
                reply.send(m);
            }
        }catch(Throwable ignored){}
    }

    private void replyError(Messenger reply,int id,String error){
        if(reply==null)return;
        try{
            Message m=Message.obtain(null,MSG_RESULT_META);m.arg1=id;m.arg2=-1;
            Bundle b=new Bundle();b.putString("error",error);b.putInt("scripts",0);b.putInt("ms",0);m.setData(b);reply.send(m);
        }catch(Throwable ignored){}
    }

    @Override public void onDestroy(){
        worker.shutdownNow();
        synchronized(this){requests.clear();}
        super.onDestroy();
    }
}
