/*
 *  SBMLsqueezer creates rate equations for reactions in SBML files
 *  (http://sbml.org).
 *  Copyright (C) 2009 ZBIT, University of Tübingen, Andreas Dräger
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.sbml;

/**
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * 
 */
public class Event extends NamedSBase {
	private boolean useValuesFromTriggerTime;

	private Trigger trigger;

	private ListOf<EventAssignment> listOfEventAssignments;

	private Delay delay;

	public Event() {
		super();
		initDefaults();
	}

	public Event(Event event) {
		super(event);
		this.trigger = event.getTrigger().clone();
		this.useValuesFromTriggerTime = event.isUseValuesFromTriggerTime();
		this.delay = event.getDelay().clone();
		this.listOfEventAssignments = event.getListOfEventAssignments().clone();
	}

	public Event(String id) {
		super(id);
		initDefaults();
	}

	public Event(String id, String name) {
		super(id, name);
		initDefaults();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBase#clone()
	 */
	// @Override
	public Event clone() {
		return new Event(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBase#equals(java.lang.Object)
	 */
	// @Override
	public boolean equals(Object o) {
		if (o instanceof Event) {
			Event e = (Event) o;
			return e.getUseValuesFromTriggerTime() == getUseValuesFromTriggerTime()
					&& e.getDelay().equals(getDelay())
					&& e.getListOfEventAssignments().equals(
							getListOfEventAssignments())
					&& e.getSBOTerm() == getSBOTerm()
					&& e.getTimeUnits().equals(getTimeUnits())
					&& e.getTrigger().equals(getTrigger());
		}
		return false;
	}

	public Delay getDelay() {
		return delay;
	}

	public EventAssignment getEventAssignment(int n) {
		return listOfEventAssignments.get(n);
	}

	public ListOf<EventAssignment> getListOfEventAssignments() {
		return listOfEventAssignments;
	}

	public int getNumEventAssignments() {
		return listOfEventAssignments.size();
	}

	public Trigger getTrigger() {
		return trigger;
	}

	public boolean getUseValuesFromTriggerTime() {
		return getUseValuesFromTriggerTime();
	}

	public void initDefaults() {
		useValuesFromTriggerTime = true;
		trigger = new Trigger();
		listOfEventAssignments = new ListOf<EventAssignment>();
		delay = null;
	}

	public boolean isUseValuesFromTriggerTime() {
		return useValuesFromTriggerTime;
	}

	public void setDelay(Delay delay) {
		this.delay = delay;
	}

	public void setListOfEventAssignments(
			ListOf<EventAssignment> listOfEventAssignments) {
		this.listOfEventAssignments = listOfEventAssignments;
	}

	public void setTrigger(Trigger trigger) {
		this.trigger = trigger;
	}

	public void setUseValuesFromTriggerTime(boolean useValuesFromTriggerTime) {
		this.useValuesFromTriggerTime = useValuesFromTriggerTime;
	}

	public String getTimeUnits() {
		// TODO Auto-generated method stub
		return "";
	}

}
