/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of SBMLsqueezer, a Java program that creates rate 
 * equations for reactions in SBML files (http://sbml.org).
 *
 * Copyright (C) 2006-2013 by the University of Tuebingen, Germany.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * ---------------------------------------------------------------------
 */
package org.sbml.squeezer.sabiork.wizard.model;

import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
* @author Matthias Rall
* @version $Rev$
*/
public class WizardProperties {

	/**
	 * Returns the corresponding text for a given key (File:
	 * Configuration.properties).
	 * 
	 * @param key
	 * @return
	 */
	public static String getText(String key) {
		String text = "";
		try {
			Properties properties = new Properties();
			properties.load(WizardProperties.class
					.getResourceAsStream("Configuration.properties"));
			if (properties.containsKey(key)) {
				text = properties.getProperty(key);
			} else {
				throw new IOException("Can not read property " + key + ".");
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return text.trim();
	}

	/**
	 * Returns the corresponding color for a given key (File:
	 * Configuration.properties).
	 * 
	 * @param key
	 * @return
	 */
	public static Color getColor(String key) {
		Color color = Color.BLACK;
		try {
			String text = getText(key);
			if (text.matches("\\d+[ ]+\\d+[ ]+\\d+")) {
				String[] rgb = text.split("[ ]+");
				color = new Color(Integer.valueOf(rgb[0]),
						Integer.valueOf(rgb[1]), Integer.valueOf(rgb[2]));
			} else {
				throw new IOException("Can not read property " + key + ".");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return color;
	}

}
