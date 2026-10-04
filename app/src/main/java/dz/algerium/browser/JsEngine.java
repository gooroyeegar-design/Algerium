package dz.algerium.browser;

import com.whl.quickjs.wrapper.JSCallFunction;
import com.whl.quickjs.wrapper.JSObject;
import com.whl.quickjs.wrapper.QuickJSContext;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public final class JsEngine {
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "algerium-js");
        t.setDaemon(true);
        return t;
    });
    private static final JsEngine INSTANCE = new JsEngine();
    private volatile String lastError = "";
    private volatile int scriptsRun = 0;
    private volatile long lastRunMs = 0;

    public static JsEngine get() { return INSTANCE; }
    public String getLastError() { return lastError; }
    public int getScriptsRun() { return scriptsRun; }
    public long getLastRunMs() { return lastRunMs; }

    public String run(String html, String baseUrl) {
        if (html == null) return "";
        if (html.length() > BrowserPolicy.pageLimit() * 2) {
            lastError = "Page source exceeded safety limit";
            return html.substring(0, Math.min(html.length(), BrowserPolicy.pageLimit()));
        }
        try {
            return executor.submit(() -> runContext(html, baseUrl)).get(12, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            lastError = "JavaScript execution timed out";
            return html;
        } catch (Exception e) {
            lastError = safeError(e);
            return html;
        }
    }

    private String runContext(String html, String baseUrl) {
        long start = System.currentTimeMillis();
        QuickJSContext ctx = null;
        scriptsRun = 0;
        lastError = "";
        try {
            ctx = QuickJSContext.create();
            installHost(ctx, baseUrl);
            ctx.evaluate(bootstrap(html, baseUrl), "<algerium-dom>");

            java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("(?is)<script\\b([^>]*)>(.*?)</script>").matcher(html);

            while (m.find()) {
                String attrs = m.group(1);
                String code = m.group(2);
                String src = attr(attrs, "src");
                try {
                    if (src != null && !src.isEmpty()) {
                        if (!BrowserPolicy.isSafeScriptUrl(baseUrl, src)) {
                            lastError = "Blocked script URL";
                            continue;
                        }
                        code = fetchText(BrowserPolicy.resolveHttp(baseUrl, src),
                                BrowserPolicy.scriptLimit(), false);
                    }
                    if (code == null || code.trim().isEmpty()) continue;
                    if (code.length() > BrowserPolicy.scriptLimit()) {
                        lastError = "Script exceeded safety limit";
                        continue;
                    }
                    if (looksLikeUnboundedLoop(code)) {
                        lastError = "Blocked suspicious unbounded loop";
                        continue;
                    }
                    ctx.evaluate(code, src == null ? "<inline-script>" : src);
                    scriptsRun++;
                } catch (Throwable ex) {
                    lastError = safeError(ex);
                }
            }

            try {
                Object out = ctx.evaluate("document.body.innerHTML", "<result>");
                return out == null ? html : String.valueOf(out);
            } catch (Throwable e) {
                lastError = safeError(e);
                return html;
            }
        } catch (Throwable e) {
            lastError = safeError(e);
            return html;
        } finally {
            lastRunMs = System.currentTimeMillis() - start;
            if (ctx != null) {
                try { ctx.destroy(); } catch (Throwable ignored) {}
            }
        }
    }

    private void installHost(QuickJSContext ctx, String baseUrl) {
        JSObject console = ctx.createNewJSObject();
        JSCallFunction sink = new JSCallFunction() {
            public Object call(Object... a) { return null; }
        };
        console.setProperty("log", sink);
        console.setProperty("warn", sink);
        console.setProperty("error", sink);
        ctx.getGlobalObject().setProperty("console", console);

        ctx.getGlobalObject().setProperty("algeriumResolve", new JSCallFunction() {
            public Object call(Object... a) {
                try {
                    if (a.length == 0) return "";
                    return BrowserPolicy.resolveHttp(baseUrl, String.valueOf(a[0]));
                } catch (Exception e) {
                    lastError = "Blocked URL resolution";
                    return "";
                }
            }
        });

        ctx.getGlobalObject().setProperty("algeriumFetch", new JSCallFunction() {
            public Object call(Object... a) {
                try {
                    if (a.length == 0) return "";
                    String u = String.valueOf(a[0]);
                    URI x = new URI(u);
                    if (!BrowserPolicy.isHttpUrl(u) || BrowserPolicy.isPrivateOrLocalHost(x.getHost())) {
                        lastError = "Blocked cross-origin/private-network fetch";
                        return "";
                    }
                    return fetchText(u, BrowserPolicy.fetchLimit(), true);
                } catch (Exception e) {
                    lastError = safeError(e);
                    return "";
                }
            }
        });
    }

    private String bootstrap(String html, String baseUrl) {
        return "var window=this;"
        + "var location={href:" + quote(baseUrl) + ",origin:" + quote(origin(baseUrl))
        + ",protocol:" + quote(protocol(baseUrl)) + "};"
        + "var navigator={userAgent:'Mozilla/5.0 (Linux; Android 13) Algerium/1.0',language:'en-US',platform:'Android',onLine:true};"
        + "var __raw=" + quote(html) + ";"
        + "function __esc(s){return String(s).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/\"/g,'&quot;').replace(/'/g,'&#39;');}"
        + "function __attrs(raw){var a={},re=/([:\\w-]+)(?:\\s*=\\s*(?:\"([^\"]*)\"|'([^']*)'|([^\\s>]+)))?/g,m;while((m=re.exec(raw))){a[m[1].toLowerCase()]=m[2]??m[3]??m[4]??'';}return a;}"
        + "function __node(type,tag,text){var e={nodeType:type,tagName:(tag||'').toUpperCase(),parentNode:null,children:[],childNodes:[],attributes:{},style:{},value:'',id:'',className:'',onclick:null,_events:{}};"
        + "e.appendChild=function(x){if(!x)return x;x.parentNode=e;e.children.push(x);e.childNodes=e.children;return x;};"
        + "e.removeChild=function(x){var i=e.children.indexOf(x);if(i>=0){e.children.splice(i,1);x.parentNode=null;}return x;};"
        + "e.remove=function(){if(e.parentNode)e.parentNode.removeChild(e);};"
        + "e.setAttribute=function(k,v){k=String(k).toLowerCase();v=String(v);e.attributes[k]=v;if(k==='id')e.id=v;if(k==='class')e.className=v;if(k==='style'){v.split(';').forEach(function(p){var z=p.split(':');if(z.length>1)e.style[z[0].trim()]=z.slice(1).join(':').trim();});}};"
        + "e.getAttribute=function(k){k=String(k).toLowerCase();return e.attributes[k]===undefined?null:e.attributes[k];};"
        + "e.hasAttribute=function(k){return e.getAttribute(k)!==null;};"
        + "e.addEventListener=function(n,f){if(!e._events[n])e._events[n]=[];e._events[n].push(f);if(n==='click')e.onclick=f;};"
        + "e.dispatchEvent=function(ev){var n=ev&&ev.type,aa=e._events[n]||[];aa.forEach(function(f){try{f.call(e,ev)}catch(x){}});return true;};"
        + "e.matches=function(s){return __match(e,s);};"
        + "Object.defineProperty(e,'innerHTML',{get:function(){return e.children.map(__serialize).join('')},set:function(v){e.children=[];e.childNodes=e.children;__parseInto(e,String(v));}});"
        + "Object.defineProperty(e,'outerHTML',{get:function(){return __serialize(e)}});"
        + "Object.defineProperty(e,'textContent',{get:function(){if(e.nodeType===3)return e._text||'';return e.children.map(function(x){return x.textContent||''}).join('')},set:function(v){e.children=[];e.childNodes=e.children;e.appendChild(__text(String(v)));}});"
        + "return e;}"
        + "function __text(v){var n=__node(3,'',v);n._text=v;return n;}"
        + "function __serialize(e){if(e.nodeType===3)return __esc(e._text||'');var at=Object.keys(e.attributes).map(function(k){return ' '+k+'=\"'+__esc(e.attributes[k])+'\"'}).join('');return '<'+e.tagName.toLowerCase()+at+'>'+e.children.map(__serialize).join('')+'</'+e.tagName.toLowerCase()+'>';}"
        + "function __parseInto(root,src){var re=/<\\/?([a-zA-Z][\\w:-]*)([^>]*)>/g,last=0,stack=[root],m;while((m=re.exec(src))){var tx=src.slice(last,m.index);if(tx)stack[stack.length-1].appendChild(__text(tx));var tag=m[1].toLowerCase(),closing=m[0][1]==='/',self=/\\/\\s*>$/.test(m[0])||/^(br|hr|img|input|meta|link|source|area|base|embed|param|track|wbr)$/i.test(tag);if(closing){for(var i=stack.length-1;i>0;i--){if(stack[i].tagName.toLowerCase()===tag){stack.length=i;break;}}}else{var n=__node(1,tag,'');var aa=__attrs(m[2]);Object.keys(aa).forEach(function(k){n.setAttribute(k,aa[k])});stack[stack.length-1].appendChild(n);if(!self)stack.push(n);}last=re.lastIndex;}var tail=src.slice(last);if(tail)stack[stack.length-1].appendChild(__text(tail));}"
        + "function __all(root){var a=[];root.children.forEach(function(x){a.push(x);a=a.concat(__all(x));});return a;}"
        + "function __match(e,s){s=String(s).trim();var id=s.match(/#([\\w-]+)/),cl=s.match(/\\.([\\w-]+)/),tg=s.match(/^([a-zA-Z][\\w-]*)/),at=s.match(/\\[([\\w-]+)/);if(id&&e.id!==id[1])return false;if(cl&&(' '+e.className+' ').indexOf(' '+cl[1]+' ')<0)return false;if(tg&&e.tagName.toLowerCase()!==tg[1].toLowerCase())return false;if(at&&!e.hasAttribute(at[1]))return false;return !!(id||cl||tg||at);}"
        + "function __select(root,s){var parts=String(s).trim().split(/\\s+/),cur=[root];parts.forEach(function(sel){var next=[];cur.forEach(function(r){__all(r).forEach(function(e){if(__match(e,sel)&&next.indexOf(e)<0)next.push(e);});});cur=next;});return cur;}"
        + "var document={readyState:'complete',cookie:'',title:'',createElement:function(t){return __node(1,t,'')},createTextNode:function(t){return __text(String(t))},addEventListener:function(n,f){if(n==='DOMContentLoaded')try{f({type:n})}catch(e){}},querySelector:function(s){return __select(document.documentElement,s)[0]||null},querySelectorAll:function(s){return __select(document.documentElement,s)},getElementById:function(id){return __select(document.documentElement,'#'+id)[0]||null},getElementsByTagName:function(t){return __select(document.documentElement,t)},write:function(v){document.body.innerHTML=document.body.innerHTML+String(v)}};"
        + "document.documentElement=__node(1,'html','');document.head=__node(1,'head','');document.body=__node(1,'body','');document.documentElement.appendChild(document.head);document.documentElement.appendChild(document.body);"
        + "__parseInto(document.body,__raw.replace(/<head[\\s\\S]*?<\\/head>/i,'').replace(/<body[^>]*>/i,'').replace(/<\\/body>/i,''));"
        + "document.title=((__raw.match(/<title[^>]*>([\\s\\S]*?)<\\/title>/i)||[])[1]||'').replace(/<[^>]+>/g,'').trim();"
        + "window.document=document;"
        + "window.setTimeout=function(f){if(typeof f==='function')try{f()}catch(e){}return 1};window.setInterval=window.setTimeout;window.clearTimeout=function(){};window.clearInterval=function(){};"
        + "window.requestAnimationFrame=function(f){if(typeof f==='function')try{f(Date.now())}catch(e){}return 1};window.cancelAnimationFrame=function(){};"
        + "window.scrollTo=function(){};window.scrollX=0;window.scrollY=0;"
        + "window.fetch=function(u){var full=algeriumResolve(String(u));if(!full)return Promise.reject(new Error('Blocked URL'));return Promise.resolve({ok:true,status:200,url:full,text:function(){return Promise.resolve(algeriumFetch(full))},json:function(){var t=algeriumFetch(full);try{return Promise.resolve(JSON.parse(t))}catch(e){return Promise.reject(e)}},headers:{get:function(){return null}}});};"
        + "window.localStorage={_d:{},getItem:function(k){return Object.prototype.hasOwnProperty.call(this._d,k)?this._d[k]:null},setItem:function(k,v){this._d[k]=String(v)},removeItem:function(k){delete this._d[k]},clear:function(){this._d={}},key:function(i){return Object.keys(this._d)[i]||null}};"
        + "window.sessionStorage=window.localStorage;window.alert=function(){};window.confirm=function(){return true};window.prompt=function(){return null};"
        + "window.matchMedia=function(){return {matches:false,media:'',addListener:function(){},removeListener:function(){},addEventListener:function(){},removeEventListener:function(){}}};"
        + "window.getComputedStyle=function(e){return e?e.style:{}};"
        + "window.performance={now:function(){return Date.now()}};";
    }

    private String fetchText(String u, int max, boolean blockPrivate) throws Exception {
        if (!BrowserPolicy.isHttpUrl(u)) throw new SecurityException("Only HTTP(S) allowed");
        URI x = new URI(u);
        if (blockPrivate && BrowserPolicy.isPrivateOrLocalHost(x.getHost())) throw new SecurityException("Private network blocked");
        HttpURLConnection c = (HttpURLConnection)new URL(u).openConnection();
        c.setInstanceFollowRedirects(true);
        c.setConnectTimeout(8000);
        c.setReadTimeout(12000);
        c.setRequestProperty("User-Agent","Algerium/1.0");
        c.setRequestProperty("Accept","text/html,application/javascript,text/javascript,application/json,text/plain,*/*");
        int code = c.getResponseCode();
        InputStream in = code >= 400 ? c.getErrorStream() : c.getInputStream();
        return BrowserPolicy.readLimited(in, max);
    }

    private static boolean looksLikeUnboundedLoop(String s) {
        String x = s.replaceAll("(?s)/\\*.*?\\*/","").replaceAll("(?m)//.*$","");
        return x.matches("(?s).*while\\s*\\(\\s*true\\s*\\).*")
            || x.matches("(?s).*for\\s*\\(\\s*;\\s*;\\s*\\).*");
    }

    private static String attr(String a,String k) {
        java.util.regex.Matcher m=java.util.regex.Pattern.compile("(?i)\\b"+k+"\\s*=\\s*[\\\"']([^\\\"']+)[\\\"']").matcher(a);
        return m.find()?m.group(1):null;
    }
    private static String origin(String u) {
        try { URI x=new URI(u); return x.getScheme()+"://"+x.getAuthority(); }
        catch(Exception e){return u;}
    }
    private static String protocol(String u) {
        try { return new URI(u).getScheme()+":"; } catch(Exception e){return "https:";}
    }
    private static String quote(String s) {
        if(s==null)s="";
        return "'"+s.replace("\\","\\\\").replace("'","\\'").replace("\r","\\r").replace("\n","\\n").replace("\u2028","\\u2028").replace("\u2029","\\u2029")+"'";
    }
    private static String safeError(Throwable t) {
        String s=t==null?"unknown error":String.valueOf(t);
        return s.length()>300?s.substring(0,300):s;
    }
}
