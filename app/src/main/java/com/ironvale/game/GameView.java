package com.ironvale.game;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.DisplayCutout;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Locale;

class GameView extends View {
  final Paint p=new Paint(3);
  final Handler handler=new Handler(Looper.getMainLooper());
  final GameState s;

  int W,H,insetTop=0,insetBottom=0,screen=0,mapX=0,mapY=0,reportOffset=0;
  boolean menuScreen=true;
  float downX,downY;

  final int BG=Color.rgb(237,231,214), PANEL=Color.rgb(249,246,237), PANEL2=Color.rgb(228,219,199);
  final int BORDER=Color.rgb(190,178,149), TEXT=Color.rgb(57,54,46), MUTED=Color.rgb(116,108,91);
  final int TOP=Color.rgb(42,58,45), TOP2=Color.rgb(55,75,57), GREEN=Color.rgb(91,137,64);
  final int GREEN2=Color.rgb(121,158,83), GOLD=Color.rgb(185,139,51), RED=Color.rgb(178,73,61);
  final int CLAY=Color.rgb(159,91,62), IRON=Color.rgb(96,111,116), CROP=Color.rgb(188,157,70);
  final int BLUE=Color.rgb(75,118,145), WHITE=Color.rgb(249,247,239);

  final String[] nav={"FIELDS","VILLAGE","MAP","REPORTS","HERO"};
  final String[] buildingKeys={"main","warehouse","granary","rally","barracks","academy","smithy","stable","residence","mansion","market","wall"};
  final float[] buildingX={.50f,.28f,.72f,.50f,.20f,.80f,.34f,.66f,.19f,.81f,.50f,.50f};
  final float[] buildingY={.44f,.31f,.31f,.62f,.49f,.49f,.66f,.66f,.78f,.78f,.83f,.15f};

  final Runnable loop=new Runnable(){
    public void run(){
      if(!menuScreen)s.tick(System.currentTimeMillis());
      invalidate();
      handler.postDelayed(this,250);
    }
  };

  GameView(Context c){
    super(c);
    s=new GameState(c);
    if(s.started&&!s.paused){
      s.tick(System.currentTimeMillis());
      s.paused=true;
      s.save();
    }
    if(Build.VERSION.SDK_INT>=20){
      setOnApplyWindowInsetsListener((v,insets)->{
        insetTop=Math.max(0,insets.getSystemWindowInsetTop());
        insetBottom=Math.max(0,insets.getSystemWindowInsetBottom());
        if(Build.VERSION.SDK_INT>=28){
          DisplayCutout cut=insets.getDisplayCutout();
          if(cut!=null){
            insetTop=Math.max(insetTop,cut.getSafeInsetTop());
            insetBottom=Math.max(insetBottom,cut.getSafeInsetBottom());
          }
        }
        invalidate();
        return insets;
      });
      requestApplyInsets();
    }
    setFocusable(true);
    handler.post(loop);
  }

  void dispose(){handler.removeCallbacks(loop);s.save();}
  float d(float v){return v*getResources().getDisplayMetrics().density;}
  float safeTop(){return insetTop;}
  float safeBottom(){return H-insetBottom;}
  float hudTop(){return safeTop();}
  float stockTop(){return hudTop()+d(38);}
  float navTop(){return stockTop()+d(45);}
  float contentTop(){return navTop()+d(48);}
  float contentBottom(){return safeBottom()-d(8);}

  void txt(Canvas c,String str,float x,float y,float size,int color,boolean bold){
    p.setStyle(Paint.Style.FILL);p.setColor(color);p.setTextSize(d(size));
    p.setTypeface(Typeface.create("sans",bold?Typeface.BOLD:Typeface.NORMAL));c.drawText(str,x,y,p);
  }

  void box(Canvas c,float l,float t,float r,float b,float rad,int color){
    p.setStyle(Paint.Style.FILL);p.setColor(color);c.drawRoundRect(l,t,r,b,d(rad),d(rad),p);
  }

  void outline(Canvas c,float l,float t,float r,float b,float rad,float width,int color){
    p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(d(width));p.setColor(color);
    c.drawRoundRect(l,t,r,b,d(rad),d(rad),p);p.setStyle(Paint.Style.FILL);
  }

  void line(Canvas c,float x1,float y1,float x2,float y2,float width,int color){
    p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(d(width));p.setStrokeCap(Paint.Cap.ROUND);p.setColor(color);
    c.drawLine(x1,y1,x2,y2,p);p.setStyle(Paint.Style.FILL);
  }

  @Override protected void onDraw(Canvas c){
    super.onDraw(c);W=getWidth();H=getHeight();c.drawColor(BG);
    if(menuScreen){drawMenu(c);return;}
    drawHud(c);
    if(screen==0)drawFields(c);
    else if(screen==1)drawVillage(c);
    else if(screen==2)drawWorld(c);
    else if(screen==3)drawReports(c);
    else drawHero(c);
    if(s.paused)drawPause(c);
  }

