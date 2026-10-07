package com.ironvale.game;

import android.content.Context;
import android.graphics.*;
import android.os.*;
import android.view.*;
import java.util.*;

class GameView extends View {
  static class Shot {float x,y,vx,vy,r;boolean enemy;Shot(float a,float b,float c,float d,float e,boolean f){x=a;y=b;vx=c;vy=d;r=e;enemy=f;}}
  static class Enemy {float x,y,bx,by,phase,scale;int hp,max,type;boolean dive;float vx,vy;Enemy(float a,float b,int h,int t,float ph,float sc){x=bx=a;y=by=b;hp=max=h;type=t;phase=ph;scale=sc;}}
  static class Drop {float x,y,vy;int type;Drop(float a,float b,float c,int t){x=a;y=b;vy=c;type=t;}}
  static class Spark {float x,y,vx,vy,life,max;int col;Spark(float a,float b,float c,float d,float e,int f){x=a;y=b;vx=c;vy=d;life=max=e;col=f;}}
  static class Star {float x,y,speed,size;Star(float a,float b,float c,float d){x=a;y=b;speed=c;size=d;}}

  final Paint p=new Paint(3), stroke=new Paint(3);
  final Handler h=new Handler(Looper.getMainLooper());
  final Random rnd=new Random();
  final ArrayList<Shot> shots=new ArrayList<>();
  final ArrayList<Enemy> enemies=new ArrayList<>();
  final ArrayList<Drop> drops=new ArrayList<>();
  final ArrayList<Spark> sparks=new ArrayList<>();
  final ArrayList<Star> stars=new ArrayList<>();
  final ProgressData prog;
  final SoundEngine sound;

  int W,H,mode=0,sector=0,selectedLevel=1,lives=3,weapon=1,bombs=2,score=0,missionWave=1,resultStars=0,resultReward=0;
  float px,py,pr,fire=0,enemyFire=0,waveDelay=0,banner=0,inv=0,shield=0,touchX,touchY,tutorial=0;
  boolean touching=false,paused=false,systemPaused=false;
  long last=0;
  final int SPACE=Color.rgb(5,8,18),WHITE=Color.rgb(245,247,250),CYAN=Color.rgb(79,205,226),BLUE=Color.rgb(78,117,220),RED=Color.rgb(222,71,71),ORANGE=Color.rgb(244,147,62),YELLOW=Color.rgb(250,213,77),GREEN=Color.rgb(100,208,112);
  final int PANEL=Color.argb(220,18,26,46),PANEL2=Color.argb(235,31,42,68);

  final Runnable loop=new Runnable(){public void run(){long n=System.nanoTime();if(last==0)last=n;float dt=Math.min(.034f,(n-last)/1e9f);last=n;if(mode==2&&!paused&&!systemPaused)update(dt);invalidate();h.postDelayed(this,16);}};

  GameView(Context c){
    super(c);setFocusable(true);GameSprites.build();prog=new ProgressData(c);sound=new SoundEngine(c,prog.sfx,prog.music);h.post(loop);
  }
  void dispose(){h.removeCallbacks(loop);prog.save();sound.release();}
  void pauseFromSystem(){if(mode==2){systemPaused=true;paused=true;}sound.pauseMusic();prog.save();}
  boolean handleBack(){if(mode==2&&!paused){paused=true;return true;}if(mode==2&&paused){mode=1;paused=false;systemPaused=false;return true;}if(mode!=0){mode=0;return true;}return false;}
  float d(float v){return v*getResources().getDisplayMetrics().density;}

  @Override protected void onSizeChanged(int w,int h0,int ow,int oh){W=w;H=h0;px=W*.5f;py=H*.82f;pr=Math.max(d(15),Math.min(W,H)*.035f);stars.clear();for(int i=0;i<100;i++)stars.add(new Star(rnd.nextFloat()*W,rnd.nextFloat()*H,18+rnd.nextFloat()*100,.5f+rnd.nextFloat()*1.8f));}
  @Override protected void onDraw(Canvas c){c.drawColor(SPACE);background(c);if(mode==0)menu(c);else if(mode==1)campaign(c);else if(mode==2){game(c);if(paused)pause(c);}else if(mode==3)result(c);else if(mode==4)armory(c);else if(mode==5)achievements(c);else settings(c);}

