package com.cpen321.circuitsolver.ui.draw;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.cpen321.circuitsolver.R;
import com.cpen321.circuitsolver.model.CircuitNode;
import com.cpen321.circuitsolver.model.ResetComponents;
import com.cpen321.circuitsolver.model.SimplePoint;
import com.cpen321.circuitsolver.model.components.CircuitElm;
import com.cpen321.circuitsolver.model.components.ResistorElm;
import com.cpen321.circuitsolver.model.components.VoltageElm;
import com.cpen321.circuitsolver.model.components.WireElm;
import com.cpen321.circuitsolver.ngspice.NgSpice;
import com.cpen321.circuitsolver.ngspice.SpiceInterfacer;
import com.cpen321.circuitsolver.service.AllocateNodes;
import com.cpen321.circuitsolver.service.CircuitDefParser;
import com.cpen321.circuitsolver.ui.HomeActivity;
import com.cpen321.circuitsolver.util.CircuitProject;
import com.cpen321.circuitsolver.util.Constants;
import com.cpen321.circuitsolver.util.ImageUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import static com.cpen321.circuitsolver.ui.draw.AddComponentState.DC_SOURCE;
import static com.cpen321.circuitsolver.ui.draw.AddComponentState.ERASE;
import static com.cpen321.circuitsolver.ui.draw.AddComponentState.INVALID;
import static com.cpen321.circuitsolver.ui.draw.AddComponentState.RESISTOR;
import static com.cpen321.circuitsolver.ui.draw.AddComponentState.SOLVED;
import static com.cpen321.circuitsolver.ui.draw.AddComponentState.WIRE;
import static com.cpen321.circuitsolver.ui.draw.TouchState.UP;

public class DrawActivity extends AppCompatActivity implements View.OnTouchListener {
    public static final String TAG = "DrawActivity";

    private ImageButton componentMenuButton;
    private ImageButton eraseButton;
    private ImageButton solveButton;
    private TextView unitsText;
    private TextView voltageText;
    private TextView currentText;
    private EditText componentValueText;
    private CircuitProject circuitProject;
    private CircuitDefParser parser = new CircuitDefParser();

    private int screenHeight;
    private int screenWidth;

    private boolean firstZoom = true;
    private static ArrayList<CircuitElm> circuitElms = new ArrayList<CircuitElm>();
    private static final ReentrantLock circuitElmsLock = new ReentrantLock();
    private CircuitView circuitView;
    private static AddComponentState componentState = DC_SOURCE;
    private TouchState touchState = UP;

    private static SimplePoint startPoint = new SimplePoint(0, 0);
    private static SimplePoint endPoint = new SimplePoint(0, 0);


    private static CircuitElm selectedElm = null;
    private static CircuitElm candidateElement = null;

    private int[] location = new int[2];
    private static int truncateBits = 5; //helps with drawing parallel lines by limiting angles
    private static final int sigFigs = 5;

    public static ArrayList<CircuitElm> getCircuitElms() {
        return circuitElms;
    }

    // Scaling objects
    private static ScaleGestureDetector mScaleDetector;
    private static float mScaleFactor = 1.f;
    // The focus point for the scaling
    private static float scalePointX;
    private static float scalePointY;
    private static Rect rect;

    public static int getEndY() {
        return endPoint.getY();
    }

    public static int getEndX() {
        return endPoint.getX();
    }

    public static int getStartX() {
        return startPoint.getX();
    }

    public static int getStartY() {
        return startPoint.getY();
    }


    private AddComponentState prevComponentState = DC_SOURCE;

