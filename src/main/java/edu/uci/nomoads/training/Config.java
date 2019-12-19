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

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Reads the configuration file and keep the needed params in memory for usage by other classes.
 */
class Config {
    /** The configuration directory */
    public static final String CONFIG_DIR = "config/";

    /** Specifies the root directory (where folders such as "raw_data" and etc. are held). */
    private static final String dataRootDir = "dataRootDir";

    /** Specifies which of the child {@link Trainer} classes to use for training. */
    private static final String trainerClass = "trainerClass";

    /** How to split data for training classifiers. Refer to the README for details about this
     * setting. */
    private static final String dataSplit = "dataSplit";

    /** Configuration for whether or not to do cross validation, or just build a classifier */
    private static final String crossValidation = "crossValidation";

    /** The name of the file containing stop words. */
    private static final String stopwordConfig  = "stopwordConfig";

    /** Name of config key specifying the JSON label key in data files */
    private static final String labelConfig = "label";

    /** Configurations that are expected to be found in the config file. */
    private final Set<String> expectedConfigs = new HashSet<>(Arrays.asList(dataRootDir,
            trainerClass, dataSplit, crossValidation, stopwordConfig, labelConfig));

    /** The {@link Trainer} selected by the {@link #trainerClass} configuration */
    private final Trainer selectedTrainer;

    /** Whether or not to do cross validation, or just build a classifier */
    private final boolean crossValEnabled;

    /** Map for keeping selected configuration values. */
    private final Map<String, String> configs;

    /**
     * Loads the configuration file
     */
    public Config(String configFile) {
        configs = new HashMap<String, String>();

        ConfigReader reader = new ConfigReader();
        reader.readFile(CONFIG_DIR + configFile);

        // Sanity checks
        // Make sure data split type is supported
        String selectedDataSplit = configs.get(dataSplit);
        if (!DataSplitter.SUPPORTED_TYPES.contains(selectedDataSplit)) {
            System.err.println("Invalid " + dataSplit + " parameter: " + selectedDataSplit + ". " +
                    "Exiting.");
            System.exit(-1);
        }

        // Make sure cross-validation is a boolean
        crossValEnabled = Boolean.parseBoolean(configs.get(crossValidation));

        // Prepare a trainer object
        String selectedClass = configs.get(trainerClass);
        Trainer tempTrainer = null; // use a temporary variable to make compiler happy
        try {
            // prepend selected class name with package name
            String fullClassName = Trainer.class.getPackage().getName() + "." + selectedClass;
            System.out.println("Using " + fullClassName);

            Class trainerClass = Class.forName(fullClassName);
            Constructor constructor = trainerClass.getConstructor(ServerUtils.class);
            tempTrainer = (Trainer) constructor.newInstance(ServerUtils.getInstance(Config.this));
        } catch (Exception e) {
            System.err.println("Invalid " + trainerClass + " parameter: " + selectedClass + ". " +
                    "Exiting.");
            System.exit(-1);
        }
        selectedTrainer = tempTrainer;
    }

    /** @return the {@link #labelConfig} configuration */
    String getLabel() { return configs.get(labelConfig); }

    /** @return the root directory specified in the config file (see {@link #dataRootDir}) */
    String getRootConfig() { return configs.get(dataRootDir) + "/"; }

    /** @return the {@link #dataSplit} configuration */
    String getDataSplit() { return configs.get(dataSplit); }

    /** @return whether or not to do cross validation, or just build a classifier */
    boolean isCrossValidationEnabled() { return crossValEnabled; }

    /** @return the {@link #stopwordConfig} configuration */
    String getStopwordConfig() { return configs.get(stopwordConfig); }

    /** @return a {@link Trainer} object as specified by {@link #trainerClass} */
    Trainer getSelectedTrainer() { return selectedTrainer; }

    /** @return the path to the experiments directory, where tree models and evaluation results will
     * be saved. Currently the name is decided based on the {@link #trainerClass} selected. */
    String getExperimentsConfig() { return getRootConfig() + configs.get(trainerClass) + "/"; }

    private class ConfigReader extends ConfigFileReader {

        @Override
        public void processLine(String line) {
            String[] values = line.split("=");
            if (values.length >= 2) {
                String configName = values[0].trim();
                String configValue = values[1].trim();

                // Warn user about unsupported configurations
                if (!expectedConfigs.contains(configName)) {
                    System.out.println("WARNING: Ignoring unknown configuration " + configName);
                    return;
                }

                // Warn user about duplicate configurations
                if (configs.containsKey(configName))
                    System.out.println("WARNING: Overwriting configuration " + configName);

                configs.put(configName, configValue);
            } else {
                System.out.println("WARNING: Ignoring configuration line " + line);
            }
        }

        /**
         * Checks that all required configurations were loaded
         */
        @Override
        public void onProcessingDone() {
            for(String configName : expectedConfigs) {
                if (!configs.containsKey(configName)) {
                    System.err.println("Please provide '" + configName + "' in the configuration " +
                            "file. Exiting.");
                    System.exit(-1);
                }
            }
        }
    }
}
