package com.cpen321.circuitsolver.util;

/**
 * Created by Neil Goossen on 2016-10-15.
 */

public final class Constants {
    public static final String CIRCUIT_PROJECT_FOLDER = "circuit_projec_folder";

    // key for determining FAB action taken (used in onActivityResult)
    public static final int RESULT_LOAD_IMAGE = 2;
    public static final int REQUEST_TAKE_PHOTO = 1;
    public static final int DRAW_NEW_CIRCUIT = 3;

    public static final int COMPRESSION_QUALITY = 20;
    public static final int PROCESSING_WIDTH = 500;

    //TODO change many of these things to enum for type safety
    public static final String RESISTOR = "r";
    public static final String INDUCTOR = "l";
    public static final String CAPACITOR = "c";
    public static final String WIRE = "w";
    public static final String DC_VOLTAGE = "v";

    // unit types
    public static final String RESISTOR_UNITS = "ohms";
    public static final String INDUCTOR_UNTIS = "henrys";
    public static final String CAPACITOR_UNITS = "farads";
    public static final String VOLTAGE_UNITS = "volts";
    public static final String NOTHING_SELECTED = "--";
    public static final String WIRE_UNTIS = "--";

    //The openCv constants, to tweek depending on resolution
    public static int distanceFromComponent = 12*4;
    public static int maxLinesToBeChunk = 3;
    public static int radius = 5*6;
    public static int minPoints = 20*6;
    public static int twoCornersTooNear = 15*4;
    public static int thresholdXY = 10*4;
    public static int tooNearFromComponent = 10*4;
    //The size of the subimage for tensorflow
    public static int imageWH =10*7;


    //    Tensorflow values!
    public static final int NUM_CLASSES = 5;
    public static final int INPUT_SIZE = 299;
    public static final int IMAGE_MEAN = 128;
    public static final float IMAGE_STD = 128;
    public static final String INPUT_NAME = "Mul:0";
    public static final String OUTPUT_NAME = "final_result:0";

    public static final String MODEL_FILE = "file:///android_asset/opt_output_graph.pb";
    public static final String LABEL_FILE =
            "file:///android_asset/output_labels.txt";
    // possible tensorFlow tags
    public static final String TF_RESISTOR = "resistor";
    public static final String TF_INDUCTOR = "inductor";
    public static final String TF_CURRENT_SOURCE = "current source";
    public static final String TF_VOLTAGE_SOURCE = "voltage source";
    public static final String TF_CAPACITOR = "capacitor";


}
