package sabiork.wizard.gui;

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
 * @see ReactionPanel
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
