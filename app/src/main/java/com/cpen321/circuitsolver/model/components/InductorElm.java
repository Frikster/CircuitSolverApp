package com.cpen321.circuitsolver.model.components;


import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;

import com.cpen321.circuitsolver.model.SimplePoint;
import com.cpen321.circuitsolver.model.SpiceElm;
import com.cpen321.circuitsolver.model.SpiceLabel;
import com.cpen321.circuitsolver.util.Constants;

/**
 * Created by Jennifer on 10/12/2016.
 */
public class InductorElm extends CircuitElm implements SpiceElm {
    private static int numResistors = 1;

    private double resistance;
    private String name;

    public InductorElm(SimplePoint p1, SimplePoint p2, double resistance){
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
    public void onDraw(Canvas canvas, Paint paint,
                       int disp) {
        SimplePoint p1 = this.getPoint(0);
        SimplePoint p2 = this.getPoint(1);
        RectF rectF = new RectF(p1.getY(), p1.getX(), p1.getX() + disp, p2.getY());

        if (p1.getX() == p2.getX()) {
            int fullLength = (p2.getY() - p1.getY());
            float quarterLength = ((float) fullLength) / 4f;
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
            int fullLength = (p2.getX() - p1.getX());
            float quarterLength = ((float) fullLength) / 4f;
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
}
