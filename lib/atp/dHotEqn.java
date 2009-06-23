package atp;

/*****************************************************************************
*                                                                            *
*         dHotEqn Equation Viewer Applet with dynamic features               *
*                                                                            *
******************************************************************************
* Java Applet to view mathematical Equations provided in the LaTeX language  *
******************************************************************************

Copyright 2006 Stefan M�ller and Christian Schmid

This file is part of the HotEqn package.

    HotEqn is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; 
    HotEqn is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

******************************************************************************
*                                                                            *
*  Parameter:         Value:                                                 *
*      name           name tag of the applet (necessary for data exchange)   *
*      prev0          name of previous equation (data source 0)              *
*      prev1          name of previous equation (data source 1)              *
*      prev2          name of previous equation (data source 2)              *
*      prevXXX        name of previous equation (data source XXX)            *
*      Equation       mathematical expression (equation) in LaTeX notation   *
*      LeftSide       left-hand side of the equation in LaTeX notation       *
*      RightSide      right-hand side of the equation in LaTeX notation      *
*                     <equation> = <LeftSide> +" = "+ <RightSide>            *
*      Fontname       name of the font                                       *
*      Fontsize1      Size of the normal font                                *
*      Fontsize2      Size of the 1. recursive font (smaller)                *
*      Fontsize3      Size of the 2. recursive font (more smaller)           *
*      Fontsize4      Size of the 3. recursive font (more smaller)           *
*      bgcolor        background color in hex notation rrggbb                *
*      fgcolor        foreground color in hex notation rrggbb                *
*      border         rectangular border in color in in hex notation rrggbb  *
*      rborder        rounded border in color in hex notation rrggbb         *
*      envcolor       document color for border in hex notation rrggbb       *
*      debug          Debug mode: true (default) ... reports on console      *
*      editable       Makes the component almost editable. Parts of the      *
*                     displayed equation are selectable when editable is set *
*                     true. This is turned off by default.                   *
*                                                                            *
******************************************************************************
*                                                                            *
* Public Methods:                                                            *
*      setEquation(String Equation)     sets expression (equation)           *
*      setEquation(String LeftSideS, String RightSideS) sets both sides      *
*      setLeftSide(String LeftSideS)    sets left-hand side                  *
*      setRightSide(String RightSideS)  sets right-hand side                 *
*      String getLeftSide()             gets left-hand side                  *
*      String getRightSide()            gets right-hand side                 *
*      int getWidth()                   gets graphics window width           *
*      int getHeight()                  gets graphics window height          *
*      setBGColor(String bgcolor)       Sets the background color            *
*      setFGColor(String fgcolor)       Sets the foreground color            *
*      Dimension getSizeof(String equation) Returns the size required to     *
*                                       display the given equation.          *
*                                                                            *
******************************************************************************
*    (c) 1998, 1997 Chr. Schmid, S. Mueller                                  *
*                                                                            *
* 16.04.1998 Redesign wegen JDK 1.15 und Vererbung                   (2.02)  *
* 22.05.1998 migration to JDK1.1x AWT                                (2.03a) *
*                                                                            *
******************************************************************************
**************   Release of Version 3.00  ************************************
* (c) 2001    Chr. Schmid                                                    *
* 18.01.2001  parameter correctly as in old dHotEqn and for new cHotEqn      *
*             font size selection modified of not spec. per param.           *
* 19.02.2002  Environment color parameter to adapt to document        (3.01) * 
* 23.03.2002  New method getSizeof to determine size of equation      (3.11) * 
**************   Release of Version 4.00 *************************************
* 14.07.2003  Adapted to XPCom. Same as 3.11,only mHotEqn affected    (4.00) *
*****************************************************************************/

