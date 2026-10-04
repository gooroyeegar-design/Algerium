package dz.algerium.browser;

import android.os.SystemClock;
import com.whl.quickjs.wrapper.JSCallFunction;
import com.whl.quickjs.wrapper.JSObject;import com.whl.quickjs.wrapper.QuickJSLoader;
import com.whl.quickjs.wrapper.QuickJSContext;
import java.io.*;import java.net.*;import java.util.*;import java.util.concurrent.*;

public final class JsEngine {
 private final ExecutorService jsThread=Executors.newSingleThreadExecutor(r->{Thread t=new Thread(r,"algerium-js");t.setDaemon(true);return t;});
 private QuickJSContext ctx; private volatile String lastError=""; private volatile int scriptsRun=0;
 private static final JsEngine INSTANCE=new JsEngine();
 public static JsEngine get(){return INSTANCE;}
 public String getLastError(){return lastError;} public int getScriptsRun(){return scriptsRun;}
 private void init(){
  if(ctx!=null)return;
  ctx=QuickJSContext.create();
  JSObject console=ctx.createNewJSObject();
  console.setProperty("log",new JSCallFunction(){public Object call(Object...a){return null;}});
  console.setProperty("warn",new JSCallFunction(){public Object call(Object...a){return null;}});
  console.setProperty("error",new JSCallFunction(){public Object call(Object...a){return null;}});
  ctx.getGlobalObject().setProperty("console",console);
  ctx.getGlobalObject().setProperty("algeriumFetch",new JSCallFunction(){public Object call(Object...a){if(a.length==0)return "";try{return fetch(String.valueOf(a[0]));}catch(Exception e){lastError=e.toString();return "";}}});
 }
 public String run(String html,String baseUrl){
  try{return jsThread.submit(()->runOnThread(html,baseUrl)).get(45,TimeUnit.SECONDS);}
  catch(Exception e){lastError=e.toString();return html;}
 }
 private String runOnThread(String html,String baseUrl){
  init();lastError="";
  try{
   String bootstrap=""+
   "var window=this;"+
   "var location={href:"+q(baseUrl)+",origin:"+q(origin(baseUrl))+"};"+
   "var navigator={userAgent:'Mozilla/5.0 (Linux; Android 13) Algerium/1.0',language:'en-US',platform:'Android'};"+
   "var __html="+q(html)+";"+
   "function __el(tag){var e={tagName:String(tag).toUpperCase(),style:{},children:[],attributes:{},innerHTML:'',textContent:'',value:'',id:'',className:'',onclick:null};"+
   "e.setAttribute=function(k,v){e.attributes[k]=String(v);if(k==='id')e.id=String(v);if(k==='class')e.className=String(v);};"+
   "e.getAttribute=function(k){return e.attributes[k]===undefined?null:e.attributes[k]};"+
   "e.appendChild=function(x){e.children.push(x);return x;};"+
   "e.remove=function(){}; e.addEventListener=function(n,f){if(n==='click')e.onclick=f;};"+
   "return e;}"+
   "var document={readyState:'complete',cookie:'',title:'',body:__el('body'),head:__el('head'),documentElement:__el('html'),"+
   "createElement:function(t){return __el(t)},createTextNode:function(t){return {textContent:String(t)}},"+
   "getElementById:function(id){var e=__el('div');e.id=id;return e},"+
   "querySelector:function(s){return __el('div')},querySelectorAll:function(s){return []},"+
   "getElementsByTagName:function(s){return []},addEventListener:function(n,f){if(n==='DOMContentLoaded')f();},"+
   "write:function(x){__html+=String(x);document.body.innerHTML=__html;}};"+
   "document.body.innerHTML=__html;document.documentElement.innerHTML=__html;"+
   "window.document=document;window.setTimeout=function(f){try{f()}catch(e){console.error(e)}return 1};"+
   "window.setInterval=function(f){try{f()}catch(e){console.error(e)}return 1};window.clearTimeout=function(){};window.clearInterval=function(){};"+
   "window.requestAnimationFrame=function(f){try{f(Date.now())}catch(e){}return 1};"+
   "window.cancelAnimationFrame=function(){};window.scrollTo=function(){};window.scrollX=0;window.scrollY=0;"+
   "window.fetch=function(u){return Promise.resolve({ok:true,status:200,text:function(){return Promise.resolve(algeriumFetch(new URL(String(u),location.href).href))},json:function(){var t=algeriumFetch(new URL(String(u),location.href).href);try{return Promise.resolve(JSON.parse(t))}catch(e){return Promise.reject(e)}}})};"+
   "window.localStorage={_d:{},getItem:function(k){return this._d[k]??null},setItem:function(k,v){this._d[k]=String(v)},removeItem:function(k){delete this._d[k]},clear:function(){this._d={}}};"+
   "window.sessionStorage=window.localStorage;window.alert=function(){};window.confirm=function(){return true};window.prompt=function(){return null};";
   ctx.evaluate(bootstrap,"<algerium-dom>");
   java.util.regex.Matcher m=java.util.regex.Pattern.compile("(?is)<script\\b([^>]*)>(.*?)</script>").matcher(html);
   while(m.find()){
    String attrs=m.group(1),code=m.group(2);String src=attr(attrs,"src");
    try{if(src!=null&&!src.isEmpty()){String full=new URL(new URL(baseUrl),src).toString();code=fetch(full);}if(code!=null&&!code.trim().isEmpty()){ctx.evaluate(code,src==null?"<inline-script>":src);scriptsRun++;}}
    catch(Exception ex){lastError=ex.toString();}
   }
   try{Object out=ctx.evaluate("document.body.innerHTML","<result>");return out==null?html:String.valueOf(out);}catch(Exception e){lastError=e.toString();return html;}
  }catch(Exception e){lastError=e.toString();return html;}
 }
 private String fetch(String u)throws Exception{HttpURLConnection c=(HttpURLConnection)new URL(u).openConnection();c.setInstanceFollowRedirects(true);c.setConnectTimeout(10000);c.setReadTimeout(15000);c.setRequestProperty("User-Agent","Mozilla/5.0 (Linux; Android 13) Algerium/1.0");InputStream in=c.getResponseCode()>=400?c.getErrorStream():c.getInputStream();if(in==null)return "";BufferedReader r=new BufferedReader(new InputStreamReader(in,"UTF-8"));StringBuilder s=new StringBuilder();String x;while((x=r.readLine())!=null)s.append(x).append('\n');return s.toString();}
 private static String attr(String a,String k){java.util.regex.Matcher m=java.util.regex.Pattern.compile("(?i)\\b"+k+"\\s*=\\s*[\\\"']([^\\\"']+)[\\\"']").matcher(a);return m.find()?m.group(1):null;}
 private static String origin(String u){try{URL x=new URL(u);return x.getProtocol()+"://"+x.getAuthority();}catch(Exception e){return u;}}
 private static String q(String s){return "'"+s.replace("\\","\\\\").replace("'","\\'").replace("\r","\\r").replace("\n","\\n")+"'";}
}