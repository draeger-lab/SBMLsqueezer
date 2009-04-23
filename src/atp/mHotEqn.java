package atp;

/*****************************************************************************
*                                                                            *
*         mHotEqn  Equation Viewer Applet with MATLAB functionality          *
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
*      next           name of next equation (data destination)               *
*      vclab          name of the VCLab Plugin (default: matlab)             *
*      mEvalString    expression to be executed in MATLAB on click           *
*      mEvalMFile     M-file to be executed in MATLAB on click               *
*      mGetArray      name of a matrix in MATLAB workspace. The content of   *
*                     matrix is converted to LaTeX notation and the equation *
*                     <matrix name> = <matrix content> is displayed on click *
*                     or updated if update parameter is set (see below)      *
*      mMatrix2LaTeX  The content of the actual matrix of the (VCLabPlugin)  *
*                     is converted to LaTeX notation and substituted into    *
*                     right-hand side of the equation on click               *
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
*      border         rectangular border in color in hex notation rrggbb     *
*      rborder        rounded border in color in in hex notation rrggbb      *
*      envcolor       document color for border in hex notation rrggbb       *
*      debug          Debug mode: true (default) ... reports on console      *
*      editable       Makes the component almost editable. Parts of the      *
*                     displayed equation are selectable when editable is set *
*                     true. This is turned off by default.                   *
*      update         update interval in milliseconds for the matrix given   *
*                     by the mGetArray parameter (see above)                 *
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
*                                                                            *
******************************************************************************
**************   Release of Version 2.0  (derived from HotEqn) ***************
*                                                                            *
*        1997, 1998 Chr. Schmid, S. Mueller                                  *
*             Redesign wegen Matlab 5                                        *
* 05.11.1997  Umbenennungen der Parameter                                    *
*             alt:             neu:                                          *
*             engEvalString    mEvalString                                   *
*             eval             mEvalString                                   *
*             evalMFile        mEvalMFile                                    *
*             engGetFull       mGetArray                                     *
*             Matrix2LaTeX     mMatrix2LaTeX                                 *
* 09.11.1997  Background und Foreground Color, Border, Size                  *
* 10.11.1997  Separation into HotEqn(no MATLAB) and mHotEqn(MATLAB) version  *
* 13.11.1997  update parameter introduced                                    *
* 08.01.1998  minor documentation correction to be compatible to HotEqn      *
* 17.01.1998  Separation into HotEqn and dHotEqn version.            (2.01)  *
*             HotEqn is only for Eqn. viewing and dHotEqn includes           *
*             all public methods. The mHotEqn is now based on dHotEqn.       *
* 12.05.1998  Cast exception catched when vclab plugin not found     (2.01f) *
* 22.05.1998  migration to JDK1.1x AWT                               (2.03a) *
*                                                                            *
******************************************************************************
**************   Release of Version 3.00  ************************************
*     2001    Chr. Schmid                                                    *
* 18.01.2001  parameter correctly as in old mHotEqn and for new cHotEqn      *
*             font size selection modified of not spec. per param.           *
* 19.02.2002  Environment color parameter to adapt to document       (3.01)  * 
* 19.02.2002  Environment color parameter to adapt to document       (3.01)  * 
**************   Release of Version 4.00 *************************************
* 14.07.2003  Adapted to XPCom.                                      (4.00)  *
*****************************************************************************/

import java.awt.event.MouseEvent;

