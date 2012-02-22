/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of SBMLsqueezer, a Java program that creates rate 
 * equations for reactions in SBML files (http://sbml.org).
 *
 * Copyright (C) 2006-2012 by the University of Tuebingen, Germany.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * ---------------------------------------------------------------------
 */
package org.sbml.squeezer;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.sbml.jsbml.CVTerm.Qualifier;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBO;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.squeezer.util.Bundles;

import de.zbit.util.prefs.SBPreferences;

/**
 * The purpose of this class is to analyze a reaction object for several
 * properties, such as the number of reactants and products, the types of
 * modifiers and their number, if the stoichiometries are always integer numbers
 * or real valued and much more. With this information at hand the reaction type
 * is able to suggest applicable kinetic equations from the list of available
 * kinetic laws and also to select the default kinetic equation for this
 * specific type. This class can also answer several questions regarding a
 * reaction, for instance, whether a reaction is follows a bi-bi mechanism. This
 * can be used for GUIs to display reaction properties to a user.
 * 
 * @author Andreas Dr&auml;ger
 * @author Sarah R. M&uuml;ller vom Hagen
 * @date 2009-10-01
 * @since 1.3
 * @version $Rev$
 */
public class ReactionType {

  /**
   * A {@link Logger} for this class.
   */
  private static final transient Logger logger = Logger.getLogger(ReactionType.class.getName());

  
	/**
	 * Set of those kinetic equations that can only be reversible.
	 */
	private static Set<Class> notIrreversible;

	/**
	 * Set of kinetic equations that can only be applied to irreversible
	 * reactions.
	 */
	private static Set<Class> notReversible;

	/**
	 * Initializes the sets of not reversible and not irreversible kinetic
	 * equations.
	 */
	static {
		notIrreversible = new HashSet<Class>();
		notIrreversible.addAll(SBMLsqueezer.getKineticsReversible());
		notIrreversible.removeAll(SBMLsqueezer.getKineticsIrreversible());
		notReversible = new HashSet<Class>();
		notReversible.addAll(SBMLsqueezer.getKineticsIrreversible());
		notReversible.removeAll(SBMLsqueezer.getKineticsReversible());
	}

	/**
	 * Checks if the given set of kinetics can be used given the property if all
	 * reactions should be treated reversibly.
	 * 
	 * @param treatReactionsReversible
	 * @param set
	 * @return A set of kinetics that can be used given the reversible property.
	 */
	private static Set<Class> checkReactions(boolean treatReactionsReversible,
			Set<Class> set) {
		Set<Class> kinetics = new HashSet<Class>();
		kinetics.addAll(set);
		kinetics.removeAll(notReversible);
		if (!treatReactionsReversible) {
			kinetics.removeAll(notIrreversible);
		}
		/*
		 * else kinetics.retainAll(notIrreversible);
		 */
		return kinetics;
	}

	/**
	 * 
	 * @param treatReactionsReversible
	 * @return
	 */
	public static Set<Class> getKineticsArbitraryEnzyme(
			boolean treatReactionsReversible) {
		return checkReactions(treatReactionsReversible, SBMLsqueezer
				.getKineticsArbitraryEnzymeMechanism());
	}

	/**
	 * 
	 * @param treatReactionsReversible
	 * @return
	 */
	public static Set<Class> getKineticsBiBi(boolean treatReactionsReversible) {
		return checkReactions(treatReactionsReversible, SBMLsqueezer
				.getKineticsBiBi());
	}

	/**
	 * 
	 * @param treatReactionsReversible
	 * @return
	 */
	public static Set<Class> getKineticsBiUni(boolean treatReactionsReversible) {
		return checkReactions(treatReactionsReversible, SBMLsqueezer
				.getKineticsBiUni());
	}

	/**
	 * 
	 * @param treatReactionsReversible
	 * @return
	 */
	public static Set<Class> getKineticsGeneRegulation(
			boolean treatReactionsReversible) {
		return checkReactions(treatReactionsReversible, SBMLsqueezer
				.getKineticsGeneRegulatoryNetworks());
	}

	/**
	 * 
	 * @param treatReactionsReversible
	 * @return
	 */
	public static Set<Class> getKineticsNonEnzyme(
			boolean treatReactionsReversible) {
		return checkReactions(treatReactionsReversible, SBMLsqueezer
				.getKineticsNonEnzyme());
	}

	/**
	 * 
	 * @param treatReactionsReversible
	 * @return
	 */
	public static Set<Class> getKineticsUniUni(boolean treatReactionsReversible) {
		return checkReactions(treatReactionsReversible, SBMLsqueezer
				.getKineticsUniUni());
	}

	/**
	 * identify which Modifer is used
	 * 
	 * @param reactionNum
	 */
	public static final void identifyModifers(Reaction reaction,
			List<String> enzymes, List<String> activators,
			List<String> inhibitors, List<String> nonEnzymeCatalysts) {
		enzymes.clear();
		activators.clear();
		inhibitors.clear();
		nonEnzymeCatalysts.clear();
		int type;
		for (ModifierSpeciesReference modifier : reaction.getListOfModifiers()) {
			type = modifier.getSBOTerm();
			// if (SBO.isModifier(type)) {
			// Ok, this is confusing...
			// inhibitors.add(modifier.getSpecies());
			// activators.add(modifier.getSpecies());
			// }
			if (SBO.isCatalyst(type)) {
				if (SBO.isEnzymaticCatalysis(type)) {
				  logger.fine(MessageFormat.format(Bundles.MESSAGES.getString("IS_ENZYMATIC_CATALYST"), modifier));
					enzymes.add(modifier.getSpecies());
				} else {
				  logger.fine(MessageFormat.format(Bundles.MESSAGES.getString("IS_NON_ENZYMATIC_CATALYST"), modifier));
					nonEnzymeCatalysts.add(modifier.getSpecies());
				}
			} else if (SBO.isTranscriptionalInhibitor(type)
					|| SBO.isTranslationalInhibitor(type)) {
			  logger.fine(MessageFormat.format(Bundles.MESSAGES.getString("IS_TRANSCRIPTIONAL_OR_TRANSLATIONAL_INHIBITOR"), modifier));
				inhibitors.add(modifier.getSpecies());
			} else if (SBO.isInhibitor(type)) {
			  logger.fine(MessageFormat.format(Bundles.MESSAGES.getString("IS_INHIBITOR"), modifier));
				inhibitors.add(modifier.getSpecies());
			} else if (SBO.isTranscriptionalActivation(type)
					|| SBO.isTranslationalActivation(type)) {
			  logger.fine(MessageFormat.format(Bundles.MESSAGES.getString("IS_TRANSCRIPTIONAL_OR_TRANSLATIONAL_ACTIVATOR"), modifier));
				activators.add(modifier.getSpecies());
			} else if (SBO.isTrigger(type) || SBO.isStimulator(type)) {
				// no extra support for unknown catalysis anymore...
				// physical stimulation is now also a stimulator.
			  logger.fine(MessageFormat.format(Bundles.MESSAGES.getString("IS_ACTIVATOR"), modifier));
				activators.add(modifier.getSpecies());
			}
		}
	}

	/**
	 * 
	 * @param reaction
	 * @return
	 */
	public static double productOrder(Reaction reaction) {
		double stoichiometryRight = 0;
		for (SpeciesReference specRef : reaction.getListOfProducts())
			stoichiometryRight += specRef.getStoichiometry();
		return stoichiometryRight;
	}

	/**
	 * 
	 * Analyze properties of the reaction: compute stoichiometric properties.
	 * 
	 * @param r
	 */
	public static double reactantOrder(Reaction reaction) {
		double stoichiometryLeft = 0;
		for (SpeciesReference specRef : reaction.getListOfReactants())
			stoichiometryLeft += specRef.getStoichiometry();
		return stoichiometryLeft;
	}

	/**
	 * Convenient method to check whether the given ListOf is either empty or
	 * contains only elements with an SBO annotation that corresponds to an
	 * empty set.
	 * 
	 * @param listOf
	 * @return
	 */
	public static boolean representsEmptySet(ListOf<SpeciesReference> listOf) {
		if (listOf.size() == 0)
			return true;
		boolean emptySet = true;
		for (SpeciesReference specRef : listOf)
			emptySet &= SBO.isEmptySet(specRef.getSpeciesInstance()
					.getSBOTerm());
		return emptySet;
	}

	private List<String> enzymes, inhibitors, nonEnzymeCatalysts, activators;

	private Reaction reaction;

	private boolean biBi, biUni, integerStoichiometry, nonEnzyme,
			reactionWithGenes = false, reactionWithRNAs = false, reversibility,
			stoichiometryIntLeft = true, uniUni, withoutModulation;

	private SBPreferences prefs;

	private double stoichiometryLeft = 0d, stoichiometryRight = 0d;


	/**
	 * Analyses the given reaction for several properties.
	 * 
	 * @param r
	 *            The reaction to be analyzed.
	 * @throws RateLawNotApplicableException
	 */
	public ReactionType(Reaction r)
			throws RateLawNotApplicableException {
		int i;
		this.reaction = r;
		// Check ignore list:
		prefs = SBPreferences.getPreferencesFor(SqueezerOptions.class);
		String ignore = prefs.get(SqueezerOptions.OPT_IGNORE_THESE_SPECIES_WHEN_CREATING_LAWS);
		removeSpeciesAccordingToIgnoreList(reaction, ignore.contains(",") ? ignore.split(",") : new String[] {ignore});
		this.prefs = SBPreferences.getPreferencesFor(SqueezerOptions.class);
		this.reversibility = prefs
				.getBoolean(SqueezerOptions.OPT_TREAT_ALL_REACTIONS_REVERSIBLE);

		/*
		 * Analyze properties of the reaction: compute stoichiometric properties
		 */
		double stoichiometry;
		for (i = 0; i < reaction.getNumReactants(); i++) {
			stoichiometry = reaction.getReactant(i).getStoichiometry();
			stoichiometryLeft += stoichiometry;
			if (((int) stoichiometry) - stoichiometry != 0d)
				stoichiometryIntLeft = false;
			// Transcription or translation?
			Species reactantSpecies = reaction.getReactant(i)
					.getSpeciesInstance();
			if (SBO.isGeneOrGeneCodingRegion(reactantSpecies.getSBOTerm()))
				reactionWithGenes = true;
			else if (SBO.isRNAOrMessengerRNA(reactantSpecies.getSBOTerm()))
				reactionWithRNAs = true;
		}

		// is at least one modifier a gene or RNA?
		for (ModifierSpeciesReference msr : reaction.getListOfModifiers()) {
			if (SBO.isGeneOrGeneCodingRegion(msr.getSpeciesInstance()
					.getSBOTerm())) {
				reactionWithGenes = true;
				break;
			}
			if (SBO.isRNAOrMessengerRNA(msr.getSpeciesInstance().getSBOTerm())) {
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
		nonEnzymeCatalysts = new LinkedList<String>();
		inhibitors = new LinkedList<String>();
		activators = new LinkedList<String>();
		enzymes = new LinkedList<String>();
		if (reaction.getNumModifiers() > 0)
			identifyModifers(reaction, enzymes, activators, inhibitors,
					nonEnzymeCatalysts);
		nonEnzyme = ((!prefs.getBoolean(SqueezerOptions.OPT_ALL_REACTIONS_ARE_ENZYME_CATALYZED) && enzymes.size() == 0)
				|| (nonEnzymeCatalysts.size() > 0) 
				|| (((reaction.getNumProducts() == 0)
						|| (SBO.isEmptySet(reaction.getProduct(0).getSpeciesInstance().getSBOTerm()))) && reaction.isReversible()));

		uniUni = (stoichiometryLeft == 1d) && (stoichiometryRight == 1d);
		biUni = (stoichiometryLeft == 2d) && (stoichiometryRight == 1d);
		biBi = (stoichiometryLeft == 2d) && (stoichiometryRight == 2d);
		integerStoichiometry = stoichiometryIntLeft;
		withoutModulation = inhibitors.size() == 0 && activators.size() == 0;

		/*
		 * Check if this reaction makes sense at all.
		 */
		if (reactionWithGenes
				|| reaction.getNumReactants() == 0
				|| (reaction.getNumReactants() == 1 && SBO.isEmptySet(reaction
						.getReactant(0).getSBOTerm()))) {
			boolean transcription = false;
			for (i = 0; i < reaction.getNumProducts(); i++) {
				Species species = reaction.getProduct(i).getSpeciesInstance();
				if (SBO.isRNA(species.getSBOTerm())
						|| SBO.isMessengerRNA(species.getSBOTerm()))
					transcription = reactionWithRNAs = true;
			}
			if (transcription && SBO.isTranslation(reaction.getSBOTerm()))
				throw new RateLawNotApplicableException(MessageFormat.format(
						Bundles.WARNINGS.getString("REACTION_MUST_BE_TRANSRIPTION"), reaction.getId()));
		}
		if (reactionWithRNAs
				|| reaction.getNumReactants() == 0
				|| (reaction.getNumReactants() == 1 && SBO.isEmptySet(reaction
						.getReactant(0).getSBOTerm()))) {
			boolean translation = false;
			for (i = 0; i < reaction.getNumProducts() && !translation; i++) {
				Species product = reaction.getProduct(i).getSpeciesInstance();
				if (SBO.isProtein(product.getSBOTerm())
						|| SBO.isGeneric(product.getSBOTerm()))
					translation = reactionWithRNAs = true;
			}
			if (SBO.isTranscription(reaction.getSBOTerm()) && translation)
				throw new RateLawNotApplicableException(MessageFormat.format(
						Bundles.WARNINGS.getString("REACTION_MUST_BE_TRANSLATION"), reaction.getId()));
		}
		if (uniUni) {
			Species species = reaction.getReactant(0).getSpeciesInstance();
			if (SBO.isGeneOrGeneCodingRegion(species.getSBOTerm())) {
				if (prefs
						.getBoolean(SqueezerOptions.OPT_SET_BOUNDARY_CONDITION_FOR_GENES)) {
					setBoundaryCondition(species, true);
				}
				if (SBO.isTranslation(reaction.getSBOTerm())) {
					throw new RateLawNotApplicableException(MessageFormat.format(
							Bundles.WARNINGS.getString("REACTION_MUST_BE_TRANSRIPTION"), reaction.getId()));
				}
			} else if (SBO.isRNAOrMessengerRNA(species.getSBOTerm())) {
				if (SBO.isTranscription(reaction.getSBOTerm())) {
					throw new RateLawNotApplicableException(MessageFormat.format(
							Bundles.WARNINGS.getString("REACTION_MUST_BE_TRANSLATION"), reaction.getId()));
				}
			}
		}
		if ((SBO.isTranslation(reaction.getSBOTerm()) || SBO.isTranscription(reaction.getSBOTerm()))
				&& !(reactionWithGenes || reactionWithRNAs)) {
			throw new RateLawNotApplicableException(MessageFormat.format(
					Bundles.WARNINGS.getString("REACTION_MUST_BE_TRANSITION"), reaction.getId()));
		}
	}

	/**
	 * 
	 * @param r
	 * @param ignoreList
	 */
	public void removeSpeciesAccordingToIgnoreList(
		Reaction r, String... ignoreList) {
		if ((ignoreList != null) && (ignoreList.length >= 0)) {
			removeSpeciesAccordingToIgnoreList(r.getListOfReactants(), ignoreList);
			removeSpeciesAccordingToIgnoreList(r.getListOfProducts(), ignoreList);
		}
	}

	/**
	 * 
	 * @param listOfParticipants
	 * @param ignoreList
	 */
	private void removeSpeciesAccordingToIgnoreList(
		ListOf<SpeciesReference> listOfParticipants, String... ignoreList) {
			Species spec;
			for (int i = listOfParticipants.size() - 1; i >= 0; i--) {
				spec = listOfParticipants.get(i).getSpeciesInstance();
				for (String string : ignoreList) {
					if (spec.filterCVTerms(Qualifier.BQB_IS, string).size() > 0) {
						listOfParticipants.remove(i);
						break;
					}
				}
			}
	}

	/**
	 * Checks whether for this reaction the given kinetic law can be applied
	 * just based on the reversibility property (nothing else is checked).
	 * 
	 * @param reaction
	 * @param className
	 * @return
	 */
	private boolean checkReversibility(Reaction reaction, Class className) {
		return (reaction.getReversible() && SBMLsqueezer
				.getKineticsReversible().contains(className))
				|| (!reaction.getReversible() && SBMLsqueezer
						.getKineticsIrreversible().contains(className));
	}

	/**
	 * @return the activators
	 */
	public List<String> getActivators() {
		return activators;
	}

	/**
	 * @return the enzymes
	 */
	public List<String> getEnzymes() {
		return enzymes;
	}

	/**
	 * @return the inhibitors
	 */
	public List<String> getInhibitors() {
		return inhibitors;
	}

	/**
	 * @return the nonEnzymeCatalysts
	 */
	public List<String> getNonEnzymeCatalysts() {
		return nonEnzymeCatalysts;
	}

	/**
	 * @return the reaction
	 */
	public Reaction getReaction() {
		return reaction;
	}

	/**
	 * @return the stoichiometryLeft
	 */
	public double getStoichiometryLeft() {
		return stoichiometryLeft;
	}

	/**
	 * @return the stoichiometryRight
	 */
	public double getStoichiometryRight() {
		return stoichiometryRight;
	}

	/**
	 * identify the reactionType for generating the kinetics
	 * @throws ClassNotFoundException 
	 * 
	 */
	public Class<?> identifyPossibleKineticLaw() throws ClassNotFoundException {
		if (representsEmptySet(reaction.getListOfReactants())) {
			if (reactionWithGenes || reactionWithRNAs) {
				if (SBMLsqueezer.getKineticsZeroReactants().contains(
						prefs.get(SqueezerOptions.KINETICS_GENE_REGULATION))) {
					return prefs.getClass(SqueezerOptions.KINETICS_GENE_REGULATION);
				}
				for (Class<?> kin : SBMLsqueezer.getKineticsGeneRegulatoryNetworks()) {
					if (SBMLsqueezer.getKineticsZeroReactants().contains(kin)) {
						return kin;
					}
				}
			}
			for (Class<?> kin : SBMLsqueezer.getKineticsZeroReactants()) {
				return kin;
			}
		}
		if (representsEmptySet(reaction.getListOfProducts())
				&& (reversibility || reaction.getReversible())) {
			if (reactionWithGenes || reactionWithRNAs)
				for (Class<?> kin : SBMLsqueezer.getKineticsGeneRegulatoryNetworks())
					if (SBMLsqueezer.getKineticsZeroReactants().contains(kin)) {
						return kin;
					}
			for (Class<?> kin : SBMLsqueezer.getKineticsZeroProducts()) {
				return kin;
			}
		}

		boolean enzymeCatalyzed = prefs
				.getBoolean(SqueezerOptions.OPT_ALL_REACTIONS_ARE_ENZYME_CATALYZED)
				|| enzymes.size() > 0;
		Class<?> whichkin = prefs.getClass(SqueezerOptions.KINETICS_NONE_ENZYME_REACTIONS);

		if (enzymeCatalyzed) {
			whichkin = prefs.getClass(SqueezerOptions.KINETICS_ARBITRARY_ENZYME_REACTIONS);
		}
		if (stoichiometryLeft == 1d) {
			Species reactant = reaction.getReactant(0).getSpeciesInstance();
			if (stoichiometryRight == 1d) {
				Species product = reaction.getProduct(0).getSpeciesInstance();
				if ((reactionWithGenes || reactionWithRNAs || (SBO
						.isGeneOrGeneCodingRegion(reactant.getSBOTerm()) || (SBO
						.isEmptySet(reactant.getSBOTerm()) && (SBO
						.isProtein(product.getSBOTerm())
						|| SBO.isGeneric(product.getSBOTerm()) || SBO
						.isRNAOrMessengerRNA(product.getSBOTerm()))))
						&& !(SBO.isEmptySet(product.getSBOTerm())))) {
					whichkin = prefs.getClass(SqueezerOptions.KINETICS_GENE_REGULATION);
				}
			}
			if (!whichkin.equals(prefs
					.get(SqueezerOptions.KINETICS_GENE_REGULATION))
					&& (((enzymes.size() > 0) || enzymeCatalyzed) && ((stoichiometryRight == 1d) || !(reaction
							.getReversible() || reversibility)))) {
				whichkin = prefs.getClass(SqueezerOptions.KINETICS_UNI_UNI_TYPE);
			}
		} else if (biUni && enzymeCatalyzed) {
			whichkin = prefs.getClass(SqueezerOptions.KINETICS_BI_UNI_TYPE);
		} else if (biBi && enzymeCatalyzed) {
			whichkin = prefs.getClass(SqueezerOptions.KINETICS_BI_BI_TYPE);
		}
		return whichkin;
	}

	/**
	 * 
	 * @return Returns a sorted array of possible kinetic equations for the
	 *         given reaction in the model (the names of the implementing
	 *         classes).
	 * @throws RateLawNotApplicableException
	 */
	public Class<?>[] identifyPossibleKineticLaws() {
		Set<Class> types = new HashSet<Class>();
		boolean emptyListOfReactants = representsEmptySet(reaction
				.getListOfReactants());
		boolean enzymeKinetics = false;
		if (emptyListOfReactants
				|| ((reaction.getReversible() || reversibility) && representsEmptySet(reaction
						.getListOfProducts()))) {
			/*
			 * Special case that occurs if we have at least one empty list of
			 * species references.
			 */
			if (emptyListOfReactants) {
				for (Class<?> className : SBMLsqueezer.getKineticsZeroReactants()) {
					types.add(className);
				}
			} else {
				for (Class<?> className : SBMLsqueezer.getKineticsZeroProducts()) {
					if (SBMLsqueezer.getKineticsReversible().contains(className)) {
						types.add(className);
					}
				}
			}
			// Gene-regulation
			if (reactionWithGenes || reactionWithRNAs) {
				for (Class<?> className : SBMLsqueezer.getKineticsGeneRegulatoryNetworks()) {
					if ((reaction.getReversible() && !notReversible
							.contains(className))
							|| (!reaction.getReversible() && !notIrreversible
									.contains(className))) {
						types.add(className);
					}
				}
			} else if (!nonEnzyme) {
				enzymeKinetics = true;
			}

		} else {
			if (nonEnzyme) {
				// non enzyme reactions
				types.addAll(SBMLsqueezer.getKineticsNonEnzyme());
			} else {
				enzymeKinetics = true;
			}

			/*
			 * Gene regulation
			 */
			if (uniUni) {
				Species reactant = reaction.getReactant(0).getSpeciesInstance();
				Species product = reaction.getProduct(0).getSpeciesInstance();
				if ((reactionWithGenes || reactionWithRNAs || (SBO
						.isGeneOrGeneCodingRegion(reactant.getSBOTerm()) || (SBO
						.isEmptySet(reactant.getSBOTerm()) && (SBO
						.isRNAOrMessengerRNA(product.getSBOTerm())
						|| SBO.isProtein(product.getSBOTerm()) || SBO
						.isGeneric(product.getSBOTerm()))))))
					for (Class<?> className : SBMLsqueezer.getKineticsGeneRegulatoryNetworks()) {
						if ((reaction.getReversible() && !notReversible.contains(className))
								|| (!reaction.getReversible() && !notIrreversible
										.contains(className))) {
							types.add(className);
						}
					}
			}
		}
		if (enzymeKinetics) {
			/*
			 * Enzym-Kinetics: Assign possible rate laws for arbitrary enzyme
			 * reations.
			 */
			for (Class<?> className : SBMLsqueezer.getKineticsArbitraryEnzymeMechanism()) {
				if (checkReversibility(reaction, className)
						&& (!SBMLsqueezer.getKineticsIntStoichiometry().contains(className) || integerStoichiometry)
						&& (SBMLsqueezer.getKineticsModulated().contains(className) || withoutModulation)) {
					types.add(className);
				}
			}
			if (uniUni || ((stoichiometryLeft == 1d) && !(reaction.getReversible() || reversibility))) {
				Set<Class> onlyUniUni = new HashSet<Class>();
				onlyUniUni.addAll(SBMLsqueezer.getKineticsUniUni());
				onlyUniUni.removeAll(SBMLsqueezer.getKineticsArbitraryEnzymeMechanism());
				if (!integerStoichiometry) {
					onlyUniUni.removeAll(SBMLsqueezer.getKineticsIntStoichiometry());
				}
				if (!withoutModulation) {
					onlyUniUni.retainAll(SBMLsqueezer.getKineticsModulated());
				}
				for (Class<?> className : onlyUniUni) {
					if (checkReversibility(reaction, className)) {
						types.add(className);
					}
				}
			} else if (biUni || ((stoichiometryLeft == 2d) && !(reaction.getReversible() || reversibility))) {
				Set<Class> onlyBiUni = new HashSet<Class>();
				onlyBiUni.addAll(SBMLsqueezer.getKineticsBiUni());
				onlyBiUni.removeAll(SBMLsqueezer
						.getKineticsArbitraryEnzymeMechanism());
				if (!integerStoichiometry)
					onlyBiUni.removeAll(SBMLsqueezer
							.getKineticsIntStoichiometry());
				if (!withoutModulation)
					onlyBiUni.retainAll(SBMLsqueezer.getKineticsModulated());
				for (Class<?> className : onlyBiUni) {
					if (checkReversibility(reaction, className)) {
						types.add(className);
					}
				}
			} else if (biBi) {
				Set<Class> onlyBiBi = new HashSet<Class>();
				onlyBiBi.addAll(SBMLsqueezer.getKineticsBiBi());
				onlyBiBi.removeAll(SBMLsqueezer
						.getKineticsArbitraryEnzymeMechanism());
				if (!withoutModulation) {
					onlyBiBi.retainAll(SBMLsqueezer.getKineticsModulated());
				}
				for (Class<?> className : onlyBiBi) {
					if (checkReversibility(reaction, className)) {
						types.add(className);
					}
				}
			}
		}
		Class<?> t[] = types.toArray(new Class[] {});
		Arrays.sort(t, new Comparator<Class<?>>() {

			/* (non-Javadoc)
       * @see java.util.Comparator#compare(T, T)
       */
			public int compare(Class<?> clazz1, Class<?> clazz2) {
				return clazz1.getName().compareTo(clazz2.getName());
			}
			
		});
		return t;
	}

	/**
	 * @return the biBi
	 */
	public boolean isBiBi() {
		return biBi;
	}

	/**
	 * @return the biUni
	 */
	public boolean isBiUni() {
		return biUni;
	}

	/**
	 * Returns true only if this reaction should be considered enzyme-catalyzed
	 * (independend of any settings, based on the SBO annotation of the current
	 * model).
	 * 
	 * @param reaction
	 * @return
	 */
	public boolean isEnzymeReaction() {
		return (enzymes.size() > 0 && nonEnzymeCatalysts.size() == 0);
	}

	/**
	 * @return the integerStoichiometry
	 */
	public boolean isIntegerStoichiometry() {
		return integerStoichiometry;
	}

	/**
	 * @return the nonEnzyme
	 */
	public boolean isNonEnzyme() {
		return nonEnzyme;
	}

	/**
	 * Returns true only if given the current settings this reaction cannot be
	 * considered enzyme-catalyzed.
	 * 
	 * @param reaction
	 * @return
	 */
	public boolean isNonEnzymeReaction() {
		return enzymes.size() == 0 && nonEnzymeCatalysts.size() > 0;
	}

	/**
	 * @return the reactionWithGenes
	 */
	public boolean isReactionWithGenes() {
		return reactionWithGenes;
	}

	/**
	 * @return the reactionWithRNAs
	 */
	public boolean isReactionWithRNAs() {
		return reactionWithRNAs;
	}

	/**
	 * @return the stoichiometryIntLeft
	 */
	public boolean isStoichiometryIntLeft() {
		return stoichiometryIntLeft;
	}

	/**
	 * @return the uniUni
	 */
	public boolean isUniUni() {
		return uniUni;
	}

	/**
	 * @return the withoutModulation
	 */
	public boolean isWithoutModulation() {
		return withoutModulation;
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
		}
	}

}
