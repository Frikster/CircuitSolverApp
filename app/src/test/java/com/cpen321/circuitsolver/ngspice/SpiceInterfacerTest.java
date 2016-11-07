package com.cpen321.circuitsolver.ngspice;

import android.util.Log;

import com.cpen321.circuitsolver.model.CircuitNode;
import com.cpen321.circuitsolver.model.ResetComponents;
import com.cpen321.circuitsolver.model.SimplePoint;
import com.cpen321.circuitsolver.model.components.CircuitElm;
import com.cpen321.circuitsolver.model.components.ResistorElm;
import com.cpen321.circuitsolver.model.components.VoltageElm;
import com.cpen321.circuitsolver.model.components.WireElm;
import com.cpen321.circuitsolver.service.AnalyzeCircuitImpl;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

/**
 * Created by lotus on 05/11/16.
 */

public class SpiceInterfacerTest {
    @Before
    public void init() {
        ResetComponents.resetNumComponents();
    }

    @Test
    public void getNgSpiceInputTest() {
        List<CircuitElm> elements = new ArrayList<CircuitElm>();
        elements.add(new WireElm(new SimplePoint(0, 0), new SimplePoint(0, 1)));
        elements.add(new VoltageElm(new SimplePoint(0, 2), new SimplePoint(0, 1), 24));
        elements.add(new WireElm(new SimplePoint(0, 2), new SimplePoint(0, 3)));
        elements.add(new WireElm(new SimplePoint(0, 3), new SimplePoint(1, 3)));
        elements.add(new ResistorElm(new SimplePoint(1, 3), new SimplePoint(2, 3), 10000));
        elements.add(new WireElm(new SimplePoint(2, 3), new SimplePoint(3, 3)));
        elements.add(new WireElm(new SimplePoint(3, 3), new SimplePoint(4, 3)));
        elements.add(new ResistorElm(new SimplePoint(4, 3), new SimplePoint(5, 3), 8100));
        elements.add(new WireElm(new SimplePoint(5, 3), new SimplePoint(6, 3)));
        elements.add(new WireElm(new SimplePoint(6, 3), new SimplePoint(6, 2)));
        elements.add(new VoltageElm(new SimplePoint(6, 2), new SimplePoint(6, 1), 15));
        elements.add(new WireElm(new SimplePoint(6, 1), new SimplePoint(6, 0)));
        elements.add(new WireElm(new SimplePoint(3, 3), new SimplePoint(3, 2)));
        elements.add(new ResistorElm(new SimplePoint(3, 2), new SimplePoint(3, 1), 4700));
        elements.add(new WireElm(new SimplePoint(3, 1), new SimplePoint(3, 0)));
        elements.add(new WireElm(new SimplePoint(0, 0), new SimplePoint(3, 0)));
        elements.add(new WireElm(new SimplePoint(3, 0), new SimplePoint(6, 0)));

        AnalyzeCircuitImpl analyzeCircuitImpl = new AnalyzeCircuitImpl(elements);
        analyzeCircuitImpl.init();
        List<CircuitNode> resultNodes = analyzeCircuitImpl.getNodes();
        System.out.println(resultNodes);
        assertTrue(resultNodes.size() == 4);

        SpiceInterfacer spiceInterfacer = new SpiceInterfacer(analyzeCircuitImpl.getNodes() ,analyzeCircuitImpl.getElements());
        String ngSpiceInput = spiceInterfacer.getNgSpiceInput();
        System.out.println(ngSpiceInput);

        String expected = "* My Circuit\n" +
                "v1 1 0 dc 24.0\n" +
                "r1 1 2 10000.0\n" +
                "r2 2 3 8100.0\n" +
                "v2 3 0 dc 15.0\n" +
                "r3 2 0 4700.0\n" +
                "\n" +
                ".CONTROL\n" +
                "tran 1ns 1ns\n" +
                ".ENDC\n" +
                ".END";

        assertTrue(ngSpiceInput.contains(expected));
    }

