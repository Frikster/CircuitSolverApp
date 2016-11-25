package com.cpen321.circuitsolver.util;

/**
 * Created by Neil Goossen on 2016-10-15.
 */

public final class Constants {
    public static final String IMAGE_LOCATION_KEY = "image_location_key";
    public static final String CIRCUIT_PROJECT_FOLDER = "circuit_projec_folder";
    public static final String OUTPUT_BITMAP = "hough_lines_output_bitmap";
    public static final String PROCESSING_DATA_LOCATION_KEY = "processing_data_location_key";
    public static final int CANVAS_MARGIN = 10; //leaves buffer space so that circuit components aren't right on the edge of the screen

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

    public static final int lowerCannyThreshold = 40;
    public static final int upperCannyThreshold = 200;
    //The openCv constants, to tweek depending on resolution
    // cluster = collection of meaningful points identified
    public static int distanceFromComponent = 12 * 4; //... like tooNearFromComponent... but different?
    public static int maxLinesToBeChunk = 3; // the maximum length of a line to be considered as a chunk (used to remove clusters that only have wire): used by dbtoArray
    public static int radius = 5 * 6; // maximum radius of a cluster: used by dbscan
    public static int minPoints = 20 * 6; // minimum number of points needed to identify a cluster: used by dbscan
    public static int twoCornersTooNear = 15 * 4; // The min distance between two corners: used by singleCorners
    public static int thresholdXY = 10 * 4; // the dmmax distance to be considered as same distance (?) <- threshold tolerance wires can be said to be on the same x or y axis: used by completeMissingEndings
    public static int tooNearFromComponent = 10 * 4; // used to define how close a corner can be to a component: in goodCorners
    public static int cornerSearchRadius = 10; // The max distance between two line endings to be considered as a corner: used by findCorners
}