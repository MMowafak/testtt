package com.ironvale.game;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Random;

class GameState {
  static final double SPEED=20.0;
  static final int WOOD=1, CLAY=2, IRON=3, CROP=4;
  static final int[] FIELD_TYPES={WOOD,CROP,WOOD,IRON,CLAY,CLAY,IRON,CROP,CROP,IRON,IRON,CROP,CROP,WOOD,CROP,CLAY,WOOD,CLAY};

  final SharedPreferences sp;
  final Random rng=new Random();
  final ArrayList<Bot> bots=new ArrayList<>();
  final ArrayList<Mission> missions=new ArrayList<>();
  final ArrayList<String> reports=new ArrayList<>();
  final HashSet<String> explored=new HashSet<>();
  final HashSet<String> oases=new HashSet<>();
  final int[] fields=new int[18];

  boolean started=false,paused=true;
  long lastReal=System.currentTimeMillis();
  double sim=0;
  double wood=800,clay=800,iron=800,grain=800;

  int main=1,warehouse=1,granary=1,rally=1,barracks=0,academy=0,smithy=0,stable=0,residence=0,mansion=0,market=0,wall=0;
  int infantry=14,scouts=0,cavalry=0;
  int hero=1,heroXp=0,heroHealth=100,heroWeapon=0,heroArmor=0;

  String buildKey="",trainType="";
  int buildTarget=0,trainCount=0;
  double buildFinish=0,trainFinish=0;
  boolean heroAway=false;
  double heroFinish=0;
  int heroX=0,heroY=0;

  GameState(Context c){
    sp=c.getSharedPreferences("ironvale_reference_v5",0);
    load();
  }

  void newRealm(){
    started=true;paused=false;lastReal=System.currentTimeMillis();sim=0;
    wood=clay=iron=grain=800;
    for(int i=0;i<fields.length;i++)fields[i]=1;
    main=warehouse=granary=rally=1;barracks=academy=smithy=stable=residence=mansion=market=wall=0;
    infantry=14;scouts=0;cavalry=0;hero=1;heroXp=0;heroHealth=100;heroWeapon=0;heroArmor=0;
    buildKey="";buildTarget=0;buildFinish=0;trainType="";trainCount=0;trainFinish=0;
    heroAway=false;heroFinish=0;heroX=heroY=0;
    bots.clear();missions.clear();reports.clear();explored.clear();oases.clear();
    seedBots();
    addReport("Realm founded. Rival villages are already growing around Emberhold.");
    addReport("Realm speed is x20. One real minute equals twenty game minutes.");
    save();
  }

  void seedBots(){
    bots.clear();
    addBot("Iron Wolves",4,2,2,24,1,1.00);
    addBot("Red Fen",-5,3,2,21,2,1.10);
    addBot("Oakclaw",2,-6,3,28,3,0.95);
    addBot("Duskguard",-6,-4,3,31,3,1.20);
    addBot("Ash Crown",7,-3,4,38,5,1.28);
    addBot("Obsidian Host",0,8,5,48,8,1.40);
    addBot("Silver Boar",9,4,4,36,6,1.05);
    addBot("Marsh King",-9,0,4,39,5,1.32);
    addBot("Northwatch",-3,10,5,52,7,1.20);
    addBot("Sun Legion",10,-8,6,60,11,1.45);
    for(Bot b:bots)b.nextAttack=sim+1800+rng.nextInt(1800);
  }

  void addBot(String n,int x,int y,int level,int inf,int cav,double aggression){
    Bot b=new Bot(n,x,y,level,inf,cav,aggression);
    b.stock=1000+level*500;
    bots.add(b);
  }

