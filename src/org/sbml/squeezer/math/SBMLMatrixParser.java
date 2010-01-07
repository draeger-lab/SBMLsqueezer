package org.sbml.squeezer.math;

import java.util.HashMap;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;

/**
 * This Class represents a tool to get the information needed to do a stability
 * analysis out of a SBML file
 * 
 * @author <a href="mailto:a.doerr@uni-tuebingen.de">Alexander D&ouml;rr</a>
 * @date 2009-12-18
 * @since 1.3
 */
public class SBMLMatrixParser {

	/**
	 * Loads the the SBML library
	 */

	private SBMLDocument doc;
	private HashMap<String, Integer> hashReactions, hashSpecies;
	private int numSpecies, numReactions;
	private HashMap<String, HashMap<Integer, Integer>> sBOTerms;
	private static int initvalue = 1;
	private String[] reactions, species;

	/**
	 * Creates a new SBMLParser for the file in the given path
	 * 
	 * @param path
	 */
	public SBMLMatrixParser(SBMLDocument doc) {
		this.doc = doc;
		Model m = doc.getModel();
		System.out.println(m.getName());
		this.hashReactions = new HashMap<String, Integer>();
		this.hashSpecies = new HashMap<String, Integer>();
		this.numSpecies = (int) m.getListOfSpecies().size();
		this.numReactions = (int) m.getListOfReactions().size();
		this.reactions = new String[numReactions];
		this.species = new String[numSpecies];
		hashSpezies();
		hashReactions();
		sBOTerms = new HashMap<String, HashMap<Integer, Integer>>();
	}

	/**
	 * Creates a HashMap of the available species and an array with the ids in
	 * the same order as they are hashed to the species indeces used to build
	 * the stoichiometric matrix
	 */
	private void hashSpezies() {
		Model m = doc.getModel();
		for (int i = 0; i < numSpecies; i++) {
			hashSpecies.put(m.getSpecies(i).getId(), Integer.valueOf(i));
			species[i] = m.getSpecies(i).getId();
		}

	}

	/**
	 * Creates a HashMap of the available reactions and an array with the ids in
	 * the same order as they are hashed to the reaction indeces used to build
	 * the stoichiometric matrix
	 */
	private void hashReactions() {
		Model m = doc.getModel();
		for (int i = 0; i < numReactions; i++) {
			hashReactions.put(m.getReaction(i).getId(), i);
			reactions[i] = m.getReaction(i).getId();
		}

	}

	/**
	 * Returns an array with the ids of all participating reactions in the same
	 * order as they are used in the stoichiometric matrix
	 * 
	 * @return
	 */
	public String[] getReactionNames() {
		return this.reactions;
	}

	/**
	 * Returns an array with the ids of all participating species in the same
	 * order as they are used in the stoichiometric matrix
	 * 
	 * @return
	 */
	public String[] getSpeciesNames() {
		return this.species;
	}

	/**
	 * Builds the stoichiometric matrix for the current model
	 * 
	 * @return
	 */
	public StabilityMatrix getStoichiometric() {
		StoichiometricMatrix matrixN = new StoichiometricMatrix(numSpecies,
				numReactions, 0);

		ListOf<SpeciesReference> losr;
		Reaction reac;
		SpeciesReference speciesRef;
		Model model = doc.getModel();
		for (int n = 0; n < numReactions; n++) {
			reac = model.getReaction(n);
			// reactants
			losr = reac.getListOfReactants();

			for (int m = 0; m < losr.size(); m++) {
				speciesRef = reac.getReactant(m);

				matrixN.set(hashSpecies.get(speciesRef.getSpecies()),
						hashReactions.get(reac.getId()), speciesRef
								.getStoichiometry());
			}

			// products
			losr = model.getReaction(n).getListOfProducts();

			for (int m = 0; m < losr.size(); m++) {
				speciesRef = reac.getProduct(m);

				matrixN.set(hashSpecies.get(speciesRef.getSpecies()),
						hashReactions.get(reac.getId()), speciesRef
								.getStoichiometry());
			}

		}

		return matrixN;

	}

