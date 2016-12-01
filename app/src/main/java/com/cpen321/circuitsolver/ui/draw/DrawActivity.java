package com.cpen321.circuitsolver.ui.draw;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

import static com.cpen321.circuitsolver.ui.draw.AddComponentState.DC_SOURCE;
import static com.cpen321.circuitsolver.ui.draw.AddComponentState.ERASE;
import static com.cpen321.circuitsolver.ui.draw.AddComponentState.RESISTOR;
import static com.cpen321.circuitsolver.ui.draw.AddComponentState.SOLVED;
import static com.cpen321.circuitsolver.ui.draw.AddComponentState.WIRE;
import static com.cpen321.circuitsolver.ui.draw.TouchState.UP;

public class DrawActivity extends AppCompatActivity implements View.OnTouchListener {
    public static final String TAG = "DrawActivity";

    private Button componentMenuButton;
    private Button eraseButton;
    private Button solveButton;
    private TextView unitsText;
    private TextView voltageText;
    private TextView currentText;
    private EditText componentValueText;
    private CircuitProject circuitProject;
    private CircuitDefParser parser = new CircuitDefParser();

    private int screenHeight;
    private int screenWidth;

    private DrawController drawController;
    private boolean firstZoom = true;
    private static ArrayList<CircuitElm> circuitElms = new ArrayList<CircuitElm>();
    private static final ReentrantLock circuitElmsLock = new ReentrantLock();
    private CircuitView circuitView;
    private static AddComponentState componentState = DC_SOURCE;
    private TouchState touchState = UP;

    private static SimplePoint startPoint = new SimplePoint(0, 0);
    private static SimplePoint endPoint = new SimplePoint(0, 0);
    private static SimplePoint eraserPoint = new SimplePoint(0, 0);


    private static CircuitElm selectedElm = null;
    private static CircuitElm candidateElement = null;

    private int[] location = new int[2];

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


    @Override
    public void onConfigurationChanged(Configuration configure) {
        super.onConfigurationChanged(configure);

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
    }

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

        circuitView = (CircuitView) findViewById(R.id.circuitFrame);
        componentMenuButton = (Button) findViewById(R.id.componentMenuButton);
        eraseButton = (Button) findViewById(R.id.eraseButton);
        solveButton = (Button) findViewById(R.id.solveButton);
        voltageText = (TextView) findViewById(R.id.voltageText);
        currentText = (TextView) findViewById(R.id.currentText);
        circuitView.setOnTouchListener(this);
        unitsText = (TextView) findViewById(R.id.units_display);
        componentValueText = (EditText) findViewById(R.id.component_value);
        this.drawController = new DrawController();

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        screenHeight = displaymetrics.heightPixels;
        screenWidth = displaymetrics.widthPixels;

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
                        if (selectedElm != null) {
                            circuitView.pause();
                            changeElementType(selectedElm, componentState);
                            circuitView.resume();
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
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                {
                    eraserPoint = new SimplePoint((int) (event.getRawX() - location[0]),
                            (int) (event.getRawY() - location[0]));
                    if (selectedElm != null) {
                        if (componentState == SOLVED) {
                            componentState = prevComponentState;
                        }
                        circuitView.pause();
                        circuitElms.remove(selectedElm);
                        selectedElm = null;
                        circuitView.resume();
                    }
                    else{
                        Toast.makeText(DrawActivity.this, "Select Component to Erase", Toast.LENGTH_SHORT).show();
                    }
                    CircuitElm toRemove = getSelected(eraserPoint.getX(), eraserPoint.getY());
                    if (toRemove != null) {
                        if (componentState == SOLVED) {
                            componentState = prevComponentState;
                        }
                        circuitView.pause();
                        circuitElms.remove(toRemove);
                        circuitView.resume();
                    }


                    displayElementInfo();
                }
                return true;
            }
        });

        solveButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (componentState != SOLVED) {
                        prevComponentState = componentState;
                    }
                    componentState = SOLVED;
                    CircuitNode.resetNumNodes();
                    AllocateNodes circuit = new AllocateNodes(circuitElms);
                    circuit.allocate();
                    SpiceInterfacer interfacer = new SpiceInterfacer(circuit.getNodes(), circuit.getElements());
                    if (interfacer.solveCircuit(NgSpice.getInstance(DrawActivity.this))) {
                        Toast.makeText(DrawActivity.this, "Solved!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(DrawActivity.this, "Invalid Circuit..", Toast.LENGTH_SHORT).show();
                    }
                    displayElementInfo();
                }
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

                if (componentState == SOLVED && componentValueText.getTag() != null) {
                    componentState = prevComponentState;
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

    public static CircuitElm getCandidateElement() {
        return candidateElement;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getPointerCount() == 2) {
            startPoint = null;
            endPoint = null;
            SimplePoint fingerOne = new SimplePoint((int) event.getX(0), (int) event.getY(0));
            SimplePoint fingerTwo = new SimplePoint((int) event.getX(1), (int) event.getY(1));
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
        } else {
            int x;
            int y;
            int truncateBits = 5;
            v.getLocationOnScreen(location);
            x = (int) ((event.getRawX() - location[0]) / this.circuitView.scale);
            y = (int) ((event.getRawY() - location[1]) / this.circuitView.scale);
            x = (x >> truncateBits) << truncateBits;
            y = (y >> truncateBits) << truncateBits;
            int lengthThreshHold = 40;

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    selectedElm = getSelected(x, y);
                    startPoint = new SimplePoint(x, y);
                    endPoint = new SimplePoint(x, y);
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
                    if (startPoint == null)
                        break;
                    endPoint = new SimplePoint(x, y);
                    if (startPoint.distanceFrom(endPoint) > lengthThreshHold) {
                        circuitView.pause();
                        selectedElm = null;
                        circuitView.resume();
                        if (componentState == SOLVED) {
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
                    if (startPoint == null)
                        break;
                    endPoint = new SimplePoint(x, y);
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
            displayElementInfo();
            return true;
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

    private void displayElementInfo() {
        if (selectedElm == null) {
            unitsText.setText(Constants.NOTHING_SELECTED);
            componentValueText.setText("--");
            voltageText.setText("--");
            currentText.setText("--");
            this.toggleAddButtonText(false);
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
            this.toggleAddButtonText(true);
        }

        if (componentState == SOLVED && selectedElm != null && !selectedElm.getType().equals(Constants.WIRE)) {
            voltageText.setText(Double.toString(selectedElm.getVoltageDiff()) + " V");
            currentText.setText(Double.toString(selectedElm.getCurrent()) + " A");
        } else {
            voltageText.setText("--");
            currentText.setText("--");
        }
    }

    private void toggleAddButtonText(boolean addText) {
        if (addText) {
            this.componentMenuButton.setText(R.string.change_button_tag);
        } else {
            this.componentMenuButton.setText(R.string.add_button_tag);
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


}