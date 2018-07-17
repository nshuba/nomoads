package edu.uci.nomoads.training;

import org.json.simple.JSONObject;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

import edu.uci.nomoads.Util;
import edu.uci.nomoads.prediction.AdsPredictor;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SparseInstance;

/**
 * Trains based on destination domain
 */
class DomainAdsTrainer extends Trainer {

    private ArrayList<String> domainsList;

    public DomainAdsTrainer(ServerUtils serverUtils) { super(serverUtils); }

    @Override
    public Instances populateArff(Info info, TrainingData trainingData, int theta) {
        System.out.println("popArff" + getClass().getSimpleName());

        // Get all possible domains first
        HashSet<String> domains = new HashSet<>(225);
        for (Object k : trainingData.trFlows.keySet()) {
            JSONObject packet = (JSONObject) trainingData.trFlows.get(k);
            String domain = ServerUtils.getStringFromJSONObject(packet, JsonKeyDef.F_KEY_DOMAIN);
            domains.add(domain);
        }

        // Convert to list
        domainsList = new ArrayList<>(domains);

        // Populate Features
        ArrayList<Attribute> attributes = new ArrayList<Attribute>();
        attributes.add(new Attribute(JsonKeyDef.F_KEY_DOMAIN, domainsList));

        addClassLabels(attributes);

        Instances trainingInstances = new Instances("Rel", attributes, 0);
        trainingInstances.setClassIndex(trainingInstances.numAttributes() - 1);
        for (Object k : trainingData.trFlows.keySet()) {
            JSONObject packet = (JSONObject) trainingData.trFlows.get(k);
            trainingInstances.add(convertObjectToInstance(packet, attributes.size()));
        }

        saveArff(trainingData.mem.info.domainOS, trainingInstances);
        trainingData.trainingInstances = trainingInstances;

        return trainingInstances;
    }

    private Instance convertObjectToInstance(JSONObject packet, int attrSize) {
        double[] instanceValue = new double[attrSize];
        String domain = ServerUtils.getStringFromJSONObject(packet, JsonKeyDef.F_KEY_DOMAIN);

        instanceValue[0] = domainsList.indexOf(domain);

        // Last attribute is the label
        int adLabel = ServerUtils.getIntFromJSONObject(packet, jsonKeyLabel);
        instanceValue[1] = adLabel;

        return new SparseInstance(1.0, instanceValue);
    }

    protected Instance convertObjectToInstance(JSONObject packet, AdsPredictor predictor, String
            domainOS) {
        //System.out.println("convertObjectToInstance: " + getClass().getSimpleName());

        Map<String, Integer> fi = predictor.getClassifierFeatures(domainOS);
        return convertObjectToInstance(packet, fi.size());
    }

    /**
     * No packet contents are used by this Trainer
     * @param packet the packet object
     * @return empty String
     */
    @Override
    public String getLine(JSONObject packet) {
        return "";
    }
}
