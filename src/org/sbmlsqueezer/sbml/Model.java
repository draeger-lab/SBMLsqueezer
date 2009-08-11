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
package org.sbmlsqueezer.sbml;

import java.util.LinkedList;

/**
 * 
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * 
 */
public class Model extends NamedSBase {

	LinkedList<Species> listofSpecies = new LinkedList<Species>();
	LinkedList<Reaction> listofReactions = new LinkedList<Reaction>();

	public Model(String id) {
		super(id);
	}

	public void addSpecies(Species spec) {
		listofSpecies.add(spec);
		stateChanged();
	}

	public void removeSpecies(Species spec) {
		listofSpecies.remove(spec);
		stateChanged();
	}

	public void addReaction(Reaction reac) {
		listofReactions.add(reac);
		stateChanged();
	}

	public void removeReaction(Reaction reac) {
		listofReactions.remove(reac);
		stateChanged();
	}
}
