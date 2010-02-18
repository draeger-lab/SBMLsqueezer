package ode;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JButton;
import javax.swing.JSpinner;
import javax.swing.JScrollPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.JTable;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Point;
import javax.swing.JInternalFrame;
import java.awt.BorderLayout;
import javax.swing.JSplitPane;
import java.awt.event.KeyEvent;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import java.awt.Color;
import java.awt.GridBagConstraints;
import javax.swing.border.TitledBorder;
import java.awt.Font;

/**
 * @author Nadine Hassis <Nadine.hassis@gmail.com>
 * @date 01.08.2007
 */
public class SBMLsqueezerUI{
	
	// UI ELEMENTS DEFINITION: MainFrame 
	private JFrame jFrameMainFrame = null;  //  @jve:decl-index=0:visual-constraint="0,5"
	private JPanel jPanelMainFrame = null;
	private JRadioButton jRadioButtonMainFrameDefaultSettings = null;
	private JRadioButton jRadioButtonMainFrameProcessTypeConv = null;
	private JRadioButton jRadioButtonMainFrameProcessTypeMMK = null;
	private JButton jButtonMainFrameCancel = null;
	private JButton jButtonMainFrameSettings = null;
	private JButton jButtonMainFrameConfirm = null;

	// UI USER SETTINGS: MainFrame
	private boolean processTypeConv = true;
	private boolean defaultSettings = true;
	
	// UI ELEMENTS DEFINITION: SettingsFrame 
	private JFrame jFrameSettingsFrame = null;  //  @jve:decl-index=0:visual-constraint="403,-47"
	private JPanel jContentPaneSettingsFrame = null;
	
	private JCheckBox jCheckBoxSettingsFrameForceAllReactionsAsEnzyeReaction = null;
	private JSpinner jSpinnerSettingsFrameSpinWarnings = null;
	private JCheckBox jCheckBoxSettingsFrameWarnings = null;

	private JRadioButton jRadioButtonSettingsFrameGenOnlyMissKin = null;
	private JRadioButton jRadioButtonSettingsFrameGenForAllReac = null;
	
	private JRadioButton jRadioButtonSettingsFrameForceReacRev = null;
	private JRadioButton jRadioButtonSettingsFrameForceRevAsCD = null;
	
	private JCheckBox jCheckBoxSettingsFramePossibleEnzymeRNA = null;
	private JCheckBox jCheckBoxSettingsFramePossibleEnzymeAsRNA = null;
	private JCheckBox jCheckBoxSettingsFramePossibleEnzymeGenericProtein = null;
	private JCheckBox jCheckBoxSettingsFramePossibleEnzymeTruncatedProtein = null;
	private JCheckBox jCheckBoxSettingsFramePossibleEnzymeSimpleMolecule = null;
	private JCheckBox jCheckBoxSettingsFramePossibleEnzymeComplex = null;
	private JCheckBox jCheckBoxSettingsFramePossibleEnzymeReceptor = null;
	private JCheckBox jCheckBoxSettingsFramePossibleEnzymeUnknown = null;
	
	private JRadioButton jRadioButtonSettingsFrameMAK = null;

	private JRadioButton jRadioButtonSettingsFrameUniUniMMK = null;
	private JRadioButton jRadioButtonSettingsFrameUniUniCONV = null;
	
	private JRadioButton jRadioButtonSettingsFrameBiUniRND = null;
	private JRadioButton jRadioButtonSettingsFrameBiUniCONV = null;
	private JRadioButton jRadioButtonSettingsFrameBiUniORD = null;
	
	private JRadioButton jRadioButtonSettingsFrameBiBiRND = null;
	private JRadioButton jRadioButtonSettingsFrameBiBiCONV = null;
	private JRadioButton jRadioButtonSettingsFrameBiBiORD = null;
	private JRadioButton jRadioButtonSettingsFrameBiBiPP = null;
	
	private JRadioButton jRadioButtonSettingsFrameOtherEnzymCONV = null;
	
	private JButton jButtonSettingsFrameApply = null;
	private JButton jButtonSettingsFrameRestoreDefault = null;
	private JButton jButtonSettingsFrameLoad = null;
	private JButton jButtonSettingsFrameSave = null;
    
	// UI USER SETTINGS: SETTINGS
	private boolean forceAllReactionsAsEnzymeReaction=false;
	private boolean maxSpeciesWarnings = true;
	private int 	maxSpecies = 4;

	private boolean generateKineticForAllReaction=false;

	/*		
	 * 		true : reversible
	 * 		false : use Celldesigner presettings
	 */
	private boolean reversibility = true;  //  @jve:decl-index=0:

	private boolean possibleEnzymeRNA=true;
	private boolean possibleEnzymeGenericProtein=true;
	private boolean possibleEnzymeTruncatedProtein=true;
	private boolean possibleEnzymeComplex=true;
	private boolean possibleEnzymeUnknown=false;
	private boolean possibleEnzymeReceptor=false;
	private boolean possibleEnzymeSimpleMolecule=false;
	private boolean possibleEnzymeAsRNA=false;
	private boolean possibleEnzymeAllNotChecked=false;
	
	private boolean noReactionMAK = true;
	/* 	1: generalized MAK (gMAk)
	 * 	2: convinience (Conv)
	 * 	3: michaelis menten (MMK)
	 *  4: random (RND)
	 * 	5: ping pong (PP)
	 *	6: ordered (ORD)
	 */
	private int uniUniType =3;
	private int	biUniType  =4;
	private int	biBiType   =4; 
	
	// UI ELEMENTS DEFINITION: ReactionFrame
	private boolean KineticsAndParametersStoredInSBML = false;
	private JFrame jFrameReactionsFrame = null;  //  @jve:decl-index=0:visual-constraint="915,-26"
	private JPanel jContentPaneReactionsFrame = null;
	private JScrollPane jScrollPaneReactionsFrame = null;
	private JTable jTableReactionsFrameReactionTable = null;
	private JLabel jLabelReactionsFrameWarnings = null;
	private JButton jButtonReactionsFrameCancel = null;
	private JButton jButtonReactionsFrameSave = null;
	private JButton jButtonReactionsFrameCloseApplication = null;
		
	// CELL DESIGNER VARIABELS
    private SBMLsqueezerPlugin plugin;
    private ExistKineticLaw ekl;
	private	int numOfWarnings = 0;
	
	// UI debug purposes -- start gui without plugin
	private boolean loadedAsPlugin = false;
	private JPanel jPanelSettingsFrame1 = null;
	private JPanel jPanelsettingsFrame2 = null;
	private JPanel jPanelSettingsFrame3 = null;
	private JPanel jPanelSettingdFrame4 = null;
	private JPanel jPanelsettingsFrame5 = null;
	private JPanel jPaneSettingsFrame6 = null;
	private JPanel jPanelSettingsFrame8 = null;
	private JPanel jPanelSettingsFrame9 = null;
	private JPanel jPanelsettingsFrame10 = null;
	private JPanel jPanelsettingsFrame11 = null;
	// DEFAULT Constructor
	public SBMLsqueezerUI(SBMLsqueezerPlugin plugin) {
		this.plugin = plugin;
		this.loadedAsPlugin=true;
	}

	// DEBUG Constructor
	public SBMLsqueezerUI() {
	}
	
	
	//String reversibility = "rev";
//------------------------------------------------------------------------------------
//
//                        MAIN FRAME
//
//------------------------------------------------------------------------------------
	
	/**
	 * This method initializes jFrameMainFrame	
	 * 	
	 * @return javax.swing.JFrame	
	 */
	public JFrame getJFrameMainFrame() {
		if (jFrameMainFrame == null) {
			jFrameMainFrame = new JFrame();
			jFrameMainFrame.setSize(new Dimension(350, 200));
			jFrameMainFrame.setContentPane(getJPanelMainFrame());
			jFrameMainFrame.setTitle("SBMLsqueezer");
			jFrameMainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		}
		return jFrameMainFrame;
	}
	
