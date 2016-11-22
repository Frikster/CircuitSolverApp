package com.cpen321.circuitsolver.opencv.opencvModel;

/**Abstract superclass defining an Element (extended by Corner and Component)
 * Created by Simon on 24.10.2016.
 */

public abstract class Element {
    private float positionX;
    private float positionY;

    public Element(float x, float y){
        positionX = x;
        positionY = y;
    }

    public float getX(){
        return positionX;
    }
    public float getY(){
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
