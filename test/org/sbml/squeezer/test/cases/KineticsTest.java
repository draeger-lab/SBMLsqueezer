/*
 * $Id: KineticsTest.java 14.03.2012 10:59:29 draeger$
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
package org.sbml.squeezer.test.cases;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.logging.Level;

import javax.swing.JDialog;
import javax.swing.JScrollPane;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.UnitDefinition;
import org.sbml.squeezer.KineticLawGenerator;
import org.sbml.squeezer.ReactionType;
import org.sbml.squeezer.OptionsGeneral;

import de.zbit.sbml.gui.SBMLTree;
import de.zbit.util.logging.LogUtil;
import de.zbit.util.prefs.SBPreferences;

/**
 * Abstract class for classes that test generated kinetic equations and derived units
 * @author Andreas Dr&auml;ger
 * @version $Rev$
 * @since 1.4
 */
public abstract class KineticsTest {
	
	private static int count;
	
	
	/**
	 * Initializes counter and the summary report before the test.
	 */
	@BeforeClass
	public static void init() {
		LogUtil.initializeLogging(Level.FINE, "org.sbml", "de.zbit");
		count = 0;
		System.out.print("Count\tReaction\tDerived units\tRate law\tFormula\n");
		System.out.print("-----\t--------\t-------------\t--------\t-------\n");
	}
	
	/**
	 * The rate law generator.
	 */
	protected KineticLawGenerator klg;
	/**
	 * 
	 */
	protected Model model;
	/**
	 * User settings for the test cases.
	 */
	protected SBPreferences prefs;

	
	/**
	 * The abstract constructor that automatically initializes a {@link Model} and a corresponding
	 * {@link KineticLawGenerator}
	 */
	public KineticsTest() {
		prefs = SBPreferences.getPreferencesFor(OptionsGeneral.class);
		model = initModel();
		try {
			klg = new KineticLawGenerator(model);
			/*
			 * This is necessary because without checking the reaction type, species
			 * on the ignore list are not removed from the reactions within the model.
			 */
			for (Reaction r : klg.getSubmodel().getListOfReactions()) {
				ReactionType rt = new ReactionType(r, klg.isReversibility(),
					klg.isAllReactionsAsEnzymeCatalyzed(), klg.isSetBoundaryCondition(),
					klg.getSpeciesIgnoreList());
				// TODO: Print the identified kinetic law types or do an assertion.
				rt.identifyPossibleKineticLaws();
			}
		} catch (Throwable e) {
			e.printStackTrace();
			fail();
		}
	}

	/**
	 * Creates a model for testing purposes.
	 * 
	 * @return
	 */
	protected abstract Model initModel();
	
	/**
	 * Report the result of a single unit test
	 * @param kl
	 * @throws Throwable
	 */
	public void testUnits(KineticLaw kl) throws Throwable {
		UnitDefinition ud = kl.getDerivedUnitDefinition();
		if (ud != null) {
			System.out.printf("%d\t%s\t\t%s\t%s\t%s\n",
				count++,
				kl.getParent(), UnitDefinition.printUnits(ud, true), 
				kl.getClass().getSimpleName(),
				kl.getMath().toFormula());
			if (!ud.isVariantOfSubstancePerTime()) {
				showTestDialog(kl.getMath());
			}
			assertTrue("Derived UnitDefinition is not of the type SubstancePerTime", ud.isVariantOfSubstancePerTime());
		} else {
			fail("Could not derive a UnitDefinition");
		}
	}
	
	/**
	 * 
	 * @param math
	 */
	private void showTestDialog(ASTNode math) {
		JDialog d = new JDialog();
		d.getContentPane().add(new JScrollPane(new SBMLTree(math)));
		d.setResizable(true);
		d.pack();
		d.setModal(true);
		d.setLocationRelativeTo(null);
		d.setVisible(true);
	}

	/**
	 * Do the complete test, incl. unit checking with the JUnit 4 {@link Assert}.assertEquals method
	 * @param kl
	 * @param formula
	 * @throws Throwable
	 */
	protected void test(Reaction r, KineticLaw kl, String formula) throws Throwable {
		if (r.isSetKineticLaw() && r.getKineticLaw().equals(kl)) {
			fail();
		}
		klg.storeKineticLaw(kl);
		// Check if storing works.
		if (!r.isSetKineticLaw() && !r.getKineticLaw().equals(kl)) {
			fail();
		}
		testUnits(kl);
		assertEquals(formula, kl.getMath().toFormula());
	}
	
	/**
	 * Helper method to gain an overview of generated kinetic equations.
	 * 
	 * @param r the reaction for which a rate law was created.
	 */
	protected void printDetails(Reaction r) {
		KineticLaw kl = r.getKineticLaw();
		System.out.println("Reactants\n=========");
		for (SpeciesReference ref : r.getListOfReactants()) {
			System.out.println(ref.getSpecies() + " in " + UnitDefinition.printUnits(ref.getSpeciesInstance().getDerivedUnitDefinition(), true));
		}
		System.out.println("Products\n=========");
		for (SpeciesReference ref : r.getListOfProducts()) {
			System.out.println(ref.getSpecies() + " in " + UnitDefinition.printUnits(ref.getSpeciesInstance().getDerivedUnitDefinition(), true));
		}
		System.out.println("Parameters\n=========");
		for (int i = 0; i < kl.getListOfLocalParameters().size(); i++) {
			LocalParameter lp = kl.getLocalParameter(i);
			System.out.println(lp.getId() + " in " + UnitDefinition.printUnits(lp.getUnitsInstance(), true));
		}
		System.out.println("\nReaction in " + UnitDefinition.printUnits(kl.getDerivedUnitDefinition(), true));
		System.out.println();
	}
	
	/**
	 * 
	 * @param math
	 */
	protected void showTree(ASTNode math) {
		JDialog d = new JDialog();
		SBMLTree tree = new SBMLTree(math);
		d.setContentPane(new JScrollPane(tree));
		d.pack();
		d.setLocationRelativeTo(null);
		d.setModal(true);
		d.setVisible(true);
	}
	
}
