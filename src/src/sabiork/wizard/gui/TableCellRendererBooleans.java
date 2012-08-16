package sabiork.wizard.gui;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * A class that renders {@link Boolean} values as disabled check boxes.
 * 
 * @author Matthias Rall
 * 
 */
public class TableCellRendererBooleans implements TableCellRenderer {

	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		JCheckBox checkBox = new JCheckBox();
		Color colorForeground = table.getForeground();
		Color colorBackground = table.getBackground();
		Color colorSelectionForeground = table.getSelectionForeground();
		Color colorSelectionBackground = table.getSelectionBackground();
		if (value instanceof Boolean) {
			checkBox.setSelected((Boolean) value);
		}
		checkBox.setEnabled(false);
		checkBox.setForeground(colorForeground);
		checkBox.setBackground(colorBackground);
		if (isSelected) {
			checkBox.setForeground(colorSelectionForeground);
			checkBox.setBackground(colorSelectionBackground);
		}
		return checkBox;
	}

}