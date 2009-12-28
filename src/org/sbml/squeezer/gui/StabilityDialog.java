package org.sbml.squeezer.gui;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.sbml.squeezer.io.SBMLio;

/**
 * A dialog window to start a stability analysis on the currently selected
 * model.
 * 
 * @author <a href="mailto:a.doerr@uni-tuebingen.de">Alexander D&ouml;rr</a>
 * @date 2009-12-18
 * @since 1.3
 */

public class StabilityDialog extends JDialog implements ActionListener {

	public static final boolean APPROVE_OPTION = true;
	public static final boolean CANCEL_OPTION = false;
	private static final String APPLY = "Apply";
	private static final String CANCEL = "Cancel";

	private JButton apply;
	private JButton cancel;
	private StabilityPanel stabilityPanel;
	private Properties settings;
	private SBMLio sbmlIO;
	private boolean exitStatus;

	/**
	 * Generated serial version id.
	 */
	private static final long serialVersionUID = 1L;

	public StabilityDialog(Frame owner) {
		super(owner, "Stability");
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals(CANCEL)) {
			dispose();

		} else if (e.getActionCommand().equals(APPLY)) {
			sbmlIO.getSelectedModel();
		}

	}

	/**
	 * Initializes this dialog.
	 */
	private void init() {
		stabilityPanel = new StabilityPanel(this.settings);
		getContentPane().add(stabilityPanel, BorderLayout.CENTER);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		JPanel p = new JPanel();

		cancel = new JButton("Cancel");
		cancel.addActionListener(this);
		cancel.setActionCommand(CANCEL);
		cancel.setSize(cancel.getSize());
		apply = new JButton("Apply");
		apply.setSize(cancel.getSize());
		apply.addActionListener(this);
		apply.setActionCommand(APPLY);
		apply.setEnabled(true);
		p.add(cancel);
		p.add(apply);
		add(p, BorderLayout.SOUTH);
	}

	/**
	 * 
	 * @return
	 */
	public boolean showStabilityDialog(Properties settings, SBMLio sbmlIO) {
		this.settings = settings;
		this.sbmlIO = sbmlIO;
		this.exitStatus = CANCEL_OPTION;
		init();
		pack();
		setLocationRelativeTo(getOwner());
		setResizable(false);
		setModal(true);
		setVisible(true);
		return exitStatus;
	}

}
