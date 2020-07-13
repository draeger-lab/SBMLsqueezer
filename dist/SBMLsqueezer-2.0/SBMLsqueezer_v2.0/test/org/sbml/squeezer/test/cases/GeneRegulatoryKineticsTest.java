/*
 * $Id:  GeneRegulatoryKineticsTest.java 1:23:59 PM snagel$
 * $URL: https://rarepos.cs.uni-tuebingen.de/svn-path/SBMLsqueezer/trunk/test/org/sbml/squeezer/test/cases/GeneRegulatoryKineticsTest.java $
 * ---------------------------------------------------------------------
 * This file is part of SBMLsqueezer, a Java program that creates rate
 * equations for reactions in SBML files (http://sbml.org).
 *
 * Copyright (C) 2006-2018  by the University of Tuebingen, Germany.
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
 *
 * @since 2.0
 */
public class GeneRegulatoryKineticsTest extends KineticsTest{
  
  public GeneRegulatoryKineticsTest() {
    super();
  }
  
  /* (non-Javadoc)
   * @see org.sbml.squeezer.test.cases.KineticsTest#initModel()
   */
  @Override
  public Model initModel() {
    SBMLDocument doc = new SBMLDocument(3, 1);
    Model model = doc.createModel("m1");
    Compartment c = model.createCompartment("c1");
    Species emptySet = model.createSpecies("s1", c);
    emptySet.setSBOTerm(SBO.getEmptySet());
    Species rna = model.createSpecies("p1", c);
    rna.setSBOTerm(SBO.getRNA());
    Species gene = model.createSpecies("e1", c);
    gene.setSBOTerm(SBO.getGene());
    
    for (Species s : model.getListOfSpecies()) {
      s.setValue(1d);
      s.setHasOnlySubstanceUnits(true);
    }
    
    Reaction r1 = model.createReaction("r1");
    r1.setReversible(true);
    r1.createReactant(emptySet).setStoichiometry(1d);
    r1.createProduct(rna).setStoichiometry(1d);
    r1.createModifier(gene).setSBOTerm(SBO.getTrigger());
    
    Reaction r2 = model.createReaction("r2");
    r2.setReversible(false);
    r2.createReactant(emptySet).setStoichiometry(1d);
    r2.createProduct(rna).setStoichiometry(1d);
    r2.createModifier(gene).setSBOTerm(SBO.getTrigger());
    
    return model;
  }
  
