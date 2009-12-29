/*
 *  SBMLsqueezer creates rate equations for reactions in SBML files
 *  (http://sbml.org).
 *  Copyright (C) 2009 ZBIT, University of Tübingen, Andreas Dräger
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.sbml.squeezer;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.sbml.jsbml.AssignmentRule;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.Constraint;
import org.sbml.jsbml.Event;
import org.sbml.jsbml.EventAssignment;
import org.sbml.jsbml.FunctionDefinition;
import org.sbml.jsbml.InitialAssignment;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.RateRule;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Rule;
import org.sbml.jsbml.SBO;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.Unit;
import org.sbml.jsbml.UnitDefinition;
import org.sbml.squeezer.kinetics.BasicKineticLaw;
import org.sbml.squeezer.math.GaussianRank;

/**
 * This class identifies and generates the missing kinetic laws for a the
 * selected model in the given plug-in.
 * 
 * @since 1.0
 * @version
 * @author <a href="mailto:Nadine.hassis@gmail.com">Nadine Hassis</a>
 * @author <a href="mailto:andreas.draeger@uni-tuebingen.de">Andreas
 *         Dr&auml;ger</a>
 * @date Aug 1, 2007
 */
public class KineticLawGenerator {

	/**
	 * The column rank of the soichiometric matrix of the original model.
	 */
	private int columnRank = -1;

	/**
	 * This list contains all fast reactions of the original model. Since the
	 * fast attribute is still ignored by SBMLsqueezer, we use this list to
	 * decide whether a warning message should be displayed later on.
	 */
	private List<Reaction> listOfFastReactions;

	/**
	 * A copy of the model that only covers all compartments, all parameters,
	 * and all reactions for which kinetic equations are to be created. Hence,
	 * it also contains all other information needed for that purpose, i.e., all
	 * species that participate as reactants, products, or modifiers in at least
	 * one of these reactions.
	 */
	private Model miniModel;

	/**
	 * 
	 */
	private Model modelOrig;

	/**
	 * A hashtable that contains all settings of how to create kinetic
	 * equations.
	 */
	private Properties settings;

	/**
	 * Takes a model and settings for kinetic law generation as input, creates a
	 * copy of the model, updates the enzymatic catalysis information using the
	 * given settings and creates kinetic laws for each reaction in the model
	 * copy. The original model remains unchanged.
	 * 
	 * @param model
	 * @param settings
	 * @throws Throwable
	 */
	public KineticLawGenerator(Model model, Properties settings)
			throws Throwable {
		this.settings = settings;
		this.modelOrig = model;
		init(null);
		generateLaws();
	}

	/**
	 * 
	 * @param model
	 * @param reactionID
	 * @param settings
	 */
	public KineticLawGenerator(Model model, String reactionID,
			Properties settings) {
		this.settings = settings;
		this.modelOrig = model;
		init(reactionID);
	}

	/**
	 * 
	 * @param compartment
	 */
	private void checkUnits(Compartment compartment) {
		Model model = compartment.getModel();
		if (!compartment.isSetSize())
			compartment.setSize(1d);
		if (!compartment.isSetUnits()) {
			switch (compartment.getSpatialDimensions()) {
			case 1:
				compartment.setUnits(model.getUnitDefinition("length"));
				break;
			case 2:
				compartment.setUnits(model.getUnitDefinition("area"));
				break;
			case 3:
				compartment.setUnits(model.getUnitDefinition("volume"));
				break;
			default:
				break;
			}
		}
		if (compartment.getSpatialDimensions() == 0
				|| compartment.getSpatialDimensions() > 3) {
			compartment.setSpatialDimensions((short) 3);
			compartment.setUnits(model.getUnitDefinition("volume"));
			System.err
					.printf(
							"Compartment %s had an invalid spacial dimension and was therefore set to a volume.\n",
							compartment.getId());
		}
		if (model.getUnitDefinition(compartment.getUnits()) == null)
			model.addUnitDefinition(compartment.getUnitsInstance());
	}

	/**
	 * check units of species and surrounding compartment.
	 * 
	 * @param species
	 */
	private void checkUnits(Species species) {
		if (!species.isSetSubstanceUnits()
				|| !species.getSubstanceUnitsInstance().isVariantOfSubstance())
			species.setSubstanceUnits(species.getModel().getUnitDefinition(
					"substance"));
	}

