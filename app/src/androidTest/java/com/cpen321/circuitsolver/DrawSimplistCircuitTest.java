package com.cpen321.circuitsolver;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.action.CoordinatesProvider;
import android.support.test.espresso.action.GeneralClickAction;
import android.support.test.espresso.action.GeneralLocation;
import android.support.test.espresso.action.GeneralSwipeAction;
import android.support.test.espresso.action.Press;
import android.support.test.espresso.action.Swipe;
import android.support.test.espresso.action.Tap;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import com.cpen321.circuitsolver.model.components.ResistorElm;
import com.cpen321.circuitsolver.ui.draw.CircuitView;
import com.cpen321.circuitsolver.ui.draw.DrawActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static java.security.AccessController.getContext;

/**
 * Created by Cornelis Dirk Haupt on 11/28/2016.
 * Crucial code retrieved from: https://github.com/gilvegliach/StackOverflowAnswers/blob/master/EspressoAssertWhilePressed/app/src/androidTest/java/it/gilvegliach/learning/espressoassertwhilepressed/AssertWhilePressedTest.java
 */
@RunWith(AndroidJUnit4.class)
public class DrawSimplistCircuitTest {
    private final static String TAG = "DrawSimplistCircuitTest";

    private float[] test_coordinates;

//    @Rule
//    public ActivityTestRule<HomeActivity> mHomeActivityRule =
//            new ActivityTestRule<>(HomeActivity.class);
    @Rule
    public ActivityTestRule<DrawActivity> mDrawActivityRule =
            new ActivityTestRule<>(DrawActivity.class);

    @Test
    public void simpleCircuitTest() {
        onView(withId(R.id.componentMenuButton)).perform(click());
        onView(withText("DC Source")).perform(click());
        onView(withId(R.id.circuitFrame)).perform(swipeDownLeft());

        onView(withId(R.id.componentMenuButton)).perform(click());
        onView(withText("Wire")).perform(click());
        onView(withId(R.id.circuitFrame)).perform(swipeRightBottom());

        onView(withId(R.id.componentMenuButton)).perform(click());
        onView(withText("Resistor")).perform(click());
        onView(withId(R.id.circuitFrame)).perform(swipeUpRight());

        onView(withId(R.id.componentMenuButton)).perform(click());
        onView(withText("Wire")).perform(click());
        onView(withId(R.id.circuitFrame)).perform(swipeLeftTop());

        float[] coordinates =  GeneralLocation.BOTTOM_RIGHT.calculateCoordinates(view);
        coordinates[0] = coordinates[0] - 100;
        coordinates[1] = coordinates[1] - 300;

        onView(withId(R.id.circuitFrame)).perform(clickXY());
        onView(withId(R.id.solveButton)).perform(click());

//        onView(withId(R.id.draw_fab)).check(matches(isDisplayed()));
//        View v = mHomeActivityRule.getActivity().findViewById(R.id.draw_fab);
//        Log.d(TAG, String.valueOf(v.getVisibility()==v.VISIBLE));
//        Log.d(TAG, String.valueOf(v.isShown()));
//        Log.d(TAG, String.valueOf(v.isEnabled()));
//
//        onView(withId(R.id.multiple_actions)).perform(click());
//        onView(withId(R.id.capture_fab)).check(matches(isDisplayed()));
//        Log.d(TAG, String.valueOf(v.getVisibility()==v.VISIBLE));
//        Log.d(TAG, String.valueOf(v.isShown()));
//        Log.d(TAG, String.valueOf(v.isEnabled()));
//
//
//        onView(withId(R.id.draw_fab)).perform(pressAndHold());
//        onView(withId(R.id.componentMenuButton)).perform(click());
//        onView(withId(R.id.dropDownSourceButton)).perform(click());
//        onView(withId(R.id.circuitFrame)).perform(swipeDown());
    }

//    @Test
//    public void testAssertWhilePressed() {
//        onView(withId(R.id.button)).perform(pressAndHold());
//        onView(withId(R.id.text)).check(matches(withText("Button is held down")));
//        onView(withId(R.id.button)).perform(release());
//    }

//    public static ViewAction swipeDownLeft() {
//        Log.d(TAG, GeneralLocation.TOP_LEFT.toString());
//        Log.d(TAG, GeneralLocation.BOTTOM_LEFT.toString());
//        return new GeneralSwipeAction(Swipe.SLOW, GeneralLocation.TOP_LEFT,
//                GeneralLocation.BOTTOM_LEFT, Press.FINGER);
//    }

    public static ViewAction clickXY(final int x, final int y){
        return new GeneralClickAction(
                Tap.SINGLE,
                new CoordinatesProvider() {
                    @Override
                    public float[] calculateCoordinates(View view) {

                        final int[] screenPos = new int[2];
                        view.getLocationOnScreen(screenPos);

                        final float screenX = screenPos[0] + x;
                        final float screenY = screenPos[1] + y;
                        float[] coordinates = {screenX, screenY};

                        return coordinates;
                    }
                },
                Press.FINGER);
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
        float[] swipeUpRight_coordinates;

        CoordinatesProvider cp = new CoordinatesProvider() {
            @Override
            public float[] calculateCoordinates(View view) {
                float[] coordinates =  GeneralLocation.BOTTOM_RIGHT.calculateCoordinates(view);
                coordinates[0] = coordinates[0] - 100;
                coordinates[1] = coordinates[1] - 300;
                return coordinates;
            }
        };
        swipeUpRight_coordinates = cp.calculateCoordinates(mDrawActivityRule);

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

//    public static ViewAction customClick() {
//        return actionWithAssertions(
//                new CustomGeneralClickAction(Tap.SINGLE, GeneralLocation.VISIBLE_CENTER, Press.FINGER));
//    }
}
