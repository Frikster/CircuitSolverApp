package com.cpen321.circuitsolver.opencv.comparators;

import java.util.Comparator;


/**Class to compare two lines.
 * First criteria : the starting y coordinate of the line
 * Second criteria : the starting x coordinate of the line
 * Sorts from smallest to biggest.
 * Created by Simon on 19.10.2016.
 */

public class LinesComparatorYX implements Comparator<float[]> {
    @Override
    public int compare(float[] a, float[] b){
        if(a[1]>b[1]){
            return 1;
        }
        else if(a[1]==b[1]){
            if(a[0]>=b[0]){
                return 1;
            }
            return -1;
        }
        else{
            return -1;
        }
    }

}
