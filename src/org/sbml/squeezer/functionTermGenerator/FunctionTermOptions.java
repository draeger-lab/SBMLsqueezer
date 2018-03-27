package org.sbml.squeezer.functionTermGenerator;

import java.util.ResourceBundle;

import org.sbml.jsbml.ext.qual.Sign;
import org.sbml.squeezer.util.Bundles;

import de.zbit.util.ResourceManager;
import de.zbit.util.prefs.KeyProvider;
import de.zbit.util.prefs.Option;
/**
 * This is a list of possible command line options and configuration of
 * SBMLsqueezer, concerning the generation of default function terms.
 * 
 * @author Andreas Dräger
 * @author Lisa Falk
 * @since 2.1.1
 * @version $Rev$
 */

public interface FunctionTermOptions extends KeyProvider{

	public static final ResourceBundle OPTIONS_BUNDLE = ResourceManager.getBundle(Bundles.OPTIONS);

	public static final Option<Sign> DEFAULT_SIGN = new Option<Sign>("DEFAULT_SIGN", Sign.class, OPTIONS_BUNDLE, Sign.positive);
	
	public static final Option<DefaultTerm> DEFAULT_TERM = new Option<DefaultTerm>("DEFAULT_TERM", DefaultTerm.class, OPTIONS_BUNDLE, DefaultTerm.none);
}
