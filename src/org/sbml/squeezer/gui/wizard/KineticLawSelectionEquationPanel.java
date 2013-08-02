/*
 * $Id: KineticLawSelectionEquationPanel.java 830 2012-02-26 00:33:31Z snagel $
 * $URL: https://rarepos.cs.uni-tuebingen.de/svn-path/SBMLsqueezer/trunk/src/org/sbml/squeezer/gui/wizard/KineticLawSelectionEquationPanel.java $
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

package org.sbml.squeezer.gui.wizard;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;

import org.sbml.squeezer.KineticLawGenerator;
import org.sbml.squeezer.OptionsGeneral;
import org.sbml.squeezer.gui.KineticLawTable;
import org.sbml.squeezer.gui.KineticLawTableModel;
import org.sbml.squeezer.gui.SBMLsqueezerUI;
import org.sbml.squeezer.io.SBMLio;
import org.sbml.squeezer.util.Bundles;
import org.sbml.tolatex.LaTeXOptions;
import org.sbml.tolatex.LaTeXOptions.PaperSize;
import org.sbml.tolatex.io.LaTeXReportGenerator;
import org.sbml.tolatex.io.TextExport;

import de.zbit.gui.GUIOptions;
import de.zbit.gui.GUITools;
import de.zbit.gui.SystemBrowser;
import de.zbit.gui.wizard.WizardFinishingListener;
import de.zbit.io.filefilter.SBFileFilter;
import de.zbit.util.ResourceManager;
import de.zbit.util.StringUtil;
import de.zbit.util.prefs.SBPreferences;
import de.zbit.util.progressbar.gui.ProgressBarSwing;

/**
 * This class implements the reaction frame
 * 
 * @author Sebastian Nagel
 * @date Feb 25, 2012
 * @since 1.4
 * @version $Rev: 830 $
 */
public class KineticLawSelectionEquationPanel extends JPanel implements ActionListener, PropertyChangeListener {
	
	/**
	 * 
	 */
	public static final transient ResourceBundle MESSAGES = ResourceManager.getBundle(Bundles.MESSAGES);
	/**
	 * 
	 */
	public static final transient ResourceBundle LABELS = ResourceManager.getBundle(Bundles.LABELS);

	/**
	 * Generated serial version identifier.
	 */
	private static final long serialVersionUID = 2381499189035630841L;
	
	/**
	 * A {@link Logger} for this class.
	 */
	private final Logger logger = Logger.getLogger(KineticLawSelectionEquationPanelDescriptor.class.getName());
	
	private KineticLawGenerator klg;
	
	private SBMLio sbmlIO;
	
	private SBPreferences prefs;

	private boolean kineticsAndParametersStoredInSBML = false;
	
	private int numOfWarnings = 0;
	private JPanel reactionsPanel;
	private KineticLawTable tableOfKinetics;
	private ProgressBarSwing progressBarSwing;
	private List<WizardFinishingListener> listOfFinishingListeners;
	
	
	
	/**
	 * 
	 * @param klg 
	 * @param sbmlIO
	 */
	public KineticLawSelectionEquationPanel(KineticLawGenerator klg, SBMLio sbmlIO) {
		super(new BorderLayout());
		
		this.prefs = new SBPreferences(OptionsGeneral.class);
		this.sbmlIO = sbmlIO;
		this.klg = klg;
		
		init();
	}
	
	/**
	 * init all panel components
	 */
	private void init() {
		JButton jButtonReactionsFrameSave = new JButton();
		jButtonReactionsFrameSave.setEnabled(true);
		jButtonReactionsFrameSave.setBounds(35, 285, 100, 25);
		jButtonReactionsFrameSave.setToolTipText(StringUtil.toHTML(MESSAGES.getString("EXPORT_CHANGES_TOOLTIP"), 40));
		jButtonReactionsFrameSave.setText(MESSAGES.getString("EXPORT_CHANGES"));
		jButtonReactionsFrameSave.setIcon(UIManager.getIcon("ICON_LATEX_TINY"));
		jButtonReactionsFrameSave.addActionListener(this);
		
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		buttonPanel.add(jButtonReactionsFrameSave);
		
		add(buttonPanel, BorderLayout.SOUTH);
	}
	
