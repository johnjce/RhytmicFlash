package com.jhonts.rhythmicflash;
/*
  @author John Jairo Castaño Echeverri
 * Copyright (c) <2017> <jjce- ..::jhonts::..>
 */
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;

import java.util.Random;

import com.apptracker.android.listener.AppModuleListener;
import com.apptracker.android.track.AppTracker;


public class MainActivity extends Activity {
    /**----------------publicidad------------------**/
    private static final String APP_API_KEY 		    = "mq9zUPkFjZBP6K6G9FH2HevQY60pHUHf"; //<-real - prueba-> // "dAICGF8bVShbB7rYTaQs9vI7gLloSI1l"; // change this to your App specific API KEY
    /*----------------publicidad------------------**/
    private double lastLevel = 0;
    private int bufferSize;
    private final int min = 15;
    private static final int SAMPLE_DELAY = 75;
    private static final int sampleRate = 8000;
    private AudioRecord audio;
    private Thread thread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View mainView = new View(this);


        if(savedInstanceState == null){
            AppTracker.setModuleListener(leadboltListener);
            AppTracker.startSession(getApplicationContext(), APP_API_KEY);

            AppTracker.destroyModule();
            AppTracker.loadModuleToCache(getApplicationContext(), "inapp");

            // aqui llamo publicidad
            if(AppTracker.isAdReady("inapp")) AppTracker.loadModule(mainView.getContext(), "inapp");
            AppTracker.destroyModule();

        }
        SeekBar barvolumen = new SeekBar(this);
        barvolumen.setMax(2000);
        barvolumen.setIndeterminate(true);
        ShapeDrawable thumb = new ShapeDrawable(new OvalShape());

        thumb.setIntrinsicHeight(80);
        thumb.setIntrinsicWidth(30);
        barvolumen.setThumb(thumb);
        barvolumen.setProgress(1);
        barvolumen.setVisibility(View.VISIBLE);
        barvolumen.setBackgroundColor(Color.BLUE);

        try {
            bufferSize = AudioRecord
                        .getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT);
        } catch (Exception e) {
            Log.e("TrackingFlow", "Exception", e);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onResume() {
        super.onResume();

        final View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        audio = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, bufferSize);

        audio.startRecording();
        thread = new Thread(new Runnable() {
            public void run() {
                while(thread != null && !thread.isInterrupted()){
                    try{Thread.sleep(SAMPLE_DELAY);}catch(InterruptedException ie){ie.printStackTrace();}
                    readAudioBuffer();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            decorView.setBackgroundColor(Color.parseColor(getColor(lastLevel,min)));
                            if(lastLevel <= min){
                                com.jhonts.rhythmicflash.flash.flashOff();
                            }else {
                                com.jhonts.rhythmicflash.flash.swichFlash();
                            }
                        }
                    });
                }
            }
        });
        thread.start();
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(R.string.delete_title);
        alertDialogBuilder
                .setMessage(R.string.comfirm_delete)
                .setCancelable(false)
                .setPositiveButton(R.string.yes,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                moveTaskToBack(true);
                                android.os.Process.killProcess(android.os.Process.myPid());
                                System.exit(1);
                            }
                        })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        thread.interrupt();
        thread = null;
        try {
            if (audio != null) {
                audio.stop();
                audio.release();
                audio = null;
            }
        } catch (Exception e) {e.printStackTrace();}
    }

    private String getColor(double lastLevel, int min) {
        if(lastLevel<min) return "#000000";
        Random randomGenerator = new Random();
        lastLevel+=6;
        int     r = (int) lastLevel * randomGenerator.nextInt(100)+1,
                g = (int) lastLevel * randomGenerator.nextInt(100)+1,
                b = (int) lastLevel * randomGenerator.nextInt(100)+1;
        String color = String.format("#%02x%02x%02x", r, g, b);
        if(color.length()>7) {
            color = color.substring(0,7);
        }
        return color;

    }

    private void readAudioBuffer() {
        try {
            short[] buffer = new short[bufferSize];
            int bufferReadResult = 1;
            if (audio != null) {
                bufferReadResult = audio.read(buffer, 0, bufferSize);
                double sumLevel = 0;
                for (int i = 0; i < bufferReadResult; i++) {
                    sumLevel += buffer[i];
                }
                lastLevel = Math.abs((sumLevel / bufferReadResult));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**----------------publicidad------------------**/
    private AppModuleListener leadboltListener = new AppModuleListener() {
        @Override
        public void onModuleCached(final String placement) {

        }
        @Override
        public void onModuleClicked(String placement) {
            Log.i("AppTracker", "Ad clicked by user - "+ placement);
            // SharedPreferences saves = getSharedPreferences(coins, 0);
            //view.setSocket(saves.getInt(coins, 0));
        }

        @Override
        public void onModuleClosed(final String placement) {}

        @Override
        public void onModuleFailed(String placement, String error, boolean isCache) { }

        @Override
        public void onModuleLoaded(String s) {

            // Add code here to pause game and/or all media including audio
        }

        @Override
        public void onMediaFinished(boolean b) { }
    };
    /**----------------publicidad------------------**/
}