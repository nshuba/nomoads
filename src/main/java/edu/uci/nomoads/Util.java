/*
 *  This file is part of NoMoAds <http://athinagroup.eng.uci.edu/projects/nomoads/>.
 *  Copyright (C) 2018 Anastasia Shuba, University of California, Irvine.
 *
 *  NoMoAds is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  NoMoAds is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with NoMoAds.  If not, see <http://www.gnu.org/licenses/>.
 */
package edu.uci.nomoads;

import java.io.File;

import edu.uci.nomoads.prediction.DPIInterface;

/**
 * Util class used during training and prediction
 */
public class Util {
    protected final String experimentsDir;

    private final String arffFolder;
    private final String modelFolder;
	private final String treeLabelsFile;

	private static final boolean DEBUG = false;

    public Util(String experimentsDir) {
        this.experimentsDir = experimentsDir;

        arffFolder = experimentsDir + "arff/";
        modelFolder = experimentsDir + "model/";

        // Create these directories if they don't exist
        createDir(arffFolder);
        createDir(modelFolder);

		treeLabelsFile = experimentsDir + "treeLabels.json";
    }

    public String getExperimentsDir() { return experimentsDir; }

    public String getFeaturesDir() { return arffFolder; }

    public String getModelDir() { return modelFolder; }

	public String getTreeLabelsFile() { return treeLabelsFile; }

    /**
     * Creates directory if it does not exist. If needed, parent directories are also created.
     * @param pathToDir path to directory
     */
    public void createDir(String pathToDir) {
        File dir = new File(pathToDir);
        if (!dir.exists()) {
            if(!dir.mkdirs())
                System.err.println("Could not create directory: " + pathToDir);
        }
    }

	public static void debug(String msg) {
		if (DEBUG)
			System.out.println("debugging:>>>>>" + msg);
	}

}
