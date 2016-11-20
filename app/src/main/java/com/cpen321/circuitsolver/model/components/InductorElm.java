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
public class InductorElm extends CircuitElm implements SpiceElm {
    private static int numInductors = 1;

    private double inductance;
    private String name;
    private boolean isSelected;

    public InductorElm(SimplePoint p1, SimplePoint p2, double inductance){
        super(p1, p2);
        this.inductance = inductance;
        this.name = "l" + numInductors;
        numInductors++;
    }

    public double getVoltageDiff() {
        return 0;
    }

    public double calculateCurrent() {
        return 0;
    }

    public void setValue(double value) {
        this.inductance = value;
    }

    @Override
    public String getType() {
        return Constants.INDUCTOR;
    }

    @Override
    public String getSpiceLabel() {
        return this.name;
    }

    @Override
    public String constructSpiceLine() {
        return this.name + " " + getNode(0).getSpiceLabel() + " " + getNode(1).getSpiceLabel() + " " + inductance;
    }

    @Override
    public double getValue() {
        return this.inductance;
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
     * Draws inductor element as a green coloured wire
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
        rpaint.setColor(Color.GREEN);
        rpaint.setStyle(Paint.Style.STROKE);

        canvas.drawLine(startX, startY, endX, endY, rpaint);

        if (isSelected()){
            showSelected(canvas);
        }

    }


    @Override
    public  void draw(Canvas canvas, float startX, float startY, float stopX, float stopY, Paint paint) {
        //TODO implement inductor draw
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
                canvas.drawRect(left, top, right, bottom, rectPaint);
            }

            canvas.drawLine(p1.getX(), p1.getY(), p1.getX(), p1.getY() + quarterLength, paint);
            canvas.drawLine(p2.getX(), p2.getY() - quarterLength, p2.getX(), p2.getY(), paint);

            float halfLength = quarterLength * 2f;
            int numSpikes = 3;
            float interval = halfLength / ((float) numSpikes);

            Point spikeStart = new Point(p1.getX(), (int) (p1.getY() + quarterLength));

            Paint.Style tmp = paint.getStyle();
            paint.setStyle(Paint.Style.STROKE);
            for (int i=0; i < numSpikes; i++) {
                rectF.left = spikeStart.x - disp;
                rectF.bottom = spikeStart.y + interval * (i+1);
                rectF.top = spikeStart.y + interval * (i);
                rectF.right = spikeStart.x + disp;
                canvas.drawArc(rectF, 270f, 180f, false, paint);
            }
            paint.setStyle(tmp);
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
                canvas.drawRect(left, top, right, bottom, rectPaint);
            }

            canvas.drawLine(p1.getX(), p1.getY(), p1.getX() + quarterLength, p1.getY(), paint);
            canvas.drawLine(p2.getX(), p2.getY(), p2.getX() - quarterLength, p2.getY(), paint);

            float halfLength = quarterLength * 2f;
            int numSpikes = 3;
            float interval = halfLength / ((float) numSpikes);

            Point spikeStart = new Point((int) (p1.getX() + quarterLength), p1.getY());

            Paint.Style tmp = paint.getStyle();
            paint.setStyle(Paint.Style.STROKE);
            for (int i=0; i < numSpikes; i++) {
                rectF.left = spikeStart.x + interval * i;
                rectF.bottom = spikeStart.y + disp;
                rectF.top = spikeStart.y - disp;
                rectF.right = spikeStart.x + interval * (i+1);
                canvas.drawArc(rectF, 180f, 180f, false, paint);
            }
            paint.setStyle(tmp);
        }
    }

    public static void resetNumElements() {
        numInductors = 1;
    }
}
