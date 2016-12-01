package com.cpen321.circuitsolver.opencv;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.cpen321.circuitsolver.util.Constants.tooNearFromComponent;
import static com.cpen321.circuitsolver.util.Constants.twoCornersTooNear;

/**Classes that detects the corners from lines.
 * Created by Simon on 01.12.2016.
 */

public class CornerDetector {
    private List<double[]> residualLinesWithoutChunk;
    private List<PointDB> assignedPoints;

    public CornerDetector(List<double[]> residualLinesWithoutChunk, List<PointDB> assignedPoints){
        this.residualLinesWithoutChunk = new ArrayList<>(residualLinesWithoutChunk);
        this.assignedPoints = new ArrayList<>(assignedPoints);
    }

    public List<double[]> process(){

        List<double[]> verticalLines = verticalLines(residualLinesWithoutChunk);
        List<double[]> horizontalLines = horizontalLines(residualLinesWithoutChunk);
        List<double[]> corners = findCorners(verticalLines,horizontalLines,10);

        List<double[]> singleCorners = singleCorners(corners,twoCornersTooNear);
        List<double[]> validCorners = goodCorners(assignedPoints,singleCorners,tooNearFromComponent);

        //If removing too near components removes everything, just keep the corners before the filtering
        if(validCorners.size() == 0){
            validCorners = new ArrayList<>(singleCorners);
        }
        return validCorners;
    }

    /**Returns the vertical lines from a collection of lines
     *
     * @param lines the collection of lines
     * @return the vertical lines from it
     */

    private List<double[]> verticalLines(List<double[]> lines){
        List<double[]> verticalLines = new ArrayList<>();
        for(double[] line : lines){
            double x1 = line[0];
            double y1 = line[1];
            double x2 = line[2];
            double y2 = line[3];

            if(x1 == x2){
                verticalLines.add(line);
            }
        }
        return verticalLines;
    }

    /**Returns the horizontal lines from a collection of lines
     *
     * @param lines the collection of lines
     * @return the horizontal lines from it
     */

    private List<double[]> horizontalLines(List<double[]> lines){
        List<double[]> horizontalLines = new ArrayList<>();
        for(double[] line : lines){
            double x1 = line[0];
            double y1 = line[1];
            double x2 = line[2];
            double y2 = line[3];

            if(y1 == y2){
                horizontalLines.add(line);
            }
        }
        return horizontalLines;
    }

    /**Finds the corners from a set of horizontal and vertical lines
     * If two ends of respectively an horizontal and a vertical lines is close enough (defined by searchRadius),
     * calculate the intersection between the two lines
     *
     * @param verticals The list of vertical lines
     * @param horizontals The list of horizontal lines
     * @param searchRadius The max distance between two line endings to be considered as a corner
     * @return a list with the found corners
     */
    private List<double[]> findCorners(List<double[]> verticals, List<double[]> horizontals, int searchRadius){

        List<double[]> corners = new ArrayList<>();
        Collections.sort(verticals,new LinesComparatorYX());
        Collections.sort(horizontals,new LinesComparatorYX());
        for(double[] verticalLine : verticals){
            double x11 = verticalLine[0];
            double y11 = verticalLine[1];
            double x21 = verticalLine[2];
            double y21 = verticalLine[3];
            for(double[] horizontalLine: horizontals){
                double x12 = horizontalLine[0];
                double y12 = horizontalLine[1];
                double x22 = horizontalLine[2];
                double y22 = horizontalLine[3];

                if(Math.abs(y11-y12)<= searchRadius){
                    double[] point = new double[3];
                    point[0]=x11;
                    point[1]=y12;
                    point[2]=10;
                    corners.add(point);
                }
                else if(Math.abs(y21-y12) <=searchRadius){
                    double[] point = new double[3];
                    point[0]=x11;
                    point[1]=y22;
                    point[2]=10;
                    corners.add(point);
                }

            }
        }
        return corners;
    }

    /**Removes the superfluous corners that could have been detected by the corner detection
     * (Sometimes when calculating the corners, if the search radius is not perfectly defined
     * for every single corners, it will detect multiple corners at the same space)
     *
     * @param corners The found corners
     * @param minDistance The min distance between two corners
     * @return A list of single corners
     */

    private List<double[]> singleCorners(List<double[]> corners, int minDistance){
        List<double[]> singleCorners = new ArrayList<>();
        for(int i = 0; i< corners.size();i++){
            boolean hasEquivalent = false;
            double[] corner1 = corners.get(i);
            double x1 = corner1[0];
            double y1 = corner1[1];
            for(int j = i+1; j<corners.size();j++){
                if(i!= j){

                    double[] corner2 = corners.get(j);

                    double x2 = corner2[0];
                    double y2 = corner2[1];
                    if(Math.sqrt(Math.pow(x1-x2,2)+Math.pow(y1-y2,2))<= minDistance){
                        hasEquivalent = true;
                        break;
                    }
                }
            }
            if(!hasEquivalent){
                singleCorners.add(corner1);
            }
        }
        return singleCorners;
    }

    /**returns corners that are not too near from components
     *
     * @param assignedPoints, the list of points that have been assigned to a cluster
     * @param corners The foubd corners
     * @return The corners that are not too near from the components
     */
    private List<double[]> goodCorners(List<PointDB> assignedPoints, List<double[]> corners, int minDistance){
        List<double[]> acceptableCorners = new ArrayList<>();
        for(double[] corner: corners){
            double x = corner[0];
            double y = corner[1];
            //find closest component point
            double minDis = Double.POSITIVE_INFINITY;
            for(PointDB assPoint : assignedPoints){
                if(Math.sqrt(Math.pow(assPoint.getX()-x,2)+Math.pow(assPoint.getY()-y,2))<minDis){
                    minDis = Math.sqrt(Math.pow(assPoint.getX()-x,2)+Math.pow(assPoint.getY()-y,2));
                }
            }
            if(minDis>minDistance){
                acceptableCorners.add(corner);
            }
        }
        return acceptableCorners;
    }


}
