package sabiork.wizard.gui;

import java.awt.Color;
import javax.swing.table.TableCellRenderer;

/**
 * A class that renders {@link Double} values as a color within a gradient.
 * 
 * @author Matthias Rall
 * 
 */
public abstract class TableCellRendererColorGradient implements
		TableCellRenderer {

	/**
	 * Returns the rounded value.
	 * 
	 * @param value
	 * @return
	 */
	protected double round(double value) {
		value = value * 10;
		value = Math.round(value);
		value = value / 10;
		return value;
	}

	/**
	 * Returns the {@link Color} of a {@link Double} value within a color
	 * gradient between the color of the maximum and the minimum.
	 * 
	 * @param value
	 * @param minimum
	 * @param maximum
	 * @param colorMinimum
	 * @param colorMaximum
	 * @return
	 */
	protected Color getGradientColor(double value, double minimum,
			double maximum, Color colorMinimum, Color colorMaximum) {
		if (value < minimum) {
			return colorMinimum;
		} else if (maximum < value) {
			return colorMaximum;
		} else {
			double difference = maximum - minimum;
			double ratio = (value - minimum) / difference;
			int r = (int) Math.round(ratio * colorMaximum.getRed()
					+ (1 - ratio) * colorMinimum.getRed());
			int g = (int) Math.round(ratio * colorMaximum.getGreen()
					+ (1 - ratio) * colorMinimum.getGreen());
			int b = (int) Math.round(ratio * colorMaximum.getBlue()
					+ (1 - ratio) * colorMinimum.getBlue());
			int a = (int) Math.round(ratio * colorMaximum.getAlpha()
					+ (1 - ratio) * colorMinimum.getAlpha());
			return new Color(r, g, b, a);
		}
	}

}
