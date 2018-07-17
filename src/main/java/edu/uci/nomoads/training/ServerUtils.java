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

    /** Points to an {@code index_dat.json} file, as prepared by our scripts */
    private final String trainingIndex;

    /** A reference to the {@link Config} object to access various settings */
    private final Config config;

    /** Words that are not to be used as features during training (version codes, phone model
     * name, etc).
     */
    private final Map<String, Integer> stopWords = new HashMap<>();

    /**
     * Initializes server-side utility object
     */
    public ServerUtils(Config config) {
        super(config.getExperimentsConfig());

        this.config = config;

        String rootDir = config.getRootConfig();

        trainingDir = rootDir + "tr_data_per_" + config.getClassifierType() + "/";
        logDir = experimentsDir + "logs/";
        resultsDir = experimentsDir + "results/";

        // Create these directories if they don't exist
        createDir(logDir);
        createDir(resultsDir);

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

    String getClassifierType() { return config.getClassifierType(); }

    int getBinSize() { return config.getBinSize(); }

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
     * @param classifierType the type of classifier (per-domain or per-app)
     * @return a {@link Info} object if the file was approved, {@code null} otherwise
     */
    static Info approveFile(JSONObject info, String classifierType) {
        if (info == null) {
            System.out.println("WARNING: no JSON info provided");
            return null;
        }


        Info inf = new Info();
        inf.domain = ServerUtils.getStringFromJSONObject(info, classifierType);
        inf.OS = ServerUtils.getStringFromJSONObject(info, JsonKeyDef.F_KEY_PLATFORM);
        inf.domainOS = inf.domain + "_" + inf.OS;
        inf.initNumPos = ServerUtils.getIntFromJSONObject(info, JsonKeyDef.NUM_POSITIVE);
        inf.initNumTotal = ServerUtils.getIntFromJSONObject(info, JsonKeyDef.NUM_SAMPLES);
        inf.initNumNeg = inf.initNumTotal - inf.initNumPos;
        return inf;
    }

    public static void writeToFile(String fullPath, String line){
        new File(fullPath).delete();
        appendLineToFile(fullPath, line);
    }

    public static void appendLineToFile(String fullPath, String line){
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(fullPath, true));
            bw.append(line+"\n");
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
