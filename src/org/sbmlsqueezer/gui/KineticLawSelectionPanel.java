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
import java.io.File;
import java.io.IOException;
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

import org.sbmlsqueezer.io.LaTeXExport;
import org.sbmlsqueezer.io.ParameterLogger;
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

	private boolean isReactionReversible;

	private boolean isExistingRateLawSelected;

	private boolean isReversibleSelected;

	private boolean isParametersGlobal;

	private boolean isGlobalSelected;

	private boolean isKineticLawDefined;

	private short possibleTypes[];

	private JPanel optionsPanel;

	private JRadioButton rButtonsKineticEquations[];

	private JRadioButton rButtonReversible;

	private JRadioButton rButtonIrreversible;

	private JRadioButton rButtonGlobalParameters;

	private JRadioButton rButtonLocalParameters;

	private PluginModel model;

	private PluginReaction reaction;

	private KineticLawGenerator klg;

	private Box kineticsPanel;

	private JPanel eqnPrev;

	private String notes;

	private String[] laTeXpreview;

	private static final int width = 310, height = 175;

	/**
	 * @param plugin
	 * @param model
	 * @param reaction
	 * @throws RateLawNotApplicableException
	 * @throws IOException
	 */
	public KineticLawSelectionPanel(KineticLawGenerator klg, PluginModel model,
			PluginReaction reaction) throws RateLawNotApplicableException,
			IOException {
		super(new GridBagLayout());
		this.klg = klg;
		this.model = model;
		this.reaction = reaction;
		this.isReactionReversible = reaction.getReversible();
		this.isKineticLawDefined = reaction.getKineticLaw() != null;
		if (isKineticLawDefined)
			this.isParametersGlobal = reaction.getKineticLaw()
					.getListOfParameters().size() == 0;
		else
			this.isParametersGlobal = true;
		isGlobalSelected = isParametersGlobal;
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
		optionsPanel = new JPanel(new GridBagLayout());
		optionsPanel.setBorder(BorderFactory
				.createTitledBorder("Reaction options"));

		rButtonReversible = new JRadioButton("Reversible", reaction
				.getReversible());
		rButtonIrreversible = new JRadioButton("Irreversible", !reaction
				.getReversible());
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

	private Box initKineticsPanel() throws RateLawNotApplicableException {
		possibleTypes = klg.identifyPossibleReactionTypes(model, reaction);
		String[] kineticEquations = new String[possibleTypes.length];
		String[] toolTips = new String[possibleTypes.length];
		laTeXpreview = new String[possibleTypes.length + 1];
		int i;
		for (i = 0; i < possibleTypes.length; i++) {
			BasicKineticLaw kinetic = klg.createKineticLaw(model, reaction,
					possibleTypes[i], false);
			laTeXpreview[i] = kinetic.getKineticTeX();
			toolTips[i] = toHTML(kinetic.getName(), 40);
			kineticEquations[i] = toHTML(klg.getEquationName(possibleTypes[i]),
					40);
		}

		if (isKineticLawDefined)
			try {
				laTeXpreview[laTeXpreview.length - 1] = (new LaTeXExport())
						.toLaTeX(model, reaction.getKineticLaw().getMath())
						.toString();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.err.println("Error: Unhandled IOException");
			}

		JPanel kineticsPanel = new JPanel(new GridBagLayout());
		rButtonsKineticEquations = new JRadioButton[kineticEquations.length + 1];
		ButtonGroup buttonGroup = new ButtonGroup();

		for (i = 0; i < rButtonsKineticEquations.length; i++) {
			if (i < rButtonsKineticEquations.length - 1) {
				rButtonsKineticEquations[i] = (i != 0) ? new JRadioButton(
						kineticEquations[i], false) : new JRadioButton(
						kineticEquations[i], true);
				rButtonsKineticEquations[i].setToolTipText(toolTips[i]);
			} else {
				rButtonsKineticEquations[i] = new JRadioButton(
						"Existing rate law", false);

				if (reaction.getNotesString().length() > 0)
					rButtonsKineticEquations[i].setToolTipText(toHTML(reaction
							.getNotesString(), 40));
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

		kineticsPanel.setBorder(BorderFactory
				.createTitledBorder(" Please choose one kinetic law "));
		setPreviewPanel(0);
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
				+ laTeXpreview[kinNum].replaceAll("mathrm", "mbox").replaceAll(
						"text", "mbox").replaceAll("mathtt", "mbox")
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
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroll.setBorder(BorderFactory.createLoweredBevelBorder());
		scroll.setBackground(Color.WHITE);
		scroll.setPreferredSize(dim);
		// ContainerHandler.setAllBackground(scroll, Color.WHITE);
		eqnPrev.add(scroll, BorderLayout.CENTER);
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
					reaction.setReversible(getReversible());
					remove(kineticsPanel);
					kineticsPanel = initKineticsPanel();
					LayoutHelper.addComponent(this, (GridBagLayout) this
							.getLayout(), kineticsPanel, 0, 1, 1, 1, 1, 1);
				} catch (RateLawNotApplicableException exc) {
					throw new RuntimeException(exc.getMessage(), exc);
				}
			} else {
				klg.setAddAllParametersGlobally(getGlobal());
				isGlobalSelected = getGlobal();
			}
		} else {
			int i = 0; /*
						 * , width = eqnPrev.getWidth(), height = eqnPrev
						 * .getHeight();
						 */
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
			ContainerHandler.setAllEnabled(optionsPanel,
					i != rButtonsKineticEquations.length - 1);
		}
		validate();
		getTopLevelAncestor().validate();
		Window w = (Window) getTopLevelAncestor();
		int width = w.getWidth();
		w.pack();
		w.setSize(width, w.getHeight());
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

	/**
	 * Returns true if all parameters are set to be global.
	 * 
	 * @return
	 */

	public boolean getGlobal() {
		return rButtonGlobalParameters.isSelected();
	}

	public boolean getExistingRateLawSelected() {
		return isExistingRateLawSelected;
	}

	public String getReactionNotes() {
		return notes;
	}
}
