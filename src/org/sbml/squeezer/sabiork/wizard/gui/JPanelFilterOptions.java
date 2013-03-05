/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of SBMLsqueezer, a Java program that creates rate 
 * equations for reactions in SBML files (http://sbml.org).
 *
 * Copyright (C) 2006-2013 by the University of Tuebingen, Germany.
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.prefs.BackingStoreException;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.sbml.squeezer.sabiork.SABIORK;
import org.sbml.squeezer.sabiork.SABIORKOptions;
import org.sbml.squeezer.sabiork.wizard.model.WizardProperties;

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
		super(SABIORKOptions.class);
		dateFormat = new SimpleDateFormat("dd/MM/yyyy");
	}


	/**
	 * Generated serial version identifier.
	 */
	private static final long serialVersionUID = 5448185584732193553L;
	private Date dateSubmitted;
	private JButton buttonCalendar;
	private JPanel panelCalendar;
	private JTextField textFieldCalendar;
	private SimpleDateFormat dateFormat;

	/**
	 * 
	 */
	private void initialize() {
		textFieldCalendar = new JTextField();
		textFieldCalendar.setText(dateFormat.format(dateSubmitted));
		textFieldCalendar.setEnabled(false);

		buttonCalendar = new JButton(new ImageIcon(this.getClass().getResource(
				WizardProperties
						.getText("JPANEL_FILTER_OPTIONS_IMAGE_CALENDAR"))));
		
		panelCalendar = new JPanel();
		panelCalendar.setLayout(new BoxLayout(panelCalendar, BoxLayout.X_AXIS));
		panelCalendar.add(textFieldCalendar);
		panelCalendar.add(buttonCalendar);
	}


	/**
	 * Returns a SABIO-RK query for the currently selected filter options.
	 * 
	 * @return
	 */
	public String getFilterOptionsQuery() {
		try {
			return SABIORK.getFilterOptionsQuery(
				this.getProperty(SABIORKOptions.IS_WILDTYPE),
				this.getProperty(SABIORKOptions.IS_MUTANT),
				this.getProperty(SABIORKOptions.IS_RECOMBINANT),
				this.getProperty(SABIORKOptions.HAS_KINETIC_DATA),
				this.getProperty(SABIORKOptions.LOWEST_PH_VALUE), 
				this.getProperty(SABIORKOptions.HIGHEST_PH_VALUE),
				this.getProperty(SABIORKOptions.LOWEST_TEMPERATURE_VALUE),
				this.getProperty(SABIORKOptions.HIGHEST_TEMPERATURE_VALUE),
				this.getProperty(SABIORKOptions.IS_DIRECT_SUBMISSION),
				this.getProperty(SABIORKOptions.IS_JOURNAL),
				this.getProperty(SABIORKOptions.IS_ENTRIES_INSERTED_SINCE),
				dateFormat.parse(this.getProperty(SABIORKOptions.LOWEST_DATE)));
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}

	

	
	/**
	 * Saves the settings of the filtering
	 */
	public void saveSettings() {
		SBPreferences prefs = SBPreferences
				.getPreferencesFor(SABIORKOptions.class);
		
		prefs.put(SABIORKOptions.IS_WILDTYPE, this.getProperty(SABIORKOptions.IS_WILDTYPE));
		
		prefs.put(SABIORKOptions.IS_MUTANT, this.getProperty(SABIORKOptions.IS_MUTANT));
		
		prefs.put(SABIORKOptions.IS_RECOMBINANT, this.getProperty(SABIORKOptions.IS_RECOMBINANT));
		
		prefs.put(SABIORKOptions.HAS_KINETIC_DATA, this.getProperty(SABIORKOptions.HAS_KINETIC_DATA));
		
		prefs.put(SABIORKOptions.IS_DIRECT_SUBMISSION, this.getProperty(SABIORKOptions.IS_DIRECT_SUBMISSION));
		
		prefs.put(SABIORKOptions.IS_JOURNAL, this.getProperty(SABIORKOptions.IS_JOURNAL));

		prefs.put(SABIORKOptions.IS_ENTRIES_INSERTED_SINCE, this.getProperty(SABIORKOptions.IS_ENTRIES_INSERTED_SINCE));
	
		prefs.put(SABIORKOptions.LOWEST_PH_VALUE, this.getProperty(SABIORKOptions.LOWEST_PH_VALUE));
		
		prefs.put(SABIORKOptions.HIGHEST_PH_VALUE, this.getProperty(SABIORKOptions.HIGHEST_PH_VALUE));
		
		prefs.put(SABIORKOptions.LOWEST_TEMPERATURE_VALUE, this.getProperty(SABIORKOptions.LOWEST_TEMPERATURE_VALUE));
		
		prefs.put(SABIORKOptions.HIGHEST_TEMPERATURE_VALUE, this.getProperty(SABIORKOptions.HIGHEST_TEMPERATURE_VALUE));
		
		prefs.put(SABIORKOptions.LOWEST_DATE, this.getProperty(SABIORKOptions.LOWEST_DATE));
		
	
		try {
			prefs.flush();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
		
	}


}
