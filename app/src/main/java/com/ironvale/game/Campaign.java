package com.ironvale.game;
class Campaign {
  static class Level {
    String name,sector;int waves,difficulty;boolean boss;
    Level(String n,String s,int w,int d,boolean b){name=n;sector=s;waves=w;difficulty=d;boss=b;}
  }
  static final String[] SECTORS={"Home Orbit","Moon Run","Red Nebula","Frozen Belt","Solar Forge","Dark Roost"};
  static final Level[] LEVELS=new Level[30];
  static{
    String[] names={"First Scramble","Egg Patrol","Feather Storm","Supply Raid","Mother Hen",
      "Lunar Clucks","Crater Flock","Gravity Nest","Satellite Siege","Roost Cruiser",
      "Nebula Drift","Wing Squadron","Meteor Omelette","Red Alert","Brood Captain",
      "Ice Peck","Comet Coop","Frozen Wings","Glacier Run","Polar Rooster",
      "Solar Fry","Flare Flock","Hot Wings","Corona Clash","Inferno Hen",
      "Dark Feathers","Void Eggs","Black Nest","Final Scramble","Emperor Roost"};
    for(int i=0;i<30;i++)LEVELS[i]=new Level(names[i],SECTORS[i/5],3+(i/8),1+i/3,(i+1)%5==0);
  }
}
