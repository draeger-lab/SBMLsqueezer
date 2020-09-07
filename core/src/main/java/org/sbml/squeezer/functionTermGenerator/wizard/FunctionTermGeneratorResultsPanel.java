package org.sbml.squeezer.functionTermGenerator.wizard;

import de.zbit.gui.GUITools;
import de.zbit.gui.SystemBrowser;
import de.zbit.gui.wizard.WizardFinishingListener;
import de.zbit.util.ResourceManager;
import de.zbit.util.prefs.SBPreferences;
import de.zbit.util.progressbar.gui.ProgressBarSwing;
import org.sbml.jsbml.ext.qual.QualConstants;
import org.sbml.jsbml.ext.qual.QualModelPlugin;
import org.sbml.squeezer.SBMLsqueezer;
import org.sbml.squeezer.functionTermGenerator.DefaultTerm;
import org.sbml.squeezer.functionTermGenerator.FunctionTermGenerator;
import org.sbml.squeezer.functionTermGenerator.FunctionTermOptions;
import org.sbml.squeezer.functionTermGenerator.FunctionTermTable;
import org.sbml.squeezer.io.SBMLio;
import org.sbml.squeezer.util.Bundles;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implements class for results displaying panel (Aug 20, 2020)
 *
 * Based on:
 * @see org.sbml.squeezer.gui.wizard.KineticLawSelectionEquationPanel
 * by:
 * @author Sebastian Nagel
 *
 * Adapted by:
 * @author Eike Pertuch
 *
 * @since 2.1.2
 */
public class FunctionTermGeneratorResultsPanel extends JPanel implements PropertyChangeListener{

    /**
     * Localization support.
     */
    public static final transient ResourceBundle MESSAGES = ResourceManager.getBundle(Bundles.MESSAGES);
    /**
     * Localization support.
     */
    public static final transient ResourceBundle LABELS = ResourceManager.getBundle(Bundles.LABELS);

    /**
     * Generated serial version identifier.
     */
    private static final long serialVersionUID = 2381499189035630841L;

    /**
     * A {@link Logger} for this class.
     */
    private final Logger logger = Logger.getLogger(FunctionTermGeneratorResultsPanelDescriptor.class.getName());

    private FunctionTermGenerator ftg;

    private SBMLio<?> sbmlIO;

    SBPreferences prefs;

    private JPanel transitionsPanel;
    private FunctionTermTable tableOfFunctionTerms;
    private ProgressBarSwing progressBarSwing;
    private List<WizardFinishingListener> listOfFinishingListeners;
    private boolean functionTermsStoredInSBML = false;

    /**
     *
     *
     * @param ftg
     * @param sbmlIO
     */
    public FunctionTermGeneratorResultsPanel(FunctionTermGenerator ftg, SBMLio<?> sbmlIO) {
        super(new BorderLayout());

        prefs = new SBPreferences(FunctionTermOptions.class);
        this.ftg = ftg;
        this.sbmlIO = sbmlIO;
    }

    /**
     * shows the table with all generated function terms
     */
    public void generateFunctionTermDone() {

        if (transitionsPanel != null) {
            remove(transitionsPanel);
        }
        transitionsPanel = new JPanel(new BorderLayout());
        JProgressBar progressbar = new JProgressBar(0, ftg.getCreatedFunctionTermsCount());
        progressBarSwing = new ProgressBarSwing(progressbar);
        transitionsPanel.add(progressbar, BorderLayout.CENTER);
        add(transitionsPanel, BorderLayout.CENTER);
        validate();
        tableOfFunctionTerms = new FunctionTermTable(ftg, progressBarSwing, this);
    }


    /* (non-Javadoc)
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        try {
            transitionsPanel.removeAll();
            transitionsPanel.validate();

            remove(transitionsPanel);
            transitionsPanel = new JPanel(new BorderLayout());

            JScrollPane scroll = null;

            if (ftg.getCreatedFunctionTermsCount() == 0) {
                JEditorPane pane = new JEditorPane(
                        ((QualModelPlugin)(sbmlIO.getSelectedModel().getPlugin(QualConstants.shortLabel))).getTransitionCount() > 0 ?
                                ftg.getDefaultTerm() == DefaultTerm.none?
                                        SBMLsqueezer.class.getResource("resources/html/DefaultTermNotSpecified.html"):
                                        SBMLsqueezer.class.getResource("resources/html/NoNewFunctionTermsCreated.html")
                                :
                                SBMLsqueezer.class.getResource("resources/html/ModelDoesNotContainAnyTransitions.html"));
                pane.addHyperlinkListener(new SystemBrowser());
                pane.setBackground(Color.WHITE);
                pane.setEditable(false);
                scroll = new JScrollPane(pane);
            } else {
                JLabel label = new JLabel("<html><br/><b>" + MESSAGES.getString("FUNCTION_TERMS") + "</b><br/><br/></html>");
                transitionsPanel.add(label, BorderLayout.NORTH);
                scroll = new JScrollPane(tableOfFunctionTerms);
            }
            scroll.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
            scroll.setBackground(Color.WHITE);

            transitionsPanel.add(scroll, BorderLayout.CENTER);

            add(transitionsPanel, BorderLayout.CENTER);
            validate();
        } catch (Throwable exc) {
            GUITools.showErrorMessage(this, exc);
        }
    }

    /**
     * Applies changes to sbml model (stores generated function terms)
     *
     */
    public void apply() {
        SwingWorker<Void, Void> sw = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                ftg.storeChanges(sbmlIO.getSelectedModel());
                functionTermsStoredInSBML = true;
                return null;
            }

            protected void done() {
                logger.log(Level.INFO, LABELS.getString("READY"));
                for (WizardFinishingListener listener : listOfFinishingListeners) {
                    listener.wizardFinished();
                }
            }
        };
        sw.execute();
    }

    /**
     *
     * @param listener
     */
    public boolean addFinishingListener(WizardFinishingListener listener) {
        if (listOfFinishingListeners == null) {
            listOfFinishingListeners = new LinkedList<WizardFinishingListener>();
        }
        return listOfFinishingListeners.add(listener);
    }

    /**
     * Method that indicates whether or not changes have been introduced into
     * the given model.
     *
     * @return {@code true} if function terms were
     *         changed by SBMLsqueezer.
     */
    public boolean areFunctionTermsStoredInSBML() {
        return functionTermsStoredInSBML;
    }

}
