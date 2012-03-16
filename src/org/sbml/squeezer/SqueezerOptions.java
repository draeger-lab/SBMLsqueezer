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

import java.util.ResourceBundle;

import org.sbml.squeezer.kinetics.ConvenienceKinetics;
import org.sbml.squeezer.kinetics.GeneralizedMassAction;
import org.sbml.squeezer.kinetics.HillHinzeEquation;
import org.sbml.squeezer.kinetics.MichaelisMenten;
import org.sbml.squeezer.kinetics.RandomOrderMechanism;
import org.sbml.squeezer.util.Bundles;

import de.zbit.util.ResourceManager;
import de.zbit.util.objectwrapper.ValuePairUncomparable;
import de.zbit.util.prefs.KeyProvider;
import de.zbit.util.prefs.Option;
import de.zbit.util.prefs.OptionGroup;
import de.zbit.util.prefs.Range;

/**
 * This is a list of possible command line options and configuration of
 * SBMLsqueezer. Each element listed here determines a key for a configuration
 * value.
 * 
 * @author Andreas Dr&auml;ger
 * @author Sarah R. M&uuml;ller vom Hagen
 * @author Sebastian Nagel
 * @since 1.4
 * @version $Rev$
 */
public interface SqueezerOptions extends KeyProvider {
	
	/**
	 * 
	 */
	public static final ResourceBundle OPTIONS_BUNDLE = ResourceManager.getBundle(Bundles.OPTIONS);
	
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
	 * @author Sebastian Nagel
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
	 * Determines the key for the standard kinetic law to be applied for
	 * reactions that are identified to be enzyme-catalyzed (with or without
	 * explicit catalyst) and that do not belong to one of the other standard
	 * enzyme-catalysis schemes. The value can be any rate law that implements
	 * {@link org.sbml.squeezer.kinetics.InterfaceArbitraryEnzymeKinetics}.
	 */
	@SuppressWarnings("rawtypes")
	public static final Option<Class> KINETICS_ARBITRARY_ENZYME_REACTIONS = new Option<Class>(
			"KINETICS_ARBITRARY_ENZYME_REACTIONS",
			Class.class,
			OPTIONS_BUNDLE,
			new Range<Class>(Class.class, SBMLsqueezer.getKineticsArbitraryEnzymeMechanism()),
			ConvenienceKinetics.class);

	/**
	 * The class name of the default kinetic law for bi-bi reactions. This can
	 * be any class that implements the
	 * {@link org.sbml.squeezer.kinetics.InterfaceBiBiKinetics}.
	 */
	@SuppressWarnings("rawtypes")
	public static final Option<Class> KINETICS_BI_BI_TYPE = new Option<Class>(
			"KINETICS_BI_BI_TYPE",
			Class.class,
			OPTIONS_BUNDLE,
			new Range<Class>(Class.class, SBMLsqueezer.getKineticsBiBi()),
			RandomOrderMechanism.class);

	/**
	 * The class name of the default kinetic law for bi-uni reactions. This can
	 * be any class that implements the
	 * {@link org.sbml.squeezer.kinetics.InterfaceBiUniKinetics}.
	 */
	@SuppressWarnings("rawtypes")
	public static final Option<Class> KINETICS_BI_UNI_TYPE = new Option<Class>(
			"KINETICS_BI_UNI_TYPE",
			Class.class,
			OPTIONS_BUNDLE,
			new Range<Class>(Class.class, SBMLsqueezer.getKineticsBiUni()),
			RandomOrderMechanism.class);

	/**
	 * Determines the key for the standard kinetic law to be applied for
	 * reactions that are identified to belong to gene-regulatory processes,
	 * such as transcription or translation. The value is the class name of any
	 * class that implements the
	 * {@link org.sbml.squeezer.kinetics.InterfaceGeneRegulatoryNetworks}.
	 */
	@SuppressWarnings("rawtypes")
	public static final Option<Class> KINETICS_GENE_REGULATION = new Option<Class>(
			"KINETICS_GENE_REGULATION",
			Class.class,
			OPTIONS_BUNDLE,
			new Range<Class>(Class.class, SBMLsqueezer.getKineticsGeneRegulatoryNetworks()),
			HillHinzeEquation.class);

