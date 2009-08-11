package org.sbmlsqueezer.sbml;

public class SpeciesReference extends SimpleSpeciesReference {
	
	double stoichiometry = 1;
	
	public SpeciesReference(){
		super();
		
	}
	public SpeciesReference(Species spec){
		super(spec);
	}

	public double getStoichiometry() {
		return stoichiometry;
	}

	public void setStoichiometry(double stoichiometry) {
		this.stoichiometry = stoichiometry;
		stateChanged();
	}
	


}
