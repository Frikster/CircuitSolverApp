package com.cpen321.circuitsolver.model.components;


import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

import com.cpen321.circuitsolver.model.SimplePoint;
import com.cpen321.circuitsolver.model.SpiceElm;
import com.cpen321.circuitsolver.util.Constants;

/**
 * Created by Jennifer on 10/12/2016.
 */
public class ResistorElm extends CircuitElm implements SpiceElm {
    private static int numResistors = 1;

    private double resistance;
    private String name;
    private boolean isSelected;

    public ResistorElm(SimplePoint p1, SimplePoint p2, double resistance){
        super(p1, p2);
        this.resistance = resistance;
        this.name = "r" + numResistors;
        numResistors++;

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

    @Override
    public String getSpiceLabel() {
        return this.name;
    }

    @Override
    public String constructSpiceLine() {
        return this.name + " " + getNode(0).getSpiceLabel() + " " + getNode(1).getSpiceLabel() + " " + resistance;
    }

    @Override
    public double getValue() {
        return this.resistance;
    }

    @Override
    public void toggleIsSelected(){
        Log.i("RECT", "CORRECT");
        Log.i("RECT", this.getType());
        isSelected = !isSelected;
    }

    @Override
    public void onDraw(Canvas canvas, Paint paint,
                       int disp) {
        // super.onDraw(canvas, paint, disp);
        SimplePoint p1 = this.getPoint(0);
        SimplePoint p2 = this.getPoint(1);

        Log.i("RECT", "onDraw");
        if (p1.getX() == p2.getX()) {
            int fullLength = Math.abs(p2.getY() - p1.getY());
            float quarterLength = ((float) fullLength) / 4f;
            paint.setStyle(Paint.Style.STROKE);

            // Draw a rectangle as required
            if (isSelected){
                float left = p1.getX() - quarterLength;
                float top = p1.getY();
                float right = p1.getX() + quarterLength;
                float bottom = p2.getY();
                if (top > bottom){
                    top = p2.getY();
                    bottom = p1.getY();
                }
                canvas.drawRect(left, top, right, bottom, paint);
            }

            float halfLength = quarterLength * 2f;
            int numSpikes = 7;
            float interval = halfLength / ((float) numSpikes);

            canvas.drawLine(p1.getX(), p1.getY(), p1.getX(), p1.getY() - quarterLength, paint);
            canvas.drawLine(p2.getX() , p2.getY() + quarterLength, p2.getX(), p2.getY(), paint);

            SimplePoint spikeStart = new SimplePoint(p1.getX(), (int) (p1.getY() - quarterLength));

            for (int i=0; i < numSpikes; i++) {
                if (i == 0) {
                    canvas.drawLine(spikeStart.getX(),
                            spikeStart.getY() - (interval * i),
                            spikeStart.getX() + (disp * (-1)^i),
                            spikeStart.getY() - (interval * (i+1)), paint);
                } else if (i == (numSpikes - 1)) {
                    canvas.drawLine(spikeStart.getX() + disp,
                            spikeStart.getY() - (interval * i),
                            spikeStart.getX(),
                            spikeStart.getY() - (interval * (i+1)), paint);
                } else if (i % 2 == 0) {
                    canvas.drawLine(spikeStart.getX() + (disp * (-1)^i),
                            spikeStart.getY() - (interval * (i+1)),
                            spikeStart.getX()- (disp * (-1)^(i+1)),
                            spikeStart.getY() - (interval * (i)), paint);
                } else {
                    canvas.drawLine(spikeStart.getX() + (disp * (-1)^i),
                            spikeStart.getY()  - (interval * i),
                            spikeStart.getX() - (disp * (-1)^(i+1)),
                            spikeStart.getY()  - (interval * (i+1)), paint);
                }
            }
        } else {
            int fullLength = Math.abs(p2.getX() - p1.getX());
            float quarterLength = ((float) fullLength) / 4f;
            paint.setStyle(Paint.Style.STROKE);

            // Draw a rectangle as required
            if (isSelected){
                float left = p1.getX();
                float top = p1.getY() - quarterLength;
                float right = p2.getX();
                float bottom = p1.getY() + quarterLength;
                if (left > right){
                    left = p2.getX();
                    right = p1.getX();
                }
                canvas.drawRect(left, top, right, bottom, paint);
            }

            canvas.drawLine(p1.getX(), p1.getY(), p1.getX() - quarterLength, p1.getY(), paint);
            canvas.drawLine(p2.getX() + quarterLength, p2.getY(), p2.getX(), p2.getY(), paint);

            float halfLength = quarterLength * 2f;
            int numSpikes = 7;
            float xInterval = halfLength / ((float) numSpikes);

            SimplePoint spikeStart = new SimplePoint((int) (p1.getX() - quarterLength), p1.getY());

            int startY = p1.getY();

            for (int i=0; i < numSpikes; i++) {
                if (i == 0) {
                    canvas.drawLine(spikeStart.getX() - (xInterval * i), startY, spikeStart.getX() - (xInterval * (i+1)), startY + (disp * (-1)^i), paint);
                } else if (i == (numSpikes - 1)) {
                    canvas.drawLine(spikeStart.getX() - (xInterval * i), startY + disp, spikeStart.getX() - (xInterval * (i+1)), startY, paint);
                } else if (i % 2 == 0) {
                    canvas.drawLine(spikeStart.getX() - (xInterval * (i+1)), startY + (disp * (-1)^i),
                            spikeStart.getX() - (xInterval * (i)), startY - (disp * (-1)^(i+1)),
                            paint);
                } else {
                    canvas.drawLine(spikeStart.getX() - (xInterval * i), startY + (disp * (-1)^i) , spikeStart.getX() - (xInterval * (i+1)), startY - (disp * (-1)^(i+1)), paint);
                }
            }
        }

    }
}
