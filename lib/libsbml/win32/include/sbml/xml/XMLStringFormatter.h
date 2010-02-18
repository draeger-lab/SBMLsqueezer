/**
 * \file    XMLStringFormatter.h
 * \brief   Formats XML Strings from SAX2 parse events
 * \author  Ben Bornstein
 * 
 * $Id: XMLStringFormatter.h,v 1.4 2005/04/20 19:04:07 bbornstein Exp $
 * $Source: /cvsroot/sbml/libsbml/src/xml/XMLStringFormatter.h,v $
 */
/* Copyright 2002 California Institute of Technology and
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


#ifndef XMLStringFormatter_h
#define XMLStringFormatter_h


#include "common.h"


#ifdef __cplusplus


#ifdef USE_EXPAT
#  include <expat.h>
#  include "ExpatAttributes.h"
#  include "ExpatFormatter.h"
#else
#  include <xercesc/sax2/Attributes.hpp>
#  include <xercesc/framework/XMLFormatter.hpp>
#  include <xercesc/framework/MemBufFormatTarget.hpp>
#  include <xercesc/util/XMLUniDefs.hpp>
   using xercesc::Attributes;
   using xercesc::XMLFormatter;
   using xercesc::MemBufFormatTarget;
#endif  /* USE_EXPAT */


/**
 * The XMLStringFormatter is designed to receive SAX-like callback events
 * and reconstruct the XML string that produced those events.  This is
 * useful if you want your SAX Handler to leave certain chunks of XML
 * untouched.  To do so, simply delegate all SAX events (tags) you are not
 * interested in to this formatter.  The corresponding XML will be
 * reconstructed and can be retrieved with the getString() method (and its
 * length with getLength()).
 *
 * For instance, in the SBML parser, this formatter is used to store the
 * original XML contents of <notes> (which can contain XHTML) and
 * <annotations> elements.
 *
 * A formatter can be reset() (emptied) so that it can be reused to process
 * another set of XML tags.  The underlying character encoding is
 * configurable.
 *
 * A note on SAX2 qnames vs. localnames:
 *
 * They are identical if no namespace prefix is specified.  For example,
 * suppose:
 *
 *   <prefix:myelement .../>
 *
 * In this case, localname will be 'myelement' and qname will be
 * 'prefix:myelement'.  If prefix were not present, qname would also be
 * 'myelement'.  Since the goal is of XMLStringFormatter is to reproduce
 * the input as close as possible, qname is used.
 *
 * See Xerces-C SAX2 API for a description of method parameters.
*/
class XMLStringFormatter
{

public:

  /**
   * Creates a new XMLStringFormatter
   */
  XMLStringFormatter (const char* encoding);

  /**
   * Destroys this XMLStringFormatter
   */
  ~XMLStringFormatter ();


  /**
   * Formats a string of the form:
   *   '<qname qname:attrs1="value1" ... qname:attrsN="valueN">'.
   */
  void startElement (const XMLCh* const qname, const Attributes& attrs);

  /**
   * Formats a string of the form '</qname>'.
   */
  void endElement (const XMLCh* const qname);

  /**
   * Formats a string composed of the chars.
   */
  void characters (const XMLCh* const chars, unsigned int length);

  /**
   * Formats a string composed of whitespace chars.
   */
  void ignorableWhitespace (const XMLCh* const chars, unsigned int length);

  /**
   * Resets (empties) the internal character buffer.  Use this method when
   * you want the formatter to begin creating a new XML string.
   */
  void reset ();

  /**
   * @return the length of the current XML string.
   */
  unsigned int getLength () const;

  /**
   * @return a the underlying formatted XML string.  The caller does not
   * own this string and therefore is not allowed to modify it.
   */
  const char* getString ();


private:

  XMLFormatter*        mFormatter;
  MemBufFormatTarget*  mTarget;
};


#endif  /* __cplusplus */
#endif  /* XMLStringFormatter_h */
