package com.oscarhmg.indoorpositioningsystem;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by user on 26/01/2017.
 */
public class AsyncTaskHttpRequestOnlinePeople extends AsyncTask<Void, Void, ArrayList<String>> {
    ArrayList<String> persons = new ArrayList<>();
    public ReturnData returnData;
    @Override
    protected ArrayList<String> doInBackground(Void... voids) {
        HttpHandler request = new HttpHandler();
        String response = request.getJSON("https://testpositionserver-dot-navigator-cloud.appspot.com/find_online_people");
        Log.i("Online people", "" + response);

        persons.addAll(getNamesofJSON(response));
        return persons;
    }

    @Override
    protected void onPostExecute(ArrayList<String> strings) {
        super.onPostExecute(strings);
        if(strings == null)
            Log.e("Error","NULO");
        returnData.returnDataList(strings);
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
                    Log.i("Names:", "" + name);
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
