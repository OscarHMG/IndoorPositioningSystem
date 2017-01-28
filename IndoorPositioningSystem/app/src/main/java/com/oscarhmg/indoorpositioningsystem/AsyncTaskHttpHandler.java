package com.oscarhmg.indoorpositioningsystem;

import android.app.Activity;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

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
    private static Marker visitedMarker;
    private static ArrayList <LatLng>ubications = new ArrayList();
    private static ArrayList <Polyline> polylines = new ArrayList<>();
    private String visitorName;
    private Activity activityMap;
    private static Room tmpVisitor,tmpVisited;
    private int operation;
    private String response;

    public AsyncTaskHttpHandler(Activity activityMap) {
        this.activityMap = activityMap;
    }

    @Override
    protected ArrayList<Object> doInBackground(Object... params) {
        ArrayList<Object> result = new ArrayList<>();
        arrayAdapter = (BeaconArrayAdapter) params[0];
        map = (GoogleMap) params[1];
        operation = (int) params[2];
        String optionSelected = (String) params[3];
        visitorName = (String)params[4];
       if(!isCancelled() && arrayAdapter.getCount()!=0) {
           cliente = new HttpHandler();
           Room visitor = EnviarDatos();
           //Room visitor = new Room("Hall", new LatLng((-2.14588796618926),-79.94867786765099),"pasilloproto1");
           Room visited = doOperation(operation, optionSelected);

           result.add(visitor);
           result.add(visited);
           getRequestPath(visitor, visited);
           /*if (visitor != null && visited != null) {
               //Get the ubications to follow and later draw the path in postExecute method
               getRequestPath(visitor, visited);
           }*/
       }else{
           this.cancel(true);
           return null;
       }
        return result;

    }

    @Override
    protected void onPostExecute(ArrayList<Object> objects) {
        super.onPostExecute(objects);
        //this.cancel(true);

        if (objects != null) {
            Room myPoint = (Room) objects.get(0);
            Room visitedPoint = (Room)objects.get(1);
            if (visitorMarker != null)
                visitorMarker.remove();
            visitorMarker = map.addMarker(new MarkerOptions().position(myPoint.getCoordinates()));
            visitorMarker.setIcon(BitmapDescriptorFactory.fromResource(R.raw.visitor));
            if(visitedMarker!=null)
                visitedMarker.remove();
            visitedMarker = map.addMarker(new MarkerOptions().position(visitedPoint.getCoordinates()));
            if(operation==1){
                visitedMarker.setIcon(BitmapDescriptorFactory.fromResource(R.raw.visited));
            }else{
                visitedMarker.setIcon(BitmapDescriptorFactory.fromResource(R.raw.room));
            }

        }

            //visitorMarker = map.addMarker(new MarkerOptions().position(myPoint));
            //Then draw marker of visited
            //Now draw path
        drawPath();
        this.cancel(true);
    }
        //this.cancel(true);


    public Room doOperation(int operation, String optionSelected){
        Log.i("operation selected",optionSelected);
        switch (operation){
            case 1:
                if(optionSelected.equals("Oscar")){
                    return new Room("Rapid Prototyping Lab", new LatLng((-2.145940902667342),-79.94867417961359),"labproto");
                }
                if(optionSelected.equals("Sergio Moncayo")){
                    return new Room("Hall", new LatLng((-2.14588796618926),-79.94867786765099),"pasilloproto1");
                }
                if(optionSelected.equals("fer")){
                    return new Room("Lounge", new LatLng((-2.145801190566184),-79.94862053543329),"salaespera");
                }
            case 2:
                return getRoomByName(optionSelected);
        }
        return null;
    }


    public Room getRoomByName(String name){
        Room room = null;
        for(Room r : RoomsCTI.rooms){
            if(r.getNameRoom().equals(name)){
                room = r;
            }

        }
        //Log.i("Room:",""+room.getNickName());
        return room;
    }

    private boolean CargarDatos() {
        _User = "AppAndroid";
        _Server = "http://200.126.23.144:49160/track";
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
                response = cliente.request(_Server, toJSON());

                HttpHandler requestPush = new HttpHandler();
                requestPush.pushToHistoryPeople(buildJSONToPOST(response),"https://testpositionserver-dot-navigator-cloud.appspot.com/push_message");
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

    public JSONObject buildJSONToPOST(String response){
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(response);
            jsonObject.put("username",visitorName);
            Log.i("PUSH SERGIO:",""+jsonObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    private String toJSON() {
        JSONObject jsonObject = new JSONObject();
        ArrayList<JBeacon> arrayJBeacons = new ArrayList<>();

        try {
            jsonObject.put("group", _Group);
            jsonObject.put("username", visitorName);

            for (int x = 0; x < arrayAdapter.getCount(); x++) {
                JBeacon jb = new JBeacon();
                jb.setAddress(arrayAdapter.getItem(x).getDeviceAddress());
                jb.setRssi(arrayAdapter.getItem(x).getRssi());
                arrayJBeacons.add(jb);
            }
            arrayAdapter.clear();
            jsonObject.put("wifi-fingerprint", new JSONArray(arrayJBeacons.toString()));
            Log.i("JSON TO SEND", "" + jsonObject.toString());
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
        ubications.clear();
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
            Log.i("DIJKSTRA: ", "" + jsonResponseDijsktra.toString());
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
        if (polylines.size()>0){
            deletePath();
        }
        if(ubications.size()>=2) {
            for (int i = 0; i < ubications.size() - 1; i++) {
                polylines.add(map.addPolyline(new PolylineOptions()
                        .add(ubications.get(i), ubications.get(i + 1))
                        .width(5)
                        .color(Color.RED)));
            }
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
