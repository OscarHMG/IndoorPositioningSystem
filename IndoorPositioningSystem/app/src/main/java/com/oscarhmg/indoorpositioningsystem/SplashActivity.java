package com.oscarhmg.indoorpositioningsystem;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by user on 17/11/2016.
 */
public class SplashActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(this, IdentificationActivity.class);
        startActivity(intent);
        finish();
    }
}
