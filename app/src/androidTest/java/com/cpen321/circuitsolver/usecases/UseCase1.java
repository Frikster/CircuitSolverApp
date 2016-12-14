package com.cpen321.circuitsolver.usecases;

import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.SystemClock;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.cpen321.circuitsolver.R;
import com.cpen321.circuitsolver.model.SimplePoint;
import com.cpen321.circuitsolver.model.components.CircuitElm;
import com.cpen321.circuitsolver.ui.HomeActivity;
import com.cpen321.circuitsolver.ui.draw.DrawActivity;
import com.cpen321.circuitsolver.util.CircuitProject;
import com.cpen321.circuitsolver.util.Constants;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withTagValue;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.cpen321.circuitsolver.usecases.UseCaseConstants.TEST_CIRCUITS_UC1;
import static com.cpen321.circuitsolver.usecases.Util.checkUnits;
import static com.cpen321.circuitsolver.usecases.Util.clickXY;
import static com.cpen321.circuitsolver.usecases.Util.countElem;
import static com.cpen321.circuitsolver.usecases.Util.getFirstOfType;
import static com.cpen321.circuitsolver.usecases.Util.isToast;
import static com.cpen321.circuitsolver.usecases.Util.midpoint;
import static com.cpen321.circuitsolver.usecases.Util.withStringMatching;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.StringStartsWith.startsWith;


/**
 * Created by Cornelis Dirk Haupt on 12/1/2016.
 * USE CASE 1: Edit a previous saved circuit in order to change some components or change numerical values.
 * ASSUMPTION: Circuits defined in TEST_CIRCUITS_UC1 are legal
 */
@RunWith(AndroidJUnit4.class)
public class UseCase1{
    private final static String TAG = "UC1";

    private ActivityTestRule<DrawActivity> mDrawActivityRule =
            new ActivityTestRule<>(DrawActivity.class);

    @Rule
    public IntentsTestRule <HomeActivity> mHomeActivityRule =
            new IntentsTestRule<>(HomeActivity.class);

    private static boolean ranOnce = false;
    @Before
    public void setupBitmaps(){
        if(!ranOnce) {
            Util.deleteAllProjects(mHomeActivityRule.getActivity());
            mHomeActivityRule.getActivity().setCircuitProjects(new ArrayList<CircuitProject>());
            Intent intent = mHomeActivityRule.getActivity().getIntent();
            mHomeActivityRule.getActivity().finish();
            mHomeActivityRule.getActivity().startActivity(intent);
            // Use circuit test cases from constant class.
            // Add more and tests will iterate through all of them
            for (int test_circuit_id : TEST_CIRCUITS_UC1) {
                Bitmap bm = BitmapFactory.decodeResource(
                        mHomeActivityRule.getActivity().getResources(), test_circuit_id);
                Util.createProjectfromBitmap(mHomeActivityRule.getActivity(), bm);
                Espresso.pressBack();
            }
            mHomeActivityRule.getActivity().finish();
            mHomeActivityRule.getActivity().startActivity(intent);
        }
        ranOnce = true;
    }

    @Test
    public void selectProjectAndOpenIt(){
        ArrayList<CircuitProject> circuitProjects = mHomeActivityRule.getActivity().
                getCircuitProjects();
        CircuitProject circuitProject_one = circuitProjects.get(0);
        onView(withTagValue(withStringMatching(circuitProject_one.getFolderID()))).perform(scrollTo(),
                click());
        onView(withId(R.id.processing_fab)).perform(click());
        intended(hasComponent(new ComponentName(getTargetContext(), DrawActivity.class)));
    }

    @Test
    public void selectComponent(){
        ArrayList<CircuitProject> circuitProjects = mHomeActivityRule.getActivity().
                getCircuitProjects();
        CircuitProject circuitProject_one = circuitProjects.get(0);
        Log.e(TAG,circuitProject_one.getFolderID());
        onView(withTagValue(withStringMatching(circuitProject_one.getFolderID()))).perform(scrollTo(),
                click());
        onView(withId(R.id.processing_fab)).perform(click());

        ArrayList<CircuitElm> circuitElms = mDrawActivityRule.getActivity().getCircuitElms();
        CircuitElm selectedElem = circuitElms.get(0);
        SimplePoint elem_midpoint_coords = midpoint(selectedElem.getP2(),selectedElem.getP1());
        onView(withId(R.id.circuitFrame)).perform(clickXY(elem_midpoint_coords.getX(),
                elem_midpoint_coords.getY()));

        // Check changes
        // NOTE: a few days ago we dicided to change the UI buttons to have images instead.
        // This breaks the tests since Espresso has problems specifically with our tinted VectorDrawables
        // See here: http://stackoverflow.com/questions/33763425/using-espresso-to-test-drawable-changes
        //onView(withId(R.id.componentMenuButton)).check(matches(withCompoundDrawable(R.drawable.ic_swap)));
        //onView(withCompoundDrawable(R.drawable.ic_swap)).check(matches(isDisplayed()));
        onView(withId(R.id.component_value)).check(matches(withText(not(Constants.NOTHING))));
        checkUnits(selectedElem);
        onView(withId(R.id.currentText)).check(matches(withText(Constants.NOTHING)));
        onView(withId(R.id.voltageText)).check(matches(withText(Constants.NOTHING)));

    }

