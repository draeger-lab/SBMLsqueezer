package org.sbml.squeezer.gui;

import de.zbit.sbml.gui.SBMLModelSplitPane;
import de.zbit.util.ResourceManager;
import org.sbml.squeezer.functionTermGenerator.wizard.FunctionTermGeneratorWizard;
import org.sbml.squeezer.gui.wizard.KineticLawSelectionWizard;
import org.sbml.squeezer.io.SBMLio;
import org.sbml.squeezer.util.Bundles;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.EventHandler;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ResourceBundle;

/**
 * This class implements a dialog which lets the user choose whether they want to create kinetic equations or
 * default function terms in the case that a model is both qualitative (contains transitions) as well as
 * quantitative (contains reactions) (Aug 30, 2020)
 *
 * @author Eike Pertuch
 *
 * @since 2.1.2
 *
 */
public class GeneratorSelectionDialog extends JDialog  {
    /**
     * Localization support.
     */
    private static final transient ResourceBundle MESSAGES = ResourceManager.getBundle(Bundles.MESSAGES);

    private ButtonGroup rbGroup;

    final private SBMLio<?> sbmlIO;
    final private Frame owner;
    private boolean changesStoredInSBML = false;

    /**
     *
     * @param owner
     * @param sbmlIO
     */
    public GeneratorSelectionDialog(Frame owner, SBMLio<?> sbmlIO) {
        super(owner);

        this.sbmlIO = sbmlIO;
        this.owner = owner;

        init();
    }

    /**
     * Creates the content for the selection dialog
     *
     */
    private void init() {
        setResizable(false);

        JPanel mainPanel = new JPanel();
        JPanel rbPanel = new JPanel();
        JLabel labQues = new JLabel("<html><b>" + MESSAGES.getString("GENERATOR_SELECT_1") + "<br>" +
                MESSAGES.getString("GENERATOR_SELECT_2") + "</b></html>");
        rbPanel.add(labQues);

        JRadioButton rbQuan = new JRadioButton(MESSAGES.getString("KINETIC_EQUATION_SELECT"));
        rbQuan.setSelected(true);
        rbPanel.add(rbQuan);

        JRadioButton rbQual = new JRadioButton(MESSAGES.getString("FUNCTION_TERM_SELECT"));
        rbPanel.add(rbQual);

        rbPanel.setLayout(new BoxLayout(rbPanel, BoxLayout.Y_AXIS));
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(rbPanel);

        rbGroup = new ButtonGroup();
        rbGroup.add(rbQuan);
        rbGroup.add(rbQual);

        JOptionPane pane = new JOptionPane(mainPanel,
                JOptionPane.QUESTION_MESSAGE,
                JOptionPane.OK_CANCEL_OPTION, UIManager
                .getIcon("ICON_LEMON_SMALL"));
        pane.selectInitialValue();
        pane.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent event) {
                if (isVisible()
                        && (event.getSource() == pane)
                        && (event.getPropertyName().equals(JOptionPane.VALUE_PROPERTY))
                        && (event.getNewValue() != null)
                        && (event.getNewValue() != JOptionPane.UNINITIALIZED_VALUE)) {
                    int i = 0;
                    setVisible(false);
                    if (((Integer) event.getNewValue()).intValue() == JOptionPane.OK_OPTION) {
                        if (rbQuan.isSelected()) {
                            KineticLawSelectionWizard wizard = new KineticLawSelectionWizard(owner, sbmlIO);
                            wizard.getDialog().addWindowListener(EventHandler.create(WindowListener.class, owner,
                                    "windowClosed", ""));
                            wizard.showModalDialog();
                            changesStoredInSBML = wizard.isKineticsAndParametersStoredInSBML();
                        } else {
                            FunctionTermGeneratorWizard wizard = new FunctionTermGeneratorWizard(owner, sbmlIO);
                            wizard.getDialog().addWindowListener(EventHandler.create(WindowListener.class, owner,
                                    "windowClosed", ""));
                            wizard.showModalDialog();
                            changesStoredInSBML = wizard.areFunctionTermsStoredInSBML();
                        }
                    }
                }
            }
        });

        Container content = getContentPane();
        content.setLayout(new BorderLayout());
        content.add(pane);
        setTitle(MESSAGES.getString("GENERATOR_SELECT_TITLE"));
        setLocationRelativeTo(owner);
        setModal(true);
        pack();
    }

    public boolean areChangesStoredInSBML() {
        return changesStoredInSBML;
    }
}
