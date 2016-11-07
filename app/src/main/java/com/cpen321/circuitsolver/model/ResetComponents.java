package com.cpen321.circuitsolver.model;

import com.cpen321.circuitsolver.model.components.CapacitorElm;
import com.cpen321.circuitsolver.model.components.InductorElm;
import com.cpen321.circuitsolver.model.components.ResistorElm;
import com.cpen321.circuitsolver.model.components.VoltageElm;

/**
 * Created by lotus on 06/11/16.
 */

public class ResetComponents {
    /*
     * Resets the static number of elements
     */
    static public void resetNumComponents() {
        CapacitorElm.resetNumElements();
        InductorElm.resetNumElements();
        ResistorElm.resetNumElements();
        VoltageElm.resetNumElements();
        CircuitNode.resetNumNodes();
    }
}
