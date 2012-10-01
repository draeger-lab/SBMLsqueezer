/*
 * $Id: SqueezerOptionsRateLaws.java 14.09.2012 10:14:12 draeger$
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
package org.sbml.squeezer.kinetics;

import java.util.ResourceBundle;

import org.sbml.squeezer.SBMLsqueezer;
import org.sbml.squeezer.OptionsGeneral;
import org.sbml.squeezer.util.Bundles;

import de.zbit.util.ResourceManager;
import de.zbit.util.objectwrapper.ValuePairUncomparable;
import de.zbit.util.prefs.KeyProvider;
import de.zbit.util.prefs.Option;
import de.zbit.util.prefs.OptionGroup;
import de.zbit.util.prefs.Range;

/**
 * User settings for rate laws.
 * 
 * @author Andreas Dr&auml;ger
 * @version $Rev$
 * @since 1.4
 */
public interface OptionsRateLaws extends KeyProvider {

	/**
	 * 
	 */
	public static final ResourceBundle OPTIONS_BUNDLE = ResourceManager.getBundle(Bundles.OPTIONS);
	
	/**
	 * Property that decides whether to set all reactions to reversible before
	 * creating new kinetic equations.
	 */
	public static final Option<Boolean> TREAT_ALL_REACTIONS_REVERSIBLE = new Option<Boolean>(
			"TREAT_ALL_REACTIONS_REVERSIBLE",
			Boolean.class,
			OPTIONS_BUNDLE,
			Boolean.valueOf(true));
	
	/**
	 * If true the information about reversiblity will be left unchanged.
	 */
	public static final Option<Boolean> TREAT_REACTIONS_REVERSIBLE_AS_GIVEN = new Option<Boolean>(
			"TREAT_REACTIONS_REVERSIBLE_AS_GIVEN",
			Boolean.class,
			OPTIONS_BUNDLE,
			Boolean.valueOf(false));

	/**
	 * One of the following values: cat, hal or weg (important for
	 * Liebermeister's standard kinetics).
	 */
	public static final Option<TypeStandardVersion> TYPE_STANDARD_VERSION = new Option<TypeStandardVersion>(
			"TYPE_STANDARD_VERSION",
			TypeStandardVersion.class,
			OPTIONS_BUNDLE,
			new Range<TypeStandardVersion>(
					TypeStandardVersion.class, 
					Range.toRangeString(TypeStandardVersion.class)),
					TypeStandardVersion.cat);
	
	/**
	 * Determines the key for the standard kinetic law to be applied for
	 * reactions that are identified to be enzyme-catalyzed (with or without
	 * explicit catalyst) and that do not belong to one of the other standard
	 * enzyme-catalysis schemes. The value can be any rate law that implements
	 * {@link org.sbml.squeezer.kinetics.InterfaceArbitraryEnzymeKinetics}.
	 */
	@SuppressWarnings("rawtypes")
	public static final Option<Class> KINETICS_REVERSIBLE_ARBITRARY_ENZYME_REACTIONS = new Option<Class>(
			"KINETICS_REVERSIBLE_ARBITRARY_ENZYME_REACTIONS",
			Class.class,
			OPTIONS_BUNDLE,
			new Range<Class>(Class.class, SBMLsqueezer.getKineticsReversibleArbitraryEnzymeMechanism()),
			CommonModularRateLaw.class);

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
	public static final Option<Class> KINETICS_REVERSIBLE_NON_ENZYME_REACTIONS = new Option<Class>(
			"KINETICS_REVERSIBLE_NON_ENZYME_REACTIONS",
			Class.class,
			OPTIONS_BUNDLE,
			new Range<Class>(Class.class, SBMLsqueezer.getKineticsReversibleNonEnzyme()),
			GeneralizedMassAction.class);
	
