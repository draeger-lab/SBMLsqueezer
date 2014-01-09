/*
 * $Id$
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
package org.sbml.squeezer.sabiork.wizard.gui;

import java.io.IOException;
import java.util.prefs.BackingStoreException;

import org.sbml.squeezer.sabiork.SABIORK;
import org.sbml.squeezer.sabiork.SABIORKPreferences;
import de.zbit.gui.prefs.PreferencesPanelForKeyProvider;
import de.zbit.util.prefs.SBPreferences;

/**
 * A class that provides the possibility to set the different filter options.
 * 
 * @author Matthias Rall
 * @version $Rev$
 */
public class JPanelFilterOptions extends PreferencesPanelForKeyProvider{

	/**
	 * @param provider
	 * @throws IOException
	 */
	public JPanelFilterOptions()
		throws IOException {
		super(SABIORKPreferences.class);
	}


	/**
	 * Generated serial version identifier.
	 */
	private static final long serialVersionUID = 5448185584732193553L;


	/**
	 * Returns a SABIO-RK query for the currently selected filter options.
	 * 
	 * @return
	 */
	public String getFilterOptionsQuery() {
		return SABIORK.getFilterOptionsQuery(
				this.getProperty(SABIORKPreferences.IS_WILDTYPE),
				this.getProperty(SABIORKPreferences.IS_MUTANT),
				this.getProperty(SABIORKPreferences.IS_RECOMBINANT),
				this.getProperty(SABIORKPreferences.HAS_KINETIC_DATA),
				this.getProperty(SABIORKPreferences.LOWEST_PH_VALUE), 
				this.getProperty(SABIORKPreferences.HIGHEST_PH_VALUE),
				this.getProperty(SABIORKPreferences.LOWEST_TEMPERATURE_VALUE),
				this.getProperty(SABIORKPreferences.HIGHEST_TEMPERATURE_VALUE),
				this.getProperty(SABIORKPreferences.IS_DIRECT_SUBMISSION),
				this.getProperty(SABIORKPreferences.IS_JOURNAL),
				this.getProperty(SABIORKPreferences.IS_ENTRIES_INSERTED_SINCE),
				this.getProperty(SABIORKPreferences.LOWEST_DATE));
		
	}

	

	
	/**
	 * Saves the settings of the filtering
	 */
	public void saveSettings() {
		SBPreferences prefs = SBPreferences
				.getPreferencesFor(SABIORKPreferences.class);
		
		prefs.put(SABIORKPreferences.IS_WILDTYPE, this.getProperty(SABIORKPreferences.IS_WILDTYPE));
		
		prefs.put(SABIORKPreferences.IS_MUTANT, this.getProperty(SABIORKPreferences.IS_MUTANT));
		
		prefs.put(SABIORKPreferences.IS_RECOMBINANT, this.getProperty(SABIORKPreferences.IS_RECOMBINANT));
		
		prefs.put(SABIORKPreferences.HAS_KINETIC_DATA, this.getProperty(SABIORKPreferences.HAS_KINETIC_DATA));
		
		prefs.put(SABIORKPreferences.IS_DIRECT_SUBMISSION, this.getProperty(SABIORKPreferences.IS_DIRECT_SUBMISSION));
		
		prefs.put(SABIORKPreferences.IS_JOURNAL, this.getProperty(SABIORKPreferences.IS_JOURNAL));

		prefs.put(SABIORKPreferences.IS_ENTRIES_INSERTED_SINCE, this.getProperty(SABIORKPreferences.IS_ENTRIES_INSERTED_SINCE));
	
		prefs.put(SABIORKPreferences.LOWEST_PH_VALUE, this.getProperty(SABIORKPreferences.LOWEST_PH_VALUE));
		
		prefs.put(SABIORKPreferences.HIGHEST_PH_VALUE, this.getProperty(SABIORKPreferences.HIGHEST_PH_VALUE));
		
		prefs.put(SABIORKPreferences.LOWEST_TEMPERATURE_VALUE, this.getProperty(SABIORKPreferences.LOWEST_TEMPERATURE_VALUE));
		
		prefs.put(SABIORKPreferences.HIGHEST_TEMPERATURE_VALUE, this.getProperty(SABIORKPreferences.HIGHEST_TEMPERATURE_VALUE));
		
		prefs.put(SABIORKPreferences.LOWEST_DATE, this.getProperty(SABIORKPreferences.LOWEST_DATE));
		
	
		try {
			prefs.flush();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
		
	}


}
