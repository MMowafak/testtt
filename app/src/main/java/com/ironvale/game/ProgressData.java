package com.ironvale.game;
import android.content.*;
import java.util.*;
class ProgressData {
  final SharedPreferences p;
  int unlocked=1,coins=0,totalKills=0,bossKills=0;
  int blaster=0,shield=0,nova=0;
  boolean music=true,sfx=true;
  final int[] stars=new int[30];
  ProgressData(Context c){p=c.getSharedPreferences("feather_force_v2",0);load();}
  void load(){unlocked=p.getInt("unlocked",1);coins=p.getInt("coins",0);totalKills=p.getInt("kills",0);bossKills=p.getInt("bossKills",0);blaster=p.getInt("blaster",0);shield=p.getInt("shield",0);nova=p.getInt("nova",0);music=p.getBoolean("music",true);sfx=p.getBoolean("sfx",true);String[] a=p.getString("stars","").split(",");for(int i=0;i<30&&i<a.length;i++)try{stars[i]=Integer.parseInt(a[i]);}catch(Exception ignored){}}
  void save(){StringBuilder s=new StringBuilder();for(int i=0;i<30;i++){if(i>0)s.append(',');s.append(stars[i]);}p.edit().putInt("unlocked",unlocked).putInt("coins",coins).putInt("kills",totalKills).putInt("bossKills",bossKills).putInt("blaster",blaster).putInt("shield",shield).putInt("nova",nova).putBoolean("music",music).putBoolean("sfx",sfx).putString("stars",s.toString()).apply();}
  int totalStars(){int n=0;for(int x:stars)n+=x;return n;}
  void finish(int level,int star,int reward,boolean boss){int i=level-1;stars[i]=Math.max(stars[i],star);coins+=reward;if(level<30)unlocked=Math.max(unlocked,level+1);if(boss)bossKills++;save();}
  int cost(String k){int lv=k.equals("blaster")?blaster:k.equals("shield")?shield:nova;return (lv+1)*(lv+1)*350;}
  boolean buy(String k,int max){int lv=k.equals("blaster")?blaster:k.equals("shield")?shield:nova;if(lv>=max)return false;int c=cost(k);if(coins<c)return false;coins-=c;if(k.equals("blaster"))blaster++;else if(k.equals("shield"))shield++;else nova++;save();return true;}
}
