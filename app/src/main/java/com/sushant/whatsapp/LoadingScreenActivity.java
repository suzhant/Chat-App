package com.sushant.whatsapp;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;

import java.util.Objects;

public class LoadingScreenActivity extends AppCompatActivity {

    TextView textView;
    ImageView imageView, img_toolbar;
    LottieAnimationView animationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading_screen);
        textView = findViewById(R.id.textView);
        imageView = findViewById(R.id.img);
        animationView = findViewById(R.id.animation);


        Objects.requireNonNull(getSupportActionBar()).hide();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                animationView.cancelAnimation();
                Intent intent = new Intent(LoadingScreenActivity.this, SignInActivity.class);
                Pair[] pairs = new Pair[1];
                pairs[0] = new Pair<View, String>(imageView, "login_whatsapp");
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(LoadingScreenActivity.this, pairs);
                    startActivity(intent, options.toBundle());
                }
                finish();//splash screen will not show up when using back button. This screen will be popped out from the stack
            }
        }, 2000);
    }
}