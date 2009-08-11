package org.sbmlsqueezer.sbml;

import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public abstract class SBase {

	String metaid;
	String SBOTerm;
	private List<ChangeListener> listOfListeners;

	public SBase() {
	}

	public String getMetaid() {
		return metaid;
	}

	public void setMetaid(String metaid) {
		this.metaid = metaid;
		stateChanged();
	}

	public String getSBOTerm() {
		return SBOTerm;
	}

	public void setSBOTerm(String term) {
		SBOTerm = term;
		stateChanged();
	}



	public void stateChanged() {
		for (ChangeListener listener : listOfListeners)
			listener.stateChanged(new ChangeEvent(this));
	}

	public void addChangeListener(ChangeListener l) {
		listOfListeners.add(l);
	}

	public void removeChangeListener(ChangeListener l) {
		listOfListeners.remove(l);
	}
}
