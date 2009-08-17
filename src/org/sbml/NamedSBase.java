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

	public NamedSBase() {
		super();
		id = null;
		name = null;
	}

	public NamedSBase(NamedSBase nsb) {
		super(nsb);
		if (nsb.isSetId())
			this.id = new String(nsb.getId());
		if (nsb.isSetName())
			this.name = new String(nsb.getName());
	}

	public NamedSBase(String id) {
		super();
		this.id = id;
		name = null;
	}

	public NamedSBase(String id, String name) {
		super();
		this.id = id;
		this.name = name;
	}

	public String getId() {
		return isSetId() ? id : "";
	}

	public String getName() {
		return isSetName() ? name : "";
	}

	public boolean isSetId() {
		return id != null ? true : false;
	}

	public boolean isSetName() {
		return name != null ? true : false;
	}

	public void setId(String id) {
		this.id = id;
		stateChanged();
	}

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
		if (isSetName())
			return name;
		if (isSetId())
			return id;
		String name = getClass().getName();
		return name.substring(name.lastIndexOf('.') + 1);
	}
}
