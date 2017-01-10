package com.oscarhmg.indoorpositioningsystem;

import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;
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
 * Created by user on 10/01/2017.
 */
public class AsyncTaskHttpHandler extends AsyncTask<Object,Void,ArrayList<Object>> {
    private long time_Send;
    final private long interval = 2050;
    final private long wait = 950;
    private HttpHandler cliente;
    private String _User;
    private String _Server;
    private String _Group;
    private BeaconArrayAdapter arrayAdapter;
    private GoogleMap map;
    private static Marker visitorMarker;
    private ArrayList <LatLng>ubications = new ArrayList();
    private static ArrayList <Polyline> polylines = new ArrayList<>();


    @Override
    protected ArrayList<Object> doInBackground(Object... params) {
        ArrayList<Object> result= new ArrayList<>();
        arrayAdapter = (BeaconArrayAdapter) params[0];
        map = (GoogleMap) params[1];
        //visitorMarker = (Marker) params[2];
        cliente = new HttpHandler();
        Room visitor = EnviarDatos();
        result.add(visitor);
        //get Visited point
        Room visited =  new Room("Human Computer Interaction Lab", new LatLng((-2.1457268114567625),-79.94881600141525),"labihm");
        //get the path (Json GET)
        if(visitor!= null && visited!=null){
            getRequestPath(visitor,visited);
        }

        return result;
    }

    @Override
    protected void onPostExecute(ArrayList<Object> objects) {
        super.onPostExecute(objects);
        Room myPoint = (Room) objects.get(0);

        if(myPoint!=null) {
            if (visitorMarker != null)
                visitorMarker.remove();
            visitorMarker = map.addMarker(new MarkerOptions().position(myPoint.getCoordinates()));
            visitorMarker.setIcon(BitmapDescriptorFactory.fromResource(R.raw.visitor));
        }
        //visitorMarker = map.addMarker(new MarkerOptions().position(myPoint));
        //Then draw marker of visited
        //Now draw path
        drawPath();

    }

    private boolean CargarDatos() {
        _User = "AppAndroid";
        _Server = "http://navigator-cloud.appspot.com/track";
        _Group = "test1";
        if (_User == null || _Server == null || _Group == null)
            return false;
        return true;
    }

    public Room EnviarDatos() {
        //onResume();
        Room roomVisitor = null;
        if ((time_Send + interval) < System.currentTimeMillis()) {
            if (CargarDatos()) {
                String response = cliente.request(_Server, toJSON());
                //Here take the response and take the location
                Log.i("DATOS RESPONSE: ", response);
                String room = getLocation(response);
                if (room != null) { //Succesfull
                    Log.i("ROOM UBICATION: ", room);
                    roomVisitor = getLntLong(room); // return el punto LatLng
                }
            }
            try { //deja de escanear por un intervalo de tiempo
                synchronized (this) {
                    wait(wait);
                }
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            time_Send = System.currentTimeMillis();
        }
        return roomVisitor;
    }


    private String toJSON() {
        JSONObject jsonObject = new JSONObject();
        ArrayList<JBeacon> arrayJBeacons = new ArrayList<>();

        try {
            jsonObject.put("group", _Group);
            jsonObject.put("username", _User);

            for (int x = 0; x < arrayAdapter.getCount(); x++) {
                JBeacon jb = new JBeacon();
                jb.setAddress(arrayAdapter.getItem(x).getDeviceAddress());
                jb.setRssi(arrayAdapter.getItem(x).getRssi());
                arrayJBeacons.add(jb);
            }
            arrayAdapter.clear();
            jsonObject.put("wifi-fingerprint", new JSONArray(arrayJBeacons.toString()));
            //Log.i("JSON TO SEND",""+jsonObject.toString());
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getLocation(String stringResult) {
        String location = null;
        try {
            JSONObject jsonObject = new JSONObject(stringResult);
            location = jsonObject.getString("location");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return location;
    }

    public Room getLntLong(String location) {
        Room tmp = null;
        for (Room r : RoomsCTI.rooms) {
            if (r.getNickName().equals(location)) {
                tmp = r;
            }
        }
        //Here locations are null sometimes.
        return tmp;
    }

    public void getRequestPath(Room visitor, Room visited){
        String jsonResponseDijsktra = null;
        ubications = new ArrayList();
        try {
            HttpHandler requestDijkstra = new HttpHandler();
            jsonResponseDijsktra = requestDijkstra.requestJSONPubSub(visitor.getNickName(),visited.getNickName());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(jsonResponseDijsktra ==null){
            Log.i("Error"," NUlo");
        }else{
            //Log.i("DIJKSTRA: ", "" + x.toString());
            ubications = getShortestPathFromJSON(jsonResponseDijsktra);
        }
        //
        //drawPath(ubications);

    }

    public ArrayList getShortestPathFromJSON(String msg){
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


    public void drawPath() {
        //-2.145835029709358),-79.9487455934286
        if (polylines.size()>0)
            deletePath();
        for (int i = 0; i < ubications.size() - 1; i++) {
            polylines.add(map.addPolyline(new PolylineOptions()
                    .add(ubications.get(i), ubications.get(i + 1))
                    .width(5)
                    .color(Color.RED)));
        }
    }

    public void deletePath(){
        for(Polyline line : polylines) {
            line.remove();
        }
        polylines.clear();
    }

    //Visited functions

    public void getRequestVisitedJSON(){

    }
}