    @Test
    public void changeComponent(){
        ArrayList<CircuitProject> circuitProjects = mHomeActivityRule.getActivity().
                getCircuitProjects();
        CircuitProject circuitProject_one = circuitProjects.get(0);
        onView(withTagValue(withStringMatching(circuitProject_one.getFolderID()))).perform(scrollTo(),
                click());
        onView(withId(R.id.processing_fab)).perform(click());
        ArrayList<CircuitElm> circuitElms = mDrawActivityRule.getActivity().getCircuitElms();
        CircuitElm selectedDC_Voltage = getFirstOfType(circuitElms, Constants.DC_VOLTAGE);
        CircuitElm selectedResistor = getFirstOfType(circuitElms, Constants.RESISTOR);
        SimplePoint dc_midpoint_coords = midpoint(selectedDC_Voltage.getP2(),selectedDC_Voltage.getP1());
        SimplePoint res_midpoint_coords = midpoint(selectedResistor.getP2(),selectedResistor.getP1());

        int counts_before_src = countElem(circuitElms, Constants.DC_VOLTAGE);
        int counts_before_res = countElem(circuitElms, Constants.RESISTOR);

        // Swap the dc and resistor
        onView(withId(R.id.circuitFrame)).perform(clickXY(dc_midpoint_coords.getX(),
                dc_midpoint_coords.getY()));
        onView(withId(R.id.componentMenuButton)).perform(click());
        onView(withText("Resistor")).perform(click()); //todo: move "Resistor" to Constants - make list of all element possibilities cycle through em all
        onView(withText(startsWith("Drag"))).inRoot(isToast()).check(matches(isDisplayed()));
        //checkSelectedToast(Constants.RESISTOR, mDrawActivityRule);
        assert(countElem(circuitElms, Constants.RESISTOR) == counts_before_res + 1);
        assert(countElem(circuitElms, Constants.DC_VOLTAGE) == counts_before_src - 1);

        onView(withId(R.id.circuitFrame)).perform(clickXY(res_midpoint_coords.getX(),
                res_midpoint_coords.getY()));
        onView(withId(R.id.componentMenuButton)).perform(click());
        onView(withText("DC Source")).perform(click()); //todo: move "Resistor" to Constants - make list of all element possibilities cycle through em all
        onView(withText(startsWith("Drag"))).inRoot(isToast()).check(matches(isDisplayed()));
        //checkSelectedToast(Constants.DC_VOLTAGE, mDrawActivityRule);
        assert(countElem(circuitElms, Constants.RESISTOR) == counts_before_res);
        assert(countElem(circuitElms, Constants.DC_VOLTAGE) == counts_before_src);

        // Check changes
        // NOTE: a few days ago we dicided to change the UI buttons to have images instead.
        // This breaks the tests since Espresso has problems specifically with our tinted VectorDrawables
        // See here: http://stackoverflow.com/questions/33763425/using-espresso-to-test-drawable-changes
        //onView(withId(R.id.componentMenuButton)).check(matches(withText("Add")));
        onView(withId(R.id.units_display)).check(matches(withText(Constants.NOTHING)));
        onView(withId(R.id.component_value)).check(matches(withText(Constants.NOTHING)));
        onView(withId(R.id.currentText)).check(matches(withText(Constants.NOTHING)));
        onView(withId(R.id.voltageText)).check(matches(withText(Constants.NOTHING)));
    }

