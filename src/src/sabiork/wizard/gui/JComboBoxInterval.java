package sabiork.wizard.gui;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

/**
 * A class that allows the creation of a combo box containing {@link Double}
 * values from <code>minimum</code> to <code>maximum</code> (step size of
 * <code>0.1</code>).
 * 
 * @author Matthias Rall
 * 
 */
@SuppressWarnings("serial")
public class JComboBoxInterval extends JComboBox {

	private double minimum;
	private double maximum;

	public JComboBoxInterval(double minimum, double maximum) {
		this.minimum = round(minimum);
		this.maximum = round(maximum);
		setInterval(minimum, maximum);
	}

	/**
	 * Returns the rounded value.
	 * 
	 * @param value
	 * @return
	 */
	private double round(double value) {
		value = value * 10;
		value = Math.round(value);
		value = value / 10;
		return value;
	}

	/**
	 * Adds all values from <code>lowerValue</code> to <code>upperValue</code>
	 * to the model (step size of <code>0.1</code>).
	 * 
	 * @param lowerValue
	 * @param upperValue
	 */
	public void setInterval(double lowerValue, double upperValue) {
		DefaultComboBoxModel model = new DefaultComboBoxModel();
		lowerValue = round(lowerValue);
		upperValue = round(upperValue);
		for (double i = lowerValue; i <= upperValue; i = i + 0.1) {
			model.addElement(round(i));
		}
		setModel(model);
	}

	/**
	 * Returns the minimum value.
	 * 
	 * @return
	 */
	public double getMinimum() {
		return minimum;
	}

	/**
	 * Returns the maximum value.
	 * 
	 * @return
	 */
	public double getMaximum() {
		return maximum;
	}

	/**
	 * Returns the selected value.
	 * 
	 * @return
	 */
	public double getSelectedValue() {
		return ((Double) getSelectedItem());
	}

}
