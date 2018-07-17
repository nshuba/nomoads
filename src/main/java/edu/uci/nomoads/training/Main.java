/*
 * This file is part of NoMoAds,
 * Copyright (C) 2018-present UC Irvine
 *
 * NoMoAds is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as
 * published by the Free Software Foundation.
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

public class Main {

	public static void main(String[] args) {
		
		// Load configurations
        String configFile = args[0];
		Config config = new Config(configFile);

		// Start training based on the configuration
		config.getSelectedTrainer().train();
	}

}
