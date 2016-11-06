package com.cpen321.circuitsolver.model.components;


import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.Log;

import com.cpen321.circuitsolver.model.SimplePoint;
import com.cpen321.circuitsolver.model.SpiceElm;
import com.cpen321.circuitsolver.util.Constants;

/**
 * Created by Jennifer on 10/12/2016.
 */
public class CapacitorElm extends CircuitElm implements SpiceElm {
    private static int numResistors = 1;

    private double capacitance;
    private String name;
    private boolean isSelected;

    public CapacitorElm(SimplePoint p1, SimplePoint p2, double capacitance){
        super(p1, p2);
        this.capacitance = capacitance;
        this.name = "c" + numResistors;
        numResistors++;
    }

    public double getVoltageDiff() {
        return 0;
    }

    public double calculateCurrent() {
        return 0;
    }

    public void setValue(double value) {
        this.capacitance = value;
    }

    @Override
    public String getType() {
        return Constants.CAPACITOR;
    }

    @Override
    public String getSpiceLabel() {
        return this.name;
    }

    @Override
    public String constructSpiceLine() {
        return this.name + " " + getNode(0).getSpiceLabel() + " " + getNode(1).getSpiceLabel() + " " + capacitance;
    }

    @Override
    public double getValue() {
        return this.capacitance;
    }

    @Override
    public boolean isSelected(){
        return isSelected;
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
        SimplePoint p1 = this.getPoint(0);
        SimplePoint p2 = this.getPoint(1);
        Paint rectPaint = new Paint();
        rectPaint.setColor(Color.RED);
        rectPaint.setStyle(Paint.Style.STROKE);
        RectF rectF = new RectF(p1.getY(), p1.getX(), p1.getX() + disp, p2.getY());

        if (p1.getX() == p2.getX()) {
            int fullLength = Math.abs(p2.getY() - p1.getY());
            float fifthLength = ((float) fullLength) / 5f;
            paint.setStyle(Paint.Style.STROKE);

            // Draw a rectangle as required
            if (isSelected){
                float left = p1.getX() - fifthLength;
                float top = p1.getY();
                float right = p1.getX() + fifthLength;
                float bottom = p2.getY();
                if (top > bottom){
                    top = p2.getY();
                    bottom = p1.getY();
                }
                canvas.drawRect(left, top, right, bottom, rectPaint);
            }

            canvas.drawLine(p1.getX(), p1.getY() + (2*fifthLength), p1.getX(), p1.getY(), paint);
            canvas.drawLine(p2.getX(), p2.getY() - (2*fifthLength), p2.getX(), p2.getY(), paint);

            Point spikeStart = new Point(p1.getX(), (int) (p1.getY() + (2*fifthLength)));

            canvas.drawLine(spikeStart.x - disp, spikeStart.y,
                    spikeStart.x + disp, spikeStart.y, paint);
            canvas.drawLine(spikeStart.x - disp, spikeStart.y + fifthLength,
                    spikeStart.x + disp, spikeStart.y + fifthLength, paint);
        } else {
            int fullLength = Math.abs(p2.getX() - p1.getX());
            float fifthLength = ((float) fullLength) / 5f;
            paint.setStyle(Paint.Style.STROKE);

            // Draw a rectangle as required
            if (isSelected){
                float left = p1.getX();
                float top = p1.getY() - fifthLength;
                float right = p2.getX();
                float bottom = p1.getY() + fifthLength;
                if (left > right){
                    left = p2.getX();
                    right = p1.getX();
                }
                canvas.drawRect(left, top, right, bottom, rectPaint);
            }

            canvas.drawLine(p1.getX(), p1.getY(), p1.getX() + (2*fifthLength), p1.getY(), paint);
            canvas.drawLine(p2.getX() - (2*fifthLength), p2.getY(), p2.getX(), p2.getY(), paint);

            Point spikeStart = new Point((int) (p1.getX() + (2*fifthLength)), p1.getY());

            canvas.drawLine(spikeStart.x, spikeStart.y - disp,
                    spikeStart.x, spikeStart.y + disp, paint);
            canvas.drawLine(spikeStart.x + fifthLength, spikeStart.y - disp,
                    spikeStart.x + fifthLength, spikeStart.y + disp, paint);
        }

    }
}
