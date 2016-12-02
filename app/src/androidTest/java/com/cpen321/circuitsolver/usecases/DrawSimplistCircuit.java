package com.cpen321.circuitsolver.usecases;

import android.support.test.espresso.ViewAction;
import android.support.test.espresso.action.CoordinatesProvider;
import android.support.test.espresso.action.GeneralLocation;
import android.support.test.espresso.action.GeneralSwipeAction;
import android.support.test.espresso.action.Press;
import android.support.test.espresso.action.Swipe;
import android.view.View;

import com.cpen321.circuitsolver.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Created by Cornelis Dirk Haupt on 12/1/2016.
 */
public class DrawSimplistCircuit {

    public DrawSimplistCircuit(String left, String right, String top, String bottom){
        onView(withId(R.id.componentMenuButton)).perform(click());
        onView(withText(left)).perform(click());
        onView(withId(R.id.circuitFrame)).perform(swipeDownLeft());
        onView(withId(R.id.componentMenuButton)).perform(click());
        onView(withText(bottom)).perform(click());
        onView(withId(R.id.circuitFrame)).perform(swipeRightBottom());
        onView(withId(R.id.componentMenuButton)).perform(click());
        onView(withText(right)).perform(click());
        onView(withId(R.id.circuitFrame)).perform(swipeUpRight());
        onView(withId(R.id.componentMenuButton)).perform(click());
        onView(withText(top)).perform(click());
        onView(withId(R.id.circuitFrame)).perform(swipeLeftTop());
    }

    public static ViewAction swipeDownLeft() {
        return new GeneralSwipeAction(Swipe.SLOW, new CoordinatesProvider() {
            @Override
            public float[] calculateCoordinates(View view) {
                float[] coordinates =  GeneralLocation.TOP_LEFT.calculateCoordinates(view);
                coordinates[0] = coordinates[0] + 100;
                coordinates[1] = coordinates[1] + 300;
                return coordinates;
            }
        },
                new CoordinatesProvider() {
                    @Override
                    public float[] calculateCoordinates(View view) {
                        float[] coordinates =  GeneralLocation.BOTTOM_LEFT.calculateCoordinates(view);
                        coordinates[0] = coordinates[0] + 100;
                        coordinates[1] = coordinates[1] - 300;
                        return coordinates;
                    }
                }, Press.FINGER);
    }
    public static ViewAction swipeRightBottom() {
        return new GeneralSwipeAction(Swipe.SLOW, new CoordinatesProvider() {
            @Override
            public float[] calculateCoordinates(View view) {
                float[] coordinates =  GeneralLocation.BOTTOM_LEFT.calculateCoordinates(view);
                coordinates[0] = coordinates[0] + 100;
                coordinates[1] = coordinates[1] - 300;
                return coordinates;
            }
        },
                new CoordinatesProvider() {
                    @Override
                    public float[] calculateCoordinates(View view) {
                        float[] coordinates =  GeneralLocation.BOTTOM_RIGHT.calculateCoordinates(view);
                        coordinates[0] = coordinates[0] - 100;
                        coordinates[1] = coordinates[1] - 300;
                        return coordinates;
                    }
                }, Press.FINGER);
    }
    public static ViewAction swipeUpRight() {
        CoordinatesProvider cp = new CoordinatesProvider() {
            @Override
            public float[] calculateCoordinates(View view) {
                float[] coordinates =  GeneralLocation.BOTTOM_RIGHT.calculateCoordinates(view);
                coordinates[0] = coordinates[0] - 100;
                coordinates[1] = coordinates[1] - 300;
                return coordinates;
            }
        };
        return new GeneralSwipeAction(Swipe.SLOW, cp,
                new CoordinatesProvider() {
                    @Override
                    public float[] calculateCoordinates(View view) {
                        float[] coordinates =  GeneralLocation.TOP_RIGHT.calculateCoordinates(view);
                        coordinates[0] = coordinates[0] - 100;
                        coordinates[1] = coordinates[1] + 300;
                        return coordinates;
                    }
                }, Press.FINGER);
    }
    public static ViewAction swipeLeftTop() {
        return new GeneralSwipeAction(Swipe.SLOW, new CoordinatesProvider() {
            @Override
            public float[] calculateCoordinates(View view) {
                float[] coordinates =  GeneralLocation.TOP_RIGHT.calculateCoordinates(view);
                coordinates[0] = coordinates[0] - 100;
                coordinates[1] = coordinates[1] + 300;
                return coordinates;
            }
        },
                new CoordinatesProvider() {
                    @Override
                    public float[] calculateCoordinates(View view) {
                        float[] coordinates =  GeneralLocation.TOP_LEFT.calculateCoordinates(view);
                        coordinates[0] = coordinates[0] + 100;
                        coordinates[1] = coordinates[1] + 300;
                        return coordinates;
                    }
                }, Press.FINGER);
    }
}
