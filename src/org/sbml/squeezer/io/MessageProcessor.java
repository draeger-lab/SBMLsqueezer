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
import java.util.Calendar;

/**
 * An implementation of {@link MessageListener} that provides some nice methods
 * for date presentation in combination with messages.
 * 
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * @since 1.3
 */
public class MessageProcessor implements MessageListener {

	/**
	 * 
	 * @return
	 */
	public static final String getTime() {
		Calendar c = Calendar.getInstance();
		StringBuffer sb = new StringBuffer();
		sb.append(twoDigits(c.get(Calendar.YEAR)));
		sb.append('-');
		sb.append(twoDigits(c.get(Calendar.MONTH) + 1));
		sb.append('-');
		sb.append(twoDigits(c.get(Calendar.DATE)));
		sb.append("T");
		sb.append(twoDigits(c.get(Calendar.HOUR_OF_DAY)));
		sb.append(':');
		sb.append(twoDigits(c.get(Calendar.MINUTE)));
		sb.append(':');
		sb.append(twoDigits(c.get(Calendar.SECOND)));
		return sb.toString();
	}

	/**
	 * 
	 * @param digit
	 * @return
	 */
	private static final String twoDigits(int digit) {
		if (digit < 10) {
			StringBuffer sb = new StringBuffer();
			sb.append(Character.valueOf('0'));
			sb.append(Integer.toString(digit));
			return sb.toString();
		}
		return Integer.toString(digit);
	}

	/**
	 * 
	 */
	private PrintStream err;

	/**
	 * 
	 */
	private PrintStream out;

	/**
	 * 
	 */
	private boolean writeDate;

	/**
	 * 
	 */
	private boolean verbose;

	/**
	 * 
	 */
	public MessageProcessor() {
		this(System.out, System.err);
	}

	/**
	 * 
	 * @param out
	 * @param err
	 */
	public MessageProcessor(PrintStream out, PrintStream err) {
		this.out = out;
		this.err = err;
		this.writeDate = true;
		this.verbose = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see quick.io.MessageListener#logError(java.lang.Object)
	 */
	public void err(Object message) {
		writeMSG(err, message);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.squeezer.io.MessageListener#errf(java.lang.String,
	 * java.lang.Object[])
	 */
	public void errf(String format, Object... args) {
		writeMSGf(err, format, args);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.squeezer.io.MessageListener#errln(java.lang.String)
	 */
	public void errln(String string) {
		writeMSG(err, string);
		if (verbose)
			err.println();
	}

	/**
	 * 
	 * @return
	 */
	public PrintStream getErrorStream() {
		return err;
	}

	/**
	 * 
	 * @return
	 */
	public PrintStream getOutStream() {
		return out;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see quick.io.MessageListener#logMessage(java.lang.Object)
	 */
	public void log(Object message) {
		writeMSG(out, message);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.squeezer.io.MessageListener#logf(java.lang.String,
	 * java.lang.Object[])
	 */
	public void logf(String format, Object... args) {
		writeMSGf(out, format, args);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.squeezer.io.MessageListener#logln(java.lang.String)
	 */
	public void logln(String message) {
		writeMSG(out, message);
		if (verbose)
			out.println();
	}

	/**
	 * 
	 * @param err
	 */
	public void setErrorStream(PrintStream err) {
		this.err = err;
	}

	/**
	 * 
	 * @param out
	 */
	public void setOutStream(PrintStream out) {
		this.out = out;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.squeezer.io.MessageListener#setVerbose(boolean)
	 */
	public void setVerbose(boolean yes) {
		verbose = yes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.squeezer.io.MessageListener#setDateEnabled(boolean)
	 */
	public void setWriteTime(boolean writeDate) {
		this.writeDate = writeDate;
	}

	/**
	 * 
	 * @param stream
	 * @param message
	 */
	private void writeMSG(PrintStream stream, Object message) {
		writeMSGf(stream, message.toString());
	}

	/**
	 * 
	 * @param stream
	 * @param format
	 * @param args
	 */
	private void writeMSGf(PrintStream stream, String format, Object... args) {
		if (verbose) {
			if (writeDate) {
				stream.print('[');
				stream.print(getTime());
				stream.print("]\t");
			}
			stream.printf(format, args);
		}
	}
}
