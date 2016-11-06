package com.cpen321.circuitsolver.service;

import com.cpen321.circuitsolver.model.components.CircuitElm;

/**
 * Created by jen on 2016-11-04.
 */

public interface EditCircuit {

    /**
     * Replaces given element, "oldElm", with new element, "newElm".
     * New element has same coordinates as old element.
     * @param oldElm element to be replaced
     * @param newElm element that will replace old element
     *               requires: element-specific values already intialized in newElm
     */
    public void editElement(CircuitElm oldElm, CircuitElm newElm);

    /**
     * Modifies specified circuit element's value
     * @param elm
     * @param value
     */
    public void editElementValue(CircuitElm elm, double value);
}
