package com.oscarhmg.indoorpositioningsystem.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.oscarhmg.indoorpositioningsystem.R;

/**
 * Created by user on 10/01/2017.
 */
public class ActionActivity extends Activity {
    private RadioGroup radioGroup;
    private Button submit;
    private RadioButton radioButtonOtion;
    private static String optionFindPeople;
    private static String optionFindRoom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_action_scanning);
        radioGroup = (RadioGroup)findViewById(R.id.radioGroup);
        radioGroup.clearCheck();
        optionFindPeople = getResources().getString(R.string.radioButtonVisitedPerson);
        optionFindRoom = getResources().getString(R.string.radioButtonVisitedRoom);
        submit = (Button)findViewById(R.id.submitAction);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Launch the activity.
                if(radioGroup.getCheckedRadioButtonId()!=-1){
                    int operation = 0;
                    int selectedId = radioGroup.getCheckedRadioButtonId();
                    radioButtonOtion=(RadioButton)findViewById(selectedId);
                    String optionSelected = radioButtonOtion.getText().toString();
                    Log.i("RadioButton:",""+radioButtonOtion.getText().toString());
                    if (optionSelected.equals(optionFindPeople)){
                        operation = 1;
                        Toast.makeText(ActionActivity.this,"Accion escogida:"+radioButtonOtion.getText(), Toast.LENGTH_LONG).show();
                    }else if(optionSelected.equals(optionFindRoom)){
                        Log.i("DATA PASS:","HABITACION");
                        operation = 2;
                    }
                    Intent intent = new Intent(ActionActivity.this, IdentificationActivity.class);
                    intent.putExtra("action", operation);
                    startActivity(intent);
                }else{
                    Toast.makeText(ActionActivity.this,"Seleccione una opcion", Toast.LENGTH_LONG).show();
                }

            }
        });
    }

}
