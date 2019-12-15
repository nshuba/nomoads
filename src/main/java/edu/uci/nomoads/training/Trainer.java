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

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import edu.uci.nomoads.Util;
import edu.uci.nomoads.prediction.AdsPredictor;
import weka.classifiers.*;
import weka.classifiers.trees.*;
import weka.core.*;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Trains binary classifiers
 */
abstract class Trainer {
	// Class label
	public final static int LABEL_POSITIVE = 1;
	public final static int LABEL_NEGATIVE = 0;

	protected String jsonKeyLabel;

    protected final int DEFAULT_THETA = 2;

	/** Keeps tree labels belonging to each domainOS. It's filled out during training
	 * and is saved to a JSON file upon training completion. */
	protected final JSONObject jsonDomainOSTreeLabels;

	protected HashSet<String> pkgNames;

	protected HashSet<String> piisSet;

    protected final ServerUtils mServerUtils;

    public Trainer(ServerUtils serverUtils) {
        mServerUtils = serverUtils;
        jsonDomainOSTreeLabels = new JSONObject();
        jsonKeyLabel = serverUtils.label;

		pkgNames = new HashSet<>();
		piisSet = new HashSet<>();
	}

	/**
	 * Train a classifier given based on provided training data
	 *
	 * @param trFlows JSON object containing all training data
	 * @param mem
	 *            - filled with info description of the training dataset
	 */
	public Instances trainOneDomain(JSONObject trFlows, MetaEvaluationMeasures mem) {

		long t1 = System.nanoTime();
		TrainingData trainingData = populateTrainingSet(trFlows, DEFAULT_THETA, mem);
		long t2 = System.nanoTime();
		mem.populatingTime = (t2 - t1) / 10e8;

		Classifier classifier;
		try {
			classifier = getClassifier();
			trainWithClassifier(classifier, trainingData.trainingInstances, mem.info
					.domainOS, mem);
			return trainingData.trainingInstances;
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}

		return null;
	}

	public TrainingData populateTrainingSet(JSONObject trFlows, int thresholdFrequency,
											MetaEvaluationMeasures mem) {

		TrainingData trainingData = new TrainingData(trFlows);

		// Prepare structure
		trainingData.mem = mem;
		trainingData.mem.numPositive = mem.info.initNumPos;
		trainingData.mem.numNegative = mem.info.initNumNeg;
		trainingData.mem.numTotal = mem.info.initNumTotal;
		trainingData.mem.numInstance = mem.info.initNumTotal;

		Instances trainingSet = null;
		//if (trainingData.mem.numTotal >= 2 && trainingData.mem.numPositive >= 1) {
  //      if (trainingData.mem.numTotal >= NUM_CROSS_FOLDS) {
			populateTrainingMatrix(trFlows, trainingData);
			//if (trainingData.mem.numOfPossibleFeatures > 5)
			trainingSet = populateArff(mem.info, trainingData, thresholdFrequency);
			//else
			//	System.out.println("WARNING: Not enough features! Classifier:" + mem.info.domainOS);
//		} else {
//            System.out.println("WARNING: Not enough data! Classifier:" + mem.info.domainOS);
//            trainingData.mem.error = "not enough data";
//        }

		trainingData.trainingInstances = trainingSet;

		return trainingData;
	}

