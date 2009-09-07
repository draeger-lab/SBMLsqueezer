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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.IllegalFormatException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

import org.sbml.jlibsbml.Reaction;
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
public class KineticLawSelectionPanel extends JPanel implements ActionListener {

	/**
	 * Generated Serial ID.
	 */
	private static final long serialVersionUID = -3145019506487267364L;

	private boolean isReactionReversible;

	private boolean isExistingRateLawSelected;

	private boolean isReversibleSelected;

	private boolean isParametersGlobal;

	private boolean isGlobalSelected;

	private boolean isKineticLawDefined;

	private Kinetics possibleTypes[];

	private JPanel optionsPanel;

	private JRadioButton rButtonsKineticEquations[];

	private JRadioButton rButtonReversible;

	private JRadioButton rButtonIrreversible;

	private JRadioButton rButtonGlobalParameters;

	private JRadioButton rButtonLocalParameters;

	private Reaction reaction;

	private KineticLawGenerator klg;

	private Box kineticsPanel;

	private JPanel eqnPrev;

	private String notes;

	private String selected;

	private StringBuffer[] laTeXpreview;

	private static final int width = 310, height = 175;

	/**
	 * @param plugin
	 * @param model
	 * @param reaction
	 * @throws RateLawNotApplicableException
	 * @throws IOException
	 */
	public KineticLawSelectionPanel(KineticLawGenerator klg, Reaction reaction)
			throws RateLawNotApplicableException, IOException {
		super(new GridBagLayout());
		this.selected = "";
		this.klg = klg;
		this.reaction = reaction;
		this.isReactionReversible = reaction.getReversible();
		this.isKineticLawDefined = reaction.getKineticLaw() != null;
		if (isKineticLawDefined)
			this.isParametersGlobal = reaction.getKineticLaw()
					.getListOfParameters().size() == 0;
		else
			this.isParametersGlobal = true;
		isGlobalSelected = isParametersGlobal;
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
		optionsPanel = new JPanel(new GridBagLayout());
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
		rButtonIrreversible = new JRadioButton("Irreversible", !reaction
				.getReversible());
		rButtonIrreversible.setToolTipText(GUITools.toHTML(
				"If selected, SBMLsqueezer will not take effects of products into "
						+ "account when creating rate equations.", 40));
		ButtonGroup revGroup = new ButtonGroup();
		revGroup.add(rButtonReversible);
		revGroup.add(rButtonIrreversible);
		LayoutHelper.addComponent(optionsPanel, (GridBagLayout) optionsPanel
				.getLayout(), rButtonReversible, 0, 0, 1, 1, 1, 1);
		LayoutHelper.addComponent(optionsPanel, (GridBagLayout) optionsPanel
				.getLayout(), rButtonIrreversible, 1, 0, 1, 1, 1, 1);
		rButtonIrreversible.addActionListener(this);
		rButtonReversible.addActionListener(this);
		isReversibleSelected = reaction.getReversible();
		rButtonGlobalParameters = new JRadioButton("Global parameters",
				isParametersGlobal);
		rButtonGlobalParameters
				.setToolTipText("<html> If selected, newly created parameters will <br>"
						+ "be stored globally in the model. </html>");
		rButtonLocalParameters = new JRadioButton("Local parameters",
				!isParametersGlobal);
		rButtonLocalParameters
				.setToolTipText("<html> If selected, newly created parameters will <br>"
						+ "be stored locally in this reaction. </html>");
		ButtonGroup paramGroup = new ButtonGroup();
		paramGroup.add(rButtonGlobalParameters);
		paramGroup.add(rButtonLocalParameters);
		LayoutHelper.addComponent(optionsPanel, (GridBagLayout) optionsPanel
				.getLayout(), rButtonGlobalParameters, 0, 1, 1, 1, 1, 1);
		LayoutHelper.addComponent(optionsPanel, (GridBagLayout) optionsPanel
				.getLayout(), rButtonLocalParameters, 1, 1, 1, 0, 1, 1);
		rButtonGlobalParameters.addActionListener(this);
		rButtonLocalParameters.addActionListener(this);

		kineticsPanel = initKineticsPanel();

		LayoutHelper.addComponent(this, (GridBagLayout) this.getLayout(),
				kineticsPanel, 0, 1, 1, 1, 1, 1);
		LayoutHelper.addComponent(this, (GridBagLayout) this.getLayout(),
				new JSeparator(), 0, 2, 1, 1, 1, 1);
		LayoutHelper.addComponent(this, (GridBagLayout) this.getLayout(),
				optionsPanel, 0, 3, 1, 1, 1, 1);

		// ContainerHandler.setAllBackground(this, Color.WHITE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		JRadioButton rbutton = (JRadioButton) e.getSource();
		if (rbutton.getParent().equals(optionsPanel)) {
			if (rbutton.getText().contains("eversible")) {
				isReversibleSelected = getReversible();
				try {
					// reversible property was changed.
					selected = "";
					for (int i = 0; i < rButtonsKineticEquations.length
							&& selected.length() == 0; i++)
						if (rButtonsKineticEquations[i].isSelected())
							selected = rButtonsKineticEquations[i].getText();
					reaction.setReversible(getReversible());
					remove(kineticsPanel);
					kineticsPanel = initKineticsPanel();
					LayoutHelper.addComponent(this, (GridBagLayout) this
							.getLayout(), kineticsPanel, 0, 1, 1, 1, 1, 1);
				} catch (RateLawNotApplicableException exc) {
					throw new RuntimeException(exc.getMessage(), exc);
				} catch (IOException exc) {
					JOptionPane.showMessageDialog(this, "<html>"
							+ exc.getMessage() + "</html>", exc.getClass()
							.getName(), JOptionPane.ERROR_MESSAGE);
					exc.printStackTrace();
				}
			} else {
				klg.getSettings().put(
						CfgKeys.ADD_NEW_PARAMETERS_ALWAYS_GLOBALLY,
						Boolean.valueOf(getGlobal()));
				isGlobalSelected = getGlobal();
			}
		} else {
			int i = 0;
			while ((i < rButtonsKineticEquations.length)
					&& (!rbutton.equals(rButtonsKineticEquations[i])))
				i++;
			kineticsPanel.remove(eqnPrev);
			setPreviewPanel(i);
			kineticsPanel.add(eqnPrev);
			if (i == rButtonsKineticEquations.length - 1) {
				rButtonReversible.setSelected(isReactionReversible);
				rButtonIrreversible.setSelected(!isReactionReversible);
				rButtonGlobalParameters.setSelected(isParametersGlobal);
				rButtonLocalParameters.setSelected(!isParametersGlobal);
				isExistingRateLawSelected = true;
			} else {
				isExistingRateLawSelected = false;
				rButtonReversible.setSelected(isReversibleSelected);
				rButtonIrreversible.setSelected(!isReversibleSelected);
				rButtonGlobalParameters.setSelected(isGlobalSelected);
				rButtonLocalParameters.setSelected(!isGlobalSelected);
			}
			GUITools.setAllEnabled(optionsPanel,
					i != rButtonsKineticEquations.length - 1);
		}
		validate();
		getTopLevelAncestor().validate();
		Window w = (Window) getTopLevelAncestor();
		int width = w.getWidth();
		w.pack();
		w.setSize(width, w.getHeight());
	}

