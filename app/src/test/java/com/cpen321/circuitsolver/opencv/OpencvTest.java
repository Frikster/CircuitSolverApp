package com.cpen321.circuitsolver.opencv;

import org.junit.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Simon on 29.11.2016.
 */

public class OpencvTest {
    @Test
    public void  findWiresTest() {

        Corner c1 = new Corner(0,0);
        c1.setNewDirection('w');
        c1.setNewDirection('e');
        c1.setNewDirection('s');
        c1.setNewDirection('n');
        Corner c2 = new Corner(100,100);
        c2.setNewDirection('w');
        c2.setNewDirection('e');
        c2.setNewDirection('s');
        c2.setNewDirection('n');
        Corner c3 = new Corner(0,100);
        c3.setNewDirection('w');
        c3.setNewDirection('e');
        c3.setNewDirection('s');
        c3.setNewDirection('n');
        Corner c4 = new Corner(100,0);
        c4.setNewDirection('w');
        c4.setNewDirection('e');
        c4.setNewDirection('s');
        c4.setNewDirection('n');
        Component co1 = new Component(0,50,"resistor");
        List<Element> elements = new ArrayList<>();
        elements.add(c1);
        elements.add(c2);
        elements.add(c3);
        elements.add(c4);
        elements.add(co1);
        double[] l1 = {1,0,99,0};
        double[] l2 = {100,1,100,99};
        double[] l3 = {1,100,99,100};
        double[] l4 = {0,51,0,99};
        double[] l5 = {0,1,0,49};
        List<double[]> resLines = new ArrayList<>();
        resLines.add(l1);
        resLines.add(l2);
        resLines.add(l3);
        resLines.add(l4);
        resLines.add(l5);
        WireCalculator wireP = new WireCalculator(elements,c1,resLines);
        List<List<Element>> wires = wireP.process();
        assert(wires.size() == 4);
    }

    @Test
    public void  processWiresTest() {

        Corner c1 = new Corner(0,0);
        Corner c2 = new Corner(100,0);
        Component co1 = new Component(50,0,"resistor");
        Component co2 = new Component(0,50,"resistor");
        Component co3 = new Component(100,30,"resistor");
        Component co4 = new Component(100,60,"resistor");
        Component co5 = new Component(50,100,"resistor");
        List<Element> wireFalse = new ArrayList<>();
        wireFalse.add(c1);
        wireFalse.add(c2);
        List<Element> wire1 = new ArrayList<>();
        wire1.add(c1);
        wire1.add(co1);
        wire1.add(c2);
        List<Element> wire2 = new ArrayList<>();
        wire2.add(c1);
        wire2.add(co2);
        List<Element> wire3 = new ArrayList<>();
        wire3.add(c2);
        wire3.add(co3);
        wire3.add(co4);
        List<List<Element>> wires = new ArrayList<>();
        wires.add(wire1);
        wires.add(wire2);
        wires.add(wire3);
        wires.add(wireFalse);

        List<Component> components = new ArrayList<>();
        components.add(co1);
        components.add(co2);
        components.add(co3);
        components.add(co4);
        components.add(co5);

        WireProcessor wireP = new WireProcessor(wires,components);
        List<List<Element>> normalizedWires = wireP.process();
        assert(normalizedWires.size()== 7);

    }
    @Test
    public void  processCornersTest() {

        //close to be horizontal
       double[] line1 = {1,0,100,0};

        //Diagonale
        double[] line2 ={0,0,100,100};

        //Vertical
        double[] line3 = {0,0,0,100};

        //horizontal
        double[] line4 = {0,100,95,100};

        //Vertical
        double[] line5 = {100,99,100,20};

        List<double[]> lines = new ArrayList<>();
        lines.add(line1);
        lines.add(line2);
        lines.add(line3);
        lines.add(line4);
        lines.add(line5);

        CornerDetector cornerD = new CornerDetector(lines,new ArrayList<PointDB>());
        List<double[]> corners = cornerD.process();
        assert(corners.size() == 3);

    }

}
