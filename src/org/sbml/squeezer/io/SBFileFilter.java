/**
 * Aug 3, 2007
 *
 * @since 2.0
 * @author Andreas Dr&auml;ger (draeger) <andreas.draeger@uni-tuebingen.de>
 *         Copyright (c) ZBiT, University of T&uuml;bingen, Germany Compiler:
 *         JDK 1.6.0
 */
package org.sbml.squeezer.io;

import java.io.File;

import javax.swing.filechooser.FileFilter;

/**
 * A file filter implementation for TeX and text files. It also accepts
 * directories. Otherwise one could not browse in the file system.
 * 
 * @since 2.0
 * @version
 * @author Andreas Dr&auml;ger (draeger) <andreas.draeger@uni-tuebingen.de>
 *         Copyright (c) ZBiT, University of T&uuml;bingen, Germany Compiler:
 *         JDK 1.6.0 Aug 3, 2007
 */
public class SBFileFilter extends FileFilter implements java.io.FileFilter {

	/**
	 * True if this filter accepts plain ASCII files
	 */
	public static final short TEXT_FILES = 0;

	/**
	 * True if this filter accepts (La)TeX files.
	 */
	public static final short TeX_FILES = 1;

	/**
	 * To be selected if SBML files (XML files) can be choosen.
	 */
	public static final short SBML_FILES = 2;

	private short type;

	/**
	 * Constructs a file filter that accepts or not accepts the following files
	 * (defined by the given parameters).
	 * 
	 * @param type
	 *            One of the short numbers defined in this class.
	 */
	public SBFileFilter(short type) {
		this.type = type;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
	 */
	// @Override
	public boolean accept(File f) {
		if (f.isDirectory() || (type == TEXT_FILES && isTextFile(f))
				|| (type == TeX_FILES && isTeXFile(f))
				|| (type == SBML_FILES && isSBMLFile(f)))
			return true;
		return false;
	}

	/**
	 * Returns true if this file filter accepts SBML files.
	 * 
	 * @return
	 */
	public boolean acceptsSBMLFiles() {
		return type == SBML_FILES;
	}

	public boolean acceptsTeXFiles() {
		return type == TeX_FILES;
	}

	public boolean acceptsTextFiles() {
		return type == TEXT_FILES;
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
		default:
			return "";
		}
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
