package com.cpen321.circuitsolver.model.components;


import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
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
        //Edit MAX_LENGTH as needed
        float MAX_LENGTH = 100;
        float x = stopX - startX;
        float y = stopY - startY;
        float slope = y / x;
        float b = stopY - slope * stopX;
        float hypotenuse = (float) Math.hypot(x, y);
        float d = (hypotenuse - MAX_LENGTH) / 2;
        float angle = (float) Math.atan(slope);
        float innerD = (hypotenuse - 2 * d) / 5;

        float x3, x4, x5, x6, x7, x8, y3, y4, y5, y6, y7, y8;

        //when length of entire inductor is less than MAX_LENGTH
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

            x5 = stopX - (MAX_LENGTH + d - innerD) * ((float) Math.cos(angle));
            y5 = stopY - (MAX_LENGTH + d - innerD) * ((float) Math.sin(angle));

            x6 = stopX - (MAX_LENGTH + d - 2 * innerD) * ((float) Math.cos(angle));
            y6 = stopY - (MAX_LENGTH + d - 2 * innerD) * ((float) Math.sin(angle));

            x7 = stopX - (MAX_LENGTH + d - 3 * innerD) * ((float) Math.cos(angle));
            y7 = stopY - (MAX_LENGTH + d - 3 * innerD) * ((float) Math.sin(angle));

            x8 = stopX - (MAX_LENGTH + d - 4 * innerD) * ((float) Math.cos(angle));
            y8 = stopY - (MAX_LENGTH + d - 4 * innerD) * ((float) Math.sin(angle));
        }
        //when drawn right to left
        else if (x < 0) {
            x3 = startX - (d) * ((float) Math.cos(angle));
            y3 = startY - (d) * ((float) Math.sin(angle));
            x4 = startX - (MAX_LENGTH + d) * ((float) Math.cos(angle));
            y4 = startY - (MAX_LENGTH + d) * ((float) Math.sin(angle));

            x5 = startX - (d + innerD) * ((float) Math.cos(angle));
            y5 = startY - (d + innerD) * ((float) Math.sin(angle));

            x6 = startX - (d + 2 * innerD) * ((float) Math.cos(angle));
            y6 = startY - (d + 2 * innerD) * ((float) Math.sin(angle));

            x7 = startX - (d + 3 * innerD) * ((float) Math.cos(angle));
            y7 = startY - (d + 3 * innerD) * ((float) Math.sin(angle));

            x8= startX - (d + 4 * innerD) * ((float) Math.cos(angle));
            y8 = startY - (d + 4 * innerD) * ((float) Math.sin(angle));
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
            x5 = startX ;
            x6 = startX ;
            x7 = startX ;
            x8 = startX;
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
            x5 = startX ;
            x6 = startX ;
            x7 = startX ;
            x8 = startX ;
        }
        //draw wire section
        canvas.drawLine(startX, startY, x3, y3, paint);
        canvas.drawLine(x4, y4, stopX, stopY, paint);

        //draw loops
        int radius = 75;
        drawCurved((int)x3,(int) y3, (int) x5,  (int)y5, radius ,canvas, paint);
        drawCurved((int)x5,(int) y5, (int) x6,  (int)y6, radius ,canvas, paint);
        drawCurved((int)x6,(int) y6, (int) x7,  (int)y7, radius ,canvas, paint);
        drawCurved((int)x7,(int) y7, (int) x8,  (int)y8, radius ,canvas, paint);
        drawCurved((int)x8,(int) y8, (int) x4,  (int)y4, radius ,canvas, paint);
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
    //draw curved line
    public void drawCurved(int x1, int y1, int x2, int y2, int curveRadius, Canvas canvas, Paint paint) {
        final Path path = new Path();
        int midX            = x1 + ((x2 - x1) / 2);
        int midY            = y1 + ((y2 - y1) / 2);
        float xDiff         = midX - x1;
        float yDiff         = midY - y1;
        double angle        = (Math.atan2(yDiff, xDiff) * (180 / Math.PI)) - 90;
        double angleRadians = Math.toRadians(angle);
        float pointX        = (float) (midX + curveRadius * Math.cos(angleRadians));
        float pointY        = (float) (midY + curveRadius * Math.sin(angleRadians));

        Paint curvePaint  = new Paint();
        curvePaint.setAntiAlias(true);
        curvePaint.setStyle(Paint.Style.STROKE);
        curvePaint.setStrokeWidth(10);
        curvePaint.setColor(paint.getColor());

        path.moveTo(x1, y1);
        path.cubicTo(x1,y1,pointX, pointY, x2, y2);
        canvas.drawPath(path, curvePaint);
    }
}