  /**
   * 
   * @throws Throwable
   */
  @Test
  public void testAdditiveModelLinearRevAmount() throws Throwable {
    Reaction r1 = model.getReaction("r1");
    KineticLaw kl = klg.createKineticLaw(r1, AdditiveModelLinear.class, true, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
    test(r1, kl, "m_r1*(w_r1_p1*p1+w_r1_e1*e1+b_r1)");
  }
  
  /**
   * 
   * @throws Throwable
   */
  @Test
  public void testAdditiveModelLinearRevConcentration() throws Throwable {
    Reaction r1 = model.getReaction("r1");
    
    KineticLaw kl = klg.createKineticLaw(r1, AdditiveModelLinear.class, true, TypeStandardVersion.cat, UnitConsistencyType.concentration, 1d);
    
    test(r1, kl, "m_r1*(w_r1_p1*p1/c1+w_r1_e1*e1/c1+b_r1)");
  }
  /**
   * 
   * @throws Throwable
   */
  @Test
  public void testAdditiveModelLinearIrrevAmount() throws Throwable {
    Reaction r1 = model.getReaction("r2");
    KineticLaw kl = klg.createKineticLaw(r1, AdditiveModelLinear.class, false, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
    test(r1, kl, "m_r2*(w_r2_p1*p1+w_r2_e1*e1+b_r2)");
  }
  
  /**
   * 
   * @throws Throwable
   */
  @Test
  public void testAdditiveModelLinearIrrevConcentration() throws Throwable {
    Reaction r1 = model.getReaction("r2");
    KineticLaw kl = klg.createKineticLaw(r1, AdditiveModelLinear.class, false, TypeStandardVersion.cat, UnitConsistencyType.concentration, 1d);
    test(r1, kl, "m_r2*(w_r2_p1*p1/c1+w_r2_e1*e1/c1+b_r2)");
  }
  
  /**
   * 
   * @throws Throwable
   */
  @Test
  public void testAdditiveModelNonLinearRevAmount() throws Throwable {
    Reaction r1 = model.getReaction("r1");
    KineticLaw kl = klg.createKineticLaw(r1, AdditiveModelNonLinear.class, true, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
    test(r1, kl, "m_r1*1/(1+e^(-(w_r1_p1*p1+w_r1_e1*e1+b_r1)))");
  }
  
  /**
   * 
   * @throws Throwable
   */
  @Test
  public void testAdditiveModelNonLinearRevConcentration() throws Throwable {
    Reaction r1 = model.getReaction("r1");
    KineticLaw kl = klg.createKineticLaw(r1, AdditiveModelNonLinear.class, true, TypeStandardVersion.cat, UnitConsistencyType.concentration, 1d);
    test(r1, kl, "m_r1*1/(1+e^(-(w_r1_p1*p1/c1+w_r1_e1*e1/c1+b_r1)))");
  }
  /**
   * 
   * @throws Throwable
   */
  @Test
  public void testAdditiveModelNonLinearIrrevAmount() throws Throwable {
    Reaction r1 = model.getReaction("r2");
    KineticLaw kl = klg.createKineticLaw(r1, AdditiveModelNonLinear.class, false, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
    test(r1, kl, "m_r2*1/(1+e^(-(w_r2_p1*p1+w_r2_e1*e1+b_r2)))");
  }
  
  /**
   * 
   * @throws Throwable
   */
  @Test
  public void testAdditiveModelNonLinearIrrevConcentration() throws Throwable {
    Reaction r1 = model.getReaction("r2");
    KineticLaw kl = klg.createKineticLaw(r1, AdditiveModelNonLinear.class, false, TypeStandardVersion.cat, UnitConsistencyType.concentration, 1d);
    test(r1, kl, "m_r2*1/(1+e^(-(w_r2_p1*p1/c1+w_r2_e1*e1/c1+b_r2)))");
  }
  
  /**
   * 
   * @throws Throwable
   */
  @Test
  public void testHillEquationRevAmount() throws Throwable {
    Reaction r1 = model.getReaction("r1");
    KineticLaw kl = klg.createKineticLaw(r1, HillEquation.class, true, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
    test(r1, kl, "vmaf_r1*s1/ksp_r1*(1-p1/(keq_r1*s1))*(s1/ksp_r1+p1/ksp_r1)^(hic_r1-1)/((kmc_r1_e1^hic_r1+e1^hic_r1)/(kmc_r1_e1^hic_r1+beta_r1*e1^hic_r1)+(s1/ksp_r1+p1/ksp_r1)^hic_r1)");
  }
  
  /**
   * 
   * @throws Throwable
   */
  @Test
  public void testHillEquationRevConcentration() throws Throwable {
    Reaction r1 = model.getReaction("r1");
    KineticLaw kl = klg.createKineticLaw(r1, HillEquation.class, true, TypeStandardVersion.cat, UnitConsistencyType.concentration, 1d);
    test(r1, kl, "vmaf_r1*s1/c1/ksp_r1*(1-p1/c1/(keq_r1*s1/c1))*(s1/c1/ksp_r1+p1/c1/ksp_r1)^(hic_r1-1)/((kmc_r1_e1^hic_r1+(e1/c1)^hic_r1)/(kmc_r1_e1^hic_r1+beta_r1*(e1/c1)^hic_r1)+(s1/c1/ksp_r1+p1/c1/ksp_r1)^hic_r1)");
  }
  
  /**
   * 
   * @throws Throwable
   */
  @Test
  public void testHillEquationIrrevAmount() throws Throwable {
    Reaction r1 = model.getReaction("r2");
    KineticLaw kl = klg.createKineticLaw(r1, HillEquation.class, false, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
    test(r1, kl, "vmax_r2*s1^hic_r2/((kmc_r2_e1^hic_r2+e1^hic_r2)/(kmc_r2_e1^hic_r2+beta_r2*e1^hic_r2)*ksp_r2^hic_r2+s1^hic_r2)");
  }
  
  /**
   * 
   * @throws Throwable
   */
  @Test
  public void testHillEquationIrrevConcentration() throws Throwable {
    Reaction r1 = model.getReaction("r2");
    KineticLaw kl = klg.createKineticLaw(r1, HillEquation.class, false, TypeStandardVersion.cat, UnitConsistencyType.concentration, 1d);
    test(r1, kl, "vmax_r2*(s1/c1)^hic_r2/((kmc_r2_e1^hic_r2+(e1/c1)^hic_r2)/(kmc_r2_e1^hic_r2+beta_r2*(e1/c1)^hic_r2)*ksp_r2^hic_r2+(s1/c1)^hic_r2)");
  }
  
  /**
   * 
   * @throws Throwable
   */
  @Test
  public void testHillHinzeEquationRevAmount() throws Throwable {
    Reaction r1 = model.getReaction("r1");
    KineticLaw kl = klg.createKineticLaw(r1, HillHinzeEquation.class, true, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
    test(r1, kl, "vmaf_r1*e1^hic_r1_e1/(e1^hic_r1_e1+ksp_r1_e1^hic_r1_e1)");
  }
  
  /**
   * 
   * @throws Throwable
   */
  @Test
  public void testHillHinzeEquationRevConcentration() throws Throwable {
    Reaction r1 = model.getReaction("r1");
    KineticLaw kl = klg.createKineticLaw(r1, HillHinzeEquation.class, true, TypeStandardVersion.cat, UnitConsistencyType.concentration, 1d);
    test(r1, kl, "vmaf_r1*(e1/c1)^hic_r1_e1/((e1/c1)^hic_r1_e1+ksp_r1_e1^hic_r1_e1)");
  }
  
  /**
   * 
   * @throws Throwable
   */
  @Test
  public void testHillHinzeEquationIrrevAmount() throws Throwable {
    Reaction r1 = model.getReaction("r2");
    KineticLaw kl = klg.createKineticLaw(r1, HillHinzeEquation.class, false, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
    test(r1, kl, "vmax_r2*e1^hic_r2_e1/(e1^hic_r2_e1+ksp_r2_e1^hic_r2_e1)");
  }
  
  /**
   * 
   * @throws Throwable
   */
  @Test
  public void testHillHinzeEquationIrrevConcentration() throws Throwable {
    Reaction r1 = model.getReaction("r2");
    KineticLaw kl = klg.createKineticLaw(r1, HillHinzeEquation.class, false, TypeStandardVersion.cat, UnitConsistencyType.concentration, 1d);
    test(r1, kl, "vmax_r2*(e1/c1)^hic_r2_e1/((e1/c1)^hic_r2_e1+ksp_r2_e1^hic_r2_e1)");
  }
  
  /**
   * 
   * @throws Throwable
   */
  @Test
  public void testHillRaddeEquationRevAmount() throws Throwable {
    Reaction r1 = model.getReaction("r1");
    KineticLaw kl = klg.createKineticLaw(r1, HillRaddeEquation.class, true, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
    test(r1, kl, "b_r1+w_r1_e1*e1^hic_r1_e1/(e1^hic_r1_e1+theta_r1_e1^hic_r1_e1)");
  }
  
  /**
   * 
   * @throws Throwable
   */
  @Test
  public void testHillRaddeEquationRevConcentration() throws Throwable {
    Reaction r1 = model.getReaction("r1");
    KineticLaw kl = klg.createKineticLaw(r1, HillRaddeEquation.class, true, TypeStandardVersion.cat, UnitConsistencyType.concentration, 1d);
    test(r1, kl, "b_r1+w_r1_e1*(e1/c1)^hic_r1_e1/((e1/c1)^hic_r1_e1+theta_r1_e1^hic_r1_e1)");
  }
  
  /**
   * 
   * @throws Throwable
   */
  @Test
  public void testHillRaddeEquationIrrevAmount() throws Throwable {
    Reaction r1 = model.getReaction("r2");
    KineticLaw kl = klg.createKineticLaw(r1, HillRaddeEquation.class, false, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
    test(r1, kl, "b_r2+w_r2_e1*e1^hic_r2_e1/(e1^hic_r2_e1+theta_r2_e1^hic_r2_e1)");
  }
  
  /**
   * 
   * @throws Throwable
   */
  @Test
  public void testHillRaddeEquationIrrevConcentration() throws Throwable {
    Reaction r1 = model.getReaction("r2");
    KineticLaw kl = klg.createKineticLaw(r1, HillRaddeEquation.class, false, TypeStandardVersion.cat, UnitConsistencyType.concentration, 1d);
    test(r1, kl, "b_r2+w_r2_e1*(e1/c1)^hic_r2_e1/((e1/c1)^hic_r2_e1+theta_r2_e1^hic_r2_e1)");
  }
  
  /**
   * 
   * @throws Throwable
   */
  @Test
  public void testHSystemRevAmount() throws Throwable {
    Reaction r1 = model.getReaction("r1");
    KineticLaw kl = klg.createKineticLaw(r1, HSystem.class, true, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
    test(r1, kl, "b_r1+(w_r1_e1*e1+p1*v_r1_e1*e1)");
  }
  
  /**
   * 
   * @throws Throwable
   */
  @Test
  public void testHSystemRevConcentration() throws Throwable {
    Reaction r1 = model.getReaction("r1");
    KineticLaw kl = klg.createKineticLaw(r1, HSystem.class, true, TypeStandardVersion.cat, UnitConsistencyType.concentration, 1d);
    test(r1, kl, "b_r1+(w_r1_e1*e1/c1+p1/c1*v_r1_e1*e1/c1)");
  }
  
  /**
   * 
   * @throws Throwable
   */
  @Test
  public void testHSystemIrrevAmount() throws Throwable {
    Reaction r1 = model.getReaction("r2");
    KineticLaw kl = klg.createKineticLaw(r1, HSystem.class, false, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
    test(r1, kl, "b_r2+(w_r2_e1*e1+p1*v_r2_e1*e1)");
  }
  
  /**
   * 
   * @throws Throwable
   */
  @Test
  public void testHSystemIrrevConcentration() throws Throwable {
    Reaction r1 = model.getReaction("r2");
    KineticLaw kl = klg.createKineticLaw(r1, HSystem.class, false, TypeStandardVersion.cat, UnitConsistencyType.concentration, 1d);
    test(r1, kl, "b_r2+(w_r2_e1*e1/c1+p1/c1*v_r2_e1*e1/c1)");
  }
  
  
  
  
  /**
   * 
   * @throws Throwable
   */
  @Test
  public void testNetGeneratorLinearRevAmount() throws Throwable {
    Reaction r1 = model.getReaction("r1");
    KineticLaw kl = klg.createKineticLaw(r1, NetGeneratorLinear.class, true, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
    test(r1, kl, "1*(w_r1_p1*p1+w_r1_e1*e1)");
  }
  
  /**
   * 
   * @throws Throwable
   */
  @Test
  public void testNetGeneratorLinearRevConcentration() throws Throwable {
    Reaction r1 = model.getReaction("r1");
    KineticLaw kl = klg.createKineticLaw(r1, NetGeneratorLinear.class, true, TypeStandardVersion.cat, UnitConsistencyType.concentration, 1d);
    test(r1, kl, "1*(w_r1_p1*p1/c1+w_r1_e1*e1/c1)");
  }
  
  /**
   * 
   * @throws Throwable
   */
  @Test
  public void testNetGeneratorLinearIrrevAmount() throws Throwable {
    Reaction r1 = model.getReaction("r2");
    KineticLaw kl = klg.createKineticLaw(r1, NetGeneratorLinear.class, false, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
    test(r1, kl, "1*(w_r2_p1*p1+w_r2_e1*e1)");
  }
  
  /**
   * 
   * @throws Throwable
   */
  @Test
  public void testNetGeneratorLinearIrrevConcentration() throws Throwable {
    Reaction r1 = model.getReaction("r2");
    KineticLaw kl = klg.createKineticLaw(r1, NetGeneratorLinear.class, false, TypeStandardVersion.cat, UnitConsistencyType.concentration, 1d);
    test(r1, kl, "1*(w_r2_p1*p1/c1+w_r2_e1*e1/c1)");
  }
  
  /**
   * 
   * @throws Throwable
   */
  @Test
  public void testNetGeneratorNonLinearRevAmount() throws Throwable {
    Reaction r1 = model.getReaction("r1");
    KineticLaw kl = klg.createKineticLaw(r1, NetGeneratorNonLinear.class, true, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
    test(r1, kl, "m_r1*1/(1+e^(-(w_r1_p1*p1+w_r1_e1*e1+b_r1)))+1*w_r1_p1*p1");
  }
  
  /**
   * 
   * @throws Throwable
   */
  @Test
  public void testNetGeneratorNonLinearRevConcentration() throws Throwable {
    Reaction r1 = model.getReaction("r1");
    KineticLaw kl = klg.createKineticLaw(r1, NetGeneratorNonLinear.class, true, TypeStandardVersion.cat, UnitConsistencyType.concentration, 1d);
    test(r1, kl, "m_r1*1/(1+e^(-(w_r1_p1*p1/c1+w_r1_e1*e1/c1+b_r1)))+1*w_r1_p1*p1/c1");
  }
  
  /**
   * 
   * @throws Throwable
   */
  @Test
  public void testNetGeneratorNonLinearIrrevAmount() throws Throwable {
    Reaction r1 = model.getReaction("r2");
    KineticLaw kl = klg.createKineticLaw(r1, NetGeneratorNonLinear.class, false, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
    test(r1, kl, "m_r2*1/(1+e^(-(w_r2_p1*p1+w_r2_e1*e1+b_r2)))+1*w_r2_p1*p1");
  }
  
  /**
   * 
   * @throws Throwable
   */
  @Test
  public void testNetGeneratorNonLinearIrrevConcentration() throws Throwable {
    Reaction r1 = model.getReaction("r2");
    KineticLaw kl = klg.createKineticLaw(r1, NetGeneratorNonLinear.class, false, TypeStandardVersion.cat, UnitConsistencyType.concentration, 1d);
    System.out.println(kl.getMath().toFormula());
    test(r1, kl, "m_r2*1/(1+e^(-(w_r2_p1*p1/c1+w_r2_e1*e1/c1+b_r2)))+1*w_r2_p1*p1/c1");
  }
  
  /**
   * 
   * @throws Throwable
   */
  @Test
  public void testSSystemRevAmount() throws Throwable {
    Reaction r1 = model.getReaction("r1");
    KineticLaw kl = klg.createKineticLaw(r1, SSystem.class, true, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
    test(r1, kl, "(alpha_r1-beta_r1*p1^ssexp_r1_p1)*e1^ssexp_r1_e1");
  }
  
  /**
   * 
   * @throws Throwable
   */
  @Test
  public void testSSystemRevConcentration() throws Throwable {
    Reaction r1 = model.getReaction("r1");
    KineticLaw kl = klg.createKineticLaw(r1, SSystem.class, true, TypeStandardVersion.cat, UnitConsistencyType.concentration, 1d);
    test(r1, kl, "(alpha_r1-beta_r1*(p1/c1)^ssexp_r1_p1)*(e1/c1)^ssexp_r1_e1");
  }
  
  /**
   * 
   * @throws Throwable
   */
  @Test
  public void testSSystemIrrevAmount() throws Throwable {
    Reaction r1 = model.getReaction("r2");
    KineticLaw kl = klg.createKineticLaw(r1, SSystem.class, false, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
    test(r1, kl, "alpha_r2*e1^ssexp_r2_e1");
  }
  
  /**
   * 
   * @throws Throwable
   */
  @Test
  public void testSSystemIrrevConcentration() throws Throwable {
    Reaction r1 = model.getReaction("r2");
    KineticLaw kl = klg.createKineticLaw(r1, SSystem.class, false, TypeStandardVersion.cat, UnitConsistencyType.concentration, 1d);
    test(r1, kl, "alpha_r2*(e1/c1)^ssexp_r2_e1");
  }
  
  
  
  /**
   * 
   * @throws Throwable
   */
  @Test
  public void testVohradskyRevAmount() throws Throwable {
    Reaction r1 = model.getReaction("r1");
    KineticLaw kl = klg.createKineticLaw(r1, Vohradsky.class, true, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
    test(r1, kl, "m_r1*1/(1+e^(-(w_r1_p1*p1+w_r1_e1*e1+b_r1)))");
  }
  
  /**
   * 
   * @throws Throwable
   */
  @Test
  public void testVohradskyRevConcentration() throws Throwable {
    Reaction r1 = model.getReaction("r1");
    KineticLaw kl = klg.createKineticLaw(r1, Vohradsky.class, true, TypeStandardVersion.cat, UnitConsistencyType.concentration, 1d);
    test(r1, kl, "m_r1*1/(1+e^(-(w_r1_p1*p1/c1+w_r1_e1*e1/c1+b_r1)))");
  }
  
  /**
   * 
   * @throws Throwable
   */
  @Test
  public void testVohradskyIrrevAmount() throws Throwable {
    Reaction r1 = model.getReaction("r2");
    KineticLaw kl = klg.createKineticLaw(r1, Vohradsky.class, false, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
    test(r1, kl, "m_r2*1/(1+e^(-(w_r2_p1*p1+w_r2_e1*e1+b_r2)))");
  }
  
  /**
   * 
   * @throws Throwable
   */
  @Test
  public void testVohradskyIrrevConcentration() throws Throwable {
    Reaction r1 = model.getReaction("r2");
    KineticLaw kl = klg.createKineticLaw(r1, Vohradsky.class, false, TypeStandardVersion.cat, UnitConsistencyType.concentration, 1d);
    test(r1, kl, "m_r2*1/(1+e^(-(w_r2_p1*p1/c1+w_r2_e1*e1/c1+b_r2)))");
  }
  
  /**
   * 
   * @throws Throwable
   */
  @Test
  public void testWeaverRevAmount() throws Throwable {
    Reaction r1 = model.getReaction("r1");
    KineticLaw kl = klg.createKineticLaw(r1, Weaver.class, true, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
    test(r1, kl, "m_r1*1/(1+e^((-alpha_r1)*(w_r1_p1*p1+w_r1_e1*e1)+beta_r1))");
  }
  
  /**
   * 
   * @throws Throwable
   */
  @Test
  public void testWeaverRevConcentration() throws Throwable {
    Reaction r1 = model.getReaction("r1");
    KineticLaw kl = klg.createKineticLaw(r1, Weaver.class, true, TypeStandardVersion.cat, UnitConsistencyType.concentration, 1d);
    test(r1, kl, "m_r1*1/(1+e^((-alpha_r1)*(w_r1_p1*p1/c1+w_r1_e1*e1/c1)+beta_r1))");
  }
  
  /**
   * 
   * @throws Throwable
   */
  @Test
  public void testWeaverIrrevAmount() throws Throwable {
    Reaction r1 = model.getReaction("r2");
    KineticLaw kl = klg.createKineticLaw(r1, Weaver.class, false, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
    test(r1, kl, "m_r2*1/(1+e^((-alpha_r2)*(w_r2_p1*p1+w_r2_e1*e1)+beta_r2))");
  }
  
  /**
   * 
   * @throws Throwable
   */
  @Test
  public void testWeaverIrrevConcentration() throws Throwable {
    Reaction r1 = model.getReaction("r2");
    KineticLaw kl = klg.createKineticLaw(r1, Weaver.class, false, TypeStandardVersion.cat, UnitConsistencyType.concentration, 1d);
    test(r1, kl, "m_r2*1/(1+e^((-alpha_r2)*(w_r2_p1*p1/c1+w_r2_e1*e1/c1)+beta_r2))");
  }
  
}
