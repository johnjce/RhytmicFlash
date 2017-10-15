package com.jh0nts.rhythmicflash;
import java.util.List;

import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.util.Log;

class Flash {

    Camera dispCamara;
    boolean state = false;

    Flash() {
        try {
            dispCamara = Camera.open();
        } catch( Exception e ) {
            Log.v("error","No se ha podido acceder a la cámara");
        }
    }


    void flashOff(){
        if( dispCamara != null ){
            Parameters parametrosCamara = dispCamara.getParameters();
            parametrosCamara.setFlashMode(Parameters.FLASH_MODE_OFF);
            dispCamara.setParameters(parametrosCamara);
        } else {
            Log.v("error","No se ha podido acceder al Flash de la cámara");
        }
        state = false;
    }

    void swichFlash() {
        if(!state) {
            flashOn();
        }
        else {
            flashOff();
        }
    }

    void flashOn() {

        if( dispCamara != null ){
            Parameters parametrosCamara = dispCamara.getParameters();

            //Get supported flash modes
            List modosFlash = parametrosCamara.getSupportedFlashModes ();


            if (modosFlash != null && modosFlash.contains(Parameters.FLASH_MODE_TORCH)) {
                //Set the flash parameter to use the torch
                parametrosCamara.setFlashMode(Parameters.FLASH_MODE_TORCH);
                try {
                    dispCamara.setParameters(parametrosCamara);
                    dispCamara.startPreview();
                } catch (Exception e) {
                    Log.v("error","Error al activar la linterna");
                }
            } else {
                Log.v("error","El dispositivo no tiene el modo de Flash Linterna");
            }
        } else {
            Log.v("error","No se ha podido acceder al Flash de la cámara");
        }
        state = true;
    }

}