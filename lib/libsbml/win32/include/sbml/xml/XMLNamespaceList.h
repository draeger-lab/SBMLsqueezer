/**
 * \file    XMLNamespaces.h
 * \brief   A list of XMLNamepaces
 * \author  Ben Bornstein
 * 
 * $Id: XMLNamespaceList.h,v 1.3 2005/05/25 18:30:03 bbornstein Exp $
 * $Source: /cvsroot/sbml/libsbml/src/xml/XMLNamespaceList.h,v $
 */
/* Copyright 2004 California Institute of Technology and
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
 */


#ifndef XMLNamespaceList_h
#define XMLNamespaceList_h


#ifdef __cplusplus


#include <list>
#include <string>

#include "../common/extern.h"
#include "XMLNamespace.h"


/**
 * Namespaces is a list of namespace URIs and their corresponding prefixes.
 */
class XMLNamespaceList
{
public:

  /**
   * Adds XMLNamespace to this list of XML namespaces.
   */
  LIBSBML_EXTERN
  void add (const XMLNamespace& ns);

  /**
   * Adds (prefix, URI) to this list of XML namespaces.
   *
   * If prefix starts with 'xmlns:' (case-insensitive), it will be removed.
   */
  LIBSBML_EXTERN
  void add (const std::string& prefix, const std::string& URI);

  /**
   * @return the number of XML namespaces in this list.
   */
  LIBSBML_EXTERN
  unsigned int getLength () const;

  /**
   * @return the nth XMLNamespace in this list.
   */
  LIBSBML_EXTERN
  const XMLNamespace& getNamespace (unsigned int n) const;

  /**
   * @return the prefix of the nth XML namespace in this list.
   */
  LIBSBML_EXTERN
  const std::string& getPrefix (unsigned int n) const;

  /**
   * @return the prefix of the XML namespace with the given URI.  If URI is
   * not in this list of namespaces, an empty string is returned.
   */
  LIBSBML_EXTERN
  const std::string& getPrefix (const std::string& URI) const;

  /**
   * @return the URI of the nth XML namespace in this list.
   */
  LIBSBML_EXTERN
  const std::string& getURI (unsigned int n) const;

  /**
   * @return the URI of the XML namespace with the given prefix.  If prefix
   * was not found, an empty string is returned.
   */
  LIBSBML_EXTERN
  const std::string& getURI (const std::string& prefix) const;



protected:

  std::list<XMLNamespace>    mNamespaces;
  static const XMLNamespace  mEmptyNamespace;
};


#endif  /* __cplusplus */
#endif  /* XMLNamespaceList_h */