	/**
	 * This method initializes jPanelMainFrame	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanelMainFrame() {
		if (jPanelMainFrame == null) {
			jPanelMainFrame = new JPanel();
			jPanelMainFrame.setLayout(null);
			jPanelMainFrame.add(getJButtonMainFrameConfirm(), null);
			jPanelMainFrame.add(getJRadioButtonMainFrameProcessTypeConv(), null);
			jPanelMainFrame.add(getJRadioButtonMainFrameProcessTypeMMK(), null);
			jPanelMainFrame.add(getJButtonMainFrameCancel(), null);
			jPanelMainFrame.add(getJButtonMainFrameSettings(), null);
			jPanelMainFrame.add(getJRadioButtonMainFrameDefaultSettings(), null);
		}
		return jPanelMainFrame;
	}
	
	/**
	 * This method initializes jButtonMainFrameConfirm	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButtonMainFrameConfirm() {
		if (jButtonMainFrameConfirm == null) {
			jButtonMainFrameConfirm = new JButton();
			jButtonMainFrameConfirm.setBounds(new Rectangle(25, 120, 100, 25));
			jButtonMainFrameConfirm.setToolTipText("Pressing \"Generate\" will start the ode-generating process.");
			jButtonMainFrameConfirm.setText("Generate");
			jButtonMainFrameConfirm.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					System.out.println("actionPerformed(Startframe:Generate)"); 
					
					if (defaultSettings){
						getJFrameSettingsFrame().setVisible(false);
						System.out.println("DEFAULT SETTINGS USED !");
						uniUniType =3;//"MMK";
						jRadioButtonSettingsFrameUniUniCONV.setSelected(false);
						jRadioButtonSettingsFrameUniUniMMK.setSelected(true);
													
						biUniType =6;//"ORD";
						jRadioButtonSettingsFrameBiUniCONV.setSelected(false);
						jRadioButtonSettingsFrameBiUniRND.setSelected(false);
						jRadioButtonSettingsFrameBiUniORD.setSelected(true);
						
						biBiType =4;//"RND";
						jRadioButtonSettingsFrameBiBiORD.setSelected(false);
						jRadioButtonSettingsFrameBiBiCONV.setSelected(false);
						jRadioButtonSettingsFrameBiBiRND.setSelected(true);
						jRadioButtonSettingsFrameBiBiPP.setSelected(false);
					
						maxSpeciesWarnings = true;
						maxSpecies = 4;
						noReactionMAK = true;
						jCheckBoxSettingsFrameWarnings.setSelected(true);
						jRadioButtonSettingsFrameMAK.setSelected(true);	
						SpinnerNumberModel blub = new SpinnerNumberModel(maxSpecies,2,10,1);
						jSpinnerSettingsFrameSpinWarnings.setModel(blub);
						
						jRadioButtonSettingsFrameUniUniMMK.setEnabled(true);
						jRadioButtonSettingsFrameUniUniCONV.setEnabled(true);
						jRadioButtonSettingsFrameBiUniORD.setEnabled(true);
						jRadioButtonSettingsFrameBiUniCONV.setEnabled(true);
						jRadioButtonSettingsFrameBiUniRND.setEnabled(true);
						jRadioButtonSettingsFrameBiBiPP.setEnabled(true);
						jRadioButtonSettingsFrameBiBiORD.setEnabled(true);
						jRadioButtonSettingsFrameBiBiCONV.setEnabled(true);
						jRadioButtonSettingsFrameBiBiRND.setEnabled(true);
						
						jCheckBoxSettingsFramePossibleEnzymeRNA.setSelected(true);
						jCheckBoxSettingsFramePossibleEnzymeGenericProtein.setSelected(true);
						jCheckBoxSettingsFramePossibleEnzymeTruncatedProtein.setSelected(true);
						jCheckBoxSettingsFramePossibleEnzymeAsRNA.setSelected(false);
						jCheckBoxSettingsFramePossibleEnzymeUnknown.setSelected(false);
						jCheckBoxSettingsFramePossibleEnzymeComplex.setSelected(true);
						jCheckBoxSettingsFramePossibleEnzymeReceptor.setSelected(false);
						jCheckBoxSettingsFramePossibleEnzymeSimpleMolecule.setSelected(false);
						
						possibleEnzymeRNA=true;
						possibleEnzymeGenericProtein=true;
						possibleEnzymeTruncatedProtein=true;
						possibleEnzymeComplex=true;
						possibleEnzymeUnknown=false;
						possibleEnzymeReceptor=false;
						possibleEnzymeSimpleMolecule=false;
						possibleEnzymeAsRNA=false;
						possibleEnzymeAllNotChecked=false;
						
						jCheckBoxSettingsFrameForceAllReactionsAsEnzyeReaction.setSelected(false);
						forceAllReactionsAsEnzymeReaction=false;
						
						generateKineticForAllReaction=false;
						jRadioButtonSettingsFrameGenForAllReac.setSelected(false);
						jRadioButtonSettingsFrameGenOnlyMissKin.setSelected(true);
						
						reversibility = true;
						jRadioButtonSettingsFrameForceRevAsCD.setSelected(false);
						jRadioButtonSettingsFrameForceReacRev.setSelected(true);
						
						possibleEnzymeTestAllNotChecked();
					}
					
					if (loadedAsPlugin)
					ekl = new ExistKineticLaw(
							plugin,
							processTypeConv,
							forceAllReactionsAsEnzymeReaction,
							biBiType,
							biUniType,
							uniUniType,
							maxSpecies,
							maxSpeciesWarnings,
							possibleEnzymeAllNotChecked,
							possibleEnzymeAsRNA,
							possibleEnzymeSimpleMolecule,
							false,
							possibleEnzymeReceptor,
							false,
							possibleEnzymeUnknown,
							possibleEnzymeComplex,
							false,
							false,
							possibleEnzymeTruncatedProtein,
							possibleEnzymeGenericProtein,
							possibleEnzymeRNA,
							generateKineticForAllReaction,
							reversibility
					);
					getJFrameMainFrame().setVisible(false);
					getJFrameReactionsFrame().setVisible(true);
					jScrollPaneReactionsFrame.setViewportView(getJTableReactionsFrameReactionTable());
				}	
			});
		}
		return jButtonMainFrameConfirm;
	}
	
	/**
	 * This method initializes jButtonMainFrameCancel	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButtonMainFrameCancel() {
		if (jButtonMainFrameCancel == null) {
			jButtonMainFrameCancel = new JButton();
			jButtonMainFrameCancel.setBounds(new Rectangle(150, 120, 100, 25));
			jButtonMainFrameCancel.setText("Close");
			jButtonMainFrameCancel.setToolTipText("Pressing \"Close\" will close SBMLsqueezer.");
			jButtonMainFrameCancel.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					System.out.println("actionPerformed(Startframe:Cancel)"); 
					jFrameMainFrame.dispose();
					//System.out.println("System.exit(0)");
					//System.exit(0);
				}
			});
		}
		return jButtonMainFrameCancel;
	}

	/**
	 * This method initializes jButtonMainFrameSettings	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButtonMainFrameSettings() {
		if (jButtonMainFrameSettings == null) {
			jButtonMainFrameSettings = new JButton();
			jButtonMainFrameSettings.setBounds(new Rectangle(180, 40, 100, 20));
			jButtonMainFrameSettings.setToolTipText("Pressing \"Settings\" will open the settings-frame, wherein the advanced settings can be changed.");
			jButtonMainFrameSettings.setEnabled(false);
			jButtonMainFrameSettings.setText("Settings");
			jButtonMainFrameSettings.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					System.out.println("actionPerformed(Settings)");									
					getJFrameMainFrame().setVisible(false);
					getJFrameSettingsFrame().setVisible(true);
					
				}
			});
		}
		return jButtonMainFrameSettings;
	}
	
	/**
	 * This method initializes jRadioButtonMainFrameProcessTypeConv	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getJRadioButtonMainFrameProcessTypeConv() {
		if (jRadioButtonMainFrameProcessTypeConv == null) {
			jRadioButtonMainFrameProcessTypeConv = new JRadioButton();
			jRadioButtonMainFrameProcessTypeConv.setSize(new Dimension(303, 20));
			jRadioButtonMainFrameProcessTypeConv.setText("Convenience kinetics for all Enzyme Reactions");
			jRadioButtonMainFrameProcessTypeConv.setToolTipText("Check this box, if you want to use convenience kinetics for all reactions as default");
			jRadioButtonMainFrameProcessTypeConv.setSelected(true);
			jRadioButtonMainFrameProcessTypeConv.setLocation(new Point(25, 15));
			jRadioButtonMainFrameProcessTypeConv
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(java.awt.event.ActionEvent e) {
							System.out.println("actionPerformed(Startframe:allConvinience)");
							defaultSettings = false;
							processTypeConv = true;
							jButtonMainFrameSettings.setEnabled(false);
							jRadioButtonMainFrameDefaultSettings.setSelected(false);
							jRadioButtonMainFrameProcessTypeMMK.setSelected(false);
							jRadioButtonMainFrameProcessTypeConv.setSelected(true);					
						}
					});
		}
		return jRadioButtonMainFrameProcessTypeConv;
	}

	/**
	 * This method initializes jRadioButtonMainFrameProcessTypeMMK	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getJRadioButtonMainFrameProcessTypeMMK() {
		if (jRadioButtonMainFrameProcessTypeMMK == null) {
			jRadioButtonMainFrameProcessTypeMMK = new JRadioButton();
			jRadioButtonMainFrameProcessTypeMMK.setSize(new Dimension(150, 20));
			jRadioButtonMainFrameProcessTypeMMK.setText("Customized Settings");
			jRadioButtonMainFrameProcessTypeMMK.setToolTipText("Check this box, if you want to customize the advanced settings.");
			jRadioButtonMainFrameProcessTypeMMK.setLocation(new Point(25, 40));
			jRadioButtonMainFrameProcessTypeMMK
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(java.awt.event.ActionEvent e) {
							System.out.println("actionPerformed(Startframe:customSettings)");
							defaultSettings = false;
							processTypeConv = false;
							jButtonMainFrameSettings.setEnabled(true);
							jRadioButtonMainFrameDefaultSettings.setSelected(false);
							jRadioButtonMainFrameProcessTypeMMK.setSelected(true);
							jRadioButtonMainFrameProcessTypeConv.setSelected(false);				
						}
					});
		}
		return jRadioButtonMainFrameProcessTypeMMK;
	}
	
	/**
	 * This method initializes jRadioButtonMainFrameDefaultSettings	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getJRadioButtonMainFrameDefaultSettings() {
		if (jRadioButtonMainFrameDefaultSettings == null) {
			jRadioButtonMainFrameDefaultSettings = new JRadioButton();
			jRadioButtonMainFrameDefaultSettings.setSize(new Dimension(250, 20));
			jRadioButtonMainFrameDefaultSettings.setText("Default Settings");
			jRadioButtonMainFrameDefaultSettings.setToolTipText("Check this box, if you want to use the default settings.");
			jRadioButtonMainFrameDefaultSettings.setLocation(new Point(25, 65));
			jRadioButtonMainFrameDefaultSettings
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(java.awt.event.ActionEvent e) {
							System.out.println("actionPerformed(Startframe:DefaultSettings)");
							defaultSettings = true;
							processTypeConv = false;
							jButtonMainFrameSettings.setEnabled(false);
							jRadioButtonMainFrameDefaultSettings.setSelected(true);
							jRadioButtonMainFrameProcessTypeMMK.setSelected(false);
							jRadioButtonMainFrameProcessTypeConv.setSelected(false);	
						}
					});
		}
		return jRadioButtonMainFrameDefaultSettings;
	}

//	------------------------------------------------------------------------------------
//
//	                        Settings  FRAME
//
//	------------------------------------------------------------------------------------
	
	/**
	 * This method initializes jFrameSettingsFrame	
	 * 	
	 * @return javax.swing.JFrame	
	 */
	private JFrame getJFrameSettingsFrame() {
		if (jFrameSettingsFrame == null) {
			jFrameSettingsFrame = new JFrame();
			jFrameSettingsFrame.setSize(new Dimension(500, 595));
			jFrameSettingsFrame.setContentPane(getJContentPaneSettingsFrame());
			jFrameSettingsFrame.setTitle("SBMLsqeezer Settings");
		}
		return jFrameSettingsFrame;
	}
	
