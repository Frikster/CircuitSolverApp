package com.cpen321.circuitsolver.ui.draw;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

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
        paint.setStrokeWidth(6f);
    }

    @Override
    public void run() {
        while(run) {
            if(!holder.getSurface().isValid()) {
                continue;
            }
            Canvas canvas = holder.lockCanvas();
            canvas.drawColor(Color.WHITE);
            canvas.drawCircle(DrawActivity.getX(), DrawActivity.getY(), 20,paint);
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