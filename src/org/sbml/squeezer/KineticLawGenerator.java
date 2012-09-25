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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBO;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.squeezer.kinetics.BasicKineticLaw;
import org.sbml.squeezer.kinetics.TypeStandardVersion;
import org.sbml.squeezer.math.GaussianRank;
import org.sbml.squeezer.util.Bundles;
import org.sbml.squeezer.util.ModelChangeListener;
import org.sbml.squeezer.util.ProgressAdapter;
import org.sbml.squeezer.util.ProgressAdapter.TypeOfProgress;

import de.zbit.sbml.util.SBMLtools;
import de.zbit.util.ResourceManager;
import de.zbit.util.prefs.Option;
import de.zbit.util.prefs.SBPreferences;
import de.zbit.util.progressbar.AbstractProgressBar;

/**
 * This class identifies and generates the missing kinetic laws for a the
 * selected model in the given plug-in.
 * 
 * @since 1.0
 * @version $Rev$
 * @author Nadine Hassis
 * @author Andreas Dr&auml;ger
 * @author Sarah R. M&uuml;ller vom Hagen
 * @date Aug 1, 2007
 */
public class KineticLawGenerator {
	
	public static final transient ResourceBundle MESSAGES = ResourceManager.getBundle(Bundles.MESSAGES);
	public static final transient ResourceBundle WARNINGS = ResourceManager.getBundle(Bundles.WARNINGS);
	
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

	private ProgressAdapter progressAdapter = null;
	protected AbstractProgressBar progressBar = null;
	
	private SubmodelController submodelController;
	
	private final Logger logger = Logger.getLogger(KineticLawGenerator.class.getName());
	
	private TypeStandardVersion typeStandardVersion;
	private UnitConsistencyType typeUnitConsistency;
	private double defaultParamVal;
	private boolean reversibility;
	private boolean addParametersGlobally;

	/**
	 * @return the setBoundaryCondition
	 */
	public boolean isSetBoundaryCondition() {
		return setBoundaryCondition;
	}

	/**
	 * @return the addParametersGlobally
	 */
	public boolean isAddParametersGlobally() {
		return addParametersGlobally;
	}

	/**
	 * @param addParametersGlobally the addParametersGlobally to set
	 */
	public void setAddParametersGlobally(boolean addParametersGlobally) {
		this.addParametersGlobally = addParametersGlobally;
	}

	/**
	 * @return the reversibility
	 */
	public boolean isReversibility() {
		return reversibility;
	}
	
	public boolean isRemoveUnnecessaryParameters() {
		return removeUnnecessaryParameters;
	}

	/**
	 * @param reversibility the reversibility to set
	 */
	public void setReversibility(boolean reversibility) {
		this.reversibility = reversibility;
	}

	private SortedSet<Integer> possibleEnzymes;
	private boolean allReactionsAsEnzymeCatalyzed;
	private Class<?> kineticsGeneRegulation;
	private Class<?> kineticsReversibleNonEnzymeReactions;
	private Class<?> kineticsReversibleUniUniType;
	private Class<?> kineticsReversibleArbitraryEnzymeReaction;
	private Class<?> kineticsReversibleBiUniType;
	private Class<?> kineticsReversibleBiBiType;
	private Class<?> kineticsIrreversibleNonEnzymeReactions;
	private Class<?> kineticsIrreversibleUniUniType;
	private Class<?> kineticsIrreversibleArbitraryEnzymeReaction;
	private Class<?> kineticsIrreversibleBiUniType;
	private Class<?> kineticsIrreversibleBiBiType;
	private Class<?> kineticsZeroReactants;
	private Class<?> kineticsZeroProducts;
	private String speciesIgnoreList[];
	private double defaultSpatialDimension;
	private boolean generateLawsForAllReactions;
	private boolean removeUnnecessaryParameters;
	private boolean defaultHasOnlySubstanceUnits;
	private boolean setBoundaryCondition;
	private double defaultSpeciesInitVal;
	private double defaultCompartmentInitSize;