    /**
     * Get selected element
     *
     * @return selected element, or null
     */
    public static CircuitElm getSelectedElm() {
        return selectedElm;
    }


//    @Override
//    public void onConfigurationChanged(Configuration configure) {
//        super.onConfigurationChanged(configure);

//        // Convert list of elements to a circuit def file to save
//        String circStr = parser.elementsToTxt(circuitElms, screenWidth, screenHeight);
//        circuitProject.saveCircuitDefinitionFile(circStr);
//
//        float scaleX = ((float) Constants.PROCESSING_WIDTH)/((float) screenWidth);
//        float scaleY = ((float) Constants.PROCESSING_WIDTH)/((float) screenHeight);
//
//        Bitmap tmp = Bitmap.createBitmap(Constants.PROCESSING_WIDTH, Constants.PROCESSING_WIDTH,
//                Bitmap.Config.RGB_565);
//        Canvas tmpcanvas = new Canvas(tmp);
//        tmpcanvas.scale(scaleX, scaleY);
//        this.circuitView.fakeDraw(tmpcanvas);
//
//        try {
//            File screenShot = this.circuitProject.getOriginalImageLocation();
//            FileOutputStream screenshotStream = new FileOutputStream(screenShot);
//            tmp.compress(Bitmap.CompressFormat.PNG, 50, screenshotStream);
//        } catch (FileNotFoundException ex) {
//            ex.printStackTrace();
//        } catch (IOException ioEx) {
//            ioEx.printStackTrace();
//        }


        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.
//        savedInstanceState.putParcelableArrayList("circuitElms",circuitElms);
//
//        savedInstanceState.putBoolean("MyBoolean", true);
//        savedInstanceState.putDouble("myDouble", 1.9);
//        savedInstanceState.putInt("MyInt", 1);
//        savedInstanceState.putString("MyString", "Welcome back to Android");
        // etc.
 //   }

//    @Override
//    public void onRestoreInstanceState(Bundle savedInstanceState) {
//        super.onRestoreInstanceState(savedInstanceState);
//        // Restore UI state from the savedInstanceState.
//        // This bundle has also been passed to onCreate.
//        boolean myBoolean = savedInstanceState.getBoolean("MyBoolean");
//        double myDouble = savedInstanceState.getDouble("myDouble");
//        int myInt = savedInstanceState.getInt("MyInt");
//        String myString = savedInstanceState.getString("MyString");
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);

        mScaleDetector = new ScaleGestureDetector(this, new ScaleListener());

        circuitView = (CircuitView) findViewById(R.id.circuitFrame);
        componentMenuButton = (ImageButton) findViewById(R.id.componentMenuButton);
        eraseButton = (ImageButton) findViewById(R.id.eraseButton);
        solveButton = (ImageButton) findViewById(R.id.solveButton);
        voltageText = (TextView) findViewById(R.id.voltageText);
        currentText = (TextView) findViewById(R.id.currentText);
        circuitView.setOnTouchListener(this);
        unitsText = (TextView) findViewById(R.id.units_display);
        componentValueText = (EditText) findViewById(R.id.component_value);

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        screenHeight = displaymetrics.heightPixels;
        screenWidth = displaymetrics.widthPixels;
        updateDisplayInfo();

        //For loading previous circuit or new openCV interpreted circuit
        Bundle extras = getIntent().getExtras();
        String dataLocation = null;
        if (extras != null) {
            dataLocation = (String) extras.get(Constants.CIRCUIT_PROJECT_FOLDER);
        }
        if (dataLocation != null) {
            File circuitFolder = new File(dataLocation);
            this.circuitProject = new CircuitProject(circuitFolder);
            if (dataLocation.contains("example")) {
                generateExampleCircuitElms();
            } else {
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
        } else {
            circuitElms.clear();
            this.circuitProject = new CircuitProject(ImageUtils.getTimeStamp(),
                    getExternalFilesDir(Environment.DIRECTORY_PICTURES));
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
                                "Drag to draw",
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
                        if (selectedElm != null) {
                            circuitView.pause();
                            changeElementType(selectedElm, componentState);
                            circuitView.resume();
                            updateDisplayInfo();
                        }
                        return true;
                    }
                });
                popup.show(); //showing popup menu
            }
        }); //closing the setOnClickListener method

        eraseButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                PopupMenu popup = new PopupMenu(DrawActivity.this, eraseButton);
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    //eraseButton.setColorFilter(Color.argb(255, 255, 255, 255));
                } else if(event.getAction() == MotionEvent.ACTION_UP) {
                    if (selectedElm != null) {
                        if (componentState == SOLVED || componentState == INVALID) {
                            componentState = prevComponentState;
                        }
                        circuitView.pause();
                        circuitElms.remove(selectedElm);
                        selectedElm = null;
                        circuitView.resume();
                    }
                    updateDisplayInfo();
                }
                return true;
            }
        });

        solveButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                if (action == MotionEvent.ACTION_UP) {
                    if (componentState == SOLVED) {
                        Toast.makeText(DrawActivity.this, "Select an element to view its values.", Toast.LENGTH_SHORT).show();
                        return false;
                    } else if(componentState == INVALID) {
                        Toast.makeText(DrawActivity.this, "invalid circuit", Toast.LENGTH_SHORT).show();
                        return false;
                    } else {
                        prevComponentState = componentState;
                    }
                    CircuitNode.resetNumNodes();
                    AllocateNodes circuit = new AllocateNodes(circuitElms);
                    circuit.allocate();
                    SpiceInterfacer interfacer = new SpiceInterfacer(circuit.getNodes(), circuit.getElements());
                    if (interfacer.solveCircuit(NgSpice.getInstance(DrawActivity.this))) {
                        Toast.makeText(DrawActivity.this, "solved", Toast.LENGTH_SHORT).show();
                        for (CircuitElm circuitElm : circuitElms) {
                            circuitElm.calculateCurrent();
                        }
                        componentState = SOLVED;
                    } else {
                        Toast.makeText(DrawActivity.this, "invalid circuit", Toast.LENGTH_SHORT).show();
                        componentState = INVALID;
                    }
                    updateDisplayInfo();
                }
                return true;
            }
        });

