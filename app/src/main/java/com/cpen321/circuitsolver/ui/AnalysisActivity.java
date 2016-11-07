package com.cpen321.circuitsolver.ui;

import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.cpen321.circuitsolver.R;
import com.cpen321.circuitsolver.model.SimplePoint;
import com.cpen321.circuitsolver.model.components.CapacitorElm;
import com.cpen321.circuitsolver.model.components.CircuitElm;
import com.cpen321.circuitsolver.model.components.InductorElm;
import com.cpen321.circuitsolver.model.components.ResistorElm;
import com.cpen321.circuitsolver.model.components.VoltageElm;
import com.cpen321.circuitsolver.model.components.WireElm;
import com.cpen321.circuitsolver.util.BaseActivity;
import com.cpen321.circuitsolver.util.CircuitProject;
import com.cpen321.circuitsolver.util.Constants;
import android.graphics.Paint;

import java.io.File;
import java.io.IOException;


public class AnalysisActivity extends BaseActivity {

    CircuitDisplay circuitDisplay;
    CircuitProject circuitProject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle extras = getIntent().getExtras();
        String dataLocation = null;

        if (extras != null) {
            dataLocation = (String) extras.get(Constants.CIRCUIT_PROJECT_FOLDER);
        }
        // Not sure why or if we should use this constant as a key
        String component = extras.getString(Constants.CIRCUIT_PROJECT_FOLDER);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis);
        this.circuitDisplay = new CircuitDisplay(getApplicationContext());
        this.circuitDisplay.displayComponent(component);
        this.circuitDisplay.invalidate();
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.content_analysis);
        layout.addView(this.circuitDisplay);
//        this.loadCircuit(new File(dataLocation));
    }


    public void loadCircuit(File circuitDirectory) {
//        this.circuitProject = new CircuitProject(circuitDirectory);
//        this.circuitProject.print();
//
//        ImageView outputImageView = (ImageView) findViewById(R.id.output_image);
//        outputImageView.setScaleType(ImageView.ScaleType.FIT_XY);
//        try {
//            outputImageView.setImageBitmap(this.circuitProject.getProcessedImage());
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }

    }

    @Override
    public void onBackPressed() {
        Intent backToHome = new Intent(this, EditActivity.class);
        startActivity(backToHome);
        super.onBackPressed();
    }

}