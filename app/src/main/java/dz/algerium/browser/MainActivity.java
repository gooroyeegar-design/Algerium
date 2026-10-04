package dz.algerium.browser;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.view.*;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity{
 EditText address,homeSearch; LinearLayout homeLayer; PageView page; TextView title;
 ArrayList<String> history=new ArrayList<>(),bookmarks=new ArrayList<>();
 String current="about:home";
 int beige=Color.rgb(243,232,210), cream=Color.rgb(255,249,238), green=Color.rgb(49,88,58), brown=Color.rgb(107,89,69);
 android.content.SharedPreferences prefs;

 @Override public void onCreate(Bundle b){
  super.onCreate(b);
  getWindow().setStatusBarColor(beige);getWindow().setNavigationBarColor(beige);
  getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
  prefs=getSharedPreferences("algerium_private",MODE_PRIVATE);JsEngine.get().init(this);loadLists();build();home();
 }

 TextView tv(String s,float z){TextView x=new TextView(this);x.setText(s);x.setTextSize(z);x.setTextColor(brown);x.setGravity(Gravity.CENTER);return x;}
 Button b(String s){Button x=new Button(this);x.setText(s);x.setTextSize(20);x.setTextColor(brown);x.setAllCaps(false);x.setMinWidth(0);x.setMinHeight(0);x.setPadding(0,0,0,0);x.setBackgroundColor(Color.TRANSPARENT);return x;}
 LinearLayout.LayoutParams p(int w,int h){return new LinearLayout.LayoutParams(w,h);}
 GradientDrawable bg(int color,float r){GradientDrawable g=new GradientDrawable();g.setColor(color);g.setCornerRadius(r);g.setStroke(1,Color.rgb(229,215,190));return g;}

 void build(){
  LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(beige);
  LinearLayout tabs=new LinearLayout(this);tabs.setGravity(Gravity.CENTER_VERTICAL);tabs.setPadding(12,5,8,0);
  title=tv("Algerium",17);title.setTextColor(green);title.setGravity(Gravity.CENTER_VERTICAL);title.setPadding(14,0,0,0);title.setBackground(bg(Color.rgb(250,242,227),26));
  tabs.addView(title,new LinearLayout.LayoutParams(0,52,1));Button plus=b("+");plus.setTextSize(32);tabs.addView(plus,p(58,52));root.addView(tabs);

  LinearLayout bar=new LinearLayout(this);bar.setGravity(Gravity.CENTER_VERTICAL);bar.setPadding(10,4,10,5);
  Button back=b("‹"),fwd=b("›"),reload=b("↻");bar.addView(back,p(42,46));bar.addView(fwd,p(42,46));bar.addView(reload,p(42,46));
  address=new EditText(this);address.setSingleLine();address.setTextSize(16);address.setTextColor(brown);address.setHintTextColor(Color.rgb(150,137,116));address.setHint("Search or enter address…");address.setPadding(18,0,18,0);address.setBackgroundResource(R.drawable.rounded_search);address.setImeOptions(EditorInfo.IME_ACTION_GO);
  bar.addView(address,new LinearLayout.LayoutParams(0,46,1));Button menu=b("⋮");menu.setTextSize(25);bar.addView(menu,p(42,46));root.addView(bar);

  FrameLayout content=new FrameLayout(this);
  page=new PageView(this);page.setNavigationListener(u->navigate(u));content.addView(page,new FrameLayout.LayoutParams(-1,-1));

  homeLayer=new LinearLayout(this);homeLayer.setOrientation(LinearLayout.VERTICAL);homeLayer.setGravity(Gravity.CENTER_HORIZONTAL);homeLayer.setPadding(30,30,30,28);homeLayer.setBackgroundColor(Color.TRANSPARENT);
  ImageView logo=new ImageView(this);logo.setImageResource(R.drawable.ic_algerium);homeLayer.addView(logo,new LinearLayout.LayoutParams(126,126));
  TextView brand=tv("Algerium",34);brand.setTextColor(green);brand.setTypeface(android.graphics.Typeface.create("sans",android.graphics.Typeface.NORMAL));brand.setLetterSpacing(.01f);
  homeLayer.addView(brand,new LinearLayout.LayoutParams(-2,58));
  TextView tagline=tv("a browser for Algeria",17);tagline.setTextColor(brown);tagline.setLetterSpacing(.18f);homeLayer.addView(tagline,new LinearLayout.LayoutParams(-2,42));
  homeSearch=new EditText(this);homeSearch.setSingleLine(true);homeSearch.setTextSize(18);homeSearch.setTextColor(brown);homeSearch.setHintTextColor(Color.rgb(150,137,116));homeSearch.setHint("Search or enter address…");homeSearch.setPadding(22,0,22,0);homeSearch.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_search,0,0,0);homeSearch.setCompoundDrawablePadding(14);homeSearch.setBackgroundResource(R.drawable.rounded_search);
  LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-1,62);sp.topMargin=36;homeLayer.addView(homeSearch,sp);
  HomeArtView art=new HomeArtView(this);LinearLayout.LayoutParams ap=new LinearLayout.LayoutParams(-1,300);ap.topMargin=18;homeLayer.addView(art,ap);
  FrameLayout.LayoutParams hp=new FrameLayout.LayoutParams(-1,-2,Gravity.TOP);hp.topMargin=115;content.addView(homeLayer,hp);

  LinearLayout nav=new LinearLayout(this);nav.setGravity(Gravity.CENTER);nav.setPadding(8,3,8,4);
  String[] labs={"⌂\nHome","☆\nBookmarks","◷\nHistory","☰\nMenu"};
  for(String s:labs){TextView n=tv(s,13);n.setTextColor(brown);n.setLineSpacing(0,.85f);nav.addView(n,new LinearLayout.LayoutParams(0,68,1));}
  FrameLayout.LayoutParams np=new FrameLayout.LayoutParams(-1,72,Gravity.BOTTOM);content.addView(nav,np);
  root.addView(content,new LinearLayout.LayoutParams(-1,0,1));setContentView(root);

  address.setOnEditorActionListener((v,a,e)->{navigate(address.getText().toString());return true;});
  homeSearch.setOnEditorActionListener((v,a,e)->{navigate(homeSearch.getText().toString());return true;});
  reload.setOnClickListener(v->navigate(current,false));
  back.setOnClickListener(v->{if(page.canBack()){current=page.backUrl();navigate(current,false);}else home();});
  fwd.setOnClickListener(v->{if(page.canForward()){current=page.forwardUrl();navigate(current,false);}});
  plus.setOnClickListener(v->home());menu.setOnClickListener(v->popup(menu));
  nav.getChildAt(0).setOnClickListener(v->home());nav.getChildAt(1).setOnClickListener(v->showList("Bookmarks",bookmarks));
  nav.getChildAt(2).setOnClickListener(v->showList("History",history));nav.getChildAt(3).setOnClickListener(v->popup(menu));
 }

 void showAdmin(){
  String s="Algerium security diagnostics\n\nJS runtime: QuickJS\nScripts executed: "+JsEngine.get().getScriptsRun()
   +"\nLast JS error: "+JsEngine.get().getLastError()+"\nLast JS run: "+JsEngine.get().getLastRunMs()+" ms"
   +"\n\nRenderer: independent Canvas\nChromium/WebView: not used"
   +"\nNetwork: HTTPS-only app traffic; private-network JS fetch blocked"
   +"\nPage/script size limits: enabled"
   +"\n\nThis panel is local. Browsing data is not uploaded.";
  new AlertDialog.Builder(this).setTitle("Algerium diagnostics").setMessage(s).setPositiveButton("Close",null).show();
 }

 void popup(View anchor){
  PopupMenu m=new PopupMenu(this,anchor);
  m.getMenu().add("New tab");m.getMenu().add("Bookmarks");m.getMenu().add("History");m.getMenu().add("Find in page");
  m.getMenu().add("Share page");m.getMenu().add("Settings");m.getMenu().add("Engine diagnostics");
  m.setOnMenuItemClickListener(i->{String s=i.getTitle().toString();
   if(s.equals("New tab"))home();else if(s.equals("Bookmarks"))showList("Bookmarks",bookmarks);else if(s.equals("History"))showList("History",history);
   else if(s.equals("Find in page"))find();else if(s.equals("Share page")){Intent x=new Intent(Intent.ACTION_SEND);x.setType("text/plain");x.putExtra(Intent.EXTRA_TEXT,current);startActivity(Intent.createChooser(x,"Share page"));}
   else if(s.equals("Engine diagnostics"))showAdmin();else settings();return true;});m.show();
 }

 void find(){final EditText e=new EditText(this);e.setHint("Find text");new AlertDialog.Builder(this).setTitle("Find in page").setView(e).setPositiveButton("Find",(d,w)->page.find(e.getText().toString())).setNegativeButton("Cancel",null).show();}

 void settings(){
  new AlertDialog.Builder(this).setTitle("Algerium settings")
   .setItems(new String[]{"Search engine: Google","JavaScript: experimental","Block private-network JS fetch: on","Clear browsing data"},(d,w)->{
    if(w==3){history.clear();bookmarks.clear();prefs.edit().clear().apply();Toast.makeText(this,"Browsing data cleared",Toast.LENGTH_SHORT).show();}
   }).show();
 }

 void showList(String t,ArrayList<String> a){
  new AlertDialog.Builder(this).setTitle(t).setItems(a.toArray(new String[0]),(d,w)->navigate(a.get(w))).setPositiveButton("Close",null).show();
 }

 void home(){current="about:home";address.setText("");homeSearch.setText("");title.setText("Algerium");homeLayer.setVisibility(View.VISIBLE);page.home();}

 void navigate(String raw){navigate(raw,true);}
 void navigate(String raw,boolean add){
  String u=BrowserPolicy.normalize(raw);
  if(u.equals("algerium://admin")){showAdmin();return;}
  if(!u.equals("about:home")&&!BrowserPolicy.isHttpUrl(u)){Toast.makeText(this,"Blocked unsafe URL",Toast.LENGTH_SHORT).show();return;}
  current=u;address.setText(u);title.setText(u.equals("about:home")?"Algerium":u);homeLayer.setVisibility(View.GONE);
  if(add&&!u.equals("about:home")){if(history.size()>200)history.remove(history.size()-1);history.add(0,u);saveLists();}
  page.load(u);
 }

 void loadLists(){
  String h=prefs==null?null:prefs.getString("history","");
  if(h!=null&&!h.isEmpty())for(String s:h.split("\\n"))if(BrowserPolicy.isHttpUrl(s))history.add(s);
  String b=prefs==null?null:prefs.getString("bookmarks","");
  if(b!=null&&!b.isEmpty())for(String s:b.split("\\n"))if(BrowserPolicy.isHttpUrl(s))bookmarks.add(s);
 }
 void saveLists(){
  StringBuilder h=new StringBuilder();for(String s:history){if(h.length()>0)h.append('\n');h.append(s.replace("\\n",""));}
  StringBuilder b=new StringBuilder();for(String s:bookmarks){if(b.length()>0)b.append('\n');b.append(s.replace("\\n",""));}
  prefs.edit().putString("history",h.toString()).putString("bookmarks",b.toString()).apply();
 }
 @Override protected void onPause(){super.onPause();saveLists();}
}
