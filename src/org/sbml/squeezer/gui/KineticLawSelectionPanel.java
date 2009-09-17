/*
 *  SBMLsqueezer creates rate equations for reactions in SBML files
 *  (http://sbml.org).
 *  Copyright (C) 2009 ZBIT, University of Tübingen, Andreas Dräger
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.sbml.squeezer.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.IllegalFormatException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.Reaction;
import org.sbml.squeezer.CfgKeys;
import org.sbml.squeezer.KineticLawGenerator;
import org.sbml.squeezer.Kinetics;
import org.sbml.squeezer.RateLawNotApplicableException;
import org.sbml.squeezer.kinetics.BasicKineticLaw;

import atp.sHotEqn;

/**
 * A panel, which contains all possible kinetic equations for the current
 * reaction. A panel that contains the whole message for the user: the message
 * itself, the reversibility and the applicable kinetics.
 * 
 * @since 1.0
 * @version
 * @author Andreas Dr&auml;ger (draeger) <andreas.draeger@uni-tuebingen.de>
 * @author Hannes Borch <hannes.borch@googlemail.com>
 * @date Feb 7, 2008
 */
public class KineticLawSelectionPanel extends JPanel implements ItemListener {

	/**
	 * Generated Serial ID.
	 */
	private static final long serialVersionUID = -3145019506487267364L;

	private boolean isExistingRateLawSelected;

	private Kinetics possibleTypes[];

	private JPanel optionsPanel;

	private JRadioButton rButtonsKineticEquations[];

	private JRadioButton rButtonReversible;

	private JRadioButton rButtonGlobalParameters;

	private Reaction reaction;

	private KineticLawGenerator klg;

	private Box kineticsPanel;

	private JPanel eqnPrev;

	private String selected;

	private StringBuffer[] laTeXpreview;

	private JCheckBox treatAsEnzymeReaction;

	private JComboBox kineticLawComboBox;

	private static final String EXISTING_RATE_LAW = "Existing rate law";

	private static final int width = 310, height = 175;

	/**
	 * 
	 * @param possibleLaws
	 * @param selected
	 */
	public KineticLawSelectionPanel(KineticLaw[] possibleLaws, int selected) {
		super(new BorderLayout());
		if (possibleLaws == null || selected < 0
				|| selected > possibleLaws.length || possibleLaws.length < 1)
			throw new IllegalArgumentException(
					"at least one rate law must be given and the index must be between zero and the number of rate laws.");
		this.reaction = possibleLaws[0].getParentSBMLObject();
		this.possibleTypes = new Kinetics[possibleLaws.length];
		String[] possibleTypesNames = new String[possibleLaws.length];
		laTeXpreview = new StringBuffer[possibleTypes.length];
		for (int i = 0; i < possibleTypes.length; i++) {
			possibleTypes[i] = Kinetics.getTypeForName(possibleLaws[i]
					.getClass().getName());
			possibleTypesNames[i] = possibleTypes[i].getEquationName();
			laTeXpreview[i] = possibleLaws[i].getMath().toLaTeX();
		}
		kineticLawComboBox = new JComboBox(possibleTypesNames);
		kineticLawComboBox.setEditable(false);
		kineticLawComboBox.setBackground(Color.WHITE);
		kineticLawComboBox.setSelectedIndex(selected);
		setPreviewPanel(kineticLawComboBox.getSelectedIndex());
		kineticLawComboBox.addItemListener(this);
		add(kineticLawComboBox, BorderLayout.NORTH);
		add(eqnPrev, BorderLayout.CENTER);
	}

