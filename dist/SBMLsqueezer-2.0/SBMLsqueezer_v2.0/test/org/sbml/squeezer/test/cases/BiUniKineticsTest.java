/*
 * $Id:  BiUniKinetics.java 3:20:45 PM jpfeuffer$
 * $URL: https://rarepos.cs.uni-tuebingen.de/svn-path/SBMLsqueezer/trunk/test/org/sbml/squeezer/test/cases/BiUniKineticsTest.java $
 * ---------------------------------------------------------------------
 * This file is part of SBMLsqueezer, a Java program that creates rate
 * equations for reactions in SBML files (http://sbml.org).
 *
 * Copyright (C) 2006-2014 by the University of Tuebingen, Germany.
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
import org.sbml.squeezer.UnitConsistencyType;
import org.sbml.squeezer.kinetics.CommonModularRateLaw;
import org.sbml.squeezer.kinetics.DirectBindingModularRateLaw;
import org.sbml.squeezer.kinetics.ForceDependentModularRateLaw;
import org.sbml.squeezer.kinetics.OrderedMechanism;
import org.sbml.squeezer.kinetics.PowerLawModularRateLaw;
import org.sbml.squeezer.kinetics.SimultaneousBindingModularRateLaw;
import org.sbml.squeezer.kinetics.TypeStandardVersion;

/**
 * Class that tests the generated equations and derived units for reactions of the form
 * A + B (<)-> C and 2A (<)-> C.
 * @author Julianus Pfeuffer
 * @version $Rev: 1082 $
 * @since 2.0
 */
public class BiUniKineticsTest extends KineticsTest {
  
  /**
   * 
   */
  private Reaction r1, r2;
  
  /**
   * Initialization of a model and basic reactions for this test class.
   * @throws Throwable
   */
  @Override
  protected Model initModel() {
    
    /**
     * Initialization
     */
    SBMLDocument doc = new SBMLDocument(3, 1);
    model = doc.createModel("biuni_model");
    Compartment c = model.createCompartment("c1");
    Species s1 = model.createSpecies("s1", c);
    Species s2 = model.createSpecies("s2", c);
    Species p1 = model.createSpecies("p1", c);
    
    for (Species s : model.getListOfSpecies()) {
      s.setHasOnlySubstanceUnits(false);
    }
    
    /**
     * Reaction with two substrates and one product. No modifiers (later added).
     */
    r1 = model.createReaction("r1");
    r1.setReversible(false);
    r1.createReactant(s1).setStoichiometry(1d);
    r1.createReactant(s2).setStoichiometry(1d);
    r1.createProduct(p1).setStoichiometry(1d);
    
    r2 = model.createReaction("r2");
    r2.createReactant(s1).setStoichiometry(2d);
    r2.createProduct(p1).setStoichiometry(1d);
    r2.setReversible(false);
    
    return model;
  }
  
  /*========================================
	               A + B (<)-> C
	  ========================================*/
  
  /*==================================================
                 UnitConsistencyType = A M O U N T
	  ==================================================*/
  
  /*=========================================================*
   *      I R R E V E R S I B L E   R E A C T I O N S        *
   *=========================================================*/
  
