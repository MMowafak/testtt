package com.ironvale.game;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.Random;

class GameView extends View {
  static class Shot {
    float x,y,vx,vy,r; int damage; boolean enemy;
    Shot(float x,float y,float vx,float vy,float r,int damage,boolean enemy){
      this.x=x;this.y=y;this.vx=vx;this.vy=vy;this.r=r;this.damage=damage;this.enemy=enemy;
    }
  }
  static class Enemy {
    float x,y,baseX,baseY,vx,vy,phase,scale;
    int hp,maxHp,type,row,col;
    boolean diving=false;
    Enemy(float x,float y,int hp,int type,int row,int col,float phase,float scale){
      this.x=x;this.y=y;this.baseX=x;this.baseY=y;this.hp=hp;this.maxHp=hp;this.type=type;this.row=row;this.col=col;this.phase=phase;this.scale=scale;
    }
  }
  static class Drop {
    float x,y,vy; int type;
    Drop(float x,float y,float vy,int type){this.x=x;this.y=y;this.vy=vy;this.type=type;}
  }
  static class Spark {
    float x,y,vx,vy,life,max; int color;
    Spark(float x,float y,float vx,float vy,float life,int color){this.x=x;this.y=y;this.vx=vx;this.vy=vy;this.life=life;this.max=life;this.color=color;}
  }
  static class Star {
    float x,y,speed,size;
    Star(float x,float y,float speed,float size){this.x=x;this.y=y;this.speed=speed;this.size=size;}
  }

  final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
  final Paint stroke=new Paint(Paint.ANTI_ALIAS_FLAG);
  final Handler handler=new Handler(Looper.getMainLooper());
  final Random rng=new Random();
  final ArrayList<Shot> shots=new ArrayList<>();
  final ArrayList<Enemy> enemies=new ArrayList<>();
  final ArrayList<Drop> drops=new ArrayList<>();
  final ArrayList<Spark> sparks=new ArrayList<>();
  final ArrayList<Star> stars=new ArrayList<>();
  final SharedPreferences prefs;
  final ToneGenerator tones;

  int W,H;
  int mode=0; // 0 menu, 1 game, 2 game over
  boolean paused=false,systemPaused=false;
  float px,py,playerR;
  int lives=3,weapon=1,bombs=2,score=0,wave=1,bestScore=0,bestWave=1;
  float shield=0;
  long lastNs=0;
  float fireClock=0,enemyFireClock=0,waveDelay=0,banner=0,invuln=0,shake=0;
  float touchX,touchY;
  boolean touching=false;

  final int SPACE=Color.rgb(5,8,18), SPACE2=Color.rgb(12,17,34), WHITE=Color.rgb(245,247,250);
  final int CYAN=Color.rgb(79,205,226), BLUE=Color.rgb(78,117,220), RED=Color.rgb(222,71,71);
  final int ORANGE=Color.rgb(244,147,62), YELLOW=Color.rgb(250,213,77), GREEN=Color.rgb(100,208,112);
  final int CHICKEN=Color.rgb(244,235,196), CHICKEN2=Color.rgb(225,209,159), BEAK=Color.rgb(241,157,51);
  final int PANEL=Color.argb(210,18,26,46), PANEL2=Color.argb(230,31,42,68);

  final Runnable loop=new Runnable(){
    public void run(){
      long now=System.nanoTime();
      if(lastNs==0)lastNs=now;
      float dt=Math.min(.034f,(now-lastNs)/1_000_000_000f);
      lastNs=now;
      if(mode==1&&!paused&&!systemPaused)update(dt);
      invalidate();
      handler.postDelayed(this,16);
    }
  };

  GameView(Context c){
    super(c);
    setFocusable(true);
    prefs=c.getSharedPreferences("feather_force_local",0);
    bestScore=prefs.getInt("bestScore",0);
    bestWave=prefs.getInt("bestWave",1);
    tones=new ToneGenerator(AudioManager.STREAM_MUSIC,32);
    handler.post(loop);
  }

  void dispose(){handler.removeCallbacks(loop);saveProgress();tones.release();}
  void pauseFromSystem(){if(mode==1){systemPaused=true;paused=true;}saveProgress();}
  boolean handleBack(){
    if(mode==1&&!paused){paused=true;return true;}
    if(mode==1&&paused){mode=0;paused=false;systemPaused=false;return true;}
    if(mode==2){mode=0;return true;}
    return false;
  }

