/**
 * \file    SBMLReader.h
 * \brief   Reads an SBML Document into memory
 * \author  Ben Bornstein
 *
 * $Id: SBMLReader.h,v 1.14 2005/05/02 02:55:43 bbornstein Exp $
 * $Source: /cvsroot/sbml/libsbml/src/sbml/SBMLReader.h,v $
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
 */


#ifndef SBMLReader_h
#define SBMLReader_h


#include "common/extern.h"
#include "xml/XMLSchemaValidation.h"


/**
 * Warnings, errors and fatal errors are logged to an SBMLDocument while
 * reading.  Three types of fatal errors in particular will be logged
 * immediately.  They have the following ParseMessage ids:
 */
typedef enum
{
    SBML_READ_ERROR_UNKNOWN        = 0
  , SBML_READ_ERROR_FILE_NOT_FOUND = 1
  , SBML_READ_ERROR_NOT_SBML       = 2
} SBMLReadError_t;


#ifdef __cplusplus


#include <string>


class SBMLDocument;


class SBMLReader
{
public:

  /**
   * Creates a new SBMLReader and returns it.
   *
   * By default schema validation is off (XML_SCHEMA_VALIDATION_NONE) and
   * schemaFilenames are empty.
   */
  LIBSBML_EXTERN
  SBMLReader (XMLSchemaValidation_t level = XML_SCHEMA_VALIDATION_NONE);

  /**
   * Destorys this SBMLReader.
   */
  LIBSBML_EXTERN
  virtual ~SBMLReader ();


  /**
   * @return the schema filename used by this SBMLReader to validate SBML
   * Level 1 version 1 documents.
   */
  LIBSBML_EXTERN
  const std::string& getSchemaFilenameL1v1 () const;

  /**
   * @return the schema filename used by this SBMLReader to validate SBML
   * Level 1 version 2 documents.
   */
  LIBSBML_EXTERN
  const std::string& getSchemaFilenameL1v2 () const;

  /**
   * @return the schema filename used by this SBMLReader to validate SBML
   * Level 2 version 1 documents.
   */
  LIBSBML_EXTERN
  const std::string& getSchemaFilenameL2v1 () const;

  /**
   * Sets the schema validation level used by this SBMLReader.
   */
  LIBSBML_EXTERN
  XMLSchemaValidation_t getSchemaValidationLevel() const;

  /**
   * Reads an SBML document from the given file.  If filename does not
   * exist or is not an SBML file, a fatal error will be logged.
   * Errors can be identified by their unique ids, e.g.:
   *
   * <code>
   *   SBMLDocument* d = reader.readSBML(filename);
   *
   *   if (d->getNumFatals() > 0)
   *   {
   *     if (d->getFatal(0)->getId() == SBML_READ_ERROR_FILE_NOT_FOUND)
   *     if (d->getFatal(0)->getId() == SBML_READ_ERROR_NOT_SBML)
   *   }
   * </code>
   *
   * @return a pointer to the SBMLDocument read.
   */
  LIBSBML_EXTERN
  SBMLDocument* readSBML (const std::string& filename);

  /**
   * Reads an SBML document from the given XML string.
   *
   * The XML string must be complete and legal XML document.  Among other
   * things, it must start with an XML processing instruction.  For e.g.,:
   *
   *   <?xml version='1.0' encoding='UTF-8'?>
   *
   * This method will log a fatal error if the XML string is not SBML.  See
   * the method documentation for readSBML(filename) for example error
   * checking code.
   *
   * @return a pointer to the SBMLDocument read.
   */
  LIBSBML_EXTERN
  SBMLDocument* readSBMLFromString (const std::string& xml);

  /**
   * Sets the schema filename used by this SBMLReader to validate SBML
   * Level 1 version 1 documents.
   *
   * The filename should be either i) an absolute path or ii) relative to
   * the directory contain the SBML file(s) to be read.
   */
  LIBSBML_EXTERN
  void setSchemaFilenameL1v1 (const std::string& filename);

  /**
   * Sets the schema filename used by this SBMLReader to validate SBML
   * Level 1 version 2 documents.
   *
   * The filename should be either i) an absolute path or ii) relative to
   * the directory contain the SBML file(s) to be read.
   */
  LIBSBML_EXTERN
  void setSchemaFilenameL1v2 (const std::string& filename);

