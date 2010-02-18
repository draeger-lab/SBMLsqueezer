/**
 * \file    Event.h
 * \brief   SBML Event
 * \author  Ben Bornstein
 *
 * $Id: Event.h,v 1.10 2005/03/29 03:14:29 bbornstein Exp $
 * $Source: /cvsroot/sbml/libsbml/src/sbml/Event.h,v $
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


#ifndef Event_h
#define Event_h


#include "common/extern.h"


#ifdef __cplusplus


#include <string>

#include "SBase.h"
#include "ListOf.h"


class ASTNode;
class EventAssignment;
class SBMLVisitor;


class Event : public SBase
{
public:

  /**
   * Creates a new Event, optionally with its id, trigger and delay
   * attribute set.  Trigger and delay may be specified as infix formula
   * strings.
   */
  LIBSBML_EXTERN
  Event (   const std::string&  id      = ""
          , const std::string&  trigger = ""
          , const std::string&  delay   = "" );

  /**
   * Creates a new Event with an id and trigger and (optionally) delay
   * attributes set.
   */
  LIBSBML_EXTERN
  Event (   const std::string&  id
          , ASTNode*            trigger
          , ASTNode*            delay   = NULL );

  /**
   * Destroys this Event.
   */
  LIBSBML_EXTERN
  virtual ~Event ();


  /**
   * Accepts the given SBMLVisitor.
   *
   * @return the result of calling <code>v.visit()</code>, which indicates
   * whether or not the Visitor would like to visit the Model's next Event
   * (if available).
   */
  LIBSBML_EXTERN
  bool accept (SBMLVisitor& v) const;

  /**
   * @return the id of this Event.
   */
  LIBSBML_EXTERN
  const std::string& getId () const;

  /**
   * @return the name of this Event.
   */
  LIBSBML_EXTERN
  const std::string& getName () const;

  /**
   * @return the trigger of this Event.
   */
  LIBSBML_EXTERN
  const ASTNode* getTrigger () const;

  /**
   * @return the delay of this Event.
   */
  LIBSBML_EXTERN
  const ASTNode* getDelay () const;

  /**
   * @return the timeUnits of this Event.
   */
  LIBSBML_EXTERN
  const std::string& getTimeUnits () const;

  /**
   * @return true if the id of this Event has been set, false otherwise.
   */
  LIBSBML_EXTERN
  bool isSetId () const;

  /**
   * @return true if the name of this Event has been set, false otherwise.
   */
  LIBSBML_EXTERN
  bool isSetName () const;

  /**
   * @return true if the trigger of this Event has been set, false
   * otherwise.
   */
  LIBSBML_EXTERN
  bool isSetTrigger () const;

  /**
   * @return true if the delay of this Event has been set, false otherwise.
   */
  LIBSBML_EXTERN
  bool isSetDelay () const;

  /**
   * @return true if the timeUnits of this Event has been set, false
   * otherwise.
   */
  LIBSBML_EXTERN
  bool isSetTimeUnits () const;

  /**
   * Sets the id of this Event to a copy of sid.
   */
  LIBSBML_EXTERN
  void setId (const std::string& sid);

  /**
   * Sets the name of this Event to a copy of string.
   */
  LIBSBML_EXTERN
  void setName (const std::string& str);

  /**
   * Sets the trigger of this Event to the given ASTNode.
   *
   * The node <b>is not copied</b> and this Event <b>takes ownership</b> of
   * it; i.e. subsequent calls to this function or a call to Event_free()
   * will free the ASTNode (and any child nodes).
   */
  LIBSBML_EXTERN
  void setTrigger (ASTNode* math);

  /**
   * Sets the delay of this Event to the given ASTNode.
   *
   * The node <b>is not copied</b> and this Event <b>takes ownership</b> of
   * it; i.e. subsequent calls to this function or a call to Event_free()
   * will free the ASTNode (and any child nodes).
   */
  LIBSBML_EXTERN
  void setDelay (ASTNode* math);

  /**
   * Sets the timeUnits of this Event to a copy of sid.
   */
  LIBSBML_EXTERN
  void setTimeUnits (const std::string& sid);

  /**
   * Unsets the id of this Event.
   */
  LIBSBML_EXTERN
  void unsetId ();

  /**
   * Unsets the name of this Event.
   */
  LIBSBML_EXTERN
  void unsetName ();

  /**
   * Unsets the delay of this Event.
   */
  LIBSBML_EXTERN
  void unsetDelay ();

  /**
   * Unsets the timeUnits of this Event.
   */
  LIBSBML_EXTERN
  void unsetTimeUnits ();

  /**
   * Appends the given EventAssignment to this Event.
   */
  LIBSBML_EXTERN
  void addEventAssignment (EventAssignment& ea);

  /**
   * @return the list of EventAssignments for this Event.
   */
  LIBSBML_EXTERN
  ListOf& getListOfEventAssignments ();

  /**
   * @return the list of EventAssignments for this Event.
   */
  LIBSBML_EXTERN
  const ListOf& getListOfEventAssignments () const;

  /**
   * @return the nth EventAssignment of this Event.
   */
  LIBSBML_EXTERN
  EventAssignment* getEventAssignment (unsigned int n) const;

  /**
   * @return the number of EventAssignments in this Event.
   */
  LIBSBML_EXTERN
  unsigned int getNumEventAssignments () const;


protected:

  std::string id;
  std::string name;
  ASTNode*    trigger;
  ASTNode*    delay;
  std::string timeUnits;
  ListOf      eventAssignment;


  friend class SBMLFormatter;
  friend class SBMLHandler;
};


