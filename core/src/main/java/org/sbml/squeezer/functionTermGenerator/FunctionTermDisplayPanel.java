package org.sbml.squeezer.functionTermGenerator;

import de.zbit.gui.GUITools;
import de.zbit.util.ResourceManager;
import de.zbit.util.prefs.SBPreferences;
import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.ext.qual.Sign;
import org.sbml.jsbml.ext.qual.Transition;
import org.sbml.jsbml.math.ASTFunction;
import org.sbml.jsbml.text.parser.FormulaParserLL3;
import org.sbml.jsbml.text.parser.IFormulaParser;
import org.sbml.jsbml.util.compilers.LaTeXCompiler;
import org.sbml.squeezer.gui.LaTeXRenderer;
import org.sbml.squeezer.io.SBMLio;
import org.sbml.squeezer.util.Bundles;
import org.sbml.tolatex.LaTeXOptions;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Panel for displaying function term for specific transition ID
 *
 * Based on
 * @see org.sbml.squeezer.gui.KineticLawSelectionPanel
 * by:
 * @author Sebastian Nagel
 *
 * @author Eike Pertuch
 *
 */
public class FunctionTermDisplayPanel extends JPanel implements ItemListener {

    /**
     * Localization support.
     */
    public static final transient ResourceBundle MESSAGES = ResourceManager.getBundle(Bundles.MESSAGES);
    /**
     * Localization support.
     */
    public static final transient ResourceBundle WARNINGS = ResourceManager.getBundle(Bundles.WARNINGS);

    private FunctionTermGenerator ftg;
    private SBMLio<?> sbmlIO;

    private JPanel signOpt, defTermOpt, optPanel;
    private DefaultTerm curDefTerm;
    private Sign curDefSign;
    private JRadioButton rbSignOpt[], rbDefTermOpt[];
    private Transition transition;
    private JPanel eqnPrev;
    private GridBagLayout gbc;
    private GridBagConstraints c;

    public ASTNode getCurGeneratedFunctionTerm() {
        return curGeneratedFunctionTerm;
    }

    private ASTNode curGeneratedFunctionTerm;

    /**
     * Generated Serial ID.
     */
    private static final long serialVersionUID = -3145019506487267364L;

    private static final int width = 310, height = 175;

    /**
     * Creates the part of the panel in which the transition ID and the its corresponding newly generated function term
     * is displayed in LaTeX code opened when clicking on the corresponding result table entry.
     *
     * @param transition Transition for which newly generated function term is displayed
     */
    public FunctionTermDisplayPanel(Transition transition) {
        super(new BorderLayout());

        String laTeXpreview;
        SBPreferences prefsLaTeX;
        JPanel eqnPrev;

        JLabel transitionID = new JLabel("<html><br><b>"+ MESSAGES.getString("TRANSITION_ID") + ":</b>  " + transition.getId() + "<br><br></html>");
        add(transitionID, BorderLayout.NORTH);
        prefsLaTeX = SBPreferences.getPreferencesFor(LaTeXOptions.class);
        laTeXpreview = transition.getListOfFunctionTerms().get(0).getMath().compile(
                new LaTeXCompiler(prefsLaTeX
                        .getBoolean(LaTeXOptions.PRINT_NAMES_IF_AVAILABLE))).toString();
        StringBuilder sb = new StringBuilder();
        sb.append(laTeXpreview.replace("mathrm", "mbox")
                .replace("text", "mbox").replace("mathtt", "mbox"));
        eqnPrev = new JPanel(new BorderLayout());
        eqnPrev.setBorder(BorderFactory.createTitledBorder(' ' + MESSAGES.getString("FUNCTION_TERM_DISPLAY") + ' '));
        eqnPrev.add(new LaTeXRenderer(width, height).renderEquation(sb.toString().replace("\\-", "")), BorderLayout.CENTER);
        add(eqnPrev, BorderLayout.CENTER);
    }

    /**
     * Creates a panel for a single transition when it was selected to be "squeezed" in the menu.
     * Here the different parameters can be selected.
     *
     * @param ftg Function Term Generator used for the generation of the term for the selected transition
     * @param transition transition to be "squeezed"
     */
    public FunctionTermDisplayPanel(FunctionTermGenerator ftg, SBMLio<?> sbmlIO, Transition transition) {

        this.ftg = ftg;
        this.sbmlIO = sbmlIO;
        this.transition = transition;

        initFTDP();
    }


