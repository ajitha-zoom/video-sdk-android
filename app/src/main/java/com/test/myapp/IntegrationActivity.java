package com.test.myapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class IntegrationActivity extends AppCompatActivity {

    public static final String ACTION_RETURN_TO_CONF = IntegrationActivity.class.getName() + ".action.RETURN_TO_CONF";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(this, MeetingActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.setAction(ACTION_RETURN_TO_CONF);
        this.startActivity(intent);
        finish();

    }
}
