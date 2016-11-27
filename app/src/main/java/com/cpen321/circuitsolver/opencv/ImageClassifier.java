package com.cpen321.circuitsolver.opencv;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.util.Log;

import com.cpen321.circuitsolver.util.Constants;

import org.tensorflow.demo.Classifier;
import org.tensorflow.demo.TensorFlowImageClassifier;

import java.io.IOException;
import java.util.List;

import static com.cpen321.circuitsolver.util.Constants.RESISTOR;

public class ImageClassifier {
    private TensorFlowImageClassifier classifier;

    public ImageClassifier(TensorFlowImageClassifier imgClassifier) {
        this.classifier = imgClassifier;
    }

    public ImageClassifier(AssetManager manager) {
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
            Log.i("tF", "after initialization");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public String classifyComponent(Bitmap bitmap) {
        if (this.classifier == null)
            return null;
        if (bitmap == null)
            return null;

        List<Classifier.Recognition> possibilities = this.classifier.recognizeImage(bitmap);
        for (Classifier.Recognition possibile : possibilities)
            Log.i("tf", possibile.getTitle());

        Classifier.Recognition first = possibilities.get(0);

        switch (first.getTitle()) {
            case "resistor":
                return Constants.RESISTOR;
            case "voltage source":
                return Constants.DC_VOLTAGE;
            case "current source":
                return Constants.DC_VOLTAGE;
            case "capacitor":
                return Constants.CAPACITOR;
            case "inductor":
                return Constants.INDUCTOR;
            default:
                return Constants.RESISTOR;
        }
    }

    public Component infoToComponent(Bitmap bitmap, double[] component) {
        String tmp = this.classifyComponent(bitmap);
        Log.i("tf", "recognized as : " + tmp);

        return new Component(component[0], component[1], tmp);
    }
}
