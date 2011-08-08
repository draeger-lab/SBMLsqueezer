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

import java.io.File;

import org.sbml.squeezer.kinetics.InterfaceKinteticsType;

import com.sun.xml.internal.bind.v2.schemagen.xmlschema.List;

import de.zbit.util.prefs.KeyProvider;
import de.zbit.util.prefs.Option;
import de.zbit.util.prefs.Range;

/**
 * This is a list of possible command line options and configuration of
 * SBMLsqueezer. Each element listed here determines a key for a configuration
 * value.
 * 
 * @author Andreas Dr&auml;ger
 * @since 1.4
 */
public interface SqueezerOptions extends KeyProvider {
    /**
     * The possible selections for the three versions of modular rate laws (cf.
     * <a href=
     * "http://bioinformatics.oxfordjournals.org/cgi/content/abstract/btq141v1"
     * >Liebermeister et al. (2010), Modular rate laws for enzymatic reactions:
     * thermodynamics, elasticities,
     * and implementation</a>)
     * 
     * @author Andreas Dr&auml;ger
     * @date 2010-10-29
     */
    public static enum TypeStandardVersion {
	/**
	 * The most simple version.
	 */
	cat,
	/**
	 * The more complicated version in which all parameters fulfill the
	 * Haldane relationship.
	 */
	hal,
	/**
	 * The most sophisticated version in which all parameters fulfill
	 * Wegscheider's condition.
	 */
	weg;
    }

    /**
     * This is an enumeration of the two possible ways of how to ensure
     * consistent units within
     * an SBML model; reacting species might either be given in molecule counts
     * ({@link #amount}) or
     * their {@link #concentration}.
     * 
     * @author Andreas Dr&auml;ger
     * @date 2010-10-29
     */
    public static enum TypeUnitConsistency {
	/**
	 * 
	 */
	amount,
	/**
	 * 
	 */
	concentration;
    }

