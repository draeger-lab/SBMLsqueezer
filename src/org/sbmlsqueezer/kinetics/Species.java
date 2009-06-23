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
package org.sbmlsqueezer.kinetics;

import jp.sbi.celldesigner.plugin.PluginSpecies;

/**
 * 
 * TODO: comment missing
 * 
 * @since 1.0
 * @version
 * @author <a href="mailto:Nadine.hassis@gmail.com">Nadine Hassis</a>
 * @author <a href="mailto:andreas.draeger@uni-tuebingen.de">Andreas
 *         Dr&auml;ger</a>
 * @date Aug 1, 2007
 * 
 */
public class Species extends PluginSpecies {

	/**
	 * 
	 * TODO: comment missing
	 * 
	 * @param species
	 * @return
	 */
	public static String idToTeX(String species) {
		String idTeX = toTeX(species);
		return idTeX.substring(1, idTeX.length() - 1);
	}

	/**
	 * 
	 * TODO: comment missing
	 * 
	 * @param species
	 * @return
	 */
	public static String toTeX(String species) {
		int index = species.length();
		for (int i = 0; i < 10; i++) {
			String j = "" + i;
			if (index > species.indexOf(j) && species.indexOf(j) > 0) {
				index = species.indexOf(j);
			}
		}
		String num = species.substring(index);
		String speciesTex = "[\\text{" + species.substring(0, index) + "}";
		speciesTex += num.length() > 0 ? "_{" + num + "}]" : "]";

		return speciesTex;
	}

}