	/**
	 * Determines the key for the standard kinetic law to be applied for
	 * reactions that are catalyzed by non-enzymes or that are not catalyzed at
	 * all. The value may be any rate law that implements
	 * {@link org.sbml.squeezer.kinetics.InterfaceNonEnzymeKinetics}.
	 */
	@SuppressWarnings("rawtypes")
	public static final Option<Class> KINETICS_NONE_ENZYME_REACTIONS = new Option<Class>(
			"KINETICS_NONE_ENZYME_REACTIONS",
			Class.class,
			OPTIONS_BUNDLE,
			new Range<Class>(Class.class, SBMLsqueezer.getKineticsNonEnzyme()),
			GeneralizedMassAction.class);

	/**
	 * This key defines the default kinetic law to be applied to
	 * enzyme-catalyzed reactions with one reactant and one product. Possible
	 * values are the names of classes that implement
	 * {@link org.sbml.squeezer.kinetics.InterfaceUniUniKinetics}.
	 */
	@SuppressWarnings("rawtypes")
	public static final Option<Class> KINETICS_UNI_UNI_TYPE = new Option<Class>(
			"KINETICS_UNI_UNI_TYPE",
			Class.class,
			OPTIONS_BUNDLE,
			new Range<Class>(Class.class, SBMLsqueezer.getKineticsUniUni()),
			MichaelisMenten.class);
	
	/**
	 * If true all parameters are stored globally for the whole model (default)
	 * else parameters are stored locally for the respective kinetic equation
	 * they belong to.
	 */
	public static final Option<Boolean> NEW_PARAMETERS_GLOBAL = new Option<Boolean>(
			"NEW_PARAMETERS_GLOBAL",
			Boolean.class,			
			OPTIONS_BUNDLE,
			true);

	/**
	 * If true, all reactions within the network are considered enzyme
	 * reactions. If false, an explicit enzymatic catalyst must be assigned to a
	 * reaction to obtain this status.
	 */
	public static final Option<Boolean> ALL_REACTIONS_AS_ENZYME_CATALYZED = new Option<Boolean>(
			"ALL_REACTIONS_AS_ENZYME_CATALYZED",
			Boolean.class,
			OPTIONS_BUNDLE,
			false);

	/**
	 * If not specified the value corresponding to this argument will be used to
	 * initialize the size of compartments.
	 */
	public static final Option<Double> DEFAULT_COMPARTMENT_SIZE = new Option<Double>(
			"DEFAULT_COMPARTMENT_SIZE",
			Double.class,
			OPTIONS_BUNDLE,
			1.0d);
	
	/**
	 * If not specified the value corresponding to this argument will be used to
	 * initialize species depending on their hasOnlySubstanceUnits property as
	 * initial amount or initial concentration.
	 */
	public static final Option<Double> DEFAULT_SPECIES_INIT_VAL = new Option<Double>(
			"DEFAULT_SPECIES_INIT_VAL",
			Double.class,
			OPTIONS_BUNDLE,
			1.0d);

	/**
	 * The value that is set for newly created parameters.
	 */
	public static final Option<Double> DEFAULT_NEW_PARAMETER_VAL = new Option<Double>(
			"DEFAULT_NEW_PARAMETER_VAL", 
			Double.class,
			OPTIONS_BUNDLE,
			1.0d);
	
	/**
	 * If true a new rate law will be created for each reaction irrespective of
	 * whether there is already a rate law assigned to this reaction or not.
	 */
	public static final Option<Boolean> GENERATE_KINETIC_LAWS_FOR_ALL_REACTIONS = new Option<Boolean>(
			"GENERATE_KINETIC_LAWS_FOR_ALL_REACTIONS",
			Boolean.class,
			OPTIONS_BUNDLE,
			true);

	/**
	 * If true kinetics are only generated if missing in the SBML file.
	 */
	public static final Option<Boolean> GENERATE_KINETIC_LAW_ONLY_IF_MISSING = new Option<Boolean>(
			"GENERATE_KINETIC_LAW_ONLY_IF_MISSING",
			Boolean.class,
			OPTIONS_BUNDLE,
			false);
	