	/**
	 * Determines the key for the standard kinetic law to be applied for
	 * reactions that are catalyzed by non-enzymes or that are not catalyzed at
	 * all. The value may be any rate law that implements
	 * {@link org.sbml.squeezer.kinetics.InterfaceNonEnzymeKinetics}.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static final Option<Class> KINETICS_IRREVERSIBLE_NON_ENZYME_REACTIONS = new Option<Class>(
			"KINETICS_IRREVERSIBLE_NON_ENZYME_REACTIONS",
			Class.class,
			OPTIONS_BUNDLE,
			new Range<Class>(Class.class, SBMLsqueezer.getKineticsIrreversibleNonEnzyme()),
			GeneralizedMassAction.class,
			new ValuePairUncomparable<Option<Boolean>, Range<Boolean>>(
					TREAT_ALL_REACTIONS_REVERSIBLE,
					OptionsGeneral.RANGE_FALSE));

	/**
	 * This key defines the default kinetic law to be applied to
	 * enzyme-catalyzed reactions with one reactant and one product. Possible
	 * values are the names of classes that implement
	 * {@link org.sbml.squeezer.kinetics.InterfaceUniUniKinetics}.
	 */
	@SuppressWarnings("rawtypes")
	public static final Option<Class> KINETICS_REVERSIBLE_UNI_UNI_TYPE = new Option<Class>(
			"KINETICS_REVERSIBLE_UNI_UNI_TYPE",
			Class.class,
			OPTIONS_BUNDLE,
			new Range<Class>(Class.class, SBMLsqueezer.getKineticsReversibleUniUni()),
			MichaelisMenten.class);

	/**
	 * Default rate law with zeroth order reactants
	 */
	@SuppressWarnings("rawtypes")
	public static final Option<Class> KINETICS_ZERO_REACTANTS = new Option<Class>(
			"KINETICS_ZERO_REACTANTS",
			Class.class,
			OPTIONS_BUNDLE,
			new Range<Class>(Class.class, SBMLsqueezer.getKineticsZeroReactants()),
			ZerothOrderReverseGMAK.class);
	
	/**
	 * Default rate law with zeroth order products
	 */
	@SuppressWarnings("rawtypes")
	public static final Option<Class> KINETICS_ZERO_PRODUCTS = new Option<Class>(
			"KINETICS_ZERO_PRODUCTS",
			Class.class,
			OPTIONS_BUNDLE,
			new Range<Class>(Class.class, SBMLsqueezer.getKineticsZeroProducts()),
			ZerothOrderReverseGMAK.class);
	
	/**
	 * 
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static final Option<Class> KINETICS_IRREVERSIBLE_ARBITRARY_ENZYME_REACTIONS = new Option<Class>(
			"KINETICS_IRREVERSIBLE_ARBITRARY_ENZYME_REACTIONS",
			Class.class,
			OPTIONS_BUNDLE,
			new Range<Class>(Class.class, SBMLsqueezer.getKineticsIrreversibleArbitraryEnzymeMechanism()),
			IrrevNonModulatedNonInteractingEnzymes.class,
			new ValuePairUncomparable<Option<Boolean>, Range<Boolean>>(
					TREAT_ALL_REACTIONS_REVERSIBLE,
					OptionsGeneral.RANGE_FALSE));

	/**
	 * The class name of the default kinetic law for reversible bi-bi reactions.
	 * This can be any class that implements the {@link InterfaceBiBiKinetics} and
	 * {@link InterfaceReversibleKinetics}.
	 */
	@SuppressWarnings("rawtypes")
	public static final Option<Class> KINETICS_REVERSIBLE_BI_BI_TYPE = new Option<Class>(
			"KINETICS_REVERSIBLE_BI_BI_TYPE",
			Class.class,
			OPTIONS_BUNDLE,
			new Range<Class>(Class.class, SBMLsqueezer.getKineticsReversibleBiBi()),
			RandomOrderMechanism.class);
	
	/**
	 * The class name of the default kinetic law for irreversible bi-bi reactions.
	 * This can be any class that implements the {@link InterfaceBiBiKinetics} and
	 * {@link InterfaceIrreversibleKinetics}.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static final Option<Class> KINETICS_IRREVERSIBLE_BI_BI_TYPE = new Option<Class>(
			"KINETICS_IRREVERSIBLE_BI_BI_TYPE",
			Class.class,
			OPTIONS_BUNDLE,
			new Range<Class>(Class.class, SBMLsqueezer.getKineticsIrreversibleBiBi()),
			RandomOrderMechanism.class,
			new ValuePairUncomparable<Option<Boolean>, Range<Boolean>>(
					TREAT_ALL_REACTIONS_REVERSIBLE,
					OptionsGeneral.RANGE_FALSE));

	/**
	 * The class name of the default kinetic law for bi-uni reactions. This can
	 * be any class that implements the
	 * {@link org.sbml.squeezer.kinetics.InterfaceBiUniKinetics}.
	 */
	@SuppressWarnings("rawtypes")
	public static final Option<Class> KINETICS_REVERSIBLE_BI_UNI_TYPE = new Option<Class>(
			"KINETICS_REVERSIBLE_BI_UNI_TYPE",
			Class.class,
			OPTIONS_BUNDLE,
			new Range<Class>(Class.class, SBMLsqueezer.getKineticsReversibleBiUni()),
			RandomOrderMechanism.class);
	
