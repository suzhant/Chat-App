package com.sushant.whatsapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Pair;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Objects;

public class LoadingScreenActivity extends AppCompatActivity {

    TextView textView;
    ImageView imageView,img_toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading_screen);
        textView=findViewById(R.id.textView);
        imageView=findViewById(R.id.img);



        Objects.requireNonNull(getSupportActionBar()).hide();
//        textView.setVisibility(View.GONE);

        Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                // Hide your View after 1 seconds
//                textView.setVisibility(View.VISIBLE);
//                setUpFadeAnimation(textView);
//            }
//        }, 1000);

//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                textView.setText("Welcome Back");
//                setUpFadeAnimation(textView);
//            }
//        },2700);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent= new Intent(LoadingScreenActivity.this,SignInActivity.class);
                Pair[] pairs= new Pair[1];
                pairs[0]= new Pair<View,String>(imageView,"login_whatsapp");
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    ActivityOptions options= ActivityOptions.makeSceneTransitionAnimation(LoadingScreenActivity.this,pairs);
                    startActivity(intent,options.toBundle());
                }
                finish();//splash screen will not show up when using back button. This screen will be popped out from the stack
            }
        },2000);
    }
    private void setUpFadeAnimation(final TextView textView) {
        // Start from 0.1f if you desire 90% fade animation
        final Animation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(500);
        fadeIn.setStartOffset(500);
        // End to 0.1f if you desire 90% fade animation
        final Animation fadeOut = new AlphaAnimation(1.0f, 0.0f);
        fadeOut.setDuration(500);
        fadeOut.setStartOffset(500);

        fadeIn.setAnimationListener(new Animation.AnimationListener(){
            @Override
            public void onAnimationEnd(Animation arg0) {
                // start fadeOut when fadeIn ends (continue)
                textView.startAnimation(fadeOut);
            }

            @Override
            public void onAnimationRepeat(Animation arg0) {
            }

            @Override
            public void onAnimationStart(Animation arg0) {
            }
        });

        fadeOut.setAnimationListener(new Animation.AnimationListener(){
            @Override
            public void onAnimationEnd(Animation arg0) {
                // start fadeIn when fadeOut ends (repeat)
                textView.startAnimation(fadeIn);
            }

            @Override
            public void onAnimationRepeat(Animation arg0) {
            }

            @Override
            public void onAnimationStart(Animation arg0) {
            }
        });

        textView.startAnimation(fadeIn);
    }
}