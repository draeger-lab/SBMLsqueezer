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
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.sbml.squeezer.standalone;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.sbml.squeezer.gui.SBMLsqueezerUI;
import org.sbml.squeezer.io.AbstractSBMLconverter;
import org.sbml.squeezer.resources.Resource;

/**
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * 
 */
public class SBMLsqueezer {

	AbstractSBMLconverter converter;

	public SBMLsqueezer(String fileName) {
		converter = new LibSBMLReader(fileName);
		showGUI(converter);
	}

	public SBMLsqueezer() {
		converter = new LibSBMLReader();
		showGUI(converter);
	}

	private void showGUI(AbstractSBMLconverter converter) {
		SBMLsqueezerUI gui = new SBMLsqueezerUI(converter);
		gui.setLocationRelativeTo(null);
		gui.setVisible(true);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.loadLibrary("sbmlj");
		try {
			BufferedReader br = new BufferedReader(new FileReader(
					Resource.class.getResource("txt/disclaimer.txt").getFile()));
			String line;
			while ((line = br.readLine()) != null)
				System.out.println(line);
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
		if (args.length == 1)
			new SBMLsqueezer(args[0]);
		else
			new SBMLsqueezer();
	}

}
