/*
 * This file is part of NoMoAds <http://athinagroup.eng.uci.edu/projects/nomoads/>.
 * Copyright (C) 2018, 2019 Anastasia Shuba
 * Copyright (C) 2016 Jingjing Ren, Northeastern University
 *
 * NoMoAds is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NoMoAds is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NoMoAds.  If not, see <http://www.gnu.org/licenses/>.
 */
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