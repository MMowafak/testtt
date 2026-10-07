package com.ironvale.game;

import android.graphics.*;
import android.graphics.drawable.*;
import java.util.*;

class GameSprites {
  static Bitmap ship, chicken0, chicken1, chicken2, chicken3, boss, power, shield, nova, coin;

  static void build(){
    if(ship!=null)return;
    ship=makeShip(256);
    chicken0=makeChicken(256,0,false);
    chicken1=makeChicken(256,1,false);
    chicken2=makeChicken(256,2,false);
    chicken3=makeChicken(256,3,false);
    boss=makeChicken(360,3,true);
    power=makePower(128,0);
    shield=makePower(128,1);
    nova=makePower(128,2);
    coin=makePower(128,3);
  }

  static Bitmap makeChicken(int n,int variant,boolean isBoss){
    Bitmap b=Bitmap.createBitmap(n,n,Bitmap.Config.ARGB_8888);
    Canvas c=new Canvas(b);
    Paint p=new Paint(3);
    float s=n/256f;
    c.scale(s,s);
    // soft glow
    p.setShader(new RadialGradient(128,135,95,
      isBoss?0x60ff8a3a:0x30fff2b0,0x00000000,Shader.TileMode.CLAMP));
    c.drawCircle(128,135,95,p);p.setShader(null);

    int outline=Color.rgb(66,52,40);
    int body=isBoss?Color.rgb(250,219,168):(variant==1?Color.rgb(236,226,205):Color.rgb(246,235,194));
    int wing=variant==2?Color.rgb(211,228,244):Color.rgb(232,224,199);

    p.setStyle(Paint.Style.FILL);p.setColor(wing);
    c.drawOval(new RectF(18,91,93,176),p);c.drawOval(new RectF(163,88,241,175),p);
    p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(7);p.setColor(outline);
    c.drawOval(new RectF(18,91,93,176),p);c.drawOval(new RectF(163,88,241,175),p);

    p.setStyle(Paint.Style.FILL);
    p.setShader(new RadialGradient(135,120,90,Color.rgb(255,249,222),body,Shader.TileMode.CLAMP));
    c.drawOval(new RectF(57,65,202,220),p);p.setShader(null);
    p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(8);p.setColor(outline);c.drawOval(new RectF(57,65,202,220),p);

    p.setStyle(Paint.Style.FILL);p.setShader(new RadialGradient(156,73,60,Color.WHITE,Color.rgb(246,239,216),Shader.TileMode.CLAMP));
    c.drawOval(new RectF(101,29,207,130),p);p.setShader(null);
    p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(7);p.setColor(outline);c.drawOval(new RectF(101,29,207,130),p);

    // comb and wattles
    p.setStyle(Paint.Style.FILL);p.setColor(Color.rgb(213,52,47));
    c.drawCircle(120,32,16,p);c.drawCircle(145,24,18,p);c.drawCircle(170,31,15,p);
    c.drawOval(new RectF(150,111,170,146),p);c.drawOval(new RectF(170,109,190,143),p);

    // eyes
    p.setColor(Color.WHITE);
    c.drawOval(new RectF(129,57,157,88),p);c.drawOval(new RectF(161,55,190,87),p);
    p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(4);p.setColor(outline);
    c.drawOval(new RectF(129,57,157,88),p);c.drawOval(new RectF(161,55,190,87),p);
    p.setStyle(Paint.Style.FILL);p.setColor(Color.rgb(25,29,35));c.drawCircle(150,72,5,p);c.drawCircle(181,70,5,p);
    p.setColor(Color.WHITE);c.drawCircle(151,70,1.5f,p);c.drawCircle(182,68,1.5f,p);

    // beak
    Path beak=new Path();beak.moveTo(190,72);beak.lineTo(237,91);beak.lineTo(191,108);beak.close();
    p.setColor(Color.rgb(246,153,44));c.drawPath(beak,p);
    p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(5);p.setColor(outline);c.drawPath(beak,p);
    p.setStrokeWidth(3);c.drawLine(193,91,228,91,p);

    // legs and feet
    p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(7);p.setStrokeCap(Paint.Cap.ROUND);p.setColor(Color.rgb(234,137,35));
    for(float xx:new float[]{98,161}){
      c.drawLine(xx,204,xx,235,p);c.drawLine(xx,233,xx-18,246,p);c.drawLine(xx,233,xx+18,246,p);
    }

    if(variant==1){
      // aviator goggles
      p.setStyle(Paint.Style.FILL);p.setColor(0x994a9bd0);
      c.drawOval(new RectF(119,50,160,92),p);c.drawOval(new RectF(160,48,201,90),p);
      p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(5);p.setColor(Color.rgb(48,77,101));
      c.drawOval(new RectF(119,50,160,92),p);c.drawOval(new RectF(160,48,201,90),p);c.drawLine(158,70,164,70,p);
    }else if(variant==2){
      // helmet
      p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(12);p.setColor(Color.rgb(63,121,192));
      c.drawArc(new RectF(94,15,213,124),205,132,false,p);
      p.setStyle(Paint.Style.FILL);p.setColor(Color.rgb(69,132,204));c.drawRoundRect(new RectF(103,31,207,53),8,8,p);
    }

    if(variant==3||isBoss){
      // chest armor
      p.setStyle(Paint.Style.FILL);p.setColor(Color.rgb(74,80,100));
      c.drawRoundRect(new RectF(72,121,197,206),22,22,p);
      p.setColor(Color.rgb(112,122,149));c.drawRect(123,131,149,200,p);
      p.setColor(Color.rgb(221,163,48));c.drawCircle(96,150,13,p);
      if(isBoss){
        p.setColor(Color.rgb(78,85,108));
        c.drawRoundRect(new RectF(25,111,78,140),10,10,p);
        c.drawRoundRect(new RectF(191,111,244,140),10,10,p);
        p.setColor(Color.rgb(123,134,160));c.drawRect(8,119,38,132,p);c.drawRect(231,119,256,132,p);
      }
    }
    return b;
  }

