package com.cpen321.circuitsolver.usecases;

import android.content.ComponentName;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.SystemClock;
import android.support.test.espresso.Root;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.espresso.matcher.RootMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.MotionEvent;
import android.widget.EditText;
import android.widget.TextView;

import com.cpen321.circuitsolver.R;
import com.cpen321.circuitsolver.model.SimplePoint;
import com.cpen321.circuitsolver.model.components.CircuitElm;
import com.cpen321.circuitsolver.ui.HomeActivity;
import com.cpen321.circuitsolver.ui.draw.DrawActivity;
import com.cpen321.circuitsolver.util.CircuitProject;
import com.cpen321.circuitsolver.util.Constants;

import org.hamcrest.Matcher;
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
import static com.cpen321.circuitsolver.usecases.Util.checkSelectedToast;
import static com.cpen321.circuitsolver.usecases.Util.checkUnits;
import static com.cpen321.circuitsolver.usecases.Util.clickXY;
import static com.cpen321.circuitsolver.usecases.Util.countElem;
import static com.cpen321.circuitsolver.usecases.Util.getFirstOfType;
import static com.cpen321.circuitsolver.usecases.Util.isToast;
import static com.cpen321.circuitsolver.usecases.Util.midpoint;
import static com.cpen321.circuitsolver.usecases.Util.withStringMatching;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.core.IsNot.not;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static org.hamcrest.core.StringStartsWith.startsWith;


/**
 * Created by Cornelis Dirk Haupt on 12/1/2016.
 */
@RunWith(AndroidJUnit4.class)
public class UseCase1 {
    private final static String TAG = "UC1";

    private ActivityTestRule<DrawActivity> mDrawActivityRule =
            new ActivityTestRule<>(DrawActivity.class);

    @Rule
    public IntentsTestRule <HomeActivity> mHomeActivityRule =
            new IntentsTestRule<>(HomeActivity.class);

    @Before
    public void sendBitmapifNoneExists(){
        // todo: possibly populate with list of bitmaps
        if(mHomeActivityRule.getActivity().getCircuitProjects().size() == 0){
            Bitmap bm = BitmapFactory.decodeResource(
                    mHomeActivityRule.getActivity().getResources(), R.drawable.example_1);
            Util.createProjectfromBitmap(mHomeActivityRule, bm);
            mHomeActivityRule.getActivity().recreate();
        }
    }

    @Test
    public void selectProjectAndOpenIt(){
        // get circuitProjects
        ArrayList<CircuitProject> circuitProjects = mHomeActivityRule.getActivity().
                getCircuitProjects();
        CircuitProject circuitProject_one = circuitProjects.get(0);
        onView(withTagValue(withStringMatching(circuitProject_one.getFolderID()))).perform(scrollTo(),
                click());
        onView(withId(R.id.processing_fab)).perform(click());
        //Log.e(TAG,getTargetContext().getClass().toString());
        //Log.e(TAG, hasComponent(new ComponentName(getTargetContext(), DrawActivity.class)).toString());
        intended(hasComponent(new ComponentName(getTargetContext(), DrawActivity.class)));
    }

    @Test
    public void selectComponent(){
        // - - Click component: turn red, "Add" -> "Change", bottom value matches component
        // get circuitProjects
        ArrayList<CircuitProject> circuitProjects = mHomeActivityRule.getActivity().
                getCircuitProjects();
        CircuitProject circuitProject_one = circuitProjects.get(0);
        onView(withTagValue(withStringMatching(circuitProject_one.getFolderID()))).perform(scrollTo(),
                click());
        onView(withId(R.id.processing_fab)).perform(click());

        ArrayList<CircuitElm> circuitElms = mDrawActivityRule.getActivity().getCircuitElms();
        CircuitElm selectedElem = circuitElms.get(0);
        SimplePoint elem_midpoint_coords = midpoint(selectedElem.getP2(),selectedElem.getP1());
        onView(withId(R.id.circuitFrame)).perform(clickXY(elem_midpoint_coords.getX(),
                elem_midpoint_coords.getY()));

        // Check changes
        onView(withId(R.id.componentMenuButton)).check(matches(withText("Change"))); //todo: move "Change/Add to constants"
        onView(withId(R.id.component_value)).check(matches(withText(not(Constants.NOTHING))));
        checkUnits(selectedElem);
        onView(withId(R.id.currentText)).check(matches(withText(Constants.NOTHING)));
        onView(withId(R.id.voltageText)).check(matches(withText(Constants.NOTHING)));

    }

