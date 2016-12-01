package com.cpen321.circuitsolver;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;
import android.util.Log;

import com.cpen321.circuitsolver.ui.HomeActivity;
import com.cpen321.circuitsolver.ui.draw.DrawActivity;
import com.cpen321.circuitsolver.util.CircuitProject;
import com.cpen321.circuitsolver.util.Constants;
import com.cpen321.circuitsolver.util.ImageUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intending;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.not;

/**
 * Created by Cornelis Dirk Haupt on 11/28/2016.
 */
@RunWith(AndroidJUnit4.class)
public class TakePictureTest {
    /**
     * A JUnit {@link Rule @Rule} to init and release Espresso Intents before and after each
     * test run.
     * <p>
     * Rules are interceptors which are executed for each test method and will run before
     * any of your setup code in the {@link Before @Before} method.
     * <p>
     * This rule is based on {@link ActivityTestRule} and will create and launch of the activity
     * for you and also expose the activity under test.
     */
    @Rule
    public IntentsTestRule<HomeActivity> mHomeActivityRule =
            new IntentsTestRule<>(HomeActivity.class);

    @Before
    public void stubCameraIntent() {
        Instrumentation.ActivityResult result = createImageCaptureActivityResultStub();

        // Stub the Intent.
        intending(hasAction(MediaStore.ACTION_IMAGE_CAPTURE)).respondWith(result);
    }

    @Test
    public void takePhoto_drawableIsApplied() {
        // Check that the ImageView doesn't have a drawable applied.
        //onView(withId(R.id.imageView)).check(matches(not(hasDrawable())));

        // Click on the button that will trigger the stubbed intent.
        onView(withId(R.id.fab_expand_menu_button)).perform(click());
        SystemClock.sleep(1000);
        onView(withId(R.id.capture_fab)).perform(click());
        SystemClock.sleep(5000);

        // With no user interaction, the ImageView will have a drawable.
        // need to define hasDrawable() to test for circuit being placed
        //onView(withId(R.id.circuitFrame)).check(matches(hasDrawable()));
    }

    private Instrumentation.ActivityResult createImageCaptureActivityResultStub() {


        //File photoFile = candidateProject.generateOriginalImageFile();

        // Create the Intent that will include the bundle.
        Intent resultData = new Intent();        // Put the drawable in a bundle.
        Bundle bundle = new Bundle();
        Bitmap bm = BitmapFactory.decodeResource(
                mHomeActivityRule.getActivity().getResources(), R.drawable.example_1);
        bundle.putParcelable(Integer.toString(Constants.REQUEST_TAKE_PHOTO), bm);

        // Place the test image on the phone
        CircuitProject candidateProject = new CircuitProject(ImageUtils.getTimeStamp()+"TEST_IMAGE",
                mHomeActivityRule.getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES));
        mHomeActivityRule.getActivity().setCandidateProject(candidateProject);
        resultData.putExtras(bundle);

        // Create the ActivityResult with the Intent.
        return new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);
    }


}
