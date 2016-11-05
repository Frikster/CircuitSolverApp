package com.cpen321.circuitsolver.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.method.TextKeyListener;
import android.view.View;

import com.cpen321.circuitsolver.R;
import com.cpen321.circuitsolver.model.SimplePoint;
import com.cpen321.circuitsolver.model.components.CapacitorElm;
import com.cpen321.circuitsolver.model.components.CircuitElm;
import com.cpen321.circuitsolver.model.components.InductorElm;
import com.cpen321.circuitsolver.model.components.ResistorElm;
import com.cpen321.circuitsolver.model.components.VoltageElm;
import com.cpen321.circuitsolver.model.components.WireElm;
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

    private Paint mTextPaint;

    private CircuitProject circuitProject;
    private ArrayList<CircuitElm> components = new ArrayList<>();

    public CircuitDisplay(Context context) {
        super(context);
        this.circuitPaint = new Paint();
        this.circuitPaint.setColor(Color.BLACK);
        this.rectF = new RectF(200, 100, 300, 200);
        this.circuitPaint.setStrokeWidth(2.5f);
        this.init();
    }
    public CircuitDisplay(Context context, CircuitProject project) {
        super(context);
        this.circuitProject = project;
        this.circuitPaint = new Paint();
        this.circuitPaint.setColor(Color.BLACK);
        this.circuitPaint.setStrokeWidth(2.5f);
        this.rectF = new RectF(200, 100, 300, 200);
    }

    private void init() {
        this.components.add(new InductorElm(new SimplePoint(300, 300),
                new SimplePoint(500, 300), 5));
        this.components.add(new WireElm(new SimplePoint(500, 300),
                new SimplePoint(700, 500)));
        this.components.add(new CapacitorElm(new SimplePoint(700, 500),
                new SimplePoint(700, 700), 5));
        this.components.add(new WireElm(new SimplePoint(500, 900), new SimplePoint(700, 700)));
        this.components.add(new ResistorElm(new SimplePoint(500, 900),
                new SimplePoint(300, 900), 5));
        this.components.add(new WireElm(new SimplePoint(300, 900),
                new SimplePoint(300, 700)));
        this.components.add(new VoltageElm(new SimplePoint(300, 700),
                new SimplePoint(300, 500), 5));
        this.components.add(new WireElm(new SimplePoint(300, 500),
                new SimplePoint(300, 300)));

//      test stuff for the rectangle
//        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//        mTextPaint.setColor(mTextColor);
//        if (mTextHeight == 0) {
//            mTextHeight = mTextPaint.getTextSize();
//        } else {
//            mTextPaint.setTextSize(mTextHeight);
//        }
//
//        mPiePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//        mPiePaint.setStyle(Paint.Style.FILL);
//        mPiePaint.setTextSize(mTextHeight);
//
//        mShadowPaint = new Paint(0);
//        mShadowPaint.setColor(0xff101010);
//        mShadowPaint.setMaskFilter(new BlurMaskFilter(8, BlurMaskFilter.Blur.NORMAL));


    }

    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(this.tmpColor);
        for (CircuitElm circuitElm : this.components) {
            circuitElm.onDraw(canvas, this.circuitPaint, 50);
        }

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

    /**
     * Find the CircuitElm touched by (x, y). Return null if none are touched
     */
    public CircuitElm getCircuitElemTouched(int x, int y){
        CircuitElm candidate = null;
        double candidate_distance = Double.POSITIVE_INFINITY;
        for(CircuitElm circuitElm : components ){
            int x1 = circuitElm.getPoint(0).getX();
            int y1 = circuitElm.getPoint(0).getY();
            int x2 = circuitElm.getPoint(1).getX();
            int y2 = circuitElm.getPoint(1).getY();
            double mid_x = (x1 + x2) / 2.0;
            double mid_y = (y1 + y2) / 2.0;
            double distance = Math.sqrt((mid_x-x)*(mid_x-x) + (mid_y-y)*(mid_y-y));
            if(distance < candidate_distance) {
                candidate_distance = distance;
                candidate = circuitElm;
            }
        }
        if(candidate == null){
            return candidate;
        }
        // Find bounding rect defining
        int x1 = candidate.getPoint(0).getX();
        int y1 = candidate.getPoint(0).getY();
        int x2 = candidate.getPoint(1).getX();
        int y2 = candidate.getPoint(1).getY();
        int touchThreshold = 50;
        assert(x1==x2 || y1==y2);
        if (x1 == x2){
            int bound_rect_x1 = x1 - touchThreshold;
            int bound_rect_x2 = x1 + touchThreshold;
        }
        else{
            int bound_rect_y1 = y1 - touchThreshold;
            int bound_rect_y2 = y1 + touchThreshold;
        }

//        int touchThreshold = 50;
//
//
//        int bound_rect_y = x1 + touchThreshold;
//        int bound_rect_y = y1 + touchThreshold;
//
//
//        CircuitElm candidate = null;
//        double candidate_distance = Double.POSITIVE_INFINITY;
//        for(CircuitElm circuitElm : components ){
//            int x1 = circuitElm.getPoint(0).getX();
//            int y1 = circuitElm.getPoint(0).getY();
//            int x2 = circuitElm.getPoint(1).getX();
//            int y2 = circuitElm.getPoint(1).getY();
//            double mid_x = (x1 + x2) / 2.0;
//            double mid_y = (y1 + y2) / 2.0;
//            double distance = Math.sqrt((mid_x-x)*(mid_x-x) + (mid_y-y)*(mid_y-y));
//            if(distance < candidate_distance && distance < touchThreshold) {
//                candidate_distance = distance;
//                candidate = circuitElm;
//            }
//        }




        return candidate;
    }
}