	/**
	 * This method initializes jContentPaneSettingsFrame	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJContentPaneSettingsFrame() {
		if (jContentPaneSettingsFrame == null) {
			
			
			
			
			
			jContentPaneSettingsFrame = new JPanel();
			jContentPaneSettingsFrame.setLayout(null);
			
			jContentPaneSettingsFrame.add(getJButtonSettingsFrameApply(), null);
			jContentPaneSettingsFrame.add(getJButtonSettingsFrameRestoreDefault(), null);
			jContentPaneSettingsFrame.add(getJButtonSettingsFrameLoad(), null);
			jContentPaneSettingsFrame.add(getJButtonSettingsFrameSave(), null);
			
			
			
			
			
			
//			jContentPaneSettingsFrame.add(getJCheckBoxSettingsFramePossibleEnzymePhenotype(), null);
//			jContentPaneSettingsFrame.add(getJCheckBoxSettingsFramePossibleEnzymeGene(), null);
//			jContentPaneSettingsFrame.add(getJCheckBoxSettingsFramePossibleEnzymeIon(), null);
			jContentPaneSettingsFrame.add(getJPanelSettingsFrame1(), null);
			jContentPaneSettingsFrame.add(getJPanelsettingsFrame2(), null);
			jContentPaneSettingsFrame.add(getJPanelSettingsFrame3(), null);
			jContentPaneSettingsFrame.add(getJPanelSettingdFrame4(), null);
			jContentPaneSettingsFrame.add(getJPanelsettingsFrame5(), null);
			
		}
		return jContentPaneSettingsFrame;
	}
	
	/**
	 * This method initializes jButtonSettingsFrameApply	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButtonSettingsFrameApply() {
		if (jButtonSettingsFrameApply == null) {
			jButtonSettingsFrameApply = new JButton();
			jButtonSettingsFrameApply.setBounds(new Rectangle(9, 515, 100, 25));
			jButtonSettingsFrameApply.setToolTipText("Press the button \"Apply\" if you will use the current settings. Otherwise use the \"Restore\" button to restore default values, then apply these default settings.");
			jButtonSettingsFrameApply.setText("Apply");
			jButtonSettingsFrameApply
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(java.awt.event.ActionEvent e) {
							System.out.println("actionPerformed(Settings:Apply)");
							getJFrameMainFrame().setVisible(true);
							getJFrameSettingsFrame().setVisible(false);	
						}
					});
		}
		return jButtonSettingsFrameApply;
	}
	
	/**
	 * This method initializes jButtonSettingsFrameRestoreDefault	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButtonSettingsFrameRestoreDefault() {
		if (jButtonSettingsFrameRestoreDefault == null) {
			jButtonSettingsFrameRestoreDefault = new JButton();
			jButtonSettingsFrameRestoreDefault.setBounds(new Rectangle(124, 515, 100, 25));
			jButtonSettingsFrameRestoreDefault.setToolTipText("Pressing the button \"Restore\" will restore default settings.");
			jButtonSettingsFrameRestoreDefault.setText("Restore");
			jButtonSettingsFrameRestoreDefault
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(java.awt.event.ActionEvent e) {
							System.out.println("actionPerformed(Settings:Restore Defaults)");
							
							uniUniType =3;//"MMK";
							jRadioButtonSettingsFrameUniUniCONV.setSelected(false);
							jRadioButtonSettingsFrameUniUniMMK.setSelected(true);
														
							biUniType =4;//"RND";
							jRadioButtonSettingsFrameBiUniCONV.setSelected(false);
							jRadioButtonSettingsFrameBiUniRND.setSelected(true);
							jRadioButtonSettingsFrameBiUniORD.setSelected(false);
							
							biBiType =4;//"RND";
							jRadioButtonSettingsFrameBiBiORD.setSelected(false);
							jRadioButtonSettingsFrameBiBiCONV.setSelected(false);
							jRadioButtonSettingsFrameBiBiRND.setSelected(true);
							jRadioButtonSettingsFrameBiBiPP.setSelected(false);
						
							maxSpeciesWarnings = true;
							jCheckBoxSettingsFrameWarnings.setSelected(true);
							
							maxSpecies = 4;
							SpinnerNumberModel blub = new SpinnerNumberModel(maxSpecies,2,10,1);
							jSpinnerSettingsFrameSpinWarnings.setModel(blub);
							
							noReactionMAK = true;
							jRadioButtonSettingsFrameMAK.setSelected(true);	
							
							possibleEnzymeRNA=true;
							possibleEnzymeGenericProtein=true;
							possibleEnzymeTruncatedProtein=true;
							possibleEnzymeComplex=true;
							possibleEnzymeUnknown=false;
							possibleEnzymeReceptor=false;
							possibleEnzymeSimpleMolecule=false;
							possibleEnzymeAsRNA=false;
							possibleEnzymeAllNotChecked=false;
							jRadioButtonSettingsFrameUniUniMMK.setEnabled(true);
							jRadioButtonSettingsFrameUniUniCONV.setEnabled(true);
							jRadioButtonSettingsFrameBiUniORD.setEnabled(true);
							jRadioButtonSettingsFrameBiUniCONV.setEnabled(true);
							jRadioButtonSettingsFrameBiUniRND.setEnabled(true);
							jRadioButtonSettingsFrameBiBiPP.setEnabled(true);
							jRadioButtonSettingsFrameBiBiORD.setEnabled(true);
							jRadioButtonSettingsFrameBiBiCONV.setEnabled(true);
							jRadioButtonSettingsFrameBiBiRND.setEnabled(true);
							jCheckBoxSettingsFramePossibleEnzymeRNA.setSelected(true);
							jCheckBoxSettingsFramePossibleEnzymeGenericProtein.setSelected(true);
							jCheckBoxSettingsFramePossibleEnzymeTruncatedProtein.setSelected(true);
							jCheckBoxSettingsFramePossibleEnzymeAsRNA.setSelected(false);
							jCheckBoxSettingsFramePossibleEnzymeUnknown.setSelected(false);
							jCheckBoxSettingsFramePossibleEnzymeComplex.setSelected(true);
							jCheckBoxSettingsFramePossibleEnzymeReceptor.setSelected(false);
							jCheckBoxSettingsFramePossibleEnzymeSimpleMolecule.setSelected(false);
							
							forceAllReactionsAsEnzymeReaction=false;
							jCheckBoxSettingsFrameForceAllReactionsAsEnzyeReaction.setSelected(false);

							
							generateKineticForAllReaction=false;
							jRadioButtonSettingsFrameGenForAllReac.setSelected(false);
							jRadioButtonSettingsFrameGenOnlyMissKin.setSelected(true);
							
							reversibility = true;
							jRadioButtonSettingsFrameForceRevAsCD.setSelected(false);
							jRadioButtonSettingsFrameForceReacRev.setSelected(true);

							
							possibleEnzymeTestAllNotChecked();
						}
					});
		}
		return jButtonSettingsFrameRestoreDefault;
	}
	
	/**
	 * This method initializes jButtonSettingsFrameLoad	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButtonSettingsFrameLoad() {
		if (jButtonSettingsFrameLoad == null) {
			jButtonSettingsFrameLoad = new JButton();
			jButtonSettingsFrameLoad.setBounds(new Rectangle(266, 515, 100, 25));
			jButtonSettingsFrameLoad.setToolTipText("Pressing the button \"Load\" will open a fileDialog wherein you can load a SBMLsqeezer-settings file, which was stored before.");
			jButtonSettingsFrameLoad.setText("Load");
			jButtonSettingsFrameLoad.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					System.out.println("actionPerformed(Settings:Load)");
				       
			        LoadSettings load = new LoadSettings();				
			        
			        String paramLine="";
			        boolean error = false;
			        while (!paramLine.equals("END"))
			        {
			        	paramLine=load.read();
			        	int start=0;
			        	
			        	if (paramLine.contains("maxSpecies")){
			        		start=paramLine.indexOf(":");
			        		String paramValue = paramLine.substring(start+1);
			        		try {
								maxSpecies = new Integer(paramValue);
							} catch (NumberFormatException e1) {
								error=true;
								System.err.println("Unknown token:("+paramValue+") at LoadToken:("+0+"). Expected is a regular Value for param:(maxSpecies).");
								e1.printStackTrace();
							}
							if (maxSpecies <2 || maxSpecies >10){
								error=true;
								System.err.println("Irregular Value:("+paramValue+") at LoadToken:("+0+"). Expected is a regular Value of range 2..10:(maxSpecies).");
							}
			        	}
			        	if (paramLine.contains("warnings")){
			        		start=paramLine.indexOf(":");
			        		String paramValue = paramLine.substring(start+1);
			        		if (paramValue.equals("true"))
			        			maxSpeciesWarnings = true;
			        		else if (paramValue.equals("false"))
			        			maxSpeciesWarnings = false;
			        		else{
			        			System.err.println("Unknown token:("+paramValue+") at LoadToken:("+1+"). Expected is a regular Value for param:(warnings).");
			        			error=true;
			        		}
			        	}
			        	if (paramLine.contains("noReactionMAK")){
			        		start=paramLine.indexOf(":");
			        		String paramValue = paramLine.substring(start+1);
			        		if (paramValue.equals("true"))
			        			noReactionMAK = true;
			        		else if (paramValue.equals("false"))
			        			noReactionMAK = false;
			        		else{
			        			System.err.println("Unknown token:("+paramValue+") at LoadToken:("+2+"). Expected is a regular Value for param:(noReactionMAK).");
			        			error=true;
			        		}
			        	}
			        	if (paramLine.contains("uniUniType")){
			        		start=paramLine.indexOf(":");
			        		int paramValue = new Integer (paramLine.substring(start+1));
			        		//System.out.println(paramValue);
			        		
			        		//if (paramValue.equals("MMK") ||paramValue.equals("CONV"))
			        		if (paramValue==3 || paramValue==2)
			        		uniUniType=paramValue;
			        		else{
			        			System.err.println("Unknown token:("+paramValue+") at LoadToken:("+3+"). Expected is a regular Value for param:(uniUniType).");
			        			error=true;
			        		}
			        	}
			        	if (paramLine.contains("biUniType")){
			        		start=paramLine.indexOf(":");
			        		int paramValue = new Integer (paramLine.substring(start+1));
			        		//System.out.println(paramValue);
			        		//if (paramValue.equals("RND") || paramValue.equals("ORD") ||paramValue.equals("CONV"))
			        		if (paramValue==4 || paramValue==6 || paramValue==2)
			        			biUniType=paramValue;
			        		else{
			        			error=true;
			        			System.err.println("Unknown token:("+paramValue+") at LoadToken:("+4+"). Expected is a regular Value for param:(biUniType).");
			        	}
			        	}
			        	if (paramLine.contains("biBiType")){
			        		start=paramLine.indexOf(":");
			        		int paramValue = new Integer (paramLine.substring(start+1));
			        		//System.out.println(paramValue);
			        		//if (paramValue.equals("RND") || paramValue.equals("ORD") || paramValue.equals("CONV")|| paramValue.equals("PP"))
			        		if (paramValue==4 || paramValue==6 || paramValue==2 || paramValue==5)	
			        			biBiType=paramValue;
			        		else{
			        			error=true;
			        			System.err.println("Unknown token:("+paramValue+") at LoadToken:("+5+"). Expected is a regular Value for param:(biBiType).");
			        		}
			        	}
			        	if (paramLine.contains("possibleEnzymeRNA")){
			        		start=paramLine.indexOf(":");
			        		String paramValue = paramLine.substring(start+1);
			        		if (paramValue.equals("true"))
			        			possibleEnzymeRNA = true;
			        		else if (paramValue.equals("false"))
			        			possibleEnzymeRNA = false;
			        		else{
			        			System.err.println("Unknown token:("+paramValue+") at LoadToken:("+6+"). Expected is a regular Value for param:(possibleEnzymeRNA).");
			        			error=true;
			        		}
			        	}
			        	if (paramLine.contains("possibleEnzymeGenericProtein")){
			        		start=paramLine.indexOf(":");
			        		String paramValue = paramLine.substring(start+1);
			        		if (paramValue.equals("true"))
			        			possibleEnzymeGenericProtein = true;
			        		else if (paramValue.equals("false"))
			        			possibleEnzymeGenericProtein = false;
			        		else{
			        			System.err.println("Unknown token:("+paramValue+") at LoadToken:("+7+"). Expected is a regular Value for param:(possibleEnzymeGenericProtein).");
			        			error=true;
			        		}
			        	}
			        	if (paramLine.contains("possibleEnzymeTruncatedProtein")){
			        		start=paramLine.indexOf(":");
			        		String paramValue = paramLine.substring(start+1);
			        		if (paramValue.equals("true"))
			        			possibleEnzymeTruncatedProtein = true;
			        		else if (paramValue.equals("false"))
			        			possibleEnzymeTruncatedProtein = false;
			        		else{
			        			System.err.println("Unknown token:("+paramValue+") at LoadToken:("+8+"). Expected is a regular Value for param:(possibleEnzymeTruncatedProtein).");
			        			error=true;
			        		}
			        	}
			        	
			        	if (paramLine.contains("GenKinForAllReac")){
			        		start=paramLine.indexOf(":");
			        		String paramValue = paramLine.substring(start+1);
			        		if (paramValue.equals("true"))
			        			generateKineticForAllReaction = true;
			        		else if (paramValue.equals("false"))
			        			generateKineticForAllReaction = false;
			        		else{
			        			System.err.println("Unknown token:("+paramValue+") at LoadToken:("+9+"). Expected is a regular Value for param:(GenKinForAllReac).");
			        			error=true;
			        		}
			        	}
				 
			        	if (paramLine.contains("reversibility:")){
			        		start=paramLine.indexOf(":");
			        		String paramValue = paramLine.substring(start+1);
			        		//System.out.println("value:"+paramValue);
			        		if (paramValue.equals("true"))
			        			reversibility = true;
			        		else if (paramValue.equals("false"))
			        			reversibility = false;
			        		else{
			        			System.err.println("Unknown token:("+paramValue+") at LoadToken:("+10+"). Expected is a regular Value for param:(reversibility).");
			        			error=true;
			        		}
			        	}
			        	
			        	if (paramLine.contains("possibleEnzymeComplex")){
			        		start=paramLine.indexOf(":");
			        		String paramValue = paramLine.substring(start+1);
			        		if (paramValue.equals("true"))
			        			possibleEnzymeComplex = true;
			        		else if (paramValue.equals("false"))
			        			possibleEnzymeComplex = false;
			        		else{
			        			System.err.println("Unknown token:("+paramValue+") at LoadToken:("+11+"). Expected is a regular Value for param:(possibleEnzymeComplex).");
			        			error=true;
			        		}
			        	}
			        	if (paramLine.contains("possibleEnzymeUnknown")){
			        		start=paramLine.indexOf(":");
			        		String paramValue = paramLine.substring(start+1);
			        		if (paramValue.equals("true"))
			        			possibleEnzymeUnknown = true;
			        		else if (paramValue.equals("false"))
			        			possibleEnzymeUnknown = false;
			        		else{
			        			System.err.println("Unknown token:("+paramValue+") at LoadToken:("+12+"). Expected is a regular Value for param:(possibleEnzymeUnknown).");
			        			error=true;
			        		}
			        	}
			        	if (paramLine.contains("possibleEnzymeReceptor")){
			        		start=paramLine.indexOf(":");
			        		String paramValue = paramLine.substring(start+1);
			        		if (paramValue.equals("true"))
			        			possibleEnzymeReceptor = true;
			        		else if (paramValue.equals("false"))
			        			possibleEnzymeReceptor = false;
			        		else{
			        			System.err.println("Unknown token:("+paramValue+") at LoadToken:("+13+"). Expected is a regular Value for param:(possibleEnzymeReceptor).");
			        			error=true;
			        		}
			        	}

			        	if (paramLine.contains("possibleEnzymeSimpleMolecule")){
			        		start=paramLine.indexOf(":");
			        		String paramValue = paramLine.substring(start+1);
			        		if (paramValue.equals("true"))
			        			possibleEnzymeSimpleMolecule = true;
			        		else if (paramValue.equals("false"))
			        			possibleEnzymeSimpleMolecule = false;
			        		else{
			        			System.err.println("Unknown token:("+paramValue+") at LoadToken:("+14+"). Expected is a regular Value for param:().");
			        			error=true;
			        		}
			        	}
			        	if (paramLine.contains("possibleEnzymeAsRNA")){
			        		start=paramLine.indexOf(":");
			        		String paramValue = paramLine.substring(start+1);
			        		if (paramValue.equals("true"))
			        			possibleEnzymeAsRNA = true;
			        		else if (paramValue.equals("false"))
			        			possibleEnzymeAsRNA = false;
			        		else{
			        			System.err.println("Unknown token:("+paramValue+") at LoadToken:("+15+"). Expected is a regular Value for param:(possibleEnzymeAsRNA).");
			        			error=true;
			        		}
			        	}
			        	if (paramLine.contains("forceAllReactionsAsEnzymeReaction")){
			        		start=paramLine.indexOf(":");
			        		String paramValue = paramLine.substring(start+1);
			        		if (paramValue.equals("true"))
			        			possibleEnzymeAllNotChecked = true;
			        		else if (paramValue.equals("false"))
			        			possibleEnzymeAllNotChecked = false;
			        		else{
			        			System.err.println("Unknown token:("+paramValue+") at LoadToken:("+16+"). Expected is a regular Value for param:(forceAllReactionsAsEnzymeReaction).");
			        			error=true;
			        		}
			        	}
			        	if (paramLine.contains("forceAllReactionsAsEnzymeReaction")){
			        		start=paramLine.indexOf(":");
			        		String paramValue = paramLine.substring(start+1);
			        		if (paramValue.equals("true"))
			        			forceAllReactionsAsEnzymeReaction = true;
			        		else if (paramValue.equals("false"))
			        			forceAllReactionsAsEnzymeReaction = false;
			        		else{
			        			System.err.println("Unknown token:("+paramValue+") at LoadToken:("+17+"). Expected is a regular Value for param:(forceAllReactionsAsEnzymeReaction).");
			        			error=true;
			        		}
			        	}
			        }
			        load.close();
			        if (error==true){
			        	System.err.println("actionPerformed(Settings:Restore Defaults after LOAD ERROR)");
			        	
			        	
			        	//TODO
			        	//getJFrameErrorFrame().setVisible(true);
			        	//getJFrameErrorFrame().setTitle("ERROR (while loading settings)");

			        	//getJFrameErrorFrame().setTitle("ERROR (while loading settings)");
			        	//jLabelErrorFrameText.setText("<html>An error has occoured while loading a settings file.<p/Possible reasons:<p/ >wrong modification of stored file by hand<p/ >using data of unactual format (actual:1.00)   <p/<p/As a result of this error, default settings have been loaded.</html>");
			        	//jLabelErrorFrameText.setForeground(Color.red);

			        	uniUniType =3;//"MMK";
						jRadioButtonSettingsFrameUniUniCONV.setSelected(false);
						jRadioButtonSettingsFrameUniUniMMK.setSelected(true);
													
						biUniType =4;//"RND";
						jRadioButtonSettingsFrameBiUniCONV.setSelected(false);
						jRadioButtonSettingsFrameBiUniRND.setSelected(true);
						jRadioButtonSettingsFrameBiUniORD.setSelected(false);
						
						biBiType =4;//"RND";
						jRadioButtonSettingsFrameBiBiORD.setSelected(false);
						jRadioButtonSettingsFrameBiBiCONV.setSelected(false);
						jRadioButtonSettingsFrameBiBiRND.setSelected(true);
						jRadioButtonSettingsFrameBiBiPP.setSelected(false);
					
						maxSpeciesWarnings = true;
						maxSpecies = 4;
						noReactionMAK = true;
						jCheckBoxSettingsFrameWarnings.setSelected(true);
						jRadioButtonSettingsFrameMAK.setSelected(true);	
						SpinnerNumberModel blub = new SpinnerNumberModel(maxSpecies,2,10,1);
						jSpinnerSettingsFrameSpinWarnings.setModel(blub);
						
						jRadioButtonSettingsFrameUniUniMMK.setEnabled(true);
						jRadioButtonSettingsFrameUniUniCONV.setEnabled(true);
						jRadioButtonSettingsFrameBiUniORD.setEnabled(true);
						jRadioButtonSettingsFrameBiUniCONV.setEnabled(true);
						jRadioButtonSettingsFrameBiUniRND.setEnabled(true);
						jRadioButtonSettingsFrameBiBiPP.setEnabled(true);
						jRadioButtonSettingsFrameBiBiORD.setEnabled(true);
						jRadioButtonSettingsFrameBiBiCONV.setEnabled(true);
						jRadioButtonSettingsFrameBiBiRND.setEnabled(true);
						
						jCheckBoxSettingsFramePossibleEnzymeRNA.setSelected(true);
						jCheckBoxSettingsFramePossibleEnzymeGenericProtein.setSelected(true);
						jCheckBoxSettingsFramePossibleEnzymeTruncatedProtein.setSelected(true);
						jCheckBoxSettingsFramePossibleEnzymeAsRNA.setSelected(false);
						jCheckBoxSettingsFramePossibleEnzymeUnknown.setSelected(false);
						jCheckBoxSettingsFramePossibleEnzymeComplex.setSelected(false);
						jCheckBoxSettingsFramePossibleEnzymeReceptor.setSelected(false);
						jCheckBoxSettingsFramePossibleEnzymeSimpleMolecule.setSelected(false);
						
						possibleEnzymeRNA=true;
						possibleEnzymeGenericProtein=true;
						possibleEnzymeTruncatedProtein=true;
						possibleEnzymeComplex=true;
						possibleEnzymeUnknown=false;
						possibleEnzymeReceptor=false;
						possibleEnzymeSimpleMolecule=false;
						possibleEnzymeAsRNA=false;
						possibleEnzymeAllNotChecked=false;
						
						jCheckBoxSettingsFrameForceAllReactionsAsEnzyeReaction.setSelected(false);
						forceAllReactionsAsEnzymeReaction=false;
						
						generateKineticForAllReaction=false;
						jRadioButtonSettingsFrameGenForAllReac.setSelected(false);
						jRadioButtonSettingsFrameGenOnlyMissKin.setSelected(true);
						
						reversibility = true;
						jRadioButtonSettingsFrameForceRevAsCD.setSelected(false);
						jRadioButtonSettingsFrameForceReacRev.setSelected(true);
						
						possibleEnzymeTestAllNotChecked();
			        }
			        else{	        				        	
							jRadioButtonSettingsFrameUniUniMMK.setEnabled(true);
							jRadioButtonSettingsFrameUniUniCONV.setEnabled(true);
							jRadioButtonSettingsFrameBiUniORD.setEnabled(true);
							jRadioButtonSettingsFrameBiUniCONV.setEnabled(true);
							jRadioButtonSettingsFrameBiUniRND.setEnabled(true);
							jRadioButtonSettingsFrameBiBiPP.setEnabled(true);
							jRadioButtonSettingsFrameBiBiORD.setEnabled(true);
							jRadioButtonSettingsFrameBiBiCONV.setEnabled(true);
							jRadioButtonSettingsFrameBiBiRND.setEnabled(true);
				        	
							if (uniUniType==3){//.equals("MMK")){
							jRadioButtonSettingsFrameUniUniCONV.setSelected(false);
							jRadioButtonSettingsFrameUniUniMMK.setSelected(true);
							}
							if (uniUniType==2){//.equals("CONV")){
								jRadioButtonSettingsFrameUniUniCONV.setSelected(true);
								jRadioButtonSettingsFrameUniUniMMK.setSelected(false);
							}
							if (biUniType==4){//.equals("RND")){
								jRadioButtonSettingsFrameBiUniCONV.setSelected(false);
								jRadioButtonSettingsFrameBiUniRND.setSelected(true);
								jRadioButtonSettingsFrameBiUniORD.setSelected(false);
							}
							if (biUniType==2){//.equals("CONV")){
								jRadioButtonSettingsFrameBiUniCONV.setSelected(true);
								jRadioButtonSettingsFrameBiUniRND.setSelected(false);
								jRadioButtonSettingsFrameBiUniORD.setSelected(false);
							}
							if (biUniType==6){//.equals("ORD")){
								jRadioButtonSettingsFrameBiUniCONV.setSelected(false);
								jRadioButtonSettingsFrameBiUniRND.setSelected(false);
								jRadioButtonSettingsFrameBiUniORD.setSelected(true);
							}
							if (biBiType==4){//.equals("RND")){
								jRadioButtonSettingsFrameBiBiCONV.setSelected(false);
								jRadioButtonSettingsFrameBiBiRND.setSelected(true);
								jRadioButtonSettingsFrameBiBiORD.setSelected(false);
								jRadioButtonSettingsFrameBiBiPP.setSelected(false);
							}
							if (biBiType==2){//.equals("CONV")){
								jRadioButtonSettingsFrameBiBiCONV.setSelected(true);
								jRadioButtonSettingsFrameBiBiRND.setSelected(false);
								jRadioButtonSettingsFrameBiBiORD.setSelected(false);
								jRadioButtonSettingsFrameBiBiPP.setSelected(false);
							}
							if (biBiType==6){//.equals("ORD")){
								jRadioButtonSettingsFrameBiBiCONV.setSelected(false);
								jRadioButtonSettingsFrameBiBiRND.setSelected(false);
								jRadioButtonSettingsFrameBiBiORD.setSelected(true);
								jRadioButtonSettingsFrameBiBiPP.setSelected(false);
							}
							if (biBiType==5){//.equals("PP")){
								jRadioButtonSettingsFrameBiBiCONV.setSelected(false);
								jRadioButtonSettingsFrameBiBiRND.setSelected(false);
								jRadioButtonSettingsFrameBiBiORD.setSelected(false);
								jRadioButtonSettingsFrameBiBiPP.setSelected(true);
							}
	
							jCheckBoxSettingsFrameWarnings.setSelected(maxSpeciesWarnings);
							jRadioButtonSettingsFrameMAK.setSelected(noReactionMAK);
							SpinnerNumberModel blub = new SpinnerNumberModel(maxSpecies,2,10,1);
							jSpinnerSettingsFrameSpinWarnings.setModel(blub);
							
							
							jCheckBoxSettingsFramePossibleEnzymeRNA.setSelected(possibleEnzymeRNA);
							jCheckBoxSettingsFramePossibleEnzymeGenericProtein.setSelected(possibleEnzymeGenericProtein);
							jCheckBoxSettingsFramePossibleEnzymeTruncatedProtein.setSelected(possibleEnzymeTruncatedProtein);
							jCheckBoxSettingsFramePossibleEnzymeAsRNA.setSelected(possibleEnzymeAsRNA);
							jCheckBoxSettingsFramePossibleEnzymeUnknown.setSelected(possibleEnzymeUnknown);
							jCheckBoxSettingsFramePossibleEnzymeComplex.setSelected(possibleEnzymeComplex);
							jCheckBoxSettingsFramePossibleEnzymeReceptor.setSelected(possibleEnzymeReceptor);
							jCheckBoxSettingsFramePossibleEnzymeSimpleMolecule.setSelected(possibleEnzymeSimpleMolecule);
							jCheckBoxSettingsFrameForceAllReactionsAsEnzyeReaction.setSelected(forceAllReactionsAsEnzymeReaction);  
							
							jRadioButtonSettingsFrameGenForAllReac.setSelected(generateKineticForAllReaction);
							jRadioButtonSettingsFrameGenOnlyMissKin.setSelected(!generateKineticForAllReaction);
							
							if (reversibility==true){
									jRadioButtonSettingsFrameForceRevAsCD.setSelected(false);
									jRadioButtonSettingsFrameForceReacRev.setSelected(true);
							}
							if (reversibility==false){
									jRadioButtonSettingsFrameForceRevAsCD.setSelected(true);
									jRadioButtonSettingsFrameForceReacRev.setSelected(false);
							}
							
							possibleEnzymeTestAllNotChecked();
			        }
				}
			});
		}
		return jButtonSettingsFrameLoad;
	}
	
	/**
	 * This method initializes jButtonSettingsFrameSave	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButtonSettingsFrameSave() {
		if (jButtonSettingsFrameSave == null) {
			jButtonSettingsFrameSave = new JButton();
			jButtonSettingsFrameSave.setBounds(new Rectangle(381, 515, 100, 25));
			jButtonSettingsFrameSave.setToolTipText("Pressing the button \"Save\" will open a fileDialog wherein you can save the current settings into a SBMLsqeezer-settings file.");
			jButtonSettingsFrameSave.setText("Save");
			jButtonSettingsFrameSave.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					SaveSettings save = new SaveSettings();
					save.write ("SBMLsqeezer Settings-file");
			        save.append("version:1.00");
			        save.append("START");
			        save.append("maxSpecies:"+maxSpecies);
			        save.append("uniUniType:"+uniUniType);
			        save.append("biUniType:"+biUniType);
			        save.append("biBiType:"+biBiType);
			        save.append("warnings:"+maxSpeciesWarnings);
			        save.append("noReactionMAK:"+noReactionMAK);
			        save.append("possibleEnzymeRNA:"+possibleEnzymeRNA);
			        save.append("GenKinForAllReac:"+generateKineticForAllReaction);  
			        save.append("reversibility:"+reversibility);
			        save.append("possibleEnzymeGenericProtein:"+possibleEnzymeGenericProtein);
			        save.append("possibleEnzymeTruncatedProtein:"+possibleEnzymeTruncatedProtein);
			        save.append("possibleEnzymeComplex:"+possibleEnzymeComplex);
			        save.append("possibleEnzymeUnknown:"+possibleEnzymeUnknown);
			        save.append("possibleEnzymeReceptor:"+possibleEnzymeReceptor);
			        save.append("possibleEnzymeSimpleMolecule:"+possibleEnzymeSimpleMolecule);
			        save.append("possibleEnzymeAsRNA:"+possibleEnzymeAsRNA);
			        save.append("possibleEnzymeAllNotChecked:"+possibleEnzymeAllNotChecked);
			        save.append("forceAllReactionsAsEnzymeReaction:"+forceAllReactionsAsEnzymeReaction);  
			        save.append("END");
			        System.out.println("actionPerformed(Settings:Save)");
			        save.close();
				}
			});
		}
		return jButtonSettingsFrameSave;
	}
	
	/**
	 * This method initializes jCheckBoxSettingsFrameWarnings	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getJCheckBoxSettingsFrameWarnings() {
		if (jCheckBoxSettingsFrameWarnings == null) {
			jCheckBoxSettingsFrameWarnings = new JCheckBox();
			jCheckBoxSettingsFrameWarnings.setSelected(true);
			jCheckBoxSettingsFrameWarnings.setBounds(new Rectangle(15, 40, 220, 20));
			jCheckBoxSettingsFrameWarnings.setToolTipText("Check this box, if you want to be warned, if the number of reactants involved in a single reaction is higher than specified here.");
			jCheckBoxSettingsFrameWarnings.setText("Warnings for too many reactants");
			jCheckBoxSettingsFrameWarnings
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(java.awt.event.ActionEvent e) {
							System.out.println("actionPerformed(Settings:Warnings)");
							maxSpeciesWarnings=!(maxSpeciesWarnings);
						}
					});
		}
		return jCheckBoxSettingsFrameWarnings;
	}
	
	/**
	 * This method initializes jCheckBoxSettingsFrameMAK	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JRadioButton getJRadioButtonSettingsFrameMAK() {
		if (jRadioButtonSettingsFrameMAK == null) {
			jRadioButtonSettingsFrameMAK = new JRadioButton();
			jRadioButtonSettingsFrameMAK.setSelected(true);
			jRadioButtonSettingsFrameMAK.setEnabled(true);
			jRadioButtonSettingsFrameMAK.setBounds(new Rectangle(15, 20, 174, 20));
			jRadioButtonSettingsFrameMAK.setToolTipText("Reactions, which are not catalized by an enzyme, are described by a generalized mass action kinetic (gMAK).");
			jRadioButtonSettingsFrameMAK.setText("Generalized M.A.K.");
			
			jRadioButtonSettingsFrameMAK
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(java.awt.event.ActionEvent e) {
							System.out.println("actionPerformed(Settings:noReactionMAK)");
							noReactionMAK=!(noReactionMAK);
							noReactionMAK=!(noReactionMAK);
							jRadioButtonSettingsFrameMAK.setSelected(true);
							//
							System.out.println(noReactionMAK);
						}
					});
			 	
		
		}
		return jRadioButtonSettingsFrameMAK;
	}
		
	/**
	 * This method initializes jRadioButtonSettingsFrameUniUniMMK	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getJRadioButtonSettingsFrameUniUniMMK() {
		if (jRadioButtonSettingsFrameUniUniMMK == null) {
			jRadioButtonSettingsFrameUniUniMMK = new JRadioButton();
			jRadioButtonSettingsFrameUniUniMMK.setSelected(true);
			jRadioButtonSettingsFrameUniUniMMK.setBounds(new Rectangle(15, 20, 184, 20));
			jRadioButtonSettingsFrameUniUniMMK.setToolTipText("Check this box, if you want Michaelis Menten kinetic as reaction scheme for Uni-Uni reactions (one reactant, one product). ");
			jRadioButtonSettingsFrameUniUniMMK.setText("Michaelis Menten kinetics");
			jRadioButtonSettingsFrameUniUniMMK
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(java.awt.event.ActionEvent e) {
							System.out.println("actionPerformed(Startframe:UniUniMMK)");
							uniUniType =3;//"MMK";
							jRadioButtonSettingsFrameUniUniCONV.setSelected(false);
							jRadioButtonSettingsFrameUniUniMMK.setSelected(true);	
							
						}
					});
		}
		return jRadioButtonSettingsFrameUniUniMMK;
	}
	
	/**
	 * This method initializes jRadioButtonSettingsFrameUniUniCONV	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getJRadioButtonSettingsFrameUniUniCONV() {
		if (jRadioButtonSettingsFrameUniUniCONV == null) {
			jRadioButtonSettingsFrameUniUniCONV = new JRadioButton();
			jRadioButtonSettingsFrameUniUniCONV.setText("Convenience kinetics");
			jRadioButtonSettingsFrameUniUniCONV.setToolTipText("Check this box, if you want Convenience kinetic as reaction scheme for Uni-Uni reactions (one reactant, one product). ");
			jRadioButtonSettingsFrameUniUniCONV.setBounds(new Rectangle(15, 40, 175, 20));
			jRadioButtonSettingsFrameUniUniCONV
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(java.awt.event.ActionEvent e) {
							System.out.println("actionPerformed(Startframe:UniUniCONV)");
							uniUniType =2;//"CONV";
							jRadioButtonSettingsFrameUniUniMMK.setSelected(false);	
							jRadioButtonSettingsFrameUniUniCONV.setSelected(true);	
							
						}
					});
		}
		return jRadioButtonSettingsFrameUniUniCONV;
	}

	/**
	 * This method initializes jRadioButtonSettingsFrameBiUniRND	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getJRadioButtonSettingsFrameBiUniRND() {
		if (jRadioButtonSettingsFrameBiUniRND == null) {
			jRadioButtonSettingsFrameBiUniRND = new JRadioButton();
			jRadioButtonSettingsFrameBiUniRND.setSelected(true);
			jRadioButtonSettingsFrameBiUniRND.setBounds(new Rectangle(15, 60, 175, 20));
			jRadioButtonSettingsFrameBiUniRND.setToolTipText("Check this box, if you want Random scheme as reaction scheme for Bi-Uni reactions (two reactant, one product). ");
			jRadioButtonSettingsFrameBiUniRND.setText("Random");
			jRadioButtonSettingsFrameBiUniRND
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(java.awt.event.ActionEvent e) {
							System.out.println("actionPerformed(Startframe:BiUniRND)");
							biUniType =4;//"RND";
							jRadioButtonSettingsFrameBiUniCONV.setSelected(false);
							jRadioButtonSettingsFrameBiUniORD.setSelected(false);	
							jRadioButtonSettingsFrameBiUniRND.setSelected(true);	
							
						}
					});
		}
		return jRadioButtonSettingsFrameBiUniRND;
	}
	
	/**
	 * This method initializes jRadioButtonSettingsFrameBiUniCONV	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getJRadioButtonSettingsFrameBiUniCONV() {
		if (jRadioButtonSettingsFrameBiUniCONV == null) {
			jRadioButtonSettingsFrameBiUniCONV = new JRadioButton();
			jRadioButtonSettingsFrameBiUniCONV.setText("Convenience kinetics");
			jRadioButtonSettingsFrameBiUniCONV.setToolTipText("Check this box, if you want Convenience kinetic as reaction scheme for Bi-Uni reactions (two reactant, one product). ");
			jRadioButtonSettingsFrameBiUniCONV.setBounds(new Rectangle(15, 40, 175, 20));
			jRadioButtonSettingsFrameBiUniCONV
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(java.awt.event.ActionEvent e) {
							System.out.println("actionPerformed(Startframe:BiUniCONV)");
							biUniType =2;//"CONV";
							jRadioButtonSettingsFrameBiUniRND.setSelected(false);
							jRadioButtonSettingsFrameBiUniORD.setSelected(false);
							jRadioButtonSettingsFrameBiUniCONV.setSelected(true);
							
						}
					});
		}
		return jRadioButtonSettingsFrameBiUniCONV;
	}

	/**
	 * This method initializes jRadioButtonSettingsFrameBiUniORD	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getJRadioButtonSettingsFrameBiUniORD() {
		if (jRadioButtonSettingsFrameBiUniORD == null) {
			jRadioButtonSettingsFrameBiUniORD = new JRadioButton();
			jRadioButtonSettingsFrameBiUniORD.setSelected(false);
			jRadioButtonSettingsFrameBiUniORD.setBounds(new Rectangle(15, 20, 175, 20));
			jRadioButtonSettingsFrameBiUniORD.setToolTipText("Check this box, if you want Ordered scheme as reaction scheme for Bi-Uni reactions (two reactant, one product). ");
			jRadioButtonSettingsFrameBiUniORD.setText("Ordered");
			jRadioButtonSettingsFrameBiUniORD
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(java.awt.event.ActionEvent e) {
							System.out.println("actionPerformed(Startframe:BiUniORD)");
							biUniType =6;//"ORD";
							jRadioButtonSettingsFrameBiUniCONV.setSelected(false);
							jRadioButtonSettingsFrameBiUniRND.setSelected(false);
							jRadioButtonSettingsFrameBiUniORD.setSelected(true);
							
						}
					});
		}
		return jRadioButtonSettingsFrameBiUniORD;
	}
	
	/**
	 * This method initializes jRadioButtonSettingsFrameBiBiRND	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getJRadioButtonSettingsFrameBiBiRND() {
		if (jRadioButtonSettingsFrameBiBiRND == null) {
			jRadioButtonSettingsFrameBiBiRND = new JRadioButton();
			jRadioButtonSettingsFrameBiBiRND.setSelected(true);
			jRadioButtonSettingsFrameBiBiRND.setBounds(new Rectangle(15, 20, 157, 20));
			jRadioButtonSettingsFrameBiBiRND.setToolTipText("Check this box, if you want Random scheme as reaction scheme for Bi-Bi reactions (two reactant, two product). ");
			jRadioButtonSettingsFrameBiBiRND.setText("Random");
			jRadioButtonSettingsFrameBiBiRND
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(java.awt.event.ActionEvent e) {
							System.out.println("actionPerformed(Startframe:BiBiRND)");
							biBiType =4;//"RND";
							jRadioButtonSettingsFrameBiBiCONV.setSelected(false);
							jRadioButtonSettingsFrameBiBiORD.setSelected(false);
							jRadioButtonSettingsFrameBiBiPP.setSelected(false);
							jRadioButtonSettingsFrameBiBiRND.setSelected(true);
							
						}
					});
		}
		return jRadioButtonSettingsFrameBiBiRND;
	}
	
	/**
	 * This method initializes jRadioButtonSettingsFrameBiBiCONV	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getJRadioButtonSettingsFrameBiBiCONV() {
		if (jRadioButtonSettingsFrameBiBiCONV == null) {
			jRadioButtonSettingsFrameBiBiCONV = new JRadioButton();
			jRadioButtonSettingsFrameBiBiCONV.setText("Convenience kinetics");
			jRadioButtonSettingsFrameBiBiCONV.setToolTipText("Check this box, if you want Convenience kinetics as reaction scheme for Bi-Bi reactions (two reactant, two product). ");
			jRadioButtonSettingsFrameBiBiCONV.setBounds(new Rectangle(15, 40, 166, 20));
			jRadioButtonSettingsFrameBiBiCONV
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(java.awt.event.ActionEvent e) {
							System.out.println("actionPerformed(Startframe:BiBiCONV)");
							biBiType =2;//"CONV";
							jRadioButtonSettingsFrameBiBiORD.setSelected(false);
							jRadioButtonSettingsFrameBiBiRND.setSelected(false);
							jRadioButtonSettingsFrameBiBiPP.setSelected(false);					
							jRadioButtonSettingsFrameBiBiCONV.setSelected(true);
							
						}
					});
		}
		return jRadioButtonSettingsFrameBiBiCONV;
	}
	
	/**
	 * This method initializes jRadioButtonSettingsFrameBiBiORD	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getJRadioButtonSettingsFrameBiBiORD() {
		if (jRadioButtonSettingsFrameBiBiORD == null) {
			jRadioButtonSettingsFrameBiBiORD = new JRadioButton();
			jRadioButtonSettingsFrameBiBiORD.setText("Ordered");
			jRadioButtonSettingsFrameBiBiORD.setToolTipText("Check this box, if you want Ordered scheme as reaction scheme for Bi-Bi reactions (two reactant, two product). ");
			jRadioButtonSettingsFrameBiBiORD.setBounds(new Rectangle(15, 60, 140, 20));
			jRadioButtonSettingsFrameBiBiORD
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(java.awt.event.ActionEvent e) {
							System.out.println("actionPerformed(Startframe:BiBiORD)");
							biBiType =6;//"ORD";
							jRadioButtonSettingsFrameBiBiCONV.setSelected(false);
							jRadioButtonSettingsFrameBiBiRND.setSelected(false);
							jRadioButtonSettingsFrameBiBiPP.setSelected(false);
							jRadioButtonSettingsFrameBiBiORD.setSelected(true);
							
						}
					});
		}
		return jRadioButtonSettingsFrameBiBiORD;
	}
	
	/**
	 * This method initializes jRadioButtonSettingsFrameBiBiPP	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getJRadioButtonSettingsFrameBiBiPP() {
		if (jRadioButtonSettingsFrameBiBiPP == null) {
			jRadioButtonSettingsFrameBiBiPP = new JRadioButton();
			jRadioButtonSettingsFrameBiBiPP.setText("Ping-Pong");
			jRadioButtonSettingsFrameBiBiPP.setToolTipText("Check this box, if you want Ping-Pong scheme as reaction scheme for Bi-Bi reactions (two reactant, two product). ");
			jRadioButtonSettingsFrameBiBiPP.setBounds(new Rectangle(15, 80, 135, 20));
			jRadioButtonSettingsFrameBiBiPP
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(java.awt.event.ActionEvent e) {
							System.out.println("actionPerformed(Startframe:BiBiPP)");
							biBiType =5;//"PP";
							jRadioButtonSettingsFrameBiBiCONV.setSelected(false);
							jRadioButtonSettingsFrameBiBiRND.setSelected(false);
							jRadioButtonSettingsFrameBiBiORD.setSelected(false);
							jRadioButtonSettingsFrameBiBiPP.setSelected(true);
						}
					});
		}
		return jRadioButtonSettingsFrameBiBiPP;
	}
	
	/**
	 * This method initializes jRadioButtonOtherEnzymCONV	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getJRadioButtonSettingsFrameOtherEnzymCONV() {
		if (jRadioButtonSettingsFrameOtherEnzymCONV == null) {
			jRadioButtonSettingsFrameOtherEnzymCONV = new JRadioButton();
			jRadioButtonSettingsFrameOtherEnzymCONV.setSelected(true);
			jRadioButtonSettingsFrameOtherEnzymCONV.setEnabled(true);
			jRadioButtonSettingsFrameOtherEnzymCONV.setBounds(new Rectangle(15, 20, 162, 20));
			jRadioButtonSettingsFrameOtherEnzymCONV.setToolTipText("Reactions, which are catalized by en enzyme, and are no Uni-Uni / Bi-Uni / Bi-Bi reaction, are described by a Convenience kinetic as reaction scheme.  ");
			jRadioButtonSettingsFrameOtherEnzymCONV.setText("Convenience kinetics");
			
			jRadioButtonSettingsFrameOtherEnzymCONV
			.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					System.out.println("actionPerformed(Settings:Other Enzym Reaction)");
					jRadioButtonSettingsFrameOtherEnzymCONV.setSelected(true);	
				}
			});
			
			
		}
		return jRadioButtonSettingsFrameOtherEnzymCONV;
	}

	/**
	 * This method initializes jSpinnerSettingsFrameSpinWarnings
	 * 	
	 * @return javax.swing.JSpinner	
	 */
	private JSpinner getJSpinnerSettingsFrameSpinWarnings() {
		if (jSpinnerSettingsFrameSpinWarnings == null) {
			SpinnerNumberModel blub = new SpinnerNumberModel(4,2,10,1); 
			jSpinnerSettingsFrameSpinWarnings = new JSpinner(blub);
			jSpinnerSettingsFrameSpinWarnings.setBounds(new Rectangle(235, 40, 70, 20));
			jSpinnerSettingsFrameSpinWarnings.setToolTipText("Specifiy here the number of reactants, from which on warnings are given.");
			jSpinnerSettingsFrameSpinWarnings
					.addChangeListener(new javax.swing.event.ChangeListener() {
						public void stateChanged(javax.swing.event.ChangeEvent e) {
							System.out.println("stateChanged(SettingsFrame:JSpinnerWarnings)");
							Object value = jSpinnerSettingsFrameSpinWarnings.getValue();
							maxSpecies=new Integer (value.toString());
							//System.out.println(maxSpecies);
						}
					});
		}
		return jSpinnerSettingsFrameSpinWarnings;
	}
	
