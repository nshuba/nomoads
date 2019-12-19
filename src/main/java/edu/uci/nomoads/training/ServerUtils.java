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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import edu.uci.nomoads.Util;
import edu.uci.nomoads.prediction.Predictor;


/**
 * Util class for server-side processing: training and prediction of multiple files
 */
class ServerUtils extends Util {
    /** Directory containing training files, as prepared by our scripts */
    private final String trainingDir;

    /** Directory where training logs will be saved */
    private final String logDir;

    /** Directory where prediction results will be saved */
    private final String resultsDir;

    /** Directory where .dot files of trees will be saved for easy visualization by Python later */
    private final String treeDotDir;

    /** Points to an {@code index_dat.json} file, as prepared by our scripts */
    private final String trainingIndex;

    /** JSON key to be used as a label */
    final String label;

    /** A reference to the {@link Config} object to access various settings */
    private final Config config;

    /** Words that are not to be used as features during training (version codes, phone model
     * name, etc).
     */
    private final Map<String, Integer> stopWords = new HashMap<>();

    private static ServerUtils instance;

    static ServerUtils getInstance(Config config) {
        if (instance == null) {
            instance = new ServerUtils(config);
            return instance;
        }
        else
            return instance;
    }

    /**
     * Initializes server-side utility object
     */
    private ServerUtils(Config config) {
        super(config.getExperimentsConfig());

        this.config = config;

        String rootDir = config.getRootConfig();

        trainingDir = rootDir + "tr_data_per_" + config.getDataSplit() + "/";
        logDir = experimentsDir + "logs/";
        resultsDir = experimentsDir + "results/";
        treeDotDir = experimentsDir + "tree_dot_files/";
        label = config.getLabel();

        // Create these directories if they don't exist
        createDir(logDir);
        createDir(resultsDir);
        createDir(treeDotDir);

        trainingIndex = trainingDir + "index_dat.json";

        String stopwordConfig = config.getStopwordConfig();
        StopWordReader stopWordReader = new StopWordReader();
        stopWordReader.readFile(stopwordConfig);
    }

    boolean isStopWord(String key) {
        return stopWords.containsKey(key);
    }

    String getTrainingDir() { return trainingDir; }

    String getTrainingIndex() { return trainingIndex; }

    String getLogDir() { return logDir; }

    String getResultsDir() { return resultsDir; }

    String getTreeDotDir() { return treeDotDir; }

    String getDataSplit() { return config.getDataSplit(); }

    private class StopWordReader extends ConfigFileReader {

        @Override
        public void processLine(String line) {
            String[] ll = line.split("\t");
            if (ll.length > 1)
                stopWords.put(ll[0].toLowerCase(), Integer.parseInt(ll[1]));
            else
                stopWords.put(line.trim().toLowerCase(), 0);
        }
    }

    /*
    |--------------------------------------------------------------------------
    | Static utility methods below
    |--------------------------------------------------------------------------
    */

    public static String getStringFromJSONObject(JSONObject obj, String keyName) {
        return (String) obj.get(keyName);
    }

    public static int getIntFromJSONObject(JSONObject obj, String keyName) {
        return (int) (long) obj.get(keyName);
    }

    /**
     * Reads the given file info to see if there are enough positive and negative samples in this
     * file
     * @param info
     * @param dataSplit the type of classifier (per-domain or per-app)
     * @return a {@link Info} object if the file was approved, {@code null} otherwise
     */
    static Info approveFile(JSONObject info, String dataSplit) {
        if (info == null) {
            System.out.println("WARNING: no JSON info provided");
            return null;
        }


        Info inf = new Info();
        inf.domain = ServerUtils.getStringFromJSONObject(info, dataSplit);
        inf.OS = ServerUtils.getStringFromJSONObject(info, JsonKeyDef.F_KEY_PLATFORM);
        inf.domainOS = inf.domain + "_" + inf.OS;
        inf.initNumPos = ServerUtils.getIntFromJSONObject(info, JsonKeyDef.NUM_POSITIVE);
        inf.initNumTotal = ServerUtils.getIntFromJSONObject(info, JsonKeyDef.NUM_SAMPLES);
        inf.initNumNeg = inf.initNumTotal - inf.initNumPos;
        return inf;
    }

    public static void appendLineToFile(String fullPath, String line){
        writeToFile(fullPath, line, true);
    }

    public static void overwriteFile(String fullPath, String line){
        writeToFile(fullPath, line, false);
    }

    private static void writeToFile(String fullPath, String line, boolean append){
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(fullPath, append));
            bw.append(line+"\n");
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
