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
package org.sbml.squeezer.standalone;

import org.sbml.ASTNode;
import org.sbml.Compartment;
import org.sbml.Constants;
import org.sbml.KineticLaw;
import org.sbml.Model;
import org.sbml.ModifierSpeciesReference;
import org.sbml.Parameter;
import org.sbml.Reaction;
import org.sbml.SBase;
import org.sbml.Species;
import org.sbml.SpeciesReference;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.libsbmlConstants;
import org.sbml.squeezer.io.AbstractSBMLconverter;

/**
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * 
 */
public class LibSBMLReader extends AbstractSBMLconverter implements
		libsbmlConstants {

	private Model model;
	private SBMLDocument doc;

	/**
	 * get a libsbml model converts it to sbmlsquezzer format and save the new
	 * model
	 * 
	 * @param model
	 */
	public LibSBMLReader(org.sbml.libsbml.Model model) {
		super();
		this.model = convert(model);
		this.model.addChangeListener(this);
	}

	/**
	 * get a xml file, converts it and save the sbmlsquezzer model
	 * 
	 * @param fileName
	 */
	public LibSBMLReader(String fileName) {
		super();
		doc = (new org.sbml.libsbml.SBMLReader()).readSBML(fileName);
		this.model = convert(doc.getModel());
		this.model.addChangeListener(this);
	}

	public Model getModel() {
		return model;
	}

	public Model convert(org.sbml.libsbml.Model model) {
		this.model = new Model(model.getId());
		int i;
		for (i = 0; i < model.getNumCompartments(); i++)
			this.model.addCompartment(convert(model.getCompartment(i)));
		for (i = 0; i < model.getNumReactions(); i++)
			this.model.addReaction(convert(model.getReaction(i)));
		for (i = 0; i < model.getNumSpecies(); i++)
			this.model.addSpecies(convert(model.getSpecies(i)));
		for (i = 0; i < model.getNumParameters(); i++)
			this.model.addParameter(convert(model.getParameter(i)));
		return this.model;
	}

	public Reaction convert(org.sbml.libsbml.Reaction reac) {
		Reaction reaction = new Reaction(reac.getId());
		for (int i = 0; i < reac.getNumReactants(); i++)
			reaction.addReactant(convert(reac.getReactant(i)));
		for (int i = 0; i < reac.getNumProducts(); i++)
			reaction.addProduct(convert(reac.getProduct(i)));
		for (int i = 0; i < reac.getNumModifiers(); i++)
			reaction.addModifier(convert(reac.getModifier(i)));
		reaction.setSBOTerm(reac.getSBOTerm());
		if (reac.isSetKineticLaw())
			reaction.setKineticLaw(convert(reac.getKineticLaw()));
		if (reac.isSetName())
			reaction.setName(reac.getName());
		reaction.setSBOTerm(reac.getSBOTerm());
		reaction.setNotes(reac.getNotesString());
		reaction.setFast(reac.getFast());
		reaction.setReversible(reac.getReversible());
		reaction.addChangeListener(this);
		return reaction;
	}

	public Species convert(org.sbml.libsbml.Species spec) {
		Species species = new Species(spec.getId());
		species.setSBOTerm(spec.getSBOTerm());
		if (spec.isSetCharge())
			species.setCharge(spec.getCharge());
		species.setBoundaryCondition(spec.getBoundaryCondition());
		species.setConstant(spec.getConstant());
		species.setCompartment(model.getCompartment(spec.getCompartment()));
		species.addChangeListener(this);
		return species;
	}

	public Compartment convert(org.sbml.libsbml.Compartment compartment) {
		Compartment c = new Compartment(compartment.getId(), compartment
				.getName());
		c.setConstant(compartment.getConstant());
		if (compartment.isSetOutside()) {
			Compartment outside = model
					.getCompartment(compartment.getOutside());
			if (outside == null)
				model.addCompartment(convert(compartment));
			c.setOutside(outside);
		}
		c.setSize(compartment.getSize());
		c.setSpatialDimensions((int) compartment.getSpatialDimensions());
		return c;
	}

	public SpeciesReference convert(org.sbml.libsbml.SpeciesReference specref) {
		SpeciesReference spec = new SpeciesReference(new Species(specref
				.getSpecies()));
		spec.setStoichiometry(specref.getStoichiometry());
		spec.addChangeListener(this);
		return spec;
	}

	public ModifierSpeciesReference convert(
			org.sbml.libsbml.ModifierSpeciesReference plumod) {
		ModifierSpeciesReference mod = new ModifierSpeciesReference(
				new Species(plumod.getSpecies()));
		mod.addChangeListener(this);
		return mod;
	}

	public KineticLaw convert(org.sbml.libsbml.KineticLaw plukinlaw) {
		KineticLaw kinlaw = new KineticLaw();
		kinlaw.setNotes(plukinlaw.getNotesString());
		kinlaw.setSBOTerm(plukinlaw.getSBOTerm());
		if (plukinlaw.isSetMath()) {
			ASTNode ast = convert(plukinlaw.getMath(), kinlaw);
			ast.reduceToBinary();
			kinlaw.setMath(ast);
		}
		for (int i = 0; i < plukinlaw.getNumParameters(); i++)
			kinlaw.addParameter(convert(plukinlaw.getParameter(i)));
		kinlaw.addChangeListener(this);
		return kinlaw;
	}

	public ASTNode convert(org.sbml.libsbml.ASTNode math, SBase parent) {
		ASTNode ast;
		switch (math.getType()) {
		case AST_REAL:
			ast = new ASTNode(Constants.AST_REAL, parent);
			ast.setValue(math.getReal());
			break;
		case AST_INTEGER:
			ast = new ASTNode(Constants.AST_INTEGER, parent);
			ast.setValue(math.getInteger());
			break;
		case AST_FUNCTION_LOG:
			ast = new ASTNode(Constants.AST_FUNCTION_LOG, parent);
			break;
		case AST_POWER:
			ast = new ASTNode(Constants.AST_POWER, parent);
			break;
		case AST_PLUS:
			ast = new ASTNode(Constants.AST_PLUS, parent);
			break;
		case AST_MINUS:
			ast = new ASTNode(Constants.AST_MINUS, parent);
			break;
		case AST_TIMES:
			ast = new ASTNode(Constants.AST_TIMES, parent);
			break;
		case AST_DIVIDE:
			ast = new ASTNode(Constants.AST_DIVIDE, parent);
			break;
		case AST_RATIONAL:
			ast = new ASTNode(Constants.AST_RATIONAL, parent);
			break;
		case AST_NAME_TIME:
			ast = new ASTNode(Constants.AST_NAME_TIME, parent);
			break;
		case AST_FUNCTION_DELAY:
			ast = new ASTNode(Constants.AST_FUNCTION_DELAY, parent);
			break;
		case AST_NAME:
			ast = new ASTNode(Constants.AST_NAME, parent);
			ast.setName(math.getName());
			break;
		case AST_CONSTANT_PI:
			ast = new ASTNode(Constants.AST_CONSTANT_PI, parent);
			break;
		case AST_CONSTANT_E:
			ast = new ASTNode(Constants.AST_CONSTANT_E, parent);
			break;
		case AST_CONSTANT_TRUE:
			ast = new ASTNode(Constants.AST_CONSTANT_TRUE, parent);
			break;
		case AST_CONSTANT_FALSE:
			ast = new ASTNode(Constants.AST_CONSTANT_FALSE, parent);
			break;
		case AST_REAL_E:
			ast = new ASTNode(Constants.AST_REAL_E, parent);
			ast.setValue(math.getMantissa(), math.getExponent());
			break;
		case AST_FUNCTION_ABS:
			ast = new ASTNode(Constants.AST_FUNCTION_ABS, parent);
			break;
		case AST_FUNCTION_ARCCOS:
			ast = new ASTNode(Constants.AST_FUNCTION_ARCCOS, parent);
			break;
		case AST_FUNCTION_ARCCOSH:
			ast = new ASTNode(Constants.AST_FUNCTION_ARCCOSH, parent);
			break;
		case AST_FUNCTION_ARCCOT:
			ast = new ASTNode(Constants.AST_FUNCTION_ARCCOT, parent);
			break;
		case AST_FUNCTION_ARCCOTH:
			ast = new ASTNode(Constants.AST_FUNCTION_ARCCOTH, parent);
			break;
		case AST_FUNCTION_ARCCSC:
			ast = new ASTNode(Constants.AST_FUNCTION_ARCCSC, parent);
			break;
		case AST_FUNCTION_ARCCSCH:
			ast = new ASTNode(Constants.AST_FUNCTION_ARCCSCH, parent);
			break;
		case AST_FUNCTION_ARCSEC:
			ast = new ASTNode(Constants.AST_FUNCTION_ARCSEC, parent);
			break;
		case AST_FUNCTION_ARCSECH:
			ast = new ASTNode(Constants.AST_FUNCTION_ARCSECH, parent);
			break;
		case AST_FUNCTION_ARCSIN:
			ast = new ASTNode(Constants.AST_FUNCTION_ARCSIN, parent);
			break;
		case AST_FUNCTION_ARCSINH:
			ast = new ASTNode(Constants.AST_FUNCTION_ARCSINH, parent);
			break;
		case AST_FUNCTION_ARCTAN:
			ast = new ASTNode(Constants.AST_FUNCTION_ARCTAN, parent);
			break;
		case AST_FUNCTION_ARCTANH:
			ast = new ASTNode(Constants.AST_FUNCTION_ARCTANH, parent);
			break;
		case AST_FUNCTION_CEILING:
			ast = new ASTNode(Constants.AST_FUNCTION_CEILING, parent);
			break;
		case AST_FUNCTION_COS:
			ast = new ASTNode(Constants.AST_FUNCTION_COS, parent);
			break;
		case AST_FUNCTION_COSH:
			ast = new ASTNode(Constants.AST_FUNCTION_COSH, parent);
			break;
		case AST_FUNCTION_COT:
			ast = new ASTNode(Constants.AST_FUNCTION_COT, parent);
			break;
		case AST_FUNCTION_COTH:
			ast = new ASTNode(Constants.AST_FUNCTION_COTH, parent);
			break;
		case AST_FUNCTION_CSC:
			ast = new ASTNode(Constants.AST_FUNCTION_CSC, parent);
			break;
		case AST_FUNCTION_CSCH:
			ast = new ASTNode(Constants.AST_FUNCTION_CSCH, parent);
			break;
		case AST_FUNCTION_EXP:
			ast = new ASTNode(Constants.AST_FUNCTION_EXP, parent);
			break;
		case AST_FUNCTION_FACTORIAL:
			ast = new ASTNode(Constants.AST_FUNCTION_FACTORIAL, parent);
			break;
		case AST_FUNCTION_FLOOR:
			ast = new ASTNode(Constants.AST_FUNCTION_FLOOR, parent);
			break;
		case AST_FUNCTION_LN:
			ast = new ASTNode(Constants.AST_FUNCTION_LN, parent);
			break;
		case AST_FUNCTION_POWER:
			ast = new ASTNode(Constants.AST_FUNCTION_POWER, parent);
			break;
		case AST_FUNCTION_ROOT:
			ast = new ASTNode(Constants.AST_FUNCTION_ROOT, parent);
			break;
		case AST_FUNCTION_SEC:
			ast = new ASTNode(Constants.AST_FUNCTION_SEC, parent);
			break;
		case AST_FUNCTION_SECH:
			ast = new ASTNode(Constants.AST_FUNCTION_SECH, parent);
			break;
		case AST_FUNCTION_SIN:
			ast = new ASTNode(Constants.AST_FUNCTION_SIN, parent);
			break;
		case AST_FUNCTION_SINH:
			ast = new ASTNode(Constants.AST_FUNCTION_SINH, parent);
			break;
		case AST_FUNCTION_TAN:
			ast = new ASTNode(Constants.AST_FUNCTION_TAN, parent);
			break;
		case AST_FUNCTION_TANH:
			ast = new ASTNode(Constants.AST_FUNCTION_TANH, parent);
			break;
		case AST_FUNCTION:
			ast = new ASTNode(Constants.AST_FUNCTION, parent);
			ast.setName(math.getName());
			break;
		case AST_LAMBDA:
			ast = new ASTNode(Constants.AST_LAMBDA, parent);
			break;
		case AST_LOGICAL_AND:
			ast = new ASTNode(Constants.AST_LOGICAL_AND, parent);
			break;
		case AST_LOGICAL_XOR:
			ast = new ASTNode(Constants.AST_LOGICAL_XOR, parent);
			break;
		case AST_LOGICAL_OR:
			ast = new ASTNode(Constants.AST_LOGICAL_OR, parent);
			break;
		case AST_LOGICAL_NOT:
			ast = new ASTNode(Constants.AST_LOGICAL_NOT, parent);
			break;
		case AST_FUNCTION_PIECEWISE:
			ast = new ASTNode(Constants.AST_FUNCTION_PIECEWISE, parent);
			break;
		case AST_RELATIONAL_EQ:
			ast = new ASTNode(Constants.AST_RELATIONAL_EQ, parent);
			break;
		case AST_RELATIONAL_GEQ:
			ast = new ASTNode(Constants.AST_RELATIONAL_GEQ, parent);
			break;
		case AST_RELATIONAL_GT:
			ast = new ASTNode(Constants.AST_RELATIONAL_GT, parent);
			break;
		case AST_RELATIONAL_NEQ:
			ast = new ASTNode(Constants.AST_RELATIONAL_NEQ, parent);
			break;
		case AST_RELATIONAL_LEQ:
			ast = new ASTNode(Constants.AST_RELATIONAL_LEQ, parent);
			break;
		case AST_RELATIONAL_LT:
			ast = new ASTNode(Constants.AST_RELATIONAL_LT, parent);
			break;
		default:
			ast = new ASTNode(Constants.AST_UNKNOWN, parent);
			break;
		}
		for (int i = 0; i < math.getNumChildren(); i++)
			ast.addChild(convert(math.getChild(i), parent));
		return ast;
	}

	public Parameter convert(org.sbml.libsbml.Parameter plupara) {
		Parameter para = new Parameter(plupara.getId());
		para.addChangeListener(this);
		return para;
	}

}