  void background(Canvas c){for(Star s:stars){p.setColor(Color.argb((int)(100+Math.min(150,s.speed)),210,225,255));c.drawCircle(s.x,s.y,d(s.size),p);}}
  void t(Canvas c,String s,float x,float y,float sz,int col,boolean bold,boolean center){p.setColor(col);p.setTextSize(d(sz));p.setTypeface(Typeface.create("sans",bold?Typeface.BOLD:Typeface.NORMAL));p.setTextAlign(center?Paint.Align.CENTER:Paint.Align.LEFT);c.drawText(s,x,y,p);p.setTextAlign(Paint.Align.LEFT);}
  void box(Canvas c,float l,float top,float r,float b,float rad,int col){p.setColor(col);c.drawRoundRect(l,top,r,b,d(rad),d(rad),p);}
  void sprite(Canvas c,Bitmap b,float x,float y,float size){c.drawBitmap(b,null,new RectF(x-size/2,y-size/2,x+size/2,y+size/2),p);}
  void click(){sound.click();}

  void menu(Canvas c){
    float cx=W/2f,top=d(25);sprite(c,GameSprites.chicken0,cx,top+d(60),d(94));t(c,"FEATHER FORCE",cx,top+d(132),27,WHITE,true,true);t(c,"GALACTIC CAMPAIGN",cx,top+d(157),10,CYAN,true,true);
    float bw=Math.min(d(290),W*.55f),y=top+d(184);
    button(c,cx-bw/2,y,bw,d(46),"PLAY CAMPAIGN",BLUE);
    button(c,cx-bw/2,y+d(56),bw,d(40),"ARMORY  •  "+prog.coins+" C",Color.rgb(50,68,106));
    button(c,cx-bw/2,y+d(104),bw,d(40),"ACHIEVEMENTS",Color.rgb(50,68,106));
    button(c,cx-bw/2,y+d(152),bw,d(40),"SETTINGS",Color.rgb(50,68,106));
    t(c,"30 missions • bosses • upgrades • stars • offline",cx,H-d(18),7,Color.rgb(145,164,202),false,true);
  }
  void button(Canvas c,float x,float y,float w,float hh,String s,int col){box(c,x,y,x+w,y+hh,11,col);t(c,s,x+w/2,y+hh*.63f,9,WHITE,true,true);}

  void campaign(Canvas c){
    header(c,"CAMPAIGN",Campaign.SECTORS[sector]);
    float cx=W/2f,y=d(74);t(c,"Stars "+prog.totalStars()+" / 90    Coins "+prog.coins,cx,y,8,Color.rgb(180,198,230),true,true);
    float startX=W*.18f,endX=W*.82f,step=(endX-startX)/4f,ny=H*.48f;
    for(int i=0;i<5;i++){int level=sector*5+i+1;float x=startX+i*step;boolean open=level<=prog.unlocked;int st=prog.stars[level-1];p.setColor(open?Color.rgb(38,83,145):Color.rgb(45,50,67));c.drawCircle(x,ny,d(28),p);stroke.setStyle(Paint.Style.STROKE);stroke.setStrokeWidth(d(3));stroke.setColor(open?CYAN:Color.rgb(88,94,111));c.drawCircle(x,ny,d(28),stroke);t(c,""+level,x,ny+d(5),12,open?WHITE:Color.rgb(120,126,142),true,true);t(c,starsText(st),x,ny+d(48),10,st>0?YELLOW:Color.rgb(85,94,117),true,true);if(i<4){stroke.setStrokeWidth(d(3));stroke.setColor(Color.rgb(67,89,128));c.drawLine(x+d(31),ny,x+step-d(31),ny,stroke);}}
    int first=sector*5+1,last=first+4;t(c,Campaign.LEVELS[first-1].name,startX,ny-d(47),7,WHITE,true,true);t(c,Campaign.LEVELS[last-1].name,endX,ny-d(47),7,WHITE,true,true);
    button(c,d(18),H-d(54),d(95),d(36),"‹ SECTOR",Color.rgb(45,59,88));button(c,W-d(113),H-d(54),d(95),d(36),"SECTOR ›",Color.rgb(45,59,88));button(c,W/2-d(55),H-d(54),d(110),d(36),"BACK",Color.rgb(45,59,88));
  }
  String starsText(int n){return n==0?"☆☆☆":n==1?"★☆☆":n==2?"★★☆":"★★★";}
  void header(Canvas c,String a,String b){t(c,a,d(18),d(28),16,WHITE,true,false);t(c,b,W-d(18),d(28),9,CYAN,true,false);}

