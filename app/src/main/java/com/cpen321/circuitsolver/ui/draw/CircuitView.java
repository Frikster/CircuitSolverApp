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

import static com.cpen321.circuitsolver.ui.draw.AddComponentState.ERASE;

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
            ReentrantLock lock = DrawActivity.getLock();
            lock.lock();
            for(CircuitElm circuitElm : DrawActivity.getCircuitElms()) {
                SimplePoint start = circuitElm.getP1();
                SimplePoint end = circuitElm.getP2();
                canvas.drawLine(start.getX(), start.getY(), end.getX(), end.getY(), paint);
            }
            lock.unlock();
            if(DrawActivity.getComponentState() != ERASE) {
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
}