package com.cpen321.circuitsolver.opencv;

/**Abstract superclass defining an Element (extended by Corner and Component)
 * Created by Simon on 24.10.2016.
 */

public abstract class Element {
    protected double positionX;
    protected double positionY;

    public Element(double x, double y){
        positionX = x;
        positionY = y;
    }

    public double getX(){
        return positionX;
    }
    public double getY(){
        return positionY;
    }

    @Override
    public boolean equals(Object o){
        if(o == null) return false;
        if(o == this) return true;
        if(!(o instanceof Element)) return false;
        else{
            return ((Element)o).positionY == positionY && ((Element)o).positionX == positionX;
        }
    }

}
