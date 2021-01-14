package org.sbml.squeezer.functionTermGenerator;

import de.zbit.gui.StatusBar;
import de.zbit.util.ResourceManager;
import de.zbit.util.prefs.SBPreferences;
import de.zbit.util.progressbar.AbstractProgressBar;
import org.sbml.squeezer.io.SBMLio;
import org.sbml.squeezer.util.Bundles;

import javax.swing.*;
import java.awt.*;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class implements the dialog which is shown when the function term for a single class
 * is created (Aug 30, 2020)
 *
 * Based on
 * @see org.sbml.squeezer.gui.KineticLawSelectionDialog
 * by:
 * @author Nadine Hassis
 * @author Andreas Dr&auml;ger
 * @author Hannes Borch
 * @author Sebastian Nagel
 *
 * Adapted by:
 * @author Eike Pertuch
 *
 * @since 2.1.2
 */
public class FunctionTermSelectionDialog extends JDialog {

    /**
     * Localization support.
     */
    public static final transient ResourceBundle MESSAGES = ResourceManager.getBundle(Bundles.MESSAGES);
    /**
     * Localization support.
     */
    public static final transient ResourceBundle LABELS = ResourceManager.getBundle(Bundles.LABELS);

    /**
     * Generated serial version id.
     */
    private static final long serialVersionUID = -5980678130366530716L;

    private boolean functionTermsStoredInSBML = false;

    /**
     *
     */
    private final Logger logger = Logger.getLogger(FunctionTermSelectionDialog.class.getName());

    SBPreferences prefs;

    private StatusBar statusBar;


    /**
     * Creates an empty dialog with the given settings and sbml io object.
     *
     * @param owner
     */
    private FunctionTermSelectionDialog(Frame owner) {
        super(owner, System.getProperty("app.name"), true);
        prefs = new SBPreferences(FunctionTermOptions.class);
    }

    /**
     * This constructor is necessary for the GUI to generate just one single
     * function term for the given transition.
     *
     * @param owner
     * @param sbmlIO
     * @param transitionID
     * @throws Throwable
     */
    public FunctionTermSelectionDialog(Frame owner, SBMLio<?> sbmlIO, String transitionID) throws Throwable {
        this(owner);

        // This thing is necessary for CellDesigner!
        FunctionTermWindowAdapter adapter = new FunctionTermWindowAdapter(this,
                sbmlIO, transitionID);

        pack();
        setMinimumSize(new Dimension(500, 450));
        setResizable(true);
        setLocationRelativeTo(owner);
        setVisible(true);

        if (statusBar != null) {
            AbstractProgressBar progressBar = statusBar.showProgress();
            adapter.showProgress(progressBar);
        }
        functionTermsStoredInSBML = adapter.areFunctionTermsStoredInSBML();
        dispose();
        if (statusBar != null) {
            statusBar.hideProgress();
        }
        logger.log(Level.INFO, LABELS.getString("READY"));
    }

    /**
     *
     * @return
     */
    public boolean areFunctionTermsStoredInSBML() {
        return functionTermsStoredInSBML;
    }
}
