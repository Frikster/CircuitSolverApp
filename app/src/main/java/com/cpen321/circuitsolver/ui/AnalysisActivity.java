package com.cpen321.circuitsolver.ui;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.widget.RelativeLayout;

import com.cpen321.circuitsolver.R;
import com.cpen321.circuitsolver.util.BaseActivity;


public class AnalysisActivity extends BaseActivity {

    CircuitDisplay circuitDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        this.circuitDisplay = new CircuitDisplay(getApplicationContext());

        RelativeLayout layout = (RelativeLayout) findViewById(R.id.content_analysis);
        layout.addView(this.circuitDisplay);

    }

}
