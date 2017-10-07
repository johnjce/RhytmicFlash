package com.jh0nts.rhythmicflash;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

class Micro {
    private double lastLevel = 0;
    private int bufferSize;
    private static final int sampleRate = 8000;
    private AudioRecord audio;

    Micro() {
        try {
            bufferSize = AudioRecord
                    .getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO,
                            AudioFormat.ENCODING_PCM_16BIT);

            audio = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, bufferSize);

            audio.startRecording();

        } catch (Exception e) {
            Log.e("TrackingFlow", "Exception", e);
        }

    }

    double getLastLevel(){
        readAudioBuffer();
        return lastLevel;
    }

    private void readAudioBuffer() {
        try {
            short[] buffer = new short[bufferSize];
            int bufferReadResult;
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
}