	/**
	 * The class name of the default kinetic law for bi-uni reactions. This can
	 * be any class that implements the
	 * {@link org.sbml.squeezer.kinetics.InterfaceBiUniKinetics}.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static final Option<Class> KINETICS_IRREVERSIBLE_BI_UNI_TYPE = new Option<Class>(
			"KINETICS_IRREVERSIBLE_BI_UNI_TYPE",
			Class.class,
			OPTIONS_BUNDLE,
			new Range<Class>(Class.class, SBMLsqueezer.getKineticsIrreversibleBiUni()),
			RandomOrderMechanism.class,
			new ValuePairUncomparable<Option<Boolean>, Range<Boolean>>(
					TREAT_ALL_REACTIONS_REVERSIBLE,
					OptionsGeneral.RANGE_FALSE));

	
	/**
	 * This key defines the default kinetic law to be applied to
	 * enzyme-catalyzed reactions with one reactant and one product. Possible
	 * values are the names of classes that implement
	 * {@link org.sbml.squeezer.kinetics.InterfaceUniUniKinetics}.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static final Option<Class> KINETICS_IRREVERSIBLE_UNI_UNI_TYPE = new Option<Class>(
			"KINETICS_IRREVERSIBLE_UNI_UNI_TYPE",
			Class.class,
			OPTIONS_BUNDLE,
			new Range<Class>(Class.class, SBMLsqueezer.getKineticsIrreversibleUniUni()),
			MichaelisMenten.class,
			new ValuePairUncomparable<Option<Boolean>, Range<Boolean>>(
					TREAT_ALL_REACTIONS_REVERSIBLE,
					OptionsGeneral.RANGE_FALSE));

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
	 * Select the default rate law for each irreversible mechanism.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static final OptionGroup<Class> GROUP_GENE_REGULATION_KINETICS = new OptionGroup<Class>(
			"GROUP_GENE_REGULATION_KINETICS",
			OPTIONS_BUNDLE,
			KINETICS_GENE_REGULATION,
			KINETICS_ZERO_REACTANTS,
			KINETICS_ZERO_PRODUCTS);
	
	/**
	 * Select the default rate law for each reversible mechanism.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static final OptionGroup<?> GROUP_REVERSIBLE_KINETICS = new OptionGroup(
			"GROUP_REVERSIBLE_KINETICS",
			OPTIONS_BUNDLE,
			TYPE_STANDARD_VERSION,
			KINETICS_REVERSIBLE_NON_ENZYME_REACTIONS,
			KINETICS_REVERSIBLE_UNI_UNI_TYPE,
			KINETICS_REVERSIBLE_BI_UNI_TYPE,
			KINETICS_REVERSIBLE_BI_BI_TYPE,
			KINETICS_REVERSIBLE_ARBITRARY_ENZYME_REACTIONS);
	
	/**
	 * Select the default rate law for each irreversible mechanism.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static final OptionGroup<Class> GROUP_IRREVERSIBLE_KINETICS = new OptionGroup<Class>(
			"GROUP_IRREVERSIBLE_KINETICS",
			OPTIONS_BUNDLE,
			KINETICS_IRREVERSIBLE_NON_ENZYME_REACTIONS,
			KINETICS_IRREVERSIBLE_UNI_UNI_TYPE,
			KINETICS_IRREVERSIBLE_BI_UNI_TYPE,
			KINETICS_IRREVERSIBLE_BI_BI_TYPE,
			KINETICS_IRREVERSIBLE_ARBITRARY_ENZYME_REACTIONS);
	
}
