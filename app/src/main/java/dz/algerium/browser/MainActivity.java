package dz.algerium.browser;

import android.app.*;import android.os.*;import android.graphics.Color;import android.view.*;import android.view.inputmethod.EditorInfo;import android.widget.*;import java.net.*;import java.io.*;

public class MainActivity extends Activity {
 EditText address; PageView page; TextView status; String current="";
 @Override public void onCreate(Bundle b){super.onCreate(b); build(); load("https://www.google.com");}
 void build(){
  LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(Color.WHITE);
  LinearLayout bar=new LinearLayout(this); bar.setPadding(10,8,10,8); bar.setGravity(Gravity.CENTER_VERTICAL);
  Button back=btn("‹"), forward=btn("›"), reload=btn("↻"); address=new EditText(this); address.setSingleLine(true); address.setHint("Search or enter address"); address.setImeOptions(EditorInfo.IME_ACTION_GO); address.setInputType(33);
  bar.addView(back,sz(42));bar.addView(forward,sz(42));bar.addView(reload,sz(42));bar.addView(address,new LinearLayout.LayoutParams(0,50,1));
  root.addView(bar); status=new TextView(this); status.setText("Algerium");status.setTextSize(12);status.setTextColor(Color.DKGRAY);status.setPadding(14,2,14,2);root.addView(status,new LinearLayout.LayoutParams(-1,30));
  page=new PageView(this);root.addView(page,new LinearLayout.LayoutParams(-1,0,1));setContentView(root);
  address.setOnEditorActionListener((v,a,e)->{load(normalize(address.getText().toString()));return true;});
  reload.setOnClickListener(v->load(current)); back.setOnClickListener(v->{if(page.canBack())page.back();}); forward.setOnClickListener(v->{if(page.canForward())page.forward();});
 }
 Button btn(String s){Button b=new Button(this);b.setText(s);b.setTextSize(22);b.setPadding(0,0,0,0);return b;} LinearLayout.LayoutParams sz(int w){return new LinearLayout.LayoutParams(w,50);}
 String normalize(String s){s=s.trim();if(s.isEmpty())return "https://www.google.com";if(!s.matches("(?i)^[a-z][a-z0-9+.-]*://.*")){if(s.contains(" "))return "https://www.google.com/search?q="+URLEncoder(s);s="https://"+s;}return s;}
 String URLEncoder(String s){try{return java.net.URLEncoder.encode(s,"UTF-8");}catch(Exception e){return s;}}
 void load(String url){current=url;address.setText(url);status.setText("Loading…");new Thread(()->{try{HttpURLConnection c=(HttpURLConnection)new URL(url).openConnection();c.setConnectTimeout(12000);c.setReadTimeout(20000);c.setRequestProperty("User-Agent","Algerium/0.1");int code=c.getResponseCode();InputStream in=code>=400?c.getErrorStream():c.getInputStream();String html=read(in);String base=c.getURL().toString();runOnUiThread(()->{status.setText(code+"  "+base);page.showDocument(base,html);});}catch(Exception e){runOnUiThread(()->{status.setText("Could not load page");page.showError(current,e.getMessage());});}}).start();}
 String read(InputStream in)throws Exception{if(in==null)return "";BufferedReader r=new BufferedReader(new InputStreamReader(in,"UTF-8"));StringBuilder s=new StringBuilder();String x;while((x=r.readLine())!=null)s.append(x).append('\n');return s.toString();}
}
