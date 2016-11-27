package com.cpen321.circuitsolver.util;

/**
 * Created by Neil Goossen on 2016-10-15.
 */

public final class Constants {
    public static final String IMAGE_LOCATION_KEY = "image_location_key";
    public static final String CIRCUIT_PROJECT_FOLDER = "circuit_projec_folder";
    public static final String OUTPUT_BITMAP = "hough_lines_output_bitmap";
    public static final String PROCESSING_DATA_LOCATION_KEY = "processing_data_location_key";

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


}
