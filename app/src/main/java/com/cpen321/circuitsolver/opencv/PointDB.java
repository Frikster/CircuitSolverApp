package com.cpen321.circuitsolver.opencv;

/**Classes that modelizes a point, and has some useful fields to use for the dbscan algorithm
 * Created by Simon on 01.11.2016.
 * Some of this class has been copied from dataonfocus.com
 */

public class PointDB {
    public static int nrClusters = 1;
    private int cluster;
    private double X;
    private double Y;
    private boolean isCore;
    private boolean isAssigned;

    public PointDB()
    {
        X=0; Y=0;
    }
    public PointDB(double x,double y)
    {
        X=x;
        Y=y;
    }

    public void setAssigned(boolean val){
        isAssigned=val;
    }
    public int getCluster(){
        return cluster;
    }
    public void setCluster(int i){
        cluster = i;
    }

    public void setCore(boolean val){
        isCore = val;
    }
    public boolean isAssigned(){
        return isAssigned;
    }
    public boolean isCore(){
        return isCore;
    }

    public double getX() {
        return X;
    }
    public void setX(float x) {
        X = x;
    }
    public double getY() {
        return  Y;
    }
    public void setY(float y) {
        Y = y;
    }

    public String toString()
    {
        return "("+this.X+","+this.Y+")";
    }
}