package com.cpen321.circuitsolver.model;

import android.graphics.Point;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Jennifer on 10/10/2016.
 */
public class CircuitNode {

    private List<Point> points = new ArrayList<Point>();
    private boolean isValidVoltage;
    private double voltage;

    public CircuitNode(){
        isValidVoltage = false;
    }

    /*
    TODO: Can't think of a scenario where we might use this constructor.
    We would probably know points before voltage.
     */
    public CircuitNode(double voltage){
        isValidVoltage = true;
        this.voltage = voltage;
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
    public boolean correspondsToPoint(Point p){
        if(points.contains(p))
            return true;
        return false;
    }

    public void addPoint(Point p){
        points.add(p);
    }

    /**
     * Appends the points to CircuitNode's list of points
     * @param points
     */
    public void addPoints(List<Point> points){
        //Rep invariant: parameter points should not be modified
        this.points.addAll(points);
    }

    public List<Point> getPoints(){
        return Collections.unmodifiableList(points);
    }

    @Override
    public String toString(){
        StringBuilder s = new StringBuilder();
        s.append("CircuitNode: {");
        for(Point p: points){
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
            for(Point p: points){
                if(!((CircuitNode) other).getPoints().contains(p)){
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
