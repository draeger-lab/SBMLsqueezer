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
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.sbmlsqueezer.kinetics;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import jp.sbi.celldesigner.plugin.CellDesignerPlugin;
import jp.sbi.celldesigner.plugin.PluginKineticLaw;
import jp.sbi.celldesigner.plugin.PluginModel;
import jp.sbi.celldesigner.plugin.PluginParameter;
import jp.sbi.celldesigner.plugin.PluginReaction;
import jp.sbi.celldesigner.plugin.PluginSpecies;
import jp.sbi.celldesigner.plugin.PluginSpeciesReference;

import org.sbml.libsbml.ASTNode;
import org.sbml.libsbml.SpeciesReference;
import org.sbmlsqueezer.math.GaussianRank;

/**
 * This class identifies and generates the missin kinetic laws for a the
 * selected model in the given plugin.
 * 
 * @since 1.0
 * @version
 * @author <a href="mailto:Nadine.hassis@gmail.com">Nadine Hassis</a>
 * @author <a href="mailto:andreas.draeger@uni-tuebingen.de">Andreas
 *         Dr&auml;ger</a>
 * @date Aug 1, 2007
 */
public class KineticLawGenerator {
	private PluginModel model;

	private CellDesignerPlugin plugin;

	private HashMap<Integer, String> numAndSpeciesID = new HashMap<Integer, String>();

	private HashMap<String, Integer> speciesIDandNum = new HashMap<String, Integer>();

	private HashMap<String, String> speciesAndODE = new HashMap<String, String>();

	private HashMap<String, String> speciesAndSimpleODE = new HashMap<String, String>();

	private HashMap<String, String> speciesAndSimpleODETeX = new HashMap<String, String>();

	private HashMap<String, String> speciesAndODETeX = new HashMap<String, String>();

	private HashMap<Integer, BasicKineticLaw> reactionNumAndKineticLaw = new HashMap<Integer, BasicKineticLaw>();

	private HashMap<Integer, String> reactionNumAndId = new HashMap<Integer, String>();

	private List<Integer> reactionNumOfNotExistKinetics = new ArrayList<Integer>();

	private List<Integer> reacNumOfExistKinetics = new ArrayList<Integer>();

	/**
	 * Possible values are:
	 * <ul>
	 * <li>1 = generalized mass-action kinetics</li>
	 * <li>2 = Convenience kinetics</li>
	 * <li>3 = Michaelis-Menten kinetics</li>
	 * </ul>
	 */
	private short uniUniType;

	/**
	 * Possible values are:
	 * <ul>
	 * <li>1 = generalized mass-action kinetics</li>
	 * <li>2 = Convenience kinetics</li>
	 * <li>4 = Random Order Michealis Menten kinetics</li>
	 * <li>6 = Ordered</li>
	 * </ul>
	 */
	private short biUniType;

	/**
	 * Possible values are:
	 * <ul>
	 * <li>1 = generalized mass-action kinetics</li>
	 * <li>2 = Convenience kinetics</li>
	 * <li>4 = Random Order Michealis Menten kinetics</li>
	 * <li>5 = Ping-Pong</li>
	 * <li>6 = Ordered</li>
	 * </ul>
	 */
	private short biBiType;

	private boolean considerEachReactionEnzymeCatalysed;

	// private boolean considerAllParametersAsGlobal;

	private boolean generateKineticLawForEachReaction;

	private List<PluginReaction> listOfFastReactions;

	private List<String> listOfPossibleEnzymes;

	private int columnRank = -1;

	private String[] reactionNumAndKineticTeX;

	private String[] reactionNumAndKinetictexName;

	/**
	 * If true all parameters are stored globally for the whole model (default)
	 * else parameters are stored locally for the respective kinetic equation
	 * they belong to.
	 */
	private boolean addAllParametersGlobally = true;

	/**
	 * Generalized mass action kinetics
	 */
	public static final short GENERALIZED_MASS_ACTION = 1;
	/**
	 * Convenience kinetics
	 */
	public static final short CONVENIENCE_KINETICS = 2;
	/**
	 * Michaelis Menten kinetics
	 */
	public static final short MICHAELIS_MENTEN = 3;
	/**
	 * Random order mechanism
	 */
	public static final short RANDOM_ORDER_MECHANISM = 4;
	/**
	 * Ping-pong mechanism
	 */
	public static final short PING_PONG_MECAHNISM = 5;
	/**
	 * Ordered mechanism
	 */
	public static final short ORDERED_MECHANISM = 6;
	/**
	 * Hill equation
	 */
	public static final short HILL_EQUATION = 7;
	/**
	 * Irreversible non-modulation
	 */
	public static final short IRREV_NON_MODULATED_ENZYME_KIN = 8;
	/**
	 * Zeroth order forward generalized mass action kinetics
	 */
	public static final short ZEROTH_ORDER_FORWARD_MA = 9;
	/**
	 * Zeroth order reverse generalized mass action kinetics
	 */
	public static final short ZEROTH_ORDER_REVERSE_MA = 10;
	/**
	 * Competitive non-exclusive, non-cooperative inihibition
	 */
	public static final short COMPETETIVE_NON_EXCLUSIVE_INHIB = 11;

	/**
	 * A default constructor.
	 * 
	 * @param plugin
	 */
	public KineticLawGenerator(CellDesignerPlugin plugin) {
		this.plugin = plugin;
		considerEachReactionEnzymeCatalysed = false;
		generateKineticLawForEachReaction = true;
		uniUniType = GENERALIZED_MASS_ACTION;
		biUniType = GENERALIZED_MASS_ACTION;
		biBiType = GENERALIZED_MASS_ACTION;
		listOfPossibleEnzymes = BasicKineticLaw
				.getDefaultListOfPossibleEnzymes();
		init();
	}

