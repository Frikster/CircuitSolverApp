package com.cpen321.circuitsolver.workingEspresso;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.os.SystemClock;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.action.CoordinatesProvider;
import android.support.test.espresso.action.GeneralClickAction;
import android.support.test.espresso.action.GeneralLocation;
import android.support.test.espresso.action.GeneralSwipeAction;
import android.support.test.espresso.action.Press;
import android.support.test.espresso.action.Swipe;
import android.support.test.espresso.action.Tap;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;
import android.util.Log;
import android.view.View;

import com.cpen321.circuitsolver.R;
import com.cpen321.circuitsolver.model.SimplePoint;
import com.cpen321.circuitsolver.model.components.CircuitElm;
import com.cpen321.circuitsolver.ui.HomeActivity;
import com.cpen321.circuitsolver.ui.ProcessingActivity;
import com.cpen321.circuitsolver.ui.draw.DrawActivity;
import com.cpen321.circuitsolver.util.CircuitProject;
import com.cpen321.circuitsolver.util.Constants;
import com.cpen321.circuitsolver.util.ImageUtils;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Iterator;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.core.deps.guava.base.Preconditions.checkNotNull;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withTagValue;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.cpen321.circuitsolver.usecases.Util.clickXY;
import static com.cpen321.circuitsolver.usecases.Util.midpoint;
import static com.cpen321.circuitsolver.usecases.Util.withStringMatching;
import static org.hamcrest.Matchers.equalTo;

/**
 * Created by Cornelis Dirk Haupt on 11/28/2016.
 * Crucial code retrieved from: https://github.com/gilvegliach/StackOverflowAnswers/blob/master/EspressoAssertWhilePressed/app/src/androidTest/java/it/gilvegliach/learning/espressoassertwhilepressed/AssertWhilePressedTest.java
 */
@RunWith(AndroidJUnit4.class)
public class DrawSimplistCircuitTest{
    private final static String TAG = "DrawSimplistCircuitTest";
    @Rule
    public ActivityTestRule<HomeActivity> mHomeActivityRule =
            new ActivityTestRule<>(HomeActivity.class);
    private ActivityTestRule<DrawActivity> mDrawActivityRule =
            new ActivityTestRule<>(DrawActivity.class);
//    private ActivityTestRule<ProcessingActivity> mProcessingActivityRule =
//            new ActivityTestRule<>(ProcessingActivity.class);