	public boolean getExistingRateLawSelected() {
		return isExistingRateLawSelected;
	}

	/**
	 * Returns true if all parameters are set to be global.
	 * 
	 * @return
	 */

	public boolean getGlobal() {
		return rButtonGlobalParameters.isSelected();
	}

	public String getReactionNotes() {
		return notes;
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
		while ((i < rButtonsKineticEquations.length)
				&& (!rButtonsKineticEquations[i].isSelected()))
			i++;
		return possibleTypes[i];
	}

	/**
	 * 
	 * @return
	 * @throws RateLawNotApplicableException
	 * @throws IOException
	 */
	private Box initKineticsPanel() throws RateLawNotApplicableException,
			IOException {
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
				toolTips[i] = !kinetic.isSetSBOTerm() ? "<b>"
						+ kinetic.getSBOTermID() + "</b> " : "";
				toolTips[i] = GUITools.toHTML(toolTips[i] + kinetic.getName(),
						40);
				kineticEquations[i] = GUITools.toHTML(possibleTypes[i]
						.getEquationName(), 40);
			} catch (IllegalFormatException e) {
				e.printStackTrace();
			}
		if (isKineticLawDefined)
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
						"Existing rate law", false);

				if (reaction.getNotesString().length() > 0)
					rButtonsKineticEquations[i].setToolTipText(GUITools.toHTML(
							reaction.getNotesString(), 40));
				else
					rButtonsKineticEquations[i]
							.setToolTipText("<html> This rate law is currently assigned to this reaction.</html>");
			}
			buttonGroup.add(rButtonsKineticEquations[i]);
			rButtonsKineticEquations[i].addActionListener(this);
			if (i < rButtonsKineticEquations.length - 1 || isKineticLawDefined)
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
				+ laTeXpreview[kinNum].toString().replace("mathrm", "mbox")
						.replace("text", "mbox").replace("mathtt", "mbox")
				+ "\\end{equation}"), BorderLayout.CENTER);
		preview.setBackground(Color.WHITE);
		eqnPrev = new JPanel();
		eqnPrev.setBorder(BorderFactory
				.createTitledBorder(" Equation Preview "));
		eqnPrev.setLayout(new BorderLayout());
		Dimension dim = new Dimension(width, height);
		notes = rButtonsKineticEquations[kinNum].getToolTipText().replace(
				"<html>", "").replace("</html>", "").replace("<body>", "")
				.replace("</body>", "").replace("<br>", "");
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
}