  void armory(Canvas c){
    header(c,"ARMORY","Coins "+prog.coins);float y=d(72);upgradeCard(c,y,"BLASTER CORE","Start missions with stronger weapon power.",prog.blaster,5,"blaster");y+=d(82);upgradeCard(c,y,"SHIELD MATRIX","Power-up shields last longer.",prog.shield,5,"shield");y+=d(82);upgradeCard(c,y,"NOVA RACK","Carry more screen-clearing Nova charges.",prog.nova,3,"nova");button(c,W/2-d(55),H-d(52),d(110),d(35),"BACK",Color.rgb(45,59,88));
  }
  void upgradeCard(Canvas c,float y,String name,String sub,int lv,int max,String key){float x=d(35),w=W-d(70);box(c,x,y,x+w,y+d(67),12,PANEL);t(c,name,x+d(18),y+d(24),10,WHITE,true,false);t(c,sub,x+d(18),y+d(44),7,Color.rgb(174,190,220),false,false);t(c,"LV "+lv+"/"+max,x+w-d(120),y+d(25),9,CYAN,true,false);if(lv<max)t(c,"BUY "+prog.cost(key),x+w-d(120),y+d(49),8,YELLOW,true,false);else t(c,"MAXED",x+w-d(120),y+d(49),8,GREEN,true,false);}

  void achievements(Canvas c){
    header(c,"ACHIEVEMENTS",prog.totalStars()+" stars");String[] n={"First Flight","Flock Breaker","Boss Buster","Star Collector","Galactic Ace"};String[] dsc={"Complete mission 1","Destroy 100 enemies","Defeat any boss","Earn 30 stars","Complete mission 30"};boolean[] ok={prog.stars[0]>0,prog.totalKills>=100,prog.bossKills>0,prog.totalStars()>=30,prog.stars[29]>0};float y=d(62);for(int i=0;i<n.length;i++){box(c,d(28),y,W-d(28),y+d(52),10,ok[i]?Color.argb(220,32,72,62):PANEL);t(c,ok[i]?"✓":"○",d(46),y+d(33),16,ok[i]?GREEN:Color.rgb(130,145,175),true,false);t(c,n[i],d(78),y+d(22),9,WHITE,true,false);t(c,dsc[i],d(78),y+d(39),7,Color.rgb(173,189,220),false,false);y+=d(60);}button(c,W/2-d(55),H-d(48),d(110),d(34),"BACK",Color.rgb(45,59,88));
  }
  void settings(Canvas c){
    header(c,"SETTINGS","Local only");float y=d(90);settingRow(c,y,"MUSIC",prog.music);y+=d(70);settingRow(c,y,"SOUND EFFECTS",prog.sfx);y+=d(70);box(c,d(35),y,W-d(35),y+d(62),11,PANEL);t(c,"CONTROLS",d(55),y+d(24),9,WHITE,true,false);t(c,"Drag anywhere to move • Auto-fire • Tap NOVA button",d(55),y+d(44),7,Color.rgb(175,191,221),false,false);button(c,W/2-d(55),H-d(50),d(110),d(35),"BACK",Color.rgb(45,59,88));
  }
  void settingRow(Canvas c,float y,String name,boolean on){box(c,d(35),y,W-d(35),y+d(52),11,PANEL);t(c,name,d(55),y+d(32),9,WHITE,true,false);t(c,on?"ON":"OFF",W-d(78),y+d(32),9,on?GREEN:RED,true,false);}

  void startMission(int lv){
    selectedLevel=lv;mode=2;paused=false;systemPaused=false;score=0;lives=3;weapon=1+prog.blaster;bombs=2+prog.nova;shield=prog.shield*1.2f;missionWave=1;tutorial=lv==1?5f:0;shots.clear();enemies.clear();drops.clear();sparks.clear();px=W*.5f;py=H*.82f;fire=0;enemyFire=0;waveDelay=0;banner=2;spawnWave();sound.resumeMusic();
  }
  Campaign.Level level(){return Campaign.LEVELS[selectedLevel-1];}

