package com.ironvale.game;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Random;

class GameState {
  static final double SPEED = 20.0;
  final SharedPreferences sp;
  final Random rng = new Random();
  boolean started=false, paused=true;
  long lastReal=System.currentTimeMillis();
  double sim=0;
  double wood=1500, clay=1500, iron=1300, grain=1700;
  int woodField=1, clayField=1, ironField=1, cropField=1;
  int main=1, warehouse=1, granary=1, barracks=0, rally=1, wall=0, smithy=0, academy=0, residence=0, mansion=0;
  int infantry=12, scouts=0, cavalry=0, hero=1, heroXp=0;
  String buildKey="", trainType="";
  int buildTarget=0, trainCount=0;
  double buildFinish=0, trainFinish=0;
  final ArrayList<Bot> bots=new ArrayList<>();
  final ArrayList<Mission> missions=new ArrayList<>();
  final ArrayList<String> reports=new ArrayList<>();
  final HashSet<String> explored=new HashSet<>();
  final HashSet<String> oases=new HashSet<>();

  GameState(Context c){
    sp=c.getSharedPreferences("ironvale_warfront_v4",0);
    load();
  }

  void newRealm(){
    started=true; paused=false; sim=0; lastReal=System.currentTimeMillis();
    wood=1500;clay=1500;iron=1300;grain=1700;
    woodField=clayField=ironField=cropField=1;
    main=warehouse=granary=rally=1; barracks=wall=smithy=academy=residence=mansion=0;
    infantry=12;scouts=0;cavalry=0;hero=1;heroXp=0;
    buildKey="";trainType="";buildTarget=0;trainCount=0;buildFinish=0;trainFinish=0;
    missions.clear();reports.clear();explored.clear();oases.clear();bots.clear();
    addBot("Iron Wolves",4,2,2,22,2,1.0);
    addBot("Red Fen", -5,3,2,18,4,1.15);
    addBot("Oakclaw",2,-6,3,28,5,0.9);
    addBot("Duskguard",-6,-4,3,32,3,1.25);
    addBot("Ash Crown",7,-3,4,40,8,1.35);
    addBot("Obsidian Host",0,8,5,52,12,1.5);
    for(Bot b:bots) b.nextAttack=sim+1200+rng.nextInt(1800);
    addReport("Realm founded. Six rival warlords are expanding around Emberhold.");
    addReport("Realm speed is x20. One real minute equals twenty game minutes.");
    save();
  }

  void addBot(String n,int x,int y,int level,int inf,int cav,double aggression){
    bots.add(new Bot(n,x,y,level,inf,cav,aggression));
  }

