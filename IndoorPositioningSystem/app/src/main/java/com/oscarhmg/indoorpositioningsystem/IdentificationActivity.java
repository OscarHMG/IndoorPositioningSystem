package com.oscarhmg.indoorpositioningsystem;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.Arrays;


/**
 * Created by user on 17/11/2016.
 */
public class IdentificationActivity extends Activity{
    private String userName;
    private String server;
    private String group;
    private static final int PERMISSIONS = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_identification);
        userName = (getResources().getString(R.string.userNameDefault));
        server = (getResources().getString(R.string.serverDefault));
        group = (getResources().getString(R.string.groupDefault));
        checkPermissions();
    }


    public void checkPermissions(){
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED) {
        } else {
            Toast.makeText(this, "Error en Permisos", Toast.LENGTH_LONG).show();
            ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    100);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 100:
                if (grantResults.length > 0
                        || grantResults[0] == PackageManager.PERMISSION_GRANTED
                        || grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    /* User checks permission. */
                    Log.d("Permisos", "OK Permisos esteblecidos");
                } else {
                    Log.w("No Permisos", "onRequestPermissionsResult() called with: " + "requestCode = [" + requestCode + "], permissions = [" + Arrays.toString(permissions) + "], grantResults = [" + Arrays.toString(grantResults) + "]");
                    Toast.makeText(this, "Permission is denied.", Toast.LENGTH_SHORT).show();
                    finish();
                }
                return;
        }
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }
}
