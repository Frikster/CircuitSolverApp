package com.cpen321.circuitsolver.model;

import com.cpen321.circuitsolver.model.components.CircuitElm;
import com.cpen321.circuitsolver.model.components.ResistorElm;
import com.cpen321.circuitsolver.model.components.VoltageElm;
import com.cpen321.circuitsolver.model.components.WireElm;
import com.cpen321.circuitsolver.service.AllocateNodes;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jennifer on 10/30/2016.
 */

public class SpiceElmTest {

    @Test
    public void testPrintSimpleCircuit(){
        List<CircuitElm> elements = new ArrayList<CircuitElm>();
        elements.add(new WireElm(new SimplePoint(10,10), new SimplePoint(10, 0)));
        elements.add(new ResistorElm(new SimplePoint(0, 10), new SimplePoint(10, 10), 10));
        elements.add(new VoltageElm(new SimplePoint(0, 0), new SimplePoint(10, 0), 10));
        elements.add(new WireElm(new SimplePoint(0,0), new SimplePoint(0, 10)));

        AllocateNodes allocateNodes = new AllocateNodes(elements);
        allocateNodes.allocate();

        for(CircuitElm elm : allocateNodes.getElements()){
            if(elm instanceof SpiceElm)
                System.out.println(((SpiceElm) elm).constructSpiceLine());
        }
    }

    @Test
    public void testPrintIntermediateCircuit(){
        List<CircuitElm> elements = new ArrayList<CircuitElm>();
        elements.add(new ResistorElm(new SimplePoint(0, 15), new SimplePoint(5, 15), 10));
        elements.add(new ResistorElm(new SimplePoint(0, 10), new SimplePoint(5, 10), 10));
        elements.add(new ResistorElm(new SimplePoint(0, 5), new SimplePoint(5, 5), 10));

        elements.add(new WireElm(new SimplePoint(0,0), new SimplePoint(0, 5)));
        elements.add(new WireElm(new SimplePoint(0,5), new SimplePoint(0,10)));
        elements.add(new WireElm(new SimplePoint(0,10), new SimplePoint(0,15)));
        elements.add(new WireElm(new SimplePoint(5,15), new SimplePoint(5,10)));
        elements.add(new WireElm(new SimplePoint(5,10), new SimplePoint(5,5)));
        elements.add(new WireElm(new SimplePoint(5,5), new SimplePoint(5, 0)));

        elements.add(new VoltageElm(new SimplePoint(0, 0), new SimplePoint(5, 0), 10));

        AllocateNodes allocateNodes = new AllocateNodes(elements);
        allocateNodes.allocate();

        for(CircuitElm elm : allocateNodes.getElements()){
            if(elm instanceof SpiceElm)
                System.out.println(((SpiceElm) elm).constructSpiceLine());
        }
    }

    @Test
    public void testPrintComplexCircuit(){
        List<CircuitElm> elements = new ArrayList<CircuitElm>();
        elements.add(new ResistorElm(new SimplePoint(0, 10), new SimplePoint(5, 10), 10));
        elements.add(new ResistorElm(new SimplePoint(0, 5), new SimplePoint(5, 5), 10));
        elements.add(new ResistorElm(new SimplePoint(10, 8), new SimplePoint(10, 3), 10));

        elements.add(new WireElm(new SimplePoint(0,0), new SimplePoint(0, 5)));
        elements.add(new WireElm(new SimplePoint(5,0), new SimplePoint(5,3)));
        elements.add(new WireElm(new SimplePoint(5,3), new SimplePoint(10,3)));
        elements.add(new WireElm(new SimplePoint(5,3), new SimplePoint(5,5)));
        elements.add(new WireElm(new SimplePoint(5,5), new SimplePoint(5,8)));
        elements.add(new WireElm(new SimplePoint(10,8), new SimplePoint(5, 8)));
        elements.add(new WireElm(new SimplePoint(5,8), new SimplePoint(5, 10)));


        elements.add(new VoltageElm(new SimplePoint(0, 5), new SimplePoint(0,10), 10));
        elements.add(new VoltageElm(new SimplePoint(0, 0), new SimplePoint(5, 0), 10));


        AllocateNodes allocateNodes = new AllocateNodes(elements);
        allocateNodes.allocate();

        for(CircuitElm elm : allocateNodes.getElements()){
            if(elm instanceof SpiceElm)
                System.out.println(((SpiceElm) elm).constructSpiceLine());
        }
    }
}
