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
import org.sbml.squeezer.SBMLsqueezer;
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
	public static int getEnzymaticCatalysis() {
		return 460;
	}

	/**
	 * 
	 * @return
	 */
	public static Ontology getOntology() {
		return sbo;
	}

	/**
	 * 
	 * @param sboTerm
	 * @return
	 */
	public static Term getTerm(int sboTerm) {
		return sbo.getTerm(intToString(sboTerm));
	}

	/**
	 * 
	 * @param sboTerm
	 * @return
	 */
	public static Term getTerm(String sboTerm) {
		return sbo.getTerm(sboTerm);
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
	 * 
	 * @param sboTerm
	 * @return
	 */
	public static boolean isAntisenseRNA(int sboTerm) {
		return isChildOf(sboTerm, SBMLsqueezer.convertAlias2SBO("ANTISENSERNA"));
	}

	/**
	 * 
	 * @param sboTerm
	 * @return
	 */
	public static boolean isCatalyst(int sboTerm) {
		return isChildOf(sboTerm, SBMLsqueezer.convertAlias2SBO("CATALYSIS"));
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
		if (subject.equals(object))
			return true;
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
	 * 
	 * @param sboTerm
	 * @return
	 */
	public static boolean isDrug(int sboTerm) {
		return isChildOf(sboTerm, SBMLsqueezer.convertAlias2SBO("DRUG"));
	}

	/**
	 * 
	 * @param sboTerm
	 * @return
	 */
	public static boolean isEmptySet(int sboTerm) {
		return isChildOf(sboTerm, SBMLsqueezer.convertAlias2SBO("DEGRADED"));
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
	 * 
	 * @param sboTerm
	 * @return
	 */
	public static boolean isEnzymaticCatalysis(int sboTerm) {
		return isChildOf(sboTerm, 460);
	}

	/**
	 * Function for checking the SBO term is from correct part of SBO.
	 * 
	 * @param sboTerm
	 * @return true if the term is-an Event, false otherwise
	 */
	public static boolean isEvent(int sboTerm) {
		// TODO
		return isChildOf(sboTerm, 231);
	}

	/**
	 * Function for checking the SBO term is from correct part of SBO.
	 * 
	 * @param sboTerm
	 * @return true if the term is-a functional compartment, false otherwise
	 */
	public static boolean isFunctionalCompartment(int sboTerm) {
		return isChildOf(sboTerm, 289);
	}

	/**
	 * Function for checking the SBO term is from correct part of SBO.
	 * 
	 * @param sboTerm
	 * @return true if the term is-a functional entity, false otherwise
	 */
	public static boolean isFunctionalEntity(int sboTerm) {
		return isChildOf(sboTerm, 241);
	}

	/**
	 * 
	 * @param term
	 * @return
	 */
	public static boolean isGene(int sboTerm) {
		return isChildOf(sboTerm, SBMLsqueezer.convertAlias2SBO("GENE"));
	}

	/**
	 * 
	 * @param sboTerm
	 * @return
	 */
	public static boolean isInhibitor(int sboTerm) {
		return isChildOf(sboTerm, SBMLsqueezer.convertAlias2SBO("INHIBITION"));
	}

	/**
	 * Function for checking the SBO term is from correct part of SBO.
	 * 
	 * @param sboTerm
	 * @return true if the term is-an interaction, false otherwise
	 */
	public static boolean isInteraction(int sboTerm) {
		return isChildOf(sboTerm, 231);
	}

	/**
	 * 
	 * @param sboTerm
	 * @return
	 */
	public static boolean isIon(int sboTerm) {
		return isChildOf(sboTerm, SBMLsqueezer.convertAlias2SBO("ION"));
	}

	/**
	 * Function for checking the SBO term is from correct part of SBO.
	 * 
	 * @param sboTerm
	 * @return true if the term is-a kinetic constant, false otherwise
	 */
	public static boolean isKineticConstant(int sboTerm) {
		return isChildOf(sboTerm, 9);
	}

	/**
	 * Function for checking the SBO term is from correct part of SBO.
	 * 
	 * @param sboTerm
	 * @return true if the term is-a logical framework, false otherwise
	 */
	public static boolean isLogicalFramework(int sboTerm) {
		return isChildOf(sboTerm, 234);
	}

	/**
	 * Function for checking the SBO term is from correct part of SBO.
	 * 
	 * @param sboTerm
	 * @return true if the term is-a material entity, false otherwise
	 */
	public static boolean isMaterialEntity(int sboTerm) {
		return isChildOf(sboTerm, 240);
	}

	/**
	 * Function for checking the SBO term is from correct part of SBO.
	 * 
	 * @param sboTerm
	 * @return true if the term is-a mathematical expression, false otherwise
	 */
	public static boolean isMathematicalExpression(int sboTerm) {
		return isChildOf(sboTerm, 64);
	}

	/**
	 * 
	 * @param term
	 * @return
	 */
	public static boolean isMessengerRNA(int sboTerm) {
		return isChildOf(sboTerm, 278);
	}
	
	/**
	 * Function for checking the SBO term is from correct part of SBO.
	 * 
	 * @param sboTerm
	 * @return true if the term is-a modelling framework, false otherwise
	 */
	public static boolean isModellingFramework(int sboTerm) {
		return isChildOf(sboTerm, 4);
	}
	
	/**
	 * Function for checking the SBO term is from correct part of SBO.
	 * 
	 * @param sboTerm
	 * @return true if the term is-a modifier, false otherwise
	 */
	public static boolean isModifier(int sboTerm) {
		return isChildOf(sboTerm, SBMLsqueezer.convertAlias2SBO("MODULATION"));
	}

	/**
	 * Function for checking whether the SBO term is obselete.
	 * 
	 * @param sboTerm
	 * @return true if the term is-an obsolete term, false otherwise
	 */
	public static boolean isObselete(int sboTerm) {
		return sbo.getTerm(intToString(sboTerm)).getDescription().startsWith(
				"obsolete");
	}

	/**
	 * Function for checking the SBO term is from correct part of SBO.
	 * 
	 * @param sboTerm
	 * @return true if the term is-a participant, false otherwise
	 */
	public static boolean isParticipant(int sboTerm) {
		// TODO: obsolete
		return isChildOf(sboTerm, 235);
	}

	/**
	 * Function for checking the SBO term is from correct part of SBO.
	 * 
	 * @param sboTerm
	 * @return true if the term is-a participant role, false otherwise
	 */
	public static boolean isParticipantRole(int sboTerm) {
		return isChildOf(sboTerm, 3);
	}

	/**
	 * 
	 * @param sboTerm
	 * @return
	 */
	public static boolean isPhenotype(int sboTerm) {
		return isChildOf(sboTerm, SBMLsqueezer.convertAlias2SBO("PHENOTYPE"));
	}

	/**
	 * Function for checking the SBO term is from correct part of SBO.
	 * 
	 * @param sboTerm
	 * @return true if the term is-a physical participant, false otherwise
	 */
	public static boolean isPhysicalParticipant(int sboTerm) {
		// TODO: what is this?
		return isChildOf(sboTerm, 240); // material entity?
	}

	/**
	 * Function for checking the SBO term is from correct part of SBO.
	 * 
	 * @param sboTerm
	 * @return true if the term is-a product, false otherwise
	 */
	public static boolean isProduct(int sboTerm) {
		return isChildOf(sboTerm, 11);
	}

	/**
	 * 
	 * @param sboTerm
	 * @return
	 */
	public static boolean isProtein(int sboTerm) {
		return isChildOf(sboTerm, SBMLsqueezer.convertAlias2SBO("PROTEIN"));
	}

	/**
	 * Function for checking the SBO term is from correct part of SBO.
	 * 
	 * @param sboTerm
	 * @return true if the term is-a quantitative parameter, false otherwise
	 */
	public static boolean isQuantitativeParameter(int sboTerm) {
		return isChildOf(sboTerm, 2);
	}

	/**
	 * Function for checking the SBO term is from correct part of SBO.
	 * 
	 * @param sboTerm
	 * @return true if the term is-a rate law, false otherwise
	 */
	public static boolean isRateLaw(int sboTerm) {
		return isChildOf(sboTerm, 1);
	}

	/**
	 * Function for checking the SBO term is from correct part of SBO.
	 * 
	 * @param sboTerm
	 * @return true if the term is-a reactant, false otherwise
	 */
	public static boolean isReactant(int sboTerm) {
		return isChildOf(sboTerm, 10);
	}

	/**
	 * 
	 * @param sboTerm
	 * @return
	 */
	public static boolean isRNA(int sboTerm) {
		return isChildOf(sboTerm, SBMLsqueezer.convertAlias2SBO("RNA"));
	}
	
	/**
	 * 
	 * @param sboTerm
	 * @return
	 */
	public static boolean isSimpleMolecule(int sboTerm) {
		return isChildOf(sboTerm, SBMLsqueezer.convertAlias2SBO("SIMPLE_MOLECULE"));
	}

	/**
	 * 
	 * @param sboTerm
	 * @return
	 */
	public static boolean isStateTransition(int sboTerm) {
		return isChildOf(sboTerm, SBMLsqueezer.convertAlias2SBO("STATE_TRANSITION"));
	}
	
	/**
	 * Function for checking the SBO term is from correct part of SBO.
	 * 
	 * @param sboTerm
	 * @return true if the term is-a steady state expression, false otherwise
	 */
	public static boolean isSteadyStateExpression(int sboTerm) {
		return isChildOf(sboTerm, 391);
	}
	
	/**
	 * 
	 * @param sboTerm
	 * @return
	 */
	public static boolean isStimulator(int sboTerm) {
		return isChildOf(sboTerm, SBMLsqueezer.convertAlias2SBO("PHYSICAL_STIMULATION"));
	}

	/**
	 * 
	 * @param term
	 * @return
	 */
	public static boolean isTranscription(int sboTerm) {
		return isChildOf(sboTerm, SBMLsqueezer.convertAlias2SBO("TRANSCRIPTION"));
	}

	/**
	 * 
	 * @param sboTerm
	 * @return
	 */
	public static boolean isTranscriptionalActivation(int sboTerm) {
		return isChildOf(sboTerm, SBMLsqueezer.convertAlias2SBO("TRANSCRIPTIONAL_ACTIVATION"));
	}
	
	/**
	 * 
	 * @param sboTerm
	 * @return
	 */
	public static boolean isTranscriptionalInhibitor(int sboTerm) {
		return isChildOf(sboTerm, SBMLsqueezer.convertAlias2SBO("TRANSCRIPTIONAL_INHIBITION"));
	}
	
	/**
	 * 
	 * @param term
	 * @return
	 */
	public static boolean isTransitionOmitted(int sboTerm) {
		return isChildOf(sboTerm, SBMLsqueezer.convertAlias2SBO("KNOWN_TRANSITION_OMITTED"));
	}
	
	/**
	 * 
	 * @param term
	 * @return
	 */
	public static boolean isTranslation(int sboTerm) {
		return isChildOf(sboTerm, SBMLsqueezer.convertAlias2SBO("TRANSLATION"));
	}
	
	/**
	 * 
	 * @param sboTerm
	 * @return
	 */
	public static boolean isTranslationalActivation(int sboTerm) {
		return isChildOf(sboTerm, SBMLsqueezer.convertAlias2SBO("TRANSLATIONAL_ACTIVATION"));
	}
	
	/**
	 * 
	 * @param sboTerm
	 * @return
	 */
	public static boolean isTranslationalInhibitor(int sboTerm) {
		return isChildOf(sboTerm, SBMLsqueezer.convertAlias2SBO("TRANSLATIONAL_INHIBITION"));
	}
	
	/**
	 * 
	 * @param term
	 * @return
	 */
	public static boolean isTransport(int sboTerm) {
		return isChildOf(sboTerm, SBMLsqueezer.convertAlias2SBO("TRANSPORT"));
	}
	
	/**
	 * 
	 * @param sboTerm
	 * @return
	 */
	public static boolean isTrigger(int sboTerm) {
		return isChildOf(sboTerm, SBMLsqueezer.convertAlias2SBO("TRIGGER"));
	}
	
	/**
	 * 
	 * @param sboTerm
	 * @return
	 */
	public static boolean isUnknownMolecule(int sboTerm) {
		return isChildOf(sboTerm, SBMLsqueezer.convertAlias2SBO("UNKNOWN"));
	}
	
	/**
	 * 
	 * @param term
	 * @return
	 */
	public static boolean isUnknownTransition(int sboTerm) {
		return isChildOf(sboTerm, SBMLsqueezer.convertAlias2SBO("UNKNOWN_TRANSITION"));
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
}
