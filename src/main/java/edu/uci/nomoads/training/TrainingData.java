package edu.uci.nomoads.training;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import weka.core.Instances;

/**
 * Created by Nastia on 3/6/2017.
 */
class TrainingData {
    /** Count of all words, accross entire dataset */
    public Map<String, Integer> wordCount;

    /** Count of words occurring at a data point. Index of array list is the data point index. */
    public ArrayList<Map<String, Integer>> trainMatrix;

    /** Types of PII contained within the given data point */
    public ArrayList<JSONArray> piiLabels;

    public final JSONObject trFlows;
    public ArrayList<Integer> adLabels;

    /** List of package names at each data point */
    public ArrayList<String> pkgNames;
    public Instances trainingInstances;

    public MetaEvaluationMeasures mem;

    public TrainingData(JSONObject trFlows){
        wordCount = new HashMap<>();
        trainMatrix = new ArrayList<>();
        piiLabels = new ArrayList<>();
        pkgNames = new ArrayList<>();
        mem = new MetaEvaluationMeasures();
        adLabels = new ArrayList<>();
        this.trFlows = trFlows;
    }
}
