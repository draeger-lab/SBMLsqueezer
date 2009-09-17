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
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.sbml.squeezer.kinetics;

import java.util.IllegalFormatException;
import java.util.List;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.squeezer.RateLawNotApplicableException;

/**
 * Represents the reversible power law (RP) from Liebermeister et al.
 * 
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * @date 2009-09-17
 */
public class ReversiblePowerLaw extends BasicKineticLaw {

	public static enum Type {
		/**
		 * 
		 */
		CAT,
		/**
		 * 
		 */
		HAL,
		/**
		 * 
		 */
		WEG
	}

	private Type type;

	/**
	 * 
	 * @param parentReaction
	 * @param type
	 * @throws RateLawNotApplicableException
	 * @throws IllegalFormatException
	 */
	public ReversiblePowerLaw(Reaction parentReaction, Object type)
			throws RateLawNotApplicableException, IllegalFormatException {
		super(parentReaction, type);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.squeezer.kinetics.BasicKineticLaw#createKineticEquation(java
	 * .util.List, java.util.List, java.util.List, java.util.List,
	 * java.util.List, java.util.List)
	 */
	// @Override
	ASTNode createKineticEquation(List<String> modE, List<String> modActi,
			List<String> modTActi, List<String> modInhib,
			List<String> modTInhib, List<String> modCat)
			throws RateLawNotApplicableException, IllegalFormatException {
		Reaction r = getParentSBMLObject();
		ASTNode numerator;
		type = Type.valueOf(getTypeParameters()[0].toString());
		switch (type) {
		case CAT:
			numerator = cat(r);
			break;
		case HAL:
			numerator = hal(r);
			break;
		default: // WEG
			numerator = weg(r);
			break;
		}
		// essential activation
		// non-essential activation
		// non-competetive inhibition
		// competetive inhibiton
		unsetSBOTerm();
		return numerator;
	}

	/**
	 * Weg version of the numerator
	 * 
	 * @param listOfReactants
	 * @param listOfProducts
	 * @return
	 */
	ASTNode weg(Reaction r) {
		Parameter kV = createOrGetParameter("kV_", r.getId());
		kV.setName(concat("KV value for reaction ", r.getId()).toString());
		ASTNode numerator = new ASTNode(kV, this);
		ASTNode forward = null;
		for (SpeciesReference specRef : r.getListOfReactants()) {
			// TODO
		}
		if (r.getReversible()) {
			ASTNode backward = null;
			for (SpeciesReference specRef : r.getListOfProducts()) {
				// TODO
			}
			forward.minus(backward);
		}
		return numerator.multiplyWith(forward).divideBy(createRoot(r));
	}

	/**
	 * Hal version of the numerator
	 * 
	 * @param listOfReactants
	 * @param listOfProducts
	 * @return
	 */
	ASTNode hal(Reaction r) {
		Parameter kVr = createOrGetParameter("kv_", r.getId());
		kVr.setName(concat("KV value of reaction ", r.getId()).toString());
		ASTNode numerator = new ASTNode(kVr, this);
		Parameter keq = createOrGetParameter("keq_", r.getId());
		keq.setSBOTerm(281);
		keq.setName(concat("equilibrium constant of reaction ", r.getId())
				.toString());
		Parameter hr = createOrGetParameter("h_", r.getId());
		hr.setSBOTerm(193);
		hr.setName("Hill coefficient");
		ASTNode forward = ASTNode.sqrt(ASTNode.pow(new ASTNode(keq, this),
				new ASTNode(hr, this)));
		for (SpeciesReference specRef : r.getListOfReactants()) {
			Parameter mriplus = createOrGetParameter("m_fwd_", r.getId(),
					underscore, specRef.getSpecies());
			mriplus.setSBOTerm(382);
			mriplus.setName("structure coefficient");
			forward.multiplyWith(ASTNode.pow(this,
					specRef.getSpeciesInstance(), mriplus));
		}
		numerator.multiplyWith(forward);
		if (r.getReversible()) {
			ASTNode backward = ASTNode.frac(1, ASTNode.sqrt(ASTNode.pow(this,
					keq, hr)));
			for (SpeciesReference specRef : r.getListOfProducts()) {
				Parameter mriminus = createOrGetParameter("m_bwd_", r.getId(),
						underscore, specRef.getSpecies());
				mriminus.setSBOTerm(382);
				mriminus.setName("structure coefficient");
				backward.multiplyWith(ASTNode.pow(this, specRef
						.getSpeciesInstance(), mriminus));
			}
			numerator.minus(backward);
		}
		return numerator.divideBy(createRoot(r));
	}

