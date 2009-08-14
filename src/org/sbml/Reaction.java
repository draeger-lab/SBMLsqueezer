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
public class Reaction extends NamedSBase {

	Boolean reversible = true;
	Boolean fast = false;

	private ListOf<SpeciesReference> listOfReactants;
	private ListOf<SpeciesReference> listOfProducts;
	private ListOf<ModifierSpeciesReference> listOfModifiers;
	private KineticLaw kineticLaw;

	public Reaction(String id) {
		super(id);
		listOfReactants = new ListOf<SpeciesReference>();
		listOfProducts = new ListOf<SpeciesReference>();
		listOfModifiers = new ListOf<ModifierSpeciesReference>();
	}

	public Reaction(Reaction reaction) {
		this(reaction.getId());
		// TODO Auto-generated constructor stub
	}

	public void addModifier(ModifierSpeciesReference modspecref) {
		listOfModifiers.add(modspecref);
		stateChanged();
	}

	public void addProduct(SpeciesReference specref) {
		listOfProducts.add(specref);
		stateChanged();
	}

	public void addReactant(SpeciesReference specref) {
		listOfReactants.add(specref);
		stateChanged();
	}

	public Boolean getFast() {
		return fast;
	}

	public KineticLaw getKineticLaw() {
		return kineticLaw;
	}

	public Boolean getReversible() {
		return reversible;
	}

	public void removeModifier(ModifierSpeciesReference modspecref) {
		listOfModifiers.remove(modspecref);
		stateChanged();
	}

	public void removeProduct(SpeciesReference specref) {
		listOfProducts.remove(specref);
		stateChanged();
	}

	public void removeReactant(SpeciesReference specref) {
		listOfReactants.remove(specref);
		stateChanged();
	}

	public void setFast(Boolean fast) {
		this.fast = fast;
		stateChanged();
	}

	public void setKineticLaw(KineticLaw kineticLaw) {
		this.kineticLaw = kineticLaw;
		this.kineticLaw.setReaction(this);
		stateChanged();
	}

	public void setReversible(Boolean reversible) {
		this.reversible = reversible;
		stateChanged();
	}

	// @Override
	public Reaction clone() {
		return new Reaction(this);
	}

	@Override
	public boolean equals(Object o) {
		// TODO Auto-generated method stub
		return false;
	}
}
