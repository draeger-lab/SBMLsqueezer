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
 * 
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * 
 */
public abstract class NamedSBase extends SBase {

	private String id;
	private String name;

	/**
	 * 
	 */
	public NamedSBase() {
		super();
		id = null;
		name = null;
	}

	/**
	 * 
	 * @param nsb
	 */
	public NamedSBase(NamedSBase nsb) {
		super(nsb);
		if (nsb.isSetId())
			this.id = new String(nsb.getId());
		if (nsb.isSetName())
			this.name = new String(nsb.getName());
	}

	/**
	 * 
	 * @param id
	 */
	public NamedSBase(String id) {
		super();
		this.id = new String(id);
		name = null;
	}

	/**
	 * 
	 * @param id
	 * @param name
	 */
	public NamedSBase(String id, String name) {
		super();
		this.id = new String(id);
		this.name = new String(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBase#equals(java.lang.Object)
	 */
	// @Override
	public boolean equals(Object o) {
		boolean equals = super.equals(o);
		if (o instanceof NamedSBase) {
			NamedSBase nsb = (NamedSBase) o;
			if ((nsb.isSetId() && !isSetId()) || (!nsb.isSetId() && isSetId()))
				return false;
			else if (nsb.isSetId() && isSetId())
				equals &= nsb.getId().equals(getId());
			if ((nsb.isSetName() && !isSetName())
					|| (!nsb.isSetName() && isSetName()))
				return false;
			else if (nsb.isSetName() && isSetName())
				equals &= nsb.getName().equals(getName());
		} else
			equals = false;
		return equals;
	}

	/**
	 * 
	 * @return
	 */
	public String getId() {
		return isSetId() ? id : "";
	}

	/**
	 * 
	 * @return
	 */
	public String getName() {
		return isSetName() ? name : "";
	}

	/**
	 * 
	 * @return
	 */
	public boolean isSetId() {
		return id != null;
	}

	/**
	 * 
	 * @return
	 */
	public boolean isSetName() {
		return name != null;
	}

	/**
	 * 
	 * @param id
	 */
	public void setId(String id) {
		this.id = id;
		stateChanged();
	}

	/**
	 * 
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
		stateChanged();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBase#toString()
	 */
	// @Override
	public String toString() {
		if (isSetName() && getName().length() > 0)
			return name;
		if (isSetId())
			return id;
		String name = getClass().getName();
		return name.substring(name.lastIndexOf('.') + 1);
	}
}