	/**
	 * Creates a kinetic law for the given reaction, which can be assigned to
	 * the given reaction.
	 * 
	 * @param r
	 *            The reaction for which a kinetic law is to be created.
	 * @param reversibility
	 *            If true this reaction will be set to reversible and the
	 *            kinetic equation will be created accordingly. If this
	 *            parameter is false, the reversibility property of this
	 *            reaction will not be chaged.
	 * @param kinetic
	 *            an element from the Kinetics enum.
	 * @return A kinetic law for the given reaction.
	 * @throws Throwable
	 */
	public BasicKineticLaw createKineticLaw(Reaction r,
			String kineticsClassName, boolean reversibility) throws Throwable {
		Reaction reaction = miniModel.getReaction(r.getId());
		if (reaction == null)
			reaction = r;
		reaction.setReversible(reversibility || reaction.getReversible());
		try {
			if (!kineticsClassName.startsWith(SBMLsqueezer.KINETICS_PACKAGE))
				kineticsClassName = SBMLsqueezer.KINETICS_PACKAGE + '.'
						+ kineticsClassName;
			Class<?> kinCls = Class.forName(kineticsClassName);
			Object typeParameters[] = new Object[] {
					settings.get(CfgKeys.TYPE_STANDARD_VERSION),
					Boolean.valueOf(hasFullColumnRank(modelOrig)),
					settings.get(CfgKeys.TYPE_UNIT_CONSISTENCY),
					settings.get(CfgKeys.OPT_DEFAULT_VALUE_OF_NEW_PARAMETERS) };
			Constructor<?> constructor = kinCls.getConstructor(reaction
					.getClass(), typeParameters.getClass());
			return (BasicKineticLaw) constructor.newInstance(reaction,
					typeParameters);
		} catch (InstantiationException e) {
			e.printStackTrace();
			throw e.getCause();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			throw e.getCause();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			throw e.getCause();
		}
	}

	/**
	 * Creates a minimal copy of the original model that only covers those
	 * elements needed for the creation of rate equations.
	 * 
	 * @param reactionID
	 * 
	 * @return
	 */
	private Model createMinimalModel(String reactionID) {
		Model miniModel = new Model(modelOrig.getId(), modelOrig.getLevel(),
				modelOrig.getVersion());
		boolean create = ((Boolean) settings
				.get(CfgKeys.OPT_GENERATE_KINETIC_LAW_FOR_EACH_REACTION))
				.booleanValue();
		/*
		 * Copy needed species and reactions.
		 */
		for (Reaction reacOrig : modelOrig.getListOfReactions()) {
			/*
			 * Let us find all fast reactions. This feature is currently
			 * ignored.
			 */
			if (reacOrig.getFast())
				listOfFastReactions.add(reacOrig);
			if (reactionID != null) {
				if (!reacOrig.getId().equals(reactionID))
					continue;
				else
					create = true;
			}
			if (reacOrig.isSetKineticLaw()) {
				String formula = reacOrig.getKineticLaw().getFormula();
				if (formula.equals("") || formula.equals(" ")) {
					System.err
							.printf(
									"Reaction %s in the model has an incorrect format. This means there is either an empty kinetic law in this reaction or a kinetic law that only consists of a white space. If you decide not to save this generated model, there is only one solution: open this SBML file in an editor and delete the whole kinetic law. SBMLsqueezer ignores this misstake and generates a proper equation. Therfore we recomment that you save this generated model.\n",
									reacOrig.getId());
					create = true;
				}
			}
			if (!reacOrig.isSetKineticLaw() || create) {
				Reaction reac = new Reaction(reacOrig.getId(), reacOrig
						.getLevel(), reacOrig.getVersion());
				if (reacOrig.isSetSBOTerm())
					reac.setSBOTerm(reacOrig.getSBOTerm());
				miniModel.addReaction(reac);
				reac.setFast(reacOrig.getFast());
				reac.setReversible(reacOrig.getReversible());
				for (SpeciesReference specRefOrig : reacOrig
						.getListOfReactants()) {
					Species speciesOrig = specRefOrig.getSpeciesInstance();
					SpeciesReference sr = specRefOrig.clone();
					sr.setSpecies(copySpecies(speciesOrig, miniModel));
					reac.addReactant(sr);
				}
				for (SpeciesReference s : reacOrig.getListOfProducts()) {
					Species speciesOrig = s.getSpeciesInstance();
					SpeciesReference sr = s.clone();
					sr.setSpecies(copySpecies(speciesOrig, miniModel));
					reac.addProduct(sr);
				}
				for (ModifierSpeciesReference s : reacOrig.getListOfModifiers()) {
					Species speciesOrig = s.getSpeciesInstance();
					ModifierSpeciesReference sr = s.clone();
					sr.setSpecies(copySpecies(speciesOrig, miniModel));
					reac.addModifier(sr);
				}
				/*
				 * This will be over written later on anyway but ignoring it
				 * would be confusing for users...
				 */
				if (reacOrig.isSetKineticLaw()) {
					KineticLaw l = reacOrig.getKineticLaw();
					if (l.isSetMath())
						for (Parameter parameter : modelOrig
								.getListOfParameters())
							if (l.getMath().refersTo(parameter.getId()))
								miniModel.addParameter(parameter.clone());
					reac.setKineticLaw(l.clone());
				}
			}
		}
		UnitDefinition ud = miniModel.getUnitDefinition("substance");
		Unit u = ud.getListOfUnits().getFirst();
		if (u.isMole()) {
			u.setScale(-3);
			ud.setName("mmole");
		}
		ud = miniModel.getUnitDefinition("volume");
		u = ud.getListOfUnits().getFirst();
		if (u.isLitre()) {
			u.setScale(-3);
			ud.setName("ml");
		}
		return miniModel;
	}

