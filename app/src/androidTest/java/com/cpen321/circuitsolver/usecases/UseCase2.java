package com.cpen321.circuitsolver.usecases;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.SystemClock;
import android.support.test.annotation.UiThreadTest;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.rule.ActivityTestRule;
import android.widget.LinearLayout;

import com.cpen321.circuitsolver.R;
import com.cpen321.circuitsolver.ui.HomeActivity;
import com.cpen321.circuitsolver.ui.ProcessingActivity;
import com.cpen321.circuitsolver.ui.draw.DrawActivity;
import com.cpen321.circuitsolver.util.CircuitProject;
import com.cpen321.circuitsolver.util.Constants;
import com.cpen321.circuitsolver.util.ImageUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.matcher.ViewMatchers.isClickable;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withTagValue;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.cpen321.circuitsolver.usecases.Util.allowPermissionsIfNeeded;
import static com.cpen321.circuitsolver.usecases.Util.withStringMatching;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;

/**
 * Created by Cornelis Dirk Haupt on 12/2/2016.
 */

public class UseCase2 {
    private final static String TAG = "UC2";
    private static int initialProjectCount = 0;
//
//    private IntentsTestRule<DrawActivity> mDrawActivityRule =
//            new IntentsTestRule<>(DrawActivity.class);
    private ActivityTestRule<ProcessingActivity> mProcessingActivityRule =
            new ActivityTestRule<>(ProcessingActivity.class);

    @Rule
    public IntentsTestRule<HomeActivity> mHomeActivityRule =
            new IntentsTestRule<>(HomeActivity.class);

    @Before
    public void sendBitmap(){
        ArrayList<CircuitProject> circuitProjects = mHomeActivityRule.getActivity().
                getCircuitProjects();
        initialProjectCount = circuitProjects.size();
        // todo: possibly populate with list of bitmaps
        CircuitProject candidateProject = new CircuitProject(ImageUtils.getTimeStamp(),
                mHomeActivityRule.getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES));
        Bitmap bm = BitmapFactory.decodeResource(
                mHomeActivityRule.getActivity().getResources(), R.drawable.example_1);
        candidateProject.saveOriginalImage(bm);
        Intent analysisIntent = new Intent(mHomeActivityRule.getActivity().getApplicationContext(),
                ProcessingActivity.class);
        analysisIntent.putExtra(Constants.CIRCUIT_PROJECT_FOLDER, candidateProject.getFolderPath());
        allowPermissionsIfNeeded();
        mHomeActivityRule.getActivity().startActivity(analysisIntent);
    }

    @UiThreadTest
    public void restartActivity(Activity activity){
        onView(withId(android.R.id.home)).perform(click());

//        onView(isRoot()).perform(ViewActions.pressMenuKey());
//        getInstrumentation().callActivityOnRestart(activity);

//        onView(withText(R.string.options_item_3_text)).check(matches(isDisplayed())).perform(click());
//        onView(withId(R.id.text_menu_result)).check(matches(withText(R.string.options_item_3_text)));
//        getInstrumentation().callActivityOnPause(mHomeActivityRule.getActivity());
//        SystemClock.sleep(1000);
//        getInstrumentation().callActivityOnResume(mHomeActivityRule.getActivity());
//
//        getInstrumentation().callActivityOnRestart(activity);
    }

  //  @Test
    public void returnToProcessing() {
        // - - Return to application on Processing or DrawActivity: http://stackoverflow.com/a/10307126/2734863: Check that circuit is    }
        // make activity falling into restart phase:
        SystemClock.sleep(1000);
        restartActivity(mProcessingActivityRule.getActivity());
        SystemClock.sleep(2000);
        //onView(isRoot()).perform(ViewActions.pressMenuKey());

//        SystemClock.sleep(2000);
//        onView(withId(R.id.fab_expand_menu_button)).perform(click());
        //getInstrumentation().callActivityOnPause(mHomeActivityRule.getActivity());
//        SystemClock.sleep(1000);
//        getInstrumentation().callActivityOnResume(mHomeActivityRule.getActivity());
//        SystemClock.sleep(1000);
//        getInstrumentation().callActivityOnPause(mHomeActivityRule.getActivity());
//        getInstrumentation().callActivityOnStop(mHomeActivityRule.getActivity());
//        SystemClock.sleep(1000);
//        getInstrumentation().callActivityOnRestart(mHomeActivityRule.getActivity());
//        getInstrumentation().callActivityOnStart(mHomeActivityRule.getActivity());
//        getInstrumentation().callActivityOnResume(mHomeActivityRule.getActivity());
       // intended(hasComponent(new ComponentName(getTargetContext(), DrawActivity.class)));
    }

//    @Test
//    public void returnToDraw() {
//        // - Return to application on Processing or DrawActivity: http://stackoverflow.com/a/10307126/2734863: Check that circuit is    }
    // Cant get this working for same reason as returnToProcessing, nothing is working
//    }
//
    @Test
    public void goBackFromDraw() {
        // Go back from DrawActivity -> check that circuit exists
        getInstrumentation().waitForIdleSync();
        intended(hasComponent(new ComponentName(getTargetContext(), DrawActivity.class)));
        Espresso.pressBack();
        intended(hasComponent(new ComponentName(getTargetContext(), HomeActivity.class)));

        LinearLayout ll = (LinearLayout) mHomeActivityRule.getActivity().findViewById(
                R.id.saved_circuits_scroll);
        int  childCount = ll.getChildCount();
        assert(childCount == initialProjectCount + 1);
        // todo: stupid horizontalScrollView - assertion above fails (equal) despite there being one additional element
        // the HSV... the newly added one... doesn't register
    }

    @Test
    public void goBackFromDrawandPicknew() {
        //- Go back from DrawActivity -> pick new one -> go FAB -> changed
        getInstrumentation().waitForIdleSync();
        goBackFromDraw();
        if(initialProjectCount > 0){
            ArrayList<CircuitProject> circuitProjects = mHomeActivityRule.getActivity().
                    getCircuitProjects();
            CircuitProject circuitProject_one = circuitProjects.get(0);
            onView(withTagValue(withStringMatching(circuitProject_one.getFolderID()))).perform(scrollTo(),
                    click());
            onView(withId(R.id.processing_fab)).perform(click());
            intended(hasComponent(new ComponentName(getTargetContext(), DrawActivity.class)));
        }
        else{
            onView(withId(R.id.fab_expand_menu_button)).perform(click());
            onView(withText("Draw Circuit")).perform(click()); //todo: move to constants
            goBackFromDraw();
        }
    }
}
