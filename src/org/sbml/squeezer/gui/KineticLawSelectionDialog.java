/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of SBMLsqueezer, a Java program that creates rate 
 * equations for reactions in SBML files (http://sbml.org).
 *
 * Copyright (C) 2006-2012 by the University of Tuebingen, Germany.
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
import java.beans.EventHandler;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;

import org.sbml.jsbml.Model;
import org.sbml.squeezer.KineticLawGenerator;
import org.sbml.squeezer.SqueezerOptions;
import org.sbml.squeezer.io.SBMLio;
import org.sbml.squeezer.resources.Resource;
import org.sbml.squeezer.util.Bundles;
import org.sbml.tolatex.LaTeXOptions;
import org.sbml.tolatex.io.LaTeXReportGenerator;
import org.sbml.tolatex.io.TextExport;

import de.zbit.gui.GUIOptions;
import de.zbit.gui.JHelpBrowser;
import de.zbit.gui.LayoutHelper;
import de.zbit.gui.StatusBar;
import de.zbit.gui.SystemBrowser;
import de.zbit.gui.prefs.PreferencesDialog;
import de.zbit.io.SBFileFilter;
import de.zbit.util.AbstractProgressBar;
import de.zbit.util.StringUtil;
import de.zbit.util.prefs.SBPreferences;

/**
 * This is the main GUI class.
 * 
 * @author Nadine Hassis
 * @author Andreas Dr&auml;ger
 * @author Hannes Borch
 * @date Aug 3, 2007
 * @since 1.0
 * @version $Rev$
 */
public class KineticLawSelectionDialog extends JDialog implements ActionListener {

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
	
	/**
	 * 
	 */
	private final Logger logger = Logger.getLogger(KineticLawSelectionDialog.class.getName());

	private int numOfWarnings = 0;

	private JButton options;

	private SBMLio sbmlIO;
	
	SBPreferences prefs;

	private StatusBar statusBar;


	/**
	 * Creates an empty dialog with the given settings and sbml io object.
	 * 
	 * @param owner
	 * @param progressListener 
	 */
	private KineticLawSelectionDialog(Frame owner) {
		super(owner, Bundles.MESSAGES.getString("SBMLSQUEEZER"), true);
		// if (owner == null)
		// setIconImage(GUITools.ICON_LEMON);
		this.sbmlIO = null;
		this.prefs = new SBPreferences(SqueezerOptions.class);
		// setAlwaysOnTop(true);
	}

	/**
	 * Creates a kinetic law selection dialog to create kinetic equations for a
	 * whole model.
	 * 
	 * @param owner
	 * @param sbmlIO
	 */
	public KineticLawSelectionDialog(Frame owner, SBMLio sbmlIO) {
		this(owner);
		this.sbmlIO = sbmlIO;
		init();
		// get new statusbar and limit the log message length
		statusBar = StatusBar.addStatusBar((JFrame) this.getOwner());
		statusBar.limitLogMessageLength(this.getWidth()-130);
	}

	/**
	 * This constructor is necessary for the GUI to generate just one single
	 * rate equation for the given reaction.
	 * 
	 * @param sbmlIO
	 * @param reaction
	 */
	public KineticLawSelectionDialog(Frame owner, SBMLio sbmlIO, String reactionID) {
		this(owner);
		
		try {
			
			// This thing is necessary for CellDesigner!
			final KineticLawWindowAdapter adapter = new KineticLawWindowAdapter(this,
					sbmlIO, reactionID);
			pack();
			setResizable(false);
			setLocationRelativeTo(owner);
			setVisible(true);
			
			// get new statusbar and limit the log message length
			statusBar = StatusBar.addStatusBar((JFrame) this.getOwner());
			statusBar.limitLogMessageLength(this.getWidth()-130);

			AbstractProgressBar progressBar = statusBar.showProgress();
			adapter.showProgress(progressBar);
			KineticsAndParametersStoredInSBML = adapter.isKineticsAndParametersStoredInSBML();
			dispose();
			statusBar.hideProgress();
			statusBar.unsetLogMessageLimit();
			logger.log(Level.INFO, Bundles.LABELS.getString("READY"));
			
		} catch (Throwable exc) {
			GUITools.showErrorMessage(this, exc);
		}
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@SuppressWarnings("unchecked")
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof JButton) {
			String text = ((JButton) e.getSource()).getText();
			if (text.equals(Bundles.MESSAGES.getString("SHOW_OPTIONS"))) {
				PreferencesDialog.showPreferencesDialog(this, SqueezerOptions.class);
			} else if (text.equals(Bundles.BASE.getString("HELP"))) {
				JHelpBrowser helpBrowser = new JHelpBrowser(this,
						Bundles.MESSAGES.getString("SBMLSQUEEZER") 
							+ " " + String.format(Bundles.LABELS.getString("ONLINE_HELP_FOR_THE_PROGRAM"),
													System.getProperty("app.version")), 
						getClass().getResource("../resources/html/help.html"));
				helpBrowser.addWindowListener(EventHandler.create(WindowListener.class, this, "windowClosing", ""));
				helpBrowser.setLocationRelativeTo(this);
				helpBrowser.setSize(640, 640);
				helpButton.setEnabled(false);
				helpBrowser.setVisible(true);

			} else if (text.equals(Bundles.LABELS.getString("WIZARD_CANCEL"))) {
				dispose();
				logger.log(Level.INFO, Bundles.LABELS.getString("READY"));
				klg = null;
			} else if (text.equals(Bundles.MESSAGES.getString("RESTORE"))) {
				pack();
				setLocationRelativeTo(null);

			} else if (text.equals(Bundles.MESSAGES.getString("GENERATE"))) {
				// show statusBar for the law generation, generate kinetic laws
				// get new statusbar and limit the log message length
				statusBar = StatusBar.addStatusBar((JFrame) this.getOwner());
				statusBar.limitLogMessageLength(this.getWidth()-130);
				
				if (sbmlIO != null){
					
					new Thread(new Runnable() {
						public void run() {
							generateKineticLaws();
						}
					}).start();
					
				}
				else {
					klg = null;
				}

			} else if (text.equals(Bundles.LABELS.getString("WIZARD_BACK"))) {
				getContentPane().remove(footPanel);
				getContentPane().add(footPanel = getFootPanel(0),
						BorderLayout.SOUTH);
				getContentPane().remove(centralPanel);
				getContentPane().add(centralPanel = initOptionsPanel(),
						BorderLayout.CENTER);
				setResizable(false);
			} else if (text.equals(Bundles.MESSAGES.getString("EXPORT_CHANGES"))) {
				/*
				 * new Thread(new Runnable() { public void run() {//
				 */
				exportKineticEquations();
				/*
				 * } }).start();/
				 */

			} else if (text.equals(Bundles.LABELS.getString("APPLY").split(";")[0])
					&& !KineticsAndParametersStoredInSBML && klg != null
					&& sbmlIO != null) {
				// GUITools.setAllEnabled(this, false);
				new Thread(new Runnable() {
					public void run() {
						setVisible(false);
						storeKineticsInOriginalModel();
						dispose();
						statusBar.hideProgress();
						statusBar.unsetLogMessageLimit();
						logger.log(Level.INFO, Bundles.LABELS.getString("READY"));
					}
				}).start();
			}
		}
	}

	/**
	 * This method allows to write the generated kinetic equations to an ASCII
	 * or TeX file.
	 */
	private void exportKineticEquations() {
		if (klg != null) {
			SBFileFilter ff1 = SBFileFilter.createTeXFileFilter();
			SBFileFilter ff2 = SBFileFilter.createTextFileFilter();
			SBPreferences guiPrefs = SBPreferences
					.getPreferencesFor(GUIOptions.class);
			JFileChooser chooser = GUITools.createJFileChooser(guiPrefs
					.get(GUIOptions.OPEN_DIR), false, false,
					JFileChooser.FILES_ONLY, ff1, ff2);
			if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
				try {
					final File f = chooser.getSelectedFile();
					if (ff1.accept(f)) {
						SBPreferences lprefs = SBPreferences
								.getPreferencesFor(LaTeXOptions.class);
						LaTeXReportGenerator export = new LaTeXReportGenerator(
								lprefs.getBoolean(LaTeXOptions.LANDSCAPE),
								lprefs.getBoolean(LaTeXOptions.TYPEWRITER),
								lprefs.getShort(LaTeXOptions.FONT_SIZE),
								lprefs.get(LaTeXOptions.PAPER_SIZE),
								lprefs.getBoolean(LaTeXOptions.SHOW_PREDEFINED_UNITS),
								lprefs.getBoolean(LaTeXOptions.TITLE_PAGE),
								lprefs.getBoolean(LaTeXOptions.PRINT_NAMES_IF_AVAILABLE));
						export.toLaTeX(klg.getMiniModel(), f);
						guiPrefs.put(GUIOptions.OPEN_DIR, f.getParentFile());
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
						new TextExport(klg.getMiniModel(), f);
						guiPrefs.put(GUIOptions.OPEN_DIR, f.getParentFile());
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
					guiPrefs.flush();
				} catch (Exception exc) {
					GUITools.showErrorMessage(this, exc);
				}
		}
	}

	/**
	 * 
	 */
	private void generateKineticLaws() {
		try {
			options.setEnabled(false);
			GUITools.setAllEnabled(footPanel, false);
			prefs.flush();
			Model model = sbmlIO.getSelectedModel();
			
			klg = new KineticLawGenerator(model);
			
			AbstractProgressBar progressBar = statusBar.showProgress();
			
			klg.setProgressBar(progressBar);
			klg.generateLaws();
			//TODO: is this needed?
			statusBar.hideProgress();
			statusBar.unsetLogMessageLimit();
			
			if (klg.getFastReactions().size() > 0) {
				StringBuilder message = new StringBuilder();
				String modelContains = Bundles.MESSAGES.getString("THE_MODEL_CONTAINS")+" ";
				if (klg.getFastReactions().size() > 1)
					message.append(MessageFormat.format(modelContains, Bundles.MESSAGES.getString("FAST_REACTIONS")));
				else {
					message.append(MessageFormat.format(modelContains, Bundles.MESSAGES.getString("THE_FAST_REACTION"))+" ");
					message.append(klg.getFastReactions().get(0).getId());
				}
				message.append(". ");
				message.append(Bundles.MESSAGES.getString("NOT_SUPPORTED"));
				if (klg.getFastReactions().size() > 1) {
					message.append(Bundles.MESSAGES.getString("ATTRIBUTE_OF_REACTIONS_BEEING_IGNORED"));
					message.append("<ul type=\"disc\">");
					for (int i = 0; i < klg.getFastReactions().size(); i++) {
						message.append("<li>");
						message.append(klg.getFastReactions().get(i).getId());
						message.append("</li>");
					}
					message.append("</ul>");
				} else
					message.append(Bundles.MESSAGES.getString("ATTRIBUTE_BEEING_IGNORED"));
				final JOptionPane pane = new JOptionPane(StringUtil.toHTML(
						message.toString(), 40), JOptionPane.WARNING_MESSAGE);
				final JDialog d = new JDialog();
				d.setTitle(Bundles.MESSAGES.getString("FAST_REACTIONS"));
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
				statusBar.hideProgress();
				logger.log(Level.INFO, Bundles.LABELS.getString("READY"));
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
							+ "<b>" + Bundles.MESSAGES.getString("KINETIC_EQUATIONS") + "</b></td><td>"
							+ "<td>" + Bundles.MESSAGES.getString("NUMBER_OF_WARNINGS")
							+ " " + "("+Bundles.MESSAGES.getString("COLOR_RED")+"): " + numOfWarnings
							+ "</td></tr></table></htlm>");
			numberOfWarnings
					.setToolTipText(StringUtil.toHTML(
									Bundles.MESSAGES.getString("NUMBER_OF_WARNING_TOOLTIP"),
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
			GUITools.showErrorMessage(this, exc);
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

		JButton cancel = new JButton(Bundles.LABELS.getString("WIZARD_CANCEL"), UIManager.getIcon("ICON_DELETE"));
		JButton apply = new JButton();
		cancel.setToolTipText(StringUtil.toHTML(Bundles.MESSAGES.getString("EXIT_WITHOUT_SAVING"), 40));
		cancel.addActionListener(this);
		apply.addActionListener(this);

		rightPanel.add(cancel);
		switch (type) {
		case 0:
			apply.setToolTipText(StringUtil.toHTML(Bundles.MESSAGES.getString("GENERATE_TOOLTIP"), 40));
			apply.setText(Bundles.MESSAGES.getString("GENERATE"));
			apply.setIcon(UIManager.getIcon("ICON_LEMON_TINY"));
			helpButton = new JButton(Bundles.BASE.getString("HELP"), UIManager
					.getIcon("ICON_HELP_TINY"));
			helpButton.addActionListener(this);
			leftPanel.add(helpButton);
			break;
		default:
			apply.setToolTipText(StringUtil.toHTML(Bundles.MESSAGES.getString("WRITE_KINETICS_TO_SBML_FILE"), 40));
			apply.setText(Bundles.LABELS.getString("APPLY").split(";")[0]);
			apply.setIcon(UIManager.getIcon("ICON_TICK_TINY"));

			JButton jButtonReactionsFrameSave = new JButton();
			jButtonReactionsFrameSave.setEnabled(true);
			jButtonReactionsFrameSave.setBounds(35, 285, 100, 25);
			jButtonReactionsFrameSave.setToolTipText(StringUtil.toHTML(Bundles.MESSAGES.getString("EXPORT_CHANGES_TOOLTIP"), 40));
			jButtonReactionsFrameSave.setText(Bundles.MESSAGES.getString("EXPORT_CHANGES"));
			jButtonReactionsFrameSave.setIcon(UIManager
					.getIcon("ICON_LATEX_TINY"));
			jButtonReactionsFrameSave.addActionListener(this);
			leftPanel.add(jButtonReactionsFrameSave);
			JButton back = new JButton(Bundles.LABELS.getString("WIZARD_BACK"), UIManager
					.getIcon("ICON_LEFT_ARROW_TINY"));
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
	 * This method actually initializes the GUI.
	 */
	private void init() {
		centralPanel = initOptionsPanel();
		setLayout(new BorderLayout());
		getContentPane().add(centralPanel, BorderLayout.CENTER);
		JLabel label = new JLabel(UIManager.getIcon("ICON_LOGO_SMALL"));
		label.setBackground(Color.WHITE);
		label.setText("<html><body><br><br><br><br><br><br>"
						+ Bundles.MESSAGES.getString("VERSION") + " "
						+ System.getProperty("app.version") + "</body></html>");
		Dimension d = GUITools.getDimension(UIManager
				.getIcon("ICON_LOGO_SMALL"));
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
	}

	/**
	 * Returns a JPanel that displays the user options.
	 * 
	 * @return
	 */
	private JPanel initOptionsPanel() {
		JPanel p = new JPanel(new BorderLayout());
		options = new JButton(Bundles.MESSAGES.getString("SHOW_OPTIONS"));
		Icon icon = UIManager.getIcon("ICON_PREFS_16");
		if (icon != null) {
			options.setIcon(icon);
		}
		options.setIconTextGap(5);
		options.setBorderPainted(false);
		options.setSize(150, 20);
		options.setToolTipText(Bundles.MESSAGES.getString("SHOW_OPTIONS_TOOLTIP"));
		options.addActionListener(this);
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(options);
		options.setBackground(new Color(panel.getBackground().getRGB()));
		p.add(panel, BorderLayout.NORTH);
		return p;
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
	 */
	private void storeKineticsInOriginalModel() {
		// show statusBar for the synchronization
		// get new statusbar and limit the log message length
		statusBar = StatusBar.addStatusBar((JFrame) this.getOwner());
		statusBar.limitLogMessageLength(this.getWidth()-130);
		AbstractProgressBar progressBar = statusBar.showProgress();
		klg.setProgressBar(progressBar);
		
		klg.storeKineticLaws();
		SBMLsqueezerUI.checkForSBMLErrors(this, sbmlIO.getSelectedModel(),
				sbmlIO.getWriteWarnings(), prefs
						.getBoolean(SqueezerOptions.SHOW_SBML_WARNINGS));
		KineticsAndParametersStoredInSBML = true;
		
		statusBar.hideProgress();
		statusBar.unsetLogMessageLimit();
	}

	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowClosing(java.awt.event.WindowEvent)
	 */
	public void windowClosing(WindowEvent e) {
		if (e.getSource() instanceof JHelpBrowser) {
			helpButton.setEnabled(true);
		}
	}

}
