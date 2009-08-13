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
package org.sbml.squeezer.io;

import java.util.LinkedList;
import java.util.List;

import org.sbml.SBase;

/**
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * 
 */
public abstract class AbstractSBMLconverter implements SBaseChangedListener {

	private List<SBase> added;
	private List<SBase> removed;
	private List<SBase> changed;

	public AbstractSBMLconverter() {
		added = new LinkedList<SBase>();
		removed = new LinkedList<SBase>();
		changed = new LinkedList<SBase>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBaseChangedListener#sbaseAdded(org.sbml.SBase)
	 */
	public void sbaseAdded(SBase sb) {
		added.add(sb);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBaseChangedListener#sbaseRemoved(org.sbml.SBase)
	 */
	public void sbaseRemoved(SBase sb) {
		removed.add(sb);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBaseChangedListener#stateChanged(org.sbml.SBase)
	 */
	public void stateChanged(SBase sb) {
		changed.add(sb);
	}
	
}
