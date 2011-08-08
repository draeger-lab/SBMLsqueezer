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
package org.sbml.squeezer.test;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.junit.Ignore;

/**
 * @author Andreas Dr&auml;ger
 * 
 */
@Ignore
public class PreferencesTest {

	Preferences prefs = Preferences.userRoot().node("/org/sbml/squeezer");

	void fillRegistry() throws BackingStoreException {
		for (Object o : System.getProperties().keySet()) {
			String key = o.toString();
			if (key.startsWith("user.")
					&& System.getProperty(key).length() != 0)
				prefs.put(key, System.getProperty(key));
		}
		prefs.flush();
	}

	void display() {
		try {
			for (String key : prefs.keys())
				System.out.println(key + ": " + prefs.get(key, "---"));
		} catch (BackingStoreException e) {
			System.err.println("Knoten können nicht ausgelesen werden: " + e);
		}
	}

	public PreferencesTest() throws BackingStoreException {
		fillRegistry();
		display();
	}

	/**
	 * @param args
	 * @throws BackingStoreException
	 */
	public static void main(String[] args) throws BackingStoreException {
		new PreferencesTest();
	}

}
