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
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileFilter;

import org.sbml.squeezer.resources.Resource;

/**
 * A convenient class that provides several handy methods for working with
 * graphical user interfaces. It loads all icons that are required by the GUI
 * and provides methods for changing color and state of GUI elements.
 * 
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * @author <a href="mailto:hannes.borch@googlemail.com">Hannes Borch</a>
 * @since 1.3
 * @date 2009-09-04
 */
public class GUITools {

	/**
	 * 
	 */
	public static Icon ICON_DELETE = null;
	/**
	 * 
	 */
	public static Icon ICON_DIAGRAM_TINY = null;
	/**
	 * 
	 */
	public static Icon ICON_DOWN_ARROW = null;
	/**
	 * 
	 */
	public static Icon ICON_GEAR_TINY = null;
	/**
	 * 
	 */
	public static Icon ICON_HELP_TINY = null;
	/**
	 * 
	 */
	public static Icon ICON_INFO_TINY = null;
	/**
	 * 
	 */
	public static Icon ICON_LATEX_SMALL = null;
	/**
	 * 
	 */
	public static Icon ICON_LATEX_TINY = null;
	/**
	 * 
	 */
	public static Icon ICON_LEFT_ARROW = null;
	/**
	 * 
	 */
	public static Icon ICON_LEFT_ARROW_TINY = null;
	/**
	 * 
	 */
	public static Image ICON_LEMON = null;
	/**
	 * 
	 */
	public static Icon ICON_LEMON_SMALL = null;
	/**
	 * 
	 */
	public static Icon ICON_LEMON_TINY = null;
	/**
	 * 
	 */
	public static Icon ICON_LOGO_SMALL = null;
	/**
	 * 
	 */
	public static Icon ICON_OPEN = null;
	/**
	 * 
	 */
	public static Icon ICON_RIGHT_ARROW = null;
	/**
	 * 
	 */
	public static Icon ICON_SAVE = null;

	/**
	 * 
	 */
	public static Icon ICON_STABILITY_SMALL = null;
	/**
	 * 
	 */
	public static Icon ICON_TICK_TINY = null;
	/**
	 * 
	 */
	public static Icon ICON_TRASH_TINY = null;
	/**
	 * 
	 */
	public static Icon ICON_PICTURE_TINY = null;

	/**
	 * Generated serial version id.
	 */
	private static final long serialVersionUID = 5662654607939013825L;

