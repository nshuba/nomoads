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

package_name = "package_name"
version = "package_version"
type = "type"
ats_pkg = "ats_pkg"
id = "pkt_id"
pii_label = "pii_types"
predicted = "predicted"
list_label = "list_labels"

source = "_source"
layers = "layers"

dst_ip = "dst_ip"
ip = "ip"
tcp = "tcp"

http = "http"
method = "method"
uri = "uri"
headers = "headers"
referer = "referer"
domain = "domain"
host = "host"
dst_port = "dst_port"

http_req = http + ".request."
http_req_method = http_req + method
http_req_uri = http_req + uri
http_req_line = http_req + "line"

pkt_comment = "pkt_comment"
ats_label = "ats"
trace = "trace"

frame = "frame"
frame_num = frame + ".number"
frame_comment = frame + ".comment"
frame_ts = frame + ".time_epoch"

# UC Irvine coordinates, as an example
LOCATION_PII = [("33.64", "-117.84"), ("33.6", "-117.8")]

# PII values below have been redacted for privacy. Adjust as necessary
PII_VALUES = {
    "Example Device ID": "###########"
}
