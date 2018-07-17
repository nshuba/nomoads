#!/usr/bin/python

"""
Loads the configuration file into variables to be used by other Python scripts

Note: there is some duplication with Config.java. Consider using Jython to combine this file with 
Config.java
"""

import os, sys

def init(config_file):
    global DATA_ROOT_FOLDER, EXPERIMENTS_DIR, STATS_TRAINING, CLASSIFIER_TYPE

    #TODO: similar to Java Config file. Not sure if there's an easy way to combine them
    
    configs = {}
    with open(os.path.dirname(os.path.realpath(__file__)) + "/../config/" + config_file) as f:
        for line in f:
            # Skip comments and empty spaces
            if line[:1] == '#' or line.split() == '':
                continue
                
            values = line.split("=")
            if len(values) == 2:
                configs[values[0].split()[0]] = values[1].split()[0]

    # Check that all needed configurations were loaded
    dataRootDir = 'dataRootDir'
    trainerClass = 'trainerClass'
    classifierType = 'classifierType'

    expectedConfigs = [dataRootDir, trainerClass, classifierType]
    for config in expectedConfigs:
        if config not in configs:
            sys.exit("ERROR: '" + config + "' not found. Exiting.")

    # Set global variables
    DATA_ROOT_FOLDER = configs[dataRootDir] + "/"
    EXPERIMENTS_DIR = DATA_ROOT_FOLDER + configs[trainerClass] + "/"
    STATS_TRAINING = DATA_ROOT_FOLDER + "tr_data_per_app_all/index_dat.csv"
    CLASSIFIER_TYPE = configs[classifierType]


