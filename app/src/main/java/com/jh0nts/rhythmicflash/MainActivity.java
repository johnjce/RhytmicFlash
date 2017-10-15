package com.jh0nts.rhythmicflash;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;


import com.apptracker.android.listener.AppModuleListener;
import com.apptracker.android.track.AppTracker;

import java.util.Random;

public class MainActivity extends Activity {
    //---------------publicidad-----------------//
    private static final String APP_API_KEY = "mq9zUPkFjZBP6K6G9FH2HevQY60pHUHf";
    //---------------publicidad-----------------//
    private int delay = 50;
    private double lastLevel = 0;
    private Thread thread;
    private View decorView;
    double prevLastLevel;
    Flash led;
    Micro micro;
    Random randomGenerator = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.INVISIBLE);

        //.................. control de la publicidad .....................//
        if (savedInstanceState == null) {
            AppTracker.setModuleListener(leadboltListener);
            AppTracker.startSession(getApplicationContext(), APP_API_KEY);

            AppTracker.destroyModule();
            AppTracker.loadModuleToCache(getApplicationContext(), "inapp");

            // aqui llamo publicidad
            if (AppTracker.isAdReady("inapp")) {
                AppTracker.loadModule(new View(this).getContext(), "inapp");
            }
            AppTracker.destroyModule();

        }
        //.................. control de la publicidad .....................*/

        if(verifyPermission()){
            if(led ==null) {
                led= new Flash();
                if(micro ==null) {
                    micro = new Micro();
                }
            }
        }
    }

    private boolean verifyPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestRationalPermission();
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 0);
                Log.v("Error", "Permiso denegado, camara.");
                return false;
            }
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
                Log.v("Error", "Permiso denegado, microfono.");
                return false;
            }
        }
        return true;
    }

    private void requestRationalPermission() {
        if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.CAMERA)) {
            dialogUser("El permiso de la camara es necesario para encender la luz");
        }
        if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.RECORD_AUDIO)) {
            dialogUser("El permiso del microfono es necesario para que las luces parpadeen");
        }
    }

    private void dialogUser(String text) {
        Toast.makeText(getApplicationContext(), text , Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 0: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    led = new Flash();
                }
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    dialogUser("Sin permiso de la camara es no se enciende la luz");
                    led=null;
                }
                break;
            }
            case 1: {
                //Si la petición es cancelada, el resultado estará vacío.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.v("Error", "Permiso aceptado, se podría acceder al microfono del dispositivo.");
                    micro = new Micro();
                } else {
                    Log.v("Error", "Permiso denegado. Desactivar la funcionalidad que dependía de dicho permiso.");
                    micro = null;
                }
                break;
            }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        thread = new Thread(new Runnable() {
            public void run() {

                while (thread != null && !thread.isInterrupted()) {
                    try{
                        thread.sleep(delay);
                    }catch (InterruptedException e){ }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                        if(micro != null ) {
                            lastLevel = micro.getLastLevel();
                        }else {
                            lastLevel = (double) randomGenerator.nextInt(999) + 1;
                        }

                        if(led != null) {
                            if(lastLevel>=15) {
                                if (lastLevel - prevLastLevel >= 20) {
                                    led.flashOn();
                                    changeColor();
                                }
                                prevLastLevel = lastLevel;
                            }
                            led.flashOff();
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

        try {
            thread.interrupt();
            thread = null;
            if( led != null ) {
                led.dispCamara.release();
                led = null;
            }
            if( micro != null ) {
                micro.stoped();
                micro = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void changeColor() {
        int r = randomGenerator.nextInt(254),
            g = randomGenerator.nextInt(254),
            b = randomGenerator.nextInt(254);

        String color = String.format("#%02x%02x%02x", r, g, b);

        if (color.length() > 7) {
            color = color.substring(0, 7);
        }

        decorView.setBackgroundColor(Color.parseColor(color));
        Log.v("info", "color: "+ color);
        try {
            thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    //---------------publicidad-----------------//
    private AppModuleListener leadboltListener = new AppModuleListener() {
        @Override
        public void onModuleCached(final String placement) { }

        @Override
        public void onModuleClicked(String placement) {
            Log.i("AppTracker", "Ad clicked by user - " + placement);
        }

        @Override
        public void onModuleClosed(final String placement) { }

        @Override
        public void onModuleFailed(String placement, String error, boolean isCache) { }

        @Override
        public void onModuleLoaded(String s) { }

        @Override
        public void onMediaFinished(boolean b) { }
    };
    // ----------------publicidad-----------------//

}