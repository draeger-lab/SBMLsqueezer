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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IllegalFormatException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.AssignmentRule;
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
import org.sbml.jsbml.UnitDefinition;
import org.sbml.squeezer.kinetics.BasicKineticLaw;
import org.sbml.squeezer.kinetics.IrrevNonModulatedNonInteractingEnzymes;
import org.sbml.squeezer.math.GaussianRank;

/**
 * This class identifies and generates the missing kinetic laws for a the
 * selected model in the given plug-in.
 * 
 * @since 1.0
 * @version
 * @author <a href="mailto:Nadine.hassis@gmail.com">Nadine Hassis</a>
 * @author <a href="mailto:andreas.draeger@uni-tuebingen.de">Andreas Dr&auml;ger</a>
 * @date Aug 1, 2007
 */
public class KineticLawGenerator {

	/**
	 * Goes through the formula and identifies all global parameters that are
	 * referenced by this rate equation.
	 * 
	 * @param math
	 * @return
	 */
	public static List<Parameter> findReferencedGlobalParameters(ASTNode math) {
		LinkedList<Parameter> pList = new LinkedList<Parameter>();
		if (math.getType().equals(ASTNode.Type.NAME)
				&& (math.getVariable() instanceof Parameter)
				&& (math.getParentSBMLObject().getModel().getParameter(
						math.getVariable().getId()) != null))
			pList.add((Parameter) math.getVariable());
		for (ASTNode child : math.getListOfNodes())
			pList.addAll(findReferencedGlobalParameters(child));
		return pList;
	}

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
	 * A default constructor.
	 * 
	 * @param io
	 */
	public KineticLawGenerator(Model model) {
		this.modelOrig = model;
		init(null);
	}

