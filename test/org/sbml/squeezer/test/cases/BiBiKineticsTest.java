/*
 * $Id:  BiBiKineticsTest.java 3:02:18 PM jpfeuffer$
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

import org.junit.Test;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.Species;
import org.sbml.squeezer.kinetics.CommonModularRateLaw;
import org.sbml.squeezer.kinetics.ForceDependentModularRateLaw;
import org.sbml.squeezer.kinetics.PowerLawModularRateLaw;
import org.sbml.squeezer.kinetics.SimultaneousBindingModularRateLaw;

/**
 * @author Julianus Pfeuffer
 * @version $Rev$
 * @since 1.4
 */
public class BiBiKineticsTest extends KineticsTest {

	/**
	 * 
	 */
	private Reaction r1;
	
	/* (non-Javadoc)
	 * @see org.sbml.squeezer.test.cases.KineticsTest#initModel()
	 */
	public Model initModel() {
		SBMLDocument doc = new SBMLDocument(2, 4);
		Model model = doc.createModel("bibi_model");
		Compartment c = model.createCompartment("c1");
		Species s1 = model.createSpecies("s1", c);
		Species s2 = model.createSpecies("s2", c);
		Species p1 = model.createSpecies("p1", c);
		Species p2 = model.createSpecies("p2", c);
		Species e1 = model.createSpecies("e1", c);
		
		r1 = model.createReaction("r1");
		r1.setReversible(false);
		r1.createReactant(s1);
		r1.createReactant(s2);
		r1.createProduct(p1);
		r1.createProduct(p2);
		r1.createModifier(e1);
		return model;
	}
	
	/**
	 * 
	 * @throws Throwable
	 */
	@Test
	public void testCommonModularRateLaw() throws Throwable{
		KineticLaw kl1 = klg.createKineticLaw(r1, CommonModularRateLaw.class, false);
		test(kl1, "vmax_r1*(s1*c1/kmc_r1_s1)^(hco_r1)*(s2*c1/kmc_r1_s2)^(hco_r1)/((1+s1*c1/kmc_r1_s1)^(hco_r1)*(1+s2*c1/kmc_r1_s2)^(hco_r1)+(1+p1*c1/kmc_r1_p1)^(hco_r1)*(1+p2*c1/kmc_r1_p2)^(hco_r1)-1)");
	}
	
	/**
	 * 
	 * @throws Throwable
	 */
	@Test
	public void testPowerLawModularRateLaw() throws Throwable{
		KineticLaw kl = klg.createKineticLaw(r1, PowerLawModularRateLaw.class, false);
		test(kl, "vmax_r1*(s1*c1/kmc_r1_s1)^(hco_r1)*(s2*c1/kmc_r1_s2)^(hco_r1)");
	}
	
	/**
	 * 
	 * @throws Throwable
	 */
	@Test
	public void testSimultaneousBindingModularRateLaw() throws Throwable{
		KineticLaw kl = klg.createKineticLaw(r1, SimultaneousBindingModularRateLaw.class, false);
		test(kl, "vmax_r1*(s1*c1/kmc_r1_s1)^(hco_r1)*(s2*c1/kmc_r1_s2)^(hco_r1)/((1+s1*c1/kmc_r1_s1)^(hco_r1)*(1+s2*c1/kmc_r1_s2)^(hco_r1)*(1+p1*c1/kmc_r1_p1)^(hco_r1)*(1+p2*c1/kmc_r1_p2)^(hco_r1))");
	}
	
	/**
	 * 
	 * @throws Throwable
	 */
	@Test
	public void testForceDependentModularRateLaw() throws Throwable{
		KineticLaw kl = klg.createKineticLaw(r1, ForceDependentModularRateLaw.class, false);
		test(kl, "vmax_r1*(s1*c1/kmc_r1_s1)^(hco_r1)*(s2*c1/kmc_r1_s2)^(hco_r1)/((s1*c1/kmc_r1_s1)^(hco_r1)*(s2*c1/kmc_r1_s2)^(hco_r1)*(p1*c1/kmc_r1_p1)^(hco_r1)*(p2*c1/kmc_r1_p2)^(hco_r1))^(0.5)");
	}
	
	/**
	 * 
	 * @throws Throwable
	 */
	@Test
	public void testCommonModularRateLawRev() throws Throwable{
		KineticLaw kl2 = klg.createKineticLaw(r1, CommonModularRateLaw.class, true);
		test(kl2, "(vmaf_r1*(s1*c1/kmc_r1_s1)^(hco_r1)*(s2*c1/kmc_r1_s2)^(hco_r1)-vmar_r1*(p1*c1/kmc_r1_p1)^(hco_r1)*(p2*c1/kmc_r1_p2)^(hco_r1))/((1+s1*c1/kmc_r1_s1)^(hco_r1)*(1+s2*c1/kmc_r1_s2)^(hco_r1)+(1+p1*c1/kmc_r1_p1)^(hco_r1)*(1+p2*c1/kmc_r1_p2)^(hco_r1)-1)");
	}

}
