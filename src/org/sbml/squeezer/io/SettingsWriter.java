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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * 
 * TODO: comment missing
 * 
 * @since 2.0
 * @version
 * @author Andreas Dr&auml;ger (draeger) <andreas.draeger@uni-tuebingen.de>
 * @author Nadine Hassis <Nadine.hassis@gmail.com> Copyright (c) ZBiT,
 *         University of T&uuml;bingen, Germany Compiler: JDK 1.6.0
 * @date Aug 1, 2007
 * 
 */
public class SettingsWriter {

	BufferedWriter out;

	/**
	 * @param file
	 * @param maxEducts
	 * @param uniUniType
	 * @param biUniType
	 * @param biBiType
	 * @param maxSpeciesWarnings
	 * @param noReactionMAK
	 * @param possibleEnzymeRNA
	 * @param generateKineticForAllReaction
	 * @param reversibility
	 * @param possibleEnzymeGenericProtein
	 * @param possibleEnzymeTruncatedProtein
	 * @param possibleEnzymeComplex
	 * @param possibleEnzymeUnknown
	 * @param possibleEnzymeReceptor
	 * @param possibleEnzymeSimpleMolecule
	 * @param possibleEnzymeAsRNA
	 * @param possibleEnzymeAllNotChecked
	 * @param forceAllReactionsAsEnzymeReaction
	 */
	public SettingsWriter(File file, int maxEducts, short uniUniType,
			short biUniType, short biBiType, boolean maxSpeciesWarnings,
			boolean noReactionMAK, boolean possibleEnzymeRNA,
			boolean generateKineticForAllReaction, boolean reversibility,
			boolean possibleEnzymeGenericProtein,
			boolean possibleEnzymeTruncatedProtein,
			boolean possibleEnzymeComplex, boolean possibleEnzymeUnknown,
			boolean possibleEnzymeReceptor,
			boolean possibleEnzymeSimpleMolecule, boolean possibleEnzymeAsRNA,
			boolean possibleEnzymeAllNotChecked,
			boolean forceAllReactionsAsEnzymeReaction,
			boolean addAllParametersGlobally) {
		try {
			out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file.getPath())));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		write("SBMLsqeezer Settings-file");
		append("version:1.00");
		append("START");
		append("maxSpecies:" + maxEducts);
		append("uniUniType:" + uniUniType);
		append("biUniType:" + biUniType);
		append("biBiType:" + biBiType);
		append("warnings:" + maxSpeciesWarnings);
		append("noReactionMAK:" + noReactionMAK);
		append("possibleEnzymeRNA:" + possibleEnzymeRNA);
		append("GenKinForAllReac:" + generateKineticForAllReaction);
		append("reversibility:" + reversibility);
		append("possibleEnzymeGenericProtein:" + possibleEnzymeGenericProtein);
		append("possibleEnzymeTruncatedProtein:"
				+ possibleEnzymeTruncatedProtein);
		append("possibleEnzymeComplex:" + possibleEnzymeComplex);
		append("possibleEnzymeUnknown:" + possibleEnzymeUnknown);
		append("possibleEnzymeReceptor:" + possibleEnzymeReceptor);
		append("possibleEnzymeSimpleMolecule:" + possibleEnzymeSimpleMolecule);
		append("possibleEnzymeAsRNA:" + possibleEnzymeAsRNA);
		append("possibleEnzymeAllNotChecked:" + possibleEnzymeAllNotChecked);
		append("forceAllReactionsAsEnzymeReaction:"
				+ forceAllReactionsAsEnzymeReaction);
		append("addAllParametersGlobally:" + addAllParametersGlobally);
		append("END");
		close();
	}

	private void append(String str) {
		try {
			out.write(str);
			out.newLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void close() {
		try {
			out.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private void write(String str) {
		try {
			out.write(str);
			out.newLine();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

}
