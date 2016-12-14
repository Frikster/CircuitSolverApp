package com.cpen321.circuitsolver.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.cpen321.circuitsolver.R;
import com.cpen321.circuitsolver.opencv.ImageClassifier;
import com.cpen321.circuitsolver.opencv.MainOpencv;
import com.cpen321.circuitsolver.ui.draw.DrawActivity;
import com.cpen321.circuitsolver.util.CircuitProject;
import com.cpen321.circuitsolver.util.Constants;

import java.io.File;

public class ProcessingActivity extends AppCompatActivity {

    private String dataLocation;
    private CircuitProject circuitProject;
    private boolean isNoOpenCvTest = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_processing);

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            this.dataLocation = (String) extras.get(Constants.CIRCUIT_PROJECT_FOLDER);
        }

        this.circuitProject = new CircuitProject(new File(this.dataLocation));

        Thread tmp = new Thread() {
            @Override
            public void run() {
                Bitmap bMap;
                try{
                    ProcessingActivity.this.circuitProject.print();
                    ProcessingActivity.this.circuitProject.convertOriginalToDownsized();
                    bMap = ProcessingActivity.this.circuitProject.getDownsizedImage();
                } catch(Exception ex) {
                    ex.printStackTrace();
                    return;
                }

                Log.d("UI","right before processing");
                MainOpencv main = new MainOpencv();
                main.setComponentClassifier(new ImageClassifier(getAssets()));
                main.houghLines(bMap);
                ProcessingActivity.this.circuitProject.saveCircuitDefinitionFile(
                        main.getCircuitText());
                ProcessingActivity.this.circuitProject.print();
                Log.d("UI","out of processing");
                ProcessingActivity.this.displayOutputImage();

            }
        };

        tmp.start();

    }

    private void displayOutputImage() {
        Intent displayIntent = new Intent(getApplicationContext(), DrawActivity.class);
        Log.e("Processing",this.circuitProject.getFolderPath());
        displayIntent.putExtra(Constants.CIRCUIT_PROJECT_FOLDER, this.circuitProject.getFolderPath());
        startActivity(displayIntent);
    }

    // USED FOR TESTING PURPOSES. UNKNOWN IF IT IS WISE TO HAVE THIS SETTER. USE WITH CAUTION
    public void setCircuitProject(CircuitProject candidateProject){
        this.circuitProject = candidateProject;
    }
}