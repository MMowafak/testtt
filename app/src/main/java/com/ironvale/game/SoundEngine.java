package com.ironvale.game;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import java.io.*;
import java.util.*;

class SoundEngine {
  static final int SR=22050;
  final Context ctx;
  SoundPool pool;
  MediaPlayer music;
  int laser, hit, explosion, pickup, nova, cluck, boss, victory, click, shield;
  boolean sfxOn=true, musicOn=true;
  long lastLaser=0;

  SoundEngine(Context c, boolean sfx, boolean mus){
    ctx=c.getApplicationContext();sfxOn=sfx;musicOn=mus;
    try{
      AudioAttributes aa=new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build();
      pool=new SoundPool.Builder().setMaxStreams(10).setAudioAttributes(aa).build();
      File dir=new File(ctx.getCacheDir(),"ff_audio");dir.mkdirs();
      laser=load(dir,"laser.wav",laserWave());
      hit=load(dir,"hit.wav",hitWave());
      explosion=load(dir,"explosion.wav",explosionWave());
      pickup=load(dir,"pickup.wav",pickupWave());
      nova=load(dir,"nova.wav",novaWave());
      cluck=load(dir,"cluck.wav",cluckWave(false));
      boss=load(dir,"boss.wav",cluckWave(true));
      victory=load(dir,"victory.wav",victoryWave());
      click=load(dir,"click.wav",clickWave());
      shield=load(dir,"shield.wav",shieldWave());
      File mf=new File(dir,"music.wav");
      if(!mf.exists())writeWav(mf,musicWave());
      music=new MediaPlayer();
      music.setDataSource(mf.getAbsolutePath());music.setLooping(true);music.setVolume(.28f,.28f);music.prepare();
      if(musicOn)music.start();
    }catch(Exception ignored){}
  }

  int load(File dir,String name,short[] pcm)throws Exception{
    File f=new File(dir,name);if(!f.exists())writeWav(f,pcm);return pool.load(f.getAbsolutePath(),1);
  }
  void setMusic(boolean on){musicOn=on;try{if(music==null)return;if(on){if(!music.isPlaying())music.start();}else if(music.isPlaying())music.pause();}catch(Exception ignored){}}
  void setSfx(boolean on){sfxOn=on;}
  void pauseMusic(){try{if(music!=null&&music.isPlaying())music.pause();}catch(Exception ignored){}}
  void resumeMusic(){if(!musicOn)return;try{if(music!=null&&!music.isPlaying())music.start();}catch(Exception ignored){}}
  void release(){try{if(pool!=null)pool.release();}catch(Exception ignored){}try{if(music!=null)music.release();}catch(Exception ignored){}}
  void play(int id,float vol){if(!sfxOn||pool==null||id==0)return;try{pool.play(id,vol,vol,1,0,1);}catch(Exception ignored){}}
  void laser(){long n=System.currentTimeMillis();if(n-lastLaser>85){lastLaser=n;play(laser,.18f);}}
  void hit(){play(hit,.34f);} void explode(){play(explosion,.50f);} void pickup(){play(pickup,.42f);}
  void nova(){play(nova,.58f);} void cluck(){play(cluck,.26f);} void boss(){play(boss,.42f);}
  void victory(){play(victory,.48f);} void click(){play(click,.30f);} void shield(){play(shield,.36f);}

