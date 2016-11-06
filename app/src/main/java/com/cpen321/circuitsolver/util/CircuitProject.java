package com.cpen321.circuitsolver.util;


// Class to fully define the circuit, to keep track of where are the images and data is kept
// hereeeee wee gooooooooooooo


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class CircuitProject {
    private File originalImage = null;
    private File downsizedImage = null;
    private File processedImage = null;
    private File circuitDefinition = null;

    private File savedFolder = null;

    public CircuitProject(File directory) {
        this.savedFolder = directory;
        this.loadFromFolder();
    }

    public CircuitProject(String folderName, File parentDirectory) {
        File tmpFolder = new File(parentDirectory, folderName);
        tmpFolder.mkdir();
        this.savedFolder = tmpFolder;
    }

    public Bitmap getThumbnail() {
        try {
            return ImageUtils.downsizeImage(this.getOriginalImage(), 600);
        } catch (IOException ex) {
            return null;
        }
    }

    public Bitmap getOriginalImage() throws IOException {
        return this.getImage(this.originalImage, false);
    }
    public Bitmap getProcessedImage() throws IOException {
        return this.getImage(this.processedImage, true);
    }
    public Bitmap getDownsizedImage() throws IOException {
        return this.getImage(this.downsizedImage, true);
    }

    private Bitmap getImage(File imageLocation, boolean downsize) throws IOException{
        FileInputStream inStream = new FileInputStream(imageLocation);
        Bitmap thumbnail = BitmapFactory.decodeStream(inStream);

        if (thumbnail == null)
            return null;

        if (downsize) {
            inStream.close();
            return ImageUtils.downsizeImage(thumbnail, Constants.PROCESSING_WIDTH);
        } else {
            inStream.close();
            return thumbnail;
        }

    }

    public void saveOriginalImage(Bitmap originalBM) {
        if (this.originalImage == null)
            this.generateOriginalImageFile();
        this.saveImage(originalBM, this.originalImage);
    }

    public void saveDownsizedImage(Bitmap downsizedBM) {
        if (this.downsizedImage == null)
            this.generateDownsizedImageFile();
        this.saveImage(downsizedBM, this.downsizedImage);
    }

    public void saveProcessedImage(Bitmap processedBM) {
        if (this.processedImage == null)
            this.generateProcessedImageFile();
        this.saveImage(processedBM, this.processedImage);
    }

    private void saveImage(Bitmap imgBmp, File filename) {
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(filename);
            imgBmp.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.close();
        } catch (FileNotFoundException ex) {
            System.out.println("FILE NOT FOUND");
            ex.printStackTrace();
            return;
        } catch (IOException ex) {
            System.out.println("IO EXCEPTION");
            ex.printStackTrace();
            return;
        }
    }

    private void loadFromFolder() {
        File[] files = this.savedFolder.listFiles();
        for (File file : files) {
            if (file.getName().contains("original_"))
                this.originalImage = file;
            else if (file.getName().contains("downsized_"))
                this.downsizedImage = file;
            else if (file.getName().contains("processed_"))
                this.processedImage = file;
        }
    }


    public File generateProcessedImageFile() {
        // this function generates a file for the processed image and assigns it to the class variable
        this.processedImage = this.generateImage("processed_");
        return this.processedImage;
    }

    public File generateDownsizedImageFile() {
        // this function generates a file for the processed image and assigns it to the class variable
        this.downsizedImage = this.generateImage("downsized_");
        return this.downsizedImage;
    }

    public File generateOriginalImageFile() {
        // this function generates a file for the processed image and assigns it to the class variable
        this.originalImage = this.generateImage("original_");
        return this.originalImage;
    }

    private File generateImage(String prefix) {
        if (this.savedFolder == null){
            System.out.println("saved folder is null....");
            return null;
        }

        File tmpFile = null;
        try {
            String filename = prefix + ImageUtils.getTimeStamp();
            tmpFile = File.createTempFile(filename, ".jpg", this.savedFolder);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return tmpFile;
    }

    public void convertOriginalToDownsized() {
        try {
            this.saveDownsizedImage(this.getImage(this.originalImage, true));
        } catch (IOException ex) {
            System.out.println(" HUGGGGEEE CRASSSHHHHHH");
            ex.printStackTrace();
            return;
        }
    }

    public String getFolderPath() {
        return this.savedFolder.getAbsolutePath();
    }

    @Override
    public String toString() {
        String tmpString = "";
        if (this.savedFolder != null)
            tmpString += " Folder: " + this.savedFolder.toString();
        if (this.originalImage != null)
            tmpString += " Original Image: " + this.originalImage.toString();
        if (this.processedImage != null)
            tmpString += " Processed Image: " + this.processedImage.toString();

        return tmpString;
    }

    public void print() {
        System.out.println(this.toString());
    }

    public String getFolderID() {
        return this.savedFolder.getName();
    }

    public boolean deleteFileSystem() {
        if (this.downsizedImage != null)
            this.downsizedImage.delete();
        if (this.originalImage != null)
            this.originalImage.delete();
        if (this.processedImage != null)
            this.processedImage.delete();
        if (this.circuitDefinition != null)
            this.circuitDefinition.delete();
        if (this.savedFolder != null)
            this.savedFolder.delete();
        return true;
    }

}