	/**
	 * Takes a {@link Model} as input, creates a copy of the {@link Model},
	 * updates the enzymatic catalysis information using the given settings and
	 * creates kinetic laws for each reaction in the model copy. The original
	 * {@link Model} remains unchanged. Note that this constructor only initializes
	 * the {@link KineticLawGenerator}. The actual kinetic equations are created
	 * by calling {@link #generateLaws()}. In order to synchronize the sub-model
	 * with the original {@link Model}, call {@link #storeKineticLaws()}.
	 * 
	 * @param model
	 *        for whose reactions kinetic equations are to be created.
	 * @throws ClassNotFoundException
	 *         happens if the preferences contain references to classes of kinetic
	 *         equations that do not exist.
	 */
	public KineticLawGenerator(Model model) throws ClassNotFoundException {
		// Initialize user settings:
		configure();
		submodelController = new SubmodelController(model);
	}
//
//	/**
//	 * Creates a sub-model as a reduced copy for the given {@link Model} that
//	 * contains only the reaction for the given identifier including all
//	 * {@link Species}, {@link Compartment}s, and {@link UnitDefinition}s linked
//	 * to this reaction. If the identifier is {@code null}, all reactions of the
//	 * given {@link Model} will be copied. The sub-model can then be used to
//	 * {@link #generateLaws()} that can in turn be synchronized with the original
//	 * model by calling {@link #storeKineticLaws()} or
//	 * {@link #storeKineticLaw(KineticLaw)}.
//	 * 
//	 * @param model
//	 *        for whose reactions kinetic equations are to be created. If the
//	 *        second parameter is not {@code null}, kinetic equations will only be
//	 *        created for this reaction.
//	 * @param reactionID
//	 *        the identifier of the reaction for which a kinetic equation is to be
//	 *        created. This argument can be {@code null}.
//	 * @throws ClassNotFoundException
//	 *         happens if the preferences contain references to classes of kinetic
//	 *         equations that do not exist.
//	 */
//	public KineticLawGenerator(Model model, String reactionID) throws ClassNotFoundException {
//		
//
//	}

