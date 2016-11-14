package com.cpen321.circuitsolver.ui.draw;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.cpen321.circuitsolver.R;
import com.cpen321.circuitsolver.model.components.CircuitElm;
import com.cpen321.circuitsolver.ui.EditActivity;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class DrawActivity extends AppCompatActivity implements View.OnTouchListener {

    private Button componentMenuButton;
    private ArrayList<CircuitElm> components;
    private CircuitView circuitView;
    private static int x;
    private static int y;
    private FrameLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);
        componentMenuButton = (Button) findViewById(R.id.componentMenuButton);
        components = new ArrayList<CircuitElm>();
        circuitView = (CircuitView) findViewById(R.id.circuitFrame);


        circuitView.setOnTouchListener(this);

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
                        return true;
                    }
                });
                popup.show(); //showing popup menu
            }
        }); //closing the setOnClickListener method

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
        x = (int) event.getX();
        y = (int) event.getY();
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
                break;
        }
        return true;
    }

    public static int getX() {
        return x;
    }

    public static int getY() {
        return y;
    }
}