  void load(){
    started=sp.getBoolean("started",false);
    if(!started){for(int i=0;i<fields.length;i++)fields[i]=1;paused=true;return;}
    paused=sp.getBoolean("paused",false);
    lastReal=sp.getLong("lastReal",System.currentTimeMillis());
    sim=dbl("sim",0);wood=dbl("wood",800);clay=dbl("clay",800);iron=dbl("iron",800);grain=dbl("grain",800);
    readFields(sp.getString("fields",""));
    main=sp.getInt("main",1);warehouse=sp.getInt("warehouse",1);granary=sp.getInt("granary",1);rally=sp.getInt("rally",1);
    barracks=sp.getInt("barracks",0);academy=sp.getInt("academy",0);smithy=sp.getInt("smithy",0);stable=sp.getInt("stable",0);
    residence=sp.getInt("residence",0);mansion=sp.getInt("mansion",0);market=sp.getInt("market",0);wall=sp.getInt("wall",0);
    infantry=sp.getInt("infantry",14);scouts=sp.getInt("scouts",0);cavalry=sp.getInt("cavalry",0);
    hero=sp.getInt("hero",1);heroXp=sp.getInt("heroXp",0);heroHealth=sp.getInt("heroHealth",100);
    heroWeapon=sp.getInt("heroWeapon",0);heroArmor=sp.getInt("heroArmor",0);
    buildKey=sp.getString("buildKey","");buildTarget=sp.getInt("buildTarget",0);buildFinish=dbl("buildFinish",0);
    trainType=sp.getString("trainType","");trainCount=sp.getInt("trainCount",0);trainFinish=dbl("trainFinish",0);
    heroAway=sp.getBoolean("heroAway",false);heroFinish=dbl("heroFinish",0);heroX=sp.getInt("heroX",0);heroY=sp.getInt("heroY",0);
    try{
      JSONArray a=new JSONArray(sp.getString("bots","[]"));for(int i=0;i<a.length();i++)bots.add(Bot.from(a.getJSONObject(i)));
      JSONArray m=new JSONArray(sp.getString("missions","[]"));for(int i=0;i<m.length();i++)missions.add(Mission.from(m.getJSONObject(i)));
      JSONArray r=new JSONArray(sp.getString("reports","[]"));for(int i=0;i<r.length();i++)reports.add(r.optString(i));
      JSONArray e=new JSONArray(sp.getString("explored","[]"));for(int i=0;i<e.length();i++)explored.add(e.optString(i));
      JSONArray o=new JSONArray(sp.getString("oases","[]"));for(int i=0;i<o.length();i++)oases.add(o.optString(i));
    }catch(Exception ignored){}
    if(bots.isEmpty())seedBots();
  }

  void readFields(String str){
    String[] a=str.split(",");
    for(int i=0;i<fields.length;i++){
      if(i<a.length){try{fields[i]=Math.max(0,Integer.parseInt(a[i]));}catch(Exception e){fields[i]=1;}}
      else fields[i]=1;
    }
  }

  String fieldString(){
    StringBuilder b=new StringBuilder();
    for(int i=0;i<fields.length;i++){if(i>0)b.append(',');b.append(fields[i]);}
    return b.toString();
  }

  double dbl(String k,double def){return Double.longBitsToDouble(sp.getLong(k,Double.doubleToLongBits(def)));}

