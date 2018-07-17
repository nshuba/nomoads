#!/usr/bin/python

import os

# Global variables used by other scripts
GENERAL_FILE_NAME = 'general.json'

# Classification labels
LABEL_POSITIVE = 1
LABEL_NEGATIVE = 0

# JSON keys
json_key_rule = 'rule'
json_key_tk_flag = 'tk_flag' # whether the packet is ad-related or not
json_key_ad_label = 'ad' # whether the packet is ad-related or not
json_key_ad_pkg = "package_responsible" # which app is responsible for the ad
json_key_package_name = 'package_name' # which app generated the packet
json_key_version = "package_version"
json_key_label = json_key_ad_label  # for NoMoAds the label is always an ad label
json_key_pii_types = 'pii_types'    # a list
json_key_predicted_types = 'predict_piiTypes'
json_key_domain = 'domain'
json_key_host = 'host'
json_key_dst_ip = 'dst_ip'

# Headers for CSV files
hAdLibs = 'ad_libraries'
hPkgName = 'package'

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