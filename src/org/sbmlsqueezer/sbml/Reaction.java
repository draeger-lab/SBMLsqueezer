package org.sbmlsqueezer.sbml;

import java.util.LinkedList;

public class Reaction {
	String id;
	String name;
	Boolean reversible;
	LinkedList<SpeciesReference> specref = new LinkedList<SpeciesReference>();
	LinkedList<ModifierSpeciesReference> modspecref = new LinkedList<ModifierSpeciesReference>();

}
