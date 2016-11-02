package com.cpen321.circuitsolver.ngspice;

import android.content.Context;
import android.util.Log;

import com.cpen321.circuitsolver.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
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
    private static final String OUTPUT_FILE_NAME = "ngspice_output";
    private final String execFilePath;
    private final String inputFilePath;
    private final String outputFilePath;
    private static NgSpice ngSpiceSingleton;

    private NgSpice(Context context) {
        //TODO: check phone's cpu and create proper executable depending on CPU version
        execFilePath = createExecutable(context, R.raw.ngspice_arm);
        inputFilePath = getPath(context, INPUT_FILE_NAME);
        outputFilePath = getPath(context, OUTPUT_FILE_NAME);
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
        return exec("-b " + inputFilePath);
    }

    /**
     * Executes ngspice
     * @param args the command line arguments to ngspice
     * @return the program's output to std out
     */
    private String exec(String args) {
        String returnString = null;
        try {
            String command = execFilePath + " " + "-b " + inputFilePath; //args;
            Process process = Runtime.getRuntime().exec(command);
            Log.d(TAG, "exec command: " + command);
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
        String filePath = getPath(context, EXEC_FILE_NAME);
        File file = new File(filePath);
        if(!file.exists()) { //if file already exists, it should be the working executable, unless there is another file with same name
            Log.d(TAG, "createExecutable: file already exists");
            try {
                copyRawToFile(context, resourceId, EXEC_FILE_NAME);
                changeToExecutable(context, EXEC_FILE_NAME);
            } catch (Exception e) {
                Log.e(TAG, "Error was thrown while creating executable");
            }
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


    /**
     * Get path of file
     * @param context the current context
     * @param fileName the name of the file
     * @return the path of the file
     */
    private String getPath(Context context, String fileName) {
        File folder = context.getFilesDir();
        String path = null;
        try {
            path = context.getFilesDir().getCanonicalPath() + '/' + fileName;
        } catch (IOException e) {
            Log.e(TAG, "getPath: Error");
        }
        return path;
    }
}
