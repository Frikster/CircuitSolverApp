package com.cpen321.circuitsolver.ngspice;

import com.cpen321.circuitsolver.model.CircuitNode;
import com.cpen321.circuitsolver.model.SimplePoint;
import com.cpen321.circuitsolver.model.components.CircuitElm;
import com.cpen321.circuitsolver.model.components.ResistorElm;
import com.cpen321.circuitsolver.model.components.VoltageElm;
import com.cpen321.circuitsolver.model.components.WireElm;
import com.cpen321.circuitsolver.service.AnalyzeCircuitImpl;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import static junit.framework.Assert.assertTrue;

/**
 * Created by lotus on 05/11/16.
 */

public class SpiceInterfacerTest {
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

        SpiceInterfacer spiceInterfacer = new SpiceInterfacer(analyzeCircuitImpl.getElements());
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


}
