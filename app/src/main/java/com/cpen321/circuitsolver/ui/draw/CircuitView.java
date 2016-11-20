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
            if (this.zoomPoint != null)
                this.canvas.scale(this.scale, this.scale);
            canvas.drawColor(Color.WHITE);
            paint.setColor(Color.BLACK);
            //get component state
            AddComponentState state = DrawActivity.getComponentState();
            for (CircuitElm circuitElm : DrawActivity.getCircuitElms()) {
                SimplePoint start = circuitElm.getP1();
                SimplePoint end = circuitElm.getP2();
                drawCircuitElm(canvas, circuitElm.getType(), start.getX(), start.getY(), end.getX(), end.getY(), paint);
            }
            ;
            CircuitElm selected = DrawActivity.getSelectedElm();
            if (selected != null) {
                SimplePoint start = selected.getP1();
                SimplePoint end = selected.getP2();
                paint.setColor(Color.RED);
                drawCircuitElm(canvas, selected.getType(), start.getX(), start.getY(), end.getX(), end.getY(), paint);
            }
            //AddComponentState state = DrawActivity.getComponentState();
            paint.setColor(Color.RED);
            int threshHold = 40;
            if ((getDistance(DrawActivity.getStartX(), DrawActivity.getStartY(), DrawActivity.getEndX(), DrawActivity.getEndY()) > threshHold) && state != ERASE) {
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
        while (true && thread != null) {
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

    private void drawCircuitElm(Canvas canvas, String type, int startX, int startY, int stopX, int stopY, Paint paint) {
        //draw resistor
        if (type.equals(Constants.RESISTOR)) {
            drawResistor(canvas, startX, startY, stopX, stopY, paint);
        } else if (type.equals(Constants.DC_VOLTAGE)) {
            drawDCsource(canvas, startX, startY, stopX, stopY, paint);
            //canvas.drawLine(startX, startY, stopX, stopY, paint);
        } else {
            canvas.drawLine(startX, startY, stopX, stopY, paint);
        }
    }

    float getHypotenuse(float x, float y) {
        float hypotenuse = (float) Math.sqrt(Math.pow((double) y, 2.0) + Math.pow((double) x, 2.0));
        return hypotenuse;
    }

    private void drawResistor(Canvas canvas, float startX, float startY, float stopX, float stopY, Paint paint) {
        //Edit WIDTH and MAX_LENGTH as needed
        float WIDTH = 35;
        float MAX_LENGTH = 100;
        float x = stopX - startX;
        float y = stopY - startY;
        float slope = y / x;
        float b = stopY - slope * stopX;
        float hypotenuse = getHypotenuse(x, y);
        float d = (hypotenuse - MAX_LENGTH) / 2;
        float angle = (float) Math.atan(slope);
        float perpAngle = (float) Math.atan(x / y);
        float innerD = (hypotenuse - 2 * d) / 5;

        float x3, x4, x5, x6, x7, x8, y3, y4, y5, y6, y7, y8;
        float x5i, x6i, x7i, x8i, y3i, y4i, y5i, y6i, y7i, y8i;

        //when length of entire resistor is less than MAX_LENGTH
        if (hypotenuse < MAX_LENGTH) {
            MAX_LENGTH = MAX_LENGTH / 2;
            d = (hypotenuse - MAX_LENGTH) / 2;
            innerD = (hypotenuse - 2 * d) / 5;
        }
        //when drawn from left to right
        if (x > 0) {
            x3 = stopX - (MAX_LENGTH + d) * ((float) Math.cos(angle));
            y3 = stopY - (MAX_LENGTH + d) * ((float) Math.sin(angle));
            x4 = stopX - (d) * ((float) Math.cos(angle));
            y4 = stopY - (d) * ((float) Math.sin(angle));

            x5i = stopX - (MAX_LENGTH + d - innerD) * ((float) Math.cos(angle));
            x5 = x5i - WIDTH * ((float) Math.cos(perpAngle));
            y5i = stopY - (MAX_LENGTH + d - innerD) * ((float) Math.sin(angle));
            y5 = y5i + WIDTH * ((float) Math.sin(perpAngle));

            x6i = stopX - (MAX_LENGTH + d - 2 * innerD) * ((float) Math.cos(angle));
            x6 = x6i + WIDTH * ((float) Math.cos(perpAngle));
            y6i = stopY - (MAX_LENGTH + d - 2 * innerD) * ((float) Math.sin(angle));
            y6 = y6i - WIDTH * ((float) Math.sin(perpAngle));

            x7i = stopX - (MAX_LENGTH + d - 3 * innerD) * ((float) Math.cos(angle));
            x7 = x7i - WIDTH * ((float) Math.cos(perpAngle));
            y7i = stopY - (MAX_LENGTH + d - 3 * innerD) * ((float) Math.sin(angle));
            y7 = y7i + WIDTH * ((float) Math.sin(perpAngle));

            x8i = stopX - (MAX_LENGTH + d - 4 * innerD) * ((float) Math.cos(angle));
            x8 = x8i + WIDTH * ((float) Math.cos(perpAngle));
            y8i = stopY - (MAX_LENGTH + d - 4 * innerD) * ((float) Math.sin(angle));
            y8 = y8i - WIDTH * ((float) Math.sin(perpAngle));
        }
        //when drawn right to left
        else if (x < 0) {
            x3 = startX - (d) * ((float) Math.cos(angle));
            y3 = startY - (d) * ((float) Math.sin(angle));
            x4 = startX - (MAX_LENGTH + d) * ((float) Math.cos(angle));
            y4 = startY - (MAX_LENGTH + d) * ((float) Math.sin(angle));

            x5i = startX - (d + innerD) * ((float) Math.cos(angle));
            x5 = x5i - WIDTH * ((float) Math.cos(perpAngle));
            y5i = startY - (d + innerD) * ((float) Math.sin(angle));
            y5 = y5i + WIDTH * ((float) Math.sin(perpAngle));

            x6i = startX - (d + 2 * innerD) * ((float) Math.cos(angle));
            x6 = x6i + WIDTH * ((float) Math.cos(perpAngle));
            y6i = startY - (d + 2 * innerD) * ((float) Math.sin(angle));
            y6 = y6i - WIDTH * ((float) Math.sin(perpAngle));

            x7i = startX - (d + 3 * innerD) * ((float) Math.cos(angle));
            x7 = x7i - WIDTH * ((float) Math.cos(perpAngle));
            y7i = startY - (d + 3 * innerD) * ((float) Math.sin(angle));
            y7 = y7i + WIDTH * ((float) Math.sin(perpAngle));

            x8i = startX - (d + 4 * innerD) * ((float) Math.cos(angle));
            x8 = x8i + WIDTH * ((float) Math.cos(perpAngle));
            y8i = startY - (d + 4 * innerD) * ((float) Math.sin(angle));
            y8 = y8i - WIDTH * ((float) Math.sin(perpAngle));
        }
        //when drawn vertically pointing down
        else if (y > 0) {
            x3 = stopX;
            y3 = startY + d;
            x4 = stopX;
            y4 = stopY - d;
            y5 = startY + d + innerD;
            y6 = startY + d + 2 * innerD;
            y7 = startY + d + 3 * innerD;
            y8 = startY + d + 4 * innerD;
            x5 = startX - WIDTH;
            x6 = startX + WIDTH;
            x7 = startX - WIDTH;
            x8 = startX + WIDTH;
        }
        //when drawn vertically pointing up
        else {
            x3 = stopX;
            y3 = startY - d;
            x4 = stopX;
            y4 = stopY + d;
            y5 = startY - d - innerD;
            y6 = startY - d - 2 * innerD;
            y7 = startY - d - 3 * innerD;
            y8 = startY - d - 4 * innerD;
            x5 = startX - WIDTH;
            x6 = startX + WIDTH;
            x7 = startX - WIDTH;
            x8 = startX + WIDTH;
        }
        //draw wire section
        canvas.drawLine(startX, startY, x3, y3, paint);
        canvas.drawLine(x4, y4, stopX, stopY, paint);

        //draw zigzag part
        canvas.drawLine(x3, y3, x5, y5, paint);
        canvas.drawLine(x5, y5, x6, y6, paint);
        canvas.drawLine(x6, y6, x7, y7, paint);
        canvas.drawLine(x7, y7, x8, y8, paint);
        canvas.drawLine(x8, y8, x4, y4, paint);
    }

    private void drawDCsource(Canvas canvas, float startX, float startY, float stopX, float stopY, Paint paint) {
        //Edit ANODE_WIDTH, CATHODE_WIDTH, and SPACE_LENGTH as needed
        float ANODE_WIDTH = 47;
        float CATHODE_WIDTH = 25;
        float SPACE_LENGTH = 22;
        float x = stopX - startX;
        float y = stopY - startY;
        float slope = y / x;
        float b = stopY - slope * stopX;
        float hypotenuse = getHypotenuse(x, y);
        float d = (hypotenuse - SPACE_LENGTH) / 2;
        float angle = (float) Math.atan(slope);
        float perpAngle = (float) Math.atan(x / y);
        float innerD = (hypotenuse - 2 * d) / 5;

        float x3, x4, x5, x6, x7, x8, y3, y4, y5, y6, y7, y8;

        //when drawn from left to right
        if (x > 0) {
            x3 = stopX - (SPACE_LENGTH + d) * ((float) Math.cos(angle));
            y3 = stopY - (SPACE_LENGTH + d) * ((float) Math.sin(angle));
            x4 = stopX - (d) * ((float) Math.cos(angle));
            y4 = stopY - (d) * ((float) Math.sin(angle));

            x5 = x3 - ANODE_WIDTH * ((float) Math.cos(perpAngle));
            y5 = y3 + ANODE_WIDTH * ((float) Math.sin(perpAngle));

            x6 = x3 + ANODE_WIDTH * ((float) Math.cos(perpAngle));
            y6 = y3 - ANODE_WIDTH * ((float) Math.sin(perpAngle));

            x7 = x4 - CATHODE_WIDTH * ((float) Math.cos(perpAngle));
            y7 = y4 + CATHODE_WIDTH * ((float) Math.sin(perpAngle));

            x8 = x4 + CATHODE_WIDTH * ((float) Math.cos(perpAngle));
            y8 = y4 - CATHODE_WIDTH * ((float) Math.sin(perpAngle));
        }
        //when drawn right to left
        else if (x < 0) {
            x3 = startX - (d) * ((float) Math.cos(angle));
            y3 = startY - (d) * ((float) Math.sin(angle));
            x4 = startX - (SPACE_LENGTH + d) * ((float) Math.cos(angle));
            y4 = startY - (SPACE_LENGTH + d) * ((float) Math.sin(angle));

            x5 = x3 - ANODE_WIDTH * ((float) Math.cos(perpAngle));
            y5 = y3 + ANODE_WIDTH * ((float) Math.sin(perpAngle));

            x6 = x3 + ANODE_WIDTH * ((float) Math.cos(perpAngle));
            y6 = y3 - ANODE_WIDTH * ((float) Math.sin(perpAngle));

            x7 = x4 - CATHODE_WIDTH * ((float) Math.cos(perpAngle));
            y7 = y4 + CATHODE_WIDTH * ((float) Math.sin(perpAngle));

            x8 = x4 + CATHODE_WIDTH * ((float) Math.cos(perpAngle));
            y8 = y4 - CATHODE_WIDTH * ((float) Math.sin(perpAngle));
        }
        //when drawn vertically pointing down
        else if (y > 0) {
            x3 = stopX;
            y3 = startY + d;
            x4 = stopX;
            y4 = stopY - d;
            y5 = y3;
            y6 = y3;
            y7 = y4;
            y8 = y4;
            x5 = startX + ANODE_WIDTH;
            x6 = startX - ANODE_WIDTH;
            x7 = startX + CATHODE_WIDTH;
            x8 = startX - CATHODE_WIDTH;
        }
        //when drawn vertically pointing up
        else {
            x3 = stopX;
            y3 = startY - d;
            x4 = stopX;
            y4 = stopY + d;
            y5 = y3;
            y6 = y3;
            y7 = y4;
            y8 = y4;
            x5 = startX + ANODE_WIDTH;
            x6 = startX - ANODE_WIDTH;
            x7 = startX + CATHODE_WIDTH;
            x8 = startX - CATHODE_WIDTH;
        }

        //draw wire section
        canvas.drawLine(startX, startY, x3, y3, paint);
        canvas.drawLine(x4, y4, stopX, stopY, paint);
        //draw perpendicular lines part
        canvas.drawLine(x3, y3, x5, y5, paint);
        canvas.drawLine(x3, y3, x6, y6, paint);
        canvas.drawLine(x4, y4, x7, y7, paint);
        canvas.drawLine(x4, y4, x8, y8, paint);
    }
}