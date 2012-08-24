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
package org.sbml.squeezer.sabiork.wizard.gui;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import org.sbml.jsbml.Reaction;
import de.zbit.sbml.gui.ReactionPanel;

/**
 * A class that renders a {@link Reaction} as a LaTeX formula.
 * 
 * @author Matthias Rall
 * @version $Rev$
 * ${tags}
 */
public class TableCellRendererReactions implements TableCellRenderer {

	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		JPanel panel = new JPanel();
		Color colorForeground = table.getForeground();
		Color colorBackground = table.getBackground();
		Color colorSelectionForeground = table.getSelectionForeground();
		Color colorSelectionBackground = table.getSelectionBackground();
		if (value instanceof Reaction) {
			panel = new ReactionPanel((Reaction) value, true);
			table.setRowHeight(row, panel.getPreferredSize().height);
		}
		panel.setForeground(colorForeground);
		panel.setBackground(colorBackground);
		if (isSelected) {
			panel.setForeground(colorSelectionForeground);
			panel.setBackground(colorSelectionBackground);
		}
		return panel;
	}

}
