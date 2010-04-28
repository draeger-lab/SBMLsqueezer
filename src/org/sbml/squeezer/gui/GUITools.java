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
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;

import org.sbml.squeezer.resources.Resource;

/**
 * A convenient class that provides several handy methods for working with
 * graphical user interfaces. It loads all icons that are required by the GUI
 * and provides methods for changing color and state of GUI elements.
 * 
 * @author Andreas Dr&auml;ger
 * @author Hannes Borch
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
	public static Image IMAGE_LEMON = null;
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
	 * 
	 */
	public static Icon ICON_SETTINGS_TINY = null;

	/**
	 * 
	 */
	public static Icon ICON_STRUCTURAL_MODELING_TINY = null;

	/**
	 * 
	 */
	public static Icon ICON_LICENCE_TINY = null;
	/**
	 * 
	 */
	public static Icon ICON_FORWARD = null;

	/**
	 * Generated serial version id.
	 */
	private static final long serialVersionUID = 5662654607939013825L;

	static {
		loadImages("org/sbml/squeezer/resources/img/");
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
	 * Creates and returns a JCheckBox with all the given properties.
	 * 
	 * @param label
	 * @param selected
	 * @param name
	 *            The name for the component to be identifiable by the
	 *            ItemListener
	 * @param listener
	 * @param toolTip
	 * @return
	 */
	public static JCheckBox createJCheckBox(String label, boolean selected,
			String name, ItemListener listener, String toolTip) {
		JCheckBox chkbx = new JCheckBox(label, selected);
		chkbx.setName(name);
		chkbx.addItemListener(listener);
		chkbx.setToolTipText(toHTML(toolTip, 40));
		return chkbx;
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
		return icon == null ? new Dimension(0, 0) : new Dimension(icon
				.getIconWidth(), icon.getIconHeight());
	}

	/**
	 * 
	 * @param path
	 * @return
	 * @throws IOException
	 */
	private static Icon loadIcon(String path) {
		Image img = loadImage(path);
		return img != null ? new ImageIcon(img) : null;
	}

	/**
	 * 
	 * @param path
	 * @return
	 */
	private static Image loadImage(String path) {
		try {
			String p = path.substring(path.indexOf("img"));
			URL url = Resource.class.getResource(p);
			return url != null ? ImageIO.read(Resource.class.getResource(path
					.substring(path.indexOf("img")))) : ImageIO.read(new File(
					path));
		} catch (IOException exc) {
			System.err.printf("Could not load image %s\n", path);
			return null;
		}
	}

	/**
	 * Loads locale-specific resources: strings, images, et cetera
	 * 
	 * @param prefix
	 * @throws IOException
	 */
	private static void loadImages(String prefix) {
		ICON_FORWARD = loadIcon(prefix + "forward.png");
		// image = image.getScaledInstance(22, 22, Image.SCALE_SMOOTH);
		ICON_LEMON_TINY = loadIcon(prefix + "Lemon_tiny.png");
		ICON_LATEX_TINY = loadIcon(prefix + "SBML2LaTeX_vertical_tiny.png");
		ICON_LATEX_SMALL = loadIcon(prefix + "SBML2LaTeX_vertical_small.png");
		ICON_LEMON_SMALL = loadIcon(prefix + "Lemon_small.png");
		ICON_STABILITY_SMALL = loadIcon(prefix + "Stability_tiny.png");
		// .getScaledInstance(100, 100, Image.SCALE_SMOOTH));
		ICON_HELP_TINY = loadIcon(prefix + "help_16.png");
		ICON_INFO_TINY = loadIcon(prefix + "info_16.png");
		ICON_TICK_TINY = loadIcon(prefix + "tick_16.png");
		ICON_LEFT_ARROW = loadIcon(prefix + "back.png");
		ICON_DELETE = loadIcon(prefix + "delete_16.png");
		ICON_DIAGRAM_TINY = loadIcon(prefix + "diagram_16.png");
		ICON_TRASH_TINY = loadIcon(prefix + "trash_16.png");
		ICON_GEAR_TINY = loadIcon(prefix + "gear_16.png");
		ICON_PICTURE_TINY = loadIcon(prefix + "camera_16.png");
		ICON_SETTINGS_TINY = loadIcon(prefix + "settings_16.png");
		ICON_LICENCE_TINY = loadIcon(prefix + "licence_16.png");
		ICON_STRUCTURAL_MODELING_TINY = loadIcon(prefix + "steuer_icon.png");
		/*
		 * Icons with default from the system
		 */
		ICON_SAVE = UIManager.getIcon("FileView.floppyDriveIcon");
		if (ICON_SAVE == null) {
			ICON_SAVE = loadIcon(prefix + "save_16.png");
		}

		try {
			ResourceBundle resources = ResourceBundle
					.getBundle("samples.resources.bundles.MetalEditResources");
			String imagePath = resources.getString("images.path");
			ICON_OPEN = new ImageIcon(Resource.class.getResource(imagePath
					+ resources.getString("imageOpen")));
		} catch (Exception e) {
		}
		// UIManager.getIcon("FileView.directoryIcon");
		if (ICON_OPEN == null) {
			ICON_OPEN = loadIcon(prefix + "folder_16.png");
		}

		/*
		 * Icons to be read from image first.
		 */
		Image image = loadImage(prefix + "rightarrow.png");
		ICON_RIGHT_ARROW = image != null ? new ImageIcon(image
				.getScaledInstance(10, 10, Image.SCALE_SMOOTH)) : null;
		image = loadImage(prefix + "downarrow.png");
		ICON_DOWN_ARROW = image != null ? new ImageIcon(image
				.getScaledInstance(10, 10, Image.SCALE_SMOOTH)) : null;
		image = loadImage(prefix + "logo_small.png");
		// image = image.getScaledInstance(490, 150, Image.SCALE_SMOOTH);
		ICON_LOGO_SMALL = image != null ? new ImageIcon(image) : null;
		image = loadImage(prefix + "back.png");
		ICON_LEFT_ARROW_TINY = image != null ? new ImageIcon(image
				.getScaledInstance(16, 16, Image.SCALE_SMOOTH)) : null;

		/*
		 * Images
		 */
		IMAGE_LEMON = loadImage(prefix + "icon.png");
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
	 * Enables or disables actions that can be performed by SBMLsqueezer, i.e.,
	 * all menu items and buttons that are associated with the given actions are
	 * enabled or disabled.
	 * 
	 * @param state
	 *            if true buttons, items etc. are enabled, otherwise disabled.
	 * @param menuBar
	 * @param toolbar
	 * @param commands
	 */
	public static void setEnabled(boolean state, JMenuBar menuBar,
			JToolBar toolbar, Object... commands) {
		int i, j;
		Set<String> setOfCommands = new HashSet<String>();
		for (Object command : commands)
			setOfCommands.add(command.toString());
		if (menuBar != null)
			for (i = 0; i < menuBar.getMenuCount(); i++) {
				JMenu menu = menuBar.getMenu(i);
				for (j = 0; j < menu.getItemCount(); j++) {
					JMenuItem item = menu.getItem(j);
					if (item instanceof JMenu) {
						JMenu m = (JMenu) item;
						boolean containsCommand = false;
						for (int k = 0; k < m.getItemCount(); k++) {
							JMenuItem it = m.getItem(k);
							if (it != null
									&& it.getActionCommand() != null
									&& setOfCommands.contains(it
											.getActionCommand())) {
								it.setEnabled(state);
								containsCommand = true;
							}
						}
						if (containsCommand)
							m.setEnabled(state);
					}
					if (item != null && item.getActionCommand() != null
							&& setOfCommands.contains(item.getActionCommand()))
						item.setEnabled(state);
				}
			}
		if (toolbar != null)
			for (i = 0; i < toolbar.getComponentCount(); i++) {
				Object o = toolbar.getComponent(i);
				if (o instanceof JButton) {
					JButton b = (JButton) o;
					if (setOfCommands.contains(b.getActionCommand())) {
						b.setEnabled(state);
						if (b.getIcon() != null
								&& b.getIcon() instanceof CloseIcon)
							((CloseIcon) b.getIcon())
									.setColor(state ? Color.BLACK : Color.GRAY);
					}
				}
			}
	}

	/**
	 * 
	 * @param state
	 * @param menuBar
	 * @param commands
	 */
	public static void setEnabled(boolean state, JMenuBar menuBar,
			Object... commands) {
		setEnabled(state, menuBar, null, commands);
	}

	/**
	 * 
	 * @param state
	 * @param toolbar
	 * @param commands
	 */
	public static void setEnabled(boolean state, JToolBar toolbar,
			Object... commands) {
		setEnabled(state, null, toolbar, commands);
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
