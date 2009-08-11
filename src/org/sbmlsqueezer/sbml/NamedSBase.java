package org.sbmlsqueezer.sbml;

public abstract class NamedSBase extends SBase {

	String id;
	String name;

	public NamedSBase() {
	}

	public NamedSBase(String id) {
		super();
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
		stateChanged();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
		stateChanged();
	}

}
