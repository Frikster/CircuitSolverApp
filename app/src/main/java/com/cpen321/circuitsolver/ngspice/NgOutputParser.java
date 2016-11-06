package com.cpen321.circuitsolver.ngspice;

import android.util.Log;

import com.cpen321.circuitsolver.model.CircuitNode;
import com.cpen321.circuitsolver.model.components.CircuitElm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Bruce on 2016-11-05.
 */

public class NgOutputParser {

    public static void callNgOutputParser(String input, Map<String, CircuitNode> nodes, Map<String, CircuitElm> elms ) {
        boolean addWord = false;
        boolean isValueNum = false;
        boolean isNode = false;
        String key = null;

        //replace all "\n" with spaces
        String s = input;
        s = s.replaceAll("\n", " ");
        //split everything separated by spaces into new words
        String[] arr = s.split(" ");

        for ( String word : arr) {
            //stop adding words after "No." word
            if(word.equals("No.")) {
                addWord = false;
            }
            //use this word
            if(addWord && !word.equals("") && word != null) {
                //node or element name, not a value
                if (!isValueNum) {
                    Log.v("Name of element or node", word);
                    //next word will be a value number
                    isValueNum = true;
                    //get the key for the Map
                    key = word;
                    //is a branch
                    if(word.length() != 0 && word.charAt(0)!= 'v'){
                        isNode = true;
                    }
                    //is a node
                    else{
                        isNode = false;
                    }
                }
                //is a voltage or current value
                else {
                    Log.v("Value to add", word );
                    //next word is not a value
                    isValueNum = false;
                    //add voltage to node
                    if(isNode){
                        CircuitNode currNode = nodes.get(key);
                        double value = Double.parseDouble(word);
                        if(currNode != null)
                            currNode.setVoltage(value);
                    }
                    //add current to element
                    else {
                        CircuitElm currElm = elms.get(key);
                        if(currElm != null)
                            currElm.setCurrent(Double.parseDouble(word));
                    }
                }
            }
            //Start adding words after word "-------"
            if(word.equals("-------")){
                addWord = true;
            }
        }

    }
}
