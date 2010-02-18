package ode;

import java.awt.event.ActionEvent;
import javax.swing.JMenuItem;
import jp.sbi.celldesigner.plugin.*;

/**
 * @author Akira Funahashi <funa@celldesigner.org>
 *
 */
public class SBMLsqueezerPluginAction extends PluginAction 
{
	private SBMLsqueezerPlugin plugin;
	
	public SBMLsqueezerPluginAction(SBMLsqueezerPlugin plugin) 
	{
		this.plugin = plugin;
	}
 	
	public void myActionPerformed(ActionEvent e) 
	{
		System.err.println("Version 0.1.0");
		if (((JMenuItem)e.getSource()).getText() == plugin.menuItem1.getText()) 
		{
	//		TextfieldOutput test = new TextfieldOutput("ODE-Maker Output", plugin);
		//    test.setVisible(true);
		    
			SBMLsqueezerUI application = new SBMLsqueezerUI(plugin);
			application.getJFrameMainFrame().setVisible(true);
			
		
		}
	}
	
}
		    
