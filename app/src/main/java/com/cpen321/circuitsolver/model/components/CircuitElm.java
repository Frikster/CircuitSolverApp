package com.cpen321.circuitsolver.model.components;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import com.cpen321.circuitsolver.model.CircuitNode;
import com.cpen321.circuitsolver.model.SimplePoint;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.abs;

/**
 * Created by Jennifer on 10/10/2016.
 */
public abstract class CircuitElm{



    //protected CircuitNode[] nodes;
    private CircuitNode n1;
    private CircuitNode n2;

    private SimplePoint p1;
    private SimplePoint p2;

    private boolean isSelected;

    public double getCurrent() {
        return current;
    }

    public void setCurrent(double current) {
        this.current = current;
    }

    private double current;

    public CircuitElm() {

    }

    public CircuitElm(SimplePoint p1, SimplePoint p2){
//        if (p2.getX() <= p1.getX() && p2.getY() <= p1.getY()) {
//            this.p1 = p2;
//            this.p2 = p1;
//        } else {
            this.p1 = p1;
            this.p2 = p2;
//        }
    }


    /**
     * Returns true if node corresponds to the given point, false otherwise
     * @param p the point to check
     * @return true if node coresponds to the given point, false otherwise
     */
    public boolean correspondsToPoint(SimplePoint p){
        if(p.equals(p1) || p.equals(p2))
            return true;
        return false;
    }


    public SimplePoint getP1() {
        return p1;
    }

    public void setP1(SimplePoint p1) {
        this.p1 = p1;
    }

    public SimplePoint getP2() {
        return p2;
    }

    public void setP2(SimplePoint p2) {
        this.p2 = p2;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CircuitElm))
            return false;
        if (!this.getType().equals(((CircuitElm) o).getType()))
            return false;

        return (this.p1.equals( ((CircuitElm) o).getPoint(0)) && this.p2.equals( ((CircuitElm) o).getPoint(1)));
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



    public void onDraw(Canvas canvas, Paint paint, int yDisp) {

    }

    public void onDraw(Canvas canvas, Paint paint, int yDisp, boolean test) {


    }

    public abstract void draw(Canvas canvas, float startX, float startY, float stopX, float stopY, Paint paint);

    public boolean isSelected(){
        return isSelected;
    }


    public void toggleIsSelected(){
        Log.i("RECT", "in CircuitElm toggleIsSelected");
        isSelected = !isSelected;
    }

    protected void showSelected(Canvas canvas) {
        SimplePoint p1 = this.getP1();
        SimplePoint p2 = this.getP2();

        float left, right, top, bottom;
        float quarterLength = 20;


        if(p1.getY() == p2.getY()){
            if(p1.getX() < p2.getX()){
                left = p1.getX();
                right = p2.getX();
            }
            else{
                left = p2.getX();
                right = p1.getX();
            }
            top = p1.getY() - quarterLength;
            bottom = p1.getY() + quarterLength;
        }
        else{
            if(p1.getY() < p2.getY()){
                top = p1.getY();
                bottom = p2.getY();
            }
            else{
                top = p2.getY();
                bottom = p1.getY();
            }
            left = p1.getX() - quarterLength;
            right = p1.getX() + quarterLength;

        }
        Paint rectPaint = new Paint();
        rectPaint.setColor(Color.RED);
        rectPaint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(left, top, right, bottom, rectPaint);
    }

    public double getValue() {
        return 0;
    }

    @Override
    public String toString() {
        return "{type: " + this.getType() + ", val: " + this.getValue() + ", sX: " + this.getP1().getX()
            + ", sY:" + this.getP1().getY() + ", eX: " + this.getP2().getX() + ", eY: " + this.getP2().getY() +  "}";
    }

    public boolean isWire(){
        return false;
    }

    public boolean isVertical() {
        if(Math.abs(this.p1.getX() - this.p2.getX()) < 50 ) {
            return true;
        }
        return false;
    }
}
