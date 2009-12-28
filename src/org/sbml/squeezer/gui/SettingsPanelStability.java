package org.sbml.squeezer.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.sbml.squeezer.CfgKeys;
import org.sbml.squeezer.SBMLsqueezer;

/**
 * This class is a panel, which contains all necessary options to perform a
 * stability analysis of the given model.
 * 
 * @author <a href="mailto:a.doerr@uni-tuebingen.de">Alexander D&ouml;rr</a>
 * @date 2009-12-18
 * @since 1.3
 */
public class SettingsPanelStability extends JPanel implements ChangeListener,
		ItemListener, KeyListener {

	/**
	 * Generated serial version ID.
	 */
	private static final long serialVersionUID = 1L;

	private List<ChangeListener> changeListeners;

	private List<ItemListener> itemListeners;

	private List<KeyListener> keyListeners;

	private JTextField jTextFieldDelta;

	private JTextField jTextFieldNumberofRuns;

	private JSpinner jSpinnerValueofN;

	private JSpinner jSpinnerValueofM;

	private JTextField jTextFieldPCStepSize;

	private JTextField jTextFieldPCOutputLocation;

	private JTextField jTextFieldMIStepSize;

	private JTextField jTextFieldMIOutputLocation;

	private Properties settings;

	public SettingsPanelStability(Properties settings) {
		super(new GridBagLayout());
		this.settings = new Properties();

		for (Object key : settings.keySet()) {
			String k = key.toString();
			if (k.startsWith("STEUER_") || k.startsWith("STABILITY_"))
				this.settings.put(key, settings.get(key));
		}

		this.itemListeners = new LinkedList<ItemListener>();
		this.changeListeners = new LinkedList<ChangeListener>();
		this.keyListeners = new LinkedList<KeyListener>();
		init();

	}

	private void init() {
		// ButtonGroup buttonGroup;
		Font titleFont = new Font("Dialog", Font.BOLD, 12);
		Color borderColor = new Color(51, 51, 51);

		// Top Panel
		GridBagLayout layout = new GridBagLayout();
		JPanel jPanelStabilityAnalysis = new JPanel(layout);
		jPanelStabilityAnalysis.setBorder(BorderFactory.createTitledBorder(
				null, " Stability Analysis ",
				TitledBorder.DEFAULT_JUSTIFICATION,
				TitledBorder.DEFAULT_POSITION, titleFont, borderColor));

		jTextFieldDelta = new JTextField(1);
		jTextFieldDelta.setText(settings.get(CfgKeys.STABILITY_VALUE_OF_DELTA)
				.toString());
		jTextFieldDelta.setToolTipText(GUITools.toHTML(
				"Set the value for numerical differentiation", 25));
		LayoutHelper.addComponent(jPanelStabilityAnalysis, layout, new JLabel(
				GUITools.toHTML("Value for numerical differentiation:", 20)),
				0, 0, 1, 1, 1, 0);
		LayoutHelper.addComponent(jPanelStabilityAnalysis, layout,
				jTextFieldDelta, 1, 0, 1, 1, 1, 0);

		// Component c, int x, int y, int width, int height, double weightx,
		// double weighty

		// 2nd Panel
		layout = new GridBagLayout();
		JPanel jPanelStructuralKinetic = new JPanel(layout);
		jPanelStructuralKinetic.setBorder(BorderFactory.createTitledBorder(
				null, " Structural Kinetic Modelling ",
				TitledBorder.DEFAULT_JUSTIFICATION,
				TitledBorder.DEFAULT_POSITION, titleFont, borderColor));

		jTextFieldNumberofRuns = new JTextField(1);
		jTextFieldNumberofRuns.setText(settings.get(
				CfgKeys.STEUER_NUMBER_OF_RUNS).toString());
		jTextFieldNumberofRuns.setToolTipText(GUITools.toHTML(
				"Set the number of sampled Jacobians", 25));

		jSpinnerValueofN = new JSpinner(new SpinnerNumberModel(
				((Integer) settings.get(CfgKeys.STEUER_VALUE_OF_N)).intValue(),
				2, 10, 1));
		jSpinnerValueofN.setToolTipText(GUITools.toHTML(
				"Specifiy the value of n.", 25));

		jSpinnerValueofM = new JSpinner(new SpinnerNumberModel(
				((Integer) settings.get(CfgKeys.STEUER_VALUE_OF_M)).intValue(),
				2, 10, 1));
		jSpinnerValueofM.setToolTipText(GUITools.toHTML(
				"Specifiy the value of m.", 25));

		jTextFieldPCStepSize = new JTextField(1);
		jTextFieldPCStepSize.setText(settings.get(CfgKeys.STEUER_PC_STEPSIZE)
				.toString());
		jTextFieldPCStepSize.setToolTipText(GUITools.toHTML(
				"Set the bin size for the Pearson-Correlation", 25));

		jTextFieldPCOutputLocation = new JTextField(1);
		jTextFieldPCOutputLocation.setText(settings.get(
				CfgKeys.STEUER_PC_OUTPUT).toString());
		jTextFieldPCOutputLocation.setToolTipText(GUITools.toHTML(
				"Set the output folder for the Pearson-Correlation", 25));

		jTextFieldMIStepSize = new JTextField(1);
		jTextFieldMIStepSize.setText(settings.get(CfgKeys.STEUER_MI_STEPSIZE)
				.toString());
		jTextFieldMIStepSize.setToolTipText(GUITools.toHTML(
				"Set the bin size for the Mutual Information", 25));

		jTextFieldMIOutputLocation = new JTextField(1);
		jTextFieldMIOutputLocation.setText(settings.get(
				CfgKeys.STEUER_PC_OUTPUT).toString());
		jTextFieldMIOutputLocation.setToolTipText(GUITools.toHTML(
				"Set the output folder for Mutual Information", 25));

		LayoutHelper.addComponent(jPanelStructuralKinetic, layout, new JLabel(
				GUITools.toHTML("Number of sampled Jacobians:", 20)), 0, 0, 1,
				1, 1, 0);
		LayoutHelper.addComponent(jPanelStructuralKinetic, layout,
				jTextFieldNumberofRuns, 1, 0, 1, 1, 0, 0);

		LayoutHelper.addComponent(jPanelStructuralKinetic, layout, new JLabel(
				GUITools.toHTML("Value for n:", 20)), 0, 1, 1, 1, 1, 0);
		LayoutHelper.addComponent(jPanelStructuralKinetic, layout,
				jSpinnerValueofN, 1, 1, 1, 1, 1, 1);

		LayoutHelper.addComponent(jPanelStructuralKinetic, layout, new JLabel(
				GUITools.toHTML("Value for m:", 20)), 0, 2, 1, 1, 1, 0);
		LayoutHelper.addComponent(jPanelStructuralKinetic, layout,
				jSpinnerValueofM, 1, 2, 1, 1, 1, 1);

		LayoutHelper.addComponent(jPanelStructuralKinetic, layout, new JLabel(
				GUITools.toHTML("Bin size for Pearson-Correlation:", 25)), 0,
				3, 0, 1, 1, 0);
		LayoutHelper.addComponent(jPanelStructuralKinetic, layout,
				jTextFieldPCStepSize, 1, 3, 1, 1, 1, 1);

		LayoutHelper.addComponent(jPanelStructuralKinetic, layout, new JLabel(
				GUITools.toHTML("Output folder for Pearson-Correlation:", 25)),
				0, 4, 1, 1, 1, 0);
		LayoutHelper.addComponent(jPanelStructuralKinetic, layout,
				jTextFieldPCOutputLocation, 1, 4, 1, 1, 1, 1);

		LayoutHelper.addComponent(jPanelStructuralKinetic, layout, new JLabel(
				GUITools.toHTML("Bin size for Mutual Information:", 25)), 0, 5,
				1, 1, 1, 0);
		LayoutHelper.addComponent(jPanelStructuralKinetic, layout,
				jTextFieldMIStepSize, 1, 5, 1, 1, 1, 1);

		LayoutHelper.addComponent(jPanelStructuralKinetic, layout, new JLabel(
				GUITools.toHTML("Output folder  for Mutual Information:", 25)),
				0, 6, 1, 1, 1, 0);
		LayoutHelper.addComponent(jPanelStructuralKinetic, layout,
				jTextFieldMIOutputLocation, 1, 6, 1, 1, 1, 1);

		layout = (GridBagLayout) this.getLayout();

		LayoutHelper helper = new LayoutHelper(this);
		helper.add(jPanelStabilityAnalysis);
		helper.add(jPanelStructuralKinetic);

		GUITools.setAllBackground(this, Color.WHITE);
		setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));

		// Add change listener
		jTextFieldDelta.addKeyListener(this);
		jTextFieldNumberofRuns.addKeyListener(this);
		jSpinnerValueofN.addChangeListener(this);
		jSpinnerValueofM.addChangeListener(this);
		jTextFieldPCStepSize.addKeyListener(this);
		jTextFieldPCOutputLocation.addKeyListener(this);
		jTextFieldMIStepSize.addKeyListener(this);
		jTextFieldMIOutputLocation.addKeyListener(this);
	}

	/**
	 * 
	 * @param l
	 */
	public void addChangeListener(ChangeListener l) {
		changeListeners.add(l);
	}

	/**
	 * 
	 * @param l
	 */
	public void addItemListener(ItemListener l) {
		itemListeners.add(l);
	}

	/**
	 * 
	 */
	public void restoreDefaults() {
		String openDir = settings.get(CfgKeys.OPEN_DIR).toString();
		String saveDir = settings.get(CfgKeys.SAVE_DIR).toString();
		settings = SBMLsqueezer.getDefaultSettings();
		settings.put(CfgKeys.OPEN_DIR, openDir);
		settings.put(CfgKeys.SAVE_DIR, saveDir);
		init();
		validate();
	}

	/**
	 * 
	 * @return
	 */
	public Properties getSettings() {
		return settings;
	}

	public void stateChanged(ChangeEvent e) {

		for (int i = 0; i < changeListeners.size(); i++)
			changeListeners.get(i).stateChanged(e);

	}

	public void itemStateChanged(ItemEvent e) {
		if (e.getSource().equals(jSpinnerValueofN)) {
			settings.put(CfgKeys.STEUER_VALUE_OF_N, Integer
					.parseInt(jSpinnerValueofN.getValue().toString()));
		} else if (e.getSource().equals(jSpinnerValueofM)) {
			settings.put(CfgKeys.STEUER_VALUE_OF_M, Double
					.valueOf(jSpinnerValueofM.getValue().toString()));
		}
		for (ItemListener i : itemListeners)
			i.itemStateChanged(e);

	}

	public void keyPressed(KeyEvent e) {
		if (e.getSource().equals(jTextFieldDelta)) {
			if (!isDouble(jTextFieldDelta.getText()))
				System.out.println("only double please");
		}

		for (KeyListener i : keyListeners)
			i.keyPressed(e);

	}

	public void keyReleased(KeyEvent e) {

	}

	public void keyTyped(KeyEvent e) {

	}

	/**
	 * 
	 * @param input
	 * @return
	 */
	private boolean isDouble(String input) {
		try {
			Integer.parseInt(input);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

}
