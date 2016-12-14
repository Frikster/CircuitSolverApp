package com.cpen321.circuitsolver.usecases;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.SystemClock;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.widget.LinearLayout;

import com.cpen321.circuitsolver.R;
import com.cpen321.circuitsolver.ui.HomeActivity;
import com.cpen321.circuitsolver.util.CircuitProject;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withTagValue;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.cpen321.circuitsolver.usecases.UseCaseConstants.TEST_CIRCUITS_UC1;
import static com.cpen321.circuitsolver.usecases.UseCaseConstants.TEST_CIRCUITS_UC3;
import static com.cpen321.circuitsolver.usecases.UseCaseConstants.TEST_CIRCUITS_UC5;
import static com.cpen321.circuitsolver.usecases.Util.isToast;
import static com.cpen321.circuitsolver.usecases.Util.withStringMatching;
import static org.hamcrest.core.StringStartsWith.startsWith;

/**
 * Created by Cornelis Dirk Haupt on 12/2/2016.
 */

public class UseCase5 {
    private final static String TAG = "UC5";
    private CircuitProject candidateProject;

    @Rule
    public IntentsTestRule<HomeActivity> mHomeActivityRule =
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
            for (int test_circuit_id : TEST_CIRCUITS_UC5) {
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

    //@Test
    public void deleteMultipleProjects() {
        ArrayList<CircuitProject> circuitProjects = mHomeActivityRule.getActivity().
                getCircuitProjects();
        if(circuitProjects.size() <= 3){
            // make some more projects to delete
            for(int i = 0; i <= 3; i++){
                onView(withId(R.id.fab_expand_menu_button)).perform(click());
                onView(withText("Draw Circuit")).perform(click()); //// TODO: constant
                Espresso.pressBack();
            }
        }

        // get count (in addition to circuitProjects.size()
        LinearLayout ll = (LinearLayout) mHomeActivityRule.getActivity().findViewById(
                R.id.saved_circuits_scroll);
        final int initialChildCount = ll.getChildCount();
        final int initialCircuitProjCount = circuitProjects.size();
        assert(initialChildCount == initialCircuitProjCount);

        ArrayList<CircuitProject> circuitProjects_copy = new ArrayList<>();
        for(CircuitProject circuitProject : circuitProjects) {
            circuitProjects_copy.add(circuitProject.clone());
        }

        //delete 3
        Iterator<CircuitProject> scroll_iter = circuitProjects_copy.iterator();
        int flag = 3;
        while (scroll_iter.hasNext() && flag > 0) {
            CircuitProject circuitProject = scroll_iter.next();
            onView(withTagValue(withStringMatching(circuitProject.getFolderID()))).perform(scrollTo(),
                    click());
            onView(withId(R.id.processing_fab)).check(matches(isDisplayed()));
            onView(withId(R.id.delete_fab)).check(matches(isDisplayed()));
            onView(withId(R.id.delete_fab)).perform(click());
            onView(withText(startsWith("Project Deleted"))).inRoot(isToast()).check(matches(isDisplayed()));
            SystemClock.sleep(2000);
            // toast check
            flag--;
        }

        //Check you deleted 3
        assert(circuitProjects.size() == initialCircuitProjCount - 3);
        assert(ll.getChildCount() == initialChildCount - 3);
    }

    @Test
    public void deleteAllProjects() {
        getInstrumentation().waitForIdleSync();
        ArrayList<CircuitProject> circuitProjects = mHomeActivityRule.getActivity().
                getCircuitProjects();
        if(circuitProjects.size() <= 3){
            // make some more projects to delete
            for(int i = 0; i <= 3; i++){
                onView(withId(R.id.fab_expand_menu_button)).perform(click());
                onView(withText("Draw Circuit")).perform(click()); //// TODO: constant
                Espresso.pressBack();
            }
        }

        // get count (in addition to circuitProjects.size()
        LinearLayout ll = (LinearLayout) mHomeActivityRule.getActivity().findViewById(
                R.id.saved_circuits_scroll);
        final int initialChildCount = ll.getChildCount();
        final int initialCircuitProjCount = circuitProjects.size();
        assert(initialChildCount == initialCircuitProjCount);

        ArrayList<CircuitProject> circuitProjects_copy = new ArrayList<>();
        for(CircuitProject circuitProject : circuitProjects) {
            circuitProjects_copy.add(circuitProject.clone());
        }

        // Delete all
        Iterator<CircuitProject> scroll_iter = circuitProjects_copy.iterator();
        while (scroll_iter.hasNext()) {
            CircuitProject circuitProject = scroll_iter.next();
            onView(withTagValue(withStringMatching(circuitProject.getFolderID()))).perform(scrollTo(),
                    click());
            onView(withId(R.id.processing_fab)).check(matches(isDisplayed()));
            onView(withId(R.id.delete_fab)).check(matches(isDisplayed()));
            onView(withId(R.id.delete_fab)).perform(click());
            onView(withText(startsWith("Project Deleted"))).inRoot(isToast()).check(matches(isDisplayed()));
            SystemClock.sleep(2000);
        }

        // Check all gone
        assert(circuitProjects.size() == 0);
        assert(ll.getChildCount() == 0);
    }
}