	/**
	 * Allows the user to ignore species that are annotated with the given
	 * compound identifiers when creating rate laws for reactions that involve
	 * these species. For instance, water or single protons can often be ignored
	 * when creating rate equations, hence simplifying the resulting rate
	 * equations. Preselected are the KEGG compound identifiers for water and
	 * protons.
	 */
	public static final Option<String> IGNORE_THESE_SPECIES_WHEN_CREATING_LAWS = new Option<String>(
			"IGNORE_THESE_SPECIES_WHEN_CREATING_LAWS",
			String.class,
			OPTIONS_BUNDLE,
			"C00001,C00038,C00070,C00076,C00080,C00175,C00238,C00282,C00291,C01327,C01528,C14818,C14819"
			);
	
	/**
	 * If true parameters and units that are never referenced by any element of
	 * the model are deleted when creating kinetic equations with SBMLsqueezer.
	 */
	public static final Option<Boolean> REMOVE_UNNECESSARY_PARAMETERS_AND_UNITS = new Option<Boolean>(
			"REMOVE_UNNECESSARY_PARAMETERS_AND_UNITS",
			Boolean.class,
			OPTIONS_BUNDLE,
			true);
	
	/**
	 * Decide whether or not to set the boundary condition for genes to true.
	 */
	public static final Option<Boolean> SET_BOUNDARY_CONDITION_FOR_GENES = new Option<Boolean>(
			"SET_BOUNDARY_CONDITION_FOR_GENES", 
			Boolean.class,
			OPTIONS_BUNDLE,
			true);
	
	/**
	 * Helper variable that contains as only possible value within its
	 * {@link Range} the value {@link Boolean#TRUE}.
	 */
	public static final Range<Boolean> RANGE_BOOLEAN = new Range<Boolean>(Boolean.class, Boolean.TRUE);
	
	
	/**
	 * If true the information about reversiblity will be left unchanged.
	 */
	public static final Option<Boolean> TREAT_REACTIONS_REVERSIBLE_AS_GIVEN = new Option<Boolean>(
			"TREAT_REACTIONS_REVERSIBLE_AS_GIVEN",
			Boolean.class,
			OPTIONS_BUNDLE,
			false);
	
	/**
	 * Property that decides whether to set all reactions to reversible before
	 * creating new kinetic equations.
	 */
	public static final Option<Boolean> TREAT_ALL_REACTIONS_REVERSIBLE = new Option<Boolean>(
			"TREAT_ALL_REACTIONS_REVERSIBLE",
			Boolean.class,
			OPTIONS_BUNDLE,
			true);

	/**
	 * If true, warnings will be displayed for too many reactants.
	 */
	public static final Option<Boolean> WARNINGS_FOR_TOO_MANY_REACTANTS = new Option<Boolean>(
			"WARNINGS_FOR_TOO_MANY_REACTANTS", 
			Boolean.class,
			OPTIONS_BUNDLE,
			true);
	
	/**
	 * The maximal number of reactants so that the reaction is still considered
	 * plausible.
	 */
	@SuppressWarnings("unchecked")
	public static final Option<Integer> MAX_NUMBER_OF_REACTANTS = new Option<Integer>(
			"MAX_NUMBER_OF_REACTANTS",
			Integer.class,
			OPTIONS_BUNDLE,
			3,
			new ValuePairUncomparable<Option<Boolean>, 
			Range<Boolean>>(WARNINGS_FOR_TOO_MANY_REACTANTS, RANGE_BOOLEAN));

	/**
	 * Determines whether or not antisense RNA molecules are accepted as enzymes
	 * when catalyzing a reaction.
	 */
	public static final Option<Boolean> POSSIBLE_ENZYME_ANTISENSE_RNA = new Option<Boolean>(
			"POSSIBLE_ENZYME_ANTISENSE_RNA",
			Boolean.class,
			OPTIONS_BUNDLE,
			false);

	/**
	 * Determines whether or not enzyme complexes are accepted as enzymes when
	 * catalyzing a reaction.
	 */
	public static final Option<Boolean> POSSIBLE_ENZYME_COMPLEX = new Option<Boolean>(
			"POSSIBLE_ENZYME_COMPLEX",
			Boolean.class,
			OPTIONS_BUNDLE,
			true);

