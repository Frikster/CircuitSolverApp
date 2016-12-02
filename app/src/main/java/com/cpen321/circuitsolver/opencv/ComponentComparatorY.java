package com.cpen321.circuitsolver.opencv;

import java.util.Comparator;

/**Class to compare two components's position.
 * Criteria : the y coordinate of the component
 * Sorts from smallest to biggest.
 * Created by Simon on 19.10.2016.
 */

public class ComponentComparatorY implements Comparator<Element> {

        @Override
        public int compare(Element a, Element b){
            if(a.getY() >= b.getY()){
                return 1;
            }
            else{
                return -1;
            }
        }

}
