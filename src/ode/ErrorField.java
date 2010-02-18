package ode;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.Dimension;
import javax.swing.JButton;
import java.awt.Rectangle;
import javax.swing.JLabel;

/**
 * @author Nadine Hassis <Nadine.hassis@gmail.com>
 * @date 01.08.2007
 */
public class ErrorField {

	private JFrame jErrorFrame = null;  //  @jve:decl-index=0:visual-constraint="214,37"
	private JPanel jContentPaneErrorFrame = null;
	private JButton jButtonOk = null;
	private JLabel jLabelErrorFrame = null;
	private String errorLabel;
	
	ErrorField(String errorLabel)
	{
		this.errorLabel = errorLabel;
		this.getJErrorFrame().setVisible(true);
	}
	
	/**
	 * This method initializes jErrorFrame	
	 * 	
	 * @return javax.swing.JFrame	
	 */
	private JFrame getJErrorFrame() {
		if (jErrorFrame == null) {
			jErrorFrame = new JFrame();
			jErrorFrame.setSize(new Dimension(300, 160));
			jErrorFrame.setTitle("Error");
			jErrorFrame.setContentPane(getJContentPaneErrorFrame());
		}
		return jErrorFrame;
	}

	/**
	 * This method initializes jContentPaneErrorFrame	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJContentPaneErrorFrame() {
		if (jContentPaneErrorFrame == null) {
			jLabelErrorFrame = new JLabel();
			jLabelErrorFrame.setBounds(new Rectangle(15, 20, 260, 40));
			jLabelErrorFrame.setText(errorLabel);
			jContentPaneErrorFrame = new JPanel();
			jContentPaneErrorFrame.setLayout(null);
			jContentPaneErrorFrame.add(getJButtonOk(), null);
			jContentPaneErrorFrame.add(jLabelErrorFrame, null);
		}
		return jContentPaneErrorFrame;
	}

	/**
	 * This method initializes jButtonOk	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButtonOk() {
		if (jButtonOk == null) {
			jButtonOk = new JButton();
			jButtonOk.setBounds(new Rectangle(100, 80, 100, 25));
			jButtonOk.setText("OK");
			jButtonOk.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					System.out.println("actionPerformed()"); 
					jErrorFrame.dispose();
				}
			});
		}
		return jButtonOk;
	}
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				ErrorField err = new ErrorField("xxx");
				//err.getJErrorFrame().setVisible(true);
			}
		});
	}
}