	/**
	 * 
	 * @param speciesOrig
	 * @param miniModel
	 * @return
	 */
	private Species copySpecies(Species speciesOrig, Model miniModel) {
		if (speciesOrig.isSetCompartment())
			copyCopmpartment(speciesOrig.getCompartmentInstance(), miniModel);
		if (miniModel.getSpecies(speciesOrig.getId()) == null)
			miniModel.addSpecies(speciesOrig.clone());
		Species spec = miniModel.getSpecies(speciesOrig.getId());
		spec.setCompartment(miniModel.getCompartment(speciesOrig
				.getCompartment()));
		if (speciesOrig.isSetSubstanceUnits()) {
			if (miniModel.getUnitDefinition(speciesOrig.getSubstanceUnits()) == null)
				miniModel.addUnitDefinition(speciesOrig
						.getSubstanceUnitsInstance().clone());
			spec.setSubstanceUnits(miniModel.getUnitDefinition(speciesOrig
					.getSubstanceUnits()));
		} else
			checkUnits(spec);
		return spec;
	}

	/**
	 * 
	 * @param compartmenOrig
	 * @param miniModel
	 * @return
	 */
	private Compartment copyCopmpartment(Compartment compartmenOrig,
			Model miniModel) {
		if (miniModel.getCompartment(compartmenOrig.getId()) == null)
			miniModel.addCompartment(compartmenOrig.clone());
		Compartment compartment = miniModel.getCompartment(compartmenOrig
				.getId());
		if (compartment.isSetUnits()) {
			if (miniModel.getUnitDefinition(compartment.getUnits()) == null)
				miniModel.addUnitDefinition(compartment.getUnitsInstance()
						.clone());
			compartment.setUnits(miniModel.getUnitDefinition(compartment
					.getUnits()));
		} else
			checkUnits(compartment);
		return compartment;
	}

	/**
	 * @throws Throwable
	 */
	private void generateLaws() throws Throwable {
		boolean reversibility = ((Boolean) settings
				.get(CfgKeys.OPT_TREAT_ALL_REACTIONS_REVERSIBLE))
				.booleanValue();
		for (Reaction r : miniModel.getListOfReactions()) {
			ReactionType rt = new ReactionType(r, settings);
			createKineticLaw(r, rt.identifyPossibleKineticLaw(), reversibility);
		}
	}

	/**
	 * Returns all reactions of the model that have the attribute to be fast.
	 * 
	 * @return Returns all reactions of the model that have the attribute to be
	 *         fast.
	 */
	public List<Reaction> getFastReactions() {
		return listOfFastReactions;
	}

	/**
	 * 
	 * @return
	 */
	public Model getModel() {
		return modelOrig;
	}

	/**
	 * 
	 * @param i
	 * @return
	 */
	public Reaction getModifiedReaction(int i) {
		return miniModel.getReaction(i);
	}

	/**
	 * 
	 * @param reactionID
	 * @return
	 */
	public Reaction getModifiedReaction(String reactionID) {
		return miniModel.getReaction(reactionID);
	}

	/**
	 * 
	 * @return
	 */
	public int getNumCreatedKinetics() {
		return miniModel.getNumReactions();
	}

