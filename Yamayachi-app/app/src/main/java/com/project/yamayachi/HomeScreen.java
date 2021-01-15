package com.project.yamayachi;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;

public class HomeScreen extends AppCompatActivity {

    public void backgroundSelect(View v){
        Intent intent = new Intent(this, BackgroundSelect.class);
        startActivity(intent);
    }

    public void goToSettings(View v){
        Intent intent = new Intent(this, UserSettings.class);
        startActivity(intent);
    }

    public void goToAuthor(View v){
        Intent intent = new Intent(this, AboutAuthor.class);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);

    }



}