	/**
	 * 
	 * @throws ClassNotFoundException
	 */
	private void configure() throws ClassNotFoundException {
		
		SBPreferences prefs = SBPreferences.getPreferencesFor(SqueezerOptionsGeneral.class);
		
		typeUnitConsistency = UnitConsistencyType.valueOf(prefs.get(SqueezerOptionsGeneral.TYPE_UNIT_CONSISTENCY));
		defaultParamVal = prefs.getDouble(SqueezerOptionsGeneral.DEFAULT_NEW_PARAMETER_VAL);
		defaultSpatialDimension = prefs.getDouble(SqueezerOptionsGeneral.DEFAULT_COMPARTMENT_SPATIAL_DIM);
		allReactionsAsEnzymeCatalyzed = prefs.getBoolean(SqueezerOptionsGeneral.ALL_REACTIONS_AS_ENZYME_CATALYZED);
		addParametersGlobally = prefs.getBoolean(SqueezerOptionsGeneral.NEW_PARAMETERS_GLOBAL);
		
		String l = prefs.getString(SqueezerOptionsGeneral.IGNORE_THESE_SPECIES_WHEN_CREATING_LAWS);
		if ((l != null) && l.contains(",")) {
			speciesIgnoreList = l.split(",");
		}

		possibleEnzymes = new TreeSet<Integer>();
		String name;
		@SuppressWarnings("unchecked")
		Option<Boolean> options[] = (Option<Boolean>[]) new Option[] {
				SqueezerOptionsGeneral.POSSIBLE_ENZYME_ANTISENSE_RNA,
				SqueezerOptionsGeneral.POSSIBLE_ENZYME_COMPLEX,
				SqueezerOptionsGeneral.POSSIBLE_ENZYME_GENERIC,
				SqueezerOptionsGeneral.POSSIBLE_ENZYME_RECEPTOR,
				SqueezerOptionsGeneral.POSSIBLE_ENZYME_RNA,
				SqueezerOptionsGeneral.POSSIBLE_ENZYME_SIMPLE_MOLECULE,
				/*SqueezerOptions.POSSIBLE_ENZYME_MACROMOLECULE,*/
				SqueezerOptionsGeneral.POSSIBLE_ENZYME_TRUNCATED,
				SqueezerOptionsGeneral.POSSIBLE_ENZYME_UNKNOWN 
		};
		for (Option<Boolean> option : options) {
			name = option.toString().substring(16);
			if (prefs.getBoolean(option)) {
				logger.fine(name + ":\t" + SBO.convertAlias2SBO(name));
				possibleEnzymes.add(Integer.valueOf(SBO.convertAlias2SBO(name)));
			}
		}
		// One more enzyme type that is not reflected in CellDesigner:
		if (prefs.getBoolean(SqueezerOptionsGeneral.POSSIBLE_ENZYME_MACROMOLECULE)) {
			name = SqueezerOptionsGeneral.POSSIBLE_ENZYME_MACROMOLECULE.getName();
			possibleEnzymes.add(Integer.valueOf(SBO.getMacromolecule()));
		}
		
		generateLawsForAllReactions = prefs.getBoolean(SqueezerOptionsGeneral.GENERATE_KINETIC_LAWS_FOR_ALL_REACTIONS);
		removeUnnecessaryParameters = prefs.getBoolean(SqueezerOptionsGeneral.REMOVE_UNNECESSARY_PARAMETERS_AND_UNITS);
		defaultHasOnlySubstanceUnits = prefs.getBoolean(SqueezerOptionsGeneral.DEFAULT_SPECIES_HAS_ONLY_SUBSTANCE_UNITS);
		addParametersGlobally = prefs.getBoolean(SqueezerOptionsGeneral.NEW_PARAMETERS_GLOBAL);
		setBoundaryCondition = prefs.getBoolean(SqueezerOptionsGeneral.SET_BOUNDARY_CONDITION_FOR_GENES);
		defaultSpeciesInitVal = prefs.getDouble(SqueezerOptionsGeneral.DEFAULT_SPECIES_INIT_VAL);
		defaultCompartmentInitSize = prefs.getDouble(SqueezerOptionsGeneral.DEFAULT_COMPARTMENT_SIZE);
		
		prefs = SBPreferences.getPreferencesFor(SqueezerOptionsRateLaws.class);
		
		typeStandardVersion = TypeStandardVersion.valueOf(prefs.get(SqueezerOptionsRateLaws.TYPE_STANDARD_VERSION));
		kineticsZeroReactants = prefs.getClass(SqueezerOptionsRateLaws.KINETICS_ZERO_REACTANTS);
		kineticsZeroProducts = prefs.getClass(SqueezerOptionsRateLaws.KINETICS_ZERO_PRODUCTS);
		kineticsReversibleNonEnzymeReactions = prefs.getClass(SqueezerOptionsRateLaws.KINETICS_REVERSIBLE_NON_ENZYME_REACTIONS);
		kineticsIrreversibleNonEnzymeReactions = prefs.getClass(SqueezerOptionsRateLaws.KINETICS_IRREVERSIBLE_NON_ENZYME_REACTIONS);
		kineticsGeneRegulation = prefs.getClass(SqueezerOptionsRateLaws.KINETICS_GENE_REGULATION);
		kineticsReversibleUniUniType = prefs.getClass(SqueezerOptionsRateLaws.KINETICS_REVERSIBLE_UNI_UNI_TYPE);
		kineticsIrreversibleUniUniType = prefs.getClass(SqueezerOptionsRateLaws.KINETICS_IRREVERSIBLE_UNI_UNI_TYPE);
		kineticsReversibleArbitraryEnzymeReaction = prefs.getClass(SqueezerOptionsRateLaws.KINETICS_REVERSIBLE_ARBITRARY_ENZYME_REACTIONS);
		kineticsIrreversibleArbitraryEnzymeReaction = prefs.getClass(SqueezerOptionsRateLaws.KINETICS_IRREVERSIBLE_ARBITRARY_ENZYME_REACTIONS);
		kineticsReversibleBiUniType = prefs.getClass(SqueezerOptionsRateLaws.KINETICS_REVERSIBLE_BI_UNI_TYPE);
		kineticsIrreversibleBiUniType = prefs.getClass(SqueezerOptionsRateLaws.KINETICS_IRREVERSIBLE_BI_UNI_TYPE);
		kineticsReversibleBiBiType = prefs.getClass(SqueezerOptionsRateLaws.KINETICS_REVERSIBLE_BI_BI_TYPE);
		kineticsIrreversibleBiBiType = prefs.getClass(SqueezerOptionsRateLaws.KINETICS_IRREVERSIBLE_BI_BI_TYPE);
		
		reversibility = prefs.getBoolean(SqueezerOptionsRateLaws.TREAT_ALL_REACTIONS_REVERSIBLE);
	}

