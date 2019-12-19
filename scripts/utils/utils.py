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

import os
import math

# Global variables used by other scripts
GENERAL_DATA_SPLIT = "general"
GENERAL_FILE_NAME = 'general.json'

# Classification labels
LABEL_POSITIVE = 1
LABEL_NEGATIVE = 0

def print_stats(metric_name, list, is_percent=True):
    """Convenience method for printing the average and standard deviation of the provided list"""
    avg = sum(list)/float(len(list))

    # Std dev:
    for i, sample in enumerate(list):
        list[i] = (sample - avg)*(sample - avg)

    std_dev = math.sqrt(sum(list)/len(list))

    if is_percent:
        print metric_name + ": avg = " + str(avg*100) + "; std = " + str(std_dev*100)
    else:
        print metric_name + ": avg = " + str(avg) + "; std = " + str(std_dev)

def get_ml_metrics(truePos, falsePos, trueNeg, falseNeg, verbose=False):
    """Prints and returns 4-tuple (accuracy, specificity, recall, f1) based on provided TP/FP/TN/FN"""
    acc = (float) (truePos + trueNeg)/(truePos + trueNeg + falsePos + falseNeg)

    denom = (float) (truePos + falseNeg)
    if denom == 0:
        recall = 1
    else:
        recall = truePos/denom

    denom = (float) (trueNeg + falsePos)
    if denom == 0:
        spec = 1
    else:
        spec = trueNeg/denom

    denom = (float) (truePos + falsePos)
    if denom == 0:
        prec = 1
    else:
        prec = truePos/denom

    denom = (float) (prec + recall)
    if denom == 0:
        f_measure = 0
    else:
        f_measure = 2 * (prec * recall)/denom

    if verbose:
        print "\tTP: " + str(truePos) + "\tFP: " + str(falsePos) + "\tTN: " + str(trueNeg) + \
              "\tFN: " + str(falseNeg)
        print "\n\tF-measure: " + str(f_measure) + "\tAccuracy: " + str(acc)
        print "\tSpecificity: " + str(spec)+ "\tRecall: " + str(recall) + "\n"

    return (acc, spec, recall, f_measure)

def apply_funct_to_dir(dir, funct, recursive=True):
    for fn in os.listdir(dir):
        full_path = dir + "/" + fn
        if recursive and os.path.isdir(full_path):
            funct(full_path)
            continue

        funct(full_path)

def apply_funct_to_dir_or_file(dir_or_file, funct, recursive=True):
    if os.path.isdir(dir_or_file):
        apply_funct_to_dir(dir_or_file, funct, recursive)
    else:
        funct(dir_or_file)

def writable_file(prospective_file):
    if not os.path.isfile(args.filter_list):
        raise Exception(prospective_file + " is not an existing file.")
    if os.access(prospective_file, os.W_OK):
        return prospective_file
    else:
        raise Exception(prospective_file + " is not a readable file.")

def readable_file(prospective_file):
    if not os.path.isfile(prospective_file):
        raise Exception(prospective_file + " is not an existing file.")
    if os.access(prospective_file, os.R_OK):
        return prospective_file
    else:
        raise Exception(prospective_file + " is not a readable file.")

def readable_dir(prospective_dir):
    if not os.path.isdir(prospective_dir):
        raise Exception("readable_dir:{0} is not a valid path".format(prospective_dir))
    if os.access(prospective_dir, os.R_OK):
        return prospective_dir
    else:
        raise Exception("readable_dir:{0} is not a readable dir".format(prospective_dir))