  void drawMenu(Canvas c){
    float top=safeTop()+d(18),bottom=safeBottom()-d(18),mid=(top+bottom)/2f;
    drawSword(c,W/2f,top+d(70),d(42),GOLD);
    txt(c,"IRONVALE",W/2f-d(66),top+d(145),26,TOP,true);
    txt(c,"WARFRONT",W/2f-d(47),top+d(169),12,GOLD,true);
    txt(c,"Local x20 strategy realm",W/2f-d(78),top+d(194),10,MUTED,false);

    float y=mid-d(95);
    box(c,d(18),y,W-d(18),y+d(118),15,PANEL);
    outline(c,d(18),y,W-d(18),y+d(118),15,1,BORDER);
    txt(c,"OFFLINE REALM",d(34),y+d(27),9,GREEN,true);
    txt(c,"18 resource fields • village buildings • world map",d(34),y+d(51),9,TEXT,true);
    txt(c,"Rival warlords expand, raid and attack while running.",d(34),y+d(73),8,MUTED,false);
    txt(c,"Pause freezes the realm. Saves stay on this phone.",d(34),y+d(94),8,MUTED,false);

    y+=d(139);
    box(c,d(28),y,W-d(28),y+d(52),11,GREEN);
    txt(c,s.started?"CONTINUE REALM":"START NEW REALM",d(48),y+d(33),11,WHITE,true);
    if(s.started){
      y+=d(64);box(c,d(28),y,W-d(28),y+d(45),10,PANEL2);outline(c,d(28),y,W-d(28),y+d(45),10,1,BORDER);
      txt(c,"START FRESH REALM",d(48),y+d(29),9,TEXT,true);
      y+=d(61);txt(c,"Saved • "+s.gameClock(),d(35),y,8,MUTED,false);
    }
    txt(c,"Original Ironvale art and ruleset • no login required",d(24),bottom-d(5),7,MUTED,false);
  }

  void drawSword(Canvas c,float cx,float cy,float size,int col){
    line(c,cx-size*.38f,cy+size*.38f,cx+size*.27f,cy-size*.27f,6,col);
    line(c,cx-size*.12f,cy+size*.06f,cx+size*.16f,cy+size*.34f,4,TOP);
    Path blade=new Path();blade.moveTo(cx+size*.20f,cy-size*.20f);blade.lineTo(cx+size*.46f,cy-size*.46f);
    blade.lineTo(cx+size*.34f,cy-size*.11f);blade.close();p.setColor(TOP);p.setStyle(Paint.Style.FILL);c.drawPath(blade,p);
    box(c,cx-size*.45f,cy+size*.31f,cx-size*.23f,cy+size*.49f,4,col);
  }

  void drawHud(Canvas c){
    float y=hudTop();
    p.setColor(TOP);c.drawRect(0,y,W,navTop()+d(46),p);
    txt(c,"IRONVALE",d(12),y+d(25),14,WHITE,true);
    txt(c,"x20 • "+s.gameClock(),d(102),y+d(24),8,Color.rgb(212,220,208),false);

    Mission inc=s.soonestIncoming();
    if(inc!=null){
      box(c,W-d(122),y+d(6),W-d(56),y+d(31),8,RED);
      txt(c,"ATTACK "+s.realEta(inc.arrival-s.sim),W-d(116),y+d(23),7,WHITE,true);
    }
    box(c,W-d(48),y+d(6),W-d(9),y+d(31),8,TOP2);txt(c,"Ⅱ",W-d(35),y+d(24),12,WHITE,true);

    float sy=stockTop(),gap=d(4),cw=(W-d(20)-gap*3)/4f;
    long[] vals={(long)s.wood,(long)s.clay,(long)s.iron,(long)s.grain};
    int[] cols={GREEN,CLAY,IRON,CROP};String[] n={"WOOD","CLAY","IRON","CROP"};
    for(int i=0;i<4;i++){
      float x=d(10)+i*(cw+gap);box(c,x,sy,x+cw,sy+d(39),7,Color.rgb(246,243,232));
      box(c,x+d(5),sy+d(6),x+d(10),sy+d(33),2,cols[i]);
      txt(c,n[i],x+d(14),sy+d(14),6,MUTED,true);txt(c,compact(vals[i]),x+d(14),sy+d(31),10,TEXT,true);
    }

    float ny=navTop(),nw=(W-d(10))/5f;
    for(int i=0;i<5;i++){
      float x=d(5)+i*nw;if(i==screen)box(c,x+d(2),ny+d(3),x+nw-d(2),ny+d(42),8,TOP2);
      drawNavIcon(c,i,x+nw/2f,ny+d(16),i==screen?WHITE:Color.rgb(205,216,203));
      txt(c,nav[i],x+d(7),ny+d(37),6,i==screen?WHITE:Color.rgb(205,216,203),true);
      if(i==3&&s.incomingCount()>0){
        box(c,x+nw-d(18),ny+d(4),x+nw-d(5),ny+d(17),7,RED);txt(c,""+s.incomingCount(),x+nw-d(14),ny+d(14),6,WHITE,true);
      }
    }
  }

  void drawNavIcon(Canvas c,int i,float x,float y,int col){
    if(i==0){
      line(c,x-d(7),y+d(7),x-d(3),y-d(6),2,col);line(c,x+d(1),y+d(7),x+d(5),y-d(6),2,col);line(c,x+d(8),y+d(7),x+d(10),y-d(4),2,col);
    }else if(i==1){
      box(c,x-d(8),y-d(1),x+d(8),y+d(8),2,col);Path r=new Path();r.moveTo(x-d(10),y);r.lineTo(x,y-d(8));r.lineTo(x+d(10),y);r.close();p.setColor(col);c.drawPath(r,p);
    }else if(i==2){
      outline(c,x-d(9),y-d(7),x+d(9),y+d(7),2,1,col);line(c,x,y-d(7),x,y+d(7),1,col);line(c,x-d(9),y,x+d(9),y,1,col);
    }else if(i==3){
      box(c,x-d(8),y-d(7),x+d(8),y+d(7),2,col);line(c,x-d(5),y-d(3),x+d(5),y-d(3),1,TOP);line(c,x-d(5),y+1,x+d(5),y+1,1,TOP);
    }else{
      p.setColor(col);c.drawCircle(x,y-d(3),d(5),p);box(c,x-d(7),y+d(2),x+d(7),y+d(9),5,col);
    }
  }

