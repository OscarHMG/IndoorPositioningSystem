package com.oscarhmg.indoorpositioningsystem;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
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
import android.os.Bundle;
import android.os.ParcelUuid;
//import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 15/12/2016.
 */
public class IdentificationActivityFragment extends Fragment {
    private static final String TAG = "EddystoneValidator";
    private static final int REQUEST_ENABLE_BLUETOOTH = 1;
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;
    //Aggressive scanning for nearby devices and reports inmediately
    /*private static ScanSettings SCAN_SETTINGS =
            new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).setReportDelay(0).build();*/

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
    //UI Elements
    
    private EditText visitor;
    private Spinner professor;
    private Button submit;


    @Override
    public void onCreate(final Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        cliente = new HttpHandler(); //To send data to the server.
        time_Send = System.currentTimeMillis();
        initScanners(); //init();
        scanFilters = new ArrayList<>();
        scanFilters.add(new ScanFilter.Builder().setServiceUuid(EDDYSTONE_SERVICE_UUID).build());
        arrayAdapter = new BeaconArrayAdapter(getActivity(), R.layout.beacon_list_item, new ArrayList<Beacon>());
        scanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                ScanRecord scanRecord = result.getScanRecord();
                if(result.getDevice().getAddress() == null){
                    Log.e("Error","Problemas");
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

    public void EnviarDatos(){
        onResume();
        if ((time_Send + interval) < System.currentTimeMillis()) {
            if (CargarDatos()) {
                Log.i("JSON",toJSON());
                cliente.request(_Server, toJSON());
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
        IdentificationActivity _main = (IdentificationActivity) getActivity();
        _User = _main.getUserName();
        _Server = _main.getServer();
        _Group = _main.getGroup();
        if (_User == null || _Server == null  || _Group ==null)
            return false;
        return true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Identification layout in the inflater.
        final View view = inflater.inflate(R.layout.fragment_identification, container, false);
        visitor = (EditText)view.findViewById(R.id.visitorName);
        professor = (Spinner)view.findViewById(R.id.prof_spinner);
        submit = (Button)view.findViewById(R.id.submit);

        submit.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //Toast.makeText((IdentificationActivity) getActivity(),"CLICK",Toast.LENGTH_LONG).show();
                /*if (scanner != null) {
                    scanner.startScan(scanFilters, SCAN_SETTINGS, scanCallback);
                }else{
                    Toast.makeText((IdentificationActivity) getActivity(),"Scanner es null",Toast.LENGTH_LONG).show();
                }*/
                //onResume();
                //scannEnviroment();
                //actividad
                Intent intent = new Intent((IdentificationActivity)getActivity(), MapActivity.class);
                startActivity(intent);
            }
        });

        registerForContextMenu(view);
        return view;
        //return null;
    }

    public void scannEnviroment(){
        if (scanner != null) {
            Log.v("OnResume","Scanning");
            //scanner.startScan(scanFilters, SCAN_SETTINGS, scanCallback);
        }else{
            Log.v("OnResume","Scanning is null");
        }
    }
    @Override
    public void onResume() {
        super.onResume();


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BLUETOOTH) {
            if (resultCode == Activity.RESULT_OK) {
                initScanners();
            } else {
                getActivity().finish();
            }
        }
    }


    private void initScanners() {
        BluetoothManager manager = (BluetoothManager) getActivity().getApplicationContext()
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

    /**
     * Funcion que convierte el array encontrado de los Beacons en una la sentencia jSON que recibe el servidor
     * @return estructura Json para el servidor FIND
     */
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
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
        Log.e(TAG, message);
    }


    private void showFinishingAlertDialog(String title, String message) {
        new AlertDialog.Builder(getActivity()).setTitle(title).setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        getActivity().finish();
                    }
                }).show();
    }
}
