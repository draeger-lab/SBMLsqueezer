/**
 * \file    EventAssignment.h
 * \brief   SBML EventAssignment
 * \author  Ben Bornstein
 *
 * $Id: EventAssignment.h,v 1.7 2005/03/29 03:14:29 bbornstein Exp $
 * $Source: /cvsroot/sbml/libsbml/src/sbml/EventAssignment.h,v $
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
 */


#ifndef EventAssignment_h
#define EventAssignment_h


#include "common/extern.h"


#ifdef __cplusplus


#include <string>
#include "SBase.h"


class ASTNode;
class SBMLVisitor;


class EventAssignment : public SBase
{
public:

  /**
   * Creates a new EventAssignment, optionally with its variable and math
   * (via infix formula string) attributes set.
   */
  LIBSBML_EXTERN
  EventAssignment (   const std::string& variable = ""
                    , const std::string& formula  = "" );
  /**
   * Creates a new EventAssignment with its variable and math attributes
   * set.
   */
  LIBSBML_EXTERN
  EventAssignment (   const std::string& variable
                    , ASTNode*           math     );

  /**
   * Destroys this EventAssignment.
   */
  LIBSBML_EXTERN
  virtual ~EventAssignment ();


  /**
   * Accepts the given SBMLVisitor.
   *
   * @return the result of calling <code>v.visit()</code>, which indicates
   * whether or not the Visitor would like to visit the Event's next
   * EventAssignment (if available).
   */
  LIBSBML_EXTERN
  bool accept (SBMLVisitor& v) const;

  /**
   * @return the variable of this EventAssignment.
   */
  LIBSBML_EXTERN
  const std::string& getVariable () const;

  /**
   * @return the math of this EventAssignment.
   */
  LIBSBML_EXTERN
  const ASTNode* getMath () const;

  /**
   * @return true if the variable of this EventAssignment has been set, false
   * otherwise.
   */
  LIBSBML_EXTERN
  bool isSetVariable () const;

  /**
   * @return true if the math of this EventAssignment has been set, false
   * otherwise.
   */
  LIBSBML_EXTERN
  bool isSetMath () const;

  /**
   * Sets the variable of this EventAssignment to a copy of sid.
   */
  LIBSBML_EXTERN
  void setVariable (const std::string& sid);

  /**
   * Sets the math of this EventAssignment to the given ASTNode.
   *
   * The node <b>is not copied</b> and this EventAssignment <b>takes
   * ownership</b> of it; i.e. subsequent calls to this function or a call
   * to EventAssignment_free() will free the ASTNode (and any child nodes).
   */
  LIBSBML_EXTERN
  void setMath (ASTNode* math);


protected:

  std::string variable;
  ASTNode*    math;


  friend class SBMLFormatter;
  friend class SBMLHandler;
};


#endif  /* __cplusplus */


#ifndef SWIG


BEGIN_C_DECLS


#include "common/sbmlfwd.h"


/**
 * Creates a new EventAssignment and returns a pointer to it.
 */
LIBSBML_EXTERN
EventAssignment_t *
EventAssignment_create (void);

/**
 * Creates a new EventAssignment with the given variable and math and
 * returns a pointer to it.  This convenience function is functionally
 * equivalent to:
 *
 *   ea = EventAssignment_create();
 *   EventAssignment_setVariable(ea, variable);
 *   EventAssignment_setMath(ea, math);
 */
LIBSBML_EXTERN
EventAssignment_t *
EventAssignment_createWith (const char *variable, ASTNode_t *math);

/**
 * Frees the given EventAssignment.
 */
LIBSBML_EXTERN
void
EventAssignment_free (EventAssignment_t *ea);


/**
 * @return the variable of this EventAssignment.
 */
LIBSBML_EXTERN
const char *
EventAssignment_getVariable (const EventAssignment_t *ea);

/**
 * @return the math of this EventAssignment.
 */
LIBSBML_EXTERN
const ASTNode_t *
EventAssignment_getMath (const EventAssignment_t *ea);


/**
 * @return 1 if the variable of this EventAssignment has been set, 0
 * otherwise.
 */
LIBSBML_EXTERN
int
EventAssignment_isSetVariable (const EventAssignment_t *ea);

/**
 * @return 1 if the math of this EventAssignment has been set, 0 otherwise.
 */
LIBSBML_EXTERN
int
EventAssignment_isSetMath (const EventAssignment_t *ea);


/**
 * Sets the variable of this EventAssignment to a copy of sid.
 */
LIBSBML_EXTERN
void
EventAssignment_setVariable (EventAssignment_t *ea, const char *sid);

/**
 * Sets the math of this EventAssignment to the given ASTNode.
 *
 * The node <b>is not copied</b> and this EventAssignment <b>takes
 * ownership</b> of it; i.e. subsequent calls to this function or a call to
 * EventAssignment_free() will free the ASTNode (and any child nodes).
 */
LIBSBML_EXTERN
void
EventAssignment_setMath (EventAssignment_t *ea, ASTNode_t *math);


END_C_DECLS


#endif  /* !SWIG */
#endif  /* EventAssignment_h */
