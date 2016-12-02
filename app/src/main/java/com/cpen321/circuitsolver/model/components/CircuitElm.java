package com.cpen321.circuitsolver.model.components;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import com.cpen321.circuitsolver.model.CircuitNode;
import com.cpen321.circuitsolver.model.SimplePoint;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jennifer on 10/10/2016.
 */
public abstract class CircuitElm implements Cloneable{



    //protected CircuitNode[] nodes;
    private CircuitNode n1;
    private CircuitNode n2;

    private SimplePoint p1;
    private SimplePoint p2;

    private boolean isSelected;

    /**
     * Positive value means the flow of negative charge is from p1 to p2
     * @return the current
     */
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
     * Find the voltage difference across the element, v(p2) - v(p2)
     * @return
     */
    public abstract double getVoltageDiff();

    /**
     * Find the current flowing through the element
     * Updates current field
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

    public void drawCurrent(Canvas canvas, Paint paint) {
        if(p1 != null && p2 != null) {
            if(getCurrent() < 0) {
                drawArrow(canvas, p1.getX(), p1.getY(), p2.getX(), p2.getY(), paint, true);
            } else {
                drawArrow(canvas, p1.getX(), p1.getY(), p2.getX(), p2.getY(), paint, false);
            }
        }
    }

    private void drawArrow(Canvas canvas, float startX, float startY, float stopX, float stopY, Paint paint, boolean dirStartToStop){
            //Edit ARROW_WIDTH, ARROW_LENGTH, and SPACE_LENGTH as needed
            float ARROW_WIDTH = 20;
            float ARROW_LENGTH;
            float x = stopX - startX;
            float y = stopY - startY;
            float slope = y / x;
            float b = stopY - slope * stopX;
            float hypotenuse =  (float) Math.hypot(x, y);
            float SPACE_LENGTH = 3*hypotenuse/4;
            float d = (hypotenuse - SPACE_LENGTH) / 2;
            float angle = (float) Math.atan(slope);
            float perpAngle = (float) Math.atan(x / y);
            float innerD = (hypotenuse - 2 * d) / 5;

            float x3, x4, x5, x6, x7, x8, y3, y4, y5, y6, y7, y8;
            float x3i, y3i,x4i,y4i;

            //set the direction of the arrow
            if(dirStartToStop) {
                ARROW_LENGTH = 20;
            }else{
                ARROW_LENGTH = -20;
            }

            //when drawn from left to right
            if (x > 0) {
                x3 = stopX - (SPACE_LENGTH + d) * ((float) Math.cos(angle));
                y3 = stopY - (SPACE_LENGTH + d) * ((float) Math.sin(angle));
                x4 = stopX - (d) * ((float) Math.cos(angle));
                y4 = stopY - (d) * ((float) Math.sin(angle));

                x3i = stopX - (ARROW_LENGTH+SPACE_LENGTH + d) * ((float) Math.cos(angle));
                y3i = stopY - (ARROW_LENGTH+SPACE_LENGTH + d) * ((float) Math.sin(angle));
                x4i = stopX - (ARROW_LENGTH+d) * ((float) Math.cos(angle));
                y4i = stopY - (ARROW_LENGTH+d) * ((float) Math.sin(angle));

                x5 = x3i - ARROW_WIDTH * ((float) Math.cos(perpAngle));
                y5 = y3i + ARROW_WIDTH * ((float) Math.sin(perpAngle));

                x6 = x3i + ARROW_WIDTH * ((float) Math.cos(perpAngle));
                y6 = y3i - ARROW_WIDTH * ((float) Math.sin(perpAngle));

                x7 = x4i - ARROW_WIDTH * ((float) Math.cos(perpAngle));
                y7 = y4i + ARROW_WIDTH * ((float) Math.sin(perpAngle));

                x8 = x4i + ARROW_WIDTH * ((float) Math.cos(perpAngle));
                y8 = y4i - ARROW_WIDTH * ((float) Math.sin(perpAngle));
            }
            //when drawn right to left
            else if (x < 0) {
                x3 = startX - (d) * ((float) Math.cos(angle));
                y3 = startY - (d) * ((float) Math.sin(angle));
                x4 = startX - (SPACE_LENGTH + d) * ((float) Math.cos(angle));
                y4 = startY - (SPACE_LENGTH + d) * ((float) Math.sin(angle));

                x3i = startX - (d-ARROW_LENGTH) * ((float) Math.cos(angle));
                y3i = startY - (d-ARROW_LENGTH) * ((float) Math.sin(angle));
                x4i = startX - (SPACE_LENGTH + d-ARROW_LENGTH) * ((float) Math.cos(angle));
                y4i = startY - (SPACE_LENGTH + d-ARROW_LENGTH) * ((float) Math.sin(angle));

                x5 = x3i - ARROW_WIDTH * ((float) Math.cos(perpAngle));
                y5 = y3i + ARROW_WIDTH * ((float) Math.sin(perpAngle));

                x6 = x3i + ARROW_WIDTH * ((float) Math.cos(perpAngle));
                y6 = y3i - ARROW_WIDTH * ((float) Math.sin(perpAngle));

                x7 = x4i - ARROW_WIDTH * ((float) Math.cos(perpAngle));
                y7 = y4i + ARROW_WIDTH * ((float) Math.sin(perpAngle));

                x8 = x4i + ARROW_WIDTH * ((float) Math.cos(perpAngle));
                y8 = y4i - ARROW_WIDTH * ((float) Math.sin(perpAngle));
            }
            //when drawn vertically pointing down
            else if (y < 0) {
                x3 = stopX;
                y3 = startY - d;
                x4 = stopX;
                y4 = stopY + d;
                y5 = y3 +ARROW_LENGTH;
                y6 = y3 +ARROW_LENGTH;
                y7 = y4 +ARROW_LENGTH;
                y8 = y4 +ARROW_LENGTH;
                x5 = startX + ARROW_WIDTH;
                x6 = startX - ARROW_WIDTH;
                x7 = startX + ARROW_WIDTH;
                x8 = startX - ARROW_WIDTH;
            }
            //when drawn vertically pointing up
            else {
                x3 = stopX;
                y3 = startY + d;
                x4 = stopX;
                y4 = stopY - d;
                y5 = y3-ARROW_LENGTH;
                y6 = y3-ARROW_LENGTH;
                y7 = y4-ARROW_LENGTH;
                y8 = y4-ARROW_LENGTH;
                x5 = startX + ARROW_WIDTH;
                x6 = startX - ARROW_WIDTH;
                x7 = startX + ARROW_WIDTH;
                x8 = startX - ARROW_WIDTH;
            }
            //draw arrow lines
            canvas.drawLine(x3, y3, x5, y5, paint);
            canvas.drawLine(x3, y3, x6, y6, paint);
            canvas.drawLine(x4, y4, x7, y7, paint);
            canvas.drawLine(x4, y4, x8, y8, paint);

    }

    // Fix java's "protected clone" mistake: http://stackoverflow.com/a/1138790/2734863
    // note: returns null if cloning fails
    @Override
    public CircuitElm clone(){
        //return (CircuitElm) super.clone();
        Object clone = null;
        try {
            clone = super.clone();
        }
        catch (CloneNotSupportedException exception){
            // Why the try/catch? http://stackoverflow.com/a/8609338/2734863
        }
        return (CircuitElm) clone;
    }
}
