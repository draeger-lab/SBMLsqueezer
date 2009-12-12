/**
 * Aug 3, 2007
 *
 * @since 2.0
 * @author Andreas Dr&auml;ger (draeger) <andreas.draeger@uni-tuebingen.de>
 *         Copyright (c) ZBiT, University of T&uuml;bingen, Germany Compiler:
 *         JDK 1.6.0
 */
package org.sbmlsqueezer.io;

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
public class MyFileFilter extends FileFilter implements java.io.FileFilter {

	/**
	 * True if this filter accepts plain ASCII files
	 */
	private boolean	textFiles;

	/**
	 * True if this filter accepts (La)TeX files.
	 */
	private boolean	TeXFiles;

	/**
	 * Constructs a file filter that accepts or not accepts the following files
	 * (defined by the given parameters).
	 *
	 * @param acceptTextFiles
	 *          True if this filter accepts plain ASCII files
	 * @param acceptTeXfiles
	 *          True if this filter accepts (La)TeX files.
	 */
	public MyFileFilter(boolean acceptTextFiles, boolean acceptTeXfiles) {
		this.textFiles = acceptTextFiles;
		this.TeXFiles = acceptTeXfiles;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
	 */
	@Override
	public boolean accept(File f) {
		if (f.isDirectory() || (textFiles && isTextFile(f))
		    || (TeXFiles && isTeXFile(f))) return true;
		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see javax.swing.filechooser.FileFilter#getDescription()
	 */
	@Override
	public String getDescription() {
		String description = "";
		if (textFiles) description += "Text files (*.txt)";
		if (description.length() > 0) description += ", ";
		if (TeXFiles) description += "TeX files (*.tex)";
		return description;
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
