package com.ironvale.game;

import org.json.JSONObject;

class Mission {
  boolean enemy;
  int botIndex, infantry, cavalry;
  double arrival;
  String label;

  Mission(boolean enemy,int botIndex,int infantry,int cavalry,double arrival,String label){
    this.enemy=enemy; this.botIndex=botIndex; this.infantry=infantry; this.cavalry=cavalry; this.arrival=arrival; this.label=label;
  }

  JSONObject json(){
    JSONObject o=new JSONObject();
    try{
      o.put("enemy",enemy); o.put("botIndex",botIndex); o.put("infantry",infantry); o.put("cavalry",cavalry);
      o.put("arrival",arrival); o.put("label",label);
    }catch(Exception ignored){}
    return o;
  }

  static Mission from(JSONObject o){
    return new Mission(o.optBoolean("enemy"),o.optInt("botIndex"),o.optInt("infantry"),o.optInt("cavalry"),
      o.optDouble("arrival"),o.optString("label","Army"));
  }
}
