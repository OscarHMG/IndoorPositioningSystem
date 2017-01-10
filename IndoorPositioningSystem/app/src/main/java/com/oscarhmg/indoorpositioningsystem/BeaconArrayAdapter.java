package com.oscarhmg.indoorpositioningsystem;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 14/12/2016.
 */
public class BeaconArrayAdapter extends ArrayAdapter<Beacon> implements Filterable {
    private List<Beacon> allBeacons;
    private List<Beacon> filteredBeacons;

    public BeaconArrayAdapter(Context context, int resource, List<Beacon> allBeacons){
        super(context, resource, allBeacons);
        this.allBeacons = allBeacons;
        this.filteredBeacons = allBeacons;
    }



    @Override
    public int getCount() {
        return filteredBeacons.size();
    }

    @Override
    public Beacon getItem(int position) {
        return filteredBeacons.get(position);
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                FilterResults results = new FilterResults();
                List<Beacon> filteredBeacons;
                if(charSequence != null && charSequence.length() != 0){
                    filteredBeacons = new ArrayList<>();
                    for(Beacon beacon : allBeacons){
                        if(beacon.contains(charSequence.toString())){
                            filteredBeacons.add(beacon);
                        }
                    }
                }else{
                    filteredBeacons = allBeacons;
                }
                results.count = filteredBeacons.size();
                results.values = filteredBeacons;
                return results;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                filteredBeacons = (List<Beacon>) filterResults.values;
                if(filterResults.count == 0){
                    notifyDataSetInvalidated();
                }else{
                    notifyDataSetChanged();
                }
            }
        };
    }

    public Boolean existBeacon(String _address){
        for(int x = 0; x<filteredBeacons.size(); x++)
            if(filteredBeacons.get(x).getDeviceAddress().equalsIgnoreCase(_address))
                return true;
        return false;
    }

    public Beacon getItem(String _address){
        for(int x=0; x<filteredBeacons.size(); x++)
            if(filteredBeacons.get(x).getDeviceAddress().equalsIgnoreCase(_address))
                return filteredBeacons.get(x);
        return null;
    }
}
