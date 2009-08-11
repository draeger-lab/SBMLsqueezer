package org.sbmlsqueezer.sbml;

import java.util.LinkedList;

public class Model extends NamedSBase {
	
LinkedList<Species> listofSpecies = new LinkedList<Species>();
LinkedList<Reaction> listofReactions = new LinkedList<Reaction>();

public Model(String id) {
	super(id);
}

public void addSpecies(Species spec){
	listofSpecies.add(spec);
}

public void removeSpecies(Species spec){
	listofSpecies.remove(spec);
}

public void addReaction(Reaction reac){
	listofReactions.add(reac);
}

public void removeReaction(Reaction reac){
	listofReactions.remove(reac);
}




}