    @Before
    public void sendBitmap(){
        CircuitProject candidateProject = new CircuitProject(ImageUtils.getTimeStamp()+"TEST_IMAGE",
                mHomeActivityRule.getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES));
        //mProcessingActivityRule.getActivity().setCircuitProject(candidateProject);
        Bitmap bm = BitmapFactory.decodeResource(
                mHomeActivityRule.getActivity().getResources(), R.drawable.example_1);
        candidateProject.saveOriginalImage(bm);
        Intent analysisIntent = new Intent(mHomeActivityRule.getActivity().getApplicationContext(),
                ProcessingActivity.class);
        analysisIntent.putExtra(Constants.CIRCUIT_PROJECT_FOLDER, candidateProject.getFolderPath());
        allowPermissionsIfNeeded();
        mHomeActivityRule.getActivity().startActivity(analysisIntent);


//        File file = new File(myDir, fname);
//        if (file.exists())
//            file.delete();
//        try {
//            FileOutputStream out = new FileOutputStream(file);
//            bm.compress(Bitmap.CompressFormat.JPEG, 90, out);
//            out.flush();
//            out.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        // Put the drawable in a bundle.


//        Bundle bundle = new Bundle();
//        bundle.putParcelable(Integer.toString(Constants.REQUEST_TAKE_PHOTO), bm);
//
//        // Place the test image on the phone
//        CircuitProject candidateProject = new CircuitProject(ImageUtils.getTimeStamp()+"TEST_IMAGE",
//                mHomeActivityRule.getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES));
//        mHomeActivityRule.getActivity().setCandidateProject(candidateProject);
    }

    @Test
    public void simpleCircuitTest() {

        // get circuitProjects from HomeActivty. Match location of a particular one in list with where to click
        //


//        RecyclerViewInteraction.
//                <ClipData.Item>onRecyclerView(withId(R.id.recyclerview))
//                .withItems(items)
//                .check(new ItemViewAssertion<ClipData.Item>() {
//                    @Override
//                    public void check(ClipData.Item item, View view, NoMatchingViewException e) {
//                        matches(hasDescendant(withText(item.getDisplayName())))
//                                .check(view, e);
//                    }
//                });
//
//        onView(nthChildOf(withId(R.id.saved_circuits_scrollview))).perform(click());


//        onView(with(R.id.saved_circuits_scroll).atPosition(0));
//
//
//        onView(allOf(withText("7"), hasSibling(withText("item: 0"))))
//                .perform(click());
//
//
//        onView(withId(R.id.saved_circuits_scroll))
//                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        SystemClock.sleep(1000*10); // wait for proceessing to complete todo: find way to only continue once other threads complete
        ArrayList<CircuitElm> circuitElms = mDrawActivityRule.getActivity().getCircuitElms();
        ArrayList<CircuitElm> circuitElms_copy = new ArrayList<>();
        for(CircuitElm circuitElm : circuitElms) {
            circuitElms_copy.add(circuitElm.clone());
        }

        Iterator<CircuitElm> iter = circuitElms_copy.iterator();
        while (iter.hasNext()) {
            CircuitElm circuitElm = iter.next();
            SimplePoint circuitElm_midpoint_coords = midpoint(circuitElm.getP2(),circuitElm.getP1());
            onView(withId(R.id.circuitFrame)).perform(clickXY(circuitElm_midpoint_coords.getX(),
                    circuitElm_midpoint_coords.getY()));
            SystemClock.sleep(300);
            onView(withId(R.id.eraseButton)).perform(click());
        }

        onView(withId(R.id.componentMenuButton)).perform(click());
        onView(withText("DC Source")).perform(click());
        onView(withId(R.id.circuitFrame)).perform(swipeDownLeft());

        onView(withId(R.id.componentMenuButton)).perform(click());
        onView(withText("Wire")).perform(click());
        onView(withId(R.id.circuitFrame)).perform(swipeRightBottom());

        onView(withId(R.id.componentMenuButton)).perform(click());
        onView(withText("Resistor")).perform(click());
        onView(withId(R.id.circuitFrame)).perform(swipeUpRight());

        onView(withId(R.id.componentMenuButton)).perform(click());
        onView(withText("Wire")).perform(click());
        onView(withId(R.id.circuitFrame)).perform(swipeLeftTop());

        CircuitElm source = circuitElms.get(0);
        SimplePoint source_midpoint_coords = midpoint(source.getP2(),source.getP1());
        onView(withId(R.id.circuitFrame)).perform(clickXY(source_midpoint_coords.getX(),
                source_midpoint_coords.getY()));
        SystemClock.sleep(1000);
        onView(withId(R.id.solveButton)).perform(click());
        SystemClock.sleep(1000);
        onView(withId(R.id.component_value)).perform(replaceText("23.4"));
        SystemClock.sleep(1000);
        onView(withId(R.id.solveButton)).perform(click());
        SystemClock.sleep(1000);

        CircuitElm wire = circuitElms.get(3);
        SimplePoint wire_midpoint_coords = midpoint(wire.getP2(),wire.getP1());
        onView(withId(R.id.circuitFrame)).perform(clickXY(wire_midpoint_coords.getX(),
                wire_midpoint_coords.getY()));
        SystemClock.sleep(1000);
        onView(withId(R.id.componentMenuButton)).perform(click());
        onView(withText("Resistor")).perform(click());
        onView(withId(R.id.circuitFrame)).perform(clickXY(wire_midpoint_coords.getX(),
                wire_midpoint_coords.getY()));
        SystemClock.sleep(1000);
        onView(withId(R.id.solveButton)).perform(click());
        SystemClock.sleep(1000);
        Espresso.pressBack();
        SystemClock.sleep(1000);

        //todo: when you press the back button it spawns a new activity seperate from mHomeActivity
        // stupid horizontalScrollView
        ArrayList<CircuitProject> circuitProjects = mHomeActivityRule.getActivity().
                getCircuitProjects();
        CircuitProject circuitProject_one = circuitProjects.get(0);
        onView(withTagValue(withStringMatching(circuitProject_one.getFolderID()))).perform(scrollTo(),
                click());
        onView(withId(R.id.processing_fab)).perform(click());
        SystemClock.sleep(1000);
        Espresso.pressBack();

        ArrayList<CircuitProject> circuitProjects_copy = new ArrayList<>();
        for(CircuitProject circuitProject : circuitProjects) {
            circuitProjects_copy.add(circuitProject.clone());
        }

        Iterator<CircuitProject> iter_2 = circuitProjects_copy.iterator();
        while (iter_2.hasNext()) {
            CircuitProject circuitProject = iter_2.next();
            onView(withTagValue(withStringMatching(circuitProject.getFolderID()))).perform(scrollTo(),
                    click());
            onView(withId(R.id.delete_fab)).perform(click());
        }
//        circuitProject_one = circuitProjects.get(0);
//        Log.d(TAG, String.valueOf(circuitProjects.size()));
//        Log.d(TAG, String.valueOf(circuitProject_one));
//        onView(withTagValue(withStringMatching(circuitProject_one.getFolderID()))).perform(click());
//        onView(withId(R.id.delete_fab)).perform(click());

        SystemClock.sleep(5000);

//        float[] coordinates =  GeneralLocation.BOTTOM_RIGHT.calculateCoordinates(view);
//        coordinates[0] = coordinates[0] - 100;
//        coordinates[1] = coordinates[1] - 300;

//        onView(withId(R.id.circuitFrame)).perform(clickXY());
//        onView(withId(R.id.solveButton)).perform(click());

//        onView(withId(R.id.draw_fab)).check(matches(isDisplayed()));
//        View v = mHomeActivityRule.getActivity().findViewById(R.id.draw_fab);
//        Log.d(TAG, String.valueOf(v.getVisibility()==v.VISIBLE));
//        Log.d(TAG, String.valueOf(v.isShown()));
//        Log.d(TAG, String.valueOf(v.isEnabled()));
//
//        onView(withId(R.id.multiple_actions)).perform(click());
//        onView(withId(R.id.capture_fab)).check(matches(isDisplayed()));
//        Log.d(TAG, String.valueOf(v.getVisibility()==v.VISIBLE));
//        Log.d(TAG, String.valueOf(v.isShown()));
//        Log.d(TAG, String.valueOf(v.isEnabled()));
//
//
//        onView(withId(R.id.draw_fab)).perform(pressAndHold());
//        onView(withId(R.id.componentMenuButton)).perform(click());
//        onView(withId(R.id.dropDownSourceButton)).perform(click());
//        onView(withId(R.id.circuitFrame)).perform(swipeDown());
    }

