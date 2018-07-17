package edu.uci.nomoads.training;

/**
 * Information of the training data set, or description of the network flows.
 * */
class Info {

    public String domain;
    public String OS;
    public String domainOS;
    public String fileNameRelative;
    public int initNumPos;
    public int initNumNeg;
    public int initNumTotal;
    public int trackerFlag;

    public Info() {
        init();
    }

    public Info(String domain) {
        init();
        this.domain = domain;
        this.domainOS = domain + OS;
    }

    private void init() {
        initNumPos = 0;
        initNumNeg = 0;
        initNumTotal = 0;

        OS = "android";
    }

}