/*
 *  This file is part of NoMoAds <http://athinagroup.eng.uci.edu/projects/nomoads/>.
 *  Copyright (C) 2018 Anastasia Shuba, University of California, Irvine.
 *  Copyright (C) 2016 Jingjing Ren, Northeastern University.
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

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import weka.core.Instances;

/**
 * Convenience structure for keeping data
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
