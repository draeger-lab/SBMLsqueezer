package org.sbml.squeezer.functionTermGenerator;

import de.zbit.util.ResourceManager;
import org.sbml.squeezer.util.Bundles;

import java.util.ResourceBundle;

/**
 * @author Andreas Dr&auml;ger
 * @author Lisa Falk
 * @since 2.1.1
 */
public enum DefaultTerm {
    /**
     *
     */
    oneActivatorAndNoInhibitor,
    /**
     *
     */
    allActivatorsAndNoInhibitor,
    /**
     *
     */
    none;

    /**
     * Gets the simple Name of default term
     *
     * @return simple name
     */
    public String getSimpleName() {

        ResourceBundle OPTIONS = ResourceManager.getBundle(Bundles.OPTIONS);

        switch (this) {
            case allActivatorsAndNoInhibitor:
                return OPTIONS.getString("ALL_ACTI");
            case oneActivatorAndNoInhibitor:
                return OPTIONS.getString("ONE_ACTI");
            default:
                return OPTIONS.getString("NONE");
        }
    }

    public static DefaultTerm getDefaultTermFromSimpleName(String simpleName) {

        ResourceBundle OPTIONS = ResourceManager.getBundle(Bundles.OPTIONS);

        if(simpleName.equals(OPTIONS.getString("ALL_ACTI"))) {
            return allActivatorsAndNoInhibitor;
        }
        else if(simpleName.equals(OPTIONS.getString("ONE_ACTI"))) {
            return oneActivatorAndNoInhibitor;
        }
        else {
            return none;
        }
    }
}
