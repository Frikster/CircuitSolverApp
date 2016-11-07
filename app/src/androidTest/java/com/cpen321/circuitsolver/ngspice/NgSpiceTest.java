package com.cpen321.circuitsolver.ngspice;

import android.os.Looper;
import android.support.test.runner.AndroidJUnitRunner;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

/**
 * Created by lotus on 31/10/16.
 */

@RunWith(AndroidJUnit4.class)
public class NgSpiceTest extends AndroidJUnitRunner {

    private static String input = "* bla circuit\n" +
            "v1 1 0 dc 24\n" +
            "v2 3 0 dc 15\n" +
            "r1 1 2 10k\n" +
            "r2 2 3 8.1k\n" +
            "r3 2 0 4.7k\n" +
            ".CONTROL\n" +
            "tran 1ns 1ns\n" +
            ".ENDC\n" +
            ".END";
    private static String expected = "Node                                   Voltage\n" +
            "----                                   -------\n" +
            "1                                           24\n" +
            "3                                           15\n" +
            "2                                      9.74697\n" +
            "v2#branch                         -0.000648522\n" +
            "v1#branch                           -0.0014253\n";
    @Before
    public void initialize() {
        Looper.prepare();
    }

    @Test
    public void callNgSpiceTest() {
        NgSpice ngSpice = NgSpice.getInstance(InstrumentationRegistry.getTargetContext());
        String output = ngSpice.callNgSpice(input);
        assertTrue(output.contains(expected));
    }

}
