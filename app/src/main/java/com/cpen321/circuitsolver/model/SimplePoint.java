package com.cpen321.circuitsolver.model;

/**
 * Created by Jennifer on 10/29/2016.
 */

public class SimplePoint {
    private int x;
    private int y;

    public static SimplePoint copy(SimplePoint other){
        return new SimplePoint(other.getX(), other.getY());
    }

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
        if (( x*x + y*y ) > ( (other.getX() * other.getX()) + (other.getY() * other.getY())) ){
            return true;
        }
        else{
            return false;
        }
    }

    @Override
    public String toString(){
        return "(" + x + "," + y + ")";
    }

    public double distanceFrom(SimplePoint point2) {
        if (point2 == null)
            return 0;
        return Math.hypot(point2.getX() - this.getX(),
                point2.getY()-this.getY());
    }
}
