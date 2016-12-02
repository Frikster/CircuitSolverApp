package com.cpen321.circuitsolver.usecases;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.test.rule.ActivityTestRule;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;
import android.util.Log;

import com.cpen321.circuitsolver.R;
import com.cpen321.circuitsolver.ui.HomeActivity;
import com.cpen321.circuitsolver.ui.ProcessingActivity;
import com.cpen321.circuitsolver.util.CircuitProject;
import com.cpen321.circuitsolver.util.Constants;
import com.cpen321.circuitsolver.util.ImageUtils;

import org.hamcrest.Description;
import org.hamcrest.Matcher;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.core.deps.guava.base.Preconditions.checkNotNull;
import static org.hamcrest.Matchers.equalTo;

/**
 * Created by Cornelis Dirk Haupt on 12/1/2016.
 */

public class Util {
    public static void allowPermissionsIfNeeded() {
        if (Build.VERSION.SDK_INT >= 23) {
            UiDevice device = UiDevice.getInstance(getInstrumentation());
            UiObject allowPermissions = device.findObject(new UiSelector().text("Allow"));
            if (allowPermissions.exists()) {
                try {
                    allowPermissions.click();
                } catch (UiObjectNotFoundException e) {
                    Log.d("TakePictureTest", "There is no permissions dialog to interact with ");
                }
            }
        }
    }

    public static void createProjectfromBitmap(ActivityTestRule<HomeActivity> mHomeActivityRule, Bitmap bm){
        CircuitProject candidateProject = new CircuitProject(ImageUtils.getTimeStamp(),
                mHomeActivityRule.getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES));
        candidateProject.saveOriginalImage(bm);
        Intent analysisIntent = new Intent(mHomeActivityRule.getActivity().getApplicationContext(),
                ProcessingActivity.class);
        analysisIntent.putExtra(Constants.CIRCUIT_PROJECT_FOLDER, candidateProject.getFolderPath());
        Util.allowPermissionsIfNeeded();
        mHomeActivityRule.getActivity().startActivity(analysisIntent);
    }

    public static Matcher<Object> withStringMatching(String expectedText) {
        checkNotNull(expectedText);
        return withStringMatching(equalTo(expectedText));
    }

    @SuppressWarnings("rawtypes")
    public static Matcher<Object> withStringMatching(final Matcher<String> itemTextMatcher) {
        checkNotNull(itemTextMatcher);
        return new BoundedMatcher<Object, String>(String.class) {
            @Override
            public boolean matchesSafely(String string) {
                return itemTextMatcher.matches(string);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("with string: ");
                itemTextMatcher.describeTo(description);
            }
        };
    }
}
