package com.cpen321.circuitsolver.service;


import com.cpen321.circuitsolver.model.CircuitNode;
import com.cpen321.circuitsolver.model.SimplePoint;
import com.cpen321.circuitsolver.model.components.CircuitElm;
import com.cpen321.circuitsolver.model.components.ResistorElm;
import com.cpen321.circuitsolver.model.components.VoltageElm;
import com.cpen321.circuitsolver.model.components.WireElm;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;


/**
 * Created by Jennifer on 10/13/2016.
 */
public class AnalyzeCircuitImplTest {

    @Test
    public void testInitAndAllocateNodes_twoNodeCircuit(){

        List<CircuitElm> elements = new ArrayList<CircuitElm>();
        elements.add(new WireElm(new SimplePoint(10,10), new SimplePoint(10, 0)));
        elements.add(new ResistorElm(new SimplePoint(0, 10), new SimplePoint(10, 10), 10));
        elements.add(new VoltageElm(new SimplePoint(0, 0), new SimplePoint(10, 0), 10));
        elements.add(new WireElm(new SimplePoint(0,0), new SimplePoint(0, 10)));

        AnalyzeCircuitImpl analyzeCircuitImpl = new AnalyzeCircuitImpl(elements);
        analyzeCircuitImpl.init();

        List<CircuitNode> result = analyzeCircuitImpl.getNodes();
        System.out.println(result);

        /*
        Assert that nodes correspond to correct points
         */

        //Node 1 should correspond to (0,0), (0,10)
        SimplePoint p1 = new SimplePoint(0,0);
        SimplePoint p2 = new SimplePoint(0,10);
        CircuitNode cn = new CircuitNode();
        cn.addPoint(p1);
        cn.addPoint(p2);
        assertTrue("No node corresponding to " + cn, result.contains(cn));

        //Node 2 should correspond to (10,0), (10,10)
        p1 = new SimplePoint(10,0);
        p2 = new SimplePoint(10,10);
        cn = new CircuitNode();
        cn.addPoint(p1);
        cn.addPoint(p2);
        assertTrue("No node corresponding to " + cn, result.contains(cn));

        /*
        Assert that elements correspond to correct nodes
         */

        //Each element's nodes should contain one of the elements coordinates
        for(CircuitElm e: elements){
            p1 = e.getPoint(0);
            p2 = e.getPoint(1);
            assertTrue("No element nodes in " + e + " match its post at " + p1, e.getNode(0).correspondsToPoint(p1) || e.getNode(1).correspondsToPoint(p1));
            assertTrue("No element nodes in " + e + " match its post at " + p2, e.getNode(0).correspondsToPoint(p2) || e.getNode(1).correspondsToPoint(p2));
        }


    }

    @Test
    public void testInitAndAllocateNodes_intermediateCircuit(){
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



        AnalyzeCircuitImpl analyzeCircuitImpl = new AnalyzeCircuitImpl(elements);
        analyzeCircuitImpl.init();

        List<CircuitNode> result = analyzeCircuitImpl.getNodes();
        System.out.println(result);

        //Node 1 should correspond to (0,0), (0,5), (0,10), (0,15)
        SimplePoint[] points1 = {new SimplePoint(0,0), new SimplePoint(0,5), new SimplePoint(0,10), new SimplePoint(0,15)};

        CircuitNode cn = new CircuitNode();
        cn.addPoints(Arrays.asList(points1));
        assertTrue("No node corresponding to " + cn, result.contains(cn));

        //Node 2 should correspond to (5,0), (5,5), (5,10), (5,15)
        SimplePoint[] points2 = {new SimplePoint(5,0), new SimplePoint(5,5), new SimplePoint(5,10), new SimplePoint(5,15)};
        cn = new CircuitNode();
        cn.addPoints(Arrays.asList(points2));
        assertTrue("No node corresponding to " + cn, result.contains(cn));

        /*
        Assert that elements correspond to correct nodes
        */

        //Each element's nodes should contain one of the elements coordinates
        verifyElementNodes(elements);

    }

    @Test
    public void testInitAndAllocateNodes_complexCircuit(){
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


        AnalyzeCircuitImpl analyzeCircuitImpl = new AnalyzeCircuitImpl(elements);
        analyzeCircuitImpl.init();

        List<CircuitNode> result = analyzeCircuitImpl.getNodes();
        System.out.println(result);

        //Node 1 should correspond to (0,0), (0,5)
        SimplePoint[] points1 = {new SimplePoint(0,0), new SimplePoint(0,5)};

        CircuitNode cn = new CircuitNode();
        cn.addPoints(Arrays.asList(points1));
        assertTrue("No node corresponding to " + cn, result.contains(cn));

        //Node 2 should correspond to (5,0), (5,3), (5,5), (5,8), (5,10), (10,3), (10,8)
        SimplePoint[] points2 = {new SimplePoint(5,0), new SimplePoint(5,3), new SimplePoint(5,5), new SimplePoint(5,8), new SimplePoint(5,10), new SimplePoint(10,3), new SimplePoint(10,8)};
        cn = new CircuitNode();
        cn.addPoints(Arrays.asList(points2));
        assertTrue("No node corresponding to " + cn, result.contains(cn));

        //Node 3 should correspond to (0,10)
        SimplePoint point3 = new SimplePoint(0,10);
        cn = new CircuitNode(); 
        cn.addPoint(point3);
        assertTrue("No node corresponding to " + cn, result.contains(cn));

        /*
        Assert that elements correspond to correct nodes
        */

        //Each element's nodes should contain one of the elements coordinates
        verifyElementNodes(elements);

    }

    private void verifyElementNodes(List<CircuitElm> elements) {
        for(CircuitElm e: elements){
            SimplePoint p1 = e.getPoint(0);
            SimplePoint p2 = e.getPoint(1);
            assertTrue("No element nodes in " + e + " match its post at " + p1, e.getNode(0).correspondsToPoint(p1) || e.getNode(1).correspondsToPoint(p1));
            assertTrue("No element nodes in " + e + " match its post at " + p2, e.getNode(0).correspondsToPoint(p2) || e.getNode(1).correspondsToPoint(p2));
        }
    }


    @Test
    public void testGetCurrent_threeNodeCircuit_returnsVoltageCurrent(){
        fail("not implemented");
    }

    @Test
    public void testGetCurrent_sixNodeCircuit_returnsResistorCurrent(){
        fail("not implemented");
    }

    @Test
    public void testGetCurrent_sixNodeCircuit_returnsVoltageCurrent(){
        fail("not implemented");
    }


}