    @Test
    public void parserTest() {
        List<CircuitElm> elements = new ArrayList<CircuitElm>();
        elements.add(new WireElm(new SimplePoint(0, 0), new SimplePoint(0, 1)));
        elements.add(new VoltageElm(new SimplePoint(0, 2), new SimplePoint(0, 1), 24));
        elements.add(new WireElm(new SimplePoint(0, 2), new SimplePoint(0, 3)));
        elements.add(new WireElm(new SimplePoint(0, 3), new SimplePoint(1, 3)));
        elements.add(new ResistorElm(new SimplePoint(1, 3), new SimplePoint(2, 3), 10000));
        elements.add(new WireElm(new SimplePoint(2, 3), new SimplePoint(3, 3)));
        elements.add(new WireElm(new SimplePoint(3, 3), new SimplePoint(4, 3)));
        elements.add(new ResistorElm(new SimplePoint(4, 3), new SimplePoint(5, 3), 8100));
        elements.add(new WireElm(new SimplePoint(5, 3), new SimplePoint(6, 3)));
        elements.add(new WireElm(new SimplePoint(6, 3), new SimplePoint(6, 2)));
        elements.add(new VoltageElm(new SimplePoint(6, 2), new SimplePoint(6, 1), 15));
        elements.add(new WireElm(new SimplePoint(6, 1), new SimplePoint(6, 0)));
        elements.add(new WireElm(new SimplePoint(3, 3), new SimplePoint(3, 2)));
        elements.add(new ResistorElm(new SimplePoint(3, 2), new SimplePoint(3, 1), 4700));
        elements.add(new WireElm(new SimplePoint(3, 1), new SimplePoint(3, 0)));
        elements.add(new WireElm(new SimplePoint(0, 0), new SimplePoint(3, 0)));
        elements.add(new WireElm(new SimplePoint(3, 0), new SimplePoint(6, 0)));

        String input =
                "Note: can't find init_test file.\n" +
                        "\n" +
                        "Circuit: * my circuit\n" +
                        "\n" +
                        "Doing analysis at TEMP = 27.000000 and TNOM = 27.000000\n" +
                        "\n" +
                        "\n" +
                        "Initial Transient Solution\n" +
                        "--------------------------\n" +
                        "\n" +
                        "Node                                   Voltage\n" +
                        "----                                   -------\n" +
                        "1                                           24\n" +
                        "2                                      9.74697\n" +
                        "3                                           15\n" +
                        "v2#branch                         -0.000648522\n" +
                        "v1#branch                           -0.0014253\n";

        AnalyzeCircuitImpl analyzeCircuitImpl = new AnalyzeCircuitImpl(elements);
        analyzeCircuitImpl.init();
        List<CircuitNode> resultNodes = analyzeCircuitImpl.getNodes();
        List<CircuitElm> resultElms = analyzeCircuitImpl.getElements();
        System.out.println(resultNodes);
        assertTrue(resultNodes.size() == 4);

        SpiceInterfacer spiceInterfacer = new SpiceInterfacer(resultNodes ,analyzeCircuitImpl.getElements());
        spiceInterfacer.testParser(input);

        for(CircuitNode resultNode : resultNodes) {
            String spiceLabel = resultNode.getSpiceLabel();
            Double voltage = resultNode.getVoltage();
            if(spiceLabel.equals("1")) {
                assertEquals(24.0, voltage);
            } else if(spiceLabel.equals("2")) {
                assertEquals(9.74697, voltage);
            } else if(spiceLabel.equals("3")) {
                assertEquals(15.0, voltage);
            } else {
                assertEquals(0.0, voltage);
                assertEquals("0", spiceLabel);
            }
        }

        for(CircuitElm resultElm : resultElms) {
            int numSources = 0;
            if(resultElm instanceof VoltageElm) {
                String spiceLabel = ((VoltageElm) resultElm).getSpiceLabel();
                Double current = resultElm.getCurrent();
                if(spiceLabel.equals("v2")) {
                    assertEquals(current, -0.000648522);
                } else if(spiceLabel.equals("v1")) {
                    assertEquals(current, -0.0014253);
                } else {
                    fail();
                }
            }
        }
    }

    @Test
    public void callNgOutputParserTest() {
        String input =
                "Note: can't find init_test file.\n" +
                        "\n" +
                        "Circuit: * my circuit\n" +
                        "\n" +
                        "Doing analysis at TEMP = 27.000000 and TNOM = 27.000000\n" +
                        "\n" +
                        "\n" +
                        "Initial Transient Solution\n" +
                        "--------------------------\n" +
                        "\n" +
                        "Node                                   Voltage\n" +
                        "----                                   -------\n" +
                        "1                                           24\n" +
                        "2                                      9.74697\n" +
                        "3                                           15\n" +
                        "v2#branch                         -0.000648522\n" +
                        "v1#branch                           -0.0014253\n";

        SimplePoint f = new SimplePoint(100,200);//arb inputs
        SimplePoint g = new SimplePoint(100,200);//arb inputs

        final CircuitNode node1 = new CircuitNode();
        final CircuitNode node2 = new CircuitNode();
        final CircuitNode node3 = new CircuitNode();
        // represents the v1#branch
        final CircuitElm elm1 = new ResistorElm(g, f, 1.0);
        //represents the v2#branch
        final CircuitElm elm2 = new ResistorElm(f, g, 3.0);

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

        SpiceInterfacer spiceInterfacer = new SpiceInterfacer(null, null);

        spiceInterfacer.callNgOutputParser(input, nodes, elms);
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
