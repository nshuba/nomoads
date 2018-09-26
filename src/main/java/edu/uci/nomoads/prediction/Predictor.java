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
package edu.uci.nomoads.prediction;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.uci.nomoads.Util;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

/**
 * Parent class for other predictors (binary, multi-label, etc.)
 */
public abstract class Predictor {
    public static final String GENERAL_CLASSIFIER = "general_android";

    protected final Map<String, Object> domainOSModel;
    protected final Map<String, Map<String, Integer>> classifierFeatures;
    protected final Map<String, Instances> domainOSStruct;

    /** Feature set - the set of strings we should search for with DPI */
    protected final Set<String> treeLabels;

    /** Used to search for known PII and features that are used by the classifiers */
    protected final DPIInterface dpiInterface;

    /** A set of known PII. These can be used as features when doing non-PII classification, or
     * these can simply be PII that do not require classification and can be found via
     * string matching instead. Defaults to an empty set. */
    protected Set<String> knownPII;

    /**
     * Constructor:
     * Loads all models for the given class name, from files into local variables
     * to be used during prediction
     * @param className the name of the model to load (e.g. J48.class.toString().substring(6))
     * @param util utility object
     * @param dpiInterface which implementation of {@link DPIInterface} is to be used for searching
     * packets for features
     * @param knownPII set of PII that are known in advance (see {@link #knownPII})
     */
    public Predictor(String className, Util util, DPIInterface dpiInterface, Collection<String>
            knownPII) {
        this.dpiInterface = dpiInterface;

        domainOSModel = new HashMap<String, Object>();
        classifierFeatures = new HashMap<String, Map<String, Integer>>();
        domainOSStruct = new HashMap<String, Instances>();
        treeLabels = new HashSet<>();

        try {
            File modelFolder = new File(util.getModelDir());
            File[] models = modelFolder.listFiles();
            if (models == null)
                return; // TODO: 239 Print out warning or throw exception

            JSONParser parser = new JSONParser();
            JSONObject jsonTreeLabels = (JSONObject) parser.parse(new FileReader(
                    util.getTreeLabelsFile()));
            for (int i = 0; i < models.length; i++) {
                String fn = models[i].getName();
                if (!fn.endsWith(className + ".model"))
                    continue;
                String domainOS = fn.substring(0,
                        fn.length() - className.length() - ".model".length() - 1);
                loadDomainModel(util.getModelDir() + fn, domainOS);


                ArffLoader loader = new ArffLoader();
                String arffStructureFile = util.getFeaturesDir() + domainOS + ".arff";
                File af = new File(arffStructureFile);
                if (!af.exists())
                    continue;

                loader.setFile(new File(arffStructureFile));
                Instances structure;
                try {
                    structure = loader.getStructure();
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(-1);
                    break; // Make compiler happy
                }
                structure.setClassIndex(structure.numAttributes() - 1);
                domainOSStruct.put(domainOS, structure);

                // Load tree labels
                JSONObject treeLabels = (JSONObject) jsonTreeLabels.get(domainOS);
                if (treeLabels != null && treeLabels.size() > 0) {
                    for (Object k : treeLabels.keySet()) {
                        this.treeLabels.add(treeLabels.get(k) + "");
                    }
                    System.out.println("number of loaded tree nodes: " + treeLabels.size());
                }

                // Load features
                Map<String, Integer> fi = new HashMap<String, Integer>();
                for (int j = 0; j < structure.numAttributes(); j++) {
                    fi.put(structure.attribute(j).name(), j);
                }
                classifierFeatures.put(domainOS, fi);
            }
        } catch (Exception e) {
            // TODO: 239
            e.printStackTrace();
        }

        // Prepare strings to search for
        addKnownPII(knownPII);
    }

    /**
     * Adds {@link #knownPII} to the set of strings to be searched by {@link #dpiInterface} in
     * addition to the feature set.
     * @param knownPII the set of pre-defined/known PII
     */
    public synchronized void addKnownPII(Collection<String> knownPII) {
        String[] searchStrings = new String[treeLabels.size() + knownPII.size()];
        // Add tree labels to strings to search for
        int i = 0;
        for (String treeLabel : treeLabels) {
            searchStrings[i] = treeLabel;
            i++;
        }

        // Add known PII
        this.knownPII = new HashSet<>(knownPII.size());
        for (String pii : knownPII) {
            this.knownPII.add(pii);
            searchStrings[i] = pii;
            i++;
        }
        dpiInterface.init(searchStrings);
    }

    /**
     * Convenience method for retrieving features associated with a particular classifier
     * @param classifier the classifier whose features to retrieve
     * @return features associated with a particular classifier if a model for it exists;
     * otherwise general classifier features are returned, if one exists; otherwise an empty
     * {@link HashMap} is returned.
     */
    public Map<String, Integer> getClassifierFeatures(String classifier) {
        if (classifierFeatures.containsKey(classifier))
            return classifierFeatures.get(classifier);

        if (classifierFeatures.containsKey(GENERAL_CLASSIFIER))
            return classifierFeatures.get(GENERAL_CLASSIFIER);

        return new HashMap<>(0);
    }

    /**
     * Convenience method for retrieving a specific classifier model
     * @param classifierName the name of the classifier to retrieve
     * @return the model associated with the specified classifier (usually a
     * {@link weka.classifiers.Classifier} or a {@link mulan.classifier.MultiLabelLearnerBase}.
     * If there is no model for the given classifier, the general classifier model is returned.
     * If there is no general classifier model, {@code null} is returned.
     */
    public Object getClassifierModel(String classifierName) {
        if (domainOSModel.containsKey(classifierName))
            return domainOSModel.get(classifierName);

        return domainOSModel.get(Predictor.GENERAL_CLASSIFIER);
    }

    /**
     * Convenience method for retrieving {@link Instances} belonging to a specific classifier
     * @param classifierName the name of the classifier whose structure to retrieve
     * @return the structure associated with the specified classifier.
     * If there is no structure for the given classifier, the general classifier structure is
     * returned.
     * If there is no general classifier structure, {@code null} is returned.
     */
    public Instances getClassifierInstances(String classifierName) {
        if (domainOSStruct.containsKey(classifierName))
            return domainOSStruct.get(classifierName);

        return domainOSStruct.get(Predictor.GENERAL_CLASSIFIER);
    }

    /**
     * Loads the given model based on the type of predictor/classifier is selected upon
     * initialization
     * @param pathToModel path to the model file
     * @param domainOS the domain (TLD) and operating system to which this model should be mapped
     *                 for future uses (see {@link #domainOSModel})
     * @throws Exception if an error occurred while trying to read the model
     */
    protected abstract void loadDomainModel(String pathToModel, String domainOS) throws Exception;
}