//    @Test
//    public void testAssertWhilePressed() {
//        onView(withId(R.id.button)).perform(pressAndHold());
//        onView(withId(R.id.text)).check(matches(withText("Button is held down")));
//        onView(withId(R.id.button)).perform(release());
//    }

//    public static ViewAction swipeDownLeft() {
//        Log.d(TAG, GeneralLocation.TOP_LEFT.toString());
//        Log.d(TAG, GeneralLocation.BOTTOM_LEFT.toString());
//        return new GeneralSwipeAction(Swipe.SLOW, GeneralLocation.TOP_LEFT,
//                GeneralLocation.BOTTOM_LEFT, Press.FINGER);
//    }






    public static ViewAction swipeDownLeft() {
        return new GeneralSwipeAction(Swipe.SLOW, new CoordinatesProvider() {
            @Override
            public float[] calculateCoordinates(View view) {
                float[] coordinates =  GeneralLocation.TOP_LEFT.calculateCoordinates(view);
                coordinates[0] = coordinates[0] + 100;
                coordinates[1] = coordinates[1] + 300;
                return coordinates;
            }
        },
                new CoordinatesProvider() {
                    @Override
                    public float[] calculateCoordinates(View view) {
                        float[] coordinates =  GeneralLocation.BOTTOM_LEFT.calculateCoordinates(view);
                        coordinates[0] = coordinates[0] + 100;
                        coordinates[1] = coordinates[1] - 300;
                        return coordinates;
                    }
                }, Press.FINGER);
    }

    public static ViewAction swipeRightBottom() {
        return new GeneralSwipeAction(Swipe.SLOW, new CoordinatesProvider() {
            @Override
            public float[] calculateCoordinates(View view) {
                float[] coordinates =  GeneralLocation.BOTTOM_LEFT.calculateCoordinates(view);
                coordinates[0] = coordinates[0] + 100;
                coordinates[1] = coordinates[1] - 300;
                return coordinates;
            }
        },
                new CoordinatesProvider() {
                    @Override
                    public float[] calculateCoordinates(View view) {
                        float[] coordinates =  GeneralLocation.BOTTOM_RIGHT.calculateCoordinates(view);
                        coordinates[0] = coordinates[0] - 100;
                        coordinates[1] = coordinates[1] - 300;
                        return coordinates;
                    }
                }, Press.FINGER);
    }

    public static ViewAction swipeUpRight() {
        float[] swipeUpRight_coordinates;

        CoordinatesProvider cp = new CoordinatesProvider() {
            @Override
            public float[] calculateCoordinates(View view) {
                float[] coordinates =  GeneralLocation.BOTTOM_RIGHT.calculateCoordinates(view);
                coordinates[0] = coordinates[0] - 100;
                coordinates[1] = coordinates[1] - 300;
                return coordinates;
            }
        };
        //swipeUpRight_coordinates = cp.calculateCoordinates(mDrawActivityRule);

        return new GeneralSwipeAction(Swipe.SLOW, cp,
            new CoordinatesProvider() {
                @Override
                public float[] calculateCoordinates(View view) {
                    float[] coordinates =  GeneralLocation.TOP_RIGHT.calculateCoordinates(view);
                    coordinates[0] = coordinates[0] - 100;
                    coordinates[1] = coordinates[1] + 300;
                    return coordinates;
                }
            }, Press.FINGER);
    }

    public static ViewAction swipeLeftTop() {
        return new GeneralSwipeAction(Swipe.SLOW, new CoordinatesProvider() {
            @Override
            public float[] calculateCoordinates(View view) {
                float[] coordinates =  GeneralLocation.TOP_RIGHT.calculateCoordinates(view);
                coordinates[0] = coordinates[0] - 100;
                coordinates[1] = coordinates[1] + 300;
                return coordinates;
            }
        },
                new CoordinatesProvider() {
                    @Override
                    public float[] calculateCoordinates(View view) {
                        float[] coordinates =  GeneralLocation.TOP_LEFT.calculateCoordinates(view);
                        coordinates[0] = coordinates[0] + 100;
                        coordinates[1] = coordinates[1] + 300;
                        return coordinates;
                    }
                }, Press.FINGER);
    }

    private static void allowPermissionsIfNeeded() {
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



//    public static ViewAction customClick() {
//        return actionWithAssertions(
//                new CustomGeneralClickAction(Tap.SINGLE, GeneralLocation.VISIBLE_CENTER, Press.FINGER));
//    }
}