  void spawnWave(){
    enemies.clear();shots.removeIf(s->s.enemy);banner=1.7f;boolean boss=level().boss&&missionWave==level().waves;
    if(boss){enemies.add(new Enemy(W*.5f,d(115),95+selectedLevel*8,9,0,2.1f));sound.boss();return;}
    int rows=Math.min(4,2+(missionWave+selectedLevel/5)/2),cols=Math.min(9,6+selectedLevel/6);float sx=Math.min(d(60),(W-d(130))/(float)Math.max(1,cols-1)),sy=d(50),start=W*.5f-sx*(cols-1)/2f;
    for(int r=0;r<rows;r++)for(int j=0;j<cols;j++){int type=(r+j+selectedLevel)%4;int hp=1+level().difficulty/4+(type==3?1:0);enemies.add(new Enemy(start+j*sx,d(83)+r*sy,hp,type,r*.8f+j*.43f,1));}
  }

  void update(float dt){
    for(Star s:stars){s.y+=s.speed*dt;if(s.y>H){s.y=0;s.x=rnd.nextFloat()*W;}}
    if(tutorial>0)tutorial-=dt;if(banner>0)banner-=dt;if(inv>0)inv-=dt;if(shield>0)shield-=dt;
    if(touching){float dx=touchX-px,dy=touchY-py,dist=(float)Math.sqrt(dx*dx+dy*dy),step=Math.min(dist,d(500)*dt);if(dist>1){px+=dx/dist*step;py+=dy/dist*step;}}
    px=Math.max(pr+d(5),Math.min(W-pr-d(5),px));py=Math.max(d(55)+pr,Math.min(H-pr-d(8),py));
    fire-=dt;if(fire<=0){fire=Math.max(.055f,.13f-weapon*.011f);firePlayer();sound.laser();}
    enemyFire-=dt;if(enemyFire<=0){enemyFire=Math.max(.20f,1.05f-level().difficulty*.045f);enemyFire();}
    float tm=System.nanoTime()/1e9f;for(Enemy e:enemies){if(e.type==9){e.x=W*.5f+(float)Math.sin(tm*.8)*W*.30f;e.y=d(105)+(float)Math.sin(tm*1.4)*d(17);}else if(e.dive){e.x+=e.vx*dt;e.y+=e.vy*dt;if(e.y>H+d(60)){e.dive=false;e.x=e.bx;e.y=e.by;}}else{e.x=e.bx+(float)Math.sin(tm*.85+e.phase)*d(16);e.y=e.by+(float)Math.sin(tm*1.2+e.phase)*d(5);if(selectedLevel>3&&rnd.nextFloat()<dt*.012f*level().difficulty){e.dive=true;float dx=px-e.x,dy=py-e.y,dd=(float)Math.sqrt(dx*dx+dy*dy);e.vx=dx/Math.max(1,dd)*d(150);e.vy=Math.abs(dy/Math.max(1,dd))*d(190)+d(90);}}}
    for(Shot s:shots){s.x+=s.vx*dt;s.y+=s.vy*dt;}for(Drop q:drops)q.y+=q.vy*dt;for(Spark q:sparks){q.x+=q.vx*dt;q.y+=q.vy*dt;q.life-=dt;}
    collisions();shots.removeIf(s->s.y<-d(40)||s.y>H+d(40)||s.x<-d(40)||s.x>W+d(40));drops.removeIf(q->q.y>H+d(40));sparks.removeIf(q->q.life<=0);
    if(enemies.isEmpty()){waveDelay+=dt;if(waveDelay>1.35f){waveDelay=0;if(missionWave<level().waves){missionWave++;spawnWave();}else finishMission();}}else waveDelay=0;
  }

