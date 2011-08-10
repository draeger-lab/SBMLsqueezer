package org.sbml.squeezer;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLInputConverter;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.util.IOProgressListener;
import org.sbml.jsbml.util.SBaseChangeListener;

public class SqSBMLReader implements SBMLInputConverter {

	/**
	 * working copy of the original JSBL model
	 */
	Model model;
	/**
	 * original JSBML model
	 */
	Model originalModel;

	private HashSet<SBMLDocument> setOfDocuments;
	
	private HashSet<IOProgressListener> setOfIOListeners;
	
	private LinkedList<SBaseChangeListener> listOfSBaseChangeListeners;

	
	/**
	 * 
	 */
	public SqSBMLReader() {
		listOfSBaseChangeListeners = new LinkedList<SBaseChangeListener>();
		setOfDocuments = new HashSet<SBMLDocument>();
		setOfIOListeners = new HashSet<IOProgressListener>();
	}

	/**
	 * 
	 * @param model
	 * @throws Exception
	 */
	public SqSBMLReader(Object model) throws Exception {
		this();
		this.model = convertModel(model);
	}

	public void addIOProgressListener(IOProgressListener listener) {
		setOfIOListeners.add(listener);

	}

	public Model convertModel(Object model) throws IllegalArgumentException, IOException, XMLStreamException {
		if (model instanceof java.lang.String){
			// file name or XML given; create SBML Document
			org.sbml.jsbml.SBMLDocument doc = model2SBML(model.toString());
			// construct and return model
			return readModelFromSBML(doc);
		}else if(model instanceof org.sbml.jsbml.Model){
			// JSBML model given; return model
			return (org.sbml.jsbml.Model) model;
		}else if(model instanceof org.sbml.jsbml.SBMLDocument){
			// SBMLDocument given; construct and return model
			return readModelFromSBML((org.sbml.jsbml.SBMLDocument) model);
		}else{
			throw new IllegalArgumentException("model must be an instance of java.lang.String, org.sbml.jsbml.Model or org.sbml.jsbml.SBMLDocument");
		}
	}

	/**
	 * 
	 * @param model			a file name or XML String 
	 * @return				the corresponding sBML document
	 * @throws IOException
	 * @throws XMLStreamException 
	 */
	private org.sbml.jsbml.SBMLDocument model2SBML(String model)
	throws IOException, XMLStreamException {
		File file = new File((String) model);
		org.sbml.jsbml.SBMLDocument doc = null;
		if (!file.exists() || !file.isFile() || !file.canRead()) {
			// XML
			doc = SBMLReader.read((String) model);
		}else{
			// File name
			doc = SBMLReader.read(file);
		}
		setOfDocuments.add(doc);
		return doc;
	}

	/**
	 * 
	 * @param sbmldoc
	 * @param model
	 * @return
	 */
	private Model readModelFromSBML(SBMLDocument sbmldoc) {
		// set original model
		this.originalModel = sbmldoc.getModel();
		// copy the original model to working copy
		this.model = new Model(originalModel);
		// add all SBaseChangeListeners to model
		for (SBaseChangeListener listener : listOfSBaseChangeListeners)
			this.model.addChangeListener(listener);
		// return working copy
		return this.model;
	}
	
	public int getNumErrors() {
		return 0;
	}

	public Object getOriginalModel() {
		return originalModel;
	}

	public List<SBMLException> getWarnings() {
		List<SBMLException> excl = new LinkedList<SBMLException>();
		return excl;
	}

}
