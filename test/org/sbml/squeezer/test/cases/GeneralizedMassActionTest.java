/*
 * $Id: GeneralizedMassActionTest.java 14.03.2012 10:40:14 draeger$
 * $URL$
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

import static org.junit.Assert.assertTrue;

import javax.xml.stream.XMLStreamException;

import org.junit.Test;
import org.sbml.jsbml.CVTerm;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLWriter;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.UnitDefinition;
import org.sbml.squeezer.OptionsGeneral;
import org.sbml.squeezer.ReactionType;
import org.sbml.squeezer.UnitConsistencyType;
import org.sbml.squeezer.kinetics.CommonModularRateLaw;
import org.sbml.squeezer.kinetics.GeneralizedMassAction;
import org.sbml.squeezer.kinetics.TypeStandardVersion;

/**
 * @author Andreas Dr&auml;ger
 * @version $Rev$
 * @since 2.0
 */
public class GeneralizedMassActionTest extends KineticsTest {
  
  /**
   * Switch to decide if the model has already been printed on the screen.
   */
  private static boolean printed = true;
  
  public GeneralizedMassActionTest() {
    super();
    // Init the default ignore list for species:
    prefs.put(OptionsGeneral.IGNORE_THESE_SPECIES_WHEN_CREATING_LAWS, "C00001,C00038,C00070,C00076,C00080,C00175,C00238,C00282,C00291,C01327,C01528,C14818,C14819");
  }
  