  float d(float v){return v*getResources().getDisplayMetrics().density;}
  float topHud(){return d(10);}
  float bottomHud(){return H-d(10);}
  float playTop(){return d(52);}
  float playBottom(){return H-d(18);}

  void saveProgress(){
    if(score>bestScore)bestScore=score;
    if(wave>bestWave)bestWave=wave;
    prefs.edit().putInt("bestScore",bestScore).putInt("bestWave",bestWave).apply();
  }

  @Override protected void onSizeChanged(int w,int h,int oldw,int oldh){
    W=w;H=h;
    px=W*.5f;py=H*.82f;playerR=Math.max(d(16),Math.min(W,H)*.035f);
    stars.clear();
    for(int i=0;i<90;i++)stars.add(new Star(rng.nextFloat()*W,rng.nextFloat()*H,20+rng.nextFloat()*100,.7f+rng.nextFloat()*1.9f));
  }

  @Override protected void onDraw(Canvas c){
    super.onDraw(c);
    c.drawColor(SPACE);
    drawBackground(c);
    if(mode==0)drawMenu(c);
    else {
      drawGame(c);
      if(mode==2)drawGameOver(c);
      else if(paused)drawPause(c);
    }
  }

  void drawBackground(Canvas c){
    p.setStyle(Paint.Style.FILL);
    for(Star s:stars){
      int alpha=(int)(120+Math.min(135,s.speed));
      p.setColor(Color.argb(alpha,210,225,255));
      c.drawCircle(s.x,s.y,d(s.size),p);
    }
    p.setColor(Color.argb(25,88,118,190));
    c.drawCircle(W*.16f,H*.23f,d(95),p);
    p.setColor(Color.argb(18,200,90,180));
    c.drawCircle(W*.80f,H*.66f,d(120),p);
  }

  void drawMenu(Canvas c){
    float cx=W*.5f, top=Math.max(d(24),H*.08f);
    drawLogoChicken(c,cx,top+d(52),d(38));
    text(c,"FEATHER FORCE",cx,top+d(116),26,WHITE,true,true);
    text(c,"ARCADE DEFENSE",cx,top+d(142),10,CYAN,true,true);
    text(c,"Original offline arcade shooter inspired by classic chicken-wave games",cx,top+d(167),8,Color.rgb(164,181,214),false,true);

    float boxW=Math.min(W-d(70),d(420)), boxX=cx-boxW/2, y=top+d(190);
    round(c,boxX,y,boxX+boxW,y+d(88),16,PANEL);
    text(c,"DRAG TO MOVE • AUTO-FIRE",cx,y+d(27),10,WHITE,true,true);
    text(c,"Destroy waves, dodge eggs, collect upgrades, survive bosses.",cx,y+d(51),8,Color.rgb(188,202,230),false,true);
    text(c,"All progress and high scores stay on this device.",cx,y+d(70),8,Color.rgb(188,202,230),false,true);

    float bw=Math.min(d(260),W*.55f), bh=d(48), by=y+d(112);
    round(c,cx-bw/2,by,cx+bw/2,by+bh,14,BLUE);
    text(c,"START MISSION",cx,by+d(31),12,WHITE,true,true);

    float statY=by+d(78);
    text(c,"BEST SCORE  "+bestScore+"     BEST WAVE  "+bestWave,cx,statY,9,Color.rgb(174,191,223),true,true);
    text(c,"Safe-area layout • landscape • no login",cx,H-d(18),7,Color.rgb(120,139,176),false,true);
  }

  void drawLogoChicken(Canvas c,float x,float y,float s){
    p.setStyle(Paint.Style.FILL);
    p.setColor(CHICKEN);c.drawOval(new RectF(x-s*.72f,y-s*.45f,x+s*.72f,y+s*.45f),p);
    p.setColor(WHITE);c.drawCircle(x+s*.48f,y-s*.30f,s*.32f,p);
    p.setColor(RED);
    c.drawCircle(x+s*.36f,y-s*.60f,s*.12f,p);c.drawCircle(x+s*.52f,y-s*.65f,s*.11f,p);c.drawCircle(x+s*.65f,y-s*.58f,s*.10f,p);
    Path beak=new Path();beak.moveTo(x+s*.72f,y-s*.30f);beak.lineTo(x+s*1.02f,y-s*.18f);beak.lineTo(x+s*.72f,y-s*.06f);beak.close();p.setColor(BEAK);c.drawPath(beak,p);
    p.setColor(Color.BLACK);c.drawCircle(x+s*.55f,y-s*.32f,s*.045f,p);
    p.setColor(CHICKEN2);c.drawOval(new RectF(x-s*.78f,y-s*.26f,x-s*.36f,y+s*.18f),p);
  }

