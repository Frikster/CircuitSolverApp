package com.cpen321.circuitsolver.ui.draw;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.cpen321.circuitsolver.model.SimplePoint;
import com.cpen321.circuitsolver.model.components.CircuitElm;
import com.cpen321.circuitsolver.util.Constants;

import java.util.concurrent.locks.ReentrantLock;

import static com.cpen321.circuitsolver.ui.draw.AddComponentState.ERASE;
import static com.cpen321.circuitsolver.ui.draw.AddComponentState.DC_SOURCE;
import static com.cpen321.circuitsolver.ui.draw.AddComponentState.RESISTOR;


/**
 * Created by lotus on 14/11/16.
 */

public class CircuitView extends SurfaceView implements Runnable {
    Thread thread;
    SurfaceHolder holder;
    Paint paint;
    boolean run;

    private Canvas canvas;
    public float scale;
    public Point zoomPoint;

    public CircuitView(Context context, AttributeSet attrs) {
        super(context, attrs);
        thread = null;
        holder = getHolder();
        paint = new Paint();
        run = false;
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(10);

        this.scale = 1;
    }

    @Override
    public void run() {
        while(run) {
            if(!holder.getSurface().isValid()) {
                continue;
            }
            try {
                Thread.sleep(50);
            } catch(InterruptedException e) {
                e.printStackTrace();
            }
            this.canvas = holder.lockCanvas();
            if (this.zoomPoint != null)
                this.canvas.scale(this.scale, this.scale);
            canvas.drawColor(Color.WHITE);
            paint.setColor(Color.BLACK);
            //get component state
            AddComponentState state = DrawActivity.getComponentState();
            for(CircuitElm circuitElm : DrawActivity.getCircuitElms()) {
                SimplePoint start = circuitElm.getP1();
                SimplePoint end = circuitElm.getP2();
                drawCircuitElm(canvas, circuitElm.getType(), start.getX(), start.getY(), end.getX(), end.getY(), paint);
            };
            CircuitElm selected = DrawActivity.getSelectedElm();
            if(selected != null) {
                SimplePoint start = selected.getP1();
                SimplePoint end = selected.getP2();
                paint.setColor(Color.RED);
                drawCircuitElm(canvas, selected.getType(), start.getX(), start.getY(), end.getX(), end.getY(), paint);
            }
            //AddComponentState state = DrawActivity.getComponentState();
            paint.setColor(Color.RED);
            int threshHold = 40;
            if((getDistance(DrawActivity.getStartX(), DrawActivity.getStartY(), DrawActivity.getEndX(), DrawActivity.getEndY()) > threshHold) && state != ERASE) {
                String type = convertStateToType(DrawActivity.getComponentState());
                drawCircuitElm(canvas, type, DrawActivity.getStartX(), DrawActivity.getStartY(), DrawActivity.getEndX(), DrawActivity.getEndY(), paint);
            }
            holder.unlockCanvasAndPost(canvas);
        }
    }

    public void control(DrawController controller) {
        this.scale = (controller.getZoomScale() + this.scale) / 2f;
        this.zoomPoint = controller.getMiddlePoint();
    }

    public void pause() {
        run = false;
        while(true && thread != null) {
            try {
                thread.join();
                break;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        thread = null;
    }

    public void resume() {
        run = true;
        thread = new Thread(this);
        thread.start();
    }

    private int getDistance(int x1, int y1, int x2, int y2) {
        return (int) Math.hypot(x1 - x2, y1 - y2);
    }

    //this is just a terrible workaround cause no time to change old code
    private String convertStateToType(AddComponentState state) {
        switch(state) {
            case DC_SOURCE:
                return Constants.DC_VOLTAGE;
            case RESISTOR:
                return Constants.RESISTOR;
            case WIRE:
                return Constants.WIRE;
            default:
                return Constants.WIRE;
        }
    }

    private void drawCircuitElm(Canvas canvas, String type, int startX, int startY, int stopX, int stopY, Paint paint) {
        //draw resistor
        if(type.equals(Constants.RESISTOR)) {
            drawResistor(canvas, startX, startY, stopX, stopY, paint);
        }else if(type.equals(Constants.DC_VOLTAGE)){
            canvas.drawLine(startX, startY, stopX, stopY, paint);
        }else{
            canvas.drawLine(startX, startY, stopX, stopY, paint);
        }
    }

    //TODO move draw methods into the circuitElm classes themselves

    private void drawResistor(Canvas canvas, float startX, float startY, float stopX, float stopY, Paint paint){
        //simple lines sections
        float p1X = startX + (float)0.25*(stopX - startX);
        float p6X = startX + (float)0.75*(stopX - startX);
        float p1Y = startY + (float)0.25*(stopY - startY);
        float p6Y = startY + (float)0.75*(stopY - startY);
        //zigzag sections
        float p2X, p3X, p4X, p5X, p2Y, p3Y, p4Y, p5Y;

        float width;
        //if Y-difference is greater or equal to than X-difference
        if(Math.abs(startY-stopY)>= Math.abs(startX-stopX)){
            width = Math.abs(startY-stopY)/2;
            p2X = startX + (float)0.35*(stopX - startX) + (width/2);
            p3X = startX + (float)0.45*(stopX - startX) - (width/2);
            p4X = startX + (float)0.55*(stopX - startX) + (width/2);
            p5X = startX + (float)0.65*(stopX - startX) - (width/2);

            p2Y = startY + (float)0.35*(stopY - startY);
            p3Y = startY + (float)0.45*(stopY - startY);
            p4Y = startY + (float)0.55*(stopY - startY);
            p5Y = startY + (float)0.65*(stopY - startY);
        }
        //if X-difference is greater than Y-difference
        else{
            width = Math.abs(startX-stopX)/2;
            p2X = startX + (float)0.35*(stopX - startX);
            p3X = startX + (float)0.45*(stopX - startX);
            p4X = startX + (float)0.55*(stopX - startX);
            p5X = startX + (float)0.65*(stopX - startX);

            p2Y = startY + (float)0.35*(stopY - startY) + (width/2);
            p3Y = startY + (float)0.45*(stopY - startY) - (width/2);
            p4Y = startY + (float)0.55*(stopY - startY) + (width/2);
            p5Y = startY + (float)0.65*(stopY - startY) - (width/2);
        }

        canvas.drawLine(startX, startY, p1X, p1Y, paint);

        canvas.drawLine(p1X, p1Y, p2X, p2Y, paint);
        canvas.drawLine(p2X, p2Y, p3X, p3Y, paint);
        canvas.drawLine(p3X, p3Y, p4X, p4Y, paint);
        canvas.drawLine(p4X, p4Y, p5X, p5Y, paint);
        canvas.drawLine(p5X, p5Y, p6X, p6Y, paint);

        canvas.drawLine(p6X, p6Y, stopX, stopY, paint);
    }
}