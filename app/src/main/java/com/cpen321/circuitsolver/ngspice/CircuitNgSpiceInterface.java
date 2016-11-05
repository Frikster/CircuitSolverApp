package com.cpen321.circuitsolver.ngspice;

import com.cpen321.circuitsolver.model.CircuitNode;
import com.cpen321.circuitsolver.model.components.CircuitElm;

import java.util.List;

/**
 * Created by lotus on 05/11/16.
 */

public class CircuitNgSpiceInterface {
    private final List<CircuitNode> nodes;
    private final List<CircuitElm> elements;

    private final static String controlCommands = ".CONTROL\n" +
            "tran 1ns 1ns\n" +
            ".ENDC\n" +
            ".END";

    public CircuitNgSpiceInterface(List<CircuitNode> nodes, List<CircuitElm> elements) {
        this.nodes = nodes;
        this.elements = elements;
    }
}
