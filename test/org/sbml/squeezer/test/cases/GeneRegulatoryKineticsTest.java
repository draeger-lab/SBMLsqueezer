/*
 * $Id:  GeneRegulatoryKineticsTest.java 1:23:59 PM snagel$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of SBMLsqueezer, a Java program that creates rate 
 * equations for reactions in SBML files (http://sbml.org).
 *
 * Copyright (C) 2006-2011 by the University of Tuebingen, Germany.
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
import org.sbml.jsbml.SBO;
import org.sbml.jsbml.Species;
import org.sbml.squeezer.UnitConsistencyType;
import org.sbml.squeezer.kinetics.AdditiveModelLinear;
import org.sbml.squeezer.kinetics.AdditiveModelNonLinear;
import org.sbml.squeezer.kinetics.HSystem;
import org.sbml.squeezer.kinetics.HillEquation;
import org.sbml.squeezer.kinetics.HillHinzeEquation;
import org.sbml.squeezer.kinetics.HillRaddeEquation;
import org.sbml.squeezer.kinetics.NetGeneratorLinear;
import org.sbml.squeezer.kinetics.NetGeneratorNonLinear;
import org.sbml.squeezer.kinetics.SSystem;
import org.sbml.squeezer.kinetics.TypeStandardVersion;
import org.sbml.squeezer.kinetics.Vohradsky;
import org.sbml.squeezer.kinetics.Weaver;

/**
 * 
 * @author Sebastian Nagel
 * @version $Rev$
 * @since 1.4
 */
public class GeneRegulatoryKineticsTest extends KineticsTest{
	
	public GeneRegulatoryKineticsTest() {
		super();
	}

	public Model initModel() {
		SBMLDocument doc = new SBMLDocument(2, 4);
		Model model = doc.createModel("m1");
		Compartment c = model.createCompartment("c1");
		Species s1 = model.createSpecies("s1", c);
		s1.setSBOTerm(SBO.getEmptySet());
		Species p1 = model.createSpecies("p1", c);
		p1.setSBOTerm(SBO.getRNA());
		Species e1 = model.createSpecies("e1", c);
		e1.setSBOTerm(SBO.getGene());
		
		Reaction r1 = model.createReaction("r1");
		r1.setReversible(false);
		r1.createReactant(s1);
		r1.createProduct(p1);
		r1.createModifier(e1);
		r1.setSBOTerm(SBO.getTrigger());
		
		return model;
	}
	
	/**
	 * 
	 * @throws Throwable
	 */
	@Test
	public void testAdditiveModelLinearTest() throws Throwable {
		Reaction r1 = model.getReaction("r1");
		KineticLaw kl = klg.createKineticLaw(r1, AdditiveModelLinear.class, true, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
		test(r1, kl, "m_r1*(w_r1_p1*p1+w_r1_e1*e1+b_r1)");
	}
	
	/**
	 * 
	 * @throws Throwable
	 */
	@Test
	public void testAdditiveModelNonLinearTest() throws Throwable {
		Reaction r1 = model.getReaction("r1");
		KineticLaw kl = klg.createKineticLaw(r1, AdditiveModelNonLinear.class, true, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
		test(r1, kl, "m_r1*1/(1+e^(-(w_r1_p1*p1+w_r1_e1*e1+b_r1)))");
	}
	
	/**
	 * 
	 * @throws Throwable
	 */
	@Test
	public void testHillEquation() throws Throwable {
		Reaction r1 = model.getReaction("r1");
		KineticLaw kl = klg.createKineticLaw(r1, HillEquation.class, true, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
		test(r1, kl, "vmaf_r1*s1/ksp_r1*(1-p1/(keq_r1*s1))*(s1/ksp_r1+p1/ksp_r1)^(hic_r1-1)/(1+(s1/ksp_r1+p1/ksp_r1)^(hic_r1))");
	}
	
	/**
	 * 
	 * @throws Throwable
	 */
	@Test
	public void testHillHinzeEquation() throws Throwable {
		Reaction r1 = model.getReaction("r1");
		KineticLaw kl = klg.createKineticLaw(r1, HillHinzeEquation.class, true, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
		test(r1, kl, "vmaf_r1*(e1^(hic_r1_e1)/(e1^(hic_r1_e1)+theta_r1_e1^(hic_r1_e1)))^w_e1_r1*(1-(e1^(hic_r1_e1)/(e1^(hic_r1_e1)+theta_r1_e1^(hic_r1_e1)))^w_e1_r1)");
	}
	
	/**
	 * 
	 * @throws Throwable
	 */
	@Test
	public void testHillRaddeEquation() throws Throwable {
		Reaction r1 = model.getReaction("r1");
		KineticLaw kl = klg.createKineticLaw(r1, HillRaddeEquation.class, true, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
		test(r1, kl, "b_r1+w_r1_e1*e1^(hic_r1_e1)/(e1^(hic_r1_e1)+theta_r1_e1^(hic_r1_e1))");
	}
	
	/**
	 * 
	 * @throws Throwable
	 */
	@Test
	public void testHSystemTest() throws Throwable {
		Reaction r1 = model.getReaction("r1");
		KineticLaw kl = klg.createKineticLaw(r1, HSystem.class, true, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
		test(r1, kl, "b_r1+(w_r1_e1*e1+p1*v_r1_e1*e1)");
	}
	
	/**
	 * 
	 * @throws Throwable
	 */
	@Test
	public void testNetGeneratorLinear() throws Throwable {
		Reaction r1 = model.getReaction("r1");
		KineticLaw kl = klg.createKineticLaw(r1, NetGeneratorLinear.class, true, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
		test(r1, kl, "w_r1_p1*p1+w_r1_e1*e1");
	}
	
	/**
	 * 
	 * @throws Throwable
	 */
	@Test
	public void testNetGeneratorNonLinear() throws Throwable {
		Reaction r1 = model.getReaction("r1");
		KineticLaw kl = klg.createKineticLaw(r1, NetGeneratorNonLinear.class, true, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
		test(r1, kl, "m_r1*1/(1+e^(-(w_r1_p1*p1+w_r1_e1*e1+b_r1)))+w_r1_p1*p1");
	}
	
	/**
	 * 
	 * @throws Throwable
	 */
	@Test
	public void testSSystem() throws Throwable {
		Reaction r1 = model.getReaction("r1");
		KineticLaw kl = klg.createKineticLaw(r1, SSystem.class, true, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
		test(r1, kl, "(alpha_r1-beta_r1*p1^(ssexp_r1_p1))*e1^(ssexp_r1_e1)");
	}
	
	/**
	 * 
	 * @throws Throwable
	 */
	@Test
	public void testVohradsky() throws Throwable {
		Reaction r1 = model.getReaction("r1");
		KineticLaw kl = klg.createKineticLaw(r1, Vohradsky.class, true, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
		test(r1, kl, "m_r1*1/(1+e^(-(w_r1_p1*p1+w_r1_e1*e1+b_r1)))");
	}
	
	/**
	 * 
	 * @throws Throwable
	 */
	@Test
	public void testWeaver() throws Throwable {
		Reaction r1 = model.getReaction("r1");
		KineticLaw kl = klg.createKineticLaw(r1, Weaver.class, true, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
		test(r1, kl, "m_r1*1/(1+e^((-alpha_r1)*(w_r1_p1*p1+w_r1_e1*e1)+beta_r1))");
	}
}
