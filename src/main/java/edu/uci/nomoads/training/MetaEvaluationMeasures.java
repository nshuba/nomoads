/*
 * This file is part of NoMoAds <http://athinagroup.eng.uci.edu/projects/nomoads/>.
 * Copyright (C) 2018, 2019 Anastasia Shuba
 * Copyright (C) 2016 Jingjing Ren, Northeastern University
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

import java.util.Random;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;

/**
 * Intermediate and final results during training a classifier for the domain,os.
 * */
class MetaEvaluationMeasures {
    /** If there was an error during training, this string gets set with an error message */
    public String error = null;

    public double falsePositiveRate = -1;
    public double falseNegativeRate = -1;
    public double trainingTime = -1;
    public double populatingTime = -1;

    public int numTotal = -1;
    public int numPositive = -1;
    public int numNegative = -1;
    public int numOfPossibleFeatures = -1;

    public double AUC = -1;
    public double fMeasure = -1;
    public double numInstance = -1;
    public int numCorrectlyClassified = -1;
    public double accuracy = -1; // = NumCorrectlyClassified / NumTotal
    public double specificity = -1;
    public double recall = -1;

    public double TP = -1;
    public double TN = -1;
    public double FP = -1;
    public double FN = -1;
    public Info info;

    /*
    |--------------------------------------------------------------------------
    | Fields that represent the tree being evaluated
    |--------------------------------------------------------------------------
    */

    public double treeSize;
    public double numLeaves;
    public double numNonLeafNodes;

    public MetaEvaluationMeasures() {}

    public MetaEvaluationMeasures(Info info) {
        this.info = info;
    }

    /**
     * Do evaluation on trained classifier/model, including the summary, false
     * positive/negative rate, AUC, running time
     *
     * @param classifier
     *            - the trained classifier
     */
    protected void doEvaluation(Classifier classifier, Instances tras, int numFolds) {
        try {
            Evaluation evaluation = new Evaluation(tras);

            if (numFolds > 1)
                evaluation.crossValidateModel(classifier, tras, numFolds, new Random(1));
            else
                evaluation.evaluateModel(classifier, tras);

            numInstance = evaluation.numInstances();
            double M = evaluation.numTruePositives(1)
                    + evaluation.numFalseNegatives(1);
            numPositive = (int) M;
            AUC = evaluation.areaUnderROC(1);
            numCorrectlyClassified = (int) evaluation.correct();
            accuracy = 1.0 * numCorrectlyClassified / numInstance;
            falseNegativeRate = evaluation.falseNegativeRate(1);
            falsePositiveRate = evaluation.falsePositiveRate(1);
            fMeasure = evaluation.fMeasure(1);
            TP = evaluation.numTruePositives(1);
            TN = evaluation.numTrueNegatives(1);
            FP = evaluation.numFalsePositives(1);
            FN = evaluation.numFalseNegatives(1);

            specificity = evaluation.trueNegativeRate(1);
            recall = evaluation.recall(1);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public JSONObject getJSONobj() {
        JSONObject obj = new JSONObject();
        obj.put("error", error);
        obj.put("accuracy", this.accuracy);
        obj.put("fpr", falsePositiveRate);
        obj.put("fnr", falseNegativeRate);
        obj.put("f_measure", fMeasure);
        obj.put("auc", AUC);
        obj.put("traing_time", trainingTime);
        obj.put("populating_time", populatingTime);

        if (info != null) {
            obj.put("init_num_pos", info.initNumPos);
            obj.put("init_num_neg", info.initNumNeg);
            obj.put("init_num_total", info.initNumTotal);
        }

        obj.put("specificity", specificity);
        obj.put("recall", recall);

        obj.put("numNonLeafNodes", numNonLeafNodes);

        return obj;
    }

}
