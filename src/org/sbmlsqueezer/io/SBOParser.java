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
package org.sbmlsqueezer.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;

import org.sbmlsqueezer.resources.Resource;

/**
 * @author wouamba
 * @author <a href="mailto:andreas.draeger@uni-tuebingen.de">Andreas
 *         Dr&auml;ger</a>
 */
public class SBOParser {

	/**
	 * The SBO definition file to be parsed in OBO format.
	 */
	private static URL obo = Resource.class.getClassLoader().getResource(
			"org/sbmlsqueezer/resources/txt/SBO_OBO.obo");

	/**
	 * A hash to save the SBO name for a given number.
	 */
	private static Hashtable<Integer, String> sboName = new Hashtable<Integer, String>();
	/**
	 * A hash to save the SBO definition for a given number.
	 */
	private static Hashtable<Integer, String> sboDefinition = new Hashtable<Integer, String>();

	/**
	 * Returns the location of the SBO definition file.
	 * 
	 * @return
	 */
	public static final String getSBOOboFile() {
		return obo.toExternalForm();
	}

	/**
	 * This method allows you to specify the location of the obo file containing
	 * the definitions of all SBO terms.
	 * 
	 * @param sboFilePath
	 *            Example: /home/user/controlledVocabulary/SBO.obo
	 */
	public static final void setSBOOboFile(String url) {
		try {
			obo = new URL(url);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method returns by a giving SBO term id the corresponding SBO term
	 * name
	 * 
	 * @param sboTermID
	 * @return SBOTermName
	 * @throws IOException
	 */
	public static final String getSBOTermName(int sboTermID) throws IOException {
		Integer id = Integer.valueOf(sboTermID);
		if (sboName.containsKey(id))
			return sboName.get(id);
		String name = "";
		BufferedReader input = new BufferedReader(new InputStreamReader(obo
				.openStream()));
		String line = null;
		while ((line = input.readLine()) != null) {
			if (line.equals("") || !line.startsWith("id: SBO:"))
				continue;
			else if (line.startsWith("id: SBO:")) {
				if (Integer.parseInt((line.substring(8, line.length()))) == sboTermID) {
					line = input.readLine();
					name = line.substring(6);
					break;
				}
			}
		}
		input.close();
		if (name.length() == 0)
			name = "Unknown SBO id " + sboTermID;
		else
			name = convertName(name);
		sboName.put(id, name);
		return name;
	}

	/**
	 * 
	 * @param name
	 * @return
	 */
	private static String convertName(String name) {
		String n = name.replaceAll("\\\\,", ",");
		n = n.replaceAll("\\\\n", " ");
		n = n.replaceAll("\\\\:", ":");
		n = n.replaceAll("\\\\\"", "\"");
		n = n.trim();
		if (n.endsWith("\"") && !n.startsWith("\""))
			n = n.substring(0, n.length() - 2);
		return n;
	}

	/**
	 * This method returns by a giving SBO term id the corresponding SBO term
	 * definition
	 * 
	 * @param SBOTermID
	 * @return SBOTermDesc
	 */
	public static final String getSBOTermDef(int sboTermID) throws IOException {
		Integer id = Integer.valueOf(sboTermID);
		if (sboDefinition.containsKey(id))
			return sboDefinition.get(id);
		String def = "";
		BufferedReader input = new BufferedReader(new InputStreamReader(obo
				.openStream()));
		String line = null;
		while ((line = input.readLine()) != null) {
			if (line.equals("") || !line.startsWith("id: SBO:"))
				continue;
			else if (line.startsWith("id: SBO:")) {
				if (Integer.parseInt((line.substring(8, line.length()))) == sboTermID) {
					line = input.readLine();
					line = input.readLine();
					int last = 0;
					for (int k = 0; k < line.length(); k++) {
						last++;
						if (line.charAt(k) == '<' || line.charAt(k) == '[')
							break;
					}
					if (last > 0)
						def = line.substring(6, last - 1);
					break;
				}
			}
		}
		input.close();
		if (def.length() == 0)
			def = "Unknown SBO id " + sboTermID;
		else {
			def = convertName(def);
			if (def.endsWith("\"") && !def.startsWith("\""))
				def = def.substring(0, def.length() - 2);
		}
		sboDefinition.put(id, def);
		return def;
	}

	/**
	 * For a given name this method looks for the corresponding SBO term id
	 * thereby ignoring upper/lower case. If no match is found -1 is returned.
	 * 
	 * @param name
	 * @return
	 * @throws IOException
	 */
	public static final int getSBOidForName(String name) throws IOException {
		int id = -1;
		if (obo != null) {
			BufferedReader br = new BufferedReader(new InputStreamReader(obo
					.openStream()));
			String line, readName = "";

			while (((line = br.readLine()) != null)
					&& !name.equalsIgnoreCase(readName)) {
				if (line.startsWith("id: SBO:"))
					id = Integer.parseInt(line.substring(8).trim());
				else if (line.startsWith("name:"))
					readName = convertName(line.substring(6));
			}
			br.close();
			if (!readName.equals(name))
				id = -1;
		}
		return id;
	}
}
