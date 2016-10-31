package com.cpen321.circuitsolver.util;

import android.graphics.Bitmap;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by neil on 29/10/16.
 */

public class ImageUtils {
    public static File createImageFile(File storageDir) throws IOException {
        // Create an image file name
        String timeStamp = ImageUtils.getTimeStamp();
        String imageFileName = "JPEG_" + timeStamp + "_";
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        return image;
    }

    public static Uri bitmapToUri(Bitmap bitmap, File storageDir) throws IOException {
        File imageFile = ImageUtils.createImageFile(storageDir);
        FileOutputStream outputStream = new FileOutputStream(imageFile);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        outputStream.close();
        return Uri.fromFile(imageFile);
    }

    public static String getTimeStamp() {
        return new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    }

    public static Bitmap downsizeImage(Bitmap originalImage, int newWidth) {
        if (originalImage == null)
            return null;

        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        float widthScale = width / newWidth;
        float newHeight = height / widthScale;
        return Bitmap.createScaledBitmap(originalImage, newWidth, (int) newHeight, false);
    }
}
