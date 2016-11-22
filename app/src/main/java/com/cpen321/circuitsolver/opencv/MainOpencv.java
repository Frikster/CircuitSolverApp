package com.cpen321.circuitsolver.opencv;

import android.graphics.Bitmap;

import com.cpen321.circuitsolver.model.CircuitElmFactory;
import com.cpen321.circuitsolver.model.SimplePoint;
import com.cpen321.circuitsolver.model.components.CircuitElm;
import com.cpen321.circuitsolver.opencv.comparators.ComponentComparatorX;
import com.cpen321.circuitsolver.opencv.comparators.ComponentComparatorY;
import com.cpen321.circuitsolver.opencv.comparators.LinesComparatorYX;
import com.cpen321.circuitsolver.opencv.opencvModel.Component;
import com.cpen321.circuitsolver.opencv.opencvModel.Corner;
import com.cpen321.circuitsolver.opencv.opencvModel.Element;
import com.cpen321.circuitsolver.opencv.opencvModel.PointDB;
import com.cpen321.circuitsolver.opencv.opencvModel.TuplePoints;
import com.cpen321.circuitsolver.opencv.processingClasses.CornerDetection;
import com.cpen321.circuitsolver.opencv.processingClasses.DBSCAN;
import com.cpen321.circuitsolver.opencv.processingClasses.WireProcessing;
import com.cpen321.circuitsolver.service.CircuitDefParser;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.cpen321.circuitsolver.util.Constants.*;
import static org.opencv.imgproc.Imgproc.COLOR_GRAY2BGR;
import static org.opencv.imgproc.Imgproc.cvtColor;


/**Main opencv class
 * Created by Simon Haefeli on 27.10.2016.
 */

public class MainOpencv {

    //########MODIFIED ###############
    //MainOpenCV
    //##############TODO :#################
    //Integration with tenserflow
    //##########################################


    private List<List<Element>> wires  = new ArrayList<>();
    private List<List<Element>> separatedComponents  = new ArrayList<>();

    private int bitMapWidth;
    private int bitMapHeight;


    /**Temporary method for debugging purposes**
     *
     * @param bMap The input image
     * @param test true if this a mock test
     * @return the result
     */
    public Bitmap houghLines(Bitmap bMap, boolean test){
        if(!test)
            return houghLines(bMap);
        return bMap;
    }

