package com.cpen321.circuitsolver.ngspice;

import android.util.Log;

import com.cpen321.circuitsolver.model.CircuitNode;
import com.cpen321.circuitsolver.model.SpiceElm;
import com.cpen321.circuitsolver.model.components.CircuitElm;
import com.cpen321.circuitsolver.model.components.VoltageElm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lotus on 05/11/16.
 */

public class SpiceInterfacer {
    public static String TAG = "SpiceInterfacer";
    private final List<CircuitElm> elements;
    private final List<CircuitNode> nodes;
    private final static String controlCommands = ".CONTROL\n" +
            "tran 1ns 1ns\n" +
            ".ENDC\n" +
            ".END\n";
    private final static String netListName = "* My Circuit\n";
    private final Map<String, CircuitNode> circuitNodeMap;
    private final Map<String, CircuitElm> circuitElmMap;


    /**
     * Pre: nodes and elements must describe a proper circuit (a non wire element must be connected to two nodes, no gaps in circuit)
     * @param elements
     */
    public SpiceInterfacer(List<CircuitNode> nodes, List<CircuitElm> elements) {
        this.nodes = nodes;
        this.elements = elements;
        circuitNodeMap = new HashMap<String, CircuitNode>();
        circuitElmMap = new HashMap<String, CircuitElm>();
        for(CircuitNode node : nodes) {
            if(node.getSpiceLabel().equals("0")) {
                return;
            }
        }
        nodes.get(0).setSpiceLabel("0");
    }

    /**
     * Updates nodes with voltage values, and voltage sources with current values
     * @param ngSpice
     * @return true if successful
     */
    public boolean solveCircuit(NgSpice ngSpice) {
        String input = getNgSpiceInput();
        String spiceOutput = ngSpice.callNgSpice(input);
        if(spiceOutput.contains("failed")) {
            return false;
        }
        populateMaps();
        callNgOutputParser(spiceOutput, circuitNodeMap, circuitElmMap);
        return true;
    }

    //TODO every mothid below this point should be private later or removed

    public String getNgSpiceInput() {
        return addControl(createNetlist());
    }

    public void testParser(String input) {
        populateMaps();
        callNgOutputParser(input, circuitNodeMap, circuitElmMap);
    }

    public void callNgOutputParser(String input, Map<String, CircuitNode> nodes, Map<String, CircuitElm> elms ) {
        boolean addWord = false;
        boolean isValueNum = false;
        boolean isNode = false;
        String key = null;

        //replace all "\n" with spaces
        String s = input;
        s = s.replaceAll("\n", " ");
        //split everything separated by spaces into new words
        String[] arr = s.split(" ");

        if(nodes.containsKey("0")) {
            nodes.get("0").setVoltage(0);
        }

        for ( String word : arr) {
            //stop adding words after "No." word
            if(word.equals("No.")) {
                addWord = false;
            }
            //use this word
            if(addWord && !word.equals("") && word != null) {
                //node or element name, not a value
                if (!isValueNum) {
                    Log.v("Name of element or node", word);
                    //next word will be a value number
                    isValueNum = true;
                    //get the key for the Map
                    key = word;
                    //is a branch
                    if(word.length() != 0 && word.charAt(0)!= 'v'){
                        isNode = true;
                    }
                    //is a node
                    else{
                        isNode = false;
                    }
                }
                //is a voltage or current value
                else {
                    Log.v("Value to add", word );
                    //next word is not a value
                    isValueNum = false;
                    //add voltage to node
                    if(isNode){
                        CircuitNode currNode = nodes.get(key);
                        double value = Double.parseDouble(word);
                        if(currNode != null)
                            currNode.setVoltage(value);
                    }
                    //add current to element
                    else {
                        CircuitElm currElm = elms.get(key);
                        if(currElm != null)
                            currElm.setCurrent(Double.parseDouble(word));
                    }
                }
            }
            //Start adding words after word "-------"
            if(word.equals("-------")){
                addWord = true;
            }
        }
    }

    /**
     * Create and return a string which describes the netlist in ngspice format
     * Updates internal CircuitElm/CircuitNode maps used for quick parsing
     * @return a string which describes the netlist in ngspice format
     */
    private String createNetlist() {
        StringBuilder netlist = new StringBuilder();
        netlist.append(netListName);
        for(CircuitElm element : elements) {
            if(element instanceof SpiceElm) {
                SpiceElm spiceElm = (SpiceElm)element;
                netlist.append(spiceElm.constructSpiceLine() + "\n");
            }
        }
        String result = netlist.toString();
        Log.d(TAG, result);
        return result;
    }

    /**
     * Adds command controls to netlist
     * @param netlist a proper netlist
     * @return a string with added control commands to netlist
     */
    private String addControl(String netlist) {
        return netlist + "\n" + controlCommands;
    }

    /*
     * Populates private maps used used by parser
     */
    private void populateMaps() {
        for(CircuitNode node : nodes) {
            circuitNodeMap.put(node.getSpiceLabel(), node);
        }
        for(CircuitElm element : elements) {
            if(element instanceof VoltageElm) {
                circuitElmMap.put(((VoltageElm) element).getSpiceLabel() + "#branch", element);
            }
        }
    }
}