package com.cpen321.circuitsolver.model.components;


import com.cpen321.circuitsolver.model.SimplePoint;
import com.cpen321.circuitsolver.util.Constants;

/**
 * Created by Jennifer on 10/12/2016.
 */
public class ResistorElm extends CircuitElm {

    private double resistance;

    public ResistorElm(SimplePoint p1, SimplePoint p2, double resistance){
        super(p1, p2);
        this.resistance = resistance;
    }

    public double getVoltageDiff() {
        return 0;
    }

    public double calculateCurrent() {
        return 0;
    }

    public void setValue(double value) {
        this.resistance = value;
    }

    @Override
    public String getType() {
        return Constants.RESISTOR;
    }

}