  /**
   * Sets the schema filename used by this SBMLReader to validate SBML Level
   * 2 version 1 documents.
   *
   * The filename should be either i) an absolute path or ii) relative to the
   * directory contain the SBML file(s) to be read.
   */
  LIBSBML_EXTERN
  void setSchemaFilenameL2v1 (const std::string& filename);

  /**
   * Sets the schema validation level used by this SBMLReader.
   *
   * The levels are:
   *
   *   XML_SCHEMA_VALIDATION_NONE (0) turns schema validation off.
   *
   *   XML_SCHEMA_VALIDATION_BASIC (1) validates an XML instance document
   *   against an XML Schema.  Those who wish to perform schema checking on
   *   SBML documents should use this option.
   *
   *   XML_SCHEMA_VALIDATION_FULL (2) validates both the instance document
   *   itself *and* the XML Schema document.  The XML Schema document is
   *   checked for violation of particle unique attribution constraints and
   *   particle derivation restrictions, which is both time-consuming and
   *   memory intensive.
   */
  LIBSBML_EXTERN
  void setSchemaValidationLevel (XMLSchemaValidation_t level);


protected:

  char*          getSchemaLocation (SBMLDocument* d) const;
  SBMLDocument*  readSBML_internal (const char* filename, const char* xml);


  XMLSchemaValidation_t schemaValidationLevel;

  std::string schemaFilenameL1v1;
  std::string schemaFilenameL1v2;
  std::string schemaFilenameL2v1;
};


#endif /* __cplusplus */


BEGIN_C_DECLS


#ifndef SWIG


#include "common/sbmlfwd.h"


/**
 * Creates a new SBMLReader and returns a pointer to it.
 *
 * By default schema validation is off (XML_SCHEMA_VALIDATION_NONE) and
 * schemaFilename is NULL.
 */
LIBSBML_EXTERN
SBMLReader_t *
SBMLReader_create (void);

/**
 * Frees the given SBMLReader.
 */
LIBSBML_EXTERN
void
SBMLReader_free (SBMLReader_t *sr);


/**
 * @return the schema filename used by this SBMLReader to validate SBML
 * Level 1 version 1 documents.
 */
LIBSBML_EXTERN
const char *
SBMLReader_getSchemaFilenameL1v1 (const SBMLReader_t *sr);

/**
 * @return the schema filename used by this SBMLReader to validate SBML
 * Level 1 version 2 documents.
 */
LIBSBML_EXTERN
const char *
SBMLReader_getSchemaFilenameL1v2 (const SBMLReader_t *sr);

/**
 * @return the schema filename used by this SBMLReader to validate SBML Level
 * 2 version 1 documents.
 */
LIBSBML_EXTERN
const char *
SBMLReader_getSchemaFilenameL2v1 (const SBMLReader_t *sr);

/**
 * @return the schema validation level used by this SBMLReader.
 */
LIBSBML_EXTERN
XMLSchemaValidation_t
SBMLReader_getSchemaValidationLevel(const SBMLReader_t *sr);

/**
 * Reads an SBML document from the given file.  If filename does not exist
 * or is not an SBML file, a fatal error will be logged.  Errors can be
 * identified by their unique ids, e.g.:
 *
 * <code>
 *   SBMLReader     *sr;
 *   SBMLDocument_t *d;
 *
 *   sr = SBMLReader_create();
 *   SBMLReader_setSchemaValidationLevel(sr, XML_SCHEMA_VALIDATION_BASIC);
 *   SBMLReader_setSchemaFilenameL1v1("sbml-l1v1.xsd");
 *   SBMLReader_setSchemaFilenameL1v2("sbml-l1v2.xsd");
 *   SBMLReader_setSchemaFilenameL2v1("sbml-l2v1.xsd");
 *
 *   d = SBMLReader_readSBML(reader, filename);
 *
 *   if (SBMLDocument_getNumFatals(d) > 0)
 *   {
 *     ParseMessage_t *pm = SBMLDocument_getFatal(d, 0);
 *     if (ParseMessage_getId(pm) == SBML_READ_ERROR_FILE_NOT_FOUND)
 *     if (ParseMessage_getId(pm) == SBML_READ_ERROR_NOT_SBML)
 *   }
 * </code>
 *
 * @return a pointer to the SBMLDocument read.
 */