	/**
	 * @return the allReactionsAsEnzymeCatalyzed
	 */
	public boolean isAllReactionsAsEnzymeCatalyzed() {
		return allReactionsAsEnzymeCatalyzed;
	}

	/**
	 * @param allReactionsAsEnzymeCatalyzed the allReactionsAsEnzymeCatalyzed to set
	 */
	public void setAllReactionsAsEnzymeCatalyzed(
		boolean allReactionsAsEnzymeCatalyzed) {
		this.allReactionsAsEnzymeCatalyzed = allReactionsAsEnzymeCatalyzed;
	}

	/**
	 * @return the kineticsGeneRegulation
	 */
	public Class<?> getKineticsGeneRegulation() {
		return kineticsGeneRegulation;
	}

	/**
	 * @param kineticsGeneRegulation the kineticsGeneRegulation to set
	 */
	public void setKineticsGeneRegulation(Class<?> kineticsGeneRegulation) {
		this.kineticsGeneRegulation = kineticsGeneRegulation;
	}

	/**
	 * @return the kineticsNoneEnzymeReactions
	 */
	public Class<?> getKineticsNoneEnzymeReactions() {
		return kineticsReversibleNonEnzymeReactions;
	}

	/**
	 * @param kineticsNoneEnzymeReactions the kineticsNoneEnzymeReactions to set
	 */
	public void setKineticsNoneEnzymeReactions(Class<?> kineticsNoneEnzymeReactions) {
		this.kineticsReversibleNonEnzymeReactions = kineticsNoneEnzymeReactions;
	}

	/**
	 * @return the kineticsUniUniType
	 */
	public Class<?> getKineticsUniUniType() {
		return kineticsReversibleUniUniType;
	}

	/**
	 * @param kineticsUniUniType the kineticsUniUniType to set
	 */
	public void setKineticsUniUniType(Class<?> kineticsUniUniType) {
		this.kineticsReversibleUniUniType = kineticsUniUniType;
	}

	/**
	 * @return the kineticsArbitraryEnzymeReaction
	 */
	public Class<?> getKineticsArbitraryEnzymeReaction() {
		return kineticsReversibleArbitraryEnzymeReaction;
	}

	/**
	 * @param kineticsArbitraryEnzymeReaction the kineticsArbitraryEnzymeReaction to set
	 */
	public void setKineticsArbitraryEnzymeReaction(
		Class<?> kineticsArbitraryEnzymeReaction) {
		this.kineticsReversibleArbitraryEnzymeReaction = kineticsArbitraryEnzymeReaction;
	}

	/**
	 * @return the kineticsBiUniType
	 */
	public Class<?> getKineticsBiUniType() {
		return kineticsReversibleBiUniType;
	}

	/**
	 * @param kineticsBiUniType the kineticsBiUniType to set
	 */
	public void setKineticsBiUniType(Class<?> kineticsBiUniType) {
		this.kineticsReversibleBiUniType = kineticsBiUniType;
	}

	/**
	 * @return the kineticsBiBiType
	 */
	public Class<?> getKineticsBiBiType() {
		return kineticsReversibleBiBiType;
	}

	/**
	 * @param kineticsBiBiType the kineticsBiBiType to set
	 */
	public void setKineticsBiBiType(Class<?> kineticsBiBiType) {
		this.kineticsReversibleBiBiType = kineticsBiBiType;
	}

	/**
	 * @return the speciesIgnoreList
	 */
	public String[] getSpeciesIgnoreList() {
		return speciesIgnoreList;
	}

	/**
	 * @param speciesIgnoreList the speciesIgnoreList to set
	 */
	public void setSpeciesIgnoreList(String[] speciesIgnoreList) {
		this.speciesIgnoreList = speciesIgnoreList;
	}