    /**
     * Initializes the panel if it is created for the modification of a single transition
     *
     */
    private void initFTDP() {

        SBPreferences prefs = SBPreferences.getPreferencesFor(FunctionTermOptions.class);
        curDefTerm = DefaultTerm.valueOf(prefs.get(FunctionTermOptions.DEFAULT_TERM));
        curDefSign = Sign.valueOf(prefs.get(FunctionTermOptions.DEFAULT_SIGN));

        List<String> rbDefTermNames = Arrays.stream(DefaultTerm.values()).map(Enum::toString).collect(Collectors.toList());
        rbDefTermNames.remove(rbDefTermNames.size()-1);
        ButtonGroup defaultTermBut = new ButtonGroup();
        defTermOpt = new JPanel();
        defTermOpt.setLayout(new BoxLayout(defTermOpt, BoxLayout.Y_AXIS));
        defTermOpt.setBorder(BorderFactory.createTitledBorder(MESSAGES.getString("DEFAULT_TERM_FT_DISPLAY_PANEL")));
        rbDefTermOpt = new JRadioButton[rbDefTermNames.size()];
        for(int i = 0; i < rbDefTermOpt.length; i++) {
            rbDefTermOpt[i] = new JRadioButton(rbDefTermNames.get(i));
            rbDefTermOpt[i].addItemListener(this);
            defTermOpt.add(rbDefTermOpt[i]);
            defaultTermBut.add(rbDefTermOpt[i]);
            if(curDefTerm == DefaultTerm.values()[i]) {
                rbDefTermOpt[i].setSelected(true);
            }
        }

        List<String> signOptNames = Arrays.stream(Sign.values()).map(Enum::toString).collect(Collectors.toList());
        ButtonGroup signBut = new ButtonGroup();
        signOpt = new JPanel();
        signOpt.setLayout(new BoxLayout(signOpt, BoxLayout.Y_AXIS));
        signOpt.setBorder(BorderFactory.createTitledBorder(MESSAGES.getString("SIGN_FT_DISPLAY_PANEL")));
        rbSignOpt = new JRadioButton[signOptNames.size()];
        for(int i = 0; i < rbSignOpt.length; i++) {
            rbSignOpt[i] = new JRadioButton(signOptNames.get(i));
            rbSignOpt[i].setName("");
            rbSignOpt[i].addItemListener(this);
            signOpt.add(rbSignOpt[i]);
            signBut.add(rbSignOpt[i]);
            if(curDefSign == Sign.values()[i]) {
                rbSignOpt[i].setSelected(true);
            }
        }

        ftg.setSign(curDefSign);
        ftg.setDefaultSign(sbmlIO.getSelectedModel());

        createPreviewPanel();

        //set layout
        gbc = new GridBagLayout();
        optPanel = new JPanel();
        optPanel.setLayout(gbc);
        c = new GridBagConstraints();
        c.fill = GridBagConstraints.VERTICAL;
        c.insets = new Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 400;
        c.gridheight = 200;
        c.ipadx = 280;
        c.ipady = 10;
        gbc.setConstraints(signOpt, c);
        optPanel.add(signOpt);

        c.gridy = 200;
        c.ipadx = 160;
        gbc.setConstraints(defTermOpt, c);
        optPanel.add(defTermOpt);

        c.gridy = 400;
        c.ipadx = 10;
        gbc.setConstraints(eqnPrev, c);
        optPanel.add(eqnPrev);

        add(optPanel, BorderLayout.NORTH);
    }

    /**
     * Creates the preview panel which displays the default function term for the given transition and parameters
     *
     */
    private void createPreviewPanel(){

        String laTeXpreview;
        SBPreferences prefsLaTeX;

        eqnPrev = new JPanel();

        ASTFunction math;
        ASTNode math2 = new ASTNode();

        if(curDefTerm == DefaultTerm.allActivatorsAndNoInhibitor) {
            math = ftg.generateFunctionTermForOneTransition(transition, ASTNode.Type.LOGICAL_AND);
        }
        else {
            math = ftg.generateFunctionTermForOneTransition(transition, ASTNode.Type.LOGICAL_OR);
        }
        IFormulaParser parser = new FormulaParserLL3(new StringReader(""));
        String helper = math.toFormula().replace("xor", "Xor");
        try {
            math2 = ASTNode.parseFormula(helper, parser);
        }
        catch (Exception exc){
            exc.printStackTrace();
            GUITools.showErrorMessage(this.getParent(), exc);
        }

        prefsLaTeX = SBPreferences.getPreferencesFor(LaTeXOptions.class);
        laTeXpreview = math2.compile(
                new LaTeXCompiler(prefsLaTeX
                        .getBoolean(LaTeXOptions.PRINT_NAMES_IF_AVAILABLE))).toString();
        StringBuilder sb = new StringBuilder();
        sb.append(laTeXpreview.replace("mathrm", "mbox")
                .replace("text", "mbox").replace("mathtt", "mbox"));
        eqnPrev = new JPanel(new BorderLayout());
        eqnPrev.setBorder(BorderFactory.createTitledBorder(' ' + MESSAGES.getString("FUNCTION_TERM_DISPLAY") + ' '));
        JComponent jc = new LaTeXRenderer(width, height).renderEquation(sb.toString().replace("\\-", ""));
        eqnPrev.add(jc, BorderLayout.CENTER);

        curGeneratedFunctionTerm = math2;
    }


    /* (non-Javadoc)
     * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
     */
    @Override
    public void itemStateChanged(ItemEvent event) {

        if(event.getSource() instanceof JRadioButton) {
            JRadioButton changedBut = (JRadioButton) event.getSource();
            if(changedBut.getParent().equals(signOpt)) {
                for (int i = 0; i<rbSignOpt.length; i++) {
                    if(rbSignOpt[i].isSelected()) {
                        curDefSign = Sign.values()[i];
                        ftg.setSign(curDefSign);
                        ftg.setDefaultSign(sbmlIO.getSelectedModel());
                        break;
                    }
                }
            }
            else {
                for (int i = 0; i < rbDefTermOpt.length; i++) {
                    if (rbDefTermOpt[i].isSelected()) {
                        curDefTerm = DefaultTerm.values()[i];
                        break;
                    }
                }
            }

            if(optPanel != null && eqnPrev != null) {
                optPanel.remove(eqnPrev);

                createPreviewPanel();

                gbc.setConstraints(eqnPrev, c);
                optPanel.add(eqnPrev);
            }
        }
    }
}
