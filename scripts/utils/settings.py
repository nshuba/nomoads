#!/usr/bin/python

#  This file is part of NoMoAds <http://athinagroup.eng.uci.edu/projects/nomoads/>.
#  Copyright (C) 2018, 2019 Anastasia Shuba
#
#  NoMoAds is free software: you can redistribute it and/or modify
#  it under the terms of the GNU General Public License as published by
#  the Free Software Foundation, either version 3 of the License, or
#  (at your option) any later version.
#
#  NoMoAds is distributed in the hope that it will be useful,
#  but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#  GNU General Public License for more details.
#
#  You should have received a copy of the GNU General Public License
#  along with NoMoAds.  If not, see <http://www.gnu.org/licenses/>.

"""
Loads the configuration file into variables to be used by other Python scripts

Note: there is some duplication with Config.java. Consider using Jython to combine this file with 
Config.java
"""

import os, sys

def init(config_file):
    global DATA_ROOT_FOLDER, EXPERIMENTS_DIR, DATA_SPLIT, LOGS_DIR
    global PREDICTION_LABEL
    global json_key_label

    #TODO: similar to Java Config file. Not sure if there's an easy way to combine them
    
    configs = {}
    with open(os.path.dirname(os.path.realpath(__file__)) + "/../../config/" + config_file) as f:
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
    dataSplit = 'dataSplit'

    expectedConfigs = [dataRootDir, trainerClass, dataSplit]
    for config in expectedConfigs:
        if config not in configs:
            sys.exit("ERROR: '" + config + "' not found. Exiting.")

    # Set global variables
    DATA_ROOT_FOLDER = configs[dataRootDir] + "/"
    EXPERIMENTS_DIR = DATA_ROOT_FOLDER + configs[trainerClass] + "/"
    LOGS_DIR = EXPERIMENTS_DIR + "logs/"
    DATA_SPLIT = configs[dataSplit]
    PREDICTION_LABEL = "predicted_" + DATA_SPLIT

    label = "label"
    json_key_label = configs.get(label, label)