	/**
	 * @param plugin
	 *            An instance of the CellDesigner's plugin interface. *
	 * @param generateKineticForAllReaction
	 *            Indicates weather or not kinetic laws should be generated for
	 *            each reaction even though a there is already an existing rate
	 *            law (true).
	 * @param reversibility
	 *            This flag is needed to specify the reversibility information
	 *            of the given SBML model should be ignored (true) and each
	 *            reaction is to be treated as a reversible reaction.
	 * @param forceAllReactionsAsEnzymeReaction
	 *            Indicates if even reactions without an explicit catalyst
	 *            should be modeled as an enzyme catalysed reaction.
	 * @param uniUniType
	 *            Possible values are:
	 *            <ul>
	 *            <li>1 = generalized mass-action kinetics</li>
	 *            <li>2 = Convenience kinetics</li>
	 *            <li>3 = Michaelis-Menten kinetics</li>
	 *            </ul>
	 * @param biUniType
	 *            Possible values are:
	 *            <ul>
	 *            <li>1 = generalized mass-action kinetics</li>
	 *            <li>2 = Convenience kinetics</li>
	 *            <li>4 = Random Order Michealis Menten kinetics</li>
	 *            <li>6 = Ordered</li>
	 *            </ul>
	 * @param biBiType
	 *            Possible values are:
	 *            <ul>
	 *            <li>1 = generalized mass-action kinetics</li>
	 *            <li>2 = Convenience kinetics</li>
	 *            <li>4 = Random Order Michealis Menten kinetics</li>
	 *            <li>5 = Ping-Pong</li>
	 *            <li>6 = Ordered</li>
	 *            </ul>
	 * @param listOfPossibleEnzymes
	 *            A list which contains the names of all species that are
	 *            accepted to act as enzymes during a reaction. Valid are the
	 *            following entries (upper and lower case is ignored):
	 *            <ul>
	 *            <li>ANTISENSE_RNA</li>
	 *            <li>SIMPLE_MOLECULE</li>
	 *            <li>RECEPTOR</li>
	 *            <li>UNKNOWN</li>
	 *            <li>COMPLEX</li>
	 *            <li>TRUNCATED</li>
	 *            <li>GENERIC</li>
	 *            <li>RNA</li>
	 *            </ul>
	 *            Not allowed entries like <li>Phenotype</li> <li>Gene</li> <li>
	 *            IonChannel</li> <li>Ion</li> </ul> will be filtered out of the
	 *            list.
	 * @throws IllegalFormatException
	 * @throws ModificationException
	 * @throws RateLawNotApplicableException
	 */
	public KineticLawGenerator(CellDesignerPlugin plugin,
			boolean forceAllReactionsAsEnzymeReaction,
			boolean addAllParametersGlobally,
			boolean generateKineticForAllReaction, short uniUniType,
			short biUniType, short biBiType, List<String> listOfPossibleEnzymes)
			throws IllegalFormatException, ModificationException,
			RateLawNotApplicableException {
		this.plugin = plugin;
		this.uniUniType = uniUniType;
		this.biUniType = biUniType;
		this.biBiType = biBiType;
		this.listOfPossibleEnzymes = BasicKineticLaw
				.getDefaultListOfPossibleEnzymes();
		this.listOfPossibleEnzymes.retainAll(listOfPossibleEnzymes);
		considerEachReactionEnzymeCatalysed = forceAllReactionsAsEnzymeReaction;
		generateKineticLawForEachReaction = generateKineticForAllReaction;
		setAddAllParametersGlobally(addAllParametersGlobally);
		init();
	}

