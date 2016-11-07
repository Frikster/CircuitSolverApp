package com.cpen321.circuitsolver.ngspice;

import android.os.Looper;
import android.support.test.runner.AndroidJUnitRunner;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.cpen321.circuitsolver.model.SimplePoint;
import com.cpen321.circuitsolver.model.components.CircuitElm;
import com.cpen321.circuitsolver.model.CircuitNode;
import com.cpen321.circuitsolver.model.ResetComponents;
import com.cpen321.circuitsolver.model.SimplePoint;
import com.cpen321.circuitsolver.model.components.CircuitElm;
import com.cpen321.circuitsolver.model.components.ResistorElm;
import com.cpen321.circuitsolver.model.components.VoltageElm;
import com.cpen321.circuitsolver.model.components.WireElm;
import com.cpen321.circuitsolver.service.AnalyzeCircuitImpl;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

/**
 * Created by lotus on 06/11/16.
 */

@RunWith(AndroidJUnit4.class)
public class SpiceInterfacerAndroidTest extends AndroidJUnitRunner {
    @Before
    public void initialize() {
        Looper.prepare();
    }

    @Test
    public void solveCircuitTest() {
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
        List<CircuitElm> resultElms = analyzeCircuitImpl.getElements();

        SpiceInterfacer spiceInterfacer = new SpiceInterfacer(resultNodes, resultElms);
        spiceInterfacer.solveCircuit(NgSpice.getInstance(InstrumentationRegistry.getTargetContext()));

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
}
