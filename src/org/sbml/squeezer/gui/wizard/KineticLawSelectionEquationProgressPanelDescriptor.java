package org.sbml.squeezer.gui.wizard;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.sbml.squeezer.KineticLawGenerator;

import de.zbit.gui.wizard.WizardPanelDescriptor;

/**
 * 
 * @author Sebastian Nagel
 * @version $Rev$
 * @since 1.4
 */
public class KineticLawSelectionEquationProgressPanelDescriptor  extends WizardPanelDescriptor implements PropertyChangeListener {
	public static final String IDENTIFIER = "KINETIC_LAW_EQUATION_PROGRESS_PANEL";
	
	private KineticLawSelectionEquationProgressPanel panel;
	
	public KineticLawSelectionEquationProgressPanelDescriptor(KineticLawGenerator klg) {
		super(IDENTIFIER, new KineticLawSelectionEquationProgressPanel(klg));
		this.panel = ((KineticLawSelectionEquationProgressPanel) this.getPanelComponent());
		this.panel.addPropertyChangeListener(this);
	}
	
	/* (non-Javadoc)
	 * @see de.zbit.gui.wizard.WizardPanelDescriptor#displayingPanel()
	 */
	@Override
	public void displayingPanel() {
		this.getWizard().setNextFinishButtonEnabled(false);
		this.getWizard().setBackButtonEnabled(false);
		
		new Thread(new Runnable() {
			public void run() {
				panel.generateKineticLaw();
			}
		}).start();
	}
	
	/* (non-Javadoc)
	 * @see de.zbit.gui.wizard.WizardPanelDescriptor#aboutToHidePanel()
	 */
	@Override
	public void aboutToHidePanel() {
	}    
	
	/* (non-Javadoc)
	 * @see de.zbit.gui.wizard.WizardPanelDescriptor#getNextPanelDescriptor()
	 */
	@Override
	public Object getNextPanelDescriptor() {
		return KineticLawSelectionEquationPanelDescriptor.IDENTIFIER;
	}
	
	/*
 (non-Javadoc)
	 * @see de.zbit.gui.wizard.WizardPanelDescriptor#getBackPanelDescriptor()
	 */
	@Override
	public Object getBackPanelDescriptor() {
		return KineticLawSelectionOptionPanelDescriptor.IDENTIFIER;
	}
	
	/* (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	//@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals("generateKineticLawDone")){
			this.getWizard().goToNextPanel();
		}
	}
}
