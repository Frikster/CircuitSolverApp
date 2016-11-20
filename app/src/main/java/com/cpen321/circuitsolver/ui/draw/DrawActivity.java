package com.cpen321.circuitsolver.ui.draw;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
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
import com.cpen321.circuitsolver.model.ResetComponents;
import com.cpen321.circuitsolver.model.SimplePoint;
import com.cpen321.circuitsolver.model.components.CircuitElm;
import com.cpen321.circuitsolver.model.components.ResistorElm;
import com.cpen321.circuitsolver.model.components.VoltageElm;
import com.cpen321.circuitsolver.model.components.WireElm;
import com.cpen321.circuitsolver.ngspice.NgSpice;
import com.cpen321.circuitsolver.ngspice.SpiceInterfacer;
import com.cpen321.circuitsolver.service.AnalyzeCircuitImpl;
import com.cpen321.circuitsolver.ui.EditActivity;
import com.cpen321.circuitsolver.ui.HomeActivity;
import com.cpen321.circuitsolver.util.CircuitProject;
import com.cpen321.circuitsolver.util.Constants;
import com.cpen321.circuitsolver.util.ImageUtils;

import org.w3c.dom.Text;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantLock;

import static com.cpen321.circuitsolver.ui.draw.AddComponentState.*;
import static com.cpen321.circuitsolver.ui.draw.TouchState.UP;

public class DrawActivity extends AppCompatActivity implements View.OnTouchListener {
    public static final String TAG ="DrawActivity";

    private Button componentMenuButton;
    private Button eraseButton;
    private Button solveButton;
    private Button moveModeButton;
    private TextView unitsText;
    private TextView voltageText;
    private TextView currentText;
    private EditText componentValueText;
    private CircuitProject circuitProject;
    private CircuitDefParser parser = new CircuitDefParser();

    private DrawController drawController;

    private boolean moveMode = false;
    private boolean firstZoom = true;

    private static ArrayList<CircuitElm> circuitElms = new ArrayList<CircuitElm>();

    private static final ReentrantLock circuitElmsLock = new ReentrantLock();

    private CircuitView circuitView;

    private static AddComponentState componentState = DC_SOURCE;

    private TouchState touchState = UP;
    private static int startX = 0;
    private static int startY = 0;
    private static int endX = 0;
    private static int endY = 0;
    private static int eraserX = 0;
    private static int eraserY = 0;

    private static CircuitElm selectedElm = null;

