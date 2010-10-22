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

import org.sbml.tolatex.LaTeXCfgKeys;

import de.zbit.gui.GUIKeys;

/**
 * This is a list of possible command line options and configuration of
 * SBMLsqueezer. Each element listed here determines a key for a configuration
 * value.
 * 
 * @author Andreas Dr&auml;ger
 * @since 1.3
 */
public interface CfgKeys extends LaTeXCfgKeys, GUIKeys {
	/**
	 * The class name of the default kinetic law for bi-bi reactions. This can
	 * be any class that implements the
	 * {@link org.sbml.squeezer.kinetics.InterfaceBiBiKinetics}.
	 */
	public static final String KINETICS_BI_BI_TYPE = "KINETICS_BI_BI_TYPE";
	/**
	 * The class name of the default kinetic law for bi-uni reactions. This can
	 * be any class that implements the
	 * {@link org.sbml.squeezer.kinetics.InterfaceBiUniKinetics}.
	 */
	public static final String KINETICS_BI_UNI_TYPE = "KINETICS_BI_UNI_TYPE";
	/**
	 * Determines the key for the standard kinetic law to be applied for
	 * reactions that are identified to belong to gene-regulatory processes,
	 * such as transcription or translation. The value is the class name of any
	 * class that implements the
	 * {@link org.sbml.squeezer.kinetics.InterfaceGeneRegulatoryNetworks}.
	 */
	public static final String KINETICS_GENE_REGULATION = "KINETICS_GENE_REGULATION";
	/**
	 * Determines the key for the standard kinetic law to be applied for
	 * reactions that are catalyzed by non-enzymes or that are not catalyzed at
	 * all. The value may be any rate law that implements
	 * {@link org.sbml.squeezer.kinetics.InterfaceNonEnzymeKinetics}.
	 */
	public static final String KINETICS_NONE_ENZYME_REACTIONS = "KINETICS_NONE_ENZYME_REACTIONS";
	/**
	 * Determines the key for the standard kinetic law to be applied for
	 * reactions that are identified to be enzyme-catalyzed (with or without
	 * explicit catalyst) and that do not belong to one of the other standard
	 * enzyme-catalysis schemes. The value can be any rate law that implements
	 * {@link org.sbml.squeezer.kinetics.InterfaceArbitraryEnzymeKinetics}.
	 */
	public static final String KINETICS_OTHER_ENZYME_REACTIONS = "KINETICS_OTHER_ENZYME_REACTIONS";
	/**
	 * This key defines the default kinetic law to be applied to
	 * enzyme-catalyzed reactions with one reactant and one product. Possible
	 * values are the names of classes that implement
	 * {@link org.sbml.squeezer.kinetics.InterfaceUniUniKinetics}.
	 */
	public static final String KINETICS_UNI_UNI_TYPE = "KINETICS_UNI_UNI_TYPE";
	/**
	 * If true all parameters are stored globally for the whole model (default)
	 * else parameters are stored locally for the respective kinetic equation
	 * they belong to.
	 */
	public static final String OPT_ADD_NEW_PARAMETERS_ALWAYS_GLOBALLY = "OPT_ADD_NEW_PARAMETERS_ALWAYS_GLOBALLY";
	/**
	 * If true, all reactions within the network are considered enzyme
	 * reactions. If false, an explicit enzymatic catalyst must be assigned to a
	 * reaction to obtain this status.
	 */
	public static final String OPT_ALL_REACTIONS_ARE_ENZYME_CATALYZED = "OPT_ALL_REACTIONS_ARE_ENZYME_CATALYZED";
	/**
	 * If not specified the value corresponding to this argument will be used to
	 * initialize the size of compartments.
	 */
	public static final String OPT_DEFAULT_COMPARTMENT_INITIAL_SIZE = "OPT_DEFAULT_COMPARTMENT_INITIAL_SIZE";
	/**
	 * If not specified the value corresponding to this argument will be used to
	 * initialize species depending on their hasOnlySubstanceUnits property as
	 * initial amount or initial concentration.
	 */
	public static final String OPT_DEFAULT_SPECIES_INITIAL_VALUE = "OPT_DEFAULT_SPECIES_INITIAL_VALUE";
	/**
	 * The value that is set for newly created parameters.
	 */
	public static final String OPT_DEFAULT_VALUE_OF_NEW_PARAMETERS = "OPT_DEFAULT_VALUE_OF_NEW_PARAMETERS";
	/**
	 * If true a new rate law will be created for each reaction irrespective of
	 * whether there is already a rate law assigned to this reaction or not.
	 */
	public static final String OPT_GENERATE_KINETIC_LAW_FOR_EACH_REACTION = "OPT_GENERATE_KINETIC_LAW_FOR_EACH_REACTION";
	/**
	 * Allows the user to ignore species that are annotated with the given
	 * compound identifiers when creating rate laws for reactions that involve
	 * these species. For instance, water or single protons can often be ignored
	 * when creating rate equations, hence simplifying the resulting rate
	 * equations. Preselected are the KEGG compound identifiers for water and
	 * protons
	 */
	public static final String OPT_IGNORE_THESE_SPECIES_WHEN_CREATING_LAWS = "OPT_IGNORE_THESE_SPECIES_WHEN_CREATING_LAWS";
	/**
	 * The maximal number of reactants so that the reaction is still considered
	 * plausible.
	 */
	public static final String OPT_MAX_NUMBER_OF_REACTANTS = "OPT_MAX_NUMBER_OF_REACTANTS";
	/**
	 * If true parameters and units that are never referenced by any element of
	 * the model are deleted when creating kinetic equations with SBMLsqueezer.
	 */
	public static final String OPT_REMOVE_UNNECESSARY_PARAMETERS_AND_UNITS = "OPT_REMOVE_UNNECESSARY_PARAMETERS_AND_UNITS";
	/**
	 * Decide whether or not to set the boundary condition for genes to true.
	 */
	public static final String OPT_SET_BOUNDARY_CONDITION_FOR_GENES = "OPT_SET_BOUNDARY_CONDITION_FOR_GENES";
	/**
	 * Property that decides whether to set all reactions to reversible before
	 * creating new kinetic equations.
	 */
	public static final String OPT_TREAT_ALL_REACTIONS_REVERSIBLE = "OPT_TREAT_ALL_REACTIONS_REVERSIBLE";
	/**
	 * If true, warnings will be displayed for too many reactants.
	 */
	public static final String OPT_WARNINGS_FOR_TOO_MANY_REACTANTS = "OPT_WARNINGS_FOR_TOO_MANY_REACTANTS";
	/**
	 * Determins whether or not antisense RNA molecules are accepted as enzymes
	 * when catalyzing a reaction.
	 */
	public static final String POSSIBLE_ENZYME_ANTISENSE_RNA = "POSSIBLE_ENZYME_ANTISENSE_RNA";
	/**
	 * Determins whether or not enzyme complexes are accepted as enzymes when
	 * catalyzing a reaction.
	 */
	public static final String POSSIBLE_ENZYME_COMPLEX = "POSSIBLE_ENZYME_COMPLEX";
	/**
	 * Determins whether or not generic proteins are accepted as enzymes when
	 * catalyzing a reaction.
	 */
	public static final String POSSIBLE_ENZYME_GENERIC = "POSSIBLE_ENZYME_GENERIC";
	/**
	 * Determins whether or not receptors are accepted as enzymes when
	 * catalyzing a reaction.
	 */
	public static final String POSSIBLE_ENZYME_RECEPTOR = "POSSIBLE_ENZYME_RECEPTOR";
	/**
	 * Determins whether or not RNA molecules are accepted as enzymes when
	 * catalyzing a reaction.
	 */
	public static final String POSSIBLE_ENZYME_RNA = "POSSIBLE_ENZYME_RNA";
	/**
	 * Determins whether or not simple molecules are accepted as enzymes when
	 * catalyzing a reaction.
	 */
	public static final String POSSIBLE_ENZYME_SIMPLE_MOLECULE = "POSSIBLE_ENZYME_SIMPLE_MOLECULE";
	/**
	 * Determins whether or not trunkated proteins are accepted as enzymes when
	 * catalyzing a reaction.
	 */
	public static final String POSSIBLE_ENZYME_TRUNCATED = "POSSIBLE_ENZYME_TRUNCATED";
	/**
	 * Determins whether or not unknown molecules are accepted as enzymes when
	 * catalyzing a reaction.
	 */
	public static final String POSSIBLE_ENZYME_UNKNOWN = "POSSIBLE_ENZYME_UNKNOWN";
	/**
	 * SBML input file.
	 */
	public static final String SBML_FILE = "SBML_FILE";
	/**
	 * Specifies the file where SBMLsqueezer writes its SBML output.
	 */
	public static final String SBML_OUT_FILE = "SBML_OUT_FILE";
	/**
	 * Can be true or false, depending on if the user wants to see SBML
	 * warnings.
	 */
	public static final String SHOW_SBML_WARNINGS = "SHOW_SBML_WARNINGS";
	/**
     * 
     */
	public static final String STABILITY_VALUE_OF_DELTA = "STABILITY_VALUE_OF_DELTA";
	/**
	 * 
	 */
	public static final String STEUER_MI_OUTPUT = "STEUER_MI_OUTPUT";
	/**
	 * 
	 */
	public static final String STEUER_MI_STEPSIZE = "STEUER_MI_STEPSIZE";
	/**
	 * 
	 */
	public static final String STEUER_NUMBER_OF_RUNS = "STEUER_NUMBER_OF_RUNS";
	/**
	 * 
	 */
	public static final String STEUER_PC_OUTPUT = "STEUER_PC_OUTPUT";
	/**
	 * 
	 */
	public static final String STEUER_PC_STEPSIZE = "STEUER_PC_STEPSIZE";
	/**
	 * 
	 */
	public static final String STEUER_VALUE_OF_M = "STEUER_VALUE_OF_M";
	/**
	 * 
	 */
	public static final String STEUER_VALUE_OF_N = "STEUER_VALUE_OF_N";
	/**
	 * One of the following values: cat, hal or weg (important for
	 * Liebermeister's standard kinetics).
	 */
	public static final String TYPE_STANDARD_VERSION = "TYPE_STANDARD_VERSION";
	/**
	 * How to ensure unit consistency in kinetic equations? One way is to set
	 * each participating species to an initial amount and to set the unit to
	 * mmole. The other way is to set the initial concentration of each species,
	 * set the unit to mmole per l and to multiply the species with the size of
	 * the surrounding compartment whenever it occurs in a kinetic equation.
	 * Hence, this type paramter belongs to two values.
	 */
	public static final String TYPE_UNIT_CONSISTENCY = "TYPE_UNIT_CONSISTENCY";

}
