package com.cpen321.circuitsolver.ngspice;

import android.os.Looper;
import android.support.test.runner.AndroidJUnitRunner;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.runner.RunWith;
import org.junit.Test;

import static junit.framework.Assert.assertTrue;

/**
 * Created by lotus on 31/10/16.
 */

@RunWith(AndroidJUnit4.class)
public class NgSpiceTest extends AndroidJUnitRunner {

    @Test
    public void newNgSpice() {
        Looper.prepare();
        NgSpice ngSpice = NgSpice.getInstance(InstrumentationRegistry.getTargetContext());
        String output = ngSpice.exec("-b -o out");
        //Test whether ngspice actually executed by checking its output
        assertTrue(output.contains("Circuit level simulation program"));
    }

}
