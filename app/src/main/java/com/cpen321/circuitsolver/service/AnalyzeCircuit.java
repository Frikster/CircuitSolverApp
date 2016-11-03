package com.cpen321.circuitsolver.service;


import com.cpen321.circuitsolver.model.CircuitNode;
import com.cpen321.circuitsolver.model.SimplePoint;
import com.cpen321.circuitsolver.model.components.CircuitElm;
import com.cpen321.circuitsolver.util.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by jen on 2016-10-13.
 */
public class AnalyzeCircuit {

    private List<CircuitNode> nodes;
    private List<CircuitElm> elements;

    /**
     * Note that list of elements may be modified by AnalyzeCircuit instance in its other methods
     * @param elements
     */
    public AnalyzeCircuit(List<CircuitElm> elements){
        this.elements = new ArrayList(elements);
    }

    /**
     * Initializes circuit nodes from elements
     */
    public void init(){
        nodes = new ArrayList<CircuitNode>();

        for(CircuitElm e: elements){
            //Wire elements are technically part of one node (voltage is the same at both ends)
            if(e.getType().equals(Constants.WIRE)){
                SimplePoint p1 = e.getPoint(0);
                SimplePoint p2 = e.getPoint(1);
                CircuitNode cn;
                //if p1 corresponds to node but p2 doesn't, then add p2 to corresponding node
                if(nodesContainPoint(p1) && !nodesContainPoint(p2)) {
                    cn = getNodeWithPoint(p1);
                    cn.addPoint(p2);
                }
                //if p2 corresponds to node but p1 doesn't, then add p1 to corresponding node
                else if(nodesContainPoint(p2) && !nodesContainPoint(p1)) {
                    cn = getNodeWithPoint(p2);
                    cn.addPoint(p1);
                }
                //if p1 and p2 correspond to unique nodes, merge nodes
                else if(nodesContainPoint(p2) && nodesContainPoint(p1) && !getNodeWithPoint(p1).equals(getNodeWithPoint(p2))) {
                    CircuitNode n1 = getNodeWithPoint(p1);
                    CircuitNode n2 = getNodeWithPoint(p2);
                    cn = mergeNodes(n1, n2);
                }
                //if p1 and p2 do not correspond to any nodes, create new node corresponding to both points
                else if(!nodesContainPoint(p1) && !nodesContainPoint(p2)){
                    cn = new CircuitNode();
                    cn.addPoint(p1);
                    cn.addPoint(p2);
                    nodes.add(cn);
                }
                //if p1 and p2 correspond to same node, no need to do anything
                else{
                    cn = getNodeWithPoint(p1);
                }

                //Both ends of the wire are connected to the same node
                e.setNode(0, cn);
                e.setNode(1, cn);

            }
            //Handle node allocation for all circuit elements other than wires...
            else{
                for(int i = 0; i < e.getNumPoints(); i++){
                    SimplePoint p = e.getPoint(i);
                    CircuitNode cn;
                    if(nodesContainPoint(p)){
                        cn = getNodeWithPoint(p);
                        e.setNode(i, cn);
                    }
                    else{
                        cn = new CircuitNode();
                        cn.addPoint(p);
                        e.setNode(i, cn);
                        nodes.add(cn);
                    }

                }
            }

        }

    }

    private boolean nodesContainPoint(SimplePoint p){
        for(CircuitNode node: nodes){
            if(node.correspondsToPoint(p))
                return true;
        }
        return false;
    }

    /**
     *
     * @param p
     * @return node corresponding to point, or null if no nodes correspond to point
     */
    private CircuitNode getNodeWithPoint(SimplePoint p){
        for(CircuitNode node: nodes){
            if(node.correspondsToPoint(p))
                return node;
        }
        return null;
    }

    private CircuitNode mergeNodes(CircuitNode n1, CircuitNode n2){
        //Add all the points corresponding to n2 to n1
        for(SimplePoint p: n2.getPoints()){
            n1.addPoint(p);
        }
        //Update all elements pointing to n2 to point to n1
        for(CircuitElm e: elements){
            int index = e.indexOfNode(n2);
            if(index > -1){
                e.setNode(index, n1);
            }
        }
        //Delete n2
        nodes.remove(n2);
        return n1;
    }

    /**
     * Replaces given element, "oldElm", with new element, "newElm".
     * New element has same coordinates as old element.
     * @param oldElm element to be replaced
     * @param newElm element that will replace old element
     *               requires: element-specific values already intialized in newElm
     */
    public void editElement(CircuitElm oldElm, CircuitElm newElm){
        //TODO: implement this method
    }

    /**
     * Modifies specified circuit element's value
     * @param elm
     * @param value
     */
    public void editElementValue(CircuitElm elm, double value){
        //TODO: implement this method

    }

    /**
     * Returns voltage difference across element
     * TODO: Do we return relative voltage for wire element? It might be useful to return voltage of nodes?
     * @param elm
     * @return
     */
    public double getVoltageDiff(CircuitElm elm){
        //TODO: implement this method

        return 0;
    }

    /**
     * Returns current running through element
     * @param elm
     * @return
     */
    public double getCurrent(CircuitElm elm){
        //TODO: implement this method

        return 0;
    }

    public List<CircuitNode> getNodes(){
        return Collections.unmodifiableList(nodes);
    }

    public List<CircuitElm> getElements(){
        return Collections.unmodifiableList(elements);
    }
}
