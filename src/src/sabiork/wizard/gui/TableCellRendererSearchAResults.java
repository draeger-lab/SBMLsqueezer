package sabiork.wizard.gui;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import org.sbml.jsbml.Reaction;

import sabiork.wizard.console.SearchAResult;
import sabiork.wizard.model.WizardProperties;
import de.zbit.sbml.gui.ReactionPanel;

/**
 * A class that renders a complete {@link TableModelSearchAResults} according to
 * the {@link SearchAResult} within it.
 * 
 * @author Matthias Rall
 */
public class TableCellRendererSearchAResults implements TableCellRenderer {

	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		JPanel panel = new JPanel();
		Color colorForeground = table.getForeground();
		Color colorBackground = table.getBackground();
		Color colorSelectionForeground = table.getSelectionForeground();
		Color colorSelectionBackground = table.getSelectionBackground();
		TableModel tableModel = table.getModel();
		if (tableModel instanceof TableModelSearchAResults) {
			if (value instanceof Reaction) {
				panel = new ReactionPanel((Reaction) value, true);
				table.setRowHeight(row, panel.getPreferredSize().height);
			}
			if (value instanceof String) {
				panel.add(new JLabel((String) value));
			}
			if (value instanceof Integer) {
				panel.add(new JLabel(String.valueOf(value)));
			}
			int matchingEntries = (Integer) tableModel.getValueAt(row, 3);
			int partialOrNonMatchingEntries = (Integer) tableModel.getValueAt(
					row, 4);
			int totalEntries = (Integer) tableModel.getValueAt(row, 5);
			if (matchingEntries > 0 && totalEntries > 0) {
				colorBackground = WizardProperties
						.getColor("TABLE_CELL_RENDERER_SEARCH_A_RESULTS_RGB_COLOR_HAS_MATCHING_ENTRIES");
			}
			if (matchingEntries == 0 && partialOrNonMatchingEntries > 0
					&& totalEntries > 0) {
				colorBackground = WizardProperties
						.getColor("TABLE_CELL_RENDERER_SEARCH_A_RESULTS_RGB_COLOR_HAS_ONLY_PARTIAL/NON-MATCHING_ENTRIES");
			}
			if (matchingEntries == 0 && partialOrNonMatchingEntries == 0
					&& totalEntries == 0) {
				colorBackground = WizardProperties
						.getColor("TABLE_CELL_RENDERER_SEARCH_A_RESULTS_RGB_COLOR_HAS_NO_ENTRIES");
			}
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