	/**
	 * The path to the associated configuration file, which contains one default
	 * value for each option defined in this interface.
	 */
	public static final String CONFIG_FILE_LOCATION = "resources/cfg/SBMLsqueezer.xml";
	/**
	 * The class name of the default kinetic law for bi-bi reactions. This can
	 * be any class that implements the
	 * {@link org.sbml.squeezer.kinetics.InterfaceBiBiKinetics}.
	 */
	public static final Option<InterfaceKinteticsType> KINETICS_BI_BI_TYPE = new Option<InterfaceKinteticsType>(
			"KINETICS_BI_BI_TYPE",
			InterfaceKinteticsType.class,
			" The class name of the default kinetic law for bi-bi reactions. This can be any class that implements InterfaceBiBiKinetics");
	/**
	 * The class name of the default kinetic law for bi-uni reactions. This can
	 * be any class that implements the
	 * {@link org.sbml.squeezer.kinetics.InterfaceBiUniKinetics}.
	 */
	public static final Option<InterfaceKinteticsType> KINETICS_BI_UNI_TYPE = new Option<InterfaceKinteticsType>(
			"KINETICS_BI_UNI_TYPE",
			InterfaceKinteticsType.class,
			"The class name of the default kinetic law for bi-uni reactions. This can be any class that implements InterfaceBiUniKinetics");
	/**
	 * Determines the key for the standard kinetic law to be applied for
	 * reactions that are identified to belong to gene-regulatory processes,
	 * such as transcription or translation. The value is the class name of any
	 * class that implements the
	 * {@link org.sbml.squeezer.kinetics.InterfaceGeneRegulatoryNetworks}.
	 */
	public static final Option<InterfaceKinteticsType> KINETICS_GENE_REGULATION = new Option<InterfaceKinteticsType>(
			"KINETICS_GENE_REGULATION",
			InterfaceKinteticsType.class,
			"Determines the key for the standard kinetic law to be applied for reactions that are identified to belong to gene-regulatory processes,such as transcription or translation. The value is the class name of any class that implements InterfaceGeneRegulatoryNetworks");
	/**
	 * Determines the key for the standard kinetic law to be applied for
	 * reactions that are catalyzed by non-enzymes or that are not catalyzed at
	 * all. The value may be any rate law that implements
	 * {@link org.sbml.squeezer.kinetics.InterfaceNonEnzymeKinetics}.
	 */
	public static final Option<InterfaceKinteticsType> KINETICS_NONE_ENZYME_REACTIONS = new Option<InterfaceKinteticsType>(
			"KINETICS_NONE_ENZYME_REACTIONS",
			InterfaceKinteticsType.class,
			"Determines the key for the standard kinetic law to be applied for reactions that are catalyzed by non-enzymes or that are not catalyzed at all. The value may be any rate law that implements InterfaceNonEnzymeKinetics");
	/**
	 * Determines the key for the standard kinetic law to be applied for
	 * reactions that are identified to be enzyme-catalyzed (with or without
	 * explicit catalyst) and that do not belong to one of the other standard
	 * enzyme-catalysis schemes. The value can be any rate law that implements
	 * {@link org.sbml.squeezer.kinetics.InterfaceArbitraryEnzymeKinetics}.
	 */
	public static final Option<InterfaceKinteticsType> KINETICS_OTHER_ENZYME_REACTIONS = new Option<InterfaceKinteticsType>(
			"KINETICS_OTHER_ENZYME_REACTIONS",
			InterfaceKinteticsType.class,
			"Determines the key for the standard kinetic law to be applied for reactions that are identified to be enzyme-catalyzed (with or without explicit catalyst) and that do not belong to one of the other standard enzyme-catalysis schemes. The value can be any rate law that implements InterfaceArbitraryEnzymeKinetics.");
	/**
	 * This key defines the default kinetic law to be applied to
	 * enzyme-catalyzed reactions with one reactant and one product. Possible
	 * values are the names of classes that implement
	 * {@link org.sbml.squeezer.kinetics.InterfaceUniUniKinetics}.
	 */
	public static final Option<InterfaceKinteticsType> KINETICS_UNI_UNI_TYPE = new Option<InterfaceKinteticsType>(
			"KINETICS_UNI_UNI_TYPE",
			InterfaceKinteticsType.class,
			"This key defines the default kinetic law to be applied to enzyme-catalyzed reactions with one reactant and one product. Possible values are the names of classes that implement InterfaceUniUniKinetics.");
	/**
	 * If true all parameters are stored globally for the whole model (default)
	 * else parameters are stored locally for the respective kinetic equation
	 * they belong to.
	 */
	public static final Option<Boolean> OPT_ADD_NEW_PARAMETERS_ALWAYS_GLOBALLY = new Option<Boolean>(
			"OPT_ADD_NEW_PARAMETERS_ALWAYS_GLOBALLY",
			Boolean.class,
			"If true all parameters are stored globally for the whole model (default) else parameters are stored locally for the respective kinetic equation they belong to.");
	/**
	 * If true, all reactions within the network are considered enzyme
	 * reactions. If false, an explicit enzymatic catalyst must be assigned to a
	 * reaction to obtain this status.
	 */
	public static final Option<Boolean> OPT_ALL_REACTIONS_ARE_ENZYME_CATALYZED = new Option<Boolean>(
			"OPT_ALL_REACTIONS_ARE_ENZYME_CATALYZED",
			Boolean.class,
			" If true, all reactions within the network are considered enzyme reactions. If false, an explicit enzymatic catalyst must be assigned to a reaction to obtain this status.");
	/**
	 * If not specified the value corresponding to this argument will be used to
	 * initialize the size of compartments.
	 */
	public static final Option<Double> OPT_DEFAULT_COMPARTMENT_INITIAL_SIZE = new Option<Double>(
			"OPT_DEFAULT_COMPARTMENT_INITIAL_SIZE",
			Double.class,
			" If not specified the value corresponding to this argument will be used to initialize the size of compartments.");
	/**
	 * If not specified the value corresponding to this argument will be used to
	 * initialize species depending on their hasOnlySubstanceUnits property as
	 * initial amount or initial concentration.
	 */
	public static final Option<Double> OPT_DEFAULT_SPECIES_INITIAL_VALUE = new Option<Double>(
			"OPT_DEFAULT_SPECIES_INITIAL_VALUE",
			Double.class,
			"If not specified the value corresponding to this argument will be used to initialize species depending on their hasOnlySubstanceUnits property as initial amount or initial concentration.");
	/**
	 * The value that is set for newly created parameters.
	 */
	public static final Option<Double> OPT_DEFAULT_VALUE_OF_NEW_PARAMETERS = new Option<Double>(
			"OPT_DEFAULT_VALUE_OF_NEW_PARAMETERS", Double.class,
			"The value that is set for newly created parameters.");
	/**
	 * If true a new rate law will be created for each reaction irrespective of
	 * whether there is already a rate law assigned to this reaction or not.
	 */
	public static final Option<Boolean> OPT_GENERATE_KINETIC_LAW_FOR_EACH_REACTION = new Option<Boolean>(
			"OPT_GENERATE_KINETIC_LAW_FOR_EACH_REACTION",
			Boolean.class,
			"If true a new rate law will be created for each reaction irrespective of whether there is already a rate law assigned to this reaction or not.");
	/**
	 * Allows the user to ignore species that are annotated with the given
	 * compound identifiers when creating rate laws for reactions that involve
	 * these species. For instance, water or single protons can often be ignored
	 * when creating rate equations, hence simplifying the resulting rate
	 * equations. Preselected are the KEGG compound identifiers for water and
	 * protons
	 */
	public static final Option<List> OPT_IGNORE_THESE_SPECIES_WHEN_CREATING_LAWS = new Option<List>(
			"OPT_IGNORE_THESE_SPECIES_WHEN_CREATING_LAWS",
			List.class,
			"Allows the user to ignore species that are annotated with the given compound identifiers when creating rate laws for reactions that involve these species. For instance, water or single protons can often be ignored when creating rate equations, hence simplifying the resulting rate equations. Preselected are the KEGG compound identifiers for water and protons.");
	/**
	 * The maximal number of reactants so that the reaction is still considered
	 * plausible.
	 */
	public static final Option<Integer> OPT_MAX_NUMBER_OF_REACTANTS = new Option<Integer>(
			"OPT_MAX_NUMBER_OF_REACTANTS",
			Integer.class,
			"The maximal number of reactants so that the reaction is still considered plausible.");
	/**
	 * If true parameters and units that are never referenced by any element of
	 * the model are deleted when creating kinetic equations with SBMLsqueezer.
	 */
	public static final Option<Boolean> OPT_REMOVE_UNNECESSARY_PARAMETERS_AND_UNITS = new Option<Boolean>(
			"OPT_REMOVE_UNNECESSARY_PARAMETERS_AND_UNITS",
			Boolean.class,
			"If true parameters and units that are never referenced by any element of the model are deleted when creating kinetic equations with SBMLsqueezer.");
	/**
	 * Decide whether or not to set the boundary condition for genes to true.
	 */
	public static final Option<Boolean> OPT_SET_BOUNDARY_CONDITION_FOR_GENES = new Option<Boolean>(
			"OPT_SET_BOUNDARY_CONDITION_FOR_GENES", Boolean.class,
			"Decide whether or not to set the boundary condition for genes to true.");
	/**
	 * Property that decides whether to set all reactions to reversible before
	 * creating new kinetic equations.
	 */
	public static final Option<Boolean> OPT_TREAT_ALL_REACTIONS_REVERSIBLE = new Option<Boolean>(
			"OPT_TREAT_ALL_REACTIONS_REVERSIBLE",
			Boolean.class,
			"Property that decides whether to set all reactions to reversible before creating new kinetic equations.");
	/**
	 * If true, warnings will be displayed for too many reactants.
	 */
	public static final Option<Boolean> OPT_WARNINGS_FOR_TOO_MANY_REACTANTS = new Option<Boolean>(
			"OPT_WARNINGS_FOR_TOO_MANY_REACTANTS", Boolean.class,
			"If true, warnings will be displayed for too many reactants.");
	/**
	 * Determines whether or not antisense RNA molecules are accepted as enzymes
	 * when catalyzing a reaction.
	 */
	public static final Option<Boolean> POSSIBLE_ENZYME_ANTISENSE_RNA = new Option<Boolean>(
			"POSSIBLE_ENZYME_ANTISENSE_RNA",
			Boolean.class,
			"Determines whether or not antisense RNA molecules are accepted as enzymes when catalyzing a reaction.");
	/**
	 * Determines whether or not enzyme complexes are accepted as enzymes when
	 * catalyzing a reaction.
	 */
	public static final Option<Boolean> POSSIBLE_ENZYME_COMPLEX = new Option<Boolean>(
			"POSSIBLE_ENZYME_COMPLEX",
			Boolean.class,
			"Determines whether or not enzyme complexes are accepted as enzymes when catalyzing a reaction.");
	/**
	 * Determines whether or not generic proteins are accepted as enzymes when
	 * catalyzing a reaction.
	 */
	public static final Option<Boolean> POSSIBLE_ENZYME_GENERIC = new Option<Boolean>(
			"POSSIBLE_ENZYME_GENERIC",
			Boolean.class,
			"Determines whether or not generic proteins are accepted as enzymes when catalyzing a reaction.");
	/**
	 * Determines whether or not receptors are accepted as enzymes when
	 * catalyzing a reaction.
	 */
	public static final Option<Boolean> POSSIBLE_ENZYME_RECEPTOR = new Option<Boolean>(
			"POSSIBLE_ENZYME_RECEPTOR",
			Boolean.class,
			"Determines whether or not receptors are accepted as enzymes when catalyzing a reaction.");
	/**
	 * Determines whether or not RNA molecules are accepted as enzymes when
	 * catalyzing a reaction.
	 */
	public static final Option<Boolean> POSSIBLE_ENZYME_RNA = new Option<Boolean>(
			"POSSIBLE_ENZYME_RNA",
			Boolean.class,
			"Determines whether or not RNA molecules are accepted as enzymes when catalyzing a reaction.");
	/**
	 * Determines whether or not simple molecules are accepted as enzymes when
	 * catalyzing a reaction.
	 */
	public static final Option<Boolean> POSSIBLE_ENZYME_SIMPLE_MOLECULE = new Option<Boolean>(
			"POSSIBLE_ENZYME_SIMPLE_MOLECULE",
			Boolean.class,
			"Determines whether or not simple molecules are accepted as enzymes when catalyzing a reaction.");
	/**
	 * Determines whether or not trunkated proteins are accepted as enzymes when
	 * catalyzing a reaction.
	 */
	public static final Option<Boolean> POSSIBLE_ENZYME_TRUNCATED = new Option<Boolean>(
			"POSSIBLE_ENZYME_TRUNCATED",
			Boolean.class,
			"Determines whether or not trunkated proteins are accepted as enzymes when catalyzing a reaction.");
	/**
	 * Determines whether or not unknown molecules are accepted as enzymes when
	 * catalyzing a reaction.
	 */
	public static final Option<Boolean> POSSIBLE_ENZYME_UNKNOWN = new Option<Boolean>(
			"POSSIBLE_ENZYME_UNKNOWN",
			Boolean.class,
			"Determines whether or not unknown molecules are accepted as enzymes when catalyzing a reaction.");
	/**
	 * SBML input file.
	 */
	public static final Option<File> SBML_FILE = new Option<File>("SBML_FILE",
			File.class, "SBML input file.");
	/**
	 * Specifies the file where SBMLsqueezer writes its SBML output.
	 */
	public static final Option<File> SBML_OUT_FILE = new Option<File>(
			"SBML_OUT_FILE", File.class,
			"Specifies the file where SBMLsqueezer writes its SBML output.");
	/**
	 * Can be true or false, depending on if the user wants to see SBML
	 * warnings.
	 */
	public static final Option<Boolean> SHOW_SBML_WARNINGS = new Option<Boolean>(
			"SHOW_SBML_WARNINGS", Boolean.class,
			"Can be true or false, depending on if the user wants to see SBML warnings.");