  String compact(long v){
    if(v>=1000000)return String.format(Locale.US,"%.1fM",v/1000000.0);
    if(v>=10000)return String.format(Locale.US,"%.1fk",v/1000.0);
    return Long.toString(v);
  }

  void sectionTitle(Canvas c,String title,String sub){
    float y=contentTop()+d(7);txt(c,title,d(15),y+d(17),16,TEXT,true);txt(c,sub,d(15),y+d(35),8,MUTED,false);
  }

  void quest(Canvas c){
    float y=contentTop()+d(49);box(c,d(12),y,W-d(12),y+d(27),8,Color.rgb(220,230,207));
    txt(c,s.quest(),d(22),y+d(18),8,Color.rgb(62,91,47),true);
  }

  void drawFields(Canvas c){
    sectionTitle(c,"Resource Fields","18 individual fields feed Emberhold.");
    quest(c);
    float top=contentTop()+d(88),bottom=contentBottom()-d(54),cx=W/2f,cy=(top+bottom)/2f;
    float rx=Math.max(d(112),W*.39f),ry=Math.max(d(150),(bottom-top)*.42f);
    for(int i=0;i<18;i++){
      float[] pt=fieldPoint(i,cx,cy,rx,ry);
      drawFieldNode(c,i,pt[0],pt[1]);
    }
    box(c,cx-d(55),cy-d(37),cx+d(55),cy+d(37),13,Color.rgb(216,203,169));
    outline(c,cx-d(55),cy-d(37),cx+d(55),cy+d(37),13,1,BORDER);
    drawVillageIcon(c,cx,cy-d(6),TOP);txt(c,"VILLAGE CENTER",cx-d(43),cy+d(24),7,TEXT,true);
    drawQueue(c,contentBottom()-d(44));
  }

  float[] fieldPoint(int i,float cx,float cy,float rx,float ry){
    double a=-Math.PI/2+(Math.PI*2*i/18.0);
    double ring=(i%3==1)?0.79:1.0;
    return new float[]{cx+(float)Math.cos(a)*rx*(float)ring,cy+(float)Math.sin(a)*ry*(float)ring};
  }

  int fieldColor(int type){if(type==GameState.WOOD)return GREEN;if(type==GameState.CLAY)return CLAY;if(type==GameState.IRON)return IRON;return CROP;}

  void drawFieldNode(Canvas c,int idx,float x,float y){
    int type=s.fieldType(idx),lv=s.fields[idx],col=fieldColor(type);
    float r=d(20);box(c,x-r,y-r,x+r,y+r,10,Color.rgb(250,247,238));outline(c,x-r,y-r,x+r,y+r,10,1,BORDER);
    drawResourceIcon(c,type,x,y-d(3),col);
    box(c,x+d(8),y+d(7),x+d(24),y+d(23),8,TOP);txt(c,""+lv,x+d(13),y+d(19),7,WHITE,true);
  }

  void drawResourceIcon(Canvas c,int type,float x,float y,int col){
    p.setColor(col);p.setStyle(Paint.Style.FILL);
    if(type==GameState.WOOD){
      line(c,x,y-d(10),x,y+d(10),3,col);line(c,x-d(7),y-d(2),x+d(7),y-d(2),2,col);line(c,x-d(5),y+d(4),x+d(5),y+d(4),2,col);
    }else if(type==GameState.CLAY){
      box(c,x-d(8),y-d(7),x+d(8),y+d(7),5,col);line(c,x-d(4),y-d(7),x+d(3),y+d(7),1,WHITE);
    }else if(type==GameState.IRON){
      line(c,x-d(7),y+d(7),x+d(7),y-d(7),5,col);line(c,x-d(2),y-d(8),x+d(8),y+d(2),2,col);
    }else{
      line(c,x,y-d(10),x,y+d(10),2,col);line(c,x-d(6),y-d(5),x,y,2,col);line(c,x+d(6),y-d(4),x,y+d(1),2,col);
    }
  }

  void drawVillageIcon(Canvas c,float x,float y,int col){
    box(c,x-d(10),y-d(2),x+d(10),y+d(10),2,col);
    Path roof=new Path();roof.moveTo(x-d(13),y);roof.lineTo(x,y-d(11));roof.lineTo(x+d(13),y);roof.close();p.setColor(col);c.drawPath(roof,p);
  }

  void drawQueue(Canvas c,float y){
    box(c,d(12),y,W-d(12),y+d(38),8,PANEL2);outline(c,d(12),y,W-d(12),y+d(38),8,1,BORDER);
    if(s.buildKey.isEmpty()){
      txt(c,"CONSTRUCTION",d(22),y+d(15),7,MUTED,true);txt(c,"Queue empty",d(22),y+d(30),8,TEXT,false);
    }else{
      String q=s.buildKey.startsWith("field:")?"Resource field → Lv "+s.buildTarget:s.label(s.buildKey)+" → Lv "+s.buildTarget;
      txt(c,"CONSTRUCTION",d(22),y+d(15),7,GREEN,true);txt(c,q+" • "+s.realEta(s.buildFinish-s.sim),d(22),y+d(30),8,TEXT,true);
    }
  }

