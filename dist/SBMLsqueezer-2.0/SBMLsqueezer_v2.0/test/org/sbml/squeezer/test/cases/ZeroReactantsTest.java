/*
 * $Id:  ZeroReactantsTest.java 3:58:03 PM jpfeuffer$
 * $URL: https://rarepos.cs.uni-tuebingen.de/svn-path/SBMLsqueezer/trunk/test/org/sbml/squeezer/test/cases/ZeroReactantsTest.java $
 * ---------------------------------------------------------------------
 * This file is part of SBMLsqueezer, a Java program that creates rate
 * equations for reactions in SBML files (http://sbml.org).
 *
 * Copyright (C) 2006-2015 by the University of Tuebingen, Germany.
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
import org.sbml.squeezer.kinetics.GeneralizedMassAction;
import org.sbml.squeezer.kinetics.TypeStandardVersion;
import org.sbml.squeezer.kinetics.ZerothOrderForwardGMAK;
import org.sbml.squeezer.kinetics.ZerothOrderReverseGMAK;

/**
 * Class that tests the generated equations and derived units for reactions of
 * the form _ (<)-> B
 * 
 * @author Julianus Pfeuffer
 * @version $Rev: 1092 $
 * @since 2.0
 */
public class ZeroReactantsTest extends KineticsTest {
  
  /**
   * Initializes species and reactions
   */
  private Reaction r1;
  
  /* (non-Javadoc)
   * @see org.sbml.squeezer.test.cases.KineticsTest#initModel()
   */
  @Override
  protected Model initModel() {
    SBMLDocument doc = new SBMLDocument(2, 4);
    Model model = doc.createModel("uniuni_model");
    Compartment c = model.createCompartment("c1");
    Species p1 = model.createSpecies("p1", c);
    
    for (Species s : model.getListOfSpecies()) {
      s.setHasOnlySubstanceUnits(false);
    }
    
    r1 = model.createReaction("r1");
    r1.createProduct(p1);
    r1.setReversible(false);
    
    return model;
  }
  
  
  /*=========================================================*
   *      I R R E V E R S I B L E   R E A C T I O N S        *
   *=========================================================*/
  
  
  /**
   * Tests {@link GeneralizedMassAction} Kinetics for the reaction _ -> B with 0th order forward reaction
   * @throws Throwable
   */
  @Test
  public void testGMAK() throws Throwable {
    KineticLaw kl = klg.createKineticLaw(r1, ZerothOrderForwardGMAK.class, false, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
    test(r1, kl, "zkf_r1");
  }
  
  /**
   * Tests {@link GeneralizedMassAction} Kinetics for the reaction _ -> B with 0th order reverse reaction
   * @throws Throwable
   */
  @Test
  public void testRevGMAK() throws Throwable {
    KineticLaw kl = klg.createKineticLaw(r1, ZerothOrderReverseGMAK.class, false, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
    test(r1, kl, "zkf_r1");
  }
  
  
  /*=========================================================*
   *      R E V E R S I B L E   R E A C T I O N S            *
   *=========================================================*/
  
  
  /**
   * Tests {@link GeneralizedMassAction} Kinetics for the reaction _ <=> B with 0th order forward reaction
   * @throws Throwable
   */
  @Test
  public void testGMAKRev() throws Throwable {
    KineticLaw kl = klg.createKineticLaw(r1, ZerothOrderForwardGMAK.class, true, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
    test(r1, kl, "zkf_r1-kr_r1*p1*c1");
  }
  
  /**
   * Tests {@link GeneralizedMassAction} Kinetics for the reaction _ <=> B with 0th order reverse reaction
   * @throws Throwable
   */
  @Test
  public void testRevGMAKRev() throws Throwable {
    KineticLaw kl = klg.createKineticLaw(r1, ZerothOrderReverseGMAK.class, true, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
    test(r1, kl, "zkf_r1-zkr_r1");
  }
  
}
