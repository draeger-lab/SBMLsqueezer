/*
 * Feb 7, 2008 Copyright (c) ZBiT, University of T&uuml;bingen, Germany
 * Compiler: JDK 1.6.0
 */
package org.sbmlsqueezer.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;

import jp.sbi.celldesigner.plugin.PluginModel;
import jp.sbi.celldesigner.plugin.PluginReaction;

import org.sbmlsqueezer.kinetics.BasicKineticLaw;
import org.sbmlsqueezer.kinetics.KineticLawGenerator;
import org.sbmlsqueezer.kinetics.RateLawNotApplicableException;

import atp.sHotEqn;

/**
 * A panel, which contains all possible kinetic equations for the current
 * reaction. A panel that contains the whole message for the user: the message
 * itself, the reversibility and the applicable kinetics.
 * 
 * @since 2.0
 * @version
 * @author Andreas Dr&auml;ger (draeger) <andreas.draeger@uni-tuebingen.de>
 * @date Feb 7, 2008
 */
public class KineticLawSelectionPanel extends JPanel implements ActionListener {

	/**
	 * Generated Serial ID.
	 */
	private static final long serialVersionUID = -3145019506487267364L;

	private short possibleTypes[];

	private JRadioButton rButtonsKineticEquations[];

	private JRadioButton rButtonReversible;

	private PluginModel model;

	private PluginReaction reaction;

	private KineticLawGenerator klg;

	private Box kineticsPanel;

	private JPanel eqnPrev;

	private String[] laTeXpreview;

	private static final int width = 310, height = 175;

	/**
	 * @param plugin
	 * @param model
	 * @param reaction
	 * @throws RateLawNotApplicableException
	 */
	public KineticLawSelectionPanel(KineticLawGenerator klg, PluginModel model,
			PluginReaction reaction) throws RateLawNotApplicableException {
		super(new GridBagLayout());
		this.klg = klg;
		this.model = model;
		this.reaction = reaction;

		JLabel label = new JLabel("<html><body>", JLabel.LEFT);

		double stoichiometry = 0;
		for (int i = 0; i < reaction.getNumReactants(); i++)
			stoichiometry += reaction.getReactant(i).getStoichiometry();
		if (stoichiometry > 2) {
			label.setText(label.getText() + "<p><span color=\"red\">"
					+ "Warning: ");
			if (stoichiometry - ((int) stoichiometry) == 0)
				label.setText(label.getText()
						+ Integer.toString((int) stoichiometry));
			else
				label.setText(label.getText() + stoichiometry);
			label.setText(label.getText() + " molecules are unlikely "
					+ "to collide spontainously.</span></p>");
		}
		if (reaction.getFast()) {
			label.setText(label.getText() + "<p><span color=\"#505050\">"
					+ "This is a fast reaction. Note that this attribute is "
					+ "currently ignored.</span></p>");
		}
		label.setText(label.getText() + "</body></html>");
		if (reaction.getFast() || (stoichiometry > 2)) {
			JPanel p = new JPanel();
			p.setBackground(Color.WHITE);
			p.add(label);
			p.setBorder(BorderFactory.createRaisedBevelBorder());
			LayoutHelper.addComponent(this, (GridBagLayout) this.getLayout(),
					p, 0, 0, 1, 1, 1, 1);
		} else
			LayoutHelper.addComponent(this, (GridBagLayout) this.getLayout(),
					label, 0, 0, 1, 1, 1, 1);

		/*
		 * A panel, which contains the question, weather the reaction should be
		 * set to reversible or irreversible. The default is taken from the
		 * current setting of the given reaction.
		 */
		JPanel reversibilityPanel = new JPanel(new GridBagLayout());
		rButtonReversible = new JRadioButton("reversible", reaction
				.getReversible());
		JRadioButton rButtonIrreversible = new JRadioButton("irreversible",
				!reaction.getReversible());
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(rButtonReversible);
		buttonGroup.add(rButtonIrreversible);
		LayoutHelper.addComponent(reversibilityPanel,
				(GridBagLayout) reversibilityPanel.getLayout(),
				rButtonReversible, 0, 0, 1, 1, 1, 1);
		LayoutHelper.addComponent(reversibilityPanel,
				(GridBagLayout) reversibilityPanel.getLayout(),
				rButtonIrreversible, 1, 0, 1, 1, 1, 1);
		rButtonIrreversible.addActionListener(this);
		rButtonReversible.addActionListener(this);

		kineticsPanel = initKineticsPanel();
		LayoutHelper.addComponent(this, (GridBagLayout) this.getLayout(),
				kineticsPanel, 0, 1, 1, 1, 1, 1);
		LayoutHelper.addComponent(this, (GridBagLayout) this.getLayout(),
				new JSeparator(), 0, 2, 1, 1, 1, 1);
		LayoutHelper.addComponent(this, (GridBagLayout) this.getLayout(),
				reversibilityPanel, 0, 3, 1, 1, 1, 1);
	}

