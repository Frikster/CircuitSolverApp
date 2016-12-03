package com.cpen321.circuitsolver.usecases;

import android.content.ComponentName;
import android.os.SystemClock;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.rule.ActivityTestRule;

import com.cpen321.circuitsolver.R;
import com.cpen321.circuitsolver.model.SimplePoint;
import com.cpen321.circuitsolver.model.components.CircuitElm;
import com.cpen321.circuitsolver.ui.HomeActivity;
import com.cpen321.circuitsolver.ui.draw.DrawActivity;
import com.cpen321.circuitsolver.util.Constants;

import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.cpen321.circuitsolver.usecases.Util.checkUnits;
import static com.cpen321.circuitsolver.usecases.Util.clickXY;
import static com.cpen321.circuitsolver.usecases.Util.countElem;
import static com.cpen321.circuitsolver.usecases.Util.isToast;
import static com.cpen321.circuitsolver.usecases.Util.midpoint;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.StringStartsWith.startsWith;

/**
 * Created by Cornelis Dirk Haupt on 12/2/2016.
 */

public class UseCase4 {
    private final static String TAG = "UC4";
    public ActivityTestRule<DrawActivity> mDrawActivityRule =
            new ActivityTestRule<>(DrawActivity.class);

    @Rule
    public IntentsTestRule<HomeActivity> mHomeActivityRule =
            new IntentsTestRule<>(HomeActivity.class);

    @Test
    public void drawSimplistCircuitandSolve() {
        onView(withId(R.id.fab_expand_menu_button)).perform(click());
        SystemClock.sleep(1000);
        onView(withText("Draw Circuit")).perform(click());
        SystemClock.sleep(1000);
        intended(hasComponent(new ComponentName(getTargetContext(), DrawActivity.class)));
        DrawSimplistCircuit simplist = new DrawSimplistCircuit("DC Source","Wire","Resistor","Wire"); //// TODO: move to constants

        ArrayList<CircuitElm> circuitElms = mDrawActivityRule.getActivity().getCircuitElms();
        onView(withId(R.id.solveButton)).perform(click());
        onView(withText(startsWith("Solved"))).inRoot(isToast()).check(matches(isDisplayed()));
        // press solve, check toast...
        for(CircuitElm circuitElm : circuitElms) {
            SimplePoint circuitElm_midpoint_coords = midpoint(circuitElm.getP2(),circuitElm.getP1());
            onView(withId(R.id.circuitFrame)).perform(clickXY(circuitElm_midpoint_coords.getX(),
                    circuitElm_midpoint_coords.getY()));
            onView(withId(R.id.componentMenuButton)).check(matches(withText("Change")));
            if(circuitElm.getType() != Constants.WIRE){
                checkUnits(circuitElm);
                onView(withId(R.id.component_value)).perform(replaceText("23.4"));
                onView(withId(R.id.solveButton)).perform(click());
                onView(withText(startsWith("Solved"))).inRoot(isToast()).check(matches(isDisplayed()));
                SystemClock.sleep(2000);
                onView(withId(R.id.component_value)).check(matches(not(withText(Constants.NOTHING))));
                onView(withId(R.id.currentText)).check(matches(not(withText(Constants.NOTHING))));
                onView(withId(R.id.voltageText)).check(matches(not(withText(Constants.NOTHING))));
            }
            else{
                onView(withId(R.id.component_value)).check(matches((withText(Constants.NOTHING))));
                onView(withId(R.id.currentText)).check(matches((withText(Constants.NOTHING))));
                onView(withId(R.id.voltageText)).check(matches((withText(Constants.NOTHING))));
                onView(withId(R.id.units_display)).check(matches(withText(Constants.NOTHING)));
            }
            // check values for each...
        }
        assert(countElem(circuitElms, Constants.DC_VOLTAGE) == 1);
        assert(countElem(circuitElms, Constants.RESISTOR) == 1);
        assert(countElem(circuitElms, Constants.WIRE) == 2);
    }
}