	/**
	 * 
	 * @return
	 */
	public Set<Integer> getPossibleEnzymes() {
		Set<Integer> possibleEnzymes = new HashSet<Integer>();
		String prefix = "POSSIBLE_ENZYME_", k;
		for (Object key : settings.keySet()) {
			k = key.toString();
			if (k.startsWith(prefix))
				possibleEnzymes.add(Integer.valueOf(SBO.convertAlias2SBO(k
						.substring(prefix.length()))));
		}
		return possibleEnzymes;
	}

	/**
	 * 
	 * @param reactionID
	 * @return
	 * @throws RateLawNotApplicableException
	 */
	public ReactionType getReactionType(String reactionID)
			throws RateLawNotApplicableException {
		return new ReactionType(miniModel.getReaction(reactionID), settings);
	}

	/**
	 * 
	 * @return
	 */
	public Properties getSettings() {
		return settings;
	}

	/**
	 * Returns true if the given model's stoichiometric matrix has full column
	 * rank.
	 * 
	 * @param model
	 * @return
	 */
	private boolean hasFullColumnRank(Model model) {
		boolean fullRank = false;
		if ((model.getNumSpecies() >= model.getNumReactions())
				&& (columnRank == -1)) {
			GaussianRank gaussian = new GaussianRank(stoechMatrix(model));
			columnRank = gaussian.getColumnRank();
			fullRank = gaussian.hasFullRank();
		} else if (columnRank == model.getNumReactions())
			fullRank = true;
		return fullRank;
	}

	/**
	 * load default settings and initialize this object.
	 * 
	 * @param reactionID
	 */
	private void init(String reactionID) {
		if (settings == null)
			settings = SBMLsqueezer.getDefaultSettings();
		listOfFastReactions = new LinkedList<Reaction>();
		this.miniModel = createMinimalModel(reactionID);
		updateEnzymeCatalysis(getPossibleEnzymes());
	}

	/**
	 * Sets the initial amounts of all modifiers, reactants and products to the
	 * specified value.
	 * 
	 * @param reaction
	 * @param initialValue
	 * @param sizeValue
	 */
	private void setInitialConcentrationTo(Reaction reaction,
			double initialValue, double sizeValue) {
		Species species;
		for (int reactant = 0; reactant < reaction.getNumReactants(); reactant++) {
			species = reaction.getReactant(reactant).getSpeciesInstance();
			initializeSpeciesAndCompartmentIfNecessary(species, initialValue,
					species.getCompartmentInstance(), sizeValue);
		}
		for (int product = 0; product < reaction.getNumProducts(); product++) {
			species = reaction.getProduct(product).getSpeciesInstance();
			initializeSpeciesAndCompartmentIfNecessary(species, initialValue,
					species.getCompartmentInstance(), sizeValue);
		}
		for (int modifier = 0; modifier < reaction.getNumModifiers(); modifier++) {
			species = reaction.getModifier(modifier).getSpeciesInstance();
			initializeSpeciesAndCompartmentIfNecessary(species, initialValue,
					species.getCompartmentInstance(), sizeValue);
		}
	}

	/**
	 * 
	 * @param species
	 * @param initialValue
	 * @param compartmentInstance
	 * @param sizeValue
	 */
	private void initializeSpeciesAndCompartmentIfNecessary(Species species,
			double initialValue, Compartment compartment, double sizeValue) {
		if (!species.isSetInitialAmount()
				&& !species.isSetInitialConcentration()) {
			if (species.getHasOnlySubstanceUnits())
				species.setInitialAmount(initialValue);
			else
				species.setInitialConcentration(initialValue);
		}
		if (compartment != null && !compartment.isSetSize())
			compartment.setSize(sizeValue);
	}

