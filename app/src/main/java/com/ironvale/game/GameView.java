package com.ironvale.game;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;
import java.util.Locale;

class GameView extends View {
  final Paint p=new Paint(3);
  final Handler handler=new Handler(Looper.getMainLooper());
  final GameState s;
  int W,H,screen=0,mapX=0,mapY=0;
  boolean menuScreen=true;
  float downX,downY;

  final int BG=Color.rgb(13,22,18), PANEL=Color.rgb(27,39,32), PANEL2=Color.rgb(39,54,44);
  final int PANEL3=Color.rgb(48,64,52), GOLD=Color.rgb(218,173,73), CREAM=Color.rgb(242,235,216);
  final int MUTED=Color.rgb(169,182,172), GREEN=Color.rgb(74,142,88), RED=Color.rgb(180,72,65);
  final int BLUE=Color.rgb(72,113,139), CLAY=Color.rgb(151,91,62), IRON=Color.rgb(86,103,111);
  final String[] nav={"FIELDS","CENTER","MAP","ARMY","REPORTS"};
  final String[] buildKeys={"main","warehouse","granary","barracks","rally","wall","smithy","academy","residence","mansion"};
  final String[] buildShort={"Main Hall","Warehouse","Granary","Barracks","Rally Point","Stone Wall","Smithy","Academy","Residence","Hero Lodge"};

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
    if(s.started && !s.paused){
      s.tick(System.currentTimeMillis());
      s.paused=true;
      s.save();
    }
    setFocusable(true);
    handler.post(loop);
  }

  void dispose(){handler.removeCallbacks(loop);s.save();}
  float d(float v){return v*getResources().getDisplayMetrics().density;}
  float contentTop(){return d(118);}
  float contentBottom(){return H-d(74);}

  void txt(Canvas c,String str,float x,float y,float size,int color,boolean bold){
    p.setColor(color);p.setTextSize(d(size));p.setTypeface(Typeface.create("sans",bold?Typeface.BOLD:Typeface.NORMAL));p.setStyle(Paint.Style.FILL);
    c.drawText(str,x,y,p);
  }

  void box(Canvas c,float l,float t,float r,float b,float rad,int color){
    p.setStyle(Paint.Style.FILL);p.setColor(color);c.drawRoundRect(l,t,r,b,d(rad),d(rad),p);
  }

  void line(Canvas c,float x1,float y1,float x2,float y2,float width,int color){
    p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(d(width));p.setStrokeCap(Paint.Cap.ROUND);p.setColor(color);c.drawLine(x1,y1,x2,y2,p);p.setStyle(Paint.Style.FILL);
  }

  @Override protected void onDraw(Canvas c){
    super.onDraw(c);W=getWidth();H=getHeight();c.drawColor(BG);
    if(menuScreen){drawMenu(c);return;}
    drawTop(c);
    if(screen==0)drawFields(c); else if(screen==1)drawCenter(c); else if(screen==2)drawWorld(c); else if(screen==3)drawArmy(c); else drawReports(c);
    drawBottom(c);
    if(s.paused)drawPause(c);
  }

  void drawMenu(Canvas c){
    float cy=d(95);
    drawSword(c,W/2f,cy,d(46),GOLD);
    txt(c,"IRONVALE",W/2f-d(66),cy+d(72),27,CREAM,true);
    txt(c,"WARFRONT",W/2f-d(48),cy+d(96),13,GOLD,true);
    txt(c,"Local strategy realm • x20 speed",W/2f-d(94),cy+d(123),10,MUTED,false);
    float y=cy+d(154);
    box(c,d(18),y,W-d(18),y+d(118),18,PANEL);
    txt(c,"LOCAL REALM",d(34),y+d(28),10,GOLD,true);
    txt(c,"No account. No server. Progress stays on this device.",d(34),y+d(52),9,CREAM,false);
    txt(c,"Bots expand, raid and attack while the realm is running.",d(34),y+d(73),9,MUTED,false);
    txt(c,"Pause freezes the simulation completely.",d(34),y+d(94),9,MUTED,false);
    y+=d(140);
    box(c,d(26),y,W-d(26),y+d(54),14,GOLD);
    txt(c,s.started?"CONTINUE REALM":"START NEW REALM",d(44),y+d(34),12,BG,true);
    if(s.started){
      y+=d(68);box(c,d(26),y,W-d(26),y+d(48),13,PANEL2);txt(c,"START FRESH REALM",d(44),y+d(31),10,CREAM,true);
      y+=d(66);txt(c,"Saved: "+s.gameClock(),d(34),y,9,MUTED,false);
    }
    float fy=H-d(48);txt(c,"Single-player • offline save • original Ironvale world",d(24),fy,8,MUTED,false);
  }

  void drawSword(Canvas c,float cx,float cy,float size,int color){
    line(c,cx-size*.42f,cy+size*.42f,cx+size*.28f,cy-size*.28f,7,color);
    line(c,cx-size*.15f,cy+size*.08f,cx+size*.15f,cy+size*.38f,5,CREAM);
    Path blade=new Path();
    blade.moveTo(cx+size*.20f,cy-size*.20f);blade.lineTo(cx+size*.46f,cy-size*.46f);blade.lineTo(cx+size*.36f,cy-size*.12f);blade.close();
    p.setColor(CREAM);p.setStyle(Paint.Style.FILL);c.drawPath(blade,p);
    box(c,cx-size*.48f,cy+size*.34f,cx-size*.24f,cy+size*.52f,5,color);
  }

  void drawTop(Canvas c){
    box(c,0,0,W,d(38),0,Color.rgb(17,28,23));
    txt(c,"IRONVALE",d(14),d(25),15,GOLD,true);
    txt(c,s.gameClock(),d(112),d(24),9,MUTED,false);
    box(c,W-d(57),d(6),W-d(10),d(33),9,PANEL3);
    txt(c,"Ⅱ",W-d(41),d(25),13,CREAM,true);

    float y=d(44),gap=d(5),cw=(W-d(26)-gap*3)/4f;
    long[] vals={(long)s.wood,(long)s.clay,(long)s.iron,(long)s.grain};
    String[] names={"WOOD","CLAY","IRON","GRAIN"};
    int[] cols={GREEN,CLAY,IRON,GOLD};
    for(int i=0;i<4;i++){
      float x=d(13)+i*(cw+gap);box(c,x,y,x+cw,y+d(43),9,PANEL);
      box(c,x+d(6),y+d(7),x+d(10),y+d(35),2,cols[i]);
      txt(c,names[i],x+d(15),y+d(16),7,MUTED,true);txt(c,compact(vals[i]),x+d(15),y+d(34),11,CREAM,true);
    }

    Mission incoming=s.soonestIncoming();
    float sy=d(92);
    if(incoming!=null){
      box(c,d(13),sy,W-d(13),sy+d(21),7,Color.rgb(67,36,32));
      Bot b=s.bots.get(incoming.botIndex);
      txt(c,"INCOMING • "+b.name+" • "+s.realEta(incoming.arrival-s.sim),d(22),sy+d(15),8,Color.rgb(255,190,177),true);
    }else{
      txt(c,"x20 REALM • "+s.bots.size()+" rival settlements • "+s.oases.size()+" oasis bonuses",d(16),sy+d(15),8,MUTED,false);
    }
  }

  String compact(long v){
    if(v>=1000000)return String.format(Locale.US,"%.1fM",v/1000000.0);
    if(v>=10000)return String.format(Locale.US,"%.1fk",v/1000.0);
    return Long.toString(v);
  }

  void title(Canvas c,String a,String b){
    float y=contentTop();txt(c,a,d(16),y+d(22),19,CREAM,true);txt(c,b,d(16),y+d(42),9,MUTED,false);
  }

  void drawFields(Canvas c){
    title(c,"Resource Fields","Upgrade the four districts feeding Emberhold.");
    float y=contentTop()+d(56),gap=d(10),cw=(W-d(38))/2f,ch=d(112);
    fieldCard(c,d(14),y,cw,ch,"WOODCUTTERS",s.woodField,s.production(s.woodField),GREEN,"wood");
    fieldCard(c,d(24)+cw,y,cw,ch,"CLAY PITS",s.clayField,s.production(s.clayField),CLAY,"clay");
    y+=ch+gap;
    fieldCard(c,d(14),y,cw,ch,"IRON MINES",s.ironField,s.production(s.ironField),IRON,"iron");
    fieldCard(c,d(24)+cw,y,cw,ch,"CROPLANDS",s.cropField,s.production(s.cropField),GOLD,"crop");

    y+=ch+d(22);
    box(c,d(14),y,W-d(14),y+d(106),15,PANEL);
    txt(c,"EMberhold production",d(28),y+d(24),9,MUTED,true);
    txt(c,"Storage "+s.storageCap()+" • Granary "+s.granaryCap(),d(28),y+d(47),10,CREAM,true);
    txt(c,"Oasis bonus +"+(s.oases.size()*10)+"% • Troop crop "+(s.infantry+s.scouts+s.cavalry*3)+"/h",d(28),y+d(69),9,MUTED,false);
    txt(c,"All timers and production run at x20 world speed.",d(28),y+d(91),9,GOLD,false);
    drawQueue(c,contentBottom()-d(56));
  }

  void fieldCard(Canvas c,float x,float y,float w,float h,String name,int lv,double prod,int col,String key){
    box(c,x,y,x+w,y+h,14,PANEL);
    box(c,x+d(10),y+d(11),x+d(46),y+d(47),10,col);
    if(key.equals("wood")){line(c,x+d(28),y+d(18),x+d(28),y+d(40),4,CREAM);line(c,x+d(20),y+d(24),x+d(36),y+d(24),3,CREAM);}
    else if(key.equals("clay")){box(c,x+d(19),y+d(21),x+d(38),y+d(38),4,CREAM);}
    else if(key.equals("iron")){line(c,x+d(20),y+d(37),x+d(36),y+d(21),5,CREAM);}
    else{line(c,x+d(28),y+d(18),x+d(28),y+d(41),3,CREAM);line(c,x+d(22),y+d(24),x+d(28),y+d(29),2,CREAM);line(c,x+d(34),y+d(24),x+d(28),y+d(29),2,CREAM);}
    txt(c,name,x+d(12),y+d(65),9,CREAM,true);txt(c,"Level "+lv,x+d(12),y+d(84),9,GOLD,true);
    txt(c,(int)prod+"/h",x+w-d(54),y+d(84),9,MUTED,false);txt(c,"UPGRADE",x+d(12),y+d(103),8,MUTED,true);
  }

  void drawCenter(Canvas c){
    title(c,"Village Center","Economic, military and expansion buildings.");
    float y=contentTop()+d(55),gap=d(8),cw=(W-d(36))/2f,ch=d(62);
    for(int i=0;i<buildKeys.length;i++){
      int row=i/2,col=i%2;float x=d(14)+col*(cw+gap), yy=y+row*(ch+gap);
      String key=buildKeys[i];int lv=s.level(key);String req=s.requirement(key);
      box(c,x,yy,x+cw,yy+ch,12,req.isEmpty()?PANEL:PANEL2);
      txt(c,buildShort[i],x+d(11),yy+d(23),10,CREAM,true);
      txt(c,lv==0?"Not built":"Level "+lv,x+d(11),yy+d(43),8,lv==0?MUTED:GOLD,true);
      if(!req.isEmpty())txt(c,"LOCKED",x+cw-d(48),yy+d(43),7,RED,true);
    }
    drawQueue(c,contentBottom()-d(55));
  }

  void drawQueue(Canvas c,float y){
    if(y<contentTop()+d(40))return;
    box(c,d(14),y,W-d(14),y+d(44),11,PANEL2);
    if(s.buildKey.isEmpty()){
      txt(c,"BUILD QUEUE",d(27),y+d(18),8,MUTED,true);txt(c,"Idle",d(27),y+d(35),9,CREAM,false);
    }else{
      txt(c,"BUILDING • "+s.label(s.buildKey)+" → Lv "+s.buildTarget,d(27),y+d(18),8,GOLD,true);
      txt(c,s.realEta(s.buildFinish-s.sim)+" real time remaining",d(27),y+d(35),9,CREAM,false);
    }
  }

  void drawWorld(Canvas c){
    title(c,"World Map","Tap a tile. Pan to hunt rivals, ruins and oases.");
    float y=contentTop()+d(52);
    float bw=d(38);
    button(c,d(14),y,bw,d(34),"W");button(c,d(58),y,bw,d(34),"N");
    button(c,W-d(96),y,bw,d(34),"S");button(c,W-d(52),y,bw,d(34),"E");
    txt(c,"Center ("+mapX+"|"+mapY+")",W/2f-d(48),y+d(22),9,MUTED,true);

    float top=y+d(44);
    float cell=Math.min((W-d(20))/9f,(contentBottom()-top-d(12))/9f);
    float left=(W-cell*9)/2f;
    for(int row=0;row<9;row++){
      for(int col=0;col<9;col++){
        int wx=mapX+col-4,wy=mapY+4-row;
        float x=left+col*cell,yy=top+row*cell;
        drawMapTile(c,x,yy,cell-1,wx,wy);
      }
    }
  }

  void button(Canvas c,float x,float y,float w,float h,String label){
    box(c,x,y,x+w,y+h,9,PANEL2);txt(c,label,x+w/2-d(4),y+h/2+d(4),10,CREAM,true);
  }

  void drawMapTile(Canvas c,float x,float y,float size,int wx,int wy){
    int t=s.tileType(wx,wy),col=Color.rgb(42,63,48);
    if(t==1)col=Color.rgb(45,83,53);else if(t==2)col=Color.rgb(85,83,45);else if(t==3)col=Color.rgb(75,68,58);
    else if(t==8)col=Color.rgb(93,51,47);else if(t==9)col=Color.rgb(88,75,43);
    p.setColor(col);p.setStyle(Paint.Style.FILL);c.drawRect(x,y,x+size,y+size,p);
    if(t==8){box(c,x+size*.26f,y+size*.25f,x+size*.74f,y+size*.72f,2,Color.rgb(183,143,105));line(c,x+size*.22f,y+size*.25f,x+size*.78f,y+size*.25f,2,CREAM);}
    else if(t==9){box(c,x+size*.24f,y+size*.28f,x+size*.76f,y+size*.74f,3,GOLD);txt(c,"★",x+size*.34f,y+size*.63f,10,BG,true);}
    else if(t==1){line(c,x+size*.5f,y+size*.24f,x+size*.5f,y+size*.76f,2,Color.rgb(194,221,182));}
    else if(t==2){line(c,x+size*.3f,y+size*.68f,x+size*.72f,y+size*.32f,2,Color.rgb(230,218,156));}
    else if(t==3){line(c,x+size*.28f,y+size*.68f,x+size*.5f,y+size*.3f,2,CREAM);line(c,x+size*.5f,y+size*.3f,x+size*.72f,y+size*.68f,2,CREAM);}
    if(s.oases.contains(wx+"|"+wy)){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(d(2));p.setColor(GOLD);c.drawRect(x+2,y+2,x+size-2,y+size-2,p);p.setStyle(Paint.Style.FILL);}
  }

  void drawArmy(Canvas c){
    title(c,"Army & Rally Point","Train units, watch movements and defend the realm.");
    float y=contentTop()+d(55),gap=d(8),cw=(W-d(44))/3f;
    troop(c,d(14),y,cw,"SHIELDGUARD",s.infantry,12,GREEN);
    troop(c,d(22)+cw,y,cw,"PATHFINDER",s.scouts,0,BLUE);
    troop(c,d(30)+cw*2,y,cw,"IRON RIDER",s.cavalry,30,GOLD);
    y+=d(92);
    box(c,d(14),y,W-d(14),y+d(58),12,PANEL);
    txt(c,"Defense "+s.defensePower()+" • Attack "+s.attackPower(),d(27),y+d(24),10,CREAM,true);
    txt(c,"Hero Astra Lv "+s.hero+" • XP "+s.heroXp+"/"+(s.hero*100),d(27),y+d(44),8,MUTED,false);
    y+=d(70);
    button(c,d(14),y,(W-d(44))/3f,d(45),"+5 INF");
    button(c,d(22)+(W-d(44))/3f,y,(W-d(44))/3f,d(45),"+2 SCOUT");
    button(c,d(30)+2*(W-d(44))/3f,y,(W-d(44))/3f,d(45),"+2 CAV");
    y+=d(58);
    if(!s.trainType.isEmpty()){
      box(c,d(14),y,W-d(14),y+d(43),11,PANEL2);txt(c,"TRAINING • "+s.trainCount+" "+s.unitLabel(s.trainType),d(27),y+d(18),8,GOLD,true);
      txt(c,s.realEta(s.trainFinish-s.sim)+" remaining",d(27),y+d(35),8,CREAM,false);y+=d(53);
    }
    txt(c,"MOVEMENTS",d(16),y+d(18),9,MUTED,true);y+=d(27);
    int shown=0;
    for(Mission m:s.missions){
      if(shown>=4)break;
      box(c,d(14),y,W-d(14),y+d(47),10,m.enemy?Color.rgb(64,37,33):PANEL);
      txt(c,m.enemy?"INCOMING":"OUTGOING",d(26),y+d(18),8,m.enemy?Color.rgb(255,181,166):GOLD,true);
      txt(c,m.label,d(26),y+d(36),9,CREAM,false);txt(c,s.realEta(m.arrival-s.sim),W-d(63),y+d(29),9,MUTED,true);
      y+=d(54);shown++;
    }
    if(shown==0)txt(c,"No armies are moving.",d(27),y+d(24),9,MUTED,false);
  }

  void troop(Canvas c,float x,float y,float w,String name,int count,int power,int col){
    box(c,x,y,x+w,y+d(80),12,PANEL);box(c,x+d(9),y+d(10),x+d(16),y+d(46),3,col);
    txt(c,name,x+d(22),y+d(23),7,MUTED,true);txt(c,""+count,x+d(22),y+d(50),18,CREAM,true);
    if(power>0)txt(c,"Atk "+power,x+d(22),y+d(68),7,MUTED,false);
  }

  void drawReports(Canvas c){
    title(c,"Reports","Battles, construction and realm events.");
    float y=contentTop()+d(56);
    if(s.reports.isEmpty()){txt(c,"No reports yet.",d(24),y+d(25),10,MUTED,false);return;}
    int max=(int)((contentBottom()-y)/d(58));
    for(int i=0;i<Math.min(max,s.reports.size());i++){
      box(c,d(14),y,W-d(14),y+d(50),10,PANEL);
      String line=s.reports.get(i);
      drawWrapped(c,line,d(26),y+d(18),W-d(52),8,i==0?CREAM:MUTED);
      y+=d(58);
    }
  }

  void drawWrapped(Canvas c,String str,float x,float y,float maxWidth,float size,int color){
    String a=str,b="";p.setTextSize(d(size));p.setTypeface(Typeface.create("sans",Typeface.NORMAL));
    if(p.measureText(a)>maxWidth){
      int cut=a.length();while(cut>8&&p.measureText(a.substring(0,cut))>maxWidth)cut--;
      int space=a.lastIndexOf(' ',cut);if(space>10)cut=space;
      b=a.substring(cut).trim();a=a.substring(0,cut).trim();
    }
    txt(c,a,x,y,size,color,false);if(!b.isEmpty())txt(c,b,x,y+d(16),size,color,false);
  }

  void drawBottom(Canvas c){
    float y=H-d(66);box(c,d(8),y,W-d(8),H-d(8),15,Color.rgb(18,30,24));
    float cw=(W-d(16))/5f;
    for(int i=0;i<5;i++){
      float x=d(8)+cw*i;if(i==screen)box(c,x+d(3),y+d(6),x+cw-d(3),H-d(14),11,PANEL3);
      txt(c,nav[i],x+d(8),y+d(34),7,i==screen?GOLD:MUTED,true);
      if(i==3&&s.incomingCount()>0){box(c,x+cw-d(20),y+d(8),x+cw-d(7),y+d(21),7,RED);txt(c,""+s.incomingCount(),x+cw-d(16),y+d(19),7,Color.WHITE,true);}
    }
  }

  void drawPause(Canvas c){
    p.setColor(Color.argb(185,5,9,7));c.drawRect(0,0,W,H,p);
    float w=W-d(48),x=d(24),y=H/2f-d(150);
    box(c,x,y,x+w,y+d(300),20,Color.rgb(24,35,29));
    drawSword(c,W/2f,y+d(54),d(27),GOLD);
    txt(c,"REALM PAUSED",W/2f-d(62),y+d(104),17,CREAM,true);
    txt(c,"Resources, bots and armies are frozen.",W/2f-d(102),y+d(126),9,MUTED,false);
    box(c,x+d(22),y+d(150),x+w-d(22),y+d(196),12,GOLD);txt(c,"RESUME",x+d(45),y+d(180),11,BG,true);
    box(c,x+d(22),y+d(208),x+w-d(22),y+d(250),12,PANEL3);txt(c,"SAVE & MAIN MENU",x+d(45),y+d(235),10,CREAM,true);
    txt(c,"Tap outside buttons to stay paused.",x+d(36),y+d(278),8,MUTED,false);
  }

  void showBuild(String key){
    int lv=s.level(key);int[] cost=s.buildCost(key);String req=s.requirement(key);
    String msg="Current level: "+lv+"\nNext level: "+(lv+1)+"\n\nCost: "+cost[0]+" wood • "+cost[1]+" clay • "+cost[2]+" iron • "+cost[3]+" grain";
    if(!req.isEmpty())msg+="\n\nLocked: "+req;
    else if(!s.buildKey.isEmpty())msg+="\n\nBuild queue occupied by "+s.label(s.buildKey)+".";
    new AlertDialog.Builder(getContext()).setTitle(s.label(key)).setMessage(msg).setNegativeButton("Close",null)
      .setPositiveButton("Upgrade",(dialog,which)->{
        if(s.queueBuild(key))toast("Construction queued");
        else if(!s.buildKey.isEmpty())toast("Build queue is busy");
        else if(!req.isEmpty())toast(req);
        else toast("Not enough resources");
      }).show();
  }

  void showTile(int wx,int wy){
    int t=s.tileType(wx,wy),bi=s.botAt(wx,wy);
    if(bi>=0){
      Bot b=s.bots.get(bi);
      String msg="Coordinates ("+wx+"|"+wy+")\nWarlord level "+b.level+"\nEstimated garrison: "+b.infantry+" infantry, "+b.cavalry+" cavalry\nTravel: "+s.realEta(s.travelSeconds(wx,wy));
      String[] opts={"Raid with 25%","Raid with 50%","Raid with all available troops"};
      new AlertDialog.Builder(getContext()).setTitle(b.name).setMessage(msg).setItems(opts,(dialog,which)->{
        int pct=which==0?25:which==1?50:100;
        if(!s.launchRaid(bi,pct))toast("You need available troops");
        else toast("Raid launched");
      }).setNegativeButton("Close",null).show();
    }else if(t==1||t==2){
      String msg="Coordinates ("+wx+"|"+wy+")\nWild oasis. Annexed oases grant +10% production each.\nHero Lodge level: "+s.mansion+"\nControlled oases: "+s.oases.size()+"/"+Math.min(3,s.mansion);
      new AlertDialog.Builder(getContext()).setTitle(s.tileName(wx,wy)).setMessage(msg).setNegativeButton("Close",null)
        .setPositiveButton("Annex",(dialog,which)->{if(s.annexOasis(wx,wy))toast("Oasis annexed");else toast("Build/upgrade the Hero Lodge or free an oasis slot");}).show();
    }else if(t==3){
      boolean done=s.explored.contains(wx+"|"+wy);
      new AlertDialog.Builder(getContext()).setTitle("Ancient Ruins").setMessage("Coordinates ("+wx+"|"+wy+")\n"+(done?"Already explored.":"Send Astra to search for resources and experience."))
        .setNegativeButton("Close",null).setPositiveButton(done?"Done":"Explore",(dialog,which)->{if(!done&&s.exploreRuin(wx,wy))toast("Ruins explored");}).show();
    }else{
      new AlertDialog.Builder(getContext()).setTitle("Open Valley").setMessage("Coordinates ("+wx+"|"+wy+")\nUnoccupied land. Future settlement territory.").setPositiveButton("Close",null).show();
    }
  }

  void train(String type,int count){
    if(s.queueTrain(type,count))toast("Training started");
    else if(!s.trainType.isEmpty())toast("Training queue is busy");
    else if(s.barracks<1)toast("Build a Barracks first");
    else if(type.equals("scouts")&&s.academy<1)toast("Requires Academy level 1");
    else if(type.equals("cavalry")&&(s.barracks<3||s.academy<2))toast("Requires Barracks 3 and Academy 2");
    else toast("Not enough resources");
  }

  void toast(String str){Toast.makeText(getContext(),str,Toast.LENGTH_SHORT).show();}

  void confirmNewRealm(){
    if(!s.started){s.newRealm();menuScreen=false;screen=0;return;}
    new AlertDialog.Builder(getContext()).setTitle("Start a fresh realm?").setMessage("This permanently replaces the current local save.")
      .setNegativeButton("Cancel",null).setPositiveButton("Start New",(d,w)->{s.newRealm();menuScreen=false;screen=0;invalidate();}).show();
  }

  @Override public boolean onTouchEvent(MotionEvent e){
    if(e.getAction()==MotionEvent.ACTION_DOWN){downX=e.getX();downY=e.getY();return true;}
    if(e.getAction()!=MotionEvent.ACTION_UP)return true;
    float x=e.getX(),y=e.getY();

    if(menuScreen){
      float cy=d(95),by=cy+d(154)+d(140);
      if(y>=by&&y<=by+d(54)){
        if(s.started){menuScreen=false;s.paused=false;s.lastReal=System.currentTimeMillis();s.save();}
        else{s.newRealm();menuScreen=false;}
        invalidate();return true;
      }
      if(s.started&&y>=by+d(68)&&y<=by+d(116)){confirmNewRealm();return true;}
      return true;
    }

    if(s.paused){
      float py=H/2f-d(150);
      if(y>=py+d(150)&&y<=py+d(196)){s.paused=false;s.lastReal=System.currentTimeMillis();s.save();invalidate();return true;}
      if(y>=py+d(208)&&y<=py+d(250)){s.paused=true;s.save();menuScreen=true;invalidate();return true;}
      return true;
    }

    if(y<d(39)&&x>W-d(70)){s.paused=true;s.save();invalidate();return true;}
    if(y>H-d(72)){
      screen=Math.max(0,Math.min(4,(int)((x-d(8))/((W-d(16))/5f))));
      invalidate();return true;
    }

    if(screen==0){
      float fy=contentTop()+d(56),gap=d(10),cw=(W-d(38))/2f,ch=d(112);
      if(y>=fy&&y<=fy+ch){showBuild(x<W/2?"wood":"clay");return true;}
      if(y>=fy+ch+gap&&y<=fy+ch*2+gap){showBuild(x<W/2?"iron":"crop");return true;}
    }else if(screen==1){
      float top=contentTop()+d(55),gap=d(8),cw=(W-d(36))/2f,ch=d(62);
      int row=(int)((y-top)/(ch+gap));int col=x<W/2?0:1;int idx=row*2+col;
      if(row>=0&&row<5&&idx>=0&&idx<buildKeys.length){showBuild(buildKeys[idx]);return true;}
    }else if(screen==2){
      float topButtons=contentTop()+d(52);
      if(y>=topButtons&&y<=topButtons+d(34)){
        if(x<d(54))mapX-=4;else if(x<d(102))mapY+=4;else if(x>W-d(54))mapX+=4;else if(x>W-d(102))mapY-=4;
        mapX=Math.max(-30,Math.min(30,mapX));mapY=Math.max(-30,Math.min(30,mapY));invalidate();return true;
      }
      float top=topButtons+d(44);float cell=Math.min((W-d(20))/9f,(contentBottom()-top-d(12))/9f);float left=(W-cell*9)/2f;
      if(x>=left&&x<left+cell*9&&y>=top&&y<top+cell*9){
        int col=(int)((x-left)/cell),row=(int)((y-top)/cell);
        int wx=mapX+col-4,wy=mapY+4-row;showTile(wx,wy);return true;
      }
      float dx=x-downX,dy=y-downY;
      if(Math.abs(dx)>d(35)||Math.abs(dy)>d(35)){
        if(Math.abs(dx)>Math.abs(dy))mapX+=dx>0?-2:2;else mapY+=dy>0?2:-2;
        mapX=Math.max(-30,Math.min(30,mapX));mapY=Math.max(-30,Math.min(30,mapY));invalidate();
      }
    }else if(screen==3){
      float ty=contentTop()+d(55)+d(92)+d(70);
      float tw=(W-d(44))/3f;
      if(y>=ty&&y<=ty+d(48)){
        if(x<d(18)+tw)train("infantry",5);else if(x<d(26)+tw*2)train("scouts",2);else train("cavalry",2);
      }
    }
    return true;
  }
}
