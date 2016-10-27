package com.cpen321.circuitsolver.opencv;

import java.util.ArrayList;
import java.util.List;

/**Class to be able to return two elements at the time
 * Created by Simon on 21.10.2016.
 */

public class TuplePoints {
    private List<double[]> firstList;
    private List<double[]> secondList;
    public TuplePoints(List<double[]> a, List<double[]> b){
        firstList = new ArrayList<>(a);
        secondList = new ArrayList<>(b);
    }

    public List<double[]> getFirst(){
        return firstList;
    }

    public List<double[]> getSecond(){
        return secondList;
    }
}
