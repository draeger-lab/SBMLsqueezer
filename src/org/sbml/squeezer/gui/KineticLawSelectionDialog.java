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
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.sbml.squeezer.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.BevelBorder;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.NamedSBase;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.io.IOProgressListener;
import org.sbml.squeezer.CfgKeys;
import org.sbml.squeezer.KineticLawGenerator;
import org.sbml.squeezer.LawListener;
import org.sbml.squeezer.SBMLsqueezer;
import org.sbml.squeezer.io.LaTeXExport;
import org.sbml.squeezer.io.SBFileFilter;
import org.sbml.squeezer.io.SBMLio;
import org.sbml.squeezer.io.TextExport;
import org.sbml.squeezer.resources.Resource;

/**
 * This is the main GUI class.
 * 
 * @since 1.0
 * @version
 * @author <a href="mailto:Nadine.hassis@gmail.com">Nadine Hassis</a>
 * @author <a href="mailto:andreas.draeger@uni-tuebingen.de">Andreas
 *         Dr&auml;ger</a>
 * @author <a href="mailto:hannes.borch@googlemail.com">Hannes Borch</a>
 * @date Aug 3, 2007
 */
public class KineticLawSelectionDialog extends JDialog implements
		ActionListener, WindowListener, LawListener, IOProgressListener {

	private static final int fullHeight = 720;

	/**
	 * Generated serial version id.
	 */
	private static final long serialVersionUID = -5980678130366530716L;

	private JPanel centralPanel;

	private JPanel footPanel;

	private JButton helpButton;

	// UI ELEMENTS DEFINITION: ReactionFrame
	private boolean KineticsAndParametersStoredInSBML = false;

	private KineticLawGenerator klg;

	private JLabel label;

	private int numOfWarnings = 0;

	private JButton options;

	private JProgressBar progressBar;

	private JDialog progressDialog;

	private SBMLio sbmlIO;

	private Properties settings;

	private SettingsPanelAll settingsPanel;

	/**
	 * Creates an empty dialog with the given settings and sbml io object.
	 * 
	 * @param owner
	 * @param settings
	 */
	private KineticLawSelectionDialog(Frame owner, Properties settings) {
		super(owner, "SBMLsqueezer", true);
		// if (owner == null)
		// setIconImage(GUITools.ICON_LEMON);
		this.settings = settings;
		this.sbmlIO = null;
		// setAlwaysOnTop(true);
	}

	/**
	 * This constructor allows us to store the given model or the given reaction
	 * in a text file. This can be a LaTeX or another format.
	 * 
	 * @param sbase
	 *            allowed are a reaction or a model instance.
	 */
	public KineticLawSelectionDialog(Frame owner, Properties settings,
			SBase sbase) {
		this(owner, settings);
		SettingsPanelLaTeX panel = new SettingsPanelLaTeX(settings, true);
		if (JOptionPane.showConfirmDialog(this, panel, "LaTeX export",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
				GUITools.ICON_LATEX_SMALL) == JOptionPane.OK_OPTION) {
			for (Object key : panel.getProperties().keySet())
				settings.put(key, panel.getProperties().get(key));
			try {
				final File f = new File(panel.getTeXFile());
				if (f.exists() && f.isDirectory())
					JOptionPane
							.showMessageDialog(
									this,
									GUITools
											.toHTML(
													"No appropriate file was selected and therefore no TeX file was created.",
													40), "Warning",
									JOptionPane.WARNING_MESSAGE);
				else if (!f.exists()
						|| GUITools.overwriteExistingFileDialog(owner, f) == JOptionPane.YES_OPTION) {
					BufferedWriter buffer = new BufferedWriter(
							new FileWriter(f));
					LaTeXExport exporter = new LaTeXExport(panel
							.getProperties());
					if (sbase instanceof Model)
						buffer
								.write(exporter.toLaTeX((Model) sbase)
										.toString());
					else if (sbase instanceof Reaction)
						buffer.write(exporter.toLaTeX((Reaction) sbase)
								.toString());
					buffer.close();
					// new Thread(new Runnable() {
					//
					// public void run() {
					// try {
					// Desktop.getDesktop().open(f);
					// } catch (IOException e) {
					// // e.printStackTrace();
					// }
					// }
					// }).start();
				}
				dispose();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	/**
	 * Creates a kinetic law selection dialog to create kinetic equations for a
	 * whole model.
	 * 
	 * @param owner
	 * @param settings
	 * @param sbmlIO
	 */
	public KineticLawSelectionDialog(Frame owner, Properties settings,
			SBMLio sbmlIO) {
		this(owner, settings);
		this.sbmlIO = sbmlIO;
		init();
	}

	/**
	 * This constructor is necessary for the GUI to generate just one single
	 * rate equation for the given reaction.
	 * 
	 * @param sbmlIO
	 * @param reaction
	 */
	public KineticLawSelectionDialog(Frame owner, Properties settings,
			SBMLio sbmlIO, String reactionID) {
		this(owner, settings);
		try {
			// This thing is necessary for CellDesigner!
			KineticLawWindowAdapter adapter = new KineticLawWindowAdapter(this,
					settings, sbmlIO, reactionID, this);
			pack();
			setResizable(false);
			setLocationRelativeTo(owner);
			setVisible(true);
			KineticsAndParametersStoredInSBML = adapter
					.isKineticsAndParametersStoredInSBML();
			dispose();
		} catch (Throwable exc) {
			JOptionPane.showMessageDialog(this, GUITools.toHTML(exc
					.getMessage(), 40), exc.getClass().getSimpleName(),
					JOptionPane.WARNING_MESSAGE);
			exc.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof JButton) {
			String text = ((JButton) e.getSource()).getText();
			if (text.equals("show options")) {
				showSettingsPanel(true);
			} else if (text.equals("hide options")) {
				showSettingsPanel(false);

			} else if (text.equals("Help")) {
				JHelpBrowser helpBrowser = new JHelpBrowser(this,
						"SBMLsqueezer " + SBMLsqueezer.getVersionNumber()
								+ " - Online Help");
				helpBrowser.addWindowListener(this);
				helpBrowser.setLocationRelativeTo(this);
				helpBrowser.setSize(640, 640);
				helpButton.setEnabled(false);
				helpBrowser.setVisible(true);

			} else if (text.equals("Cancel")) {
				dispose();
				klg = null;
			} else if (text.equals("Restore")) {
				settingsPanel.restoreDefaults();
				getContentPane().remove(settingsPanel);
				pack();
				setLocationRelativeTo(null);

			} else if (text.equals("Generate")) {
				if (sbmlIO != null)
					new Thread(new Runnable() {
						public void run() {
							generateKineticLaws();
						}
					}).start();
				else
					klg = null;

			} else if (text.equals("Back")) {
				getContentPane().remove(footPanel);
				getContentPane().add(footPanel = getFootPanel(0),
						BorderLayout.SOUTH);
				getContentPane().remove(centralPanel);
				getContentPane().add(centralPanel = initOptionsPanel(),
						BorderLayout.CENTER);
				setResizable(false);
				showSettingsPanel(true);
			} else if (text.equals("Export changes")) {
				/*
				 * new Thread(new Runnable() { public void run() {//
				 */
				exportKineticEquations();
				/*
				 * } }).start();/
				 */

			} else if (text.equals("Apply")
					&& !KineticsAndParametersStoredInSBML && klg != null
					&& sbmlIO != null) {
				// GUITools.setAllEnabled(this, false);
				new Thread(new Runnable() {
					public void run() {
						setVisible(false);
						storeKineticsInOriginalModel();
						dispose();
					}
				}).start();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.squeezer.LawListener#currentState(org.sbml.jsbml.SBase,
	 * int)
	 */
	public void currentState(SBase item, int num) {
		label.setText(" Done with " + Integer.toString(num) + " ");
		progressBar.setValue(num);
		if (num >= progressBar.getMaximum())
			progressDialog.dispose();
	}

	/**
	 * This method allows to write the generated kinetic equations to an ASCII
	 * or TeX file.
	 */
	private void exportKineticEquations() {
		if (klg != null) {
			SBFileFilter ff1 = SBFileFilter.TeX_FILE_FILTER;
			SBFileFilter ff2 = SBFileFilter.TEXT_FILE_FILTER;
			JFileChooser chooser = GUITools.createJFileChooser(settings.get(
					CfgKeys.SAVE_DIR).toString(), false, false,
					JFileChooser.FILES_ONLY, ff1, ff2);
			if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
				try {
					final File f = chooser.getSelectedFile();
					if (ff1.accept(f)) {
						LaTeXExport.writeLaTeX(klg.getMiniModel(), f, settings);
						settings.put(CfgKeys.LATEX_DIR, f.getParentFile());
						// new Thread(new Runnable() {
						// public void run() {
						// try {
						// Desktop.getDesktop().open(f);
						// } catch (IOException e) {
						// // e.printStackTrace();
						// }
						// }
						// }).start();
					}
					if (ff2.accept(f)) {
						new TextExport(klg.getMiniModel(), f, settings);
						settings.put(CfgKeys.SAVE_DIR, f.getParentFile());
						// new Thread(new Runnable() {
						//
						// public void run() {
						// try {
						// Desktop.getDesktop().edit(f);
						// } catch (IOException e) {
						// // e.printStackTrace();
						// }
						// }
						// }).start();
					}
				} catch (IOException exc) {
					JOptionPane.showMessageDialog(this, GUITools.toHTML(exc
							.getMessage(), 40), exc.getClass()
							.getCanonicalName(), JOptionPane.WARNING_MESSAGE);
					exc.printStackTrace();
				}
		}
	}

	/**
	 * 
	 */
	private void generateKineticLaws() {
		try {
			showSettingsPanel(false);
			options.setEnabled(false);
			GUITools.setAllEnabled(footPanel, false);
			Properties props = settingsPanel.getSettings();
			for (Object key : props.keySet())
				settings.put(key, props.get(key));
			Model model = sbmlIO.getSelectedModel();
			klg = new KineticLawGenerator(model, settings);
			if (klg.getFastReactions().size() > 0) {
				StringBuilder message = new StringBuilder();
				message.append("The model contains ");
				if (klg.getFastReactions().size() > 1)
					message.append("fast reactions");
				else {
					message.append("the fast reaction ");
					message.append(klg.getFastReactions().get(0).getId());
				}
				message.append(". This feature is currently not supported ");
				message.append("by SBMLsqueezer. Rate laws can still ");
				message.append("be generated properly but the fast attribute ");
				if (klg.getFastReactions().size() > 1) {
					message
							.append("of the following reactions is beeing ignored:");
					message.append("<ul type=\"disc\">");
					for (int i = 0; i < klg.getFastReactions().size(); i++) {
						message.append("<li>");
						message.append(klg.getFastReactions().get(i).getId());
						message.append("</li>");
					}
					message.append("</ul>");
				} else
					message.append("is beeing ignored.");
				final JOptionPane pane = new JOptionPane(GUITools.toHTML(
						message.toString(), 40), JOptionPane.WARNING_MESSAGE);
				final JDialog d = new JDialog();
				d.setTitle("Fast Reactions");
				d.setModal(true);
				d.getContentPane().add(pane);
				d.pack();
				d.setResizable(false);
				d.setLocationRelativeTo(this);
				WindowAdapter adapter = new WindowAdapter() {
					private boolean gotFocus = false;

					public void windowClosing(WindowEvent we) {
						pane.setValue(null);
					}

					public void windowGainedFocus(WindowEvent we) {
						if (!gotFocus) {
							pane.selectInitialValue();
							gotFocus = true;
						}
					}
				};
				d.addWindowListener(adapter);
				d.addWindowFocusListener(adapter);
				d.addComponentListener(new ComponentAdapter() {
					public void componentShown(ComponentEvent ce) {
						pane.setValue(JOptionPane.UNINITIALIZED_VALUE);
					}
				});
				pane.addPropertyChangeListener(new PropertyChangeListener() {
					public void propertyChange(PropertyChangeEvent evt) {
						if (d.isVisible()
								&& evt.getSource() == pane
								&& evt.getPropertyName().equals(
										JOptionPane.VALUE_PROPERTY)
								&& evt.getNewValue() != null
								&& evt.getNewValue() != JOptionPane.UNINITIALIZED_VALUE)
							d.setVisible(false);
					}
				});
				d.setVisible(true);
				d.dispose();
				// JOptionPane.showMessageDialog(null, GUITools.toHTML(message
				// .toString(), 40), "Fast Reactions",
				// JOptionPane.WARNING_MESSAGE);
			}

			JPanel reactionsPanel = new JPanel(new BorderLayout());
			JTable tableOfKinetics = new KineticLawTable(klg);
			numOfWarnings = ((KineticLawTableModel) tableOfKinetics.getModel())
					.getNumOfWarnings();
			tableOfKinetics.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

			JScrollPane scroll;

			if (tableOfKinetics.getRowCount() == 0) {
				JEditorPane pane = new JEditorPane(
						sbmlIO.getSelectedModel().getNumReactions() > 0 ? Resource.class
								.getResource("html/NoNewKineticsCreated.html")
								: Resource.class
										.getResource("html/ModelDoesNotContainAnyReactions.html"));
				pane.addHyperlinkListener(new SystemBrowser());
				pane.setBackground(Color.WHITE);
				pane.setEditable(false);
				scroll = new JScrollPane(pane,
						JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
						JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			} else {
				scroll = new JScrollPane(tableOfKinetics,
						JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
						JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			}
			scroll.setBorder(BorderFactory
					.createBevelBorder(BevelBorder.LOWERED));
			scroll.setBackground(Color.WHITE);
			reactionsPanel.add(scroll, BorderLayout.CENTER);

			JLabel numberOfWarnings = new JLabel(
					"<html><table align=left width=500 cellspacing=10><tr><td>"
							+ "<b>Kinetic Equations</b></td><td>"
							+ "<td>Number of warnings (red): " + numOfWarnings
							+ "</td></tr></table></htlm>");
			numberOfWarnings
					.setToolTipText(GUITools
							.toHTML(
									"The number of reactions an unlikely number of reactants. These are also highlighted in red in the table.",
									40));
			reactionsPanel.add(numberOfWarnings, BorderLayout.NORTH);

			centralPanel.removeAll();
			centralPanel.add(reactionsPanel, BorderLayout.CENTER);
			getContentPane().remove(footPanel);
			getContentPane().add(footPanel = getFootPanel(1),
					BorderLayout.SOUTH);
			setResizable(true);
			setSize(getWidth(), 640);
			GUITools.setAllEnabled(footPanel, true);
			options.setEnabled(true);
			validate();

		} catch (Throwable exc) {
			JOptionPane.showMessageDialog(this, GUITools.toHTML(exc
					.getMessage(), 40), exc.getClass().getSimpleName(),
					JOptionPane.WARNING_MESSAGE);
			exc.printStackTrace();
		}

	}

	/**
	 * Returns the panel on the bottom of this window.
	 * 
	 * @param type
	 *            if i = 0 this will return the foot containing the buttons
	 *            "Help", "Cancel" and "Generate" for i = 1 there will be
	 *            "Export", "Cancel", "Back" and "Apply".
	 * @return
	 */
	private JPanel getFootPanel(int type) {
		GridBagLayout gbl = new GridBagLayout();
		JPanel south = new JPanel(gbl), rightPanel = new JPanel(new FlowLayout(
				FlowLayout.RIGHT)), leftPanel = new JPanel();

		JButton cancel = new JButton("Cancel", GUITools.ICON_DELETE), apply = new JButton();
		cancel.setToolTipText(GUITools.toHTML(
				"Exit SBMLsqueezer without saving changes.", 40));
		cancel.addActionListener(this);
		apply.addActionListener(this);

		rightPanel.add(cancel);
		switch (type) {
		case 0:
			apply
					.setToolTipText(GUITools
							.toHTML(
									"Start generating an ordinary differential equation system.",
									40));
			apply.setText("Generate");
			apply.setIcon(GUITools.ICON_LEMON_TINY);
			helpButton = new JButton("Help", GUITools.ICON_HELP_TINY);
			helpButton.addActionListener(this);
			leftPanel.add(helpButton);
			break;
		default:
			apply
					.setToolTipText(GUITools
							.toHTML(
									"Write the generated kinetics and parameters to the SBML file.",
									40));
			apply.setText("Apply");
			apply.setIcon(GUITools.ICON_TICK_TINY);

			JButton jButtonReactionsFrameSave = new JButton();
			jButtonReactionsFrameSave.setEnabled(true);
			jButtonReactionsFrameSave.setBounds(35, 285, 100, 25);
			jButtonReactionsFrameSave
					.setToolTipText(GUITools
							.toHTML(
									"Allowes yout to save the generated differential equations as *.txt or *.tex files. Note that this export only contains those reactions together with referenced, species, compartments, compartment- and species-types, units, and parameters for which new kinetic equations have been generated. If you wish to obtain a complete model report, please choose the \"save as\" function in the main menu.",
									40));
			jButtonReactionsFrameSave.setText("Export changes");
			jButtonReactionsFrameSave.setIcon(GUITools.ICON_LATEX_TINY);
			jButtonReactionsFrameSave.addActionListener(this);
			leftPanel.add(jButtonReactionsFrameSave);
			JButton back = new JButton("Back", GUITools.ICON_LEFT_ARROW_TINY);
			back.addActionListener(this);
			rightPanel.add(back);
			break;
		}
		rightPanel.add(apply);
		LayoutHelper.addComponent(south, gbl, leftPanel, 0, 0, 1, 1, 0, 0);
		LayoutHelper.addComponent(south, gbl, rightPanel, 1, 0, 3, 1, 1, 1);

		return south;
	}

	/**
	 * This method initializes a Panel that shows all possible settings of the
	 * program.
	 * 
	 * @return javax.swing.JPanelsb.append("<br>
	 *         ");
	 */
	private SettingsPanelAll getJSettingsPanel() {
		if (settingsPanel == null) {
			settingsPanel = new SettingsPanelAll(settings);
			// settingsPanel.setBackground(Color.WHITE);
		}
		return settingsPanel;
	}

	/**
	 * This method actually initializes the GUI.
	 */
	private void init() {
		centralPanel = initOptionsPanel();
		setLayout(new BorderLayout());
		getContentPane().add(centralPanel, BorderLayout.CENTER);
		JLabel label = new JLabel(GUITools.ICON_LOGO_SMALL);
		label.setBackground(Color.WHITE);
		label.setText("<html><body><br><br><br><br><br><br>Version "
				+ SBMLsqueezer.getVersionNumber() + "</body></html>");
		Dimension d = GUITools.getDimension(GUITools.ICON_LOGO_SMALL);
		d.setSize(d.getWidth() + 125, d.getHeight() + 10);
		label.setPreferredSize(new Dimension(d));
		JPanel p = new JPanel();
		p.add(label);
		JScrollPane scroll = new JScrollPane(p,
				JScrollPane.VERTICAL_SCROLLBAR_NEVER,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		GUITools.setAllBackground(scroll, Color.WHITE);
		getContentPane().add(scroll, BorderLayout.NORTH);

		footPanel = getFootPanel(0);
		getContentPane().add(footPanel, BorderLayout.SOUTH);

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setDefaultLookAndFeelDecorated(true);
		setLocationByPlatform(true);
		setResizable(false);
		d.setSize(d.getWidth(), d.getHeight() + 125);
		setMinimumSize(new Dimension(d));
		pack();

		int height = getHeight();
		setSize(getWidth(), fullHeight);
		setLocationRelativeTo(null);
		setSize(getWidth(), height);
		settingsPanel = getJSettingsPanel();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.squeezer.LawListener#initLawListener(java.lang.String, int)
	 */
	public void initLawListener(String className, int count) {
		if (count > 0) {
			if (!className.endsWith("s") && count != 1)
				className += "s";
			progressBar = new JProgressBar(0, count - 1);
			progressBar.setToolTipText("Saving changes in " + className);
			progressBar.setValue(0);
			progressDialog = new JDialog(this, "Saving changes");
			progressDialog
					.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
			JPanel p = new JPanel();
			LayoutHelper lh = new LayoutHelper(p);
			String space = " ";
			for (int i = 0; i < Integer.toString(count).length(); i++)
				space += ' ';
			label = new JLabel(" Done with " + Integer.valueOf(0) + space);
			lh.add(label, 0, 0, 1, 1, 1, 1);
			JPanel progress = new JPanel();
			progress.add(progressBar);
			progress.setBorder(BorderFactory.createLoweredBevelBorder());
			lh.add(progress, 1, 0, 1, 1, 1, 1);
			lh.add(new JLabel(" of " + count + ' ' + className), 2, 0, 1, 1, 1,
					1);
			JPanel outer = new JPanel();
			outer.add(p);
			progressDialog.getContentPane().add(outer);
			progressDialog.pack();
			progressDialog.setResizable(false);
			progressDialog.setLocationRelativeTo(null);
			progressDialog.setVisible(true);
		}
	}

	/**
	 * Returns a JPanel that displays the user options.
	 * 
	 * @return
	 */
	private JPanel initOptionsPanel() {
		JPanel p = new JPanel(new BorderLayout());
		options = new JButton("show options");
		options.setIcon(GUITools.ICON_RIGHT_ARROW);
		options.setIconTextGap(5);
		options.setBorderPainted(false);
		options.setSize(150, 20);
		options.setToolTipText("Customize the advanced settings.");
		options.addActionListener(this);
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(options);
		options.setBackground(new Color(panel.getBackground().getRGB()));
		p.add(panel, BorderLayout.NORTH);
		return p;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.io.IOProgressListener#progress(java.lang.Object)
	 */
	public void ioProgressOn(Object currObject) {
		if (currObject == null)
			progressDialog.dispose();
		else {
			StringBuilder sb = new StringBuilder();
			sb.append("writing ");
			sb.append(currObject.getClass().getSimpleName());
			if (currObject instanceof NamedSBase) {
				sb.append(' ');
				sb.append(((NamedSBase) currObject).getId());
			}
			label.setText(GUITools.toHTML(sb.toString(), 40));
		}
	}

	/**
	 * Method that indicates whether or not changes have been introduced into
	 * the given model.
	 * 
	 * @return True if kinetic equations and parameters or anything else were
	 *         changed by SBMLsqueezer.
	 */
	public boolean isKineticsAndParametersStoredInSBML() {
		return KineticsAndParametersStoredInSBML;
	}

	/**
	 * 
	 * @param show
	 */
	private void showSettingsPanel(boolean show) {
		if (show) {
			options.setIcon(GUITools.ICON_DOWN_ARROW);
			options.setIconTextGap(5);
			options.setToolTipText("<html>Hide detailed options</html>");
			options.setText("hide options");
			settingsPanel = getJSettingsPanel();
			JScrollPane scroll = new JScrollPane(settingsPanel,
					JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
					JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			scroll.setBackground(Color.WHITE);
			centralPanel.add(scroll, BorderLayout.CENTER);
			centralPanel.validate();
		} else {
			options.setIcon(GUITools.ICON_RIGHT_ARROW);
			options.setIconTextGap(5);
			options.setText("show options");
			options.setToolTipText(GUITools
					.toHTML("Customize the advanced settings."));
			centralPanel.removeAll();
			JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			panel.add(options);
			centralPanel.add(panel, BorderLayout.NORTH);
		}
		pack();
		// setSize(getWidth(), (int) Math.min(fullHeight, GraphicsEnvironment
		// .getLocalGraphicsEnvironment().getMaximumWindowBounds()
		// .getHeight()));
		validate();
	}

	/**
	 * 
	 */
	private void storeKineticsInOriginalModel() {
		try {
			klg.storeKineticLaws(this);
			progressDialog = new JDialog(this, "Saving changes");
			progressDialog
					.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
			label = new JLabel();
			progressDialog.getContentPane().add(label);
			progressDialog.setSize(200, 150);
			progressDialog.setLocationRelativeTo(null);
			progressDialog.setVisible(true);
			sbmlIO.saveChanges(this);
			SBMLsqueezerUI.checkForSBMLErrors(this, sbmlIO.getSelectedModel(),
					sbmlIO.getWriteWarnings(), ((Boolean) settings
							.get(CfgKeys.SHOW_SBML_WARNINGS)).booleanValue());
			KineticsAndParametersStoredInSBML = true;
		} catch (SBMLException exc) {
			JOptionPane.showMessageDialog(this, GUITools.toHTML(exc
					.getMessage(), 40), exc.getClass().getCanonicalName(),
					JOptionPane.WARNING_MESSAGE);
			exc.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.WindowListener#windowActivated(java.awt.event.WindowEvent)
	 */
	public void windowActivated(WindowEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.WindowListener#windowClosed(java.awt.event.WindowEvent)
	 */
	public void windowClosed(WindowEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.WindowListener#windowClosing(java.awt.event.WindowEvent)
	 */
	public void windowClosing(WindowEvent e) {
		if (e.getSource() instanceof JHelpBrowser)
			helpButton.setEnabled(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.WindowListener#windowDeactivated(java.awt.event.WindowEvent
	 * )
	 */
	public void windowDeactivated(WindowEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.WindowListener#windowDeiconified(java.awt.event.WindowEvent
	 * )
	 */
	public void windowDeiconified(WindowEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.WindowListener#windowIconified(java.awt.event.WindowEvent)
	 */
	public void windowIconified(WindowEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.WindowListener#windowOpened(java.awt.event.WindowEvent)
	 */
	public void windowOpened(WindowEvent e) {
	}
}
