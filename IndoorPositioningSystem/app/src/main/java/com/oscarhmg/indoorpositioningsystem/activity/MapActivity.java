package com.oscarhmg.indoorpositioningsystem.activity;

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
import com.oscarhmg.indoorpositioningsystem.AsyncMapTask;
import com.oscarhmg.indoorpositioningsystem.BearingNorthProvider;
import com.oscarhmg.indoorpositioningsystem.R;
import com.oscarhmg.indoorpositioningsystem.beacon.Beacon;
import com.oscarhmg.indoorpositioningsystem.beacon.BeaconArrayAdapter;
import com.oscarhmg.indoorpositioningsystem.room.Room;
import com.oscarhmg.indoorpositioningsystem.room.RoomsCTI;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback, AsyncMapTask.RotateMarker{

    private GoogleMap mapCTI; // Might be null if Google Play services APK is not available.
    //Scanners
    private static final String TAG = "EddystoneValidator";
    private static final int REQUEST_ENABLE_BLUETOOTH = 1;
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
    private  int operation;
    private String optionSelected;
    private AsyncMapTask asyncThread;
    private String visitorName;

    private BearingNorthProvider mBearingProvider;
    private double angleRotation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        //mBearingProvider = new BearingNorthProvider(this);
        //mBearingProvider.setChangeEventListener(this);
        Intent intent=getIntent();
        Bundle extras =intent.getExtras();
        operation = (int) extras.get("operation");
        optionSelected = (String)extras.get("option");
        visitorName = (String)extras.get("visitorName");
        prepareMap();
        prepareScanners();
        //setMarkersOnRooms();
        /*mapCTI.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                MarkerOptions marker = new MarkerOptions().position(
                        new LatLng(point.latitude, point.longitude)).title("New Marker");

                mapCTI.addMarker(marker);

                Log.i("Position:","("+point.latitude +","+point.longitude+")");
            }
        });*/

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
        //First init the scanners and then execute the async thread to send and receive data from the cloud
        initScanners(); //init();
        scanFilters = new ArrayList<>();
        scanFilters.add(new ScanFilter.Builder().setServiceUuid(EDDYSTONE_SERVICE_UUID).build());
        arrayAdapter = new BeaconArrayAdapter(this, R.layout.beacon_list_item, new ArrayList<Beacon>());
        scanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                ScanRecord scanRecord = result.getScanRecord();
                if(result.getDevice().getAddress() == null){
                    Log.e("Error","Address of beacons is null");
                }
                if(scanRecord == null){
                    Log.v("No Beacons ","No results");
                    return;
                }
                if(arrayAdapter.existBeacon(result.getDevice().getAddress()))
                    arrayAdapter.getItem(result.getDevice().getAddress()).setRssi(result.getRssi());
                else
                    arrayAdapter.add(new Beacon(result.getDevice().getAddress(), result.getRssi()));

                //Log.v(TAG, "MAAC: " + result.getDevice().getAddress() + ", RSSI: " + result.getRssi());
                //Log.i("ROTATION:",""+angleRotation);
                asyncThread = (AsyncMapTask) new AsyncMapTask(MapActivity.this).execute(arrayAdapter, mapCTI,operation,optionSelected,visitorName,MapActivity.this);
                return;
            }

            @Override
            public void onScanFailed(int errorCode) {
                switch (errorCode){
                    case SCAN_FAILED_ALREADY_STARTED:
                        logErrorAndShowToast("SCAN_FAILED_ALREADY_STARTED");
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

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                super.onBatchScanResults(results);
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        if (scanner != null) {
            scanner.startScan(scanFilters, SCAN_SETTINGS, scanCallback);
            //mBearingProvider.start();
        }else{
            Log.i("Process:","ERROR, NOT SCANNING");
        }
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mapCTI} is not null.
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
        if (mapCTI == null) {
            // Try to obtain the map from the SupportMapFragment.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapCTI = mapFragment.getMap();
            mapFragment.getMapAsync(this);
            // Check if we were successful in obtaining the map.
            if (mapCTI != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mapCTI} is not null.
     */
    private void setUpMap() {
        LatLng cti = new LatLng(-2.145818, -79.948939);
        mapCTI.moveCamera(CameraUpdateFactory.newLatLngZoom(cti, 20));

        setOverlayCTIMap();
        /*mapCTI.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                MarkerOptions marker = new MarkerOptions().position(
                        new LatLng(latLng.latitude, latLng.longitude)).title("New Marker");

                mapCTI.addMarker(marker);
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
        mapCTI.addGroundOverlay(groundOverlayOptions);
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        setUpMap();
    }

    private void initScanners() {
        BluetoothManager manager = (BluetoothManager) this.getApplicationContext()
                .getSystemService(Context.BLUETOOTH_SERVICE); //solicita el bluetooth al telefono
        BluetoothAdapter btAdapter = manager.getAdapter();
        if (btAdapter == null) {
            showFinishingAlertDialog("Bluetooth Error", "Bluetooth not detected on device");
        } else if (!btAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BLUETOOTH);

        } else {
            scanner = btAdapter.getBluetoothLeScanner();
        }

    }


    public void setMarkersOnRooms() {
        Log.i("Size: ", "" + RoomsCTI.rooms.size());
        Log.i("Positions: ", "" + RoomsCTI.rooms.get(1).getCoordinates());
        Room tmp = null;
        for(Room r: RoomsCTI.rooms){
            tmp = r ;
            mapCTI.addMarker(new MarkerOptions().position(tmp.getCoordinates()).title(tmp.getNameRoom()));
        }
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        scanner.stopScan(scanCallback);
        asyncThread.cancel(true);
       // mBearingProvider.stop();
        this.finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
      //  mBearingProvider.stop();
    }



    @Override
    public void getRotationMarker(Marker marker) {
        marker.setRotation((float) angleRotation);
    }
}


