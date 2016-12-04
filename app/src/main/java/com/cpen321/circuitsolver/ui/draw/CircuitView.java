package com.cpen321.circuitsolver.ui.draw;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.cpen321.circuitsolver.R;
import com.cpen321.circuitsolver.model.SimplePoint;
import com.cpen321.circuitsolver.model.components.CircuitElm;
import com.cpen321.circuitsolver.util.Constants;

import java.util.ArrayList;
import java.util.List;

import static com.cpen321.circuitsolver.ui.draw.AddComponentState.SOLVED;


/**
 * Created by lotus on 14/11/16.
 */

public class CircuitView extends SurfaceView implements Runnable {
    private Thread thread;
    private SurfaceHolder holder;
    private Paint paint;
    private boolean run;

    private Canvas canvas;
    public float scale;
    public Point zoomPoint;
    private final int color;

    private List<Point> points = new ArrayList<>();

    public CircuitView(Context context, AttributeSet attrs) {
        super(context, attrs);
        thread = null;
        holder = getHolder();
        paint = new Paint();
        run = false;
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(7);
        this.scale = 1;
//        TypedValue typedValue = new  TypedValue();
//        getContext().getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
        color = Color.rgb(217, 44, 44);
    }

    @Override
    public void run() {
        while (run) {
            if (!holder.getSurface().isValid()) {
                continue;
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            this.canvas = holder.lockCanvas();
            this.fakeDraw(this.canvas);
            holder.unlockCanvasAndPost(canvas);
        }
    }

    public void fakeDraw(Canvas canvas) {
        if (this.zoomPoint != null)
            canvas.scale(this.scale, this.scale, this.getWidth()/2, this.getHeight()/2);
        canvas.drawColor(Color.WHITE);
        paint.setColor(Color.DKGRAY);
        //get component state
        AddComponentState state = DrawActivity.getComponentState();
        for (CircuitElm circuitElm : DrawActivity.getCircuitElms()) {
            SimplePoint start = circuitElm.getP1();
            SimplePoint end = circuitElm.getP2();
            circuitElm.draw(canvas, start.getX(), start.getY(), end.getX(), end.getY(), paint);
        }
        CircuitElm selected = DrawActivity.getSelectedElm();
        if (selected != null) {
            SimplePoint start = selected.getP1();
            SimplePoint end = selected.getP2();
            paint.setColor(color);
            selected.draw(canvas, start.getX(), start.getY(), end.getX(), end.getY(), paint);
            if(state == SOLVED) {
                paint.setColor(color);
                selected.drawCurrent(canvas, paint);
            }
        }
        //AddComponentState state = DrawActivity.getComponentState();
        paint.setColor(color);
        CircuitElm candidate = DrawActivity.getCandidateElement();
        if (candidate != null && !DrawActivity.isZooming()) {
            String type = convertStateToType(DrawActivity.getComponentState());
            candidate.draw(canvas, DrawActivity.getStartX(), DrawActivity.getStartY(), DrawActivity.getEndX(), DrawActivity.getEndY(), paint);
        }
    }

    public void control(DrawController controller) {
        this.scale = (controller.getZoomScale() + this.scale) / 2f;
        this.zoomPoint = controller.getMiddlePoint();
    }

    public void pause() {
        run = false;
        while (thread != null) {
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

    //this is just a terrible workaround cause no time to change old code
    private String convertStateToType(AddComponentState state) {
        switch (state) {
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

}