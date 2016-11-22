package com.cpen321.circuitsolver.opencv.opencvModel;

/**Class that modelizes the components
 * Created by Simon on 24.10.2016.
 */

public class Component extends Element {
    protected String type;
    public Component(float x, float y, String type){
        super(x,y);
        this.type = type;
    }

    public String getType(){
        return type;
    }

}
