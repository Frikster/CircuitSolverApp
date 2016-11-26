package com.cpen321.circuitsolver.opencv;

import android.graphics.Bitmap;

import com.cpen321.circuitsolver.model.CircuitElmFactory;
import com.cpen321.circuitsolver.model.SimplePoint;
import com.cpen321.circuitsolver.model.components.CircuitElm;
import com.cpen321.circuitsolver.service.CircuitDefParser;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.Core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.cpen321.circuitsolver.util.Constants.RESISTOR;
import static com.cpen321.circuitsolver.util.Constants.cornerSearchRadius;
import static com.cpen321.circuitsolver.util.Constants.distanceFromComponent;
import static com.cpen321.circuitsolver.util.Constants.lowerCannyThreshold;
import static com.cpen321.circuitsolver.util.Constants.maxLinesToBeChunk;
import static com.cpen321.circuitsolver.util.Constants.minPoints;
import static com.cpen321.circuitsolver.util.Constants.radius;
import static com.cpen321.circuitsolver.util.Constants.thresholdXY;
import static com.cpen321.circuitsolver.util.Constants.tooNearFromComponent;
import static com.cpen321.circuitsolver.util.Constants.twoCornersTooNear;
import static com.cpen321.circuitsolver.util.Constants.upperCannyThreshold;
import static org.opencv.core.Core.NORM_MINMAX;
import static org.opencv.core.CvType.CV_32FC1;
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
        Mat tmp3 = new Mat (bMap.getWidth(), bMap.getHeight(), CvType.CV_8UC1);
        Mat tmp_postHarris = new Mat (bMap.getWidth(), bMap.getHeight(), CvType.CV_8UC1);
        Utils.bitmapToMat(bMap, tmp);

        System.out.println("Width/height : "+bitMapHeight +" , "+bitMapWidth);
        //Convert to a canny edge detector grayscale mat
        Imgproc.Canny(tmp, tmp2, lowerCannyThreshold, upperCannyThreshold);

        /// Detector parameters
        int blockSize = 2;
        int apertureSize = 3;
        double k = 0.04;
        Imgproc.cornerHarris(tmp2, tmp_postHarris, blockSize, apertureSize, k );
        //Core.normalize( tmp_postHarris, tmp_postHarris, 0, 255, NORM_MINMAX, CV_32FC1, new Mat() );

        /// Drawing a circle around corners
