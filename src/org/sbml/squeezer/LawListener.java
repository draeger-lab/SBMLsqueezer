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
package org.sbml.squeezer;

import java.util.EventListener;

import org.sbml.jsbml.SBase;
import org.sbml.jsbml.UnitDefinition;
import org.sbml.squeezer.kinetics.BasicKineticLaw;

/**
 * Classes that implement this interface can display the progress made by
 * {@link KineticLawGenerator} when assigning {@link BasicKineticLaw},
 * {@link Parameter}, {@link UnitDefinition} objects and so forth to a model.
 * 
 * @author Andreas Dr&auml;ger
 * @since 1.1
 */
public interface LawListener extends EventListener {

	/**
	 * Allows you to tell this listener which number (in a list or in an array
	 * or what ever) you are currently working with.
	 * 
	 * @param item
	 * @param num
	 *            The current element.
	 */
	public void currentState(SBase item, int num);

	/**
	 * Allows you to tell this listener the total number of elements to work
	 * with.
	 * 
	 * @param className
	 *            the name of objects that is to be changed now.
	 * @param numberOfElements
	 *            Number of elements.
	 */
	public void initLawListener(String className, int numberOfElements);
}