  void startGame(){
    mode=1;paused=false;systemPaused=false;
    score=0;wave=1;lives=3;weapon=1;bombs=2;shield=0;invuln=0;shake=0;
    shots.clear();enemies.clear();drops.clear();sparks.clear();
    px=W*.5f;py=H*.82f;
    fireClock=0;enemyFireClock=0;waveDelay=0;banner=2f;
    spawnWave();
  }

  void spawnWave(){
    enemies.clear();
    shots.removeIf(s->s.enemy);
    banner=2.1f;
    if(wave%5==0){
      float scale=2.2f+wave*.03f;
      Enemy boss=new Enemy(W*.5f,playTop()+d(90),80+wave*18,9,0,0,0,scale);
      enemies.add(boss);
      return;
    }
    int rows=Math.min(4,2+(wave-1)/3);
    int cols=Math.min(9,6+(wave-1)/2);
    float spacingX=Math.min(d(62),(W-d(120))/(float)Math.max(1,cols-1));
    float spacingY=d(53);
    float startX=W*.5f-spacingX*(cols-1)/2f;
    float startY=playTop()+d(55);
    for(int r=0;r<rows;r++){
      for(int col=0;col<cols;col++){
        int type=(wave+r+col)%4;
        int hp=1+(wave/4)+(type==3?1:0);
        float phase=r*.9f+col*.43f;
        Enemy e=new Enemy(startX+col*spacingX,startY+r*spacingY,hp,type,r,col,phase,1f);
        enemies.add(e);
      }
    }
  }

  void update(float dt){
    for(Star s:stars){
      s.y+=s.speed*dt;
      if(s.y>H){s.y=0;s.x=rng.nextFloat()*W;}
    }
    if(banner>0)banner-=dt;
    if(invuln>0)invuln-=dt;
    if(shield>0)shield-=dt;
    if(shake>0)shake=Math.max(0,shake-dt*4);

    if(touching){
      float speed=Math.max(d(460),W*.55f);
      float dx=touchX-px,dy=touchY-py;
      float dist=(float)Math.sqrt(dx*dx+dy*dy);
      if(dist>1){
        float step=Math.min(dist,speed*dt);
        px+=dx/dist*step;py+=dy/dist*step;
      }
    }
    float margin=playerR+d(8);
    px=Math.max(margin,Math.min(W-margin,px));
    py=Math.max(playTop()+margin,Math.min(playBottom()-margin,py));

    fireClock-=dt;
    if(fireClock<=0){
      fireClock=Math.max(.065f,.13f-weapon*.012f);
      firePlayer();
    }

    enemyFireClock-=dt;
    if(enemyFireClock<=0){
      enemyFireClock=Math.max(.17f,1.1f-wave*.045f);
      enemyFire();
    }

    float formationT=(float)(System.nanoTime()/1_000_000_000.0);
    for(Enemy e:enemies){
      if(e.type==9){
        e.x=W*.5f+(float)Math.sin(formationT*.9)*W*.30f;
        e.y=playTop()+d(88)+(float)Math.sin(formationT*1.5)*d(18);
      }else if(e.diving){
        e.x+=e.vx*dt;e.y+=e.vy*dt;
        if(e.y>playBottom()+d(70)){e.diving=false;e.x=e.baseX;e.y=e.baseY;}
      }else{
        float amp=d(18)+wave*d(.4f);
        e.x=e.baseX+(float)Math.sin(formationT*.8+e.phase)*amp;
        e.y=e.baseY+(float)Math.sin(formationT*1.2+e.phase)*d(5);
        if(wave>2&&rng.nextFloat()<dt*.018f*wave){
          e.diving=true;
          float dx=px-e.x,dy=py-e.y,dist=(float)Math.sqrt(dx*dx+dy*dy);
          e.vx=dx/Math.max(1,dist)*(d(120)+wave*d(6));
          e.vy=Math.abs(dy/Math.max(1,dist))*(d(150)+wave*d(7))+d(100);
        }
      }
    }

    for(Shot s:shots){s.x+=s.vx*dt;s.y+=s.vy*dt;}
    for(Drop d:drops){d.y+=d.vy*dt;}
    for(Spark s:sparks){s.x+=s.vx*dt;s.y+=s.vy*dt;s.vy+=d(40)*dt;s.life-=dt;}

    handleCollisions();

    for(Iterator<Shot> it=shots.iterator();it.hasNext();){
      Shot s=it.next();if(s.y<-d(40)||s.y>H+d(40)||s.x<-d(40)||s.x>W+d(40))it.remove();
    }
    for(Iterator<Drop> it=drops.iterator();it.hasNext();){Drop dr=it.next();if(dr.y>H+d(30))it.remove();}
    for(Iterator<Spark> it=sparks.iterator();it.hasNext();){if(it.next().life<=0)it.remove();}

    if(enemies.isEmpty()){
      waveDelay+=dt;
      if(waveDelay>1.8f){wave++;bestWave=Math.max(bestWave,wave);waveDelay=0;spawnWave();tones.startTone(ToneGenerator.TONE_PROP_ACK,120);}
    }else waveDelay=0;
  }

