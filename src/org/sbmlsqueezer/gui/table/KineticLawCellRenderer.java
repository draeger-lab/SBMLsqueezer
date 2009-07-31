package org.sbmlsqueezer.gui.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.TableCellRenderer;

/**
 * TODO: comment missing
 * 
 * @since 2.0
 * @version
 * @author Nadine Hassis <Nadine.hassis@gmail.com>
 * @author Andreas Dr&auml;ger (draeger) <andreas.draeger@uni-tuebingen.de>
 *         Copyright (c) ZBiT, University of T&uuml;bingen, Germany Compiler:
 *         JDK 1.5.0
 * @date Aug 1, 2007
 */
public class KineticLawCellRenderer extends JTextArea implements
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
	public KineticLawCellRenderer(int maxSpecies) {
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
