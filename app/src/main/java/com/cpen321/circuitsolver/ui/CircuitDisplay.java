package com.cpen321.circuitsolver.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.view.View;

import com.cpen321.circuitsolver.R;

/**
 * Created by Neil Goossen on 2016-10-15.
 */

public class CircuitDisplay extends View {
    private Paint textPaint;
    private Paint circuitPaint;

    private Resources res = getResources();
    private int tmpColor = res.getColor(R.color.circuitBackground);

    public CircuitDisplay(Context context) {
        super(context);
        this.circuitPaint = new Paint();
        this.circuitPaint.setColor(Color.BLACK);
        this.circuitPaint.setStrokeWidth(2.5f);
    }

    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(this.tmpColor);

        canvas.drawCircle(200f, 200f, 100, this.circuitPaint);
    }


    private void drawResistor(Canvas canvas, Point start, Point end, boolean horizontal) {




    }

}