  void load(){
    started=sp.getBoolean("started",false);
    if(!started){paused=true;return;}
    paused=sp.getBoolean("paused",false);
    lastReal=sp.getLong("lastReal",System.currentTimeMillis());
    sim=Double.longBitsToDouble(sp.getLong("simBits",Double.doubleToLongBits(0)));
    wood=dbl("wood",1500);clay=dbl("clay",1500);iron=dbl("iron",1300);grain=dbl("grain",1700);
    woodField=sp.getInt("woodField",1);clayField=sp.getInt("clayField",1);ironField=sp.getInt("ironField",1);cropField=sp.getInt("cropField",1);
    main=sp.getInt("main",1);warehouse=sp.getInt("warehouse",1);granary=sp.getInt("granary",1);
    barracks=sp.getInt("barracks",0);rally=sp.getInt("rally",1);wall=sp.getInt("wall",0);smithy=sp.getInt("smithy",0);
    academy=sp.getInt("academy",0);residence=sp.getInt("residence",0);mansion=sp.getInt("mansion",0);
    infantry=sp.getInt("infantry",12);scouts=sp.getInt("scouts",0);cavalry=sp.getInt("cavalry",0);
    hero=sp.getInt("hero",1);heroXp=sp.getInt("heroXp",0);
    buildKey=sp.getString("buildKey","");trainType=sp.getString("trainType","");
    buildTarget=sp.getInt("buildTarget",0);trainCount=sp.getInt("trainCount",0);
    buildFinish=dbl("buildFinish",0);trainFinish=dbl("trainFinish",0);
    try{
      JSONArray a=new JSONArray(sp.getString("bots","[]"));
      for(int i=0;i<a.length();i++)bots.add(Bot.from(a.getJSONObject(i)));
      JSONArray m=new JSONArray(sp.getString("missions","[]"));
      for(int i=0;i<m.length();i++)missions.add(Mission.from(m.getJSONObject(i)));
      JSONArray r=new JSONArray(sp.getString("reports","[]"));
      for(int i=0;i<r.length();i++)reports.add(r.optString(i));
      JSONArray e=new JSONArray(sp.getString("explored","[]"));
      for(int i=0;i<e.length();i++)explored.add(e.optString(i));
      JSONArray o=new JSONArray(sp.getString("oases","[]"));
      for(int i=0;i<o.length();i++)oases.add(o.optString(i));
    }catch(Exception ignored){}
    if(bots.isEmpty()){
      addBot("Iron Wolves",4,2,2,22,2,1.0); addBot("Red Fen",-5,3,2,18,4,1.15);
      addBot("Oakclaw",2,-6,3,28,5,0.9); addBot("Duskguard",-6,-4,3,32,3,1.25);
      addBot("Ash Crown",7,-3,4,40,8,1.35); addBot("Obsidian Host",0,8,5,52,12,1.5);
      for(Bot b:bots)b.nextAttack=sim+1200+rng.nextInt(1800);
    }
  }

  double dbl(String key,double def){
    return Double.longBitsToDouble(sp.getLong(key,Double.doubleToLongBits(def)));
  }

  void save(){
    JSONArray ba=new JSONArray(), ma=new JSONArray(), ra=new JSONArray(), ea=new JSONArray(), oa=new JSONArray();
    for(Bot b:bots)ba.put(b.json()); for(Mission m:missions)ma.put(m.json()); for(String s:reports)ra.put(s);
    for(String s:explored)ea.put(s); for(String s:oases)oa.put(s);
    sp.edit().putBoolean("started",started).putBoolean("paused",paused).putLong("lastReal",lastReal)
      .putLong("simBits",Double.doubleToLongBits(sim)).putLong("wood",Double.doubleToLongBits(wood))
      .putLong("clay",Double.doubleToLongBits(clay)).putLong("iron",Double.doubleToLongBits(iron))
      .putLong("grain",Double.doubleToLongBits(grain)).putInt("woodField",woodField).putInt("clayField",clayField)
      .putInt("ironField",ironField).putInt("cropField",cropField).putInt("main",main).putInt("warehouse",warehouse)
      .putInt("granary",granary).putInt("barracks",barracks).putInt("rally",rally).putInt("wall",wall)
      .putInt("smithy",smithy).putInt("academy",academy).putInt("residence",residence).putInt("mansion",mansion)
      .putInt("infantry",infantry).putInt("scouts",scouts).putInt("cavalry",cavalry).putInt("hero",hero)
      .putInt("heroXp",heroXp).putString("buildKey",buildKey).putInt("buildTarget",buildTarget)
      .putLong("buildFinish",Double.doubleToLongBits(buildFinish)).putString("trainType",trainType)
      .putInt("trainCount",trainCount).putLong("trainFinish",Double.doubleToLongBits(trainFinish))
      .putString("bots",ba.toString()).putString("missions",ma.toString()).putString("reports",ra.toString())
      .putString("explored",ea.toString()).putString("oases",oa.toString()).apply();
  }

  void tick(long now){
    if(!started){lastReal=now;return;}
    if(paused){lastReal=now;return;}
    double real=Math.max(0,(now-lastReal)/1000.0);
    if(real<=0)return;
    lastReal=now;
    double ds=real*SPEED;
    sim+=ds;
    produce(ds);
    processConstruction();
    processTraining();
    processBots(ds);
    processMissions();
    if(((long)sim)%60<5)save();
  }

