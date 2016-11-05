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

    @Override
    public void onDraw(Canvas canvas, Paint paint, int yDisp) {
        int startX = this.getPoint(0).getX();
        int startY = this.getPoint(0).getY();
        int endX = this.getPoint(1).getX();
        int endY = this.getPoint(1).getY();

//        if (startY > endY) {
//            canvas.drawLine(startX, startY, endX, startY, paint);
//            canvas.drawLine(startX, endY, startX, startY, paint);
//        } else{
            canvas.drawLine(startX, startY, endX, startY, paint);
            canvas.drawLine(endX, endY, endX, startY, paint);
//        }
    }

    public boolean isWire(){
        return true;
    }
}
