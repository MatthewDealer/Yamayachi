package com.project.yamayachi;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class UserSettings extends AppCompatActivity {

    private final static int MAX_VOLUME = 100;
    int currentMusicVolume;
    SharedPreferences sharedPref;
    BackgroundSoundService.LocalBinder binder;
    Intent musicPlayer;
    public BackgroundSoundService mService;
    public boolean mBound = false;

    @Override
    protected void onStart(){
        super.onStart();
        musicPlayer = new Intent(getApplicationContext(), BackgroundSoundService.class);
        bindService(musicPlayer, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_settings);

        sharedPref = getSharedPreferences("yamayachi",Context.MODE_MULTI_PROCESS);
        currentMusicVolume = sharedPref.getInt("musicVolume",100);
        sharedPref = getSharedPreferences("yamayachi",Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = sharedPref.edit();

        SeekBar musicVolume = (SeekBar) findViewById(R.id.musicVolume);
        TextView volumeText = (TextView) findViewById(R.id.musicVolumeText);
        musicVolume.setProgress(currentMusicVolume);
        volumeText.setText(Integer.toString(currentMusicVolume));
        musicVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChangedValue = 0;

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChangedValue = progress;
                volumeText.setText(Integer.toString(progress));

                float volume = (float) (1 - (Math.log(MAX_VOLUME - progress) / Math.log(MAX_VOLUME)));
                mService.setVolume(volume);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                Toast.makeText(getApplicationContext(), "Volume is set to:" + progressChangedValue, Toast.LENGTH_SHORT).show();
                editor.putInt("musicVolume", progressChangedValue);
                editor.apply();
            }
        });
    }


    // SERVICE CONNECTION //////////////////////////////////////////////////////////////////////////
    private ServiceConnection connection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service)
        {
            binder = (BackgroundSoundService.LocalBinder)service;
            mService = binder.getService();
            mService.startMusic();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName className)
        {
            mBound = false;
        }
    };

}