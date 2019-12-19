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
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import edu.uci.nomoads.Util;
import edu.uci.nomoads.prediction.AdsPredictor;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SparseInstance;

/**
 * Trains on URL + Headers + Apps
 */
class UrlHeadersAppsAdsTrainer extends UrlHeadersAdsTrainer {

    protected ArrayList<String> pkgNamesList;

    public UrlHeadersAppsAdsTrainer(ServerUtils serverUtils) { super(serverUtils); }

    @Override
    public Instances populateArff(Info info, TrainingData trData, int theta) {
        System.out.println("pop arff: " + UrlHeadersAppsAdsTrainer.class.getSimpleName());

        Map<String, Integer> fi = new HashMap<String, Integer>();
        ArrayList<Attribute> attributes = new ArrayList<Attribute>();
        int index = addWordsToFeatureSet(fi, attributes, 0, trData.wordCount);

        // Convert all possible apps to list
        pkgNamesList = new ArrayList<>(pkgNames);
        attributes.add(new Attribute(JsonKeyDef.F_KEY_PKG_NAME, pkgNamesList));
        fi.put(JsonKeyDef.F_KEY_PKG_NAME, index);

        addClassLabels(attributes);

        // Populate Data Points
        Iterator<Map<String, Integer>> all = trData.trainMatrix.iterator();
        int count = 0;
        Instances trainingInstances = new Instances("Rel", attributes, 0);
        trainingInstances.setClassIndex(trainingInstances.numAttributes() - 1);
        while (all.hasNext()) {
            double[] instanceValue = prepopulateDataPointWithWords(fi, all.next(), attributes
                    .size());

            // Add package name attribute
            instanceValue[fi.get(JsonKeyDef.F_KEY_PKG_NAME)] =
                    pkgNamesList.indexOf(trData.pkgNames.get(count));

            Instance data = finalizeInstance(trData.adLabels.get(count), instanceValue, attributes);
            trainingInstances.add(data);
            count++;
        }

        saveArff(info.domainOS, trainingInstances);
        return trainingInstances;
    }

    @Override
    protected Instance convertObjectToInstance(JSONObject packet, AdsPredictor predictor, String
            domainOS) {
        System.out.println("convertObjectToInstance: " + UrlHeadersAppsAdsTrainer.class
                .getSimpleName());
        String line = getLine(packet);
        byte[] lineBytes = line.getBytes(Charset.forName("UTF-8"));
        ByteBuffer packetBuffer = ByteBuffer.wrap(lineBytes);

        ArrayList<String> features = predictor.getFeatures(packetBuffer);


/*        JSONArray pii = (JSONArray) packet.get(JsonKeyDef.F_KEY_PII_TYPES);
        for (Object piiObj : pii)
            features.add((String) piiObj);*/


        Map<String, Integer> fi = predictor.getClassifierFeatures(domainOS);
        int numAttributes = fi.size();
        double instanceValues[] = new double[numAttributes];
        for (int i = 0; i < numAttributes; i++) {
            instanceValues[i] = 0;
        }

        for (String feature : features) {
            if (fi.containsKey(feature)) {
                instanceValues[fi.get(feature)]++;
            }
        }

        instanceValues[fi.get(JsonKeyDef.F_KEY_PKG_NAME)] =
                pkgNamesList.indexOf(packet.get(JsonKeyDef.F_KEY_PKG_NAME));

        instanceValues[numAttributes - 1] = ServerUtils.getIntFromJSONObject(packet, jsonKeyLabel);
        return new SparseInstance(1.0, instanceValues);
    }
}
