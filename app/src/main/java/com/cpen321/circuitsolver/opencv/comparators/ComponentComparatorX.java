package com.cpen321.circuitsolver.opencv.comparators;

import com.cpen321.circuitsolver.opencv.opencvModel.Element;

import java.util.Comparator;

/**Class to compare two components's position.
 * Criteria : the x coordinate of the component
 * Sorts from smallest to biggest.
 * Created by Simon on 19.10.2016.
 */

public class ComponentComparatorX implements Comparator<Element> {
    @Override
    public int compare(Element a, Element b){
        if(a.getX()>= b.getY()){
            return 1;
        }
        else{
            return -1;
        }
    }
}
