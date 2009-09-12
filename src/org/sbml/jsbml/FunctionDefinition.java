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
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.sbml.jsbml;

/**
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * 
 */
public class FunctionDefinition extends MathContainer implements NamedSBase {

	/**
	 * 
	 */
	private String id;
	/**
	 * optional
	 */
	private String name;

	/**
	 * @param sb
	 */
	public FunctionDefinition(FunctionDefinition sb) {
		super(sb);
	}

	/**
	 * 
	 * @param id
	 */
	public FunctionDefinition(String id, int level, int version) {
		super(level, version);
		this.id = id;
	}

	/**
	 * 
	 * @param id
	 * @param lambda
	 */
	public FunctionDefinition(String id, ASTNode lambda, int level, int version) {
		super(lambda, level, version);
		if (!lambda.isLambda())
			throw new IllegalArgumentException(
					"Math element must be of type Lambda.");
		this.id = id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.MathContainer#clone()
	 */
	// @Override
	public FunctionDefinition clone() {
		return new FunctionDefinition(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.MathContainer#equals(java.lang.Object)
	 */
	// @Override
	public boolean equals(Object o) {
		if (o instanceof FunctionDefinition) {
			boolean equal = super.equals(o);
			FunctionDefinition fd = (FunctionDefinition) o;
			equal &= fd.getId().equals(getId());
			equal &= fd.isSetName() == isSetName();
			if (fd.isSetName() && isSetName())
				equal &= fd.getName().equals(getName());
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.NamedSBase#getId()
	 */
	public String getId() {
		return id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.NamedSBase#getName()
	 */
	public String getName() {
		return isSetName() ? name : "";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.NamedSBase#isSetId()
	 */
	public boolean isSetId() {
		return id != null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.NamedSBase#isSetName()
	 */
	public boolean isSetName() {
		return name != null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.MathContainer#setFormula(java.lang.String)
	 */
	// @Override
	public void setFormula(String formula) {
		ASTNode math = ASTNode.parseFormula(formula);
		if (!math.isLambda())
			throw new IllegalArgumentException(
					"Math element must be of type Lambda.");
		setMath(math);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.NamedSBase#setId(java.lang.String)
	 */
	public void setId(String id) {
		this.id = id;
		stateChanged();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.MathContainer#setMath(org.sbml.ASTNode)
	 */
	// @Override
	public void setMath(ASTNode math) {
		if (!math.isLambda())
			throw new IllegalArgumentException(
					"Math element must be of type Lambda.");
		super.setMath(math.clone());
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
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.NamedSBase#setName(java.lang.String)
	 */
	public void setName(String name) {
		this.name = name;
		stateChanged();
	}

}
