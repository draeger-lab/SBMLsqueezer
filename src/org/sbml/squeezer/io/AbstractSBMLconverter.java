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
package org.sbml.squeezer.io;

import java.util.LinkedList;
import java.util.List;

import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.sbml.ASTNode;
import org.sbml.Constants;
import org.sbml.MathContainer;
import org.sbml.Model;
import org.sbml.SBase;
import org.sbml.libsbml.libsbmlConstants;

/**
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * 
 */
public abstract class AbstractSBMLconverter implements SBaseChangedListener,
		ChangeListener, libsbmlConstants {

	private List<SBase> added;
	private List<SBase> removed;
	private List<SBase> changed;
	protected LinkedList<Model> listOfModels;
	private int selectedModel;

	public void setSelectedModel(int selectedModel) {
		this.selectedModel = selectedModel;
	}

	/**
	 * 
	 * @return
	 */
	public Model getSelectedModel() {
		return listOfModels.get(selectedModel);
	}

	/**
	 * 
	 */
	public AbstractSBMLconverter() {
		selectedModel = -1;
		listOfModels = new LinkedList<Model>();
		added = new LinkedList<SBase>();
		removed = new LinkedList<SBase>();
		changed = new LinkedList<SBase>();
	}

	/**
	 * 
	 * @param model
	 */
	public AbstractSBMLconverter(Object model) {
		this();
		this.listOfModels.addLast(convert(model));
	}

	/**
	 * 
	 * @param model
	 * @return
	 */
	public abstract Model convert(Object model);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBaseChangedListener#sbaseAdded(org.sbml.SBase)
	 */
	public void sbaseAdded(SBase sb) {
		added.add(sb);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBaseChangedListener#sbaseRemoved(org.sbml.SBase)
	 */
	public void sbaseRemoved(SBase sb) {
		removed.add(sb);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBaseChangedListener#stateChanged(org.sbml.SBase)
	 */
	public void stateChanged(SBase sb) {
		changed.add(sb);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent
	 * )
	 */
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() instanceof JTabbedPane) {
			JTabbedPane tabbedPane = (JTabbedPane) e.getSource();
			if (tabbedPane.getComponentCount() == 0)
				listOfModels.clear();
			else {
				for (Model m : listOfModels) {
					boolean contains = false;
					for (int i = 0; i < tabbedPane.getTabCount()
							&& !contains; i++) {
						String title = tabbedPane.getTitleAt(i);
						if (title.equals(m.getName())
								|| title.equals(m.getId()))
							contains = true;
					}
					if (!contains)
						listOfModels.remove(m);
				}
				selectedModel = tabbedPane.getSelectedIndex();
			}
		}
	}

	/**
	 * 
	 * @param math
	 * @param parent
	 * @return
	 */
	public ASTNode convert(org.sbml.libsbml.ASTNode math, MathContainer parent) {
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

	public List<Model> getListOfModels() {
		return listOfModels;
	}

}
