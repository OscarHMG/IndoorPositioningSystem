package com.oscarhmg.indoorpositioningsystem;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by OscarHMG on 26/01/2017.
 */
public class AsyncOnlinePeopleTask extends AsyncTask<Void, Void, ArrayList<String>> {
    ArrayList<String> persons = new ArrayList<>();
    private ReturnData returnData;

    public AsyncOnlinePeopleTask(ReturnData returnData) {
        this.returnData = returnData;
    }

    @Override
    protected ArrayList<String> doInBackground(Void... voids) {
        HttpHandler request = new HttpHandler();
        String response = request.getJSON(Constants.URL_FIND_ALL_PEOPLE_ONLINE);
        persons.addAll(getOnlinePeopleFromJSON(response));
        return persons;
    }

    @Override
    protected void onPostExecute(ArrayList<String> strings) {
        super.onPostExecute(strings);
        returnData.returnDataList(strings);
    }

    public ArrayList<String> getOnlinePeopleFromJSON(String response){
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


    public interface ReturnData {
        void returnDataList(List<String> list);
    }
}
