package sabiork.wizard.gui;

import java.awt.Dimension;
import javax.swing.JComponent;
import javax.swing.border.EtchedBorder;

/**
 * A class for drawing an etched horizontal line.
 * 
 * @author Matthias Rall
 * 
 */
@SuppressWarnings("serial")
public class JComponentEtchedLine extends JComponent {

	public JComponentEtchedLine() {
		setBorder(new EtchedBorder(EtchedBorder.LOWERED));
		setPreferredSize(new Dimension(1, 2));
		setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
	}

}
