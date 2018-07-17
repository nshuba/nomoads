package edu.uci.nomoads.training;

import org.json.simple.JSONObject;

/**
 * Trains on URLs
 */
class UrlAdsTrainer extends UrlPathAdsTrainer {

    public UrlAdsTrainer(ServerUtils serverUtils) {
        super(serverUtils);
    }

    public String getLine(JSONObject packet) {
        String line = super.getLine(packet); // gets URI
        line += "host: " + packet.get(JsonKeyDef.F_KEY_HOST) + "\r\n";

        return line;
    }
}