	private Box initKineticsPanel() throws RateLawNotApplicableException {
		possibleTypes = klg.identifyPossibleReactionTypes(model, reaction);
		String[] kineticEquations = new String[possibleTypes.length];
		String[] toolTips = new String[possibleTypes.length];
		laTeXpreview = new String[possibleTypes.length];
		int i;
		for (i = 0; i < possibleTypes.length; i++) {
			BasicKineticLaw kinetic = klg.createKineticLaw(model, reaction,
					possibleTypes[i], false);
			laTeXpreview[i] = kinetic.getKineticTeX();
			toolTips[i] = toHTML(kinetic.getName(), 40);
			kineticEquations[i] = toHTML(klg.getEquationName(possibleTypes[i]),
					40);
		}

		JPanel kineticsPanel = new JPanel(new GridBagLayout());
		rButtonsKineticEquations = new JRadioButton[kineticEquations.length];
		ButtonGroup buttonGroup = new ButtonGroup();

		for (i = 0; i < rButtonsKineticEquations.length; i++) {
			rButtonsKineticEquations[i] = (i != 0) ? new JRadioButton(
					kineticEquations[i], false) : new JRadioButton(
					kineticEquations[i], true);
			buttonGroup.add(rButtonsKineticEquations[i]);
			rButtonsKineticEquations[i].addActionListener(this);
			LayoutHelper.addComponent(kineticsPanel,
					(GridBagLayout) kineticsPanel.getLayout(),
					rButtonsKineticEquations[i], 0, i, 1, 1, 1, 1);
			rButtonsKineticEquations[i].setToolTipText(toolTips[i]);
		}

		kineticsPanel.setBorder(BorderFactory
				.createTitledBorder(" Please choose one kinetic law "));
		setPreviewPanel(0);
		Box info = new Box(BoxLayout.Y_AXIS);
		info.add(kineticsPanel);
		info.add(eqnPrev);

		return info; // kineticsPanel;
	}

	/**
	 * Sets up the panel for the preview of the formula.
	 * 
	 * @param kinNum
	 */
	private void setPreviewPanel(int kinNum) {
		JPanel preview = new JPanel(new BorderLayout());
		preview.add(new sHotEqn("\\begin{equation}v_\\mbox{"
				+ reaction.getId()
				+ "}="
				+ laTeXpreview[kinNum].replaceAll("mathrm", "mbox").replaceAll(
						"text", "mbox") + "\\end{equation}"),
				BorderLayout.CENTER);
		preview.setBackground(Color.WHITE);
		eqnPrev = new JPanel();
		eqnPrev.setBorder(BorderFactory
				.createTitledBorder(" Equation Preview "));
		Dimension dim = new Dimension(width, height);
		/*
		 * new Dimension((int) Math.min(width, preview
		 * .getPreferredSize().getWidth()), (int) Math.min(height, preview
		 * .getPreferredSize().getHeight()));//
		 */
		JScrollPane scroll = new JScrollPane(preview,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroll.setBorder(BorderFactory.createLoweredBevelBorder());
		scroll.setBackground(Color.WHITE);
		scroll.setPreferredSize(dim);
		eqnPrev.add(scroll);
	}

	/**
	 * Returns a HTML formated String, in which each line is at most lineBreak
	 * symbols long.
	 * 
	 * @param string
	 * @param lineBreak
	 * @return
	 */
	private String toHTML(String string, int lineBreak) {
		StringTokenizer st = new StringTokenizer(string, " ");
		string = new String(st.nextElement().toString());
		int length = string.length();
		while (st.hasMoreElements()) {
			if (length >= lineBreak) {
				string += "<br>";
				length = 0;
			} else
				string += " ";
			String tmp = st.nextElement().toString();
			length += tmp.length() + 1;
			string += tmp;
		}
		string = "<html><body>" + string + "</body></html>";
		return string;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof JRadioButton) {
			JRadioButton rbutton = (JRadioButton) e.getSource();
			if (rbutton.getText().endsWith("reversible"))
				try {
					// reversible property was changed.
					reaction.setReversible(getReversible());
					remove(kineticsPanel);
					kineticsPanel = initKineticsPanel();
					LayoutHelper.addComponent(this, (GridBagLayout) this
							.getLayout(), kineticsPanel, 0, 1, 1, 1, 1, 1);
				} catch (RateLawNotApplicableException exc) {
					throw new RuntimeException(exc.getMessage(), exc);
				}
			else {
				int i = 0, width = eqnPrev.getWidth(), height = eqnPrev
						.getHeight();
				while ((i < rButtonsKineticEquations.length)
						&& (!rbutton.equals(rButtonsKineticEquations[i])))
					i++;
				kineticsPanel.remove(eqnPrev);
				setPreviewPanel(i);
				kineticsPanel.add(eqnPrev);
			}
			validate();
			getTopLevelAncestor().validate();
			Window w = (Window) getTopLevelAncestor();
			int width = w.getWidth();
			w.pack();
			w.setSize(width, w.getHeight());
		}
	}

	/**
	 * Returns the selected kinetic law from the list of possible kinetic laws.
	 * 
	 * @return
	 */
	public short getSelectedKinetic() {
		int i = 0;
		while ((i < rButtonsKineticEquations.length)
				&& (!rButtonsKineticEquations[i].isSelected()))
			i++;
		return possibleTypes[i];
	}

	/**
	 * Returns true if the reaction was set to reversible.
	 * 
	 * @return
	 */
	public boolean getReversible() {
		return rButtonReversible.isSelected();
	}
}
