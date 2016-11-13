package com.cpen321.circuitsolver.ui;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.cpen321.circuitsolver.R;
import com.cpen321.circuitsolver.model.components.CircuitElm;
import com.cpen321.circuitsolver.util.CircuitProject;
import com.cpen321.circuitsolver.util.Constants;

import java.io.File;
import java.io.IOException;

public class EditActivity extends AppCompatActivity {
    public static String TAG = "EditActivity";

    private CircuitDisplay circuitDisplay;
    private Button resistorButton;
    private Button capacitorButton;
    private Button inductorButton;
    private Button voltageSourceButton;
    private TextView valueUnits;
    private EditText componentValue;
    private Button solveCircuitButton;
    private TextView currentAndVoltageText;
    private String currentAndVoltageString;
    private ImageView cvBitmap;
    private double computedCurrent;
    private double computedResistance;

    private CircuitProject circuitProject;

    private CircuitElm tappedElement;

    private FloatingActionButton rotateFab;
    private FloatingActionButton analysisActivity;

    private View.OnTouchListener handleTouch = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {

            int x = (int) event.getX();
            int y = (int) event.getY();
            Context context = getApplicationContext();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    Log.i("TAG", "touched down");
                    CharSequence text = "Touched (" + x + "," + y + ")";
                    break;
                case MotionEvent.ACTION_MOVE:
                    Log.i("TAG", "moving: (" + x + ", " + y + ")");
                    break;
                case MotionEvent.ACTION_UP:
                    Log.i("TAG", "touched up");
                    EditActivity.this.tappedElement = circuitDisplay.getCircuitElemTouched(x, y);
                    break;
            }
            //EditActivity.this.tappedElement = null;
            //EditActivity.this.tappedElement = circuitDisplay.getCircuitElemTouched(x, y);
            EditActivity.this.displayElement();
            return true;
        }
    };

    @Override
    public void onBackPressed() {
        Intent backToHomeIntent = new Intent(this, HomeActivity.class);
        startActivity(backToHomeIntent);
        finish();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle extras = getIntent().getExtras();
        String dataLocation = null;
        if (extras != null) {
            dataLocation = (String) extras.get(Constants.CIRCUIT_PROJECT_FOLDER);
        }

        if (dataLocation != null){
            File test = new File(dataLocation);
            if (dataLocation.contains("example"))
                this.circuitDisplay = new CircuitDisplay(getApplicationContext());
            else {
                this.circuitProject = new CircuitProject(test);
                this.circuitDisplay = new CircuitDisplay(getApplicationContext(), this.circuitProject);
            }
        } else {
            this.circuitDisplay = new CircuitDisplay(getApplicationContext());
        }

        // todo: uncomment this one and comment out the previous two to switch to using opencv!

        circuitDisplay.setOnTouchListener(handleTouch);
        CoordinatorLayout relativeLayout = (CoordinatorLayout) findViewById(R.id.content_edit);
        relativeLayout.addView(this.circuitDisplay, 0);

        this.resistorButton = (Button) findViewById(R.id.resistorButton);
        this.capacitorButton = (Button) findViewById(R.id.capacitor_button);
        this.inductorButton = (Button) findViewById(R.id.inductorButton);
        this.voltageSourceButton = (Button) findViewById(R.id.voltage_source);
        this.valueUnits = (TextView) findViewById(R.id.units_display);
        this.componentValue = (EditText) findViewById(R.id.component_value);
        this.solveCircuitButton = (Button) findViewById(R.id.solveButton);
        this.currentAndVoltageText = (TextView) findViewById(R.id.voltageAndCurrent);
        this.rotateFab = (FloatingActionButton) findViewById(R.id.rotate_fav);
        this.analysisActivity = (FloatingActionButton) findViewById(R.id.component_analysis);
        this.initElements();
        this.tappedElement = this.circuitDisplay.getRandomElement();
        this.cvBitmap = (ImageView) findViewById(R.id.cvImage);
        try {
            this.cvBitmap.setImageBitmap(this.circuitProject.getProcessedImage());
            Log.i("edit", "after setting bitmap");
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        //this.displayElement();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void enableAllButtons() {
        this.solveCircuitButton.setEnabled(true);
        this.resistorButton.setEnabled(true);
        this.capacitorButton.setEnabled(true);
        this.inductorButton.setEnabled(true);
        this.voltageSourceButton.setEnabled(true);
        this.componentValue.setEnabled(true);
        this.componentValue.setFocusable(true);
        this.componentValue.setClickable(true);

        this.currentAndVoltageText.setEnabled(true);
        this.currentAndVoltageText.setFocusable(true);
        this.currentAndVoltageText.setClickable(true);

    }

    private void displayElement() {
        this.enableAllButtons();

        if (this.tappedElement == null) {
            this.valueUnits.setText(Constants.NOTHING_SELECTED);
            this.componentValue.setText("--");
            return;
        }

        switch (this.tappedElement.getType()) {
            case Constants.CAPACITOR: {
                this.capacitorButton.setEnabled(false);
                this.valueUnits.setText(Constants.CAPACITOR_UNITS);
                break;
            }
            case Constants.RESISTOR: {
                this.resistorButton.setEnabled(false);
                this.valueUnits.setText(Constants.RESISTOR_UNITS);
                break;
            }
            case Constants.DC_VOLTAGE: {
                this.voltageSourceButton.setEnabled(false);
                this.valueUnits.setText(Constants.VOLTAGE_UNITS);
                break;
            }
            case Constants.INDUCTOR: {
                this.inductorButton.setEnabled(false);
                this.valueUnits.setText(Constants.INDUCTOR_UNTIS);
                break;
            }
        }

        circuitDisplay.solveCircuit();
        this.componentValue.setText(Double.toString(this.tappedElement.getValue()));
        this.currentAndVoltageString = "\n\n\n\n\n\n\n\n\n\n" + Double.toString(this.tappedElement.getCurrent()) + " A\n" + Double.toString(this.tappedElement.getVoltageDiff())+ " V";
        this.currentAndVoltageText.setText(currentAndVoltageString);
        Log.d(TAG ,"\n\n" + Double.toString(this.tappedElement.getCurrent()) + " A\n" + Double.toString(this.tappedElement.getVoltageDiff())+ " V");
    }

    private void initElements() {
        this.solveCircuitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                circuitDisplay.solveCircuit();
            }
        });
        this.resistorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditActivity.this.changeElementType(Constants.RESISTOR);
            }
        });
        this.capacitorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditActivity.this.changeElementType(Constants.CAPACITOR);
            }
        });
        this.inductorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditActivity.this.changeElementType(Constants.INDUCTOR);
            }
        });
        this.voltageSourceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditActivity.this.changeElementType(Constants.DC_VOLTAGE);
            }
        });
        this.rotateFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditActivity.this.rotateElement();
            }
        });
        this.analysisActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditActivity.this.analyzeComponent();
            }
        });
        this.componentValue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                EditActivity.this.changeElementValue();
            }
        });
    }

    private void changeElementType(String newElementType) {
        if (this.tappedElement == null)
            return;
        this.circuitDisplay.changeElementType(this.tappedElement, newElementType);
    }

    private void changeElementValue() {
        if (this.tappedElement == null)
            return;
        String newValue = this.componentValue.getText().toString();

        if (newValue.isEmpty())
            return;

        if (newValue.equals("--")) {
            return;
        }

        this.circuitDisplay.changeElementValue(this.tappedElement,
                Double.valueOf(newValue));
    }

    private void rotateElement() {
        if (this.tappedElement == null)
            return;
        this.circuitDisplay.rotateElement(this.tappedElement);
    }

    private void analyzeComponent(){
        Intent analysisIntent = new Intent(EditActivity.this, AnalysisActivity.class);
        Bundle extras = new Bundle();
        // todo: figure out what we should use as keys...
        extras.putString("tappedElementType", this.tappedElement.getType());
        extras.putDouble("computedCurrent", this.tappedElement.getCurrent());
        extras.putDouble("computedVoltageDiff", this.tappedElement.getVoltageDiff());
        analysisIntent.putExtras(extras);
        startActivity(analysisIntent);
    }
}