  void firePlayer(){
    float y=py-playerR*.85f;
    if(weapon<=1){
      shots.add(new Shot(px,y,0,-d(620),d(3),1,false));
    }else if(weapon==2){
      shots.add(new Shot(px-d(8),y,0,-d(650),d(3),1,false));
      shots.add(new Shot(px+d(8),y,0,-d(650),d(3),1,false));
    }else if(weapon==3){
      shots.add(new Shot(px,y,0,-d(690),d(3.2f),1,false));
      shots.add(new Shot(px-d(10),y,-d(45),-d(650),d(3),1,false));
      shots.add(new Shot(px+d(10),y,d(45),-d(650),d(3),1,false));
    }else{
      shots.add(new Shot(px,y,0,-d(720),d(3.4f),1,false));
      shots.add(new Shot(px-d(11),y,-d(70),-d(675),d(3.1f),1,false));
      shots.add(new Shot(px+d(11),y,d(70),-d(675),d(3.1f),1,false));
      shots.add(new Shot(px-d(15),y,-d(125),-d(620),d(2.8f),1,false));
      shots.add(new Shot(px+d(15),y,d(125),-d(620),d(2.8f),1,false));
    }
  }

  void enemyFire(){
    if(enemies.isEmpty())return;
    int count=Math.min(enemies.size(),wave%5==0?3:1+(wave/8));
    for(int i=0;i<count;i++){
      Enemy e=enemies.get(rng.nextInt(enemies.size()));
      float dx=px-e.x,dy=py-e.y;
      float dist=(float)Math.sqrt(dx*dx+dy*dy);
      float speed=d(165)+wave*d(4);
      if(e.type==9)speed*=1.18f;
      float vx=dx/Math.max(1,dist)*speed*.42f;
      float vy=Math.max(d(110),dy/Math.max(1,dist)*speed);
      shots.add(new Shot(e.x,e.y+d(16)*e.scale,vx,vy,d(e.type==9?7:5),1,true));
    }
  }

