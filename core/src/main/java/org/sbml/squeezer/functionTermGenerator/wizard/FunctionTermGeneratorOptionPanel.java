package org.sbml.squeezer.functionTermGenerator.wizard;

import de.zbit.util.ResourceManager;
import de.zbit.util.prefs.SBPreferences;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.ext.qual.Sign;
import org.sbml.squeezer.functionTermGenerator.*;
import org.sbml.squeezer.util.Bundles;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

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
        DefaultTerm defaultTerm = DefaultTerm.getDefaultTermFromSimpleName(prefs.get(FunctionTermOptions.DEFAULT_TERM));
        Sign defaultSign = Sign.valueOf(prefs.get(FunctionTermOptions.DEFAULT_SIGN));
        JPanel mainPanel = new JPanel();

        ftg.setDefaultTerm(defaultTerm);
        ftg.setSign(defaultSign);

        //Add Panel (ComboBox with Label) for default Term
        JLabel dtLabel = new JLabel(OPTIONS.getString("DEFAULT_TERM"));
        List<String> dtNamesList = Arrays.stream(DefaultTerm.values()).map(DefaultTerm::name).collect(Collectors.toList());
        dtNamesList.remove(dtNamesList.size()-1);
        String[] dtNames = dtNamesList.toArray(new String[0]);
        JComboBox<String> dtComboBox = new JComboBox<>(dtNames);
        dtComboBox.setName("dtComboBox");
        dtComboBox.setSelectedItem(defaultTerm.name());
        dtComboBox.setBackground(new Color(mainPanel.getBackground().getRGB()));
        dtComboBox.addActionListener(this);
        JPanel dtPanel = new JPanel();
        dtPanel.add(dtLabel);
        dtPanel.add(dtComboBox);
        dtPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        // Add Panel (ComboBox with Label) for default Sign
        JLabel dsLabel = new JLabel(OPTIONS.getString("DEFAULT_SIGN"));
        JComboBox<Sign> dsComboBox = new JComboBox<Sign>(Sign.values());
        dsComboBox.setName("dsComboBox");
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
            if(((JComboBox<?>) event.getSource()).getName().equals("dsComboBox")) {
                ftg.setSign((Sign) changedObj);
            }
            else if(((JComboBox<?>) event.getSource()).getName().equals("dtComboBox")) {
                ftg.setDefaultTerm(DefaultTerm.getDefaultTermFromSimpleName((String)changedObj));
            }
        }
    }
}
