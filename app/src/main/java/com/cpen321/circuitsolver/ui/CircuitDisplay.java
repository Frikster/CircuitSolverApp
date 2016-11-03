package com.cpen321.circuitsolver.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.View;

import com.cpen321.circuitsolver.R;
import com.cpen321.circuitsolver.opencv.Component;
import com.cpen321.circuitsolver.util.CircuitProject;

import java.util.ArrayList;

/**
 * Created by Neil Goossen on 2016-10-15.
 */

public class CircuitDisplay extends View {
    private Paint textPaint;
    private Paint circuitPaint;
    private RectF rectF;

    private Resources res = getResources();
    private int tmpColor = res.getColor(R.color.circuitBackground);

    private CircuitProject circuitProject;
    private ArrayList<Component> components = new ArrayList<>();

    public CircuitDisplay(Context context) {
        super(context);
        this.circuitPaint = new Paint();
        this.circuitPaint.setColor(Color.BLACK);
        this.rectF = new RectF(200, 100, 300, 200);
        this.circuitPaint.setStrokeWidth(2.5f);
    }
    public CircuitDisplay(Context context, CircuitProject project) {
        super(context);
        this.circuitProject = project;
        this.circuitPaint = new Paint();
        this.circuitPaint.setColor(Color.BLACK);
        this.circuitPaint.setStrokeWidth(2.5f);
        this.rectF = new RectF(200, 100, 300, 200);
    }

    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(this.tmpColor);
        this.drawResistor(canvas, new Point(1000, 600), new Point(1300, 600), true);
        this.drawInductor(canvas, new Point(1000, 800), new Point(1300, 800), true);
        this.drawCapacitor(canvas, new Point(1000, 1000), new Point(1300, 1000), true);

    }


    private void drawResistor(Canvas canvas, Point start, Point end, boolean horizontal) {
        int fullLength = horizontal ? (end.x - start.x) : (end.y - start.y);
        float quarterLength = ((float) fullLength) / 4f;
        canvas.drawLine(start.x, start.y, start.x + quarterLength, start.y, this.circuitPaint);
        canvas.drawLine(end.x - quarterLength, end.y, end.x, end.y, this.circuitPaint);

        float halfLength = quarterLength * 2f;
        int numSpikes = 7;
        float xInterval = halfLength / ((float) numSpikes);

        Point spikeStart = new Point((int) (start.x + quarterLength), start.y);

        int startY = start.y;
        int yDisp = 50;

        for (int i=0; i < numSpikes; i++) {
            if (i == 0) {
                canvas.drawLine(spikeStart.x + (xInterval * i), startY, spikeStart.x + (xInterval * (i+1)), startY + (yDisp * (-1)^i), this.circuitPaint);
            } else if (i == (numSpikes - 1)) {
                canvas.drawLine(spikeStart.x + (xInterval * i), startY + yDisp, spikeStart.x + (xInterval * (i+1)), startY, this.circuitPaint);
            } else if (i % 2 == 0) {
                canvas.drawLine(spikeStart.x + (xInterval * (i+1)), startY + (yDisp * (-1)^i),
                        spikeStart.x + (xInterval * (i)), startY - (yDisp * (-1)^(i+1)),
                        this.circuitPaint);
            } else {
                canvas.drawLine(spikeStart.x + (xInterval * i), startY + (yDisp * (-1)^i) , spikeStart.x + (xInterval * (i+1)), startY - (yDisp * (-1)^(i+1)), this.circuitPaint);
            }
        }
    }
    private void drawInductor(Canvas canvas, Point start, Point end, boolean horizontal) {
        int fullLength = horizontal ? (end.x - start.x) : (end.y - start.y);
        float quarterLength = ((float) fullLength) / 4f;
        canvas.drawLine(start.x, start.y, start.x + quarterLength, start.y, this.circuitPaint);
        canvas.drawLine(end.x - quarterLength, end.y, end.x, end.y, this.circuitPaint);

        float halfLength = quarterLength * 2f;
        int numSpikes = 3;
        float xInterval = halfLength / ((float) numSpikes);

        Point spikeStart = new Point((int) (start.x + quarterLength), start.y);

        int yDisp = 50;

        Paint.Style tmp = this.circuitPaint.getStyle();
        this.circuitPaint.setStyle(Paint.Style.STROKE);
        for (int i=0; i < numSpikes; i++) {

            this.rectF.left = spikeStart.x + xInterval * i;
            this.rectF.bottom = spikeStart.y + yDisp;
            this.rectF.top = spikeStart.y - yDisp;
            this.rectF.right = spikeStart.x + xInterval * (i+1);
            canvas.drawArc(this.rectF, 180f, 180f, false, this.circuitPaint);
        }
        this.circuitPaint.setStyle(tmp);
    }
    private void drawCapacitor(Canvas canvas, Point start, Point end, boolean horizontal) {
        int fullLength = horizontal ? (end.x - start.x) : (end.y - start.y);
        float fifthLength = ((float) fullLength) / 5f;
        canvas.drawLine(start.x, start.y, start.x + (fifthLength*2), start.y, this.circuitPaint);
        canvas.drawLine(end.x - (2*fifthLength), end.y, end.x, end.y, this.circuitPaint);

        Point spikeStart = new Point((int) (start.x + (2*fifthLength)), start.y);

        int yDisp = 50;
        canvas.drawLine(spikeStart.x, spikeStart.y - yDisp,
                spikeStart.x, spikeStart.y + yDisp, this.circuitPaint);
        canvas.drawLine(spikeStart.x + fifthLength, spikeStart.y - yDisp,
                spikeStart.x + fifthLength, spikeStart.y + yDisp, this.circuitPaint);

    }

}