	/**
	 * Creates the root term for hal and weg.
	 * 
	 * @param r
	 * @return
	 */
	private ASTNode createRoot(Reaction r) {
		ASTNode root = null;
		for (SpeciesReference specRef : r.getListOfReactants()) {
			Parameter mriplus = createOrGetParameter("m_fwd_", r.getId(),
					underscore, specRef.getSpecies());
			mriplus.setSBOTerm(382);
			mriplus.setName("structure coefficient");
			Parameter kM = createOrGetParameter("kM_", r.getId(), underscore,
					specRef.getSpecies());
			kM.setSBOTerm(27);
			kM.setName(concat("Michaelis constant of species ",
					specRef.getSpecies(), " in reaction ", r.getId())
					.toString());
			ASTNode exponent = new ASTNode(mriplus, this);
			if (r.getReversible()) {
				Parameter mriminus = createOrGetParameter("m_bwd_", r.getId(),
						underscore, specRef.getSpecies());
				mriminus.setSBOTerm(382);
				mriminus.setName("structure coefficient");
				exponent.plus(new ASTNode(mriminus, this));
			}
			ASTNode curr = ASTNode.pow(new ASTNode(kM, this), exponent);
			if (root == null)
				root = curr;
			else
				root.multiplyWith(curr);
		}
		if (r.getReversible()) {
			for (SpeciesReference specRef : r.getListOfProducts()) {
				Parameter mriminus = createOrGetParameter("m_bwd_", r.getId(),
						underscore, specRef.getSpecies());
				mriminus.setSBOTerm(382);
				mriminus.setName("structure coefficient");
				Parameter kM = createOrGetParameter("kM_", r.getId(),
						underscore, specRef.getSpecies());
				ASTNode exponent = new ASTNode(mriminus, this);
				Parameter mriplus = createOrGetParameter("m_fwd_", r.getId(),
						underscore, specRef.getSpecies());
				mriplus.setSBOTerm(382);
				mriplus.setName("structure coefficient");
				ASTNode curr = ASTNode.pow(new ASTNode(kM, this), exponent
						.plus(mriplus));
				if (root == null)
					root = curr;
				else
					root.multiplyWith(curr);
			}
		}
		return root.sqrt();
	}

	/**
	 * Cat version of the numerator
	 * 
	 * @param listOfReactants
	 * @param listOfProducts
	 * @return
	 */
	private ASTNode cat(Reaction r) {
		ASTNode forward = cat(r, true);
		if (r.getReversible())
			forward.minus(cat(r, false));
		return forward;
	}

	/**
	 * This actually creates the rate for forward or backward cat version
	 * equation.
	 * 
	 * @param r
	 * @param forwardOrBackward
	 * @return
	 */
	private ASTNode cat(Reaction r, boolean forwardOrBackward) {
		Parameter kr = createOrGetParameter("kcat_", forwardOrBackward ? "fwd"
				: "bwd", underscore, r.getId());
		kr.setSBOTerm(forwardOrBackward ? 320 : 321);
		ASTNode rate = new ASTNode(kr, this);
		for (SpeciesReference specRef : r.getListOfReactants()) {
			Parameter kMrSi = createOrGetParameter("km_", r.getId(),
					underscore, specRef.getSpecies());
			kMrSi.setSBOTerm(27);
			Parameter mriPM = createOrGetParameter("m_",
					forwardOrBackward ? "fwd" : "bwd", underscore, r.getId(),
					underscore, specRef.getSpecies());
			mriPM.setSBOTerm(382);
			mriPM.setName("structure coefficient");
			rate.multiplyWith(ASTNode.pow(ASTNode.frac(this, specRef
					.getSpeciesInstance(), kMrSi), new ASTNode(mriPM, this)));
		}
		return rate;
	}

}