  void firePlayer(){float y=py-pr*.9f;shots.add(new Shot(px,y,0,-d(650),d(3),false));if(weapon>=2){shots.add(new Shot(px-d(9),y,-d(28),-d(640),d(3),false));shots.add(new Shot(px+d(9),y,d(28),-d(640),d(3),false));}if(weapon>=4){shots.add(new Shot(px-d(14),y,-d(95),-d(610),d(2.8f),false));shots.add(new Shot(px+d(14),y,d(95),-d(610),d(2.8f),false));}}
  void enemyFire(){if(enemies.isEmpty())return;Enemy e=enemies.get(rnd.nextInt(enemies.size()));float dx=px-e.x,dy=py-e.y,dd=(float)Math.sqrt(dx*dx+dy*dy),sp=d(150+level().difficulty*6);shots.add(new Shot(e.x,e.y+d(12),dx/Math.max(1,dd)*sp*.45f,Math.max(d(100),dy/Math.max(1,dd)*sp),d(e.type==9?7:5),true));}
  void collisions(){
    for(Iterator<Shot> it=shots.iterator();it.hasNext();){Shot b=it.next();if(!b.enemy){Enemy hit=null;for(Enemy e:enemies){float rr=d(e.type==9?31:17)*e.scale+b.r,dx=b.x-e.x,dy=b.y-e.y;if(dx*dx+dy*dy<rr*rr){hit=e;break;}}if(hit!=null){hit.hp--;it.remove();sound.hit();if(hit.hp<=0)kill(hit);continue;}}else if(inv<=0){float dx=b.x-px,dy=b.y-py,rr=pr*.7f+b.r;if(dx*dx+dy*dy<rr*rr){it.remove();if(shield>0){shield=Math.max(0,shield-2);sound.shield();}else playerHit();}}}
    for(Iterator<Drop> it=drops.iterator();it.hasNext();){Drop q=it.next();float dx=q.x-px,dy=q.y-py;if(dx*dx+dy*dy<d(27)*d(27)){if(q.type==0)weapon=Math.min(6,weapon+1);else if(q.type==1)shield=9+prog.shield*2;else if(q.type==2)bombs=Math.min(7,bombs+1);else {prog.coins+=25;prog.save();}sound.pickup();it.remove();}}
  }
  void kill(Enemy e){enemies.remove(e);prog.totalKills++;int val=e.type==9?4500:100+e.type*40;score+=val+selectedLevel*12;burst(e.x,e.y,e.type==9?30:12,e.type==9?ORANGE:YELLOW);sound.explode();if(e.type==9){prog.bossKills++;}if(rnd.nextFloat()<(e.type==9?.8f:.13f))drops.add(new Drop(e.x,e.y,d(95),e.type==9?0:rnd.nextInt(4)));if(rnd.nextFloat()<.16f)sound.cluck();}
  void playerHit(){lives--;weapon=Math.max(1+prog.blaster,weapon-1);inv=1.5f;burst(px,py,24,RED);sound.explode();if(lives<=0){resultStars=0;resultReward=Math.max(50,score/25);prog.coins+=resultReward;prog.save();mode=3;touching=false;}}
  void burst(float x,float y,int n,int col){for(int i=0;i<n;i++){float a=rnd.nextFloat()*6.28f,sp=d(40+rnd.nextFloat()*130);sparks.add(new Spark(x,y,(float)Math.cos(a)*sp,(float)Math.sin(a)*sp,.3f+rnd.nextFloat()*.35f,col));}}

  void finishMission(){int target=3500+selectedLevel*650;resultStars=1+(lives>=2?1:0)+(score>=target?1:0);resultReward=350+selectedLevel*70+resultStars*150;prog.finish(selectedLevel,resultStars,resultReward,level().boss);sound.victory();mode=3;touching=false;}