	/**
	 * Method that indicates whether or not changes have been introduced into
	 * the given model.
	 * 
	 * @return {@code true} if kinetic equations and parameters or anything else were
	 *         changed by SBMLsqueezer.
	 */
	public boolean isKineticsAndParametersStoredInSBML() {
		return kineticsAndParametersStoredInSBML;
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
								PaperSize.valueOf(lprefs.getString(LaTeXOptions.PAPER_SIZE)),
								lprefs.getBoolean(LaTeXOptions.SHOW_PREDEFINED_UNITS),
								lprefs.getBoolean(LaTeXOptions.TITLE_PAGE),
								lprefs.getBoolean(LaTeXOptions.PRINT_NAMES_IF_AVAILABLE));
						export.toLaTeX(klg.getSubmodel(), f);
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
						new TextExport(klg.getSubmodel(), f);
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
	 * store generated kinetic laws in model
	 */
	public void apply() {
		if (!kineticsAndParametersStoredInSBML && (klg != null)
				&& (sbmlIO != null)) {
			kineticsAndParametersStoredInSBML = true;
			reactionsPanel.removeAll();
			JProgressBar progress = new JProgressBar();
			ProgressBarSwing pbs = new ProgressBarSwing(progress);
			klg.setProgressBar(pbs);
			reactionsPanel.add(progress, BorderLayout.CENTER);
			validate();
			final Component parent = this;
			SwingWorker<Void, Void> sw = new SwingWorker<Void, Void>() {

				/* (non-Javadoc)
				 * @see javax.swing.SwingWorker#doInBackground()
				 */
				@Override
				protected Void doInBackground() throws Exception {
					klg.storeKineticLaws();
					return null;
				}

				/* (non-Javadoc)
				 * @see javax.swing.SwingWorker#done()
				 */
				@Override
				protected void done() {
					SBMLsqueezerUI.checkForSBMLErrors(
						parent,
						sbmlIO.getSelectedModel(),
						sbmlIO.getWriteWarnings(), 
						prefs.getBoolean(OptionsGeneral.SHOW_SBML_WARNINGS)
				  );
					kineticsAndParametersStoredInSBML = true;
					logger.log(Level.INFO, LABELS.getString("READY"));
					for (WizardFinishingListener listener : listOfFinishingListeners) {
						listener.wizardFinished();
					}
				}
				
			};
			sw.execute();
		}
	}
	
	/**
	 * shows the table with all generated kinetic laws
	 */
	public void generateKineticLawDone() {
		if (klg.getFastReactions().size() > 0) {
			StringBuilder message = new StringBuilder();
			String modelContains = MESSAGES.getString("THE_MODEL_CONTAINS") + " ";
			if (klg.getFastReactions().size() > 1) {
				message.append(MessageFormat.format(modelContains, MESSAGES.getString("FAST_REACTIONS")));
			} else {
				message.append(MessageFormat.format(modelContains, MESSAGES.getString("THE_FAST_REACTION")) + " ");
				message.append(klg.getFastReactions().get(0).getId());
			}
			message.append(". ");
			message.append(MESSAGES.getString("NOT_SUPPORTED"));
			if (klg.getFastReactions().size() > 1) {
				message.append(MESSAGES.getString("ATTRIBUTE_OF_REACTIONS_BEEING_IGNORED"));
				message.append("<ul type=\"disc\">");
				for (int i = 0; i < klg.getFastReactions().size(); i++) {
					message.append("<li>");
					message.append(klg.getFastReactions().get(i).getId());
					message.append("</li>");
				}
				message.append("</ul>");
			} else {
				message.append(MESSAGES.getString("ATTRIBUTE_BEEING_IGNORED"));
			}
			final JOptionPane pane = new JOptionPane(StringUtil.toHTML(
				message.toString(), 40), JOptionPane.WARNING_MESSAGE);
			final JDialog d = new JDialog();
			d.setTitle(MESSAGES.getString("FAST_REACTIONS"));
			d.setModal(true);
			d.getContentPane().add(pane);
			d.pack();
			d.setResizable(false);
			WindowAdapter adapter = new WindowAdapter() {
				private boolean gotFocus = false;
				
				/* (non-Javadoc)
				 * @see java.awt.event.WindowAdapter#windowClosing(java.awt.event.WindowEvent)
				 */
				@Override
				public void windowClosing(WindowEvent we) {
					pane.setValue(null);
				}
				
				/* (non-Javadoc)
				 * @see java.awt.event.WindowAdapter#windowGainedFocus(java.awt.event.WindowEvent)
				 */
				@Override
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
				/* (non-Javadoc)
				 * @see java.awt.event.ComponentAdapter#componentShown(java.awt.event.ComponentEvent)
				 */
				@Override
				public void componentShown(ComponentEvent ce) {
					pane.setValue(JOptionPane.UNINITIALIZED_VALUE);
				}
			});
			pane.addPropertyChangeListener(new PropertyChangeListener() {
				/* (non-Javadoc)
				 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
				 */
				public void propertyChange(PropertyChangeEvent evt) {
					if (d.isVisible()
							&& (evt.getSource() == pane)
							&& evt.getPropertyName().equals(JOptionPane.VALUE_PROPERTY)
								&& (evt.getNewValue() != null)
								&& (evt.getNewValue() != JOptionPane.UNINITIALIZED_VALUE)) {
						d.setVisible(false);
					}
				}
			});
			d.setVisible(true);
			d.dispose();
			
			logger.log(Level.INFO, LABELS.getString("READY"));
			// JOptionPane.showMessageDialog(null, GUITools.toHTML(message
			// .toString(), 40), "Fast Reactions",
			// JOptionPane.WARNING_MESSAGE);
		}

