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

import org.sbml.squeezer.io.SBaseChangedListener;

/**
 * 
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * 
 */
public class Reaction extends NamedSBase {

	private Boolean reversible;
	private Boolean fast;

	private ListOf<SpeciesReference> listOfReactants;
	private ListOf<SpeciesReference> listOfProducts;
	private ListOf<ModifierSpeciesReference> listOfModifiers;
	private KineticLaw kineticLaw;

	public Reaction(Reaction reaction) {
		super(reaction);
		this.fast = reaction.getFast();
		if (reaction.isSetKineticLaw())
			setKineticLaw(reaction.getKineticLaw().clone());
		this.listOfReactants = reaction.getListOfReactants().clone();
		this.listOfReactants.parentSBMLObject = this;
		this.listOfProducts = reaction.getListOfProducts().clone();
		this.listOfProducts.parentSBMLObject = this;
		this.listOfModifiers = reaction.getListOfModifiers().clone();
		this.listOfModifiers.parentSBMLObject = this;
		this.reversible = reaction.getReversible();
	}

	public Reaction(String id) {
		super(id);
		listOfReactants = new ListOf<SpeciesReference>();
		listOfProducts = new ListOf<SpeciesReference>();
		listOfModifiers = new ListOf<ModifierSpeciesReference>();
		kineticLaw = null;
		reversible = true;
		fast = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.SBase#addChangeListener(org.sbml.squeezer.io.SBaseChangedListener
	 * )
	 */
	public void addChangeListener(SBaseChangedListener l) {
		super.addChangeListener(l);
		listOfReactants.addChangeListener(l);
		listOfProducts.addChangeListener(l);
		listOfModifiers.addChangeListener(l);
	}

	public void addModifier(ModifierSpeciesReference modspecref) {
		if (!listOfModifiers.contains(modspecref)) {
			listOfModifiers.add(modspecref);
			modspecref.parentSBMLObject = this;
			modspecref.sbaseAdded();
		}
	}

	public void addProduct(SpeciesReference specref) {
		if (!listOfProducts.contains(specref)) {
			listOfProducts.add(specref);
			specref.parentSBMLObject = this;
			specref.sbaseAdded();
		}
	}

	public void addReactant(SpeciesReference specref) {
		if (!listOfReactants.contains(specref)) {
			listOfReactants.add(specref);
			specref.parentSBMLObject = this;
			specref.sbaseAdded();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBase#clone()
	 */
	// @Override
	public Reaction clone() {
		return new Reaction(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBase#equals(java.lang.Object)
	 */
	// @Override
	public boolean equals(Object o) {
		if (o instanceof Reaction) {
			Reaction r = (Reaction) o;
			return r.getFast() == fast && r.getKineticLaw().equals(kineticLaw)
					&& r.getListOfModifiers().equals(listOfModifiers)
					&& r.getListOfProducts().equals(listOfProducts)
					&& r.getListOfReactants().equals(listOfReactants)
					&& r.getName().equals(getName())
					&& r.getReversible() == reversible
					&& r.getSBOTerm() == getSBOTerm();
		}
		return false;
	}

	public Boolean getFast() {
		return fast;
	}

	public KineticLaw getKineticLaw() {
		return kineticLaw;
	}

	public ListOf<ModifierSpeciesReference> getListOfModifiers() {
		return listOfModifiers;
	}

	public ListOf<SpeciesReference> getListOfProducts() {
		return listOfProducts;
	}

	public ListOf<SpeciesReference> getListOfReactants() {
		return listOfReactants;
	}

	public ModifierSpeciesReference getModifier(int i) {
		return listOfModifiers.get(i);
	}

	public int getNumModifiers() {
		return listOfModifiers.size();
	}

	public int getNumProducts() {
		return listOfProducts.size();
	}

	public int getNumReactants() {
		return listOfReactants.size();
	}

	public SpeciesReference getProduct(int i) {
		return listOfProducts.get(i);
	}

	public SpeciesReference getReactant(int i) {
		return listOfReactants.get(i);
	}

	public Boolean getReversible() {
		return reversible;
	}

	public boolean isSetKineticLaw() {
		return kineticLaw != null;
	}

	public void removeModifier(ModifierSpeciesReference modspecref) {
		if (listOfModifiers.remove(modspecref))
			modspecref.sbaseRemoved();
	}

	public void removeProduct(SpeciesReference specref) {
		if (listOfProducts.remove(specref))
			specref.sbaseRemoved();
	}

	public void removeReactant(SpeciesReference specref) {
		if (listOfReactants.remove(specref))
			specref.sbaseRemoved();
	}

	public void setFast(Boolean fast) {
		this.fast = fast;
		stateChanged();
	}

	public void setKineticLaw(KineticLaw kineticLaw) {
		this.kineticLaw = kineticLaw;
		this.kineticLaw.parentSBMLObject = this;
		this.kineticLaw.sbaseAdded();
		stateChanged();
	}

	public void setReversible(Boolean reversible) {
		this.reversible = reversible;
		stateChanged();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.sbml.SBase#getParentSBMLObject()
	 */
	@Override
	public Model getParentSBMLObject() {
		return (Model) super.getParentSBMLObject();
	}
}
