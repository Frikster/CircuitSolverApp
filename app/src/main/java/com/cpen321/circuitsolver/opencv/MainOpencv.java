package com.cpen321.circuitsolver.opencv;

import android.graphics.Bitmap;


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


import static org.opencv.imgproc.Imgproc.COLOR_GRAY2BGR;
import static org.opencv.imgproc.Imgproc.cvtColor;


/**Main opencv class
 * Created by Simon on 27.10.2016.
 */

public class MainOpencv {

    //########MODIFIED ###############
    //MainOpenCV
    //##############ADDED################
    //PointDB, DBSCAN
    //###########TO REMOVE################
    // MainOpenCvOld, PointK, Cluster, Kmeans
    //##############TODO :#################
    //Control that there exists at least a line between two same-width or same-height corners before adding
    //Add comments
    //Integrate with Jenny and Tenserflow
    //##########################################


    List<List<Element>> wires = new ArrayList<>();

    /**Method to detect the components,main method of this class
     *
     * @return the bitmap with the detected lines and a circle around the components
     */
    public Bitmap houghLines(Bitmap bMap){

        //Convert to a canny edge detector grayscale mat
        System.out.println("width/height :"+bMap.getWidth()+" , "+ bMap.getHeight());
        Mat tmp = new Mat (bMap.getWidth(), bMap.getHeight(), CvType.CV_8UC1);
        Mat tmp2 = new Mat (bMap.getWidth(), bMap.getHeight(), CvType.CV_8UC1);
        Utils.bitmapToMat(bMap, tmp);
        Imgproc.Canny(tmp, tmp2, 50, 200);

        Mat tmp3 = new Mat (bMap.getWidth(), bMap.getHeight(), CvType.CV_8UC1);

        //Execute the hough transform on the canny edge detector
        Mat lines = new Mat();
        Imgproc.HoughLinesP(tmp2,lines,1,Math.PI/180,0);

        cvtColor(tmp2, tmp3, COLOR_GRAY2BGR);

        //remove chunks from hough transform and make one line from them
        List<double[]> smoothedLines = smoothLines(MatToList(lines));


        int maxLinesToBeChunk = 2;
        int radius = 5;
        int minPoints = 10;


        List<PointDB> assPoints = dbscan(keepChunks(smoothedLines,2), tmp3, radius, minPoints);
        List<PointDB> assignedPoints = assignedPoints(assPoints);
        TuplePoints residAssigned = dbToArray(assignedPoints , smoothedLines, maxLinesToBeChunk);
        List<double[]> residualLines = residAssigned.getFirst();
        List<double[]> components = residAssigned.getSecond();



        List<double[]> residualLinesWithoutChunk= removeChunks(residualLines, maxLinesToBeChunk);
        List<double[]> withoutBorders = removeImageBorder(residualLinesWithoutChunk);
        List<double[]> verticalLines = verticalLines(withoutBorders);
        List<double[]> horizontalLines = horizontalLines(withoutBorders);
        List<double[]> corners = findCorners(verticalLines,horizontalLines,10);



        int twoCornersTooNear = 8;
        List<double[]> singleCorners = singleCorners(corners,twoCornersTooNear);
        int tooNearFromComponent = 4;
        List<double[]> validCorners = goodCorners(assignedPoints,singleCorners,tooNearFromComponent);

        if(validCorners.size() == 0){
            validCorners = new ArrayList<>(singleCorners);
        }
        drawCircles(tmp3,validCorners, new Scalar(0,255,0),10);
        drawCircles(tmp3,components, new Scalar(255,0,0),10);

        System.out.println("Nr of corners : "+validCorners.size());
        System.out.println("Nr of components : "+components.size());
        //eliminer les corners trop près des components
        correctCallToWires(validCorners, components);
        List<List<Element>> separatedComponents = separateComponents(wires);

        //Prints the found wires
        for(List<Element> wire : separatedComponents){
            System.out.println("New wire : ");
            for(Element e : wire){
                if(e instanceof Corner){
                    System.out.println("Corner, x : "+e.getX()+", y: "+e.getY());
                }
                else{
                    System.out.println("Component, x : "+e.getX()+", y: "+e.getY());
                }

            }
        }

        //Create and return the final bitmap
        Bitmap bm = Bitmap.createBitmap(tmp3.cols(), tmp3.rows(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(tmp3, bm);
        tmp.release();
        tmp2.release();
        tmp3.release();
        return bm;
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

    /**Function to make from [Corner, Component1, Component2,...., ComponentN, Corner] => [Corner, Component1, Corner] , [Corner, Component2, Corner],... [Corner, ComponentN, Corner]
     *
     * @param wires The initial wires found
     * @return The components separated
     */
    private List<List<Element>> separateComponents(List<List<Element>> wires){
        List<List<Element>> newWires = new ArrayList<>(wires);
        List<List<Element>> result = new ArrayList<>();
        while(!allWiresMax3(newWires)){
            result = new ArrayList<>();
            for(List<Element> wire : newWires){
                if(wire.size()>3){
                    //Search for the two adjacent components

                    for(int i =1; i<wire.size()-1;i++){
                        if(wire.get(i) instanceof Component && wire.get(i+1) instanceof Component){
                            List<Element> newWire1 = new ArrayList<>();
                            newWire1.add(wire.get(i-1));
                            newWire1.add(wire.get(i));
                            Corner newCorner = new Corner((wire.get(i).getX()+wire.get(i+1).getX())/2,(wire.get(i).getY()+wire.get(i+1).getY())/2 );
                            newWire1.add(newCorner);

                            List<Element> newWire2 = new ArrayList<>();
                            newWire2.add(newCorner);
                            newWire2.add(wire.get(i+1));
                            //If a corner detection went wrong, we need to place an if
                            if(wire.size()>i+2) {
                                newWire2.add(wire.get(i + 2));
                            }
                            result.add(newWire1);
                            result.add(newWire2);

                        }
                    }
                }
                else{
                    result.add(wire);
                }
            }
            newWires = new ArrayList<>(result);
        }
        return result;
    }

    /** Small utilitary method to know if all wires are standardized in [corner,component,corner]
     *
     * @param wires
     * @return true if all standardized
     */
    private boolean allWiresMax3(List<List<Element>> wires){
        for(List<Element> wire : wires){
            if(wire.size()>3){
                return false;
            }
        }
        return true;
    }

    /**Recursive function to detect all the wires
     *
     * @param elements The elements to detect from
     * @param currCorner The corner from which we look for wires
     * Changes a global field of the class
     */
    private void detectWires(List<Element> elements, Corner currCorner){
        int threshold = 7;
        if(currCorner != null && !currCorner.exploredDirections.isEmpty()){

            //get same horizontal and vertical components
            List<Element> sameY = getSameYElements(elements,currCorner,threshold);
            List<Element> sameX = getSameXElements(elements,currCorner,threshold);
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
                    aWire.add(elem);
                    if(elem instanceof Corner){
                        ((Corner)elem).exploredDirections.remove('e');

                        detectWires(elements,(Corner)elem);
                        break;
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

                    aWire.add(elem);
                    if(elem instanceof Corner){
                        ((Corner)elem).exploredDirections.remove('w');

                        detectWires(elements,(Corner)elem);
                        break;
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

                    aWire.add(elem);
                    if(elem instanceof Corner){
                        ((Corner)elem).exploredDirections.remove('s');

                        detectWires(elements,(Corner)elem);
                        break;
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

                    aWire.add(elem);
                    if(elem instanceof Corner){
                        ((Corner)elem).exploredDirections.remove('n');

                        detectWires(elements,(Corner)elem);
                        break;
                    }
                }
                if(aWire.size() > 1){

                    wires.add(aWire);
                }
            }
        }
    }


    /**A temporary function to pass from point array to object and doing the right call to the function to detect the wires
     *
     * @param corners the points in array form
     * @param components the components in array form
     */
    private void correctCallToWires(List<double[]> corners, List<double[]> components){
        List<Corner> cornerObjects = objectizeCorners(corners);
        List<Component> componentObjects = objectizeComponents(components);
        List<Element> everything = new ArrayList<>();
        everything.addAll(cornerObjects);
        everything.addAll(componentObjects);
        Corner firstCorner = null;
        if(cornerObjects.size() != 0) {
            firstCorner = cornerObjects.get(0);
        }
        detectWires(everything, firstCorner);
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
            componentObjects.add(new Component(component[0], component[1],"resistor"));
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

    /**Removes the border from the image
     * (the lines of the border of the image are detected by the hough transform and can be removed using this function)
     * @param lines the list of lines containng a border
     * @return The list of lines without the border
     */

    private List<double[]> removeImageBorder(List<double[]> lines){
        List<double[]> line = new ArrayList<>(lines);
        Collections.sort(line,new LinesComparatorYX());
        line.remove(0);
        line.remove(line.size()-1);

        Collections.sort(line,new LinesComparatorXY());
        line.remove(0);
        line.remove(line.size()-1);

        return line;
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

    /**Draw a single circle
     *
     * @param i "x" position of center
     * @param j "y" position of center
     * @param radius radius of the circle
     * @param toDraw the Mat to draw on
     */

    private void drawSCircle(int i, int j, int radius, Mat toDraw){
        double[] circle = new double[3];
        circle[0]=i;
        circle[1]=j;
        circle[2]=radius;
        List<double[]> circles = new ArrayList<>();
        circles.add(circle);
        drawCircles(toDraw, circles, new Scalar(0,0,255), 15);
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

    /**
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
                double y21 = vec1[3];

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

