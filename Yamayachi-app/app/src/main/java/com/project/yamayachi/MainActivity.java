package com.project.yamayachi;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NotificationCompat;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.app.usage.UsageEvents;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.Image;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import static java.lang.Math.*;
import static java.lang.Thread.*;

import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private final static int MAX_VOLUME = 100;
    private int[] backgrounds = {R.drawable.bg_holder, R.drawable.bg2, R.drawable.bg3};
    private int[] barkSounds = {R.raw.bark1 ,R.raw.bark2, R.raw.bark3};
    private int moveHead = 5;

    boolean snackVisibility = false;
    boolean exit = false;

    ObjectAnimator headPattinganimation;
    MediaPlayer barkingPlayer;

    SharedPreferences sharedPref;
    int musicVolume;
    int background;

    BackgroundSoundService.LocalBinder binder;
    public BackgroundSoundService mService;
    public boolean mBound = false;

    ImageProcessingThread imgProcess;
    BarkingSoundThread barkProcess;

    ImageView eyeRight;
    int[] eyeRightLocation;
    ImageView eyeLeft;
    int[] eyeLeftLocation;
    ImageView imgHeadView;

    private SensorManager sensorManager;
    private Sensor sensor;

    private static final float NS2S = 1.0f / 1000000000.0f;
    private final float[] deltaRotationVector = new float[4];
    private float timestamp;

    private float xRotation = 0;
    private float yRotation = 0;
    int oldX = 0;
    int oldY = 0;

    public void goToHome(View v){
        mService.pause=false;
        Intent intent = new Intent(this, HomeScreen.class);
        startActivity(intent);
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public final void onSensorChanged(SensorEvent event) {

        if (timestamp != 0) {
            final float dT = (event.timestamp - timestamp) * NS2S;

            float axisX = event.values[0];
            float axisY = event.values[1];
            float axisZ = event.values[2];

            float omegaMagnitude = (float) sqrt(axisX*axisX + axisY*axisY + axisZ*axisZ);

            if (omegaMagnitude > 0.05) {
                axisX /= omegaMagnitude;
                axisY /= omegaMagnitude;
                axisZ /= omegaMagnitude;
            }

            float thetaOverTwo = omegaMagnitude * dT / 2.0f;
            float sinThetaOverTwo = (float) sin(thetaOverTwo);
            float cosThetaOverTwo = (float) cos(thetaOverTwo);
            deltaRotationVector[0] = sinThetaOverTwo * axisX;
            deltaRotationVector[1] = sinThetaOverTwo * axisY;
            deltaRotationVector[2] = sinThetaOverTwo * axisZ;
            deltaRotationVector[3] = cosThetaOverTwo;


            xRotation+= deltaRotationVector[0];
            yRotation+= deltaRotationVector[1];

            int x = (int) (toDegrees(xRotation) * 0.8);
            //System.out.print("X=" + x);

            int y = (int) toDegrees(yRotation);
            //System.out.println(" Y=" + y);

            if(eyeLeftLocation==null){
                eyeLeftLocation = new int[2];
                eyeLeft.getLocationOnScreen(eyeLeftLocation);

                eyeRightLocation = new int[2];
                eyeRight.getLocationOnScreen(eyeRightLocation);
            }

            imgHeadView.setImageBitmap(imgProcess.getResults());
            if((x != oldX && abs(oldX - x)>2) || (y != oldY && abs(oldY - y) >2)) {
                imgProcess.update(y*3,x*3);
                moveView(eyeRight, eyeRightLocation[0], eyeRightLocation[1], max(-90, min(x, 90)), max(-90, min(y, 90)));
                moveView(eyeLeft, eyeLeftLocation[0], eyeLeftLocation[1], max(-90, min(x, 90)), max(-90, min(y, 90)));
                oldX = x;
                oldY = y;
            }
        }
        timestamp = event.timestamp;
        float[] deltaRotationMatrix = new float[9];
        SensorManager.getRotationMatrixFromVector(deltaRotationMatrix, deltaRotationVector);

    }

    private void moveView(View view, int x, int y, int xRotation, int yRotation) {
        int xDest = x - yRotation;
        int yDest = y - xRotation/2;

        view.setX(xDest);
        view.setY(yDest);
    }

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

    private void setBackground(int index){
        ConstraintLayout ct = (ConstraintLayout) findViewById(R.id.MainScreen);
        System.out.println(index);
        ct.setBackgroundResource(backgrounds[index]);
    }

    @Override
    protected void onStart(){
        sharedPref = getSharedPreferences("yamayachi",Context.MODE_MULTI_PROCESS);
        musicVolume = sharedPref.getInt("musicVolume",100);
        super.onStart();
        Intent musicPlayer = new Intent(getApplicationContext(), BackgroundSoundService.class);
        bindService(musicPlayer, connection, Context.BIND_AUTO_CREATE);
    }

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPref = getSharedPreferences("yamayachi",Context.MODE_MULTI_PROCESS);
        background = sharedPref.getInt("background",0);

        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        setBackground(background);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        eyeRight = (ImageView) findViewById(R.id.eyeRight);

        eyeLeft = (ImageView) findViewById(R.id.eyeLeft);

        imgHeadView = (ImageView) findViewById(R.id.mascotHead);

        Bitmap headBitmap = ((BitmapDrawable)imgHeadView.getDrawable()).getBitmap();
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inScaled = false;
        Bitmap mapBitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.depth_mask, opt);
        imgProcess = new ImageProcessingThread(headBitmap, mapBitmap, 0 ,0);
        imgProcess.start();

        barkProcess = new BarkingSoundThread(this, barkSounds);
        barkProcess.start();

        headPattinganimation = ObjectAnimator.ofFloat(imgHeadView, "translationY", moveHead);
        headPattinganimation.setDuration(200);

        imgHeadView.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int eventaction = event.getAction();
                int[] location = new int[2];
                imgHeadView.getLocationOnScreen(location);

                switch(eventaction) {
                    case MotionEvent.ACTION_DOWN:
                        eyeLeft.setVisibility(View.INVISIBLE);
                        eyeRight.setVisibility(View.INVISIBLE);
                        imgHeadView.setImageResource(R.drawable.macot_head_pat);
                        barkProcess.barkSound();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        imgHeadView.setImageResource(R.drawable.macot_head_pat);
                        if(!headPattinganimation.isRunning())
                            headPattinganimation.start();
                        moveHead = moveHead *(-1);
                        break;
                    case MotionEvent.ACTION_UP:
                        eyeLeft.setVisibility(View.VISIBLE);
                        eyeRight.setVisibility(View.VISIBLE);
                        imgHeadView.setImageResource(R.drawable.macot_head);
                        break;
                }
                return true;
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.snackButton);
        ImageView snack1 = (ImageView) findViewById(R.id.snackView1);
        ImageView snack2 = (ImageView) findViewById(R.id.snackView2);
        ImageView snack3 = (ImageView) findViewById(R.id.snackView3);
        ImageView snackMenu = (ImageView) findViewById(R.id.snackMenuBg);
        snack1.setOnTouchListener(snackTouchListener());
        snack2.setOnTouchListener(snackTouchListener());
        snack3.setOnTouchListener(snackTouchListener());

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!snackVisibility) {
                    snackVisibility = true;
                    snackMenu.setVisibility(View.VISIBLE);
                    snackMenu.setClickable(true);
                    scaleViewAnimation(snackMenu, 0, 1f,0);
                    snack1.setVisibility(View.VISIBLE);
                    snack1.setClickable(true);
                    scaleViewAnimation(snack1, 0, 1f,500);
                    snack2.setVisibility(View.VISIBLE);
                    snack2.setClickable(true);
                    scaleViewAnimation(snack2, 0, 1f, 300);
                    snack3.setVisibility(View.VISIBLE);
                    snack3.setClickable(true);
                    scaleViewAnimation(snack3, 0, 1f, 100);
                }
                else{
                    snackVisibility = false;
                    scaleViewAnimation(snack1, 1f, 0, 0);
                    snack1.setVisibility(View.INVISIBLE);
                    snack1.setClickable(false);
                    scaleViewAnimation(snack2,1f,0, 200);
                    snack2.setVisibility(View.INVISIBLE);
                    snack2.setClickable(false);
                    scaleViewAnimation(snack3, 1f,0, 400);
                    snack3.setVisibility(View.INVISIBLE);
                    snack3.setClickable(false);
                    scaleViewAnimation(snackMenu, 1f, 0,500);
                    snackMenu.setVisibility(View.INVISIBLE);
                    snackMenu.setClickable(false);

                }
            }
        });
        imgHeadView.setOnDragListener(snackDragListener());
    }

    @Override
    public void finish(){
        imgProcess.finish = true;
        barkProcess.finish = true;
        unbindService(connection);
        mBound = false;
        super.finish();
    }

    @Override
    protected void onResume() {
        exit = false;
        super.onResume();
        if(mService!=null){
            mService.startMusic();
            mService.pause = true;
        }
        imgProcess.finish = false;
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onPause() {
        sensorManager.unregisterListener(this);
        mService.stopMusic();
        super.onPause();
    }

    @Override
    public void onBackPressed(){
        if(!exit) {
            Toast.makeText(getApplicationContext(), "Click once again to exit", Toast.LENGTH_SHORT).show();
            exit = true;
        }
        else {
            finish();
            this.finishAffinity();
        }
    }
    protected View.OnTouchListener snackTouchListener(){
        return new View.OnTouchListener(){
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    ClipData data = ClipData.newPlainText("", "");
                    View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(
                            view);
                    view.startDrag(data, shadowBuilder, view, 0);
                    view.setVisibility(View.INVISIBLE);
                    return true;
                } else {
                    return false;
                }
            }
        };
    }
    protected  View.OnDragListener snackDragListener(){
        return new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                int action = event.getAction();
                if (event.getAction() == DragEvent.ACTION_DROP) {
                  Toast.makeText(getApplicationContext(),"MNIAM!",Toast.LENGTH_SHORT).show();
                    barkProcess.snackSound();
                }
                return true;
            }
        };
    }
    // SERVICE CONNECTION //////////////////////////////////////////////////////////////////////////
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            binder = (BackgroundSoundService.LocalBinder)service;
            mService = binder.getService();

            float volume = (float) (1 - (Math.log(MAX_VOLUME - musicVolume) / Math.log(MAX_VOLUME)));
            mService.setVolume(volume);
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