  void game(Canvas c){
    for(Enemy e:enemies)drawEnemy(c,e);for(Shot s:shots)drawShot(c,s);for(Drop q:drops){sprite(c,GameSprites.power(q.type),q.x,q.y,d(35));}for(Spark q:sparks){p.setColor(Color.argb((int)(255*Math.max(0,q.life/q.max)),Color.red(q.col),Color.green(q.col),Color.blue(q.col)));c.drawCircle(q.x,q.y,d(2),p);}drawPlayer(c);hud(c);
    if(banner>0){p.setColor(Color.argb(155,8,13,28));c.drawRect(0,H*.42f,W,H*.59f,p);t(c,level().boss&&missionWave==level().waves?"BOSS WAVE":"WAVE "+missionWave,W*.5f,H*.50f,22,WHITE,true,true);t(c,Campaign.LEVELS[selectedLevel-1].name,W*.5f,H*.55f,8,CYAN,true,true);}
    if(tutorial>0){box(c,d(60),H-d(88),W-d(60),H-d(24),12,Color.argb(225,20,29,49));t(c,"DRAG TO MOVE • AUTO-FIRE • TAP NOVA TO CLEAR ENEMY SHOTS",W*.5f,H-d(56),9,WHITE,true,true);t(c,"Collect glowing power-ups. Survive all waves to earn up to 3 stars.",W*.5f,H-d(35),7,CYAN,false,true);}
  }
  void drawEnemy(Canvas c,Enemy e){Bitmap b=e.type==9?GameSprites.boss:GameSprites.chicken(e.type);sprite(c,b,e.x,e.y,d(e.type==9?92:48)*e.scale);if(e.type==9){float hp=e.hp/(float)e.max;box(c,e.x-d(55),e.y+d(48),e.x+d(55),e.y+d(54),3,Color.rgb(47,56,75));box(c,e.x-d(55),e.y+d(48),e.x-d(55)+d(110)*hp,e.y+d(54),3,RED);}}
  void drawShot(Canvas c,Shot s){if(s.enemy){p.setColor(WHITE);c.drawOval(new RectF(s.x-s.r,s.y-s.r*1.3f,s.x+s.r,s.y+s.r*1.3f),p);p.setColor(Color.rgb(226,211,166));c.drawCircle(s.x,s.y,s.r*.5f,p);}else{p.setColor(CYAN);c.drawRoundRect(new RectF(s.x-s.r*.6f,s.y-s.r*2.4f,s.x+s.r*.6f,s.y+s.r*2.4f),s.r,s.r,p);}}
  void drawPlayer(Canvas c){sprite(c,GameSprites.ship,px,py,pr*3.1f);if(shield>0){stroke.setStyle(Paint.Style.STROKE);stroke.setStrokeWidth(d(2));stroke.setColor(CYAN);c.drawCircle(px,py,pr*1.35f,stroke);}}
  void hud(Canvas c){box(c,d(8),d(8),d(185),d(42),10,PANEL);t(c,"SCORE "+score,d(18),d(30),9,WHITE,true,false);box(c,W*.5f-d(95),d(8),W*.5f+d(95),d(42),10,PANEL);t(c,"MISSION "+selectedLevel+"  •  "+missionWave+"/"+level().waves,W*.5f,d(30),9,CYAN,true,true);box(c,W-d(192),d(8),W-d(8),d(42),10,PANEL);t(c,"♥ "+lives+"   POWER "+weapon+"   NOVA "+bombs,W-d(180),d(30),8,WHITE,true,false);box(c,W-d(50),H-d(52),W-d(10),H-d(12),20,Color.argb(230,57,70,107));t(c,"N",W-d(30),H-d(27),12,WHITE,true,true);box(c,W-d(48),d(47),W-d(10),d(80),9,PANEL2);t(c,"Ⅱ",W-d(29),d(70),13,WHITE,true,true);}

  void pause(Canvas c){p.setColor(Color.argb(190,0,0,0));c.drawRect(0,0,W,H,p);float cx=W/2f,y=H*.27f,w=Math.min(d(370),W*.55f);box(c,cx-w/2,y,cx+w/2,y+d(170),15,PANEL2);t(c,"PAUSED",cx,y+d(40),18,WHITE,true,true);button(c,cx-d(120),y+d(72),d(240),d(38),"RESUME",BLUE);button(c,cx-d(120),y+d(118),d(240),d(34),"EXIT MISSION",Color.rgb(53,63,88));}

