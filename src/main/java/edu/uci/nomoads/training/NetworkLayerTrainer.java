/*
 * This file is part of NoMoAds <http://athinagroup.eng.uci.edu/projects/nomoads/>.
 * Copyright (C) 2018, 2019 Anastasia Shuba
 *
 * NoMoAds is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NoMoAds is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NoMoAds.  If not, see <http://www.gnu.org/licenses/>.
 */
package edu.uci.nomoads.training;

import org.json.simple.JSONObject;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import edu.uci.nomoads.Util;
import edu.uci.nomoads.prediction.AdsPredictor;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SparseInstance;

/**
 * Trains based on destination IP and port numbers
 */
class NetworkLayerTrainer extends Trainer {

    public NetworkLayerTrainer(ServerUtils serverUtils) { super(serverUtils); }

    @Override
    public Instances populateArff(Info info, TrainingData trainingData, int theta) {
        System.out.println("popArff" + NetworkLayerTrainer.class.getSimpleName());

        // Populate Features
        ArrayList<Attribute> attributes = new ArrayList<Attribute>();

        // TCP/IP features:
        attributes.add(new Attribute(JsonKeyDef.DST_PORT)); // numeric feature

        // Use numeric feature for IP because String features are not supported by C45 trees. See:
        // https://stackoverflow.com/questions/7932888/weka-j48-classifier-cannot-handle-numeric-class
        attributes.add(new Attribute(JsonKeyDef.DST_IP));

        addClassLabels(attributes);

        Instances trainingInstances = new Instances("Rel", attributes, 0);
        trainingInstances.setClassIndex(trainingInstances.numAttributes() - 1);
        for (Object k : trainingData.trFlows.keySet()) {
            JSONObject packet = (JSONObject) trainingData.trFlows.get(k);

            Instance data = convertObjectToInstance(packet, attributes.size());
            trainingInstances.add(data);
        }

        saveArff(trainingData.mem.info.domainOS, trainingInstances);
        trainingData.trainingInstances = trainingInstances;

        return trainingInstances;
    }

    private Instance convertObjectToInstance(JSONObject packet, int attrSize) {
        double[] instanceValue = new double[attrSize];
        int port = ServerUtils.getIntFromJSONObject(packet, JsonKeyDef.DST_PORT);
        String ip = ServerUtils.getStringFromJSONObject(packet, JsonKeyDef.DST_IP);

        // Convert IP to int:
        String[] ipArray = ip.split("\\.");
        int ipInt = ((Integer.parseInt(ipArray[0]) << 24 & 0xFF000000) |
                (Integer.parseInt(ipArray[1]) << 16 & 0x00FF0000) |
                (Integer.parseInt(ipArray[2]) << 8 & 0x0000FF00) |
                (Integer.parseInt(ipArray[3]) & 0x000000FF));

        instanceValue[0] = port;
        instanceValue[1] = ipInt;

        // Last attribute is the label
        int adLabel = ServerUtils.getIntFromJSONObject(packet, jsonKeyLabel);
        instanceValue[2] = adLabel;

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