	/**
	 * Delete unnecessary paremeters from the model. A parameter is defined to
	 * be unnecessary if and only if no kinetic law, no event assignment, no
	 * rule and no function makes use of this parameter.
	 * 
	 * @param selectedModel
	 */
	private void removeUnnecessaryParameters(Model model, LawListener l) {
		boolean isNeeded;
		int i, j, k, num = 0;
		Parameter p;
		// remove unnecessary global parameters
		l.initLawListener(Parameter.class.getSimpleName(), model
				.getNumParameters());
		for (i = model.getNumParameters() - 1; i >= 0; i--) {
			isNeeded = false;
			p = model.getParameter(i);
			/*
			 * Is this parameter necessary for some kinetic law or is this
			 * parameter a conversion factor in some stoichiometric math?
			 */
			for (j = 0; (j < model.getNumReactions()) && !isNeeded; j++) {
				Reaction r = model.getReaction(j);
				if (r.isSetKineticLaw() && r.getKineticLaw().isSetMath()
						&& r.getKineticLaw().getMath().refersTo(p.getId())) {
					/*
					 * ok, parameter occurs here but there could also be a local
					 * parameter with the same id.
					 */
					if (r.getKineticLaw().getParameter(p.getId()) == null)
						isNeeded = true;
				}
				if (isNeeded)
					break;
				SpeciesReference specRef;
				for (k = 0; k < r.getNumReactants(); k++) {
					specRef = r.getReactant(k);
					if (specRef.isSetStoichiometryMath()
							&& specRef.getStoichiometryMath().isSetMath()
							&& specRef.getStoichiometryMath().getMath()
									.refersTo(p.getId()))
						isNeeded = true;
				}
				if (isNeeded)
					break;
				for (k = 0; k < r.getNumProducts(); k++) {
					specRef = r.getProduct(k);
					if (specRef.isSetStoichiometryMath()
							&& specRef.getStoichiometryMath().isSetMath()
							&& specRef.getStoichiometryMath().getMath()
									.refersTo(p.getId()))
						isNeeded = true;
				}
			}

			// is this parameter necessary for some rule?
			for (j = 0; (j < model.getNumRules()) && !isNeeded; j++) {
				Rule r = model.getRule(j);
				if (r instanceof AssignmentRule
						&& ((AssignmentRule) r).getVariable().equals(p.getId()))
					isNeeded = true;
				if (r instanceof RateRule
						&& ((RateRule) r).getVariable().equals(p.getId()))
					isNeeded = true;
				if (r.isSetMath() && r.getMath().refersTo(p.getId()))
					isNeeded = true;
			}

			// is this parameter necessary for some event?
			for (j = 0; (j < model.getNumEvents()) && !isNeeded; j++) {
				Event e = model.getEvent(j);
				if (e.isSetTrigger() && e.getTrigger().isSetMath()
						&& e.getTrigger().getMath().refersTo(p.getId()))
					isNeeded = true;
				if (e.isSetDelay() && e.getDelay().isSetMath()
						&& e.getDelay().getMath().refersTo(p.getId()))
					isNeeded = true;
				for (k = 0; k < model.getEvent(j).getNumEventAssignments()
						&& !isNeeded; k++) {
					EventAssignment ea = e.getEventAssignment(k);
					if ((ea.isSetVariable() && ea.getVariable().equals(
							p.getId()))
							|| ea.isSetMath()
							&& ea.getMath().refersTo(p.getId()))
						isNeeded = true;
				}
			}

			// is this parameter necessary for some function?
			for (j = 0; j < model.getNumFunctionDefinitions() && !isNeeded; j++) {
				FunctionDefinition fd = model.getFunctionDefinition(j);
				if (fd.isSetMath() && fd.getMath().refersTo(p.getId()))
					isNeeded = true;
			}

			// is this parameter necessary for some initial assignment?
			for (j = 0; j < model.getNumInitialAssignments() && !isNeeded; j++) {
				InitialAssignment ia = model.getInitialAssignment(j);
				if ((ia.isSetSymbol() && ia.getSymbol().equals(p.getId()))
						|| ia.isSetMath() && ia.getMath().refersTo(p.getId()))
					isNeeded = true;
			}

			// is this parameter necessary for some constraint?
			for (j = 0; j < model.getNumConstraints() && !isNeeded; j++) {
				Constraint c = model.getConstraint(j);
				if (c.isSetMath() && c.getMath().refersTo(p.getId()))
					isNeeded = true;
			}

			if (!isNeeded) // is this parameter necessary at all?
				model.getListOfParameters().remove(i);
			l.currentState(p, ++num);
		}
		// remove unnecessary local parameters
		num = 0;
		l.initLawListener(Reaction.class.getSimpleName(), model
				.getNumReactions());
		for (i = 0; i < model.getNumReactions(); i++) {
			Reaction r = model.getReaction(i);
			if (r.isSetKineticLaw()) {
				KineticLaw law = r.getKineticLaw();
				for (j = law.getNumParameters() - 1; j >= 0; j--) {
					if (law.isSetMath()
							&& !law.getMath().refersTo(
									law.getParameter(j).getId()))
						law.removeParameter(j);
				}
			}
			l.currentState(r, ++num);
		}
	}

