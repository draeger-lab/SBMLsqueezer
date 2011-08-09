package org.sbml.squeezer;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.AlgebraicRule;
import org.sbml.jsbml.AssignmentRule;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.CompartmentType;
import org.sbml.jsbml.Constraint;
import org.sbml.jsbml.Event;
import org.sbml.jsbml.ExplicitRule;
import org.sbml.jsbml.FunctionDefinition;
import org.sbml.jsbml.InitialAssignment;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.RateRule;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Rule;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLOutputConverter;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesType;
import org.sbml.jsbml.UnitDefinition;
import org.sbml.jsbml.util.IOProgressListener;

public class SqSBMLWriter implements SBMLOutputConverter{

	private Set<IOProgressListener> setOfIOListeners = new HashSet<IOProgressListener>();
	private org.sbml.jsbml.Model originalModel;
	
	@Override
	public void addIOProgressListener(IOProgressListener listener) {
		setOfIOListeners.add(listener);
	}

	@Override
	public int getNumErrors(Object sbase) {
		return 0;
	}

	@Override
	public List<SBMLException> getWriteWarnings(Object sbase) {
		List<SBMLException> excl = new LinkedList<SBMLException>();
		return excl;
	}

	@Override
	public void removeUnneccessaryElements(Model model, Object orig) {
		// TODO Auto-generated method stub
		
	}
	/**
	 * 
	 * @param currObject
	 */
	private void fireIOEvent(Object currObject) {
		for (IOProgressListener iopl : setOfIOListeners) {
			iopl.ioProgressOn(currObject);
		}
	}
	@SuppressWarnings("deprecation")
	@Override
	public boolean saveChanges(Model model, Object object) throws SBMLException {
		if (!(object instanceof org.sbml.jsbml.Model))
			throw new IllegalArgumentException(
					"only instances of org.sbml.jsbml.Model can be considered.");
		originalModel = (org.sbml.jsbml.Model) object;

		// Function definitions
		FunctionDefinition fd1;
		for (FunctionDefinition fd2 : model.getListOfFunctionDefinitions()) {
			fd1 = originalModel.getFunctionDefinition(fd2.getId());
			if (fd1 == null) {
				// new function definition
				originalModel.addFunctionDefinition(fd2);
				fireIOEvent(fd2);
			} else if(!fd1.equals(fd2)){
				// changed function definition
				originalModel.removeFunctionDefinition(fd2.getId());
				originalModel.addFunctionDefinition(fd2);
				fireIOEvent(fd2);
			}
		}

		// Unit definitions
		UnitDefinition ud1;
		for (UnitDefinition ud2 : model.getListOfUnitDefinitions()){
			ud1 = originalModel.getUnitDefinition(ud2.getId());
			if(ud1 == null){
				// new unit definition
				originalModel.addUnitDefinition(ud2);
				fireIOEvent(ud2);
			} else if(!UnitDefinition.areIdentical(ud1, ud2)){
				// changed unit definition
				originalModel.removeUnitDefinition(ud1);
				originalModel.addUnitDefinition(ud2);
				fireIOEvent(ud2);
			}
		}
		
		// Compartment types
		CompartmentType ct1;
		for (CompartmentType ct2 : model.getListOfCompartmentTypes()) {
			ct1 = originalModel.getCompartmentType(ct2.getId());
			if (ct1 == null){
				originalModel.addCompartmentType(ct2);
				fireIOEvent(ct2);
			}else if(!ct1.equals(ct2)){
				originalModel.removeCompartmentType(ct1.getId());
				originalModel.addCompartmentType(ct2);
				fireIOEvent(ct2);
			}
		}

		// Species types
		SpeciesType st1;
		for (SpeciesType st2 : model.getListOfSpeciesTypes()) {
			st1 = originalModel.getSpeciesType(st2.getId());
			if (st1 == null) {
				originalModel.addSpeciesType(st2);
				fireIOEvent(st2);
			} else if(!st1.equals(st2)){
				originalModel.removeSpeciesType(st1.getId());
				originalModel.addSpeciesType(st2);
				fireIOEvent(st2);
			}
		}

		// Compartments
		Compartment cm1;
		for (Compartment cm2 : model.getListOfCompartments()) {
			cm1 = originalModel.getCompartment(cm2.getId());
			if (cm1 == null) {
				originalModel.addCompartment(cm2);
				fireIOEvent(cm2);
			} else if(!cm1.equals(cm2)){
				originalModel.removeCompartment(cm1.getId());
				originalModel.addCompartment(cm2);
				fireIOEvent(cm2);
			}
		}

		// Species
		Species sp1;
		for (Species sp2 : model.getListOfSpecies()) {
			sp1 = originalModel.getSpecies(sp2.getId());
			if (sp1 == null) {
				originalModel.addSpecies(sp2);
			} else if(!sp1.equals(sp2)){
				originalModel.removeSpecies(sp1);
				originalModel.addSpecies(sp2);
				fireIOEvent(sp2);
			}
		}

		// add or change parameters
		Parameter pa1;
		for (Parameter pa2 : model.getListOfParameters()) {
			pa1 = originalModel.getParameter(pa2.getId());
			if (pa1 == null) {
				originalModel.addParameter(pa2);
				fireIOEvent(pa2);
			} else if(!pa1.equals(pa2)){
				originalModel.removeParameter(pa1);
				originalModel.addParameter(pa2);
				fireIOEvent(pa2);
			}
		}

		// initial assignments
		InitialAssignment ia1;
		int contains;
		for (InitialAssignment ia2 : model.getListOfInitialAssignments()) {			
			contains = -1;			
			for (int i = 0; i < originalModel.getNumInitialAssignments() && contains < 0; i++) {
				ia1 = originalModel.getInitialAssignment(i);
				if (ia1.equals(ia2)){
					contains = i;
				}
			}
			if (contains < 0) {
				originalModel.addInitialAssignment(ia2);
				fireIOEvent(ia2);
			} else if(!originalModel.getInitialAssignment(contains).equals(ia2)){
				originalModel.removeInitialAssignment(contains);
				originalModel.addInitialAssignment(ia2);
				fireIOEvent(ia2);
			}			
		}	

		// rules
		Rule ru1;
		ExplicitRule eru1;
		ExplicitRule eru2;
		boolean equal = false;
		for (Rule ru2 : model.getListOfRules()) {	
			eru2 = ((ExplicitRule) ru2);			
			contains = -1;			
			for (int i = 0; i < originalModel.getNumRules() && contains < 0; i++) {
				ru1 = originalModel.getRule(i);
				eru1 = ((ExplicitRule) ru1);
				if ((ru2 instanceof AlgebraicRule) && (ru1 instanceof AlgebraicRule)) {
					equal = true;
				}else {
					if ((ru2 instanceof RateRule) && (ru1 instanceof org.sbml.jsbml.RateRule)) {
						equal = ((RateRule) ru2).getVariable().equals(
									((RateRule) ru1).getVariable());
					} else if ((ru2 instanceof AssignmentRule) && (ru1 instanceof org.sbml.jsbml.AssignmentRule)) {
						equal = ((AssignmentRule) ru2).getVariable().equals(
								((AssignmentRule) ru1).getVariable());
					}
					equal &= eru2.isSetUnits() && eru1.isSetUnits();
					if (equal && eru1.isSetUnits()) {
						equal &= eru1.getUnits().equals(((ExplicitRule) ru2).getUnits());
					}
				}
				if (equal) {
					equal &= eru2.getMath().equals(ru1.getMath());
				}
				if (equal) {
					contains = i;
				}
			}
			if (contains < 0) {
				originalModel.addRule(ru2);
				fireIOEvent(ru2);
			} else if(!originalModel.getRule(contains).equals(ru2)){
				originalModel.removeRule(contains);
				originalModel.addRule(ru2);
				fireIOEvent(ru2);
				
			}			
		}	

		// constraints
		Constraint cn1;
		for (Constraint cn2 : model.getListOfConstraints()) {
			contains = -1;
			for (int i = 0; i < originalModel.getNumConstraints() && contains < 0; i++) {
				cn1 = originalModel.getConstraint(i);
				if (cn2.getMath().equals(cn1.getMath()))
					contains = i;
			}
			if (contains < 0) {
				originalModel.addConstraint(cn2);
				fireIOEvent(cn2);
			} else {
				originalModel.removeConstraint(contains);
				originalModel.addConstraint(cn2);
				fireIOEvent(cn2);
			}
		}

		// add or change reactions
		Reaction re1;
		for (Reaction re2 : model.getListOfReactions()) {
			re1 = originalModel.getReaction(re2.getId());
			if (re1 == null) {
				originalModel.addReaction(re2);
				fireIOEvent(re2);
			} else if(!re1.equals(re2)){
				originalModel.removeReaction(re1.getId());
				originalModel.addReaction(re2);
				fireIOEvent(re2);
			}
		}

		// events
		Event ev1;
		for (Event ev2 : model.getListOfEvents()) {
			ev1 = originalModel.getEvent(ev2.getId());
			if (ev1 == null) {
				originalModel.addEvent(ev2);
				fireIOEvent(ev2);
			} else if(!ev1.equals(ev2)){
				originalModel.removeEvent(ev1.getId());
				originalModel.addEvent(ev2);
				fireIOEvent(ev2);
			}
		}
		
		removeUnneccessaryElements(model, object);
		fireIOEvent(null);
		//saveNamedSBaseProperties(model, originalModel);
		return true;
	}