class ImageProcessingThread extends Thread {
    ImageDisplacer displacer;
    Bitmap image;
    int x;
    int y;
    int oldX = 0;
    int oldY = 0;
    public boolean finish = false;

   @RequiresApi(api = Build.VERSION_CODES.Q)
   public ImageProcessingThread(Bitmap headBitmap, Bitmap mapBitmap, int x, int y){
        displacer = new ImageDisplacer(headBitmap, mapBitmap);
        image = displacer.getDisplacementImage(x, y);
        this.x = x;
        this.y = y;
   }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void run() {
       while(!finish) {
           if(oldX != x || oldY != y)
            image = displacer.getDisplacementImage(x, y);
       }
    }
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void update(int newx, int newy){
       oldX = this.x;
       oldY = this.y;
       this.x = newx;
       this.y = newy;
    }
   @RequiresApi(api = Build.VERSION_CODES.Q)
   public Bitmap getResults(){
      return image;
   }
}

class BarkingSoundThread extends Thread{
    final static int MAX_SECONDS_COOLDOWN = 60;
    public boolean finish = false;
    int[] barkingSounds;
    MediaPlayer player;
    Context context;
    public BarkingSoundThread(Context context, int[] barkingSounds){
        this.context = context;
        this.barkingSounds = barkingSounds;
        player = new MediaPlayer();
    }
    public void barkSound(){
        if(player != null){
            player.stop();
            player.release();
            player = null;
        }
        player = MediaPlayer.create(context, barkingSounds[(int) (random() * 3)]);
        player.start();
    }
    public void snackSound(){
        if(player != null){
            player.stop();
            player.release();
            player = null;
        }
        player = MediaPlayer.create(context, barkingSounds[0]);
        player.start();
    }
    
    @Override
    public void run() {
        while(!finish) {
            if(player != null){
                player.stop();
                player.release();
                player = null;
            }
            player = MediaPlayer.create(context, barkingSounds[(int) (random() * 3)]);
            player.start();
            try {
                sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            player.stop();
            player.release();
            player = null;
            int cooldown = (int) (random()*MAX_SECONDS_COOLDOWN*1000);
            try {
                sleep(cooldown);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}