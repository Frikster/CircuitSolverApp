package com.cpen321.circuitsolver.opencv;

import java.util.Comparator;

/**Class to compare two lines.
 * First criteria : the starting x coordinate of the line
 * Second criteria : the starting y coordinate of the line
 * Sorts from smallest to biggest.
 * Created by Simon on 19.10.2016.
 */

public class LinesComparatorXY  implements Comparator<double[]> {
    @Override
    public int compare(double[] a, double[] b){
        if(a[0]>b[0]){
            return 1;
        }
        else if(a[0]==b[0]){
            if(a[1]>=b[1]){
                return 1;
            }
            return -1;
        }
        else{
            return -1;
        }
    }
}
