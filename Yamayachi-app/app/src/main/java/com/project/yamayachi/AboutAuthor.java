package com.project.yamayachi;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;

public class AboutAuthor extends AppCompatActivity {
    BackgroundSoundService.LocalBinder binder;
    public BackgroundSoundService mService;
    public boolean mBound = false;

    public void scaleViewAnimation(View v, float startScale, float endScale, int offset) {
        Animation anim = new ScaleAnimation(
                startScale, endScale,
                startScale, endScale,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setFillAfter(true);
        anim.setDuration(500);
        anim.setStartOffset(offset);
        v.startAnimation(anim);
    }

    public void sendEmail(View v){
        mService.pause=true;
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto: 246712@student.pwr.edu.pl"));
        intent.putExtra(Intent.EXTRA_SUBJECT, "Yamayachi");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    @Override
    public void onBackPressed(){
        mService.pause=false;
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        mService.stopMusic();
        super.onPause();
    }

    @Override
    protected void onResume() {
        if(mService!=null)
            mService.startMusic();
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_author);

        ImageView img = (ImageView) findViewById(R.id.profileImageView);
        scaleViewAnimation(img, 0,1,0);
        Intent musicPlayer = new Intent(getApplicationContext(), BackgroundSoundService.class);
        bindService(musicPlayer, connection, Context.BIND_AUTO_CREATE);
    }
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            binder = (BackgroundSoundService.LocalBinder)service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName className)
        {
            mBound = false;
        }
    };
}