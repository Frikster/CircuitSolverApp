package com.cpen321.circuitsolver.workingEspresso;

import android.os.SystemClock;
import android.support.test.espresso.Espresso;
import android.support.test.rule.ActivityTestRule;
import android.util.Log;
import android.view.View;

import com.cpen321.circuitsolver.R;
import com.cpen321.circuitsolver.ui.HomeActivity;

import org.junit.Rule;
import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Created by Cornelis Dirk Haupt on 11/29/2016.
 */

public class FABtoDrawTests {

    private final static String TAG = "DrawSimplistCircuitTest";

    @Rule
    public ActivityTestRule<HomeActivity> mHomeActivityRule =
            new ActivityTestRule<>(HomeActivity.class);


    @Test
    public void drawFABtest() {
        View v = mHomeActivityRule.getActivity().findViewById(R.id.multiple_actions);
        Log.d(TAG, String.valueOf(v.getVisibility()==v.VISIBLE));
        Log.d(TAG, String.valueOf(v.isShown()));
        Log.d(TAG, String.valueOf(v.isEnabled()));

        org.hamcrest.Matcher<View> debugThis = withId(R.id.multiple_actions);
        Log.d(TAG, String.valueOf(withId(R.id.multiple_actions)));

        onView(withId(R.id.fab_expand_menu_button)).perform(click());
        SystemClock.sleep(1000);
        onView(withText("Draw Circuit")).perform(click());
        SystemClock.sleep(1000);
        Espresso.pressBack();
        SystemClock.sleep(5000);


    }
}
