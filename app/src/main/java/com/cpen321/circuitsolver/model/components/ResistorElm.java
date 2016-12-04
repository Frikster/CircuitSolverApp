package com.cpen321.circuitsolver.model.components;


import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import com.cpen321.circuitsolver.model.CircuitNode;
import com.cpen321.circuitsolver.model.SimplePoint;
import com.cpen321.circuitsolver.model.SpiceElm;
import com.cpen321.circuitsolver.util.Constants;

import java.io.PrintWriter;

import static java.lang.Math.PI;
import static java.lang.Math.abs;

/**
 * Created by Jennifer on 10/12/2016.
 */
public class ResistorElm extends CircuitElm implements SpiceElm {
    private static final String TAG = "ResistorElm";

    private static int numResistors = 1;

    private double resistance;
    private String name;
    private boolean isSelected;
    private WireElm wire; //wire is used to represent 0 ohm resistor, used for NgSpice to get current

    public ResistorElm() {
        super();
        this.name = "r" + numResistors;
        wire = new WireElm();
        numResistors++;
    }

    public ResistorElm(SimplePoint p1, SimplePoint p2){
        super(p1, p2);
        this.resistance = 10;
        this.name = "r" + numResistors;
        wire = new WireElm(p1, p2);
        numResistors++;

    }
    public ResistorElm(SimplePoint p1, SimplePoint p2, double resistance) {
        super(p1, p2);
        this.resistance = resistance;
        this.name = "r" + numResistors;
        wire = new WireElm(p1, p2);
        numResistors++;
    }

    public double getVoltageDiff() {
        if(getNode(0) == null || getNode(0) == null) {
            return Double.MAX_VALUE;
        }
        return getNode(0).getVoltage() - getNode(1).getVoltage();
    }

    @Override
    public double calculateCurrent() {
        if(resistance == 0) {
            return getCurrent();
        } else {
            setCurrent(-1*getVoltageDiff()/resistance);
            return getCurrent();
        }
    }

    public void setValue(double value) {
        this.resistance = value;
    }

    @Override
    public void setCurrent(double current) {
        super.setCurrent(current);
        if(resistance == 0) {
            Log.d(TAG, "resistance 0, set current to " + current);
        } else {
            Log.d(TAG, "set current to " + current);
        }
    }

    @Override
    public String getType() {
        return Constants.RESISTOR;
    }

    @Override
    public String getSpiceLabel() {
        if(resistance == 0) {
            return wire.getSpiceLabel();
        } else {
            return this.name;
        }
    }

    @Override
    public String constructSpiceLine() {
        if(resistance == 0) {
            return wire.constructSpiceLine();
        } else {
            return this.name + " " + getNode(0).getSpiceLabel() + " " + getNode(1).getSpiceLabel() + " " + resistance;
        }
    }

    @Override
    public void setNode(int i, CircuitNode node) {
        wire.setNode(i, node);
        super.setNode(i, node);
    }

    @Override
    public double getValue() {
        return this.resistance;
    }