LIBSBML_EXTERN
SBMLDocument_t *
SBMLReader_readSBML (SBMLReader_t *sr, const char *filename);

/**
 * Reads an SBML document from the given XML string.
 *
 * The XML string must be complete and legal XML document.  Among other
 * things, it must start with an XML processing instruction.  For e.g.,:
 *
 *   <?xml version='1.0' encoding='UTF-8'?>
 *
 * This method will log a fatal error if the XML string is not SBML.  See
 * the function documentation for SBMLReader_readSBML(filename) for example
 * error checking code.
 *
 * @return a pointer to the SBMLDocument read.
 */
LIBSBML_EXTERN
SBMLDocument_t *
SBMLReader_readSBMLFromString (SBMLReader_t *sr, const char *xml);

/**
 * Sets the schema filename used by this SBMLReader to validate SBML Level
 * 1 version 1 documents.
 *
 * The filename should be either i) an absolute path or ii) relative to the
 * directory contain the SBML file(s) to be read.
 */
LIBSBML_EXTERN
void
SBMLReader_setSchemaFilenameL1v1 (SBMLReader_t *sr, const char *filename);

/**
 * Sets the schema filename used by this SBMLReader to validate SBML Level
 * 1 version 2 documents.
 *
 * The filename should be either i) an absolute path or ii) relative to the
 * directory contain the SBML file(s) to be read.
 */
LIBSBML_EXTERN
void
SBMLReader_setSchemaFilenameL1v2 (SBMLReader_t *sr, const char *filename);

/**
 * Sets the schema filename used by this SBMLReader to validate SBML Level
 * 2 version 1 documents.
 *
 * The filename should be either i) an absolute path or ii) relative to the
 * directory contain the SBML file(s) to be read.
 */
LIBSBML_EXTERN
void
SBMLReader_setSchemaFilenameL2v1 (SBMLReader_t *sr, const char *filename);


/**
 * Sets the schema validation level used by this SBMLReader.
 *
 * The levels are:
 *
 *   XML_SCHEMA_VALIDATION_NONE (0) turns schema validation off.
 *
 *   XML_SCHEMA_VALIDATION_BASIC (1) validates an XML instance document
 *   against an XML Schema.  Those who wish to perform schema checking on
 *   SBML documents should use this option.
 *
 *   XML_SCHEMA_VALIDATION_FULL (2) validates both the instance document
 *   itself *and* the XML Schema document.  The XML Schema document is
 *   checked for violation of particle unique attribution constraints and
 *   particle derivation restrictions, which is both time-consuming and
 *   memory intensive.
 */
LIBSBML_EXTERN
void
SBMLReader_setSchemaValidationLevel ( SBMLReader_t *sr,
                                      XMLSchemaValidation_t level );


#endif  /* !SWIG */


/**
 * Reads an SBML document from the given file.  If filename does not exist
 * or is not an SBML file, a fatal error will be logged.  Errors can be
 * identified by their unique ids, e.g.:
 *
 * <code>
 *   SBMLDocument_t *d = SBMLReader_readSBML(reader, filename);
 *
 *   if (SBMLDocument_getNumFatals(d) > 0)
 *   {
 *     ParseMessage_t *pm = SBMLDocument_getFatal(d, 0);
 *     if (ParseMessage_getId(pm) == SBML_READ_ERROR_FILE_NOT_FOUND)
 *     if (ParseMessage_getId(pm) == SBML_READ_ERROR_NOT_SBML)
 *   }
 * </code>
 *
 * @return a pointer to the SBMLDocument read.
 */
LIBSBML_EXTERN
SBMLDocument_t *
readSBML (const char *filename);

/**
 * Reads an SBML document from the given XML string.
 *
 * The XML string must be complete and legal XML document.  Among other
 * things, it must start with an XML processing instruction.  For e.g.,:
 *
 *   <?xml version='1.0' encoding='UTF-8'?>
 *
 * This method will log a fatal error if the XML string is not SBML.  See
 * the function documentation for readSBML(filename) for example error
 * checking code.
 *
 * @return a pointer to the SBMLDocument read.
 */
LIBSBML_EXTERN
SBMLDocument_t *
readSBMLFromString (const char *xml);


END_C_DECLS


#endif  /* SBMLReader_h */
