package atp;

/*****************************************************************************
*                                                                            *
*                   HotEqn Equation Viewer Basic Applet                      *
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
*      halign         horizontal alignment left|center|right (default: left) *
*      valign         vertical   alignment top|middle|bottom (default: top)  *
*      debug          Debug mode: true (default) ... reports on console      *
*                                                                            *
******************************************************************************
*                                                                            *
* Public Methods:     none (see cHotEqn and dHotEqn)                         *
*                                                                            *
******************************************************************************
**************   Release of Version 2.02  ************************************
*        1998 S. Mueller, Chr. Schmid                                        *
*             Redesign because of JDK1.1.x, Lightweight components and beans *
* 12.04.1998  basic framework                                        (2.02)  *
* 02.05.1998  parameter and method fixes                             (2.02a) *
* 28.05.1998  new parameter: debug, editable                         (2.02b) *
* 23.07.1998  bug found: debug,editable parameters resulted in error         *
*             if they were NOT used as parameters                    (2.02c) *
**************   Release of Version 3.00  ************************************
*     2001    Chr. Schmid                                                    *
* 18.01.2001  parameter correctly as in old HotEqn and for new cHotEqn       *
*             font size selection modified of not spec. per param.           *
* 19.02.2002  Environment color parameter to adapt to document       (3.01)  * 
* 27.10.2002  Package atp introduced                                 (3.12)  * 
**************   Release of Version 4.00 *************************************
* 14.07.2003  Adapted to XPCom. Same as 3.12,only mHotEqn affected   (4.00)  *
*****************************************************************************/

//***!!!! ToDo: Nachrichten z.B.: loading alpha  per action event �bermitteln
//***!!!!       und darstellen

import java.applet.Applet;
import java.awt.Color;
import java.awt.GridLayout;

public class HotEqn extends Applet {
private static final String  VERSION = "HotEqn V 4.00 ";

public  String    nameS          = null;
public  String    equation       = null;
private String    Fontname       = null;
public  String    LeftSideS      = null;
public  String    RightSideS     = null;
public  cHotEqn   HotEqnC;

//*************************  init ()  ****************************************
public void init() {

   nameS          = this.getParameter("NAME");
                    if (nameS==null) nameS="HotEqn";
  
   equation       = this.getParameter("Equation");        
                    if (equation == null) equation=" "; 

   System.out.println(VERSION+nameS);

   HotEqnC = new cHotEqn(equation, this, nameS); // (Applet) three arguments

   this.setLayout(new GridLayout(1,1));
   this.add(HotEqnC);

   LeftSideS      = this.getParameter("LeftSide");        // Linke Seite
                    if (LeftSideS == null) LeftSideS=" ";  
   RightSideS     = this.getParameter("RightSide");       // Rechte Seite
                    if (RightSideS == null) RightSideS=" ";  
                    if (!LeftSideS.equals(" ") && !RightSideS.equals(" ")) {
                        equation = LeftSideS + " = " + RightSideS;
                        HotEqnC.setEquation(equation); }

   Fontname       = this.getParameter("Fontname");        
   if (Fontname != null) HotEqnC.setFontname(Fontname);

   int    gsize1  = 14;
   int    gsize2  = 12;
   int    gsize3  = 10;
   int    gsize4  =  8;

   try   { gsize1 = Integer.parseInt(this.getParameter("Fontsize1"));}
   catch (NumberFormatException e){ gsize1 = 14; }
   try   { gsize2 = Integer.parseInt(this.getParameter("Fontsize2"));}
   catch (NumberFormatException e){ gsize2 = nextgsize(gsize1); }
   try   { gsize3 = Integer.parseInt(this.getParameter("Fontsize3"));}
   catch (NumberFormatException e){ gsize3 = nextgsize(gsize2); }
   try   { gsize4 = Integer.parseInt(this.getParameter("Fontsize4"));}
   catch (NumberFormatException e){ gsize4 = nextgsize(gsize3); }
   HotEqnC.setFontsizes(gsize1, gsize2, gsize3, gsize4); 

   try { Color color = new Color(Integer.parseInt(this.getParameter("bgcolor"),16));
           if (color!=null) HotEqnC.setBackground(color);}
   catch (NumberFormatException e){ }

   try { Color color = new Color(Integer.parseInt(this.getParameter("fgcolor"),16));
           if (color!=null) HotEqnC.setForeground(color);}
   catch (NumberFormatException e){ }

   try { Color color = new Color(Integer.parseInt(this.getParameter("border"),16));
           if (color != null) { HotEqnC.setBorderColor(color);
                                HotEqnC.setBorder(true);
                                HotEqnC.setRoundRectBorder(false);} }
   catch (NumberFormatException e){ }

   try { Color color = new Color(Integer.parseInt(this.getParameter("rborder"),16));
           if (color != null) { HotEqnC.setBorderColor(color);
                                HotEqnC.setBorder(true);
                                HotEqnC.setRoundRectBorder(true);} }
   catch (NumberFormatException e){ }

   try { Color color = new Color(Integer.parseInt(this.getParameter("envcolor"),16));
           if (color != null) { HotEqnC.setEnvColor(color); }}
   catch (NumberFormatException e){ }

   String halign = this.getParameter("halign");
      if (halign!=null) HotEqnC.setHAlign(halign);

   String valign = this.getParameter("valign");   
      if (valign!=null) HotEqnC.setVAlign(valign);

   String dummyS = this.getParameter("debug");
   if (dummyS!=null && dummyS.equals("true"))    HotEqnC.setDebug(true);

   HotEqnC.setEditable(false);

} // end init

private int nextgsize(int gsize) {
   int GreekFontSizes[]    = { 8,10,12,14,18 }; // vorhandene GreekFonts
   int i = GreekFontSizes.length-1;
   while ( (i > 0) & (GreekFontSizes[i] >= gsize) ) i--;
   return GreekFontSizes[i];
}

} // end class HotEqn 


