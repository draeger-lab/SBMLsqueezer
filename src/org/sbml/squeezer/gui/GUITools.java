/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of SBMLsqueezer, a Java program that creates rate 
 * equations for reactions in SBML files (http://sbml.org).
 *
 * Copyright (C) 2006-2012 by the University of Tuebingen, Germany.
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
package org.sbml.squeezer.gui;

import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JToolBar;

/**
 * A convenient class that provides several handy methods for working with
 * graphical user interfaces. It loads all icons that are required by the GUI
 * and provides methods for changing color and state of GUI elements.
 * 
 * @author Andreas Dr&auml;ger
 * @author Hannes Borch
 * @since 1.3
 * @date 2009-09-04
 * @version $Rev$
 */
public class GUITools extends de.zbit.gui.GUITools {

//	/**
//	 * Loads locale-specific resources: strings, images, et cetera
//	 * 
//	 * @param prefix
//	 * @throws IOException
//	 */
//	public static void loadImages(String prefix) {
//		UIManager.put("ICON_LEFT_ARROW_TINY", image != null ? new ImageIcon(
//				image.getScaledInstance(16, 16, Image.SCALE_SMOOTH)) : null);
//	}

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
		for (Object command : commands) {
			setOfCommands.add(command.toString());
		}
		if (menuBar != null) {
			for (i = 0; i < menuBar.getMenuCount(); i++) {
				JMenu menu = menuBar.getMenu(i);
				for (j = 0; j < menu.getItemCount(); j++) {
					JMenuItem item = menu.getItem(j);
					if (item instanceof JMenu) {
						JMenu m = (JMenu) item;
						boolean containsCommand = false;
						for (int k = 0; k < m.getItemCount(); k++) {
							JMenuItem it = m.getItem(k);
							if ((it != null)
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
		}
		if (toolbar != null) {
			for (i = 0; i < toolbar.getComponentCount(); i++) {
				Object o = toolbar.getComponent(i);
				if (o instanceof JButton) {
					JButton b = (JButton) o;
					if (setOfCommands.contains(b.getActionCommand())) {
						b.setEnabled(state);
						if (b.getIcon() != null
								&& (b.getIcon() instanceof CloseIcon))
							((CloseIcon) b.getIcon())
									.setColor(state ? Color.BLACK : Color.GRAY);
					}
				}
			}
		}
	}
}
