package com.cpen321.circuitsolver.opencv.processingClasses;

import com.cpen321.circuitsolver.opencv.comparators.LinesComparatorYX;
import com.cpen321.circuitsolver.opencv.opencvModel.PointDB;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.cpen321.circuitsolver.util.Constants.*;
/**Class that detects corners
 * Created by Simon on 21.11.2016.
 */

public class CornerDetection {
    List<PointDB> pointsAfterDBScan;
    List<float[]> linesToDetectCorners;
    public CornerDetection(List<float[]> residualLines, List<PointDB> assignedPoints){
        linesToDetectCorners = residualLines;
        pointsAfterDBScan = assignedPoints;
    }

    public List<float[]> process(){
        List<float[]> verticalLines = verticalLines(linesToDetectCorners);
        List<float[]> horizontalLines = horizontalLines(linesToDetectCorners);
        List<float[]> corners = findCorners(verticalLines,horizontalLines,thresholdXY);
        List<float[]> singleCorners = singleCorners(corners,twoCornersTooNear);
        List<float[]> validCorners = goodCorners(pointsAfterDBScan,singleCorners,tooNearFromComponent);
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

    private List<float[]> verticalLines(List<float[]> lines){
        List<float[]> verticalLines = new ArrayList<>();
        for(float[] line : lines){
            float x1 = line[0];
            float y1 = line[1];
            float x2 = line[2];
            float y2 = line[3];

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

    private List<float[]> horizontalLines(List<float[]> lines){
        List<float[]> horizontalLines = new ArrayList<>();
        for(float[] line : lines){
            float x1 = line[0];
            float y1 = line[1];
            float x2 = line[2];
            float y2 = line[3];

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
    private List<float[]> findCorners(List<float[]> verticals, List<float[]> horizontals, int searchRadius){

        List<float[]> corners = new ArrayList<>();
        Collections.sort(verticals,new LinesComparatorYX());
        Collections.sort(horizontals,new LinesComparatorYX());
        for(float[] verticalLine : verticals){
            float x11 = verticalLine[0];
            float y11 = verticalLine[1];
            float x21 = verticalLine[2];
            float y21 = verticalLine[3];
            for(float[] horizontalLine: horizontals){
                float x12 = horizontalLine[0];
                float y12 = horizontalLine[1];
                float x22 = horizontalLine[2];
                float y22 = horizontalLine[3];

                if(Math.abs(y11-y12)<= searchRadius){
                    float[] point = new float[3];
                    point[0]=x11;
                    point[1]=y12;
                    point[2]=10;
                    corners.add(point);
                }
                else if(Math.abs(y21-y12) <=searchRadius){
                    float[] point = new float[3];
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

    private List<float[]> singleCorners(List<float[]> corners, int minDistance){
        List<float[]> singleCorners = new ArrayList<>();
        for(int i = 0; i< corners.size();i++){
            boolean hasEquivalent = false;
            float[] corner1 = corners.get(i);
            float x1 = corner1[0];
            float y1 = corner1[1];
            for(int j = i+1; j<corners.size();j++){
                if(i!= j){

                    float[] corner2 = corners.get(j);

                    float x2 = corner2[0];
                    float y2 = corner2[1];
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
    private List<float[]> goodCorners(List<PointDB> assignedPoints, List<float[]> corners, int minDistance){
        List<float[]> acceptableCorners = new ArrayList<>();
        for(float[] corner: corners){
            float x = corner[0];
            float y = corner[1];
            //find closest component point
            float minDis = Float.POSITIVE_INFINITY;
            for(PointDB assPoint : assignedPoints){
                if(Math.sqrt(Math.pow(assPoint.getX()-x,2)+Math.pow(assPoint.getY()-y,2))<minDis){
                    minDis = (float)Math.sqrt(Math.pow(assPoint.getX()-x,2)+Math.pow(assPoint.getY()-y,2));
                }
            }
            if(minDis>minDistance){
                acceptableCorners.add(corner);
            }
        }
        return acceptableCorners;
    }
}
