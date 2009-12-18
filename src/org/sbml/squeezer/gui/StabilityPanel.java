package org.sbml.squeezer.gui;

import java.awt.Color;
import java.awt.GridBagLayout;
import java.util.Properties;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.sbml.squeezer.CfgKeys;

/**
 * @author <a href="mailto:a.doerr@uni-tuebingen.de">Alexander D&ouml;rr</a>
 * @date 2009-12-10
 * @since 1.3
 */
public class StabilityPanel extends JPanel {
	
	private JTextField jTextFieldDelta;
	private JTextField jTextFieldStable;

	/**
	 * Generated serial version id.
	 */
	private static final long serialVersionUID = 1L;
	
	private Properties settings;	
	
	public StabilityPanel(Properties properties) {
		super();
		this.settings = properties;
		
		for (Object key : settings.keySet()) {
			String k = key.toString();
			if (k.startsWith("STABILITY_"))
				this.settings.put(key, settings.get(key));
		}

		init();
	}

	private void init() {		
		GridBagLayout layout = new GridBagLayout();
		jTextFieldDelta = new JTextField();
		jTextFieldDelta.setText(settings.get(CfgKeys.STABILITY_VALUE_OF_DELTA)
				.toString());
		jTextFieldDelta.setEnabled(false);
		jTextFieldStable = new JTextField("Stability undefined");
		jTextFieldStable.setEnabled(false);
		jTextFieldStable.setBackground(Color.gray);
		
		LayoutHelper.addComponent(this, layout, new JLabel(
				GUITools.toHTML("Value for numerical differentiation:", 30)),
				0, 0, 1, 1, 1, 0);
		LayoutHelper.addComponent(this, layout,
				jTextFieldDelta, 1, 0, 1, 1, 1, 0);
		LayoutHelper.addComponent(this, layout,
				jTextFieldStable, 0, 1, 2, 1, 2, 2);		
		this.setLayout(layout);
		
	}

}
