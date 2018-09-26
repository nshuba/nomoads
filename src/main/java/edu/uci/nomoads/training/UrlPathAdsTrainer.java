/*
 *  This file is part of NoMoAds <http://athinagroup.eng.uci.edu/projects/nomoads/>.
 *  Copyright (C) 2018 Anastasia Shuba, University of California, Irvine.
 *
 *  NoMoAds is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  NoMoAds is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with NoMoAds.  If not, see <http://www.gnu.org/licenses/>.
 */
package edu.uci.nomoads.training;

import org.json.simple.JSONObject;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import edu.uci.nomoads.prediction.AdsPredictor;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Trains on the path component of the URL
 */
class UrlPathAdsTrainer extends Trainer {

    public UrlPathAdsTrainer(ServerUtils serverUtils) {
        super(serverUtils);
    }

    /**
     * Utility method for pre-populating the feature set with words
     * @param fi the feature set
     * @param attributes feature set in Weka format
     * @param startingIdx the current index of the feature set
     * @param wordCount structure containing the word and the number of times it appears (see
     * {@link TrainingData#wordCount})
     * @return the new index of the feature set
     */
    protected int addWordsToFeatureSet(Map<String, Integer> fi, ArrayList<Attribute> attributes,
                                       int startingIdx, Map<String, Integer> wordCount) {
        for (Map.Entry<String, Integer> entry : wordCount.entrySet()) {
            // filter low frequency word
            String currentWord = entry.getKey();
            int currentWordFreq = entry.getValue();
            if (currentWordFreq < DEFAULT_THETA)
                continue;

            Attribute attribute = new Attribute(currentWord);
            attributes.add(attribute);
            fi.put(currentWord, startingIdx);
            startingIdx++;
        }
        return startingIdx;
    }

    /**
     * Creates a double array that contains word-features found at a given data point
     * @param fi structure containing features
     * @param wordsMap words and their frequency at a given data point
     * @param attrSize the attributes size
     * @return double array populated with word-features
     */
    protected double[] prepopulateDataPointWithWords(Map<String, Integer> fi, Map<String, Integer>
            wordsMap, int attrSize) {

        double[] instanceValue = new double[attrSize];
        for (int i = 0; i < attrSize - 1; i++) {
            instanceValue[i] = 0;
        }

        for (Map.Entry<String, Integer> entry : wordsMap.entrySet()) {
            if (fi.containsKey(entry.getKey())) {
                int i = fi.get(entry.getKey());
                int val = entry.getValue();
                instanceValue[i] = val;
            }
        }

        return instanceValue;
    }

    @Override
    public Instances populateArff(Info info, TrainingData trData, int theta) {
        System.out.println("pop arff: " + UrlPathAdsTrainer.class.getSimpleName());

        Map<String, Integer> fi = new HashMap<String, Integer>();
        ArrayList<Attribute> attributes = new ArrayList<Attribute>();
        addWordsToFeatureSet(fi, attributes, 0, trData.wordCount);

        addClassLabels(attributes);

        // Populate Data Points
        Iterator<Map<String, Integer>> all = trData.trainMatrix.iterator();
        int count = 0;
        Instances trainingInstances = new Instances("Rel", attributes, 0);
        trainingInstances.setClassIndex(trainingInstances.numAttributes() - 1);
        while (all.hasNext()) {
            double[] instanceValue = prepopulateDataPointWithWords(fi, all.next(), attributes
                    .size());

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
        String line = getLine(packet);
        byte[] lineBytes = line.getBytes(Charset.forName("UTF-8"));
        ByteBuffer packetBuffer = ByteBuffer.wrap(lineBytes);

        ArrayList<String> features = predictor.getFeatures(packetBuffer);

        return predictor.getInstance(features, domainOS,
                ServerUtils.getIntFromJSONObject(packet, jsonKeyLabel));
    }

    @Override
    public String getLine(JSONObject packet) {
        return packet.get(JsonKeyDef.F_KEY_URI) + " HTTP/1.1\r\n";
    }
}
