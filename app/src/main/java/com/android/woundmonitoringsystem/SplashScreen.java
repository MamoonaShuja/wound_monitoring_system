package com.android.woundmonitoringsystem;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import static com.android.woundmonitoringsystem.Helpers.SHARED_PREFS;

public class SplashScreen extends AppCompatActivity {
    SharedPreferences prefs;
    public static final String USER_ID = "uid";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        RelativeLayout root = (RelativeLayout) findViewById(R.id.splashLayout);
        DisplayMetrics dm = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int statusBarOffset = dm.heightPixels - root.getMeasuredHeight();
        AnimationSet set = new AnimationSet(true);

        Animation fadeIn = FadeIn(3000);
        fadeIn.setStartOffset(0);
        set.addAnimation(fadeIn);
        int originalPos[] = new int[2];
        imageView.getLocationOnScreen(originalPos);

        int xDest = dm.widthPixels / 2;
        xDest -= (imageView.getMeasuredWidth() / 2);
        int yDest = dm.heightPixels / 2 - (imageView.getMeasuredHeight() / 2)
                - statusBarOffset;
        TranslateAnimation anim = new TranslateAnimation(0, xDest
                - originalPos[0], 0, yDest - originalPos[1]);
        anim.setDuration(1000);
        set.addAnimation(anim);

        Animation fadeOut = FadeOut(1000);
        fadeOut.setStartOffset(3000);
        set.addAnimation(fadeOut);

        set.setFillAfter(true);
        set.setFillEnabled(true);
        imageView.startAnimation(set);
        Animation animate = AnimationUtils.loadAnimation(getBaseContext(),
                R.anim.bounce);
        imageView.startAnimation(animate);

        Handler handle = new Handler();
        final Helpers helpers = new Helpers(getBaseContext());
        handle.postDelayed(new Runnable() {
            @Override
            public void run() {
                String id = helpers.getPreferences(SHARED_PREFS, USER_ID);
                if(id !=null){
                    startActivity( new Intent( getBaseContext(), MainActivity.class ) );
                    finish();
                }else {
                    startActivity(new Intent(getBaseContext(), LoginActivity.class));
                    finish();
                }
            }
        }, 4000);
    }
    private Animation FadeIn(int t) {
        Animation fade;
        fade = new AlphaAnimation(0.0f, 1.0f);
        fade.setDuration(t);
        fade.setInterpolator(new AccelerateInterpolator());
        return fade;
    }

    private Animation FadeOut(int t) {
        Animation fade;
        fade = new AlphaAnimation(1.0f, 0.1f);
        fade.setDuration(t);
        fade.setInterpolator(new AccelerateInterpolator());
        return fade;
    }
}