package org.sbml.squeezer.functionTermGenerator;

import de.zbit.util.ResourceManager;
import org.sbml.squeezer.util.Bundles;

import java.lang.reflect.Field;
import java.util.ResourceBundle;

/**
 * @author Andreas Dr&auml;ger
 * @author Lisa Falk
 * @author Eike Pertuch
 *
 * @since 2.1.1
 */
public enum DefaultTerm {
    /**
     *
     */
    oneActivatorAndNoInhibitor(ResourceManager.getBundle(Bundles.OPTIONS).getString("ONE_ACTI")),
    /**
     *
     */
    allActivatorsAndNoInhibitor(ResourceManager.getBundle(Bundles.OPTIONS).getString("ALL_ACTI")),
    /**
     *
     */
    none(ResourceManager.getBundle(Bundles.OPTIONS).getString("NONE"));


    DefaultTerm(String name) {
        try {
            Field fieldName = getClass().getSuperclass().getDeclaredField("name");
            fieldName.setAccessible(true);
            fieldName.set(this, name);
            fieldName.setAccessible(false);
        } catch (Exception e) {
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
