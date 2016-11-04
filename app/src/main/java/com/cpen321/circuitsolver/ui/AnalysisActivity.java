package com.cpen321.circuitsolver.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;

import com.cpen321.circuitsolver.R;
import com.cpen321.circuitsolver.util.BaseActivity;
import com.cpen321.circuitsolver.util.CircuitProject;
import com.cpen321.circuitsolver.util.Constants;

import java.io.File;
import java.io.IOException;


public class AnalysisActivity extends BaseActivity {

    CircuitDisplay circuitDisplay;
    CircuitProject circuitProject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle extras = getIntent().getExtras();
        String dataLocation = null;

        if (extras != null) {
            dataLocation = (String) extras.get(Constants.CIRCUIT_PROJECT_FOLDER);
        }

        this.loadCircuit(new File(dataLocation));
    }

    public void loadCircuit(File circuitDirectory) {
        this.circuitProject = new CircuitProject(circuitDirectory);
        this.circuitProject.print();

        ImageView outputImageView = (ImageView) findViewById(R.id.output_image);
        outputImageView.setScaleType(ImageView.ScaleType.FIT_XY);
        try {
            outputImageView.setImageBitmap(this.circuitProject.getProcessedImage());
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }

    }

    @Override
    public void onBackPressed() {
        Intent backToHome = new Intent(this, HomeActivity.class);
        startActivity(backToHome);
        super.onBackPressed();
    }

}
