package org.sbml.squeezer.gui.wizard;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;

import org.sbml.squeezer.KineticLawGenerator;
import org.sbml.squeezer.gui.GUITools;

import de.zbit.gui.wizard.WizardPanelDescriptor;

public class KineticLawSelectionEquationProgressPanelDescriptor  extends WizardPanelDescriptor implements PropertyChangeListener {
	public static final String IDENTIFIER = "KINETIC_LAW_EQUATION_PROGRESS_PANEL";
	
	private KineticLawSelectionEquationProgressPanel panel;
	
	public KineticLawSelectionEquationProgressPanelDescriptor(KineticLawGenerator klg) {
		super(IDENTIFIER, new KineticLawSelectionEquationProgressPanel(klg));
		this.panel = ((KineticLawSelectionEquationProgressPanel) this.getPanelComponent());
		this.panel.addPropertyChangeListener(this);
	}
	
	public void displayingPanel() {
		new Thread(new Runnable() {
			public void run() {
				panel.generateKineticLaw();
			}
		}).start();
	}
	
	public void aboutToHidePanel() {
    }    

	public Object getNextPanelDescriptor() {
        return KineticLawSelectionEquationPanelDescriptor.IDENTIFIER;
    }
    
    public Object getBackPanelDescriptor() {
        return KineticLawSelectionOptionPanelDescriptor.IDENTIFIER;
    }

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals("generateKineticLawDone")){
			this.getWizard().goToNextPanel();
		}
	}
}
