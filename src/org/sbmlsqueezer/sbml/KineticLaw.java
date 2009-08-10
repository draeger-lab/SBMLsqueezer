/*
 * Copyright (C) 2009 ZBIT, University of Tübingen, Andreas Dräger
 */
package org.sbmlsqueezer.sbml;

import java.util.LinkedList;
import java.util.List;

import org.sbml.libsbml.ASTNode;

/**
 * @author Andreas Dr&auml;ger <a href="mailto:andreas.draeger@uni-tuebingen.de">andreas.draeger@uni-tuebingen.de</a>
 *
 */
public class KineticLaw {

	List<String> listOfParameters;
	ASTNode ast;
	
	public KineticLaw() {
		listOfParameters = new LinkedList<String>();
		ast = new ASTNode();
	}

	
	public ASTNode getMath() {
		return ast;
	}
}
