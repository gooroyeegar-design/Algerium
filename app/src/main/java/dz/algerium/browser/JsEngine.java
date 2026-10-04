package dz.algerium.browser;

import android.content.*;
import android.os.*;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.regex.*;

public final class JsEngine {
    private static final JsEngine INSTANCE=new JsEngine();
    private static final int CHUNK_CHARS=180000;
    private final Object lock=new Object();
    private Context context;
    private Messenger service;
    private boolean binding;
    private int nextId=1;
    private volatile String lastError="";
    private volatile int scriptsRun=0;
    private volatile long lastRunMs=0;

    private JsEngine(){}
    public static JsEngine get(){return INSTANCE;}
    public void init(Context c){synchronized(lock){if(context==null)context=c.getApplicationContext();ensureBoundLocked();}}
    public String getLastError(){return lastError;}
    public int getScriptsRun(){return scriptsRun;}
    public long getLastRunMs(){return lastRunMs;}
    public boolean isSandboxBound(){synchronized(lock){return service!=null;}}

    public String run(String html,String baseUrl){
        if(html==null)return "";
        if(context==null)return html;
        if(html.length()>BrowserPolicy.pageLimit()*2){lastError="Page source exceeded safety limit";return html;}
        String prepared=inlineExternalScripts(html,baseUrl);
        if(prepared.length()>BrowserPolicy.pageLimit()*2){lastError="Prepared page exceeded safety limit";return html;}
        final int id;
        synchronized(lock){
            ensureBoundLocked();
            long deadline=System.currentTimeMillis()+3000;
            while(service==null && binding){
                long left=deadline-System.currentTimeMillis();
                if(left<=0)break;
                try{lock.wait(left);}catch(InterruptedException e){Thread.currentThread().interrupt();break;}
            }
            if(service==null){lastError="Isolated JavaScript service unavailable";return html;}
            id=nextId++;
        }
        Result result=new Result();
        Messenger client=new Messenger(new ReplyHandler(result,id));
        int total=(prepared.length()+CHUNK_CHARS-1)/CHUNK_CHARS;
        if(total<1)total=1;
        if(total>64){lastError="Sandbox request too large";return html;}
        try{
            Message start=Message.obtain(null,JsSandboxService.MSG_START);
            start.arg1=id;
            Bundle sb=new Bundle();sb.putInt("total",total);sb.putString("base",baseUrl==null?"":baseUrl);start.setData(sb);start.replyTo=client;
            service.send(start);
            for(int i=0;i<total;i++){
                int end=Math.min(prepared.length(),(i+1)*CHUNK_CHARS);
                Message m=Message.obtain(null,JsSandboxService.MSG_CHUNK);m.arg1=id;m.arg2=i;
                Bundle b=new Bundle();b.putInt("index",i);b.putString("data",prepared.substring(i*CHUNK_CHARS,end));m.setData(b);service.send(m);
            }
            if(!result.done.await(13,TimeUnit.SECONDS)){lastError="JavaScript sandbox timed out";return html;}
            if(result.error!=null&&!result.error.isEmpty())lastError=result.error;
            scriptsRun=result.scripts;lastRunMs=result.ms;
            return result.output==null?html:result.output;
        }catch(Throwable e){
            lastError=safeError(e);
            synchronized(lock){service=null;ensureBoundLocked();}
            return html;
        }
    }

    private final class ReplyHandler extends Handler {
        final Result result; final int id;
        ReplyHandler(Result r,int i){super(Looper.getMainLooper());result=r;id=i;}
        @Override public void handleMessage(Message msg){
            if(msg.arg1!=id)return;
            if(msg.what==JsSandboxService.MSG_RESULT_META){
                Bundle b=msg.getData();
                result.scripts=b.getInt("scripts");result.ms=b.getInt("ms");result.error=b.getString("error","");
                int total=msg.arg2;
                if(total<0){result.done.countDown();return;}
                result.parts=new String[total];
                if(total==0){result.output="";result.done.countDown();}
            }else if(msg.what==JsSandboxService.MSG_RESULT_CHUNK){
                if(result.parts==null||msg.arg2<0||msg.arg2>=result.parts.length)return;
                result.parts[msg.arg2]=msg.getData().getString("data","");
                result.received++;
                if(result.received==result.parts.length){
                    StringBuilder s=new StringBuilder();
                    for(String p:result.parts)s.append(p==null?"":p);
                    result.output=s.toString();result.done.countDown();
                }
            }
        }
    }

