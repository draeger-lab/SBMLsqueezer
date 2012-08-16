package sabiork.wizard.model;

import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class WizardProperties {

	/**
	 * Returns the corresponding text for a given key (File:
	 * Configuration.properties).
	 * 
	 * @param key
	 * @return
	 */
	public static String getText(String key) {
		String text = "";
		try {
			Properties properties = new Properties();
			properties.load(WizardProperties.class
					.getResourceAsStream("Configuration.properties"));
			if (properties.containsKey(key)) {
				text = properties.getProperty(key);
			} else {
				throw new IOException("Can not read property " + key + ".");
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return text.trim();
	}

	/**
	 * Returns the corresponding color for a given key (File:
	 * Configuration.properties).
	 * 
	 * @param key
	 * @return
	 */
	public static Color getColor(String key) {
		Color color = Color.BLACK;
		try {
			String text = getText(key);
			if (text.matches("\\d+[ ]+\\d+[ ]+\\d+")) {
				String[] rgb = text.split("[ ]+");
				color = new Color(Integer.valueOf(rgb[0]),
						Integer.valueOf(rgb[1]), Integer.valueOf(rgb[2]));
			} else {
				throw new IOException("Can not read property " + key + ".");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return color;
	}

}
