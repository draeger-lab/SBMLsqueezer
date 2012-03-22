/* $Id: KineticLawGeneratorWorker.java 831 2012-02-26 12:33:51Z snagel $
 * $URL: https://rarepos.cs.uni-tuebingen.de/svn-path/SysBio/trunk/src/de/zbit/sbml/gui/KineticLawGeneratorWorker.java $
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
package org.sbml.squeezer.gui;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingWorker;

import org.sbml.jsbml.Reaction;
import org.sbml.squeezer.KineticLawGenerator;
import org.sbml.squeezer.UnitConsistencyType;
import org.sbml.squeezer.kinetics.BasicKineticLaw;
import org.sbml.squeezer.kinetics.TypeStandardVersion;

/**
 * Generates kinetic laws using SwingWorker
 * 
 * @author Sebastian Nagel
 * @since 1.4
 * @version $Rev: 831 $
 */
public class KineticLawGeneratorWorker extends SwingWorker<BasicKineticLaw, Void> {
	/**
	 * A {@link Logger} for this class.
	 */
	private static final transient Logger logger = Logger.getLogger(KineticLawGeneratorWorker.class.getName());
		
	private KineticLawGenerator klg;
	
	private Reaction reaction;
	
	private Class<?> kineticsClass;
	
	private boolean reversibility;
	
	private TypeStandardVersion version;
	private UnitConsistencyType consistency;
	private double defaultNewParamVal;
	
	/**
	 * 
	 * @param klg
	 * @param reaction
	 * @param kineticsClass
	 * @param reversibility
	 * @param version
	 * @param consistency
	 * @param defaultNewParamVal
	 */
	public KineticLawGeneratorWorker(KineticLawGenerator klg, Reaction reaction,
		Class<?> kineticsClass, boolean reversibility, TypeStandardVersion version,
		UnitConsistencyType consistency, double defaultNewParamVal) {
		super();
		this.klg = klg;
		this.reaction = reaction;
		this.kineticsClass = kineticsClass;
		this.reversibility = reversibility;
		this.version = version;
		this.consistency = consistency;
		this.defaultNewParamVal = defaultNewParamVal;
	}

	/* (non-Javadoc)
	 * @see javax.swing.SwingWorker#doInBackground()
	 */
	protected BasicKineticLaw doInBackground() throws Exception {
		BasicKineticLaw bkl = null;
		try {
			bkl = klg.createKineticLaw(reaction, kineticsClass, reversibility, version, consistency, defaultNewParamVal);
		} catch (Throwable e) {
			logger.log(Level.WARNING, e.getMessage());
		}
		
		return bkl;
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.SwingWorker#done()
	 */
	@Override
	protected void done() {
		
	}
	
}
