package com.cpen321.circuitsolver.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cpen321.circuitsolver.R;
import com.cpen321.circuitsolver.model.components.CircuitElm;
import com.cpen321.circuitsolver.util.Constants;

import java.text.AttributedCharacterIterator;

public class EditActivity extends AppCompatActivity {

    private CircuitDisplay circuitDisplay;
    private Button resistorButton;
    private Button capacitorButton;
    private Button inductorButton;
    private Button voltageSourceButton;
    private TextView valueUnits;
    private EditText componentValue;

    private CircuitElm tappedElement;

    private View.OnTouchListener handleTouch = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {

            int x = (int) event.getX();
            int y = (int) event.getY();
            Context context = getApplicationContext();
            int duration = Toast.LENGTH_SHORT;

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    Log.i("TAG", "touched down");
                    CharSequence text = "Touched (" + x + "," + y + ")";
                    Toast.makeText(context, text, duration).show();
                    break;
                case MotionEvent.ACTION_MOVE:
                    Log.i("TAG", "moving: (" + x + ", " + y + ")");
                    break;
                case MotionEvent.ACTION_UP:
                    Log.i("TAG", "touched up");
                    break;
            }
            EditActivity.this.tappedElement = circuitDisplay.getCircuitElemTouched(x, y);

            if (EditActivity.this.tappedElement != null){
                Toast.makeText(context, EditActivity.this.tappedElement.getType(),duration).show();
                EditActivity.this.displayElement();
            }
            else{
                Toast.makeText(context, "Nothing nearby :(",duration).show();
            }
            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        this.circuitDisplay = new CircuitDisplay(getApplicationContext());
        circuitDisplay.setOnTouchListener(handleTouch);
        CoordinatorLayout relativeLayout = (CoordinatorLayout) findViewById(R.id.content_edit);
        relativeLayout.addView(this.circuitDisplay, 0);

        this.resistorButton = (Button) findViewById(R.id.resistorButton);
        this.capacitorButton = (Button) findViewById(R.id.capacitor_button);
        this.resistorButton = (Button) findViewById(R.id.inductorButton);
        this.voltageSourceButton = (Button) findViewById(R.id.voltage_source);
        this.valueUnits = (TextView) findViewById(R.id.units_display);
        this.componentValue = (EditText) findViewById(R.id.component_value);
        this.initButtons();
        this.tappedElement = this.circuitDisplay.getRandomElement();
        this.displayElement();
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
        this.resistorButton.setEnabled(true);
        this.capacitorButton.setEnabled(true);
//        this.inductorButton.setEnabled(true);
//        this.voltageSourceButton.setEnabled(true);
        this.componentValue.setEnabled(true);
        this.componentValue.setFocusable(true);
        this.componentValue.setClickable(true);
    }

    private void displayElement() {
        this.enableAllButtons();
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

        this.componentValue.setText(Double.toString(this.tappedElement.getValue()));

    }

    private void changeElementType(String newElementType) {

    }

    private void initButtons() {
//        this.resistorButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                EditActivity.this.changeElementType(Constants.RESISTOR);
//            }
//        });
//
//        this.capacitorButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                EditActivity.this.changeElementType(Constants.CAPACITOR);
//            }
//        });
//
//        this.inductorButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                EditActivity.this.changeElementType(Constants.INDUCTOR);
//            }
//        });
//
//        this.voltageSourceButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                EditActivity.this.changeElementType(Constants.DC_VOLTAGE);
//            }
//        });

    }



}
