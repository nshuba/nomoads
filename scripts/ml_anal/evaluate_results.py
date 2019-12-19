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
Goes through the "results" directory and saves various machine learning metrics into CSV format.

USAGE:
./evaluate_results.py <config.cfg>
"""

import os, sys, math
import json, csv

sys.path.append(os.path.dirname(os.path.abspath(__file__)) + os.sep + "..")
from utils import utils, settings, json_keys

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
            ad_label = int(data[k].get(settings.json_key_label, utils.LABEL_NEGATIVE))
            orig_label = data[k].get(settings.PREDICTION_LABEL, utils.LABEL_NEGATIVE)
            if ad_label == utils.LABEL_POSITIVE:
                num_pos_samples += 1
                if orig_label == utils.LABEL_POSITIVE:
                    truePos += 1
                else:
                    #print "\tFN at " + k + "; h=" + data[k].get(json_keys.host)
                    falseNeg += 1
            else:
                if orig_label == utils.LABEL_POSITIVE:
                    #print "\tFP at " + k + "; h=" + data[k].get(json_keys.host, "N/A")
                    falsePos += 1
                else:
                    trueNeg += 1

            #print key

            num_packets = num_packets + 1

    (acc, spec, rec, f1) = utils.get_ml_metrics(truePos, falsePos, trueNeg, falseNeg, verbose=True)

    # Save results in csv format for easy processing
    row = [get_app_name(fn), num_pos_samples, f1*100, acc*100, spec*100, rec*100]
    csv_writer.writerow(row)

    return num_packets, acc, spec, rec, f1

def process_results(results_dir, csv_writer):
    # Per-classifier stats (macro-stats):
    all_files_acc = []
    all_files_specificity = []
    all_files_recall = []
    all_files_f1 = []
    num_packets_tot = 0

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
    utils.print_stats("acc", all_files_acc)
    utils.print_stats("spec", all_files_specificity)
    utils.print_stats("rec", all_files_recall)
    utils.print_stats("f1", all_files_f1)

    print "Num data points = " + str(num_packets_tot) + "\n---------------------------\n"

if __name__ == '__main__':
    # Init global variables
    global tr_libs

    settings.init(sys.argv[1])
    with open(settings.EXPERIMENTS_DIR + "results_split.csv", "wb") as f:
        csv_writer = csv.writer(f) #acc, prec, rec
        csv_writer.writerow([json_keys.package_name, "pos_samples", "f1", "accuracy",
                             "specificity", "recall"])

        # Go through the results and save stats
        results_dir = settings.EXPERIMENTS_DIR + "results/"
        process_results(results_dir, csv_writer)
