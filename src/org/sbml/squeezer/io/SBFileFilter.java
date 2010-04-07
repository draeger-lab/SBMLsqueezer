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

import java.io.File;

import javax.swing.filechooser.FileFilter;

/**
 * A file filter implementation for TeX and text files. It also accepts
 * directories. Otherwise one could not browse in the file system.
 * 
 * @since 1.0
 * @date 2007-08-03
 * @version
 * @author Andreas Dr&auml;ger
 */
public class SBFileFilter extends FileFilter implements java.io.FileFilter {

	/**
	 * 
	 * @author Andreas Dr&auml;ger
	 * @since 1.4
	 *
	 */
	public static enum FileType {
		/**
		 * To be selected if CSV files (comma separated files) can be chosen.
		 */
		CSV_FILES,
		/**
		 * To be selected if SBML files (XML files) can be chosen.
		 */
		SBML_FILES,
		/**
		 * True if this filter accepts (La)TeX files.
		 */
		TeX_FILES,
		/**
		 * True if this filter accepts plain ASCII files
		 */
		TEXT_FILES
	}

	/**
	 * A filter for CSV files
	 */
	public static SBFileFilter CSV_FILE_FILTER = new SBFileFilter(
			FileType.CSV_FILES);

	/**
	 * A filter for SBML files
	 */
	public static final SBFileFilter SBML_FILE_FILTER = new SBFileFilter(
			FileType.SBML_FILES);

	/**
	 * A filter for TeX files
	 */
	public static final SBFileFilter TeX_FILE_FILTER = new SBFileFilter(
			FileType.TeX_FILES);

	/**
	 * A filter for Text files.
	 */
	public static final SBFileFilter TEXT_FILE_FILTER = new SBFileFilter(
			FileType.TEXT_FILES);

	/**
	 * Allowable file type.
	 */
	private FileType type;

	/**
	 * Constructs a file filter that accepts or not accepts the following files
	 * (defined by the given parameters).
	 * 
	 * @param type
	 *            One of the short numbers defined in this class.
	 */
	public SBFileFilter(FileType type) {
		this.type = type;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
	 */
	// @Override
	public boolean accept(File f) {
		if ((f.isDirectory() || (type == FileType.TEXT_FILES && isTextFile(f)))
				|| (type == FileType.TeX_FILES && isTeXFile(f))
				|| (type == FileType.SBML_FILES && isSBMLFile(f))
				|| (type == FileType.CSV_FILES && isCSVFile(f)))
			return true;
		return false;
	}

	/**
	 * 
	 * @return
	 */
	public boolean acceptsCSVFiles() {
		return type == FileType.CSV_FILES;
	}

	/**
	 * Returns true if this file filter accepts SBML files.
	 * 
	 * @return
	 */
	public boolean acceptsSBMLFiles() {
		return type == FileType.SBML_FILES;
	}

	/**
	 * 
	 * @return
	 */
	public boolean acceptsTeXFiles() {
		return type == FileType.TeX_FILES;
	}

	/**
	 * 
	 * @return
	 */
	public boolean acceptsTextFiles() {
		return type == FileType.TEXT_FILES;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.filechooser.FileFilter#getDescription()
	 */
	// @Override
	public String getDescription() {
		switch (type) {
		case TEXT_FILES:
			return "Text files (*.txt)";
		case TeX_FILES:
			return "TeX files (*.tex)";
		case SBML_FILES:
			return "SBML files (*.sbml, *.xml)";
		case CSV_FILES:
			return "Comma separated files (*.csv)";
		default:
			return "";
		}
	}

	/**
	 * 
	 * @param f
	 * @return
	 */
	public boolean isCSVFile(File f) {
		return f.getName().toLowerCase().endsWith(".csv");
	}

	/**
	 * Returns true if the given file is an SBML file.
	 * 
	 * @param f
	 * @return
	 */
	public boolean isSBMLFile(File f) {
		String extension = f.getName().toLowerCase();
		return extension.endsWith(".xml") || extension.endsWith(".sbml");
	}

	/**
	 * Returns true if the given file is a TeX file.
	 * 
	 * @param f
	 * @return
	 */
	public boolean isTeXFile(File f) {
		return f.getName().toLowerCase().endsWith(".tex");
	}

	/**
	 * Returns true if the given file is a text file.
	 * 
	 * @param f
	 * @return
	 */
	public boolean isTextFile(File f) {
		return f.getName().toLowerCase().endsWith(".txt");
	}
}
