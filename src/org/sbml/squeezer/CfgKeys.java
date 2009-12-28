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

/**
 * This is a list of possible command line options and configuration of
 * SBMLsqueezer. Each element listed here determins a key for a configuration
 * value.
 * 
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * @since 1.3
 */
public enum CfgKeys {
	/**
	 * Can be used in combination with = true or = false or just --gui.
	 */
	GUI,
	/**
	 * SBML input file.
	 */
	SBML_FILE,
	/**
	 * Specifies the file where SBMLsqueezer writes its SBML output.
	 */
	SBML_OUT_FILE,
	/**
	 * Standard directory where SBML files can be found.
	 */
	OPEN_DIR,
	/**
	 * Standard directory where SBML or Text files can be saved.
	 */
	SAVE_DIR,
	/**
	 * Can be true or false, depending on if the user wants to see SBML
	 * warnings.
	 */
	SHOW_SBML_WARNINGS,
	/**
	 * Decide whether or not SBMLsqueezer should search for updates at start-up.
	 */
	CHECK_FOR_UPDATES,
	/**
	 * Possible values are, for instance,
	 * <ul>
	 * <li>generalized mass-action kinetics</li>
	 * <li>Convenience kinetics</li>
	 * <li>Michaelis-Menten kinetics</li>
	 * </ul>
	 */
	KINETICS_UNI_UNI_TYPE,
	/**
	 * Possible values are, e.g.,
	 * <ul>
	 * <li>generalized mass-action kinetics</li>
	 * <li>Convenience kinetics</li>
	 * <li>Random Order Michealis Menten kinetics</li>
	 * <li>Ordered</li>
	 * </ul>
	 */
	KINETICS_BI_UNI_TYPE,
	/**
	 * Possible values are, among others,
	 * <ul>
	 * <li>generalized mass-action kinetics</li>
	 * <li>Convenience kinetics</li>
	 * <li>Random Order Michealis Menten kinetics</li>
	 * <li>Ping-Pong</li>
	 * <li>Ordered</li>
	 * </ul>
	 */
	KINETICS_BI_BI_TYPE,
	/**
	 * Determins the key for the standar kinetic law to be applied for reactions
	 * that are catalyzed by non-enzymes or that are not catalyzed at all.
	 */
	KINETICS_NONE_ENZYME_REACTIONS,
	/**
	 * Determins the key for the standar kinetic law to be applied for reactions
	 * that are identified to belong to gene-regulatory processes, such as
	 * transcription or translation.
	 */
	KINETICS_GENE_REGULATION,
	/**
	 * Determins the key for the standar kinetic law to be applied for reactions
	 * that are identified to be enzyme-catalyzed (with or without explicit
	 * catalyst) and that do not belong to one of the other standard
	 * enzyme-catalysis schemes.
	 */
	KINETICS_OTHER_ENZYME_REACTIONS,
	/**
	 * One of the following values: cat, hal or weg (important for
	 * Liebermeister's standard kinetics).
	 */
	TYPE_STANDARD_VERSION,
	/**
	 * How to ensure unit consistency in kinetic equations? One way is to set
	 * each participating species to an initial amount and to set the unit to
	 * mmole. The other way is to set the initial concentration of each species,
	 * set the unit to mmole per l and to multiply the species with the size of
	 * the surrounding compartment whenever it occurs in a kinetic equation.
	 * Hence, this type paramter belongs to two values.
	 */
	TYPE_UNIT_CONSISTENCY,
	/**
	 * If true, all reactions within the network are considered enzyme
	 * reactions. If false, an explicit enzymatic catalyst must be assigned to a
	 * reaction to obtain this status.
	 */
	OPT_ALL_REACTIONS_ARE_ENZYME_CATALYZED,
	/**
	 * If true a new rate law will be created for each reaction irrespective of
	 * whether there is already a rate law assigned to this reaction or not.
	 */
	OPT_GENERATE_KINETIC_LAW_FOR_EACH_REACTION,
	/**
	 * If true all parameters are stored globally for the whole model (default)
	 * else parameters are stored locally for the respective kinetic equation
	 * they belong to.
	 */
	OPT_ADD_NEW_PARAMETERS_ALWAYS_GLOBALLY,
	/**
	 * Property that decides whether to set all reactions to reversible before
	 * creating new kinetic equations.
	 */
	OPT_TREAT_ALL_REACTIONS_REVERSIBLE,
	/**
	 * The maximal number of reactants so that the reaction is still considered
	 * plausible.
	 */
	OPT_MAX_NUMBER_OF_REACTANTS,
	/**
	 * If not specified the value corresponding to this argument will be used to
	 * initialize the size of compartments.
	 */
	OPT_DEFAULT_COMPARTMENT_INITIAL_SIZE,
	/**
	 * If not specified the value corresponding to this argument will be used to
	 * initialize species depending on their hasOnlySubstanceUnits property as
	 * initial amount or initial concentration.
	 */
	OPT_DEFAULT_SPECIES_INITIAL_VALUE,
	/**
	 * The value that is set for newly created parameters.
	 */
	OPT_DEFAULT_VALUE_OF_NEW_PARAMETERS,
	/**
	 * If true, warnings will be displayed for too many reactants.
	 */
	OPT_WARNINGS_FOR_TOO_MANY_REACTANTS,
	/**
	 * Decide whether or not to set the boundary condition for genes to true.
	 */
	OPT_SET_BOUNDARY_CONDITION_FOR_GENES,
	/**
	 * If true parameters and units that are never referenced by any element of
	 * the model are deleted when creating kinetic equations with SBMLsqueezer.
	 */
	OPT_REMOVE_UNNECESSARY_PARAMETERS_AND_UNITS,
	/**
	 * Allows the user to ignore species that are annotated with the given
	 * compound identifiers when creating rate laws for reactions that involve
	 * these species. For instance, water or single protons can often be ignored
	 * when creating rate equations, hence simplifying the resulting rate
	 * equations. Preselected are the KEGG compound identifiers for water and
	 * protons
	 */
	OPT_IGNORE_THESE_SPECIES_WHEN_CREATING_LAWS,
	/**
	 * Determins whether or not generic proteins are accepted as enzymes when
	 * catalyzing a reaction.
	 */
	POSSIBLE_ENZYME_GENERIC,
	/**
	 * Determins whether or not RNA molecules are accepted as enzymes when
	 * catalyzing a reaction.
	 */
	POSSIBLE_ENZYME_RNA,
	/**
	 * Determins whether or not enzyme complexes are accepted as enzymes when
	 * catalyzing a reaction.
	 */
	POSSIBLE_ENZYME_COMPLEX,
	/**
	 * Determins whether or not trunkated proteins are accepted as enzymes when
	 * catalyzing a reaction.
	 */
	POSSIBLE_ENZYME_TRUNCATED,
	/**
	 * Determins whether or not receptors are accepted as enzymes when
	 * catalyzing a reaction.
	 */
	POSSIBLE_ENZYME_RECEPTOR,
	/**
	 * Determins whether or not unknown molecules are accepted as enzymes when
	 * catalyzing a reaction.
	 */
	POSSIBLE_ENZYME_UNKNOWN,
	/**
	 * Determins whether or not antisense RNA molecules are accepted as enzymes
	 * when catalyzing a reaction.
	 */
	POSSIBLE_ENZYME_ANTISENSE_RNA,
	/**
	 * Determins whether or not simple molecules are accepted as enzymes when
	 * catalyzing a reaction.
	 */
	POSSIBLE_ENZYME_SIMPLE_MOLECULE,
	/**
	 * Standard directory where LaTeX files can be stored.
	 */
	LATEX_DIR,
	/**
	 * The paper size for LaTeX documents.
	 */
	LATEX_PAPER_SIZE,
	/**
	 * The font size for LaTeX documents.
	 */
	LATEX_FONT_SIZE,
	/**
	 * Key that decides whether or not identifiers should be written in
	 * typewriter font when these occur in mathematical equations.
	 */
	LATEX_IDS_IN_TYPEWRITER_FONT,
	/**
	 * Decides whether to set the LaTeX document in landscape or portrait mode.
	 */
	LATEX_LANDSCAPE,
	/**
	 * Decides whether to create a separate title page instead of a simple
	 * heading.
	 */
	LATEX_TITLE_PAGE,
	/**
	 * Decides whether to write the names or the identifiers of NamedSBase
	 * object in equations.
	 */
	LATEX_NAMES_IN_EQUATIONS,
	/**
     * 
     */
	STABILITY_VALUE_OF_DELTA,
	/**
	 * 
	 */
	STEUER_VALUE_OF_N,
	/**
	 * 
	 */
	STEUER_VALUE_OF_M,
	/**
	 * 
	 */
	STEUER_NUMBER_OF_RUNS,
	/**
	 * 
	 */
	STEUER_PC_STEPSIZE,
	/**
	 * 
	 */
	STEUER_PC_OUTPUT,
	/**
	 * 
	 */
	STEUER_MI_STEPSIZE,
	/**
	 * 
	 */
	STEUER_MI_OUTPUT
}