	/**
	 * This method initializes jCheckBoxSettingsFrameForceAllReactionsAsEnzyeReaction	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getJCheckBoxSettingsFrameForceAllReactionsAsEnzyeReaction() {
		if (jCheckBoxSettingsFrameForceAllReactionsAsEnzyeReaction == null) {
			jCheckBoxSettingsFrameForceAllReactionsAsEnzyeReaction = new JCheckBox();
			jCheckBoxSettingsFrameForceAllReactionsAsEnzyeReaction.setText("Consider all reactions as being catalized by an enzyme");
			jCheckBoxSettingsFrameForceAllReactionsAsEnzyeReaction.setToolTipText("Check this box, if you want that all reactions are considered as being catalized by an enzyme, including generalized mass action kinetics (gMAK).");
			jCheckBoxSettingsFrameForceAllReactionsAsEnzyeReaction.setBounds(new Rectangle(15, 20, 350, 20));
			jCheckBoxSettingsFrameForceAllReactionsAsEnzyeReaction
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(java.awt.event.ActionEvent e) {
							System.out.println("actionPerformed(SettingsFrame:ForceAllReactionsAsEnzymeReaction)");
							forceAllReactionsAsEnzymeReaction=!forceAllReactionsAsEnzymeReaction;
							 possibleEnzymeTestAllNotChecked();
						}
					});
		}
		return jCheckBoxSettingsFrameForceAllReactionsAsEnzyeReaction;
	}

	/**
	 * This method initializes jCheckBoxSettingsFramePossibleEnzymeRNA	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getJCheckBoxSettingsFramePossibleEnzymeRNA() {
		if (jCheckBoxSettingsFramePossibleEnzymeRNA == null) {
			jCheckBoxSettingsFramePossibleEnzymeRNA = new JCheckBox();
			jCheckBoxSettingsFramePossibleEnzymeRNA.setSelected(true);
			jCheckBoxSettingsFramePossibleEnzymeRNA.setBounds(new Rectangle(146, 20, 62, 20));
			jCheckBoxSettingsFramePossibleEnzymeRNA.setToolTipText("Check this box, if you want RNA treated as enzyme. If this check box is not selected, RNA catalized reactions are considered as catalized gMAK reactions.");
			jCheckBoxSettingsFramePossibleEnzymeRNA.setText("RNA");
			jCheckBoxSettingsFramePossibleEnzymeRNA
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(java.awt.event.ActionEvent e) {
							System.out.println("actionPerformed(SettingsFrame:PossbleEnzymeRNA)"); 
							possibleEnzymeRNA=!possibleEnzymeRNA;
							possibleEnzymeTestAllNotChecked();
						}
					});
		}
		return jCheckBoxSettingsFramePossibleEnzymeRNA;
	}

	/**
	 * This method initializes jCheckBoxSettingsFramePossibleEnzymeAsRNA	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getJCheckBoxSettingsFramePossibleEnzymeAsRNA() {
		if (jCheckBoxSettingsFramePossibleEnzymeAsRNA == null) {
			jCheckBoxSettingsFramePossibleEnzymeAsRNA = new JCheckBox();
			jCheckBoxSettingsFramePossibleEnzymeAsRNA.setText("asRNA");
			jCheckBoxSettingsFramePossibleEnzymeAsRNA.setToolTipText("Check this box, if you want asRNA treated as enzyme. If this check box is not selected, asRNA catalized reactions are considered as catalized gMAK reactions.");
			jCheckBoxSettingsFramePossibleEnzymeAsRNA.setBounds(new Rectangle(146, 40, 75, 20));
			jCheckBoxSettingsFramePossibleEnzymeAsRNA
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(java.awt.event.ActionEvent e) {
							System.out.println("actionPerformed(SettingsFrame:PossbleEnzymeAsRNA)"); 
							possibleEnzymeAsRNA=!possibleEnzymeAsRNA;
							possibleEnzymeTestAllNotChecked();
						}
					});
		}
		return jCheckBoxSettingsFramePossibleEnzymeAsRNA;
	}

	/**
	 * This method initializes jCheckBoxSettingsFramePossibleEnzymeGenericProtein	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getJCheckBoxSettingsFramePossibleEnzymeGenericProtein() {
		if (jCheckBoxSettingsFramePossibleEnzymeGenericProtein == null) {
			jCheckBoxSettingsFramePossibleEnzymeGenericProtein = new JCheckBox();
			jCheckBoxSettingsFramePossibleEnzymeGenericProtein.setSelected(true);
			jCheckBoxSettingsFramePossibleEnzymeGenericProtein.setBounds(new Rectangle(11, 20, 121, 20));
			jCheckBoxSettingsFramePossibleEnzymeGenericProtein.setToolTipText("Check this box, if you want generic proteins treated as enzymes. If this check box is not selected, generic proteins catalized reactions are considered as catalized gMAK reactions.");
			jCheckBoxSettingsFramePossibleEnzymeGenericProtein.setText("Generic protein");
			jCheckBoxSettingsFramePossibleEnzymeGenericProtein
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(java.awt.event.ActionEvent e) {
							System.out.println("actionPerformed(SettingsFrame:PossbleEnzymeGenericprotein)"); 
							possibleEnzymeGenericProtein=!possibleEnzymeGenericProtein;
							possibleEnzymeTestAllNotChecked();
						}
					});
		}
		return jCheckBoxSettingsFramePossibleEnzymeGenericProtein;
	}

	/**
	 * This method initializes jCheckBoxSettingsFramePossibleEnzymeTruncatedProtein	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getJCheckBoxSettingsFramePossibleEnzymeTruncatedProtein() {
		if (jCheckBoxSettingsFramePossibleEnzymeTruncatedProtein == null) {
			jCheckBoxSettingsFramePossibleEnzymeTruncatedProtein = new JCheckBox();
			jCheckBoxSettingsFramePossibleEnzymeTruncatedProtein.setSelected(true);
			jCheckBoxSettingsFramePossibleEnzymeTruncatedProtein.setBounds(new Rectangle(326, 20, 138, 20));
			jCheckBoxSettingsFramePossibleEnzymeTruncatedProtein.setToolTipText("Check this box, if you want Truncated protein treated as enzyme. If this check box is not selected, Truncated protein catalized reactions are considered as catalized gMAK reactions.");
			jCheckBoxSettingsFramePossibleEnzymeTruncatedProtein.setText("Truncated protein");
			jCheckBoxSettingsFramePossibleEnzymeTruncatedProtein
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(java.awt.event.ActionEvent e) {
							System.out.println("actionPerformed(SettingsFrame:PossbleEnzymeTruncatedProtein)"); 
							possibleEnzymeTruncatedProtein=!possibleEnzymeTruncatedProtein;
							possibleEnzymeTestAllNotChecked();
						}
					});
		}
		return jCheckBoxSettingsFramePossibleEnzymeTruncatedProtein;
	}

	/**
	 * This method initializes jCheckBoxSettingsFramePossibleEnzymeSimpleMolecule	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getJCheckBoxSettingsFramePossibleEnzymeSimpleMolecule() {
		if (jCheckBoxSettingsFramePossibleEnzymeSimpleMolecule == null) {
			jCheckBoxSettingsFramePossibleEnzymeSimpleMolecule = new JCheckBox();
			jCheckBoxSettingsFramePossibleEnzymeSimpleMolecule.setText("Simple molecule");
			jCheckBoxSettingsFramePossibleEnzymeSimpleMolecule.setToolTipText("Check this box, if you want Simple molecule treated as enzyme. If this check box is not selected, Simple molecule catalized reactions are considered as catalized gMAK reactions.");
			jCheckBoxSettingsFramePossibleEnzymeSimpleMolecule.setBounds(new Rectangle(11, 40, 121, 20));
			jCheckBoxSettingsFramePossibleEnzymeSimpleMolecule
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(java.awt.event.ActionEvent e) {
							System.out.println("actionPerformed(SettingsFrame:PossbleEnzymeSimpleMolecule)"); 
							possibleEnzymeSimpleMolecule=!possibleEnzymeSimpleMolecule;
							possibleEnzymeTestAllNotChecked();
						}
					});
		}
		return jCheckBoxSettingsFramePossibleEnzymeSimpleMolecule;
	}

	/**
	 * This method initializes jCheckBoxSettingsFramePossibleEnzymeComplex	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getJCheckBoxSettingsFramePossibleEnzymeComplex() {
		if (jCheckBoxSettingsFramePossibleEnzymeComplex == null) {
			jCheckBoxSettingsFramePossibleEnzymeComplex = new JCheckBox();
			jCheckBoxSettingsFramePossibleEnzymeComplex.setSelected(true);
			jCheckBoxSettingsFramePossibleEnzymeComplex.setBounds(new Rectangle(224, 20, 88, 20));
			jCheckBoxSettingsFramePossibleEnzymeComplex.setToolTipText("Check this box, if you want Complex treated as enzyme. If this check box is not selected, Complex catalized reactions are considered as catalized gMAK reactions.");
			jCheckBoxSettingsFramePossibleEnzymeComplex.setText("Complex");
			jCheckBoxSettingsFramePossibleEnzymeComplex
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(java.awt.event.ActionEvent e) {
							System.out.println("actionPerformed(SettingsFrame:PossbleEnzymeComplex)"); 
							possibleEnzymeComplex=!possibleEnzymeComplex;
							possibleEnzymeTestAllNotChecked();
						}
					});
		}
		return jCheckBoxSettingsFramePossibleEnzymeComplex;
	}

	/**
	 * This method initializes jCheckBoxSettingsFramePossibleEnzymeReceptor	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getJCheckBoxSettingsFramePossibleEnzymeReceptor() {
		if (jCheckBoxSettingsFramePossibleEnzymeReceptor == null) {
			jCheckBoxSettingsFramePossibleEnzymeReceptor = new JCheckBox();
			jCheckBoxSettingsFramePossibleEnzymeReceptor.setText("Receptor");
			jCheckBoxSettingsFramePossibleEnzymeReceptor.setToolTipText("Check this box, if you want Receptor treated as enzyme. If this check box is not selected, Receptor catalized reactions are considered as catalized gMAK reactions.");
			jCheckBoxSettingsFramePossibleEnzymeReceptor.setBounds(new Rectangle(326, 40, 86, 20));
			jCheckBoxSettingsFramePossibleEnzymeReceptor
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(java.awt.event.ActionEvent e) {
							System.out.println("actionPerformed(SettingsFrame:PossbleEnzymeReceptor)"); 
							possibleEnzymeReceptor=!possibleEnzymeReceptor;
							possibleEnzymeTestAllNotChecked();
							
						}
					});
		}
		return jCheckBoxSettingsFramePossibleEnzymeReceptor;
	}

	/**
	 * This method initializes jCheckBoxSettingsFramePossibleEnzymeUnknown	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getJCheckBoxSettingsFramePossibleEnzymeUnknown() {
		if (jCheckBoxSettingsFramePossibleEnzymeUnknown == null) {
			jCheckBoxSettingsFramePossibleEnzymeUnknown = new JCheckBox();
			jCheckBoxSettingsFramePossibleEnzymeUnknown.setText("Unknown");
			jCheckBoxSettingsFramePossibleEnzymeUnknown.setToolTipText("Check this box, if you want Unknown treated as enzyme. If this check box is not selected, Unknown catalized reactions are considered as catalized gMAK reactions.");
			jCheckBoxSettingsFramePossibleEnzymeUnknown.setBounds(new Rectangle(224, 40, 97, 20));
			jCheckBoxSettingsFramePossibleEnzymeUnknown
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(java.awt.event.ActionEvent e) {
							System.out.println("actionPerformed(SettingsFrame:PossbleEnzymeUnknown)"); 
							possibleEnzymeUnknown=!possibleEnzymeUnknown;
							possibleEnzymeTestAllNotChecked();	
						}
					});
		}
		return jCheckBoxSettingsFramePossibleEnzymeUnknown;
	}

	//TODO: kommentar fehlt
	private JRadioButton getJRadioButtonSettingsFrameGenOnlyMissKin(){
	if (jRadioButtonSettingsFrameGenOnlyMissKin == null) {
		jRadioButtonSettingsFrameGenOnlyMissKin = new JRadioButton();
		jRadioButtonSettingsFrameGenOnlyMissKin.setSelected(true);
		jRadioButtonSettingsFrameGenOnlyMissKin.setBounds(new Rectangle(15, 20, 350, 20));
		jRadioButtonSettingsFrameGenOnlyMissKin.setToolTipText("Check this box, if you want, that kinetics are only generated if they are missing in the sbml file.");
		jRadioButtonSettingsFrameGenOnlyMissKin.setText("only when missing");
		jRadioButtonSettingsFrameGenOnlyMissKin
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						System.out.println("actionPerformed(SettingsFrame:GenOnlyMissReac)");
						jRadioButtonSettingsFrameGenForAllReac.setSelected(false);
						jRadioButtonSettingsFrameGenOnlyMissKin.setSelected(true);
						generateKineticForAllReaction=false;	
					}
				});

		}
	return jRadioButtonSettingsFrameGenOnlyMissKin;
}

	/**
	 * This method initializes jRadioButtonSettingsFrameGenForAllReac	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getJRadioButtonSettingsFrameGenForAllReac() {
		if (jRadioButtonSettingsFrameGenForAllReac == null) {
			jRadioButtonSettingsFrameGenForAllReac = new JRadioButton();
			jRadioButtonSettingsFrameGenForAllReac.setText("for all reactions");
			jRadioButtonSettingsFrameGenForAllReac.setToolTipText("Check this box, if you want that kinetics are also generated, if they exist in the sbml file.");
			jRadioButtonSettingsFrameGenForAllReac.setBounds(new Rectangle(15, 40, 350, 20));
			jRadioButtonSettingsFrameGenForAllReac
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(java.awt.event.ActionEvent e) {
							System.out.println("actionPerformed(StetingsFrame:GenerateForAllReac)");
							jRadioButtonSettingsFrameGenForAllReac.setSelected(true);
							jRadioButtonSettingsFrameGenOnlyMissKin.setSelected(false);
							generateKineticForAllReaction=true;
						}
					});
		}
		return jRadioButtonSettingsFrameGenForAllReac;
	}
	

	
	/**
	 * This method initializes jRadioButtonSettingsFrameForceReacRev	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getJRadioButtonSettingsFrameForceReacRev() {
		if (jRadioButtonSettingsFrameForceReacRev == null) {
			jRadioButtonSettingsFrameForceReacRev = new JRadioButton();
			jRadioButtonSettingsFrameForceReacRev.setSelected(true);
			jRadioButtonSettingsFrameForceReacRev.setBounds(new Rectangle(15, 20, 316, 20));
			jRadioButtonSettingsFrameForceReacRev.setToolTipText("Check this box, if you want to generate reversible kinetics for all reactions, ignoring the information given by the sbml file.");
			jRadioButtonSettingsFrameForceReacRev.setText("Model all reactions in a reversible manner");
			jRadioButtonSettingsFrameForceReacRev
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(java.awt.event.ActionEvent e) {
							System.out.println("actionPerformed(SettingsFrame:ForceReacReversible)");
							jRadioButtonSettingsFrameForceRevAsCD.setSelected(false);
							jRadioButtonSettingsFrameForceReacRev.setSelected(true);
							reversibility = true;
						}
					});
		}
		return jRadioButtonSettingsFrameForceReacRev;
	}
	
	/**
	 * This method initializes jRadioButtonSettingsFrameForceRevAsCD	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getJRadioButtonSettingsFrameForceRevAsCD() {
		if (jRadioButtonSettingsFrameForceRevAsCD == null) {
			jRadioButtonSettingsFrameForceRevAsCD = new JRadioButton();
			jRadioButtonSettingsFrameForceRevAsCD.setText("Use information from SBML");
			jRadioButtonSettingsFrameForceRevAsCD.setToolTipText("Check this box, if you want to generate reversible kinetics only, if such information is given by the sbml file.");
			jRadioButtonSettingsFrameForceRevAsCD.setBounds(new Rectangle(15, 40, 350, 20));
			jRadioButtonSettingsFrameForceRevAsCD
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(java.awt.event.ActionEvent e) {
							System.out.println("actionPerformed(SettingsFrame:ForceReversibilityAsCellDesignerPresets)");
							jRadioButtonSettingsFrameForceRevAsCD.setSelected(true);
							jRadioButtonSettingsFrameForceReacRev.setSelected(false);
							reversibility = false;
						}
					});
		}
		return jRadioButtonSettingsFrameForceRevAsCD;
	}

	/**
	 * This method checks, if there is any possible Enzyme checked or not	
	 * 	
	 * @return: void	
	 */
	private void possibleEnzymeTestAllNotChecked(){
		if (	possibleEnzymeRNA == false &&
				possibleEnzymeAsRNA == false &&
				possibleEnzymeGenericProtein == false &&
				possibleEnzymeTruncatedProtein == false &&
				possibleEnzymeSimpleMolecule == false &&
				possibleEnzymeComplex == false &&
				possibleEnzymeReceptor == false &&
				possibleEnzymeUnknown == false	&&
				forceAllReactionsAsEnzymeReaction == false
			)
		{
			possibleEnzymeAllNotChecked=true;
			System.out.println("actionRecognized(SettingsFrame:AllNotChecked)");
			jRadioButtonSettingsFrameUniUniMMK.setSelected(false);
			jRadioButtonSettingsFrameUniUniCONV.setSelected(false);
			jRadioButtonSettingsFrameBiUniORD.setSelected(false);
			jRadioButtonSettingsFrameBiUniCONV.setSelected(false);
			jRadioButtonSettingsFrameBiUniRND.setSelected(false);
			jRadioButtonSettingsFrameBiBiPP.setSelected(false);
			jRadioButtonSettingsFrameBiBiORD.setSelected(false);
			jRadioButtonSettingsFrameBiBiCONV.setSelected(false);
			jRadioButtonSettingsFrameBiBiRND.setSelected(false);
			jRadioButtonSettingsFrameOtherEnzymCONV.setSelected(false);
			
			jRadioButtonSettingsFrameUniUniMMK.setEnabled(false);
			jRadioButtonSettingsFrameUniUniCONV.setEnabled(false);
			jRadioButtonSettingsFrameBiUniORD.setEnabled(false);
			jRadioButtonSettingsFrameBiUniCONV.setEnabled(false);
			jRadioButtonSettingsFrameBiUniRND.setEnabled(false);
			jRadioButtonSettingsFrameBiBiPP.setEnabled(false);
			jRadioButtonSettingsFrameBiBiORD.setEnabled(false);
			jRadioButtonSettingsFrameBiBiCONV.setEnabled(false);
			jRadioButtonSettingsFrameBiBiRND.setEnabled(false);
			jRadioButtonSettingsFrameOtherEnzymCONV.setEnabled(false);
			}
			
		else {
			possibleEnzymeAllNotChecked=false;
			System.out.println("actionRecognized(SettingsFrame:SomeChecked)");
			
			if (uniUniType==3)//.equals("MMK")) 
				jRadioButtonSettingsFrameUniUniMMK.setSelected(true);
			else
				jRadioButtonSettingsFrameUniUniMMK.setSelected(false);
			
			if (uniUniType==2)//.equals("CONV")) 
				jRadioButtonSettingsFrameUniUniCONV.setSelected(true);
			else
				jRadioButtonSettingsFrameUniUniCONV.setSelected(false);
			
			if (biUniType==6)//.equals("ORD")) 
				jRadioButtonSettingsFrameBiUniORD.setSelected(true);
			else
				jRadioButtonSettingsFrameBiUniORD.setSelected(false);
			
			if (biUniType==2)//.equals("CONV")) 
				jRadioButtonSettingsFrameBiUniCONV.setSelected(true);
			else
				jRadioButtonSettingsFrameBiUniCONV.setSelected(false);

			if (biUniType==4)//.equals("RND")) 
				jRadioButtonSettingsFrameBiUniRND.setSelected(true);
			else
				jRadioButtonSettingsFrameBiUniRND.setSelected(false);

			if (biBiType==5)//.equals("PP")) 
				jRadioButtonSettingsFrameBiBiPP.setSelected(true);
			else
				jRadioButtonSettingsFrameBiBiPP.setSelected(false);


			if (biBiType==6)//.equals("ORD")) 
				jRadioButtonSettingsFrameBiBiORD.setSelected(true);
			else
				jRadioButtonSettingsFrameBiBiORD.setSelected(false);


			if (biBiType==2)//.equals("CONV")) 
				jRadioButtonSettingsFrameBiBiCONV.setSelected(true);
			else
				jRadioButtonSettingsFrameBiBiCONV.setSelected(false);

			if (biBiType==4)//.equals("RND")) 
				jRadioButtonSettingsFrameBiBiRND.setSelected(true);
			else
				jRadioButtonSettingsFrameBiBiRND.setSelected(false);

			jRadioButtonSettingsFrameOtherEnzymCONV.setSelected(true);
			
			jRadioButtonSettingsFrameUniUniMMK.setEnabled(true);
			jRadioButtonSettingsFrameUniUniCONV.setEnabled(true);
			jRadioButtonSettingsFrameBiUniORD.setEnabled(true);
			jRadioButtonSettingsFrameBiUniCONV.setEnabled(true);
			jRadioButtonSettingsFrameBiUniRND.setEnabled(true);
			jRadioButtonSettingsFrameBiBiPP.setEnabled(true);
			jRadioButtonSettingsFrameBiBiORD.setEnabled(true);
			jRadioButtonSettingsFrameBiBiCONV.setEnabled(true);
			jRadioButtonSettingsFrameBiBiRND.setEnabled(true);	
			jRadioButtonSettingsFrameOtherEnzymCONV.setEnabled(true);
		}
	}

//	------------------------------------------------------------------------------------
//
//	                        REACTIONS FRAME
//
//	------------------------------------------------------------------------------------

