package com.cpen321.circuitsolver.opencv;

import android.graphics.Bitmap;
import android.util.Log;

import com.cpen321.circuitsolver.model.CircuitElmFactory;
import com.cpen321.circuitsolver.model.SimplePoint;
import com.cpen321.circuitsolver.model.components.CircuitElm;
import com.cpen321.circuitsolver.service.CircuitDefParser;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.cpen321.circuitsolver.util.Constants.imageWH;
import static com.cpen321.circuitsolver.util.Constants.maxLinesToBeChunk;
import static com.cpen321.circuitsolver.util.Constants.minPoints;
import static com.cpen321.circuitsolver.util.Constants.radius;
import static org.opencv.imgproc.Imgproc.COLOR_GRAY2BGR;
import static org.opencv.imgproc.Imgproc.cvtColor;

/**Main opencv class
 * Created by Simon Haefeli on 27.10.2016.
 */

public class MainOpencv {
    private List<List<Element>> wires  = new ArrayList<>();
    private List<List<Element>> separatedComponents  = new ArrayList<>();

    private int bitMapWidth;
    private int bitMapHeight;

    Mat originalMat;
    private ImageClassifier componentClassifier;

    public void setComponentClassifier(ImageClassifier classifier) {
        this.componentClassifier = classifier;
    }


