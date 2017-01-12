package com.oscarhmg.indoorpositioningsystem;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import com.oscarhmg.indoorpositioningsystem.room.Room;
import com.oscarhmg.indoorpositioningsystem.room.RoomsCTI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Created by user on 17/11/2016.
 */
public class IdentificationActivity extends Activity implements View.OnClickListener {
    private EditText visitor;
    private Spinner visited;
    private Button submit;
    private String visitedName;
    private int operation;
    ArrayList <String> data;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_identification);
        checkPermissions();
        visitor = (EditText)findViewById(R.id.visitorName);
        visited = (Spinner)findViewById(R.id.prof_spinner);
        submit = (Button)findViewById(R.id.submit);
        addItemsOnSpinner(visited);
        submit.setOnClickListener(this);


    }

    public ArrayList getDataByUserAction(){
        ArrayList <String> data = null;
        Intent intent=getIntent();
        Bundle extras =intent.getExtras();
        operation = (int) extras.get("action");
        Log.i("Operation :", "" + operation);
        switch (operation){
            case 1:
                data = getLivePersons();
                break;
            case 2:
                data = getRooms();
                break;
            default:
                break;
        }
        return data;
    }

    private ArrayList<String> getLivePersons() {
        HttpHandler request = new HttpHandler();
        String response = request.requestOnlinePersons();

        return null;
    }



    public ArrayList getRooms(){
        ArrayList<String> rooms = new ArrayList<>();
        for(Room r: RoomsCTI.rooms){
            String data = r.getNameRoom();
            rooms.add(data);
        }
        return rooms;
    }


    public void addItemsOnSpinner(Spinner visited) {
        List<String> list = getDataByUserAction();
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        visited.setAdapter(dataAdapter);
    }


    public void checkPermissions(){
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED) {
        } else {
            Toast.makeText(this, "Error en Permisos", Toast.LENGTH_LONG).show();
            ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    100);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 100:
                if (grantResults.length > 0
                        || grantResults[0] == PackageManager.PERMISSION_GRANTED
                        || grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    /* User checks permission. */
                    Log.d("Permisos", "OK Permisos esteblecidos");
                } else {
                    Log.w("No Permisos", "onRequestPermissionsResult() called with: " + "requestCode = [" + requestCode + "], permissions = [" + Arrays.toString(permissions) + "], grantResults = [" + Arrays.toString(grantResults) + "]");
                    Toast.makeText(this, "Permission is denied.", Toast.LENGTH_SHORT).show();
                    finish();
                }
                return;
        }
    }

    @Override
    public void onClick(View view) {
        visitedName = visited.getSelectedItem().toString();
        String tmp = (String)visitor.getText().toString();
        if(tmp!=null && visitedName!=null){
            Intent intent = new Intent(IdentificationActivity.this, MapActivity.class);
            intent.putExtra("option", visitedName);
            intent.putExtra("operation",operation);
            intent.putExtra("visitorName", tmp);
            Toast.makeText(IdentificationActivity.this,"Opcion escogida:"+visitedName,Toast.LENGTH_LONG).show();
            startActivity(intent);
            this.finish();
        }else{
            if(visitor.getText() ==null){
                Toast.makeText(IdentificationActivity.this,"Ingrese su nombre para identificarse",Toast.LENGTH_LONG);
            }else if(visitedName == null){
                Toast.makeText(IdentificationActivity.this,"No existen personas en el edificio",Toast.LENGTH_LONG);
            }
        }
    }
}
