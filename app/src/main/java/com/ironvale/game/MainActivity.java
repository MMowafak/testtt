package com.ironvale.game;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.*;
import android.view.*;
import android.widget.*;

public class MainActivity extends Activity {
  @Override public void onCreate(Bundle b){
    super.onCreate(b);
    getWindow().setStatusBarColor(Color.rgb(14,23,19));
    getWindow().setNavigationBarColor(Color.rgb(14,23,19));
    setContentView(new GameView(this));
  }
}

class GameView extends View {
  Paint p=new Paint(3);
  int wood=500,clay=450,iron=420,grain=600,keep=1,lumber=1,quarry=1,mine=1,farm=1,barracks=0,army=3,hero=1,xp=0,tab=0;
  int bg=Color.rgb(14,23,19), panel=Color.rgb(28,40,34), panel2=Color.rgb(39,54,45), gold=Color.rgb(216,170,74), cream=Color.rgb(240,234,214), muted=Color.rgb(170,182,173), green=Color.rgb(78,145,92), red=Color.rgb(178,72,66);
  SharedPreferences sp;
  String[] tabs={"Village","Build","World","Hero"};
  GameView(Context c){
    super(c);
    sp=c.getSharedPreferences("ironvale",0);
    wood=sp.getInt("wood",wood); clay=sp.getInt("clay",clay); iron=sp.getInt("iron",iron); grain=sp.getInt("grain",grain);
    keep=sp.getInt("keep",keep); lumber=sp.getInt("lumber",lumber); quarry=sp.getInt("quarry",quarry); mine=sp.getInt("mine",mine); farm=sp.getInt("farm",farm);
    barracks=sp.getInt("barracks",barracks); army=sp.getInt("army",army); hero=sp.getInt("hero",hero); xp=sp.getInt("xp",xp);
  }
  float d(float v){return v*getResources().getDisplayMetrics().density;}
  void tx(Canvas c,String s,float x,float y,float sz,int col,boolean bold){p.setColor(col);p.setTextSize(d(sz));p.setTypeface(Typeface.create("sans",bold?Typeface.BOLD:Typeface.NORMAL));c.drawText(s,x,y,p);}
  void box(Canvas c,float l,float t,float r,float b,float rad,int col){p.setColor(col);c.drawRoundRect(l,t,r,b,d(rad),d(rad),p);}
  void save(){sp.edit().putInt("wood",wood).putInt("clay",clay).putInt("iron",iron).putInt("grain",grain).putInt("keep",keep).putInt("lumber",lumber).putInt("quarry",quarry).putInt("mine",mine).putInt("farm",farm).putInt("barracks",barracks).putInt("army",army).putInt("hero",hero).putInt("xp",xp).apply();}
  int cost(int lv){return 120+lv*90;}
  boolean spend(int n){if(wood<n||clay<n||iron<n||grain<n/2)return false;wood-=n;clay-=n;iron-=n;grain-=n/2;return true;}
  @Override protected void onDraw(Canvas c){
    super.onDraw(c); c.drawColor(bg);
    tx(c,"IRONVALE",d(16),d(30),18,gold,true); tx(c,"EMBERHOLD • Realm 7",d(16),d(48),9,muted,false);
    float y=d(60), gap=d(5), cw=(getWidth()-d(26)-gap*3)/4; int[] vals={wood,clay,iron,grain}; String[] names={"WOOD","CLAY","IRON","GRAIN"};
    for(int i=0;i<4;i++){float x=d(13)+i*(cw+gap);box(c,x,y,x+cw,y+d(42),9,panel);tx(c,names[i],x+d(7),y+d(14),7,muted,true);tx(c,""+vals[i],x+d(7),y+d(31),11,cream,true);}
    if(tab==0) village(c); else if(tab==1) build(c); else if(tab==2) world(c); else hero(c);
    nav(c);
  }
  void village(Canvas c){
    float y=d(120); tx(c,"Your settlement",d(16),y,20,cream,true); tx(c,"Tap a district to manage it.",d(16),y+d(18),9,muted,false);
    y+=d(34); box(c,d(14),y,getWidth()-d(14),y+d(282),18,panel);
    district(c,d(28),y+d(20),"LUMBER","Lv "+lumber,Color.rgb(78,122,73));
    district(c,getWidth()-d(132),y+d(20),"FARMLAND","Lv "+farm,Color.rgb(131,122,67));
    district(c,d(28),y+d(188),"CLAY","Lv "+quarry,Color.rgb(150,91,62));
    district(c,getWidth()-d(132),y+d(188),"IRON","Lv "+mine,Color.rgb(80,101,107));
    float kx=getWidth()/2f-d(50), ky=y+d(100); box(c,kx,ky,kx+d(100),ky+d(80),14,Color.rgb(66,64,50)); tx(c,"KEEP",kx+d(14),ky+d(31),13,gold,true);tx(c,"Level "+keep,kx+d(14),ky+d(52),10,cream,true);
    y+=d(300); box(c,d(14),y,getWidth()-d(14),y+d(68),13,panel);
    tx(c,"Army",d(28),y+d(25),9,muted,true);tx(c,army+" units",d(28),y+d(49),14,cream,true);
    tx(c,"Power",getWidth()/2f,y+d(25),9,muted,true);tx(c,""+(army*18+hero*8+keep*10),getWidth()/2f,y+d(49),14,gold,true);
  }
  void district(Canvas c,float x,float y,String a,String b,int col){box(c,x,y,x+d(104),y+d(66),13,col);tx(c,a,x+d(10),y+d(27),9,Color.WHITE,true);tx(c,b,x+d(10),y+d(49),9,Color.WHITE,false);}
  void build(Canvas c){
    float y=d(120);tx(c,"Build & Economy",d(16),y,20,cream,true);tx(c,"Upgrade fields and unlock troops.",d(16),y+d(18),9,muted,false);y+=d(38);
    row(c,y,"Lumber Camp","Wood production",lumber); y+=d(72);
    row(c,y,"Clay Quarry","Clay production",quarry); y+=d(72);
    row(c,y,"Iron Mine","Iron production",mine); y+=d(72);
    row(c,y,"Farmland","Grain production",farm); y+=d(72);
    row(c,y,"Barracks",barracks>0?"Train troops":"Unlock military",barracks);
  }
  void row(Canvas c,float y,String a,String b,int lv){box(c,d(14),y,getWidth()-d(14),y+d(62),12,panel);tx(c,a,d(27),y+d(24),12,cream,true);tx(c,b,d(27),y+d(44),8,muted,false);box(c,getWidth()-d(92),y+d(13),getWidth()-d(28),y+d(49),10,panel2);tx(c,lv==0?"BUILD":"LV "+lv,getWidth()-d(81),y+d(36),9,gold,true);}
  void world(Canvas c){
    float y=d(120);tx(c,"Frontier",d(16),y,20,cream,true);tx(c,"Raid targets for loot and XP.",d(16),y+d(18),9,muted,false);y+=d(42);
    target(c,y,"Ashen Camp","Low risk",1,green);y+=d(91);
    target(c,y,"Wolf Banner","Medium risk",2,gold);y+=d(91);
    target(c,y,"Blackstone Fort","High risk",3,red);
  }
  void target(Canvas c,float y,String a,String b,int risk,int col){box(c,d(14),y,getWidth()-d(14),y+d(78),13,panel);box(c,d(27),y+d(17),d(70),y+d(60),12,col);tx(c,""+risk,d(43),y+d(46),13,Color.WHITE,true);tx(c,a,d(83),y+d(28),12,cream,true);tx(c,b,d(83),y+d(49),8,muted,false);tx(c,"RAID",getWidth()-d(65),y+d(46),8,gold,true);}
  void hero(Canvas c){
    float y=d(120);tx(c,"Hero Astra",d(16),y,20,cream,true);tx(c,"Commander of Emberhold",d(16),y+d(18),9,muted,false);y+=d(42);
    box(c,d(14),y,getWidth()-d(14),y+d(150),18,panel);box(c,d(28),y+d(24),d(102),y+d(98),37,Color.rgb(72,91,74));tx(c,"A",d(54),y+d(72),24,gold,true);
    tx(c,"LEVEL "+hero,d(120),y+d(42),10,gold,true);tx(c,"XP "+xp+" / "+(hero*100),d(120),y+d(67),10,cream,true);
    y+=d(170); action(c,y,"EXPEDITION","Explore ruins for rewards");y+=d(78);action(c,y,"TRAIN TROOPS",barracks>0?"Recruit Shieldguards":"Build Barracks first");y+=d(78);action(c,y,"NEW REALM","Reset local progress");
  }
  void action(Canvas c,float y,String a,String b){box(c,d(14),y,getWidth()-d(14),y+d(64),12,panel);tx(c,a,d(28),y+d(27),10,gold,true);tx(c,b,d(28),y+d(47),8,muted,false);tx(c,"›",getWidth()-d(36),y+d(40),18,cream,true);}
  void nav(Canvas c){float y=getHeight()-d(68);box(c,d(10),y,getWidth()-d(10),getHeight()-d(9),16,Color.rgb(20,31,26));float cw=(getWidth()-d(20))/4f;for(int i=0;i<4;i++){float x=d(10)+cw*i;if(i==tab)box(c,x+d(4),y+d(7),x+cw-d(4),getHeight()-d(16),12,panel2);tx(c,tabs[i],x+d(12),y+d(35),9,i==tab?gold:muted,true);}}
  void upgrade(String key){
    int lv=key.equals("lumber")?lumber:key.equals("quarry")?quarry:key.equals("mine")?mine:key.equals("farm")?farm:barracks;
    int c=cost(lv+1); if(!spend(c)){Toast.makeText(getContext(),"Not enough resources",Toast.LENGTH_SHORT).show();return;}
    if(key.equals("lumber"))lumber++; else if(key.equals("quarry"))quarry++; else if(key.equals("mine"))mine++; else if(key.equals("farm"))farm++; else barracks++;
    save();invalidate();
  }
  void popup(String key,String title){int lv=key.equals("lumber")?lumber:key.equals("quarry")?quarry:key.equals("mine")?mine:key.equals("farm")?farm:barracks;int c=cost(lv+1);new AlertDialog.Builder(getContext()).setTitle(title+" • Level "+lv).setMessage("Next level costs "+c+" of wood, clay and iron, plus "+(c/2)+" grain.").setNegativeButton("Close",null).setPositiveButton("Upgrade",(d,w)->upgrade(key)).show();}
  void raid(int risk){
    if(army<1){Toast.makeText(getContext(),"Train troops first",Toast.LENGTH_SHORT).show();return;}
    int power=army*18+hero*8, need=25+risk*35;
    if(power>=need){int reward=80+risk*120;wood+=reward;clay+=reward;iron+=reward/2;xp+=20*risk;Toast.makeText(getContext(),"Victory! Loot secured.",Toast.LENGTH_SHORT).show();}
    else{army=Math.max(0,army-1);Toast.makeText(getContext(),"Raid failed. One unit was lost.",Toast.LENGTH_SHORT).show();}
    while(xp>=hero*100){xp-=hero*100;hero++;}save();invalidate();
  }
  void expedition(){wood+=130+hero*30;grain+=120+hero*25;xp+=45;while(xp>=hero*100){xp-=hero*100;hero++;}save();invalidate();Toast.makeText(getContext(),"Expedition complete!",Toast.LENGTH_SHORT).show();}
  void train(){if(barracks<1){Toast.makeText(getContext(),"Build the Barracks first",Toast.LENGTH_SHORT).show();return;}if(wood<120||iron<100||grain<80){Toast.makeText(getContext(),"Need 120 wood, 100 iron, 80 grain",Toast.LENGTH_SHORT).show();return;}wood-=120;iron-=100;grain-=80;army++;save();invalidate();Toast.makeText(getContext(),"Shieldguard recruited",Toast.LENGTH_SHORT).show();}
  void reset(){sp.edit().clear().apply();wood=500;clay=450;iron=420;grain=600;keep=lumber=quarry=mine=farm=hero=1;barracks=0;army=3;xp=0;save();invalidate();}
  @Override public boolean onTouchEvent(MotionEvent e){
    if(e.getAction()!=MotionEvent.ACTION_UP)return true;float x=e.getX(),y=e.getY();
    if(y>getHeight()-d(74)){tab=Math.max(0,Math.min(3,(int)((x-d(10))/((getWidth()-d(20))/4f))));invalidate();return true;}
    if(tab==0){float v=d(154);if(y>v&&y<v+d(86)){if(x<getWidth()/2)popup("lumber","Lumber Camp");else popup("farm","Farmland");}else if(y>v+d(90)&&y<v+d(182)&&x>getWidth()/2-d(65)&&x<getWidth()/2+d(65)){int c=cost(keep+1);if(spend(c)){keep++;save();invalidate();}else Toast.makeText(getContext(),"Not enough resources",Toast.LENGTH_SHORT).show();}else if(y>v+d(182)&&y<v+d(270)){if(x<getWidth()/2)popup("quarry","Clay Quarry");else popup("mine","Iron Mine");}}
    else if(tab==1){int row=(int)((y-d(158))/d(72));String[] k={"lumber","quarry","mine","farm","barracks"};String[] n={"Lumber Camp","Clay Quarry","Iron Mine","Farmland","Barracks"};if(row>=0&&row<5)popup(k[row],n[row]);}
    else if(tab==2){float v=d(162);if(y>v&&y<v+d(78))raid(1);else if(y>v+d(91)&&y<v+d(169))raid(2);else if(y>v+d(182)&&y<v+d(260))raid(3);}
    else{float v=d(332);if(y>v&&y<v+d(64))expedition();else if(y>v+d(78)&&y<v+d(142))train();else if(y>v+d(156)&&y<v+d(220))new AlertDialog.Builder(getContext()).setTitle("Start a new realm?").setMessage("This erases local progress.").setNegativeButton("Cancel",null).setPositiveButton("Reset",(d,w)->reset()).show();}
    return true;
  }
}
