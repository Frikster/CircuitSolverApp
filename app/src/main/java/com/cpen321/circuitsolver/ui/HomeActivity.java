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
import com.cpen321.circuitsolver.util.Constants;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class HomeActivity extends BaseActivity {
    private static final String APP_NAME = "com.cpen321.circuitsolver.ui.CameraActivity";

    String mCurrentPhotoPath;

    static final int REQUEST_TAKE_PHOTO = 1;

    private LinearLayout savedCircuitsScroll;
    private LinearLayout exampleCircuitsScroll;

    private Uri tmpImageLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        this.savedCircuitsScroll = (LinearLayout) findViewById(R.id.saved_circuits_scroll);
        this.exampleCircuitsScroll = (LinearLayout) findViewById(R.id.example_circuits_scroll);
        this.updateSavedCircuits();

//        this.deleteSavedCircuits();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HomeActivity.this.checkNecessaryPermissions();
                HomeActivity.this.dispatchTakePictureIntent();

                Snackbar.make(view, "Camera button pressed! ", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    // TAKEN FROM OFFICIAL ANDROID DEVELOPERS PAGE (NOT MY OWN CODE)

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        APP_NAME,
                        photoFile);
                this.tmpImageLocation = photoURI;
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }
    // END OF CODE TAKEN FROM OFFICIAL ANDROID DEVELOPERS PAGE


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        this.updateSavedCircuits();
        super.onActivityResult(requestCode, resultCode, data);

        System.out.println(this.tmpImageLocation.toString());

        Snackbar.make(this.findViewById(R.id.content_home), "Returned from camera", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();

        Intent analysisIntent = new Intent(getApplicationContext(), ProcessingActivity.class);
        analysisIntent.putExtra(Constants.PROCESSING_DATA_LOCATION_KEY, this.tmpImageLocation);
        startActivity(analysisIntent);
    }


    private void deleteSavedCircuits() {
        File imageStorageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File[] images = imageStorageDir.listFiles();
        for (File image : images) {
            image.delete();
        }
    }

    protected void updateSavedCircuits(){
        System.out.println("CHECKING saved images");
        try {
            File imageStorageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File[] images = imageStorageDir.listFiles();
            this.savedCircuitsScroll.removeAllViews();

            ArrayList<Bitmap> thumbnails = this.getThumbnails(images);

            for (Bitmap image : thumbnails) {
                ImageView newImage = new ImageView(getApplicationContext());
                newImage.setPadding(10, 10, 10, 10);

                newImage.setImageBitmap(image);
                this.savedCircuitsScroll.addView(newImage);
            }
        } catch (NullPointerException ex) {
            System.out.println("No files found");
        }
    }

    public ArrayList<Bitmap> getThumbnails(File[] images){
        ArrayList<Bitmap> thumbnails = new ArrayList<>();

        for (File image : images){
            try{
                FileInputStream inStream = new FileInputStream(image);
                Bitmap thumbnail = BitmapFactory.decodeStream(inStream);
                int width = thumbnail.getWidth();
                int height = thumbnail.getHeight();
                final int scaleToHeight = 600;
                float heightScale = height / scaleToHeight;
                float newWidth = width / heightScale;

                thumbnail = Bitmap.createScaledBitmap(thumbnail, (int) newWidth,
                        scaleToHeight, false);

                thumbnails.add(thumbnail);

            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }

        Collections.reverse(thumbnails);

        return thumbnails;
    }

}
