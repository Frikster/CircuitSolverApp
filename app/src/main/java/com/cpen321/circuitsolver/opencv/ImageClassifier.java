package com.cpen321.circuitsolver.opencv;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.util.Log;

import com.cpen321.circuitsolver.util.Constants;

import org.tensorflow.demo.Classifier;
import org.tensorflow.demo.TensorFlowImageClassifier;

import java.io.IOException;
import java.util.List;

public class ImageClassifier {
    private TensorFlowImageClassifier classifier;

    public ImageClassifier(AssetManager manager) {
        // Main class that identifies images using the tensorflow pre-trained model
        // after being initialized using the constants found in the Constants.java
        // class, you can pass in a bitmap and it will return which component it thinks it is

        this.classifier = new TensorFlowImageClassifier();
        try {
            this.classifier.initializeTensorFlow(manager,
                    Constants.MODEL_FILE,
                    Constants.LABEL_FILE,
                    Constants.NUM_CLASSES,
                    Constants.INPUT_SIZE,
                    Constants.IMAGE_MEAN,
                    Constants.IMAGE_STD,
                    Constants.INPUT_NAME,
                    Constants.OUTPUT_NAME);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // raw bitmap to component type
    private String classifyComponent(Bitmap bitmap) {
        if (this.classifier == null)
            return null;
        if (bitmap == null)
            return null;

        List<Classifier.Recognition> possibilities = this.classifier.recognizeImage(bitmap);

        try { // just in case image isn't recognized at all,
            // which shouldn't happen. but just in case.
            Classifier.Recognition first = possibilities.get(0);
            switch (first.getTitle()) {
                case Constants.TF_RESISTOR:
                    return Constants.RESISTOR;
                case Constants.TF_VOLTAGE_SOURCE:
                    return Constants.DC_VOLTAGE;
                case Constants.TF_CURRENT_SOURCE:
                    return Constants.DC_VOLTAGE;
                case Constants.TF_CAPACITOR:
                    return Constants.CAPACITOR;
                case Constants.TF_INDUCTOR:
                    return Constants.INDUCTOR;
                default:
                    return Constants.WIRE;
            }
        } catch (IndexOutOfBoundsException ex) {
            return "";
        }
    }

    // opencv passes in the snippet and the component location
    // then here a component is made and returned with the determined type
    // and the correct location
    public Component infoToComponent(Bitmap bitmap, double[] component) {
        String tmp = this.classifyComponent(bitmap);
        Log.i("tf", "recognized as : " + tmp);

        return new Component(component[0], component[1], tmp);
    }
}
