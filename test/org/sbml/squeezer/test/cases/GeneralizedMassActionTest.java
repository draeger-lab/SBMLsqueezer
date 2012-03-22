/*
 * $Id: GeneralizedMassActionTest.java 14.03.2012 10:40:14 draeger$
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
import org.sbml.jsbml.UnitDefinition;
import org.sbml.squeezer.UnitConsistencyType;
import org.sbml.squeezer.kinetics.GeneralizedMassAction;
import org.sbml.squeezer.kinetics.TypeStandardVersion;

import de.zbit.sbml.util.SBMLtools;

/**
 * @author Andreas Dr&auml;ger
 * @version $Rev$
 * @since 1.4
 */
public class GeneralizedMassActionTest extends KineticsTest {

	/* (non-Javadoc)
	 * @see org.sbml.squeezer.test.cases.KineticsTest#initModel()
	 */
	public Model initModel() {
		SBMLDocument doc = new SBMLDocument(3, 1);
		Model model = doc.createModel("m1");
		
		UnitDefinition substance = UnitDefinition.substance(2, 4);
		UnitDefinition volume = UnitDefinition.volume(2, 4);
		SBMLtools.setLevelAndVersion(substance, 3, 1);
		SBMLtools.setLevelAndVersion(volume, 3, 1);
		
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
		
		// TODO: Deal with undefined values!
		for (Species s : model.getListOfSpecies()) {
			s.setInitialAmount(1d);
			s.setUnits(substance);
		}
		
		Reaction r = model.createReaction("r1");
		r.setReversible(false);
		
		r.createReactant(akg).setStoichiometry(1d);
		r.createReactant(ubiquinone).setStoichiometry(1d);
		r.createReactant(nadplus).setStoichiometry(1d);
		r.createReactant(pi).setStoichiometry(1d);
		r.createReactant(gdp).setStoichiometry(1d);
		r.createProduct(fumarate).setStoichiometry(1d);
		r.createProduct(ubiquinol).setStoichiometry(1d);
		r.createProduct(nadh).setStoichiometry(1d);
		r.createProduct(co2).setStoichiometry(1d);
		r.createProduct(hplus).setStoichiometry(1d);
		r.createProduct(gtp).setStoichiometry(1d);
		
		Reaction r2 = model.createReaction("r2");
		r2.createReactant(model.getSpecies("s01")).setStoichiometry(1d);
		r2.createProduct(model.getSpecies("s02")).setStoichiometry(1d);
		r2.setReversible(false);
		
//		try {
//			SBMLWriter.write(doc, System.out, ' ', (short) 2);
//			System.out.println();
//		} catch (SBMLException exc) {
//			exc.printStackTrace();
//		} catch (XMLStreamException exc) {
//			exc.printStackTrace();
//		}
		
		return model;
	}
	
	/**
	 * 
	 * @throws Throwable
	 */
	@Test
	public void irreversibleUniUni() throws Throwable {
		KineticLaw kl = klg.createKineticLaw(model.getReaction(1), GeneralizedMassAction.class, false, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
		test(kl, "kass_r2*s01");
	}
	
	/**
	 * 
	 * @throws Throwable
	 */
	@Test
	public void testGMAKconcentration() throws Throwable {
		KineticLaw kl = klg.createKineticLaw(model.getReaction(0), GeneralizedMassAction.class, true, TypeStandardVersion.cat, UnitConsistencyType.concentration, 1d);
		test(kl, "kass_r1*s01/cell*s02/cell*s03/cell*s04/cell*s05/cell-kdiss_r1*s06/cell*s07/cell*s08/cell*s09/cell*s10/cell*s11/cell");
	}
	
	/**
	 * 
	 * @throws Throwable
	 */
	@Test
	public void testGMAKamount() throws Throwable {
		KineticLaw kl = klg.createKineticLaw(model.getReaction(0), GeneralizedMassAction.class, true, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
		test(kl, "kass_r1*s01*s02*s03*s04*s05-kdiss_r1*s06*s07*s08*s09*s10*s11");
	}
	
}