	/**
	 * @param plugin
	 * @param model
	 * @param reaction
	 * @throws RateLawNotApplicableException
	 */
	public KineticLawSelectionPanel(KineticLawGenerator klg, Reaction reaction)
			throws RateLawNotApplicableException {
		super(new GridBagLayout());
		this.selected = "";
		this.klg = klg;
		this.reaction = reaction;
		StringBuilder label = new StringBuilder("<html><body>");
		double stoichiometry = 0;
		for (int i = 0; i < reaction.getNumReactants(); i++)
			stoichiometry += reaction.getReactant(i).getStoichiometry();
		if (stoichiometry > 2) {
			label.append("<p><span color=\"red\">" + "Warning: ");
			if (stoichiometry - ((int) stoichiometry) == 0)
				label.append(Integer.toString((int) stoichiometry));
			else
				label.append(stoichiometry);
			label.append(" molecules are unlikely ");
			label.append("to collide spontainously.</span></p>");
		}
		if (reaction.getFast()) {
			label.append("<p><span color=\"#505050\">");
			label.append("This is a fast reaction. Note that this ");
			label.append("attribute is currently ignored.</span></p>");
		}
		label.append("</body></html>");
		if (reaction.getFast() || (stoichiometry > 2)) {
			JPanel p = new JPanel();
			p.setBackground(Color.WHITE);
			p.add(new JLabel(label.toString(), SwingConstants.LEFT));
			p.setBorder(BorderFactory.createRaisedBevelBorder());
			LayoutHelper.addComponent(this, (GridBagLayout) this.getLayout(),
					p, 0, 0, 1, 1, 1, 1);
		} else
			LayoutHelper.addComponent(this, (GridBagLayout) this.getLayout(),
					new JLabel(label.toString(), SwingConstants.LEFT), 0, 0, 1,
					1, 1, 1);

		/*
		 * A panel, which contains the question, weather the reaction should be
		 * set to reversible or irreversible. The default is taken from the
		 * current setting of the given reaction.
		 */
		optionsPanel = new JPanel();
		LayoutHelper lh = new LayoutHelper(optionsPanel);
		optionsPanel.setBorder(BorderFactory
				.createTitledBorder("Reaction options"));

		rButtonReversible = new JRadioButton("Reversible", reaction
				.getReversible());
		rButtonReversible
				.setToolTipText(GUITools
						.toHTML(
								"If selected, SBMLsqueezer will take the effects of "
										+ "the products into account when creating rate equations.",
								40));
		JRadioButton rButtonIrreversible = new JRadioButton("Irreversible",
				!reaction.getReversible());
		rButtonIrreversible.setToolTipText(GUITools.toHTML(
				"If selected, SBMLsqueezer will not take effects of products into "
						+ "account when creating rate equations.", 40));
		ButtonGroup revGroup = new ButtonGroup();
		boolean nonEnzyme = klg.isNonEnzymeReaction(reaction.getId());
		boolean isEnzymeKineticsSelected = ((Boolean) klg.getSettings().get(
				CfgKeys.OPT_ALL_REACTIONS_ARE_ENZYME_CATALYZED)).booleanValue()
				|| !nonEnzyme;
		treatAsEnzymeReaction = new JCheckBox(
				"Consider this reaction to be enzyme-catalyzed",
				isEnzymeKineticsSelected);
		treatAsEnzymeReaction.setToolTipText(GUITools
				.toHTML(
						"Allows you to decide whether "
								+ "or not this reaction should be"
								+ "interpreted as being enzyme-catalyzed. "
								+ "If an enzyme catalyst exists for this "
								+ "reaction, this feature cannot be switched "
								+ "off.", 40));
		if (klg.isEnzymeReaction(reaction.getId()) || nonEnzyme)
			treatAsEnzymeReaction.setEnabled(false);
		klg.getSettings().put(CfgKeys.OPT_ALL_REACTIONS_ARE_ENZYME_CATALYZED,
				Boolean.valueOf(treatAsEnzymeReaction.isSelected()));
		lh.add(treatAsEnzymeReaction, 0, 0, 2, 1, 1, 1);
		revGroup.add(rButtonReversible);
		revGroup.add(rButtonIrreversible);
		lh.add(rButtonReversible, 0, 1, 1, 1, 1, 1);
		lh.add(rButtonIrreversible, 1, 1, 1, 1, 1, 1);
		JRadioButton rButtonLocalParameters = new JRadioButton(
				"Local parameters", reaction.isSetKineticLaw()
						&& reaction.getKineticLaw().getNumParameters() > 0);
		rButtonGlobalParameters = new JRadioButton("Global parameters",
				!rButtonLocalParameters.isSelected());
		rButtonGlobalParameters.setToolTipText(GUITools.toHTML(
				"If selected, newly created parameters will "
						+ "be stored globally in the model.", 40));
		rButtonLocalParameters.setToolTipText(GUITools.toHTML(
				"If selected, newly created parameters will "
						+ "be stored locally in this reaction.", 40));
		ButtonGroup paramGroup = new ButtonGroup();
		paramGroup.add(rButtonGlobalParameters);
		paramGroup.add(rButtonLocalParameters);
		lh.add(rButtonGlobalParameters, 0, 2, 1, 1, 1, 1);
		lh.add(rButtonLocalParameters, 1, 2, 1, 0, 1, 1);

		kineticsPanel = initKineticsPanel();

		LayoutHelper.addComponent(this, (GridBagLayout) this.getLayout(),
				kineticsPanel, 0, 1, 1, 1, 1, 1);
		LayoutHelper.addComponent(this, (GridBagLayout) this.getLayout(),
				new JSeparator(), 0, 2, 1, 1, 1, 1);
		LayoutHelper.addComponent(this, (GridBagLayout) this.getLayout(),
				optionsPanel, 0, 3, 1, 1, 1, 1);

		treatAsEnzymeReaction.addItemListener(this);
		// rButtonIrreversible.addItemListener(this);
		rButtonReversible.addItemListener(this);
		rButtonGlobalParameters.addItemListener(this);
		rButtonLocalParameters.addItemListener(this);
	}

