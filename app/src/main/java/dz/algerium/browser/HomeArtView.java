package dz.algerium.browser;
import android.content.*;import android.graphics.*;import android.view.*;
public class HomeArtView extends View{
 Paint p=new Paint(3); int beige=Color.rgb(243,232,210),sand=Color.rgb(218,195,157),sand2=Color.rgb(232,214,181),green=Color.rgb(73,91,65),dark=Color.rgb(91,78,61);
 public HomeArtView(Context c){super(c);p.setStrokeCap(Paint.Cap.ROUND);}
 protected void onDraw(Canvas c){super.onDraw(c);float w=getWidth(),h=getHeight();
  p.setStyle(Paint.Style.FILL);p.setColor(Color.rgb(250,239,215));c.drawCircle(w*.18f,h*.68f,h*.13f,p);
  Path sea=new Path();sea.moveTo(0,h*.70f);sea.lineTo(w,h*.66f);sea.lineTo(w,h);sea.lineTo(0,h);sea.close();p.setColor(Color.rgb(226,215,191));c.drawPath(sea,p);
  Path m=new Path();m.moveTo(0,h*.69f);m.lineTo(w*.17f,h*.55f);m.lineTo(w*.31f,h*.64f);m.lineTo(w*.47f,h*.48f);m.lineTo(w*.63f,h*.62f);m.lineTo(w*.82f,h*.50f);m.lineTo(w,h*.63f);m.lineTo(w,h*.72f);m.lineTo(0,h*.72f);m.close();p.setColor(sand2);c.drawPath(m,p);
  // Casbah-like shoreline blocks
  p.setColor(Color.rgb(198,181,150));for(int i=0;i<15;i++){float x=w*.28f+i*w*.035f;float bh=12+(i%4)*5;c.drawRect(x,h*.64f-bh,x+w*.028f,h*.67f,p);}
  // monument
  float cx=w*.73f, base=h*.66f; p.setColor(Color.rgb(181,164,133));Path mon=new Path();mon.moveTo(cx-42,base);mon.lineTo(cx-9,base-110);mon.lineTo(cx,base-165);mon.lineTo(cx+9,base-110);mon.lineTo(cx+42,base);mon.close();c.drawPath(mon,p);
  p.setColor(Color.rgb(244,234,211));c.drawRect(cx-4,base-160,cx+4,base-18,p);p.setStrokeWidth(3);c.drawLine(cx,base-158,cx,base-181,p);
  // palm
  p.setColor(dark);p.setStrokeWidth(5);c.drawLine(w*.91f,base,w*.91f,base-100,p);p.setStrokeWidth(4);for(int i=0;i<8;i++){double a=-Math.PI*.9+i*Math.PI*.8/7;c.drawLine(w*.91f,base-98,w*.91f+(float)Math.cos(a)*55,base-98+(float)Math.sin(a)*35,p);}
  // birds
  p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2);for(int i=0;i<3;i++){float x=w*(.55f+i*.07f),y=h*(.28f+i*.025f);Path b=new Path();b.moveTo(x-9,y);b.quadTo(x-4,y-6,x,y);b.quadTo(x+5,y-6,x+10,y);c.drawPath(b,p);}p.setStyle(Paint.Style.FILL);
  // water lines
  p.setColor(Color.rgb(196,187,166));for(int i=0;i<5;i++)c.drawRect(w*.12f+i*7,h*.77f+i*18,w*.38f+i*9,h*.775f+i*18,p);
 }
}