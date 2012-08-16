package sabiork.wizard.gui;

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
 * 
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
