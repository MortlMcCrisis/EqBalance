package com.example.myfirstapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;


public class ShowResult extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_result);

        Intent intent = getIntent();
        int politics = intent.getIntExtra(MainActivity.EXTRA_RESULT_STATE_POLITICS, 0);
        int control = intent.getIntExtra(MainActivity.EXTRA_RESULT_STATE_CONTROL, 0);
        int criticism = intent.getIntExtra(MainActivity.EXTRA_RESULT_STATE_CRITICISM, 0);
        int commitment = intent.getIntExtra(MainActivity.EXTRA_RESULT_STATE_COMMITMENT, 0);

        findViewById(R.id.politicsState).setBackgroundColor(politics);
        findViewById(R.id.contolState).setBackgroundColor(control);
        findViewById(R.id.criticismState).setBackgroundColor(criticism);
        findViewById(R.id.commitmentState).setBackgroundColor(commitment);
    }
}
