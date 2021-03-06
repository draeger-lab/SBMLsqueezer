/*
 * $Id:  ReversibleKinetics.java 1:23:59 PM jpfeuffer$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of SBMLsqueezer, a Java program that creates rate
 * equations for reactions in SBML files (http://sbml.org).
 *
 * Copyright (C) 2006-2016  by the University of Tuebingen, Germany.
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
import org.sbml.jsbml.TidySBMLWriter;
import org.sbml.squeezer.UnitConsistencyType;
import org.sbml.squeezer.kinetics.AdditiveModelLinear;
import org.sbml.squeezer.kinetics.TypeStandardVersion;

/**
 * Class with tests for general reversible reactions
 * @author Julianus Pfeuffer
 * 
 * @since 2.0
 */
public class ReversibleKinetics extends KineticsTest {
  
  /**
   * Initializes the model
   */
  private Reaction r1;
  
  /* (non-Javadoc)
   * @see org.sbml.squeezer.test.cases.KineticsTest#initModel()
   */
  @Override
  public Model initModel() {
    
    /**
     * Creation of species
     */
    SBMLDocument doc = new SBMLDocument(3, 2);
    Model model = doc.createModel("uniuni_model");
    Compartment c = model.createCompartment("c1");
    Species s1 = model.createSpecies("s1", c);
    Species p1 = model.createSpecies("p1", c);
    Species p2 = model.createSpecies("p2", c);
    Species inh = model.createSpecies("i1", c);
    Species kat = model.createSpecies("k1", c);
    Species act = model.createSpecies("a1", c);
    inh.setSBOTerm(20);
    act.setSBOTerm(462);
    kat.setSBOTerm(460);

    for (Species s : model.getListOfSpecies()) {
      s.setHasOnlySubstanceUnits(false);
    }
    
    /**
     *  Reaction with 2 products and all three modifiers.
     */
    
    r1 = model.createReaction("r1");
    r1.createReactant(s1).setStoichiometry(1d);
    r1.createProduct(p1).setStoichiometry(1d);
    r1.createProduct(p2).setStoichiometry(1d);
    r1.createModifier(act);
    r1.createModifier(kat);
    r1.createModifier(inh);
    
    return model;
  }
  
  /**
   * Tests the {@link AdditiveModelLinear} for A -&gt; B + C
   * @throws Throwable
   */
  @Test
  public void testAdditiveLinear() throws Throwable{
    TidySBMLWriter.write(model.getSBMLDocument(), System.out, ' ', (short) 2);
    KineticLaw kl = klg.createKineticLaw(r1, AdditiveModelLinear.class, false, TypeStandardVersion.cat, UnitConsistencyType.concentration, 1d);
    test(r1, kl, "m_r1*(w_r1_p1*p1+w_r1_p2*p2+b_r1+v_r1_a1*a1+v_r1_k1*k1+v_r1_i1*i1)");
  }
  
  // TODO: More test cases!
  
}
