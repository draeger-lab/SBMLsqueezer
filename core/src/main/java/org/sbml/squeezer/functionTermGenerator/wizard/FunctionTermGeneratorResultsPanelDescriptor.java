package org.sbml.squeezer.functionTermGenerator.wizard;

import de.zbit.gui.JHelpBrowser;
import de.zbit.gui.wizard.WizardFinishingListener;
import de.zbit.gui.wizard.WizardPanelDescriptor;
import de.zbit.util.ResourceManager;
import org.sbml.squeezer.SBMLsqueezer;
import org.sbml.squeezer.functionTermGenerator.FunctionTermGenerator;
import org.sbml.squeezer.io.SBMLio;
import org.sbml.squeezer.util.Bundles;

import java.awt.*;
import java.util.ResourceBundle;

/**
 * This class implements descriptor for results panel (Aug 20, 2020)
 *
 * Based on
 * @see org.sbml.squeezer.gui.wizard.KineticLawSelectionEquationPanelDescriptor
 * by
 * @author Sebastian Nagel
 *
 * Adapted by:
 * @author Eike Pertuch
 *
 * @since 2.1.2
 */
public class FunctionTermGeneratorResultsPanelDescriptor extends WizardPanelDescriptor {

    /**
     * Localization support.
     */
    public static final transient ResourceBundle MESSAGES = ResourceManager.getBundle(Bundles.MESSAGES);

    /**
     * Localization support.
     */
    public static final transient ResourceBundle LABELS = ResourceManager.getBundle(Bundles.LABELS);

    /**
     *
     */
    public static final String IDENTIFIER = "FUNCTION_TERM_RESULTS_PANEL";

    /**
     *
     */
    private FunctionTermGeneratorResultsPanel panel;

    /**
     *
     * @param ftg
     */
    public FunctionTermGeneratorResultsPanelDescriptor(FunctionTermGenerator ftg, SBMLio<?> sbmlIO) {
        super(IDENTIFIER, new FunctionTermGeneratorResultsPanel(ftg, sbmlIO));
        panel = ((FunctionTermGeneratorResultsPanel) getPanelComponent());
    }

    /* (non-Javadoc)
     * @see de.zbit.gui.wizard.WizardPanelDescriptor#displayingPanel()
     */
    @Override
    public void displayingPanel() {
        // when function terms are generated, show the respective table
        panel.generateFunctionTermDone();
    }

    /* (non-Javadoc)
     * @see de.zbit.gui.wizard.WizardPanelDescriptor#addFinishingListener(de.zbit.gui.wizard.WizardFinishingListener)
     */
    @Override
    public boolean addFinishingListener(WizardFinishingListener listener) {
        return panel.addFinishingListener(listener) && super.addFinishingListener(listener);
    }


    /* (non-Javadoc)
     * @see de.zbit.gui.wizard.WizardPanelDescriptor#getNextPanelDescriptor()
     */
    @Override
    public Object getNextPanelDescriptor() {
        return FINISH;
    }

    /* (non-Javadoc)
     * @see de.zbit.gui.wizard.WizardPanelDescriptor#getBackPanelDescriptor()
     */
    @Override
    public Object getBackPanelDescriptor() {
        return FunctionTermGeneratorOptionPanelDescriptor.IDENTIFIER;
    }

    /* (non-Javadoc)        init();
     * @see de.zbit.gui.wizard.WizardPanelDescriptor#getHelpAction()
     */
    @Override
    public Component getHelpAction() {
        JHelpBrowser helpBrowser = new JHelpBrowser(getWizard().getDialog(),
                System.getProperty("app.name")
                        + " "
                        + String.format(LABELS.getString("ONLINE_HELP_FOR_THE_PROGRAM"),
                        System.getProperty("app.version")),
                SBMLsqueezer.class.getResource("resources/html/help.html"));
        helpBrowser.setLocationRelativeTo(getWizard().getDialog());
        helpBrowser.setSize(640, 640);

        return helpBrowser;
    }

    /* (non-Javadoc)
     * @see de.zbit.gui.wizard.WizardPanelDescriptor#finish()
     */
    @Override
    public boolean finish() {
        panel.apply();
        return false;
    }
}
