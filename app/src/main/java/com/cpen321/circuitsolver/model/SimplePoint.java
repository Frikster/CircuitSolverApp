package com.cpen321.circuitsolver.model;

/**
 * Created by Jennifer on 10/29/2016.
 */

public class SimplePoint {
    private int x;
    private int y;

    public SimplePoint(int x, int y){
        this.x = x;
        this.y = y;
    }

    public int getX(){
        return this.x;
    }

    public int getY(){
        return this.y;
    }

    @Override
    public boolean equals(Object o){
        if(!(o instanceof SimplePoint))
            return false;
        if(this.x == ((SimplePoint) o).getX() && this.y == ((SimplePoint) o).getY())
            return true;
        else
            return false;
    }

    public boolean isCloserToOriginThan(SimplePoint other){
        if ( (Math.pow((double) this.x, 2) + Math.pow((double) this.y, 2) ) > (Math.pow((double) other.getX(), 2) + Math.pow((double) other.getY(), 2))) {
            return true;
        }
        else{
            return false;
        }
    }

    public String toString(){
        return "(" + x + "," + y + ")";
    }
}