	/**
	 * This method initializes jFrameReactionsFrame	
	 * 	
	 * @return javax.swing.JFrame	
	 */
	private JFrame getJFrameReactionsFrame() {
		if (jFrameReactionsFrame == null) {
			jFrameReactionsFrame = new JFrame();
			jFrameReactionsFrame.setSize(new Dimension(600, 360));
			jFrameReactionsFrame.setTitle("SBMLsqeezer ReactionList");
			jFrameReactionsFrame.setVisible(true);
			jFrameReactionsFrame.setContentPane(getJContentPaneReactionsFrame());
		}
		return jFrameReactionsFrame;
	}

	/**
	 * This method initializes jContentPaneReactionsFrame	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJContentPaneReactionsFrame() {
		if (jContentPaneReactionsFrame == null) {
			jLabelReactionsFrameWarnings = new JLabel();
			jLabelReactionsFrameWarnings.setBounds(new Rectangle(25, 240, 540, 20));
			jLabelReactionsFrameWarnings.setToolTipText("This textfield shows the number of warnings. The reactions with more reactants than what was set in the settings frame are highlighted in red.");
			jLabelReactionsFrameWarnings.setText("Number of warnings (red): "+numOfWarnings);
			jContentPaneReactionsFrame = new JPanel();
			jContentPaneReactionsFrame.setLayout(null);
			jContentPaneReactionsFrame.add(jLabelReactionsFrameWarnings, null);
			jContentPaneReactionsFrame.add(getJButtonReactionsFrameCancel(), null);
			jContentPaneReactionsFrame.add(getJScrollPaneReactionsFrame(), null);
			jContentPaneReactionsFrame.add(getJButtonReactionsFrameSave(), null);
			jContentPaneReactionsFrame.add(getjButtonReactionsFrameCloseApplication(), null);
		}
		return jContentPaneReactionsFrame;
	}
	

	
	/**
	 * This method initializes jButtonReactionsFrameCancel	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButtonReactionsFrameCancel() {
		if (jButtonReactionsFrameCancel == null) {
			jButtonReactionsFrameCancel = new JButton();
			jButtonReactionsFrameCancel.setBounds(new Rectangle(340, 285, 100, 25));
			jButtonReactionsFrameCancel.setToolTipText("Pressing the button \"Cancel\" brings you back to the previous Frame. No generated data will be transfered or stored into your open SBML file.");
			jButtonReactionsFrameCancel.setText("Cancel");
			jButtonReactionsFrameCancel
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(java.awt.event.ActionEvent e) {
							System.out.println("actionPerformed(ReactionsFrame:Cancel)");
							getJFrameMainFrame().setVisible(true);
							jFrameReactionsFrame.dispose();
						}
					});
		}
		return jButtonReactionsFrameCancel;
	}
		
	/**
	 * This method initializes jButtonSaveFrameSave	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButtonReactionsFrameSave() {
		if (jButtonReactionsFrameSave == null) {
			jButtonReactionsFrameSave = new JButton();
			jButtonReactionsFrameSave.setEnabled(true);
			jButtonReactionsFrameSave.setBounds(new Rectangle(35, 285, 100, 25));
			jButtonReactionsFrameSave.setToolTipText("This will transfer the kinetics and parameters to CellDesigner and bring up a save dialog which will let you save the generated differential equations as a *.txt or *.tex file.");
			jButtonReactionsFrameSave.setText("Save");
			jButtonReactionsFrameSave.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					System.out.println("actionPerformed(ReactionsFrame:save)");
					System.out.println("KineticsAndParametersStoredInSBML:" + KineticsAndParametersStoredInSBML);
					if(KineticsAndParametersStoredInSBML==false){
						KineticsAndParametersStoredInSBML = true;
						System.out.println("KineticsAndParametersStoredInSBML:" + KineticsAndParametersStoredInSBML);
						if (loadedAsPlugin){
							ekl.storeKineticsAndParameters();
						}
					}
					SaveODE save = new SaveODE();
					
					if (save.getSuffix().equals(".tex")){
						save.append("\\documentclass[11pt,a4paper]{scrartcl}");
						save.append("\\usepackage[latin1]{inputenc}");
						save.append("\\usepackage[scaled=.9]{helvet}");
						//save.append("\\usepackage[T1]{fontenc}");
						save.append("\\usepackage{times}");
						save.append("\\usepackage{a4wide}");
						save.append("\\usepackage[ngerman,english]{babel}");
						save.append("\\usepackage{amsmath}");
						save.append("\\title{SBML\\begin{LARGE}\\textsc{sqeezer }\\end{LARGE} Differential equations}");
						save.append("\\author{Nadine Hassis\\and Andreas Dr{\\\"a}ger\\and Andreas Zell}");
						save.append("\\date{\\today}");
						save.append("\\begin{document}");
						save.append("\\maketitle");
						//save.append("\\tableofcontents");
						save.append("\\section{Kinetics}");
						
						for(int i = 0; i < ekl.getReactionNumAndKinetictex().size(); i++)
						{
							String toWriteKinetic = "v_{" + i + "} = " + ekl.getReactionNumAndKinetictex().get(i);
							String toWriteReaction ="Reaction: \\texttt{" + ekl.getReactionNumAndId().get(i) + "}, " + ekl.getReactionNumAndKineticName().get(i);
							
							save.append("\\subsection{" + toWriteReaction + "}");
							save.append("\\begin{equation}");
							save.append	(toWriteKinetic);
							save.append("\\end{equation}");
						}
						
						save.append("\\section{Equations}");
						save.append("\\begin{description}");
						
				    	for(int i = 0; i < ekl.getAllSpecies().size(); i++)
				    	{
				    		String toWriteSpecies ="Species: \\texttt{" + ekl.getAllSpeciesName().get(ekl.getAllSpecies().get(i)) + "}(\\texttt{" + ekl.getAllSpecies().get(i) + "})";
				     		//String toWriteODE =ekl.getAllODE().get(ekl.getAllSpecies().get(i));   		
				    		String toWriteODE = "\\frac{\\text{d}[" + ekl.getAllSpecies().get(i) + "]}{\\text{dt}} = " + ekl.getSpecieAndSimpleODETex().get(ekl.getAllSpecies().get(i)); 
				    						    		
				    		save.append("\\item[" + toWriteSpecies + "]");
							save.append("\\begin{equation}");
				    		save.append	(toWriteODE);
				    		save.append("\\end{equation}");
				    	}

				    	save.append("\\end{description}");
				        save.append("\\end{document}");
				        System.out.println("actionPerformed(SaveFrame:save)");
				        save.close();
					}
					else if (save.getSuffix().equals(".txt")){
						
						save.append ("SBMLsqueezer generated and transfered values");
						save.append ("--------------------------------------------");
						
						for(int i = 0; i < ekl.getReactionNumAndKinetic().size(); i++)
						{
							String toWriteReaction ="Reaction: " + ekl.getReactionNumAndId().get(i) + ", " + ekl.getReactionNumAndKineticName().get(i);
							String toWrite ="Kinetic: v" + i + " = " + ekl.getReactionNumAndKinetic().get(i);
							save.append	(toWriteReaction);
				    		save.append	(toWrite);
				    		save.append (" ");
						}
						save.append (" ");
				    	for(int i = 0; i < ekl.getAllSpecies().size(); i++)
				    	{
				    		//String toWrite ="Species: " + ekl.getAllSpecies().get(i) + " ODE: " +	ekl.getAllODE().get(ekl.getAllSpecies().get(i));
				    		String toWrite ="Species: " + ekl.getAllSpecies().get(i) + " ODE: d[" + ekl.getAllSpecies().get(i) + "]/dt = " + ekl.getSpecieAndSimpleODE().get(ekl.getAllSpecies().get(i));
				    		save.append	(toWrite);
				    		save.append (" ");
				    	}

				        save.close();
				        System.out.println("actionPerformed(SaveFrame:save)");
				        
				        
				    }
				}
			});
		}
		return jButtonReactionsFrameSave;
	}

	/**
	 * This method initializes jButtonReactionsFrameCloseApplication	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getjButtonReactionsFrameCloseApplication() {
		if (jButtonReactionsFrameCloseApplication == null) {
			jButtonReactionsFrameCloseApplication = new JButton();
			jButtonReactionsFrameCloseApplication.setToolTipText("Pressing the Button \"Finish\" will save the kinetics and paramters in SBML-file and will close this plugin.");
			jButtonReactionsFrameCloseApplication.setBounds(new Rectangle(465, 285, 100, 25));
			jButtonReactionsFrameCloseApplication.setText("Finish");
			jButtonReactionsFrameCloseApplication
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(java.awt.event.ActionEvent e) {
							System.out.println("actionPerformed(SaveFrame:Finish)");
							if(KineticsAndParametersStoredInSBML==false){
								KineticsAndParametersStoredInSBML = true;
								if (loadedAsPlugin)
								ekl.storeKineticsAndParameters();
							}
						//	jFrameMainFrame.setVisible(false);
							//jFrameSettingsFrame.setVisible(false);
						//	jFrameReactionsFrame.setVisible(false);
						//	jFrameSaveFrame.setVisible(false);
							jFrameMainFrame.dispose();
							//jFrameSettingsFrame.dispose();
							jFrameReactionsFrame.dispose();
						}
					});
		}
		return jButtonReactionsFrameCloseApplication;
	}
	
	/**
	 * This method initializes jScrollPaneReactionsFrame	
	 * 	
	 * @return javax.swing.JScrollPaneReactionsFrame	
	 */
	private JScrollPane getJScrollPaneReactionsFrame() {
		if (jScrollPaneReactionsFrame == null) {
			jScrollPaneReactionsFrame = new JScrollPane();
			jScrollPaneReactionsFrame.setBounds(new Rectangle(25, 25, 540, 200));
		//	jScrollPaneReactionsFrame.setViewportView(getJTableReactionsFrameReactionTable());
		}
		return jScrollPaneReactionsFrame;
	}
	