  static Bitmap makeShip(int n){
    Bitmap b=Bitmap.createBitmap(n,n,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(b);Paint p=new Paint(3);
    float s=n/256f;c.scale(s,s);
    p.setShader(new RadialGradient(128,190,85,0x7057dfff,0x00000000,Shader.TileMode.CLAMP));c.drawCircle(128,188,85,p);p.setShader(null);
    int outline=Color.rgb(27,36,56);
    Path left=new Path();left.moveTo(128,32);left.lineTo(35,190);left.lineTo(94,177);left.lineTo(128,137);left.close();
    Path right=new Path();right.moveTo(128,32);right.lineTo(221,190);right.lineTo(162,177);right.lineTo(128,137);right.close();
    p.setStyle(Paint.Style.FILL);p.setColor(Color.rgb(73,128,195));c.drawPath(left,p);c.drawPath(right,p);
    p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(6);p.setColor(outline);c.drawPath(left,p);c.drawPath(right,p);

    Path core=new Path();core.moveTo(128,20);core.lineTo(94,198);core.lineTo(128,231);core.lineTo(162,198);core.close();
    p.setStyle(Paint.Style.FILL);p.setShader(new LinearGradient(100,40,160,210,Color.WHITE,Color.rgb(148,166,193),Shader.TileMode.CLAMP));c.drawPath(core,p);p.setShader(null);
    p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(6);p.setColor(outline);c.drawPath(core,p);

    Path stripe=new Path();stripe.moveTo(128,36);stripe.lineTo(119,194);stripe.lineTo(128,216);stripe.lineTo(137,194);stripe.close();
    p.setStyle(Paint.Style.FILL);p.setColor(Color.rgb(72,202,234));c.drawPath(stripe,p);

    p.setColor(Color.rgb(42,82,135));c.drawOval(new RectF(108,74,148,131),p);
    p.setShader(new LinearGradient(115,78,142,125,Color.rgb(116,220,250),Color.rgb(43,100,168),Shader.TileMode.CLAMP));c.drawOval(new RectF(113,79,143,125),p);p.setShader(null);

    for(float xx:new float[]{82,174}){
      p.setColor(Color.rgb(119,137,164));c.drawRoundRect(new RectF(xx-10,137,xx+10,207),8,8,p);
      p.setColor(Color.rgb(213,224,237));c.drawRoundRect(new RectF(xx-4,110,xx+4,150),4,4,p);
    }

    for(float xx:new float[]{108,128,148}){
      p.setColor(Color.rgb(45,58,84));c.drawOval(new RectF(xx-9,193,xx+9,232),p);
      Path flame=new Path();flame.moveTo(xx-6,225);flame.lineTo(xx,256);flame.lineTo(xx+6,225);flame.close();
      p.setColor(Color.rgb(55,205,255));c.drawPath(flame,p);
      Path hot=new Path();hot.moveTo(xx-3,225);hot.lineTo(xx,246);hot.lineTo(xx+3,225);hot.close();p.setColor(Color.rgb(255,220,95));c.drawPath(hot,p);
    }
    return b;
  }

  static Bitmap makePower(int n,int kind){
    Bitmap b=Bitmap.createBitmap(n,n,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(b);Paint p=new Paint(3);
    float s=n/128f;c.scale(s,s);
    int col=kind==0?Color.rgb(255,208,55):kind==1?Color.rgb(65,214,243):kind==2?Color.rgb(245,120,60):Color.rgb(255,198,47);
    p.setShader(new RadialGradient(64,64,55,0x80ffffff,0x00000000,Shader.TileMode.CLAMP));c.drawCircle(64,64,55,p);p.setShader(null);
    p.setColor(Color.rgb(24,32,56));c.drawCircle(64,64,43,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(6);p.setColor(col);c.drawCircle(64,64,43,p);p.setStyle(Paint.Style.FILL);
    if(kind==0){
      Path st=new Path();for(int i=0;i<10;i++){double a=-Math.PI/2+i*Math.PI/5;float rr=i%2==0?29:12;float x=64+(float)Math.cos(a)*rr,y=64+(float)Math.sin(a)*rr;if(i==0)st.moveTo(x,y);else st.lineTo(x,y);}st.close();p.setColor(col);c.drawPath(st,p);p.setColor(Color.WHITE);c.drawCircle(64,64,8,p);
    }else if(kind==1){
      Path sh=new Path();sh.moveTo(64,31);sh.lineTo(92,42);sh.lineTo(86,79);sh.lineTo(64,100);sh.lineTo(42,79);sh.lineTo(36,42);sh.close();p.setColor(col);c.drawPath(sh,p);p.setColor(Color.rgb(40,93,125));c.drawCircle(64,61,15,p);
    }else if(kind==2){
      for(int i=0;i<8;i++){double a=i*Math.PI/4;c.save();c.rotate(i*45,64,64);p.setColor(col);c.drawRoundRect(new RectF(60,18,68,48),4,4,p);c.restore();}
      p.setColor(Color.WHITE);c.drawCircle(64,64,14,p);
    }else{
      p.setColor(col);c.drawCircle(64,64,28,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(5);p.setColor(Color.rgb(153,96,12));c.drawCircle(64,64,28,p);p.setStyle(Paint.Style.FILL);
      p.setColor(Color.rgb(255,231,129));c.drawCircle(64,64,19,p);
    }
    return b;
  }

  static Bitmap chicken(int type){
    if(type==0)return chicken0;if(type==1)return chicken1;if(type==2)return chicken2;return chicken3;
  }
  static Bitmap power(int type){return type==0?power:type==1?shield:type==2?nova:coin;}
}
