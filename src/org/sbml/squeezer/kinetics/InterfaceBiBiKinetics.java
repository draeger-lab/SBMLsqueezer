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
package org.sbml.squeezer.kinetics;

/**
 * Kinetic law objects that implement this interface are able to describe
 * enzyme-catalyzed reactions with exactly two reactants (or one reactant with a
 * stoichiometry of two) and two products (or one product with a stoichiometry
 * of two). An enzyme does not necessarily have to be assigned to the reaction
 * explicitely.
 * 
 * @author Andreas Dr&auml;ger
 * @date 2009-09-22
 * @since 1.3
 * @version $Rev$
 */
public interface InterfaceBiBiKinetics extends InterfaceKinteticsType {

}
