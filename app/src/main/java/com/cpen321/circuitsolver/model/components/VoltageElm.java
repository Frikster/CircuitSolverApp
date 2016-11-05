package com.cpen321.circuitsolver.model.components;


import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.Log;

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
    private boolean isSelected;

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

    @Override
    public double getValue() {
        return this.voltage;
    }

    @Override
    public void toggleIsSelected(){
        Log.i("RECT", "CORRECT");
        Log.i("RECT", this.getType());
        isSelected = !isSelected;
    }

    @Override
    public void onDraw(Canvas canvas, Paint paint, int disp) {
        SimplePoint p1 = this.getPoint(0);
        SimplePoint p2 = this.getPoint(1);

        Paint.Style tmpStyle = paint.getStyle();
        paint.setStyle(Paint.Style.STROKE);

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

            canvas.drawLine(p1.getX(), p1.getY(), p1.getX(), p1.getY() - quarterLength, paint);
            canvas.drawLine(p2.getX() , p2.getY() + quarterLength, p2.getX(), p2.getY(), paint);
            SimplePoint halfway = new SimplePoint(p1.getX(), p1.getY() - (fullLength / 2));
            canvas.drawCircle(halfway.getX(), halfway.getY(), disp, paint);
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

            canvas.drawLine(p1.getX(), p1.getY(), p1.getX() + quarterLength, p1.getY(), paint);
            canvas.drawLine(p2.getX() - quarterLength, p2.getY(), p2.getX(), p2.getY(), paint);
            SimplePoint halfway = new SimplePoint(p1.getX() + (fullLength / 2), p1.getY());
            canvas.drawCircle(halfway.getX(), halfway.getY(), disp, paint);
        }
        paint.setStyle(tmpStyle);
    }
}
