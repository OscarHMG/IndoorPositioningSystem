package com.oscarhmg.indoorpositioningsystem.beacon;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by OscarHMG on 14/12/2016.
 */
public class JBeacon {
    private String address;
    private int rssi;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public String toString(){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("mac", this.getAddress().toString());
            jsonObject.put("rssi", this.getRssi());
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
