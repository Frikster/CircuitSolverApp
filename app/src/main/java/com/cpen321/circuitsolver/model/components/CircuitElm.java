package com.cpen321.circuitsolver.model.components;

import com.cpen321.circuitsolver.model.CircuitNode;
import com.cpen321.circuitsolver.model.SimplePoint;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jennifer on 10/10/2016.
 */
public abstract class CircuitElm {

    //protected CircuitNode[] nodes;
    private CircuitNode n1;
    private CircuitNode n2;

    private SimplePoint p1;
    private SimplePoint p2;

    public CircuitElm(SimplePoint p1, SimplePoint p2){
        this.p1 = p1;
        this.p2 = p2;
    }

    /**
     * Find the voltage difference across the element
     * @return
     */
    public abstract double getVoltageDiff();

    /**
     * Find the current flowing through the element
     * @return
     */
    public abstract double calculateCurrent();

    /**
     * Sets the value of the element (resistance for ResistorElm, voltage for VoltageElm, etc...)
     * @param value
     */
    public abstract void setValue(double value);

    /**
     * Returns element type
     * @return element type (resistor, wire, etc.)
     */
    public abstract String getType();
    
    /**
     * Returns index of node corresponding to given node, else -1 if element does not correspond to node
     * @param node
     * @return
     */
    public int indexOfNode(CircuitNode node){
        if(node.equals(n1))
            return 0;
        else if (node.equals(n2))
            return 1;

        return -1;
    }

    /*    Getters and Setters     */

    /**
     *
     * @param i
     *      Requires i = 0 or 1
     * @param node
     */
    public void setNode(int i, CircuitNode node){
        if(i == 0)
            n1 = node;
        if(i == 1)
            n2 = node;
    }

    /**
     *
     * @param i
     *      Requires i <= 2
     * @return Node corresonding to index i, null if no node corresponds to i
     */
    public CircuitNode getNode(int i){
        if(i == 0)
            return n1;
        else if(i == 1)
            return n2;
        return null;
    }

    public int getNumPoints(){
        return 2;
    }

    public SimplePoint getPoint(int i){
        if(i == 0)
            return p1;
        if(i == 1)
            return p2;
        return null;
    }

    public List<CircuitNode> getNodes(){
        List<CircuitNode> nodes = new ArrayList<CircuitNode>();
        nodes.add(n1);
        nodes.add(n2);
        return nodes;
    }

    @Override
    public String toString(){
        return p1 + ", " + p2;
    }

}