    /**Method to detect the components,main method of this class
     *This method is public, so any developper can return only the processed image with this function.
     * @return the bitmap with the detected lines and a circle around the components
     */
    public Bitmap houghLines(Bitmap bMap){

        bitMapHeight = bMap.getHeight();
        bitMapWidth = bMap.getWidth();

        Mat tmp = new Mat (bMap.getWidth(), bMap.getHeight(), CvType.CV_8UC1);

        originalMat = tmp;

        Mat tmp2 = new Mat (bMap.getWidth(), bMap.getHeight(), CvType.CV_8UC1);
        Utils.bitmapToMat(bMap, tmp);

        Log.d("cv","Width/height : "+bitMapHeight +" , "+bitMapWidth);
        //Convert to a canny edge detector grayscale mat
        Imgproc.Canny(tmp, tmp2, 40, 200);

        Mat tmp3 = new Mat (bMap.getWidth(), bMap.getHeight(), CvType.CV_8UC1);
        Bitmap tmp2_bm_postCanny = Bitmap.createBitmap(tmp2.cols(), tmp2.rows(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(tmp2, tmp2_bm_postCanny);

        //Execute the hough transform on the canny edge detector
        Mat lines = new Mat();
        Imgproc.HoughLinesP(tmp2,lines,1,Math.PI/180,0);


        //remove chunks from hough transform and make one line from them
        List<double[]> smoothedLines = smoothLines(MatToList(lines));

        // diagonal set of points from Houghlines/Canny = "a chunk"
        // first line assumes component is only made of diagonals
        //List<PointDB> assPoints = dbscan(keepChunks(smoothedLines,maxLinesToBeChunk), tmp3, radius, minPoints);
        // second line only assumes components are made of many lines (hori, vert, or diagonal)
        List<PointDB> assPoints = dbscan(smoothedLines, tmp3, radius, minPoints);
        List<PointDB> assignedPoints = assignedPoints(assPoints);
        TuplePoints residAssigned = dbToArray(assignedPoints , smoothedLines, maxLinesToBeChunk);
        List<double[]> residualLines = residAssigned.getFirst();
        List<double[]> components = residAssigned.getSecond();
        List<double[]> residualLinesWithoutChunk= removeChunks(residualLines, maxLinesToBeChunk);

        CornerDetector cornerDetection = new CornerDetector(residualLinesWithoutChunk,assignedPoints);
        List<double[]> validCorners = new ArrayList<>(cornerDetection.process());


        //Detecting the wires from the list of corners and components
        List<Element> objectizedCompAndCorners = objectizeCompAndCorner(validCorners, components);
        List<Component> objectizedComponents = getCompFromElements(objectizedCompAndCorners);

        Corner firstCorner = null;
        if(!objectizeCorners(validCorners).isEmpty()){
            firstCorner = objectizeCorners(validCorners).get(0);
        }
        WireCalculator wireCalcul = new WireCalculator(objectizedCompAndCorners,firstCorner,residualLinesWithoutChunk);
        wires = new ArrayList<>(wireCalcul.process());

        WireProcessor wireProc = new WireProcessor(wires,objectizedComponents);
        separatedComponents = new ArrayList<>(wireProc.process());


        //######Printing stuff out on tmp3 for debugging purposes#########

        Log.d("cv","Nr of corners : "+validCorners.size());
        Log.d("cv", "Nr of components : "+components.size());

        List<CircuitElm> myelement = getCircuitElements();
        for(CircuitElm c : myelement){
           Log.d("cv",""+c);
        }
        //#####ENd print stuff

        //Create and return the final bitmap

        cvtColor(tmp2, tmp3, COLOR_GRAY2BGR);
        Bitmap bm = Bitmap.createBitmap(tmp3.cols(), tmp3.rows(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(tmp3, bm);
        tmp.release();
        tmp2.release();
        tmp3.release();
        return bm;
    }


    //############################################################################################
    //###########Functions to get the found elements in a suitable way############################
    //############################################################################################

    /**
     *
     * @return The final result of opencv, beeing passed to the next part of CircuitSolver
     * The final result is a collection of wires and components, each having a starting and an end point
     */
    public List<CircuitElm> getCircuitElements(){
        CircuitElmFactory factory= new CircuitElmFactory();
        List<CircuitElm> circuitElements = new ArrayList<>();
        for(List<Element> circElem : separatedComponents){
            if(circElem.size() == 2){
                Corner corner1 = (Corner) circElem.get(0);
                Corner corner2 = (Corner) circElem.get(1);
                SimplePoint p1 = new SimplePoint((int)corner1.getX(),(int)corner1.getY());
                SimplePoint p2 = new SimplePoint((int)corner2.getX(),(int)corner2.getY());
                CircuitElm wire;
                if(p1.isCloserToOriginThan(p2)){
                    wire = factory.makeElm(p2,p1);
                }
                else{
                    wire = factory.makeElm(p1,p2);
                }
                circuitElements.add(wire);
            }
            else if (circElem.size() == 3){
                Component elem = (Component) circElem.get(1);
                Corner corner1 = (Corner) circElem.get(0);
                Corner corner2 = (Corner) circElem.get(2);
                SimplePoint p1 = new SimplePoint((int)corner1.getX(),(int)corner1.getY());
                SimplePoint p2 = new SimplePoint((int)corner2.getX(),(int)corner2.getY());
                CircuitElm newElm;

                if(p1.isCloserToOriginThan(p2)){
                    newElm = factory.makeElm(elem.getType(), p2, p1, 10);
                }
                else{
                    newElm = factory.makeElm(elem.getType(), p1, p2, 10);
                }
                circuitElements.add(newElm);
            }
        }
        return circuitElements;
    }

    public String getCircuitText(){

        CircuitDefParser parser = new CircuitDefParser();
        String circStr = parser.elementsToTxt(getCircuitElements(), bitMapWidth, bitMapHeight);

        return circStr;
    }


    //############################################################################################
    //###########Functions related to the dbscan algorithm########################################
    //############################################################################################

    /** Transforms all the lines into points usable by the dbscan algorithm
     *
     * @param lines The lines where to perform the transformation
     * @return A list of pointDB
     */
    private List<PointDB> objectizePointsForDB(List<double[]> lines){
        List<PointDB> dbPoints = new ArrayList<>();
        for(double[] line : lines){
            double x1 = line[0];
            double y1 = line[1];
            double x2 = line[2];
            double y2 = line[3];
            dbPoints.add(new PointDB(x1,y1));
            dbPoints.add(new PointDB(x2,y2));
        }
        return dbPoints;
    }

    /**Performs the DBSCAN algorithm and colors the different points in a certain colour according to their cluster
     *
     * @param pts The points to be clustered
     * @param toDraw The matrix where to draw the result of the algorithm
     * @param radius Param1 of the dbscan algo
     * @param minPoints Param2 of the dbscan algo
     * @return The points with their cluster (that can be accessed by point.getCluster())
     */
    private List<PointDB> dbscan(List<double[]> pts, Mat toDraw, int radius, int minPoints){

        DBSCAN db=new DBSCAN();
        List<PointDB> points = db.dbscanAlgo(objectizePointsForDB(pts),radius,minPoints);
        return points;
    }


    /**Function that retains all the PointDB that have been assigned to a cluster
     *
     * @param points All the points
     * @return The points that are assigned to a cluster
     */
    private List<PointDB> assignedPoints(List<PointDB> points){
        List<PointDB> residual = new ArrayList<>();
        for(PointDB point : points){
            if(point.getCluster() != 0){
                residual.add(point);
            }
        }
        return residual;
    }

    /**From all the points Db that have been assigned, finds the centers of the cluster by doing the means
     *
     * @param assigned the assigned points to clusters
     * @return The means of these clusters
     */

    private List<double[]> findCenters(List<PointDB> assigned){
        List<PointDB> assignedCopy = new ArrayList<>(assigned);
        List<double[]> means = new ArrayList<>();
        while(!assignedCopy.isEmpty()){
            double xmean = assignedCopy.get(0).getX();
            double ymean = assignedCopy.get(0).getY();
            int nrPoints = 1;
            Set<PointDB> toThisCluster = new HashSet<>();
            int currCluster = assignedCopy.get(0).getCluster();

            toThisCluster.add(assignedCopy.get(0));
            for(int i=1; i< assignedCopy.size();i++){
                if(assignedCopy.get(i).getCluster()==currCluster){
                    nrPoints++;
                    xmean += assignedCopy.get(i).getX();
                    ymean += assignedCopy.get(i).getY();
                    toThisCluster.add(assignedCopy.get(i));
                }
            }
            double[] mean = new double[2];
            mean[0] = xmean/nrPoints;
            mean[1] = ymean/nrPoints;
            means.add(mean);
            assignedCopy.removeAll(toThisCluster);
        }
        return means;
    }


    /**Takes the clustered points from dbscan and matches the lines with his points
     * Returns all the lines that have not been assigned in the first, all the assigned in the second
     *
     * @param Assignedpoints The points that have been assigned to a cluster
     * @param originalLines The smoothed lines found from the hough transform
     * @param maxTobeChunk The integer that defines the lmay length of a line to be considered as a chunk
     * @return A tuple of List<double[]>
     *     First variable : A list of lines (double[] with length 4) representing the lines that haven't been assigned to a cluster
     *     Second variable : A list of points representing the coordinates of the found components
     */

    private TuplePoints dbToArray(List<PointDB> Assignedpoints, List<double[]> originalLines, int maxTobeChunk){
        List<double[]> filteredLines = new ArrayList<>();
        for(int i =0; i<originalLines.size();i++){
            double[] line = originalLines.get(i);
            boolean hasBeenAssigned = false;
            for(PointDB point : Assignedpoints){
                if(((point.getX() == line[0] && point.getY() == line[1]) || (point.getX() == line[2] || point.getY() == line[3])) && lineIsChunk(line,maxTobeChunk)){
                    hasBeenAssigned = true;

                    break;


                }
            }

            if(!hasBeenAssigned){
                filteredLines.add(line);
            }
        }
        List<double[]> means = findCenters(Assignedpoints);
        return new TuplePoints(filteredLines,means);
    }



    //############################################################################################
    //###########Objectize components with tensorflow and corners#################################
    //############################################################################################

    /**Objectizes component and corners
     *
     * @param corners A list of corners, each corner beeing represented by an array of length 2, X, Y
     * @param components  A list of components, each component beeing represented by an array of length 2, X, Y
     * @return list of instances of Element
     */
    private List<Element> objectizeCompAndCorner(List<double[]> corners, List<double[]> components){
        List<Corner> cornerObjects = objectizeCorners(corners);
        List<Component> componentObjects = objectizeComponents(components);
        List<Element> everything = new ArrayList<>();
        everything.addAll(cornerObjects);
        everything.addAll(componentObjects);
        return everything;
    }


    /** A temporary function to pass from arrays to object for corners
     *
     * @param corners the corners to make objects from
     * @return A list of corner objects
     */
    private List<Corner> objectizeCorners (List<double[]> corners){
        List<Corner> cornerObjects = new ArrayList<>();
        for(double[] corner : corners){
            Set<Character> dir = new HashSet<>();
            Corner c = new Corner(corner[0], corner[1]);
            c.setNewDirection('w');
            c.setNewDirection('e');
            c.setNewDirection('n');
            c.setNewDirection('s');
            cornerObjects.add(c);
        }
        return cornerObjects;
    }

    /** A temporary function to pass from arrays to object for components
     *
     * @param components the corners to make objects from
     * @return A list of corner objects
     */
    //In this part add the tenserflow
    private List<Component> objectizeComponents (List<double[]> components){

        List<Component> componentObjects = new ArrayList<>();
        List<Bitmap> componentSnips = this.getSubImagesForTensorflow(components);
        for(double[] component : components){
            int index = components.indexOf(component);
            componentObjects.add(this.componentClassifier.infoToComponent(
                    componentSnips.get(index),
                    component
            ));
        }
        return componentObjects;
    }

    /**All the sub images of the detected components
     *
     * @param components The coordinates of the detected components
     * @return a list of sub-bitmap containing one component each
     */
    private List<Bitmap> getSubImagesForTensorflow(List<double []> components){
        List<Bitmap> subimages = new ArrayList<>();
        for(double[] component : components){
            System.out.println(component[0]+" , "+component[1]);
            Mat submat = this.getSubMat(component, imageWH);
            Bitmap b = Bitmap.createBitmap(submat.cols(), submat.rows(),Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(submat,b);
            subimages.add(b);
            submat.release();
        }
        return subimages;
    }

    /**Returns the opencv sub-Mat around a given coordinate
     *
     * @param component the coordinates of all the components
     * @param frameWidth The frame witdth we want around a component
     * @return
     */

    private Mat getSubMat( double[] component, int frameWidth) {
        int top = (int) (component[1] - frameWidth);
        int bottom = (int) (component[1] + frameWidth);
        int left = (int) (component[0] - frameWidth);
        int right = (int) (component[0] + frameWidth);

        if (top < 0) top = 0;
        if (left < 0) left = 0;

        if (bottom > originalMat.rows()) bottom = originalMat.rows();
        if (right > originalMat.cols()) right = originalMat.cols();

        return originalMat.submat(top, bottom, left, right);
    }

    //############################################################################################
    //###########SOME PROCESSING AND UTILITARY FUNCTIONS##########################################
    //############################################################################################

    /** Removes the chunks from a collection of lines
     *
     * @param lines the collection of lines to be filtered
     * @param minLineLength the minimum length a line should have
     * @return a collection of lines without chunks
     */

    private List<double[]> removeChunks(List<double[]> lines, int minLineLength){
        List<double[]> realLine = new ArrayList<>();
        for(double[] line : lines){
            if(!lineIsChunk(line,minLineLength)){
                realLine.add(line);
            }
        }
        return realLine;
    }

    /**Test is a line is chunk, i.e has length under a treshold
     *
     * @param line the line to consider
     * @param maxToBeChunk the maximum length of a line to be considered as a chunk
     * @return if the line is a chunk or not
     */

    private boolean lineIsChunk(double[] line, int maxToBeChunk){
        double x1 = line[0];
        double y1 = line[1];
        double x2 = line[2];
        double y2 = line[3];

        return Math.abs(x1-x2)<=maxToBeChunk && Math.abs(y1-y2)<=maxToBeChunk;
    }

    /**
     *
     * @param lines Mat of lines
     * @return a list of these lines
     */
    private List<double[]> MatToList(Mat lines){
        List<double[]> lineOneRound = new ArrayList<>();
        for (int x = 0; x < lines.rows(); x++) {
            double[] vec1 = lines.get(x, 0);
            lineOneRound.add(vec1);
        }
        return lineOneRound;
    }

    /**Makes from a lot of horizontal chunks one nice line
     *
     * @param lines found from the original hough transform
     * @return the lines, smoothed
     */
    private List<double[]> smoothLines(List<double[]> lines){
        //minimum pixel number that an horizontal line should have
        int lengthOfALine = 20;
        List<double[]> lineOneRound = new ArrayList<>(lines);

        List<double[]> lineTwoRound = new ArrayList<>();


        for(int lineDist=1; lineDist<=lengthOfALine ; lineDist++) {
            //Put all the lines going from left to right (Xstart < Xend)

            List<double[]> lineFromLeftToRight = new ArrayList<>();

            for (int x = 0; x < lineOneRound.size(); x++) {
                double[] vec1 = lineOneRound.get(x);
                double x11 = vec1[0];
                double y11 = vec1[1];
                double x21 = vec1[2];
                double y21 = vec1[3];

                if (x11 > x21) {
                    double[] inversedLine = new double[4];
                    inversedLine[0] = x11;
                    inversedLine[1] = y11;
                    inversedLine[2] = x21;
                    inversedLine[3] = y21;
                    lineFromLeftToRight.add(inversedLine);
                } else {
                    lineFromLeftToRight.add(vec1);
                }

                //Sort by yStart and then byXstart
                Collections.sort(lineFromLeftToRight, new LinesComparatorYX());
            }

            //get the lines 2 by 2, see if they are adjacent, and if so make one line from the two


            for (int x = 0; x < lineFromLeftToRight.size() - 1; x +=2 ) {


                double[] vec1 = lineFromLeftToRight.get(x);
                double x11 = vec1[0];
                double y11 = vec1[1];
                double x21 = vec1[2];
                double y21 =
                        vec1[3];

                double[] vec2 = lineFromLeftToRight.get(x + 1);
                double x12 = vec2[0];
                double y12 = vec2[1];
                double x22 = vec2[2];
                double y22 = vec2[3];


                if ((y11 == y22 && y12 == y21 && y11 == y21 ) && (x21 + 1 == x12)) {

                    double[] newVec = new double[4];
                    newVec[0] = x11;
                    newVec[1] = y11;
                    newVec[2] = x22;
                    newVec[3] = y12;
                    lineTwoRound.add(newVec);
                } else {

                    lineTwoRound.add(vec1);
                    lineTwoRound.add(vec2);
                }

            }
            if(lineFromLeftToRight.size() % 2 != 0){
                double[] myVec = lineFromLeftToRight.get(lineFromLeftToRight.size() - 1);
                double[] newVec = new double[4];
                newVec[0] = myVec[0];
                newVec[1] = myVec[1];
                newVec[2] = myVec[2];
                newVec[3] = myVec[3];

                lineTwoRound.add(newVec);
            }

            lineOneRound = new ArrayList<>(lineTwoRound);
            if(lineDist != lengthOfALine ) {
                lineTwoRound.clear();
            }
            //Repeat
        }

        return lineTwoRound;
    }

    /**Get all the components from a list of elements
     *
     * @param elements all the elements
     * @return The components from elements
     */
    private List<Component> getCompFromElements(List<Element> elements){
        List<Component> components = new ArrayList<>();
        for(Element e: elements){
            if(e instanceof Component){
                components.add((Component) e);
            }
        }
        return components;
    }
}