  /* (non-Javadoc)
   * @see org.sbml.squeezer.test.cases.KineticsTest#initModel()
   */
  @Override
  public Model initModel() {
    SBMLDocument doc = new SBMLDocument(3, 1);
    Model model = doc.createModel("m1");
    
    UnitDefinition substance = UnitDefinition.substance(2, 4);
    UnitDefinition volume = UnitDefinition.volume(2, 4);
    org.sbml.jsbml.util.SBMLtools.setLevelAndVersion(substance, 3, 1);
    org.sbml.jsbml.util.SBMLtools.setLevelAndVersion(volume, 3, 1);
    
    substance.getUnit(0).setScale(-6);
    volume.getUnit(0).setScale(-3);
    
    model.setSubstanceUnits(substance);
    model.setVolumeUnits(volume);
    
    Compartment c = model.createCompartment("cell");
    // TODO: Deal with undefined values!
    c.setSize(1d);
    c.setUnits(volume);
    
    Species akg = model.createSpecies("s01", "AKG", c);
    Species ubiquinone = model.createSpecies("s02", "Ubiquinone", c);
    Species nadplus = model.createSpecies("s03", "NAD+", c);
    Species pi = model.createSpecies("s04", "Pi", c);
    Species gdp = model.createSpecies("s05", "GDP", c);
    
    Species fumarate = model.createSpecies("s06", "Fumarate", c);
    Species ubiquinol = model.createSpecies("s07", "Ubiquinol", c);
    Species nadh = model.createSpecies("s08", "NADH", c);
    Species co2 = model.createSpecies("s09", "CO2", c);
    Species hplus = model.createSpecies("s10", "H+", c);
    Species gtp = model.createSpecies("s11", "GTP", c);
    
    Species acetolactate = model.createSpecies("s12", "2-Acetolactate", c);
    Species pyruvate = model.createSpecies("s13", "Pyruvate", c);
    
    Species atp = model.createSpecies("s14", "ATP", c);
    Species hco3minus = model.createSpecies("s15", "HCO3-", c);
    Species oaa = model.createSpecies("s16", "OAA", c);
    Species adp = model.createSpecies("s17", "ADP", c);
    
    akg.addCVTerm(new CVTerm(CVTerm.Qualifier.BQB_IS, "urn:miriam:kegg.compound:C00026"));
    ubiquinone.addCVTerm(new CVTerm(CVTerm.Qualifier.BQB_IS, "urn:miriam:kegg.compound:C00399"));
    nadplus.addCVTerm(new CVTerm(CVTerm.Qualifier.BQB_IS, "urn:miriam:kegg.compound:C00003"));
    pi.addCVTerm(new CVTerm(CVTerm.Qualifier.BQB_IS, "urn:miriam:kegg.compound:C00009"));
    gdp.addCVTerm(new CVTerm(CVTerm.Qualifier.BQB_IS, "urn:miriam:kegg.compound:C00035"));
    fumarate.addCVTerm(new CVTerm(CVTerm.Qualifier.BQB_IS, "urn:miriam:kegg.compound:C00122"));
    ubiquinol.addCVTerm(new CVTerm(CVTerm.Qualifier.BQB_IS, "urn:miriam:kegg.compound:C00390"));
    nadh.addCVTerm(new CVTerm(CVTerm.Qualifier.BQB_IS, "urn:miriam:kegg.compound:C00004"));
    co2.addCVTerm(new CVTerm(CVTerm.Qualifier.BQB_IS, "urn:miriam:kegg.compound:C00011"));
    hplus.addCVTerm(new CVTerm(CVTerm.Qualifier.BQB_IS, "urn:miriam:kegg.compound:C00080"));
    gtp.addCVTerm(new CVTerm(CVTerm.Qualifier.BQB_IS, "urn:miriam:kegg.compound:C00011"));
    acetolactate.addCVTerm(new CVTerm(CVTerm.Qualifier.BQB_IS, "urn:miriam:kegg.compound:C00900"));
    pyruvate.addCVTerm(new CVTerm(CVTerm.Qualifier.BQB_IS, "urn:miriam:kegg.compound:C00022"));
    atp.addCVTerm(new CVTerm(CVTerm.Qualifier.BQB_IS, "urn:miriam:kegg.compound:C00002"));
    hco3minus.addCVTerm(new CVTerm(CVTerm.Qualifier.BQB_IS, "urn:miriam:kegg.compound:C00288"));
    oaa.addCVTerm(new CVTerm(CVTerm.Qualifier.BQB_IS, "urn:miriam:kegg.compound:C00036"));
    adp.addCVTerm(new CVTerm(CVTerm.Qualifier.BQB_IS, "urn:miriam:kegg.compound:C00008"));
    
    // TODO: Deal with undefined values!
    for (Species s : model.getListOfSpecies()) {
      s.setHasOnlySubstanceUnits(true);
      s.setInitialAmount(1d);
      //s.setUnits(substance);
    }
    
    /*
     * AKG + Ubiquinone + NAD+ + Pi + GDP -> Fumarate + Ubiquinol + NADH + CO2 + H+ + GTP
     */
    
    Reaction r1 = model.createReaction("r1");
    r1.setReversible(false);
    
    r1.createReactant(akg).setStoichiometry(1d);
    r1.createReactant(ubiquinone).setStoichiometry(1d);
    r1.createReactant(nadplus).setStoichiometry(1d);
    r1.createReactant(pi).setStoichiometry(1d);
    r1.createReactant(gdp).setStoichiometry(1d);
    r1.createProduct(fumarate).setStoichiometry(1d);
    r1.createProduct(ubiquinol).setStoichiometry(1d);
    r1.createProduct(nadh).setStoichiometry(1d);
    r1.createProduct(co2).setStoichiometry(1d);
    r1.createProduct(hplus).setStoichiometry(1d);
    r1.createProduct(gtp).setStoichiometry(1d);
    
    Reaction r2 = model.createReaction("r2");
    r2.createReactant(model.getSpecies("s01")).setStoichiometry(1d);
    r2.createProduct(model.getSpecies("s02")).setStoichiometry(1d);
    r2.setReversible(false);
    
    /*
     * 2-Acetolactate + CO2 <=> 2 Pyruvate
     */
    Reaction r6 = model.createReaction("R00006");
    r6.createReactant(acetolactate).setStoichiometry(1d);
    r6.createReactant(co2).setStoichiometry(1d);
    r6.createProduct(pyruvate).setStoichiometry(2d);
    r6.setReversible(true);
    r6.addCVTerm(new CVTerm(CVTerm.Qualifier.BQB_IS, "urn:miriam:kegg.reaction:R00006"));
    
    /*
     * Pyruvate + ATP + HCO3- + H+ <=> OAA + Pi + ADP
     */
    Reaction r7 = model.createReaction("R00344");
    r7.createReactant(pyruvate).setStoichiometry(1d);
    r7.createReactant(atp).setStoichiometry(1d);
    r7.createReactant(hco3minus).setStoichiometry(1d);
    r7.createReactant(hplus).setStoichiometry(1d);
    r7.createProduct(oaa).setStoichiometry(1d);
    r7.createProduct(pi).setStoichiometry(1d);
    r7.createProduct(adp).setStoichiometry(1d);
    r7.addCVTerm(new CVTerm(CVTerm.Qualifier.BQB_IS, "urn:miriam:kegg.reaction:R00344"));
    
    if (!printed) {
      try {
        SBMLWriter.write(doc, System.out, ' ', (short) 2);
        System.out.println();
      } catch (SBMLException exc) {
        exc.printStackTrace();
      } catch (XMLStreamException exc) {
        exc.printStackTrace();
      }
      printed = true;
    }
    
    return model;
  }
  
