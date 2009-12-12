/**
 *
 */
package org.sbmlsqueezer.io;

import java.io.BufferedWriter;
import java.io.IOException;

import jp.sbi.celldesigner.plugin.PluginModel;

import org.sbml.libsbml.ListOf;
import org.sbml.libsbml.ListOfEvents;
import org.sbml.libsbml.SBMLDocument;

/**
 *
 * @author wouamba
 * @author Andreas Dr&auml;ger <andreas.draeger@uni-tuebingen.de>
 */
public interface DisplaySBML {

	/**
	 *
	 * @param list
	 * @param name
	 * @param buffer
	 * @param section
	 * @throws IOException
	 */
	public void format(ListOf list, BufferedWriter buffer, boolean section) throws IOException;

	/**
	 *
	 * @param events
	 * @param buffer
	 * @throws IOException
	 */
	public void format(ListOfEvents events, BufferedWriter buffer) throws IOException;


	/**
	 *
	 * @param model
	 * @param buffer
	 * @throws IOException
	 */
	public void format(PluginModel model, BufferedWriter buffer) throws IOException;


	/**
	 *
	 * @param doc
	 * @param buffer
	 * @throws IOException
	 */
	public void format(SBMLDocument doc, BufferedWriter buffer) throws IOException;
}
