package com.cpen321.circuitsolver.model;

import com.cpen321.circuitsolver.model.components.CapacitorElm;
import com.cpen321.circuitsolver.model.components.CircuitElm;
import com.cpen321.circuitsolver.model.components.InductorElm;
import com.cpen321.circuitsolver.model.components.ResistorElm;
import com.cpen321.circuitsolver.model.components.VoltageElm;
import com.cpen321.circuitsolver.model.components.WireElm;
import com.cpen321.circuitsolver.service.CircuitDefParser;
import com.cpen321.circuitsolver.util.Constants;

/**
 * Created by jen on 2016-11-05.
 */

public class CircuitElmFactory {

    public CircuitElm makeElm(String type, SimplePoint p1, SimplePoint p2, double value){
        CircuitElm elm;
        switch(type){
            case Constants.RESISTOR:
                elm = new ResistorElm(p1, p2, value);
                break;
            case Constants.CAPACITOR:
                elm = new CapacitorElm(p1,p2, value);
                break;
            case Constants.INDUCTOR:
                elm = new InductorElm(p1,p2, value);
                break;
            case Constants.DC_VOLTAGE:
                elm = new VoltageElm(p1, p2, value);
                break;
            case Constants.WIRE:
                elm = new WireElm(p1,p2);
                break;
            default: elm = null;
        }

        return elm;
    }

    public CircuitElm makeElm(SimplePoint p1, SimplePoint p2){
        return new WireElm(p1, p2);
    }


}
