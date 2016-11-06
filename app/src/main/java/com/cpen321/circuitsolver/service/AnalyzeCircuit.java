package com.cpen321.circuitsolver.service;

import com.cpen321.circuitsolver.model.components.CircuitElm;

/**
 * Created by jen on 2016-11-04.
 */

public interface AnalyzeCircuit {
    /**
     * Returns voltage difference across element
     * TODO: Do we return relative voltage for wire element? It might be useful to return voltage of nodes?
     * @param elm
     * @return
     */
    public double getVoltageDiff(CircuitElm elm);

    /**
     * Returns current running through element
     * @param elm
     * @return
     */
    public double getCurrent(CircuitElm elm);
}