	public boolean getExistingRateLawSelected() {
		return isExistingRateLawSelected;
	}

	/**
	 * Returns true if the reaction was set to reversible.
	 * 
	 * @return
	 */
	public boolean getReversible() {
		return rButtonReversible.isSelected();
	}

	/**
	 * Returns the selected kinetic law from the list of possible kinetic laws.
	 * 
	 * @return
	 */
	public Kinetics getSelectedKinetic() {
		int i = 0;
		if (rButtonsKineticEquations != null) {
			while ((i < rButtonsKineticEquations.length)
					&& (!rButtonsKineticEquations[i].isSelected()))
				i++;
		} else if (kineticLawComboBox != null)
			i = kineticLawComboBox.getSelectedIndex();
		return possibleTypes[i];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
	 */
	public void itemStateChanged(ItemEvent ie) {
		if (ie.getSource() instanceof JCheckBox)
			try {
				JCheckBox check = (JCheckBox) ie.getSource();
				klg.getSettings().put(
						CfgKeys.OPT_ALL_REACTIONS_ARE_ENZYME_CATALYZED,
						Boolean.valueOf(check.isSelected()));
				remove(kineticsPanel);
				kineticsPanel = initKineticsPanel();
				LayoutHelper.addComponent(this, (GridBagLayout) this
						.getLayout(), kineticsPanel, 0, 1, 1, 1, 1, 1);
			} catch (RateLawNotApplicableException e) {
				e.printStackTrace();
			}
		else if (ie.getSource() instanceof JRadioButton) {
			JRadioButton rbutton = (JRadioButton) ie.getSource();
			if (rbutton.getParent().equals(optionsPanel)) {
				if (rbutton.getText().contains("eversible")) {
					try {
						// reversible property was changed.
						selected = "";
						int i;
						for (i = 0; i < rButtonsKineticEquations.length
								&& selected.length() == 0; i++)
							if (rButtonsKineticEquations[i].isSelected())
								selected = rButtonsKineticEquations[i]
										.getText();
						klg.setReversible(reaction.getId(), getReversible());
						remove(kineticsPanel);
						kineticsPanel = initKineticsPanel();
						LayoutHelper.addComponent(this, (GridBagLayout) this
								.getLayout(), kineticsPanel, 0, 1, 1, 1, 1, 1);
						for (i = 0; i < rButtonsKineticEquations.length; i++)
							if (selected.equals(rButtonsKineticEquations[i]
									.getText())) {
								rButtonsKineticEquations[i].setSelected(true);
								break;
							}
						updateView();
					} catch (RateLawNotApplicableException exc) {
						throw new RuntimeException(exc.getMessage(), exc);
					}
				} else {
					klg.getSettings().put(
							CfgKeys.OPT_ADD_NEW_PARAMETERS_ALWAYS_GLOBALLY,
							Boolean.valueOf(rButtonGlobalParameters
									.isSelected()));
				}
			} else {
				int i = 0;
				while ((i < rButtonsKineticEquations.length)
						&& (!rbutton.equals(rButtonsKineticEquations[i])))
					i++;
				selected = rButtonsKineticEquations[i].getText();
				updateView();
			}
		} else if (ie.getSource() instanceof JComboBox) {
			remove(eqnPrev);
			setPreviewPanel(((JComboBox) ie.getSource()).getSelectedIndex());
			add(eqnPrev, BorderLayout.CENTER);
		}
		validate();
		getTopLevelAncestor().validate();
		Window w = (Window) getTopLevelAncestor();
		int width = w.getWidth();
		w.pack();
		w.setSize(width, w.getHeight());
	}

	/**
	 * 
	 * @return
	 * @throws RateLawNotApplicableException
	 */
	private Box initKineticsPanel() throws RateLawNotApplicableException {
		possibleTypes = klg.identifyPossibleReactionTypes(reaction.getId());
		String[] kineticEquations = new String[possibleTypes.length];
		String[] toolTips = new String[possibleTypes.length];
		laTeXpreview = new StringBuffer[possibleTypes.length + 1];
		int i;
		for (i = 0; i < possibleTypes.length; i++)
			try {
				BasicKineticLaw kinetic = klg.createKineticLaw(reaction,
						possibleTypes[i], false);
				laTeXpreview[i] = new StringBuffer(kinetic.getMath().toLaTeX()
						.toString());
				toolTips[i] = kinetic.isSetSBOTerm() ? "<b>"
						+ kinetic.getSBOTermID() + "</b> " : "";
				toolTips[i] = GUITools.toHTML(toolTips[i] + kinetic.toString(),
						40);
				kineticEquations[i] = GUITools.toHTML(possibleTypes[i]
						.getEquationName(), 40);
			} catch (IllegalFormatException e) {
				e.printStackTrace();
			}
		if (reaction.isSetKineticLaw())
			laTeXpreview[laTeXpreview.length - 1] = reaction.getKineticLaw()
					.getMath().toLaTeX();
		JPanel kineticsPanel = new JPanel(new GridBagLayout());
		rButtonsKineticEquations = new JRadioButton[kineticEquations.length + 1];
		ButtonGroup buttonGroup = new ButtonGroup();

		short kinSelected = -1;
		for (i = 0; i < rButtonsKineticEquations.length; i++) {
			if (i < rButtonsKineticEquations.length - 1) {
				if (kineticEquations[i].equals(selected)) {
					rButtonsKineticEquations[i] = new JRadioButton(
							kineticEquations[i], true);
					kinSelected = (short) i;
				} else
					rButtonsKineticEquations[i] = new JRadioButton(
							kineticEquations[i], false);
				rButtonsKineticEquations[i].setToolTipText(toolTips[i]);
			} else {
				rButtonsKineticEquations[i] = new JRadioButton(
						EXISTING_RATE_LAW, false);

				if (reaction.getNotesString().length() > 0)
					rButtonsKineticEquations[i].setToolTipText(GUITools.toHTML(
							reaction.getNotesString(), 40));
				else
					rButtonsKineticEquations[i]
							.setToolTipText("<html> This rate law is currently assigned to this reaction.</html>");
			}
			buttonGroup.add(rButtonsKineticEquations[i]);
			if (i < rButtonsKineticEquations.length - 1
					|| reaction.isSetKineticLaw())
				LayoutHelper.addComponent(kineticsPanel,
						(GridBagLayout) kineticsPanel.getLayout(),
						rButtonsKineticEquations[i], 0, i, 1, 1, 1, 1);
		}
		if (kinSelected == -1 && rButtonsKineticEquations.length > 0) {
			kinSelected = 0;
			rButtonsKineticEquations[kinSelected].setSelected(true);
		}

		kineticsPanel.setBorder(BorderFactory
				.createTitledBorder(" Please choose one kinetic law "));
		setPreviewPanel(kinSelected);
		Box info = new Box(BoxLayout.Y_AXIS);
		info.add(kineticsPanel);
		info.add(eqnPrev);
		// ContainerHandler.setAllBackground(info, Color.WHITE);

		isExistingRateLawSelected = false;

		for (i = 0; i < rButtonsKineticEquations.length; i++)
			rButtonsKineticEquations[i].addItemListener(this);

		return info; // kineticsPanel;
	}

	/**
	 * Sets up the panel for the preview of the formula.
	 * 
	 * @param kinNum
	 */
	private void setPreviewPanel(int kinNum) {
		JPanel preview = new JPanel(new BorderLayout());
		StringBuilder sb = new StringBuilder("\\begin{equation}v_\\mbox{");
		sb.append(reaction.getId());
		sb.append("}=");
		sb.append(laTeXpreview[kinNum].toString().replace("mathrm", "mbox")
				.replace("text", "mbox").replace("mathtt", "mbox"));
		sb.append("\\end{equation}");
		preview.add(new sHotEqn(sb.toString()), BorderLayout.CENTER);
		preview.setBackground(Color.WHITE);
		eqnPrev = new JPanel();
		eqnPrev.setBorder(BorderFactory
				.createTitledBorder(" Equation Preview "));
		eqnPrev.setLayout(new BorderLayout());
		Dimension dim = new Dimension(width, height);
		/*
		 * new Dimension((int) Math.min(width, preview
		 * .getPreferredSize().getWidth()), (int) Math.min(height, preview
		 * .getPreferredSize().getHeight()));//
		 */
		JScrollPane scroll = new JScrollPane(preview,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroll.setBorder(BorderFactory.createLoweredBevelBorder());
		scroll.setBackground(Color.WHITE);
		scroll.setPreferredSize(dim);
		// ContainerHandler.setAllBackground(scroll, Color.WHITE);
		eqnPrev.add(scroll, BorderLayout.CENTER);
	}

	/**
	 * 
	 * @param i
	 */
	private void updateView() {
		boolean disable = selected.equals(EXISTING_RATE_LAW);
		int i = disable ? rButtonsKineticEquations.length - 1 : 0;
		while (i < rButtonsKineticEquations.length
				&& !selected.equals(rButtonsKineticEquations[i].getText())
				&& !disable)
			i++;
		boolean global = !(reaction.isSetKineticLaw() && reaction
				.getKineticLaw().getNumParameters() > 0);
		boolean change = disable || isExistingRateLawSelected
				|| i < rButtonsKineticEquations.length - 1;
		if (disable) {
			rButtonReversible.setSelected(reaction.getReversible());
			rButtonGlobalParameters.setSelected(global);
			isExistingRateLawSelected = true;
		} else if (isExistingRateLawSelected) {
			isExistingRateLawSelected = false;
			rButtonReversible.setSelected(reaction.getReversible());
			rButtonGlobalParameters.setSelected(global);
		}
		if (change) {
			kineticsPanel.remove(eqnPrev);
			setPreviewPanel(i);
			kineticsPanel.add(eqnPrev);
			GUITools.setAllEnabled(optionsPanel, !disable);
		}
		if (klg.isEnzymeReaction(reaction.getId())
				|| klg.isNonEnzymeReaction(reaction.getId()))
			treatAsEnzymeReaction.setEnabled(false);
	}
}
