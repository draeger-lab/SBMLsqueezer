package org.sbml.squeezer.functionTermGenerator;

import de.zbit.gui.GUITools;
import de.zbit.util.progressbar.AbstractProgressBar;
import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.ext.qual.QualConstants;
import org.sbml.jsbml.ext.qual.QualModelPlugin;
import org.sbml.jsbml.ext.qual.Transition;
import org.sbml.squeezer.io.SBMLio;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * This class allows SBMLsqueezer to create a default function term interactively for just
 * one transition. (Aug 30, 2020)
 *
 * Based on
 * @see org.sbml.squeezer.gui.KineticLawWindowAdapter
 * by:
 * @author Andreas Dr&auml;ger
 *
 * Adapted by:
 * @author Eike Pertuch
 **/
public class FunctionTermWindowAdapter extends WindowAdapter implements
        ComponentListener, PropertyChangeListener {

    private boolean gotFocus;
    private JOptionPane pane;
    private JDialog dialog;
    private SBMLio<?> sbmlIO;
    private FunctionTermGenerator ftg;
    private FunctionTermDisplayPanel messagePanel;
    private Transition transition;
    private int value;
    private String transitionID;
    private boolean functionTermsStoredInSBML = false;

    /**
     * Constructor for the Window Adapter
     *
     * @param dialog
     * @param sbmlIO
     * @param transitionID
     */
    public FunctionTermWindowAdapter(JDialog dialog, SBMLio<?> sbmlIO, String transitionID) {
        super();
        value = JOptionPane.CLOSED_OPTION;
        this.dialog = dialog;
        this.sbmlIO = sbmlIO;
        this.transitionID = transitionID;
        gotFocus = false;

        QualModelPlugin qm = (QualModelPlugin)(sbmlIO.getSelectedModel().getPlugin(QualConstants.shortLabel));
        transition = qm.getTransition(transitionID);
        try {
            ftg = new FunctionTermGenerator();
            messagePanel = new FunctionTermDisplayPanel(ftg, sbmlIO, transition);
        } catch (Throwable exc) {
            exc.printStackTrace();
            GUITools.showErrorMessage(dialog, exc);
        }

        JScrollPane scroll = new JScrollPane(messagePanel);
        scroll.setBorder(null);
        GUITools.setOpaqueForAllElements(scroll, true);

        pane = new JOptionPane(scroll,
                JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION,
                UIManager.getIcon("ICON_LEMON_SMALL"), null, null);
        pane.setInitialValue(null);
        Window owner = dialog.getOwner();
        pane.setComponentOrientation(((owner == null) ? JOptionPane
                .getRootFrame() : owner).getComponentOrientation());
        Container contentPane = this.dialog.getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(pane, BorderLayout.CENTER);

        this.dialog.setComponentOrientation(pane.getComponentOrientation());
        this.dialog.addWindowListener(this);
        this.dialog.addWindowListener(this);
        this.dialog.addWindowFocusListener(this);
        this.dialog.addComponentListener(this);
        pane.addPropertyChangeListener(this);
    }

    /* (non-Javadoc)
     * @seejava.awt.event.ComponentListener#componentHidden(java.awt.event.ComponentEvent)
     */
    @Override
    public void componentHidden(ComponentEvent e) {
    }

    /* (non-Javadoc)
     * @see java.awt.event.ComponentListener#componentMoved(java.awt.event.ComponentEvent)
     */
    @Override
    public void componentMoved(ComponentEvent e) {
    }

    /* (non-Javadoc)
     * @seejava.awt.event.ComponentListener#componentResized(java.awt.event.ComponentEvent)
     */
    @Override
    public void componentResized(ComponentEvent e) {
    }

    /* (non-Javadoc)
     * @see java.awt.event.ComponentListener#componentShown(java.awt.event.ComponentEvent)
     */
    @Override
    public void componentShown(ComponentEvent e) {
        // reset value to ensure closing works properly
        pane.setValue(JOptionPane.UNINITIALIZED_VALUE);
    }

    /**
     *
     * @param progressBar
     */
    public void showProgress(AbstractProgressBar progressBar) {
        ftg.setProgressBar(progressBar);
    }

    /* (non-Javadoc)
     * @see java.awt.event.WindowAdapter#windowClosed(java.awt.event.WindowEvent)
     */
    @Override
    public void windowClosed(WindowEvent we) {
        // setParentEnabled(we, true);
        super.windowClosed(we);
    }

    /* (non-Javadoc)
     * @see java.awt.event.WindowAdapter#windowClosing(java.awt.event.WindowEvent)
     */
    @Override
    public void windowClosing(WindowEvent we) {
        pane.setValue(null);
        // setParentEnabled(we, true);
    }

    /* (non-Javadoc)
     * @see java.awt.event.WindowAdapter#windowGainedFocus(java.awt.event.WindowEvent)
     */
    @Override
    public void windowGainedFocus(WindowEvent we) {
        // Once window gets focus, set initial focus
        if (!gotFocus) {
            pane.selectInitialValue();
            gotFocus = true;
        }
    }

    /* (non-Javadoc)
     * @seejava.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    @Override
    public void propertyChange(PropertyChangeEvent event) {
        // Let the defaultCloseOperation handle the closing
        // if the user closed the window without selecting a button
        // (newValue = null in that case). Otherwise, close the
        // dialog.
        if (dialog.isVisible() && (event.getSource() == pane)
                && event.getPropertyName().equals(JOptionPane.VALUE_PROPERTY)
                && (event.getNewValue() != null)
                && (event.getNewValue() != JOptionPane.UNINITIALIZED_VALUE)) {
            Object selectedValue = pane.getValue();
            value = JOptionPane.CLOSED_OPTION;
            if ((pane.getOptions() == null) && (selectedValue instanceof Integer)) {
                value = ((Integer) selectedValue).intValue();
            }
            dialog.setVisible(false);
        }
    }

    /**
     *  Stores function terms in SBML file if OK option was selected and does otherwise nothing
     *
     * @return true, if function terms were stored otherwise false
     * @throws Throwable
     */
    public boolean areFunctionTermsStoredInSBML() throws Throwable {
        if ((value == JOptionPane.OK_OPTION)) {

            ASTNode ftn = messagePanel.getCurGeneratedFunctionTerm();
            ftg.storeFunctionTermNode(sbmlIO.getSelectedModel(), transitionID, ftn);

            functionTermsStoredInSBML = true;
        } else {
            functionTermsStoredInSBML = false;
        }
        return functionTermsStoredInSBML;
    }

}
