package org.sbml.squeezer.functionTermGenerator.wizard;

import de.zbit.util.ResourceManager;
import de.zbit.util.prefs.SBPreferences;
import org.sbml.jsbml.ext.qual.Sign;
import org.sbml.squeezer.functionTermGenerator.*;
import org.sbml.squeezer.util.Bundles;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

/**
 * This class implements the main options panel. Here the default sign and default term
 * can be set. By default they are set to the values specified in the options (Aug 13th, 2020)
 *
 * Based on
 * @see org.sbml.squeezer.gui.wizard.KineticLawSelectionOptionPanel
 * by:
 * @author Sebastian Nagel
 *
 * Adapted by:
 * @author Eike Pertuch
 * @since 2.1.2
 *
 */
    public class FunctionTermGeneratorOptionPanel extends JPanel implements ActionListener {

    /**
     * Generated serial version identifier.
     */
    private static final long serialVersionUID = -4552303683388858130L;
    /**
     * Localization support.
     */
    public static final transient ResourceBundle MESSAGES = ResourceManager.getBundle(Bundles.MESSAGES);

    public static final transient ResourceBundle OPTIONS = ResourceManager.getBundle(Bundles.OPTIONS);

    private FunctionTermGenerator ftg;

    /**
     *
     */
    public FunctionTermGeneratorOptionPanel(FunctionTermGenerator ftg) {
        super(new BorderLayout());
        this.ftg = ftg;
        init();
    }

    /**
     * Initializes the options panel
     */
    public void init() {

        SBPreferences prefs = SBPreferences.getPreferencesFor(FunctionTermOptions.class);
        DefaultTerm defaultTerm = DefaultTerm.valueOf(prefs.get(FunctionTermOptions.DEFAULT_TERM));
        Sign defaultSign = Sign.valueOf(prefs.get(FunctionTermOptions.DEFAULT_SIGN));
        JPanel mainPanel = new JPanel();

        ftg.setDefaultTerm(defaultTerm);
        ftg.setSign(defaultSign);

        //Add Panel (ComboBox with Label) for default Term
        JLabel dtLabel = new JLabel(OPTIONS.getString("DEFAULT_TERM"));
        JComboBox<DefaultTerm> dtComboBox = new JComboBox<DefaultTerm>(DefaultTerm.values());
        dtComboBox.setSelectedItem(defaultTerm);
        dtComboBox.setBackground(new Color(mainPanel.getBackground().getRGB()));
        dtComboBox.addActionListener(this);
        JPanel dtPanel = new JPanel();
        dtPanel.add(dtLabel);
        dtPanel.add(dtComboBox);
        dtPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        // Add Panel (ComboBox with Label) for default Sign
        JLabel dsLabel = new JLabel(OPTIONS.getString("DEFAULT_SIGN"));
        JComboBox<Sign> dsComboBox = new JComboBox<Sign>(Sign.values());
        dsComboBox.setSelectedItem(defaultSign);
        dsComboBox.setBackground(new Color(mainPanel.getBackground().getRGB()));
        dsComboBox.addActionListener(this);
        JPanel dsPanel = new JPanel();
        dsPanel.add(dsLabel);
        dsPanel.add(dsComboBox);
        dsPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        // Add both Panels to main Panel
        mainPanel.add(dtPanel);
        mainPanel.add(dsPanel);
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createTitledBorder(MESSAGES.getString("CHOOSE_OPTIONS_FTG")));

        add(mainPanel, BorderLayout.NORTH);
    }


    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent event) {
        if(event.getSource() instanceof JComboBox) {
            JComboBox changedCB = (JComboBox) event.getSource();
            Object changedObj = changedCB.getSelectedItem();
            if(changedObj instanceof Sign) {
                ftg.setSign((Sign) changedObj);
            }
            else if(changedObj instanceof DefaultTerm) {
                ftg.setDefaultTerm((DefaultTerm) changedObj);
            }
        }
    }
}
