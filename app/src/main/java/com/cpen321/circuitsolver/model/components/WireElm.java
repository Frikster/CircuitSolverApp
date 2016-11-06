package com.cpen321.circuitsolver.model.components;


import com.cpen321.circuitsolver.model.SimplePoint;
import com.cpen321.circuitsolver.util.Constants;

/**
 * Created by Jennifer on 10/12/2016.
 */
public class WireElm extends CircuitElm {

    public WireElm(SimplePoint p1, SimplePoint p2){
        super(p1, p2);
    }

    public double getVoltageDiff() {
        //TODO: implement this method
        return 0;
    }

    public double calculateCurrent() {
        //TODO: implement this method
        return 0;
    }

    public void setValue(double value) {
        //TODO: throw an IllegalEditWireValueException
    }

    @Override
    public String getType() {
        return Constants.WIRE;
    }

    @Override
    public String toString(){
        return "WireElm: " + super.toString();
    }

}