  void save(){
    JSONArray ba=new JSONArray(),ma=new JSONArray(),ra=new JSONArray(),ea=new JSONArray(),oa=new JSONArray();
    for(Bot b:bots)ba.put(b.json());for(Mission m:missions)ma.put(m.json());for(String r:reports)ra.put(r);
    for(String e:explored)ea.put(e);for(String o:oases)oa.put(o);
    sp.edit().putBoolean("started",started).putBoolean("paused",paused).putLong("lastReal",lastReal)
      .putLong("sim",Double.doubleToLongBits(sim)).putLong("wood",Double.doubleToLongBits(wood)).putLong("clay",Double.doubleToLongBits(clay))
      .putLong("iron",Double.doubleToLongBits(iron)).putLong("grain",Double.doubleToLongBits(grain)).putString("fields",fieldString())
      .putInt("main",main).putInt("warehouse",warehouse).putInt("granary",granary).putInt("rally",rally).putInt("barracks",barracks)
      .putInt("academy",academy).putInt("smithy",smithy).putInt("stable",stable).putInt("residence",residence).putInt("mansion",mansion)
      .putInt("market",market).putInt("wall",wall).putInt("infantry",infantry).putInt("scouts",scouts).putInt("cavalry",cavalry)
      .putInt("hero",hero).putInt("heroXp",heroXp).putInt("heroHealth",heroHealth).putInt("heroWeapon",heroWeapon).putInt("heroArmor",heroArmor)
      .putString("buildKey",buildKey).putInt("buildTarget",buildTarget).putLong("buildFinish",Double.doubleToLongBits(buildFinish))
      .putString("trainType",trainType).putInt("trainCount",trainCount).putLong("trainFinish",Double.doubleToLongBits(trainFinish))
      .putBoolean("heroAway",heroAway).putLong("heroFinish",Double.doubleToLongBits(heroFinish)).putInt("heroX",heroX).putInt("heroY",heroY)
      .putString("bots",ba.toString()).putString("missions",ma.toString()).putString("reports",ra.toString())
      .putString("explored",ea.toString()).putString("oases",oa.toString()).apply();
  }

  void tick(long now){
    if(!started){lastReal=now;return;}
    if(paused){lastReal=now;return;}
    double real=Math.max(0,(now-lastReal)/1000.0);if(real<=0)return;
    lastReal=now;double ds=real*SPEED;sim+=ds;
    produce(ds);processConstruction();processTraining();processHero();processBots(ds);processMissions();
    if(((long)sim)%120<6)save();
  }

  double productionPerHour(int type){
    double total=0;
    for(int i=0;i<fields.length;i++)if(FIELD_TYPES[i]==type)total+=fieldProduction(fields[i]);
    double oasisBonus=1.0+oases.size()*0.10;
    return total*oasisBonus;
  }

  double fieldProduction(int level){
    if(level<=0)return 0;
    return 35*Math.pow(level,1.32);
  }

  void produce(double ds){
    wood=Math.min(storageCap(),wood+productionPerHour(WOOD)*ds/3600.0);
    clay=Math.min(storageCap(),clay+productionPerHour(CLAY)*ds/3600.0);
    iron=Math.min(storageCap(),iron+productionPerHour(IRON)*ds/3600.0);
    double upkeep=infantry+scouts+cavalry*3+1;
    grain=Math.min(granaryCap(),Math.max(0,grain+(productionPerHour(CROP)-upkeep)*ds/3600.0));
  }

  int storageCap(){return 800+warehouse*2400;}
  int granaryCap(){return 800+granary*2400;}
  int cropUpkeep(){return infantry+scouts+cavalry*3+1;}
  int defensePower(){return infantry*(14+smithy*2)+cavalry*(20+smithy*3)+wall*42+hero*(25+heroArmor*4)+residence*10;}
  int attackPower(){return infantry*(12+smithy*2)+cavalry*(31+smithy*3)+hero*(22+heroWeapon*6);}

  int fieldType(int i){return FIELD_TYPES[Math.max(0,Math.min(17,i))];}
  String fieldName(int type){if(type==WOOD)return "Woodcutter";if(type==CLAY)return "Clay Pit";if(type==IRON)return "Iron Mine";return "Cropland";}
  int fieldCount(int type){int n=0;for(int t:FIELD_TYPES)if(t==type)n++;return n;}

  int[] fieldCost(int index){
    int lv=fields[index]+1;int type=FIELD_TYPES[index];
    double mult=Math.pow(1.67,lv-1);
    int base=type==CROP?70:80;
    int w=(int)(base*mult*(type==WOOD?.72:1.05));
    int c=(int)(base*mult*(type==CLAY?.72:1.00));
    int ir=(int)(base*mult*(type==IRON?.72:1.00));
    int g=(int)(base*mult*(type==CROP?.66:.78));
    return new int[]{Math.max(25,w),Math.max(25,c),Math.max(25,ir),Math.max(20,g)};
  }

