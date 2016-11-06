package com.cpen321.circuitsolver.ngspice;

import android.util.Log;

import com.cpen321.circuitsolver.model.CircuitNode;
import com.cpen321.circuitsolver.model.components.CircuitElm;
import com.cpen321.circuitsolver.util.Constants;

import java.util.List;

/**
 * Created by lotus on 05/11/16.
 */

public class CircuitNgSpiceInterface {
    public static String TAG = "CircuitNgSpiceInterface";
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
    public CircuitNgSpiceInterface(List<CircuitElm> elements) {
        this.elements = elements;
    }

    /**
     * Create and return a string which describes the netlist in ngspice format
     * @return a string which describes the netlist in ngspice format
     */
    private String createNetlist() {
        //TODO add support for elements other than inductors and dc sources
        StringBuilder netlist = new StringBuilder();
        netlist.append(netListName);
        for(CircuitElm element : elements) {
            String type = element.getType();
            if(!type.equals(Constants.WIRE)) {
                CircuitNode posTerminal = element.getNode(0);
                CircuitNode negTerminal = element.getNode(1);
                if (posTerminal == null || negTerminal == null) {
                    Log.e(TAG, "Terminal of circuit element is not connect to a node");
                }
                if(type.equals(Constants.DC_VOLTAGE)) {

                } //else if(type.eq)

            }
        }
        return null;
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