	static {
		try {
			ICON_LEMON_TINY = new ImageIcon(ImageIO.read(Resource.class
					.getResource("img/Lemon_tiny.png")));
			ICON_LATEX_TINY = new ImageIcon(ImageIO.read(Resource.class
					.getResource("img/SBML2LaTeX_vertical_tiny.png")));
			ICON_LATEX_SMALL = new ImageIcon(ImageIO.read(Resource.class
					.getResource("img/SBML2LaTeX_vertical_small.png")));
			ICON_LEMON_SMALL = new ImageIcon(ImageIO.read(Resource.class
					.getResource("img/Lemon_small.png")));
			ICON_STABILITY_SMALL = new ImageIcon(ImageIO.read(Resource.class
					.getResource("img/Stability_tiny.png")));
			// .getScaledInstance(100, 100, Image.SCALE_SMOOTH));
			Image image = ImageIO.read(Resource.class
					.getResource("img/rightarrow.png"));
			ICON_RIGHT_ARROW = new ImageIcon(image.getScaledInstance(10, 10,
					Image.SCALE_SMOOTH));
			image = ImageIO.read(Resource.class
					.getResource("img/downarrow.png"));
			ICON_DOWN_ARROW = new ImageIcon(image.getScaledInstance(10, 10,
					Image.SCALE_SMOOTH));
			image = ImageIO.read(Resource.class
					.getResource("img/logo_small.png")); // title_small.jpg
			// image = image.getScaledInstance(490, 150, Image.SCALE_SMOOTH);
			ICON_LOGO_SMALL = new ImageIcon(image);
			ICON_LEMON = ImageIO.read(Resource.class
					.getResource("img/icon.png"));
			ICON_HELP_TINY = new ImageIcon(Resource.class
					.getResource("img/help_16.png"));
			ICON_INFO_TINY = new ImageIcon(Resource.class
					.getResource("img/info_16.png"));
			ICON_TICK_TINY = new ImageIcon(Resource.class
					.getResource("img/tick_16.png"));
			ICON_LEFT_ARROW = new ImageIcon(Resource.class
					.getResource("img/back.png"));
			image = ImageIO.read(Resource.class.getResource("img/back.png"))
					.getScaledInstance(16, 16, Image.SCALE_SMOOTH);
			ICON_LEFT_ARROW_TINY = new ImageIcon(image);
			ICON_DELETE = new ImageIcon(Resource.class
					.getResource("img/delete_16.png"));
			ICON_SAVE = UIManager.getIcon("FileView.floppyDriveIcon");
			if (ICON_SAVE == null)
				ICON_SAVE = new ImageIcon(Resource.class
						.getResource("img/save_16.png"));
			ICON_OPEN = UIManager.getIcon("FileView.directoryIcon");
			if (ICON_OPEN == null)
				ICON_OPEN = new ImageIcon(Resource.class
						.getResource("img/folder_16.png"));
			ICON_DIAGRAM_TINY = new ImageIcon(Resource.class
					.getResource("img/diagram_16.png"));
			ICON_TRASH_TINY = new ImageIcon(Resource.class
					.getResource("img/trash_16.png"));
			ICON_GEAR_TINY = new ImageIcon(Resource.class
					.getResource("img/gear_16.png"));
			ICON_PICTURE_TINY = new ImageIcon(Resource.class
					.getResource("img/camera_16.png"));
		} catch (IOException e) {
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
	 * @param text
	 * @param icon
	 * @param listener
	 * @param command
	 * @param toolTip
	 * @return
	 */
	public static JButton createButton(String text, Icon icon,
			ActionListener listener, Object command, String toolTip) {
		JButton button = createButton(icon, listener, command, toolTip);
		if (text != null)
			button.setText(text);
		return button;
	}

	/**
	 * Creates a JButton with the given properties. The tool tip becomes an HTML
	 * formatted string with a line break after 40 symbols.
	 * 
	 * @param icon
	 * @param listener
	 * @param com
	 * @param toolTip
	 * @return
	 */
	public static JButton createButton(Icon icon, ActionListener listener,
			Object command, String toolTip) {
		JButton button = new JButton();
		if (icon != null)
			button.setIcon(icon);
		if (listener != null)
			button.addActionListener(listener);
		if (command != null)
			button.setActionCommand(command.toString());
		if (toolTip != null)
			button.setToolTipText(toHTML(toolTip, 40));
		return button;
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
		chooser.setAcceptAllFileFilterUsed(allFilesAcceptable);
		chooser.setMultiSelectionEnabled(multiSelectionAllowed);
		chooser.setFileSelectionMode(mode);
		int i = filter.length - 1;
		while (0 <= i)
			chooser.addChoosableFileFilter(filter[i--]);
		if (i >= 0)
			chooser.setFileFilter(filter[i]);
		return chooser;
	}

	/**
	 * Creates an entry for the menu bar.
	 * 
	 * @param text
	 * @param ks
	 * @param command
	 * @param listener
	 * @param icon
	 * @return
	 */
	public static JMenuItem createMenuItem(String text, KeyStroke ks,
			Object command, ActionListener listener, Icon icon) {
		return createMenuItem(text, ks, command, null, listener, icon);
	}

	/**
	 * Creates an entry for the menu bar.
	 * 
	 * @param text
	 * @param ks
	 * @param command
	 * @param mnemonic
	 * @param listener
	 * @param icon
	 * @return
	 */
	public static JMenuItem createMenuItem(String text, KeyStroke ks,
			Object command, Character mnemonic, ActionListener listener,
			Icon icon) {
		JMenuItem item = new JMenuItem();
		if (text != null)
			item.setText(text);
		if (ks != null)
			item.setAccelerator(ks);
		if (listener != null)
			item.addActionListener(listener);
		if (mnemonic != null)
			item.setMnemonic(mnemonic.charValue());
		if (command != null)
			item.setActionCommand(command.toString());
		if (icon != null)
			item.setIcon(icon);
		return item;
	}

	/**
	 * Creates an entry for the menu bar.
	 * 
	 * @param text
	 * @param command
	 * @param listener
	 * @return
	 */
	public static JMenuItem createMenuItem(String text, Object command,
			ActionListener listener) {
		return createMenuItem(text, null, command, listener, null);
	}

	/**
	 * Creates an entry for the menu bar.
	 * 
	 * @param text
	 * @param command
	 * @param listener
	 * @param icon
	 * @return
	 */
	public static JMenuItem createMenuItem(String text, Object command,
			ActionListener listener, Icon icon) {
		return createMenuItem(text, null, command, listener, icon);
	}

	/**
	 * Creates an entry for the menu bar.
	 * 
	 * @param text
	 * @param command
	 * @param mnemonic
	 * @param listener
	 * @param icon
	 * @return
	 */
	public static JMenuItem createMenuItem(String text, Object command,
			char mnemonic, ActionListener listener, Icon icon) {
		return createMenuItem(text, null, command, mnemonic, listener, icon);
	}

	/**
	 * Computes and returns the dimension, i.e., the size of a given icon.
	 * 
	 * @param icon
	 *            an icon whose dimension is required.
	 * @return The dimension of the given icon.
	 */
	public static Dimension getDimension(Icon icon) {
		return new Dimension(icon.getIconWidth(), icon.getIconHeight());
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

	/**
	 * 
	 * @param parent
	 * @param out
	 * @return
	 */
	public static boolean overwriteExistingFile(Component parent, File out) {
		return GUITools.overwriteExistingFileDialog(parent, out) == JOptionPane.YES_OPTION;
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
	 * 
	 * @param string
	 * @return
	 */
	public static String toHTML(String string) {
		return toHTML(string, Integer.MAX_VALUE);
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
			if (length >= lineBreak && lineBreak < Integer.MAX_VALUE) {
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
}
