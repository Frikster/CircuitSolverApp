package com.cpen321.circuitsolver.usecases;

import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.cpen321.circuitsolver.R;
import com.cpen321.circuitsolver.model.components.CircuitElm;
import com.cpen321.circuitsolver.ui.HomeActivity;
import com.cpen321.circuitsolver.ui.ProcessingActivity;
import com.cpen321.circuitsolver.ui.draw.DrawActivity;
import com.cpen321.circuitsolver.util.CircuitProject;
import com.cpen321.circuitsolver.util.Constants;
import com.cpen321.circuitsolver.util.ImageUtils;

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
import static com.cpen321.circuitsolver.usecases.Util.withStringMatching;

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
        Log.e(TAG,Integer.toString(circuitElms.size()));
    }

    @Test
    public void changeComponent(){
        // - Change component ->  bottom value matches component + component change
    }

    @Test
    public void changeComponentandSolve(){
        // - Change component value and solve -> see update, "solved!" appears (http://stackoverflow.com/a/28606603/2734863), + display current direction
    }

    @Test
    public void changeComponentandSolveUnsolvable(){
        //- Circuit unsolvable click solve: Unsolvable toast appears
    }
}