	/**
	 * This method initializes jTable	
	 * 	
	 * @return javax.swing.JTable	
	 */
	private JTable getJTableReactionsFrameReactionTable() {	
			if (loadedAsPlugin)
			{
			//jTableReactionsFrameReactionTable = new JTable(new DataTableModel(ekl.getAllODE(),ekl.getAllSpecies()) );
			jTableReactionsFrameReactionTable = new JTable(new DataTableModel(
					ekl.getReactionNumAndKinetic(),
					ekl.getReactionNumOfNotExistKinetics(),
					ekl.getReactionNumAndParameters(), 
					ekl.getReactionNumAndId(),
					ekl.getReactionNumAndNumOfReactants(),
					ekl.getReactionNumAndReactants(), 
					ekl.getReactionNumAndProducts(),
					ekl.getReactionNumAndKineticName()));
			}
			else
			jTableReactionsFrameReactionTable = new JTable( new DataTableModel() );
			

			//finde die Anzahl der warnings und gebe sie im Label an
			numOfWarnings =0;
			if (loadedAsPlugin){
				for (int i=0; i < ekl.getReactionNumOfNotExistKinetics().size();i++){
					int value = ekl.getReactionNumAndNumOfReactants().get(ekl.getReactionNumOfNotExistKinetics().get(i));
					if (value >= maxSpecies )
						numOfWarnings++;
				}
				jLabelReactionsFrameWarnings.setText("Number of warnings (red): "+numOfWarnings);
			}
			
			//set manuell the size of the columns
			jTableReactionsFrameReactionTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

			TableColumn column = null;
			column = jTableReactionsFrameReactionTable.getColumnModel().getColumn(0);
			column.setPreferredWidth(75);
			column = jTableReactionsFrameReactionTable.getColumnModel().getColumn(1);
			column.setPreferredWidth(75);
			column = jTableReactionsFrameReactionTable.getColumnModel().getColumn(2);
			column.setPreferredWidth(100);
			column = jTableReactionsFrameReactionTable.getColumnModel().getColumn(3);
			column.setPreferredWidth(100);
			column = jTableReactionsFrameReactionTable.getColumnModel().getColumn(4);
			column.setPreferredWidth(250);
			
			//resizing automatic RowHight dependent on the maximum of existing BreakLines (\n) within one row  
				//int str_length =0; //see below: adjusting column length...
			TableModel model = jTableReactionsFrameReactionTable.getModel();
			int newLines =0;
			int maxNewLines =0;
			
			for (int clmCnt = model.getColumnCount(), rowCnt = model.getRowCount(), i = 0; i < rowCnt; i++) {
				for (int j = 0; j < clmCnt; j++) {
					if (model.getValueAt(i, j) != null){
						String value = model.getValueAt(i, j).toString();
							//	max length update
						
						if (value.contains("\n")){
							boolean existingNewlines=true;
							
							while (existingNewlines){
								int index=value.indexOf("\n");
								//System.out.println("index:"+index);
								value=value.substring(index+2);
								newLines++;
								if (value.contains("\n"))
									existingNewlines=true;
								else
									existingNewlines=false;
							}
						}
						//man kann auch über die länge arbeiten und so die column-breite automatisch anpassen, aber wir lassen es mal fix . . .
						//if(value.length()>=str_length){
						//	str_length = value.length();
						//}
						if(maxNewLines<=newLines){
							maxNewLines = newLines;
						}
						newLines=0;
					}
				}
				// hier wird die grösste variable als zeilenhöhe gesetz bei der aktuellen Spalte		
				jTableReactionsFrameReactionTable.setRowHeight(i,maxNewLines*18+18);
				// reset length
				//System.out.println("max:"+maxNewLines);
				maxNewLines = 0;
			

			//unregister this table at the tool tip manager to speed up table (mouse-over..)
			ToolTipManager.sharedInstance().unregisterComponent(jTableReactionsFrameReactionTable);
			ToolTipManager.sharedInstance().unregisterComponent(jTableReactionsFrameReactionTable.getTableHeader());

			//set renderer (colorizing red, newlines (/n) in Strings)
			TableCellRenderer renderer = new CustomTableCellRenderer(maxSpecies);
			try {
				jTableReactionsFrameReactionTable.setDefaultRenderer( Class.forName
				 ( "java.lang.Integer" ), renderer );
				jTableReactionsFrameReactionTable.setDefaultRenderer( Class.forName
				( "java.lang.String" ), renderer );
					     
			} catch (ClassNotFoundException e) {
				System.exit(0);
				e.printStackTrace();
			}

		}
		
		
		
		return jTableReactionsFrameReactionTable;
	}

	
	
	
	// STATIC MAIN
	