  boolean queueField(int index){
    if(!buildKey.isEmpty()||index<0||index>=fields.length)return false;
    int[] c=fieldCost(index);if(!canPay(c))return false;
    pay(c);buildKey="field:"+index;buildTarget=fields[index]+1;
    buildFinish=sim+(260+buildTarget*150)*Math.max(.45,1-main*.035);
    addReport(fieldName(FIELD_TYPES[index])+" "+(index+1)+" upgrading to level "+buildTarget+".");save();return true;
  }

  void processConstruction(){
    if(buildKey.isEmpty()||sim<buildFinish)return;
    if(buildKey.startsWith("field:")){
      try{int idx=Integer.parseInt(buildKey.substring(6));if(idx>=0&&idx<18)fields[idx]=buildTarget;}catch(Exception ignored){}
      addReport("Resource field reached level "+buildTarget+".");
    }else{
      setLevel(buildKey,buildTarget);addReport(label(buildKey)+" reached level "+buildTarget+".");
    }
    buildKey="";buildTarget=0;buildFinish=0;
  }

  void processTraining(){
    if(trainType.isEmpty()||sim<trainFinish)return;
    if(trainType.equals("infantry"))infantry+=trainCount;else if(trainType.equals("scouts"))scouts+=trainCount;else cavalry+=trainCount;
    addReport(trainCount+" "+unitLabel(trainType)+" finished training.");
    trainType="";trainCount=0;trainFinish=0;
  }

  void processHero(){
    if(!heroAway||sim<heroFinish)return;
    heroAway=false;
    String k=heroX+"|"+heroY;explored.add(k);
    int reward=280+rng.nextInt(420);
    wood=Math.min(storageCap(),wood+reward*.28);clay=Math.min(storageCap(),clay+reward*.25);
    iron=Math.min(storageCap(),iron+reward*.25);grain=Math.min(granaryCap(),grain+reward*.22);
    heroXp+=40+rng.nextInt(30);
    if(rng.nextInt(4)==0)heroWeapon=Math.min(5,heroWeapon+1);
    else if(rng.nextInt(4)==0)heroArmor=Math.min(5,heroArmor+1);
    checkHero();
    addReport("Astra returned from ruins ("+heroX+"|"+heroY+") with "+reward+" resources.");save();
  }

  boolean launchAdventure(int x,int y){
    if(heroAway||tileType(x,y)!=3||explored.contains(x+"|"+y))return false;
    heroAway=true;heroX=x;heroY=y;heroFinish=sim+travelSeconds(x,y)*1.6;
    addReport("Astra departed for ruins at ("+x+"|"+y+").");save();return true;
  }

  void processBots(double ds){
    for(int i=0;i<bots.size();i++){
      Bot b=bots.get(i);b.stock+=(100+b.level*70)*ds/3600.0;b.growth+=ds;
      while(b.growth>=1200){
        b.growth-=1200;b.infantry+=1+Math.max(0,b.level/3);
        if(b.level>=3&&rng.nextInt(3)==0)b.cavalry++;
        if(rng.nextInt(14)==0)b.level=Math.min(12,b.level+1);
      }
      if(sim>=b.nextAttack&&!hasIncomingFrom(i)){
        launchBotAttack(i);b.nextAttack=sim+(1800+rng.nextInt(2600))/Math.max(.8,b.aggression);
      }
    }
  }

  boolean hasIncomingFrom(int bot){for(Mission m:missions)if(m.enemy&&m.botIndex==bot)return true;return false;}

  void launchBotAttack(int idx){
    Bot b=bots.get(idx);
    int si=Math.max(3,(int)(b.infantry*(.24+.13*rng.nextDouble()))),sc=Math.max(0,(int)(b.cavalry*(.18+.22*rng.nextDouble())));
    si=Math.min(si,b.infantry);sc=Math.min(sc,b.cavalry);b.infantry-=si;b.cavalry-=sc;
    double eta=sim+travelSeconds(b.x,b.y);
    missions.add(new Mission(true,idx,si,sc,eta,b.name+" attack"));
    addReport("Incoming attack: "+b.name+" reaches Emberhold in "+realEta(eta-sim)+".");
  }

