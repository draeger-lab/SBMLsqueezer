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

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IllegalFormatException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.sbml.jlibsbml.KineticLaw;
import org.sbml.jlibsbml.ListOf;
import org.sbml.jlibsbml.Model;
import org.sbml.jlibsbml.ModifierSpeciesReference;
import org.sbml.jlibsbml.Parameter;
import org.sbml.jlibsbml.Reaction;
import org.sbml.jlibsbml.SBO;
import org.sbml.jlibsbml.Species;
import org.sbml.jlibsbml.SpeciesReference;
import org.sbml.squeezer.kinetics.BasicKineticLaw;
import org.sbml.squeezer.kinetics.Convenience;
import org.sbml.squeezer.kinetics.ConvenienceIndependent;
import org.sbml.squeezer.kinetics.GeneralizedMassAction;
import org.sbml.squeezer.kinetics.HillEquation;
import org.sbml.squeezer.kinetics.IrrevCompetNonCooperativeEnzymes;
import org.sbml.squeezer.kinetics.IrrevNonModulatedNonInteractingEnzymes;
import org.sbml.squeezer.kinetics.MichaelisMenten;
import org.sbml.squeezer.kinetics.OrderedMechanism;
import org.sbml.squeezer.kinetics.PingPongMechanism;
import org.sbml.squeezer.kinetics.RandomOrderMechanism;
import org.sbml.squeezer.kinetics.ZerothOrderForwardGMAK;
import org.sbml.squeezer.kinetics.ZerothOrderReverseGMAK;
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
	 * A hashtable that contains all settings of how to create kinetic
	 * equations.
	 */
	private Properties settings;
	/**
	 * This list contains all fast reactions of the original model. Since the
	 * fast attribute is still ignored by SBMLsqueezer, we use this list to
	 * decide whether a warning message should be displayed later on.
	 */
	private List<Reaction> listOfFastReactions;
	/**
	 * The column rank of the soichiometric matrix of the original model.
	 */
	private int columnRank = -1;
	/**
	 * 
	 */
	private Model modelOrig;
	/**
	 * A copy of the model that only covers all compartments, all parameters,
	 * and all reactions for which kinetic equations are to be created. Hence,
	 * it also contains all other information needed for that purpose, i.e., all
	 * species that participate as reactants, products, or modifiers in at least
	 * one of these reactions.
	 */
	private Model miniModel;

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
	 * Creates a kinetic law for the given reaction, which can be assigned to
	 * the given reaction.
	 * 
	 * @param reaction
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
	 * @throws IOException
	 * @throws IllegalFormatException
	 */
	public BasicKineticLaw createKineticLaw(Reaction r, Kinetics kinetic,
			boolean reversibility) throws ModificationException,
			RateLawNotApplicableException, IOException, IllegalFormatException {
		Reaction reaction = miniModel.getReaction(r.getId());
		if (reaction == null)
			reaction = r;
		BasicKineticLaw kineticLaw;
		reaction.setReversible(reversibility || reaction.getReversible());
		switch (kinetic) {
		case COMPETETIVE_NON_EXCLUSIVE_INHIB:
			kineticLaw = new IrrevCompetNonCooperativeEnzymes(reaction);
			break;
		case ZEROTH_ORDER_REVERSE_MA:
			kineticLaw = new ZerothOrderReverseGMAK(reaction);
			break;
		case ZEROTH_ORDER_FORWARD_MA:
			kineticLaw = new ZerothOrderForwardGMAK(reaction);
			break;
		case IRREV_NON_MODULATED_ENZYME_KIN:
			kineticLaw = new IrrevNonModulatedNonInteractingEnzymes(reaction);
			break;
		case HILL_EQUATION:
			kineticLaw = new HillEquation(reaction);
			break;
		case ORDERED_MECHANISM:
			kineticLaw = new OrderedMechanism(reaction);
			break;
		case PING_PONG_MECAHNISM:
			kineticLaw = new PingPongMechanism(reaction);
			break;
		case RANDOM_ORDER_MECHANISM:
			kineticLaw = new RandomOrderMechanism(reaction);
			break;
		case MICHAELIS_MENTEN:
			kineticLaw = new MichaelisMenten(reaction);
			break;
		case CONVENIENCE_KINETICS:
			kineticLaw = hasFullColumnRank(modelOrig) ? new Convenience(
					reaction) : new ConvenienceIndependent(reaction);
			break;
		default:
			kineticLaw = new GeneralizedMassAction(reaction);
			break;
		}
		return kineticLaw;
	}

	/**
	 * @throws IOException
	 * @throws RateLawNotApplicableException
	 * @throws ModificationException
	 * @throws IllegalFormatException
	 * 
	 */
	public void generateLaws() throws IllegalFormatException,
			ModificationException, RateLawNotApplicableException, IOException {
		boolean reversibility = ((Boolean) settings
				.get(CfgKeys.TREAT_ALL_REACTIONS_REVERSIBLE)).booleanValue();
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
	public Kinetics[] identifyPossibleReactionTypes(String reactionID)
			throws RateLawNotApplicableException {
		Reaction reaction = miniModel.getReaction(reactionID);
		Set<Kinetics> types = new HashSet<Kinetics>();
		types.add(Kinetics.GENERALIZED_MASS_ACTION);

		if (HillEquation.isApplicable(reaction))
			types.add(Kinetics.HILL_EQUATION);

		int i;
		double stoichiometryLeft = 0d, stoichiometryRight = 0d, stoichiometry;
		boolean stoichiometryIntLeft = true;
		boolean reactionWithGenes = false, reactionWithRNA = false;

		// compute stoichiometric properties
		for (i = 0; i < reaction.getNumReactants(); i++) {
			stoichiometry = reaction.getReactant(i).getStoichiometry();
			stoichiometryLeft += stoichiometry;
			if (((int) stoichiometry) - stoichiometry != 0d)
				stoichiometryIntLeft = false;
			// Transcription or translation?
			Species species = reaction.getReactant(i).getSpeciesInstance();
			if (SBO.isGene(species.getSBOTerm()))
				reactionWithGenes = true;
			else if (SBO.isRNA(species.getSBOTerm()))
				reactionWithRNA = true;
		}

		// boolean stoichiometryIntRight = true;
		for (i = 0; i < reaction.getNumProducts(); i++) {
			stoichiometry = reaction.getProduct(i).getStoichiometry();
			stoichiometryRight += stoichiometry;
			// if (((int) stoichiometry) - stoichiometry != 0.0)
			// stoichiometryIntRight = false;
		}

		// identify types of modifiers
		List<String> nonEnzymeCatalyzers = new LinkedList<String>();
		List<String> inhibitors = new LinkedList<String>();
		List<String> activators = new LinkedList<String>();
		List<String> transActiv = new LinkedList<String>();
		List<String> transInhib = new LinkedList<String>();
		List<String> enzymes = new LinkedList<String>();
		if (reaction.getNumModifiers() > 0) {
			BasicKineticLaw.identifyModifers(reaction, inhibitors, transActiv,
					transInhib, activators, enzymes, nonEnzymeCatalyzers);
		}
		boolean nonEnzyme = ((!((Boolean) settings
				.get(CfgKeys.ALL_REACTIONS_ARE_ENZYME_CATALYZED))
				.booleanValue() && enzymes.size() == 0)
				|| (nonEnzymeCatalyzers.size() > 0)
				|| reaction.getNumProducts() == 0 || (SBO.isEmptySet(reaction
				.getProduct(0).getSpeciesInstance().getSBOTerm())));
		boolean uniUniWithoutModulation = false;

		/*
		 * Assign possible rate laws.
		 */

		// Enzym-Kinetics
		if (!reaction.getReversible() && !nonEnzyme && stoichiometryIntLeft) {
			// stoichiometryIntRight not necessary.
			if ((inhibitors.size() == 0) && (activators.size() == 0)
					&& (stoichiometryLeft > 1))
				types.add(Kinetics.IRREV_NON_MODULATED_ENZYME_KIN);
			if ((stoichiometryLeft == 1d)
					&& ((inhibitors.size() > 1) || (transInhib.size() > 0)))
				types.add(Kinetics.COMPETETIVE_NON_EXCLUSIVE_INHIB);
		}
		if (stoichiometryLeft == 1d) {
			if (stoichiometryRight == 1d) {
				// Uni-Uni: MMK/ConvenienceIndependent (1E/1P)
				Species species = reaction.getReactant(0).getSpeciesInstance();
				if (SBO.isGene(species.getSBOTerm())
						|| (SBO.isEmptySet(species.getSBOTerm()) && (SBO
								.isRNA(reaction.getProduct(0)
										.getSpeciesInstance().getSBOTerm()) || SBO
								.isProtein(reaction.getProduct(0)
										.getSpeciesInstance().getSBOTerm())))) {
					setBoundaryCondition(species, true);
					types.add(Kinetics.HILL_EQUATION);

					// throw exception if false reaction occurs
					if (SBO.isTranslation(reaction.getSBOTerm())
							&& reactionWithGenes)
						throw new RateLawNotApplicableException("Reaction "
								+ reaction.getId()
								+ " must be a transcription.");

				} else if (SBO.isRNA(species.getSBOTerm()))
					types.add(Kinetics.HILL_EQUATION);
				if (!nonEnzyme
						&& !(reaction.getReversible() && (inhibitors.size() > 1 || transInhib
								.size() > 1))) // otherwise potentially
					// identical to convenience
					// kinetics.
					types.add(Kinetics.MICHAELIS_MENTEN);
				if (inhibitors.size() == 0 && activators.size() == 0
						&& transActiv.size() == 0 && transInhib.size() == 0)
					uniUniWithoutModulation = true;
				// throw exception if false reaction occurs

				if (SBO.isTranscription(reaction.getSBOTerm())
						&& reactionWithRNA)
					throw new RateLawNotApplicableException("Reaction "
							+ reaction.getId() + " must be a translation.");

			} else if (!reaction.getReversible() && !nonEnzyme)
				// Products don't matter.
				types.add(Kinetics.MICHAELIS_MENTEN);

		} else if (stoichiometryLeft == 2d) {
			if ((stoichiometryRight == 1d) && !nonEnzyme) {
				// Bi-Uni: ConvenienceIndependent/Ran/Ord/PP (2E/1P)/(2E/2P)
				types.add(Kinetics.RANDOM_ORDER_MECHANISM);
				types.add(Kinetics.ORDERED_MECHANISM);
			} else if ((stoichiometryRight == 2d) && !nonEnzyme) {
				types.add(Kinetics.RANDOM_ORDER_MECHANISM);
				types.add(Kinetics.PING_PONG_MECAHNISM);
				types.add(Kinetics.ORDERED_MECHANISM);
			}
		}
		if (!nonEnzyme
				&& !(uniUniWithoutModulation && hasFullColumnRank(modelOrig)))
			/*
			 * more than 2 types of reacting species or higher stoichiometry or
			 * any other reaction that is possibly catalyzed by an enzyme.
			 */
			types.add((Kinetics.CONVENIENCE_KINETICS));

		if (reactionWithGenes) {
			types.add((Kinetics.ZEROTH_ORDER_FORWARD_MA));
			if (types.contains((Kinetics.GENERALIZED_MASS_ACTION)))
				types.remove((Kinetics.GENERALIZED_MASS_ACTION));
		} else if (types.contains((Kinetics.GENERALIZED_MASS_ACTION))
				&& reaction.getReversible())
			types.add((Kinetics.ZEROTH_ORDER_REVERSE_MA));
		if (!reactionWithGenes && SBO.isTranscription(reaction.getSBOTerm()))
			throw new RateLawNotApplicableException("Reaction "
					+ reaction.getId() + " must not be a transcription.");
		if (!reactionWithRNA && SBO.isTranslation(reaction.getSBOTerm()))
			throw new RateLawNotApplicableException("Reaction "
					+ reaction.getId() + " must not be a translation.");

		// remove not needed entries:
		if ((transActiv.size() == 0) && (transInhib.size() == 0)
				&& (activators.size() == 0) && (enzymes.size() == 0)
				&& (nonEnzymeCatalyzers.size() == 0)) {
			if (reactionWithGenes) {
				types = new HashSet<Kinetics>();
				types.add((Kinetics.ZEROTH_ORDER_FORWARD_MA));
				types.add((Kinetics.HILL_EQUATION));
			} /*
			 * else if (types.contains((Kinetics.HILL_EQUATION)))
			 * types.remove((Kinetics.HILL_EQUATION));
			 */
		} else if (types.contains(Kinetics.HILL_EQUATION)
				&& types.contains(Kinetics.ZEROTH_ORDER_FORWARD_MA)
				&& !reaction.getReversible())
			types.remove(Kinetics.ZEROTH_ORDER_FORWARD_MA);

		if (types.contains(Kinetics.GENERALIZED_MASS_ACTION))
			types.add(Kinetics.ZEROTH_ORDER_FORWARD_MA);
		Kinetics t[] = new Kinetics[types.size()];
		i = 0;
		for (Iterator<Kinetics> iterator = types.iterator(); iterator.hasNext(); i++)
			t[i] = iterator.next();
		Arrays.sort(t);

		return t;
	}

	/**
	 * identify the reactionType for generating the kinetics
	 * 
	 * @param reactionNum
	 */
	public Kinetics identifyReactionType(String reactionID,
			boolean reversibility) throws RateLawNotApplicableException {
		Reaction reaction = miniModel.getReaction(reactionID);
		SpeciesReference specref = reaction.getReactant(0);

		List<String> modActi = new LinkedList<String>();
		List<String> modTActi = new LinkedList<String>();
		List<String> modCat = new LinkedList<String>();
		List<String> modInhib = new LinkedList<String>();
		List<String> modTInhib = new LinkedList<String>();
		List<String> enzymes = new LinkedList<String>();
		BasicKineticLaw.identifyModifers(reaction, modInhib, modTActi,
				modTInhib, modActi, enzymes, modCat);

		Kinetics whichkin = Kinetics.GENERALIZED_MASS_ACTION;
		double stoichiometryLeft = 0d, stoichiometryRight = 0d;

		for (int i = 0; i < reaction.getNumReactants(); i++)
			stoichiometryLeft += reaction.getReactant(i).getStoichiometry();
		for (int i = 0; i < reaction.getNumProducts(); i++)
			stoichiometryRight += reaction.getProduct(i).getStoichiometry();

		// Enzym-Kinetics
		if (((Boolean) settings.get(CfgKeys.ALL_REACTIONS_ARE_ENZYME_CATALYZED))
				.booleanValue()) {
			if (stoichiometryLeft == 1d) {
				if (stoichiometryRight == 1d) {
					// Uni-Uni: MMK/ConvenienceIndependent (1E/1P)
					Species species = specref.getSpeciesInstance();
					if (SBO.isGene(species.getSBOTerm())) {
						setBoundaryCondition(species, true);
						if (SBO.isTranslation(reaction.getSBOTerm()))
							throw new RateLawNotApplicableException("Reaction "
									+ reaction.getId()
									+ " must be a transcription.");
						whichkin = Kinetics.HILL_EQUATION;
					} else if (SBO.isRNA(species.getSBOTerm())) {
						whichkin = Kinetics.HILL_EQUATION;
						if (SBO.isTranscription(reaction.getSBOTerm()))
							throw new RateLawNotApplicableException("Reaction "
									+ reaction.getId()
									+ " must be a translation.");
					} else
						whichkin = Kinetics.valueOf(settings.get(
								CfgKeys.UNI_UNI_TYPE).toString());
				} else if (!reaction.getReversible() && !reversibility) {
					// Products don't matter.
					whichkin = Kinetics.valueOf(settings.get(
							CfgKeys.UNI_UNI_TYPE).toString());
				} else
					whichkin = Kinetics.CONVENIENCE_KINETICS;

			} else if (stoichiometryLeft == 2d) {
				if (stoichiometryRight == 1d) {
					// Bi-Uni: ConvenienceIndependent/Ran/Ord/PP (2E/1P)/(2E/2P)
					whichkin = Kinetics.valueOf(settings.get(
							CfgKeys.BI_UNI_TYPE).toString());
				} else if (stoichiometryRight == 2d) {
					whichkin = Kinetics.valueOf(settings
							.get(CfgKeys.BI_BI_TYPE).toString());
				} else
					whichkin = Kinetics.CONVENIENCE_KINETICS;
			} else
				// more than 2 types of reacting species or higher stoichiometry
				whichkin = Kinetics.CONVENIENCE_KINETICS;

		} else { // Information of enzyme katalysis form SBML.

			// GMAK or Hill equation
			if (enzymes.isEmpty()) {
				switch (reaction.getNumReactants()) {
				case 1:
					if (reaction.getNumProducts() == 1) {
						if (SBO.isEmptySet(reaction.getProduct(0)
								.getSpeciesInstance().getSBOTerm()))
							whichkin = Kinetics.GENERALIZED_MASS_ACTION;
						else if (SBO.isEmptySet(reaction.getReactant(0)
								.getSpeciesInstance().getSBOTerm())
								&& (SBO.isProtein(reaction.getProduct(0)
										.getSpeciesInstance().getSBOTerm()) || SBO
										.isRNA(reaction.getProduct(0)
												.getSpeciesInstance()
												.getSBOTerm()))) {
							whichkin = Kinetics.HILL_EQUATION;
						} else {
							Species species = specref.getSpeciesInstance();
							if (SBO.isGene(species.getSBOTerm())) {
								setBoundaryCondition(species, true);
								whichkin = Kinetics.HILL_EQUATION;
							} else if (SBO.isRNA(species.getSBOTerm()))
								whichkin = Kinetics.HILL_EQUATION;
							else
								whichkin = Kinetics.GENERALIZED_MASS_ACTION;
						}
					} else
						whichkin = Kinetics.GENERALIZED_MASS_ACTION;
					break;
				default:
					whichkin = Kinetics.GENERALIZED_MASS_ACTION;
					break;
				}

			} else { // Enzym-Kinetics
				if (stoichiometryLeft == 1d) {
					if (stoichiometryRight == 1d) { // Uni-Uni:
						// MMK/ConvenienceIndependent
						// (1E/1P)
						Species species = specref.getSpeciesInstance();
						if (SBO.isGene(species.getSBOTerm())) {
							setBoundaryCondition(species, true);
							whichkin = Kinetics.HILL_EQUATION;
						} else if (SBO.isRNA(species.getSBOTerm()))
							whichkin = Kinetics.HILL_EQUATION;
						else
							whichkin = Kinetics.valueOf(settings.get(
									CfgKeys.UNI_UNI_TYPE).toString());

					} else if (!reaction.getReversible() && !reversibility)
						whichkin = Kinetics.valueOf(settings.get(
								CfgKeys.UNI_UNI_TYPE).toString());
					else
						whichkin = Kinetics.CONVENIENCE_KINETICS;
				} else if (stoichiometryLeft == 2d) {
					// Bi-Uni: ConvenienceIndependent/Ran/Ord/PP (2E/1P)/(2E/2P)
					if (stoichiometryRight == 1d) {
						whichkin = Kinetics.valueOf(settings.get(
								CfgKeys.BI_UNI_TYPE).toString());
					} else if (stoichiometryRight == 2d) { // bi-bi kinetics
						whichkin = Kinetics.valueOf(settings.get(
								CfgKeys.BI_BI_TYPE).toString());
					} else
						whichkin = Kinetics.CONVENIENCE_KINETICS;
				} else
					whichkin = Kinetics.CONVENIENCE_KINETICS; // other enzyme
				// catalysed
				// reactions.
			}
		}

		// remove double entries:
		if ((whichkin == Kinetics.HILL_EQUATION) && (modTActi.size() == 0)
				&& (modTInhib.size() == 0)) {
			boolean reactionWithGenes = false;
			for (int i = 0; i < reaction.getNumReactants(); i++) {
				Species species = reaction.getReactant(i).getSpeciesInstance();
				if (SBO.isGene(species.getSBOTerm()))
					reactionWithGenes = true;
			}
			if (reactionWithGenes) {
				boolean transcription = false;
				for (int i = 0; i < reaction.getNumProducts(); i++) {
					Species species = reaction.getProduct(i)
							.getSpeciesInstance();
					if (SBO.isRNA(species.getSBOTerm()))
						transcription = true;
				}
				if (transcription && SBO.isTranslation(reaction.getSBOTerm()))
					throw new RateLawNotApplicableException("Reaction "
							+ reaction.getId() + " must be a transcription.");
			} else {
				boolean reactionWithRNA = false;
				for (int i = 0; i < reaction.getNumReactants(); i++) {
					Species species = reaction.getReactant(i)
							.getSpeciesInstance();
					if (SBO.isRNA(species.getSBOTerm()))
						reactionWithRNA = true;
				}
				if (reactionWithRNA) {
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
					whichkin = Kinetics.HILL_EQUATION;
				else
					whichkin = Kinetics.ZEROTH_ORDER_FORWARD_MA;
			} else
				whichkin = Kinetics.GENERALIZED_MASS_ACTION;
		} else if ((SBO.isTranslation(reaction.getSBOTerm()) || SBO
				.isTranscription(reaction.getSBOTerm()))
				&& !(whichkin == Kinetics.HILL_EQUATION))
			throw new RateLawNotApplicableException("Reaction "
					+ reaction.getId() + " must be a state transition.");

		return whichkin;
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
		for (i = model.getNumParameters() - 1; i > 0; i--) {
			isNeeded = false;
			p = model.getParameter(i);
			/*
			 * Is this parameter necessary for some kinetic law or is this
			 * parameter a conversion factor in some stoichiometric math?
			 */
			for (j = 0; (j < model.getNumReactions()) && !isNeeded; j++) {
				Reaction r = model.getReaction(j);
				if (r.getKineticLaw() != null) {
					if (model.getReaction(j).getKineticLaw().getMath()
							.refersTo(p.getId())) {
						/*
						 * ok, parameter occurs here but there could also be a
						 * local parameter with the same id.
						 */
						boolean contains = false;
						for (k = 0; k < r.getKineticLaw().getNumParameters()
								&& !contains; k++)
							if (r.getKineticLaw().getParameter(k).getId()
									.equals(p.getId()))
								contains = true;
						if (!contains)
							isNeeded = true;
					}
				}
				if (isNeeded)
					break;
				SpeciesReference specRef;
				for (k = 0; k < r.getNumReactants(); k++) {
					specRef = r.getReactant(k);
					if (specRef.getStoichiometryMath() != null) {
						if (specRef.getStoichiometryMath().getMath().refersTo(
								p.getId()))
							isNeeded = true;
					}
				}
				if (isNeeded)
					break;
				for (k = 0; k < r.getNumProducts(); k++) {
					specRef = r.getProduct(k);
					if (specRef.getStoichiometryMath() != null) {
						if (specRef.getStoichiometryMath().getMath().refersTo(
								p.getId()))
							isNeeded = true;
					}
				}
			}

			// is this parameter necessary for some rule?
			for (j = 0; (j < model.getNumRules()) && !isNeeded; j++)
				if (model.getRule(j).getMath().refersTo(p.getId()))
					isNeeded = true;

			// is this parameter necessary for some event?
			for (j = 0; (j < model.getNumEvents()) && !isNeeded; j++)
				for (k = 0; k < model.getEvent(j).getNumEventAssignments(); k++)
					if (model.getEvent(j).getEventAssignment(k).getMath()
							.refersTo(p.getId()))
						isNeeded = true;

			// is this parameter necessary for some function?
			for (j = 0; (j < model.getNumFunctionDefinitions()) && !isNeeded; j++)
				if (model.getFunctionDefinition(j).getMath()
						.refersTo(p.getId()))
					isNeeded = true;

			if (!isNeeded) // is this paraemter necessary at all?
				model.getListOfParameters().remove(i);
		}
		// remove unnecessary local parameters
		for (i = 0; i < model.getNumReactions(); i++) {
			Reaction r = model.getReaction(i);
			KineticLaw law = r.getKineticLaw();
			if (law != null) {
				List<Integer> removeList = new LinkedList<Integer>();
				for (j = 0; j < law.getNumParameters(); j++) {
					p = law.getParameter(j);
					if (!law.getMath().refersTo(p.getId())
					/* || (model.getParameter(p.getId()) != null) */)
						removeList.add(Integer.valueOf(j));
				}
				while (!removeList.isEmpty()) {
					p = law.getParameter(removeList.remove(
							removeList.size() - 1).intValue());
					if (!law.getListOfParameters().remove(p))
						law.removeParameter(p);
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
		int i;
		boolean reversibility = ((Boolean) settings
				.get(CfgKeys.TREAT_ALL_REACTIONS_REVERSIBLE)).booleanValue();
		Reaction reaction = modelOrig.getReaction(kineticLaw
				.getParentSBMLObject().getId());
		if (!(kineticLaw instanceof IrrevNonModulatedNonInteractingEnzymes))
			reaction.setReversible(reversibility || reaction.getReversible());
		if (reaction.getKineticLaw() == null)
			reaction.setKineticLaw(kineticLaw);
		else
			reaction.setKineticLaw(kineticLaw);
		if ((kineticLaw instanceof BasicKineticLaw)
				&& (reaction.getNotesString().length() == 0))
			reaction.setNotes(((BasicKineticLaw) kineticLaw).getName());
		// set the BoundaryCondition to true for Genes if not set anyway:
		for (i = 0; i < reaction.getNumReactants(); i++) {
			Species species = reaction.getReactant(i).getSpeciesInstance();
			if (SBO.isGene(species.getSBOTerm()))
				setBoundaryCondition(species, true);
		}
		for (i = 0; i < reaction.getNumProducts(); i++) {
			Species species = reaction.getProduct(i).getSpeciesInstance();
			if (SBO.isGene(species.getSBOTerm()))
				setBoundaryCondition(species, true);
		}
		storeParamters(reaction);
		return reaction;
	}

	/**
	 * store the generated Kinetics in SBML-File as MathML.
	 */
	public void storeLaws(LawListener l) {
		l.totalNumber(miniModel.getNumReactions());
		for (int i = 0; i < miniModel.getNumReactions(); i++) {
			storeLaw(miniModel.getReaction(i).getKineticLaw());
			l.currentNumber(i);
		}
		removeUnnecessaryParameters(modelOrig);
	}

	/**
	 * TODO: comment missing
	 * 
	 * @param reaction
	 */
	public void storeParamters(Reaction reaction) {
		setInitialConcentrationTo(reaction, 1d);
		KineticLaw kineticLaw = reaction.getKineticLaw();
		ListOf<Parameter> paramListLocal = kineticLaw.getListOfParameters();
		if (((Boolean) settings.get(CfgKeys.ADD_NEW_PARAMETERS_ALWAYS_GLOBALLY))
				.booleanValue())
			for (int paramNum = paramListLocal.size() - 1; paramNum > 0; paramNum--)
				modelOrig.addParameter(paramListLocal.remove(paramNum));
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
				.get(CfgKeys.GENERATE_KINETIC_LAW_FOR_EACH_REACTION))
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
	}
}
