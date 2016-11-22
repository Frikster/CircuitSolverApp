package com.cpen321.circuitsolver.opencv.opencvModel;

import java.util.ArrayList;
import java.util.List;

/**Class to be able to return two List of points (double[]) at the time
 * Created by Simon on 21.10.2016.
 */

public class TuplePoints {
    private List<float[]> firstList;
    private List<float[]> secondList;
    public TuplePoints(List<float[]> a, List<float[]> b){
        firstList = new ArrayList<>(a);
        secondList = new ArrayList<>(b);
    }

    public List<float[]> getFirst(){
        return firstList;
    }

    public List<float[]> getSecond(){
        return secondList;
    }
}