	/**
	 * Creates a kinetic law for the given reaction, which can be assigned to
	 * the given reaction.
	 * 
	 * @param model
	 *            The SBML model, which contains the reaction for which a
	 *            kinetic law is to be created.
	 * @param reaction
	 *            The reaction for which a kinetic law is to be created.
	 * @param kinetic
	 *            valid values:
	 *            <ol>
	 *            <li>generalized mass-action</li>
	 *            <li>simple convenience/independent convenience</li>
	 *            <li>Michaelis-Menten</li>
	 *            <li>Random order Michaelis-Menten</li>
	 *            <li>Ping-Pong</li>
	 *            <li>Ordered</li>
	 *            <li>Hill equation</li>
	 *            <li>irreversible non-modulated non-interacting enzyme kinetics
	 *            </li>
	 *            <li>Zeroth order forward mass action kinetics</li>
	 *            <li>Zeroth order reverse mass action kinetics</li>
	 *            <li>Competitive non-exclusive, non-cooperative inihibition</li>
	 *            <ol>
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
	public BasicKineticLaw createKineticLaw(PluginModel model,
			PluginReaction reaction, short kinetic, boolean reversibility)
			throws ModificationException, RateLawNotApplicableException,
			IOException, IllegalFormatException {
		int reactionNum = 0;
		BasicKineticLaw kineticLaw;

		reaction.setReversible(reversibility || reaction.getReversible());

		while ((reactionNum < model.getNumReactions())
				&& (!model.getReaction(reactionNum).getId().equals(
						reaction.getId())))
			reactionNum++;
		if (!reactionNumOfNotExistKinetics.contains(Integer
				.valueOf(reactionNum)))
			reactionNumOfNotExistKinetics.add(Integer.valueOf(reactionNum));
		switch (kinetic) {
		case COMPETETIVE_NON_EXCLUSIVE_INHIB:
			kineticLaw = new IrrevCompetNonCooperativeEnzymes(reaction, model);
			break;
		case ZEROTH_ORDER_REVERSE_MA:
			kineticLaw = new ZerothOrderReverseGMAK(reaction, model,
					listOfPossibleEnzymes);
			break;
		case ZEROTH_ORDER_FORWARD_MA:
			kineticLaw = new ZerothOrderForwardGMAK(reaction, model,
					listOfPossibleEnzymes);
			break;
		case IRREV_NON_MODULATED_ENZYME_KIN:
			kineticLaw = new IrrevNonModulatedNonInteractingEnzymes(reaction,
					model);
			break;
		case HILL_EQUATION:
			kineticLaw = new HillEquation(reaction, model,
					listOfPossibleEnzymes);
			break;
		case ORDERED_MECHANISM:
			kineticLaw = new OrderedMechanism(reaction, model,
					listOfPossibleEnzymes);
			break;
		case PING_PONG_MECAHNISM:
			kineticLaw = new PingPongMechanism(reaction, model,
					listOfPossibleEnzymes);
			break;
		case RANDOM_ORDER_MECHANISM:
			kineticLaw = new RandomOrderMechanism(reaction, model,
					listOfPossibleEnzymes);
			break;
		case MICHAELIS_MENTEN:
			kineticLaw = new MichaelisMenten(reaction, model,
					listOfPossibleEnzymes);
			break;
		case CONVENIENCE_KINETICS:
			kineticLaw = hasFullColumnRank(model) ? new Convenience(reaction,
					model, listOfPossibleEnzymes) : new ConvenienceIndependent(
					reaction, model, listOfPossibleEnzymes);
			break;
		default:
			kineticLaw = new GeneralizedMassAction(reaction, model,
					listOfPossibleEnzymes);
			break;
		}
		reactionNumAndKineticLaw.put(reactionNum, kineticLaw);

		return kineticLaw;
	}

	/**
	 * Returns true if the given model's stoichiometric matrix has full column
	 * rank.
	 * 
	 * @param model
	 * @return
	 */
	private boolean hasFullColumnRank(PluginModel model) {
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
	 * Find reactions with missing KineticLaws and build them
	 * 
	 * @param reversibility
	 * @throws IllegalFormatException
	 * @throws ModificationException
	 * @throws RateLawNotApplicableException
	 * @throws IOException
	 */
	public void findExistingLawsAndGenerateMissingLaws(PluginModel model,
			boolean reversibility) throws IllegalFormatException,
			ModificationException, RateLawNotApplicableException, IOException {
		for (int reactionNum = 0; reactionNum < model.getNumReactions(); reactionNum++) {
			boolean create = false;
			PluginReaction reaction = model.getReaction(reactionNum);

			// Let us find all fast reactions. This feature is currently
			// ignored.
			if (reaction.getFast())
				listOfFastReactions.add(reaction);

			reactionNumAndId.put(reactionNum, reaction.getId());
			PluginKineticLaw kineticLaw = reaction.getKineticLaw();

			if (kineticLaw != null) {
				String formula = kineticLaw.getFormula();
				if (formula.equals("") || formula.equals(" ")) {
					System.err
							.println("Reaction "
									+ reaction.getId()
									+ " in the model has an incorrect format."
									+ "This means there is either an empty kinetic law in this reaction "
									+ "or a kinetic law that only consists of a white space. "
									+ "If you decide not to save this generated model, there is only one "
									+ "solution: "
									+ "open this SBML file in an editor and delete the whole kinetic law."
									+ "SBMLsqueezer ignores this misstake and generates a proper equation. "
									+ "Therfore we recomment that you save this generated model.");
					create = true;
				} else if (!generateKineticLawForEachReaction) {
					reacNumOfExistKinetics.add(reactionNum);
				} else
					create = true;
			} else
				create = true;

			if (create || generateKineticLawForEachReaction) {
				reactionNumOfNotExistKinetics.add(Integer.valueOf(reactionNum));
				createKineticLaw(
						model,
						model.getReaction(reactionNum),
						identifyReactionType(model, reactionNum, reversibility),
						reversibility);
			}
		}
	}

	/*
	 * ------------------------------------------------ -------- generate
	 * kinetics and paramters ------------------------------------------------
	 */

	/**
	 * get HashMap<String,String> generated ODE for the corresponding Species
	 * 
	 * @return
	 */
	public HashMap<String, String> getAllODE() {
		return speciesAndODE;
	}

	/**
	 * get HashMap<Integer,String> get all Species
	 * 
	 * @return
	 */
	public HashMap<Integer, String> getAllSpeciesNumAndIDs() {
		return numAndSpeciesID;
	}

	/**
	 * <ol>
	 * <li>Irreversible non-modulated non-interacting enzyme kinetics</li>
	 * <li>Generalized mass action</li>
	 * <li>Simple convenience/independent convenience</li>
	 * <li>Michaelis-Menten</li>
	 * <li>Random order mechanism</li>
	 * <li>Ping-Pong mechanism</li>
	 * <li>Ordered mechanism</li>
	 * <li>Hill equation</li>
	 * <li>Zeroth order forward/reverse mass action kinetics</li>
	 * <li>Irreversible non-exclusive non-cooperative competitive inihibition</li>
	 * <ol>
	 */
	public String getEquationName(short id) {
		// TODO: impractical to store the names here! Should access the kinetic
		// classes.
		switch (id) {
		case COMPETETIVE_NON_EXCLUSIVE_INHIB:
			return "Irreversible non-exclusive non-cooperative competitive inihibition";
		case ZEROTH_ORDER_REVERSE_MA:
			return "Zeroth order reverse mass action kinetics";
		case ZEROTH_ORDER_FORWARD_MA:
			return "Zeroth order forward mass action kinetics";
		case IRREV_NON_MODULATED_ENZYME_KIN:
			return "Irreversible non-modulated non-interacting reactants";
		case HILL_EQUATION:
			return "Hill equation";
		case ORDERED_MECHANISM:
			return "Ordered mechanism";
		case PING_PONG_MECAHNISM:
			return "Ping-Pong mechanism";
		case RANDOM_ORDER_MECHANISM:
			return "Random order mechanism";
		case MICHAELIS_MENTEN:
			return "Michaelis-Menten";
		case CONVENIENCE_KINETICS:
			return "Convenience kinetics";
		default: // TODO: default?
			return "Generalized mass-action";
		}
	}

	/**
	 * Returns all reactions of the model that have the attribute to be fast.
	 * 
	 * @return Returns all reactions of the model that have the attribute to be
	 *         fast.
	 */
	public List<PluginReaction> getFastReactions() {
		return listOfFastReactions;
	}

	/**
	 * An array, which contains the name of each formula in the model. For
	 * already existing formulas this will be something like "existing kinetic".
	 * 
	 * @return
	 */
	public String[] getKineticLawNames() {
		// TODO: ensure that this returns always a meaningful result!
		return reactionNumAndKinetictexName;
	}

	/**
	 * An array, which contains one formula in LaTeX format for each reaction
	 * number in the model, even for already existing kinetics.
	 * 
	 * @return
	 */
	public String[] getKineticLawsAsTeX() {
		// TODO: ensure that this returns always a meaningful result!
		return reactionNumAndKineticTeX;
	}

	/**
	 * get the the level for the SBML-File
	 * 
	 * @return
	 */
	public int getLevel() {
		return identifyLevel(model);
	}

	/**
	 * Returns the selected Model of the plugin.
	 * 
	 * @return
	 */
	public PluginModel getModel() {
		return plugin.getSelectedModel();
	}

	/**
	 * get HashMap<Integer,String> with reactionNumber and Id for the
	 * corresponding reaction
	 * 
	 * @return
	 */
	public HashMap<Integer, String> getReactionNumAndId() {
		return reactionNumAndId;
	}

	/**
	 * Returns a hash containg the generated kinetic laws for the given ids as
	 * keys. Each kinetic law proviedes a TeX and a C-like String representation
	 * of the kinetic equation as well as a name.
	 * 
	 * @return
	 */
	public HashMap<Integer, BasicKineticLaw> getReactionNumAndKineticLaw() {
		return reactionNumAndKineticLaw;
	}

	/**
	 * get List<Integer> with reactionNumbers for the Reactions without kinetics
	 * 
	 * @return
	 */
	public List<Integer> getReactionNumOfNotExistKinetics() {
		return reactionNumOfNotExistKinetics;
	}

	/**
	 * Returns the code of the given kinetic law.
	 * 
	 * @param kineticLaw
	 * @return
	 */
	public short getReactionType(BasicKineticLaw kineticLaw) {
		if (kineticLaw instanceof HillEquation)
			return HILL_EQUATION;
		if (kineticLaw instanceof OrderedMechanism)
			return ORDERED_MECHANISM;
		if (kineticLaw instanceof PingPongMechanism)
			return PING_PONG_MECAHNISM;
		if (kineticLaw instanceof RandomOrderMechanism)
			return RANDOM_ORDER_MECHANISM;
		if (kineticLaw instanceof MichaelisMenten)
			return MICHAELIS_MENTEN;
		if ((kineticLaw instanceof Convenience)
				|| (kineticLaw instanceof ConvenienceIndependent))
			return CONVENIENCE_KINETICS;
		if (kineticLaw instanceof ZerothOrderForwardGMAK)
			return ZEROTH_ORDER_FORWARD_MA;
		if (kineticLaw instanceof ZerothOrderReverseGMAK)
			return ZEROTH_ORDER_REVERSE_MA;
		if (kineticLaw instanceof IrrevNonModulatedNonInteractingEnzymes)
			return IRREV_NON_MODULATED_ENZYME_KIN;
		if (kineticLaw instanceof IrrevCompetNonCooperativeEnzymes)
			return COMPETETIVE_NON_EXCLUSIVE_INHIB;
		if (kineticLaw instanceof GeneralizedMassAction)
			return GENERALIZED_MASS_ACTION;
		return 0;
	}

	/**
	 * get HashMap<String,String> generated ODE with a simple equation for the
	 * corresponding Species
	 * 
	 * @return
	 */
	public HashMap<String, String> getSpecieAndSimpleODE() {
		return speciesAndSimpleODE;
	}

	/**
	 * get HashMap<String,String> generated ODE in simple Equation and
	 * LaTex-Format for the corresponding Species
	 * 
	 * @return
	 */
	public HashMap<String, String> getSpeciesAndSimpleODETeX() {
		return speciesAndSimpleODETeX;
	}

	/**
	 * Identify the level of the SBML-File
	 */
	public short identifyLevel(PluginModel model) {
		if (model.getNumSpecies() > 0) {
			PluginSpecies species = model.getSpecies(0);
			if (species.getId() != null)
				return 2;
		}
		return 1;
	}

	/**
	 * <ul>
	 * <li>1 = generalized mass action kinetics</li> <li>2 = Convenience
	 * kinetics</li> <li>3 = Michaelis-Menten kinetics</li> <li>4 = Random Order
	 * ternary kinetics</li> <li>5 = Ping-Pong</li> <li>6 = Ordered</li> <li>7 =
	 * Hill equation</li> <li>8 = Irreversible non-modulated non-interacting
	 * enzyme kinetics</li> <li>9 = Zeroth order forward mass action kinetics
	 * </li> <li>10 = Zeroth order reverse mass action kinetics</li> <li>11 =
	 * Competitive non-exclusive, non-cooperative inihibition</li>
	 * </ul>
	 * 
	 * @return Returns a sorted array of possible kinetic equations for the
	 *         given reaction in the model.
	 */
	public short[] identifyPossibleReactionTypes(PluginModel model,
			PluginReaction reaction) throws RateLawNotApplicableException {

		Set<Short> types = new HashSet<Short>();
		types.add(Short.valueOf(GENERALIZED_MASS_ACTION));

		if (HillEquation.isApplicable(reaction))
			types.add(Short.valueOf(HILL_EQUATION));

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
			PluginSpecies species = reaction.getReactant(i)
					.getSpeciesInstance();
			if (species.getSpeciesAlias(0).getType().equals("GENE"))
				reactionWithGenes = true;
			else if (species.getSpeciesAlias(0).getType().equals("RNA"))
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
		Vector<String> nonEnzymeCatalyzers = new Vector<String>();
		Vector<String> inhibitors = new Vector<String>();
		Vector<String> activators = new Vector<String>();
		Vector<String> transActiv = new Vector<String>();
		Vector<String> transInhib = new Vector<String>();
		Vector<String> enzymes = new Vector<String>();
		BasicKineticLaw.identifyModifers(reaction, BasicKineticLaw
				.getDefaultListOfPossibleEnzymes(), inhibitors, transActiv,
				transInhib, activators, enzymes, nonEnzymeCatalyzers);
		boolean nonEnzyme = ((nonEnzymeCatalyzers.size() > 0) || (reaction
				.getProduct(0).getSpeciesInstance().getSpeciesAlias(0)
				.getType().toUpperCase().equals("DEGRADED"))) ? true : false;
		boolean uniUniWithoutModulation = false;

		/*
		 * Assign possible rate laws.
		 */

		// Enzym-Kinetics
		if (!reaction.getReversible() && !nonEnzyme && stoichiometryIntLeft) {
			// stoichiometryIntRight not necessary.
			if ((inhibitors.size() == 0) && (activators.size() == 0)
					&& (stoichiometryLeft > 1))
				types.add(Short.valueOf(IRREV_NON_MODULATED_ENZYME_KIN));
			if ((stoichiometryLeft == 1d)
					&& ((inhibitors.size() > 1) || (transInhib.size() > 0)))
				types.add(Short.valueOf(COMPETETIVE_NON_EXCLUSIVE_INHIB));
		}
		if (stoichiometryLeft == 1d) {
			if (stoichiometryRight == 1d) {
				// Uni-Uni: MMK/ConvenienceIndependent (1E/1P)
				PluginSpecies species = reaction.getReactant(0)
						.getSpeciesInstance();
				if (species.getSpeciesAlias(0).getType().equals("GENE")
						|| (species.getSpeciesAlias(0).getType().equals(
								"DEGRADED") && (reaction.getProduct(0)
								.getSpeciesInstance().getSpeciesAlias(0)
								.getType().equals("RNA") || reaction
								.getProduct(0).getSpeciesInstance()
								.getSpeciesAlias(0).getType().equals("PROTEIN")))) {
					setBoundaryCondition(species.getId(), true);
					types.add(Short.valueOf(HILL_EQUATION));

					// throw exception if false reaction occurs
					if (reaction.getReactionType().equals("TRANSLATION")
							&& reactionWithGenes)
						throw new RateLawNotApplicableException("Reaction "
								+ reaction.getId()
								+ " must be a transcription.");

				} else if (species.getSpeciesAlias(0).getType().equals("RNA"))
					types.add(Short.valueOf(HILL_EQUATION));
				if (!nonEnzyme
						&& !(reaction.getReversible() && (inhibitors.size() > 1 || transInhib
								.size() > 1))) // otherwise potentially
					// identical to convenience
					// kinetics.
					types.add(Short.valueOf(MICHAELIS_MENTEN));
				if (inhibitors.size() == 0 && activators.size() == 0
						&& transActiv.size() == 0 && transInhib.size() == 0)
					uniUniWithoutModulation = true;
				// throw exception if false reaction occurs

				if (reaction.getReactionType().equals("TRANSCRIPTION")
						&& reactionWithRNA)
					throw new RateLawNotApplicableException("Reaction "
							+ reaction.getId() + " must be a translation.");

			} else if (!reaction.getReversible() && !nonEnzyme)
				// Products don't matter.
				types.add(Short.valueOf(MICHAELIS_MENTEN));

		} else if (stoichiometryLeft == 2d) {
			if ((stoichiometryRight == 1d) && !nonEnzyme) {
				// Bi-Uni: ConvenienceIndependent/Ran/Ord/PP (2E/1P)/(2E/2P)
				types.add(Short.valueOf(RANDOM_ORDER_MECHANISM));
				types.add(Short.valueOf(ORDERED_MECHANISM));
			} else if ((stoichiometryRight == 2d) && !nonEnzyme) {
				types.add(Short.valueOf(RANDOM_ORDER_MECHANISM));
				types.add(Short.valueOf(PING_PONG_MECAHNISM));
				types.add(Short.valueOf(ORDERED_MECHANISM));
			}
		}
		if (!nonEnzyme && !(uniUniWithoutModulation && hasFullColumnRank(model)))
			/*
			 * more than 2 types of reacting species or higher stoichiometry or
			 * any other reaction that is possibly catalyzed by an enzyme.
			 */
			types.add(Short.valueOf(CONVENIENCE_KINETICS));

		if (reactionWithGenes) {
			types.add(Short.valueOf(ZEROTH_ORDER_FORWARD_MA));
			if (types.contains(Short.valueOf(GENERALIZED_MASS_ACTION)))
				types.remove(Short.valueOf(GENERALIZED_MASS_ACTION));
		} else if (types.contains(Short.valueOf(GENERALIZED_MASS_ACTION))
				&& reaction.getReversible())
			types.add(Short.valueOf(ZEROTH_ORDER_REVERSE_MA));
		if (!reactionWithGenes
				&& reaction.getReactionType().equals("TRANSCRIPTION"))
			throw new RateLawNotApplicableException("Reaction "
					+ reaction.getId() + " must not be a transcription.");
		if (!reactionWithRNA
				&& reaction.getReactionType().equals("TRANSLATION"))
			throw new RateLawNotApplicableException("Reaction "
					+ reaction.getId() + " must not be a translation.");

		// remove not needed entries:
		if ((transActiv.size() == 0) && (transInhib.size() == 0)
				&& (activators.size() == 0) && (enzymes.size() == 0)
				&& (nonEnzymeCatalyzers.size() == 0)) {
			if (reactionWithGenes) {
				types = new HashSet<Short>();
				types.add(Short.valueOf(ZEROTH_ORDER_FORWARD_MA));
				types.add(Short.valueOf(HILL_EQUATION));
			} /*
			 * else if (types.contains(Short.valueOf(HILL_EQUATION)))
			 * types.remove(Short.valueOf(HILL_EQUATION));
			 */
		} else if (types.contains(Short.valueOf(HILL_EQUATION))
				&& types.contains(Short.valueOf(ZEROTH_ORDER_FORWARD_MA))
				&& !reaction.getReversible())
			types.remove(Short.valueOf(ZEROTH_ORDER_FORWARD_MA));

		if (types.contains(Short.valueOf(GENERALIZED_MASS_ACTION)))
			types.add(Short.valueOf(ZEROTH_ORDER_FORWARD_MA));
		short t[] = new short[types.size()];
		i = 0;
		for (Iterator<Short> iterator = types.iterator(); iterator.hasNext(); i++)
			t[i] = iterator.next().shortValue();
		Arrays.sort(t);

		return t;
	}

	/**
	 * identify the reactionType for generating the kinetics
	 * 
	 * @param reactionNum
	 */
	public short identifyReactionType(PluginModel model, int reactionNum,
			boolean reversibility) throws RateLawNotApplicableException {

		PluginReaction reaction = model.getReaction(reactionNum);
		PluginSpeciesReference specref = reaction.getReactant(0);

		ArrayList<String> modActi = new ArrayList<String>(), modTActi = new ArrayList<String>(), modCat = new ArrayList<String>(), modInhib = new ArrayList<String>(), modTInhib = new ArrayList<String>(), enzymes = new ArrayList<String>();

		BasicKineticLaw.identifyModifers(reaction, listOfPossibleEnzymes,
				modInhib, modTActi, modTInhib, modActi, enzymes, modCat);

		short whichkin = GENERALIZED_MASS_ACTION;
		double stoichiometryLeft = 0d, stoichiometryRight = 0d;

		for (int i = 0; i < reaction.getNumReactants(); i++)
			stoichiometryLeft += reaction.getReactant(i).getStoichiometry();
		for (int i = 0; i < reaction.getNumProducts(); i++)
			stoichiometryRight += reaction.getProduct(i).getStoichiometry();

		// Enzym-Kinetics
		if (considerEachReactionEnzymeCatalysed) {
			if (stoichiometryLeft == 1d) {
				if (stoichiometryRight == 1d) {
					// Uni-Uni: MMK/ConvenienceIndependent (1E/1P)
					PluginSpecies species = specref.getSpeciesInstance();
					if (species.getSpeciesAlias(0).getType().equals("GENE")) {
						setBoundaryCondition(species.getId(), true);

						if (reaction.getReactionType().equals("TRANSLATION"))
							throw new RateLawNotApplicableException("Reaction "
									+ reaction.getId()
									+ " must be a transcription.");

						whichkin = HILL_EQUATION;
					} else if (species.getSpeciesAlias(0).getType().equals(
							"RNA")) {
						whichkin = HILL_EQUATION;
						if (reaction.getReactionType().equals("TRANSCRIPTION"))
							throw new RateLawNotApplicableException("Reaction "
									+ reaction.getId()
									+ " must be a translation.");

					} else
						whichkin = uniUniType;
				} else if (!reaction.getReversible() && !reversibility) {
					// Products don't matter.
					whichkin = uniUniType;
				} else
					whichkin = CONVENIENCE_KINETICS;

			} else if (stoichiometryLeft == 2d) {
				if (stoichiometryRight == 1d) {
					// Bi-Uni: ConvenienceIndependent/Ran/Ord/PP (2E/1P)/(2E/2P)
					whichkin = biUniType;
				} else if (stoichiometryRight == 2d) {
					whichkin = biBiType;
				} else
					whichkin = CONVENIENCE_KINETICS;
			} else
				// more than 2 types of reacting species or higher stoichiometry
				whichkin = CONVENIENCE_KINETICS;

		} else { // Information of enzyme katalysis form SBML.

			// GMAK or Hill equation
			if (enzymes.isEmpty()) {
				switch (reaction.getNumReactants()) {
				case 1:
					if (reaction.getNumProducts() == 1) {
						if (reaction.getProduct(0).getSpeciesInstance()
								.getSpeciesAlias(0).getType().equalsIgnoreCase(
										"DEGRADED"))
							whichkin = GENERALIZED_MASS_ACTION;
						else if (reaction.getReactant(0).getSpeciesInstance()
								.getSpeciesAlias(0).getType().equalsIgnoreCase(
										"DEGRADED")
								&& (reaction.getProduct(0).getSpeciesInstance()
										.getSpeciesAlias(0).getType()
										.equalsIgnoreCase("PROTEIN") || reaction
										.getProduct(0).getSpeciesInstance()
										.getSpeciesAlias(0).getType()
										.equalsIgnoreCase("RNA"))) {
							whichkin = HILL_EQUATION;
						} else {
							int k;
							for (k = 0; (k < model.getNumSpecies())
									&& !model.getSpecies(k).getId().equals(
											specref.getSpecies()); k++)
								;
							PluginSpecies species = model.getSpecies(k);
							if (species.getSpeciesAlias(0).getType().equals(
									"GENE")) {
								setBoundaryCondition(k, true);
								whichkin = HILL_EQUATION;
							} else if (species.getSpeciesAlias(0).getType()
									.equals("RNA"))
								whichkin = HILL_EQUATION;
							else
								whichkin = GENERALIZED_MASS_ACTION;
						}
					} else
						whichkin = GENERALIZED_MASS_ACTION;
					break;
				default:
					whichkin = GENERALIZED_MASS_ACTION;
					break;
				}

			} else { // Enzym-Kinetics
				if (stoichiometryLeft == 1d) {
					if (stoichiometryRight == 1d) { // Uni-Uni:
						// MMK/ConvenienceIndependent
						// (1E/1P)
						PluginSpecies species = specref.getSpeciesInstance();
						if (species.getSpeciesAlias(0).getType().equals("GENE")) {
							setBoundaryCondition(species.getId(), true);
							whichkin = HILL_EQUATION;
						} else if (species.getSpeciesAlias(0).getType().equals(
								"RNA"))
							whichkin = HILL_EQUATION;
						else
							whichkin = uniUniType;

					} else if (!reaction.getReversible() && !reversibility)
						whichkin = uniUniType;
					else
						whichkin = CONVENIENCE_KINETICS;
				} else if (stoichiometryLeft == 2d) {
					// Bi-Uni: ConvenienceIndependent/Ran/Ord/PP (2E/1P)/(2E/2P)
					if (stoichiometryRight == 1d) {
						whichkin = biUniType;
					} else if (stoichiometryRight == 2d) { // bi-bi kinetics
						whichkin = biBiType;
					} else
						whichkin = CONVENIENCE_KINETICS;
				} else
					whichkin = CONVENIENCE_KINETICS; // other enzyme catalysed
				// reactions.
			}
		}

		// remove double entries:
		if ((whichkin == HILL_EQUATION) && (modTActi.size() == 0)
				&& (modTInhib.size() == 0)) {
			boolean reactionWithGenes = false;
			for (int i = 0; i < reaction.getNumReactants(); i++) {
				PluginSpecies species = reaction.getReactant(i)
						.getSpeciesInstance();
				if (species.getSpeciesAlias(0).getType().equals("GENE"))
					reactionWithGenes = true;
			}
			if (reactionWithGenes) {
				boolean transcription = false;
				for (int i = 0; i < reaction.getNumProducts(); i++) {
					PluginSpecies species = reaction.getProduct(i)
							.getSpeciesInstance();
					if (species.getSpeciesAlias(0).getType().equals("RNA"))
						transcription = true;
				}
				if (transcription
						&& reaction.getReactionType().equals("TRANSLATION"))
					throw new RateLawNotApplicableException("Reaction "
							+ reaction.getId() + " must be a transcription.");
			} else {
				boolean reactionWithRNA = false;
				for (int i = 0; i < reaction.getNumReactants(); i++) {
					PluginSpecies species = reaction.getReactant(i)
							.getSpeciesInstance();
					if (species.getSpeciesAlias(0).getType().equals("RNA"))
						reactionWithRNA = true;
				}
				if (reactionWithRNA) {
					boolean translation = false;
					for (int i = 0; i < reaction.getNumProducts(); i++) {
						PluginSpecies species = reaction.getProduct(i)
								.getSpeciesInstance();
						if (!species.getSpeciesAlias(0).getType().equals(
								"PROTEIN"))
							translation = true;
					}
					if (reaction.getReactionType().equals("TRANSCRIPTION")
							&& translation)
						throw new RateLawNotApplicableException("Reaction "
								+ reaction.getId() + " must be a translation.");
				}
			}
			if (!reaction.getReversible() || reactionWithGenes) {
				if ((0 < modActi.size()) || (0 < modInhib.size())
						|| (0 < modCat.size()) || (0 < enzymes.size()))
					whichkin = HILL_EQUATION;
				else
					whichkin = ZEROTH_ORDER_FORWARD_MA;
			} else
				whichkin = GENERALIZED_MASS_ACTION;
		} else if ((reaction.getReactionType().equals("TRANSLATION") || reaction
				.getReactionType().equals("TRANSCRIPTION"))
				&& !(whichkin == HILL_EQUATION))
			throw new RateLawNotApplicableException("Reaction "
					+ reaction.getId() + " must be a state transition.");

		return whichkin;
	}

	/**
	 * If true parameters are always stored globally.
	 * 
	 * @return
	 */
	public boolean isAddAllParametersGlobally() {
		return addAllParametersGlobally;
	}

	/**
	 * Delete unnecessary paremeters from the model. A parameter is defined to
	 * be unnecessary if and only if no kinetic law, no event assignment, no
	 * rule and no function makes use of this parameter.
	 * 
	 * @param selectedModel
	 */
	public void removeUnnecessaryParameters(CellDesignerPlugin plugin) {
		boolean isNeeded;
		int i, j, k;
		PluginModel model = plugin.getSelectedModel();
		PluginParameter p;
		// remove unnecessary global parameters
		for (i = 0; i < model.getNumParameters(); i++) {
			isNeeded = false;
			p = model.getParameter(i);
			/*
			 * Is this parameter necessary for some kinetic law or is this
			 * parameter a conversion factor in some stoichiometric math?
			 */
			for (j = 0; (j < model.getNumReactions()) && !isNeeded; j++) {
				PluginReaction r = model.getReaction(j);
				if (r.getKineticLaw() != null)
					if (contains(
							model.getReaction(j).getKineticLaw().getMath(), p
									.getId()))
						isNeeded = true;
				if (isNeeded)
					break;
				PluginSpeciesReference specRef;
				for (k = 0; k < r.getNumReactants(); k++) {
					specRef = r.getReactant(k);
					if (specRef.getStoichiometryMath() != null) {
						if (contains(specRef.getStoichiometryMath().getMath(),
								p.getId()))
							isNeeded = true;
					}
				}
				if (isNeeded)
					break;
				for (k = 0; k < r.getNumProducts(); k++) {
					specRef = r.getProduct(k);
					if (specRef.getStoichiometryMath() != null) {
						if (contains(specRef.getStoichiometryMath().getMath(),
								p.getId()))
							isNeeded = true;
					}
				}
			}

			// is this parameter necessary for some rule?
			for (j = 0; (j < model.getNumRules()) && !isNeeded; j++)
				if (contains(model.getRule(j).getMath(), p.getId()))
					isNeeded = true;

			// is this parameter necessary for some event?
			for (j = 0; (j < model.getNumEvents()) && !isNeeded; j++)
				for (k = 0; k < model.getEvent(j).getNumEventAssignments(); k++)
					if (contains(model.getEvent(j).getEventAssignment(k)
							.getMath(), p.getId()))
						isNeeded = true;

			// is this parameter necessary for some function?
			for (j = 0; (j < model.getNumFunctionDefinitions()) && !isNeeded; j++)
				if (contains(model.getFunctionDefinition(j).getMath(), p
						.getId()))
					isNeeded = true;

			if (!isNeeded) // is this paraemter necessary at all?
				plugin.notifySBaseDeleted(model.getListOfParameters().remove(
						i--));
		}
		// remove unnecessary local parameters
		for (i = 0; i < model.getNumReactions(); i++) {
			PluginKineticLaw law = model.getReaction(i).getKineticLaw();
			if (law != null) {
				List<Integer> removeList = new Vector<Integer>();
				for (j = 0; j < law.getNumParameters(); j++) {
					p = law.getParameter(j);
					if (!contains(law.getMath(), p.getId())
							|| (model.getParameter(p.getId()) != null))
						removeList.add(Integer.valueOf(j));
				}
				while (!removeList.isEmpty()) {
					p = law.getParameter(removeList.remove(
							removeList.size() - 1).intValue());
					if (!law.getListOfParameters().remove(p))
						law.removeParameter(p);
					plugin.notifySBaseDeleted(p);
				}
			}
		}
	}

	/**
	 * If true parameters are always stored globally, otherwise locally.
	 * 
	 * @param addAllParametersGlobally
	 */
	public void setAddAllParametersGlobally(boolean addAllParametersGlobally) {
		this.addAllParametersGlobally = addAllParametersGlobally;
	}

	/**
	 * set the boundaryCondition for a gen to the given value
	 * 
	 * @param speciesNum
	 */
	public void setBoundaryCondition(int speciesNum, boolean condition) {
		setBoundaryCondition(plugin.getSelectedModel().getSpecies(speciesNum)
				.getId(), condition);
	}

	/**
	 * set the boundaryCondition for a gen to the given value
	 * 
	 * @param speciesNum
	 */
	public void setBoundaryCondition(String speciesID, boolean condition) {
		PluginSpecies species = plugin.getSelectedModel().getSpecies(speciesID);
		if (condition != species.getBoundaryCondition()) {
			System.out
					.println("\nI am setting the boundary condition for species "
							+ speciesID + " to " + condition);
			species.setBoundaryCondition(condition);
			System.out.println("Boundary condition was now set to "
					+ species.getBoundaryCondition());
			plugin.notifySBaseChanged(species);
			System.out
					.println("I notified the plugin and now the boundary condition is "
							+ species.getBoundaryCondition());
		}
	}

	/**
	 * Computes the stoichiometric matrix of the model system.
	 * 
	 * @return
	 */
	public double[][] stoechMatrix(PluginModel model) {
		double[][] N = new double[model.getNumSpecies()][model
				.getNumReactions()];
		int reactionNum, speciesNum;
		PluginSpeciesReference speciesRef;
		for (reactionNum = 0; reactionNum < model.getNumReactions(); reactionNum++) {
			PluginReaction reaction = model.getReaction(reactionNum);
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
	 * set the generated kinetics and paramters in corresponding HashMaps
	 * 
	 * @param reversibility
	 */
	public void storeKineticsAndParameters(CellDesignerPlugin plugin,
			boolean reversibility, LawListener l) {
		l.totalNumber(reactionNumOfNotExistKinetics.size() + 11);
		storeLaws(plugin, reversibility, l);
		ODE ode = new ODE(plugin.getSelectedModel(), numAndSpeciesID,
				speciesAndODE, speciesAndODETeX, reactionNumOfNotExistKinetics,
				reacNumOfExistKinetics, reactionNumAndKineticLaw);
		l.currentNumber(reactionNumOfNotExistKinetics.size() + 1);
		speciesAndODETeX.clear();
		l.currentNumber(reactionNumOfNotExistKinetics.size() + 2);
		speciesAndODE.clear();
		l.currentNumber(reactionNumOfNotExistKinetics.size() + 3);
		speciesAndODE.clear();
		l.currentNumber(reactionNumOfNotExistKinetics.size() + 4);
		speciesAndODETeX.clear();
		l.currentNumber(reactionNumOfNotExistKinetics.size() + 5);
		speciesAndODE.putAll(ode.getAllODEs());
		l.currentNumber(reactionNumOfNotExistKinetics.size() + 6);
		speciesAndODETeX.putAll(ode.getAllODETeX());
		l.currentNumber(reactionNumOfNotExistKinetics.size() + 7);
		speciesAndSimpleODE.putAll(ode.getSpecieAndSimpleODE());
		l.currentNumber(reactionNumOfNotExistKinetics.size() + 8);
		speciesAndSimpleODETeX.putAll(ode.getSpeciesAndSimpleODETeX());
		l.currentNumber(reactionNumOfNotExistKinetics.size() + 9);
		reactionNumAndKineticTeX = ode.getReactionNumAndKinetictexId();
		l.currentNumber(reactionNumOfNotExistKinetics.size() + 10);
		reactionNumAndKinetictexName = ode.getKineticLawNames();
		l.currentNumber(reactionNumOfNotExistKinetics.size() + 11);
	}

	/**
	 * This method stores a kinetic law for the given reaction in the currently
	 * selected model given by the plugin. The kinetic law is passed as a String
	 * to this method. A boolean variable tells this method weather the formula
	 * is for a reversible or for an irreversible reaction. Afterwards all
	 * parameters within this kinetic law are also stored in the given model.
	 * There is no need to call the storeParameters method.
	 * 
	 * @param plugin
	 *            The plugin, which contains the model.
	 * @param reactionNum
	 *            The number of the reaction with the currently selected model
	 *            in the plugin.
	 * @param kineticLaw
	 *            A string with the formula to be assigned to the given
	 *            reaction.
	 */
	public PluginReaction storeLaw(CellDesignerPlugin plugin,
			PluginKineticLaw kineticLaw, boolean reversibility) {
		int i;
		PluginReaction reaction = kineticLaw.getParentReaction();
		if (!(kineticLaw instanceof IrrevNonModulatedNonInteractingEnzymes)) {
			reaction.setReversible(reversibility || reaction.getReversible());
			plugin.notifySBaseChanged(reaction);
		}
		if (reaction.getKineticLaw() == null) {
			reaction.setKineticLaw(kineticLaw);
			plugin.notifySBaseAdded(reaction.getKineticLaw());
		} else {
			reaction.setKineticLaw(kineticLaw);
			plugin.notifySBaseChanged(kineticLaw);
		}
		if ((kineticLaw instanceof BasicKineticLaw)
				&& (reaction.getNotesString().length() == 0)) {
			reaction.setNotes(((BasicKineticLaw) kineticLaw).getName());
			plugin.notifySBaseChanged(reaction);
		}
		// if (kineticLaw.getMath() == null) {
		// reaction.getKineticLaw().setMathFromFormula();
		// plugin.notifySBaseChanged(reaction.getKineticLaw());
		// }
		// set the BoundaryCondition to true for Genes if not set anyway:
		for (i = 0; i < reaction.getNumReactants(); i++) {
			PluginSpecies species = reaction.getReactant(i)
					.getSpeciesInstance();
			if (species.getSpeciesAlias(0).getType().equals("GENE")) {
				setBoundaryCondition(species.getId(), true);
				System.out.println("BoundaryCondition was set to "
						+ species.getBoundaryCondition() + " for species "
						+ species.getId());
			}
		}
		for (i = 0; i < reaction.getNumProducts(); i++) {
			PluginSpecies species = reaction.getProduct(0).getSpeciesInstance();
			if (species.getSpeciesAlias(0).getType().equals("GENE")) {
				setBoundaryCondition(species.getId(), true);
				System.out.println("BoundaryCondition was set to "
						+ species.getBoundaryCondition() + " for species "
						+ species.getId());
			}
		}
		storeParamters(plugin.getSelectedModel(), reaction);
		return reaction;
	}

	/**
	 * store the generated Kinetics in SBML-File as MathML.
	 */
	public void storeLaws(CellDesignerPlugin plugin, boolean reversibility,
			LawListener l) {
		for (int i = 0; i < reactionNumOfNotExistKinetics.size(); i++) {
			storeLaw(plugin, reactionNumAndKineticLaw
					.get(reactionNumOfNotExistKinetics.get(i)), reversibility);
			l.currentNumber(i);
		}
		removeUnnecessaryParameters(plugin);
	}

	/**
	 * TODO: comment missing
	 * 
	 * @param model
	 * @param reaction
	 */
	public void storeParamters(PluginModel model, PluginReaction reaction) {
		setInitialConcentrationTo(model, reaction, 1d);
		BasicKineticLaw kineticLaw = (BasicKineticLaw) reaction.getKineticLaw();
		List<StringBuffer> paramListLocal = kineticLaw.getLocalParameters();
		List<StringBuffer> paramListGlobal = kineticLaw.getGlobalParameters();
		int paramNumber, i;
		for (paramNumber = 0; paramNumber < paramListLocal.size(); paramNumber++) {
			PluginParameter para;
			if (addAllParametersGlobally) {
				para = new PluginParameter(paramListLocal.get(paramNumber)
						.toString(), model);
				para.setValue(1d);
				if (model.getParameter(para.getId()) == null) {
					model.addParameter(para);
					plugin.notifySBaseAdded(para);
				}
			} else {
				int contains = -1;
				para = new PluginParameter(paramListLocal.get(paramNumber)
						.toString(), kineticLaw);
				para.setValue(1d);
				for (i = 0; (i < kineticLaw.getNumParameters())
						&& (contains < 0); i++)
					if (kineticLaw.getParameter(i).getId().equals(para.getId()))
						contains = i;
				if (contains < 0) {
					kineticLaw.addParameter(para);
					plugin.notifySBaseAdded(para);
				}
			}
		}
		for (paramNumber = 0; paramNumber < paramListGlobal.size(); paramNumber++) {
			PluginParameter para = new PluginParameter(paramListGlobal.get(
					paramNumber).toString(), model);
			para.setValue(1d);
			if (model.getParameter(para.getId()) == null) {
				model.addParameter(para);
				plugin.notifySBaseAdded(para);
			}
		}
	}

	/**
	 * Returns true if the given astnode or one of its descendents contains some
	 * identifier with the given id. This method can be used to scan a formula
	 * and for a specific parameter or species and detect weather this component
	 * is used by this formula. This search is done using a DFS.
	 * 
	 * @param astnode
	 * @param id
	 * @return
	 */
	private boolean contains(ASTNode astnode, String id) {
		if (astnode.isName())
			if (astnode.getName().toLowerCase().equals(id))
				return true;
		boolean childContains = false;
		for (int i = 0; i < astnode.getNumChildren(); i++)
			childContains = childContains || contains(astnode.getChild(i), id);
		return childContains;
	}

	private void init() {
		model = plugin.getSelectedModel();
		listOfFastReactions = new Vector<PluginReaction>();

		if ((uniUniType < GENERALIZED_MASS_ACTION)
				|| (MICHAELIS_MENTEN < uniUniType))
			uniUniType = GENERALIZED_MASS_ACTION;
		if ((biUniType < GENERALIZED_MASS_ACTION)
				|| (biUniType == MICHAELIS_MENTEN)
				|| (biUniType == PING_PONG_MECAHNISM)
				|| (ORDERED_MECHANISM < biUniType))
			biUniType = GENERALIZED_MASS_ACTION;
		if ((biBiType < GENERALIZED_MASS_ACTION)
				|| (biBiType == MICHAELIS_MENTEN)
				|| (ORDERED_MECHANISM < biBiType))
			biBiType = GENERALIZED_MASS_ACTION;

		short level = identifyLevel(model);
		for (int i = 0; i < model.getNumSpecies(); i++) {
			PluginSpecies species = model.getSpecies(i);
			speciesAndODE.put(species.getName(), "");
			speciesAndODETeX.put(species.getName(), "");
			switch (level) {
			case 1:
				numAndSpeciesID.put(i, species.getName());
				speciesIDandNum.put(species.getName(), i);
				break;
			case 2:
				numAndSpeciesID.put(i, species.getId());
				speciesIDandNum.put(species.getId(), i);
				break;
			default:
				break;
			}
		}

	}

	/**
	 * Sets the initial amounts of all modifiers, reactants and products to the
	 * specified value.
	 * 
	 * @param reaction
	 * @param initialValue
	 */
	private void setInitialConcentrationTo(PluginModel model,
			PluginReaction reaction, double initialValue) {
		for (int reactant = 0; reactant < reaction.getNumReactants(); reactant++) {
			PluginSpecies species = reaction.getReactant(reactant)
					.getSpeciesInstance();
			if (species.getInitialConcentration() == 0.0) {
				species.setInitialConcentration(initialValue);
				species.setHasOnlySubstanceUnits(false);
				plugin.notifySBaseChanged(species);
			} else if (!species.getHasOnlySubstanceUnits()) {
				species.setHasOnlySubstanceUnits(false);
				plugin.notifySBaseChanged(species);
			}
		}
		for (int product = 0; product < reaction.getNumProducts(); product++) {
			PluginSpecies species = reaction.getProduct(product)
					.getSpeciesInstance();
			if (species.getInitialConcentration() == 0.0) {
				species.setInitialConcentration(initialValue);
				species.setHasOnlySubstanceUnits(false);
				plugin.notifySBaseChanged(species);
			} else if (!species.getHasOnlySubstanceUnits()) {
				species.setHasOnlySubstanceUnits(false);
				plugin.notifySBaseChanged(species);
			}
		}
		for (int modifier = 0; modifier < reaction.getNumModifiers(); modifier++) {
			PluginSpecies species = reaction.getModifier(modifier)
					.getSpeciesInstance();
			if (species.getInitialConcentration() == 0.0) {
				species.setInitialConcentration(initialValue);
				species.setHasOnlySubstanceUnits(false);
				plugin.notifySBaseChanged(species);
			} else if (!species.getHasOnlySubstanceUnits()) {
				species.setHasOnlySubstanceUnits(false);
				plugin.notifySBaseChanged(species);
			}
		}
	}

}
