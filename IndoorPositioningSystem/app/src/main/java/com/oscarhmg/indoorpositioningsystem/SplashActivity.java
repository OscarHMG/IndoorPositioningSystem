package com.oscarhmg.indoorpositioningsystem;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.oscarhmg.indoorpositioningsystem.room.Room;
import com.oscarhmg.indoorpositioningsystem.room.RoomsCTI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by user on 17/11/2016.
 */
public class SplashActivity extends Activity {
    HttpHandler cliente;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkPermissions();
        Intent intent = new Intent(this, MapActivity.class);
        startActivity(intent);
        /*Intent intent = new Intent(this, IdentificationActivity.class);
        startActivity(intent);*/
        finish();
        cliente = new HttpHandler();
        String server = getResources().getString(R.string.serverFind);
        String response = cliente.request(server, JSONTest());
        String room = getLocation(response);
        Log.i("ROOM SPLASH:",room);
        Log.i("Location SPLASH: ",getLntLong(room).toString());
    }

    //THIS FUNCTIONS HAVE TO GO IN MAP

    public String JSONTest(){
        JSONObject jsonObject= new JSONObject();
        ArrayList<JBeacon> arrayJBeacons = new ArrayList<>();
        String group = getResources().getString(R.string.groupDefault);
        String user = getResources().getString(R.string.userNameDefault);
        try {
            jsonObject.put("group", group);
            jsonObject.put("username", user);
            JBeacon jb = new JBeacon();
            jb.setAddress("E1:D6:C9:98:5F:B0");
            jb.setRssi(-30);
            JBeacon jb1 = new JBeacon();
            jb1.setAddress("E4:44:68:44:23:40");
            jb1.setRssi(-90);
            arrayJBeacons.add(jb);
            arrayJBeacons.add(jb1);
            jsonObject.put("wifi-fingerprint",new JSONArray(arrayJBeacons.toString()));

            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getLocation(String stringResult){
        String location = null;
        try {
            JSONObject jsonObject = new JSONObject(stringResult);
            location = jsonObject.getString("location");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return location;
    }

    public LatLng getLntLong(String location){
        Room tmp = null;
        for(Room r: RoomsCTI.rooms){
            if(r.getNickName().equals(location)){
                tmp = r;
            }
        }
        return tmp.getCoordinates();
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

}