#endif  /* __cplusplus */


#ifndef SWIG


BEGIN_C_DECLS


#include "common/sbmlfwd.h"


/**
 * Creates a new Event and returns a pointer to it.
 */
LIBSBML_EXTERN
Event_t *
Event_create (void);

/**
 * Creates a new Event with the given id and trigger and returns a pointer
 * to it.  This convenience function is functionally equivalent to:
 *
 *   e = Event_create();
 *   Event_setId(e, id); Event_setTrigger(e, trigger);
 */
LIBSBML_EXTERN
Event_t *
Event_createWith (const char *sid, ASTNode_t *trigger);

/**
 * Frees the given Event.
 */
LIBSBML_EXTERN
void
Event_free (Event_t *e);


/**
 * @return the id of this Event.
 */
LIBSBML_EXTERN
const char *
Event_getId (const Event_t *e);

/**
 * @return the name of this Event.
 */
LIBSBML_EXTERN
const char *
Event_getName (const Event_t *e);

/**
 * @return the trigger of this Event.
 */
LIBSBML_EXTERN
const ASTNode_t *
Event_getTrigger (const Event_t *e);

/**
 * @return the delay of this Event.
 */
LIBSBML_EXTERN
const ASTNode_t *
Event_getDelay (const Event_t *e);

/**
 * @return the timeUnits of this Event
 */
LIBSBML_EXTERN
const char *
Event_getTimeUnits (const Event_t *e);


/**
 * @return 1 if the id of this Event has been set, 0 otherwise.
 */
LIBSBML_EXTERN
int
Event_isSetId (const Event_t *e);

/**
 * @return 1 if the name of this Event has been set, 0 otherwise.
 */
LIBSBML_EXTERN
int
Event_isSetName (const Event_t *e);

/**
 * @return 1 if the trigger of this Event has been set, 0 otherwise.
 */
LIBSBML_EXTERN
int
Event_isSetTrigger (const Event_t *e);

/**
 * @return 1 if the delay of this Event has been set, 0 otherwise.
 */
LIBSBML_EXTERN
int
Event_isSetDelay (const Event_t *e);

/**
 * @return 1 if the timeUnits of this Event has been set, 0 otherwise.
 */
LIBSBML_EXTERN
int
Event_isSetTimeUnits (const Event_t *e);


/**
 * Sets the id of this Event to a copy of sid.
 */
LIBSBML_EXTERN
void
Event_setId (Event_t *e, const char *sid);

/**
 * Sets the name of this Event to a copy of string.
 */
LIBSBML_EXTERN
void
Event_setName (Event_t *e, const char *string);

/**
 * Sets the trigger of this Event to the given ASTNode.
 *
 * The node <b>is not copied</b> and this Event <b>takes ownership</b> of
 * it; i.e. subsequent calls to this function or a call to Event_free()
 * will free the ASTNode (and any child nodes).
 */
LIBSBML_EXTERN
void
Event_setTrigger (Event_t *e, ASTNode_t *math);

/**
 * Sets the delay of this Event to the given ASTNode.
 *
 * The node <b>is not copied</b> and this Event <b>takes ownership</b> of
 * it; i.e. subsequent calls to this function or a call to Event_free()
 * will free the ASTNode (and any child nodes).
 */
LIBSBML_EXTERN
void
Event_setDelay (Event_t *e, ASTNode_t *math);

/**
 * Sets the timeUnits of this Event to a copy of sid.
 */
LIBSBML_EXTERN
void
Event_setTimeUnits (Event_t *e, const char *sid);


/**
 * Unsets the id of this Event.  This is equivalent to:
 * safe_free(e->id); e->id = NULL;
 */
LIBSBML_EXTERN
void
Event_unsetId (Event_t *e);

/**
 * Unsets the name of this Event.  This is equivalent to:
 * safe_free(e->name); e->name = NULL;
 */
LIBSBML_EXTERN
void
Event_unsetName (Event_t *e);

/**
 * Unsets the delay of this Event.  This is equivalent to:
 * ASTNode_free(e->delay); e->delay = NULL;
 */
LIBSBML_EXTERN
void
Event_unsetDelay (Event_t *e);

/**
 * Unsets the timeUnits of this Event.  This is equivalent to:
 * safe_free(e->timeUnits); e->timeUnits = NULL;
 */
LIBSBML_EXTERN
void
Event_unsetTimeUnits (Event_t *e);


/**
 * Appends the given EventAssignment to this Event.
 */
LIBSBML_EXTERN
void
Event_addEventAssignment (Event_t *e, EventAssignment_t *ea);

/**
 * @return the list of EventAssignments for this Event.
 */
LIBSBML_EXTERN
ListOf_t *
Event_getListOfEventAssignments (Event_t *e);

/**
 * @return the nth EventAssignment of this Event.
 */
LIBSBML_EXTERN
EventAssignment_t *
Event_getEventAssignment (const Event_t *e, unsigned int n);

/**
 * @return the number of EventAssignments in this Event.
 */
LIBSBML_EXTERN
unsigned int
Event_getNumEventAssignments (const Event_t *e);


/**
 * The EventIdCmp function compares the string sid to e->id.
 *
 * @returns an integer less than, equal to, or greater than zero if sid is
 * found to be, respectively, less than, to match or be greater than e->id.
 * Returns -1 if either sid or e->id is NULL.
 */
LIBSBML_EXTERN
int
EventIdCmp (const char *sid, const Event_t *e);


END_C_DECLS


#endif  /* !SWIG   */
#endif  /* Event_h */