	/**
	 * @param possibleEnzymes the possibleEnzymes to set
	 */
	public void setPossibleEnzymes(SortedSet<Integer> possibleEnzymes) {
		this.possibleEnzymes = possibleEnzymes;
	}

	/**
	 * Creates a kinetic law for the given reaction, which can be assigned to the
	 * given reaction.
	 * 
	 * @param r
	 *        The reaction for which a kinetic law is to be created.
	 * @param kineticsClass
	 *        a {@link Class} object that is derived from {@link BasicKineticLaw}.
	 * @param reversibility
	 *        If true this reaction will be set to reversible and the kinetic
	 *        equation will be created accordingly. If this parameter is false,
	 *        the reversibility property of this reaction will not be changed.
	 * @param version
	 * @param consistency
	 * @param defaultNewParamVal
	 *        
	 * @return A kinetic law for the given reaction.
	 * @throws Throwable
	 */
	public BasicKineticLaw createKineticLaw(Reaction r, Class<?> kineticsClass,
		boolean reversibility, TypeStandardVersion version,
		UnitConsistencyType consistency, double defaultNewParamVal)
		throws Throwable {
		
		this.setReversibility(reversibility);
		this.typeStandardVersion = version;
		this.typeUnitConsistency = consistency;
		this.defaultParamVal = defaultNewParamVal;
		
		if (submodelController.getSubmodel() == null) {
			initSubmodel(r.getId());
		}
		Model submodel = submodelController.getSubmodel();
		Reaction reaction = submodel.getReaction(r.getId());
		if (reaction == null) {
			reaction = r;
		}
		reaction.setReversible(reversibility || reaction.getReversible());
		try {
			Object typeParameters[] = new Object[] { 
					version,
					Boolean.valueOf(hasFullColumnRank(submodel)), 
					consistency,
					Double.valueOf(defaultNewParamVal) 
			};
			Constructor<?> constructor = kineticsClass.getConstructor(reaction.getClass(), typeParameters.getClass());
			return (BasicKineticLaw) constructor.newInstance(reaction, typeParameters);
		} catch (InstantiationException e) {
			logger.warning(e.getLocalizedMessage());
			throw e.getCause();
		} catch (IllegalAccessException e) {
		  logger.warning(e.getLocalizedMessage());
			throw e.getCause();
		} catch (InvocationTargetException e) {
		  logger.warning(e.getLocalizedMessage());
			throw e.getCause();
		}
	}

	/**
	 * Initialize submodel controller and create the mini model:
	 * 
	 * @param reactionID
	 */
	private void initSubmodel(String reactionID) {
			submodelController.setProgressBar(progressBar);
			submodelController.setDefaultSpatialDimensions(defaultSpatialDimension);
			submodelController.setReversibility(reversibility);

			submodelController.setGenerateLawsForAllReactions(generateLawsForAllReactions);
			submodelController.setRemoveUnnecessaryParameters(removeUnnecessaryParameters);
			submodelController.setDefaultHasOnlySubstanceUnits(defaultHasOnlySubstanceUnits);
			submodelController.setAddParametersGlobally(addParametersGlobally);
			submodelController.setBoundaryCondition(setBoundaryCondition);
			submodelController.setDefaultSpeciesInitVal(defaultSpeciesInitVal);
			submodelController.setDefaultCompartmentInitSize(defaultCompartmentInitSize);
			
			listOfFastReactions = new LinkedList<Reaction>();
			submodelController.createSubmodel(reactionID);
			updateEnzymeCatalysis();
	}

