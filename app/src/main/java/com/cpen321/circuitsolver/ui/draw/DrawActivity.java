package com.cpen321.circuitsolver.ui.draw;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.cpen321.circuitsolver.R;
import com.cpen321.circuitsolver.model.SimplePoint;
import com.cpen321.circuitsolver.model.components.CircuitElm;
import com.cpen321.circuitsolver.model.components.ResistorElm;
import com.cpen321.circuitsolver.model.components.VoltageElm;
import com.cpen321.circuitsolver.model.components.WireElm;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.locks.ReentrantLock;

import static com.cpen321.circuitsolver.ui.draw.AddComponentState.*;
import static com.cpen321.circuitsolver.ui.draw.TouchState.UP;

public class DrawActivity extends AppCompatActivity implements View.OnTouchListener {

    private Button componentMenuButton;
    private Button eraseButton;
    private Button selectButton;
    private TextView unitsText;
    private EditText componentValueText;


    private static ArrayList<CircuitElm> circuitElms = new ArrayList<CircuitElm>();

    private static final ReentrantLock lock = new ReentrantLock();
    ;
    private CircuitView circuitView;

    private static AddComponentState componentState = DC_SOURCE;

    private TouchState touchState = UP;
    private static int startX = 0;
    private static int startY = 0;
    private static int endX = 0;
    private static int endY = 0;

    public static ReentrantLock getLock() {
        return lock;
    }

    public static ArrayList<CircuitElm> getCircuitElms() {
        return circuitElms;
    }

    public static AddComponentState getComponentState() {
        return componentState;
    }

    public TouchState getTouchState() {
        return touchState;
    }

    public static int getEndY() {
        return endY;
    }

    public static int getStartX() {
        return startX;
    }

    public static int getStartY() {
        return startY;
    }

    public static int getEndX() {
        return endX;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);
        circuitView = (CircuitView) findViewById(R.id.circuitFrame);
        componentMenuButton = (Button) findViewById(R.id.componentMenuButton);
        eraseButton = (Button) findViewById(R.id.eraseButton);
        selectButton = (Button) findViewById(R.id.selectButton);
        circuitView.setOnTouchListener(this);
        unitsText= (TextView) findViewById(R.id.units_display);
        componentValueText = (EditText) findViewById(R.id.component_value);

        componentMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Creating the instance of PopupMenu
                PopupMenu popup = new PopupMenu(DrawActivity.this, componentMenuButton);
                //Inflating the Popup using xml file
                popup.getMenuInflater()
                        .inflate(R.menu.popup_menu, popup.getMenu());

                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        Toast.makeText(
                                DrawActivity.this,
                                "You Clicked : " + item.getTitle(),
                                Toast.LENGTH_SHORT
                        ).show();
                        resetCoordinates();
                        switch (item.getItemId()) {
                            case R.id.dropDownSourceButton:
                                componentState = DC_SOURCE;
                                break;
                            case R.id.dropDownResistorButton:
                                componentState = RESISTOR;
                                break;
                            case R.id.dropDownWireButton:
                                componentState = WIRE;
                                break;
                            default:
                                break;
                        }
                        return true;
                    }
                });
                popup.show(); //showing popup menu
            }
        }); //closing the setOnClickListener method

        eraseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                componentState = ERASE;
            }
        });

        selectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                componentState = SELECT;
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        circuitView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        circuitView.resume();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();

        if (componentState == ERASE) {
            lock.lock();
            CircuitElm toRemove = getSelected(x, y);
            if(toRemove != null) {
                circuitElms.remove(toRemove);
            }
            lock.unlock();
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = x;
                startY = y;
                endX = x;
                endY = y;
                Log.i("TAG", "touched down");
                CharSequence text = "Touched (" + x + "," + y + ")";
                break;
            case MotionEvent.ACTION_MOVE:
                endX = x;
                endY = y;
                Log.i("TAG", "moving: (" + x + ", " + y + ")");
                break;
            case MotionEvent.ACTION_UP:
                endX = x;
                endY = y;
                for (CircuitElm circuitElm : circuitElms) {
                    //check to see if the new points we are drawing are near existing ones, if so connect them
                    int threshHold = 50;
                    SimplePoint p1 = circuitElm.getP1();
                    SimplePoint p2 = circuitElm.getP2();
                    int p1X = p1.getX();
                    int p1Y = p1.getY();
                    int p2X = p2.getX();
                    int p2Y = p2.getY();
                    if (getDistance(startX, startY, p1X, p1Y) < threshHold) {
                        startX = p1X;
                        startY = p1Y;
                    }
                    if (getDistance(startX, startY, p2X, p2Y) < threshHold) {
                        startX = p2X;
                        startY = p2Y;
                    }
                    if (getDistance(endX, endY, p1X, p1Y) < threshHold) {
                        endX = p1X;
                        endY = p1Y;
                    }
                    if (getDistance(endX, endY, p2X, p2Y) < threshHold) {
                        endX = p2X;
                        endY = p2Y;
                    }
                }
                int lengthThreshHold = 35;
                int length = getDistance(startX, startY, endX, endY);
                //don't create a circuit element if it is too short
                if(length > lengthThreshHold) {
                    SimplePoint startPoint = new SimplePoint(startX, startY);
                    SimplePoint endPoint = new SimplePoint(endX, endY);
                    lock.lock();
                    switch (componentState) {
                        case DC_SOURCE:
                            circuitElms.add(new VoltageElm(startPoint, endPoint, 10));
                            break;
                        case RESISTOR:
                            circuitElms.add(new ResistorElm(startPoint, endPoint, 10));
                            break;
                        case WIRE:
                            circuitElms.add(new WireElm(startPoint, endPoint));
                            break;
                        default:
                            break;
                    }
                    lock.unlock();
                }
                Log.i("TAG", "touched up");
                resetCoordinates();
                break;
        }
        return true;
    }

    private int getDistance(int x1, int y1, int x2, int y2) {
        return (int) Math.hypot(x1 - x2, y1 - y2);
    }

    private boolean isOnElement(int x, int y, CircuitElm e) {
        int threshHold = 10;
        int eStartX = e.getP1().getX();
        int eStartY = e.getP1().getY();
        int eEndX = e.getP2().getX();
        int eEndY = e.getP2().getY();
        if (Math.abs(getDistance(eStartX, eStartY, x, y) + getDistance(x, y, eEndX, eEndY) - getDistance(eStartX, eStartY, eEndX, eEndY)) < 40) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Get the circuit element which passes point x,y
     * @param x coordinate
     * @param y coordinate
     * @return the circuitElm touched, or null if non were touched
     */
    private CircuitElm getSelected(int x, int y) {
        Iterator<CircuitElm> iter = circuitElms.iterator();
        while (iter.hasNext()) {
            CircuitElm circuitElm = iter.next();
            if (isOnElement(x, y, circuitElm)) {
                return circuitElm;
            }
        }
        return null;
    }

    private void resetCoordinates() {
        startX = 0;
        startY = 0;
        endX = 0;
        endY = 0;
    }



}