	/**
	 * 
	 * @param model
	 * @param settings
	 * @throws IllegalFormatException
	 * @throws ModificationException
	 * @throws RateLawNotApplicableException
	 */
	public KineticLawGenerator(Model model, Properties settings)
			throws IllegalFormatException, ModificationException,
			RateLawNotApplicableException {
		this.settings = settings;
		this.modelOrig = model;
		init(null);
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
	 * Checks whether for this reaction the given kinetic law can be applied
	 * just based on the reversibility property (nothing else is checked).
	 * 
	 * @param reaction
	 * @param className
	 * @return
	 */
	private boolean checkReversibility(Reaction reaction, String className) {
		return (reaction.getReversible() && SBMLsqueezer
				.getKineticsReversible().contains(className))
				|| (!reaction.getReversible() && SBMLsqueezer
						.getKineticsIrreversible().contains(className));
	}

	/**
	 * Creates a kinetic law for the given reaction, which can be assigned to
	 * the given reaction.
	 * 
	 * @param r
	 *            The reaction for which a kinetic law is to be created.
	 * @param kinetic
	 *            an element from the Kinetics enum.
	 * @param reversibility
	 *            If true, a reversible kinetic will be assigned to this
	 *            reaction and the reversible attribute of the reaction will be
	 *            set to true.
	 * @return A kinetic law for the given reaction.
	 * @throws RateLawNotApplicableException
	 * @throws ModificationException
	 * @throws ClassNotFoundException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws IllegalArgumentException
	 */
	public BasicKineticLaw createKineticLaw(Reaction r,
			String kineticsClassName, boolean reversibility)
			throws ModificationException, RateLawNotApplicableException,
			ClassNotFoundException, SecurityException, NoSuchMethodException,
			IllegalArgumentException, InstantiationException,
			IllegalAccessException, InvocationTargetException {
		Reaction reaction = miniModel.getReaction(r.getId());
		if (reaction == null)
			reaction = r;
		reaction.setReversible(reversibility || reaction.getReversible());
		Class<?> kinCls = Class.forName(kineticsClassName);
		Object typeParameters[] = new Object[] {
				settings.get(CfgKeys.TYPE_STANDARD_VERSION),
				Boolean.valueOf(hasFullColumnRank(modelOrig)) };
		Constructor<?> constr = kinCls.getConstructor(reaction.getClass(),
				typeParameters.getClass());
		return (BasicKineticLaw) constr.newInstance(reaction, typeParameters);
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
		Model m = new Model(modelOrig.getId(), modelOrig.getLevel(), modelOrig
				.getVersion());
		boolean create = ((Boolean) settings
				.get(CfgKeys.OPT_GENERATE_KINETIC_LAW_FOR_EACH_REACTION))
				.booleanValue();
		/*
		 * Copy needed species and reactions.
		 */
		for (Reaction r : modelOrig.getListOfReactions()) {
			/*
			 * Let us find all fast reactions. This feature is currently
			 * ignored.
			 */
			if (r.getFast())
				listOfFastReactions.add(r);
			if (reactionID != null && !r.getId().equals(reactionID))
				continue;
			else
				create = true;
			if (r.isSetKineticLaw()) {
				String formula = r.getKineticLaw().getFormula();
				if (formula.equals("") || formula.equals(" ")) {
					System.err
							.println("Reaction "
									+ r.getId()
									+ " in the model has an incorrect format."
									+ "This means there is either an empty kinetic law in this reaction "
									+ "or a kinetic law that only consists of a white space. "
									+ "If you decide not to save this generated model, there is only one "
									+ "solution: "
									+ "open this SBML file in an editor and delete the whole kinetic law."
									+ "SBMLsqueezer ignores this misstake and generates a proper equation. "
									+ "Therfore we recomment that you save this generated model.");
					create = true;
				}
			}
			if (!r.isSetKineticLaw() || create) {
				Reaction rc = new Reaction(r.getId(), r.getLevel(), r
						.getVersion());
				if (r.isSetSBOTerm())
					rc.setSBOTerm(r.getSBOTerm());
				m.addReaction(rc);
				rc.setFast(r.getFast());
				rc.setReversible(r.getReversible());
				for (SpeciesReference s : r.getListOfReactants()) {
					Species species = s.getSpeciesInstance();
					if (m.getCompartment(species.getCompartment()) == null)
						m.addCompartment(species.getCompartmentInstance()
								.clone());
					if (m.getSpecies(species.getId()) == null)
						m.addSpecies(species.clone());
					SpeciesReference sr = s.clone();
					sr.setSpecies(m.getSpecies(s.getSpecies()));
					rc.addReactant(sr);
				}
				for (SpeciesReference s : r.getListOfProducts()) {
					Species species = s.getSpeciesInstance();
					if (m.getCompartment(species.getCompartment()) == null)
						m.addCompartment(species.getCompartmentInstance()
								.clone());
					if (m.getSpecies(species.getId()) == null)
						m.addSpecies(species.clone());
					SpeciesReference sr = s.clone();
					sr.setSpecies(m.getSpecies(s.getSpecies()));
					rc.addProduct(sr);
				}
				for (ModifierSpeciesReference s : r.getListOfModifiers()) {
					Species species = s.getSpeciesInstance();
					if (m.getCompartment(species.getCompartment()) == null)
						m.addCompartment(species.getCompartmentInstance()
								.clone());
					if (m.getSpecies(species.getId()) == null)
						m.addSpecies(species.clone());
					ModifierSpeciesReference sr = s.clone();
					sr.setSpecies(m.getSpecies(s.getSpecies()));
					rc.addModifier(sr);
				}
				/*
				 * This will be over written later on anyway but ignoring it
				 * would be confusing for users...
				 */
				if (r.isSetKineticLaw()) {
					KineticLaw l = r.getKineticLaw();
					if (l.isSetMath())
						for (Parameter parameter : modelOrig
								.getListOfParameters())
							if (l.getMath().refersTo(parameter.getId()))
								m.addParameter(parameter.clone());
					rc.setKineticLaw(l.clone());
				}
			}
		}
		return m;
	}

	/**
	 * @throws RateLawNotApplicableException
	 * @throws ModificationException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws NoSuchMethodException
	 * @throws ClassNotFoundException
	 * @throws IllegalArgumentException
	 * @throws SecurityException
	 * 
	 */
	public void generateLaws() throws ModificationException,
			RateLawNotApplicableException, SecurityException,
			IllegalArgumentException, ClassNotFoundException,
			NoSuchMethodException, InstantiationException,
			IllegalAccessException, InvocationTargetException {
		boolean reversibility = ((Boolean) settings
				.get(CfgKeys.OPT_TREAT_ALL_REACTIONS_REVERSIBLE))
				.booleanValue();
		for (Reaction r : miniModel.getListOfReactions()) {
			createKineticLaw(r, identifyReactionType(r.getId(), reversibility),
					reversibility);
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
	 * @return
	 */
	public int getNumCreatedKinetics() {
		return miniModel.getNumReactions();
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
	 * <ul>
	 * <li>1 = generalized mass action kinetics</li>
	 * <li>2 = Convenience kinetics</li>
	 * <li>3 = Michaelis-Menten kinetics</li>
	 * <li>4 = Random Order ternary kinetics</li>
	 * <li>5 = Ping-Pong</li>
	 * <li>6 = Ordered</li>
	 * <li>7 = Hill equation</li>
	 * <li>8 = Irreversible non-modulated non-interacting enzyme kinetics</li>
	 * <li>9 = Zeroth order forward mass action kinetics</li>
	 * <li>10 = Zeroth order reverse mass action kinetics</li>
	 * <li>11 = Competitive non-exclusive, non-cooperative inihibition</li>
	 * </ul>
	 * 
	 * @return Returns a sorted array of possible kinetic equations for the
	 *         given reaction in the model.
	 */
	public String[] identifyPossibleReactionTypes(String reactionID)
			throws RateLawNotApplicableException {
		int i;
		Reaction reaction = miniModel.getReaction(reactionID);
		Set<String> types = new HashSet<String>();

		if (reaction.getNumReactants() == 0
				|| (reaction.getNumProducts() == 0 && reaction.getReversible())) {
			/*
			 * Special case that occurs if we have at least one emty list of
			 * species references.
			 */
			for (String className : SBMLsqueezer.getKineticsZeroReactants())
				types.add(className);
			for (String className : SBMLsqueezer.getKineticsZeroProducts()) {
				if (SBMLsqueezer.getKineticsReversible().contains(className))
					types.add(className);
			}

		} else {
			/*
			 * Analyze properties of the reaction
			 */
			double stoichiometryLeft = 0d, stoichiometryRight = 0d, stoichiometry;
			boolean stoichiometryIntLeft = true;
			boolean reactionWithGenes = false, reactionWithRNAs = false;

			// compute stoichiometric properties
			for (i = 0; i < reaction.getNumReactants(); i++) {
				stoichiometry = reaction.getReactant(i).getStoichiometry();
				stoichiometryLeft += stoichiometry;
				if (((int) stoichiometry) - stoichiometry != 0d)
					stoichiometryIntLeft = false;
				// Transcription or translation?
				Species species = reaction.getReactant(i).getSpeciesInstance();
				if (SBO.isGeneOrGeneCodingRegion(species.getSBOTerm()))
					reactionWithGenes = true;
				else if (SBO.isRNAOrMessengerRNA(species.getSBOTerm()))
					reactionWithRNAs = true;
			}

			// is at least one modifier a gene or RNA?
			for (ModifierSpeciesReference msr : reaction.getListOfModifiers()) {
				if (SBO.isGeneOrGeneCodingRegion(msr.getSpeciesInstance()
						.getSBOTerm())) {
					reactionWithGenes = true;
					break;
				}
				if (SBO.isRNAOrMessengerRNA(msr.getSpeciesInstance()
						.getSBOTerm())) {
					reactionWithRNAs = true;
					break;
				}
			}

			// boolean stoichiometryIntRight = true;
			for (i = 0; i < reaction.getNumProducts(); i++) {
				stoichiometry = reaction.getProduct(i).getStoichiometry();
				stoichiometryRight += stoichiometry;
				// if (((int) stoichiometry) - stoichiometry != 0.0)
				// stoichiometryIntRight = false;
			}

			// identify types of modifiers
			List<String> nonEnzymeCatalysts = new LinkedList<String>();
			List<String> inhibitors = new LinkedList<String>();
			List<String> activators = new LinkedList<String>();
			List<String> transActiv = new LinkedList<String>();
			List<String> transInhib = new LinkedList<String>();
			List<String> enzymes = new LinkedList<String>();
			if (reaction.getNumModifiers() > 0) {
				BasicKineticLaw.identifyModifers(reaction, inhibitors,
						transActiv, transInhib, activators, enzymes,
						nonEnzymeCatalysts);
			}
			boolean nonEnzyme = ((!((Boolean) settings
					.get(CfgKeys.OPT_ALL_REACTIONS_ARE_ENZYME_CATALYZED))
					.booleanValue() && enzymes.size() == 0)
					|| (nonEnzymeCatalysts.size() > 0)
					|| reaction.getNumProducts() == 0 || (SBO
					.isEmptySet(reaction.getProduct(0).getSpeciesInstance()
							.getSBOTerm())));
			boolean uniUni = stoichiometryLeft == 1d
					&& stoichiometryRight == 1d;
			boolean biUni = stoichiometryLeft == 2d && stoichiometryRight == 1d;
			boolean biBi = stoichiometryLeft == 2d && stoichiometryRight == 2d;
			boolean integerStoichiometry = stoichiometryIntLeft;
			boolean withoutModulation = inhibitors.size() == 0
					&& activators.size() == 0 && transActiv.size() == 0
					&& transInhib.size() == 0;

			if (nonEnzyme) {
				// non enzyme reactions
				for (String className : SBMLsqueezer.getKineticsNonEnzyme())
					types.add(className);
			} else {
				/*
				 * Enzym-Kinetics: Assign possible rate laws for arbitrary
				 * enzyme reations.
				 */
				for (String className : SBMLsqueezer
						.getKineticsArbitraryEnzymeMechanism()) {
					if (checkReversibility(reaction, className)
							&& (!SBMLsqueezer.getKineticsIntStoichiometry()
									.contains(className) || integerStoichiometry)
							&& (SBMLsqueezer.getKineticsModulated().contains(
									className) || withoutModulation))
						types.add(className);
				}
				if (uniUni) {
					Set<String> onlyUniUni = new HashSet<String>();
					onlyUniUni.addAll(SBMLsqueezer.getKineticsUniUni());
					onlyUniUni.removeAll(SBMLsqueezer
							.getKineticsArbitraryEnzymeMechanism());
					if (!integerStoichiometry)
						onlyUniUni.removeAll(SBMLsqueezer
								.getKineticsIntStoichiometry());
					if (!withoutModulation)
						onlyUniUni.retainAll(SBMLsqueezer
								.getKineticsModulated());
					for (String className : onlyUniUni)
						if (checkReversibility(reaction, className))
							types.add(className);
				} else if (biUni) {
					Set<String> onlyBiUni = new HashSet<String>();
					onlyBiUni.addAll(SBMLsqueezer.getKineticsBiUni());
					onlyBiUni.removeAll(SBMLsqueezer
							.getKineticsArbitraryEnzymeMechanism());
					if (!integerStoichiometry)
						onlyBiUni.removeAll(SBMLsqueezer
								.getKineticsIntStoichiometry());
					if (!withoutModulation)
						onlyBiUni
								.retainAll(SBMLsqueezer.getKineticsModulated());
					for (String className : onlyBiUni)
						if (checkReversibility(reaction, className))
							types.add(className);
				} else if (biBi) {
					Set<String> onlyBiBi = new HashSet<String>();
					onlyBiBi.addAll(SBMLsqueezer.getKineticsBiBi());
					onlyBiBi.removeAll(SBMLsqueezer
							.getKineticsArbitraryEnzymeMechanism());
					if (!withoutModulation)
						onlyBiBi.retainAll(SBMLsqueezer.getKineticsModulated());
					for (String className : onlyBiBi)
						if (checkReversibility(reaction, className))
							types.add(className);
				}
			}

			/*
			 * Gene regulation
			 */
			if (uniUni) {
				Species reactant = reaction.getReactant(0).getSpeciesInstance();
				Species product = reaction.getProduct(0).getSpeciesInstance();
				if (SBO.isGeneOrGeneCodingRegion(reactant.getSBOTerm())
						|| (SBO.isEmptySet(reactant.getSBOTerm()) && (SBO
								.isRNAOrMessengerRNA(product.getSBOTerm()) || SBO
								.isProtein(product.getSBOTerm())))) {
					setBoundaryCondition(reactant, true);
					// throw exception if false reaction occurs
					if (SBO.isTranslation(reaction.getSBOTerm())
							&& !reactionWithRNAs)
						throw new RateLawNotApplicableException("Reaction "
								+ reaction.getId()
								+ " must be a transcription.");
					else if (SBO.isTranscription(reaction.getSBOTerm())
							&& !reactionWithGenes)
						throw new RateLawNotApplicableException("Reaction "
								+ reaction.getId() + " must be a translation.");

					for (String className : SBMLsqueezer
							.getKineticsGeneRegulatoryNetworks())
						types.add(className);
				}
			}
		}
		String t[] = types.toArray(new String[] {});
		Arrays.sort(t);
		return t;
	}

	/**
	 * identify the reactionType for generating the kinetics
	 * 
	 * @param reactionNum
	 */
	public String identifyReactionType(String reactionID, boolean reversibility)
			throws RateLawNotApplicableException {
		Reaction reaction = miniModel.getReaction(reactionID);
		SpeciesReference specref = reaction.getReactant(0);

		if (reaction.getNumReactants() == 0)
			for (String kin : SBMLsqueezer.getKineticsZeroReactants())
				return kin;
		if (reaction.getReversible() && reaction.getNumProducts() == 0)
			for (String kin : SBMLsqueezer.getKineticsZeroProducts())
				return kin;

		List<String> modActi = new LinkedList<String>();
		List<String> modTActi = new LinkedList<String>();
		List<String> modCat = new LinkedList<String>();
		List<String> modInhib = new LinkedList<String>();
		List<String> modTInhib = new LinkedList<String>();
		List<String> enzymes = new LinkedList<String>();
		BasicKineticLaw.identifyModifers(reaction, modInhib, modTActi,
				modTInhib, modActi, enzymes, modCat);

		String whichkin = settings.get(CfgKeys.KINETICS_NONE_ENZYME_REACTIONS)
				.toString();
		double stoichiometryLeft = 0d, stoichiometryRight = 0d;

		for (int i = 0; i < reaction.getNumReactants(); i++)
			stoichiometryLeft += reaction.getReactant(i).getStoichiometry();
		for (int i = 0; i < reaction.getNumProducts(); i++)
			stoichiometryRight += reaction.getProduct(i).getStoichiometry();

		// Enzym-Kinetics
		if (((Boolean) settings
				.get(CfgKeys.OPT_ALL_REACTIONS_ARE_ENZYME_CATALYZED))
				.booleanValue()) {
			if (stoichiometryLeft == 1d) {
				if (stoichiometryRight == 1d) {
					// Uni-Uni: MMK/ConvenienceIndependent (1E/1P)
					Species species = specref.getSpeciesInstance();
					if (SBO.isGeneOrGeneCodingRegion(species.getSBOTerm())) {
						setBoundaryCondition(species, true);
						if (SBO.isTranslation(reaction.getSBOTerm()))
							throw new RateLawNotApplicableException("Reaction "
									+ reaction.getId()
									+ " must be a transcription.");
						whichkin = settings.get(
								CfgKeys.KINETICS_GENE_REGULATION).toString();
					} else if (SBO.isRNAOrMessengerRNA(species.getSBOTerm())) {
						whichkin = settings.get(
								CfgKeys.KINETICS_GENE_REGULATION).toString();
						if (SBO.isTranscription(reaction.getSBOTerm()))
							throw new RateLawNotApplicableException("Reaction "
									+ reaction.getId()
									+ " must be a translation.");
					} else
						whichkin = settings.get(CfgKeys.KINETICS_UNI_UNI_TYPE)
								.toString();
				} else if (!reaction.getReversible() && !reversibility) {
					// Products don't matter.
					whichkin = settings.get(CfgKeys.KINETICS_UNI_UNI_TYPE)
							.toString();
				} else
					whichkin = settings.get(
							CfgKeys.KINETICS_OTHER_ENZYME_REACTIONS).toString();

			} else if (stoichiometryLeft == 2d) {
				if (stoichiometryRight == 1d) {
					// Bi-Uni: ConvenienceIndependent/Ran/Ord/PP (2E/1P)/(2E/2P)
					whichkin = settings.get(CfgKeys.KINETICS_BI_UNI_TYPE)
							.toString();
				} else if (stoichiometryRight == 2d) {
					whichkin = settings.get(CfgKeys.KINETICS_BI_BI_TYPE)
							.toString();
				} else
					whichkin = settings.get(
							CfgKeys.KINETICS_OTHER_ENZYME_REACTIONS).toString();
			} else
				// more than 2 types of reacting species or higher stoichiometry
				whichkin = settings
						.get(CfgKeys.KINETICS_OTHER_ENZYME_REACTIONS)
						.toString();

		} else { // Information of enzyme katalysis form SBML.

			// GMAK or Hill equation
			if (enzymes.isEmpty()) {
				switch (reaction.getNumReactants()) {
				case 1:
					if (reaction.getNumProducts() == 1) {
						if (SBO.isEmptySet(reaction.getProduct(0)
								.getSpeciesInstance().getSBOTerm()))
							whichkin = settings.get(
									CfgKeys.KINETICS_NONE_ENZYME_REACTIONS)
									.toString();
						else if (SBO.isEmptySet(reaction.getReactant(0)
								.getSpeciesInstance().getSBOTerm())
								&& (SBO.isProtein(reaction.getProduct(0)
										.getSpeciesInstance().getSBOTerm()) || SBO
										.isRNAOrMessengerRNA(reaction
												.getProduct(0)
												.getSpeciesInstance()
												.getSBOTerm()))) {
							whichkin = settings.get(
									CfgKeys.KINETICS_GENE_REGULATION)
									.toString();
						} else {
							Species species = specref.getSpeciesInstance();
							if (SBO.isGeneOrGeneCodingRegion(species
									.getSBOTerm())) {
								setBoundaryCondition(species, true);
								whichkin = settings.get(
										CfgKeys.KINETICS_GENE_REGULATION)
										.toString();
							} else if (SBO.isRNAOrMessengerRNA(species
									.getSBOTerm()))
								whichkin = settings.get(
										CfgKeys.KINETICS_GENE_REGULATION)
										.toString();
							else
								whichkin = settings.get(
										CfgKeys.KINETICS_NONE_ENZYME_REACTIONS)
										.toString();
						}
					} else
						whichkin = settings.get(
								CfgKeys.KINETICS_NONE_ENZYME_REACTIONS)
								.toString();
					break;
				default:
					whichkin = settings.get(
							CfgKeys.KINETICS_NONE_ENZYME_REACTIONS).toString();
					break;
				}

			} else { // Enzym-Kinetics
				if (stoichiometryLeft == 1d) {
					if (stoichiometryRight == 1d) { // Uni-Uni:
						// MMK/ConvenienceIndependent
						// (1E/1P)
						Species species = specref.getSpeciesInstance();
						if (SBO.isGeneOrGeneCodingRegion(species.getSBOTerm())) {
							setBoundaryCondition(species, true);
							whichkin = settings.get(
									CfgKeys.KINETICS_GENE_REGULATION)
									.toString();
						} else if (SBO
								.isRNAOrMessengerRNA(species.getSBOTerm()))
							whichkin = settings.get(
									CfgKeys.KINETICS_GENE_REGULATION)
									.toString();
						else
							whichkin = settings.get(
									CfgKeys.KINETICS_UNI_UNI_TYPE).toString();

					} else if (!reaction.getReversible() && !reversibility)
						whichkin = settings.get(CfgKeys.KINETICS_UNI_UNI_TYPE)
								.toString();
					else
						whichkin = settings.get(
								CfgKeys.KINETICS_OTHER_ENZYME_REACTIONS)
								.toString();
				} else if (stoichiometryLeft == 2d) {
					// Bi-Uni: ConvenienceIndependent/Ran/Ord/PP (2E/1P)/(2E/2P)
					if (stoichiometryRight == 1d) {
						whichkin = settings.get(CfgKeys.KINETICS_BI_UNI_TYPE)
								.toString();
					} else if (stoichiometryRight == 2d) {
						whichkin = settings.get(CfgKeys.KINETICS_BI_BI_TYPE)
								.toString();
					} else
						whichkin = settings.get(
								CfgKeys.KINETICS_OTHER_ENZYME_REACTIONS)
								.toString();
				} else
					whichkin = settings.get(
							CfgKeys.KINETICS_OTHER_ENZYME_REACTIONS).toString();
			}
		}

		// check entries in case of gene regulation:
		if ((whichkin == settings.get(CfgKeys.KINETICS_GENE_REGULATION))
				&& (modTActi.size() == 0) && (modTInhib.size() == 0)) {
			boolean reactionWithGenes = false;
			for (int i = 0; i < reaction.getNumReactants(); i++) {
				Species species = reaction.getReactant(i).getSpeciesInstance();
				if (SBO.isGeneOrGeneCodingRegion(species.getSBOTerm()))
					reactionWithGenes = true;
			}
			if (reactionWithGenes) {
				boolean transcription = false;
				for (int i = 0; i < reaction.getNumProducts(); i++) {
					Species species = reaction.getProduct(i)
							.getSpeciesInstance();
					if (SBO.isRNA(species.getSBOTerm())
							|| SBO.isMessengerRNA(species.getSBOTerm()))
						transcription = true;
				}
				if (transcription && SBO.isTranslation(reaction.getSBOTerm()))
					throw new RateLawNotApplicableException("Reaction "
							+ reaction.getId() + " must be a transcription.");
			} else {
				boolean reactionWithRNAs = false;
				for (int i = 0; i < reaction.getNumReactants(); i++) {
					Species species = reaction.getReactant(i)
							.getSpeciesInstance();
					if (SBO.isRNA(species.getSBOTerm())
							|| SBO.isMessengerRNA(species.getSBOTerm()))
						reactionWithRNAs = true;
				}
				if (reactionWithRNAs) {
					boolean translation = false;
					for (int i = 0; i < reaction.getNumProducts(); i++) {
						Species species = reaction.getProduct(i)
								.getSpeciesInstance();
						if (!SBO.isProtein(species.getSBOTerm()))
							translation = true;
					}
					if (SBO.isTranscription(reaction.getSBOTerm())
							&& translation)
						throw new RateLawNotApplicableException("Reaction "
								+ reaction.getId() + " must be a translation.");
				}
			}
			if (!reaction.getReversible() || reactionWithGenes) {
				if ((0 < modActi.size()) || (0 < modInhib.size())
						|| (0 < modCat.size()) || (0 < enzymes.size()))
					whichkin = settings.get(CfgKeys.KINETICS_GENE_REGULATION)
							.toString();
				else
					for (String kin : SBMLsqueezer.getKineticsZeroReactants()) {
						whichkin = kin;
						break;
					}
			} else
				whichkin = settings.get(CfgKeys.KINETICS_NONE_ENZYME_REACTIONS)
						.toString();
		} else if ((SBO.isTranslation(reaction.getSBOTerm()) || SBO
				.isTranscription(reaction.getSBOTerm()))
				&& !(whichkin == settings.get(CfgKeys.KINETICS_GENE_REGULATION)))
			throw new RateLawNotApplicableException("Reaction "
					+ reaction.getId() + " must be a state transition.");

		return whichkin;
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
	}

	/**
	 * Returns true only if this reaction should be considered enzyme-catalyzed
	 * (independend of any settings, based on the SBO annotation of the current
	 * model).
	 * 
	 * @param reaction
	 * @return
	 */
	public boolean isEnzymeReaction(String reactionID) {
		Reaction reaction = miniModel.getReaction(reactionID);
		// identify types of modifiers
		List<String> nonEnzymeCatalysts = new LinkedList<String>();
		List<String> inhibitors = new LinkedList<String>();
		List<String> activators = new LinkedList<String>();
		List<String> transActiv = new LinkedList<String>();
		List<String> transInhib = new LinkedList<String>();
		List<String> enzymes = new LinkedList<String>();
		if (reaction.getNumModifiers() > 0)
			BasicKineticLaw.identifyModifers(reaction, inhibitors, transActiv,
					transInhib, activators, enzymes, nonEnzymeCatalysts);
		return (enzymes.size() > 0 && nonEnzymeCatalysts.size() == 0);
	}

	/**
	 * Returns true only if given the current settings this reaction cannot be
	 * considered enzyme-catalyzed.
	 * 
	 * @param reaction
	 * @return
	 */
	public boolean isNonEnzymeReaction(String reactionID) {
		Reaction reaction = miniModel.getReaction(reactionID);
		// identify types of modifiers
		List<String> nonEnzymeCatalysts = new LinkedList<String>();
		List<String> inhibitors = new LinkedList<String>();
		List<String> activators = new LinkedList<String>();
		List<String> transActiv = new LinkedList<String>();
		List<String> transInhib = new LinkedList<String>();
		List<String> enzymes = new LinkedList<String>();
		if (reaction.getNumModifiers() > 0)
			BasicKineticLaw.identifyModifers(reaction, inhibitors, transActiv,
					transInhib, activators, enzymes, nonEnzymeCatalysts);
		return enzymes.size() == 0 && nonEnzymeCatalysts.size() > 0;
	}

	/**
	 * Delete unnecessary paremeters from the model. A parameter is defined to
	 * be unnecessary if and only if no kinetic law, no event assignment, no
	 * rule and no function makes use of this parameter.
	 * 
	 * @param selectedModel
	 */
	public void removeUnnecessaryParameters(Model model) {
		boolean isNeeded;
		int i, j, k;
		Parameter p;
		// remove unnecessary global parameters
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
		}
		// remove unnecessary local parameters
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
		}
	}

	/**
	 * set the boundaryCondition for a gen to the given value
	 * 
	 * @param species
	 * @param condition
	 */
	public void setBoundaryCondition(Species species, boolean condition) {
		if (condition != species.getBoundaryCondition()) {
			species.setBoundaryCondition(condition);
			species.stateChanged();
		}
	}

	/**
	 * Sets the initial amounts of all modifiers, reactants and products to the
	 * specified value.
	 * 
	 * @param reaction
	 * @param initialValue
	 */
	public void setInitialConcentrationTo(Reaction reaction, double initialValue) {
		for (int reactant = 0; reactant < reaction.getNumReactants(); reactant++) {
			Species species = reaction.getReactant(reactant)
					.getSpeciesInstance();
			if (species.getInitialConcentration() == 0d
					|| Double.isNaN(species.getInitialConcentration())) {
				species.setInitialConcentration(initialValue);
				species.setHasOnlySubstanceUnits(false);
				species.stateChanged();
			} else if (!species.getHasOnlySubstanceUnits()) {
				species.setHasOnlySubstanceUnits(false);
				species.stateChanged();
			}
		}
		for (int product = 0; product < reaction.getNumProducts(); product++) {
			Species species = reaction.getProduct(product).getSpeciesInstance();
			if (species.getInitialConcentration() == 0d
					|| Double.isNaN(species.getInitialConcentration())) {
				species.setInitialConcentration(initialValue);
				species.setHasOnlySubstanceUnits(false);
				species.stateChanged();
			} else if (!species.getHasOnlySubstanceUnits()) {
				species.setHasOnlySubstanceUnits(false);
				species.stateChanged();
			}
		}
		for (int modifier = 0; modifier < reaction.getNumModifiers(); modifier++) {
			Species species = reaction.getModifier(modifier)
					.getSpeciesInstance();
			if (species.getInitialConcentration() == 0d
					|| Double.isNaN(species.getInitialConcentration())) {
				species.setInitialConcentration(initialValue);
				species.setHasOnlySubstanceUnits(false);
				species.stateChanged();
			} else if (!species.getHasOnlySubstanceUnits()) {
				species.setHasOnlySubstanceUnits(false);
				species.stateChanged();
			}
		}
	}

	/**
	 * Sets the reaction with the given id in the minimal model copy to the
	 * given reversibility value.
	 * 
	 * @param id
	 * @param reversible
	 */
	public void setReversible(String id, boolean reversible) {
		Reaction r = miniModel.getReaction(id);
		if (r != null)
			r.setReversible(reversible);
		else
			throw new IllegalArgumentException(
					id
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
	 */
	public Reaction storeLaw(KineticLaw kineticLaw) {
		return storeLaw(kineticLaw, true);
	}

	/**
	 * 
	 * @param kineticLaw
	 * @param removeParameters
	 * @return
	 */
	private Reaction storeLaw(KineticLaw kineticLaw, boolean removeParameters) {
		int i;
		boolean reversibility = ((Boolean) settings
				.get(CfgKeys.OPT_TREAT_ALL_REACTIONS_REVERSIBLE))
				.booleanValue();
		Reaction reaction = modelOrig.getReaction(kineticLaw
				.getParentSBMLObject().getId());
		if (!(kineticLaw instanceof IrrevNonModulatedNonInteractingEnzymes))
			reaction.setReversible(reversibility || reaction.getReversible());
		reaction.setKineticLaw(kineticLaw);
		if ((kineticLaw instanceof BasicKineticLaw)
				&& (kineticLaw.getNotesString().length() == 0))
			kineticLaw.setNotes(kineticLaw.toString());
		// set the BoundaryCondition to true for Genes if not set anyway:
		for (i = 0; i < reaction.getNumReactants(); i++) {
			Species species = reaction.getReactant(i).getSpeciesInstance();
			if (SBO.isGeneOrGeneCodingRegion(species.getSBOTerm()))
				setBoundaryCondition(species, true);
		}
		for (i = 0; i < reaction.getNumProducts(); i++) {
			Species species = reaction.getProduct(i).getSpeciesInstance();
			if (SBO.isGeneOrGeneCodingRegion(species.getSBOTerm()))
				setBoundaryCondition(species, true);
		}
		storeParamters(reaction);
		if (removeParameters)
			removeUnnecessaryParameters(modelOrig);
		storeUnits();
		return reaction;
	}

	/**
	 * store the generated Kinetics in SBML-File as MathML.
	 */
	public void storeLaws(LawListener l) {
		l.totalNumber(miniModel.getNumReactions());
		for (int i = 0; i < miniModel.getNumReactions(); i++) {
			storeLaw(miniModel.getReaction(i).getKineticLaw(), false);
			l.currentNumber(i);
		}
		removeUnnecessaryParameters(modelOrig);
	}

	/**
	 * This method stores all newly created parameters in the model.
	 * 
	 * @param reaction
	 */
	public void storeParamters(Reaction reaction) {
		setInitialConcentrationTo(reaction, 1d);
		KineticLaw kineticLaw = reaction.getKineticLaw();
		ListOf<Parameter> paramListLocal = kineticLaw.getListOfParameters();
		if (((Boolean) settings
				.get(CfgKeys.OPT_ADD_NEW_PARAMETERS_ALWAYS_GLOBALLY))
				.booleanValue())
			for (int paramNum = paramListLocal.size() - 1; paramNum >= 0; paramNum--)
				modelOrig.addParameter(paramListLocal.remove(paramNum));
		for (Parameter parameter : miniModel.getListOfParameters())
			if (modelOrig.getParameter(parameter.getId()) == null)
				modelOrig.addParameter(parameter);
	}

	/**
	 * Stores all units created in the mini model in the original model.
	 */
	private void storeUnits() {
		for (UnitDefinition ud : miniModel.getListOfUnitDefinitions()) {
			if (!modelOrig.getListOfUnitDefinitions().contains(ud))
				modelOrig.addUnitDefinition(ud.clone());
		}
	}

	/**
	 * Sets the SBO annotation of modifiers to more precise values in the local
	 * mini copy of the model.
	 * 
	 * @param possibleEnzymes
	 */
	public void updateEnzymeKatalysis(Set<Integer> possibleEnzymes) {
		for (Reaction r : miniModel.getListOfReactions()) {
			for (ModifierSpeciesReference modifier : r.getListOfModifiers()) {
				if (SBO.isEnzymaticCatalysis(modifier.getSBOTerm())
						&& !possibleEnzymes.contains(Integer.valueOf(modifier
								.getSpeciesInstance().getSBOTerm())))
					modifier.setSBOTerm(SBO.getCatalysis());
				else if (SBO.isCatalyst(modifier.getSBOTerm())
						&& possibleEnzymes.contains(Integer.valueOf(modifier
								.getSpeciesInstance().getSBOTerm())))
					modifier.setSBOTerm(SBO.getEnzymaticCatalysis());
			}
		}
	}

	/**
	 * 
	 * @param id
	 * @return The kinetic law that is currently set to the reaction with the
	 *         given id.
	 */
	public KineticLaw getKineticLaw(String id) {
		return miniModel.getReaction(id).getKineticLaw();
	}
}