  void result(Canvas c){float cx=W/2f,y=d(55),w=Math.min(d(420),W*.62f);box(c,cx-w/2,y,cx+w/2,H-d(45),18,PANEL2);t(c,resultStars>0?"MISSION COMPLETE":"MISSION FAILED",cx,y+d(42),18,resultStars>0?GREEN:RED,true,true);t(c,starsText(resultStars),cx,y+d(82),22,YELLOW,true,true);t(c,"Score "+score+"   Reward "+resultReward+" coins",cx,y+d(112),9,WHITE,true,true);if(resultStars>0)t(c,"Mission "+Math.min(30,selectedLevel+1)+" unlocked",cx,y+d(137),8,CYAN,false,true);button(c,cx-d(125),y+d(160),d(250),d(38),resultStars>0&&selectedLevel<30?"NEXT MISSION":"RETRY",BLUE);button(c,cx-d(125),y+d(207),d(250),d(34),"CAMPAIGN MAP",Color.rgb(53,63,88));}

  void useNova(){if(bombs<=0)return;bombs--;shots.removeIf(s->s.enemy);for(Enemy e:new ArrayList<>(enemies)){e.hp-=e.type==9?16:99;if(e.hp<=0)kill(e);}shield=Math.max(shield,2.2f);sound.nova();}

  @Override public boolean onTouchEvent(MotionEvent e){float x=e.getX(),y=e.getY();if(e.getAction()==MotionEvent.ACTION_DOWN){
    if(mode==0){float top=d(25),yy=top+d(184),cx=W/2f,bw=Math.min(d(290),W*.55f);if(x>cx-bw/2&&x<cx+bw/2){if(y>yy&&y<yy+d(46)){click();mode=1;}else if(y>yy+d(56)&&y<yy+d(96)){click();mode=4;}else if(y>yy+d(104)&&y<yy+d(144)){click();mode=5;}else if(y>yy+d(152)&&y<yy+d(192)){click();mode=6;}}return true;}
    if(mode==1){float start=W*.18f,end=W*.82f,step=(end-start)/4f,ny=H*.48f;for(int i=0;i<5;i++){int lv=sector*5+i+1,floatDummy=0;float xx=start+i*step;if(lv<=prog.unlocked&&Math.hypot(x-xx,y-ny)<d(38)){click();startMission(lv);return true;}}if(y>H-d(60)){if(x<d(125)){sector=Math.max(0,sector-1);click();}else if(x>W-d(125)){sector=Math.min(5,sector+1);click();}else if(Math.abs(x-W/2)<d(80)){mode=0;click();}}return true;}
    if(mode==4){float yy=d(72);if(y>yy&&y<yy+d(67)){if(prog.buy("blaster",5))click();}else if(y>yy+d(82)&&y<yy+d(149)){if(prog.buy("shield",5))click();}else if(y>yy+d(164)&&y<yy+d(231)){if(prog.buy("nova",3))click();}else if(y>H-d(60)){mode=0;click();}return true;}
    if(mode==5){if(y>H-d(60)){mode=0;click();}return true;}
    if(mode==6){float yy=d(90);if(y>yy&&y<yy+d(52)){prog.music=!prog.music;prog.save();sound.setMusic(prog.music);click();}else if(y>yy+d(70)&&y<yy+d(122)){prog.sfx=!prog.sfx;prog.save();sound.setSfx(prog.sfx);click();}else if(y>H-d(60)){mode=0;click();}return true;}
    if(mode==3){float yy=d(55)+d(160),cx=W/2f;if(x>cx-d(130)&&x<cx+d(130)&&y>yy&&y<yy+d(42)){click();if(resultStars>0&&selectedLevel<30)startMission(selectedLevel+1);else startMission(selectedLevel);}else if(y>yy+d(47)&&y<yy+d(86)){mode=1;sector=(selectedLevel-1)/5;click();}return true;}
    if(mode==2){if(paused){float yy=H*.27f+d(72),cx=W/2f;if(y>yy&&y<yy+d(38)){paused=false;systemPaused=false;last=System.nanoTime();click();}else if(y>yy+d(46)&&y<yy+d(80)){mode=1;paused=false;click();}return true;}if(x>W-d(55)&&y<d(86)){paused=true;touching=false;click();return true;}if(x>W-d(60)&&y>H-d(65)){useNova();return true;}touching=true;touchX=x;touchY=y;return true;}
  }else if(e.getAction()==MotionEvent.ACTION_MOVE){if(mode==2&&!paused&&touching){touchX=x;touchY=y;}return true;}else if(e.getAction()==MotionEvent.ACTION_UP||e.getAction()==MotionEvent.ACTION_CANCEL){touching=false;return true;}return true;}
}