    @Test
    public void changeComponentandSolve(){
        ArrayList<CircuitElm> circuitElms = mDrawActivityRule.getActivity().getCircuitElms();
        CircuitElm selectedDC_Voltage = getFirstOfType(circuitElms, Constants.DC_VOLTAGE);
        CircuitElm selectedResistor = getFirstOfType(circuitElms, Constants.RESISTOR);
        SimplePoint dc_midpoint_coords = midpoint(selectedDC_Voltage.getP2(),selectedDC_Voltage.getP1());
        changeComponent();
        onView(withId(R.id.circuitFrame)).perform(clickXY(dc_midpoint_coords.getX(),
                dc_midpoint_coords.getY()));
        SystemClock.sleep(2000);
        onView(withId(R.id.solveButton)).perform(click());

        // Check changes
        // NOTE: a few days ago we dicided to change the UI buttons to have images instead.
        // This breaks the tests since Espresso has problems specifically with our tinted VectorDrawables
        // See here: http://stackoverflow.com/questions/33763425/using-espresso-to-test-drawable-changes
        //onView(withText(startsWith("Solved"))).inRoot(isToast()).check(matches(isDisplayed()));
        //onView(withId(R.id.componentMenuButton)).check(matches(withText("Change")));
        onView(withId(R.id.component_value)).check(matches(not(withText(Constants.NOTHING))));
        onView(withId(R.id.currentText)).check(matches(not(withText(""))));
        onView(withId(R.id.voltageText)).check(matches(not(withText(""))));
    }


    @Test
    public void changeComponentandSolveUnsolvable(){
        ArrayList<CircuitProject> circuitProjects = mHomeActivityRule.getActivity().
                getCircuitProjects();
        CircuitProject circuitProject_one = circuitProjects.get(0);
        onView(withTagValue(withStringMatching(circuitProject_one.getFolderID()))).perform(scrollTo(),
                click());
        onView(withId(R.id.processing_fab)).perform(click());
        ArrayList<CircuitElm> circuitElms = mDrawActivityRule.getActivity().getCircuitElms();

        // Create a state where there will always be an unsolvable circuit, namely everything being a voltage source
        for(CircuitElm circuitElm:circuitElms){
            if(circuitElm.getType() == Constants.RESISTOR){
                SimplePoint elem_midpoint_coords = midpoint(circuitElm.getP2(),circuitElm.getP1());
                onView(withId(R.id.circuitFrame)).perform(clickXY(elem_midpoint_coords.getX(),
                        elem_midpoint_coords.getY()));
                onView(withId(R.id.componentMenuButton)).perform(click());
                onView(withText("DC Source")).perform(click()); //todo: move "Resistor" to Constants - make list of all element possibilities cycle through em all
                SystemClock.sleep(2000);
            }
        }
        CircuitElm selectedCircuitElm = circuitElms.get(0);
        SimplePoint elem_midpoint_coords = midpoint(selectedCircuitElm.getP2(),selectedCircuitElm.getP1());
        onView(withId(R.id.circuitFrame)).perform(clickXY(elem_midpoint_coords.getX(),
                elem_midpoint_coords.getY()));
        // Solve the illegal circuit
        SystemClock.sleep(2000);
        onView(withId(R.id.solveButton)).perform(click());

        // Check changes
        onView(withText(startsWith("Invalid"))).inRoot(isToast()).check(matches(isDisplayed())); //todo: move Invalid to constants
        // NOTE: a few days ago we dicided to change the UI buttons to have images instead.
        // This breaks the tests since Espresso has problems specifically with our tinted VectorDrawables
        // See here: http://stackoverflow.com/questions/33763425/using-espresso-to-test-drawable-changes
        //onView(withText(startsWith("Solved"))).inRoot(isToast()).check(matches(isDisplayed()));
        //onView(withId(R.id.componentMenuButton)).check(matches(withText("Change")));
        //onView(withId(R.id.componentMenuButton)).check(matches(withText("Change")));
        onView(withId(R.id.component_value)).check(matches(not(withText(Constants.NOTHING)))); //todo: default value is not in constants
        onView(withId(R.id.currentText)).check(matches((withText(""))));
        onView(withId(R.id.voltageText)).check(matches((withText(""))));

        // Change all components to resistors except for one
        for(CircuitElm circuitElm:circuitElms){
            if(circuitElm.getType() == Constants.DC_VOLTAGE){
                elem_midpoint_coords = midpoint(circuitElm.getP2(),circuitElm.getP1());
                onView(withId(R.id.circuitFrame)).perform(clickXY(elem_midpoint_coords.getX(),
                        elem_midpoint_coords.getY()));
                onView(withId(R.id.componentMenuButton)).perform(click());
                onView(withText("Resistor")).perform(click()); //todo: move "Resistor" to Constants - make list of all element possibilities cycle through em all
                SystemClock.sleep(2000);
            }
        }
        CircuitElm selectedResistor = getFirstOfType(circuitElms, Constants.RESISTOR);
        SimplePoint res_midpoint_coords = midpoint(selectedResistor.getP2(),selectedResistor.getP1());
        onView(withId(R.id.circuitFrame)).perform(clickXY(res_midpoint_coords.getX(),
                res_midpoint_coords.getY()));
        onView(withId(R.id.componentMenuButton)).perform(click());
        onView(withText("DC Source")).perform(click()); //todo: move "Resistor" to Constants - make list of all element possibilities cycle through em all
    }
}
