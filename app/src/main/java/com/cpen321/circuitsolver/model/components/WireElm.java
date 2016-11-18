package com.cpen321.circuitsolver.model.components;


import android.graphics.Canvas;
import android.graphics.Paint;

import com.cpen321.circuitsolver.model.SimplePoint;
import com.cpen321.circuitsolver.util.Constants;

/**
 * Created by Jennifer on 10/12/2016.
 */
public class WireElm extends CircuitElm {

    public WireElm(SimplePoint p1, SimplePoint p2) {
        super(p1, p2);
    }

    public double getVoltageDiff() {
        //TODO: implement this method
        return 0;
    }

    public double calculateCurrent() {
        //TODO: implement this method
        return 0;
    }

    public void setValue(double value) {
        //TODO: throw an IllegalEditWireValueException
    }

    @Override
    public String getType() {
        return Constants.WIRE;
    }

    public void onDraw(Canvas canvas, Paint paint, int yDisp, boolean test){
        onDraw(canvas, paint, yDisp);
    }

    @Override
    public void onDraw(Canvas canvas, Paint paint, int yDisp) {
        int startX = this.getPoint(0).getX();
        int startY = this.getPoint(0).getY();
        int endX = this.getPoint(1).getX();
        int endY = this.getPoint(1).getY();

        canvas.drawLine(startX, startY, endX, endY, paint);

        if (isSelected()){
            showSelected(canvas);
        }

    }

    public boolean isWire(){
        return true;
    }
}
