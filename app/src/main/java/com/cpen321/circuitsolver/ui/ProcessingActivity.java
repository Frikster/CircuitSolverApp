package com.cpen321.circuitsolver.ui;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;

import com.cpen321.circuitsolver.R;
import com.cpen321.circuitsolver.util.Constants;

public class ProcessingActivity extends AppCompatActivity implements View.OnTouchListener {

    private Uri dataLocation;
    private ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_processing);

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            this.dataLocation = (Uri) extras.get(Constants.PROCESSING_DATA_LOCATION_KEY);
        }

        this.progressBar = (ProgressBar) findViewById(R.id.image_process_status);

        this.progressBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent showAnalysis = new Intent(getApplicationContext(), AnalysisActivity.class);
                showAnalysis.putExtra(Constants.PROCESSING_DATA_LOCATION_KEY,
                        ProcessingActivity.this.dataLocation);

                startActivity(showAnalysis);
            }
        });

    }

    public boolean onTouch(View view, MotionEvent event) {
        System.out.println(event.toString());


        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            System.out.println("tapped the screen");

            Intent showAnalysis = new Intent(getApplicationContext(), AnalysisActivity.class);
            showAnalysis.putExtra(Constants.PROCESSING_DATA_LOCATION_KEY, this.dataLocation);

            startActivity(showAnalysis);
        }

        return false;
    }

}
