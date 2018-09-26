#!/usr/bin/python

# This file is part of NoMoAds <http://athinagroup.eng.uci.edu/projects/nomoads/>.
# Copyright (C) 2018 Anastasia Shuba, University of California, Irvine.
#
# NoMoAds is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 2 of the License, or
# (at your option) any later version.
#
# NoMoAds is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with NoMoAds.  If not, see <http://www.gnu.org/licenses/>.

"""
Goes through the "results" directory and saves various machine learning metrics into CSV format.
Note: use this script only when results were generated with classifierType set to 
'package_responsible' in the configuration file.

USAGE:
./evaluate_results.py <config.cfg>
"""

import os, sys, math
import json, csv

from sets import Set

import settings, utils

all_apps = {}

def get_app_name(fn):
    fn = fn[:fn.index('.json')]
    return fn

def process_file(results_dir, fn, csv_writer):
    """
    Goes through a single JSON file, computes machine learning performance metrics, and then
    writes the results to the provided CSV file.
    """

    full_path = results_dir + '/' + fn
    print "Evaluating " + fn

    truePos = float(0)
    trueNeg = float(0)
    falsePos = float(0)
    falseNeg = float(0)
    with open(full_path, "r+") as jf:
        data = json.load(jf)

        # Re-write data in a nicer format than what Java left it in
        jf.seek(0)
        jf.write(json.dumps(data, sort_keys=True, indent=4))
        jf.truncate()

        num_packets = 0
        num_pos_samples = 0
        for k in data:
            ad_label = int(data[k].get(utils.json_key_ad_label, utils.LABEL_NEGATIVE))
            manual_label = int(data[k].get("ad_manual", utils.LABEL_NEGATIVE)) # legacy data
            final_label = utils.LABEL_NEGATIVE
            if ad_label == utils.LABEL_POSITIVE:
                final_label = utils.LABEL_POSITIVE
            elif manual_label == utils.LABEL_POSITIVE:
                final_label = utils.LABEL_POSITIVE

            orig_label = data[k].get("predicted", utils.LABEL_NEGATIVE)
            if final_label == utils.LABEL_POSITIVE:
                num_pos_samples += 1
                if orig_label == utils.LABEL_POSITIVE:
                    truePos += 1
                else:
                    #print "\tFN at " + k + "; h=" + data[k].get(utils.json_key_host)
                    falseNeg += 1
            else:
                if orig_label == utils.LABEL_POSITIVE:
                    #print "\tFP at " + k + "; h=" + data[k].get(utils.json_key_host, "N/A")
                    falsePos += 1
                else:
                    trueNeg += 1

            #print key

            num_packets = num_packets + 1

    (acc, spec, rec, f1) = utils.get_ml_metrics(truePos, falsePos, trueNeg, falseNeg)

    overlap = get_overlap(get_app_name(fn))

    # Save results in csv format for easy processing
    # "f1", "accuracy", "precision", "recall", "overlap"
    row = [os.path.basename(results_dir), fn, num_pos_samples, f1*100, acc*100, spec*100, rec*100,
           overlap]
    csv_writer.writerow(row)

    return num_packets, acc, spec, rec, f1

def process_results(results_dir, csv_writer):
    """
    Goes through JSON files of a single 'split,' finds the overlap between ad libraries of the
    test and training sets, prints, and saves ML metrics to the provided CSV file.
    """

    global tr_libs
    # Per-classifier stats (macro-stats):
    all_files_acc = []
    all_files_specificity = []
    all_files_recall = []
    all_files_f1 = []
    num_packets_tot = 0

    test_apps = []
    for fn in os.listdir(results_dir):
        fn = get_app_name(fn)
        test_apps.append(fn)

    print "Apps in the test set:"
    print test_apps
    tr_libs = Set()
    for app_key in all_apps:
        libs = all_apps[app_key]
        if app_key not in test_apps:
            for lib in libs:
                tr_libs.add(lib)

    print "\n" + str(len(tr_libs)) + " libraries in the training set:"
    print tr_libs

    for fn in os.listdir(results_dir):
        (num_packets, file_acc_score, file_spec_score, file_recall_score, file_f1_score) \
            = process_file(results_dir, fn, csv_writer)

        all_files_acc.append(file_acc_score)
        all_files_specificity.append(file_spec_score)
        all_files_recall.append(file_recall_score)
        all_files_f1.append(file_f1_score)

        num_packets_tot += num_packets

    # Print macro results
    print "\nMACRO RESULTS (Per-app):"
    print_stats("acc", all_files_acc)
    print_stats("spec", all_files_specificity)
    print_stats("rec", all_files_recall)
    print_stats("f1", all_files_f1)

    print "Num data points = " + str(num_packets_tot) + "\n---------------------------\n"

def print_stats(metric_name, list):
    """Convenience method for printing the average and standard deviation of the provided list"""
    avg = sum(list)/float(len(list))

    # Std dev:
    for i, sample in enumerate(list):
        list[i] = (sample - avg)*(sample - avg)

    std_dev = math.sqrt(sum(list)/len(list))

    print metric_name + ": avg = " + str(avg*100) + "; std = " + str(std_dev*100)

def get_overlap(test_app):
    """Prints percentage of ad libraries present in test apps that were also present in training"""
    global tr_libs

    test_libs = Set()
    libs = all_apps[test_app]
    for lib in libs:
        test_libs.add(lib)

    #print tr_libs
    diff = test_libs.difference(tr_libs) # new set with elements in test_libs but not in tr_libs
    overlap = (1 - float(len(diff))/len(test_libs)) * 100
    #print "\t" + str(overlap) + ": " + str(diff)

    return overlap


def prepare_apps_struct():
    """Builds a dictionary of apps to ad libraries they contain based on 'apps_sorted.csv'"""
    with open(settings.DATA_ROOT_FOLDER + "apps_sorted.csv", 'rb') as f:
        reader = csv.reader(f, delimiter=',')

        # Get headers
        header_row = reader.next()
        libsIdx = header_row.index(utils.hAdLibs)
        appNameIdx = header_row.index(utils.hPkgName)

        for row in reader:
            appName = str(row[appNameIdx])
            adLibs = str(row[libsIdx])
            all_apps[appName] = adLibs.split(";")

if __name__ == '__main__':
    # Init global variables
    global tr_libs

    settings.init(sys.argv[1])

    prepare_apps_struct()

    with open(settings.EXPERIMENTS_DIR + "results_split.csv", "wb") as f:
        csv_writer = csv.writer(f) #acc, prec, rec
        csv_writer.writerow(["bin", utils.hPkgName, "pos_samples", "f1", "accuracy",
                             "specificity", "recall", "overlap"])

        # Go through each directory in the "results" folder and save stats
        results_dir = settings.EXPERIMENTS_DIR + "results/"
        for dir in os.listdir(results_dir):
            process_results(results_dir + dir, csv_writer)