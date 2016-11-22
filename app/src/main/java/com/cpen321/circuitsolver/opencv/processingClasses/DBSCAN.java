package com.cpen321.circuitsolver.opencv.processingClasses;

/**Class to perform the DB scan algorithm
 *Created by Simon Haefeli entirely 3.11.2016
 */

import com.cpen321.circuitsolver.opencv.opencvModel.PointDB;

import java.util.ArrayList;
import java.util.List;

public class DBSCAN {
    List<PointDB> allPoints = new ArrayList<>();

    /**Performs the dbscan algorithm
     *
     * @param points the points to be clustered
     * @param radius the radius of two points to be in the same cluster
     * @param minPoints The min number of points a cluster must have to be a cluster
     * @return The points with their assignment (see PointDB class)
     */
    public List<PointDB> dbscanAlgo(List<PointDB> points, int radius, int minPoints){
        System.gc();
        //Initialize distance matrix
        allPoints = new ArrayList<>(points);
        int size = allPoints.size();
        float [][] distancesMatrix = new float[size][size];
        for(int i=0;i<distancesMatrix.length;i++){
            System.out.print("\n");
            for(int j=0;j<distancesMatrix.length;j++){
                if(i==j){
                    distancesMatrix[i][j] = 0;
                }
                else{
                    distancesMatrix[i][j] = (float)Math.sqrt(Math.pow(allPoints.get(i).getX()-allPoints.get(j).getX(),2)+Math.pow(allPoints.get(i).getY()-allPoints.get(j).getY(),2));
                }

            }
        }

        //Go through all points
        for(int i=0;i<allPoints.size();i++){
            int nrInCluster = 0;
            List<PointDB> probablyInSameCluster = new ArrayList<>();
            if(!allPoints.get(i).isAssigned()){
                for(int j=0;j<allPoints.size();j++){
                    if(i!=j && distancesMatrix[i][j]<=radius){
                        nrInCluster++;
                        probablyInSameCluster.add(allPoints.get(j));
                    }
                }
                if(nrInCluster >= minPoints-1){

                    allPoints.get(i).setCore(true);
                    allPoints.get(i).setCluster(PointDB.nrClusters);
                    allPoints.get(i).setAssigned(true);
                    PointDB.nrClusters++;
                    expand(allPoints.get(i),probablyInSameCluster,radius, minPoints, distancesMatrix);
                }
            }
        }

        return allPoints;
    }

    /** Expands a cluster
     *
     * @param corePt The core point from which we expand the cluster
     * @param toConsider All the points that have to be reconsidered to be in the same cluster
     * @param radius Param of dbscan
     * @param minPoints Param of dbscan
     * @param distancesMatrix The distances matrix indicating the distance between all couple of points
     */
    private void expand(PointDB corePt, List<PointDB> toConsider, int radius, int minPoints, float[][] distancesMatrix){
        for(PointDB point : toConsider) {
            if (!point.isAssigned()){
                point.setCluster(corePt.getCluster());
                point.setAssigned(true);
                List<PointDB> neighbourghs = neighbourghs(point, radius, distancesMatrix);
                if (neighbourghs.size() >= minPoints - 1) {
                    point.setCore(true);
                }
                if (point.isCore()) {
                    expand(point, neighbourghs, radius, minPoints, distancesMatrix);
                }
            }
        }
    }

    /**Gets all the neighbourgh of a given point
     *
     * @param reference The point from which we want the neighbourgh
     * @param radius The min distance to be considered as neighbourgh
     * @param distancesMatrix The distances matrix indicating the distance between all couple of points
     * @return
     */
    private List<PointDB> neighbourghs(PointDB reference, int radius, float[][] distancesMatrix ){
        int index = allPoints.indexOf(reference);
        List<PointDB> neighbourghs = new ArrayList<>();
        for(int i=0;i<allPoints.size();i++){
            if(i!= index && distancesMatrix[index][i]<=radius){
                neighbourghs.add(allPoints.get(i));

            }
        }
        return neighbourghs;
    }
}


