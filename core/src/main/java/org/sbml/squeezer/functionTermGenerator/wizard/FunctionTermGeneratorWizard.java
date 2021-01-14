package org.sbml.squeezer.functionTermGenerator.wizard;

import java.awt.Dimension;
import java.awt.Frame;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import javax.swing.JDialog;

import org.sbml.squeezer.functionTermGenerator.FunctionTermGenerator;
import org.sbml.squeezer.gui.wizard.KineticLawSelectionEquationPanelDescriptor;
import org.sbml.squeezer.io.SBMLio;
import org.sbml.squeezer.util.Bundles;

import de.zbit.gui.GUITools;
import de.zbit.gui.wizard.Wizard;
import de.zbit.gui.wizard.WizardPanelDescriptor;
import de.zbit.util.ResourceManager;

import static de.zbit.util.Utils.getMessage;

/**
 * This class implements a wizard for the FunctionTermGenerator (Aug 20, 2020)
 *
 * Based on
 * @see org.sbml.squeezer.gui.wizard.KineticLawSelectionWizard
 * by:
 * @author Sebastian Nagel
 *
 * Adapted by:
 * @author Eike Pertuch
 *
 * @since 2.1.2
 *
 */
public class FunctionTermGeneratorWizard extends Wizard {

    /**
     * Localization support.
     */
    public static final transient ResourceBundle MESSAGES = ResourceManager.getBundle(Bundles.MESSAGES);

    /**
     * A {@link Logger} for this class.
     */
    private static final transient Logger logger = Logger.getLogger(FunctionTermGeneratorWizard.class.getName());

    private SBMLio<?> sbmlIO;

    private JDialog dialog;

    public FunctionTermGeneratorWizard(Frame owner, SBMLio<?> sbmlIO) {
        super(owner);

        this.sbmlIO = sbmlIO;
        dialog = getDialog();

        // set dialog properties
        dialog.setTitle(System.getProperty("app.name"));
        dialog.setMinimumSize(new Dimension(650, 250));
        dialog.setLocationRelativeTo(owner);

        setModal(true);

        initDescriptors();
    }

    /**
     * init all descriptor (panels)
     */
    private void initDescriptors() {
        // try to init FunctionTermGenerator with the selected model
        FunctionTermGenerator ftg = null;
        try {
            ftg = new FunctionTermGenerator();
        } catch (Exception e) {
            GUITools.showErrorMessage(getDialog(), e);
        }

        WizardPanelDescriptor descriptor1 = new FunctionTermGeneratorOptionPanelDescriptor(ftg);
        registerWizardPanel(FunctionTermGeneratorOptionPanelDescriptor.IDENTIFIER, descriptor1);

        WizardPanelDescriptor descriptor2 = new FunctionTermGeneratorProgressPanelDescriptor(ftg, sbmlIO.getSelectedModel());
        registerWizardPanel(FunctionTermGeneratorProgressPanelDescriptor.IDENTIFIER, descriptor2);

        WizardPanelDescriptor descriptor3 = new FunctionTermGeneratorResultsPanelDescriptor(ftg, sbmlIO);
        registerWizardPanel(FunctionTermGeneratorResultsPanelDescriptor.IDENTIFIER, descriptor3);
        // set option panel as first panel
        setCurrentPanel(FunctionTermGeneratorOptionPanelDescriptor.IDENTIFIER);
    }

    /**
     * Method that indicates whether or not changes have been introduced into
     * the given model.
     *
     * @return {@code true} if function terms were
     *         changed by SBMLsqueezer.
     */
    public boolean areFunctionTermsStoredInSBML() {
        boolean result = true;
        FunctionTermGeneratorResultsPanelDescriptor desc = (FunctionTermGeneratorResultsPanelDescriptor) getPanel(FunctionTermGeneratorResultsPanelDescriptor.IDENTIFIER);
        try {
            result = ((FunctionTermGeneratorResultsPanel) desc.getPanelComponent()).areFunctionTermsStoredInSBML();
            logger.fine("stored function terms: " + result);
        } catch (Exception exc) {
            logger.fine(getMessage(exc));
            exc.printStackTrace();
        }
        return result;
    }

}
