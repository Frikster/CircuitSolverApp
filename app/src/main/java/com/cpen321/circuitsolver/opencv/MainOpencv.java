package com.cpen321.circuitsolver.opencv;

import android.graphics.Bitmap;
import android.util.Log;

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


    List<List<Element>> wires = new ArrayList<>();

    /**Method to detect the components,main method of this class
     *
     * @return the bitmap with the detected lines and a circle around the components
     */
    public Bitmap houghLines(Bitmap bMap){

        //Convert to a canny edge detector grayscale mat
        boolean aLotOfComponents = true;
        Mat tmp = new Mat (bMap.getWidth(), bMap.getHeight(), CvType.CV_8UC1);
        Mat tmp2 = new Mat (bMap.getWidth(), bMap.getHeight(), CvType.CV_8UC1);
        Utils.bitmapToMat(bMap, tmp);
        Imgproc.Canny(tmp, tmp2, 50, 200);

        Mat tmp3 = new Mat (bMap.getWidth(), bMap.getHeight(), CvType.CV_8UC1);

        //Execute the hough transform on the canny edge detector
        Mat lines = new Mat();
        Imgproc.HoughLinesP(tmp2,lines,1, Math.PI/180,0);

        cvtColor(tmp2, tmp3, COLOR_GRAY2BGR);

        //remove chunks from hough transform and make one line from them
        List<double[]> smoothedLines = smoothLines(MatToList(lines));


        int maxVote,minVote,radiusInComponent,distCompCorner,maxLinesToBeChunk;


        if(aLotOfComponents){
            maxVote = 35;
            minVote = 8;
            radiusInComponent = 9;
            distCompCorner = 10;
            maxLinesToBeChunk = 2;
        }
        else{
            //maxVote = 40;
            maxVote = 36;
            minVote = 12;
            radiusInComponent = 10;
            distCompCorner = 15;
            maxLinesToBeChunk = 3;
        }


        TuplePoints resLinesAndComponents = circlesAroundComponentsByVote(smoothedLines,tmp3, minVote, maxVote,radiusInComponent,maxLinesToBeChunk);
        List<double[]> residualLines = resLinesAndComponents.getFirst();
        List<double[]> components = resLinesAndComponents.getSecond();

        List<double[]> residualLinesWithoutChunk= removeChunks(residualLines, maxLinesToBeChunk);
        List<double[]> withoutBorders = removeImageBorder(residualLinesWithoutChunk);
        List<double[]> verticalLines = verticalLines(withoutBorders);
        List<double[]> horizontalLines = horizontalLines(withoutBorders);

        List<double[]> corners = findCorners(verticalLines,horizontalLines,10);

        List<double[]> singleCorners = singleCorners(corners,8);
        List<double[]> validCorners = removeCornersTooNearFromComponent(singleCorners, components, distCompCorner);
        drawCircles(tmp3,validCorners, new Scalar(0,255,0));
        drawCircles(tmp3,components, new Scalar(255,0,0));

        System.out.println("Nr of corners : "+validCorners.size());
        //eliminer les corners trop près des components
        correctCallToWires(validCorners, components);
        List<List<Element>> separatedComponents = separateComponents(wires);

        //Prints the found wires
        for(List<Element> wire : separatedComponents){
            System.out.println("New wire : ");
            for(Element e : wire){
                if(e instanceof Corner){
                    System.out.println("Corner, x : "+e.positionX+", y: "+e.positionY);
                }
                else{
                    System.out.println("Component, x : "+e.positionX+", y: "+e.positionY);
                }

            }
        }

        //Draw the found lines
        for (int x = 0; x < withoutBorders.size(); x++)
        {
            double[] vec = withoutBorders.get(x);
            double x1 = vec[0],
                    y1 = vec[1],
                    x2 = vec[2],
                    y2 = vec[3];
            Point start = new Point(x1, y1);
            Point end = new Point(x2, y2);
            //System.out.println("line :("+x1+" : "+y1+") , ("+x2+" : "+y2+")");
            if(x%3 == 0){
                Imgproc.line(tmp3, start, end, new Scalar(255,0,0), 1);
            }
            else if(x% 3 == 1){
                Imgproc.line(tmp3, start, end, new Scalar(0,255,0), 1);
            }
            else{
                Imgproc.line(tmp3, start, end, new Scalar(0,0,255), 1);
            }


        }


        //Create and return the final bitmap
        Bitmap bm = Bitmap.createBitmap(tmp3.cols(), tmp3.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(tmp3, bm);
        tmp.release();
        tmp2.release();
        tmp3.release();
        return bm;
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
                            Corner newCorner = new Corner((wire.get(i).positionX+wire.get(i+1).positionX)/2,(wire.get(i).positionY+wire.get(i+1).positionY)/2 );
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
        if(!currCorner.exploredDirections.isEmpty()){

            //get same horizontal and vertical components
            List<Element> sameY = getSameYElements(elements,currCorner,threshold);
            List<Element> sameX = getSameXElements(elements,currCorner,threshold);
            Collections.sort(sameX,new ComponentComparatorY());
            Collections.sort(sameY,new ComponentComparatorX());


            //find index of currCorner
            int i=0;
            for(int e = 0; e<sameX.size();e++){
                Element elem = sameX.get(e);
                if(elem.positionX == currCorner.positionX && elem.positionY == currCorner.positionY){
                    i = e;
                    break;
                }
            }

            int j=0;
            for(int e = 0; e<sameY.size();e++){
                Element elem = sameY.get(e);

                if(elem.positionX == currCorner.positionX && elem.positionY == currCorner.positionY){
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
        Corner firstCorner = cornerObjects.get(0);

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

            if (Math.abs(element.positionX - currentCorner.positionX)<threshold) {
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

            if (Math.abs(element.positionY - currentCorner.positionY)<threshold) {
                result.add(element);
            }

        }
        return result;
    }



    /** Remove the parasite corners that could have been detected, that are too near of the components
     * (Sometimes when the radius to find a component is not perfectly defined, it happens that a component is detected
     * not using all the lines belonging to the component. In this case it can happen that corners are detected with these
     * superfluous lines that in fact belong to the component).
     *
     * @param corners The found corners
     * @param components The found components
     * @param minDistance The min distance there has to be between a component and a corner
     * @return The purified corner lists
     */

    private List<double[]> removeCornersTooNearFromComponent(List<double[]> corners, List<double[]> components, int minDistance){
        List<double[]> validCorners = new ArrayList<>();
        for(double[] corner: corners){
            double x1= corner[0];
            double y1 = corner[1];
            boolean isTooNear = false;
            for(double[] component:components){
                double x2 = component[0];
                double y2 = component[1];
                if(Math.sqrt(Math.pow(x1-x2,2)+ Math.pow(y1-y2,2))<= minDistance){
                    isTooNear = true;
                    break;
                }
            }
            if(!isTooNear){
                validCorners.add(corner);
            }
        }
        return validCorners;
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
        //System.out.println(corners.size());
        List<double[]> singleCorners = new ArrayList<>();
        for(int i = 0; i< corners.size();i++){
            //System.out.println("Corner : x :"+corners.get(i)[0]+" , y : "+corners.get(i)[1]);
            boolean hasEquivalent = false;
            double[] corner1 = corners.get(i);
            double x1 = corner1[0];
            double y1 = corner1[1];
            for(int j = i+1; j<corners.size();j++){
                if(i!= j){

                    double[] corner2 = corners.get(j);

                    double x2 = corner2[0];
                    double y2 = corner2[1];
                    //System.out.println ("Considered : x1 : "+x1+" , y1 : "+y1+" ; x2 : "+x2+" , y2 : "+y2+" , distance : "+ Math.sqrt(Math.pow(x1-x2,2)+Math.pow(y1-y2,2)));
                    if(Math.sqrt(Math.pow(x1-x2,2)+ Math.pow(y1-y2,2))<= minDistance){
                        //System.out.println("hello bitch");
                        hasEquivalent = true;
                        break;
                    }
                }
            }
            if(!hasEquivalent){
                //System.out.println("added : x1 :"+x1+" , y1:"+y1);
                singleCorners.add(corner1);
            }
        }
        //System.out.println(singleCorners.size());
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
        /*for(int j = 0 ; j<line.size();j++){
            double[] linee = line.get(j);
            double x1 = linee[0];
            double y1 = linee[1];
            double x2 = linee[2];
            double y2 = linee[3];

            System.out.println("First round :( " + x1 + " , " + y1 + " ) ; (" + x2 + " , " + y2 + " ) ");

        }*/
        line.remove(0);
        line.remove(line.size()-1);

        Collections.sort(line,new LinesComparatorXY());
        /*for(int j = 0 ; j<line.size();j++){
            double[] linee = line.get(j);
            double x1 = linee[0];
            double y1 = linee[1];
            double x2 = linee[2];
            double y2 = linee[3];

            System.out.println("First round :( " + x1 + " , " + y1 + " ) ; (" + x2 + " , " + y2 + " ) ");

        }*/
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
        drawCircles(toDraw, circles, new Scalar(0,255,0));
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
     * @param lines containing all the lines
     * @param imageToWriteOn the opencv.Mat to wdraw the circles on
     * @param minLinesVote The min nr of lines a component must have to be recognized as a component
     * @param maxLinesVote The max nr of lines a component must have to be recognized as a component
     * @param radius has to be set to smaller if the components are small and close
     * @param maxToBeChunk all lines bigger than this are real lines
     */
    private TuplePoints circlesAroundComponentsByVote(List<double[]> lines, Mat imageToWriteOn, int minLinesVote, int maxLinesVote, int radius, int maxToBeChunk){
        List<double[]> linesCopy = new ArrayList<>(lines);
        List<double[]> componentsFound = new ArrayList<>();

        //For all votes starting from the biggest number of lines
        for(int nrLine = maxLinesVote; nrLine >= minLinesVote; nrLine--){
            for (int i = 0; i < imageToWriteOn.cols(); i++) {
                for (int j = 0; j < imageToWriteOn.rows(); j++) {
                    int vote = 0;
                    Set<double[]> potentialLinesInComponent = new HashSet<>();
                    //Find the nr of lines from around a given position
                    for (double[] line : linesCopy) {
                        if (Math.sqrt(Math.pow(i - line[0], 2) + Math.pow(j - line[1], 2)) < radius) {
                            vote++;
                            if(lineIsChunk(line, maxToBeChunk)) {
                                potentialLinesInComponent.add(line);
                            }
                        }
                    }
                    //if the nr of lines around is sufficent, add the component to the found component and delete it from the found lines
                    if (vote >= nrLine) {
                        double[] circle = new double[3];
                        circle[0] = i;
                        circle[1] = j;
                        circle[2] = radius;
                        componentsFound.add(circle);
                        linesCopy.removeAll(potentialLinesInComponent);
                    }
                }
            }
            //Print to give an approximation of time (Takes roughly 0.5 seconds per iteration)
            System.out.println("Try to find components with "+nrLine+" lines");
        }

        //Print last lines without the components
        /*Collections.sort(linesCopy,new LinesComparator());
        for(int j = 0 ; j<linesCopy.size();j++){
            double[] line = linesCopy.get(j);
            double x1 = line[0];
            double y1 = line[1];
            double x2 = line[2];
            double y2 = line[3];
            if(x1 != x2 || y1 != y2) {
                System.out.println("( " + x1 + " , " + y1 + " ) ; (" + x2 + " , " + y2 + " ) ");
            }
        }*/

        System.out.println("Number of components found : "+componentsFound.size());

        return new TuplePoints(linesCopy,componentsFound);

    }

    /**
     *
     * @param dst Mat to draw the circles
     * @param circlesToDraw The list containing the circles
     * @return the mat with the drawn circles on it
     */
    private Mat drawCircles(Mat dst, List<double[]> circlesToDraw, Scalar color){
        double xi = 0.0;
        double yi = 0.0;
        int ri = 0;

        for( int i = 0; i < circlesToDraw.size(); i++ ) {
            double[] data = circlesToDraw.get(i);

            for(int j = 0 ; j < data.length ; j++){
                xi = data[0];
                yi = data[1];
                ri = (int) data[2];
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

                //System.out.println("x : "+x);

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
