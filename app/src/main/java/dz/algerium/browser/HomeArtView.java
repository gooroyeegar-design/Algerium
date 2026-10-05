package dz.algerium.browser;
import android.content.*;import android.graphics.*;import android.view.*;
public class HomeArtView extends View{
 Paint p=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.SUBPIXEL_TEXT_FLAG);
 int sea=Color.rgb(222,214,194),sky=Color.rgb(250,240,219),mount=Color.rgb(220,203,169),mount2=Color.rgb(204,187,153),city=Color.rgb(194,178,145),green=Color.rgb(72,94,68),dark=Color.rgb(91,78,61),water=Color.rgb(205,201,183);
 public HomeArtView(Context c){super(c);p.setStrokeCap(Paint.Cap.ROUND);setLayerType(View.LAYER_TYPE_HARDWARE,null);}
 protected void onDraw(Canvas c){super.onDraw(c);float w=getWidth(),h=getHeight();
  p.setStyle(Paint.Style.FILL);p.setColor(sky);c.drawRect(0,0,w,h,p);
  p.setColor(Color.rgb(248,226,181));c.drawCircle(w*.16f,h*.70f,h*.16f,p);
  Path waterPath=new Path();waterPath.moveTo(0,h*.63f);waterPath.lineTo(w,h*.58f);waterPath.lineTo(w,h);waterPath.lineTo(0,h);waterPath.close();p.setColor(sea);c.drawPath(waterPath,p);
  Path back=new Path();back.moveTo(0,h*.62f);back.lineTo(w*.14f,h*.47f);back.lineTo(w*.27f,h*.57f);back.lineTo(w*.40f,h*.42f);back.lineTo(w*.55f,h*.55f);back.lineTo(w*.70f,h*.45f);back.lineTo(w*.86f,h*.53f);back.lineTo(w,h*.45f);back.lineTo(w,h*.65f);back.close();p.setColor(mount);c.drawPath(back,p);
  Path front=new Path();front.moveTo(0,h*.66f);front.lineTo(w*.18f,h*.57f);front.lineTo(w*.35f,h*.63f);front.lineTo(w*.53f,h*.53f);front.lineTo(w*.68f,h*.62f);front.lineTo(w*.84f,h*.55f);front.lineTo(w,h*.61f);front.lineTo(w,h*.70f);front.lineTo(0,h*.70f);front.close();p.setColor(mount2);c.drawPath(front,p);
  p.setColor(Color.rgb(234,226,204));for(int i=0;i<27;i++){float x=w*(.12f+i*.027f),bw=w*(.018f+(i%3)*.006f),bh=h*(.035f+(i%5)*.012f);c.drawRect(x,h*.69f-bh,x+bw,h*.69f,p);}
  p.setColor(city);for(int i=0;i<12;i++){float x=w*.30f+i*w*.035f;float bh=18+(i%4)*7;c.drawRect(x,h*.65f-bh,x+w*.028f,h*.69f,p);}
  float cx=w*.72f,base=h*.69f;p.setColor(Color.rgb(170,153,122));Path mon=new Path();mon.moveTo(cx-46,base);mon.lineTo(cx-12,base-112);mon.lineTo(cx,base-190);mon.lineTo(cx+12,base-112);mon.lineTo(cx+46,base);mon.close();c.drawPath(mon,p);
  p.setColor(Color.rgb(247,238,217));Path inner=new Path();inner.moveTo(cx-7,base-8);inner.lineTo(cx,base-177);inner.lineTo(cx+7,base-8);inner.close();c.drawPath(inner,p);c.drawRect(cx-2,base-185,cx+2,base-20,p);
  p.setColor(dark);p.setStrokeWidth(5);float px=w*.90f;c.drawLine(px,base,px,base-116,p);for(int i=0;i<9;i++){double ang=-Math.PI*.93+i*Math.PI*.86/8;c.drawLine(px,base-112,px+(float)Math.cos(ang)*54,base-112+(float)Math.sin(ang)*34,p);}
  float px2=w*.79f;c.drawLine(px2,base,px2,base-72,p);p.setStrokeWidth(3);for(int i=0;i<7;i++){double ang=-Math.PI*.95+i*Math.PI*.9/6;c.drawLine(px2,base-70,px2+(float)Math.cos(ang)*30,base-70+(float)Math.sin(ang)*20,p);}
  p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2);for(int i=0;i<4;i++){float x=w*(.50f+i*.065f),y=h*(.28f+(i%2)*.04f);Path b=new Path();b.moveTo(x-10,y);b.quadTo(x-5,y-7,x,y);b.quadTo(x+5,y-7,x+10,y);c.drawPath(b,p);}
  p.setStyle(Paint.Style.FILL);p.setColor(water);for(int i=0;i<8;i++){float y=h*(.75f+i*.025f);float len=w*(.14f+(i%3)*.07f);c.drawRect(w*.10f+i*w*.012f,y,w*.10f+i*w*.012f+len,y+2,p);}
 }
}