    static final class Result{
        final CountDownLatch done=new CountDownLatch(1);
        String[] parts;String output,error;int received,scripts,ms;
    }

    private void ensureBoundLocked(){
        if(binding||service!=null||context==null)return;
        binding=true;
        Intent i=new Intent(context,JsSandboxService.class);
        try{
            context.bindService(i,new ServiceConnection(){
                public void onServiceConnected(ComponentName n,IBinder b){synchronized(lock){service=new Messenger(b);binding=false;lock.notifyAll();}}
                public void onServiceDisconnected(ComponentName n){synchronized(lock){service=null;binding=false;lock.notifyAll();}}
                public void onBindingDied(ComponentName n){synchronized(lock){service=null;binding=false;lock.notifyAll();}}
                public void onNullBinding(ComponentName n){synchronized(lock){service=null;binding=false;lock.notifyAll();}}
            },Context.BIND_AUTO_CREATE);
        }catch(Throwable t){binding=false;lastError=safeError(t);}
    }

    private String inlineExternalScripts(String html,String base){
        if(html==null||base==null)return html;
        Matcher m=Pattern.compile("(?is)<script\\b([^>]*)>(.*?)</script>").matcher(html);
        StringBuffer out=new StringBuffer();int count=0;int totalBytes=0;
        while(m.find()){
            String attrs=m.group(1),code=m.group(2),src=attr(attrs,"src");
            if(src==null||src.isEmpty()){m.appendReplacement(out,Matcher.quoteReplacement(m.group()));continue;}
            if(++count>32){lastError="Too many external scripts; remaining scripts blocked";break;}
            try{
                if(!BrowserPolicy.isSafeScriptUrl(base,src)){lastError="Blocked external script URL";continue;}
                String u=BrowserPolicy.resolveHttp(base,src);URI x=new URI(u);
                if(BrowserPolicy.isPrivateOrLocalHost(x.getHost())){lastError="Blocked private-network script";continue;}
                String js=fetchScript(u);if(js==null)continue;
                totalBytes+=js.length();
                if(totalBytes>BrowserPolicy.pageLimit()){lastError="External script budget exceeded";break;}
                js=js.replace("</script","<\\/script");
                m.appendReplacement(out,Matcher.quoteReplacement("<script>"+js+"</script>"));
            }catch(Throwable e){lastError=safeError(e);}
        }
        m.appendTail(out);return out.toString();
    }

    private String fetchScript(String u)throws Exception{
        HttpURLConnection c=(HttpURLConnection)new URL(u).openConnection();
        c.setInstanceFollowRedirects(true);c.setConnectTimeout(7000);c.setReadTimeout(10000);
        c.setRequestProperty("User-Agent","Algerium/1.1");c.setRequestProperty("Accept","application/javascript,text/javascript,*/*");
        int code=c.getResponseCode();if(code<200||code>=400)throw new IOException("Script HTTP "+code);
        String finalUrl=c.getURL().toString();URI x=new URI(finalUrl);
        if(!BrowserPolicy.isHttpUrl(finalUrl)||BrowserPolicy.isPrivateOrLocalHost(x.getHost()))throw new SecurityException("Redirected script blocked");
        return BrowserPolicy.readLimited(c.getInputStream(),BrowserPolicy.scriptLimit());
    }

    private static String attr(String a,String k){
        Matcher m=Pattern.compile("(?i)\\b"+k+"\\s*=\\s*[\\\"']([^\\\"']+)[\\\"']").matcher(a);
        return m.find()?m.group(1):null;
    }
    private static String safeError(Throwable t){String s=t==null?"unknown error":String.valueOf(t);return s.length()>300?s.substring(0,300):s;}
}