  void handleCollisions(){
    for(Iterator<Shot> sit=shots.iterator();sit.hasNext();){
      Shot b=sit.next();
      if(!b.enemy){
        Enemy hit=null;
        for(Enemy e:enemies){
          float er=d(e.type==9?30:17)*e.scale;
          float dx=b.x-e.x,dy=b.y-e.y;
          if(dx*dx+dy*dy<(er+b.r)*(er+b.r)){hit=e;break;}
        }
        if(hit!=null){
          hit.hp-=b.damage;sit.remove();sparkBurst(b.x,b.y,5,YELLOW);
          if(hit.hp<=0)killEnemy(hit);
          continue;
        }
      }else if(invuln<=0){
        float dx=b.x-px,dy=b.y-py,rr=playerR*.72f+b.r;
        if(dx*dx+dy*dy<rr*rr){
          sit.remove();
          if(shield>0){shield=Math.max(0,shield-2.5f);sparkBurst(px,py,8,CYAN);}
          else playerHit();
          continue;
        }
      }
    }

    for(Iterator<Enemy> it=enemies.iterator();it.hasNext();){
      Enemy e=it.next();
      if(invuln<=0){
        float er=d(e.type==9?34:18)*e.scale;
        float dx=e.x-px,dy=e.y-py,rr=er+playerR*.65f;
        if(dx*dx+dy*dy<rr*rr){
          if(e.type!=9){it.remove();score+=25;sparkBurst(e.x,e.y,12,ORANGE);}
          playerHit();
          break;
        }
      }
    }

    for(Iterator<Drop> it=drops.iterator();it.hasNext();){
      Drop dr=it.next();float dx=dr.x-px,dy=dr.y-py,rr=d(17)+playerR*.65f;
      if(dx*dx+dy*dy<rr*rr){applyDrop(dr.type);it.remove();}
    }
  }

  void killEnemy(Enemy e){
    enemies.remove(e);
    int base=e.type==9?5000:100+e.type*35;
    score+=base+wave*10;
    bestScore=Math.max(bestScore,score);
    sparkBurst(e.x,e.y,e.type==9?38:14,e.type==9?ORANGE:CHICKEN);
    if(e.type==9){
      bombs=Math.min(5,bombs+1);weapon=Math.min(5,weapon+1);
      tones.startTone(ToneGenerator.TONE_CDMA_HIGH_L,220);
    }else if(rng.nextFloat()<.12f){
      int t=rng.nextFloat()<.65f?0:(rng.nextBoolean()?1:2);
      drops.add(new Drop(e.x,e.y,d(95),t));
    }
  }

  void playerHit(){
    if(invuln>0)return;
    lives--;weapon=Math.max(1,weapon-1);invuln=1.6f;shake=1f;
    sparkBurst(px,py,28,RED);
    tones.startTone(ToneGenerator.TONE_SUP_ERROR,220);
    if(lives<=0){
      mode=2;paused=false;touching=false;saveProgress();
    }
  }

  void applyDrop(int type){
    if(type==0){weapon=Math.min(5,weapon+1);score+=250;}
    else if(type==1){shield=10f;score+=150;}
    else {bombs=Math.min(5,bombs+1);score+=150;}
    tones.startTone(ToneGenerator.TONE_PROP_BEEP2,100);
    sparkBurst(px,py,16,type==0?YELLOW:type==1?CYAN:ORANGE);
  }