  void drawVillage(Canvas c){
    sectionTitle(c,"Emberhold","Tap a building to open its page.");
    quest(c);
    float top=contentTop()+d(84),bottom=contentBottom()-d(76),left=d(12),right=W-d(12);
    box(c,left,top,right,bottom,18,Color.rgb(210,205,165));
    outline(c,left,top,right,bottom,18,1,BORDER);

    float cx=W/2f,cy=(top+bottom)/2f;
    p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(d(5));p.setColor(Color.rgb(144,133,103));
    c.drawOval(left+d(20),top+d(18),right-d(20),bottom-d(18),p);p.setStyle(Paint.Style.FILL);
    line(c,cx,top+d(35),cx,bottom-d(30),3,Color.rgb(190,177,135));
    line(c,left+d(35),cy,right-d(35),cy,3,Color.rgb(190,177,135));

    for(int i=0;i<buildingKeys.length;i++){
      float x=left+(right-left)*buildingX[i],y=top+(bottom-top)*buildingY[i];
      drawBuilding(c,buildingKeys[i],x,y);
    }

    float ay=contentBottom()-d(68);box(c,d(12),ay,W-d(12),ay+d(58),9,PANEL);
    outline(c,d(12),ay,W-d(12),ay+d(58),9,1,BORDER);
    txt(c,"TROOPS",d(23),ay+d(18),7,MUTED,true);
    txt(c,s.infantry+" Shieldguard • "+s.scouts+" Pathfinder • "+s.cavalry+" Iron Rider",d(23),ay+d(36),8,TEXT,true);
    Mission inc=s.soonestIncoming();
    txt(c,inc==null?"No incoming attacks":"Incoming attack • "+s.realEta(inc.arrival-s.sim),d(23),ay+d(51),7,inc==null?MUTED:RED,true);
  }

  void drawBuilding(Canvas c,String key,float x,float y){
    int lv=s.level(key),col=lv>0?buildingColor(key):Color.rgb(185,181,166);
    float w=d(52),h=d(42);
    box(c,x-w/2,y-h/2,x+w/2,y+h/2,8,Color.rgb(245,240,223));outline(c,x-w/2,y-h/2,x+w/2,y+h/2,8,1,BORDER);
    drawBuildingIcon(c,key,x,y-d(5),col,lv);
    String label=s.label(key);if(label.length()>12)label=label.substring(0,12);
    txt(c,label,x-w/2+d(3),y+h/2-d(5),6,TEXT,true);
    box(c,x+w/2-d(15),y-h/2+d(2),x+w/2-d(1),y-h/2+d(16),7,TOP);txt(c,""+lv,x+w/2-d(11),y-h/2+d(12),6,WHITE,true);
  }

  int buildingColor(String key){
    if(key.equals("barracks")||key.equals("stable")||key.equals("smithy"))return RED;
    if(key.equals("warehouse")||key.equals("granary")||key.equals("market"))return GOLD;
    if(key.equals("academy")||key.equals("mansion"))return BLUE;
    if(key.equals("wall"))return IRON;
    return GREEN;
  }

  void drawBuildingIcon(Canvas c,String key,float x,float y,int col,int lv){
    if(lv==0){outline(c,x-d(9),y-d(9),x+d(9),y+d(9),4,2,col);txt(c,"+",x-d(4),y+d(6),13,col,true);return;}
    if(key.equals("wall")){line(c,x-d(13),y+d(7),x+d(13),y+d(7),5,col);line(c,x-d(9),y+d(7),x-d(9),y-d(5),3,col);line(c,x+d(9),y+d(7),x+d(9),y-d(5),3,col);return;}
    box(c,x-d(10),y-d(3),x+d(10),y+d(10),2,col);
    Path roof=new Path();roof.moveTo(x-d(12),y-d(2));roof.lineTo(x,y-d(12));roof.lineTo(x+d(12),y-d(2));roof.close();p.setColor(col);c.drawPath(roof,p);
    if(key.equals("main"))box(c,x-d(3),y-d(17),x+d(3),y-d(8),1,col);
  }

  void drawWorld(Canvas c){
    sectionTitle(c,"World Map","Villages, oases, ruins and your armies.");
    float y=contentTop()+d(51);
    button(c,d(12),y,d(42),d(32),"←");
    button(c,d(58),y,d(42),d(32),"↑");
    button(c,W-d(100),y,d(42),d(32),"↓");
    button(c,W-d(54),y,d(42),d(32),"→");
    button(c,W/2f-d(30),y,d(60),d(32),"HOME");
    txt(c,"("+mapX+"|"+mapY+")",W/2f-d(23),y+d(48),8,MUTED,true);

    float top=y+d(57),available=contentBottom()-top-d(53);
    float cell=Math.min((W-d(18))/9f,available/9f),left=(W-cell*9)/2f;
    for(int row=0;row<9;row++)for(int col=0;col<9;col++){
      int wx=mapX+col-4,wy=mapY+4-row;drawMapTile(c,left+col*cell,top+row*cell,cell-1,wx,wy);
    }
    float fy=contentBottom()-d(44);box(c,d(12),fy,W-d(12),fy+d(37),8,PANEL);
    txt(c,"Rivals "+s.bots.size()+" • Oases "+s.oases.size()+"/"+Math.min(3,s.mansion)+" • Outgoing "+outgoingCount(),d(22),fy+d(16),7,MUTED,true);
    txt(c,"Tap a tile for actions. Swipe or use arrows to pan.",d(22),fy+d(30),7,TEXT,false);
  }

