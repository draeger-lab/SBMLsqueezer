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
import java.awt.Font;

import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.TableCellRenderer;

/**
 * A renderer that paints the background of every second row white or in light
 * blue. For rows containing errors the background is set to a light red.
 * 
 * @since 1.0
 * @version
 * @author Nadine Hassis <Nadine.hassis@gmail.com>
 * @author Andreas Dr&auml;ger (draeger) <andreas.draeger@uni-tuebingen.de>
 * @date Aug 1, 2007
 */
public class KineticLawTableCellRenderer extends JTextArea implements
		TableCellRenderer {

	/**
	 * Generated serialziation id.
	 */
	private static final long serialVersionUID = -7760600735675079594L;

	private int maxNumberOfSpecies;

	public static final Color lightBlue = new Color(205, 225, 255, 50);
	public static final Color lightRed = new Color(255, 85, 85);
	public static final Color slateGray3 = new Color(159, 182, 205);

	/**
	 * TODO: Comment is missing
	 * 
	 * @param maxSpecies
	 */
	public KineticLawTableCellRenderer(int maxSpecies) {
		this.maxNumberOfSpecies = maxSpecies;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax
	 * .swing.JTable, java.lang.Object, boolean, boolean, int, int)
	 */
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		table.setGridColor(slateGray3);
		table.setBackground(Color.WHITE);
		int eductCol = 0;
		while ((eductCol < table.getColumnCount())
				&& !(table.getModel().getValueAt(row, eductCol) instanceof Double))
			eductCol++;
		double numEducts = ((Double) table.getModel().getValueAt(row, eductCol))
				.doubleValue();
		if (numEducts >= maxNumberOfSpecies) {
			setBackground(lightRed);
			setForeground(Color.WHITE);
			setFont(getFont().deriveFont(Font.PLAIN));
		} else {
			if (row % 2 == 0)
				setBackground(Color.WHITE);
			else
				setBackground(lightBlue);
			setForeground(Color.BLACK);
			setFont(getFont().deriveFont(Font.PLAIN));
		}
		if (table.convertColumnIndexToModel(column) == eductCol) {
			if (numEducts - ((int) numEducts) == 0.0)
				setText(Integer.toString((int) numEducts));
			else
				setText(Double.toString(numEducts));
		} else if (value instanceof String)
			setText((String) value);
		else
			setText(value.toString());
		return this;
	}
}
