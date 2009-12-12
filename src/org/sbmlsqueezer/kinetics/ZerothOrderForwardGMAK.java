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
package org.sbmlsqueezer.kinetics;

import java.io.IOException;
import java.util.List;

import jp.sbi.celldesigner.plugin.PluginModel;
import jp.sbi.celldesigner.plugin.PluginReaction;

/**
 * This class creates generalized mass action rate equations with zeroth order
 * in all reactants.
 * 
 * @since 1.0
 * @version
 * @author <a href="andreas.draeger@uni-tuebingen.de">Andreas Dr&auml;ger</a>
 * @date Feb 8, 2008
 **/
public class ZerothOrderForwardGMAK extends GeneralizedMassAction {

	/**
	 * @param parentReaction
	 * @param model
	 * @throws RateLawNotApplicableException
	 * @throws IOException
	 * @throws IllegalFormatException
	 */
	public ZerothOrderForwardGMAK(PluginReaction parentReaction,
			PluginModel model) throws RateLawNotApplicableException,
			IOException, IllegalFormatException {
		super(parentReaction, model);
		reactantOrder = 0;
		productOrder = Double.NaN;
	}

	public ZerothOrderForwardGMAK(PluginReaction parentReaction,
			PluginModel model, List<String> listOfPossibleEnzymes)
			throws RateLawNotApplicableException, IOException,
			IllegalFormatException {
		super(parentReaction, model, listOfPossibleEnzymes);
		reactantOrder = 0;
		productOrder = Double.NaN;
	}

	// @Override
	protected final StringBuffer association(List<String> catalysts, int catNum) {
		StringBuffer kass = concat("kass_", getParentReaction().getId());
		if (catalysts.size() > 0)
			kass = concat(kass, underscore, catalysts.get(catNum));
		addLocalParameter(kass);
		return new StringBuffer(kass);
	}
}
