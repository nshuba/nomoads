package edu.uci.nomoads.training;

import org.json.simple.JSONObject;

/**
 * Trains on URL + Headers
 */
class UrlHeadersAdsTrainer extends UrlPathAdsTrainer {

    public UrlHeadersAdsTrainer(ServerUtils serverUtils) {
        super(serverUtils);
    }

    public String getLine(JSONObject packet) {
        String line = super.getLine(packet);

        //System.out.println("getLine: " + UrlHeadersAdsTrainer.class.getSimpleName());

        // Note: host is part of headers
        JSONObject headers = (JSONObject) packet.get(JsonKeyDef.F_KEY_HEADERS);
        for (Object h : headers.keySet()) {
            line += h + ": " + headers.get(h) + "\r\n";
        }

        return line;
    }
}