  double travelSeconds(int x,int y){return 240+Math.sqrt(x*x+y*y)*110;}

  void processMissions(){
    for(int i=missions.size()-1;i>=0;i--){
      Mission m=missions.get(i);if(sim<m.arrival)continue;
      if(m.enemy)resolveEnemy(m);else resolveRaid(m);missions.remove(i);
    }
  }

  void resolveEnemy(Mission m){
    if(m.botIndex<0||m.botIndex>=bots.size())return;
    Bot b=bots.get(m.botIndex);
    int atk=m.infantry*(12+b.level)+m.cavalry*(27+b.level*2),def=Math.max(1,defensePower());
    boolean defeat=atk>def;
    double dl=defeat?Math.min(.80,.38+atk/(double)(atk+def)*.48):Math.min(.52,atk/(double)(atk+def)*.58);
    int li=(int)Math.ceil(infantry*dl),lc=(int)Math.ceil(cavalry*dl*.75);infantry=Math.max(0,infantry-li);cavalry=Math.max(0,cavalry-lc);
    double al=defeat?Math.min(.60,def/(double)(atk+def)*.72):Math.min(.94,.58+def/(double)(atk+def)*.45);
    b.infantry+=Math.max(0,(int)Math.round(m.infantry*(1-al)));b.cavalry+=Math.max(0,(int)Math.round(m.cavalry*(1-al)));
    heroHealth=Math.max(12,heroHealth-(defeat?rng.nextInt(18)+8:rng.nextInt(8)));
    if(defeat){
      double pct=.08+.08*rng.nextDouble();int loot=(int)((wood+clay+iron+grain)*pct);
      wood*=1-pct;clay*=1-pct;iron*=1-pct;grain*=1-pct;
      if(atk>def*1.5&&rng.nextBoolean())damageBuilding();
      addReport("Defeat against "+b.name+". "+li+" infantry and "+lc+" cavalry lost; about "+loot+" resources stolen.");
    }else{
      heroXp+=18+b.level*5;checkHero();addReport("Defense victory against "+b.name+". "+li+" infantry and "+lc+" cavalry lost.");
    }
  }

  void damageBuilding(){
    String[] k={"wall","warehouse","granary","barracks","smithy","academy","residence","stable"};
    String key=k[rng.nextInt(k.length)];int lv=level(key);
    if(lv>0){setLevel(key,lv-1);addReport("Siege damage: "+label(key)+" fell to level "+(lv-1)+".");}
  }

  boolean launchRaid(int bot,int pct){
    if(bot<0||bot>=bots.size()||infantry+cavalry<2)return false;
    int si=Math.max(1,infantry*pct/100),sc=cavalry*pct/100;infantry-=si;cavalry-=sc;
    Bot b=bots.get(bot);missions.add(new Mission(false,bot,si,sc,sim+travelSeconds(b.x,b.y),b.name+" raid"));
    addReport("Raid launched at "+b.name+" with "+si+" infantry and "+sc+" cavalry.");save();return true;
  }

