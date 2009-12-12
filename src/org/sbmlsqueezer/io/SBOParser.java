/**
 *
 */
package org.sbmlsqueezer.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * @author wouamba
 */
public class SBOParser {

	private static final File	obo	= new File(System.getProperty("user.dir")
	                                  + System.getProperty("file.separator")
	                                  + "resources"
	                                  + System.getProperty("file.separator")
	                                  + "SBO_OBO.obo");

	/**
	 * This method returns by a giving SBO term id the corresponding SBO term name
	 *
	 * @param SBOTermID
	 * @return SBOTermName
	 * @throws IOException
	 */
	public static String getSBOTermName(int SBOTermID) throws IOException {
		String name = "Unknown SBO id " + SBOTermID;

		BufferedReader input = new BufferedReader(new FileReader(obo));
		String line = null;
		while ((line = input.readLine()) != null) {
			if (line.equals("") || !line.startsWith("id: SBO:"))
				continue;
			else if (line.startsWith("id: SBO:")) {
				if (Integer.parseInt((line.substring(8, line.length()))) == SBOTermID) {
					line = input.readLine();
					name = line.substring(6, line.length());
					break;
				}
			}
		}

		return name;

	}

	/**
	 * This method returns by a giving SBO term id the corresponding SBO term
	 * definition
	 *
	 * @param SBOTermID
	 * @return SBOTermDesc
	 */
	public static String getSBOTermDef(int SBOTermID) throws IOException {
		String def = "Unknown SBO id " + SBOTermID;
		BufferedReader input = new BufferedReader(new FileReader(obo));
		String line = null;
		while ((line = input.readLine()) != null) {
			if (line.equals("") || !line.startsWith("id: SBO:"))
				continue;
			else if (line.startsWith("id: SBO:")) {
				if (Integer.parseInt((line.substring(8, line.length()))) == SBOTermID) {
					line = input.readLine();
					line = input.readLine();
					int last = 0;
					for (int k = 0; k < line.length(); k++) {
						last++;
						if (line.charAt(k) == '<' || line.charAt(k) == '[') break;
					}
					if (last > 0) def = line.substring(6, last - 1);
					break;
				}
			}
		}

		return def;
	}
}
