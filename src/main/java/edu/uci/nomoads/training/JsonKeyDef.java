/**
 * This file is part of ReCon.

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
 */
package edu.uci.nomoads.training;

class JsonKeyDef {

	// keys for JSON Object of the network flows
	public final static String F_KEY_LABEL = "label";
	public final static String F_KEY_AD_LABEL = "ad";

	public final static String F_KEY_PII_TYPES = "pii_types";
	public final static String F_KEY_PKG_NAME = "package_name";
	public final static String F_KEY_DOMAIN = "domain";
	public final static String F_KEY_URI = "uri";
	public final static String F_KEY_HOST = "host";
	public final static String F_KEY_POST_BODY = "post_body";
	public final static String F_KEY_REFERRER = "referrer";
	public final static String F_KEY_HEADERS = "headers";
	public final static String F_KEY_PLATFORM = "platform";
	public final static String TK_FLAG = "tk_flag";
	public final static String NUM_SAMPLES = "num_samples";
	public final static String NUM_POSITIVE = "num_positive";

	public final static String DST_PORT = "dst_port";
	public final static String DST_IP = "dst_ip";

	// For prediction results
	public final static String F_KEY_PREDICT_TYPES = "predict_piiTypes";
	public final static String F_KEY_PREDICT_LABEL = "predictLabel";
}