	/**
	 * Builds the modulation matrix for the current model
	 * 
	 * @return
	 */
	public StabilityMatrix getModulation() {

		StabilityMatrix matrixW = new StabilityMatrix(numSpecies, numReactions,
				0);

		ListOf<ModifierSpeciesReference> losr;
		Reaction reac;
		ModifierSpeciesReference modSpeciesRef;

		int classification = 0;
		Model model = doc.getModel();
		for (int n = 0; n < numReactions; n++) {
			reac = model.getReaction(n);
			// modifier
			losr = reac.getListOfModifiers();
			if (losr.size() > 0)
				sBOTerms.put(reac.getId(), new HashMap<Integer, Integer>());

			for (int m = 0; m < losr.size(); m++) {
				modSpeciesRef = reac.getModifier(m);

				if (modSpeciesRef.getSBOTerm() == -1)
					classification = classifyModifier(reac, modSpeciesRef);

				// potentiator
				if (classification > 0 || modSpeciesRef.getSBOTerm() == 21) {
					matrixW.set(hashSpecies.get(modSpeciesRef.getSpecies())
							.intValue(), hashReactions.get(reac.getId())
							.intValue(), 1);

					if (modSpeciesRef.getSBOTerm() == -1)
						sBOTerms.get(reac.getId()).put(Integer.valueOf(m), 21);

				}
				// inhibitor
				else if (classification < 0 || modSpeciesRef.getSBOTerm() == 20) {
					matrixW.set(hashSpecies.get(modSpeciesRef.getSpecies()),
							hashReactions.get(reac.getId()), -1);
					if (modSpeciesRef.getSBOTerm() == -1)
						sBOTerms.get(reac.getId()).put(Integer.valueOf(m), 20);
				}
				// catalyst
				else {
					matrixW.set(hashSpecies.get(modSpeciesRef.getSpecies()),
							hashReactions.get(reac.getId()), 0);
					if (modSpeciesRef.getSBOTerm() == -1)
						sBOTerms.get(reac.getId()).put(Integer.valueOf(m), 13);
					// modSpeciesRef.setSBOTerm(13);
				}

			}

		}

		return matrixW;

	}

	/**
	 * Classifies the given modifier in the given reaction by reference to its
	 * impact on the result of the reaction. Returning a 0 for catalyst, -1 for
	 * inhibitor and 1 for potentiator
	 * 
	 * @param reac
	 * @param msr
	 * @return
	 */
	private int classifyModifier(Reaction reac, ModifierSpeciesReference msr) {
		SBMLinterpreter sbmli;
		int result = 0;
		double normal, half, twice;
		double iA;

		initLocalParameter(reac.getKineticLaw().getListOfParameters());
		initGlobalParameter(reac.getKineticLaw().getMath());
		Model m = doc.getModel();
		sbmli = new SBMLinterpreter(m);

		iA = m.getSpecies(msr.getSpecies()).getInitialAmount();
		// evaluate reaction with normal amount
		normal = sbmli.evaluateToDouble(reac.getKineticLaw().getMath());

		// evaluate reaction with half of the normal amount
		m.getSpecies(msr.getSpecies()).setInitialAmount(iA / 2);
		sbmli = new SBMLinterpreter(m);
		half = sbmli.evaluateToDouble(reac.getKineticLaw().getMath());

		// evaluate reaction with twice the normal amount
		m.getSpecies(msr.getSpecies()).setInitialAmount(iA * 2);
		sbmli = new SBMLinterpreter(m);
		twice = sbmli.evaluateToDouble(reac.getKineticLaw().getMath());

		if (half < normal && normal < twice)
			result = 1;
		else if (half > normal && normal > twice)
			result = -1;
		
		m.getSpecies(msr.getSpecies()).setInitialAmount(iA);

		return result;
	}

	/**
	 * Adds SBOTerms to all modifiers hashed in the HashMap sBOTerms because
	 * their SBOTerm hasn't been set yet
	 */
	//TODO noch nötig?
	private void setSBOTerms() {
		HashMap<Integer, Integer> sBOReaction;
		Model m = doc.getModel();
		for (String rid : sBOTerms.keySet()) {
			sBOReaction = sBOTerms.get(rid);
			for (Integer mid : sBOReaction.keySet())
				m.getReaction(rid).getModifier(mid.intValue()).setSBOTerm(
						sBOReaction.get(mid));
		}
	}

	/**
	 * Initializes all parameters in the list of local parameter that haven't
	 * been initialized yet
	 */
	private void initLocalParameter(ListOf<Parameter> lop) {
		for (int i = 0; i < lop.size(); i++) {
			if (!lop.get(i).isSetValue())
				lop.get(i).setValue(initvalue);

		}
	}

	/**
	 * Initializes all parameters in a kinetic law that haven't been initialized
	 * yet
	 */
	private void initGlobalParameter(ASTNode astnode) {
		Model m = doc.getModel();
		ListOf<Species> los = m.getListOfSpecies();
		ListOf<Parameter> lop;
		ListOf<Compartment> loc;
		boolean found = false;
		String nodename = new String();
		int i;

		if (astnode.isName())
			nodename = astnode.getName();

		for (i = 0; i < los.size() && !found; i++) {
			if (los.get(i).getName() == nodename) {
				found = true;
				Species s = los.get(i);
				if (!s.isSetInitialAmount() && !s.isSetInitialConcentration())
					los.get(i).setInitialAmount(initvalue);
			}
		}
		lop = m.getListOfParameters();
		for (i = 0; i < lop.size() && !found; i++) {
			if (lop.get(i).getName() == nodename) {
				found = true;
				if (!lop.get(i).isSetValue())
					lop.get(i).setValue(initvalue);
			}
		}

		loc = m.getListOfCompartments();
		for (i = 0; i < los.size() && !found; i++) {
			if (loc.get(i).getName() == nodename) {
				found = true;
				loc.get(i).setSize(initvalue);
			}

		}

		if (astnode.getRightChild() != null)
			initGlobalParameter(astnode.getRightChild());

		if (astnode.getLeftChild() != null)
			initGlobalParameter(astnode.getLeftChild());

	}

}
