package org.sbml.squeezer.functionTermGenerator;

import java.util.ResourceBundle;

import de.zbit.util.prefs.OptionGroup;
import de.zbit.util.prefs.Range;
import org.sbml.jsbml.ext.qual.Sign;
import org.sbml.squeezer.util.Bundles;

import de.zbit.util.ResourceManager;
import de.zbit.util.prefs.KeyProvider;
import de.zbit.util.prefs.Option;
/**
 * This is a list of possible command line options and configuration of
 * SBMLsqueezer, concerning the generation of default function terms.
 * 
 * @author Andreas Dr&auml;ger
 * @author Lisa Falk
 * @since 2.1.1
 *
 */

public interface FunctionTermOptions extends KeyProvider{

	public static final ResourceBundle OPTIONS_BUNDLE = ResourceManager.getBundle(Bundles.OPTIONS);

	public static final Option<Sign> DEFAULT_SIGN = new Option<Sign>(
			"DEFAULT_SIGN",
			Sign.class,
			OPTIONS_BUNDLE,
			new Range<Sign>(
					Sign.class,
					Sign.positive, Sign.negative, Sign.dual
			),
			Sign.positive);
	
	public static final Option<String> DEFAULT_TERM = new Option<String>(
			"DEFAULT_TERM",
			String.class,
			OPTIONS_BUNDLE,
			new Range<String>(
					String.class,
					OPTIONS_BUNDLE.getString("ONE_ACTI"), OPTIONS_BUNDLE.getString("ALL_ACTI")),
			OPTIONS_BUNDLE.getString("ALL_ACTI"));

	/**
	 * If {@code true} a new (default) function term will be created for each transition irrespective of
	 * whether there is already a function terms assigned to this transition or not.
	 */
	public static final Option<Boolean> OVERWRITE_EXISTING_FUNCTION_TERMS = new Option<Boolean>(
			"OVERWRITE_EXISTING_FUNCTION_TERMS",
			Boolean.class,
			OPTIONS_BUNDLE,
			Boolean.valueOf(false));

	/**
	 *
	 */
	@SuppressWarnings({ "unchecked" })
	public static final OptionGroup<Boolean> GROUP_GENERAL_OPTIONS = new OptionGroup<Boolean>(
			"GROUP_GENERAL_OPTIONS", OPTIONS_BUNDLE, OVERWRITE_EXISTING_FUNCTION_TERMS);

	/**
	 *
	 */
	@SuppressWarnings({ "unchecked" })
	public static final OptionGroup<?> GROUP_DEFAULT_VALUES = new OptionGroup(
			"GROUP_DEFAULT_VALUES", OPTIONS_BUNDLE, DEFAULT_SIGN, DEFAULT_TERM
	);
}
