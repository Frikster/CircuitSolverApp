package com.cpen321.circuitsolver.opencv;

import java.util.ArrayList;
import java.util.List;

import static com.cpen321.circuitsolver.util.Constants.distanceFromComponent;
import static com.cpen321.circuitsolver.util.Constants.thresholdXY;

/**Class that normalizes the found wires to the wanted output
 * Created by Simon on 01.12.2016.
 */

public class WireProcessor {
    List<List<Element>> wires;
    List<Component> objectizedComponents;
    List<List<Element>> separatedComponents = new ArrayList<>();

    public WireProcessor(List<List<Element>> wires,  List<Component> objectizedComponents){
        this.wires = new ArrayList<>(wires);
        this.objectizedComponents = new ArrayList<>(objectizedComponents);
    }

    public List<List<Element>> process(){
        separatedComponents = separateComponents(wires);
        separatedComponents = completeMissingEndings(separatedComponents, thresholdXY, distanceFromComponent);
        separatedComponents = addOrphansToWires(separatedComponents, objectizedComponents, distanceFromComponent);
        separatedComponents = addMisingWires(separatedComponents,findCornersToWire(separatedComponents));
        separatedComponents = removeDuplicateWires(separatedComponents);
        separatedComponents = removeWireOnComponents(separatedComponents);

        return separatedComponents;
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

    /**Function that adds to the wire the components that don't belong to a wire
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

}
