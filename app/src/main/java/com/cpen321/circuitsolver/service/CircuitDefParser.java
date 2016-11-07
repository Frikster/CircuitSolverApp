package com.cpen321.circuitsolver.service;

import com.cpen321.circuitsolver.model.CircuitElmFactory;
import com.cpen321.circuitsolver.model.SimplePoint;
import com.cpen321.circuitsolver.model.components.CircuitElm;
import com.cpen321.circuitsolver.util.Constants;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by jen on 2016-11-05.
 */

public class CircuitDefParser {

    /**
     * Reads String of circuit elements into list of circuit elements
     * @param circTxt
     * @return
     */
    public List<CircuitElm> parseElements(String circTxt){

        //TODO: Lots of dangerous out of bound index cases here. Need to build safer code.
        List<CircuitElm> elements = new ArrayList<>();
        //Each line represents a new circuit element
        String strElms[] = circTxt.split("\n");

        for(String str: strElms){
            //System.out.print("Parsing line: ");
            //System.out.println(str);
            //Ignore lines starting with #
            if(!str.startsWith("#")){
                CircuitElmFactory circuitElmFactory= new CircuitElmFactory();
                CircuitElm elm;

                String col[] = str.split(" ");
                //Column 1 is element type
                String type = col[0];

                //Column 2 is x1
                int x1 = Integer.parseInt(col[1]);
                //Column 3 is y1
                int y1 = Integer.parseInt(col[2]);

                //Column 4 is x2
                int x2 = Integer.parseInt(col[3]);
                //Column 5 is y2
                int y2 = Integer.parseInt(col[4]);

                SimplePoint p1 = new SimplePoint(x1, y1);
                SimplePoint p2 = new SimplePoint(x2, y2);

                if(col.length < 6)
                    elm = circuitElmFactory.makeElm(p1, p2);
                else{
                    double v = Double.parseDouble(col[5]);
                    elm = circuitElmFactory.makeElm(type, p1, p2, v);
                }

                elements.add(elm);
            }
            else{
                System.out.println("Ignored line starting with #");
            }
        }

        return elements;
    }

    /**
     * Writes List of circuit elements into list of circuit elements
     * @param elements
     */
    public String elementsToTxt(List<CircuitElm> elements, int originalHeight, int originalWidth){
        StringBuilder sb = new StringBuilder();
        sb.append("$ " + originalHeight + " " + originalWidth + "\n");
        for(CircuitElm elm : elements){
            //Column 1 is element type
            sb.append(elm.getType() + " ");

            SimplePoint p1 = elm.getPoint(0);
            //Column 2 is x1
            sb.append(p1.getX() + " ");
            //Column 3 is y1
            sb.append(p1.getY() + " ");

            SimplePoint p2 = elm.getPoint(1);
            //Column 4 is x2
            sb.append(p2.getX() + " ");
            //Column 5 is y2
            sb.append(p2.getX() + " ");

            if(!elm.isWire()){
                sb.append(elm.getValue());
            }

            sb.append("\n");
        }
        return sb.toString();
    }
}