  void resolveRaid(Mission m){
    Bot b=bots.get(m.botIndex);
    int atk=m.infantry*(12+smithy*2)+m.cavalry*(31+smithy*3)+hero*(10+heroWeapon*3);
    int def=Math.max(1,b.infantry*(12+b.level)+b.cavalry*(20+b.level*2));boolean win=atk>def*.82;
    if(win){
      double ratio=Math.min(.86,def/(double)(atk+def));
      int ri=Math.max(0,(int)Math.round(m.infantry*(1-ratio*.55))),rc=Math.max(0,(int)Math.round(m.cavalry*(1-ratio*.45)));
      infantry+=ri;cavalry+=rc;b.infantry=Math.max(0,(int)(b.infantry*(.38+.22*rng.nextDouble())));b.cavalry=Math.max(0,(int)(b.cavalry*(.40+.22*rng.nextDouble())));
      int loot=(int)Math.min(b.stock,320+b.level*220+rng.nextInt(280));b.stock=Math.max(0,b.stock-loot);
      wood=Math.min(storageCap(),wood+loot*.34);clay=Math.min(storageCap(),clay+loot*.27);iron=Math.min(storageCap(),iron+loot*.23);grain=Math.min(granaryCap(),grain+loot*.16);
      heroXp+=25+b.level*8;checkHero();addReport("Raid victory at "+b.name+": "+loot+" resources taken. "+ri+" infantry and "+rc+" cavalry returned.");
    }else{
      int ri=(int)Math.round(m.infantry*.20),rc=(int)Math.round(m.cavalry*.30);infantry+=ri;cavalry+=rc;
      b.infantry=Math.max(0,(int)(b.infantry*.84));b.cavalry=Math.max(0,(int)(b.cavalry*.90));
      addReport("Raid failed at "+b.name+". "+ri+" infantry and "+rc+" cavalry returned.");
    }
  }

  void checkHero(){while(heroXp>=hero*100){heroXp-=hero*100;hero++;heroHealth=100;addReport("Astra reached level "+hero+".");}}

  int level(String k){
    switch(k){
      case "main":return main;case "warehouse":return warehouse;case "granary":return granary;case "rally":return rally;case "barracks":return barracks;
      case "academy":return academy;case "smithy":return smithy;case "stable":return stable;case "residence":return residence;case "mansion":return mansion;
      case "market":return market;case "wall":return wall;
    }return 0;
  }

  void setLevel(String k,int v){
    switch(k){
      case "main":main=v;break;case "warehouse":warehouse=v;break;case "granary":granary=v;break;case "rally":rally=v;break;case "barracks":barracks=v;break;
      case "academy":academy=v;break;case "smithy":smithy=v;break;case "stable":stable=v;break;case "residence":residence=v;break;case "mansion":mansion=v;break;
      case "market":market=v;break;case "wall":wall=v;break;
    }
  }

  String label(String k){
    switch(k){
      case "main":return "Main Hall";case "warehouse":return "Warehouse";case "granary":return "Granary";case "rally":return "Rally Point";case "barracks":return "Barracks";
      case "academy":return "Academy";case "smithy":return "Smithy";case "stable":return "Stable";case "residence":return "Residence";case "mansion":return "Hero Lodge";
      case "market":return "Marketplace";case "wall":return "Stone Wall";
    }return k;
  }

  String requirement(String k){
    if(k.equals("barracks")&&main<2)return "Requires Main Hall level 2";
    if(k.equals("academy")&&main<3)return "Requires Main Hall level 3";
    if(k.equals("smithy")&&barracks<2)return "Requires Barracks level 2";
    if(k.equals("stable")&&(barracks<3||academy<2))return "Requires Barracks 3 and Academy 2";
    if(k.equals("residence")&&main<4)return "Requires Main Hall level 4";
    if(k.equals("mansion")&&main<3)return "Requires Main Hall level 3";
    if(k.equals("market")&&main<3)return "Requires Main Hall level 3";
    return "";
  }

  int[] buildCost(String k){
    int next=level(k)+1;int base=k.equals("wall")?150:k.equals("warehouse")||k.equals("granary")?180:220;
    int c=(int)(base*Math.pow(1.52,next-1));
    return new int[]{c,(int)(c*.92),(int)(c*.82),(int)(c*.62)};
  }

  boolean queueBuild(String k){
    if(!buildKey.isEmpty()||!requirement(k).isEmpty())return false;
    int[] c=buildCost(k);if(!canPay(c))return false;pay(c);
    buildKey=k;buildTarget=level(k)+1;buildFinish=sim+(320+buildTarget*190)*Math.max(.42,1-main*.035);
    addReport("Construction started: "+label(k)+" level "+buildTarget+".");save();return true;
  }

