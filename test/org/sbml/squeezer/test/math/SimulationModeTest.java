package org.sbml.squeezer.test.math;

import java.io.IOException;
import java.util.Properties;

import javax.swing.JOptionPane;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.resources.Resource;
import org.sbml.jsbml.xml.libsbml.LibSBMLReader;
import org.sbml.jsbml.xml.libsbml.LibSBMLWriter;
import org.sbml.squeezer.CfgKeys;
import org.sbml.squeezer.SBMLsqueezer;
import org.sbml.squeezer.gui.SBMLsqueezerUI;
import org.sbml.squeezer.gui.SimulationDialog;
import org.sbml.squeezer.io.SBMLio;
import org.sbml.squeezer.math.ModelOverdeterminedException;

/**
 * @author draeger
 * 
 */
public class SimulationModeTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			System.loadLibrary("sbmlj");
			// Extra check to be sure we have access to libSBML:
			Class.forName("org.sbml.libsbml.libsbml");
		} catch (Exception e) {
			System.err.println("Error: could not load the libSBML library");
			e.printStackTrace();
			System.exit(1);
		}
		new SimulationModeTest(args[0]);
	}

	public SimulationModeTest(String testCasesDir) {

		String sbmlfile, csvfile, configfile;
		if (!testCasesDir.endsWith("/")) {
			testCasesDir += "/";
		}

		Properties settings = SBMLsqueezer.getProperties();
		settings.put(CfgKeys.CHECK_FOR_UPDATES, Boolean.valueOf(false));

		SBMLio sbmlIo = new SBMLio(new LibSBMLReader(), new LibSBMLWriter());
		SBMLsqueezerUI gui = new SBMLsqueezerUI(sbmlIo, settings);

		// 919
		for (int modelnr = 1; modelnr < 2; modelnr++)
			try {
				StringBuilder modelFile = new StringBuilder();
				modelFile.append(modelnr);
				while (modelFile.length() < 5) {
					modelFile.insert(0, '0');
				}
				String path = modelFile.toString();
				modelFile.append('/');
				modelFile.append(path);
				modelFile.insert(0, testCasesDir);
				path = modelFile.toString();
				sbmlfile = path + "-sbml-l2v4.xml";
				csvfile = path + "-results.csv";
				configfile = path + "-settings.txt";

				settings.put(CfgKeys.SBML_FILE, sbmlfile);
				settings.put(CfgKeys.CSV_FILE, csvfile);

				Properties cfg = Resource.readProperties(configfile);
				double start = Double.parseDouble(cfg.get("start").toString());
				double end = start
						+ Double.parseDouble(cfg.get("duration").toString());
				double stepsize = (end - start)
						/ Double.parseDouble(cfg.get("steps").toString());

				settings.put(CfgKeys.SIM_START_TIME, Double.valueOf(start));
				settings.put(CfgKeys.SIM_END_TIME, Double.valueOf(end));
				settings.put(CfgKeys.SIM_STEP_SIZE, Double.valueOf(stepsize));

				sbmlIo.convert2Model(sbmlfile);

				Model model = sbmlIo.getSelectedModel();
				if (model != null) {
					SimulationDialog d = new SimulationDialog(null, model,
							settings);
					d.setVariables(cfg.get("variables").toString().trim()
							.split(", "));
					if (csvfile != null)
						try {
							d.openExperimentalData(csvfile);
						} catch (IOException exc) {
							exc.printStackTrace();
							JOptionPane
									.showMessageDialog(null, exc.getMessage(),
											exc.getClass().getSimpleName(),
											JOptionPane.ERROR_MESSAGE);
						}
					d.simulate();
					d.setModal(true);
					d.setVisible(true);
				}

			} catch (IOException e) {
				e.printStackTrace();
			} catch (SBMLException e) {
				e.printStackTrace();
			} catch (ModelOverdeterminedException e) {
				e.printStackTrace();
			}

		gui.dispose();
		System.exit(0);
	}

}
