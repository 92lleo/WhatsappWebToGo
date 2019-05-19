package io.kuenzler.whatsappwebtogo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity  {

    private static final String DEBUG_TAG = "WAWEBTOGO";

    private final Activity activity = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent myIntent = new Intent(MainActivity.this, WebviewActivity.class);
        //Intent myIntent = new Intent(MainActivity.this, CrosswalkActivity.class);
        MainActivity.this.startActivity(myIntent);
        finish();
    }
}