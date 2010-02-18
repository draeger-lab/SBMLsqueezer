/**
 * \file    translateMath.cpp
 * \brief   Translates infix formulas into MathML and vice-versa
 * \author  Sarah Keating and Ben Bornstein
 *
 * $Id: translateMath.cpp,v 1.7 2005/09/10 12:50:04 sarahkeating Exp $
 * $Source: /cvsroot/sbml/libsbml/examples/c++/translateMath.cpp,v $
 */
/* Copyright 2003-2004 California Institute of Technology and
 * Japan Science and Technology Corporation.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and the
 * California Institute of Technology and Japan Science and Technology
 * Corporation have no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall the
 * California Institute of Technology or the Japan Science and Technology
 * Corporation be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if the
 * California Institute of Technology and/or Japan Science and Technology
 * Corporation have been advised of the possibility of such damage.  See
 * the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 *
 * The original code contained here was initially developed by:
 *
 *     Sarah Keating
 *
 *     The SBML Team
 *     STRI
 *     University of Hertfordshire
 *     Hatfield, UK
 *
 *     http://sbml.org
 *     mailto:sbml-team@caltech.edu
 *
 * Contributor(s):
 */


#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "sbml/util/util.h"
#include "sbml/math/FormulaFormatter.h"
#include "sbml/math/FormulaParser.h"
#include "sbml/math/MathMLReader.h"
#include "sbml/math/MathMLWriter.h"
#include "sbml/util/StringBuffer.h"

#define BUFFER_SIZE 1024

#include <iostream>


using namespace std;


char *translateInfix  (const char *formula);
char *translateMathML (const char *xml);


int
main (int argc, char *argv[])
{
  char            line[BUFFER_SIZE];
  char*           trimmed;
  char*           result;
  char*           str;
  unsigned int    len;
  StringBuffer_t* sb = StringBuffer_create(1024);


  cout << endl 
       << "This program translates infix formulas into MathML and" << endl
       << "vice-versa.  Enter or return on an empty line triggers" << endl
       << "translation. Ctrl-C quits" << endl
       << endl;

  while (1)
  {
    cout << "Enter infix formula or MathML expression (Ctrl-C to quit):"
         << endl << endl;
    cout << "> " ;

    cin.getline(line, BUFFER_SIZE, '\n');

    while (line != NULL)
    {
      trimmed = util_trim(line);
      len     = strlen(trimmed);

      if (len > 0)
      {
        StringBuffer_append    (sb, trimmed);
        StringBuffer_appendChar(sb, '\n');
      }
      else
      {
        str    = StringBuffer_getBuffer(sb);
        result = (str[0] == '<') ? translateMathML(str) : translateInfix(str);

        cout << "Result:" << endl << endl << result << endl << endl << endl;

        StringBuffer_reset(sb);
        break;
      }

      cin.getline(line, BUFFER_SIZE, '\n');
    }
  }

  StringBuffer_free(sb);
  return 0;
}


/**
 * Translates the given infix formula into MathML.
 *
 * @return the MathML as a string.  The caller owns the memory and is
 * responsible for freeing it.
 */
char *
translateInfix (const char *formula)
{
  char *result;

  MathMLDocument_t *d    = MathMLDocument_create();
  ASTNode_t        *math = SBML_parseFormula(formula);


  MathMLDocument_setMath(d, math);
  result = writeMathMLToString(d);

  MathMLDocument_free(d);

  return result;
}


/**
 * Translates the given MathML into an infix formula.  The MathML must
 * contain no leading whitespace, but an XML header is optional.
 *
 * @return the infix formula as a string.  The caller owns the memory and
 * is responsible for freeing it.
 */
char *
translateMathML (const char *xml)
{
  char             *result;
  MathMLDocument_t *d;
  StringBuffer_t   *sb;


  /**
   * Prepend an XML header if not already present.
   */
  if (xml[0] == '<' && xml[1] != '?')
  {
    sb = StringBuffer_create(1024);

    StringBuffer_append(sb, "<?xml version='1.0' encoding='ascii'?>\n");
    StringBuffer_append(sb, xml);

    xml = StringBuffer_getBuffer(sb);

  }

  d      = readMathMLFromString(xml);
  result = SBML_formulaToString( MathMLDocument_getMath(d) );

  MathMLDocument_free(d);
  return result;
}