  static short[] from(List<Double> v){
    short[] a=new short[v.size()];for(int i=0;i<a.length;i++)a[i]=(short)(Math.max(-1,Math.min(1,v.get(i)))*32767);return a;
  }
  static double env(double t,double d,double a,double r){
    if(t<a)return t/a;if(t>d-r)return Math.max(0,(d-t)/r);return 1;
  }
  static short[] synth(double f,double dur,double vol,double sweep,int shape){
    int n=(int)(SR*dur);short[] out=new short[n];double ph=0;
    for(int i=0;i<n;i++){
      double t=i/(double)SR,ff=f+sweep*t;ph+=2*Math.PI*ff/SR;
      double v=shape==1?(Math.sin(ph)>=0?1:-1):shape==2?((ph/(2*Math.PI))%1)*2-1:Math.sin(ph);
      v*=vol*env(t,dur,Math.min(.018,dur*.18),Math.min(.09,dur*.3));
      out[i]=(short)(Math.max(-1,Math.min(1,v))*32767);
    }return out;
  }
  static short[] mix(short[]... tracks){
    int n=0;for(short[] a:tracks)n=Math.max(n,a.length);short[] o=new short[n];
    for(int i=0;i<n;i++){int v=0;for(short[] a:tracks)if(i<a.length)v+=a[i];o[i]=(short)Math.max(-32767,Math.min(32767,v));}return o;
  }
  static short[] laserWave(){return mix(synth(950,.12,.25,-3600,1),synth(1850,.12,.12,-7000,0));}
  static short[] hitWave(){return mix(synth(230,.18,.34,-900,1),synth(90,.20,.25,-120,0));}
  static short[] explosionWave(){
    int n=(int)(SR*.62);short[] o=new short[n];Random r=new Random(31);double lp=0;
    for(int i=0;i<n;i++){double t=i/(double)SR,e=Math.pow(1-t/.62,1.5);double q=r.nextDouble()*2-1;lp=lp*.82+q*.18;double b=Math.sin(2*Math.PI*(120-75*t/.62)*t);o[i]=(short)((lp*.52+b*.32)*e*32767);}return o;
  }
  static short[] pickupWave(){
    int n=(int)(SR*.46);short[] o=new short[n];double[] f={523.25,659.25,783.99,1046.5};
    for(int k=0;k<f.length;k++){short[] q=synth(f[k],.16,.18,0,0);int off=(int)(k*.075*SR);for(int i=0;i<q.length&&off+i<n;i++)o[off+i]=(short)Math.max(-32767,Math.min(32767,o[off+i]+q[i]));}return o;
  }
  static short[] shieldWave(){return mix(synth(280,.42,.15,1050,0),synth(560,.42,.08,2100,0));}
  static short[] novaWave(){return mix(synth(85,.72,.28,1200,0),synth(170,.72,.14,2400,0));}
  static short[] cluckWave(boolean low){
    double dur=low?.65:.34;int n=(int)(SR*dur);short[] o=new short[n];Random r=new Random(low?71:17);
    for(int i=0;i<n;i++){double t=i/(double)SR,f=(low?135:325)+(low?35:125)*Math.sin(2*Math.PI*(low?4:7)*t);
      double gate=low?1:(t<.09||t>.14&&t<.23?1:.35);double v=Math.sin(2*Math.PI*f*t)*(low?.34:.26)+(r.nextDouble()*2-1)*.08;
      o[i]=(short)(v*gate*env(t,dur,.01,.06)*32767);}return o;
  }
  static short[] victoryWave(){
    int n=(int)(SR*1.05);short[] o=new short[n];double[] f={392,523.25,659.25,783.99};double[] st={0,.18,.36,.58};
    for(int k=0;k<4;k++){short[] q=synth(f[k],k==3?.45:.25,.20,0,0);int off=(int)(st[k]*SR);for(int i=0;i<q.length&&off+i<n;i++)o[off+i]=(short)Math.max(-32767,Math.min(32767,o[off+i]+q[i]));}return o;
  }
  static short[] clickWave(){return mix(synth(660,.065,.18,0,0),synth(990,.05,.09,0,0));}
  static short[] musicWave(){
    double dur=12.0;int n=(int)(SR*dur);double[] mix=new double[n];double beat=.5;
    double[][] chords={{164.81,196,246.94},{146.83,196,246.94},{164.81,196,246.94},{146.83,220,246.94}};
    double[] mel={329.63,392,493.88,392,587.33,493.88,392,440,329.63,392,493.88,659.25,587.33,493.88,440,392};
    int bars=(int)(dur/(beat*4));
    for(int bar=0;bar<bars;bar++){
      double start=bar*beat*4;double[] ch=chords[bar%4];
      add(mix,start,ch[0],beat*3.8,.055,0);
      for(int j=0;j<8;j++){add(mix,start+j*beat/2,ch[j%3],beat*.42,.028,1);add(mix,start+j*beat/2,mel[(bar*8+j)%mel.length],beat*.34,.034,0);}
      for(int j=0;j<4;j++){double ks=start+j*beat;int off=(int)(ks*SR);for(int i=0;i<(int)(SR*.10)&&off+i<n;i++){double t=i/(double)SR;mix[off+i]+=Math.sin(2*Math.PI*(90-45*t/.10)*t)*.065*(1-t/.10);}}
    }
    short[] o=new short[n];for(int i=0;i<n;i++)o[i]=(short)(Math.tanh(mix[i]*1.8)*.55*32767);return o;
  }
  static void add(double[] dst,double start,double f,double dur,double vol,int shape){
    int off=(int)(start*SR),nn=(int)(dur*SR);double ph=0;
    for(int i=0;i<nn&&off+i<dst.length;i++){double t=i/(double)SR;ph+=2*Math.PI*f/SR;double v=shape==1?(Math.sin(ph)>0?1:-1):Math.sin(ph);dst[off+i]+=v*vol*env(t,dur,.01,.06);}
  }
  static void writeWav(File f,short[] pcm)throws Exception{
    DataOutputStream o=new DataOutputStream(new BufferedOutputStream(new FileOutputStream(f)));int bytes=pcm.length*2;
    o.writeBytes("RIFF");le32(o,36+bytes);o.writeBytes("WAVEfmt ");le32(o,16);le16(o,1);le16(o,1);le32(o,SR);le32(o,SR*2);le16(o,2);le16(o,16);
    o.writeBytes("data");le32(o,bytes);for(short s:pcm){o.writeByte(s&255);o.writeByte((s>>8)&255);}o.close();
  }
  static void le16(DataOutputStream o,int v)throws Exception{o.writeByte(v&255);o.writeByte((v>>8)&255);}
  static void le32(DataOutputStream o,int v)throws Exception{o.writeByte(v&255);o.writeByte((v>>8)&255);o.writeByte((v>>16)&255);o.writeByte((v>>24)&255);}
}
