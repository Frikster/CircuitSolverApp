package com.cpen321.circuitsolver.opencv;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.cpen321.circuitsolver.util.Constants.thresholdXY;

/**Detects the wires from components and corners
 * Created by Simon on 01.12.2016.
 */

public class WireCalculator {
    private List<Element> objectizedCompAndCorners;
    private Corner firstCorner;
    private List<double[]> residualLinesWithoutChunk;
    private List<List<Element>> wires = new ArrayList<>();

    protected WireCalculator(List<Element> objectizedCompAndCorners, Corner firstCorner,  List<double[]> residualLinesWithoutChunk){
        this.objectizedCompAndCorners = new ArrayList<>(objectizedCompAndCorners);
        this.firstCorner = firstCorner;
        this.residualLinesWithoutChunk = new ArrayList<>(residualLinesWithoutChunk);
    }

    protected List<List<Element>> process(){
        detectWires(objectizedCompAndCorners,firstCorner, thresholdXY,residualLinesWithoutChunk);
        return wires;
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


}
