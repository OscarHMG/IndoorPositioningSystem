package com.oscarhmg.indoorpositioningsystem;

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
import com.oscarhmg.indoorpositioningsystem.beacon.BeaconArrayAdapter;
import com.oscarhmg.indoorpositioningsystem.beacon.JBeacon;
import com.oscarhmg.indoorpositioningsystem.room.Room;
import com.oscarhmg.indoorpositioningsystem.room.RoomsCTI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by OscarHMG on 10/01/2017.
 */
public class AsyncMapTask extends AsyncTask<Object,Void,ArrayList<Object>> {
    private long time_Send;
    final private long interval = 2050;
    final private long wait = 950;
    private HttpHandler cliente;
    private BeaconArrayAdapter arrayAdapter;
    private GoogleMap map;
    private static Marker visitorMarker;
    private static Marker visitedMarker;
    private static ArrayList <LatLng>ubications = new ArrayList();
    private static ArrayList <Polyline> polylines = new ArrayList<>();
    private String visitorName;
    private int optionOfRadioButton;
    private String response;
    private String optionSelectedInSpinner;
    private final static int OPTION_FIND_PERSON = 1;
    private final static int OPTION_FIND_ROOM = 2;


    @Override
    protected ArrayList<Object> doInBackground(Object... params) {
        ArrayList<Object> result = new ArrayList<>();
        arrayAdapter = (BeaconArrayAdapter) params[0];
        map = (GoogleMap) params[1];
        optionOfRadioButton = (int) params[2];
        optionSelectedInSpinner = (String) params[3];
        visitorName = (String)params[4];
       if(!isCancelled() && arrayAdapter.getCount()!=0) {/*If scanner info is !=null */
           cliente = new HttpHandler();
           Room visitor = GetMyActualPosition();
           Room visited = doOperationBaseInUserOption(optionOfRadioButton); //Get room or get position of X person in CTI Building
           result.add(visitor);
           result.add(visited);
           getRequestPath(visitor, visited);
       }else{
           this.cancel(true);
           return null;
       }
        return result;

    }

    @Override
    protected void onPostExecute(ArrayList<Object> objects) {
        super.onPostExecute(objects);
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
            if(optionOfRadioButton == OPTION_FIND_PERSON){
                visitedMarker.setIcon(BitmapDescriptorFactory.fromResource(R.raw.visited));
            }else{
                visitedMarker.setIcon(BitmapDescriptorFactory.fromResource(R.raw.room));
            }

        }
        drawPath();
        this.cancel(true);
    }


    public Room doOperationBaseInUserOption(int operation){
        switch (operation){
            case OPTION_FIND_PERSON:
                return getPositionOfVisitedJSONRequest();
            case OPTION_FIND_ROOM:
                return getRoomByName(optionSelectedInSpinner);
        }
        return null;
    }


    public Room getPositionOfVisitedJSONRequest(){
        String nickname = null;
        HttpHandler request = new HttpHandler();
        try{
        JSONObject jsonToSend = new JSONObject();
        jsonToSend.put("visitante", optionSelectedInSpinner);
        String response = request.postJSON(jsonToSend, Constants.URL_FIND_VISITED_PERSON);
        JSONObject jsonResponse = new JSONObject(response);
        nickname = (String) jsonResponse.get("location");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return getRoomByNickName(nickname);
    }


    public Room getRoomByNickName(String nickname){
        Room room = null;
        for(Room r : RoomsCTI.rooms){
           if(r.getNickName().equals(nickname)){
               room = r;
           }
        }
        return room;
    }

    public Room getRoomByName(String name){
        Room room = null;
        for(Room r : RoomsCTI.rooms){
            if(r.getNameRoom().equals(name)){
                room = r;
            }
        }
        return room;
    }
    

    public Room GetMyActualPosition() {
        HttpHandler requestPush = new HttpHandler();
        Room myPosition = null;
        if ((time_Send + interval) < System.currentTimeMillis()) {
            response = cliente.request(Constants.PROXY_SERVER, toJSON());
            requestPush.postJSON(createJSONTOLog(response), Constants.URL_CONNECTION_LOG);
            //Here take the response and take the location
            Log.i("DATOS RESPONSE: ", response);
            String room = getLocationFromJSONResponse(response);
            if (room != null) { //Succesfull
                Log.i("ROOM UBICATION: ", room);
                myPosition = getRoomByNickName(room); // Get the room where I am
            }
        }
        try { //Stop scanning by x time
            synchronized (this) {
                wait(wait);
            }
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        time_Send = System.currentTimeMillis();
        return myPosition;
    }

    public JSONObject createJSONTOLog(String response){
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(response);
            jsonObject.put("username", visitorName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    private String toJSON() {
        JSONObject jsonObject = new JSONObject();
        ArrayList<JBeacon> arrayJBeacons = new ArrayList<>();

        try {
            jsonObject.put("group", Constants.GROUP);
            jsonObject.put("username", visitorName);

            for (int x = 0; x < arrayAdapter.getCount(); x++) {
                JBeacon jb = new JBeacon();
                jb.setAddress(arrayAdapter.getItem(x).getDeviceAddress());
                jb.setRssi(arrayAdapter.getItem(x).getRssi());
                arrayJBeacons.add(jb);
            }
            arrayAdapter.clear();
            jsonObject.put("wifi-fingerprint", new JSONArray(arrayJBeacons.toString()));
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getLocationFromJSONResponse(String stringResult) {
        String location = null;
        try {
            JSONObject jsonObject = new JSONObject(stringResult);
            location = jsonObject.getString("location");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return location;
    }



    public void getRequestPath(Room visitor, Room visited){
        String jsonResponseDijsktra = null;
        ubications.clear();
        try {
            HttpHandler requestDijkstra = new HttpHandler();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("inicio", visitor.getNickName());
            jsonObject.put("fin", visited.getNickName());
            jsonResponseDijsktra = requestDijkstra.postJSON(jsonObject, Constants.URL_GET_SHORTEST_PATH);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(jsonResponseDijsktra ==null){
            Log.i("Null Path"," Error in path");
        }else{
            ubications = getShortestPathFromJSON(jsonResponseDijsktra);

        }
    }

    public ArrayList getShortestPathFromJSON(String msg){
        ArrayList<LatLng> tmp = new ArrayList<>();
        try {
            JSONArray jsonArr = new JSONArray(msg);
            for(int i = 0; i < jsonArr.length(); i++){
                JSONArray jsonObj = jsonArr.getJSONArray(i);
                JSONArray positions =  jsonObj.getJSONArray(0);
                JSONArray filter = positions.getJSONArray(1);
                LatLng ubication = new LatLng((Double)filter.get(0),(Double)filter.get(1));
                tmp.add(ubication);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return tmp;
    }


    public void drawPath() {
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

}