  void produce(double ds){
    double oasisBonus=1.0+oases.size()*0.10;
    wood=Math.min(storageCap(),wood+production(woodField)*oasisBonus*ds/3600.0);
    clay=Math.min(storageCap(),clay+production(clayField)*oasisBonus*ds/3600.0);
    iron=Math.min(storageCap(),iron+production(ironField)*oasisBonus*ds/3600.0);
    double upkeep=infantry*1.0+scouts*1.0+cavalry*3.0;
    grain=Math.min(granaryCap(),Math.max(0,grain+(production(cropField)*oasisBonus-upkeep)*ds/3600.0));
  }

  double production(int level){ return 80*Math.pow(Math.max(1,level),1.30)*(1+main*0.035); }
  int storageCap(){return 3000+warehouse*2500;}
  int granaryCap(){return 3000+granary*2500;}
  int defensePower(){return infantry*(14+smithy*2)+cavalry*(19+smithy*2)+wall*38+hero*28+residence*12;}
  int attackPower(){return infantry*(12+smithy*2)+cavalry*(30+smithy*3)+hero*24;}

  void processConstruction(){
    if(!buildKey.isEmpty() && sim>=buildFinish){
      setLevel(buildKey,buildTarget);
      addReport(label(buildKey)+" reached level "+buildTarget+".");
      buildKey="";buildTarget=0;buildFinish=0;
    }
  }

  void processTraining(){
    if(!trainType.isEmpty() && sim>=trainFinish){
      if(trainType.equals("infantry"))infantry+=trainCount;
      else if(trainType.equals("scouts"))scouts+=trainCount;
      else cavalry+=trainCount;
      addReport(trainCount+" "+unitLabel(trainType)+" finished training.");
      trainType="";trainCount=0;trainFinish=0;
    }
  }

  void processBots(double ds){
    for(int i=0;i<bots.size();i++){
      Bot b=bots.get(i);
      b.stock+=((110+b.level*75)*ds/3600.0);
      b.growth+=ds;
      while(b.growth>=600){
        b.growth-=600;
        b.infantry+=1+Math.max(0,b.level/2);
        if(b.level>=3 && rng.nextInt(3)==0)b.cavalry++;
        if(rng.nextInt(12)==0)b.level=Math.min(10,b.level+1);
      }
      if(sim>=b.nextAttack && !hasIncomingFrom(i)){
        launchBotAttack(i);
        b.nextAttack=sim+(1400+rng.nextInt(2200))/Math.max(0.75,b.aggression);
      }
    }
  }

  boolean hasIncomingFrom(int bot){
    for(Mission m:missions)if(m.enemy&&m.botIndex==bot)return true;
    return false;
  }

  void launchBotAttack(int index){
    Bot b=bots.get(index);
    int sendInf=Math.max(3,(int)(b.infantry*(0.28+0.16*rng.nextDouble())));
    int sendCav=Math.max(0,(int)(b.cavalry*(0.20+0.25*rng.nextDouble())));
    sendInf=Math.min(sendInf,b.infantry);sendCav=Math.min(sendCav,b.cavalry);
    b.infantry-=sendInf;b.cavalry-=sendCav;
    double eta=sim+travelSeconds(b.x,b.y);
    missions.add(new Mission(true,index,sendInf,sendCav,eta,b.name+" assault"));
    addReport("Warning: "+b.name+" launched an attack. ETA "+realEta(eta-sim)+".");
  }

  double travelSeconds(int x,int y){
    double dist=Math.sqrt(x*x+y*y);
    return 300+dist*105;
  }

  void processMissions(){
    for(int i=missions.size()-1;i>=0;i--){
      Mission m=missions.get(i);
      if(sim<m.arrival)continue;
      if(m.enemy)resolveEnemy(m); else resolveRaid(m);
      missions.remove(i);
    }
  }

