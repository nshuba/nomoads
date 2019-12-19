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
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import edu.uci.nomoads.prediction.AdsPredictor;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;

class DataSplitter {
    static final String GENERAL = "general";
    static final String PER_APP = "package_name";

    static final Set<String> SUPPORTED_TYPES = new HashSet<>(Arrays.asList(GENERAL, PER_APP));

    /** Trainer to use for training */
    public final Trainer trainer;

    /** The number of packet-based cross-validation folds to do */
    protected static final int NUM_CROSS_FOLDS = 5;

    private JSONParser parser = new JSONParser();
    private String currentSplit = "full_set";
    private final ServerUtils mServerUtils;
    private final Config config;

    /** Label to use when writing prediction results */
    private final String predictedLabel;

    /**
     * Prepares class for running experiments
     * @param config configuration for the experiment
     */
    public DataSplitter(Config config) {
        mServerUtils = ServerUtils.getInstance(config);
        trainer = config.getSelectedTrainer();
        predictedLabel = "predicted_" + config.getDataSplit();
        this.config = config;
    }

    /**
     * Runs experiment based on the configuration provided in the constructor
     */
    void runExperiment() {
        try {
            String infoFilePath = mServerUtils.getTrainingIndex();
            Object obj = parser.parse(new FileReader(infoFilePath));
            JSONObject domain_os_reports = (JSONObject) obj;
            Set<Object> allFilesSet = domain_os_reports.keySet();

            // For each file, split data for stratified cross-validation
            for(Object fileObj : allFilesSet) {
                String selectedFile = (String) fileObj;
                JSONObject info = (JSONObject) domain_os_reports.get(selectedFile);
                Info inf = ServerUtils.approveFile(info, mServerUtils.getDataSplit());
                if (inf == null) {
                    System.out.println("WARNING! Null info for " + selectedFile + ". Stopping.");
                    return;
                }

                if (inf.initNumPos < 10 || inf.initNumNeg < 10) {
                    System.out.println("\tSkipping - not enough samples");
                    continue;
                }

                JSONObject trFlows = (JSONObject) parser.parse(new FileReader(
                        mServerUtils.getTrainingDir() + selectedFile));
                if (config.isCrossValidationEnabled())
                    runCrossValidation(selectedFile, trFlows, inf);
                else
                    buildOnly(selectedFile, trFlows, inf);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Performs stratified cross-validation on the provided training data
     * @param selectedFile name of the file to which the training data belongs to
     * @param trFlows training data to split
     * @param trInfo information about the training data
     * @throws Exception
     */
    private void runCrossValidation(String selectedFile, JSONObject trFlows, Info trInfo) throws
            Exception {
        ArrayList<Set<String>> bins = splitData(trInfo, trFlows);

        // Go through all bins, selecting each one to be in the test set once
        JSONObject wekaResults = new JSONObject();
        JSONObject testResults = new JSONObject();
        for (int i = 0; i < NUM_CROSS_FOLDS; i++) {
            Set<String> testSet = bins.get(i);
            Set<String> trainSet = new HashSet<>();
            for (int j = 0; j < NUM_CROSS_FOLDS; j++) {
                if (j == i)
                    continue; // Don't add the test set

                trainSet.addAll(bins.get(j));
            }

            currentSplit = selectedFile.split("\\.json")[0] + "_s" + i;
            runSplit(trFlows, testSet, trainSet, wekaResults, testResults);
        }

        // Save results
        DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        String resultsFilePath = mServerUtils.getLogDir() + dateFormat.format(new Date()) +
                selectedFile + "_eval.json";
        ServerUtils.overwriteFile(resultsFilePath, wekaResults.toJSONString());

        resultsFilePath = mServerUtils.getResultsDir() + selectedFile;
        ServerUtils.overwriteFile(resultsFilePath, testResults.toJSONString());
    }

    private void buildOnly(String selectedFile, JSONObject trainingData, Info trainingInfo) {
        MetaEvaluationMeasures mem = new MetaEvaluationMeasures(trainingInfo);
        trainer.trainOneDomain(trainingData, mem);

        // Save results
        DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        String resultsFilePath = mServerUtils.getLogDir() + dateFormat.format(new Date()) +
                selectedFile + "_full_build.json";
        ServerUtils.overwriteFile(resultsFilePath, mem.getJSONobj().toJSONString());
    }

    /**
     * Splits provided data into {@link #NUM_CROSS_FOLDS} bins, for cross-validation
     * @param inf info object about the data
     * @param trFlows the data to split
     * @return {@link #NUM_CROSS_FOLDS} bins, where each bin contains UUIDs of selected data points
     */
    public ArrayList<Set<String>> splitData(Info inf, JSONObject trFlows) {
        Info trainingInfo = new Info();
        trainingInfo.domainOS = inf.domain;
        currentSplit = inf.domain;
        trainingInfo.initNumPos = inf.initNumPos;
        trainingInfo.initNumNeg = inf.initNumNeg;
        trainingInfo.initNumTotal = inf.initNumTotal;

        // First, separate the data into positive and negative arrays of UUIDs
        ArrayList<String> posIDs = new ArrayList<>(inf.initNumPos);
        ArrayList<String> negIDs = new ArrayList<>(inf.initNumNeg);
        for (Object k : trFlows.keySet()) {
            String uuid = (String) k;
            JSONObject pkt = (JSONObject) trFlows.get(k);
            if (ServerUtils.getIntFromJSONObject(pkt, mServerUtils.label) == Trainer.LABEL_POSITIVE)
                posIDs.add(uuid);
            else
                negIDs.add(uuid);
        }

        // Sanity check
        if (posIDs.size() != inf.initNumPos || negIDs.size() != inf.initNumNeg) {
            System.err.println("Could not split data");
            return null;
        }

        // Find the minimum number of positive/negative samples to be placed in each bin
        int minPos = inf.initNumPos / NUM_CROSS_FOLDS;
        int minNeg = inf.initNumNeg / NUM_CROSS_FOLDS;

        // Find how many remaining samples are in each sample type
        int remPos = inf.initNumPos % NUM_CROSS_FOLDS;
        int remNeg = inf.initNumNeg % NUM_CROSS_FOLDS;

        // First, fill each bin with minimum number of samples (to create even splits)
        ArrayList<Set<String>> bins = new ArrayList<>(NUM_CROSS_FOLDS);
        Random r = new Random();
        for (int i = 0; i < NUM_CROSS_FOLDS; i++) {
            // Largest size of a bin: min number of positive/negatives and +2 for remainders
            int maxSize = minPos + minNeg + 2;

            // Prepare bin, set initially capacity to largest possible bin
            Set<String> bin = new HashSet<>(maxSize);

            // Fill the positive slots in the bin
            for(int j = 0; j < minPos; j++) {
                String selectedID = posIDs.get(r.nextInt(posIDs.size()));
                posIDs.remove(selectedID); // remove to avoid future selection
                bin.add(selectedID); // place into bin
            }

            // Fill the negative slots in the bin
            for(int j = 0; j < minNeg; j++) {
                String selectedID = negIDs.get(r.nextInt(negIDs.size()));
                negIDs.remove(selectedID);
                bin.add(selectedID);
            }

            bins.add(bin);
        }

        // At this point, each bin has the same number of positive and negative samples
        // Now we add the remaining samples, one to each bin
        for (int i = 0; i < remPos; i++) {
            String selectedID = posIDs.get(r.nextInt(posIDs.size()));
            posIDs.remove(selectedID); // remove to avoid future selection
            bins.get(i).add(selectedID); // place into current bin
        }

        for (int i = 0; i < remNeg; i++) {
            String selectedID = negIDs.get(r.nextInt(negIDs.size()));
            negIDs.remove(selectedID); // remove to avoid future selection
            bins.get(i).add(selectedID); // place into current bin
        }

        return bins;
    }

    /**
     * Performs training and evaluation on the provided data
     * @param trFlows full dataset
     * @param testSet list of IDs in the dataset that are to be the test data set
     * @param trainSet list of IDs in the dataset that are to be the training data set
     * @param wekaResults JSON object to which to write ML stats computed by Weka
     * @param testData JSON object to which to write the test data to, along with the prediciton
     * value for each data point
     * @throws Exception
     */
    private void runSplit(JSONObject trFlows, Set<String> testSet, Set<String> trainSet,
                          JSONObject wekaResults, JSONObject testData) throws Exception {
        Info trainingInfo = new Info();
        trainingInfo.domainOS = currentSplit;
        System.out.println("\tTraining data points " + trainSet.size());
        JSONObject merged = new JSONObject();
        for (String selectedID : trainSet) {
            JSONObject entry = (JSONObject) trFlows.get(selectedID);
            merged.put(selectedID, entry);

            // Keep count of sample types - we will use them later for calculating accuracy and etc.
            if (ServerUtils.getIntFromJSONObject(entry, mServerUtils.label) == Trainer.LABEL_POSITIVE)
                trainingInfo.initNumPos++;
            else
                trainingInfo.initNumNeg++;
            trainingInfo.initNumTotal++;
        }

        MetaEvaluationMeasures mem = new MetaEvaluationMeasures(trainingInfo);
        Instances trInstances = trainer.trainOneDomain(merged, mem);

        // As a sanity check, do a prediction on the training set to compare to cross-evaluation
        // results above and make sure our DPI-based technique works correctly
        AdsPredictor adsPredictor = new AdsPredictor(mServerUtils);
        Classifier classifier = (Classifier) adsPredictor.getClassifierModel(currentSplit);
        mem.doEvaluation(classifier, trInstances, 0);
        wekaResults.put("training-" + currentSplit, mem.getJSONobj());

        // Get the structure of the classifier so we can use it for prediction on the test set
        Instances struct = adsPredictor.getClassifierInstances(currentSplit);
        Instances testingInstances = new Instances(struct);
        testingInstances.setClassIndex(testingInstances.numAttributes() - 1);

        // Predict on the test set
        Info testingInfo = new Info();
        System.out.println("testing: " + testSet.size());
        for (String selectedID : testSet) {
            JSONObject packet = (JSONObject) trFlows.get(selectedID);
            if (ServerUtils.getIntFromJSONObject(packet, mServerUtils.label) ==
                    Trainer.LABEL_POSITIVE)
                testingInfo.initNumPos++;
            else
                testingInfo.initNumNeg++;
            testingInfo.initNumTotal++;

            Instance instance = trainer.convertObjectToInstance(packet, adsPredictor, currentSplit);
            testingInstances.add(instance);
            instance = testingInstances.get(testingInstances.size() - 1);

            double predicted = classifier.classifyInstance(instance);
            // Add prediction to packet
            packet.put(predictedLabel, (int) predicted);
            packet.put("bin", currentSplit);
            testData.put(selectedID, packet);
        }

        MetaEvaluationMeasures teMem = new MetaEvaluationMeasures(testingInfo);
        teMem.doEvaluation(classifier, testingInstances, 0);
        wekaResults.put("testing-" + currentSplit, teMem.getJSONobj());
    }
}
