package com.ironvale.game;

import org.json.JSONObject;

class Bot {
  String name;
  int x, y, level, infantry, cavalry;
  double aggression, nextAttack, stock, growth;

  Bot(String name,int x,int y,int level,int infantry,int cavalry,double aggression){
    this.name=name; this.x=x; this.y=y; this.level=level;
    this.infantry=infantry; this.cavalry=cavalry; this.aggression=aggression;
    this.stock=900+level*400; this.growth=0; this.nextAttack=0;
  }

  JSONObject json(){
    JSONObject o=new JSONObject();
    try{
      o.put("name",name); o.put("x",x); o.put("y",y); o.put("level",level);
      o.put("infantry",infantry); o.put("cavalry",cavalry); o.put("aggression",aggression);
      o.put("nextAttack",nextAttack); o.put("stock",stock); o.put("growth",growth);
    }catch(Exception ignored){}
    return o;
  }

  static Bot from(JSONObject o){
    Bot b=new Bot(o.optString("name","Rival"),o.optInt("x"),o.optInt("y"),o.optInt("level",1),
      o.optInt("infantry",10),o.optInt("cavalry",0),o.optDouble("aggression",1.0));
    b.nextAttack=o.optDouble("nextAttack",0); b.stock=o.optDouble("stock",1000); b.growth=o.optDouble("growth",0);
    return b;
  }
}
