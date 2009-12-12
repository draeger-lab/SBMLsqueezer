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
package org.sbmlsqueezer.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.Vector;

import jp.sbi.celldesigner.plugin.PluginModel;
import jp.sbi.celldesigner.plugin.PluginReaction;

/**
 * @author <a href="mailto:Nadine.hassis@gmail.com">Nadine Hassis</a>
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * 
 */
public class ParameterLogger {

	private File file;

	private String line;
	private StringBuffer modelList;
	private String newLine = System.getProperty("line.separator");

	private Vector<StringBuffer[]> reactionsList;

	public ParameterLogger() {
		modelList = new StringBuffer();
		reactionsList = new Vector<StringBuffer[]>();
	}

	public ParameterLogger(File file) throws IOException {
		this();
		this.file = file;
		if (file.exists()) {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			modelList = new StringBuffer(reader.readLine());
			modelList.delete(0, 16);
			line = reader.readLine();
			line = reader.readLine();
			while (line != null) {
				StringBuffer[] team = new StringBuffer[2];
				team[0] = new StringBuffer(line);
				team[0].delete(0, 7);
				line = reader.readLine();
				team[1] = new StringBuffer(line);
				team[1].delete(0, 11);
				reactionsList.add(team);
				line = reader.readLine();
				line = reader.readLine();
			}
		}
	}

	public void addReaction(PluginModel model, PluginReaction reaction) {
		if (!isSetModel(model)) {
			modelList.append(model.getId() + " ");
			reactionsList.add(new StringBuffer[] {
					new StringBuffer(model.getId()),
					new StringBuffer(reaction.getId() + " ") });
		} else {
			reactionsList.get(getNumOfModel(model))[1].append(reaction.getId()
					+ " ");
		}
	}

	public boolean isSetGlobal(PluginModel model, PluginReaction reaction) {
		if (isSetModel(model)) {
			StringTokenizer reacToken = new StringTokenizer(reactionsList
					.get(getNumOfModel(model))[1].toString());
			while (reacToken.hasMoreTokens())
				if (reacToken.nextToken().equals(reaction.getId()))
					return false;
		}
		return true;
	}

	public void removeReaction(PluginModel model, PluginReaction reaction) {
		int index = reactionsList.get(getNumOfModel(model))[1].indexOf(reaction
				.getId());
		/*
		 * System.out.println("Removing from " + index + " to " + (index +
		 * reaction.getId().length()));
		 */
		reactionsList.get(getNumOfModel(model))[1].delete(index, reaction
				.getId().length() + 1);
		// System.out.println(reactionsList.get(getNumOfModel(model))[1]);
	}

	public void writeLogFile() throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		String log = "List of Models: " + modelList + newLine + newLine;
		for (int i = 0; i < reactionsList.size(); i++) {
			log += "Model: " + reactionsList.get(i)[0] + newLine
					+ "Reactions: " + reactionsList.get(i)[1] + newLine
					+ newLine;
		}
		writer.write(log);
		writer.close();
	}

	private int getNumOfModel(PluginModel model) {
		int numberOfModel = 0;
		StringTokenizer modelToken = new StringTokenizer(modelList.toString());
		while (!modelToken.nextToken().equals(model.getId()))
			numberOfModel++;
		return numberOfModel;
	}

	private boolean isSetModel(PluginModel model) {
		StringTokenizer modelToken = new StringTokenizer(modelList.toString());
		while (modelToken.hasMoreTokens())
			if (modelToken.nextToken().equals(model.getId()))
				return true;
		return false;
	}
}