  /**
   * Test for the {@link OrderedMechanism} for A + B -> C
   * @throws Throwable
   */
  @Test
  public void testOrderedMechAmountIrrev() throws Throwable {
    KineticLaw kl = klg.createKineticLaw(r1, OrderedMechanism.class, false, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
    test(r1, kl, "vmax_r1*s1*c1*s2*c1/(kic_r1_s1*kmc_r1_s2+(s1*c1*s2*c1+(kmc_r1_s2*s1*c1+kmc_r1_s1*s2*c1)))");
  }
  
  
  /*=========================================================*
   *      R E V E R S I B L E   R E A C T I O N S            *
   *=========================================================*/
  
  /**
   * Test for the {@link CommonModularRateLaw} for A + B <=> C
   * @throws Throwable
   */
  @Test
  public void testCommonModularRateLawRev() throws Throwable {
    KineticLaw kl = klg.createKineticLaw(r1, CommonModularRateLaw.class, true, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
    test(r1, kl, "(vmaf_r1*(s1*c1/kmc_r1_s1)^hco_r1*(s2*c1/kmc_r1_s2)^hco_r1-vmar_r1*(p1*c1/kmc_r1_p1)^hco_r1)/((1+s1*c1/kmc_r1_s1)^hco_r1*(1+s2*c1/kmc_r1_s2)^hco_r1+(1+p1*c1/kmc_r1_p1)^hco_r1-1)");
  }
  
  /**
   * Test for the {@link CommonModularRateLaw} for A + B <=> C
   * @throws Throwable
   */
  @Test
  public void testCommonModularRateLawHalRev() throws Throwable {
    KineticLaw kl = klg.createKineticLaw(r1, CommonModularRateLaw.class, true, TypeStandardVersion.hal, UnitConsistencyType.amount, 1d);
    test(r1, kl, "(vmag_r1*(keq_r1^hco_r1)^(0.5)*(s1*c1)^hco_r1*(s2*c1)^hco_r1/(kmc_r1_s1^hco_r1*kmc_r1_s2^hco_r1*kmc_r1_p1^hco_r1)^(0.5)-vmag_r1/(keq_r1^hco_r1)^(0.5)*(p1*c1)^hco_r1/(kmc_r1_s1^hco_r1*kmc_r1_s2^hco_r1*kmc_r1_p1^hco_r1)^(0.5))/((1+s1*c1/kmc_r1_s1)^hco_r1*(1+s2*c1/kmc_r1_s2)^hco_r1+(1+p1*c1/kmc_r1_p1)^hco_r1-1)");
  }
  
  /**
   * Test for the {@link CommonModularRateLaw} for A + B <=> C
   * @throws Throwable
   */
  @Test
  public void testCommonModularRateLawWegRev() throws Throwable {
    KineticLaw kl = klg.createKineticLaw(r1, CommonModularRateLaw.class, true, TypeStandardVersion.weg, UnitConsistencyType.amount, 1d);
    test(r1, kl, "vmag_r1*((s1*c1)^hco_r1*(s2*c1)^hco_r1/(1*e^(hco_r1*(scp_s1+scp_s2+scp_p1)/(2*T*R)))-(p1*c1)^hco_r1*1*e^(hco_r1*(scp_s1+scp_s2+scp_p1)/(2*T*R)))/(kmc_r1_s1^hco_r1*kmc_r1_s2^hco_r1*kmc_r1_p1^hco_r1)^(0.5)/((1+s1*c1/kmc_r1_s1)^hco_r1*(1+s2*c1/kmc_r1_s2)^hco_r1+(1+p1*c1/kmc_r1_p1)^hco_r1-1)");
  }
  
  /**
   * Test for the {@link DirectBindingModularRateLaw} for A + B <=> C
   * @throws Throwable
   */
  @Test
  public void testDirectBindingModularRateLawRev() throws Throwable {
    KineticLaw kl = klg.createKineticLaw(r1, DirectBindingModularRateLaw.class, true, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
    test(r1, kl, "(vmaf_r1*(s1*c1/kmc_r1_s1)^hco_r1*(s2*c1/kmc_r1_s2)^hco_r1-vmar_r1*(p1*c1/kmc_r1_p1)^hco_r1)/(1+(s1*c1/kmc_r1_s1)^hco_r1*(s2*c1/kmc_r1_s2)^hco_r1+(p1*c1/kmc_r1_p1)^hco_r1)");
  }
  
  /**
   * Test for the {@link SimultaneousBindingModularRateLaw} for A + B <=> C
   * @throws Throwable
   */
  @Test
  public void testSimultaneousBindingModularRateLawRev() throws Throwable {
    KineticLaw kl = klg.createKineticLaw(r1, SimultaneousBindingModularRateLaw.class, true, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
    test(r1, kl, "(vmaf_r1*(s1*c1/kmc_r1_s1)^hco_r1*(s2*c1/kmc_r1_s2)^hco_r1-vmar_r1*(p1*c1/kmc_r1_p1)^hco_r1)/((1+s1*c1/kmc_r1_s1)^hco_r1*(1+s2*c1/kmc_r1_s2)^hco_r1*(1+p1*c1/kmc_r1_p1)^hco_r1)");
  }
  
  /**
   * Test for the {@link ForeDependentModularRateLaw} for A + B <=> C
   * @throws Throwable
   */
  @Test
  public void testForceDependentRateLawRev() throws Throwable {
    KineticLaw kl = klg.createKineticLaw(r1, ForceDependentModularRateLaw.class, true, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
    test(r1, kl, "(vmaf_r1*(s1*c1/kmc_r1_s1)^hco_r1*(s2*c1/kmc_r1_s2)^hco_r1-vmar_r1*(p1*c1/kmc_r1_p1)^hco_r1)/((s1*c1/kmc_r1_s1)^hco_r1*(s2*c1/kmc_r1_s2)^hco_r1*(p1*c1/kmc_r1_p1)^hco_r1)^(0.5)");
  }
  
  /**
   * Test for the {@link PowerLawModularRateLaw} for A + B <=> C
   * @throws Throwable
   */
  @Test
  public void testPowerLawModularRateLawRev() throws Throwable {
    KineticLaw kl = klg.createKineticLaw(r1, PowerLawModularRateLaw.class, true, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
    test(r1, kl, "vmaf_r1*(s1*c1/kmc_r1_s1)^hco_r1*(s2*c1/kmc_r1_s2)^hco_r1-vmar_r1*(p1*c1/kmc_r1_p1)^hco_r1");
  }
  
  /**
   * Test for the {@link PowerLawModularRateLaw} for A + B <=> C
   * @throws Throwable
   */
  @Test
  public void testPowerLawModularRateLawHalRev() throws Throwable {
    KineticLaw kl = klg.createKineticLaw(r1, PowerLawModularRateLaw.class, true, TypeStandardVersion.hal, UnitConsistencyType.amount, 1d);
    test(r1, kl, "vmag_r1*(keq_r1^hco_r1)^(0.5)*(s1*c1)^hco_r1*(s2*c1)^hco_r1/(kmc_r1_s1^hco_r1*kmc_r1_s2^hco_r1*kmc_r1_p1^hco_r1)^(0.5)-vmag_r1/(keq_r1^hco_r1)^(0.5)*(p1*c1)^hco_r1/(kmc_r1_s1^hco_r1*kmc_r1_s2^hco_r1*kmc_r1_p1^hco_r1)^(0.5)");
  }
  
  /**
   * Test for the {@link PowerLawModularRateLaw} for A + B <=> C
   * @throws Throwable
   */
  @Test
  public void testPowerLawModularRateLawWegRev() throws Throwable {
    KineticLaw kl = klg.createKineticLaw(r1, PowerLawModularRateLaw.class, true, TypeStandardVersion.weg, UnitConsistencyType.amount, 1d);
    test(r1, kl, "vmag_r1*((s1*c1)^hco_r1*(s2*c1)^hco_r1/(1*e^(hco_r1*(scp_s1+scp_s2+scp_p1)/(2*T*R)))-(p1*c1)^hco_r1*1*e^(hco_r1*(scp_s1+scp_s2+scp_p1)/(2*T*R)))/(kmc_r1_s1^hco_r1*kmc_r1_s2^hco_r1*kmc_r1_p1^hco_r1)^(0.5)");
  }
  
  /**
   * Test for the {@link OrderedMechanism} for A + B <=> C
   * @throws Throwable
   */
  @Test
  public void testOrderedMechAmountRev() throws Throwable {
    KineticLaw kl = klg.createKineticLaw(r1, OrderedMechanism.class, true, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
    test(r1, kl, "(vmaf_r1/(kic_r1_s1*kmc_r1_s2)*s1*c1*s2*c1-vmar_r1/kmc_r1_p1*p1*c1)/(1+(kmc_r1_s1*s2*c1/(kic_r1_s1*kmc_r1_s2)+s1*c1/kic_r1_s1)+s1*c1*s2*c1/(kic_r1_s1*kmc_r1_s2)+kmc_r1_s1*p1*c1*s2*c1/(kic_r1_s1*kic_r1_p1*kmc_r1_s2)+p1*c1/kmc_r1_p1)");
  }
  
  
  /*==================================================
      UnitConsistencyType = C O N C E N T R A T I O N
	  ==================================================*/
  
  /*=========================================================*
   *     I R R E V E R S I B L E   R E A C T I O N S            *
   *=========================================================*/
  
  /**
   * Test for the {@link OrderedMechanism} for A + B <=> C
   * @throws Throwable
   */
  @Test
  public void testOrderedMechIrrev() throws Throwable {
    KineticLaw kl = klg.createKineticLaw(r1, OrderedMechanism.class, false, TypeStandardVersion.cat, UnitConsistencyType.concentration, 1d);
    test(r1, kl, "vmax_r1*s1*s2/(kic_r1_s1*kmc_r1_s2+(s1*s2+(kmc_r1_s2*s1+kmc_r1_s1*s2)))");
  }
  
  
  /*=========================================================*
   *      R E V E R S I B L E   R E A C T I O N S            *
   *=========================================================*/
  
  /**
   * Test for the {@link OrderedMechanism} for A + B <=> C
   * @throws Throwable
   */
  @Test
  public void testOrderedMechRev() throws Throwable {
    KineticLaw kl = klg.createKineticLaw(r1, OrderedMechanism.class, true, TypeStandardVersion.cat, UnitConsistencyType.concentration, 1d);
    test(r1, kl, "(vmaf_r1/(kic_r1_s1*kmc_r1_s2)*s1*s2-vmar_r1/kmc_r1_p1*p1)/(1+(kmc_r1_s1*s2/(kic_r1_s1*kmc_r1_s2)+s1/kic_r1_s1)+s1*s2/(kic_r1_s1*kmc_r1_s2)+kmc_r1_s1*p1*s2/(kic_r1_s1*kic_r1_p1*kmc_r1_s2)+p1/kmc_r1_p1)");
  }
  
  
  /*========================================
    			2A (<)-> C
	  ========================================*/
  
  /*==================================================
         UnitConsistencyType = A M O U N T
	  ==================================================*/
  
  /*=========================================================*
   *     I R R E V E R S I B L E   R E A C T I O N S         *
   *=========================================================*/
  
  /**
   * Test for the {@link CommonModularRateLaw} for 2A -> C
   * @throws Throwable
   */
  @Test
  public void testStoch2CommonModularRateLaw() throws Throwable {
    KineticLaw kl = klg.createKineticLaw(r2, CommonModularRateLaw.class, false, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
    test(r2, kl, "vmax_r2*(s1*c1/kmc_r2_s1)^(2*hco_r2)/((1+s1*c1/kmc_r2_s1)^(2*hco_r2)+(1+p1*c1/kmc_r2_p1)^hco_r2-1)");
  }
  
  /**
   * Test for the {@link DirectBindingModularRateLaw} for 2A -> C
   * @throws Throwable
   */
  @Test
  public void testStoch2DirectBindingModularRateLaw() throws Throwable {
    KineticLaw kl = klg.createKineticLaw(r2, DirectBindingModularRateLaw.class, false, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
    test(r2, kl, "vmax_r2*(s1*c1/kmc_r2_s1)^(2*hco_r2)/(1+(s1*c1/kmc_r2_s1)^(2*hco_r2)+(p1*c1/kmc_r2_p1)^hco_r2)");
  }
  
  /**
   * Test for the {@link PowerLawModularRateLaw} for 2A -> C
   * @throws Throwable
   */
  @Test
  public void testStoch2PowerLawModularRateLaw() throws Throwable {
    KineticLaw kl = klg.createKineticLaw(r2, PowerLawModularRateLaw.class, false, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
    test(r2, kl, "vmax_r2*(s1*c1/kmc_r2_s1)^(2*hco_r2)");
  }
  
  /**
   * Test for the {@link SimultaneousBindingModularRateLaw} for 2A -> C
   * @throws Throwable
   */
  @Test
  public void testStoch2SimultaneousBindingModularRateLaw() throws Throwable {
    KineticLaw kl = klg.createKineticLaw(r2, SimultaneousBindingModularRateLaw.class, false, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
    test(r2, kl, "vmax_r2*(s1*c1/kmc_r2_s1)^(2*hco_r2)/((1+s1*c1/kmc_r2_s1)^(2*hco_r2)*(1+p1*c1/kmc_r2_p1)^hco_r2)");
  }
  
  /**
   * Test for the {@link ForeDependentModularRateLaw} for 2A -> C
   * @throws Throwable
   */
  @Test
  public void testStoch2ForceDependentModularRateLaw() throws Throwable {
    KineticLaw kl = klg.createKineticLaw(r2, ForceDependentModularRateLaw.class, false, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
    test(r2, kl, "vmax_r2*(s1*c1/kmc_r2_s1)^(2*hco_r2)/((s1*c1/kmc_r2_s1)^(hco_r2*2)*(p1*c1/kmc_r2_p1)^hco_r2)^(0.5)");
  }
  
  
  /*=========================================================*
   *     R E V E R S I B L E   R E A C T I O N S             *
   *=========================================================*/
  
  
  /**
   * Test for the {@link CommonModularRateLaw} for 2A <=> C
   * @throws Throwable
   */
  @Test
  public void testStoch2CommonModularRateLawRev() throws Throwable {
    KineticLaw kl = klg.createKineticLaw(r2, CommonModularRateLaw.class, true, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
    test(r2, kl, "(vmaf_r2*(s1*c1/kmc_r2_s1)^(2*hco_r2)-vmar_r2*(p1*c1/kmc_r2_p1)^hco_r2)/((1+s1*c1/kmc_r2_s1)^(2*hco_r2)+(1+p1*c1/kmc_r2_p1)^hco_r2-1)");
  }
  
  /**
   * Test for the {@link DirectBindingModularRateLaw} for 2A <=> C
   * @throws Throwable
   */
  @Test
  public void testStoch2DirectBindingModularRateLawRev() throws Throwable {
    KineticLaw kl = klg.createKineticLaw(r2, DirectBindingModularRateLaw.class, true, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
    test(r2, kl, "(vmaf_r2*(s1*c1/kmc_r2_s1)^(2*hco_r2)-vmar_r2*(p1*c1/kmc_r2_p1)^hco_r2)/(1+(s1*c1/kmc_r2_s1)^(2*hco_r2)+(p1*c1/kmc_r2_p1)^hco_r2)");
  }
  
  /**
   * Test for the {@link PowerLawModularRateLaw} for 2A <=> C
   * @throws Throwable
   */
  @Test
  public void testStoch2PowerLawModularRateLawRev() throws Throwable {
    KineticLaw kl = klg.createKineticLaw(r2, PowerLawModularRateLaw.class, true, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
    test(r2, kl, "vmaf_r2*(s1*c1/kmc_r2_s1)^(2*hco_r2)-vmar_r2*(p1*c1/kmc_r2_p1)^hco_r2");
  }
  
  /**
   * Test for the {@link SimultaneousBindingModularRateLaw} for 2A <=> C
   * @throws Throwable
   */
  @Test
  public void testStoch2SimultaneousBindingModularRateLawRev() throws Throwable {
    KineticLaw kl = klg.createKineticLaw(r2, SimultaneousBindingModularRateLaw.class, true, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
    test(r2, kl, "(vmaf_r2*(s1*c1/kmc_r2_s1)^(2*hco_r2)-vmar_r2*(p1*c1/kmc_r2_p1)^hco_r2)/((1+s1*c1/kmc_r2_s1)^(2*hco_r2)*(1+p1*c1/kmc_r2_p1)^hco_r2)");
  }
  
  /**
   * Test for the {@link ForceDependentModularRateLaw} for 2A <=> C
   * @throws Throwable
   */
  @Test
  public void testStoch2ForceDependentModularRateLawRev() throws Throwable {
    KineticLaw kl = klg.createKineticLaw(r2, ForceDependentModularRateLaw.class, true, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
    test(r2, kl, "(vmaf_r2*(s1*c1/kmc_r2_s1)^(2*hco_r2)-vmar_r2*(p1*c1/kmc_r2_p1)^hco_r2)/((s1*c1/kmc_r2_s1)^(hco_r2*2)*(p1*c1/kmc_r2_p1)^hco_r2)^(0.5)");
  }
  
}
