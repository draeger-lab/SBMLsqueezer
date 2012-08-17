/*
 * $$Id${file_name} ${time} ${user} $$
 * $$URL${file_name} $$
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
import java.awt.Font;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

/**
 * A class that allows to create a combo box with non-selectable captions.
 * 
 * @author Matthias Rall
 * @version $$Rev$$
 * ${tags}
 */
@SuppressWarnings("serial")
public class ComboBoxModelCaptions extends DefaultComboBoxModel {

	private Set<Integer> captionIndices;

	public ComboBoxModelCaptions() {
		this.captionIndices = new HashSet<Integer>();
	}

	/**
	 * Adds a caption to the model.
	 * 
	 * @param anObject
	 */
	public void addCaption(Object anObject) {
		super.addElement(anObject);
		captionIndices.add(getSize() - 1);
	}

	public void insertElementAt(Object anObject, int index) {
		List<Object> captions = new ArrayList<Object>();
		for (int captionIndex : captionIndices) {
			captions.add(getElementAt(captionIndex));
		}
		super.insertElementAt(anObject, index);
		captionIndices.clear();
		for (Object caption : captions) {
			captionIndices.add(getIndexOf(caption));
		}
	}

	public void removeAllElements() {
		super.removeAllElements();
		captionIndices.clear();
	}

	public void removeElement(Object anObject) {
		super.removeElement(anObject);
		captionIndices.remove(getIndexOf(anObject));
	}

	public void removeElementAt(int index) {
		captionIndices.remove(index);
		List<Object> captions = new ArrayList<Object>();
		for (int captionIndex : captionIndices) {
			captions.add(getElementAt(captionIndex));
		}
		super.removeElementAt(index);
		captionIndices.clear();
		for (Object caption : captions) {
			captionIndices.add(getIndexOf(caption));
		}
	}

	public void setSelectedItem(Object anObject) {
		if (!captionIndices.contains(getIndexOf(anObject))) {
			super.setSelectedItem(anObject);
		}
	}

	/**
	 * Returns the Renderer that is needed to display the captions correctly.
	 * 
	 * @return
	 */
	public ComboBoxRendererCaptions getRenderer() {
		return new ComboBoxRendererCaptions();
	}

	private class ComboBoxRendererCaptions extends BasicComboBoxRenderer {

		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			JLabel label = new JLabel();
			Border border = new EmptyBorder(new Insets(0, 15, 0, 0));
			Font font = list.getFont();
			Color colorForeground = list.getForeground();
			Color colorBackground = list.getBackground();
			Color colorSelectionForeground = list.getSelectionForeground();
			Color colorSelectionBackground = list.getSelectionBackground();
			if (value != null) {
				label.setText(value.toString());
			}
			ListModel listModel = list.getModel();
			if (listModel instanceof ComboBoxModelCaptions) {
				if (((ComboBoxModelCaptions) listModel).captionIndices
						.contains(index)) {
					border = new EmptyBorder(new Insets(5, 0, 5, 0));
					font = new Font(list.getFont().getName(), Font.BOLD, list
							.getFont().getSize());
					if (isSelected) {
						colorSelectionForeground = colorForeground;
						colorSelectionBackground = colorBackground;
					}
				}
			}
			if (index == -1) {
				border = new EmptyBorder(new Insets(0, 0, 0, 0));
			}
			label.setOpaque(true);
			label.setBorder(border);
			label.setFont(font);
			label.setForeground(colorForeground);
			label.setBackground(colorBackground);
			if (isSelected) {
				label.setForeground(colorSelectionForeground);
				label.setBackground(colorSelectionBackground);
			}
			return label;
		}

	}

}