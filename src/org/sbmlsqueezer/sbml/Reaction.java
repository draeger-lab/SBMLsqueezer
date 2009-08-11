package org.sbmlsqueezer.sbml;

import java.util.LinkedList;

public class Reaction extends NamedSBase {


	Boolean reversible = true;
	Boolean fast = false;
	

	LinkedList<SpeciesReference> listOfReactants;
	LinkedList<SpeciesReference> listOfProducts;
	LinkedList<ModifierSpeciesReference> listOfModifierSpeciesReferences;

	public Reaction(String id) {
		super(id);
		listOfReactants = new LinkedList<SpeciesReference>();
		listOfProducts =new LinkedList<SpeciesReference>();
		listOfModifierSpeciesReferences = new LinkedList<ModifierSpeciesReference>();
	}

	public void addModifierSpeciesReference(ModifierSpeciesReference modspecref) {
		listOfModifierSpeciesReferences.add(modspecref);
	}

	public void removeModifierSpeciesReference(
			ModifierSpeciesReference modspecref) {
		listOfModifierSpeciesReferences.remove(modspecref);
	}

	public void addReactant(SpeciesReference specref){
		listOfReactants.add(specref);
	}
	
	public void removeReactant(SpeciesReference specref){
		listOfReactants.remove(specref);
	}

	public void addProduct(SpeciesReference specref){
		listOfProducts.add(specref);
	}
	
	public void removeProduct(SpeciesReference specref){
		listOfProducts.remove(specref);
	}


	public Boolean getFast() {
		return fast;
	}

	public Boolean getReversible() {
		return reversible;
	}



	public void setFast(Boolean fast) {
		this.fast = fast;
		stateChanged();
	}

	public void setReversible(Boolean reversible) {
		this.reversible = reversible;
		stateChanged();
	}
}