	/**
	 * @throws Throwable
	 */
	public void generateLaws() throws Throwable {
		
		initSubmodel(null);

		if (progressBar != null) {
			progressAdapter = new ProgressAdapter(progressBar, TypeOfProgress.generateLaws);
			progressAdapter.setNumberOfTags(getModel(), getSubmodel(), isRemoveUnnecessaryParameters());
		}
		
		for (Reaction reaction : submodelController.getSubmodel().getListOfReactions()) {
			ReactionType rt = new ReactionType(reaction, isReversibility(),
				allReactionsAsEnzymeCatalyzed, isSetBoundaryCondition(), speciesIgnoreList);
			
			Class<?> kineticsClass = rt.identifyPossibleKineticLaw(
				kineticsGeneRegulation, kineticsZeroReactants, kineticsZeroProducts,
				kineticsReversibleNonEnzymeReactions,
				kineticsIrreversibleNonEnzymeReactions,
				kineticsReversibleArbitraryEnzymeReaction,
				kineticsIrreversibleArbitraryEnzymeReaction,
				kineticsReversibleUniUniType, kineticsIrreversibleUniUniType,
				kineticsReversibleBiUniType, kineticsIrreversibleBiUniType,
				kineticsReversibleBiBiType, kineticsIrreversibleBiBiType);
			
			if (progressAdapter != null) {
				//progressAdapter.setNumberOfTags(modelOrig, miniModel, isRemoveUnnecessaryParameters());
				progressAdapter.progressOn();
			}
			
			createKineticLaw(reaction, kineticsClass, isReversibility(),
				typeStandardVersion, typeUnitConsistency, defaultParamVal);
			
			if (progressAdapter != null) {
				//progressAdapter.setNumberOfTags(modelOrig, miniModel, isRemoveUnnecessaryParameters());
				progressAdapter.progressOn();
			}
		}
		
		if (progressAdapter != null) {
			progressAdapter.finished();
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
	 * Returns the copy of the model that contains only those reactions together
	 * with all required species, compartments, species- and compartment types,
	 * and units for which kinetic equations are to be created.
	 * 
	 * @return
	 */
	public Model getSubmodel() {
		return submodelController.getSubmodel();
	}

	/**
	 * 
	 * @return
	 */
	public Model getModel() {
		return submodelController.getSBMLDocument().getModel();
	}

	/**
	 * 
	 * @param i
	 * @return
	 */
	public Reaction getModifiedReaction(int i) {
		return getSubmodel().getReaction(i);
	}

	/**
	 * 
	 * @param reactionID
	 * @return
	 */
	public Reaction getModifiedReaction(String reactionID) {
		return getSubmodel().getReaction(reactionID);
	}

	/**
	 * 
	 * @return
	 */
	public int getCreatedKineticsCount() {
		return getSubmodel().getReactionCount();
	}

	/**
	 * 
	 * @return
	 */
	public SortedSet<Integer> getPossibleEnzymes() {
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
		initSubmodel(reactionID);
		return new ReactionType(getSubmodel().getReaction(reactionID), isReversibility(),
			allReactionsAsEnzymeCatalyzed, isSetBoundaryCondition(), speciesIgnoreList);
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
		if ((model.getSpeciesCount() >= model.getReactionCount())
				&& (columnRank == -1)) {
			GaussianRank gaussian = new GaussianRank(stoechMatrix(model));
			columnRank = gaussian.getColumnRank();
			fullRank = gaussian.hasFullRank();
		} else if (columnRank == model.getReactionCount()) {
			fullRank = true;
		}
		return fullRank;
	}

	/**
	 * 
	 * @param progressBar
	 */
	public void setProgressBar(AbstractProgressBar progressBar) {
		this.progressBar = progressBar;
	}

	/**
	 * Sets the reaction with the given id in the minimal model copy to the
	 * given reversibility value.
	 * 
	 * @param reactionID
	 * @param reversible
	 */
	public void setReversible(String reactionID, boolean reversible) {
		Reaction r = getSubmodel().getReaction(reactionID);
		if (r != null) {
			r.setReversible(reversible);
		} else {
			throw new IllegalArgumentException(MessageFormat.format(
					WARNINGS.getString("INVALID_REACTION_ID_FOR_RATE_LAW_CREATION"), reactionID));
		}
	}

	/**
	 * Computes the stoichiometric matrix of the model system.
	 * 
	 * @return
	 */
	public double[][] stoechMatrix(Model model) {
		double[][] N = new double[model.getSpeciesCount()][model.getReactionCount()];
		int reactionNum, speciesNum;
		SpeciesReference speciesRef;
		HashMap<String, Integer> speciesIDandNum = new HashMap<String, Integer>();
		int i = 0;
		for (Species s : model.getListOfSpecies())
			speciesIDandNum.put(s.getId(), Integer.valueOf(i++));
		for (reactionNum = 0; reactionNum < model.getReactionCount(); reactionNum++) {
			Reaction reaction = model.getReaction(reactionNum);
			for (speciesNum = 0; speciesNum < reaction.getReactantCount(); speciesNum++) {
				speciesRef = reaction.getReactant(speciesNum);
				N[speciesIDandNum.get(speciesRef.getSpecies())][reactionNum] = -speciesRef
				.getStoichiometry();
			}
			for (speciesNum = 0; speciesNum < reaction.getProductCount(); speciesNum++) {
				speciesRef = reaction.getProduct(speciesNum);
				N[speciesIDandNum.get(speciesRef.getSpecies())][reactionNum] = speciesRef
				.getStoichiometry();
			}
		}
		return N;
	}

	/**
	 * This method stores a {@link KineticLaw} for the given {@link Reaction} in
	 * the currently selected {@link Model} given by the user. The
	 * {@link KineticLaw} is passed to this method. A boolean variable tells this
	 * method weather the formula is for a reversible or for an irreversible
	 * {@link Reaction} (in the user's {@link Preferences}). Afterwards all
	 * parameters within this {@link KineticLaw} are also stored in the given
	 * {@link Model}. There is no need to call the
	 * {@link #storeParamters(Reaction)} method.
	 * 
	 * @param kineticLaw
	 *        A string with the formula to be assigned to the given reaction.
	 * @param l
	 */
	public Reaction storeKineticLaw(KineticLaw kineticLaw) {
		if (progressBar != null) {
			progressAdapter = new ProgressAdapter(progressBar, TypeOfProgress.storeKineticLaw);
			progressAdapter.setNumberOfTags(getModel(), getSubmodel(), isRemoveUnnecessaryParameters());
		}
		
		Reaction r = submodelController.storeKineticLaw(kineticLaw, true);
		
		if (progressAdapter != null) {
			progressAdapter.finished();
		}
		return r;
	}

	/**
	 * store the generated Kinetics in SBML-File as MathML.
	 */
	public void storeKineticLaws() {
		
		if (getFastReactions().size() > 0) {
			logger.log(Level.FINE, MessageFormat.format(MESSAGES.getString("THE_MODEL_CONTAINS"), 
											getFastReactions().size(), getModel().getId())
									+ " " + MESSAGES.getString("FAST_REACTIONS") + "."
									+ " " + MESSAGES.getString("NOT_SUPPORTED"));
		}
		
		if (progressBar != null) {
			progressAdapter = new ProgressAdapter(progressBar, TypeOfProgress.storeKineticLaws);
			progressAdapter.setNumberOfTags(getModel(), getSubmodel(), isRemoveUnnecessaryParameters());
		}
		
		ModelChangeListener chl = new ModelChangeListener();
		getModel().addTreeNodeChangeListener(chl);

		submodelController.setProgressBar(progressBar);
		submodelController.storeKineticLaws(isRemoveUnnecessaryParameters());
		getModel().removeTreeNodeChangeListener(chl);

		if (progressAdapter != null) {
			progressAdapter.finished();
		}

	}
	
	/**
	 * Sets the SBO annotation of modifiers to more precise values in the local
	 * mini copy of the model.
	 * 
	 * Updates the minimal model so that all possible enzymes are marked as
	 * enzymatic catalyst.
	 */
	public void updateEnzymeCatalysis() {
		Set<Integer> possibleEnzymes = getPossibleEnzymes();
		for (Reaction r : getSubmodel().getListOfReactions()) {
			for (ModifierSpeciesReference modifier : r.getListOfModifiers()) {
				Species species = modifier.getSpeciesInstance();
				if (SBO.isEnzymaticCatalysis(modifier.getSBOTerm())
						&& species.isSetSBOTerm()
						&& !possibleEnzymes.contains(Integer.valueOf(species.getSBOTerm()))) {
					SBMLtools.setSBOTerm(modifier, SBO.getCatalysis());
				} else if (SBO.isCatalyst(modifier.getSBOTerm())
						&& (possibleEnzymes.contains(Integer.valueOf(species.getSBOTerm())) || !species.isSetSBOTerm())) {
					SBMLtools.setSBOTerm(modifier, SBO.getEnzymaticCatalysis());
				}
			}
		}
	}

}