  /**
   * 
   * @throws Throwable
   */
  @Test
  public void irreversibleUniUni() throws Throwable {
    Reaction r2 = model.getReaction("r2");
    KineticLaw kl = klg.createKineticLaw(r2, GeneralizedMassAction.class, false, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
    test(r2, kl, "kass_r2*s01");
    assertTrue(!r2.isReversible());
  }
  
  /**
   * 
   * @throws Throwable
   */
  @Test
  public void testGMAKconcentration() throws Throwable {
    Reaction r1 = model.getReaction("r1");
    ReactionType.removeSpeciesAccordingToIgnoreList(r1, klg.getSpeciesIgnoreList());
    KineticLaw kl = klg.createKineticLaw(r1, GeneralizedMassAction.class, true, TypeStandardVersion.cat, UnitConsistencyType.concentration, 1d);
    test(r1, kl, "kass_r1*s01/cell*s02/cell*s03/cell*s04/cell*s05/cell-kdiss_r1*s06/cell*s07/cell*s08/cell*s09/cell*s11/cell");
    assertTrue(r1.isReversible());
  }
  
  /**
   * 
   * @throws Throwable
   */
  @Test
  public void testGMAKamount() throws Throwable {
    Reaction r1 = model.getReaction("r1");
    ReactionType.removeSpeciesAccordingToIgnoreList(r1, klg.getSpeciesIgnoreList());
    KineticLaw kl = klg.createKineticLaw(r1, GeneralizedMassAction.class, true, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
    test(r1, kl, "kass_r1*s01*s02*s03*s04*s05-kdiss_r1*s06*s07*s08*s09*s11");
    assertTrue(r1.isReversible());
  }
  
  /**
   * 
   * @throws Throwable
   */
  @Test
  public void testGMAKexponent() throws Throwable {
    Reaction r = model.getReaction("R00006");
    KineticLaw kl = klg.createKineticLaw(r, GeneralizedMassAction.class, true, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
    test(r, kl, "kass_R00006*s12*s09-kdiss_R00006*s13^2");
    assertTrue(r.isReversible());
    
    kl = klg.createKineticLaw(model.getReaction("R00006"), GeneralizedMassAction.class, true, TypeStandardVersion.cat, UnitConsistencyType.concentration, 1d);
    test(r, kl, "kass_R00006*s12/cell*s09/cell-kdiss_R00006*(s13/cell)^2");
  }
  
  /**
   * 
   * @throws Throwable
   */
  @Test
  public void testFourReactantsThreeProducts() throws Throwable {
    Reaction r = model.getReaction("R00344");
    ReactionType.removeSpeciesAccordingToIgnoreList(r, klg.getSpeciesIgnoreList());
    KineticLaw kl = klg.createKineticLaw(r, GeneralizedMassAction.class, true, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
    // note that h+ is ignored
    test(r, kl, "kass_R00344*s13*s14*s15-kdiss_R00344*s16*s04*s17");
    assertTrue(r.isReversible());
  }
  
  /**
   * 
   * @throws Throwable
   */
  @Test
  public void testCommonModularRateLaw() throws Throwable {
    Reaction r = model.getReaction("R00006");
    KineticLaw kl = klg.createKineticLaw(r, CommonModularRateLaw.class, false, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
    test(r, kl, "(vmaf_R00006*(s12/kmc_R00006_s12)^hco_R00006*(s09/kmc_R00006_s09)^hco_R00006-vmar_R00006*(s13/kmc_R00006_s13)^(2*hco_R00006))/((1+s12/kmc_R00006_s12)^hco_R00006*(1+s09/kmc_R00006_s09)^hco_R00006+(1+s13/kmc_R00006_s13)^(2*hco_R00006)-1)");
  }
  
}
