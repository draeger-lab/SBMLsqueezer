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
package org.sbml.squeezer;

import java.util.List;

import org.sbml.jsbml.SBMLException;

import de.zbit.text.FormatBuilder;
import de.zbit.util.StringUtil;

/**
 * This class takes a list of {@link SBMLException}s as input and generates a
 * nicely formatted human-readable report from it - based on a given
 * {@link FormatBuilder}.
 * 
 * @author Andreas Dr&auml;ger
 * @version $Rev$
 * @since 2.0
 */
public class ConsistencyReportBuilder {
  
  /**
   * The formatter.
   */
  private FormatBuilder formatter;

  /**
   * 
   */
  public ConsistencyReportBuilder() {
  }
  
  /**
   * 
   * @param excl
   * @return
   */
  public String format(List<SBMLException> excl) {
    StringBuilder warnings = new StringBuilder();
    for (SBMLException exc : excl) {
      warnings.append("<p>");
      warnings.append(exc.getMessage().replace("<", "&lt;").replace(">", "&gt;"));
      warnings.append("</p>");
    }
    return StringUtil.toHTML(warnings.toString());
  }

  /**
   * 
   * @param formatter
   */
  public void setFormatter(FormatBuilder formatter) {
    this.formatter = formatter;
  }
  
}
