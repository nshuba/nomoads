#!/usr/bin/env python3

#  This file is part of NoMoAds <http://athinagroup.eng.uci.edu/projects/nomoads/>.
#  Copyright (C) 2018, 2019 Hieu Le, Anastasia Shuba
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

import requests
import datetime

now = datetime.datetime.now()

# get original file from moaab
src_url = "http://adblock.mahakala.is"

# get request as json
r = requests.get(src_url)

# write out the raw file
tmp_raw_file_name = "tmpmoaab.txt"
if r.status_code == 200:
    with open(tmp_raw_file_name, 'wb') as output_raw:
        output_raw.write(r.content)

#open file to write to adblockplus rules
new_path = 'adp_moaab.txt'

DOMAIN_PREFIX = "||" # denotes that this is a domain
#denotes end of domain and that it must be a thirdparty request
THIRD_PARTY_SUFFIX = "^$third-party"
NEW_LINE = "\n"

ignore_host_list = ["localhost", "localdomain", "android", "test", "testing"]

def should_ignore(host):
    for ignore in ignore_host_list:
        if host.startswith(ignore):
            return True

with open(new_path,'w') as adp_file:
    adp_file.write("[Adblock Plus 2.0]" + NEW_LINE)
    adp_file.write("! Title: MoaAB Converted to Adblock Plus Rules" + NEW_LINE)
    adp_file.write("! Last modified: " + now.strftime("%Y-%m-%d %H:%M") + NEW_LINE)
    adp_file.write("! Homepage: " + src_url + NEW_LINE)
    adp_file.write("! -----------------Hostnames to Third-party tracking domains-----------------!" + NEW_LINE)

    with open(tmp_raw_file_name, 'r') as raw_file:
        for line in raw_file:

            split_line = line.split(" ")
            if len(split_line) > 1:
                # get hostname and strip nextline
                hostname = split_line[1].replace("\n", "")
                if not should_ignore(hostname):
                    # Don't use third-party suffix, since MoaAB blocks based on hostnames during
                    # normal operation
                    #adp_file.write(DOMAIN_PREFIX + hostname + THIRD_PARTY_SUFFIX + NEW_LINE)
                    adp_file.write(DOMAIN_PREFIX + hostname + NEW_LINE)