package com.cpen321.circuitsolver.ngspice;

import android.util.Log;

import com.cpen321.circuitsolver.model.CircuitNode;
import com.cpen321.circuitsolver.model.SimplePoint;
import com.cpen321.circuitsolver.model.components.CircuitElm;
import com.cpen321.circuitsolver.model.components.ResistorElm;
import com.cpen321.circuitsolver.ngspice.NgOutputParser;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertEquals;

/**
 * Created by Bruce on 2016-11-05.
 */

public class NgOutputParserTest {

    private static String input =
            "Doing analysis at TEMP = 27.000000 and TNOM = 27.000000\n" +
                    "\n" +
                    "Initial Transient Solution\n" +
                    "--------------------------\n" +
                    "Node                                   Voltage\n" +
                    "----                                   -------\n" +
                    "1                                           24\n" +
                    "3                                           15\n" +
                    "2                                      9.74697\n" +
                    "v2#branch                         -0.000648522\n" +
                    "v1#branch                           -0.0014253\n" +
                    "No. of Data Rows : 59\n";

    SimplePoint f = new SimplePoint(100,200);//arb inputs
    SimplePoint g = new SimplePoint(100,200);//arb inputs

    CircuitNode node1 = new CircuitNode();
    CircuitNode node2 = new CircuitNode();
    CircuitNode node3 = new CircuitNode();
    // represents the v1#branch
    CircuitElm elm1 = new ResistorElm(g, f, 1.0);
    //represents the v2#branch
    CircuitElm elm2 = new ResistorElm(f, g, 3.0);

    //initialize map of Strings of nodeNum to a CircuitNode
    Map<String, CircuitNode> nodes = new HashMap<String, CircuitNode>() {{
        put("1",node1);
        put("2", node2);
        put("3", node3);
    }};
    //initialize map of Strings of a branch to a CircuitElm
    Map<String, CircuitElm> elms = new HashMap<String, CircuitElm>() {{
        put("v1#branch",elm1);
        put("v2#branch",elm2);
    }};

    @Test
    public void callNgOutputParserTest() {
        //ArrayList<String> list = NgOutputParser.callNgOutputParser(input);

        NgOutputParser.callNgOutputParser(input, nodes, elms);
        Log.v("node1 voltage", String.valueOf(node1.getVoltage()));
        Log.v("node2 voltage", String.valueOf(node2.getVoltage()));
        Log.v("node3 voltage", String.valueOf(node3.getVoltage()));
        Log.v("v1#branch", String.valueOf(elm1.getCurrent()));
        Log.v("v2#branch", String.valueOf(elm2.getCurrent()));

        assertEquals(node1.getVoltage(),24.0);
        assertEquals(node2.getVoltage(),9.74697);
        assertEquals(node3.getVoltage(),15.0);
        assertEquals(elm1.getCurrent(),-0.0014253);
        assertEquals(elm2.getCurrent(),-0.000648522);
    }


}
