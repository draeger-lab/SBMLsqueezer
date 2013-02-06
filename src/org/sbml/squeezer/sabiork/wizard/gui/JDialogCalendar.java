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

import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import javax.swing.*;
import org.sbml.squeezer.sabiork.wizard.model.WizardProperties;

/**
 * A class that provides the possibility to pick a date in a calendar dialog.
 * 
 * @author Matthias Rall
 * @version $Rev$
 */
public class JDialogCalendar extends JDialog implements ActionListener {

	/**
	 * Generated serial version identifier.
	 */
	private static final long serialVersionUID = -6496894143552652769L;
	private boolean dateChanged;
	private Calendar oldCalendar;
	private Calendar newCalendar;
	private Date selectedDate;
	private Font font;
	private JButton buttonPrevious;
	private JButton buttonNext;
	private JLabel[] labelsDayName;
	private JLabel[] labelsDay;
	private JLabel labelMonthYear;
	private JPanel panelCalendar;
	private JPanel panelLabels;
	private JPanel panelNavigation;
	private String[] dayNames;

	public JDialogCalendar(Window owner, ModalityType modalityType,
			Date selectedDate) {
		super(owner, modalityType);
		this.selectedDate = selectedDate;
		initialize();
	}

	private void initialize() {
		dateChanged = false;

		oldCalendar = Calendar.getInstance();
		oldCalendar.setTime(selectedDate);

		newCalendar = Calendar.getInstance();
		newCalendar.setTime(selectedDate);

		font = new Font(getOwner().getFont().getName(), Font.BOLD, getOwner()
				.getFont().getSize());

		buttonPrevious = new JButton(
				WizardProperties.getText("JDIALOG_CALENDAR_TEXT_PREVIOUS"));
		buttonPrevious.setFont(font);
		buttonPrevious.addActionListener(this);

		labelMonthYear = new JLabel();
		labelMonthYear.setFont(font);
		labelMonthYear.setHorizontalAlignment(SwingConstants.CENTER);

		buttonNext = new JButton(
				WizardProperties.getText("JDIALOG_CALENDAR_TEXT_NEXT"));
		buttonNext.setFont(font);
		buttonNext.addActionListener(this);

		panelLabels = new JPanel(new GridLayout(7, 7));

		labelsDayName = new JLabel[7];

		dayNames = new String[] {
				WizardProperties.getText("JDIALOG_CALENDAR_TEXT_SU"),
				WizardProperties.getText("JDIALOG_CALENDAR_TEXT_MO"),
				WizardProperties.getText("JDIALOG_CALENDAR_TEXT_TU"),
				WizardProperties.getText("JDIALOG_CALENDAR_TEXT_WE"),
				WizardProperties.getText("JDIALOG_CALENDAR_TEXT_TH"),
				WizardProperties.getText("JDIALOG_CALENDAR_TEXT_FR"),
				WizardProperties.getText("JDIALOG_CALENDAR_TEXT_SA") };

		for (int i = 0; i < labelsDayName.length; i++) {
			labelsDayName[i] = new JLabel(dayNames[i]);
			labelsDayName[i].setBorder(BorderFactory.createEmptyBorder());
			labelsDayName[i].setFont(font);
			labelsDayName[i].setHorizontalAlignment(SwingConstants.CENTER);
			labelsDayName[i]
					.setForeground(WizardProperties
							.getColor("JDIALOG_CALENDAR_RGB_COLOR_TABLE_HEAD_FOREGROUND"));
			labelsDayName[i]
					.setBackground(WizardProperties
							.getColor("JDIALOG_CALENDAR_RGB_COLOR_TABLE_HEAD_BACKGROUND"));
			labelsDayName[i].setOpaque(true);
			panelLabels.add(labelsDayName[i]);
		}

		labelsDay = new JLabel[42];

		for (int i = 0; i < labelsDay.length; i++) {
			labelsDay[i] = new JLabel();
			labelsDay[i]
					.setBorder(BorderFactory.createLineBorder(WizardProperties
							.getColor("JDIALOG_CALENDAR_RGB_COLOR_TABLE_HEAD_BACKGROUND")));
			labelsDay[i].setHorizontalAlignment(SwingConstants.CENTER);
			labelsDay[i].setForeground(WizardProperties
					.getColor("JDIALOG_CALENDAR_RGB_COLOR_FOREGROUND"));
			labelsDay[i].setBackground(WizardProperties
					.getColor("JDIALOG_CALENDAR_RGB_COLOR_BACKGROUND"));
			labelsDay[i].setOpaque(true);
			labelsDay[i].addMouseListener(new MouseAdapter() {

				public void mouseClicked(MouseEvent e) {
					JLabel labelDay = (JLabel) e.getSource();
					String labelDayText = labelDay.getText();
					if (!labelDayText.isEmpty()) {
						newCalendar.set(Calendar.DAY_OF_MONTH,
								Integer.valueOf(labelDayText));
						dateChanged = true;
						dispose();
					}
				}

				public void mouseEntered(MouseEvent e) {
					JLabel labelDay = (JLabel) e.getSource();
					String labelDayText = labelDay.getText();
					if (!labelDayText.isEmpty()) {
						labelDay.setForeground(WizardProperties
								.getColor("JDIALOG_CALENDAR_RGB_COLOR_SELECTION_FOREGROUND"));
						labelDay.setBackground(WizardProperties
								.getColor("JDIALOG_CALENDAR_RGB_COLOR_SELECTION_BACKGROUND"));
					}
				}

				public void mouseExited(MouseEvent e) {
					JLabel labelDay = (JLabel) e.getSource();
					String labelDayText = labelDay.getText();
					if (!labelDayText.isEmpty()) {
						newCalendar.set(Calendar.DAY_OF_MONTH,
								Integer.valueOf(labelDayText));
						if (newCalendar.get(Calendar.DAY_OF_MONTH) == oldCalendar
								.get(Calendar.DAY_OF_MONTH)
								&& newCalendar.get(Calendar.MONTH) == oldCalendar
										.get(Calendar.MONTH)
								&& newCalendar.get(Calendar.YEAR) == oldCalendar
										.get(Calendar.YEAR)) {
							labelDay.setForeground(WizardProperties
									.getColor("JDIALOG_CALENDAR_RGB_COLOR_SELECTED_DATE_FOREGROUND"));
							labelDay.setBackground(WizardProperties
									.getColor("JDIALOG_CALENDAR_RGB_COLOR_SELECTED_DATE_BACKGROUND"));
						} else {
							labelDay.setForeground(WizardProperties
									.getColor("JDIALOG_CALENDAR_RGB_COLOR_FOREGROUND"));
							labelDay.setBackground(WizardProperties
									.getColor("JDIALOG_CALENDAR_RGB_COLOR_BACKGROUND"));
						}
					}
				}

			});
			panelLabels.add(labelsDay[i]);
		}

		panelNavigation = new JPanel(new BorderLayout());
		panelNavigation.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
		panelNavigation.add(buttonPrevious, BorderLayout.WEST);
		panelNavigation.add(labelMonthYear, BorderLayout.CENTER);
		panelNavigation.add(buttonNext, BorderLayout.EAST);

		panelCalendar = new JPanel(new BorderLayout());
		panelCalendar
				.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		panelCalendar.add(panelNavigation, BorderLayout.NORTH);
		panelCalendar.add(panelLabels, BorderLayout.CENTER);

		setLayout(new BorderLayout());
		setTitle(WizardProperties.getText("JDIALOG_CALENDAR_TEXT_CALENDAR"));
		setSize(new Dimension(300, 250));
		setResizable(false);
		setLocationRelativeTo(getOwner());
		add(panelCalendar, BorderLayout.CENTER);
		displayDate();
		setVisible(true);
	}

