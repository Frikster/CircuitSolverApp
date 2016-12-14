package com.cpen321.circuitsolver.usecases;

import android.app.Instrumentation;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.util.Log;

import com.cpen321.circuitsolver.ui.HomeActivity;

import java.util.List;

import static com.cpen321.circuitsolver.usecases.UseCaseConstants.TEST_CIRCUITS;

/**
 * Created by Cornelis Dirk Haupt on 12/13/2016.
 */

public class MyCustomRule<A extends HomeActivity> extends IntentsTestRule<A> {
    //private HomeActivity homeActivity;
//    private Class<A> mActivityClass;
//    private Instrumentation mInstrumentation;

    public MyCustomRule(Class<A> activityClass) {
        super(activityClass);


//        mInstrumentation = InstrumentationRegistry.getInstrumentation();
//
//        homeActivity = mActivityClass.cast(mInstrumentation.startActivitySync(startIntent));
//
//        mActivityClass = activityClass;
//        Object obj = activityClass;
//        homeActivity = (HomeActivity) obj;
    }

    @Override
    protected void beforeActivityLaunched() {
        super.beforeActivityLaunched();

        // Maybe prepare some mock service calls
        // Maybe override some depency injection modules with mocks
    }

    @Override
    protected Intent getActivityIntent() {
        Intent customIntent = new Intent();
        // add some custom extras and stuff
        return customIntent;
    }

    @Override
    protected void afterActivityLaunched() {
        super.afterActivityLaunched();
        HomeActivity homeActivity = getActivity();
        List<Integer> T_C = TEST_CIRCUITS;
//        Intent intent = homeActivity.getIntent();
        for(int test_circuit_id : T_C){
            Bitmap bm = BitmapFactory.decodeResource(
                    homeActivity.getResources(), test_circuit_id);
            Util.createProjectfromBitmap(homeActivity, bm);
            Espresso.pressBack();
        }
        // maybe you want to do something here
    }
}
