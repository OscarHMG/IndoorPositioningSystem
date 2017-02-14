package com.oscarhmg.indoorpositioningsystem;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by user on 11/02/2017.
 */
public class AsyncOrientationTask extends AsyncTask {
    private double angleRotation;
    private Context context;
    private static Toast toast = null;

    public AsyncOrientationTask(Context context) {
        this.context = context;
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        LatLng init = (LatLng) objects[0];
        LatLng second = (LatLng)objects[1];
        angleRotation = (double) objects[2];
        toast = (Toast)objects[3];
        JSONObject jsonObject =createJSONTracking(init, second);
        Log.i("JSON POINTS", jsonObject.toString());
        Log.i("rotation onpost",""+angleRotation);
        HttpHandler httpRequest = new HttpHandler();
        String orientationResponse = httpRequest.postJSON(jsonObject,Constants.URL_GET_ORIENTATION);
        try {
            JSONObject responseOrientation = new JSONObject(orientationResponse);
            responseOrientation.get("instruction");
            /*if(orientation == null){
                orientation = orientationResponse;
            }else{
                if(!orientation.equals(orientationResponse)){
                    //Show orientation to the user
                    orientation = orientationResponse;
                }
            }*/
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.i("Orientation",orientationResponse);

        return orientationResponse;
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        String response = (String)o;
        toast.makeText(context,"Orientation:"+response,Toast.LENGTH_SHORT).show();
        //toast.cancel();
        this.cancel(true);
    }

    public JSONObject createJSONTracking(LatLng init, LatLng goal){
        JSONObject obj = new JSONObject();
        try {
            obj.put("pointA",init.latitude);
            obj.accumulate("pointA", init.longitude);
            obj.put("pointB", goal.latitude);
            obj.accumulate("pointB", goal.longitude);
            obj.put("angle",angleRotation);
            Log.i("Rotation JSON", "" + angleRotation);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return obj;
    }
}