  void resolveEnemy(Mission m){
    if(m.botIndex<0||m.botIndex>=bots.size())return;
    Bot b=bots.get(m.botIndex);
    int atk=m.infantry*(12+b.level)+m.cavalry*(27+b.level*2);
    int def=Math.max(1,defensePower());
    boolean lost=atk>def;
    double defenderLoss=lost?Math.min(.82,.42+atk/(double)(def+atk)*.5):Math.min(.55,atk/(double)(def+atk)*.6);
    int lostInf=(int)Math.ceil(infantry*defenderLoss), lostCav=(int)Math.ceil(cavalry*defenderLoss*.75);
    infantry=Math.max(0,infantry-lostInf);cavalry=Math.max(0,cavalry-lostCav);
    double attackerLoss=lost?Math.min(.58,def/(double)(atk+def)*.75):Math.min(.92,.58+def/(double)(atk+def)*.45);
    int returnInf=Math.max(0,(int)Math.round(m.infantry*(1-attackerLoss)));
    int returnCav=Math.max(0,(int)Math.round(m.cavalry*(1-attackerLoss)));
    b.infantry+=returnInf;b.cavalry+=returnCav;
    if(lost){
      double pct=.10+.10*rng.nextDouble();
      int loot=(int)((wood+clay+iron+grain)*pct);
      wood*=1-pct;clay*=1-pct;iron*=1-pct;grain*=1-pct;
      if(atk>def*1.45 && rng.nextBoolean())damageRandomBuilding();
      addReport("Defeat vs "+b.name+": lost "+lostInf+" infantry, "+lostCav+" cavalry and about "+loot+" resources.");
    }else{
      heroXp+=18+b.level*5;checkHero();
      addReport("Victory vs "+b.name+": the attack was repelled. Lost "+lostInf+" infantry and "+lostCav+" cavalry.");
    }
  }

  void damageRandomBuilding(){
    String[] keys={"wall","warehouse","granary","barracks","smithy","academy","residence"};
    String k=keys[rng.nextInt(keys.length)];
    int lv=level(k);
    if(lv>0){setLevel(k,lv-1);addReport("Siege damage: "+label(k)+" fell to level "+(lv-1)+".");}
  }

  boolean launchRaid(int botIndex,int percent){
    if(botIndex<0||botIndex>=bots.size())return false;
    if(infantry+cavalry<2)return false;
    int sendInf=Math.max(1,infantry*percent/100), sendCav=cavalry*percent/100;
    infantry-=sendInf;cavalry-=sendCav;
    Bot b=bots.get(botIndex);
    missions.add(new Mission(false,botIndex,sendInf,sendCav,sim+travelSeconds(b.x,b.y),b.name+" raid"));
    addReport("Raid sent to "+b.name+" with "+sendInf+" infantry and "+sendCav+" cavalry.");
    save(); return true;
  }

  void resolveRaid(Mission m){
    Bot b=bots.get(m.botIndex);
    int atk=m.infantry*(12+smithy*2)+m.cavalry*(30+smithy*3)+hero*10;
    int def=Math.max(1,b.infantry*(12+b.level)+b.cavalry*(20+b.level*2));
    boolean win=atk>def*.82;
    if(win){
      double ratio=Math.min(.85,def/(double)(atk+def));
      int survInf=Math.max(0,(int)Math.round(m.infantry*(1-ratio*.55)));
      int survCav=Math.max(0,(int)Math.round(m.cavalry*(1-ratio*.45)));
      infantry+=survInf;cavalry+=survCav;
      b.infantry=Math.max(0,(int)(b.infantry*(.35+.25*rng.nextDouble())));
      b.cavalry=Math.max(0,(int)(b.cavalry*(.35+.30*rng.nextDouble())));
      int loot=(int)Math.min(b.stock,300+b.level*220+rng.nextInt(220));
      b.stock=Math.max(0,b.stock-loot);
      wood=Math.min(storageCap(),wood+loot*.35);clay=Math.min(storageCap(),clay+loot*.28);
      iron=Math.min(storageCap(),iron+loot*.22);grain=Math.min(granaryCap(),grain+loot*.15);
      heroXp+=24+b.level*8;checkHero();
      addReport("Raid victory at "+b.name+": "+loot+" resources taken. "+survInf+" infantry and "+survCav+" cavalry returned.");
    }else{
      int survInf=(int)Math.round(m.infantry*.22),survCav=(int)Math.round(m.cavalry*.32);
      infantry+=survInf;cavalry+=survCav;
      b.infantry=Math.max(0,(int)(b.infantry*.82));b.cavalry=Math.max(0,(int)(b.cavalry*.88));
      addReport("Raid failed at "+b.name+". Only "+survInf+" infantry and "+survCav+" cavalry returned.");
    }
  }