//        for( int j = 0; j < tmp_postHarris.rows() ; j++ )
//        { for( int i = 0; i < tmp_postHarris.cols(); i++ )
//        {
//            System.out.print("kytfc");
////            if( (int) tmp_postHarris.get(j,i) > 200)
////            {
////                Imgproc.circle( tmp_postHarris, new Point( i, j ), 5,  new Scalar(0), 2, 8, 0 );
////            }
//        }
//        }

        Bitmap tmp2_bm_postHarris = Bitmap.createBitmap(tmp2.cols(), tmp2.rows(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(tmp2, tmp2_bm_postHarris);

        Bitmap tmp2_bm_postCanny = Bitmap.createBitmap(tmp2.cols(), tmp2.rows(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(tmp2, tmp2_bm_postCanny);

        //Execute the hough transform on the canny edge detector
        Mat lines = new Mat();
        Imgproc.HoughLinesP(tmp2,lines,1,Math.PI/180,0);

        //To be able to draw in color on the mat tmp3
        cvtColor(tmp2, tmp3, COLOR_GRAY2BGR);

        //remove chunks from hough transform and make one line from them
        List<double[]> smoothedLines = smoothLines(MatToList(lines));

        // diagonal set of points from Houghlines/Canny = "a chunk"
        // first line assumes component is only made of diagonals
        // List<PointDB> assPoints = dbscan(keepChunks(smoothedLines,maxLinesToBeChunk), tmp3, radius, minPoints);
        // second line only assumes components are made of many lines (hori, vert, or diagonal)
        List<PointDB> assPoints = dbscan(smoothedLines, tmp3, radius, minPoints);
        List<PointDB> assignedPoints = assignedPoints(assPoints);
        TuplePoints residAssigned = dbToArray(assignedPoints, smoothedLines, maxLinesToBeChunk);
        List<double[]> residualLines = residAssigned.getFirst();
        List<double[]> components = residAssigned.getSecond();

        List<double[]> residualLinesWithoutChunk= removeChunks(residualLines, maxLinesToBeChunk);

        List<double[]> verticalLines = verticalLines(residualLinesWithoutChunk);
        List<double[]> horizontalLines = horizontalLines(residualLinesWithoutChunk);
        List<double[]> corners = findCorners(verticalLines, horizontalLines, cornerSearchRadius);

        List<double[]> singleCorners = singleCorners(corners,twoCornersTooNear);
        List<double[]> validCorners = goodCorners(assignedPoints,singleCorners,tooNearFromComponent);

        //If removing too near components removes everything, just keep the corners before the filtering
        if(validCorners.size() == 0){
            validCorners = new ArrayList<>(singleCorners);
        }

        //####### Special for testing tensorflow testing ####
        componentsForTensorFlow = new ArrayList<>(components);
        originalMat = tmp;
        List<Bitmap> salut = getSubImagesForTensorflow();


        //Detecting the wires from the list of corners and components
        List<Element> objectizedCompAndCorners = objectizeCompAndCorner(validCorners, components);
        List<Component> objectizedComponents = getCompFromElements(objectizedCompAndCorners);

        Corner firstCorner = null;
        if(!objectizeCorners(validCorners).isEmpty()){
            firstCorner = objectizeCorners(validCorners).get(0);
        }
        detectWires(objectizedCompAndCorners,firstCorner, thresholdXY,residualLinesWithoutChunk);

        //Process the result to output a normalized version of the wires
        separatedComponents = separateComponents(wires);

        separatedComponents = completeMissingEndings(separatedComponents, thresholdXY, distanceFromComponent);

        separatedComponents = addOrphansToWires(separatedComponents, objectizedComponents, distanceFromComponent);

        separatedComponents = addMisingWires(separatedComponents,findCornersToWire(separatedComponents));

        separatedComponents = removeDuplicateWires(separatedComponents);

        separatedComponents = removeWireOnComponents(separatedComponents);

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

    //Method to call for tensorlow. imageWH is the frame around a component.
    private List<double[]> componentsForTensorFlow;
    int imageWH =10*5;
    Mat originalMat;
    public List<Bitmap> getSubImagesForTensorflow(){
        List<Bitmap> subimages = new ArrayList<>();
        for(double[] component : componentsForTensorFlow){
            System.out.println(component[0]+" , "+component[1]);
            Mat submat = originalMat.submat((int)(component[1]-imageWH),(int)(component[1]+imageWH),(int)(component[0]-imageWH),(int)(component[0]+imageWH));
            Bitmap b = Bitmap.createBitmap(submat.cols(), submat.rows(),Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(submat,b);
            subimages.add(b);
        }
        return subimages;
    }

    /**
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

    /**Keeps only the chunks from given lines
     *
     * @param lines The lines where to keep the chinks
     * @param maxToBeChunk Maximum length of a line to be considered as a chunk
     * @return The lines that are chunk
     */
    private List<double[]> keepChunks(List<double[]> lines, int maxToBeChunk){
        List<double[]> chunks = new ArrayList<>();
        for(double[] line : lines){
            if(lineIsChunk(line,maxToBeChunk)){
                chunks.add(line);
            }
        }
        return chunks;
    }

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

    private List<Component> getCompFromElements(List<Element> elements){
        List<Component> components = new ArrayList<>();
        for(Element e: elements){
            if(e instanceof Component){
                components.add((Component) e);
            }
        }
        return components;
    }

    /**
     *
     * @param wires All the current wires that are connected with corners
     * @param allComponents All the components that have originally been detected
     * @return All the wires and the previously unconnected components in a list of wires
     */
    private List<List<Element>> addOrphansToWires(List<List<Element>> wires, List<Component> allComponents, int distanceFromComponent){
        List<List<Element>> orphans = new ArrayList<>();
        List<List<Element>> wiresWithorphans = new ArrayList<>(wires);
        for(Component e : allComponents){

                boolean foundOrphan = true;
                for (List<Element> wire : wires) {

                    if(containsElement(wire,e)){

                        foundOrphan = false;
                        break;
                    }
                }

                if (foundOrphan) {
                    Component co = (Component) e;
                    Corner c1 = new Corner(co.getX() - distanceFromComponent, co.getY());
                    Corner c2 = new Corner(co.getX() + distanceFromComponent, co.getY());
                    List<Element> wire = new ArrayList<>();
                    wire.add(c1);
                    wire.add(co);
                    wire.add(c2);
                    orphans.add(wire);
                }
            }

        //Integrate orphans
        wiresWithorphans.addAll(orphans);
        return wiresWithorphans;
    }


    /**Utilitary method to return a closed circuit
     * Finds all points that are not part of a closed circuit (points that don't have two wires going in opposite direction)
     * @param wires All the wires detected
     * @return The points that need to be connected
     */

    private List<Corner> findCornersToWire(List<List<Element>> wires){
        List<Corner> toFindAnOther = new ArrayList<>();
        List<Corner> allCorners = getCornersFromWires(wires);

        for(Corner c : allCorners){
            int nrApparition = 0;
            for(List<Element> wire : wires){
                if(containsElement(wire,c)){
                    nrApparition++;
                }
            }
            if(nrApparition <= 1){
                toFindAnOther.add(c);
            }
        }
        return toFindAnOther;

    }

    /**Makes sure there isn't a wire on a component
     *
     * @param wires All the found wires
     * @return A list of unique components and wires
     */

    private List<List<Element>> removeWireOnComponents (List<List<Element>> wires){
        List<List<Element>> cleanedUpWires = new ArrayList<>();
        for(List<Element> wire : wires){
            //we're going to look for a potential wire of size 3 at the same position.
            boolean hasSame = false;
            if(wire.size() == 2){
                for(List<Element> wire1 : wires){
                    if(wire1.size() == 3){
                        if(containsElement(wire1,wire.get(0)) && containsElement(wire1,wire.get(1))){
                            hasSame = true;
                            break;
                        }
                    }
                }
                if(!hasSame) {
                    cleanedUpWires.add(wire);
                }
            }
            else{
                cleanedUpWires.add(wire);
            }
        }
        return cleanedUpWires;
    }
    /**Makes sure there isn't a wire in one direction, and the same in the opposite direction
     *
     * @param wires All the found wires
     * @return A list of unique wires
     */
    private List<List<Element>> removeDuplicateWires (List<List<Element>> wires){
        List<List<Element>> singleWires = new ArrayList<>();
        for(int i=0; i<wires.size();i++){
            boolean identicalWire = false;
            for(int j=i+1; j<wires.size();j++){
                boolean hasIdenticalComponents = true;
                if(wires.get(i).size() == wires.get(j).size()){
                    List<Element> wire1 = wires.get(i);
                    List<Element> wire2 = wires.get(j);
                    for(int e=0;e<wire1.size();e++){
                        if(!containsElement(wire2,wire1.get(e))){
                            hasIdenticalComponents=false;
                        }
                    }
                    if(hasIdenticalComponents){
                        identicalWire = true;
                    }
                }
            }
            if(!identicalWire){
                singleWires.add(wires.get(i));
            }
        }
        return singleWires;
    }

    /**Connects all the missing wires to form a closed component
     * Note that this method usies the simple assumption to connect the unconnected points to the closest unconnected points
     * This function could just be removed from the pipeline, and let the user complete the missing wires
     *
     * @param wires All the detected wires
     * @param cornersToWire The points/corners that need to be connected to another point
     * @return A list of wires representing a closed circuit
     */
    private List<List<Element>> addMisingWires(List<List<Element>> wires, List<Corner> cornersToWire){
        List<List<Element>> wiresWithMissing = new ArrayList<>(wires);
        List<Corner> allCorners = getCornersFromWires(wires);
        List<Corner> alreadyWiredCorners = new ArrayList<>();
        for(Corner c : cornersToWire){
            if(!containsCorner(alreadyWiredCorners,c)) {
                //Go and look through the unconnected corners
                boolean foundAnUnconnected = false;
                double nearestDistanceUnc = Double.MAX_VALUE;
                Corner currentCornerUnc = new Corner(0, 0);
                for (int i = 0; i < cornersToWire.size(); i++) {
                    if (cornersToWire.get(i).getX() != c.getX() && cornersToWire.get(i).getY() != c.getY()) {
                        foundAnUnconnected = true;
                        double distance = Math.sqrt(Math.pow(cornersToWire.get(i).getX() - c.getX(), 2) + Math.pow(cornersToWire.get(i).getY() - c.getY(), 2));
                        if (distance < nearestDistanceUnc) {
                            nearestDistanceUnc = distance;
                            currentCornerUnc = cornersToWire.get(i);
                        }
                    }
                }
                if(foundAnUnconnected) {
                    List<Element> newWireUnc = new ArrayList<>();
                    newWireUnc.add(c);
                    newWireUnc.add(currentCornerUnc);
                    wiresWithMissing.add(newWireUnc);
                    alreadyWiredCorners.add(currentCornerUnc);
                }


                //Go and look through all corners
                if (!foundAnUnconnected) {
                    double nearestDistance = Double.MAX_VALUE;
                    Corner currentCorner = new Corner(0, 0);
                    for (int i = 0; i < allCorners.size(); i++) {
                        if (allCorners.get(i).getX() != c.getX() && allCorners.get(i).getY() != c.getY()) {
                            double distance = Math.sqrt(Math.pow(allCorners.get(i).getX() - c.getX(), 2) + Math.pow(allCorners.get(i).getY() - c.getY(), 2));
                            if (distance < nearestDistance) {
                                nearestDistance = distance;
                                currentCorner = allCorners.get(i);
                            }
                        }
                    }
                    List<Element> newWire = new ArrayList<>();
                    newWire.add(c);
                    newWire.add(currentCorner);
                    wiresWithMissing.add(newWire);
                }
            }
        }
        return wiresWithMissing;
    }


    /**Gets all the corners once from a list of wires
     *
     * @param wires The found wires
     * @return The corners present in wires
     */

    private List<Corner> getCornersFromWires(List<List<Element>> wires){
        List<Corner> corners = new ArrayList<>();
        for(List<Element> wire : wires){
            if(wire.size() == 2){
                Corner c1 = (Corner)wire.get(0);
                Corner c2 = (Corner)wire.get(1);
                if(!containsCorner(corners,c1)){
                    corners.add(c1);
                }
                if(!containsCorner(corners,c2)){
                    corners.add(c2);
                }
            }
            else if(wire.size() == 3){
                Corner c1 = (Corner)wire.get(0);
                Corner c2 = (Corner)wire.get(2);
                if(!containsCorner(corners,c1)){
                    corners.add(c1);
                }
                if(!containsCorner(corners,c2)){
                    corners.add(c2);
                }
            }
        }
        return corners;
    }

    /**Utilitary function to know if an element is contained in a list of Element
     *
     * @param element list of Element to search in
     * @param e The element to look for
     * @return true if e is contained in Element
     */
    private boolean containsElement(List<Element> element, Element e){
        for(Element e1 : element){
            if(e1.getX() == e.getX() && e1.getY()==e.getY()){
                return true;
            }
        }
        return false;
    }

    /**Utilitary function to know if a Corner is contained in a list of Corner
     *
     * @param alreadyAdded list of Corner to search in
     * @param corner The Corner to look for
     * @return true if corner is contained in alreadyAdded
     */
    private boolean containsCorner(List<Corner> alreadyAdded, Corner corner){
        for(Corner c : alreadyAdded){
            if(c.getX() == corner.getX() && c.getY()==corner.getY()){
                return true;
            }
        }
        return false;
    }


    /**Takes all the wires as a parameter and adds a Corner at the end if it finishes by a Component
     *
     * @param wires The detected wires
     * @return
     */

    private List<List<Element>> completeMissingEndings(List<List<Element>> wires, int thresholdXY,
                                                       int distanceFromComponent){

        List<List<Element>> wiresWithHappyEnding = new ArrayList<>(); //\o/
        for(List<Element> wire : wires) {

            if (wire.get(wire.size() - 1) instanceof Component) {
                //This means that it is of size 2, because it already passed through separateComponents
                //See if the x are aligned
                Corner c = null;
                if(Math.abs(wire.get(0).getX() - wire.get(1).getX())<thresholdXY){
                    if(wire.get(0).getY() > wire.get(1).getY()){
                        c = new Corner(wire.get(1).getX(), wire.get(1).getY()-distanceFromComponent);
                    }
                    else{
                        c = new Corner(wire.get(1).getX(), wire.get(1).getY()+distanceFromComponent);
                    }
                }
                else if(Math.abs(wire.get(0).getY() - wire.get(1).getY())<thresholdXY){
                    if(wire.get(0).getX() > wire.get(1).getX()){
                        c = new Corner(wire.get(1).getX()-distanceFromComponent, wire.get(1).getY());
                    }
                    else{
                        c = new Corner(wire.get(1).getX()+distanceFromComponent, wire.get(1).getY());
                    }
                }
                List<Element> newWire = new ArrayList<>();
                newWire.add(wire.get(0));
                newWire.add(wire.get(1));
                newWire.add(c);
                wiresWithHappyEnding.add(newWire);

            }

            else{
                wiresWithHappyEnding.add(wire);
            }
        }
        return wiresWithHappyEnding;
    }

    /**Function to make from [Corner, Component1, Component2,...., ComponentN, Corner] => [Corner, Component1, Corner] , [Corner, Component2, Corner],... [Corner, ComponentN, Corner]
     *Separates two adjacent corners
     * Important note: After this function, it is sure that a wire is of max size 3, and that if it is of size 3, it starts and ends with a Corner.
     * If it is of size 2 , it starts and ends with a Corner.
     * This comes from the fact that the param wires comes from the detectWires() function, and thus each List<Element> in wires starts with a Corner
     * @param wires The initial wires found
     * @return The components separated
     */
    private List<List<Element>> separateComponents(List<List<Element>> wires){
        List<List<Element>> newWires = new ArrayList<>(wires);
        while(TwoAdjacentComponent(newWires)){

            List<List<Element>> result = new ArrayList<>();
            for(List<Element> wire : newWires){

                    boolean brokeWire = false;
                    for(int i =1; i<wire.size()-1;i++){
                        if(wire.get(i) instanceof Component && wire.get(i+1) instanceof Component){
                            brokeWire = true;
                            List<Element> newWire1 = new ArrayList<>();
                            newWire1.add(wire.get(i-1));
                            newWire1.add(wire.get(i));
                            Corner newCorner = new Corner((wire.get(i).getX()+wire.get(i+1).getX())/2,(wire.get(i).getY()+wire.get(i+1).getY())/2 );
                            newWire1.add(newCorner);

                            List<Element> newWire2 = new ArrayList<>();
                            newWire2.add(newCorner);

                            for(int e = i+1 ; e<wire.size();e++){
                                newWire2.add(wire.get(e));
                            }

                            result.add(newWire1);
                            result.add(newWire2);
                            break;

                        }
                    }
                if(!brokeWire){
                    result.add(wire);
                }


            }
            newWires = new ArrayList<>(result);
        }
        return newWires;
    }

    /** Small utilitary method to know if all wires are standardized in [corner,component,corner]
     *
     * @param wires
     * @return true if all standardized
     */
    private boolean TwoAdjacentComponent(List<List<Element>> wires){
        for(List<Element> wire : wires){
            for(int i=0; i<wire.size();i++){
                if(i!= wire.size()-1){
                    if(wire.get(i) instanceof Component && wire.get(i+1) instanceof Component){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**Recursive function to detect all the wires
     * This is the main function to detect wires from components and Corners.
     * It always starts with a corner, and searches in an horizontal and vertical way to find other components and/or corners
     *
     * @param elements The elements to detect from
     * @param currCorner The corner from which we look for wires
     * Changes a global field of the class
     */
    private void detectWires(List<Element> elements, Corner currCorner, int thresholdXY, List<double[]> residualLines){
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
        for(double[] component : components){
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
    private boolean existsALineBetweenTwoPoints(Element e1, Element e2, List<double[]> lines, char same, int threshold){
        double x1 = e1.getX();
        double y1 = e1.getY();
        double x2 = e2.getX();
        double y2 = e2.getY();
        boolean foundOne = false;
        for(double[] line : lines){
            double startX = line[0];
            double startY = line[1];
            double endX = line[2];
            double endY = line[3];
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

    /** Removes the chunks from a collection of lines
     *
     * @param lines the collection of lines to be filtered
     * @param minLineLength the minimum length a line should have
     * @return a collection of lines without chunks
     */

    private List<double[]> removeChunks(List<double[]> lines, int minLineLength){
        List<double[]> realLine = new ArrayList<>();
        for(double[] line : lines){
            double x1 = line[0];
            double y1 = line[1];
            double x2 = line[2];
            double y2 = line[3];

            if(Math.abs(x1-x2) >= minLineLength || Math.abs(y1-y2) >= minLineLength){
                realLine.add(line);
            }
        }
        return realLine;
    }

    /**
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
     * @param dst Mat to draw the circles
     * @param circlesToDraw The list containing the circles
     * @return the mat with the drawn circles on it
     */
    private Mat drawCircles(Mat dst, List<double[]> circlesToDraw, Scalar color, int radius){
        double xi = 0.0;
        double yi = 0.0;
        int ri = 0;

        for( int i = 0; i < circlesToDraw.size(); i++ ) {
            double[] data = circlesToDraw.get(i);

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

            //get the lines 2 by 2, see if they are adjacent, and if so make one line from the two7


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
}

