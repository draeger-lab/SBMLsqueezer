/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of SBMLsqueezer, a Java program that creates rate 
 * equations for reactions in SBML files (http://sbml.org).
 *
 * Copyright (C) 2006-2013 by the University of Tuebingen, Germany.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * ---------------------------------------------------------------------
 */
package org.sbml.squeezer.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;

import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBO;
import org.sbml.jsbml.util.StringTools;
import org.sbml.jsbml.util.compilers.LaTeXCompiler;
import org.sbml.squeezer.KineticLawGenerator;
import org.sbml.squeezer.OptionsGeneral;
import org.sbml.squeezer.RateLawNotApplicableException;
import org.sbml.squeezer.ReactionType;
import org.sbml.squeezer.UnitConsistencyType;
import org.sbml.squeezer.kinetics.BasicKineticLaw;
import org.sbml.squeezer.kinetics.OptionsRateLaws;
import org.sbml.squeezer.kinetics.TypeStandardVersion;
import org.sbml.squeezer.util.Bundles;
import org.sbml.tolatex.LaTeXOptions;
import org.sbml.tolatex.util.LaTeX;

import de.zbit.gui.GUITools;
import de.zbit.gui.layout.LayoutHelper;
import de.zbit.sbml.io.SBOTermFormatter;
import de.zbit.util.ResourceManager;
import de.zbit.util.StringUtil;
import de.zbit.util.prefs.SBPreferences;

/**
 * A panel, which contains all possible kinetic equations for the current
 * reaction. A panel that contains the whole message for the user: the message
 * itself, the reversibility and the applicable kinetics.
 * 
 * @author Andreas Dr&auml;ger
 * @author Hannes Borch
 * @date Feb 7, 2008
 * @since 1.0
 * @version $Rev$
*/
public class KineticLawSelectionPanel extends JPanel implements ItemListener {
	/**
	 * 
	 */
	public static final transient ResourceBundle MESSAGES = ResourceManager.getBundle(Bundles.MESSAGES);
	/**
	 * 
	 */
	public static final transient ResourceBundle WARNINGS = ResourceManager.getBundle(Bundles.WARNINGS);
	/**
	 * 
	 */
	private static final String EXISTING_RATE_LAW = MESSAGES.getString("EXISTING_RATE_LAW");

	/**
	 * Generated Serial ID.
	 */
	private static final long serialVersionUID = -3145019506487267364L;

	private static final int width = 310, height = 175;

	private JPanel eqnPrev, optionsPanel;

	private boolean isExistingRateLawSelected;

	private JComboBox kineticLawComboBox;

	private Box kineticsPanel;

	private KineticLawGenerator klg;

	private String selected, laTeXpreview[];
	
	private Class<?> possibleTypes[];

	private JRadioButton rButtonGlobalParameters, rButtonReversible, rButtonsKineticEquations[];

	private Reaction reaction;

	private JCheckBox treatAsEnzymeReaction;
	
	private SBPreferences prefsLaTeX;

	/**
	 * 
	 * @param reaction 
	 * @param possibleLaws
	 * @param settings
	 * @param selected
	 * @throws RateLawNotApplicableException
	 */
	public KineticLawSelectionPanel(Reaction reaction, BasicKineticLaw[] possibleLaws,
			int selected)
			throws RateLawNotApplicableException {
		super(new BorderLayout());
		prefsLaTeX = SBPreferences.getPreferencesFor(LaTeXOptions.class);
		if ((possibleLaws == null) || (selected < 0)
				|| (selected > possibleLaws.length) || (possibleLaws.length < 1)) {
			throw new IllegalArgumentException(WARNINGS.getString("INVALID_RATE_LAW_COUNT"));
		}
		this.reaction = reaction;
		this.possibleTypes = new Class[possibleLaws.length];
		String[] possibleTypesNames = new String[possibleLaws.length];
		laTeXpreview = new String[possibleTypes.length];
		for (int i = 0; i < possibleTypes.length; i++) {
			possibleTypesNames[i] = possibleLaws[i].getSimpleName();
			possibleTypes[i] = possibleLaws[i].getClass();
			try {
				laTeXpreview[i] = possibleLaws[i].getMath().compile(
								new LaTeXCompiler(prefsLaTeX
												.getBoolean(LaTeXOptions.PRINT_NAMES_IF_AVAILABLE)))
						.toString();
			} catch (SBMLException e) {
				laTeXpreview[i] = "invalid";
			}
		}
		kineticLawComboBox = new JComboBox(possibleTypesNames);
		kineticLawComboBox.setEditable(false);
		kineticLawComboBox.setBackground(Color.WHITE);
		kineticLawComboBox.setSelectedIndex(selected);
		createPreviewPanel(kineticLawComboBox.getSelectedIndex());
		kineticLawComboBox.addItemListener(this);
		add(kineticLawComboBox, BorderLayout.NORTH);
		add(eqnPrev, BorderLayout.CENTER);
	}

	/**
	 * A {@link Logger} for this class.
	 */
	private static final transient Logger logger = Logger.getLogger(KineticLawSelectionPanel.class.getName());
	
	/**
	 * @param plugin
	 * @param model
	 * @param reaction
	 * @throws Throwable
	 */
	public KineticLawSelectionPanel(KineticLawGenerator klg, Reaction reaction)
			throws Throwable {
		super(new GridBagLayout());
		prefsLaTeX = SBPreferences.getPreferencesFor(LaTeXOptions.class);
		this.selected = "";
		this.klg = klg;
		this.reaction = reaction;
		this.klg.setReversibility(Boolean.valueOf(this.reaction.getReversible()));
		StringBuilder label = new StringBuilder("<html><body>");
		double stoichiometry = 0;
		for (int i = 0; i < reaction.getReactantCount(); i++) {
			stoichiometry += reaction.getReactant(i).getStoichiometry();
		}
		if (stoichiometry > 2d) {
		  String message = MessageFormat.format(MESSAGES.getString("SPECIES_UNLIKELY_COLLIDE_SPONTAINOUSLY"), StringUtil.toString(stoichiometry));
			label.append("<p><span color=\"red\">" + WARNINGS.getString("WARNING") +": ");
			label.append(message);
			label.append("</span></p>");
			logger.warning(message);
		}
		if (reaction.getFast()) {
		  String message = MessageFormat.format(MESSAGES.getString("REACTION_PROCEEDS_ON_FAST_TIME_SCALE")
				  									+ " " + MESSAGES.getString("ATTRIBUTE_IGNORED"), 
				  								reaction.isSetName() ? reaction.getName() : reaction.getId());
			label.append("<p><span color=\"#505050\">");
			label.append(message);
			label.append("</span></p>");
			logger.warning(message);
		}
		label.append("</body></html>");
		if (reaction.getFast() || (stoichiometry > 2)) {
			JPanel p = new JPanel();
			p.setBackground(Color.WHITE);
			p.add(new JLabel(label.toString(), SwingConstants.LEFT));
			p.setBorder(BorderFactory.createRaisedBevelBorder());
			LayoutHelper.addComponent(this, (GridBagLayout) this.getLayout(),
					p, 0, 0, 1, 1, 1, 1);
		} else {
			LayoutHelper.addComponent(this, (GridBagLayout) this.getLayout(),
					new JLabel(label.toString(), SwingConstants.LEFT), 0, 0, 1,
					1, 1, 1);
		}

		/*
		 * A panel, which contains the question, weather the reaction should be
		 * set to reversible or irreversible. The default is taken from the
		 * current setting of the given reaction.
		 */
		optionsPanel = new JPanel();
		LayoutHelper lh = new LayoutHelper(optionsPanel);
		optionsPanel.setBorder(BorderFactory
				.createTitledBorder(MESSAGES.getString("REACTION_OPTIONS")));

		rButtonReversible = new JRadioButton(MESSAGES.getString("REVERSIBLE"), reaction.getReversible());
		rButtonReversible.setToolTipText(StringUtil.toHTML(MESSAGES.getString("REVERSIBLE_TOOLTIP"), 40));
		
		JRadioButton rButtonIrreversible = new JRadioButton(MESSAGES.getString("IRREVERSIBLE"), !reaction.getReversible());
		rButtonIrreversible.setToolTipText(StringUtil.toHTML(MESSAGES.getString("IRREVERSIBLE_TOOLTIP"), 40));
		
		ButtonGroup revGroup = new ButtonGroup();
		treatAsEnzymeReaction = new JCheckBox(MESSAGES.getString("ENZYME_CATALYSED"));
		treatAsEnzymeReaction.setToolTipText(StringUtil.toHTML(MESSAGES.getString("ENZYME_CATALYSED_TOOLTIP"), 40));
		lh.add(treatAsEnzymeReaction, 0, 0, 2, 1, 1, 1);
		revGroup.add(rButtonReversible);
		revGroup.add(rButtonIrreversible);
		lh.add(rButtonReversible, 0, 1, 1, 1, 1, 1);
		lh.add(rButtonIrreversible, 1, 1, 1, 1, 1, 1);
		JRadioButton rButtonLocalParameters = new JRadioButton(
				MESSAGES.getString("LOCAL_PARAMETERS"), !this.klg.isAddParametersGlobally());
		rButtonLocalParameters.setToolTipText(StringUtil.toHTML(MESSAGES.getString("LOCAL_PARAMETERS_TOOLTIP"), 40));
		
		rButtonGlobalParameters = new JRadioButton(MESSAGES.getString("GLOBAL_PARAMETERS"),
				!rButtonLocalParameters.isSelected());
		rButtonGlobalParameters.setToolTipText(StringUtil.toHTML(MESSAGES.getString("GLOBAL_PARAMETERS_TOOLTIP"), 40));
		ButtonGroup paramGroup = new ButtonGroup();
		paramGroup.add(rButtonGlobalParameters);
		paramGroup.add(rButtonLocalParameters);
		lh.add(rButtonGlobalParameters, 0, 2, 1, 1, 1, 1);
		lh.add(rButtonLocalParameters, 1, 2, 1, 1, 1, 1);

		checkEnzymeKineticsPossible(true);
		klg.setAllReactionsAsEnzymeCatalyzed(treatAsEnzymeReaction.isSelected());
		klg.updateEnzymeCatalysis();
		kineticsPanel = initKineticsPanel();

		LayoutHelper.addComponent(this, (GridBagLayout) this.getLayout(),
				kineticsPanel, 0, 1, 1, 1, 1, 1);
		LayoutHelper.addComponent(this, (GridBagLayout) this.getLayout(),
				optionsPanel, 0, 2, 1, 1, 1, 1);

		// rButtonIrreversible.addItemListener(this);
		treatAsEnzymeReaction.addItemListener(this);
		rButtonReversible.addItemListener(this);
		rButtonGlobalParameters.addItemListener(this);
		rButtonLocalParameters.addItemListener(this);
	}

	/**
	 * Check whether or not enzyme kinetics can be applied.
	 * 
	 * @param init
	 * @throws RateLawNotApplicableException
	 */
	private void checkEnzymeKineticsPossible(boolean init)
			throws RateLawNotApplicableException {
		ReactionType reactionType = klg.getReactionType(reaction.getId());
		boolean allEnzyme = this.klg.isAllReactionsAsEnzymeCatalyzed();
		boolean nonEnzyme = !reactionType.isEnzymeReaction()
				&& (reactionType.isNonEnzymeReaction()
						|| reactionType.isReactionWithGenes()
						|| reactionType.isReactionWithRNAs() || (reaction.getReactantCount() == 0) || (rButtonReversible
						.isSelected() && ReactionType.representsEmptySet(reaction.getListOfProducts())));
		boolean isEnzymeKineticsSelected = !nonEnzyme || (allEnzyme && !nonEnzyme);
		if (reactionType.isEnzymeReaction() || nonEnzyme
				|| reactionType.isReactionWithGenes()
				|| reactionType.isReactionWithRNAs()) {
			treatAsEnzymeReaction.setSelected(isEnzymeKineticsSelected);
			treatAsEnzymeReaction.setEnabled(false);
		} else {
			if (init) {
				treatAsEnzymeReaction.setSelected(isEnzymeKineticsSelected);
			}
			if (!ReactionType.representsEmptySet(reaction.getListOfReactants())
					&& !isExistingRateLawSelected) {
				treatAsEnzymeReaction.setEnabled(true);
			}
		}
	}

	/**
	 * Sets up the panel for the preview of the formula.
	 * 
	 * @param kinNum
	 */
	private void createPreviewPanel(int kinNum) {
		StringBuilder sb = new StringBuilder("v_\\mbox{");
		sb.append(reaction.getId());
		sb.append("}=");
		sb.append(laTeXpreview[kinNum].toString().replace("mathrm", "mbox")
				.replace("text", "mbox").replace("mathtt", "mbox"));
		eqnPrev = new JPanel(new BorderLayout());
		eqnPrev.setBorder(BorderFactory.createTitledBorder(' ' + MESSAGES.getString("EQUATION_PREVIEW") + ' '));
		eqnPrev.add(new LaTeXRenderer(width, height).renderEquation(sb.toString().replace("\\-", "")), BorderLayout.CENTER);
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
	public Class<?> getSelectedKinetic() {
		int i = 0;
		if (rButtonsKineticEquations != null) {
			while ((i < rButtonsKineticEquations.length)
					&& (!rButtonsKineticEquations[i].isSelected())) {
				i++;
			}
		} else if (kineticLawComboBox != null) {
			i = kineticLawComboBox.getSelectedIndex();
		}
		return possibleTypes[i];
	}

	/**
	 * 
	 * @return
	 * @throws Throwable
	 */
	private Box initKineticsPanel() throws Throwable {
		possibleTypes = klg.getReactionType(reaction.getId())
				.identifyPossibleKineticLaws();
		String[] kineticEquations = new String[possibleTypes.length];
		String[] toolTips = new String[possibleTypes.length];
		BasicKineticLaw kineticLaw;
		laTeXpreview = new String[possibleTypes.length + 1];
		int i;
		SBPreferences prefsGeneral = SBPreferences.getPreferencesFor(OptionsGeneral.class);
		SBPreferences prefsRateLaws = SBPreferences.getPreferencesFor(OptionsRateLaws.class);
		double defaultParamVal = prefsGeneral.getDouble(OptionsGeneral.DEFAULT_NEW_PARAMETER_VAL);
		TypeStandardVersion version = TypeStandardVersion.valueOf(prefsRateLaws.get(OptionsRateLaws.TYPE_STANDARD_VERSION));
		UnitConsistencyType consistency = UnitConsistencyType.valueOf(prefsGeneral.get(OptionsGeneral.TYPE_UNIT_CONSISTENCY));
		for (i = 0; i < possibleTypes.length; i++) {
			kineticLaw = klg.createKineticLaw(reaction, possibleTypes[i], false, version, consistency, defaultParamVal);
			laTeXpreview[i] = new String(kineticLaw.getMath().compile(
					new LaTeXCompiler(prefsLaTeX.getBoolean(LaTeXOptions.PRINT_NAMES_IF_AVAILABLE)))
					.toString());
			// toolTips[i] = kineticLaw.isSetSBOTerm() ? "<b>"
			// + kineticLaw.getSBOTermID() + "</b> " : "";
			toolTips[i] = String.format("<b>%s</b>", StringTools
					.firstLetterUpperCase(kineticLaw.toString()));
			if (kineticLaw.isSetSBOTerm()) {
				String definition = SBOTermFormatter.getShortDefinition(SBO
						.getTerm(kineticLaw.getSBOTerm()));
				if (definition != null) {
					toolTips[i] += String.format(": %s", definition);
				}
			}
			toolTips[i] = StringUtil.toHTML(toolTips[i], 60);
			kineticEquations[i] = StringUtil.toHTML(kineticLaw.getSimpleName(), 60);
		}
		sort(possibleTypes, kineticEquations, toolTips, laTeXpreview);
		if (reaction.isSetKineticLaw()) {
		  try {
			laTeXpreview[laTeXpreview.length - 1] = reaction
					.getKineticLaw().getMath().compile(
							new LaTeXCompiler(prefsLaTeX
											.getBoolean(LaTeXOptions.PRINT_NAMES_IF_AVAILABLE)))
					.toString();
		  } catch (Throwable e) {
		    laTeXpreview[laTeXpreview.length - 1] = LaTeX.mathrm("invalid").toString();
		  }
		}
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

				if (reaction.getNotesString().length() > 0) {
					rButtonsKineticEquations[i].setToolTipText(StringUtil.toHTML(
							reaction.getNotesString(), 40));
				} else {
					rButtonsKineticEquations[i]
							.setToolTipText("<html>"+MESSAGES.getString("RATE_LAW_ASSIGNED_TO_REACTION")+"</html>");
				}
			}
			buttonGroup.add(rButtonsKineticEquations[i]);
			if ((i < rButtonsKineticEquations.length - 1)
					|| reaction.isSetKineticLaw())
				LayoutHelper.addComponent(kineticsPanel,
						(GridBagLayout) kineticsPanel.getLayout(),
						rButtonsKineticEquations[i], 0, i, 1, 1, 1, 1);
		}
		if ((kinSelected == -1) && (rButtonsKineticEquations.length > 0)) {
			kinSelected = 0;
			rButtonsKineticEquations[kinSelected].setSelected(true);
		}

		kineticsPanel.setBorder(BorderFactory
				.createTitledBorder(' ' + MESSAGES.getString("CHOOSE_KINETIC_LAW") + ' '));
		createPreviewPanel(kinSelected);
		Box info = new Box(BoxLayout.Y_AXIS);
		info.add(kineticsPanel);
		info.add(eqnPrev);
		// ContainerHandler.setAllBackground(info, Color.WHITE);

		isExistingRateLawSelected = false;

		for (i = 0; i < rButtonsKineticEquations.length; i++) {
			rButtonsKineticEquations[i].addItemListener(this);
		}
		return info; // kineticsPanel;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
	 */
	public void itemStateChanged(ItemEvent ie) {
		if (ie.getSource() instanceof JCheckBox)
			try {
				JCheckBox check = (JCheckBox) ie.getSource();
				setEnzymeCatalysis(check.isSelected());
			} catch (Throwable e) {
				JOptionPane.showMessageDialog(getTopLevelAncestor(), StringUtil
						.toHTML(e.getMessage(), 40), e.getClass()
						.getSimpleName(), JOptionPane.WARNING_MESSAGE);
				e.printStackTrace();
			}
		else if (ie.getSource() instanceof JRadioButton) {
			JRadioButton rbutton = (JRadioButton) ie.getSource();
			if (rbutton.getParent().equals(optionsPanel)) {
				if (rbutton.getText().toLowerCase().contains(MESSAGES.getString("REVERSIBLE").toLowerCase())) {
					try {
						// reversible property was changed.
						selected = "";
						int i;
						for (i = 0; (i < rButtonsKineticEquations.length)
								&& (selected.length() == 0); i++) {
							if (rButtonsKineticEquations[i].isSelected()) {
								selected = rButtonsKineticEquations[i].getText();
							}
						}
						klg.setReversible(reaction.getId(), getReversible());
						klg.setReversibility(getReversible());
						remove(kineticsPanel);
						kineticsPanel = initKineticsPanel();
						LayoutHelper.addComponent(this, (GridBagLayout) this
								.getLayout(), kineticsPanel, 0, 1, 1, 1, 1, 1);
						for (i = 0; i < rButtonsKineticEquations.length; i++) {
							if (selected.equals(rButtonsKineticEquations[i].getText())) {
								rButtonsKineticEquations[i].setSelected(true);
								break;
							}
						}
						updateView();
					} catch (Throwable exc) {
						throw new RuntimeException(exc.getMessage(), exc);
					}
				} else {
					klg.setAddParametersGlobally(rButtonGlobalParameters.isSelected());
				}
			} else {
				int i = 0;
				while ((i < rButtonsKineticEquations.length)
						&& (!rbutton.equals(rButtonsKineticEquations[i]))) {
					i++;
				}
				selected = rButtonsKineticEquations[i].getText();
				try {
					updateView();
				} catch (RateLawNotApplicableException e) {
					JOptionPane.showMessageDialog(this, e.getClass()
							.getSimpleName(), StringUtil.toHTML(e.getMessage(),
							40), JOptionPane.WARNING_MESSAGE);
					e.printStackTrace();
				}
			}
		} else if (ie.getSource() instanceof JComboBox) {
			remove(eqnPrev);
			createPreviewPanel(((JComboBox) ie.getSource()).getSelectedIndex());
			add(eqnPrev, BorderLayout.CENTER);
		}
		if (getTopLevelAncestor() != null) {
			Window w = (Window) getTopLevelAncestor();
			w.validate();
			int width = w.getWidth();
			w.pack();
			w.setSize(width, w.getHeight());
			w.validate();
		}
	}

	/**
	 * 
	 * @param selected
	 * @throws Throwable
	 */
	private void setEnzymeCatalysis(boolean selected) throws Throwable {
		klg.setAllReactionsAsEnzymeCatalyzed(selected);
		klg.updateEnzymeCatalysis();
		remove(kineticsPanel);
		kineticsPanel = initKineticsPanel();
		LayoutHelper.addComponent(this, (GridBagLayout) this.getLayout(),
				kineticsPanel, 0, 1, 1, 1, 1, 1);
	}

	/**
	 * Alphabetically sort the human-readable simple names and change the
	 * ordering of all other elements so the order is the same as in the simple
	 * names array.
	 * 
	 * @param laws
	 * @param typeNames
	 * @param simpleNames
	 * @param toolTips
	 * @param laTeX
	 */
	private void sort(Class<?>[] classes, String[] simpleNames,
			String[] toolTips, String[] laTeX) {
		String names[] = new String[simpleNames.length];
		System.arraycopy(simpleNames, 0, names, 0, simpleNames.length);
		Arrays.sort(simpleNames);
		int indices[] = new int[names.length];
		int i = 0, pos;
		for (String name : simpleNames) {
			pos = 0;
			while (!name.equals(names[pos])) {
				pos++;
			}
			indices[i++] = pos;
		}
		Class<?> types[] = new Class[indices.length];
		String tipps[] = new String[indices.length];
		String lt[] = new String[indices.length];
		System.arraycopy(classes, 0, types, 0, types.length);
		System.arraycopy(toolTips, 0, tipps, 0, tipps.length);
		System.arraycopy(laTeX, 0, lt, 0, lt.length);
		for (i = 0; i < indices.length; i++) {
			classes[i] = types[indices[i]];
			toolTips[i] = tipps[indices[i]];
			laTeX[i] = lt[indices[i]];
		}
	}

	/**
	 * 
	 * @param i
	 * @throws RateLawNotApplicableException
	 */
	private void updateView() throws RateLawNotApplicableException {
		boolean disable = selected.equals(EXISTING_RATE_LAW);
		int i = disable ? rButtonsKineticEquations.length - 1 : 0;
		while ((i < rButtonsKineticEquations.length)
				&& !selected.equals(rButtonsKineticEquations[i].getText())
				&& !disable) {
			i++;
		}
		boolean global = !((reaction.isSetKineticLaw() && reaction
				.getKineticLaw().getLocalParameterCount() > 0) || klg.isAddParametersGlobally());
		boolean change = disable || isExistingRateLawSelected
				|| (i < rButtonsKineticEquations.length - 1);
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
			createPreviewPanel(Math.min(i, rButtonsKineticEquations.length - 1));
			kineticsPanel.add(eqnPrev);
			GUITools.setAllEnabled(optionsPanel, !disable);
		}
		checkEnzymeKineticsPossible(false);
	}

}
