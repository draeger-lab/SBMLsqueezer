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

import java.io.PrintStream;

/**
 * Processes messages that have been sent to this object and writes them to a
 * given {@link PrintStream}. It can be used as an alternative to
 * {@link System#out} because it is very easy to let messages appear, e.g., in a
 * {@link File} or on a GUI element.
 * 
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * @since 1.3
 */
public interface MessageListener {
	/**
	 * Process the given message.
	 * 
	 * @param message
	 */
	public void log(Object message);

	/**
	 * Process the given error message.
	 * 
	 * @param message
	 */
	public void err(Object message);

	/**
	 * 
	 * @param format
	 * @param args
	 */
	public void logf(String format, Object... args);

	/**
	 * 
	 * @param format
	 * @param args
	 */
	public void errf(String format, Object... args);

	/**
	 * 
	 * @param b
	 */
	public void setWriteTime(boolean b);

	/**
	 * 
	 * @param string
	 */
	public void logln(String string);

	/**
	 * 
	 * @param string
	 */
	public void errln(String string);

	/**
	 * Decides whether or not messages should be sent to any one of the streams.
	 * 
	 * @param yes
	 */
	public void setVerbose(boolean yes);
}
