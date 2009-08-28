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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Set;

import org.biojava.bio.seq.io.ParseException;
import org.biojava.ontology.Ontology;
import org.biojava.ontology.Term;
import org.biojava.ontology.Triple;
import org.biojava.ontology.io.OboParser;
import org.sbml.squeezer.resources.Resource;

/**
 * Methods for interacting with Systems Biology Ontology (SBO) terms.
 * 
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * 
 */
public class SBO {

	private static Ontology sbo;
	private static final String prefix = "SBO:";

	static {
		OboParser parser = new OboParser();
		try {
			sbo = parser.parseOBO(new BufferedReader(new FileReader(
					Resource.class.getResource("txt/SBO_OBO.obo").getFile())),
					"SBO", "Systems Biology Ontology");
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Checks the format of the given SBO integer portion.
	 * 
	 * @param sboTerm
	 * @return true if sboTerm is in the range {0,.., 9999999}, false otherwise.
	 */
	public static boolean checkTerm(int sboTerm) {
		return 0 <= sboTerm && sboTerm <= 9999999;
	}

	/**
	 * Checks the format of the given SBO string.
	 * 
	 * @param sboTerm
	 * @return true if sboTerm is in the correct format (a zero-padded, seven
	 *         digit string), false otherwise.
	 */
	public static boolean checkTerm(String sboTerm) {
		boolean correct = sboTerm.length() == 11;
		correct &= sboTerm.startsWith(prefix);
		if (correct)
			try {
				int sbo = Integer.parseInt(sboTerm.substring(4));
				correct &= checkTerm(sbo);
			} catch (NumberFormatException nfe) {
				correct = false;
			}
		return correct;
	}

	/**
	 * 
	 * @return
	 */
	public static Ontology getOntology() {
		return sbo;
	}

	/**
	 * Returns the integer as a correctly formatted SBO string. If the sboTerm
	 * is not in the correct range ({0,.., 9999999}), an empty string is
	 * returned.
	 * 
	 * @param sboTerm
	 * @return the given integer sboTerm as a zero-padded seven digit string.
	 */
	public static String intToString(int sboTerm) {
		if (!checkTerm(sboTerm))
			return "";
		StringBuffer sbo = new StringBuffer(prefix);
		sbo.append(Integer.toString(sboTerm));
		while (sbo.length() < 11)
			sbo.insert(4, '0');
		return sbo.toString();
	}

	/**
	 * Function for checking the SBO term is from correct part of SBO.
	 * 
	 * @param sboTerm
	 * @return true if the term is-a conservation law, false otherwise
	 */
	public static boolean isConservationLaw(int sboTerm) {
		return isChildOf(sboTerm, 355);
	}

	/**
	 * Function for checking the SBO term is from correct part of SBO.
	 * 
	 * @param sboTerm
	 * @return true if the term is-a continuous framework, false otherwise
	 */
	public static boolean isContinuousFramework(int sboTerm) {
		return isChildOf(sboTerm, 62);
	}

	/**
	 * Function for checking the SBO term is from correct part of SBO.
	 * 
	 * @param sboTerm
	 * @return true if the term is-a discrete framework, false otherwise
	 */
	public static boolean isDiscreteFramework(int sboTerm) {
		return isChildOf(sboTerm, 63);
	}

	/**
	 * Function for checking the SBO term is from correct part of SBO.
	 * 
	 * @param sboTerm
	 * @return true if the term is-a Entity, false otherwise
	 */
	public static boolean isEntity(int sboTerm) {
		return isChildOf(sboTerm, 236);
	}

	/**
	 * Function for checking the SBO term is from correct part of SBO.
	 * 
	 * @param sboTerm
	 * @return true if the term is-an Event, false otherwise
	 */
	public static boolean isEvent(int sboTerm) {
		return isChildOf(sboTerm, 63);
	}

	/**
	 * Function for checking the SBO term is from correct part of SBO.
	 * 
	 * @param sboTerm
	 * @return
	 */
	public static boolean isFunctionalCompartment(int sboTerm) {
		// TODO
		return false;
	}

	/**
	 * Function for checking the SBO term is from correct part of SBO.
	 * 
	 * @param sboTerm
	 * @return
	 */
	public static boolean isFunctionalEntity(int sboTerm) {
		// TODO
		return false;
	}

	/**
	 * Function for checking the SBO term is from correct part of SBO.
	 * 
	 * @param sboTerm
	 * @return
	 */
	public static boolean isInteraction(int sboTerm) {
		// TODO
		return false;
	}

	/**
	 * Function for checking the SBO term is from correct part of SBO.
	 * 
	 * @param sboTerm
	 * @return
	 */
	public static boolean isKineticConstant(int sboTerm) {
		// TODO
		return false;
	}

	/**
	 * Function for checking the SBO term is from correct part of SBO.
	 * 
	 * @param sboTerm
	 * @return
	 */
	public static boolean isLogicalFramework(int sboTerm) {
		// TODO
		return false;
	}

	/**
	 * Function for checking the SBO term is from correct part of SBO.
	 * 
	 * @param sboTerm
	 * @return
	 */
	public static boolean isMaterialEntity(int sboTerm) {
		// TODO
		return false;
	}

	/**
	 * Function for checking the SBO term is from correct part of SBO.
	 * 
	 * @param sboTerm
	 * @return
	 */
	public static boolean isMathematicalExpression(int sboTerm) {
		// TODO
		return false;
	}

	/**
	 * Function for checking the SBO term is from correct part of SBO.
	 * 
	 * @param sboTerm
	 * @return
	 */
	public static boolean isModellingFramework(int sboTerm) {
		// TODO
		return false;
	}

	/**
	 * Function for checking the SBO term is from correct part of SBO.
	 * 
	 * @param sboTerm
	 * @return
	 */
	public static boolean isModifier(int sboTerm) {
		// TODO
		return false;
	}

	/**
	 * Function for checking whether the SBO term is obselete.
	 * 
	 * @param sboTerm
	 * @return
	 */
	public static boolean isObselete(int sboTerm) {
		// TODO
		return false;
	}

	/**
	 * Function for checking the SBO term is from correct part of SBO.
	 * 
	 * @param sboTerm
	 * @return
	 */
	public static boolean isParticipant(int sboTerm) {
		// TODO
		return false;
	}

	/**
	 * Function for checking the SBO term is from correct part of SBO.
	 * 
	 * @param sboTerm
	 * @return
	 */
	public static boolean isParticipantRole(int sboTerm) {
		// TODO
		return false;
	}

	/**
	 * Function for checking the SBO term is from correct part of SBO.
	 * 
	 * @param sboTerm
	 * @return
	 */
	public static boolean isPhysicalParticipant(int sboTerm) {
		// TODO
		return false;
	}

	/**
	 * Function for checking the SBO term is from correct part of SBO.
	 * 
	 * @param sboTerm
	 * @return
	 */
	public static boolean isProduct(int sboTerm) {
		// TODO
		return false;
	}

	/**
	 * Function for checking the SBO term is from correct part of SBO.
	 * 
	 * @param sboTerm
	 * @return
	 */
	public static boolean isQuantitativeParameter(int sboTerm) {
		// TODO
		return false;
	}

	/**
	 * Function for checking the SBO term is from correct part of SBO.
	 * 
	 * @param sboTerm
	 * @return
	 */
	public static boolean isRateLaw(int sboTerm) {
		// TODO
		return false;
	}

	/**
	 * Function for checking the SBO term is from correct part of SBO.
	 * 
	 * @param sboTerm
	 * @return
	 */
	public static boolean isReactant(int sboTerm) {
		// TODO
		return false;
	}

	/**
	 * Function for checking the SBO term is from correct part of SBO.
	 * 
	 * @param sboTerm
	 * @return
	 */
	public static boolean isSteadyStateExpression(int sboTerm) {
		if (!checkTerm(sboTerm))
			return false;
		return isChildOf(sbo.getTerm(intToString(sboTerm)), sbo
				.getTerm("SBO:0000391"));
	}

	/**
	 * Returns the string as a correctly formatted SBO integer portion.
	 * 
	 * @param sboTerm
	 * @return the given string sboTerm as an integer. If the sboTerm is not in
	 *         the correct format (a zero-padded, seven digit string), -1 is
	 *         returned.
	 */
	public static int stringToInt(String sboTerm) {
		return checkTerm(sboTerm) ? Integer.parseInt(sboTerm.substring(4)) : -1;
	}

	/**
	 * Checks whether the given sboTerm is a member of the SBO subgraph rooted
	 * at parent.
	 * 
	 * @param sboTerm
	 *            An SBO term.
	 * @param parent
	 *            An SBO term that is the root of a certain subgraph within the
	 *            SBO.
	 * @return true if the subgraph of the SBO rooted at the term parent
	 *         contains a term with the id corresponding to sboTerm.
	 */
	public static boolean isChildOf(int sboTerm, int parent) {
		if (!checkTerm(sboTerm))
			return false;
		return isChildOf(sbo.getTerm(intToString(sboTerm)), sbo
				.getTerm(intToString(parent)));
	}

	/**
	 * Traverses the systems biology ontology starting at Term subject until
	 * either the root (SBO:0000000) or the Term object is reached.
	 * 
	 * @param subject
	 *            Child
	 * @param object
	 *            Parent
	 * @return true if subject is a child of object.
	 */
	private static boolean isChildOf(Term subject, Term object) {
		Set<Triple> relations = sbo.getTriples(subject, null, null);
		for (Triple triple : relations) {
			if (triple.getObject().equals(object))
				return true;
			if (isChildOf(triple.getObject(), object))
				return true;
		}
		return false;
	}

	/**
	 * 
	 */
	public SBO() {
		// TODO
	}
}
