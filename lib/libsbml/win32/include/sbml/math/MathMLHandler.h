/**
 * \file    MathMLHandler.h
 * \brief   MathML SAX2 Handler
 * \author  Ben Bornstein
 * 
 * $Id: MathMLHandler.h,v 1.5 2005/08/22 17:46:02 shoops Exp $
 * $Source: /cvsroot/sbml/libsbml/src/math/MathMLHandler.h,v $
 */
/* Copyright 2003 California Institute of Technology and
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
 *     Ben Bornstein
 *     The Systems Biology Markup Language Development Group
 *     ERATO Kitano Symbiotic Systems Project
 *     Control and Dynamical Systems, MC 107-81
 *     California Institute of Technology
 *     Pasadena, CA, 91125, USA
 *
 *     http://www.cds.caltech.edu/erato
 *     mailto:sbml-team@caltech.edu
 *
 * Contributor(s):
 *   Stefan Hoops
 */


#ifndef MathMLHandler_h
#define MathMLHandler_h


#ifdef __cplusplus

#include <sstream>

#ifdef USE_EXPAT
#  include <expat.h>
#  include "../xml/Expat.h"
#  include "../xml/ExpatAttributes.h"
#  include "../xml/ExpatToXerces.h"
#else
#  include <xercesc/sax/Locator.hpp>
#  include <xercesc/sax2/DefaultHandler.hpp>
   using xercesc::Attributes;
   using xercesc::DefaultHandler;
   using xercesc::Locator;
#endif  /* USE_EXPAT */

#include "../util/Stack.h"
#include "MathMLTagCodes.h"


class ASTNode;
class MathMLDocument;


/**
 * MathMLHandler
 *
 * This XML document handler is responsible for constructing an an Abstract
 * Syntax Tree from SAX2 events deliverd by a SAX2XMLReader.
 */
#ifdef USE_EXPAT
class MathMLHandler : public ExpatToXerces
#else
class MathMLHandler : public DefaultHandler
#endif  /* USE_EXPAT */
{

public:

  /**
   * Ctor
   *
   * Creates a new MathMLHandler.  The given MathMLDocument should be empty
   * and will be populated as the document is parsed.
   */
  MathMLHandler (MathMLDocument* d);


  void startElement
  (
    const XMLCh* const  uri,
    const XMLCh* const  localname,
    const XMLCh* const  qname,
    const Attributes&   attrs
  );

  void endElement
  (
    const XMLCh* const  uri,
    const XMLCh* const  localname,
    const XMLCh* const  qname
  );

  void characters (const XMLCh* const chars, const unsigned int length);


  void startDocument ();
  void endDocument   ();

  void setDocumentLocator (const Locator *const locator);


private:

  void checkFunctionArgs (ASTNode*  node);

  MathMLTagCode_t getTagCode (const XMLCh* uri, const XMLCh* localname);

  void parseCN (const char* str);
  void parseCI (const char* str);

  void reduceExpression ();

  void setTypeCN (ASTNode* node, const Attributes& a);
  void setTypeCS (ASTNode* node, const Attributes& a);


  MathMLDocument* fDocument;
  Stack_t*        fObjStack;
  Stack_t*        fTagStack;

  const Locator* fLocator;

  bool fSeenSep;

  std::ostringstream mCharacterData;
};


#endif  /* __cplusplus */
#endif  /* MathMLHandler_h */
