package com.oscarhmg.indoorpositioningsystem;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by user on 26/01/2017.
 */
public class AsyncTaskHttpRequestOnlinePeople extends AsyncTask<Void, Void, Void> {
    private ArrayList<String> persons = new ArrayList<>();

    @Override
    protected Void doInBackground(Void... voids) {
        HttpHandler request = new HttpHandler();
        String response = request.getRequestOnlinePeople("https://testpositionserver-dot-navigator-cloud.appspot.com/find_online_people");
        persons = getNamesofJSON(response);
        return null;
    }


    public ArrayList<String> getNamesofJSON(String response){
        JSONArray arrayName;
        ArrayList<String> persons = new ArrayList<>();
        try {
            arrayName = new JSONArray(response);
            if(arrayName!=null) {
                for (int i = 0; i < arrayName.length(); i++) {
                    JSONObject person = (JSONObject) arrayName.get(i);
                    String name = (String) person.get("username");
                    persons.add(name);
                }
            }else{
                return persons;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return persons;
    }

    public ArrayList<String> getPersons() {
        return persons;
    }
}
