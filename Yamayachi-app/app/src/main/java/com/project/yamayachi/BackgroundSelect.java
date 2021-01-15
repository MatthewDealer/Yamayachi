package com.project.yamayachi;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.view.ViewDebug;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

public class BackgroundSelect extends AppCompatActivity {

    private int[] backgrounds = {R.drawable.bg_holder, R.drawable.bg2, R.drawable.bg3};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_background_select);
        SharedPreferences sharedPref = getSharedPreferences("yamayachi",Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = sharedPref.edit();

        GridView grid = (GridView) findViewById(R.id.gridView1);
        BackgroundAdapter av = new BackgroundAdapter(this, backgrounds);
        grid.setAdapter(av);
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getApplicationContext(), "You selected background" + position, Toast.LENGTH_SHORT).show();
                editor.putInt("background", position);
                editor.apply();
            }
        });


    }
}