	/**
	 * Redraws the calendar if changed.
	 */
	private void displayDate() {
		for (JLabel labelDay : labelsDay) {
			labelDay.setText("");
		}
		newCalendar.set(Calendar.DAY_OF_MONTH, 1);
		int dayOfWeek = newCalendar.get(Calendar.DAY_OF_WEEK);
		int daysInMonth = newCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
		for (int i = dayOfWeek - 1, day = 1; day <= daysInMonth; i++, day++) {
			labelsDay[i].setText(String.valueOf(day));
			newCalendar.set(Calendar.DAY_OF_MONTH, day);
			if (newCalendar.get(Calendar.DAY_OF_MONTH) == oldCalendar
					.get(Calendar.DAY_OF_MONTH)
					&& newCalendar.get(Calendar.MONTH) == oldCalendar
							.get(Calendar.MONTH)
					&& newCalendar.get(Calendar.YEAR) == oldCalendar
							.get(Calendar.YEAR)) {
				labelsDay[i]
						.setForeground(WizardProperties
								.getColor("JDIALOG_CALENDAR_RGB_COLOR_SELECTED_DATE_FOREGROUND"));
				labelsDay[i]
						.setBackground(WizardProperties
								.getColor("JDIALOG_CALENDAR_RGB_COLOR_SELECTED_DATE_BACKGROUND"));
			} else {
				labelsDay[i].setForeground(WizardProperties
						.getColor("JDIALOG_CALENDAR_RGB_COLOR_FOREGROUND"));
				labelsDay[i].setBackground(WizardProperties
						.getColor("JDIALOG_CALENDAR_RGB_COLOR_BACKGROUND"));
			}

		}
		labelMonthYear
				.setText(new SimpleDateFormat("MMMM yyyy", Locale.ENGLISH)
						.format(newCalendar.getTime()));
	}

	/**
	 * Returns the picked date in the calendar.
	 * 
	 * @return
	 */
	public Date getSelectedDate() {
		if (!dateChanged) {
			return oldCalendar.getTime();
		} else {
			return newCalendar.getTime();
		}
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(buttonPrevious)) {
			newCalendar
					.set(Calendar.MONTH, newCalendar.get(Calendar.MONTH) - 1);
			displayDate();
		}
		if (e.getSource().equals(buttonNext)) {
			newCalendar
					.set(Calendar.MONTH, newCalendar.get(Calendar.MONTH) + 1);
			displayDate();
		}
	}

}
