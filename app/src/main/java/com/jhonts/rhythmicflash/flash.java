package com.jhonts.rhythmicflash;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.util.Log;

class flash extends Activity{
    private static boolean linternaOn;
    private static boolean tieneFlash;

    private static Camera objCamara;
    static android.hardware.Camera.Parameters parametrosCamara;

    public static void controlarFlash(){
        getObjCamara();
        if(linternaOn){
            apagarFlash();
        } else {
            encenderFlash();
        }
    }

    public static void getObjCamara(){
        if(objCamara == null){
            try{
                objCamara = android.hardware.Camera.open();
                parametrosCamara = objCamara.getParameters();
            } catch (RuntimeException e) {
                Log.e("Error:"," la camara no puede ser accedida");
            }
        }
    }

    static void encenderFlash(){
        if(!linternaOn){
            if(objCamara == null || parametrosCamara == null){
                return;
            }
            parametrosCamara=objCamara.getParameters();
            if(parametrosCamara == null){
                return;
            }
            parametrosCamara.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            objCamara.setParameters(parametrosCamara);
            linternaOn=true;
        }
    }

    static void apagarFlash(){
        if(linternaOn){
            if(objCamara == null || parametrosCamara == null){
                return;
            }
            parametrosCamara=objCamara.getParameters();
            if(parametrosCamara == null){
                return;
            }
            parametrosCamara.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
            objCamara.setParameters(parametrosCamara);
            linternaOn=false;
        }
    }

    public void preparaLinterna(){
        tieneFlash = getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
        if(!tieneFlash){
            return;
        }
        if(objCamara == null){
            try{
                objCamara = android.hardware.Camera.open();
                parametrosCamara = objCamara.getParameters();
            } catch (RuntimeException e) {
                Log.e("Error:"," el flash no puede ser accedido");
            }

        }
    }
}