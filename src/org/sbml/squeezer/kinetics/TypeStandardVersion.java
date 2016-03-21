/*
 * $Id: TypeStandardVersion.java 19.03.2012 17:55:34 draeger$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of SBMLsqueezer, a Java program that creates rate
 * equations for reactions in SBML files (http://sbml.org).
 *
 * Copyright (C) 2006-2016 by the University of Tuebingen, Germany.
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
 * The possible selections for the three versions of modular rate laws (cf.
 * <a href="http://bioinformatics.oxfordjournals.org/cgi/content/abstract/btq141v1">
 * Liebermeister et al. (2010)</a>, Modular rate laws for enzymatic reactions:
 * thermodynamics, elasticities, and implementation)
 * 
 * @author Andreas Dr&auml;ger
 * @date 2010-10-29
 * @version $Rev$
 * @since 2.0
 */
public enum TypeStandardVersion {
  /**
   * The most simple version.
   */
  cat,
  /**
   * The more complicated version in which all parameters fulfill the
   * Haldane relationship.
   */
  hal,
  /**
   * The most sophisticated version in which all parameters fulfill
   * Wegscheider's condition.
   */
  weg;
}
