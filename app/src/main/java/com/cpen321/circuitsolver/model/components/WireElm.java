package com.cpen321.circuitsolver.model.components;


import android.graphics.Canvas;
import android.graphics.Paint;

import com.cpen321.circuitsolver.model.SimplePoint;
import com.cpen321.circuitsolver.util.Constants;

/**
 * Created by Jennifer on 10/12/2016.
 */
public class WireElm extends VoltageElm {

    //We want NgSpice to treat wires as 0V voltage sources in order to get the current going through the wire
    public WireElm() {
        super();
        super.setValue(0);
    }

    public WireElm(SimplePoint p1, SimplePoint p2) {
        super(p1, p2);
    }

    public double getVoltageDiff() {
        //TODO: implement this method
        return 0;
    }

    public double calculateCurrent() {
        return getCurrent();
    }

    public void setValue(double value) {
        //TODO: throw an IllegalEditWireValueException
    }

    @Override
    public String getType() {
        return Constants.WIRE;
    }

    @Override
    public  void draw(Canvas canvas, float startX, float startY, float stopX, float stopY, Paint paint) {
        canvas.drawLine(startX, startY, stopX, stopY, paint);
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
