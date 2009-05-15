package org.sbmlsqueezer.gui;

import java.io.IOException;

import javax.swing.JOptionPane;

/**
 * 
 * @author Hannes Borch <hannes.borch@googlemail.com>
 * 
 */

public class UpdateMessageThread extends Thread {

	private SBMLsqueezerPlugin plugin;

	public UpdateMessageThread(SBMLsqueezerPlugin plugin) {
		super();
		this.plugin = plugin;
	}

	@Override
	public void run() {
		if (!plugin.getUpdateChecked())
			try {
				UpdateMessage.checkForUpdate(plugin);
			} catch (IOException exc) {
				JOptionPane.showMessageDialog(null, exc.getMessage());
				exc.printStackTrace();
			}
	}
}
