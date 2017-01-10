package com.oscarhmg.indoorpositioningsystem;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.oscarhmg.indoorpositioningsystem.room.Room;
import com.oscarhmg.indoorpositioningsystem.room.RoomsCTI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by user on 08/01/2017.
 */
public class AsyncScanBeacons extends AsyncTask<Activity, Void, Marker> {
    private String user;
    private String server;
    private String group;


    private Context context;
    private BeaconArrayAdapter arrayAdapter;
    private ScanCallback scanCallback;
    private long time_Send;
    final private long interval = 2050;
    final private long wait = 950;


    private HttpHandler cliente;
    private HttpHandler requestDijkstra;
    private Marker marker;
    private GoogleMap map;
    private ArrayList <Polyline> route;

    public AsyncScanBeacons(Context context,String user, String server, String group,GoogleMap map) {
        this.context = context;
        this.user = user;
        this.server = server;
        this.group = group;
        this.map = map;
    }


    //1st paramter: scanCallback; 2nd parameter:adapter
    @Override
    protected Marker doInBackground(Activity... params) {
        arrayAdapter = new BeaconArrayAdapter(context, R.layout.beacon_list_item, new ArrayList<Beacon>());
        scanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                ScanRecord scanRecord = result.getScanRecord();
                if (result.getDevice().getAddress() == null) {
                    Log.e("Error", "Beacons doesn't read properly");
                }
                if (scanRecord == null) {
                    Log.v("No Beacons", "No results of scanning");
                    return;
                }
                if (arrayAdapter.existBeacon(result.getDevice().getAddress()))
                    arrayAdapter.getItem(result.getDevice().getAddress()).setRssi(result.getRssi());
                else
                    arrayAdapter.add(new Beacon(result.getDevice().getAddress(), result.getRssi()));

                Log.v("MSSGES:", "MAAC: " + result.getDevice().getAddress() + ", RSSI: " + result.getRssi());
                EnviarDatos();
                return;
            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
                switch (errorCode) {
                    case SCAN_FAILED_ALREADY_STARTED:
                        //logErrorAndShowToast("SCAN_FAILED_ALREADY_STARTED");
                        break;
                    case SCAN_FAILED_APPLICATION_REGISTRATION_FAILED:
                        logErrorAndShowToast("SCAN_FAILED_APPLICATION_REGISTRATION_FAILED");
                        break;
                    case SCAN_FAILED_FEATURE_UNSUPPORTED:
                        logErrorAndShowToast("SCAN_FAILED_FEATURE_UNSUPPORTED");
                        break;
                    case SCAN_FAILED_INTERNAL_ERROR:
                        logErrorAndShowToast("SCAN_FAILED_INTERNAL_ERROR");
                        break;
                    default:
                        logErrorAndShowToast("Scan failed, unknown error code");
                        break;
                }
            }
        };
        return null;
    }

    public void EnviarDatos(){
        if ((time_Send + interval) < System.currentTimeMillis()) {
            String response = cliente.request(server, toJSON());
            //Here take the response and take the location
            Log.i("DATOS RESPONSE: ",response);
            String room = parseJSONResponse(response);
            if(room!=null){ //Succesfull
                Log.i("ROOM UBICATION: ", room);
                getLntLong(room); // Ubico el punto.
            }
        }
            try { //deja de escanear por un intervalo de tiempo
                synchronized (this) {
                    wait(wait);
                }
            }catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            time_Send = System.currentTimeMillis();
    }

    public String parseJSONResponse(String stringResult){
        String location = null;
        try {
            JSONObject jsonObject = new JSONObject(stringResult);
            location = jsonObject.getString("location");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return location;
    }

    private String toJSON(){
        JSONObject jsonObject= new JSONObject();
        ArrayList<JBeacon> arrayJBeacons = new ArrayList<>();

        try {
            jsonObject.put("group", group);
            jsonObject.put("username", user);

            for (int x=0; x<arrayAdapter.getCount();x++){
                JBeacon jb = new JBeacon();
                jb.setAddress(arrayAdapter.getItem(x).getDeviceAddress());
                jb.setRssi(arrayAdapter.getItem(x).getRssi());
                arrayJBeacons.add(jb);
            }
            arrayAdapter.clear();
            jsonObject.put("wifi-fingerprint",new JSONArray(arrayJBeacons.toString()));
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void getLntLong(String location){
        Room tmp = null;
        for(Room r: RoomsCTI.rooms){
            if(r.getNickName().equals(location)){
                tmp = r;
            }
        }
        //Here locations are null sometimes.
        if(marker!= null){
            marker.remove();
            marker = map.addMarker(new MarkerOptions().position(tmp.getCoordinates()).title(tmp.getNameRoom()));
        }else{
            marker = map.addMarker(new MarkerOptions().position(tmp.getCoordinates()).title(tmp.getNameRoom()));
        }
        //Here make the request to get the shortest path and get the JSON
        //String acd = requestDijkstra.request("https://testpositionserver-dot-navigator-cloud.appspot.com/get_shortest_path",requestSorthestPathJSON(tmp.getNickName(),"labihm"));
        //Get the points and use "drawPath"
        String pathJSON = null;
        ArrayList<LatLng> ubications = null;
        try {
            pathJSON = requestDijkstra.requestJSONPubSub(tmp.getNickName(),"labihm");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(pathJSON ==null){
            Log.i("Error"," NUlo");
        }else{
            //Log.i("DIJKSTRA: ", "" + x.toString());
            ubications = getDijsktraFromJSONResponse(pathJSON);
        }
        //
        drawPath(ubications);
        marker.setIcon(BitmapDescriptorFactory.fromResource(R.raw.visitor));

    }


    public ArrayList getDijsktraFromJSONResponse(String msg){
        ArrayList<LatLng> tmp = new ArrayList<>();
        try {
            JSONArray jsonArr = new JSONArray(msg);
            for(int i = 0; i < jsonArr.length(); i++){
                JSONArray jsonObj = jsonArr.getJSONArray(i);
                JSONArray positions =  jsonObj.getJSONArray(0);
                JSONArray filter = positions.getJSONArray(1);

                Double x = (Double) filter.get(0);
                Double y = (Double) filter.get(1);
                LatLng ubication = new LatLng((Double)filter.get(0),(Double)filter.get(1));
                tmp.add(ubication);
                System.out.println("LEYENDO:  -----("+x+","+y+")");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return tmp;
    }

    public void drawPath(ArrayList<LatLng> ubications) {
        //-2.145835029709358),-79.9487455934286
        if (route.size()>0)
            route.clear();
        for (int i = 0; i < ubications.size() - 1; i++) {

            Polyline line = map.addPolyline(new PolylineOptions()
                    .add(ubications.get(i), ubications.get(i + 1))
                    .width(5)
                    .color(Color.RED));
            route.add(line);
        }
    }

    private void logErrorAndShowToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }




}