  boolean canPay(int[] c){return wood>=c[0]&&clay>=c[1]&&iron>=c[2]&&grain>=c[3];}
  void pay(int[] c){wood-=c[0];clay-=c[1];iron-=c[2];grain-=c[3];}

  boolean queueTrain(String type,int count){
    if(!trainType.isEmpty()||count<1||barracks<1)return false;
    if(type.equals("scouts")&&academy<1)return false;
    if(type.equals("cavalry")&&stable<1)return false;
    int cw=type.equals("infantry")?90:type.equals("scouts")?125:195;
    int cc=type.equals("infantry")?65:type.equals("scouts")?55:120;
    int ci=type.equals("infantry")?75:type.equals("scouts")?100:165;
    int cg=type.equals("infantry")?55:type.equals("scouts")?75:135;
    if(wood<cw*count||clay<cc*count||iron<ci*count||grain<cg*count)return false;
    wood-=cw*count;clay-=cc*count;iron-=ci*count;grain-=cg*count;trainType=type;trainCount=count;
    double per=type.equals("infantry")?260:type.equals("scouts")?320:500;trainFinish=sim+(per*count)/Math.max(1,1+barracks*.07+stable*.06);
    addReport("Training "+count+" "+unitLabel(type)+".");save();return true;
  }

  String unitLabel(String t){if(t.equals("infantry"))return "Shieldguard";if(t.equals("scouts"))return "Pathfinder";return "Iron Rider";}

  int botAt(int x,int y){for(int i=0;i<bots.size();i++)if(bots.get(i).x==x&&bots.get(i).y==y)return i;return -1;}

  int tileType(int x,int y){
    if(x==0&&y==0)return 9;if(botAt(x,y)>=0)return 8;
    int h=Math.abs((x*73856093)^(y*19349663)^0x5f3759df),v=h%100;
    if(v<8)return 1;if(v<15)return 2;if(v<22)return 3;return 0;
  }

  String tileName(int x,int y){
    int b=botAt(x,y);if(b>=0)return bots.get(b).name;
    int t=tileType(x,y);if(t==9)return "Emberhold";if(t==1)return "Woodland Oasis";if(t==2)return "Cropland Oasis";if(t==3)return "Ancient Ruins";return "Open Valley";
  }

  boolean annexOasis(int x,int y){
    int t=tileType(x,y);if((t!=1&&t!=2)||mansion<1)return false;
    String k=x+"|"+y;if(oases.contains(k)||oases.size()>=Math.min(3,mansion))return false;
    oases.add(k);addReport("Oasis ("+x+"|"+y+") annexed. All production +10%.");save();return true;
  }

  int incomingCount(){int n=0;for(Mission m:missions)if(m.enemy)n++;return n;}
  Mission soonestIncoming(){Mission b=null;for(Mission m:missions)if(m.enemy&&(b==null||m.arrival<b.arrival))b=m;return b;}

  String realEta(double simSec){
    int sec=(int)Math.max(0,Math.ceil(simSec/SPEED));return String.format(Locale.US,"%d:%02d",sec/60,sec%60);
  }

  String gameClock(){
    long v=(long)sim;int day=(int)(v/86400)+1,h=(int)((v%86400)/3600),m=(int)((v%3600)/60);
    return String.format(Locale.US,"Day %d • %02d:%02d",day,h,m);
  }

  String quest(){
    if(fields[0]<2)return "Quest: upgrade any resource field";
    if(main<2)return "Quest: upgrade Main Hall to level 2";
    if(barracks<1)return "Quest: build a Barracks";
    if(infantry<20)return "Quest: train 6 Shieldguards";
    if(reports.size()<6)return "Quest: scout the world and raid a rival";
    return "Goal: expand, raid and survive the rival warlords";
  }

  void addReport(String text){
    reports.add(0,gameClock()+" — "+text);while(reports.size()>60)reports.remove(reports.size()-1);
  }
}
