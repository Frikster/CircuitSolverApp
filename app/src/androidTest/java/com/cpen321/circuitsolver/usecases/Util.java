package com.cpen321.circuitsolver.usecases;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Environment;
import android.support.test.espresso.Root;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.action.CoordinatesProvider;
import android.support.test.espresso.action.GeneralClickAction;
import android.support.test.espresso.action.Press;
import android.support.test.espresso.action.Tap;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.test.rule.ActivityTestRule;
//import android.support.test.uiautomator.UiDevice;
//import android.support.test.uiautomator.UiObject;
//import android.support.test.uiautomator.UiObjectNotFoundException;
//import android.support.test.uiautomator.UiSelector;
import android.util.Log;
import android.view.View;

import com.cpen321.circuitsolver.R;
import com.cpen321.circuitsolver.model.SimplePoint;
import com.cpen321.circuitsolver.model.components.CircuitElm;
import com.cpen321.circuitsolver.ui.HomeActivity;
import com.cpen321.circuitsolver.ui.ProcessingActivity;
import com.cpen321.circuitsolver.ui.draw.DrawActivity;
import com.cpen321.circuitsolver.util.CircuitProject;
import com.cpen321.circuitsolver.util.Constants;
import com.cpen321.circuitsolver.util.ImageUtils;

import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.util.ArrayList;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.core.deps.guava.base.Preconditions.checkNotNull;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.StringStartsWith.startsWith;

/**
 * Created by Cornelis Dirk Haupt on 12/1/2016.
 */

public class Util {
    public static void allowPermissionsIfNeeded() {
//        if (Build.VERSION.SDK_INT >= 23) { // only targets 18-25 :(
//            UiDevice device = UiDevice.getInstance(getInstrumentation());
//            UiObject allowPermissions = device.findObject(new UiSelector().text("Allow"));
//            if (allowPermissions.exists()) {
//                try {
//                    allowPermissions.click();
//                } catch (UiObjectNotFoundException e) {
//                    Log.d("TakePictureTest", "There is no permissions dialog to interact with ");
//                }
//            }
//        }
    }

    public static SimplePoint midpoint(SimplePoint p1, SimplePoint p2) {
        return new SimplePoint((p1.getX() + p2.getX())/2, (p1.getY() + p2.getY())/2);
    }

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

    public static void createProjectfromBitmap(ActivityTestRule<HomeActivity> mHomeActivityRule, Bitmap bm){
        CircuitProject candidateProject = new CircuitProject(ImageUtils.getTimeStamp(),
                mHomeActivityRule.getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES));
        candidateProject.saveOriginalImage(bm);
        Intent analysisIntent = new Intent(mHomeActivityRule.getActivity().getApplicationContext(),
                ProcessingActivity.class);
        analysisIntent.putExtra(Constants.CIRCUIT_PROJECT_FOLDER, candidateProject.getFolderPath());
        Util.allowPermissionsIfNeeded();
        mHomeActivityRule.getActivity().startActivity(analysisIntent);
    }

    public static Matcher<Object> withStringMatching(String expectedText) {
        checkNotNull(expectedText);
        return withStringMatching(equalTo(expectedText));
    }

    @SuppressWarnings("rawtypes")
    public static Matcher<Object> withStringMatching(final Matcher<String> itemTextMatcher) {
        checkNotNull(itemTextMatcher);
        return new BoundedMatcher<Object, String>(String.class) {
            @Override
            public boolean matchesSafely(String string) {
                return itemTextMatcher.matches(string);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("with string: ");
                itemTextMatcher.describeTo(description);
            }
        };
    }

    public static Matcher<Root> isToast() {
        return new ToastMatcher();
    }

    public static CircuitElm getFirstOfType(ArrayList<CircuitElm> circuitElms, String wantedComponent){
        CircuitElm selectedElem = null;
        for(CircuitElm circuitElm : circuitElms) {
            if(circuitElm.getType() == wantedComponent){
                selectedElem = circuitElm;
            }
        }
        if(selectedElem == null){
            return null;
        }
        return selectedElem;
    }

    public static void checkUnits(CircuitElm selectedElem){
        switch (selectedElem.getType()){
            case Constants.RESISTOR: {
                onView(withId(R.id.units_display)).check(matches(withText(Constants.RESISTOR_UNITS)));
                break;
            }
            case Constants.INDUCTOR: {
                onView(withId(R.id.units_display)).check(matches(withText(Constants.INDUCTOR_UNTIS)));
                break;
            }
            case Constants.CAPACITOR: {
                onView(withId(R.id.units_display)).check(matches(withText(Constants.CAPACITOR_UNITS)));
                break;
            }
            case Constants.WIRE: {
                onView(withId(R.id.units_display)).check(matches(withText(Constants.NOTHING)));
                break;
            }
            case Constants.DC_VOLTAGE: {
                onView(withId(R.id.units_display)).check(matches(withText(Constants.VOLTAGE_UNITS)));
                break;
            }
        }
    }

    public static void checkSelectedToast(String selectedElemType, ActivityTestRule<DrawActivity> mDrawActivityRule){
        switch (selectedElemType){
            // Should be changed if you want to test what content is contained in a Toast
            case Constants.RESISTOR: {
//                onView(withText(startsWith("You Clicked"))).
//                        inRoot(withDecorView(not(is(mDrawActivityRule
//                                .getActivity().getWindow().getDecorView())))).
//                        check(matches(isDisplayed()));
//
//                onView(withText(startsWith("You Clicked"))).inRoot(new ToastMatcher())
//                        .check(matches(withText("You Clicked : " + Constants.RESISTOR)));
                onView(withText(startsWith("You Clicked"))).inRoot(isToast()).check(matches(isDisplayed()));
                break;
            }
            case Constants.INDUCTOR: {
                onView(withText(startsWith("You Clicked"))).inRoot(isToast()).check(matches(isDisplayed()));
                break;
            }
            case Constants.CAPACITOR: {
                onView(withText(startsWith("You Clicked"))).inRoot(isToast()).check(matches(isDisplayed()));
                break;
            }
            case Constants.WIRE: {
                onView(withText(startsWith("You Clicked"))).inRoot(isToast()).check(matches(isDisplayed()));
                break;
            }
            case Constants.DC_VOLTAGE: {
//                Log.e("toast", withText(startsWith("You Clicked")).toString());
//                Log.e("toast", (onView(withText(startsWith("You Clicked"))).
//                        inRoot(withDecorView(not(mDrawActivityRule
//                                .getActivity().getWindow().getDecorView())))).toString());
//                Log.e("toast", withDecorView(not(mDrawActivityRule
//                        .getActivity().getWindow().getDecorView())).toString());
//                onView(withText(startsWith("You Clicked"))).
//                        inRoot(withDecorView(not(mDrawActivityRule
//                                .getActivity().getWindow().getDecorView()))).
//                        check(matches(isDisplayed()));


//                onView(withText(startsWith("You Clicked"))).inRoot(new ToastMatcher())
//                        .check(matches(withText("You Clicked : " + Constants.DC_VOLTAGE)));
                onView(withText(startsWith("You Clicked"))).inRoot(isToast()).check(matches(isDisplayed()));
                break;
            }
        }
    }

    // Count the number of components in the circuitProject
    public static int countElem(ArrayList<CircuitElm> circuitElms, String elemType){
        int count = 0;
        for(CircuitElm circuitElm:circuitElms){
               if(circuitElm.getType()==elemType){
                   count = count + 1;
               }
        }
        return count;
    }
}