  void checkHero(){
    while(heroXp>=hero*100){heroXp-=hero*100;hero++;addReport("Hero Astra reached level "+hero+".");}
  }

  int level(String key){
    switch(key){
      case "wood":return woodField;case "clay":return clayField;case "iron":return ironField;case "crop":return cropField;
      case "main":return main;case "warehouse":return warehouse;case "granary":return granary;case "barracks":return barracks;
      case "rally":return rally;case "wall":return wall;case "smithy":return smithy;case "academy":return academy;
      case "residence":return residence;case "mansion":return mansion;
    } return 0;
  }

  void setLevel(String key,int v){
    switch(key){
      case "wood":woodField=v;break;case "clay":clayField=v;break;case "iron":ironField=v;break;case "crop":cropField=v;break;
      case "main":main=v;break;case "warehouse":warehouse=v;break;case "granary":granary=v;break;case "barracks":barracks=v;break;
      case "rally":rally=v;break;case "wall":wall=v;break;case "smithy":smithy=v;break;case "academy":academy=v;break;
      case "residence":residence=v;break;case "mansion":mansion=v;break;
    }
  }

  String requirement(String key){
    if(key.equals("barracks")&&main<2)return "Requires Main Hall level 2";
    if(key.equals("smithy")&&barracks<2)return "Requires Barracks level 2";
    if(key.equals("academy")&&main<3)return "Requires Main Hall level 3";
    if(key.equals("residence")&&main<4)return "Requires Main Hall level 4";
    if(key.equals("mansion")&&main<3)return "Requires Main Hall level 3";
    return "";
  }

  int[] buildCost(String key){
    int next=level(key)+1;
    int base=(key.equals("wood")||key.equals("clay")||key.equals("iron")||key.equals("crop"))?150:220;
    int c=(int)(base*Math.pow(1.48,next-1));
    return new int[]{c,(int)(c*.92),(int)(c*.82),(int)(c*.60)};
  }

  boolean canPay(int[] c){return wood>=c[0]&&clay>=c[1]&&iron>=c[2]&&grain>=c[3];}

  boolean queueBuild(String key){
    if(!buildKey.isEmpty())return false;
    if(!requirement(key).isEmpty())return false;
    int[] c=buildCost(key); if(!canPay(c))return false;
    wood-=c[0];clay-=c[1];iron-=c[2];grain-=c[3];
    buildKey=key;buildTarget=level(key)+1;
    double duration=(260+buildTarget*170)*Math.max(.45,1-main*.035);
    buildFinish=sim+duration;
    addReport("Construction started: "+label(key)+" level "+buildTarget+".");
    save();return true;
  }

  String label(String key){
    switch(key){
      case "wood":return "Woodcutters";case "clay":return "Clay Pits";case "iron":return "Iron Mines";case "crop":return "Croplands";
      case "main":return "Main Hall";case "warehouse":return "Warehouse";case "granary":return "Granary";case "barracks":return "Barracks";
      case "rally":return "Rally Point";case "wall":return "Stone Wall";case "smithy":return "Smithy";case "academy":return "Academy";
      case "residence":return "Residence";case "mansion":return "Hero Lodge";
    }return key;
  }

