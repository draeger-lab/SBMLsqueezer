package ode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import jp.sbi.celldesigner.plugin.*;

/**
 * @author Nadine Hassis <Nadine.hassis@gmail.com>
 * @date 01.08.2007
 */
public abstract class KineticLaw
{
	protected PluginModel model;
	protected int reactionnum;
	protected int reaction2;
	protected HashMap<String,String> idAndName = new HashMap<String,String>();
	protected String formeltxt = "";
	protected String formeltex = "";
	protected String formeltexName = "";
	protected List<String> paraList = new ArrayList<String>();
	protected List<String> modActi = new ArrayList<String>();
	protected List<String> modCat = new ArrayList<String>();
	protected List<String> modInhib = new ArrayList<String>();
	protected List<String> modE = new ArrayList<String>();
	protected String activator;
	protected String inhibitor;
	protected boolean reversibility;
	
	public abstract void sMAK();
	public abstract void sMAK(int catNumber);
	
	public List<String> getParameters()
	{
		return paraList;
	}
	
	public String getKinetictxt()
	{
		return formeltxt;
	}
	
	public String getKinetictex()
	{
		return formeltex;
	}
}

