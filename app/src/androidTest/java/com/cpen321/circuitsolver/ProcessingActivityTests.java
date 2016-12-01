package com.cpen321.circuitsolver;

import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.cpen321.circuitsolver.ui.HomeActivity;
import com.cpen321.circuitsolver.ui.ProcessingActivity;

import org.junit.Rule;
import org.junit.runner.RunWith;

/**
 * Created by Cornelis Dirk Haupt on 11/30/2016.
 */
@RunWith(AndroidJUnit4.class)
public class ProcessingActivityTests {

    @Rule
    public IntentsTestRule<ProcessingActivity> mProcessingActivityRule =
            new IntentsTestRule<>(ProcessingActivity.class);


}