//        componentValueText.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });

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

                if (newValue.equals("")) {
                    return;
                }

                Double newValueDouble = selectedElm.getValue();

                try {
                    newValueDouble = Double.valueOf(newValue);
                } catch(NumberFormatException e) {
                    e.printStackTrace();
                }

                selectedElm.setValue(newValueDouble);

                if (((componentState == SOLVED || componentState == INVALID) && componentValueText.getTag() != null)) { //Some bug here where wires triggered if statement
                    componentState = prevComponentState;
                    Toast.makeText(DrawActivity.this, "prev state wut the heck", Toast.LENGTH_SHORT).show();
                    updateDisplayInfo();
                }
                componentValueText.setTag("not null");
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        setContentView(R.layout.activity_draw);
    }

    @Override
    public void onBackPressed() {
        componentState = prevComponentState;

        // Convert list of elements to a circuit def file to save
        String circStr = parser.elementsToTxt(circuitElms, screenWidth, screenHeight);
        circuitProject.saveCircuitDefinitionFile(circStr);

        float scaleX = ((float) Constants.PROCESSING_WIDTH)/((float) screenWidth);
        float scaleY = ((float) Constants.PROCESSING_WIDTH)/((float) screenHeight);

        Bitmap tmp = Bitmap.createBitmap(Constants.PROCESSING_WIDTH, Constants.PROCESSING_WIDTH,
                Bitmap.Config.RGB_565);
        Canvas tmpcanvas = new Canvas(tmp);
        tmpcanvas.scale(scaleX, scaleY);
        this.circuitView.fakeDraw(tmpcanvas);

        try {
            File screenShot = this.circuitProject.getOriginalImageLocation();
            FileOutputStream screenshotStream = new FileOutputStream(screenShot);
            tmp.compress(Bitmap.CompressFormat.PNG, 50, screenshotStream);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ioEx) {
            ioEx.printStackTrace();
        }

        Intent backToHomeIntent = new Intent(this, HomeActivity.class);
        startActivity(backToHomeIntent);
        finish();
    }


    @Override
    protected void onPause() {
        super.onPause();
        componentState = prevComponentState;

        // Convert list of elements to a circuit def file to save
        String circStr = parser.elementsToTxt(circuitElms, screenWidth, screenHeight);
        circuitProject.saveCircuitDefinitionFile(circStr);

        float scaleX = ((float) Constants.PROCESSING_WIDTH)/((float) screenWidth);
        float scaleY = ((float) Constants.PROCESSING_WIDTH)/((float) screenHeight);

        Bitmap tmp = Bitmap.createBitmap(Constants.PROCESSING_WIDTH, Constants.PROCESSING_WIDTH,
                Bitmap.Config.RGB_565);
        Canvas tmpcanvas = new Canvas(tmp);
        tmpcanvas.scale(scaleX, scaleY);
        this.circuitView.fakeDraw(tmpcanvas);

        try {
            File screenShot = this.circuitProject.getOriginalImageLocation();
            FileOutputStream screenshotStream = new FileOutputStream(screenShot);
            tmp.compress(Bitmap.CompressFormat.PNG, 50, screenshotStream);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ioEx) {
            ioEx.printStackTrace();
        }
        circuitView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        circuitView.resume();
    }

    public static boolean isZooming() {
        return zooming;
    }

    private static boolean zooming = false;
    private static boolean drawing = false;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        mScaleDetector.onTouchEvent(event);
        int x = (int) ((event.getX() - scalePointX)/mScaleFactor + scalePointX);
        int y = (int) ((event.getY() - scalePointY)/mScaleFactor + scalePointY);

        int truncatedX = (x >> truncateBits) << truncateBits;
        int truncatedY = (y >> truncateBits) << truncateBits;
        int lengthThreshHold = 55;

        if(event.getPointerCount() == 2) {
            zooming = true;
        } if(event.getPointerCount() == 1) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    zooming = false;
                    selectedElm = getSelected(x, y);
                    startPoint = new SimplePoint(truncatedX, truncatedY);
                    endPoint = new SimplePoint(truncatedX, truncatedY);
                    for (CircuitElm circuitElm : circuitElms) {
                        //check to see if the new points we are drawing are near existing ones, if so connect them
                        int threshHold = 50;
                        SimplePoint p1 = circuitElm.getP1();
                        SimplePoint p2 = circuitElm.getP2();
                        if (startPoint.distanceFrom(p1) < threshHold) {
                            startPoint = p1;
                        } else if (startPoint.distanceFrom(p2) < threshHold) {
                            startPoint = p2;
                        }
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (zooming) {
                        resetCoordinates();
                        break;
                    }
                    endPoint = new SimplePoint(truncatedX, truncatedY);
                    if (startPoint.distanceFrom(endPoint) > lengthThreshHold) {
                        circuitView.pause();
                        selectedElm = null;
                        circuitView.resume();
                        if (componentState == SOLVED || componentState == INVALID) {
                            componentState = prevComponentState;
                        }
                        switch (componentState) {
                            case DC_SOURCE:
                                candidateElement = new VoltageElm();
                                break;
                            case RESISTOR:
                                candidateElement = new ResistorElm();
                                break;
                            case WIRE:
                                candidateElement = new WireElm();
                                break;
                            default:
                                candidateElement = new WireElm();
                                break;
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (zooming) {
                        resetCoordinates();
                        break;
                    }
                    endPoint = new SimplePoint(truncatedX, truncatedY);
                    for (CircuitElm circuitElm : circuitElms) {
                        //check to see if the new points we are drawing are near existing ones, if so connect them
                        int threshHold = 50;
                        SimplePoint p1 = circuitElm.getP1();
                        SimplePoint p2 = circuitElm.getP2();
                        if (endPoint.distanceFrom(p1) < threshHold) {
                            endPoint = p1;
                        } else if (endPoint.distanceFrom(p2) < threshHold) {
                            endPoint = p2;
                        }
                    }
                    int length = (int) startPoint.distanceFrom(endPoint);
                    //don't create a circuit element if it is too short
                    if (length > lengthThreshHold && componentState != ERASE) {
                        if (candidateElement != null) {
                            candidateElement.setP1(startPoint);
                            candidateElement.setP2(endPoint);
                            candidateElement.setValue(10);
                            circuitView.pause();
                            circuitElms.add(candidateElement);
                            circuitView.resume();
                        }
                    }
                    candidateElement = null;
                    resetCoordinates();
                    break;
            }
            updateDisplayInfo();
        }
        return true;


    }

    private boolean isOnElement(int x, int y, CircuitElm e) {
        int threshHold = 10;
        SimplePoint touchPoint = new SimplePoint(x, y);
        return (Math.abs(e.getP1().distanceFrom(touchPoint) + touchPoint.distanceFrom(e.getP2()) - e.getP1().distanceFrom(e.getP2())) < threshHold);
    }

    /**
     * Get the circuit element which passes point x,y
     *
     * @param x coordinate
     * @param y coordinate
     * @return the circuitElm touched, or null if non were touched
     */
    private CircuitElm getSelected(int x, int y) {
        for (CircuitElm circuitElm : circuitElms) {
            if (isOnElement(x, y, circuitElm)) {
                return circuitElm;
            }
        }
        return null;
    }

    private void resetCoordinates() {
        startPoint = new SimplePoint(0, 0);
        endPoint = new SimplePoint(0, 0);
    }

    private void updateDisplayInfo() {
        if(circuitElms.isEmpty()) {
            solveButton.setAlpha(0.5f);
        } else {
            solveButton.setAlpha(1f);
        }
        switch(componentState) {
            case SOLVED: {
                solveButton.setImageResource(R.drawable.ic_solved);
                break;
            }
            case INVALID: {
                solveButton.setImageResource(R.drawable.ic_invalid);
                break;
            }
            default: {
                solveButton.setImageResource(R.drawable.ic_solve);
                break;
            }
        }
        if (selectedElm == null) {
            unitsText.setText(Constants.NOTHING);
            componentValueText.setEnabled(false);
            componentValueText.setText("");
            voltageText.setText("");
            currentText.setText("");
            this.toggleAddButtonText(false);
            eraseButton.setAlpha(.5f);
        } else {
            eraseButton.setAlpha(1f);
            componentValueText.setTag(null); //Used to distinguish between whether editText was changed by user, or pragmatically
            componentValueText.setText(Double.toString(selectedElm.getValue()));
            switch (selectedElm.getType()) {
                case Constants.CAPACITOR: {
                    unitsText.setText(Constants.CAPACITOR_UNITS);
                    componentValueText.setEnabled(true);
                    break;
                }
                case Constants.RESISTOR: {
                    unitsText.setText(Constants.RESISTOR_UNITS);
                    componentValueText.setEnabled(true);
                    break;
                }
                case Constants.DC_VOLTAGE: {
                    unitsText.setText(Constants.VOLTAGE_UNITS);
                    componentValueText.setEnabled(true);
                    break;
                }
                case Constants.INDUCTOR: {
                    unitsText.setText(Constants.INDUCTOR_UNTIS);
                    componentValueText.setEnabled(true);
                    break;
                }
                case Constants.WIRE: {
                    unitsText.setText(Constants.WIRE_UNITS);
                    componentValueText.setTag(null);
                    componentValueText.setText("--");
                    componentValueText.setEnabled(false);
                    break;
                }
            }
            this.toggleAddButtonText(true);
        }

        if (componentState == SOLVED && selectedElm != null) {
            double voltage = limitSigfig(sigFigs, selectedElm.getVoltageDiff());
            double current = limitSigfig(sigFigs, selectedElm.getCurrent());
            voltageText.setText(Double.toString(Math.abs(voltage)) + " V");
            currentText.setText(Double.toString(Math.abs(current)) + " A");
//            voltageText.setText(Double.toString(selectedElm.getVoltageDiff()) + " V");
//            currentText.setText(Double.toString(selectedElm.getCurrent()) + " A");
        } else {
            voltageText.setText("");
            currentText.setText("");
        }
    }

    private void toggleAddButtonText(boolean addText) {
        if (addText) {
            componentMenuButton.setImageResource(R.drawable.ic_swap);
        } else {
            componentMenuButton.setImageResource(R.drawable.ic_draw);
        }
    }

    private void changeElementType(CircuitElm element, AddComponentState newType) {
        for (CircuitElm elm : circuitElms) {
            if (elm.equals(element)) {
                int index = circuitElms.indexOf(elm);
                switch (newType) {
                    case WIRE:
                        elm = new WireElm(elm.getP1(), elm.getP2());
                        break;
//                    case :
//                        elm = new InductorElm(elm.getP1(), elm.getP2(), elm.getValue());
//                        break;
//                    case Constants.CAPACITOR:
//                        elm = new CapacitorElm(elm.getP1(), elm.getP2(), elm.getValue());
//                        break;
                    case DC_SOURCE:
                        elm = new VoltageElm(elm.getP1(), elm.getP2(), elm.getValue());
                        break;
                    case RESISTOR:
                        if (elm.getType().equals(Constants.WIRE)) {
                            elm = new ResistorElm(elm.getP1(), elm.getP2(), 10);
                        } else {
                            elm = new ResistorElm(elm.getP1(), elm.getP2(), elm.getValue());
                        }
                        break;
                }
                circuitElms.set(index, elm);
            }
        }
        selectedElm = null;
    }

    private void generateExampleCircuitElms() {
        ResetComponents.resetNumComponents();
        circuitElms.add(new WireElm(new SimplePoint(300, 200),
                new SimplePoint(300, 500)));
        circuitElms.add(new WireElm(new SimplePoint(500, 300),
                new SimplePoint(700, 500)));
        circuitElms.add(new WireElm(new SimplePoint(700, 200),
                new SimplePoint(700, 500)));
        circuitElms.add(new WireElm(new SimplePoint(700, 500),
                new SimplePoint(700, 800)));
        circuitElms.add(new ResistorElm(new SimplePoint(300, 200),
                new SimplePoint(700, 200), 10));
        circuitElms.add(new ResistorElm(new SimplePoint(300, 500),
                new SimplePoint(700, 500), 50));
        circuitElms.add(new VoltageElm(new SimplePoint(300, 800),
                new SimplePoint(700, 800), 12));
    }

    private double limitSigfig(int sigFigs, double input) {
        BigDecimal bd = new BigDecimal(input);
        bd = bd.round(new MathContext(sigFigs));
        return bd.doubleValue();
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= detector.getScaleFactor();
            scalePointX =  detector.getFocusX();
            scalePointY = detector.getFocusY();

            // Don't let the object get too small or too large.
            mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 10.0f));

            return true;
        }
    }

    public static class CircuitView extends SurfaceView implements Runnable {
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

            rect = canvas.getClipBounds();
            canvas.scale(mScaleFactor, mScaleFactor, scalePointX, scalePointY);

                //canvas.scale(this.scale, this.scale, this.getWidth()/2, this.getHeight()/2);
            canvas.drawColor(Color.WHITE);
            paint.setColor(Color.DKGRAY);
            //get component state
            for (CircuitElm circuitElm : circuitElms) {
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
                if(componentState == SOLVED) {
                    paint.setColor(color);
                    selected.drawCurrent(canvas, paint);
                }
            }
            //AddComponentState state = DrawActivity.getComponentState();
            paint.setColor(color);
            if (candidateElement != null && !DrawActivity.isZooming()) {
                String type = convertStateToType(componentState);
                candidateElement.draw(canvas, DrawActivity.getStartX(), DrawActivity.getStartY(), DrawActivity.getEndX(), DrawActivity.getEndY(), paint);
            }
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

}