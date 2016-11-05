package com.cpen321.circuitsolver.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.method.TextKeyListener;
import android.view.View;

import com.cpen321.circuitsolver.R;
import com.cpen321.circuitsolver.model.SimplePoint;
import com.cpen321.circuitsolver.model.components.CapacitorElm;
import com.cpen321.circuitsolver.model.components.CircuitElm;
import com.cpen321.circuitsolver.model.components.InductorElm;
import com.cpen321.circuitsolver.model.components.ResistorElm;
import com.cpen321.circuitsolver.model.components.VoltageElm;
import com.cpen321.circuitsolver.model.components.WireElm;
import com.cpen321.circuitsolver.opencv.Component;
import com.cpen321.circuitsolver.util.CircuitProject;

import java.util.ArrayList;

/**
 * Created by Neil Goossen on 2016-10-15.
 */

public class CircuitDisplay extends View {
    private Paint textPaint;
    private Paint circuitPaint;
    private RectF rectF;

    private Resources res = getResources();
    private int tmpColor = res.getColor(R.color.circuitBackground);

    private CircuitProject circuitProject;
    private ArrayList<CircuitElm> components = new ArrayList<>();

    public CircuitDisplay(Context context) {
        super(context);
        this.circuitPaint = new Paint();
        this.circuitPaint.setColor(Color.BLACK);
        this.rectF = new RectF(200, 100, 300, 200);
        this.circuitPaint.setStrokeWidth(2.5f);
        this.init();
    }
    public CircuitDisplay(Context context, CircuitProject project) {
        super(context);
        this.circuitProject = project;
        this.circuitPaint = new Paint();
        this.circuitPaint.setColor(Color.BLACK);
        this.circuitPaint.setStrokeWidth(2.5f);
        this.rectF = new RectF(200, 100, 300, 200);
    }

    private void init() {
        this.components.add(new InductorElm(new SimplePoint(300, 300),
                new SimplePoint(500, 300), 5));
        this.components.add(new WireElm(new SimplePoint(500, 300),
                new SimplePoint(700, 500)));
        this.components.add(new CapacitorElm(new SimplePoint(700, 500),
                new SimplePoint(700, 700), 5));
        this.components.add(new WireElm(new SimplePoint(500, 900), new SimplePoint(700, 700)));
        this.components.add(new ResistorElm(new SimplePoint(500, 900),
                new SimplePoint(300, 900), 5));
        this.components.add(new WireElm(new SimplePoint(300, 900),
                new SimplePoint(300, 700)));
        this.components.add(new VoltageElm(new SimplePoint(300, 700),
                new SimplePoint(300, 500), 5));
        this.components.add(new WireElm(new SimplePoint(300, 500),
                new SimplePoint(300, 300)));
    }

    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(this.tmpColor);
        for (CircuitElm circuitElm : this.components) {
            circuitElm.onDraw(canvas, this.circuitPaint, 50);
        }

    }
}
