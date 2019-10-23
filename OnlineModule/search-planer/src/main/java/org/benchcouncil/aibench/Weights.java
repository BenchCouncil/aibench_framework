package org.benchcouncil.aibench;

import java.io.Serializable;
import java.util.List;

public class Weights implements Serializable {

    private List<List<Double>> predictions;

    public List<List<Double>> getPredictions() {
        return predictions;
    }

    public void setPredictions(List<List<Double>> predictions) {
        this.predictions = predictions;
    }

    List<Double> getWeights() {
        return predictions.get(0);
    }
}
