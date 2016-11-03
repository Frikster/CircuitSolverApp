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
        int fullLength = horizontal ? (end.x - start.x) : (end.y - start.y);
        float quarterLength = ((float) fullLength) / 4f;
        canvas.drawLine(start.x, start.y, start.x + quarterLength, start.y, circuitPaint);
        canvas.drawLine(end.x - quarterLength, end.y, end.x, end.y, circuitPaint);

        float halfLength = quarterLength * 2f;
        int numSpikes = 7;
        float xInterval = halfLength / ((float) numSpikes);

        Point spikeStart = new Point((int) (start.x + quarterLength), start.y);

        int startY = start.y;

        for (int i=0; i < numSpikes; i++) {
            if (i == 0) {
                canvas.drawLine(spikeStart.x + (xInterval * i), startY, spikeStart.x + (xInterval * (i+1)), startY + (yDisp * (-1)^i), circuitPaint);
            } else if (i == (numSpikes - 1)) {
                canvas.drawLine(spikeStart.x + (xInterval * i), startY + yDisp, spikeStart.x + (xInterval * (i+1)), startY, circuitPaint);
            } else if (i % 2 == 0) {
                canvas.drawLine(spikeStart.x + (xInterval * (i+1)), startY + (yDisp * (-1)^i),
                        spikeStart.x + (xInterval * (i)), startY - (yDisp * (-1)^(i+1)),
                        circuitPaint);
            } else {
                canvas.drawLine(spikeStart.x + (xInterval * i), startY + (yDisp * (-1)^i) , spikeStart.x + (xInterval * (i+1)), startY - (yDisp * (-1)^(i+1)), circuitPaint);
            }
        }


    }
}
