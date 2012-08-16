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
 * A class that renders a temperature as a color within a gradient.
 * 
 * @author Matthias Rall
 * 
 */
public class TableCellRendererTemperature extends
		TableCellRendererColorGradient {

	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		JPanel panel = new JPanel();
		Color colorForeground = table.getForeground();
		Color colorBackground = table.getBackground();
		Color colorSelectionForeground = table.getSelectionForeground();
		Color colorSelectionBackground = table.getSelectionBackground();
		if (value instanceof KineticLaw) {
			String temperature = "";
			String temperatureUnit = "";
			try {
				temperature = SABIORK.getStartValueTemperature(
						(KineticLaw) value).trim();
				temperatureUnit = SABIORK
						.getTemperatureUnit((KineticLaw) value).trim();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (XMLStreamException e) {
				e.printStackTrace();
			}
			if (!temperature.isEmpty() && !temperatureUnit.isEmpty()) {
				panel.add(new JLabel(String.valueOf(temperature + " "
						+ temperatureUnit)));
				Double temperatureDouble = Double.valueOf(temperature);
				temperatureDouble = round(temperatureDouble);
				Color colorTemperatureMinus10Celsius = WizardProperties
						.getColor("TABLE_CELL_RENDERER_TEMPERATURE_RGB_COLOR_TEMPERATURE_MINUS_10_CELSIUS");
				Color colorTemperaturePlus25Celsius = WizardProperties
						.getColor("TABLE_CELL_RENDERER_TEMPERATURE_RGB_COLOR_TEMPERATURE_PLUS_25_CELSIUS");
				Color colorTemperaturePlus40Celsius = WizardProperties
						.getColor("TABLE_CELL_RENDERER_TEMPERATURE_RGB_COLOR_TEMPERATURE_PLUS_40_CELSIUS");
				Color colorTemperaturePlus75Celsius = WizardProperties
						.getColor("TABLE_CELL_RENDERER_TEMPERATURE_RGB_COLOR_TEMPERATURE_PLUS_75_CELSIUS");
				if (-10.0 <= temperatureDouble && temperatureDouble <= 25.0) {
					colorBackground = getGradientColor(temperatureDouble,
							-10.0, 25.0, colorTemperatureMinus10Celsius,
							colorTemperaturePlus25Celsius);
				} else if (25.1 <= temperatureDouble
						&& temperatureDouble <= 40.0) {
					colorBackground = getGradientColor(temperatureDouble, 25.1,
							40.0, colorTemperaturePlus25Celsius,
							colorTemperaturePlus40Celsius);
				} else if (40.1 <= temperatureDouble
						&& temperatureDouble <= 75.0) {
					colorBackground = getGradientColor(temperatureDouble, 40.1,
							75.0, colorTemperaturePlus40Celsius,
							colorTemperaturePlus75Celsius);
				} else {
					colorBackground = getGradientColor(temperatureDouble,
							-10.0, 75.0, colorTemperatureMinus10Celsius,
							colorTemperaturePlus75Celsius);
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
