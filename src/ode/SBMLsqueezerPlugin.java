package ode;

import jp.sbi.celldesigner.plugin.*;

/**
 * @author Akira Funahashi <funa@celldesigner.org>
 *
 */
public class SBMLsqueezerPlugin extends CellDesignerPlugin {
	public PluginMenu menu;
	public PluginMenuItem menuItem1;

	public SBMLsqueezerPlugin() {
		menu = new PluginMenu("SBMLsqueezer");
		SBMLsqueezerPluginAction action = new SBMLsqueezerPluginAction(this);
		menuItem1 = new PluginMenuItem("Open SBMLsqueezer", action);
		menu.add(menuItem1);
		addCellDesignerPluginMenu(menu);
	}

	public void SBaseAdded(PluginSBase arg0) {
		// TODO Auto-generated method stub
	}

	public void SBaseChanged(PluginSBase arg0) {
		// TODO Auto-generated method stub
	}

	public void SBaseDeleted(PluginSBase arg0) {
		// TODO Auto-generated method stub
	}

	public void addPluginMenu() {
		// TODO Auto-generated method stub
	}

	public void modelClosed(PluginSBase arg0) {
		// TODO Auto-generated method stub
	}

	public void modelOpened(PluginSBase arg0) {
		// TODO Auto-generated method stub
	}

	public void modelSelectChanged(PluginSBase arg0) {
		// TODO Auto-generated method stub
	}
}