	/**
	 * This method initializes jPanelSettingsFrame1	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanelSettingsFrame1() {
		if (jPanelSettingsFrame1 == null) {
			jPanelSettingsFrame1 = new JPanel();
			jPanelSettingsFrame1.setLayout(null);
			jPanelSettingsFrame1.setBounds(new Rectangle(9, 10, 472, 65));
			jPanelSettingsFrame1.setBorder(BorderFactory.createTitledBorder(null, " General options ", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
			jPanelSettingsFrame1.setToolTipText("");
			jPanelSettingsFrame1.add(getJCheckBoxSettingsFrameForceAllReactionsAsEnzyeReaction(), null);
			jPanelSettingsFrame1.add(getJCheckBoxSettingsFrameWarnings(), null);
			jPanelSettingsFrame1.add(getJSpinnerSettingsFrameSpinWarnings(), null);
		}
		return jPanelSettingsFrame1;
	}

	/**
	 * This method initializes jPanelsettingsFrame2	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanelsettingsFrame2() {
		if (jPanelsettingsFrame2 == null) {
			jPanelsettingsFrame2 = new JPanel();
			jPanelsettingsFrame2.setLayout(null);
			jPanelsettingsFrame2.setBounds(new Rectangle(9, 75, 472, 65));
			jPanelsettingsFrame2.setBorder(BorderFactory.createTitledBorder(null, " Generate new kinetics ", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
			jPanelsettingsFrame2.add(getJRadioButtonSettingsFrameGenOnlyMissKin(), null);
			jPanelsettingsFrame2.add(getJRadioButtonSettingsFrameGenForAllReac(), null);
		}
		return jPanelsettingsFrame2;
	}

	/**
	 * This method initializes jPanelSettingsFrame3	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanelSettingsFrame3() {
		if (jPanelSettingsFrame3 == null) {
			jPanelSettingsFrame3 = new JPanel();
			jPanelSettingsFrame3.setLayout(null);
			jPanelSettingsFrame3.setBounds(new Rectangle(9, 140, 472, 65));
			jPanelSettingsFrame3.setBorder(BorderFactory.createTitledBorder(null, " Reversibility ", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
			jPanelSettingsFrame3.add(getJRadioButtonSettingsFrameForceReacRev(), null);
			jPanelSettingsFrame3.add(getJRadioButtonSettingsFrameForceRevAsCD(), null);
		}
		return jPanelSettingsFrame3;
	}

	/**
	 * This method initializes jPanelSettingdFrame4	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanelSettingdFrame4() {
		if (jPanelSettingdFrame4 == null) {
			jPanelSettingdFrame4 = new JPanel();
			jPanelSettingdFrame4.setLayout(null);
			jPanelSettingdFrame4.setBounds(new Rectangle(9, 205, 472, 65));
			jPanelSettingdFrame4.setBorder(BorderFactory.createTitledBorder(null, " Species, treated as enzymes ", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
			jPanelSettingdFrame4.add(getJCheckBoxSettingsFramePossibleEnzymeGenericProtein(), null);
			jPanelSettingdFrame4.add(getJCheckBoxSettingsFramePossibleEnzymeRNA(), null);
			jPanelSettingdFrame4.add(getJCheckBoxSettingsFramePossibleEnzymeComplex(), null);
			jPanelSettingdFrame4.add(getJCheckBoxSettingsFramePossibleEnzymeTruncatedProtein(), null);
			jPanelSettingdFrame4.add(getJCheckBoxSettingsFramePossibleEnzymeReceptor(), null);
			jPanelSettingdFrame4.add(getJCheckBoxSettingsFramePossibleEnzymeUnknown(), null);
			jPanelSettingdFrame4.add(getJCheckBoxSettingsFramePossibleEnzymeAsRNA(), null);
			jPanelSettingdFrame4.add(getJCheckBoxSettingsFramePossibleEnzymeSimpleMolecule(), null);
		}
		return jPanelSettingdFrame4;
	}

	/**
	 * This method initializes jPanelsettingsFrame5	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanelsettingsFrame5() {
		if (jPanelsettingsFrame5 == null) {
			jPanelsettingsFrame5 = new JPanel();
			jPanelsettingsFrame5.setLayout(null);
			jPanelsettingsFrame5.setBounds(new Rectangle(9, 270, 472, 230));
			jPanelsettingsFrame5.setBorder(BorderFactory.createTitledBorder(null, " Reaction mechanism ", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
			jPanelsettingsFrame5.add(getJPaneSettingsFrame6(), null);
			jPanelsettingsFrame5.add(getJPanelsettingsFrame10(), null);
			jPanelsettingsFrame5.add(getJPanelSettingsFrame8(), null);
			jPanelsettingsFrame5.add(getJPanelSettingsFrame9(), null);
			jPanelsettingsFrame5.add(getJPanelsettingsFrame11(), null);
		}
		return jPanelsettingsFrame5;
	}

	/**
	 * This method initializes jPaneSettingsFrame6	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPaneSettingsFrame6() {
		if (jPaneSettingsFrame6 == null) {
			jPaneSettingsFrame6 = new JPanel();
			jPaneSettingsFrame6.setLayout(null);
			jPaneSettingsFrame6.setBounds(new Rectangle(9, 20, 455, 45));
			jPaneSettingsFrame6.setBorder(BorderFactory.createTitledBorder(null, " Non Enzyme Reaction ", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
			jPaneSettingsFrame6.add(getJRadioButtonSettingsFrameMAK(), null);
		}
		return jPaneSettingsFrame6;
	}

	/**
	 * This method initializes jPanelSettingsFrame8	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanelSettingsFrame8() {
		if (jPanelSettingsFrame8 == null) {
			jPanelSettingsFrame8 = new JPanel();
			jPanelSettingsFrame8.setLayout(null);
			jPanelSettingsFrame8.setBorder(BorderFactory.createTitledBorder(null, " Uni-Uni Reaction ", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
			jPanelSettingsFrame8.setBounds(new Rectangle(9, 70, 225, 65));
			jPanelSettingsFrame8.add(getJRadioButtonSettingsFrameUniUniMMK(), null);
			jPanelSettingsFrame8.add(getJRadioButtonSettingsFrameUniUniCONV(), null);
		}
		return jPanelSettingsFrame8;
	}

	/**
	 * This method initializes jPanelSettingsFrame9	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanelSettingsFrame9() {
		if (jPanelSettingsFrame9 == null) {
			jPanelSettingsFrame9 = new JPanel();
			jPanelSettingsFrame9.setLayout(null);
			jPanelSettingsFrame9.setBorder(BorderFactory.createTitledBorder(null, " Bi-Bi Reaction ", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
			jPanelSettingsFrame9.setBounds(new Rectangle(240, 70, 225, 105));
			jPanelSettingsFrame9.add(getJRadioButtonSettingsFrameBiBiRND(), null);
			jPanelSettingsFrame9.add(getJRadioButtonSettingsFrameBiBiCONV(), null);
			jPanelSettingsFrame9.add(getJRadioButtonSettingsFrameBiBiORD(), null);
			jPanelSettingsFrame9.add(getJRadioButtonSettingsFrameBiBiPP(), null);
		}
		return jPanelSettingsFrame9;
	}

	/**
	 * This method initializes jPanelsettingsFrame10	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanelsettingsFrame10() {
		if (jPanelsettingsFrame10 == null) {
			jPanelsettingsFrame10 = new JPanel();
			jPanelsettingsFrame10.setLayout(null);
			jPanelsettingsFrame10.setBorder(BorderFactory.createTitledBorder(null, " Bi-Uni Reaction ", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
			jPanelsettingsFrame10.setBounds(new Rectangle(9, 140, 225, 85));
			jPanelsettingsFrame10.add(getJRadioButtonSettingsFrameBiUniORD(), null);
			jPanelsettingsFrame10.add(getJRadioButtonSettingsFrameBiUniCONV(), null);
			jPanelsettingsFrame10.add(getJRadioButtonSettingsFrameBiUniRND(), null);
		}
		return jPanelsettingsFrame10;
	}

	/**
	 * This method initializes jPanelsettingsFrame11	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanelsettingsFrame11() {
		if (jPanelsettingsFrame11 == null) {
			jPanelsettingsFrame11 = new JPanel();
			jPanelsettingsFrame11.setLayout(null);
			jPanelsettingsFrame11.setBorder(BorderFactory.createTitledBorder(null, " Other Enzym Reaction ", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
			jPanelsettingsFrame11.setBounds(new Rectangle(240, 180, 225, 45));
			jPanelsettingsFrame11.add(getJRadioButtonSettingsFrameOtherEnzymCONV(), null);
		}
		return jPanelsettingsFrame11;
	}

	public static void main(String[] args) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					SBMLsqueezerUI application = new SBMLsqueezerUI();
					application.getJFrameMainFrame().setVisible(true);
				}
			});
		}
	
}
