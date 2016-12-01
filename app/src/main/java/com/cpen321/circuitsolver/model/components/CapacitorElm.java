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
    private static int numCapacitors = 1;

    private double capacitance;
    private String name;
    private boolean isSelected;

    public CapacitorElm() {
        super();
        this.name = "c" + numCapacitors;
        numCapacitors++;
    }

    public CapacitorElm(SimplePoint p1, SimplePoint p2, double capacitance){
        super(p1, p2);
        this.capacitance = capacitance;
        this.name = "c" + numCapacitors;
        numCapacitors++;
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

    /**
     * Draws capacitor element as a cyan coloured wire
     * @param canvas
     * @param paint
     * @param disp
     * @param test
     */
    public void onDraw(Canvas canvas, Paint paint, int disp, boolean test){

        int startX = this.getPoint(0).getX();
        int startY = this.getPoint(0).getY();
        int endX = this.getPoint(1).getX();
        int endY = this.getPoint(1).getY();
        Paint rpaint = new Paint();
        rpaint.setColor(Color.CYAN);
        rpaint.setStyle(Paint.Style.STROKE);
        canvas.drawLine(startX, startY, endX, endY, rpaint);

        if (isSelected()){
            showSelected(canvas);
        }

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

    @Override
    public void draw(Canvas canvas, float startX, float startY, float stopX, float stopY, Paint paint) {
        //Edit ANODE_WIDTH, CATHODE_WIDTH, and SPACE_LENGTH as needed
        float ANODE_WIDTH = 45;
        float CATHODE_WIDTH = 45;
        float SPACE_LENGTH = 22;
        float x = stopX - startX;
        float y = stopY - startY;
        float slope = y / x;
        float b = stopY - slope * stopX;
        float hypotenuse =  (float) Math.hypot(x, y);
        float d = (hypotenuse - SPACE_LENGTH) / 2;
        float angle = (float) Math.atan(slope);
        float perpAngle = (float) Math.atan(x / y);
        float innerD = (hypotenuse - 2 * d) / 5;

        float x3, x4, x5, x6, x7, x8, y3, y4, y5, y6, y7, y8;

        //when drawn from left to right
        if (x > 0) {
            x3 = stopX - (SPACE_LENGTH + d) * ((float) Math.cos(angle));
            y3 = stopY - (SPACE_LENGTH + d) * ((float) Math.sin(angle));
            x4 = stopX - (d) * ((float) Math.cos(angle));
            y4 = stopY - (d) * ((float) Math.sin(angle));

            x5 = x3 - ANODE_WIDTH * ((float) Math.cos(perpAngle));
            y5 = y3 + ANODE_WIDTH * ((float) Math.sin(perpAngle));

            x6 = x3 + ANODE_WIDTH * ((float) Math.cos(perpAngle));
            y6 = y3 - ANODE_WIDTH * ((float) Math.sin(perpAngle));

            x7 = x4 - CATHODE_WIDTH * ((float) Math.cos(perpAngle));
            y7 = y4 + CATHODE_WIDTH * ((float) Math.sin(perpAngle));

            x8 = x4 + CATHODE_WIDTH * ((float) Math.cos(perpAngle));
            y8 = y4 - CATHODE_WIDTH * ((float) Math.sin(perpAngle));
        }
        //when drawn right to left
        else if (x < 0) {
            x3 = startX - (d) * ((float) Math.cos(angle));
            y3 = startY - (d) * ((float) Math.sin(angle));
            x4 = startX - (SPACE_LENGTH + d) * ((float) Math.cos(angle));
            y4 = startY - (SPACE_LENGTH + d) * ((float) Math.sin(angle));

            x5 = x3 - ANODE_WIDTH * ((float) Math.cos(perpAngle));
            y5 = y3 + ANODE_WIDTH * ((float) Math.sin(perpAngle));

            x6 = x3 + ANODE_WIDTH * ((float) Math.cos(perpAngle));
            y6 = y3 - ANODE_WIDTH * ((float) Math.sin(perpAngle));

            x7 = x4 - CATHODE_WIDTH * ((float) Math.cos(perpAngle));
            y7 = y4 + CATHODE_WIDTH * ((float) Math.sin(perpAngle));

            x8 = x4 + CATHODE_WIDTH * ((float) Math.cos(perpAngle));
            y8 = y4 - CATHODE_WIDTH * ((float) Math.sin(perpAngle));
        }
        //when drawn vertically pointing down
        else if (y > 0) {
            x3 = stopX;
            y3 = startY + d;
            x4 = stopX;
            y4 = stopY - d;
            y5 = y3;
            y6 = y3;
            y7 = y4;
            y8 = y4;
            x5 = startX + ANODE_WIDTH;
            x6 = startX - ANODE_WIDTH;
            x7 = startX + CATHODE_WIDTH;
            x8 = startX - CATHODE_WIDTH;
        }
        //when drawn vertically pointing up
        else {
            x3 = stopX;
            y3 = startY - d;
            x4 = stopX;
            y4 = stopY + d;
            y5 = y3;
            y6 = y3;
            y7 = y4;
            y8 = y4;
            x5 = startX + ANODE_WIDTH;
            x6 = startX - ANODE_WIDTH;
            x7 = startX + CATHODE_WIDTH;
            x8 = startX - CATHODE_WIDTH;
        }

        //draw wire section
        canvas.drawLine(startX, startY, x3, y3, paint);
        canvas.drawLine(x4, y4, stopX, stopY, paint);
        //draw perpendicular lines part
        canvas.drawLine(x3, y3, x5, y5, paint);
        canvas.drawLine(x3, y3, x6, y6, paint);
        canvas.drawLine(x4, y4, x7, y7, paint);
        canvas.drawLine(x4, y4, x8, y8, paint);
    }

    public static void resetNumElements() {
        numCapacitors = 1;
    }
}
