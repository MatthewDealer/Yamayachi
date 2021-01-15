package com.project.yamayachi;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Layout;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class WelcomeScreen extends AppCompatActivity {

    public void switchToMain(View V){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_screen);
        ImageView clouds = (ImageView) findViewById(R.id.cloudsView);
        Animation connectingAnimation = AnimationUtils.loadAnimation(this, R.anim.clouds_anim);
        clouds.startAnimation(connectingAnimation);

    }
}