	/**
	 * set the boundaryCondition for a gen to the given value
	 * 
	 * @param species
	 * @param condition
	 */
	private void setBoundaryCondition(Species species, boolean condition) {
		if (condition != species.getBoundaryCondition()) {
			species.setBoundaryCondition(condition);
			species.stateChanged();
		}
	}

	/**
	 * Sets the reaction with the given id in the minimal model copy to the
	 * given reversibility value.
	 * 
	 * @param reactionID
	 * @param reversible
	 */
	public void setReversible(String reactionID, boolean reversible) {
		Reaction r = miniModel.getReaction(reactionID);
		if (r != null)
			r.setReversible(reversible);
		else
			throw new IllegalArgumentException(
					reactionID
							+ " is not the id of a reaction for which rate laws are to be created.");
	}

	/**
	 * Computes the stoichiometric matrix of the model system.
	 * 
	 * @return
	 */
	public double[][] stoechMatrix(Model model) {
		double[][] N = new double[model.getNumSpecies()][model
				.getNumReactions()];
		int reactionNum, speciesNum;
		SpeciesReference speciesRef;
		HashMap<String, Integer> speciesIDandNum = new HashMap<String, Integer>();
		int i = 0;
		for (Species s : model.getListOfSpecies())
			speciesIDandNum.put(s.getId(), Integer.valueOf(i++));
		for (reactionNum = 0; reactionNum < model.getNumReactions(); reactionNum++) {
			Reaction reaction = model.getReaction(reactionNum);
			for (speciesNum = 0; speciesNum < reaction.getNumReactants(); speciesNum++) {
				speciesRef = reaction.getReactant(speciesNum);
				N[speciesIDandNum.get(speciesRef.getSpecies())][reactionNum] = -speciesRef
						.getStoichiometry();
			}
			for (speciesNum = 0; speciesNum < reaction.getNumProducts(); speciesNum++) {
				speciesRef = reaction.getProduct(speciesNum);
				N[speciesIDandNum.get(speciesRef.getSpecies())][reactionNum] = speciesRef
						.getStoichiometry();
			}
		}
		return N;
	}

	/**
	 * This method stores a kinetic law for the given reaction in the currently
	 * selected model given by the plugin. The kinetic law is passed as a String
	 * to this method. A boolean variable tells this method weather the formula
	 * is for a reversible or for an irreversible reaction. Afterwards all
	 * parameters within this kinetic law are also stored in the given model.
	 * There is no need to call the storeParameters method.
	 * 
	 * @param kineticLaw
	 *            A string with the formula to be assigned to the given
	 *            reaction.
	 * @param l
	 */
	public Reaction storeKineticLaw(KineticLaw kineticLaw, LawListener l) {
		return storeKineticLaw(kineticLaw, true, l);
	}