    /**Method to detect the components,main method of this class
     *
     * @return the bitmap with the detected lines and a circle around the components
     */
    public Bitmap houghLines(Bitmap bMap){

        bitMapHeight = bMap.getHeight();
        bitMapWidth = bMap.getWidth();

        Mat tmp = new Mat (bMap.getWidth(), bMap.getHeight(), CvType.CV_8UC1);
        Mat tmp2 = new Mat (bMap.getWidth(), bMap.getHeight(), CvType.CV_8UC1);
        Utils.bitmapToMat(bMap, tmp);

        System.out.println("Width/height : "+bitMapHeight +" , "+bitMapWidth);
        //Convert to a canny edge detector grayscale mat
        Imgproc.Canny(tmp, tmp2, 40, 200);

        Mat tmp3 = new Mat (bMap.getWidth(), bMap.getHeight(), CvType.CV_8UC1);
        Bitmap tmp2_bm_postCanny = Bitmap.createBitmap(tmp2.cols(), tmp2.rows(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(tmp2, tmp2_bm_postCanny);

        //Execute the hough transform on the canny edge detector
        Mat lines = new Mat();
        Imgproc.HoughLinesP(tmp2,lines,1,Math.PI/180,0);

        //To be able to draw in color on the mat tmp3
        cvtColor(tmp2, tmp3, COLOR_GRAY2BGR);


        //remove chunks from hough transform and make one line from them
        List<float[]> smoothedLines = smoothLines(MatToList(lines));

        // diagonal set of points from Houghlines/Canny = "a chunk"
        // first line assumes component is only made of diagonals
        //List<PointDB> assPoints = dbscan(keepChunks(smoothedLines,maxLinesToBeChunk), tmp3, radius, minPoints);
        // second line only assumes components are made of many lines (hori, vert, or diagonal)

        //Executes a dbscan algorithm on the lines
        List<PointDB> assPoints = dbscan(smoothedLines, tmp3, radius, minPoints);
        List<PointDB> assignedPoints = assignedPoints(assPoints);
        TuplePoints residAssigned = dbToArray(assignedPoints , smoothedLines, maxLinesToBeChunk);
        List<float[]> residualLines = residAssigned.getFirst();
        List<float[]> components = residAssigned.getSecond();

        List<float[]> residualLinesWithoutChunk= removeChunks(residualLines, maxLinesToBeChunk);

        CornerDetection cornerDet = new CornerDetection(residualLines,assignedPoints);
        List<float[]> validCorners = cornerDet.process();


        //Detecting the wires from the list of corners and components
        List<Element> objectizedCompAndCorners = objectizeCompAndCorner(validCorners, components);
        List<Component> objectizedComponents = getCompFromElements(objectizedCompAndCorners);
        Corner firstCorner = null;
        if(!objectizeCorners(validCorners).isEmpty()){
            firstCorner = objectizeCorners(validCorners).get(0);
        }
        detectWires(objectizedCompAndCorners,firstCorner, thresholdXY,residualLinesWithoutChunk);

        //Processing the found wires
        WireProcessing wireProc = new WireProcessing(wires,objectizedComponents);
        separatedComponents = wireProc.processWires();


        //######Printing stuff out on tmp3 for debugging purposes#########
        // Print out the bitmap (for debugging)

        List<CircuitElm> myelement = getCircuitElements();
        for(CircuitElm c : myelement){
            System.out.println(c);
        }

        drawCircles(tmp3,validCorners, new Scalar(0,255,0), 10*4);
        drawCircles(tmp3,components, new Scalar(255,0,0), 10*4);

        System.out.println("Nr of corners : "+validCorners.size());
        System.out.println("Nr of components : "+components.size());

        int x=0;
        for (List<Element> wire : separatedComponents)
        {
            Point start = new Point();
            Point end = new Point();
            if(wire.size() == 2){
                start = new Point(wire.get(0).getX(),wire.get(0).getY());
                end = new Point(wire.get(1).getX(),wire.get(1).getY());
            }
            else if(wire.size() == 3){
                start = new Point(wire.get(0).getX(),wire.get(0).getY());
                end = new Point(wire.get(2).getX(),wire.get(2).getY());
            }
            if(x%3 == 0){
                Imgproc.line(tmp3, start, end, new Scalar(255,0,0), 1);
            }
            else if(x% 3 == 1){
                Imgproc.line(tmp3, start, end, new Scalar(0,255,0), 1);
            }
            else{
                Imgproc.line(tmp3, start, end, new Scalar(0,0,255), 1);
            }
            x++;
        }

        //######End debugging printings##########


        //Create and return the final bitmap
        Bitmap bm = Bitmap.createBitmap(tmp3.cols(), tmp3.rows(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(tmp3, bm);
        tmp.release();
        tmp2.release();
        tmp3.release();
        return bm;
    }

    /**
     *
     * @param corners A list of corners, each corner beeing represented by an array of length 2, X, Y
     * @param components  A list of components, each component beeing represented by an array of length 2, X, Y
     * @return list of instances of Element
     */
    private List<Element> objectizeCompAndCorner(List<float[]> corners, List<float[]> components){
        List<Corner> cornerObjects = objectizeCorners(corners);
        List<Component> componentObjects = objectizeComponents(components);
        List<Element> everything = new ArrayList<>();
        everything.addAll(cornerObjects);
        everything.addAll(componentObjects);
        return everything;
    }


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


    public String getCircuitText(boolean test){

        if(!test)
            return getCircuitText();

        String circStr = "$ 10 10\n" +
                "r 5 3 8 6 10.0\n" +
                "r 5 3 2 6 10.0\n" +
                "v 2 6 8 6 10.0 \n";

        return circStr;
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


    /**Takes the clustered points from dbscan and matches the lines with his points
     * Returns all the lines that have not been assigned in the first, all the assigned in the second
     *
     * @param Assignedpoints The points that have been assigned to a cluster
     * @param originalLines The smoothed lines found from the hough transform
     * @param maxTobeChunk The integer that defines the lmay length of a line to be considered as a chunk
     * @return A tuple of List<float[]>
     *     First variable : A list of lines (float[] with length 4) representing the lines that haven't been assigned to a cluster
     *     Second variable : A list of points representing the coordinates of the found components
     */

    private TuplePoints dbToArray(List<PointDB> Assignedpoints, List<float[]> originalLines, int maxTobeChunk){
        List<float[]> filteredLines = new ArrayList<>();
        for(int i =0; i<originalLines.size();i++){
            float[] line = originalLines.get(i);
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
        List<float[]> means = findCenters(Assignedpoints);
        return new TuplePoints(filteredLines,means);
    }

    /**From all the points Db that have been assigned, finds the centers of the cluster by doing the means
     *
     * @param assigned the assigned points to clusters
     * @return The means of these clusters
     */

    private List<float[]> findCenters(List<PointDB> assigned){
        List<PointDB> assignedCopy = new ArrayList<>(assigned);
        List<float[]> means = new ArrayList<>();
        while(!assignedCopy.isEmpty()){
            float xmean = assignedCopy.get(0).getX();
            float ymean = assignedCopy.get(0).getY();
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
            float[] mean = new float[2];
            mean[0] = xmean/nrPoints;
            mean[1] = ymean/nrPoints;
            means.add(mean);
            assignedCopy.removeAll(toThisCluster);
        }
        return means;
    }

    /**Performs the DBSCAN algorithm and colors the different points in a certain colour according to their cluster
     *
     * @param pts The points to be clustered
     * @param toDraw The matrix where to draw the result of the algorithm
     * @param radius Param1 of the dbscan algo
     * @param minPoints Param2 of the dbscan algo
     * @return The points with their cluster (that can be accessed by point.getCluster())
     */
    private List<PointDB> dbscan(List<float[]> pts, Mat toDraw, int radius, int minPoints){

        DBSCAN db=new DBSCAN();
        List<PointDB> points = db.dbscanAlgo(objectizePointsForDB(pts),radius,minPoints);

        for(PointDB point : points){
            if(point.getCluster() == 0){
                Imgproc.circle(toDraw,new Point(point.getX(),point.getY()),2,new Scalar(0, 0 , 0), 1,8,0);
            }
            else if(point.getCluster()%3 == 0){
                Imgproc.circle(toDraw,new Point(point.getX(),point.getY()),2,new Scalar(255, 0 , 0), 1,8,0);
            }
            else if(point.getCluster()%3 == 1){
                Imgproc.circle(toDraw,new Point(point.getX(),point.getY()),2,new Scalar(0, 255 , 0), 1,8,0);
            }
            else if(point.getCluster()%3 == 2){
                Imgproc.circle(toDraw,new Point(point.getX(),point.getY()),2,new Scalar(0, 0 , 255), 1,8,0);
            }
        }
        return points;
    }


    /** Transforms all the lines into points usable by the dbscan algorithm
     *
     * @param lines The lines where to perform the transformation
     * @return A list of pointDB
     */
    private List<PointDB> objectizePointsForDB(List<float[]> lines){
        List<PointDB> dbPoints = new ArrayList<>();
        for(float[] line : lines){
            float x1 = line[0];
            float y1 = line[1];
            float x2 = line[2];
            float y2 = line[3];
            dbPoints.add(new PointDB(x1,y1));
            dbPoints.add(new PointDB(x2,y2));
        }
        return dbPoints;
    }

    private List<Component> getCompFromElements(List<Element> elements){
        List<Component> components = new ArrayList<>();
        for(Element e: elements){
            if(e instanceof Component){
                components.add((Component) e);
            }
        }
        return components;
    }



    /**Recursive function to detect all the wires
     * This is the main function to detect wires from components and Corners.
     * It always starts with a corner, and searches in an horizontal and vertical way to find other components and/or corners
     *
     * @param elements The elements to detect from
     * @param currCorner The corner from which we look for wires
     * Changes a global field of the class
     */
    private void detectWires(List<Element> elements, Corner currCorner, int thresholdXY, List<float[]> residualLines){
        //Threshold : so that two points have sameX or sameY

        if(currCorner != null && !currCorner.exploredDirections.isEmpty()){
            //get same horizontal and vertical components
            List<Element> sameY = getSameYElements(elements,currCorner,thresholdXY);
            List<Element> sameX = getSameXElements(elements,currCorner,thresholdXY);
            Collections.sort(sameX,new ComponentComparatorY());
            Collections.sort(sameY,new ComponentComparatorX());


            //find index of currCorner
            int i=0;
            for(int e = 0; e<sameX.size();e++){
                Element elem = sameX.get(e);
                if(elem.getX() == currCorner.getX() && elem.getY() == currCorner.getY()){
                    i = e;
                    break;
                }
            }

            int j=0;
            for(int e = 0; e<sameY.size();e++){
                Element elem = sameY.get(e);

                if(elem.getX() == currCorner.getX() && elem.getY() == currCorner.getY()){
                    j = e;
                    break;
                }
            }


            if(currCorner.exploredDirections.contains('w')){
                List<Element> aWire = new ArrayList<>();
                aWire.add(currCorner);
                currCorner.exploredDirections.remove('w');

                for(int f = j-1; f>=0;f--){

                    Element elem = sameY.get(f);
                    if(existsALineBetweenTwoPoints(currCorner,elem,residualLines,'y',thresholdXY)) {
                        aWire.add(elem);
                        if (elem instanceof Component) {
                            elements.remove(elem);
                        }
                        if (elem instanceof Corner) {
                            ((Corner) elem).exploredDirections.remove('e');

                            detectWires(elements, (Corner) elem, thresholdXY, residualLines);
                            break;
                        }
                    }
                }
                if(aWire.size() > 1){
                    wires.add(aWire);
                }
            }

            if(currCorner.exploredDirections.contains('e')){

                List<Element> aWire = new ArrayList<>();
                aWire.add(currCorner);
                currCorner.exploredDirections.remove('e');


                for(int f = j+1; f<sameY.size();f++){
                    Element elem = sameY.get(f);
                    if(existsALineBetweenTwoPoints(currCorner,elem,residualLines,'y',thresholdXY)) {
                        aWire.add(elem);
                        if (elem instanceof Component) {
                            elements.remove(elem);
                        }
                        if (elem instanceof Corner) {
                            ((Corner) elem).exploredDirections.remove('w');

                            detectWires(elements, (Corner) elem, thresholdXY, residualLines);
                            break;
                        }
                    }
                }
                if(aWire.size() > 1){

                    wires.add(aWire);
                }
            }

            if(currCorner.exploredDirections.contains('n')){

                List<Element> aWire = new ArrayList<>();
                aWire.add(currCorner);
                currCorner.exploredDirections.remove('n');

                for(int f = i-1; f>=0;f--){
                    Element elem = sameX.get(f);
                    if(existsALineBetweenTwoPoints(currCorner,elem,residualLines,'x',thresholdXY)) {
                        aWire.add(elem);
                        if (elem instanceof Component) {
                            elements.remove(elem);
                        }
                        if (elem instanceof Corner) {
                            ((Corner) elem).exploredDirections.remove('s');

                            detectWires(elements, (Corner) elem, thresholdXY, residualLines);
                            break;
                        }
                    }
                }
                if(aWire.size() > 1){

                    wires.add(aWire);
                }
            }

            if(currCorner.exploredDirections.contains('s')){

                List<Element> aWire = new ArrayList<>();
                aWire.add(currCorner);
                currCorner.exploredDirections.remove('s');

                for(int f = i+1; f<sameX.size();f++){
                    Element elem = sameX.get(f);
                    if(existsALineBetweenTwoPoints(currCorner,elem,residualLines,'x',thresholdXY)) {
                        aWire.add(elem);
                        if (elem instanceof Component) {
                            elements.remove(elem);
                        }
                        if (elem instanceof Corner) {
                            ((Corner) elem).exploredDirections.remove('n');

                            detectWires(elements, (Corner) elem, thresholdXY, residualLines);
                            break;
                        }
                    }
                }
                if(aWire.size() > 1){

                    wires.add(aWire);
                }
            }
        }
    }




    /** A temporary function to pass from arrays to object for corners
     *
     * @param corners the corners to make objects from
     * @return A list of corner objects
     */
    private List<Corner> objectizeCorners (List<float[]> corners){
        List<Corner> cornerObjects = new ArrayList<>();
        for(float[] corner : corners){
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
    private List<Component> objectizeComponents (List<float[]> components){

        List<Component> componentObjects = new ArrayList<>();
        for(float[] component : components){
            componentObjects.add(new Component(component[0], component[1],RESISTOR));
        }
        return componentObjects;
    }

    /** Utilitary method to get all the components at the same width
     *
     * @param elements the elements do detect from
     * @param currentCorner the reference object
     * @param threshold the dmmax distance to beconsidered as same distance
     * @return List of elements correseponding
     */
    private List<Element> getSameXElements(List<Element> elements, Corner currentCorner, int threshold){
        List<Element> result = new ArrayList<>();
        for(Element element : elements){

            if (Math.abs(element.getX() - currentCorner.getX())<threshold) {
                result.add(element);
            }

        }
        return result;
    }

    /**Utilitary function for detectWires. makes sure there is a line between two vertically or horizontal Elements
     *
     * @param e1 element 1
     * @param e2 element 2
     * @param lines The list of found lines
     * @param same Character to say if we're interested in horizontal or vertically alignes elements
     * @param threshold The XY threshold to consider two elements as aligned
     * @return true if a line was found in lines between the two elements
     */
    private boolean existsALineBetweenTwoPoints(Element e1, Element e2, List<float[]> lines, char same, int threshold){
        float x1 = e1.getX();
        float y1 = e1.getY();
        float x2 = e2.getX();
        double y2 = e2.getY();
        boolean foundOne = false;
        for(float[] line : lines){
            float startX = line[0];
            float startY = line[1];
            float endX = line[2];
            float endY = line[3];
            if(same == 'x'){
                //There is a vertical
                if(startX == endX) {
                    //There is a line that has same X than the first point
                    if (Math.abs(x1 - startX) < threshold && Math.abs(endX - x1) < threshold) {
                        //There is a line that has same X than the second point
                        if (Math.abs(x2 - startX) < threshold && Math.abs(endX - x2) < threshold) {
                            //This line is between the two points in terms of y
                            if ((startY < y1 && endY < y1 && startY > y2 && endY > y2) || (startY < y2 && endY < y2 && startY > y1 && endY > y1)) {
                                foundOne = true;
                                break;
                            }
                        }
                    }
                }
            }
            if(same == 'y'){
                if(startY == endY) {
                    //There is a line that has same Y than the first point
                    if (Math.abs(y1 - startY) < threshold && Math.abs(endY - y1) < threshold) {
                        //There is a line that has same Y than the second point
                        if (Math.abs(y2 - startY) < threshold && Math.abs(endY - y2) < threshold) {
                            //This line is between the two points in terms of X
                            if ((startX < x1 && endX < x1 && startX > x2 && endX > x2) || (startX < x2 && endX < x2 && startX > x1 && endX > x1)) {
                                foundOne = true;
                                break;
                            }
                        }
                    }
                }
            }
        }
        return foundOne;

    }

    /** Utilitary method to get all the components at the same height
     *
     * @param elements the elements do detect from
     * @param currentCorner the reference object
     * @param threshold the dmmax distance to beconsidered as same distance
     * @return List of elements correseponding
     */
    private List<Element> getSameYElements(List<Element> elements, Corner currentCorner, int threshold){
        List<Element> result = new ArrayList<>();
        for(Element element : elements){

            if (Math.abs(element.getY() - currentCorner.getY())<threshold) {
                result.add(element);
            }

        }
        return result;
    }

    /** Removes the chunks from a collection of lines
     *
     * @param lines the collection of lines to be filtered
     * @param minLineLength the minimum length a line should have
     * @return a collection of lines without chunks
     */

    private List<float[]> removeChunks(List<float[]> lines, int minLineLength){
        List<float[]> realLine = new ArrayList<>();

        for(float[] line : lines){
            if(!lineIsChunk(line,minLineLength)){
                realLine.add(line);
            }
        }
        return realLine;
    }

    /**Determines if a line is chunk
     *
     * @param line the line to consider
     * @param maxToBeChunk the maximum length of a line to be considered as a chunk
     * @return if the line is a chunk or not
     */

    private boolean lineIsChunk(float[] line, int maxToBeChunk){
        float x1 = line[0];
        float y1 = line[1];
        float x2 = line[2];
        float y2 = line[3];

        return Math.abs(x1-x2)<=maxToBeChunk && Math.abs(y1-y2)<=maxToBeChunk;
    }

    /**Utilitary method for debuggin purposes
     *
     * @param dst Mat to draw the circles
     * @param circlesToDraw The list containing the circles
     * @return the mat with the drawn circles on it
     */
    private Mat drawCircles(Mat dst, List<float[]> circlesToDraw, Scalar color, int radius){
        float xi = 0;
        float yi = 0;
        int ri = 0;

        for( int i = 0; i < circlesToDraw.size(); i++ ) {
            float[] data = circlesToDraw.get(i);

            for(int j = 0 ; j < data.length ; j++){
                xi = data[0];
                yi = data[1];
                ri = radius;
            }

            Point center = new Point(Math.round(xi), Math.round(yi));
            // circle center
            Imgproc.circle(dst,center,2,new Scalar(0, 255, 0), 1,8,0);
            // circle outline
            Imgproc.circle(dst, center, ri, color, 1,8,0);
        }
        return dst;
    }
    /**
     *
     * @param lines Mat of lines
     * @return a list of these lines
     */
    private List<float[]> MatToList(Mat lines){
        List<float[]> lineOneRound = new ArrayList<>();
        for (int x = 0; x < lines.rows(); x++) {
            double[] vec1 = lines.get(x, 0);
            float[] vec = new float[vec1.length];
            for(int i=0; i<vec1.length;i++){
                vec[i] = (float)vec1[i];
            }
            lineOneRound.add(vec);
        }
        return lineOneRound;
    }

    /**Makes from a lot of horizontal chunks one nice line
     *
     * @param lines found from the original hough transform
     * @return the lines, smoothed
     */
    private List<float[]> smoothLines(List<float[]> lines){
        //minimum pixel number that an horizontal line should have
        int lengthOfALine = 20;
        List<float[]> lineOneRound = new ArrayList<>(lines);

        List<float[]> lineTwoRound = new ArrayList<>();


        for(int lineDist=1; lineDist<=lengthOfALine ; lineDist++) {
            //Put all the lines going from left to right (Xstart < Xend)

            List<float[]> lineFromLeftToRight = new ArrayList<>();

            for (int x = 0; x < lineOneRound.size(); x++) {
                float[] vec1 = lineOneRound.get(x);
                float x11 = vec1[0];
                float y11 = vec1[1];
                float x21 = vec1[2];
                float y21 = vec1[3];

                if (x11 > x21) {
                    float[] inversedLine = new float[4];
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

            //get the lines 2 by 2, see if they are adjacent, and if so make one line from the two7


            for (int x = 0; x < lineFromLeftToRight.size() - 1; x +=2 ) {


                float[] vec1 = lineFromLeftToRight.get(x);
                float x11 = vec1[0];
                float y11 = vec1[1];
                float x21 = vec1[2];
                float y21 = vec1[3];

                float[] vec2 = lineFromLeftToRight.get(x + 1);
                float x12 = vec2[0];
                float y12 = vec2[1];
                float x22 = vec2[2];
                float y22 = vec2[3];


                if ((y11 == y22 && y12 == y21 && y11 == y21 ) && (x21 + 1 == x12)) {

                    float[] newVec = new float[4];
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
                float[] myVec = lineFromLeftToRight.get(lineFromLeftToRight.size() - 1);
                float[] newVec = new float[4];
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
}