public class mHotEqn extends dHotEqn implements Runnable{ 

private Thread    updateThread;
private String    vclabS         = null;  // VCLabPlugin Name
private String    mGetArrayS     = null;
private String    mEvalMFileS    = null;
private String    mMatrix2LaTeXS = null;
private String    mEvalStringS   = null;
private int       updateInterval = 0;     // in milliseconds
private  boolean  first = true;    // after first usage set to false

// Netscape dependent classes
public  JSObject    win;
public  JSObject vclab;

//*************************  init ()  ****************************************
public void init() {
   // calls init of dHotEqn first
   super.init();
//   HotEqnC.addMouseListener(this);

   //***********  get the MATLAB related Applet Parameters   *****************
   nameS          = this.getParameter("NAME");
                    if (nameS==null) nameS="mHotEqn"; // overwrite name of dHotEqn
   vclabS         = this.getParameter("vclab");
                    if (vclabS == null)  vclabS = "matlab";  // also matlab
   mGetArrayS     = this.getParameter("mGetArray");       // mGetArray
   mEvalMFileS    = this.getParameter("mEvalMFile");      // eval given MFile  
   mMatrix2LaTeXS = this.getParameter("mMatrix2LaTeX");   // mMatrix2LaTeX
   mEvalStringS   = this.getParameter("mEvalString");     // mEvalString  
   try   {updateInterval = Integer.parseInt(this.getParameter("update"));}
   catch (NumberFormatException e) {updateInterval = 0;}

} // end init

//*************************  start ()  ****************************************
public void start() {
   // Netscape dependent classes
	try {Thread.sleep(100);} catch (InterruptedException e){}
	try {
		win = JSObject.getWindow(this);
	} 
	catch (JSException e) {
		System.out.println( nameS+" LiveConnect not available");
	}
	try {Thread.sleep(100);} catch (InterruptedException e){}
	if (vclabS.charAt(0) == '@') 
		vclabS = vclabS.substring(1);
		//System.out.println(nameS+": VCLab "+vclabS);
	first = true;  // for reconfiguring the connection to JS
	if (updateInterval > 0) {
		if (updateThread == null) {
			updateThread=new Thread(this);
			updateThread.start();
		}
	}
}

//*****************************************************************************
//* MEMBER FUNCTION: JSConnect ()
//*  - Is only called once on the first event after calling start() to
//*    ensure that all plugins have started and settled to their idle state.
//*****************************************************************************
private void JSConnect ()
{
	// get Matlab Plugin object
	try { vclab = (JSObject)win.getMember(vclabS);} catch (Exception e){}
	//System.out.println(nameS+": VCLab "+vclab);
	//System.out.println( nameS+Version+" doc "+vclab);
	if (vclab==null) 
		System.out.println(nameS+": Retry searching Matlab Plugin");
	else
		first = false;
}

public void run() {
   if (updateInterval > 0) {
     while(true) {
          domGetArray();
  	  try {Thread.sleep(updateInterval);} catch (InterruptedException e){}
     }
   }
}

public void stop() {
	win=null;
	vclab=null;
	if (updateInterval > 0) {
		if(updateThread != null) {
			updateThread.stop();
			updateThread=null;
		}
	}
}

private void domGetArray () {
	if (mGetArrayS != null) {
		//System.out.println(nameS+" : mGetArray : "+mGetArrayS);
		//win = JSObject.getWindow(this); //System.out.println("win: "+win);
		//doc = (JSObject)win.getMember("document"); //System.out.println("doc: "+doc);
		if (first) 
			JSConnect();
		if (vclab != null) {
//			setEquation (mGetArrayS,vclab.mGetArray(mGetArrayS)); // Plugin method aufrufen 
			try {
				//String s=(String)win.eval(vclabS+".mGetArray('"+mGetArrayS+"');");
				//System.out.println("mGetArray passed: "+s);
				setEquation (mGetArrayS,(String)win.eval(vclabS+".mGetArray('"+mGetArrayS+"');"));
			}
			catch (Exception e) {
				System.out.println(vclabS+".mGetArray('"+mGetArrayS+"'); "+e);
			}
		}
		// equation   = LeftSideS + " = " + RightSideS;
		// System.out.println("mGetArray passed: "+RightSideS);
   }
}

private void mess () {
   System.out.println(nameS+": Matlab Plugin methods not available");
}

//*************************  Eventhandler  *************************************

public void mousePressed(MouseEvent ev) 
// Bei einem MouseKlick werden die abh�ngigen Variablen mittels
// des InterAppletContext geholt und aktualisiert (s. dHotEqn).
// Zus�tzlich werden MATLAB Befehle bearbeitet.
{
	if (ev.getID() == MouseEvent.MOUSE_PRESSED && !ev.isMetaDown()) {
	// bei gedr�ckter (linker) Mousetaste

		// bei CONTROL Taste Breite/Hoehe ausgeben in dHotEqn, ansonsten ...
		if (!ev.isControlDown()) {
			if (first) 
				JSConnect();
			if (vclab != null) {
				///////// bei mEvalMFile("String") M-Datei berechnen /////////
				if (mEvalMFileS != null) {
					// System.out.println(nameS+" : mEvalMFile : "+mEvalMFileS);
					//vclab.mEvalMFile(mEvalMFileS);        // Plugin method aufrufen 
					try { 
						win.eval(vclabS+".mEvalMFile('"+mEvalMFileS+"');");
					} 
					catch (Exception e){
						System.out.println(vclabS+".mEvalMFile('"+mEvalMFileS+"'); "+e);
					}
					// System.out.println("mEvalMFile: passed");
				}
				///////// bei mEvalString("String") /////////
				if (mEvalStringS != null) {
					// System.out.println(nameS+" : mEvalString : "+mEvalStringS);
					vclab.setMember("command", mEvalStringS);
					try {
						win.eval(vclabS+".mEvalString("+vclabS+".command);");
					} 
					catch (Exception e) {
						System.out.println(vclabS+".mEvalString('"+mEvalStringS+"'); "+e);
					}
				}
				///////// bei mGetArray("String") Matrix von Matlab -> Plugin /////////
				domGetArray();
				///////// bei mMatrix2LaTeX() holt aktuelle Matrix (Plugin -> Applet) /////////
				///////// und setzt sie gleich der rechten Seite                     /////////
				if (mMatrix2LaTeXS != null) {
					// System.out.println(nameS+" : mMatrix2LaTeX()");
					try {
						setRightSide((String)win.eval(vclabS+".mMatrix2LaTeX();"));
					} 
					catch (Exception e){
						System.out.println(vclabS+".mGetArray('"+mMatrix2LaTeXS+"'); "+e);
					}
					//System.out.println("mMatrix2LaTeX passed: "+s);
					//setRightSide((String)vclab.mMatrix2LaTeX()); 
				}
			}
		} // end !ev.isControlDown()
	} // end ev.getID
	super.mousePressed(ev); // dHotEqn
} // end mousePressed

} // end ********** class mHotEqn ***************************************

