package ode;

import java.awt.*;
import java.io.File;
import javax.swing.*;

/**
 * @author Nadine Hassis <Nadine.hassis@gmail.com>
 * @date 01.08.2007
 */
class ViewComponent extends JComponent
{
private Image image;

protected void paintComponent( Graphics g )
{
if ( image != null )
g.drawImage( image, 0, 0, this );
}

public void setImage( File file )
{
image = Toolkit.getDefaultToolkit().getImage( file.getAbsolutePath() );
if ( image != null )
repaint();
}
}