	/**
	 * 
	 * @param kineticLaw
	 * @param removeParametersAndStoreUnits
	 * @param l
	 * @return
	 */
	private Reaction storeKineticLaw(KineticLaw kineticLaw,
			boolean removeParametersAndStoreUnits, LawListener l) {
		int i;
		boolean reversibility = ((Boolean) settings
				.get(CfgKeys.OPT_TREAT_ALL_REACTIONS_REVERSIBLE))
				.booleanValue();
		Reaction reaction = modelOrig.getReaction(kineticLaw
				.getParentSBMLObject().getId());
		reaction.setReversible(reversibility || reaction.getReversible());
		reaction.setKineticLaw(kineticLaw);
		// set the BoundaryCondition to true for Genes if not set anyway:
		boolean setBoundary = ((Boolean) settings
				.get(CfgKeys.OPT_SET_BOUNDARY_CONDITION_FOR_GENES))
				.booleanValue();
		for (i = 0; i < reaction.getNumReactants(); i++) {
			Species species = reaction.getReactant(i).getSpeciesInstance();
			if (SBO.isGeneOrGeneCodingRegion(species.getSBOTerm())
					&& setBoundary)
				setBoundaryCondition(species, true);
		}
		for (i = 0; i < reaction.getNumProducts(); i++) {
			Species species = reaction.getProduct(i).getSpeciesInstance();
			if (SBO.isGeneOrGeneCodingRegion(species.getSBOTerm())
					&& setBoundary)
				setBoundaryCondition(species, true);
		}
		setInitialConcentrationTo(reaction, Double.parseDouble(settings.get(
				CfgKeys.OPT_DEFAULT_SPECIES_INITIAL_VALUE).toString()), Double
				.parseDouble(settings.get(
						CfgKeys.OPT_DEFAULT_COMPARTMENT_INITIAL_SIZE)
						.toString()));
		if (removeParametersAndStoreUnits) {
			storeUnits(l);
			if (((Boolean) settings
					.get(CfgKeys.OPT_REMOVE_UNNECESSARY_PARAMETERS_AND_UNITS))
					.booleanValue())
				removeUnnecessaryParameters(modelOrig, l);
		}
		storeParamters(reaction, l);
		if (removeParametersAndStoreUnits
				&& ((Boolean) settings
						.get(CfgKeys.OPT_REMOVE_UNNECESSARY_PARAMETERS_AND_UNITS))
						.booleanValue()) {
			/*
			 * delete unnecessary units.
			 */
			int num = 0;
			l.initLawListener(UnitDefinition.class.getSimpleName(), modelOrig
					.getNumUnitDefinitions());
			for (int j = modelOrig.getNumUnitDefinitions() - 1; j >= 0; j--) {
				UnitDefinition udef = modelOrig.getUnitDefinition(j);
				boolean isNeeded = Unit
						.isBuiltIn(udef.getId(), udef.getLevel());
				for (i = 0; i < modelOrig.getNumCompartments() && !isNeeded; i++) {
					Compartment c = modelOrig.getCompartment(i);
					if (c.isSetUnits() && c.getUnits().equals(udef.getId()))
						isNeeded = true;
				}
				for (i = 0; i < modelOrig.getNumSpecies() && !isNeeded; i++) {
					Species s = modelOrig.getSpecies(i);
					if (s.isSetSubstanceUnits()
							&& s.getSubstanceUnits().equals(udef.getId()))
						isNeeded = true;
				}
				for (i = 0; i < modelOrig.getNumParameters() && !isNeeded; i++) {
					Parameter p = modelOrig.getParameter(i);
					if (p.isSetUnits() && p.getUnits().equals(udef.getId()))
						isNeeded = true;
				}
				for (i = 0; i < modelOrig.getNumReactions() && !isNeeded; i++) {
					Reaction r = modelOrig.getReaction(i);
					if (r.isSetKineticLaw())
						for (Parameter p : r.getKineticLaw()
								.getListOfParameters()) {
							if (p.isSetUnits()
									&& p.getUnits().equals(udef.getId()))
								isNeeded = true;
						}
				}
				if (!isNeeded)
					modelOrig.removeUnitDefinition(udef);
				l.currentState(udef, ++num);
			}
		}
		return reaction;
	}

	/**
	 * store the generated Kinetics in SBML-File as MathML.
	 */
	public void storeKineticLaws(LawListener l) {
		l.initLawListener(Reaction.class.getSimpleName(), miniModel
				.getNumReactions());
		for (int i = 0; i < miniModel.getNumReactions(); i++) {
			Reaction r = miniModel.getReaction(i);
			storeKineticLaw(r.getKineticLaw(), false, l);
			l.currentState(r, i);
		}
		storeUnits(l);
		if (((Boolean) settings
				.get(CfgKeys.OPT_REMOVE_UNNECESSARY_PARAMETERS_AND_UNITS))
				.booleanValue())
			removeUnnecessaryParameters(modelOrig, l);
	}

	/**
	 * This method stores all newly created parameters in the model and sets the
	 * references to the units appropriately. UnitDefinitions that are not
	 * referenced by any object are removed from the model.
	 * 
	 * @param reaction
	 */
	private void storeParamters(Reaction reaction, LawListener l) {
		// setInitialConcentrationTo(reaction, 1d);
		KineticLaw kineticLaw = reaction.getKineticLaw();
		ListOf<Parameter> paramListLocal = kineticLaw.getListOfParameters();
		boolean addGlobally = ((Boolean) settings
				.get(CfgKeys.OPT_ADD_NEW_PARAMETERS_ALWAYS_GLOBALLY))
				.booleanValue();
		Parameter p;
		for (int paramNum = paramListLocal.size() - 1; paramNum >= 0; paramNum--) {
			if (addGlobally) {
				p = paramListLocal.remove(paramNum);
				modelOrig.addParameter(p);
			} else
				p = paramListLocal.get(paramNum);
			updateUnitReferences(p);
		}
		for (Parameter parameter : miniModel.getListOfParameters())
			if (modelOrig.getParameter(parameter.getId()) == null) {
				modelOrig.addParameter(parameter);
				updateUnitReferences(parameter);
			}
	}

