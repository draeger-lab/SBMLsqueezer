package sabiork.wizard.console;

import java.util.List;
import org.sbml.jsbml.Reaction;
import sabiork.wizard.model.KineticLawImporter;

/**
 * A class for representing a result of the automatic search.
 * 
 * @author Matthias Rall
 * 
 */
public class SearchAResult {

	private Reaction reaction;
	private List<KineticLawImporter> possibleKineticLawImporters;
	private List<KineticLawImporter> impossibleKineticLawImporters;
	private List<KineticLawImporter> totalKineticLawImporters;

	/**
	 * Creates a new result of the automatic search.
	 * 
	 * @param reaction
	 * @param possibleKineticLawImporters
	 * @param impossibleKineticLawImporters
	 * @param totalKineticLawImporters
	 */
	public SearchAResult(Reaction reaction,
			List<KineticLawImporter> possibleKineticLawImporters,
			List<KineticLawImporter> impossibleKineticLawImporters,
			List<KineticLawImporter> totalKineticLawImporters) {
		this.reaction = reaction;
		this.possibleKineticLawImporters = possibleKineticLawImporters;
		this.impossibleKineticLawImporters = impossibleKineticLawImporters;
		this.totalKineticLawImporters = totalKineticLawImporters;
	}

	/**
	 * Returns the corresponding {@link Reaction}.
	 * 
	 * @return the corresponding {@link Reaction}
	 */
	public Reaction getReaction() {
		return reaction;
	}

	/**
	 * Returns a list of all importable {@link KineticLawImporter}.
	 * 
	 * @return a list of all importable {@link KineticLawImporter}
	 */
	public List<KineticLawImporter> getPossibleKineticLawImporters() {
		return possibleKineticLawImporters;
	}

	/**
	 * Returns a list of all {@link KineticLawImporter} which are not
	 * importable.
	 * 
	 * @return a list of all {@link KineticLawImporter} which are not importable
	 */
	public List<KineticLawImporter> getImpossibleKineticLawImporters() {
		return impossibleKineticLawImporters;
	}

	/**
	 * Returns a list of all {@link KineticLawImporter}.
	 * 
	 * @return a list of all {@link KineticLawImporter}.
	 */
	public List<KineticLawImporter> getTotalKineticLawImporters() {
		return totalKineticLawImporters;
	}

	/**
	 * Returns the {@link KineticLawImporter} selected for import.
	 * 
	 * @return the {@link KineticLawImporter} selected for import
	 */
	public KineticLawImporter getSelectedKineticLawImporter() {
		KineticLawImporter selectedKineticLawImporter = null;
		if (!possibleKineticLawImporters.isEmpty()) {
			selectedKineticLawImporter = possibleKineticLawImporters.get(0);
		}
		return selectedKineticLawImporter;
	}

}
