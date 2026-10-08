package dz.algerium.browser;

import android.content.*;
import android.graphics.*;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class PageView extends View {
    Paint p = new Paint(3);
    String url="", html="";
    ArrayList<String> stack=new ArrayList<>();
    int pos=-1;
    float scroll=0, sy, downX, downY;
    ArrayList<Block> blocks=new ArrayList<>();
    ArrayList<Link> links=new ArrayList<>();
    String query="";
    private NavigationListener navigationListener;

    public interface NavigationListener { void open(String url); }
    static class Link { String url; float x,y,w,h; Link(String u,float X,float Y,float W,float H){url=u;x=X;y=Y;w=W;h=H;} boolean hit(float px,float py){return px>=x&&px<=x+w&&py>=y&&py<=y+h;} }
    static class Block {
        String text; float x,y,w,h; int size,color,type; boolean bold;
        Block(String t,float X,float Y,float W,float H,int S,int C,int T,boolean B){
            text=t;x=X;y=Y;w=W;h=H;size=S;color=C;type=T;bold=B;
        }
    }

    public PageView(Context c){
        super(c);
        p.setTypeface(Typeface.create("sans",0));
        setBackgroundColor(Color.WHITE);
        setFocusable(true);
    }
    public void setNavigationListener(NavigationListener l){navigationListener=l;}
    void home(){blocks.clear();links.clear();scroll=0;setBackgroundColor(Color.rgb(243,232,210));invalidate();}

    void load(String u){
        if(u.equals("about:home")){home();return;}
        if(!BrowserPolicy.isHttpUrl(u)){error(u,"Blocked URL scheme");return;}
        new Thread(()->{
            try{
                HttpURLConnection c=(HttpURLConnection)new URL(u).openConnection();
                c.setInstanceFollowRedirects(true); c.setConnectTimeout(12000); c.setReadTimeout(20000);
                c.setRequestProperty("User-Agent","Algerium/1.0");
                c.setRequestProperty("Accept","text/html,application/xhtml+xml,text/plain,*/*");
                int code=c.getResponseCode();
                InputStream in=code>=400?c.getErrorStream():c.getInputStream();
                String h=BrowserPolicy.readLimited(in,BrowserPolicy.pageLimit());
                String finalUrl=c.getURL().toString();
                String rendered=isSearchPage(finalUrl)?h:JsEngine.get().run(h,finalUrl);
                rendered=stripNonRenderableScripts(rendered);
                final String pageHtml=rendered;
                post(()->{url=finalUrl;html=pageHtml;push(finalUrl);parse();invalidate();});
            }catch(Exception e){post(()->error(u,e.toString()));}
        }).start();
    }

    void push(String u){
        if(pos>=0&&pos<stack.size()-1)stack.subList(pos+1,stack.size()).clear();
        if(pos<0||!stack.get(pos).equals(u)){stack.add(u);pos=stack.size()-1;}
    }
    boolean isSearchPage(String u){
        try{URI x=new URI(u);String h=x.getHost()==null?"":x.getHost().toLowerCase(Locale.US);
            return h.equals("google.com")||h.endsWith(".google.com");
        }catch(Exception e){return false;}
    }
    String stripNonRenderableScripts(String s){
        if(s==null)return "";
        return s.replaceAll("(?is)<script\\b[^>]*>.*?</script>","")
                .replaceAll("(?is)<noscript\\b[^>]*>.*?</noscript>","")
                .replaceAll("(?is)<template\\b[^>]*>.*?</template>","");
    }

    void parse(){
        setBackgroundColor(Color.rgb(255,249,238)); blocks.clear(); scroll=0;
        try{
            String cleaned=stripNonRenderableScripts(html);
            String rendered=NativeEngine.render(cleaned,url,Math.max(320,getWidth()));
            for(String line:rendered.split("\\n")){
                if(line.trim().isEmpty())continue;
                String[] q=line.split("\\t",-1); if(q.length<8)continue;
                int type=Integer.parseInt(q[0]); float x=Float.parseFloat(q[1]), y=Float.parseFloat(q[2]);
                float w=Float.parseFloat(q[3]), h=Float.parseFloat(q[4]);
                int size=Math.max(1,(int)Float.parseFloat(q[5])); int color=parseColor(q[6]);
                String text=q[7].replace("\\n","\n").replace("\\t","\t").replace("\\\\","\\");
                blocks.add(new Block(text,x,y,w,h,size,color,type,size>=22));
            }
            if(!blocks.isEmpty()){buildLinkHitboxes(cleaned);invalidate();return;}
        }catch(Throwable ignored){}
        String s;
        try{s=NativeEngine.extractText(html,url);}catch(Throwable ignored){
            s=html.replaceAll("(?is)<script.*?</script>|<style.*?</style>|<svg.*?</svg>|<!--.*?-->","");
        }
        s=s.replaceAll("(?is)<(br|hr)\\s*/?>","\n");
        s=s.replaceAll("(?is)</(p|div|section|article|h[1-6]|li|tr|header|footer|main|nav|title|form|pre|table|blockquote)>","\n\n");
        s=s.replaceAll("(?is)<li[^>]*>","• ");
        s=s.replaceAll("(?is)<[^>]+>","");
        s=decode(s).replaceAll("[ \\t]+"," ").replaceAll("\\n{3,}","\n\n").trim();
        float y=30;
        for(String para:s.split("\\n+")){
            String t=para.trim();if(t.isEmpty())continue;
            int size=t.length()<65?22:17;float w=Math.max(200,getWidth()-32);
            while(!t.isEmpty()){
                int n=Math.min(t.length(),Math.max(10,(int)(w/(size*.52f))));int cut=n;
                if(n<t.length()){int k=t.lastIndexOf(' ',n);if(k>4)cut=k;}
                String part=t.substring(0,cut).trim();t=t.substring(cut).trim();
                blocks.add(new Block(part,16,y,w,size,size,Color.rgb(25,25,28),1,size>=22));y+=size*1.55f;
            }
        }
        if(blocks.isEmpty())blocks.add(new Block("No readable text was found on this page.",16,40,getWidth()-32,30,18,Color.DKGRAY,1,false));
        buildLinkHitboxes(html);
    }

    int parseColor(String s){
        try{
            if(s==null||s.equals("transparent")||s.isEmpty())return Color.TRANSPARENT;
            if(s.startsWith("#")){
                long v=Long.parseLong(s.substring(1),16);
                if(s.length()==7)return Color.rgb((int)(v>>16)&255,(int)(v>>8)&255,(int)v&255);
                if(s.length()==4)return Color.rgb((int)((v>>8)&15)*17,(int)((v>>4)&15)*17,(int)(v&15)*17);
            }
        }catch(Exception ignored){}
        return Color.DKGRAY;
    }
    String decode(String s){
        return s.replace("&nbsp;"," ").replace("&amp;","&").replace("&lt;","<")
                .replace("&gt;",">").replace("&quot;","\"").replace("&#39;","'");
    }

    @Override protected void onDraw(Canvas c){
        super.onDraw(c); c.save(); c.translate(0,-scroll); p.setSubpixelText(true);

        // Paint every background first. This prevents a later button/background box
        // from covering text that was already drawn by the renderer.
        for(Block b:blocks){
            if(b.y+b.h<scroll-80 || b.y>scroll+getHeight()+80)continue;
            if(b.type==0){
                p.setStyle(Paint.Style.FILL);p.setColor(b.color);
                c.drawRect(b.x,b.y,b.x+b.w,b.y+b.h,p);
            }else if(b.type==2){
                p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(Math.max(1,b.size));p.setColor(b.color);
                c.drawRect(b.x,b.y,b.x+b.w,b.y+b.h,p);p.setStyle(Paint.Style.FILL);
            }
        }

        // Then paint text on top and vertically center it inside its layout box.
        for(Block b:blocks){
            if(b.type!=1 || b.y+b.h<scroll-80 || b.y>scroll+getHeight()+80)continue;
            p.setStyle(Paint.Style.FILL);p.setColor(b.color);p.setTextSize(b.size);
            p.setTypeface(Typeface.create("sans",b.bold?Typeface.BOLD:Typeface.NORMAL));
            ArrayList<String> lines=wrap(b.text,Math.max(20,b.w));
            float lineHeight=b.size*1.5f;
            float total=lines.size()*lineHeight;
            float baseline=b.y+Math.max(b.size,(b.h-total+b.size)*.5f);
            for(String line:lines){c.drawText(line,b.x,baseline,p);baseline+=lineHeight;}
        }
        c.restore();
    }

    ArrayList<String> wrap(String s,float w){
        ArrayList<String>a=new ArrayList<>();String line="";
        for(String q:s.split(" ")){
            String z=line.isEmpty()?q:line+" "+q;
            if(p.measureText(z)>w&&!line.isEmpty()){a.add(line);line=q;}else line=z;
        }
        if(!line.isEmpty())a.add(line);return a;
    }

    void buildLinkHitboxes(String source){
        links.clear();
        if(source==null||source.isEmpty()||blocks.isEmpty())return;
        String lower=source.toLowerCase(Locale.US);
        int at=0;
        while((at=lower.indexOf("<a",at))>=0){
            int openEnd=source.indexOf('>',at); if(openEnd<0)break;
            int close=lower.indexOf("</a>",openEnd); if(close<0)break;
            String tag=source.substring(at,openEnd+1);
            int hs=tag.toLowerCase(Locale.US).indexOf("href=");
            if(hs>=0){
                int p=hs+5; while(p<tag.length()&&Character.isWhitespace(tag.charAt(p)))p++;
                String href="";
                if(p<tag.length()&&(tag.charAt(p)=='"'||tag.charAt(p)==(char)39')){char q=tag.charAt(p++);int e=tag.indexOf(q,p);if(e>p)href=tag.substring(p,e);}
                else {int e=p;while(e<tag.length()&&!Character.isWhitespace(tag.charAt(e))&&tag.charAt(e)!='>')e++;href=tag.substring(p,e);}
                String label=source.substring(openEnd+1,close).replaceAll("(?is)<[^>]+>","").replaceAll("\\s+"," ").trim();
                if(!href.isEmpty()&&!label.isEmpty())try{
                    String target=BrowserPolicy.resolveHttp(url,decode(href));
                    for(Block b:blocks)if(b.type==1&&b.text.toLowerCase(Locale.US).contains(label.toLowerCase(Locale.US))){
                        p.setTextSize(b.size); links.add(new Link(target,b.x,b.y,Math.min(b.w,Math.max(80,p.measureText(label)+20)),Math.max(b.h,b.size*1.5f)));break;
                    }
                }catch(Exception ignored){}
            }
            at=close+4;
        }
    }

    @Override public boolean onTouchEvent(MotionEvent e){
        if(e.getAction()==0){sy=e.getY();downX=e.getX();downY=e.getY();return true;}
        if(e.getAction()==2){
            float d=sy-e.getY(); if(Math.abs(d)<0.5f)return true;
            scroll=Math.max(0,Math.min(Math.max(0,contentHeight()-getHeight()+30),scroll+d));
            sy=e.getY();invalidate();return true;
        }
        if(e.getAction()==1){
            if(Math.abs(e.getY()-downY)+Math.abs(e.getX()-downX)<20){
                float py=e.getY()+scroll;
                for(Link l:links)if(l.hit(e.getX(),py)){if(navigationListener!=null)navigationListener.open(l.url);break;}
            }
            performClick(); return true;
        }
        return true;
    }
    @Override public boolean performClick(){super.performClick();return true;}
    int contentHeight(){return blocks.isEmpty()?0:(int)(blocks.get(blocks.size()-1).y+Math.max(100,blocks.get(blocks.size()-1).h));}
    boolean canBack(){return pos>0;} boolean canForward(){return pos>=0&&pos<stack.size()-1;}
    String backUrl(){if(pos>0){pos--;return stack.get(pos);}return "about:home";}
    String forwardUrl(){if(canForward()){pos++;return stack.get(pos);}return "about:home";}
    void find(String q){
        query=q.toLowerCase();
        for(int i=0;i<blocks.size();i++)if(blocks.get(i).text.toLowerCase().contains(query)){
            scroll=Math.max(0,blocks.get(i).y-80);invalidate();Toast.makeText(getContext(),"Found",Toast.LENGTH_SHORT).show();return;
        }
        Toast.makeText(getContext(),"Not found",Toast.LENGTH_SHORT).show();
    }
    @Override public boolean onGenericMotionEvent(MotionEvent e){
        if((e.getSource()&InputDevice.SOURCE_CLASS_POINTER)!=0&&e.getAction()==MotionEvent.ACTION_SCROLL){
            float d=-e.getAxisValue(MotionEvent.AXIS_VSCROLL)*48f;
            scroll=Math.max(0,Math.min(Math.max(0,contentHeight()-getHeight()+30),scroll+d));invalidate();return true;
        }
        return super.onGenericMotionEvent(e);
    }
    void error(String u,String e){
        blocks.clear();
        blocks.add(new Block("Algerium couldn't load this page",20,60,getWidth()-40,38,25,Color.rgb(35,35,35),1,true));
        blocks.add(new Block(u,20,112,getWidth()-40,30,16,Color.DKGRAY,1,false));
        blocks.add(new Block(e,20,160,getWidth()-40,48,15,Color.DKGRAY,1,false));
        invalidate();
    }
}
