package com.ironvale.game;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;

public class MainActivity extends Activity {
  GameView game;

  @Override public void onCreate(Bundle state){
    super.onCreate(state);
    Window w=getWindow();
    w.setStatusBarColor(Color.BLACK);
    w.setNavigationBarColor(Color.BLACK);
    if(Build.VERSION.SDK_INT>=29)w.setNavigationBarContrastEnforced(false);
    // Intentionally keep decorFitsSystemWindows=true so the game is laid out
    // inside the phone's status/navigation/cutout-safe content rectangle.
    if(Build.VERSION.SDK_INT>=30)w.setDecorFitsSystemWindows(true);
    game=new GameView(this);
    setContentView(game);
  }

  @Override protected void onPause(){
    super.onPause();
    if(game!=null)game.pauseFromSystem();
  }

  @Override protected void onDestroy(){
    if(game!=null)game.dispose();
    super.onDestroy();
  }

  @Override public void onBackPressed(){
    if(game!=null && game.handleBack()) return;
    super.onBackPressed();
  }
}
