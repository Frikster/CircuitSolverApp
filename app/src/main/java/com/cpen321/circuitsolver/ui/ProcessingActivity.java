package com.cpen321.circuitsolver.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ProgressBar;

import com.cpen321.circuitsolver.R;
import com.cpen321.circuitsolver.opencv.MainOpencv;
import com.cpen321.circuitsolver.util.CircuitProject;
import com.cpen321.circuitsolver.util.Constants;
import com.cpen321.circuitsolver.util.ImageUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class ProcessingActivity extends AppCompatActivity {

    private String dataLocation;
    private ProgressBar progressBar;

    private Bitmap outputImage;

    private CircuitProject circuitProject;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_processing);

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            this.dataLocation = (String) extras.get(Constants.CIRCUIT_PROJECT_FOLDER);
        }

        this.circuitProject = new CircuitProject(new File(this.dataLocation));
        this.displayOutputImage();

        Thread tmp = new Thread() {
            @Override
            public void run() {
                Bitmap bMap = null;
                try{
                    ProcessingActivity.this.circuitProject.print();
                    ProcessingActivity.this.circuitProject.convertOriginalToDownsized();
                    bMap = ProcessingActivity.this.circuitProject.getDownsizedImage();
                } catch(Exception ex) {
                    ex.printStackTrace();
                    return;
                }

                System.out.println("right before processing");
                MainOpencv main = new MainOpencv();
                ProcessingActivity.this.circuitProject.saveProcessedImage(main.houghLines(bMap));
                ProcessingActivity.this.circuitProject.print();
                System.out.println("out of processing");
                ProcessingActivity.this.displayOutputImage();

            }
        };

        tmp.run();

    }

    private void displayOutputImage() {

        Intent displayIntent = new Intent(getApplicationContext(), EditActivity.class);
        displayIntent.putExtra(Constants.CIRCUIT_PROJECT_FOLDER, this.circuitProject.getFolderPath());

        startActivity(displayIntent);

    }

}