  void useBomb(){
    if(bombs<=0||mode!=1||paused)return;
    bombs--;
    for(Iterator<Shot> it=shots.iterator();it.hasNext();)if(it.next().enemy)it.remove();
    for(Enemy e:new ArrayList<>(enemies)){
      e.hp-=e.type==9?18:99;
      sparkBurst(e.x,e.y,e.type==9?12:8,CYAN);
      if(e.hp<=0)killEnemy(e);
    }
    shield=Math.max(shield,2.5f);
    shake=1f;tones.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD,180);
  }

  void sparkBurst(float x,float y,int count,int col){
    for(int i=0;i<count;i++){
      float a=rng.nextFloat()*(float)Math.PI*2f,sp=d(35+rng.nextFloat()*150);
      sparks.add(new Spark(x,y,(float)Math.cos(a)*sp,(float)Math.sin(a)*sp,.25f+rng.nextFloat()*.45f,col));
    }
  }

  void drawGame(Canvas canvas){
    float sx=shake>0?(rng.nextFloat()-.5f)*d(8)*shake:0;
    float sy=shake>0?(rng.nextFloat()-.5f)*d(8)*shake:0;
    canvas.save();canvas.translate(sx,sy);

    for(Enemy e:enemies)drawEnemy(canvas,e);
    for(Shot s:shots)drawShot(canvas,s);
    for(Drop dr:drops)drawDrop(canvas,dr);
    for(Spark sp:sparks)drawSpark(canvas,sp);
    drawPlayer(canvas);

    canvas.restore();
    drawHud(canvas);
    if(banner>0){
      float alpha=Math.min(1,banner/.45f);p.setColor(Color.argb((int)(155*alpha),9,14,29));
      canvas.drawRect(0,H*.43f,W,H*.59f,p);
      text(canvas,wave%5==0?"BOSS WAVE "+wave:"WAVE "+wave,W*.5f,H*.51f,24,WHITE,true,true);
      text(canvas,wave%5==0?"THE ROOST COMMANDER":"INCOMING FLOCK",W*.5f,H*.55f,8,CYAN,true,true);
    }
  }

  void drawHud(Canvas c){
    round(c,d(8),topHud(),d(180),topHud()+d(34),10,PANEL);
    text(c,"SCORE "+score,d(19),topHud()+d(14),8,Color.rgb(182,197,228),true,false);
    text(c,String.format(Locale.US,"%06d",score),d(19),topHud()+d(29),11,WHITE,true,false);

    float cx=W*.5f;
    round(c,cx-d(80),topHud(),cx+d(80),topHud()+d(34),10,PANEL);
    text(c,"WAVE "+wave,cx-d(64),topHud()+d(14),8,CYAN,true,false);
    text(c,"LIVES "+lives+"   POWER "+weapon,cx-d(64),topHud()+d(29),9,WHITE,true,false);

    round(c,W-d(180),topHud(),W-d(8),topHud()+d(34),10,PANEL);
    text(c,"SHIELD "+(shield>0?(int)Math.ceil(shield)+"s":"OFF"),W-d(168),topHud()+d(14),7,shield>0?CYAN:Color.rgb(150,160,185),true,false);
    text(c,"NOVA x"+bombs,W-d(168),topHud()+d(29),9,ORANGE,true,false);

    round(c,W-d(51),H-d(55),W-d(9),H-d(13),21,Color.argb(220,54,69,107));
    text(c,"N",W-d(30),H-d(28),13,WHITE,true,true);
    round(c,W-d(49),d(8),W-d(9),d(42),10,Color.argb(225,44,54,82));
    text(c,"Ⅱ",W-d(29),d(31),14,WHITE,true,true);
  }

  void drawPlayer(Canvas c){
    float alpha=invuln>0&&((int)(invuln*12)%2==0)?.35f:1f;
    int a=(int)(255*alpha);
    Path ship=new Path();
    ship.moveTo(px,py-playerR);
    ship.lineTo(px-playerR*.75f,py+playerR*.75f);
    ship.lineTo(px,py+playerR*.35f);
    ship.lineTo(px+playerR*.75f,py+playerR*.75f);
    ship.close();
    p.setColor(Color.argb(a,92,190,230));c.drawPath(ship,p);
    p.setColor(Color.argb(a,235,240,249));c.drawOval(new RectF(px-playerR*.22f,py-playerR*.45f,px+playerR*.22f,py+playerR*.35f),p);
    p.setColor(Color.argb(a,244,146,61));
    c.drawOval(new RectF(px-playerR*.20f,py+playerR*.45f,px+playerR*.20f,py+playerR*1.05f),p);
    if(shield>0){
      stroke.setStyle(Paint.Style.STROKE);stroke.setStrokeWidth(d(2));stroke.setColor(Color.argb(180,CYAN>>16&255,CYAN>>8&255,CYAN&255));
      c.drawCircle(px,py,playerR*1.35f,stroke);
    }
  }

  void drawEnemy(Canvas c,Enemy e){
    float s=d(e.type==9?28:15)*e.scale;
    if(e.type==9){
      p.setColor(Color.rgb(95,104,136));c.drawOval(new RectF(e.x-s*1.35f,e.y-s*.55f,e.x+s*1.35f,e.y+s*.55f),p);
      p.setColor(CHICKEN);c.drawOval(new RectF(e.x-s*.8f,e.y-s*.75f,e.x+s*.8f,e.y+s*.65f),p);
      p.setColor(WHITE);c.drawCircle(e.x+s*.55f,e.y-s*.45f,s*.28f,p);
      p.setColor(RED);c.drawCircle(e.x+s*.45f,e.y-s*.75f,s*.12f,p);c.drawCircle(e.x+s*.64f,e.y-s*.76f,s*.10f,p);
      Path beak=new Path();beak.moveTo(e.x+s*.75f,e.y-s*.46f);beak.lineTo(e.x+s*1.05f,e.y-s*.32f);beak.lineTo(e.x+s*.76f,e.y-s*.18f);beak.close();p.setColor(BEAK);c.drawPath(beak,p);
      p.setColor(Color.BLACK);c.drawCircle(e.x+s*.61f,e.y-s*.47f,s*.05f,p);
      float hp=Math.max(0,e.hp/(float)e.maxHp);
      round(c,e.x-s*1.2f,e.y+s*.85f,e.x+s*1.2f,e.y+s*1.02f,3,Color.rgb(50,58,77));
      round(c,e.x-s*1.2f,e.y+s*.85f,e.x-s*1.2f+s*2.4f*hp,e.y+s*1.02f,3,RED);
      return;
    }
    p.setColor(CHICKEN);c.drawOval(new RectF(e.x-s*.75f,e.y-s*.45f,e.x+s*.72f,e.y+s*.53f),p);
    p.setColor(CHICKEN2);c.drawOval(new RectF(e.x-s*.90f,e.y-s*.30f,e.x-s*.40f,e.y+s*.23f),p);
    p.setColor(WHITE);c.drawCircle(e.x+s*.48f,e.y-s*.35f,s*.30f,p);
    p.setColor(RED);c.drawCircle(e.x+s*.34f,e.y-s*.62f,s*.12f,p);c.drawCircle(e.x+s*.51f,e.y-s*.67f,s*.11f,p);
    Path beak=new Path();beak.moveTo(e.x+s*.70f,e.y-s*.34f);beak.lineTo(e.x+s*.99f,e.y-s*.20f);beak.lineTo(e.x+s*.70f,e.y-s*.05f);beak.close();p.setColor(BEAK);c.drawPath(beak,p);
    p.setColor(Color.BLACK);c.drawCircle(e.x+s*.55f,e.y-s*.37f,s*.05f,p);
    if(e.type==1){p.setColor(BLUE);c.drawRect(e.x-s*.45f,e.y+s*.25f,e.x+s*.34f,e.y+s*.46f,p);}
    else if(e.type==2){p.setColor(GREEN);c.drawCircle(e.x-s*.18f,e.y+s*.07f,s*.20f,p);}
    else if(e.type==3){p.setColor(RED);c.drawRect(e.x-s*.25f,e.y-s*.58f,e.x+s*.02f,e.y-s*.38f,p);}
  }

  void drawShot(Canvas c,Shot s){
    if(s.enemy){
      p.setColor(WHITE);c.drawOval(new RectF(s.x-s.r*.72f,s.y-s.r,s.x+s.r*.72f,s.y+s.r),p);
      p.setColor(Color.rgb(226,211,166));c.drawOval(new RectF(s.x-s.r*.42f,s.y-s.r*.60f,s.x+s.r*.42f,s.y+s.r*.60f),p);
    }else{
      p.setColor(CYAN);c.drawRoundRect(new RectF(s.x-s.r*.55f,s.y-s.r*2.3f,s.x+s.r*.55f,s.y+s.r*2.3f),s.r,s.r,p);
      p.setColor(WHITE);c.drawCircle(s.x,s.y-s.r*1.2f,s.r*.45f,p);
    }
  }

  void drawDrop(Canvas c,Drop dr){
    int col=dr.type==0?YELLOW:dr.type==1?CYAN:ORANGE;
    p.setColor(Color.argb(65,255,255,255));c.drawCircle(dr.x,dr.y,d(15),p);
    p.setColor(col);c.drawCircle(dr.x,dr.y,d(10),p);
    text(c,dr.type==0?"P":dr.type==1?"S":"N",dr.x,dr.y+d(4),9,SPACE,true,true);
  }

  void drawSpark(Canvas c,Spark s){
    int alpha=(int)(255*Math.max(0,s.life/s.max));p.setColor(Color.argb(alpha,Color.red(s.color),Color.green(s.color),Color.blue(s.color)));
    c.drawCircle(s.x,s.y,d(1.5f),p);
  }

  void drawPause(Canvas c){
    p.setColor(Color.argb(185,0,0,0));c.drawRect(0,0,W,H,p);
    float cx=W*.5f,boxW=Math.min(W-d(100),d(380)),x=cx-boxW/2,y=H*.28f;
    round(c,x,y,x+boxW,y+d(178),18,PANEL2);
    text(c,"MISSION PAUSED",cx,y+d(40),18,WHITE,true,true);
    text(c,"Nothing moves until you resume.",cx,y+d(65),8,Color.rgb(185,199,228),false,true);
    round(c,x+d(30),y+d(89),x+boxW-d(30),y+d(129),11,BLUE);
    text(c,"RESUME",cx,y+d(115),10,WHITE,true,true);
    round(c,x+d(30),y+d(137),x+boxW-d(30),y+d(167),9,Color.rgb(49,60,88));
    text(c,"MAIN MENU",cx,y+d(157),8,WHITE,true,true);
  }

  void drawGameOver(Canvas c){
    p.setColor(Color.argb(188,0,0,0));c.drawRect(0,0,W,H,p);
    float cx=W*.5f,boxW=Math.min(W-d(100),d(410)),x=cx-boxW/2,y=H*.22f;
    round(c,x,y,x+boxW,y+d(220),18,PANEL2);
    text(c,"MISSION OVER",cx,y+d(42),20,WHITE,true,true);
    text(c,"SCORE "+score,cx,y+d(77),15,YELLOW,true,true);
    text(c,"Reached wave "+wave,cx,y+d(103),9,Color.rgb(187,201,230),false,true);
    text(c,"Best "+bestScore+" • Best wave "+bestWave,cx,y+d(125),8,Color.rgb(150,170,207),false,true);
    round(c,x+d(35),y+d(147),x+boxW-d(35),y+d(188),11,BLUE);
    text(c,"RETRY",cx,y+d(173),10,WHITE,true,true);
    text(c,"Tap Back for main menu",cx,y+d(207),7,Color.rgb(135,155,193),false,true);
  }

  void text(Canvas c,String s,float x,float y,float size,int color,boolean bold,boolean center){
    p.setStyle(Paint.Style.FILL);p.setColor(color);p.setTextSize(d(size));p.setTypeface(Typeface.create("sans",bold?Typeface.BOLD:Typeface.NORMAL));
    p.setTextAlign(center?Paint.Align.CENTER:Paint.Align.LEFT);c.drawText(s,x,y,p);p.setTextAlign(Paint.Align.LEFT);
  }

  void round(Canvas c,float l,float t,float r,float b,float rad,int col){
    p.setStyle(Paint.Style.FILL);p.setColor(col);c.drawRoundRect(l,t,r,b,d(rad),d(rad),p);
  }

  @Override public boolean onTouchEvent(MotionEvent e){
    float x=e.getX(),y=e.getY();
    if(e.getAction()==MotionEvent.ACTION_DOWN){
      if(mode==0){
        float top=Math.max(d(24),H*.08f),menuY=top+d(190)+d(112),bw=Math.min(d(260),W*.55f),cx=W*.5f;
        if(x>=cx-bw/2&&x<=cx+bw/2&&y>=menuY&&y<=menuY+d(48)){startGame();return true;}
        return true;
      }
      if(mode==2){
        float cx=W*.5f,boxW=Math.min(W-d(100),d(410)),bx=cx-boxW/2,by=H*.22f;
        if(x>=bx+d(35)&&x<=bx+boxW-d(35)&&y>=by+d(147)&&y<=by+d(188)){startGame();return true;}
        return true;
      }
      if(paused){
        float cx=W*.5f,boxW=Math.min(W-d(100),d(380)),bx=cx-boxW/2,by=H*.28f;
        if(x>=bx+d(30)&&x<=bx+boxW-d(30)&&y>=by+d(89)&&y<=by+d(129)){paused=false;systemPaused=false;lastNs=System.nanoTime();return true;}
        if(x>=bx+d(30)&&x<=bx+boxW-d(30)&&y>=by+d(137)&&y<=by+d(167)){mode=0;paused=false;systemPaused=false;saveProgress();return true;}
        return true;
      }
      if(x>=W-d(52)&&y<=d(46)){paused=true;touching=false;return true;}
      if(x>=W-d(60)&&y>=H-d(64)){useBomb();return true;}
      touching=true;touchX=x;touchY=y;
      return true;
    }
    if(e.getAction()==MotionEvent.ACTION_MOVE){
      if(mode==1&&!paused&&touching){touchX=x;touchY=y;}
      return true;
    }
    if(e.getAction()==MotionEvent.ACTION_UP||e.getAction()==MotionEvent.ACTION_CANCEL){
      touching=false;return true;
    }
    return true;
  }
}
