package com.cpen321.circuitsolver.opencv;

import java.util.Comparator;

/**Class to compare two components's position.
 * Criteria : the x coordinate of the component
 * Sorts from smallest to biggest.
 * Created by Simon on 19.10.2016.
 */

public class ComponentComparatorX implements Comparator<Element> {
    @Override
    public int compare(Element a, Element b){
        if(a.positionX >= b.positionX){
            return 1;
        }
        else{
            return -1;
        }
    }
}
