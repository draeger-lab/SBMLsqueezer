package org.sbml.squeezer.functionTermGenerator.wizard;

import de.zbit.gui.wizard.Wizard;
import de.zbit.gui.wizard.WizardPanelDescriptor;
import de.zbit.util.progressbar.gui.ProgressBarSwing;
import org.sbml.jsbml.Model;
import org.sbml.squeezer.functionTermGenerator.FunctionTermGenerator;
import org.sbml.squeezer.functionTermGenerator.FunctionTermGeneratorWorker;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * This class implements the progress panel
 *
 * Based on
 * @see org.sbml.squeezer.gui.wizard.KineticLawSelectionEquationProgressPanelDescriptor
 * by
 * @author Sebastian Nagel
 *
 * Adapted by:
 * @author Eike Pertuch
 *
 * @since 2.1.2
 */
public class FunctionTermGeneratorProgressPanelDescriptor extends WizardPanelDescriptor implements PropertyChangeListener {


    public static final String IDENTIFIER = "FUNCTION_TERM_GENERATOR_PROGRESS_PANEL";

    /**
     *
     */
    private FunctionTermGenerator ftg;
    private Model model;

    public FunctionTermGeneratorProgressPanelDescriptor(FunctionTermGenerator ftg, Model model) {
        super(IDENTIFIER, new JPanel(new BorderLayout()));
        this.ftg = ftg;
        this.model = model;
    }

    /* (non-Javadoc)
     * @see de.zbit.gui.wizard.WizardPanelDescriptor#displayingPanel()
     */
    @Override
    public void displayingPanel() {
        // disable the buttons while progress is running
        Wizard wizard = getWizard();
        wizard.setNextFinishButtonEnabled(false);
        wizard.setBackButtonEnabled(false);
        // set progress bar
        JProgressBar progressBar = new JProgressBar();
        JPanel p = (JPanel) getPanelComponent();
        p.add(progressBar, BorderLayout.CENTER);
        ftg.setProgressBar(new ProgressBarSwing(progressBar));
        // generate function terms
        FunctionTermGeneratorWorker worker = new FunctionTermGeneratorWorker(ftg, model);
        worker.addPropertyChangeListener(this);
        worker.execute();
    }

    /* (non-Javadoc)
     * @see de.zbit.gui.wizard.WizardPanelDescriptor#aboutToHidePanel()
     */
    @Override
    public void aboutToHidePanel() {
    }

    /* (non-Javadoc)
     * @see de.zbit.gui.wizard.WizardPanelDescriptor#getNextPanelDescriptor()
     */
    @Override
    public Object getNextPanelDescriptor() {
        return FunctionTermGeneratorResultsPanelDescriptor.IDENTIFIER;
    }

    /* (non-Javadoc)
     * @see de.zbit.gui.wizard.WizardPanelDescriptor#getBackPanelDescriptor()
     */
    @Override
    public Object getBackPanelDescriptor() {
        return FunctionTermGeneratorOptionPanelDescriptor.IDENTIFIER;
    }

    /* (non-Javadoc)
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("generateFunctionTermsDone")) {
            // when progress is done, go to next panel automatically
            getWizard().goToNextPanel();
        }
    }
}
