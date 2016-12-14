package com.cpen321.circuitsolver.usecases;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.SystemClock;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.rule.ActivityTestRule;

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

import java.util.ArrayList;
import java.util.Iterator;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withTagValue;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.cpen321.circuitsolver.usecases.UseCaseConstants.TEST_CIRCUITS_UC3;
import static com.cpen321.circuitsolver.usecases.Util.clickXY;
import static com.cpen321.circuitsolver.usecases.Util.countElem;
import static com.cpen321.circuitsolver.usecases.Util.isToast;
import static com.cpen321.circuitsolver.usecases.Util.midpoint;
import static com.cpen321.circuitsolver.usecases.Util.withStringMatching;
import static org.hamcrest.core.StringStartsWith.startsWith;

/**
 * Created by Cornelis Dirk Haupt on 12/2/2016.
 * Run Use Cases Tests Individually for them to work
 * USE CASE 3: Solve a circuit from an uploaded or taken circuit picture
 */
public class UseCase3 {
    private final static String TAG = "UC3";

    private ActivityTestRule<DrawActivity> mDrawActivityRule =
            new ActivityTestRule<>(DrawActivity.class);

    @Rule
    public IntentsTestRule<HomeActivity> mHomeActivityRule =
            new IntentsTestRule<>(HomeActivity.class);

    @Before
    public void setupBitmaps(){
        Util.deleteAllProjects(mHomeActivityRule.getActivity());
        mHomeActivityRule.getActivity().setCircuitProjects(new ArrayList<CircuitProject>());
        Intent intent = mHomeActivityRule.getActivity().getIntent();
        mHomeActivityRule.getActivity().finish();
        mHomeActivityRule.getActivity().startActivity(intent);
        // Use circuit test cases from constant class.
        // Add more and tests will iterate through all of them
        for (int test_circuit_id : TEST_CIRCUITS_UC3) {
            Bitmap bm = BitmapFactory.decodeResource(
                    mHomeActivityRule.getActivity().getResources(), test_circuit_id);
            Util.createProjectfromBitmap(mHomeActivityRule.getActivity(), bm);
            SystemClock.sleep(2000);
            Espresso.pressBack();
        }
        mHomeActivityRule.getActivity().finish();
        mHomeActivityRule.getActivity().startActivity(intent);
    }

    @Test
    public void eraseAll() {
        getInstrumentation().waitForIdleSync();
        // selectProjectAndOpenIt()
        ArrayList<CircuitProject> circuitProjects = mHomeActivityRule.getActivity().
                getCircuitProjects();
        CircuitProject circuitProject_one = circuitProjects.get(0);
        onView(withTagValue(withStringMatching(circuitProject_one.getFolderID()))).perform(scrollTo(),
                click());
        onView(withId(R.id.processing_fab)).perform(click());


        ArrayList<CircuitElm> circuitElms = mDrawActivityRule.getActivity().getCircuitElms();
        ArrayList<CircuitElm> circuitElms_copy = new ArrayList<>();
        for (CircuitElm circuitElm : circuitElms) {
            circuitElms_copy.add(circuitElm.clone());
        }

        Iterator<CircuitElm> iter = circuitElms_copy.iterator();
        while (iter.hasNext()) {
            CircuitElm circuitElm = iter.next();
            SimplePoint circuitElm_midpoint_coords = midpoint(circuitElm.getP2(), circuitElm.getP1());
            onView(withId(R.id.circuitFrame)).perform(clickXY(circuitElm_midpoint_coords.getX(),
                    circuitElm_midpoint_coords.getY()));
            onView(withId(R.id.eraseButton)).perform(click());
        }
        // Check numer of elements
        assert(countElem(circuitElms, com.cpen321.circuitsolver.util.Constants.DC_VOLTAGE) == 0);
        assert(countElem(circuitElms, Constants.RESISTOR) == 0);
        assert(countElem(circuitElms, Constants.WIRE) == 0);
        // NOTE: a few days ago we dicided to change the UI buttons to have images instead.
        // This breaks the tests since Espresso has problems specifically with our tinted VectorDrawables
        // See here: http://stackoverflow.com/questions/33763425/using-espresso-to-test-drawable-changes
        //onView(withId(R.id.componentMenuButton)).check(matches(withText("Add"))); //todo: constant
        onView(withId(R.id.component_value)).check(matches(withText(Constants.NOTHING)));
        onView(withId(R.id.currentText)).check(matches(withText(Constants.NOTHING)));
        onView(withId(R.id.voltageText)).check(matches(withText(Constants.NOTHING)));
    }

    @Test
    public void modifyMultiple() {
        getInstrumentation().waitForIdleSync();
        // selectProjectAndOpenIt()
        ArrayList<CircuitProject> circuitProjects = mHomeActivityRule.getActivity().
                getCircuitProjects();
        CircuitProject circuitProject_one = circuitProjects.get(0);
        onView(withTagValue(withStringMatching(circuitProject_one.getFolderID()))).perform(scrollTo(),
                click());
        onView(withId(R.id.processing_fab)).perform(click());

        ArrayList<CircuitElm> circuitElms = mDrawActivityRule.getActivity().getCircuitElms();
        int flag = 2;
        // Randomly modify some resistors
        for(CircuitElm circuitElm:circuitElms){
            flag = flag + 1;
            if((flag%2)==0){
                SimplePoint elem_midpoint_coords = midpoint(circuitElm.getP2(),circuitElm.getP1());
                onView(withId(R.id.circuitFrame)).perform(clickXY(elem_midpoint_coords.getX(),
                        elem_midpoint_coords.getY()));
                // NOTE: a few days ago we dicided to change the UI buttons to have images instead.
                // This breaks the tests since Espresso has problems specifically with our tinted VectorDrawables
                // See here: http://stackoverflow.com/questions/33763425/using-espresso-to-test-drawable-changes
                // onView(withId(R.id.componentMenuButton)).check(matches(withText("Change")));
                if(circuitElm.getType() != Constants.DC_VOLTAGE){
                    onView(withId(R.id.componentMenuButton)).perform(click());
                    onView(withText("Resistor")).perform(click()); //todo: move "Resistor" to Constants - make list of all element possibilities cycle through em all
                    SystemClock.sleep(2000);
                    onView(withId(R.id.solveButton)).perform(click());
                    onView(withText(startsWith("Solved"))).inRoot(isToast()).check(matches(isDisplayed()));
                    SystemClock.sleep(2000);
                    onView(withId(R.id.circuitFrame)).perform(clickXY(elem_midpoint_coords.getX(),
                            elem_midpoint_coords.getY()));
                    onView(withId(R.id.component_value)).perform(replaceText("23.4"));
                    onView(withId(R.id.component_value)).check(matches((withText("23.4"))));
                }
            }
        }
    }

    //@Test
    public void autoConnectCloseComponents(){
        // Planned test case showing that when you put one component close to another -> they connect automatically
    }

    //@Test
    public void pinchZoomInOut(){
        // stub - simulating pinch zoom gestures appear to be much more complex
        // than it is worth: http://stackoverflow.com/a/11599282/2734863
        // So this test may be omitted even for the final demo
    }
}
