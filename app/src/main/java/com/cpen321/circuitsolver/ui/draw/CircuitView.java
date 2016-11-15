package com.cpen321.circuitsolver.ui.draw;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.cpen321.circuitsolver.model.SimplePoint;
import com.cpen321.circuitsolver.model.components.CircuitElm;

import java.util.concurrent.locks.ReentrantLock;


/**
 * Created by lotus on 14/11/16.
 */

public class CircuitView extends SurfaceView implements Runnable {
    Thread thread;
    SurfaceHolder holder;
    Paint paint;
    boolean run;

    public CircuitView(Context context, AttributeSet attrs) {
        super(context, attrs);
        thread = null;
        holder = getHolder();
        paint = new Paint();
        run = false;
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(10);
    }

    @Override
    public void run() {
        while(run) {
            if(!holder.getSurface().isValid()) {
                continue;
            }
            Canvas canvas = holder.lockCanvas();
            canvas.drawColor(Color.WHITE);
            ReentrantLock lock = DrawActivity.getCircuitElmsLock();
            lock.lock();
            paint.setColor(Color.BLACK);
            for(CircuitElm circuitElm : DrawActivity.getCircuitElms()) {
                SimplePoint start = circuitElm.getP1();
                SimplePoint end = circuitElm.getP2();
                canvas.drawLine(start.getX(), start.getY(), end.getX(), end.getY(), paint);
            }
            lock.unlock();
            CircuitElm selected = DrawActivity.getSelectedElm();
            if(selected != null) {
                SimplePoint start = selected.getP1();
                SimplePoint end = selected.getP2();
                paint.setColor(Color.RED);
                canvas.drawLine(start.getX(), start.getY(), end.getX(), end.getY(), paint);
            }
            AddComponentState state = DrawActivity.getComponentState();
            paint.setColor(Color.RED);
            int threshHold = 25;
            if(getDistance(DrawActivity.getStartX(), DrawActivity.getStartY(), DrawActivity.getEndX(), DrawActivity.getEndY()) > threshHold) {
                canvas.drawLine(DrawActivity.getStartX(), DrawActivity.getStartY(), DrawActivity.getEndX(), DrawActivity.getEndY(), paint);
            }
            holder.unlockCanvasAndPost(canvas);
        }
    }

    public void pause() {
        run = false;
        while(true) {
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
}