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

import org.json.simple.JSONArray;
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

/**
 * Trains on URL + Headers + PII
 */
class UrlHeadersPiiAdsTrainer extends UrlHeadersAdsTrainer {

    public UrlHeadersPiiAdsTrainer(ServerUtils serverUtils) {
        super(serverUtils);
    }

    @Override
    public Instances populateArff(Info info, TrainingData trData, int theta) {
        System.out.println("populateArff: " + UrlHeadersPiiAdsTrainer.class
                .getSimpleName());

        Map<String, Integer> fi = new HashMap<String, Integer>();
        ArrayList<Attribute> attributes = new ArrayList<Attribute>();
        int index = addWordsToFeatureSet(fi, attributes, 0, trData.wordCount);

        // Now add all PII labels as possible features
        for (String piiLabel : piisSet) {
            Attribute attribute = new Attribute(piiLabel);
            attributes.add(attribute);
            fi.put(piiLabel, index);
            index++;
        }

        addClassLabels(attributes);

        // Populate Data Points
        Iterator<Map<String, Integer>> all = trData.trainMatrix.iterator();
        int count = 0;
        Instances trainingInstances = new Instances("Rel", attributes, 0);
        trainingInstances.setClassIndex(trainingInstances.numAttributes() - 1);
        while (all.hasNext()) {
            double[] instanceValue = prepopulateDataPointWithWords(fi, all.next(), attributes
                    .size());

            // Add PII attributes
            for (Object piiObj : trData.piiLabels.get(count)) {
                instanceValue[fi.get(piiObj)]++; // Count occurrences of pii
            }

            Instance data = finalizeInstance(trData.adLabels.get(count), instanceValue, attributes);
            trainingInstances.add(data);
            count++;
        }

        saveArff(info.domainOS, trainingInstances);
        return trainingInstances;
    }

    protected Instance convertObjectToInstance(JSONObject packet, AdsPredictor predictor, String
            domainOS) {
/*        System.out.println("convertObjectToInstance: " + UrlHeadersPiiAdsTrainer.class
                .getSimpleName());*/
        String line = getLine(packet);
        byte[] lineBytes = line.getBytes(Charset.forName("UTF-8"));
        ByteBuffer packetBuffer = ByteBuffer.wrap(lineBytes);

        ArrayList<String> features = predictor.getFeatures(packetBuffer);

        JSONArray pii = (JSONArray) packet.get(JsonKeyDef.F_KEY_PII_TYPES);
        for (Object piiObj : pii)
            features.add((String) piiObj);

        return predictor.getInstance(features, domainOS,
                ServerUtils.getIntFromJSONObject(packet, jsonKeyLabel));
    }
}
