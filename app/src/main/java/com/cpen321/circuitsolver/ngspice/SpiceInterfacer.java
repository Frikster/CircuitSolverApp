package com.cpen321.circuitsolver.ngspice;

import android.util.Log;

import com.cpen321.circuitsolver.model.CircuitNode;
import com.cpen321.circuitsolver.model.SpiceElm;
import com.cpen321.circuitsolver.model.components.CircuitElm;
import com.cpen321.circuitsolver.util.Constants;

import java.util.List;

/**
 * Created by lotus on 05/11/16.
 */

public class SpiceInterfacer {
    public static String TAG = "SpiceInterfacer";
    private final List<CircuitElm> elements;
    private final static String controlCommands = ".CONTROL\n" +
            "tran 1ns 1ns\n" +
            ".ENDC\n" +
            ".END\n";
    private final static String netListName = "* My Circuit\n";

    /**
     * Pre: nodes and elements must describe a proper circuit
     * @param elements
     */
    public SpiceInterfacer(List<CircuitElm> elements) {
        this.elements = elements;
    }

    public String getNgSpiceInput() {
        return addControl(createNetlist());
    }

    /**
     * Create and return a string which describes the netlist in ngspice format
     * @return a string which describes the netlist in ngspice format
     */
    private String createNetlist() {
        StringBuilder netlist = new StringBuilder();
        netlist.append(netListName);
        for(CircuitElm element : elements) {
            if(!element.getType().equals(Constants.WIRE)) {
                SpiceElm spiceElm = (SpiceElm)element;
                netlist.append(spiceElm.constructSpiceLine() + "\n");
            }
        }
        return netlist.toString();
    }

    /**
     * Adds command controls to netlist
     * @param netlist a proper netlist
     * @return a string with added control commands to netlist
     */
    private String addControl(String netlist) {
        return netlist + "\n" + controlCommands;
    }
}