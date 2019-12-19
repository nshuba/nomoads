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
package edu.uci.nomoads.prediction;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.uci.nomoads.Util;
import weka.classifiers.trees.J48;
import weka.core.Instance;
import weka.core.SerializationHelper;
import weka.core.SparseInstance;

/**
 * Used to predict on test set
 */
public class AdsPredictor extends Predictor {

    public AdsPredictor(Util util) throws Exception {
        // Passing empty list for known PII here since on the server side, the PII are scrubbed and
        // are separate labels in JSON objects
        super(J48.class.toString().substring(6), util, new ServerDPI(), new ArrayList<String>(0));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadDomainModel(String pathToModel, String domainOS) throws Exception {
        J48 classifier = (J48) SerializationHelper.read(pathToModel);
        domainOSModel.put(domainOS, classifier);
    }

    /**
     * Searches the given packet for any features appearing in the classifier tree
     * @param packet the packet to search
     * @return a list of features found in the packet
     */
    public ArrayList<String> getFeatures(ByteBuffer packet) {
        ArrayList<String> features = new ArrayList<>();
        ArrayList<String> foundStrings = dpiInterface.search(packet, packet.limit());
        final int LOOP_SIZE = 2;
        for (int i = 0; i < foundStrings.size(); i += LOOP_SIZE) {
            String foundStr = foundStrings.get(i);
            features.add(foundStr);
        }

        return features;
    }

    /**
     * Creates an {@link Instance} (based on provided features) that can be used by Weka to predict
     * @param features the features of this instance/data point/packet
     * @param domainOS
     * @param label
     * @return
     */
    public Instance getInstance(List<String> features, String domainOS, int label) {
        Map<String, Integer> fi = getClassifierFeatures(domainOS);

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
        instanceValues[numAttributes - 1] = label;
        return new SparseInstance(1.0, instanceValues);
    }
}
