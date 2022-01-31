package com.sushant.whatsapp;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.ortiz.touchview.TouchImageView;

public class FullScreenImage extends AppCompatActivity {

    TouchImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);
        getSupportActionBar().hide();
        imageView=findViewById(R.id.fullScreenImage);
        String image=getIntent().getStringExtra("messageImage");
        Glide.with(this).load(image).placeholder(R.drawable.placeholder).into(imageView);
    }
}