	/**
	 * Determines whether or not generic proteins are accepted as enzymes when
	 * catalyzing a reaction.
	 */
	public static final Option<Boolean> POSSIBLE_ENZYME_GENERIC = new Option<Boolean>(
			"POSSIBLE_ENZYME_GENERIC",
			Boolean.class,
			OPTIONS_BUNDLE,
			true);
	
	/**
	 * If this options is selected, species that are annotated as macromolecules
	 * are treated as enzymes when catalyzing a reaction. Otherwise,
	 * macormolecule-catalyzed reactions are not considered enzyme reactions. If
	 * a modifier of a reaction that is annotated as an enzymatic catalyst
	 * refers to a macromolecule but this option is not active, SBMLsqueezer
	 * will reduce the modifier to a simple catalyst.
	 */
	public static final Option<Boolean> POSSIBLE_ENZYME_MACROMOLECULE = new Option<Boolean>(
			"POSSIBLE_ENZYME_MACROMOLECULE", Boolean.class, OPTIONS_BUNDLE,
			true);
	
	/**
	 * Determines whether or not receptors are accepted as enzymes when
	 * catalyzing a reaction.
	 */
	public static final Option<Boolean> POSSIBLE_ENZYME_RECEPTOR = new Option<Boolean>(
			"POSSIBLE_ENZYME_RECEPTOR", Boolean.class, OPTIONS_BUNDLE,
			false);

	/**
	 * Determines whether or not RNA molecules are accepted as enzymes when
	 * catalyzing a reaction.
	 */
	public static final Option<Boolean> POSSIBLE_ENZYME_RNA = new Option<Boolean>(
			"POSSIBLE_ENZYME_RNA", Boolean.class, OPTIONS_BUNDLE,
			true);

	/**
	 * Determines whether or not simple molecules are accepted as enzymes when
	 * catalyzing a reaction.
	 */
	public static final Option<Boolean> POSSIBLE_ENZYME_SIMPLE_MOLECULE = new Option<Boolean>(
			"POSSIBLE_ENZYME_SIMPLE_MOLECULE", Boolean.class, OPTIONS_BUNDLE,
			false);

	/**
	 * Determines whether or not trunkated proteins are accepted as enzymes when
	 * catalyzing a reaction.
	 */
	public static final Option<Boolean> POSSIBLE_ENZYME_TRUNCATED = new Option<Boolean>(
			"POSSIBLE_ENZYME_TRUNCATED", Boolean.class, OPTIONS_BUNDLE,
			true);

	/**
	 * Determines whether or not unknown molecules are accepted as enzymes when
	 * catalyzing a reaction.
	 */
	public static final Option<Boolean> POSSIBLE_ENZYME_UNKNOWN = new Option<Boolean>(
			"POSSIBLE_ENZYME_UNKNOWN", Boolean.class, OPTIONS_BUNDLE,
			false);

	/**
	 * Can be true or false, depending on if the user wants to see SBML
	 * warnings.
	 */
	public static final Option<Boolean> SHOW_SBML_WARNINGS = new Option<Boolean>(
			"SHOW_SBML_WARNINGS", Boolean.class, OPTIONS_BUNDLE,
			true);
	
