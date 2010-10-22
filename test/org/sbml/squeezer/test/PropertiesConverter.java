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

import java.io.IOException;
import java.util.Properties;

import org.sbml.squeezer.SBMLsqueezer;

import de.zbit.util.SBProperties;

/**
 * @author Andreas Dr&auml;ger
 * 
 */
public class PropertiesConverter {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		SBProperties p = SBMLsqueezer.getConfiguration().getDefaultProperties();

		Properties s = new Properties();
		for (Object key : p.keySet()) {
			s.put(key.toString(), p.get(key).toString());
		}

		s.storeToXML(System.out, "comment", "utf8");
	}

}