	/**
	 * Given positive lines and negative lines, generate the overall word_count
	 * and trainMatrix. All possible features are saved (all words, all PII, package names, etc.)
	 * Child classes pick features saved here in {@link #populateArff(Info, TrainingData, int)}.
	 *
	 * @param trainingData
	 *            - the original training data object, could be empty or
	 *            prefilled with some customized entries
	 * */
	public TrainingData populateTrainingMatrix(JSONObject domainOSFlows, TrainingData trainingData) {
		ArrayList<Map<String, Integer>> trainMatrix = trainingData.trainMatrix;
		ArrayList<JSONArray> piiLabels = trainingData.piiLabels;
		Map<String, Integer> word_count = trainingData.wordCount;
		int numOfPossibleFeatures = word_count.size();
		for (Object k : domainOSFlows.keySet()) {
			JSONObject flow = (JSONObject) domainOSFlows.get(k);
			// Save all possible package names for later:
			String pkgName = (String) flow.get(JsonKeyDef.F_KEY_PKG_NAME);
			pkgNames.add(pkgName);
			trainingData.pkgNames.add(pkgName);


			String line = getLine(flow);

			// Count how often each word occurs
			RString sf = new RString();
			sf.breakLineIntoWords(line);
			Map<String, Integer> words = sf.Words;
			for (Map.Entry<String, Integer> entry : words.entrySet()) {
				//String word_key = entry.getKey().trim();
				String word_key = entry.getKey();

				// Filter out short words that do not contain letters nor digits
				int wordLen = word_key.length();
				if (wordLen <= 3)
					continue;

				// Skip past delimiter if any
				int wordStart = 0;
				char c = word_key.charAt(wordStart);
				if (!Character.isAlphabetic(c) && !Character.isDigit(c))
					wordStart++;

				int wordEnd = word_key.length() - 1;
				c = word_key.charAt(wordEnd);
				if (!Character.isAlphabetic(c) && !Character.isDigit(c))
					wordEnd--;

				String keyNoDelims = word_key.substring(wordStart, wordEnd + 1).toLowerCase().trim();

				if (mServerUtils.isStopWord(keyNoDelims)
						|| RString.isAllNumeric(keyNoDelims) || isPIIValue(keyNoDelims))
					continue;

				int frequency = entry.getValue();
				if (word_count.containsKey(word_key))
					word_count.put(word_key,
							frequency + word_count.get(word_key));
				else {
					numOfPossibleFeatures++;
					word_count.put(word_key, frequency);
				}
			}
			trainMatrix.add(words);

			// Add all the labels (not required for binary, but to avoid duplicate code with
			// multi label training, add them all here as well)
			JSONArray labels = (JSONArray) flow.get(JsonKeyDef.F_KEY_PII_TYPES);
			piiLabels.add(labels);

			// Add the Ad label
			trainingData.adLabels.add(ServerUtils.getIntFromJSONObject(flow, jsonKeyLabel));

			// Keep track of all possible PII for later use as a feature
			if (labels != null){
				for (Object piiObj : labels) {
					piisSet.add((String) piiObj);
				}
			}
		}
		trainingData.wordCount = word_count;
		trainingData.trainMatrix = trainMatrix;
		trainingData.piiLabels = piiLabels;
		trainingData.mem.numOfPossibleFeatures = numOfPossibleFeatures;
		return trainingData;
	}

