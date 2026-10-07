package com.ironvale.game;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Build;

public class MainActivity extends Activity {
  GameView game;

  @Override public void onCreate(Bundle state){
    super.onCreate(state);
    getWindow().setStatusBarColor(Color.rgb(13,22,18));
    getWindow().setNavigationBarColor(Color.rgb(13,22,18));
    if(Build.VERSION.SDK_INT>=29)getWindow().setNavigationBarContrastEnforced(false);
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
    if(game!=null && !game.menuScreen && !game.s.paused){
      game.s.paused=true;
      game.s.save();
      game.invalidate();
    }else{
      super.onBackPressed();
    }
  }
}
