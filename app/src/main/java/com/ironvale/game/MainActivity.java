package com.ironvale.game;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

public class MainActivity extends Activity {
  GameView game;

  @Override public void onCreate(Bundle state){
    super.onCreate(state);
    getWindow().setStatusBarColor(Color.rgb(42,58,45));
    getWindow().setNavigationBarColor(Color.rgb(42,58,45));
    if(Build.VERSION.SDK_INT>=29)getWindow().setNavigationBarContrastEnforced(false);
    if(Build.VERSION.SDK_INT>=30)getWindow().setDecorFitsSystemWindows(false);
    game=new GameView(this);
    setContentView(game);
  }

  @Override protected void onPause(){
    super.onPause();
    if(game!=null)game.s.save();
  }

  @Override protected void onDestroy(){
    if(game!=null)game.dispose();
    super.onDestroy();
  }

  @Override public void onBackPressed(){
    if(game!=null&&!game.menuScreen&&!game.s.paused){
      game.s.paused=true;game.s.save();game.invalidate();
    }else{
      super.onBackPressed();
    }
  }
}