	/**
	 * One of the following values: cat, hal or weg (important for
	 * Liebermeister's standard kinetics).
	 */
	@SuppressWarnings("unchecked")
	public static final Option<TypeStandardVersion> TYPE_STANDARD_VERSION = new Option<TypeStandardVersion>(
			"TYPE_STANDARD_VERSION",
			TypeStandardVersion.class,
			OPTIONS_BUNDLE,
			new Range<TypeStandardVersion>(
					TypeStandardVersion.class, 
					Range.toRangeString(TypeStandardVersion.class)),
					TypeStandardVersion.cat, 
					new ValuePairUncomparable<Option<Boolean>, Range<Boolean>>(TREAT_ALL_REACTIONS_REVERSIBLE, RANGE_BOOLEAN));
	
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
			OPTIONS_BUNDLE,
			new Range<TypeUnitConsistency>(
					TypeUnitConsistency.class, 
					Range.toRangeString(TypeUnitConsistency.class)), 
					(short) 2,
					TypeUnitConsistency.amount);
	
	/**
	 * 
	 */
	@SuppressWarnings("unchecked")
	public static final OptionGroup<Boolean> GROUP_GENERAL = new OptionGroup<Boolean>(
			"GROUP_GENERAL",
			OPTIONS_BUNDLE,
			SET_BOUNDARY_CONDITION_FOR_GENES,
			ALL_REACTIONS_AS_ENZYME_CATALYZED,
			REMOVE_UNNECESSARY_PARAMETERS_AND_UNITS,
			NEW_PARAMETERS_GLOBAL,
			WARNINGS_FOR_TOO_MANY_REACTANTS,
			SHOW_SBML_WARNINGS);
	
	/**
	 * 
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static final OptionGroup<?> GROUP_DEFAULT_VALUES = new OptionGroup(
		"GROUP_DEFAULT_VALUES",
		OPTIONS_BUNDLE,
		MAX_NUMBER_OF_REACTANTS,
		DEFAULT_COMPARTMENT_SIZE,
		DEFAULT_SPECIES_INIT_VAL,
		DEFAULT_NEW_PARAMETER_VAL,
		IGNORE_THESE_SPECIES_WHEN_CREATING_LAWS);
	
	/**
	 * 
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static final OptionGroup<Class> GROUP_KINETICS = new OptionGroup<Class>(
			"GROUP_KINETICS",
			OPTIONS_BUNDLE,
			KINETICS_ARBITRARY_ENZYME_REACTIONS,
			KINETICS_BI_BI_TYPE,
			KINETICS_BI_UNI_TYPE,
			KINETICS_GENE_REGULATION,
			KINETICS_NONE_ENZYME_REACTIONS,
			KINETICS_UNI_UNI_TYPE);

	/**
	 * 
	 */
	@SuppressWarnings("unchecked")
	public static final OptionGroup<Boolean> GROUP_ENZYMES = new OptionGroup<Boolean>(
			"GROUP_ENZYMES",
			OPTIONS_BUNDLE,
			POSSIBLE_ENZYME_ANTISENSE_RNA,
			POSSIBLE_ENZYME_COMPLEX,
			POSSIBLE_ENZYME_GENERIC,
			POSSIBLE_ENZYME_MACROMOLECULE,
			POSSIBLE_ENZYME_RECEPTOR,
			POSSIBLE_ENZYME_RNA,
			POSSIBLE_ENZYME_SIMPLE_MOLECULE,
			POSSIBLE_ENZYME_TRUNCATED,
			POSSIBLE_ENZYME_UNKNOWN);
	
	/**
	 * 
	 */
	@SuppressWarnings("unchecked")
	public static final OptionGroup<Boolean> GROUP_REVERSIBILITY = new OptionGroup<Boolean>(
			"GROUP_REVERSIBILITY",
			OPTIONS_BUNDLE,
			true,
			TREAT_ALL_REACTIONS_REVERSIBLE,
			TREAT_REACTIONS_REVERSIBLE_AS_GIVEN);
	
	/**
	 * 
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static final OptionGroup<?> GROUP_STANDARD_VERSION = new OptionGroup(
		"GROUP_STANDARD_VERSION",
		OPTIONS_BUNDLE,
		TYPE_STANDARD_VERSION);
	
	/**
	 * 
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static final OptionGroup<?> GROUP_UNIT_CONSISTENCY = new OptionGroup(
		"GROUP_UNIT_CONSISTENCY",
		OPTIONS_BUNDLE,
		TYPE_UNIT_CONSISTENCY);
	
	/**
	 * 
	 */
	@SuppressWarnings("unchecked")
	public static final OptionGroup<Boolean> GROUP_GENERATE_KINETIC_LAWS = new OptionGroup<Boolean>(
			"GROUP_GENERATE_KINETIC_LAWS",
			OPTIONS_BUNDLE,
			true,
			GENERATE_KINETIC_LAWS_FOR_ALL_REACTIONS,
			GENERATE_KINETIC_LAW_ONLY_IF_MISSING);

}