	@Override
	public boolean saveChanges(Reaction reaction, Object model)
			throws SBMLException {
		boolean success = false;
		if (!(model instanceof org.sbml.jsbml.Model)){
			throw new IllegalArgumentException("model must be an instance of org.sbml.jsbml.Model");
		}
		// convert to Model
		org.sbml.jsbml.Model m = (org.sbml.jsbml.Model) model;
		// get old reaction
		Reaction old = m.getReaction(reaction.getId());
		// remove old reaction
		success = m.removeReaction(reaction);
		// if reaction was removed successfully, add the new reaction
		if(success){
			success = m.addReaction(reaction);
			if(!success){
				// add old reaction again
				m.addReaction(old);
			}
		}
		
		return true;
	}

	@Override
	public Object writeModel(Model model) throws SBMLException {
		// TODO Auto-generated method stub
		org.sbml.jsbml.Model m = new org.sbml.jsbml.Model(model);
		
		return m;
	}

	@Override
	public boolean writeSBML(Object sbmlDocument, String filename)
			throws SBMLException, IOException {
		return writeSBML(sbmlDocument, filename, null, null);
	}

	@Override
	public boolean writeSBML(Object object, String filename,
			String programName, String versionNumber) throws SBMLException,
			IOException {
		// check arguments
		if (!(object instanceof org.sbml.jsbml.SBMLDocument) 
				&& !(object instanceof org.sbml.jsbml.Model)){
			throw new IllegalArgumentException(
			"object must be an instance of org.sbml.jsbml.SBMLDocument or org.sbml.jsbml.Model");
		}
		// convert to SBML
		org.sbml.jsbml.SBMLDocument sbmlDocument;
		if (object instanceof org.sbml.jsbml.SBMLDocument)
			sbmlDocument = (org.sbml.jsbml.SBMLDocument) object;
		else
			sbmlDocument = ((org.sbml.jsbml.Model) object).getSBMLDocument();
		// write SBML to file
		boolean success = true; 
		try {
			org.sbml.jsbml.SBMLWriter.write(sbmlDocument, filename, programName, versionNumber);
		} catch (XMLStreamException e) {
			success = false;
		}
		return success;
	}

}
