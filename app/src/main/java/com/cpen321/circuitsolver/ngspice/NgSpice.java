package com.cpen321.circuitsolver.ngspice;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.cpen321.circuitsolver.R;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by lotus on 30/10/16.
 */

public final class NgSpice extends AppCompatActivity {
    private static final String TAG = "NgSpice";
    private static final String FILE_NAME = "ngspice";
    private final String filePath;
    private static NgSpice ngSpiceSingleton;

    private NgSpice(Context context) {
        //TODO: check phone's cpu and create proper executable depending on CPU version
        filePath = createExecutable(context, R.raw.ngspice_arm);
    }

    /**
     * A singleton class.  Used to interface with ngspice
     * @param context the current contex
     * @return instance of NgSpice class
     */
    public static NgSpice getInstance(Context context) {
        if(ngSpiceSingleton == null) {
            ngSpiceSingleton = new NgSpice(context);
        }
        return ngSpiceSingleton;
    }

    /**
     * Creates executable version of the raw executable
     * @param context the current contex
     * @param resourceId of the raw native executable
     * @return the executable's filepath
     */
    private String createExecutable(Context context, int resourceId) {
        String filePath = null;
        try {
            copyRawToFile(context, resourceId, FILE_NAME);
            filePath = changeToExecutable(context, FILE_NAME);
        } catch (Exception e) {
            Log.e(TAG, "Error was thrown while creating executable");
        }
        return filePath;
    }

    /**
     * Writes contents of raw executable to a private file associated with this Context's application package.
     * @param context the current contex
     * @param resourceId the id of the raw file
     * @param fileName name of the private file to be created
     * @throws IOException
     */
    private void copyRawToFile(Context context, int resourceId, String fileName) throws IOException {
        InputStream input = context.getResources().openRawResource(resourceId);
        OutputStream output = context.openFileOutput(fileName, Context.MODE_PRIVATE);

        byte[] buffer = new byte[1024 * 4];
        int a;
        while ((a = input.read(buffer)) > 0) {
            output.write(buffer, 0, a);
        }

        input.close();
        output.close();
    }

    /**
     * Makes file with fileName executable
     * Requires: file with fileName exists as a private file associated with this Context's application package.
     * @param context the current contex
     * @param fileName the name of file to be made executable
     * @return path to the executable
     * @throws IOException
     */
    private String changeToExecutable(Context context, String fileName) throws IOException, InterruptedException {
        //First get the absolute path to the file
        File folder = context.getFilesDir();
        String fullpath;

        String filefolder = folder.getCanonicalPath();
        if (!filefolder.endsWith("/"))
            filefolder += "/";

        fullpath = filefolder + fileName;

        Runtime.getRuntime().exec("chmod 777 " + fullpath).waitFor();
        Log.d(TAG, "Make executable's returned path: " + fullpath);

        return fullpath;
    }

}