  int outgoingCount(){int n=0;for(Mission m:s.missions)if(!m.enemy)n++;return n;}

  void button(Canvas c,float x,float y,float w,float h,String label){
    box(c,x,y,x+w,y+h,8,PANEL2);outline(c,x,y,x+w,y+h,8,1,BORDER);
    txt(c,label,x+w/2-d(label.length()*2.3f),y+h/2+d(4),8,TEXT,true);
  }

  void drawMapTile(Canvas c,float x,float y,float size,int wx,int wy){
    int t=s.tileType(wx,wy),col=Color.rgb(173,192,134);
    if(t==1)col=Color.rgb(106,154,83);else if(t==2)col=Color.rgb(188,171,89);else if(t==3)col=Color.rgb(164,147,117);
    else if(t==8)col=Color.rgb(195,139,111);else if(t==9)col=Color.rgb(224,193,106);
    p.setColor(col);p.setStyle(Paint.Style.FILL);c.drawRect(x,y,x+size,y+size,p);
    p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1);p.setColor(Color.rgb(135,126,105));c.drawRect(x,y,x+size,y+size,p);p.setStyle(Paint.Style.FILL);
    if(t==8){
      drawVillageIcon(c,x+size*.50f,y+size*.46f,Color.rgb(108,64,48));
    }else if(t==9){
      drawVillageIcon(c,x+size*.50f,y+size*.43f,TOP);txt(c,"★",x+size*.39f,y+size*.82f,7,TOP,true);
    }else if(t==1){
      line(c,x+size*.50f,y+size*.22f,x+size*.50f,y+size*.74f,2,Color.rgb(47,95,44));
      p.setColor(Color.rgb(55,112,49));c.drawCircle(x+size*.43f,y+size*.30f,size*.12f,p);c.drawCircle(x+size*.58f,y+size*.34f,size*.11f,p);
    }else if(t==2){
      line(c,x+size*.30f,y+size*.67f,x+size*.68f,y+size*.32f,2,Color.rgb(112,96,41));
    }else if(t==3){
      line(c,x+size*.27f,y+size*.68f,x+size*.48f,y+size*.30f,2,Color.rgb(92,80,67));
      line(c,x+size*.48f,y+size*.30f,x+size*.72f,y+size*.68f,2,Color.rgb(92,80,67));
    }
    if(s.oases.contains(wx+"|"+wy)){outline(c,x+2,y+2,x+size-2,y+size-2,1,2,GOLD);}
  }

  void drawReports(Canvas c){
    sectionTitle(c,"Reports","Combat, construction and realm events.");
    float y=contentTop()+d(55),rowH=d(55),bottom=contentBottom()-d(43);
    int max=Math.max(1,(int)((bottom-y)/rowH));
    if(s.reports.isEmpty())txt(c,"No reports yet.",d(22),y+d(28),9,MUTED,false);
    for(int i=0;i<max&&reportOffset+i<s.reports.size();i++){
      String r=s.reports.get(reportOffset+i);
      int strip=(r.toLowerCase().contains("defeat")||r.toLowerCase().contains("incoming"))?RED:(r.toLowerCase().contains("victory")||r.toLowerCase().contains("returned"))?GREEN:GOLD;
      box(c,d(12),y,W-d(12),y+d(48),8,PANEL);outline(c,d(12),y,W-d(12),y+d(48),8,1,BORDER);
      box(c,d(12),y,d(17),y+d(48),3,strip);drawWrapped(c,r,d(25),y+d(18),W-d(50),7,TEXT);y+=rowH;
    }
    float by=contentBottom()-d(36);
    button(c,d(12),by,d(83),d(31),"NEWER");
    button(c,W-d(95),by,d(83),d(31),"OLDER");
    txt(c,(reportOffset+1)+"-"+Math.min(s.reports.size(),reportOffset+max)+" / "+s.reports.size(),W/2f-d(29),by+d(20),7,MUTED,true);
  }

  void drawWrapped(Canvas c,String str,float x,float y,float max,float size,int col){
    p.setTextSize(d(size));p.setTypeface(Typeface.create("sans",Typeface.NORMAL));
    String a=str,b="";
    if(p.measureText(a)>max){
      int cut=a.length();while(cut>8&&p.measureText(a.substring(0,cut))>max)cut--;
      int sp=a.lastIndexOf(' ',cut);if(sp>8)cut=sp;b=a.substring(cut).trim();a=a.substring(0,cut).trim();
    }
    txt(c,a,x,y,size,col,false);if(!b.isEmpty())txt(c,b,x,y+d(15),size,MUTED,false);
  }

  void drawHero(Canvas c){
    sectionTitle(c,"Hero Astra","Adventures, equipment and recovery.");
    float y=contentTop()+d(52);
    box(c,d(12),y,W-d(12),y+d(124),12,PANEL);outline(c,d(12),y,W-d(12),y+d(124),12,1,BORDER);
    p.setColor(TOP2);c.drawCircle(d(62),y+d(52),d(31),p);txt(c,"A",d(51),y+d(62),23,WHITE,true);
    txt(c,"Level "+s.hero,d(109),y+d(27),12,TEXT,true);
    txt(c,"Health "+s.heroHealth+"%",d(109),y+d(49),8,s.heroHealth<35?RED:GREEN,true);
    bar(c,d(109),y+d(57),W-d(136),s.heroHealth/100f,GREEN);
    txt(c,"XP "+s.heroXp+" / "+(s.hero*100),d(109),y+d(78),8,MUTED,false);bar(c,d(109),y+d(86),W-d(136),s.heroXp/(float)(s.hero*100),GOLD);
    equipment(c,d(30),y+d(102),"WEAPON",s.heroWeapon);equipment(c,W/2f-d(36),y+d(102),"ARMOR",s.heroArmor);
    if(s.heroAway){
      txt(c,"ADVENTURE • "+s.realEta(s.heroFinish-s.sim)+" remaining",W-d(174),y+d(115),7,BLUE,true);
    }

    y+=d(139);
    box(c,d(12),y,W-d(12),y+d(43),9,PANEL2);outline(c,d(12),y,W-d(12),y+d(43),9,1,BORDER);
    txt(c,"RECOVER HERO",d(24),y+d(18),8,TEXT,true);txt(c,"Spend 120 crop for +25 health",d(24),y+d(34),7,MUTED,false);
    txt(c,"HEAL",W-d(53),y+d(27),8,GREEN,true);

    y+=d(56);txt(c,"AVAILABLE ADVENTURES",d(15),y+d(15),8,MUTED,true);y+=d(23);
    ArrayList<int[]> targets=adventureTargets(3);
    if(targets.isEmpty()){txt(c,"No unexplored ruins nearby. Pan the world map for more.",d(22),y+d(25),8,MUTED,false);return;}
    for(int i=0;i<targets.size();i++){
      int[] a=targets.get(i);box(c,d(12),y,W-d(12),y+d(55),9,PANEL);outline(c,d(12),y,W-d(12),y+d(55),9,1,BORDER);
      txt(c,"Ancient ruins ("+a[0]+"|"+a[1]+")",d(24),y+d(22),9,TEXT,true);
      txt(c,"Travel "+s.realEta(s.travelSeconds(a[0],a[1])*1.6)+" • reward + hero XP",d(24),y+d(41),7,MUTED,false);
      txt(c,s.heroAway?"BUSY":"SEND",W-d(58),y+d(32),8,s.heroAway?MUTED:GREEN,true);y+=d(63);
    }
  }

  void equipment(Canvas c,float x,float y,String name,int lv){
    box(c,x,y-d(17),x+d(72),y+d(13),7,PANEL2);txt(c,name+" +"+lv,x+d(7),y+d(3),6,TEXT,true);
  }

  void bar(Canvas c,float x,float y,float w,float value,int col){
    box(c,x,y,x+w,y+d(5),3,Color.rgb(212,205,188));box(c,x,y,x+w*Math.max(0,Math.min(1,value)),y+d(5),3,col);
  }

  ArrayList<int[]> adventureTargets(int max){
    ArrayList<int[]> out=new ArrayList<>();
    for(int radius=1;radius<=15&&out.size()<max;radius++){
      for(int x=-radius;x<=radius&&out.size()<max;x++)for(int y=-radius;y<=radius&&out.size()<max;y++){
        if(Math.max(Math.abs(x),Math.abs(y))!=radius)continue;
        if(s.tileType(x,y)==3&&!s.explored.contains(x+"|"+y))out.add(new int[]{x,y});
      }
    }
    return out;
  }

  void drawPause(Canvas c){
    p.setColor(Color.argb(180,20,24,19));c.drawRect(0,safeTop(),W,safeBottom(),p);
    float w=W-d(48),x=d(24),y=(safeTop()+safeBottom())/2f-d(130);
    box(c,x,y,x+w,y+d(260),16,PANEL);outline(c,x,y,x+w,y+d(260),16,1,BORDER);
    drawSword(c,W/2f,y+d(43),d(25),GOLD);txt(c,"REALM PAUSED",W/2f-d(58),y+d(89),16,TEXT,true);
    txt(c,"Production, bots and armies are frozen.",W/2f-d(99),y+d(110),8,MUTED,false);
    box(c,x+d(22),y+d(134),x+w-d(22),y+d(177),10,GREEN);txt(c,"RESUME",x+d(47),y+d(162),10,WHITE,true);
    box(c,x+d(22),y+d(190),x+w-d(22),y+d(232),10,PANEL2);txt(c,"SAVE & MAIN MENU",x+d(47),y+d(217),9,TEXT,true);
  }

  void showField(int idx){
    int type=s.fieldType(idx),lv=s.fields[idx];int[] cost=s.fieldCost(idx);
    double current=s.fieldProduction(lv),next=s.fieldProduction(lv+1);
    String msg="Field "+(idx+1)+" • "+s.fieldName(type)+"\nLevel "+lv+" → "+(lv+1)+"\n\nProduction: "+(int)current+"/h → "+(int)next+"/h\nCost: "+cost[0]+" wood • "+cost[1]+" clay • "+cost[2]+" iron • "+cost[3]+" crop";
    if(!s.buildKey.isEmpty())msg+="\n\nConstruction queue is occupied.";
    new AlertDialog.Builder(getContext()).setTitle(s.fieldName(type)).setMessage(msg).setNegativeButton("Close",null)
      .setPositiveButton("Upgrade",(dialog,which)->{
        if(s.queueField(idx))toast("Field upgrade started");
        else if(!s.buildKey.isEmpty())toast("Construction queue is busy");
        else toast("Not enough resources");
      }).show();
  }

  void showBuilding(String key){
    int lv=s.level(key);int[] cost=s.buildCost(key);String req=s.requirement(key);
    String msg=s.label(key)+"\nLevel "+lv+" → "+(lv+1)+"\n\nCost: "+cost[0]+" wood • "+cost[1]+" clay • "+cost[2]+" iron • "+cost[3]+" crop";
    if(!req.isEmpty())msg+="\n\nLocked: "+req;
    if(key.equals("warehouse"))msg+="\nStorage: "+s.storageCap();
    if(key.equals("granary"))msg+="\nCrop storage: "+s.granaryCap();
    if(key.equals("wall"))msg+="\nDefense power: "+s.defensePower();
    if(key.equals("rally"))msg+="\nIncoming: "+s.incomingCount()+" • Outgoing: "+outgoingCount();

    ArrayList<String> actions=new ArrayList<>();actions.add(lv==0?"Build":"Upgrade");
    if(key.equals("barracks")){actions.add("Train 5 Shieldguard");actions.add("Train 20 Shieldguard");}
    if(key.equals("academy"))actions.add("Train 3 Pathfinder");
    if(key.equals("stable"))actions.add("Train 3 Iron Rider");
    if(key.equals("rally"))actions.add("Show army movements");
    String[] arr=actions.toArray(new String[0]);
    new AlertDialog.Builder(getContext()).setTitle(s.label(key)).setMessage(msg).setItems(arr,(dialog,which)->{
      if(which==0){
        if(s.queueBuild(key))toast("Construction started");
        else if(!req.isEmpty())toast(req);
        else if(!s.buildKey.isEmpty())toast("Construction queue is busy");
        else toast("Not enough resources");
      }else if(key.equals("barracks")){
        train("infantry",which==1?5:20);
      }else if(key.equals("academy")){
        train("scouts",3);
      }else if(key.equals("stable")){
        train("cavalry",3);
      }else if(key.equals("rally")){
        showMovements();
      }
    }).setNegativeButton("Close",null).show();
  }

  void train(String type,int count){
    if(s.queueTrain(type,count))toast("Training started");
    else if(!s.trainType.isEmpty())toast("Training queue is busy");
    else if(type.equals("scouts")&&s.academy<1)toast("Build Academy first");
    else if(type.equals("cavalry")&&s.stable<1)toast("Build Stable first");
    else toast("Not enough resources or required building");
  }

  void showMovements(){
    StringBuilder b=new StringBuilder();
    if(s.missions.isEmpty())b.append("No armies are moving.");
    for(Mission m:s.missions){
      b.append(m.enemy?"INCOMING • ":"OUTGOING • ").append(m.label).append(" • ").append(s.realEta(m.arrival-s.sim)).append("\n");
    }
    new AlertDialog.Builder(getContext()).setTitle("Rally Point").setMessage(b.toString()).setPositiveButton("Close",null).show();
  }

  void showTile(int wx,int wy){
    int bi=s.botAt(wx,wy),t=s.tileType(wx,wy);
    if(bi>=0){
      Bot b=s.bots.get(bi);
      String msg="Coordinates ("+wx+"|"+wy+")\nVillage level "+b.level+"\nEstimated garrison: "+b.infantry+" infantry • "+b.cavalry+" cavalry\nTravel "+s.realEta(s.travelSeconds(wx,wy));
      String[] opts={"Raid with 25%","Raid with 50%","Raid with all available troops"};
      new AlertDialog.Builder(getContext()).setTitle(b.name).setMessage(msg).setItems(opts,(dialog,which)->{
        int pct=which==0?25:which==1?50:100;if(s.launchRaid(bi,pct))toast("Raid launched");else toast("No available troops");
      }).setNegativeButton("Close",null).show();
    }else if(t==1||t==2){
      String msg="Coordinates ("+wx+"|"+wy+")\nWild oasis. Each controlled oasis grants +10% production.\nHero Lodge level "+s.mansion+" • controlled "+s.oases.size()+"/"+Math.min(3,s.mansion);
      new AlertDialog.Builder(getContext()).setTitle(s.tileName(wx,wy)).setMessage(msg).setNegativeButton("Close",null)
        .setPositiveButton("Annex",(dialog,which)->{if(s.annexOasis(wx,wy))toast("Oasis annexed");else toast("Hero Lodge or free oasis slot required");}).show();
    }else if(t==3){
      boolean done=s.explored.contains(wx+"|"+wy);
      new AlertDialog.Builder(getContext()).setTitle("Ancient Ruins").setMessage("Coordinates ("+wx+"|"+wy+")\n"+(done?"Already explored.":"Send Astra on a timed adventure."))
        .setNegativeButton("Close",null).setPositiveButton(done?"Done":"Send Hero",(dialog,which)->{
          if(!done&&s.launchAdventure(wx,wy))toast("Astra departed");else if(!done)toast("Hero is busy");
        }).show();
    }else{
      new AlertDialog.Builder(getContext()).setTitle("Open Valley").setMessage("Coordinates ("+wx+"|"+wy+")\nUnoccupied territory.").setPositiveButton("Close",null).show();
    }
  }

  void toast(String m){Toast.makeText(getContext(),m,Toast.LENGTH_SHORT).show();}

  void confirmNewRealm(){
    if(!s.started){s.newRealm();menuScreen=false;screen=0;return;}
    new AlertDialog.Builder(getContext()).setTitle("Start a fresh realm?").setMessage("This replaces the current local save.")
      .setNegativeButton("Cancel",null).setPositiveButton("Start New",(dialog,which)->{s.newRealm();menuScreen=false;screen=0;invalidate();}).show();
  }

  @Override public boolean onTouchEvent(MotionEvent e){
    if(e.getAction()==MotionEvent.ACTION_DOWN){downX=e.getX();downY=e.getY();return true;}
    if(e.getAction()!=MotionEvent.ACTION_UP)return true;
    float x=e.getX(),y=e.getY();

    if(menuScreen){
      float top=safeTop()+d(18),bottom=safeBottom()-d(18),mid=(top+bottom)/2f;
      float by=mid-d(95)+d(139);
      if(y>=by&&y<=by+d(52)){
        if(s.started){menuScreen=false;s.paused=false;s.lastReal=System.currentTimeMillis();s.save();}
        else{s.newRealm();menuScreen=false;}
        invalidate();return true;
      }
      if(s.started&&y>=by+d(64)&&y<=by+d(109)){confirmNewRealm();return true;}
      return true;
    }

    if(s.paused){
      float py=(safeTop()+safeBottom())/2f-d(130);
      if(y>=py+d(134)&&y<=py+d(177)){s.paused=false;s.lastReal=System.currentTimeMillis();s.save();invalidate();return true;}
      if(y>=py+d(190)&&y<=py+d(232)){s.paused=true;s.save();menuScreen=true;invalidate();return true;}
      return true;
    }

    if(y>=hudTop()&&y<=hudTop()+d(38)&&x>W-d(52)){s.paused=true;s.save();invalidate();return true;}

    if(y>=navTop()&&y<=navTop()+d(46)){
      screen=Math.max(0,Math.min(4,(int)((x-d(5))/((W-d(10))/5f))));reportOffset=0;invalidate();return true;
    }

    if(screen==0){
      float top=contentTop()+d(88),bottom=contentBottom()-d(54),cx=W/2f,cy=(top+bottom)/2f;
      float rx=Math.max(d(112),W*.39f),ry=Math.max(d(150),(bottom-top)*.42f);
      if(Math.abs(x-cx)<d(58)&&Math.abs(y-cy)<d(41)){screen=1;invalidate();return true;}
      int best=-1;double dist=d(29);
      for(int i=0;i<18;i++){
        float[] pt=fieldPoint(i,cx,cy,rx,ry);double dd=Math.hypot(x-pt[0],y-pt[1]);if(dd<dist){dist=dd;best=i;}
      }
      if(best>=0){showField(best);return true;}
    }else if(screen==1){
      float top=contentTop()+d(84),bottom=contentBottom()-d(76),left=d(12),right=W-d(12);
      int best=-1;double dist=d(35);
      for(int i=0;i<buildingKeys.length;i++){
        float bx=left+(right-left)*buildingX[i],by=top+(bottom-top)*buildingY[i];double dd=Math.hypot(x-bx,y-by);
        if(dd<dist){dist=dd;best=i;}
      }
      if(best>=0){showBuilding(buildingKeys[best]);return true;}
    }else if(screen==2){
      float by=contentTop()+d(51);
      if(y>=by&&y<=by+d(34)){
        if(x<d(56))mapX-=4;else if(x<d(104))mapY+=4;else if(x>W-d(56))mapX+=4;else if(x>W-d(104))mapY-=4;else {mapX=0;mapY=0;}
        mapX=Math.max(-45,Math.min(45,mapX));mapY=Math.max(-45,Math.min(45,mapY));invalidate();return true;
      }
      float top=by+d(57),available=contentBottom()-top-d(53),cell=Math.min((W-d(18))/9f,available/9f),left=(W-cell*9)/2f;
      if(x>=left&&x<left+cell*9&&y>=top&&y<top+cell*9){
        int col=(int)((x-left)/cell),row=(int)((y-top)/cell);showTile(mapX+col-4,mapY+4-row);return true;
      }
      float dx=x-downX,dy=y-downY;
      if(Math.abs(dx)>d(30)||Math.abs(dy)>d(30)){
        if(Math.abs(dx)>Math.abs(dy))mapX+=dx>0?-3:3;else mapY+=dy>0?3:-3;
        mapX=Math.max(-45,Math.min(45,mapX));mapY=Math.max(-45,Math.min(45,mapY));invalidate();return true;
      }
    }else if(screen==3){
      float by=contentBottom()-d(36);
      int visible=Math.max(1,(int)((by-(contentTop()+d(55)))/d(55)));
      if(y>=by&&y<=by+d(33)){
        if(x<W/2)reportOffset=Math.max(0,reportOffset-visible);
        else reportOffset=Math.min(Math.max(0,s.reports.size()-1),reportOffset+visible);
        invalidate();return true;
      }
    }else{
      float hy=contentTop()+d(52)+d(139);
      if(y>=hy&&y<=hy+d(43)&&x>W-d(80)){
        if(s.heroHealth>=100)toast("Astra is already fully healthy");
        else if(s.grain<120)toast("Need 120 crop");
        else{s.grain-=120;s.heroHealth=Math.min(100,s.heroHealth+25);s.save();toast("Astra recovered");}
        invalidate();return true;
      }
      float ay=hy+d(56)+d(23);ArrayList<int[]> t=adventureTargets(3);
      for(int i=0;i<t.size();i++){
        float yy=ay+i*d(63);
        if(y>=yy&&y<=yy+d(55)){
          if(s.heroAway)toast("Astra is already on an adventure");
          else if(s.launchAdventure(t.get(i)[0],t.get(i)[1]))toast("Astra departed");
          invalidate();return true;
        }
      }
    }
    return true;
  }
}
