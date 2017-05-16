package com.jhonts.rhythmicflash;
/*
  @author John Jairo Castaño Echeverri
 * Copyright (c) <2017> <jjce- ..::jhonts::..>
 */
import android.app.Activity;
import android.hardware.Camera;
import android.util.Log;

class flash extends Activity{
    private static boolean flashOn;
    private static Camera objFlash;
    static android.hardware.Camera.Parameters flashParameters;

    public static void swichFlash(){
        getObjFlash();
        flashParameters =objFlash.getParameters();
        if(objFlash != null && flashParameters != null){
            if(flashOn){
                flashOn();
            } else {
                flashOff();
            }
        }
    }

    static void flashOff() {
        if(objFlash == null || flashParameters == null) return;
        flashParameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
        objFlash.setParameters(flashParameters);
        flashOn = true;
    }

    static void flashOn() {
        if(objFlash == null || flashParameters == null) return;
        flashParameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        objFlash.setParameters(flashParameters);
        flashOn = false;
    }

    public static Camera getObjFlash(){
        if(objFlash == null){
            try{
                objFlash = android.hardware.Camera.open();
                flashParameters = objFlash.getParameters();
                return objFlash;
            } catch (RuntimeException e) {
                Log.e("Error:"," El flash no puede ser accedido");
            }
        }
        return null;
    }
}