import java.applet.Applet;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class dHotEqn extends HotEqn implements MouseListener {


//*************************  init ()  ****************************************
public void init() {
   super.init();     // calls init of HotEqn first
   HotEqnC.addMouseListener(this);

   //***********  get the dHotEqn related Applet Parameters  *****************
   nameS          = this.getParameter("NAME");
                    if (nameS==null) nameS="dHotEqn"; // overwrite name of HotEqn

   String dummyS = this.getParameter("editable");
   if (dummyS!=null && dummyS.equals("true")) HotEqnC.setEditable(true);

} // end init

//*************************  Public Methods ***********************************

public void setBGColor(String bgcolor) {
   try   { 
      Color color = HotEqnC.getEnvColor();
      HotEqnC.setBackground( new Color(Integer.parseInt(bgcolor,16)) );
      HotEqnC.setEnvColor(color);
   }
   catch (NumberFormatException e){ }
}
public void setFGColor(String fgcolor) {
   try   { HotEqnC.setForeground( new Color(Integer.parseInt(fgcolor,16)) );}
   catch (NumberFormatException e){}
}

public synchronized void setEquation(String equation) {
    HotEqnC.setEquation(equation);
}

public synchronized void setEquation(String SL, String SR) {
    if (SL!=null) { 
       LeftSideS = SL;
       if (SR!=null) { 
          RightSideS = SR;
          equation   = LeftSideS + " = " + RightSideS;
       }   
    }
    HotEqnC.setEquation(equation);
}

public  int getWidth()  { return HotEqnC.getPreferredSize().width;  }
public  int getHeight() { return HotEqnC.getPreferredSize().height; }

public Dimension getSizeof(String equation) { return HotEqnC.getSizeof(equation); }

public synchronized String getLeftSide()  {return LeftSideS;}
public synchronized String getRightSide() {return RightSideS;}

public synchronized void setLeftSide(String S) {
     if (S!=null) { 
        LeftSideS = S;
        equation  = LeftSideS + " = " + RightSideS;
     }
     HotEqnC.setEquation(equation);
} // end setLeftSide

public synchronized void setRightSide(String S) {
     if (S!=null) { 
        RightSideS = S;
        equation   = LeftSideS + " = " + RightSideS;
     }
     HotEqnC.setEquation(equation);
} // end setRightSide

//*************************  Eventhandler  *************************************

public void mouseReleased(MouseEvent ev) {}
public void mouseEntered(MouseEvent ev)  {}
public void mouseExited(MouseEvent ev)   {}
public void mouseClicked(MouseEvent ev)  {}

public void mousePressed(MouseEvent ev) 
// Bei einem MouseKlick werden die abh�ngigen Variablen mittels
// des InterAppletContext geholt und aktualisiert.
{
  //if (ev.getID() == MouseEvent.MOUSE_PRESSED && !ev.isMetaDown()) {
  if (!ev.isMetaDown()) {
  // bei gedr�ckter (linker) Mousetaste

     // bei CONTROL Taste Breite/Hoehe ausgeben  
     if (!ev.isControlDown()) {
        // Terme einsetzen und ersetzen
        int zz = 0;
        while (this.getParameter("prev"+zz)!=null) {
           Applet prevApplet = getAppletContext().getApplet(this.getParameter("prev"+zz));
           if (prevApplet != null) { 
              dHotEqn prevHotEqnApplet = (dHotEqn) prevApplet;
              String prevLeftSideS    = prevHotEqnApplet.getLeftSide();
              String prevRightSideS   = prevHotEqnApplet.getRightSide();
              prevHotEqnApplet = null;

              // In "equation" den Term "prevLeftSideS" durch "prevRightSideS" ersetzen.
              for (int z = 0; z < equation.length()-prevLeftSideS.length(); z++)
              {
                 if (equation.substring(z,z+prevLeftSideS.length()).equals(prevLeftSideS)){
                    equation = equation.substring(0,z)
                             + prevRightSideS
                             + equation.substring(z+prevLeftSideS.length(),equation.length());
                    HotEqnC.setEquation(equation);
                    break;
                 }
              }
           }
           else {
              HotEqnC.printStatus(this.getParameter("prev"+zz)+" not found !");
           }
           zz++;
           prevApplet = null;
        } // end while
     }   
  } 
  else {
     equation   = this.getParameter("Equation");    // Gleichung im TeX Format
     if (equation == null) equation=" ";   
     if (!LeftSideS.equals(" ") && !RightSideS.equals(" ")) equation = LeftSideS+" = "+RightSideS;
     HotEqnC.setEquation(equation);
  } // end ev.modifiers
  super.processMouseEvent(ev);
} // end mousePressed

} // end ********** class dHotEqn ****************************************