	/**
	 * Moves the pointers from the units in the mini Model to the units in the
	 * original model.
	 * 
	 * @param p
	 */
	private void updateUnitReferences(Parameter p) {
		if (p.isSetUnits()) {
			String units = p.getUnits();
			if (Unit.isUnitKind(units, p.getLevel(), p.getVersion()))
				p.setUnits(Unit.Kind.valueOf(units));
			else {
				UnitDefinition ud = modelOrig.getUnitDefinition(p.getUnits());
				if (ud == null) {
					ud = miniModel.getUnitDefinition(p.getUnits()).clone();
					modelOrig.addUnitDefinition(ud);
				}
				p.setUnits(ud);
			}
		}
	}

	/**
	 * Stores all units created in the mini model in the original model.
	 * 
	 * @param l
	 */
	private void storeUnits(LawListener l) {
		int num = 0;
		l.initLawListener(UnitDefinition.class.getSimpleName(), miniModel
				.getNumUnitDefinitions());
		for (UnitDefinition ud : miniModel.getListOfUnitDefinitions()) {
			if (modelOrig.getUnitDefinition(ud.getId()) == null)
				modelOrig.addUnitDefinition(ud.clone());
			else {
				UnitDefinition orig = modelOrig.getUnitDefinition(ud.getId());
				orig.setListOfUnits(ud.getListOfUnits());
				orig.simplify();
				if (ud.isSetName())
					orig.setName(ud.getName());
			}
			l.currentState(ud, ++num);
		}
		num = 0;
		l.initLawListener(Compartment.class.getSimpleName(), miniModel
				.getNumCompartments());
		for (Compartment c : miniModel.getListOfCompartments()) {
			Compartment corig = modelOrig.getCompartment(c.getId());
			corig.setSpatialDimensions(c.getSpatialDimensions());
			if (c.isSetUnits()) {
				if (Unit.isUnitKind(c.getUnits(), c.getLevel(), c.getVersion()))
					corig.setUnits(Unit.Kind.valueOf(c.getUnits()));
				else
					corig.setUnits(modelOrig.getUnitDefinition(c.getUnits()));
			}
			checkUnits(corig);
			l.currentState(c, ++num);
		}
		num = 0;
		l.initLawListener(Species.class.getSimpleName(), miniModel
				.getNumSpecies());
		for (Species s : miniModel.getListOfSpecies()) {
			Species sorig = modelOrig.getSpecies(s.getId());
			sorig.setHasOnlySubstanceUnits(s.getHasOnlySubstanceUnits());
			if (s.isSetSubstanceUnits()) {
				if (Unit.isUnitKind(s.getSubstanceUnits(), s.getLevel(), s
						.getVersion()))
					sorig.setSubstanceUnits(Unit.Kind.valueOf(s
							.getSubstanceUnits()));
				else
					sorig.setSubstanceUnits(modelOrig.getUnitDefinition(s
							.getSubstanceUnits()));
			}
			checkUnits(sorig);
			l.currentState(s, ++num);
		}
	}

	/**
	 * Sets the SBO annotation of modifiers to more precise values in the local
	 * mini copy of the model.
	 * 
	 * @param possibleEnzymes
	 */
	private void updateEnzymeCatalysis(Set<Integer> possibleEnzymes) {
		for (Reaction r : miniModel.getListOfReactions()) {
			for (ModifierSpeciesReference modifier : r.getListOfModifiers()) {
				Species species = modifier.getSpeciesInstance();
				if (SBO.isEnzymaticCatalysis(modifier.getSBOTerm())
						&& species.isSetSBOTerm()
						&& !possibleEnzymes.contains(Integer.valueOf(species
								.getSBOTerm())))
					modifier.setSBOTerm(SBO.getCatalysis());
				else if (SBO.isCatalyst(modifier.getSBOTerm())
						&& (possibleEnzymes.contains(Integer.valueOf(species
								.getSBOTerm())) || !species.isSetSBOTerm()))
					modifier.setSBOTerm(SBO.getEnzymaticCatalysis());
			}
		}
	}

	/**
	 * Returns the copy of the model that contains only those reactions together
	 * with all required species, compartments, species- and compartment types,
	 * and units for which kinetic equations are to be created.
	 * 
	 * @return
	 */
	public Model getMiniModel() {
		return miniModel;
	}
}
