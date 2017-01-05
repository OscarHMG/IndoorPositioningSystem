package com.oscarhmg.indoorpositioningsystem;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.ParcelUuid;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.oscarhmg.indoorpositioningsystem.room.Room;
import com.oscarhmg.indoorpositioningsystem.room.RoomsCTI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback{

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    //Scanners
    private static final String TAG = "EddystoneValidator";
    private static final int REQUEST_ENABLE_BLUETOOTH = 1;
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;
    //Aggressive scanning for nearby devices and reports inmediately
    private static ScanSettings SCAN_SETTINGS =
            new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).setReportDelay(0).build();

    //EddyStone service UUID
    private static final ParcelUuid EDDYSTONE_SERVICE_UUID =
            ParcelUuid.fromString("0000FEAA-0000-1000-8000-00805F9B34FB");

    private BluetoothLeScanner scanner;
    private BeaconArrayAdapter arrayAdapter;

    private List<ScanFilter> scanFilters;
    private ScanCallback scanCallback;
    private HttpHandler cliente;
    private String _User;
    private String _Server;
    private String _Group;
    private long time_Send;
    final private long interval = 2050;
    final private long wait = 950;
    private LatLng actualUbication;
    private Marker marker;
    private Polyline path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        prepareMap();
        prepareScanners();

    }

    public void prepareMap(){
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
        if(status == ConnectionResult.SUCCESS){
            //Google Services is avalaible, then set the Map
            setUpMapIfNeeded();
        }else{
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status,(Activity)getApplicationContext(),10);
            dialog.show();
        }
    }

    public void prepareScanners(){
        cliente = new HttpHandler(); //To send data to the server.
        time_Send = System.currentTimeMillis();
        initScanners(); //init();
        scanFilters = new ArrayList<>();
        scanFilters.add(new ScanFilter.Builder().setServiceUuid(EDDYSTONE_SERVICE_UUID).build());
        arrayAdapter = new BeaconArrayAdapter(this, R.layout.beacon_list_item, new ArrayList<Beacon>());
        scanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                ScanRecord scanRecord = result.getScanRecord();
                if(result.getDevice().getAddress() == null){
                    Log.e("Error","Probelamas");
                }
                if(scanRecord == null){
                    Log.v("No Beacons","No existen resultados");
                    return;
                }
                if(arrayAdapter.existBeacon(result.getDevice().getAddress()))
                    arrayAdapter.getItem(result.getDevice().getAddress()).setRssi(result.getRssi());
                else
                    arrayAdapter.add(new Beacon(result.getDevice().getAddress(), result.getRssi()));

                Log.v(TAG, "MAAC: " + result.getDevice().getAddress() + ", RSSI: " + result.getRssi());
                EnviarDatos();
                return;
            }

            @Override
            public void onScanFailed(int errorCode) {
                switch (errorCode){
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        if (scanner != null) {
            scanner.startScan(scanFilters, SCAN_SETTINGS, scanCallback);
            Log.i("Process:", "SCANNING");

        }
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            /*mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();*/
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mMap = mapFragment.getMap();
            mapFragment.getMapAsync(this);

            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        LatLng cti = new LatLng(-2.145818, -79.948939);
        //LatLng test = new LatLng(160,305);
        //mMap.addMarker(new MarkerOptions().position(test).title("Test"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(cti, 20));

        setOverlayCTIMap();
        /*mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                MarkerOptions marker = new MarkerOptions().position(
                        new LatLng(latLng.latitude, latLng.longitude)).title("New Marker");

                mMap.addMarker(marker);
                System.out.println("TEST POSITION: "+latLng);
            }
        });*/
    }

    public void setOverlayCTIMap(){
        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.raw.copia);
        LatLng soutWest = new LatLng(-2.14611, -79.9493155);
        LatLng northEast = new LatLng(-2.14557, -79.948475);
        LatLngBounds latLngBounds = new LatLngBounds(soutWest,northEast);
        GroundOverlayOptions groundOverlayOptions = new GroundOverlayOptions();
        groundOverlayOptions.positionFromBounds(latLngBounds);
        groundOverlayOptions.image(bitmapDescriptor);
        groundOverlayOptions.transparency(0.5f);
        mMap.addGroundOverlay(groundOverlayOptions);
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        setUpMap();
    }

    public void setMarkersOnRooms(){
        Log.i("Size: ", "" + RoomsCTI.rooms.size());
        Log.i("Positions: ", "" + RoomsCTI.rooms.get(1).getCoordinates());
        Room tmp = null;
        for(Room r: RoomsCTI.rooms){
            tmp = r ;
            mMap.addMarker(new MarkerOptions().position(tmp.getCoordinates()).title(tmp.getNameRoom()));
        }
    }


    private void initScanners() {
        BluetoothManager manager = (BluetoothManager) this.getApplicationContext()
                .getSystemService(Context.BLUETOOTH_SERVICE); //solicita el bluetooth al telefono
        BluetoothAdapter btAdapter = manager.getAdapter();
        if (btAdapter == null) {
            showFinishingAlertDialog("Bluetooth Error", "Bluetooth not detected on device");
        } else if (!btAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            this.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BLUETOOTH);
        } else {
            scanner = btAdapter.getBluetoothLeScanner();
        }

    }

    public String getLocation(String stringResult){
        String location = null;
        try {
            JSONObject jsonObject = new JSONObject(stringResult);
            location = jsonObject.getString("location");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return location;
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
            marker = mMap.addMarker(new MarkerOptions().position(tmp.getCoordinates()).title(tmp.getNameRoom()));
        }else{
            marker = mMap.addMarker(new MarkerOptions().position(tmp.getCoordinates()).title(tmp.getNameRoom()));
        }
        drawPath(tmp.getCoordinates());
        marker.setIcon(BitmapDescriptorFactory.fromResource(R.raw.visitor));

    }

    public void EnviarDatos(){
        //onResume();
        if ((time_Send + interval) < System.currentTimeMillis()) {
            if (CargarDatos()) {
                String response = cliente.request(_Server, toJSON());
                //Here take the response and take the location
                Log.i("DATOS RESPONSE: ",response);
                String room = getLocation(response);
                if(room!=null){ //Succesfull
                    Log.i("ROOM UBICATION: ", room);
                    getLntLong(room);
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
    }

    private boolean CargarDatos() {
        _User = (getResources().getString(R.string.userNameDefault));
        _Server =(getResources().getString(R.string.serverDefault));
        _Group = (getResources().getString(R.string.groupDefault));
        if (_User == null || _Server == null  || _Group ==null)
            return false;
        return true;
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BLUETOOTH) {
            if (resultCode == Activity.RESULT_OK) {
                initScanners();
            } else {
                this.finish();
            }
        }
    }

    private String toJSON(){
        JSONObject jsonObject= new JSONObject();
        ArrayList<JBeacon> arrayJBeacons = new ArrayList<>();

        try {
            jsonObject.put("group", _Group);
            jsonObject.put("username", _User);

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
    private void logErrorAndShowToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        Log.e(TAG, message);
    }


    private void showFinishingAlertDialog(String title, String message) {
        new AlertDialog.Builder(this).setTitle(title).setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                }).show();
    }


    public void drawPath(LatLng visited) {
        //-2.145835029709358),-79.9487455934286
        if (path !=null)
            path.remove();
        path = mMap.addPolyline(new PolylineOptions()
                .add(visited, new LatLng(-2.145835029709358, -79.9487455934286))
                .width(5)
                .color(Color.RED));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        scanner.stopScan(scanCallback);
    }
}
