package org.sbml.squeezer.functionTermGenerator.wizard;

import de.zbit.gui.JHelpBrowser;
import de.zbit.gui.wizard.WizardPanelDescriptor;
import de.zbit.util.ResourceManager;
import org.sbml.squeezer.SBMLsqueezer;
import org.sbml.squeezer.functionTermGenerator.FunctionTermGenerator;
import org.sbml.squeezer.util.Bundles;

import java.awt.*;
import java.util.ResourceBundle;

/**
 * This class implements the descriptor for the option (main) panel. (Aug 30, 2020)
 *
 * Based on
 * @see org.sbml.squeezer.gui.wizard.KineticLawSelectionOptionPanelDescriptor
 * by
 * @author Sebastian Nagel
 *
 * Adapted by:
 * @author Eike Pertuch
 *
 * @since 2.1.2
 */
public class FunctionTermGeneratorOptionPanelDescriptor extends WizardPanelDescriptor{

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
    public static final String IDENTIFIER = "FUNCTION_TERM_OPTION_PANEL";

    /**
     *
     */
    public FunctionTermGeneratorOptionPanelDescriptor(FunctionTermGenerator ftg) {
        super(IDENTIFIER, new FunctionTermGeneratorOptionPanel(ftg));
    }

    /* (non-Javadoc)
     * @see de.zbit.gui.wizard.WizardPanelDescriptor#aboutToDisplayPanel()
     */
    @Override
    public void aboutToDisplayPanel() {
    }

    /* (non-Javadoc)
     * @see de.zbit.gui.wizard.WizardPanelDescriptor#getNextPanelDescriptor()
     */
    @Override
    public Object getNextPanelDescriptor() {
        return FunctionTermGeneratorProgressPanelDescriptor.IDENTIFIER;
    }

    /* (non-Javadoc)
     * @see de.zbit.gui.wizard.WizardPanelDescriptor#getBackPanelDescriptor()
     */
    @Override
    public Object getBackPanelDescriptor() {
        return null;
    }

    /* (non-Javadoc)
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
}