    public static ReentrantLock getCircuitElmsLock() {
        return circuitElmsLock;
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

    private AddComponentState prevComponentState = DC_SOURCE;

    /**
     * Get selected element
     * @return selected element, or null
     */
    public static CircuitElm getSelectedElm() {
        return selectedElm;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);
        circuitView = (CircuitView) findViewById(R.id.circuitFrame);
        componentMenuButton = (Button) findViewById(R.id.componentMenuButton);
        eraseButton = (Button) findViewById(R.id.eraseButton);
        moveModeButton = (Button) findViewById(R.id.moveButton);
        solveButton = (Button) findViewById(R.id.solveButton);
        voltageText = (TextView) findViewById(R.id.voltageText);
        currentText = (TextView) findViewById(R.id.currentText);
        circuitView.setOnTouchListener(this);
        unitsText= (TextView) findViewById(R.id.units_display);
        componentValueText = (EditText) findViewById(R.id.component_value);
        this.drawController = new DrawController();

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        screenHeight = displaymetrics.heightPixels;
        screenWidth = displaymetrics.widthPixels;

        //For loading previous circuit or new openCV interpreted circuit
        Bundle extras = getIntent().getExtras();
        String dataLocation = null;
        if(extras != null){
            dataLocation = (String) extras.get(Constants.CIRCUIT_PROJECT_FOLDER);
        }
        if(dataLocation != null){
            File circuitFolder = new File(dataLocation);
            this.circuitProject = new CircuitProject(circuitFolder);
            if(dataLocation.contains("example")){
                generateExampleCircuitElms();
            }
            else{

                try {
                    String circStr = circuitProject.getCircuitText();
                    int scaleToX = screenWidth;
                    int scaleToY = screenHeight;
                    circuitElms.clear();
                    circuitElms.addAll(parser.parseElements(circStr, scaleToX, scaleToY));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        else{
            System.out.println("We should never get here");
        }

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

        this.moveModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moveMode = !moveMode;
            }
        });

        eraseButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                eraserX = (int) (event.getRawX() - location[0]);
                eraserY =  (int) (event.getRawY() - location[1]);
                if(selectedElm != null) {
                    if(componentState == SOLVED) {
                        componentState = prevComponentState;
                    }
                    circuitElmsLock.lock();
                    circuitElms.remove(selectedElm);
                    selectedElm = null;
                    circuitElmsLock.unlock();
                }
                CircuitElm toRemove = getSelected(eraserX,eraserY);
                if(toRemove != null) {
                    if(componentState == SOLVED) {
                        componentState = prevComponentState;
                    }
                    circuitElmsLock.lock();
                    circuitElms.remove(toRemove);
                    circuitElmsLock.unlock();
                }
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        Log.i(TAG, "Erase touched down");
                        break;
                    case MotionEvent.ACTION_MOVE:
                        Log.i(TAG, "Erase moving: (" + eraserX + ", " + eraserY + ")");
                        break;
                    case MotionEvent.ACTION_UP:
                        Log.i(TAG, "Erase touched up");
                        break;
                }
                return true;
            }
        });

        solveButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(componentState != SOLVED) {
                    prevComponentState = componentState;
                }
                componentState = SOLVED;
                ResetComponents.resetNumComponents();
                AnalyzeCircuitImpl circuit = new AnalyzeCircuitImpl(circuitElms);
                circuit.init();
                SpiceInterfacer interfacer = new SpiceInterfacer(circuit.getNodes(), circuit.getElements());
                interfacer.solveCircuit(NgSpice.getInstance(DrawActivity.this));
                displayElementInfo();
                return true;
            }
        });

        componentValueText.setTag("not null");
        componentValueText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (selectedElm == null)
                    return;

                String newValue = componentValueText.getText().toString();

                if (newValue.isEmpty())
                    return;

                if (newValue.equals("--")) {
                    return;
                }

                selectedElm.setValue(Double.valueOf(newValue));

                if(componentState == SOLVED && componentValueText.getTag() != null) {
                    componentState = prevComponentState;
                }

                componentValueText.setTag("not null");
            }
        });
    }


    @Override
    public void onBackPressed(){

        // Convert list of elements to a circuit def file to save
        String circStr = parser.elementsToTxt(circuitElms, screenHeight, screenWidth);
        circuitProject.saveCircuitDefinitionFile(circStr);

        Intent backToHomeIntent = new Intent(this, HomeActivity.class);
        startActivity(backToHomeIntent);
        finish();
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

    private int[] location = new int[2];


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (this.moveMode) {
            if (event.getPointerCount() == 2)
            {
                SimplePoint fingerOne = new SimplePoint((int) event.getX(0), (int) event.getY(0));
                SimplePoint fingerTwo = new SimplePoint((int) event.getX(1),(int)  event.getY(1));
                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE: {
                        if (this.firstZoom) {
                            this.firstZoom = false;
                            this.drawController.setStartFingerOne(fingerOne);
                            this.drawController.setStartFingerTwo(fingerTwo);
                        } else {
                            this.drawController.setFingerOne(fingerOne);
                            this.drawController.setFingerTwo(fingerTwo);
                        }
                        break;
                    }
                    case MotionEvent.ACTION_POINTER_UP: {
                        this.firstZoom = true;
                    }
                    default: {
                        this.drawController.setFingerOne(fingerOne);
                        this.drawController.setFingerTwo(fingerTwo);
                    }
                }
                this.circuitView.control(this.drawController);
            } else if (event.getPointerCount() == 1) {
//                this.drawController.setFingerOne(fingerOne);
            }
            return true;
        } else {

            int x;
            int y;
            int truncateBits = 5;
            v.getLocationOnScreen(location);
            x = (int) ((event.getRawX() - location[0]) / this.circuitView.scale);
            y = (int) ((event.getRawY() - location[1]) / this.circuitView.scale);
//            if (this.circuitView.zoomPoint != null){
//                x -= this.circuitView.zoomPoint.x;
//                y -= this.circuitView.zoomPoint.y;
//            }
            x = (x >> truncateBits) << truncateBits;
            y = (y >> truncateBits) << truncateBits;
            int lengthThreshHold = 40;
            Log.d(TAG, "State: " + componentState.toString());
            Log.d(TAG, "Prev State: " + prevComponentState.toString());

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    //circuitView.resume();

                    selectedElm = getSelected(x, y);
                    startX = x;
                    startY = y;
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
                        } else if (getDistance(startX, startY, p2X, p2Y) < threshHold) {
                            startX = p2X;
                            startY = p2Y;
                        }
                    }
                    Log.i(TAG, "touched down");
                    CharSequence text = "Touched (" + x + "," + y + ")";
                    break;
                case MotionEvent.ACTION_MOVE:
                    endX = x;
                    endY = y;
                    if (getDistance(startX, startY, endX, endY) > lengthThreshHold) {
                        circuitElmsLock.lock();
                        selectedElm = null;
                        circuitElmsLock.unlock();
                    }
                    Log.i(TAG, "moving: (" + x + ", " + y + ")");
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
                        if (getDistance(endX, endY, p1X, p1Y) < threshHold) {
                            endX = p1X;
                            endY = p1Y;
                        } else if (getDistance(endX, endY, p2X, p2Y) < threshHold) {
                            endX = p2X;
                            endY = p2Y;
                        }
                    }
                    int length = getDistance(startX, startY, endX, endY);
                    //don't create a circuit element if it is too short
                    if (length > lengthThreshHold && componentState != ERASE) {
                        SimplePoint startPoint = new SimplePoint(startX, startY);
                        SimplePoint endPoint = new SimplePoint(endX, endY);
                        CircuitElm elm = null;
                        if(componentState == SOLVED) {
                            componentState = prevComponentState;
                        }
                        switch (componentState) {
                            case DC_SOURCE:
                                elm = new VoltageElm(startPoint, endPoint, 10);
                                break;
                            case RESISTOR:
                                elm = new ResistorElm(startPoint, endPoint, 10);
                                break;
                            case WIRE:
                                elm = new WireElm(startPoint, endPoint);
                                break;
                            default:
                                elm = new WireElm(startPoint, endPoint);
                                break;
                        }
                        selectedElm = elm;
                        circuitElmsLock.lock();
                        circuitElms.add(elm);
                        circuitElmsLock.unlock();
                    }
                    Log.i(TAG, "touched up");
                    resetCoordinates();
                    //circuitView.pause();

                    break;
            }

            displayElementInfo();
            return true;

        }
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
        if (Math.abs(getDistance(eStartX, eStartY, x, y) + getDistance(x, y, eEndX, eEndY) - getDistance(eStartX, eStartY, eEndX, eEndY)) < threshHold) {
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

    private void displayElementInfo() {
        if (selectedElm == null) {
            unitsText.setText(Constants.NOTHING_SELECTED);
            componentValueText.setText("--");
            voltageText.setText("--");
            currentText.setText("--");
        } else {
            componentValueText.setTag(null); //Used to distinguish between whether editText was changed by user, or pragmatically
            componentValueText.setText(Double.toString(selectedElm.getValue()));
            switch (selectedElm.getType()) {
                case Constants.CAPACITOR: {
                    unitsText.setText(Constants.CAPACITOR_UNITS);
                    break;
                }
                case Constants.RESISTOR: {
                    unitsText.setText(Constants.RESISTOR_UNITS);
                    break;
                }
                case Constants.DC_VOLTAGE: {
                    unitsText.setText(Constants.VOLTAGE_UNITS);
                    break;
                }
                case Constants.INDUCTOR: {
                    unitsText.setText(Constants.INDUCTOR_UNTIS);
                    break;
                }
                case Constants.WIRE: {
                    unitsText.setText(Constants.WIRE_UNTIS);
                    componentValueText.setText("--");
                    break;
                }
            }
        }

        if(componentState == SOLVED && selectedElm != null && selectedElm.getType() != Constants.WIRE) {
            voltageText.setText(Double.toString(selectedElm.getVoltageDiff()) + " V");
            currentText.setText(Double.toString(selectedElm.getCurrent()) + " A");
        } else {
            voltageText.setText("--");
            currentText.setText("--");
        }
    }

    private void generateExampleCircuitElms(){
        ResetComponents.resetNumComponents();
        this.circuitElms.add(new WireElm(new SimplePoint(300, 200),
                new SimplePoint(300, 500)));
        this.circuitElms.add(new WireElm(new SimplePoint(500, 300),
                new SimplePoint(700, 500)));
        this.circuitElms.add(new WireElm(new SimplePoint(700, 200),
                new SimplePoint(700, 500)));
        this.circuitElms.add(new WireElm(new SimplePoint(700, 500),
                new SimplePoint(700, 800)));
        this.circuitElms.add(new ResistorElm(new SimplePoint(300, 200),
                new SimplePoint(700, 200), 10));
        this.circuitElms.add(new ResistorElm(new SimplePoint(300, 500),
                new SimplePoint(700, 500), 50));
        this.circuitElms.add(new VoltageElm(new SimplePoint(300, 800),
                new SimplePoint(700, 800), 12));

    }


}
