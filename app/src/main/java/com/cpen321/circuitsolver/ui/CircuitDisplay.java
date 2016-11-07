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
import android.util.Log;
import android.view.View;

import com.cpen321.circuitsolver.R;
import com.cpen321.circuitsolver.model.CircuitElmFactory;
import com.cpen321.circuitsolver.model.SimplePoint;
import com.cpen321.circuitsolver.model.components.CapacitorElm;
import com.cpen321.circuitsolver.model.components.CircuitElm;
import com.cpen321.circuitsolver.model.components.InductorElm;
import com.cpen321.circuitsolver.model.components.ResistorElm;
import com.cpen321.circuitsolver.model.components.VoltageElm;
import com.cpen321.circuitsolver.model.components.WireElm;
import com.cpen321.circuitsolver.opencv.Component;
import com.cpen321.circuitsolver.util.CircuitProject;
import com.cpen321.circuitsolver.util.Constants;

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

    Paint paint = new Paint();

    CircuitElmFactory circuitElmFactory = new CircuitElmFactory();

    public CircuitDisplay(Context context) {
        super(context);
        this.circuitPaint = new Paint();
        this.circuitPaint.setColor(Color.BLACK);
        this.rectF = new RectF(200, 100, 300, 200);
        this.circuitPaint.setStrokeWidth(2.5f);
        //this.init();
    }

    public CircuitDisplay(Context context, CircuitProject project) {
        super(context);
        this.circuitProject = project;
        this.circuitPaint = new Paint();
        this.circuitPaint.setColor(Color.BLACK);
        this.circuitPaint.setStrokeWidth(2.5f);
        this.rectF = new RectF(200, 100, 300, 200);
    }

    public void init() { // simply a test while we wait to get actual values from the processing
        this.components.add(new InductorElm(new SimplePoint(300, 300),
                new SimplePoint(500, 300), 1.5));
        this.components.add(new WireElm(new SimplePoint(500, 300),
                new SimplePoint(700, 500)));
        this.components.add(new CapacitorElm(new SimplePoint(700, 500),
                new SimplePoint(700, 700), 77));
        this.components.add(new WireElm(new SimplePoint(500, 900), new SimplePoint(700, 700)));
        this.components.add(new ResistorElm(new SimplePoint(500, 900),
                new SimplePoint(300, 900), 10));
        this.components.add(new WireElm(new SimplePoint(300, 900),
                new SimplePoint(300, 700)));
        this.components.add(new VoltageElm(new SimplePoint(300, 700),
                new SimplePoint(300, 500), 12));
        this.components.add(new WireElm(new SimplePoint(300, 500),
                new SimplePoint(300, 300)));
    }

    public void displayComponent(String c){
        this.components.clear();
        switch (c) {
            case Constants.CAPACITOR: {
                this.components.add(new CapacitorElm(new SimplePoint(700, 500),
                        new SimplePoint(700, 700), 77));
                break;
            }
            case Constants.RESISTOR: {
                this.components.add(new ResistorElm(new SimplePoint(500, 900),
                        new SimplePoint(300, 900), 10));
                break;
            }
            case Constants.DC_VOLTAGE: {
                this.components.add(new VoltageElm(new SimplePoint(300, 700),
                        new SimplePoint(300, 500), 12));
                break;
            }
            case Constants.INDUCTOR: {
                this.components.add(new InductorElm(new SimplePoint(300, 300),
                        new SimplePoint(500, 300), 1.5));
                break;
            }
        }
    }

    public CircuitElm getRandomElement() {
        return this.components.get(4);
    }

    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(this.tmpColor);
        for (CircuitElm circuitElm : this.components) {
            if (circuitElm == null)
                continue;
            circuitElm.onDraw(canvas, this.circuitPaint, 50);
        }
    }

    /**
     * Find the CircuitElm touched by (x, y). Return null if none are touched
     */
    public CircuitElm getCircuitElemTouched(int x, int y){
        CircuitElm candidate = null;
        double candidate_distance = 50;
        for(CircuitElm circuitElm : components )
        {
            int x1 = circuitElm.getPoint(0).getX();
            int y1 = circuitElm.getPoint(0).getY();
            int x2 = circuitElm.getPoint(1).getX();
            int y2 = circuitElm.getPoint(1).getY();
            double mid_x = (x1 + x2) / 2.0;
            double mid_y = (y1 + y2) / 2.0;
            double distance = Math.sqrt((mid_x-x)*(mid_x-x) + (mid_y-y)*(mid_y-y));
            if(distance < candidate_distance)
            {
                candidate_distance = distance;
                candidate = circuitElm;
            }
            else{
                if(circuitElm.isSelected()){
                    circuitElm.toggleIsSelected();
                }
            }
        }
        if (candidate != null)
        {
            candidate.toggleIsSelected();
            this.invalidate();
        }
        return candidate;
    }

    public void rotateElement(CircuitElm elementToRotate) {
        for (int i=0; i < this.components.size(); i++) {
            CircuitElm elm = this.components.get(i);
            if (elm.equals(elementToRotate)){
                elm = this.swapOrientation(elm);
                this.components.set(i, elm);
//                break;
            }
        }
        this.invalidate();
    }

    public void changeElementType(CircuitElm elementToChange, String newType) {
        for (int i=0; i < this.components.size(); i++) {
            CircuitElm elm = this.components.get(i);
            if (elm.equals(elementToChange)){
                elm = this.circuitElmFactory.makeElm(newType,
                        elementToChange.getP1(),
                        elementToChange.getP2(),
                        elementToChange.getValue());
                this.components.set(i, elm);
                break;
            }
        }
        this.invalidate();
    }

    public void changeElementValue(CircuitElm elementToChange, double newValue) {
        for (int i=0; i < this.components.size(); i++) {
            CircuitElm elm = this.components.get(i);
            if (elm.equals(elementToChange)){
                elm.setValue(newValue);
                this.components.set(i, elm);
                break;
            }
        }
        this.invalidate();
    }

    private CircuitElm swapOrientation(CircuitElm element) {
        SimplePoint start = element.getPoint(0);
        SimplePoint end = element.getPoint(1);

        SimplePoint newStart;
        SimplePoint newEnd;

        if (start.getX() == end.getX()) {
            int length = end.getY() - start.getY();
            int halfLength = length/2;
            int middleY = start.getY() + length/2;
            newStart = new SimplePoint(start.getX() - halfLength, middleY);
            newEnd = new SimplePoint(start.getX() + halfLength, middleY);
        } else {
            int length = end.getX() - start.getX();
            int halfLength = length/2;
            int middleX = start.getX() + length/2;
            newStart = new SimplePoint(middleX, start.getY() - halfLength);
            newEnd = new SimplePoint(middleX, start.getY() + halfLength);
        }
        element.setP1(newStart);
        element.setP2(newEnd);

        return element;
    }
}
