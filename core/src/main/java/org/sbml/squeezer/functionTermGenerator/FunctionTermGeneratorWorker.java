package org.sbml.squeezer.functionTermGenerator;

import de.zbit.util.ResourceManager;
import org.sbml.jsbml.Model;
import org.sbml.squeezer.util.Bundles;

import javax.swing.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Generates function terms using SwingWorker
 *
 * Based on
 * @see org.sbml.squeezer.gui.KineticLawGeneratorWorker
 * by:
 * @author Sebastian Nagel
 *
 * Adapted by:
 * @author Eike Pertuch
 * @since 2.1.2
 */
public class FunctionTermGeneratorWorker extends SwingWorker<Void, Void> {

    /**
     * A {@link Logger} for this class.
     */
    private static final transient Logger logger = Logger.getLogger(FunctionTermGeneratorWorker.class.getName());

    /**
     *
     */
    private FunctionTermGenerator ftg;
    private Model model;


    /**
     *
     * @param ftg
     * @param model
     */
    public FunctionTermGeneratorWorker(FunctionTermGenerator ftg, Model model) {
        super();
        this.ftg = ftg;
        this.model = model;
    }

    /* (non-Javadoc)
     * @see javax.swing.SwingWorker#doInBackground()
     */
    @Override
    protected Void doInBackground() {
        try {
            ftg.setDefaultSign(model);
            try {
                ftg.generateFunctionTerms(model);
            } catch (Exception exc) {
                exc.printStackTrace();
            }
        } catch (Throwable e) {
            logger.log(Level.WARNING, e.getLocalizedMessage());
            e.printStackTrace();
        }
        return null;
    }

    /* (non-Javadoc)
     * @see javax.swing.SwingWorker#done()
     */
    @Override
    protected void done() {
        logger.log(Level.INFO, ResourceManager.getBundle(Bundles.LABELS).getString("READY"));
        firePropertyChange("generateFunctionTermsDone", null, null);
    }
}