		if (reactionsPanel != null) {
			remove(reactionsPanel);
		}
		reactionsPanel = new JPanel(new BorderLayout());
		JProgressBar progressbar = new JProgressBar(0, klg.getCreatedKineticsCount());
		progressBarSwing = new ProgressBarSwing(progressbar);
		reactionsPanel.add(progressbar, BorderLayout.CENTER);
		add(reactionsPanel, BorderLayout.CENTER);
		validate();
		tableOfKinetics = new KineticLawTable(klg, progressBarSwing, this);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof JButton) {
			String text = ((JButton) e.getSource()).getText();
			if (text.equals(MESSAGES.getString("EXPORT_CHANGES"))) {
				/*
				 * new Thread(new Runnable() { public void run() {//
				 */
				exportKineticEquations();
				/*
				 * } }).start();/
				 */
			}
		}
	}

	/* (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		try {
			reactionsPanel.removeAll();
			reactionsPanel.validate();
			
			remove(reactionsPanel);
			reactionsPanel = new JPanel(new BorderLayout());
			
			JScrollPane scroll = null;
			
			if (klg.getCreatedKineticsCount() == 0) {
				JEditorPane pane = new JEditorPane(
					sbmlIO.getSelectedModel().getReactionCount() > 0 ? 
						getClass().getResource("../../resources/html/NoNewKineticsCreated.html") : 
						getClass().getResource("../../resources/html/ModelDoesNotContainAnyReactions.html"));
				pane.addHyperlinkListener(new SystemBrowser());
				pane.setBackground(Color.WHITE);
				pane.setEditable(false);
				scroll = new JScrollPane(pane);
			} else {
				numOfWarnings = ((KineticLawTableModel) tableOfKinetics.getModel())
						.getWarningCount();
				tableOfKinetics.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
				JLabel numberOfWarnings = new JLabel(
						"<html><table align=left width=500 cellspacing=10><tr><td>"
								+ "<b>" + MESSAGES.getString("KINETIC_EQUATIONS") + "</b></td><td>"
								+ "<td>" + MESSAGES.getString("NUMBER_OF_WARNINGS")
								+ " " + "("+MESSAGES.getString("COLOR_RED")+"): " + numOfWarnings
								+ "</td></tr></table></htlm>");
				numberOfWarnings.setToolTipText(StringUtil.toHTML(
						MESSAGES.getString("NUMBER_OF_WARNING_TOOLTIP"),
						40));
				reactionsPanel.add(numberOfWarnings, BorderLayout.NORTH);
				
				scroll = new JScrollPane(tableOfKinetics);
			}
			scroll.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
			scroll.setBackground(Color.WHITE);
			
			reactionsPanel.add(scroll, BorderLayout.CENTER);
			
			add(reactionsPanel, BorderLayout.CENTER);
			validate();
			validateTree();
			
		} catch (Throwable exc) {
			GUITools.showErrorMessage(this, exc);
		}
	}

	/**
	 * 
	 * @param listener
	 */
	public boolean addFinishingListener(WizardFinishingListener listener) {
		if (listOfFinishingListeners == null) {
			this.listOfFinishingListeners = new LinkedList<WizardFinishingListener>();
		}
		return this.listOfFinishingListeners.add(listener);
	}
	
	/**
	 * 
	 * @param listener
	 * @return
	 */
	public boolean removeFinishingListener(WizardFinishingListener listener) {
		if (listOfFinishingListeners == null) {
			return false;
		}
		return listOfFinishingListeners.remove(listener);
	}

}
