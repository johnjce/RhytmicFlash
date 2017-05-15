package com.jhonts.rhythmicflash;

import android.app.Activity;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.widget.ImageView;

public class MainActivity extends Activity {
    private static final int sampleRate = 8000;
    private AudioRecord audio;
    private int bufferSize;
    private double lastLevel = 0;
    private Thread thread;
    private static final int SAMPLE_DELAY = 75;
    private ImageView mouthImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mouthImage = (ImageView)findViewById(R.id.mounthHolder);
        mouthImage.setKeepScreenOn(true);

        try {
            bufferSize = AudioRecord
                    .getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO,
                            AudioFormat.ENCODING_PCM_16BIT);
        } catch (Exception e) {
            android.util.Log.e("TrackingFlow", "Exception", e);
        }
    }

    protected void onResume() {
        super.onResume();
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
                            if(lastLevel > 0 && lastLevel <= 50){
                                mouthImage.setBackgroundColor(Color.BLACK);
                                com.jhonts.rhythmicflash.flash.apagarFlash();
                            }else
                            if(lastLevel > 50 && lastLevel <= 100){
                                mouthImage.setBackgroundColor(Color.GREEN);
                                com.jhonts.rhythmicflash.flash.controlarFlash();
                            }else
                            if(lastLevel > 100 && lastLevel <= 170){
                                mouthImage.setBackgroundColor(Color.BLUE);
                                com.jhonts.rhythmicflash.flash.controlarFlash();
                            }
                            if(lastLevel > 170){
                                mouthImage.setBackgroundColor(Color.RED);
                                com.jhonts.rhythmicflash.flash.controlarFlash();
                            }
                        }
                    });
                }
            }
        });
        thread.start();
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
}