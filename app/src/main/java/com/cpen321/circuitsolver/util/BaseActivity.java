package com.cpen321.circuitsolver.util;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.opencv.android.OpenCVLoader;

/**
 * Created by Neil Goossen on 2016-10-15.
 */

public class BaseActivity extends AppCompatActivity{

    static{
        if(!OpenCVLoader.initDebug()){
            Log.i("opencv","opencv init failed");
        }else{
            Log.i("opencv","opencv init success");
        }
    }

    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private static final int READ_IAMGE_ACTIVITY_REQUEST_CODE = 50;
    private static final int STORAGE_REQUEST_CODE = 200;

    public void checkNecessaryPermissions(){
        this.checkPermission(android.Manifest.permission.CAMERA, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        this.checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE, READ_IAMGE_ACTIVITY_REQUEST_CODE);
        this.checkPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, STORAGE_REQUEST_CODE);
        this.checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, STORAGE_REQUEST_CODE);
    }

    private void checkPermission(String permission, int requestCode){
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                this.requestPermissions(new String[]{permission}, requestCode);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
            }
        }
    }




}
