#!/usr/bin/python
"""
This file is part of ReCon.

    ReCon is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Foobar is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Foobar.  If not, see <http://www.gnu.org/licenses/>."
"""
import os, sys
import json

import settings, utils

# For extracting domain names
# http://stackoverflow.com/questions/399250/going-where-php-parse-url-doesnt-parsing-only-the-domain/
# To install, simply run:  pip install tldextract
import tldextract

def prepare_training_data():
    """
    Reads in provided data and saves into a separate training folder.
    
    The files in the training folder are organized based on the provided settings.CLASSIFIER_TYPE
    """

    domain_os_flows = dict()

    # Read in data into our dictionary
    dump_os_domains(domain_os_flows, settings.DATA_ROOT_FOLDER + 'raw_data')
    
    # Save results to disk
    output_folder = settings.DATA_ROOT_FOLDER + 'tr_data_per_' + settings.CLASSIFIER_TYPE + "/"
    domains_stat_file = output_folder + 'index_dat.csv'
    
    os.system('mkdir -p %s' % output_folder)
    cmd = 'rm %s* 2>/dev/null' % output_folder
    os.system(cmd)
    
    key_num_samples = 'num_samples'
    key_num_positive = 'num_positive'
    with open(domains_stat_file, 'w') as df:
        df.write('#' + utils.json_key_domain +
            ',' + key_num_samples + "," + key_num_positive + '\n')
     
    # Populate a JSON and CSV report + general classifier data
    general_pos = 0
    general_total = 0
    general_entries = dict()
    report_json = dict()
    for d_o in domain_os_flows:
        entries = domain_os_flows[d_o]
        with open(output_folder+d_o, 'w') as of:
            of.write(json.dumps(entries, sort_keys=True, indent=4))
        # {domain}_{os}.json    #samples    #pos
        rj = dict()
        entry0 = entries[entries.keys()[0]]
        domain = entry0.get(settings.CLASSIFIER_TYPE, None)

        report = domain
        num_samples = len(entries)
        general_total += num_samples
        report += ',%s' % num_samples
        num_positive_samples = 0

        for k in entries:
            entry = entries[k]
            label = int(entry[utils.json_key_label])
            if label == utils.LABEL_POSITIVE:
                num_positive_samples += 1
                general_pos += 1

            general_entries[k] = entry
        report += ',%s' %  num_positive_samples
        with open(domains_stat_file, 'a+') as df:
            df.write('%s\n' % report)
        rj[settings.CLASSIFIER_TYPE] = domain
        rj[key_num_samples] = num_samples
        rj[key_num_positive] = num_positive_samples
        report_json[d_o] = rj

    gen_repor = dict()
    gen_repor[settings.CLASSIFIER_TYPE] = 'general'
    gen_repor[key_num_samples] = general_total
    gen_repor[key_num_positive] = general_pos
    
    # Add general stats to JSON report
    report_json[utils.GENERAL_FILE_NAME] = gen_repor
    
    # Save JSON report to disk
    with open(output_folder + 'index_dat.json', 'w') as df:
        df.write(json.dumps(report_json, sort_keys=True, indent=4))
        
    # Save general classifier data
    with open(output_folder + utils.GENERAL_FILE_NAME, 'w') as gf:
        gf.write(json.dumps(general_entries, sort_keys=True, indent=4))
    
    print '%s potential classifiers processed' % (len(domain_os_flows))

def dump_os_domains(domain_os_flows, fpath):
    """
    Recursively goes through the provided directory. The JSON files contained within the directory
    and the children directories are read into domain_os_flows.
    """

    for fn in os.listdir(fpath):
        full_path = fpath + '/' + fn
        
        # Recursively go through all directories
        if os.path.isdir(full_path):
            dump_os_domains(domain_os_flows, full_path)
            continue
        
        print full_path
        with open(full_path) as jf:
            data = json.load(jf)
            for k in data:
                entry = data[k]
                host = entry.get(utils.json_key_host, None)

                # Account for undefined domains
                domain = entry.get(utils.json_key_domain, None)
                if domain is None or len(domain) == 0:
                    if host is None:
                        domain = entry.get(utils.json_key_domain, None)
                        entry[utils.json_key_domain] = domain
                    else:
                        ext_result = tldextract.extract(host)
                        # Be consistent with ReCon and keep suffix
                        domain = ext_result.domain + "." + ext_result.suffix
                    # Update domain to avoid nulls in the future:
                    entry[utils.json_key_domain] = domain

                # Use updated domain or package name
                class_type = entry[settings.CLASSIFIER_TYPE]
                domain_os = '%s.json' % (class_type)
                
                if domain_os not in domain_os_flows:
                    domain_os_flows[domain_os] = dict()

                domain_os_flows[domain_os][k] = entry

if __name__ == '__main__':
    # Init global variables
    settings.init(sys.argv[1])

    prepare_training_data()