    /**
     * Draws resistor as a red line
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
        rpaint.setColor(Color.RED);
        rpaint.setStyle(Paint.Style.STROKE);

        canvas.drawLine(startX, startY, endX, endY, rpaint);

        if (isSelected()){
            showSelected(canvas);
        }

    }


    @Override
    public  void draw(Canvas canvas, float startX, float startY, float stopX, float stopY, Paint paint) {
        //Edit WIDTH and MAX_LENGTH as needed
        float WIDTH = 35;
        float MAX_LENGTH = 100;
        float x = stopX - startX;
        float y = stopY - startY;
        float slope = y / x;
        float b = stopY - slope * stopX;
        float hypotenuse = (float) Math.hypot(x, y);
        float d = (hypotenuse - MAX_LENGTH) / 2;
        float angle = (float) Math.atan(slope);
        float perpAngle = (float) Math.atan(x / y);
        float innerD = (hypotenuse - 2 * d) / 5;

        float x3, x4, x5, x6, x7, x8, y3, y4, y5, y6, y7, y8;
        float x5i, x6i, x7i, x8i, y3i, y4i, y5i, y6i, y7i, y8i;

        //when length of entire resistor is less than MAX_LENGTH
        if (hypotenuse < MAX_LENGTH) {
            MAX_LENGTH = MAX_LENGTH / 2;
            d = (hypotenuse - MAX_LENGTH) / 2;
            innerD = (hypotenuse - 2 * d) / 5;
        }
        //when drawn from left to right
        if (x > 0) {
            x3 = stopX - (MAX_LENGTH + d) * ((float) Math.cos(angle));
            y3 = stopY - (MAX_LENGTH + d) * ((float) Math.sin(angle));
            x4 = stopX - (d) * ((float) Math.cos(angle));
            y4 = stopY - (d) * ((float) Math.sin(angle));

            x5i = stopX - (MAX_LENGTH + d - innerD) * ((float) Math.cos(angle));
            x5 = x5i - WIDTH * ((float) Math.cos(perpAngle));
            y5i = stopY - (MAX_LENGTH + d - innerD) * ((float) Math.sin(angle));
            y5 = y5i + WIDTH * ((float) Math.sin(perpAngle));

            x6i = stopX - (MAX_LENGTH + d - 2 * innerD) * ((float) Math.cos(angle));
            x6 = x6i + WIDTH * ((float) Math.cos(perpAngle));
            y6i = stopY - (MAX_LENGTH + d - 2 * innerD) * ((float) Math.sin(angle));
            y6 = y6i - WIDTH * ((float) Math.sin(perpAngle));

            x7i = stopX - (MAX_LENGTH + d - 3 * innerD) * ((float) Math.cos(angle));
            x7 = x7i - WIDTH * ((float) Math.cos(perpAngle));
            y7i = stopY - (MAX_LENGTH + d - 3 * innerD) * ((float) Math.sin(angle));
            y7 = y7i + WIDTH * ((float) Math.sin(perpAngle));

            x8i = stopX - (MAX_LENGTH + d - 4 * innerD) * ((float) Math.cos(angle));
            x8 = x8i + WIDTH * ((float) Math.cos(perpAngle));
            y8i = stopY - (MAX_LENGTH + d - 4 * innerD) * ((float) Math.sin(angle));
            y8 = y8i - WIDTH * ((float) Math.sin(perpAngle));

            if(slope/(Math.abs(slope)) == -1){
                y5 = y5i - WIDTH * ((float) Math.sin(perpAngle));
                y6 = y6i + WIDTH * ((float) Math.sin(perpAngle));
                y7 = y7i - WIDTH * ((float) Math.sin(perpAngle));
                y8 = y8i + WIDTH * ((float) Math.sin(perpAngle));

                x5 = x5i + WIDTH * ((float) Math.cos(perpAngle));
                x6 = x6i - WIDTH * ((float) Math.cos(perpAngle));
                x7 = x7i + WIDTH * ((float) Math.cos(perpAngle));
                x8 = x8i - WIDTH * ((float) Math.cos(perpAngle));
            }
        }
        //when drawn right to left
        else if (x < 0) {
            x3 = startX - (d) * ((float) Math.cos(angle));
            y3 = startY - (d) * ((float) Math.sin(angle));
            x4 = startX - (MAX_LENGTH + d) * ((float) Math.cos(angle));
            y4 = startY - (MAX_LENGTH + d) * ((float) Math.sin(angle));

            x5i = startX - (d + innerD) * ((float) Math.cos(angle));
            x5 = x5i - WIDTH * ((float) Math.cos(perpAngle));
            y5i = startY - (d + innerD) * ((float) Math.sin(angle));
            y5 = y5i + WIDTH * ((float) Math.sin(perpAngle));

            x6i = startX - (d + 2 * innerD) * ((float) Math.cos(angle));
            x6 = x6i + WIDTH * ((float) Math.cos(perpAngle));
            y6i = startY - (d + 2 * innerD) * ((float) Math.sin(angle));
            y6 = y6i - WIDTH * ((float) Math.sin(perpAngle));

            x7i = startX - (d + 3 * innerD) * ((float) Math.cos(angle));
            x7 = x7i - WIDTH * ((float) Math.cos(perpAngle));
            y7i = startY - (d + 3 * innerD) * ((float) Math.sin(angle));
            y7 = y7i + WIDTH * ((float) Math.sin(perpAngle));

            x8i = startX - (d + 4 * innerD) * ((float) Math.cos(angle));
            x8 = x8i + WIDTH * ((float) Math.cos(perpAngle));
            y8i = startY - (d + 4 * innerD) * ((float) Math.sin(angle));
            y8 = y8i - WIDTH * ((float) Math.sin(perpAngle));

            if(slope/(Math.abs(slope)) == 1){
                y5 = y5i - WIDTH * ((float) Math.sin(perpAngle));
                y6 = y6i + WIDTH * ((float) Math.sin(perpAngle));
                y7 = y7i - WIDTH * ((float) Math.sin(perpAngle));
                y8 = y8i + WIDTH * ((float) Math.sin(perpAngle));

                x5 = x5i + WIDTH * ((float) Math.cos(perpAngle));
                x6 = x6i - WIDTH * ((float) Math.cos(perpAngle));
                x7 = x7i + WIDTH * ((float) Math.cos(perpAngle));
                x8 = x8i - WIDTH * ((float) Math.cos(perpAngle));
            }
        }
        //when drawn vertically pointing down
        else if (y > 0) {
            x3 = stopX;
            y3 = startY + d;
            x4 = stopX;
            y4 = stopY - d;
            y5 = startY + d + innerD;
            y6 = startY + d + 2 * innerD;
            y7 = startY + d + 3 * innerD;
            y8 = startY + d + 4 * innerD;
            x5 = startX - WIDTH;
            x6 = startX + WIDTH;
            x7 = startX - WIDTH;
            x8 = startX + WIDTH;
        }
        //when drawn vertically pointing up
        else {
            x3 = stopX;
            y3 = startY - d;
            x4 = stopX;
            y4 = stopY + d;
            y5 = startY - d - innerD;
            y6 = startY - d - 2 * innerD;
            y7 = startY - d - 3 * innerD;
            y8 = startY - d - 4 * innerD;
            x5 = startX + WIDTH;
            x6 = startX - WIDTH;
            x7 = startX + WIDTH;
            x8 = startX - WIDTH;
        }
        //draw wire section
        canvas.drawLine(startX, startY, x3, y3, paint);
        canvas.drawLine(x4, y4, stopX, stopY, paint);

        //draw zigzag part
        canvas.drawLine(x3, y3, x5, y5, paint);
        canvas.drawLine(x5, y5, x6, y6, paint);
        canvas.drawLine(x6, y6, x7, y7, paint);
        canvas.drawLine(x7, y7, x8, y8, paint);
        canvas.drawLine(x8, y8, x4, y4, paint);
    }

    @Override
    public void onDraw(Canvas canvas, Paint paint,
                       int disp) {
        // super.onDraw(canvas, paint, disp);
        SimplePoint p1 = this.getPoint(0);
        SimplePoint p2 = this.getPoint(1);
        Paint rectPaint = new Paint();
        rectPaint.setColor(Color.RED);
        rectPaint.setStyle(Paint.Style.STROKE);

        Log.i("RECT", "onDraw");
        if (p1.getX() == p2.getX()) {
            int fullLength = abs(p2.getY() - p1.getY());
            float quarterLength = ((float) fullLength) / 4f;

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
                canvas.drawRect(left, top, right, bottom, rectPaint);
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
            int fullLength = abs(p2.getX() - p1.getX());
            float quarterLength = ((float) fullLength) / 4f;

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
                canvas.drawRect(left, top, right, bottom, rectPaint);
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

    public static void resetNumElements() {
        numResistors = 1;
    }
}