	/**
	 * One of the following values: cat, hal or weg (important for
	 * Liebermeister's standard kinetics).
	 */
	public static final Option<TypeStandardVersion> TYPE_STANDARD_VERSION = new Option<TypeStandardVersion>(
			"TYPE_STANDARD_VERSION",
			TypeStandardVersion.class,
			"One of the following values: cat, hal or weg (important for Liebermeister's standard kinetics).",
			new Range<TypeStandardVersion>(TypeStandardVersion.class,
					Range.toRangeString(TypeStandardVersion.class)));

	/**
	 * How to ensure unit consistency in kinetic equations? One way is to set
	 * each participating species to an initial amount and to set the unit to
	 * mmole. The other way is to set the initial concentration of each species,
	 * set the unit to mmole per l and to multiply the species with the size of
	 * the surrounding compartment whenever it occurs in a kinetic equation.
	 * Hence, this type parameter belongs to two values.
	 */
	public static final Option<TypeUnitConsistency> TYPE_UNIT_CONSISTENCY = new Option<TypeUnitConsistency>(
			"TYPE_UNIT_CONSISTENCY",
			TypeUnitConsistency.class,
			"How to ensure unit consistency in kinetic equations? One way is to set each participating species to an initial amount and to set the unit to mmole. The other way is to set the initial concentration of each species, set the unit to mmole per l and to multiply the species with the size of the surrounding compartment whenever it occurs in a kinetic equation. Hence, this type parameter belongs to two values.",
			new Range<TypeUnitConsistency>(TypeUnitConsistency.class,
					Range.toRangeString(TypeUnitConsistency.class)));

}
