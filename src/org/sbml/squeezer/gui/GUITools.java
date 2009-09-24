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
package org.sbml.squeezer.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileFilter;

import org.sbml.squeezer.resources.Resource;

/**
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * @author <a href="mailto:hannes.borch@googlemail.com">Hannes Borch</a>
 * @since 1.2.2
 * @date 2009-09-04
 */
public class GUITools {

	public static Icon LATEX_ICON_TINY;
	public static Icon LATEX_ICON_SMALL;
	public static Icon LEMON_ICON_TINY;
	public static Icon LEMON_ICON_SMALL;
	public static Icon RIGHT_ARROW;
	public static Icon DOWN_ARROW;
	public static Icon LOGO_SMALL;
	public static Image LEMON_ICON;

	/**
	 * Generated serial version id.
	 */
	private static final long serialVersionUID = 5662654607939013825L;

	static {
		try {
			LEMON_ICON_TINY = new ImageIcon(ImageIO.read(Resource.class
					.getResource("img/Lemon_tiny.png")));
			LATEX_ICON_TINY = new ImageIcon(ImageIO.read(Resource.class
					.getResource("img/SBML2LaTeX_vertical_tiny.png")));
			LATEX_ICON_SMALL = new ImageIcon(ImageIO.read(Resource.class
					.getResource("img/SBML2LaTeX_vertical_small.png")));
			LEMON_ICON_SMALL = new ImageIcon(ImageIO.read(Resource.class
					.getResource("img/Lemon_small.png")));
			// .getScaledInstance(100, 100, Image.SCALE_SMOOTH));
			Image image = ImageIO.read(Resource.class
					.getResource("img/rightarrow.png"));
			RIGHT_ARROW = new ImageIcon(image.getScaledInstance(10, 10,
					Image.SCALE_SMOOTH));
			image = ImageIO.read(Resource.class
					.getResource("img/downarrow.png"));
			DOWN_ARROW = new ImageIcon(image.getScaledInstance(10, 10,
					Image.SCALE_SMOOTH));
			image = ImageIO.read(Resource.class
					.getResource("img/logo_small.png")); // title_small.jpg
			// image = image.getScaledInstance(490, 150, Image.SCALE_SMOOTH);
			LOGO_SMALL = new ImageIcon(image);
			LEMON_ICON = ImageIO.read(Resource.class
					.getResource("img/icon.png"));
		} catch (IOException e) {
			LATEX_ICON_TINY = null;
			LATEX_ICON_SMALL = null;
			LEMON_ICON_TINY = null;
			LEMON_ICON_SMALL = null;
			RIGHT_ARROW = null;
			DOWN_ARROW = null;
			LOGO_SMALL = null;
			LEMON_ICON = null;
			JOptionPane.showMessageDialog(null, toHTML(e.getMessage(), 40), e
					.getClass().getName(), JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException exc) {
			JOptionPane.showMessageDialog(null, toHTML(exc.getMessage(), 40),
					exc.getClass().getName(), JOptionPane.WARNING_MESSAGE);
			exc.printStackTrace();
		} catch (InstantiationException exc) {
			JOptionPane.showMessageDialog(null, toHTML(exc.getMessage(), 40),
					exc.getClass().getName(), JOptionPane.WARNING_MESSAGE);
			exc.printStackTrace();
		} catch (IllegalAccessException exc) {
			JOptionPane.showMessageDialog(null, GUITools.toHTML(exc
					.getMessage(), 40), exc.getClass().getName(),
					JOptionPane.WARNING_MESSAGE);
			exc.printStackTrace();
		} catch (UnsupportedLookAndFeelException exc) {
			JOptionPane.showMessageDialog(null, toHTML(exc.getMessage(), 40),
					exc.getClass().getName(), JOptionPane.WARNING_MESSAGE);
			exc.printStackTrace();
		}
	}

	/**
	 * Returns a HTML formated String, in which each line is at most lineBreak
	 * symbols long.
	 * 
	 * @param string
	 * @param lineBreak
	 * @return
	 */
	public static String toHTML(String string, int lineBreak) {
		StringTokenizer st = new StringTokenizer(string != null ? string : "",
				" ");
		StringBuilder sb = new StringBuilder();
		if (st.hasMoreElements())
			sb.append(st.nextElement().toString());
		int length = sb.length();
		sb.insert(0, "<html><body>");
		while (st.hasMoreElements()) {
			if (length >= lineBreak) {
				sb.append("<br>");
				length = 0;
			} else
				sb.append(' ');
			String tmp = st.nextElement().toString();
			length += tmp.length() + 1;
			sb.append(tmp);
		}
		sb.append("</body></html>");
		return sb.toString();
	}

	/**
	 * 
	 * @param c
	 * @param color
	 */
	public static void setAllBackground(Container c, Color color) {
		c.setBackground(color);
		Component children[] = c.getComponents();
		for (int i = 0; i < children.length; i++) {
			if (children[i] instanceof Container)
				setAllBackground((Container) children[i], color);
			children[i].setBackground(color);
		}
	}

	/**
	 * Checks whether the first container contains the second one.
	 * 
	 * @param c
	 * @param insight
	 * @return True if c contains insight.
	 */
	public static boolean contains(Component c, Component insight) {
		boolean contains = c.equals(insight);
		if ((c instanceof Container) && !contains)
			for (Component c1 : ((Container) c).getComponents()) {
				if (c1.equals(insight))
					return true;
				else
					contains |= contains(c1, insight);
			}
		return contains;
	}

	/**
	 * 
	 * @param c
	 * @param enabled
	 */
	public static void setAllEnabled(Container c, boolean enabled) {
		Component children[] = c.getComponents();
		for (int i = 0; i < children.length; i++) {
			if (children[i] instanceof Container)
				setAllEnabled((Container) children[i], enabled);
			children[i].setEnabled(enabled);
		}
	}

	/**
	 * Creates and sets up a JFileFilter.
	 * 
	 * @param dir
	 *            Start directory
	 * @param allFilesAcceptable
	 *            if true, all files are available besides the file filters
	 * @param multiSelectionAllowed
	 *            if true more the one file can be selected.
	 * @param mode
	 *            one of the Options in JFileChooser
	 * @param filter
	 *            no, one or several file filters.
	 * @return Returns a file filter with the desired properties.
	 */
	public static JFileChooser createJFileChooser(String dir,
			boolean allFilesAcceptable, boolean multiSelectionAllowed,
			int mode, FileFilter... filter) {
		JFileChooser chooser = new JFileChooser(dir);
		int i = 0;
		if (!allFilesAcceptable && filter.length > 0)
			chooser.setFileFilter(filter[i++]);
		while (i < filter.length)
			chooser.addChoosableFileFilter(filter[i++]);
		chooser.setAcceptAllFileFilterUsed(allFilesAcceptable);
		chooser.setMultiSelectionEnabled(multiSelectionAllowed);
		chooser.setFileSelectionMode(mode);
		return chooser;
	}

	/**
	 * Shows a dialog that asks whether or not to overwrite an existing file and
	 * returns the answer from JOptionPane constants.
	 * 
	 * @param parent
	 * @param out
	 * @return An integer representing the user's choice.
	 */
	public static int overwriteExistingFileDialog(Component parent, File out) {
		return JOptionPane.showConfirmDialog(parent, toHTML(out.getName()
				+ " already exists. Do you really want to over write it?", 40),
				"Over write existing file?", JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE);
	}
}