    @Test
    public void changeComponent(){
        // - Change component ->  bottom value matches component + component change
        // get circuitProjects
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
        checkSelectedToast(Constants.RESISTOR, mDrawActivityRule);
        assert(countElem(circuitElms, Constants.RESISTOR) == counts_before_res + 1);
        assert(countElem(circuitElms, Constants.DC_VOLTAGE) == counts_before_src - 1);

        onView(withId(R.id.circuitFrame)).perform(clickXY(res_midpoint_coords.getX(),
                res_midpoint_coords.getY()));
        onView(withId(R.id.componentMenuButton)).perform(click());
        onView(withText("DC Source")).perform(click()); //todo: move "Resistor" to Constants - make list of all element possibilities cycle through em all
        checkSelectedToast(Constants.DC_VOLTAGE, mDrawActivityRule);
        assert(countElem(circuitElms, Constants.RESISTOR) == counts_before_res);
        assert(countElem(circuitElms, Constants.DC_VOLTAGE) == counts_before_src);


        // Check changes
        onView(withId(R.id.componentMenuButton)).check(matches(withText("Add")));
        onView(withId(R.id.units_display)).check(matches(withText(Constants.NOTHING)));
        onView(withId(R.id.component_value)).check(matches(withText(Constants.NOTHING)));
        onView(withId(R.id.currentText)).check(matches(withText(Constants.NOTHING)));
        onView(withId(R.id.voltageText)).check(matches(withText(Constants.NOTHING)));

        //        CircuitElm selectedWire = getFirstOfType(circuitElms, Constants.WIRE);
//        CircuitElm selectedDC_Voltage = getFirstOfType(circuitElms, Constants.DC_VOLTAGE);
//        CircuitElm selectedResistor = getFirstOfType(circuitElms, Constants.RESISTOR);
//        if(selectedElem == null){
//            assert(false); //this can be improved with a better exception.
//        }
    }

    @Test
    public void changeComponentandSolve(){
        // - Change component value and solve -> see update, "solved!" appears (http://stackoverflow.com/a/28606603/2734863), + display current direction
        // - Change component ->  bottom value matches component + component change
        // get circuitProjects
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
        checkSelectedToast(Constants.RESISTOR, mDrawActivityRule);
        SystemClock.sleep(2000);
        assert(countElem(circuitElms, Constants.RESISTOR) == counts_before_res + 1);
        assert(countElem(circuitElms, Constants.DC_VOLTAGE) == counts_before_src - 1);

        onView(withId(R.id.circuitFrame)).perform(clickXY(res_midpoint_coords.getX(),
                res_midpoint_coords.getY()));
        onView(withId(R.id.componentMenuButton)).perform(click());
        onView(withText("DC Source")).perform(click()); //todo: move "Resistor" to Constants - make list of all element possibilities cycle through em all
        checkSelectedToast(Constants.DC_VOLTAGE, mDrawActivityRule);
        SystemClock.sleep(2000);
        assert(countElem(circuitElms, Constants.RESISTOR) == counts_before_res);
        assert(countElem(circuitElms, Constants.DC_VOLTAGE) == counts_before_src);

        onView(withId(R.id.circuitFrame)).perform(clickXY(dc_midpoint_coords.getX(),
                dc_midpoint_coords.getY()));
        SystemClock.sleep(2000);
        onView(withId(R.id.solveButton)).perform(click());
        // Check changes
        onView(withText(startsWith("Solved"))).inRoot(isToast()).check(matches(isDisplayed()));
        onView(withId(R.id.componentMenuButton)).check(matches(withText("Change")));
        onView(withId(R.id.component_value)).check(matches(not(withText(Constants.NOTHING))));
        onView(withId(R.id.currentText)).check(matches(not(withText(Constants.NOTHING))));
        onView(withId(R.id.voltageText)).check(matches(not(withText(Constants.NOTHING))));

        //        SimplePoint elem_midpoint_coords = midpoint(selectedElem.getP2(),selectedElem.getP1());
//        SystemClock.sleep(1000);
//        onView(withId(R.id.circuitFrame)).perform(clickXY(elem_midpoint_coords.getX(),
//                elem_midpoint_coords.getY()));
//        onView(withId(R.id.componentMenuButton)).perform(click());
//        onView(withText("Resistor")).perform(click());
//        onView(withId(R.id.circuitFrame)).perform(clickXY(elem_midpoint_coords.getX(),
//                elem_midpoint_coords.getY()));
    }




    @Test
    public void changeComponentandSolveUnsolvable(){
        //- Circuit unsolvable click solve: Unsolvable toast appears
        // Unsolvable = two voltage sources in parallel and...?
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
        onView(withText(startsWith("Invalid"))).inRoot(isToast()).check(matches(isDisplayed()));
        onView(withId(R.id.componentMenuButton)).check(matches(withText("Change")));
        onView(withId(R.id.component_value)).check(matches(not(withText(Constants.NOTHING))));
        onView(withId(R.id.currentText)).check(matches(not(withText(Constants.NOTHING))));
        onView(withId(R.id.voltageText)).check(matches(not(withText(Constants.NOTHING))));

        int flag = 2;
        // Randomly restore some resistors
        for(CircuitElm circuitElm:circuitElms){
            flag = flag + 1;
            if(circuitElm.getType() == Constants.DC_VOLTAGE){
                if((flag%2)==0){
                    elem_midpoint_coords = midpoint(circuitElm.getP2(),circuitElm.getP1());
                    onView(withId(R.id.circuitFrame)).perform(clickXY(elem_midpoint_coords.getX(),
                            elem_midpoint_coords.getY()));
                    onView(withId(R.id.componentMenuButton)).perform(click());
                    onView(withText("Resistor")).perform(click()); //todo: move "Resistor" to Constants - make list of all element possibilities cycle through em all
                    SystemClock.sleep(2000);
                }
            }
        }
    }
}
