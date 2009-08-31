/*
 *  SBMLsqueezer creates rate equations for reactions in SBML files
 *  (http://sbml.org).
 *  Copyright (C) 2009 ZBIT, University of Tübingen, Andreas Dräger
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.sbml.squeezer.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author Nadine Hassis <Nadine.hassis@gmail.com>
 * @date 01.08.2007
 */
public class SettingsParser {

	private String dataDir;

	private String dataName;

	private BufferedReader in = null;

	/**
	 * 
	 * @param parent
	 *            The component that is the parent of the file chooser dialog
	 *            window to be shown.
	 */
	public SettingsParser(File file) {
		try {
			in = new BufferedReader(new InputStreamReader(new FileInputStream(
					file)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public SettingsParser(String stringDataDir, String stringDataName) {
		this.dataDir = stringDataDir;
		this.dataName = stringDataName;
		try {
			in = new BufferedReader(new InputStreamReader(new FileInputStream(
					dataDir + dataName)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void close() throws IOException {
		if (in != null)
			in.close();
	}

	public String read() throws IOException {
		if (in != null)
			return in.readLine();
		return "";
	}
}
