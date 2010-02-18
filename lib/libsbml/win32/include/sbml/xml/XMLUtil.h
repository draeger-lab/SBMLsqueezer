/**
 * \file    XMLUtil.h
 * \brief   Utility functions to help manipulate XML data
 * \author  Ben Bornstein
 * 
 * $Id: XMLUtil.h,v 1.6 2005/05/01 05:32:57 bbornstein Exp $
 * $Source: /cvsroot/sbml/libsbml/src/xml/XMLUtil.h,v $
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


#ifndef XMLUtil_h
#define XMLUtil_h


#ifdef __cplusplus


#include <string>
#include "common.h"


#ifdef USE_EXPAT
#  include "ExpatAttributes.h"
#  include "ExpatFormatter.h"
#else
#  include <xercesc/framework/XMLFormatter.hpp>
#  include <xercesc/sax2/Attributes.hpp>
   using xercesc::XMLFormatter;
   using xercesc::XMLFormatTarget;
   using xercesc::Attributes;
#endif  /* USE_EXPAT */


class XMLUtil
{
public:

  /**
   * Creates a new XMLStringFormatter and returns a pointer to it.  This
   * method hides the constructor API differences between Xerces 2.3.0 and
   * earlier versions.
   */
  static XMLFormatter* createXMLFormatter
  (
       const char*      outEncoding
     , XMLFormatTarget* target
  );

  /**
   * Searches for an attribute with name.  If found, and the corresponding
   * value can be interpreted as a boolean, it is stored through the last
   * pointer argument, analogous to C's scanf.  Otherwise, the last
   * argument is left unassigned.
   *
   * Valid booleans according to the W3C Schema recommendation are "true",
   * "false", "1", and "0" (case-insensitive).
   *
   * Returns true if the last argument was assigned, false otherwise.
   */
  static bool scanAttr
  (
      const Attributes& attrs
    , const XMLCh*      name
    , bool*             value
   );

  /**
   * Searches for an attribute with name.  If found, and the corresponding
   * value can be interpreted as an integer, it is stored through the last
   * pointer argument, analogous to C's scanf.  Otherwise, the last
   * argument is left unassigned.
   *
   * Returns true if the last argument was assigned, false otherwise.
   */
  static bool scanAttr
  (
     const Attributes& attrs
   , const XMLCh*      name
   , int*              value
  );

  /**
   * Searches for an attribute with name.  If found, and the corresponding
   * value can be interpreted as a positive integer, it is stored through
   * the last pointer argument, analogous to C's scanf.  Otherwise, the
   * last argument is left unassigned.
   *
   * Returns true if the last argument was assigned, false otherwise.
   */
  static bool scanAttr
  (
     const Attributes& attrs
   , const XMLCh*      name
   , unsigned int*     value
  );

  /**
   * Searches for an attribute with name.  If found, and the corresponding
   * value can be interpreted as a double, it is stored in the location
   * pointed to by value.  Otherwise, value is left unassigned.
   *
   * Returns true if value was assigned, false otherwise.
   */
  static bool scanAttr
  (
     const Attributes& attrs
   , const XMLCh*      name
   , double*           value
  );

  /**
   * Stores the string value of the attribute at the given index.  The
   * string is stored through the last reference argument, analogous to C's
   * scanf.  If index is out of range, the last argument is left
   * unassigned.
   *
   * Returns true if the last argument was assigned, false otherwise.
   */
  static bool scanAttr
  ( 
     const Attributes&  attrs
   , const unsigned int index
   , std::string&       value
  );

  /**
   * Searches for an attribute with name.  If found, the corresponding
   * string is stored through the last reference argument, analogous to C's
   * scanf.  Otherwise, the last argument is left unassigned.
   *
   * Returns true if the last argument was assigned, false otherwise.
   */
  static bool scanAttr
  (
     const Attributes& attrs
   , const XMLCh*      name
   , std::string&      value
  );

  /**
   * Stores the string value of the attribute at the given index.  The
   * string is stored through the last pointer argument, analogous to C's
   * scanf.  If index is out of range, the last argument is left
   * unassigned.
   *
   * The space for the string is allocated by this function with malloc()
   * and must be freed with free(), i.e. it is meant to be used in C data
   * structures / programs.
   *
   * Returns true if the last argument was assigned, false otherwise.
   */
  static bool scanAttrCStr
  (
    const Attributes&  attrs,
    const unsigned int index,
    char**             value
  );

  /**
   * Searches for an attribute with name.  If found, the corresponding string
   * is stored through the last pointer argument, analogous to C's scanf.
   * Otherwise, the last argument is left unassigned.
   *
   * The space for the string is allocated by this function with malloc() and
   * must be freed with free(), i.e. it is meant to be used in C data
   * structures / programs.
   *
   * Returns true if the last argument was assigned, false otherwise.
   */
  static bool scanAttrCStr
  (
     const Attributes& attrs
   , const XMLCh*      name
   , char**            value
  );

  /**
   * Splits toSplit into XML namespace triplets.  The contents of toSplit
   * is assumed to have one of the following three forms (whitespace is not
   * part of the strings below and is provided only for separation
   * clarity):
   *
   *   localname
   *   uri sepchar localname
   *   uri sepchar localname sepchar prefix
   *
   * The split is performed in-place by replacing each sepchar with NULL.
   * If a particular namespace component is found (e.g. uri), the
   * corresponding passed-in variable will be set to the start of that
   * component.  If a namespace component is not found, the corresponding
   * passed-in variable will be set to NULL.
   *
   * To unsplit, call unsplitNamespaceTriplets()
   */
  static void splitNamespaceTriplets
  (
      XMLCh*  toSplit
    , XMLCh** uri
    , XMLCh** localname
    , XMLCh** prefix
    , XMLCh   sepchar
  );

  /**
   * Undoes the work of splitNamespaceTriplets().
   */
  static void unsplitNamespaceTriplets
  (
      XMLCh** uri
    , XMLCh** localname
    , XMLCh** prefix
    , XMLCh   sepchar
  );
};


#endif  /* __cplusplus */
#endif  /* XMLUtil_h   */
