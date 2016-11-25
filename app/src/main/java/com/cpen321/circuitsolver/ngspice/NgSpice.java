package com.cpen321.circuitsolver.ngspice;

import android.content.Context;
import android.util.Log;

import com.cpen321.circuitsolver.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * Created by lotus on 30/10/16.
 */

public final class NgSpice {
    private static final String TAG = "NgSpice";
    private static final String EXEC_FILE_NAME = "ngspice";
    private static final String INPUT_FILE_NAME = "ngspice_input";
    private static NgSpice ngSpiceSingleton;
    private String folderPath;

    private NgSpice(Context context) {
        //TODO: check phone's cpu and create proper executable depending on CPU version
        try {
            folderPath = context.getFilesDir().getCanonicalPath();
        } catch (IOException e) {
            Log.e(TAG, "NgSpice: error finding files dir");
        }
        createExecutable(context, R.raw.arm_ngspice);
    }


    /**
     * A singleton class.  Used to interface with ngspice
     * @param context the current context
     * @return instance of NgSpice class
     */
    public static NgSpice getInstance(Context context) {
        if(ngSpiceSingleton == null) {
            ngSpiceSingleton = new NgSpice(context);
        }
        return ngSpiceSingleton;
    }

    /**
     * Runs ngspice in batch mode with given input string.  Results from simulation will be returned as a string.
     * @param input the input the ngspice (will probably contain a netlist and a control section)
     * @return the simulation results as a string
     */
    public String callNgSpice(String input) {
        writeToInputFile(input);
        String returnString = null;
        try {
            String command = "./" + EXEC_FILE_NAME + " -z " + folderPath + " -b " + INPUT_FILE_NAME;
            Process process = Runtime.getRuntime().exec(command, null, new File(folderPath));
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            char[] buffer = new char[4096];
            int charsRead;
            while ((charsRead = reader.read(buffer)) > 0) {
                output.append(buffer, 0, charsRead);
            }
            reader.close();
            process.waitFor();
            returnString = output.toString();
            Log.d(TAG, "exec return string: " + returnString);
        } catch (Exception e) {
            Log.e(TAG, "Error executing ngspice");
        }

        return returnString;
    }


    /**
     * Writes input string to input file to be read my ngspice
     * @param input
     */
    private void writeToInputFile(String input) {
        String inputFilePath = folderPath + "/" + INPUT_FILE_NAME;
        Log.d(TAG, "Input: \n" + input);
        try {
            PrintWriter writer = new PrintWriter(inputFilePath);
            writer.println(input);
            writer.close();
        } catch (IOException e){
            Log.e(TAG, "Error writing to input file: " + inputFilePath);
        }
    }

    /**
     * Creates executable version of the raw executable
     * @param context the current context
     * @param resourceId of the raw native executable
     * @return the executable's filepath
     */
    private String createExecutable(Context context, int resourceId) {
        String filePath = folderPath + EXEC_FILE_NAME;
        File file = new File(filePath);

        if(!file.exists()) { //if file already exists, it should be the working executable, unless there is another file with same name
            Log.d(TAG, "createExecutable: ngspice file does not already exists");
            try {
                copyRawToFile(context, resourceId, EXEC_FILE_NAME);
                changeToExecutable(context, EXEC_FILE_NAME);
            } catch (Exception e) {
                Log.e(TAG, "Error was thrown while creating executable");
            }
        } else {
            Log.d(TAG, "createExecutable: ngspice file already exists");
        }
        return filePath;
    }

    /**
     * Writes contents of raw executable to a private file associated with this Context's application package.
     * @param context the current context
     * @param resourceId the id of the raw file
     * @param fileName name of the private file to be created
     * @throws IOException
     */
    private void copyRawToFile(Context context, int resourceId, String fileName) throws IOException {
        InputStream input = context.getResources().openRawResource(resourceId);
        OutputStream output = context.openFileOutput(fileName, Context.MODE_PRIVATE);

        byte[] buffer = new byte[4096];
        int charsRead;
        while ((charsRead = input.read(buffer)) > 0) {
            output.write(buffer, 0, charsRead);
        }

        input.close();
        output.close();
    }

    /**
     * Makes file with fileName executable
     * Requires: file with fileName exists as a private file associated with this Context's application package.
     * @param context the current context
     * @param fileName the name of file to be made executable
     * @throws IOException
     */
    private void changeToExecutable(Context context, String fileName) throws IOException, InterruptedException {
        //First get the absolute path to the file
        File folder = context.getFilesDir();
        String fullpath;

        String filefolder = folder.getCanonicalPath();
        if (!filefolder.endsWith("/"))
            filefolder += "/";

        fullpath = filefolder + fileName;
        Runtime.getRuntime().exec("chmod 777 " + fullpath).waitFor();
    }
}
