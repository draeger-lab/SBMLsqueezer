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
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JToolBar;
import javax.swing.UIManager;

import org.sbml.jsbml.UnitDefinition;
import org.sbml.squeezer.resources.Resource;
import org.sbml.squeezer.util.HTMLFormula;

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
public class GUITools extends de.zbit.gui.GUITools {

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
	 * 
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public static Icon loadIcon(String path) {
		Image img = loadImage(path);
		return img != null ? new ImageIcon(img) : null;
	}



	/**
	 * 
	 * @param path
	 * @return
	 */
	public static Image loadImage(String path) {
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
	 * Creates a JEditorPane that displays the given UnitDefinition as a HTML.
	 * 
	 * @param ud
	 * @return
	 */
	public static JEditorPane unitPreview(UnitDefinition ud) {
		JEditorPane preview = new JEditorPane("text/html", GUITools
				.toHTML(ud != null ? HTMLFormula.toHTML(ud) : ""));
		preview.setEditable(false);
		preview.setBorder(BorderFactory.createLoweredBevelBorder());
		return preview;
	}
}