  boolean queueTrain(String type,int count){
    if(!trainType.isEmpty()||count<1)return false;
    if(barracks<1)return false;
    if(type.equals("scouts")&&academy<1)return false;
    if(type.equals("cavalry")&&(barracks<3||academy<2))return false;
    int cw=type.equals("infantry")?90:type.equals("scouts")?120:190;
    int ci=type.equals("infantry")?70:type.equals("scouts")?90:160;
    int cg=type.equals("infantry")?50:type.equals("scouts")?80:130;
    int cc=type.equals("infantry")?60:type.equals("scouts")?50:120;
    if(wood<cw*count||iron<ci*count||grain<cg*count||clay<cc*count)return false;
    wood-=cw*count;iron-=ci*count;grain-=cg*count;clay-=cc*count;
    trainType=type;trainCount=count;
    double per=type.equals("infantry")?240:type.equals("scouts")?300:470;
    trainFinish=sim+(per*count)/Math.max(1,1+barracks*.08);
    addReport("Training "+count+" "+unitLabel(type)+".");
    save();return true;
  }

  String unitLabel(String type){
    if(type.equals("infantry"))return "Shieldguard";if(type.equals("scouts"))return "Pathfinder";return "Iron Rider";
  }

  int botAt(int x,int y){
    for(int i=0;i<bots.size();i++)if(bots.get(i).x==x&&bots.get(i).y==y)return i;
    return -1;
  }

  int tileType(int x,int y){
    if(x==0&&y==0)return 9;
    if(botAt(x,y)>=0)return 8;
    int h=Math.abs((x*73856093)^(y*19349663)^0x5f3759df);
    int v=h%100;
    if(v<8)return 1;
    if(v<14)return 2;
    if(v<21)return 3;
    return 0;
  }

  String tileName(int x,int y){
    int t=tileType(x,y);
    if(t==9)return "Emberhold";
    int bi=botAt(x,y);if(bi>=0)return bots.get(bi).name;
    if(t==1)return "Woodland Oasis";if(t==2)return "Cropland Oasis";if(t==3)return "Ancient Ruins";
    return "Open Valley";
  }

  boolean exploreRuin(int x,int y){
    if(tileType(x,y)!=3)return false;
    String k=x+"|"+y;if(explored.contains(k))return false;
    explored.add(k);int reward=220+rng.nextInt(280);wood=Math.min(storageCap(),wood+reward*.3);clay=Math.min(storageCap(),clay+reward*.25);
    iron=Math.min(storageCap(),iron+reward*.25);grain=Math.min(granaryCap(),grain+reward*.2);heroXp+=35;checkHero();
    addReport("Astra explored ruins at ("+x+"|"+y+") and recovered "+reward+" resources.");save();return true;
  }

  boolean annexOasis(int x,int y){
    int t=tileType(x,y);if(t!=1&&t!=2)return false;
    if(mansion<1)return false;
    String k=x+"|"+y;if(oases.contains(k))return false;
    if(oases.size()>=Math.min(3,mansion))return false;
    oases.add(k);addReport("Oasis at ("+x+"|"+y+") annexed. Production bonus increased.");save();return true;
  }

  int incomingCount(){int n=0;for(Mission m:missions)if(m.enemy)n++;return n;}
  Mission soonestIncoming(){
    Mission best=null;for(Mission m:missions)if(m.enemy&&(best==null||m.arrival<best.arrival))best=m;return best;
  }

  String realEta(double simSeconds){
    int sec=(int)Math.max(0,Math.ceil(simSeconds/SPEED));
    return String.format(Locale.US,"%d:%02d",sec/60,sec%60);
  }

  String gameClock(){
    long s=(long)sim;int day=(int)(s/86400)+1;int hour=(int)((s%86400)/3600);int min=(int)((s%3600)/60);
    return String.format(Locale.US,"Day %d • %02d:%02d",day,hour,min);
  }

  void addReport(String text){
    reports.add(0,gameClock()+" — "+text);
    while(reports.size()>40)reports.remove(reports.size()-1);
  }
}