	/**
	 * Writes the feature set into an .arff file for future use, debugging, etc.
	 * @param domainOS
	 * @param instances
	 */
	protected void saveArff(String domainOS, Instances instances) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(
					mServerUtils.getFeaturesDir() + domainOS + ".arff"));
			bw.write(instances.toString());
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

    /**
     * Adds class values to the provided attributes
     * @param attributes
     */
	protected void addClassLabels(ArrayList<Attribute> attributes) {
		ArrayList<String> classVals = new ArrayList<String>();
		classVals.add("" + LABEL_NEGATIVE);
		classVals.add("" + LABEL_POSITIVE);

        attributes.add(new Attribute(jsonKeyLabel, classVals));
    }

	/**
	 * Adds {@link #LABEL_POSITIVE} or {@link #LABEL_NEGATIVE} as the last attribute,
	 * depending on if there is an ad or not, and returns the finished {@link Instance}.
	 * @param instanceValue
	 * @param attributes
	 */
	protected Instance finalizeInstance(int adLabel, double[] instanceValue,
										ArrayList<Attribute> attributes) {
		//System.out.println("using ads as labels");
		instanceValue[attributes.size() - 1] = adLabel;
		return new SparseInstance(1.0, instanceValue);
	}

	public Classifier trainWithClassifier(
			Classifier classifier, Instances trainingSet, String domainOS,
			MetaEvaluationMeasures mem) {
		try {
/*			if (enableCrossValidation) {
				mem.doEvaluation(classifier, trainingSet, NUM_CROSS_FOLDS);
			}*/

			long t1 = System.currentTimeMillis();
			classifier.buildClassifier(trainingSet);
			long t2 = System.currentTimeMillis();
			mem.trainingTime = (t2 - t1);

			System.out.println(domainOS);

			String classifierName = classifier.getClass().toString();
			classifierName = classifierName.substring(classifierName.lastIndexOf(".") + 1);

			if (classifierName.equals("J48")) {
				J48 treeClassifier = (J48) classifier;
				try {
					saveTreeLabels(domainOS, "", treeClassifier);
				} catch (Exception e) {
					System.out.println("WARNING: Could not save tree labels!");
					e.printStackTrace();
				}

				mem.treeSize = treeClassifier.measureTreeSize();
				mem.numLeaves = treeClassifier.measureNumLeaves();
				mem.numNonLeafNodes = (mem.treeSize - mem.numLeaves);
				System.out.println(domainOS + ": " + mem.treeSize + "-" + mem.numLeaves + " = " +
						mem.numNonLeafNodes);
			}

			// Save model
			SerializationHelper.write(mServerUtils.getModelDir() + domainOS + "-"
					+ classifier.getClass().toString().substring(6) + ".model",
					classifier);

            // Save tree labels
            ServerUtils.overwriteFile(mServerUtils.getTreeLabelsFile(),
                    jsonDomainOSTreeLabels.toJSONString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return classifier;
	}

    /**
     * Saves provided data in JSON format, for saving to disk upon training completion
     * @param domainOS
     * @param pii PII label
     * @param j48classifier the tree to parse and extract nodes from
	 * @return {@code true} if labels were saved, {@code false} if no labels were found
     * @throws Exception
     */
	public boolean saveTreeLabels(String domainOS, String pii, J48 j48classifier) throws
			Exception {
		String tree = j48classifier.graph();

		// Prepare tree for parsing - deal with this \r\n special case
		tree = tree.replace("\r\n\" ]", "\\r\\n\" ]");
		tree = tree.replace("label=\"\n", "label=\"\\n");

		// Delete previous .dot file and update
		String dotFilePath = mServerUtils.getTreeDotDir() + domainOS + "_tree.dot";
		ServerUtils.overwriteFile(dotFilePath, tree);

        Map<String, String> treeLabels = new HashMap<>();
		parseTree(treeLabels, tree, pii);

		// Save tree labels
		if (treeLabels.size() > 0) {
			jsonDomainOSTreeLabels.put(domainOS, treeLabels);
			return true;
		}

		return false;
	}

	public static void parseTree(Map<String, String> treeLabels, String tree, String pii) {
		// Increment by 2 to skip lines indicating transitions between nodes
		String lines[] = tree.split("\n");

		for (int j = 1; j < lines.length; j += 2) {
			// Skip leaf nodes
			if (lines[j].contains("shape=box style=filled ]"))
				continue;

			String labelPrefix = "label=\"";
			String label = lines[j].substring(
					lines[j].indexOf(labelPrefix) + labelPrefix.length(),
					lines[j].indexOf("\" ]"));

			// Replace it back to normal when saving
			treeLabels.put(pii + j, label.replace("\\r\\n", "\r\n"));
		}
	}

	/**
	 * Get a {@link J48} classifier.
	 * NOTE: ReCon supports more classifiers here, but for now we only support trees.
	 */
	public Classifier getClassifier() {
		J48 j48 = new J48();
		j48.setUnpruned(true);
		return j48;
	}

	protected boolean isPIIValue(String key) {
		return (key.contains("xxxx"));
	}

	/*
	|--------------------------------------------------------------------------
	| Methods to be implemented by children
	|--------------------------------------------------------------------------
	*/

	/**
	 * Convert given packet object into a String. Children can pick which fields of the object to
	 * include. For instance, they can include the entire URL, or just its path component.
	 *
	 * @param packet the packet object
	 * @return a {@code String} with all the fields concatenated
	 */
	protected abstract String getLine(JSONObject packet);

	public abstract Instances populateArff(Info info, TrainingData trData, int theta);

	protected abstract Instance convertObjectToInstance(JSONObject packet, AdsPredictor
			predictor, String domainOS);
}