package sabiork.wizard.gui;

import java.awt.Color;
import java.awt.Component;
import java.io.UnsupportedEncodingException;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.xml.stream.XMLStreamException;
import org.sbml.jsbml.KineticLaw;
import sabiork.SABIORK;
import sabiork.wizard.model.WizardProperties;

/**
 * A class that renders pH values as a color within a gradient.
 * 
 * @author Matthias Rall
 * 
 */
public class TableCellRendererpHValues extends TableCellRendererColorGradient {

	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		JPanel panel = new JPanel();
		Color colorForeground = table.getForeground();
		Color colorBackground = table.getBackground();
		Color colorSelectionForeground = table.getSelectionForeground();
		Color colorSelectionBackground = table.getSelectionBackground();
		if (value instanceof KineticLaw) {
			String pHValue = "";
			try {
				pHValue = SABIORK.getStartValuepH((KineticLaw) value).trim();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (XMLStreamException e) {
				e.printStackTrace();
			}
			if (!pHValue.isEmpty()) {
				panel.add(new JLabel(String.valueOf(pHValue)));
				Double pHValueDouble = Double.valueOf(pHValue);
				pHValueDouble = round(pHValueDouble);
				Color colorpH0 = WizardProperties
						.getColor("TABLE_CELL_RENDERER_PH_VALUES_RGB_COLOR_PH0");
				Color colorpH5 = WizardProperties
						.getColor("TABLE_CELL_RENDERER_PH_VALUES_RGB_COLOR_PH5");
				Color colorpH7 = WizardProperties
						.getColor("TABLE_CELL_RENDERER_PH_VALUES_RGB_COLOR_PH7");
				Color colorpH9 = WizardProperties
						.getColor("TABLE_CELL_RENDERER_PH_VALUES_RGB_COLOR_PH9");
				Color colorpH14 = WizardProperties
						.getColor("TABLE_CELL_RENDERER_PH_VALUES_RGB_COLOR_PH14");
				if (0.0 <= pHValueDouble && pHValueDouble <= 5.0) {
					colorBackground = getGradientColor(pHValueDouble, 0.0, 5.0,
							colorpH0, colorpH5);
				} else if (5.1 <= pHValueDouble && pHValueDouble <= 7.0) {
					colorBackground = getGradientColor(pHValueDouble, 5.1, 7.0,
							colorpH5, colorpH7);
				} else if (7.1 <= pHValueDouble && pHValueDouble <= 9.0) {
					colorBackground = getGradientColor(pHValueDouble, 7.1, 9.0,
							colorpH7, colorpH9);
				} else if (9.1 <= pHValueDouble && pHValueDouble <= 14.0) {
					colorBackground = getGradientColor(pHValueDouble, 9.1,
							14.0, colorpH9, colorpH14);
				} else {
					colorBackground = getGradientColor(pHValueDouble, 0.0,
							14.0, colorpH0, colorpH14);
				}
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
