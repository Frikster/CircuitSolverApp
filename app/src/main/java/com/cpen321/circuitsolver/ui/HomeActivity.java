package com.cpen321.circuitsolver.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.cpen321.circuitsolver.R;
import com.cpen321.circuitsolver.util.BaseActivity;
import com.cpen321.circuitsolver.util.CircuitProject;
import com.cpen321.circuitsolver.util.Constants;
import com.cpen321.circuitsolver.util.ImageUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class HomeActivity extends BaseActivity {
    private static final String APP_NAME = "com.cpen321.circuitsolver";

    String mCurrentPhotoPath;

    static final int REQUEST_TAKE_PHOTO = 1;
    private LinearLayout savedCircuitsScroll;

    private ArrayList<CircuitProject> circuitProjects = new ArrayList<>();
    private CircuitProject candidateProject;

    public static String selectedTag = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        FloatingActionButton process_fab = (FloatingActionButton) findViewById(R.id.processing_fab);

        this.savedCircuitsScroll = (LinearLayout) findViewById(R.id.saved_circuits_scroll);
//        this.deleteSavedCircuits();
        this.updateSavedCircuits();


        this.checkNecessaryPermissions();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HomeActivity.this.checkNecessaryPermissions();
                HomeActivity.this.dispatchTakePictureIntent();

                Snackbar.make(view, "Camera button pressed! ", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        process_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (HomeActivity.selectedTag == null)
                    return;
                Intent displayIntent = new Intent(HomeActivity.this, AnalysisActivity.class);
                File circuitFolder = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), HomeActivity.selectedTag);
                displayIntent.putExtra(Constants.CIRCUIT_PROJECT_FOLDER, circuitFolder.getAbsolutePath());
                startActivity(displayIntent);
            }
        });
    }

    // TAKEN FROM OFFICIAL ANDROID DEVELOPERS PAGE (NOT MY OWN CODE)

    private void dispatchTakePictureIntent() {
        this.candidateProject = new CircuitProject(ImageUtils.getTimeStamp(),
                getExternalFilesDir(Environment.DIRECTORY_PICTURES));

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = this.candidateProject.generateOriginalImageFile();

            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        APP_NAME,
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
            else {
                System.out.println("photo file is null");
            }
        }
    }

    // END OF CODE TAKEN FROM OFFICIAL ANDROID DEVELOPERS PAGE


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Intent analysisIntent = new Intent(getApplicationContext(), ProcessingActivity.class);
        analysisIntent.putExtra(Constants.CIRCUIT_PROJECT_FOLDER, this.candidateProject.getFolderPath());
        startActivity(analysisIntent);
    }


    private void deleteSavedCircuits() {
        File imageStorageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        for (File dir : imageStorageDir.listFiles())
            this.deleteFolderOrFile(imageStorageDir);
    }

    private void deleteFolderOrFile(File file) {
        if (file.isDirectory()) {
            for (File child : file.listFiles())
                this.deleteFolderOrFile(child);
            file.delete();
        } else {
            file.delete();
        }
    }

    protected void updateSavedCircuits(){
        File imageStorageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File[] circuitProjects = imageStorageDir.listFiles();
        this.savedCircuitsScroll.removeAllViews();
        this.circuitProjects.clear();

        for (File project : circuitProjects) {
            if (project.isDirectory()){
                if (project.listFiles().length == 0)
                    this.deleteFolderOrFile(project);
                else {
                    this.circuitProjects.add(new CircuitProject(project));
                }
            }
        }

        for (CircuitProject circuitProject : this.circuitProjects) {
            ImageView newImage = new ImageView(getApplicationContext());
            newImage.setTag(circuitProject.getFolderID());
            newImage.setPadding(10, 10, 10, 10);
            newImage.setImageBitmap(circuitProject.getThumbnail());
            newImage.setOnClickListener(new ThumbnailListener());
            this.savedCircuitsScroll.addView(newImage);
        }
    }


}
