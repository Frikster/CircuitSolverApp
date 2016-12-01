package com.cpen321.circuitsolver.model;

import com.cpen321.circuitsolver.model.components.CircuitElm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Jennifer on 10/10/2016.
 */
public class CircuitNode implements SpiceLabel{

    private static int numNodes = 0;

    private List<SimplePoint> points = new ArrayList<SimplePoint>();
    private List<CircuitElm> elements = new ArrayList<CircuitElm>();

    private boolean isValidVoltage;
    private double voltage;
    private String label;

    public CircuitNode(){
        this.isValidVoltage = false;
        this.label = "" + numNodes; //possible that nodes may be deleted but numNodes won't be decremented. This shouldn't matter for SPICE...
        numNodes++;
    }

    public double getVoltage(){
        return voltage;
    }

    public void setVoltage(double voltage){
        isValidVoltage = true;
        this.voltage = voltage;
    }

    /**
     * Returns true if node corresponds to the given point, false otherwise
     * @param p the point to check
     * @return true if node coresponds to the given point, false otherwise
     */
    public boolean correspondsToPoint(SimplePoint p){
        if(points.contains(p))
            return true;
        return false;
    }

    public void addPoint(SimplePoint p){
        points.add(p);
    }

    /**
     * Appends the points to CircuitNode's list of points
     * @param points
     */
    public void addPoints(List<SimplePoint> points){
        //Rep invariant: parameter points should not be modified
        this.points.addAll(points);
    }

    public List<SimplePoint> getPoints(){
        return Collections.unmodifiableList(points);
    }
    public List<CircuitElm> getElements(){
        return Collections.unmodifiableList(elements);
    }

    public void addElement(CircuitElm elm){
        elements.add(elm);
    }

    /**
     * Appends the elements to CircuitNode's list of elements
     * @param elements
     */
    public void addElements(List<CircuitElm> elements){
        //Rep invariant: parameter points should not be modified
        this.elements.addAll(elements);
    }


    @Override
    public String toString(){
        StringBuilder s = new StringBuilder();
        s.append("CircuitNode: {");
        for(SimplePoint p: points){
            s.append(p.toString());
        }
        s.append("}");
        return s.toString();
    }

    @Override
    public boolean equals(Object other){
        //In a circuit, if nodes share the same points then they are the same node. Thus we do not need to check voltage.
        if(other instanceof CircuitNode){
            if(points.size() != ((CircuitNode) other).getPoints().size())
                return false;
            for(SimplePoint p: points){
                if(!((CircuitNode) other).getPoints().contains(p)){
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public String getSpiceLabel() {
        return this.label;
    }

    public void setSpiceLabel(String label) { this.label = label; }

    public static void resetNumNodes() { numNodes = 0; }
}
