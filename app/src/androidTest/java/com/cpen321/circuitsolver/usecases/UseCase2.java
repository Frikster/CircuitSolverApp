package com.cpen321.circuitsolver.usecases;

import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.widget.LinearLayout;

import com.cpen321.circuitsolver.R;
import com.cpen321.circuitsolver.ui.HomeActivity;
import com.cpen321.circuitsolver.ui.draw.DrawActivity;
import com.cpen321.circuitsolver.util.CircuitProject;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.Intents.intending;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withTagValue;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.cpen321.circuitsolver.usecases.UseCaseConstants.TEST_CIRCUITS_UC2;
import static com.cpen321.circuitsolver.usecases.Util.withStringMatching;

/**
 * Created by Cornelis Dirk Haupt on 12/2/2016.
 * Run Use Cases Tests Individually for them to work
 * USE CASE 2: View component values of a previous analyzed circuit
 */
public class UseCase2 {
    private final static String TAG = "UC2";
    private static int initialProjectCount = 0;

    @Rule
    public IntentsTestRule<HomeActivity> mHomeActivityRule =
            new IntentsTestRule<>(HomeActivity.class);

    private static boolean ranOnce = false;
    @Before
    public void setupBitmaps(){
        initialProjectCount = TEST_CIRCUITS_UC2.size();
        if(!ranOnce) {
            Util.deleteAllProjects(mHomeActivityRule.getActivity());
            mHomeActivityRule.getActivity().setCircuitProjects(new ArrayList<CircuitProject>());
            Intent intent = mHomeActivityRule.getActivity().getIntent();
            mHomeActivityRule.getActivity().finish();
            mHomeActivityRule.getActivity().startActivity(intent);
            // Use circuit test cases from constant class.
            // Add more and tests will iterate through all of them
            for (int test_circuit_id : TEST_CIRCUITS_UC2) {
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
    public void goBackFromDraw() {
        // Go back from DrawActivity -> check that circuit exists
        // selectProjectAndOpenIt() code:
        ArrayList<CircuitProject> circuitProjects = mHomeActivityRule.getActivity().
                getCircuitProjects();
        CircuitProject circuitProject_one = circuitProjects.get(0);
        onView(withTagValue(withStringMatching(circuitProject_one.getFolderID()))).perform(scrollTo(),
                click());
        onView(withId(R.id.processing_fab)).perform(click());
        intended(hasComponent(new ComponentName(getTargetContext(), DrawActivity.class)));

        // Return to the main screen
        Espresso.pressBack();
        intended(hasComponent(new ComponentName(getTargetContext(), HomeActivity.class)));

        // Check that all expected projects are there, on screen and backend objects
        LinearLayout ll = (LinearLayout) mHomeActivityRule.getActivity().findViewById(
                R.id.saved_circuits_scroll);
        int  childCount = ll.getChildCount();
        assert(circuitProjects.size() == childCount && childCount == initialProjectCount);
    }

    @Test
    public void goBackFromDrawandPicknew() {
        //- Go back from DrawActivity -> pick new one -> go FAB -> changed

        // it is unknown why, but trying to call goBackFromDraw() from here causes a failure even
        // though goBackFromDraw() passes by itself
        /////
        // Go back from DrawActivity -> check that circuit exists
        // selectProjectAndOpenIt() code:
        ArrayList<CircuitProject> circuitProjects = mHomeActivityRule.getActivity().
                getCircuitProjects();
        CircuitProject circuitProject_one = circuitProjects.get(0);
        onView(withTagValue(withStringMatching(circuitProject_one.getFolderID()))).perform(scrollTo(),
                click());
        onView(withId(R.id.processing_fab)).perform(click());

        // Return to the main screen
        Espresso.pressBack();

        // Check that all expected projects are there, on screen and backend objects
        LinearLayout ll = (LinearLayout) mHomeActivityRule.getActivity().findViewById(
                R.id.saved_circuits_scroll);
        int  childCount = ll.getChildCount();
        assert(circuitProjects.size() == childCount && childCount == initialProjectCount);
        /////

        if(initialProjectCount > 1){
            circuitProjects = mHomeActivityRule.getActivity().
                    getCircuitProjects();
            CircuitProject circuitProject_two = circuitProjects.get(1);
            onView(withTagValue(withStringMatching(circuitProject_two.getFolderID()))).perform(scrollTo(),
                    click());
            onView(withId(R.id.processing_fab)).perform(click());
            intending(hasComponent(new ComponentName(getTargetContext(), DrawActivity.class)));
        }
        else{
            onView(withId(R.id.fab_expand_menu_button)).perform(click());
            onView(withText("Draw Circuit")).perform(click()); //todo: move to constants
            intending(hasComponent(new ComponentName(getTargetContext(), DrawActivity.class)));
        }
    }



/* THE FOLLOWING TESTS DO NOT WORK. SEE TEST DOCUMENTATION FOR DETAILS AND THE STACK-OVERFLOW URL
IN THE COMMENTS IF YOU ARE ATTEMPTING TO GET IT TO WORK */


//    @UiThreadTest
//    public void restartActivity(Activity activity){
//        onView(withId(android.R.id.home)).perform(click());
//
////        onView(isRoot()).perform(ViewActions.pressMenuKey());
////        getInstrumentation().callActivityOnRestart(activity);
//
////        onView(withText(R.string.options_item_3_text)).check(matches(isDisplayed())).perform(click());
////        onView(withId(R.id.text_menu_result)).check(matches(withText(R.string.options_item_3_text)));
////        getInstrumentation().callActivityOnPause(mHomeActivityRule.getActivity());
////        SystemClock.sleep(1000);
////        getInstrumentation().callActivityOnResume(mHomeActivityRule.getActivity());
////
////        getInstrumentation().callActivityOnRestart(activity);
//    }

//  //  @Test
//    public void returnToProcessing() {
//        // - - Return to application on Processing or DrawActivity: http://stackoverflow.com/a/10307126/2734863: Check that circuit is    }
//        // make activity falling into restart phase:
////        SystemClock.sleep(1000);
////        restartActivity(mProcessingActivityRule.getActivity()); // Key line
////        SystemClock.sleep(2000);
//        //onView(isRoot()).perform(ViewActions.pressMenuKey());
//
////        SystemClock.sleep(2000);
////        onView(withId(R.id.fab_expand_menu_button)).perform(click());
//        //getInstrumentation().callActivityOnPause(mHomeActivityRule.getActivity());
////        SystemClock.sleep(1000);
////        getInstrumentation().callActivityOnResume(mHomeActivityRule.getActivity());
////        SystemClock.sleep(1000);
////        getInstrumentation().callActivityOnPause(mHomeActivityRule.getActivity());
////        getInstrumentation().callActivityOnStop(mHomeActivityRule.getActivity());
////        SystemClock.sleep(1000);
////        getInstrumentation().callActivityOnRestart(mHomeActivityRule.getActivity());
////        getInstrumentation().callActivityOnStart(mHomeActivityRule.getActivity());
////        getInstrumentation().callActivityOnResume(mHomeActivityRule.getActivity());
//       // intended(hasComponent(new ComponentName(getTargetContext(), DrawActivity.class)));
//    }

//    @Test
//    public void returnToDraw() {
//        // - Return to application on Processing or DrawActivity: http://stackoverflow.com/a/10307126/2734863: Check that circuit is    }
          // Cant get this working for same reason as returnToProcessing, see stackoverflow link
//    }
//
}
