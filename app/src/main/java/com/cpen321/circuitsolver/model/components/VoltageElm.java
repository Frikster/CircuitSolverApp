package com.cpen321.circuitsolver.model.components;


import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;

import com.cpen321.circuitsolver.model.SimplePoint;
import com.cpen321.circuitsolver.model.SpiceElm;
import com.cpen321.circuitsolver.model.SpiceLabel;
import com.cpen321.circuitsolver.util.Constants;

/**
 * Created by Jennifer on 10/12/2016.
 */
public class VoltageElm extends CircuitElm implements SpiceElm {
    private static int numVoltageElms = 1;

    private String name;
    private double voltage;

    public VoltageElm(SimplePoint p1, SimplePoint p2, double voltage){
        super(p1, p2);
        this.voltage = voltage;
        this.name = "v" + numVoltageElms;
        numVoltageElms++;
    }

    public double getVoltageDiff() {
        return 0;
    }

    public double calculateCurrent() {
        return 0;
    }

    public void setValue(double value) {
        this.voltage = value;
    }

    @Override
    public String getType() {
        return Constants.DC_VOLTAGE;
    }

    @Override
    public String getSpiceLabel() {
        return this.name;
    }

    @Override
    public String constructSpiceLine() {
        return this.name + " " + getNode(0).getSpiceLabel() + " " + getNode(1).getSpiceLabel() + " " + "dc" + " " + voltage;
    }

    public void onDraw(Canvas canvas, Point start, Point end, boolean horizontal, Paint circuitPaint,
                       int yDisp) {

    }
}
