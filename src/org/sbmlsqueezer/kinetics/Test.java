package org.sbmlsqueezer.kinetics;

import java.io.IOException;

import jp.sbi.celldesigner.plugin.PluginCompartment;
import jp.sbi.celldesigner.plugin.PluginModel;
import jp.sbi.celldesigner.plugin.PluginModifierSpeciesReference;
import jp.sbi.celldesigner.plugin.PluginReaction;
import jp.sbi.celldesigner.plugin.PluginSpecies;
import jp.sbi.celldesigner.plugin.PluginSpeciesAlias;
import jp.sbi.celldesigner.plugin.PluginSpeciesReference;

import org.sbmlsqueezer.io.LaTeXExport;


/**
 * 
 */

/**
 * @author wouamba
 *
 */
public class Test {

	/**
	 * @param args
	 * @throws IllegalFormatException 
	 */
	public static void main(String[] args) throws IllegalFormatException {
		PluginModel model = new PluginModel("test");
		model.addCompartment(new PluginCompartment("default"));
		model.addSpecies(new PluginSpecies("SIMPLE_MOLECULE", "s1"));
		model.addSpecies(new PluginSpecies("SIMPLE_MOLECULE", "s2"));
		model.addSpecies(new PluginSpecies("SIMPLE_MOLECULE", "p1"));
		model.addSpecies(new PluginSpecies("SIMPLE_MOLECULE", "p2"));
		model.addSpecies(new PluginSpecies("PROTEIN", "e1"));
		model.addReaction(new PluginReaction());
		PluginReaction r = model.getReaction(0);
		r.addReactant(new PluginSpeciesReference(r, new PluginSpeciesAlias(model.getSpecies("s1"), "SIMPLE_MOLECULE")));
		r.addReactant(new PluginSpeciesReference(r, new PluginSpeciesAlias(model.getSpecies("s2"), "SIMPLE_MOLECULE")));
		r.addProduct(new PluginSpeciesReference(r, new PluginSpeciesAlias(model.getSpecies("p1"), "SIMPLE_MOLECULE")));
		r.addProduct(new PluginSpeciesReference(r, new PluginSpeciesAlias(model.getSpecies("p2"), "SIMPLE_MOLECULE")));
		r.addModifier(new PluginModifierSpeciesReference(r, new PluginSpeciesAlias(model.getSpecies("e1"), "CATALYSIS")));
		try {
			r.setKineticLaw(new RandomOrderMechanism(r, model, true));
			LaTeXExport l = new LaTeXExport();
			System.out.println(l.toLaTeX(model, r.getKineticLaw().getMath()));
		} catch (RateLawNotApplicableException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("bla");
	}

}
