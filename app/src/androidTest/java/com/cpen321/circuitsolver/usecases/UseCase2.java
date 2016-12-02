package com.cpen321.circuitsolver.usecases;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.rule.ActivityTestRule;

import com.cpen321.circuitsolver.R;
import com.cpen321.circuitsolver.ui.HomeActivity;
import com.cpen321.circuitsolver.ui.draw.DrawActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * Created by Cornelis Dirk Haupt on 12/2/2016.
 */

public class UseCase2 {
    private final static String TAG = "UC2";

    private ActivityTestRule<DrawActivity> mDrawActivityRule =
            new ActivityTestRule<>(DrawActivity.class);

    @Rule
    public IntentsTestRule<HomeActivity> mHomeActivityRule =
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
    public void returnToProcessing() {
        // - - Return to application on Processing or DrawActivity: http://stackoverflow.com/a/10307126/2734863: Check that circuit is    }
    }

    @Test
    public void returnToDraw() {
        // - Return to application on Processing or DrawActivity: http://stackoverflow.com/a/10307126/2734863: Check that circuit is    }
    }

    @Test
    public void goBackFromDraw() {
        // Go back from DrawActivity -> check that circuit exists    }
    }

    @Test
    public void goBackFromDrawandPicknew() {
        //- Go back from DrawActivity -> pick new one -> go FAB -> changed    }
    }
}
