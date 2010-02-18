# This file was created automatically by SWIG.
# Don't modify this file, modify the SWIG interface instead.
# This file is compatible with both classic and new-style classes.

import _libsbml

def _swig_setattr_nondynamic(self,class_type,name,value,static=1):
    if (name == "this"):
        if isinstance(value, class_type):
            self.__dict__[name] = value.this
            if hasattr(value,"thisown"): self.__dict__["thisown"] = value.thisown
            del value.thisown
            return
    method = class_type.__swig_setmethods__.get(name,None)
    if method: return method(self,value)
    if (not static) or hasattr(self,name) or (name == "thisown"):
        self.__dict__[name] = value
    else:
        raise AttributeError("You cannot add attributes to %s" % self)

def _swig_setattr(self,class_type,name,value):
    return _swig_setattr_nondynamic(self,class_type,name,value,0)

def _swig_getattr(self,class_type,name):
    method = class_type.__swig_getmethods__.get(name,None)
    if method: return method(self)
    raise AttributeError,name

import types
try:
    _object = types.ObjectType
    _newclass = 1
except AttributeError:
    class _object : pass
    _newclass = 0
del types


import sys
import os.path


def conditional_abspath (filename):
  """conditional_abspath (filename) -> filename

  Returns filename with an absolute path prepended, if necessary.
  Some combinations of platforms and underlying XML parsers *require*
  an absolute path to a filename while others do not.  This function
  encapsulates the appropriate logic.  It is used by readSBML() and
  SBMLReader.readSBML().
  """
  if sys.platform.find('cygwin') != -1:
    return filename
  else:
    return os.path.abspath(filename)

def readSBML(*args):
  """readSBML(filename) -> SBMLDocument

  Reads an SBML document from the given file.  If filename does not exist
  or is not an SBML file, a fatal error will be logged.  Errors can be
  identified by their unique ids, e.g.:

    d = readSBML(filename)

    if d.getNumFatals() > 0:
      pm = d.getFatal(0)
      if pm.getId() == libsbml.SBML_READ_ERROR_FILE_NOT_FOUND: ...
      if pm.getId() == libsbml.SBML_READ_ERROR_NOT_SBML: ...
  """
  reader = SBMLReader()
  return reader.readSBML(args[0])

SBML_READ_ERROR_UNKNOWN = _libsbml.SBML_READ_ERROR_UNKNOWN
SBML_READ_ERROR_FILE_NOT_FOUND = _libsbml.SBML_READ_ERROR_FILE_NOT_FOUND
SBML_READ_ERROR_NOT_SBML = _libsbml.SBML_READ_ERROR_NOT_SBML
class SBMLReader(_object):
    """Proxy of C++ SBMLReader class"""
    __swig_setmethods__ = {}
    __setattr__ = lambda self, name, value: _swig_setattr(self, SBMLReader, name, value)
    __swig_getmethods__ = {}
    __getattr__ = lambda self, name: _swig_getattr(self, SBMLReader, name)
    def __repr__(self):
        return "<%s.%s; proxy of C++ SBMLReader instance at %s>" % (self.__class__.__module__, self.__class__.__name__, self.this,)
    def __init__(self, *args):
        """
        __init__(self, XMLSchemaValidation_t level=XML_SCHEMA_VALIDATION_NONE) -> SBMLReader
        __init__(self) -> SBMLReader

        Creates a new SBMLReader and returns it.

        By default schema validation is off (XML_SCHEMA_VALIDATION_NONE) and
        schemaFilenames are empty.


        """
        _swig_setattr(self, SBMLReader, 'this', _libsbml.new_SBMLReader(*args))
        _swig_setattr(self, SBMLReader, 'thisown', 1)
    def __del__(self, destroy=_libsbml.delete_SBMLReader):
        """__del__(self)"""
        try:
            if self.thisown: destroy(self)
        except: pass

    def getSchemaFilenameL1v1(*args): 
        """
        getSchemaFilenameL1v1(self) -> string

        Returns the schema filename used by this SBMLReader to validate SBML
        Level 1 version 1 documents.


        """
        return _libsbml.SBMLReader_getSchemaFilenameL1v1(*args)

    def getSchemaFilenameL1v2(*args): 
        """
        getSchemaFilenameL1v2(self) -> string

        Returns the schema filename used by this SBMLReader to validate SBML
        Level 1 version 2 documents.


        """
        return _libsbml.SBMLReader_getSchemaFilenameL1v2(*args)

    def getSchemaFilenameL2v1(*args): 
        """
        getSchemaFilenameL2v1(self) -> string

        Returns the schema filename used by this SBMLReader to validate SBML
        Level 2 version 1 documents.


        """
        return _libsbml.SBMLReader_getSchemaFilenameL2v1(*args)

    def getSchemaValidationLevel(*args): 
        """
        getSchemaValidationLevel(self) -> int

        Sets the schema validation level used by this SBMLReader.


        """
        return _libsbml.SBMLReader_getSchemaValidationLevel(*args)

    def readSBML(*args):
      """readSBML(filename) -> SBMLDocument

      Reads an SBML document from the given file.  If filename does not exist
      or is not an SBML file, a fatal error will be logged.  Errors can be
      identified by their unique ids, e.g.:

        reader = libsbml.SBMLReader()
        d      = reader.readSBML(filename)

        if d.getNumFatals() > 0:
          pm = d.getFatal(0)
          if pm.getId() == libsbml.SBML_READ_ERROR_FILE_NOT_FOUND: ..
          if pm.getId() == libsbml.SBML_READ_ERROR_NOT_SBML: ...
      """
      args_copy    = list(args)
      args_copy[1] = conditional_abspath(args[1])
      return _libsbml.SBMLReader_readSBML(*args_copy)


    def readSBMLFromString(*args): 
        """
        readSBMLFromString(self, string xml) -> SBMLDocument

        Reads an SBML document from the given XML string.

        The XML string must be complete and legal XML document.  Among other
        things, it must start with an XML processing instruction.  For e.g.,:

          <?xml version='1.0' encoding='UTF-8'?>

        This method will log a fatal error if the XML string is not SBML.  See
        the method documentation for readSBML(filename) for example error
        checking code.

        Returns a pointer to the SBMLDocument read.


        """
        return _libsbml.SBMLReader_readSBMLFromString(*args)

    def setSchemaFilenameL1v1(*args): 
        """
        setSchemaFilenameL1v1(self, string filename)

        Sets the schema filename used by this SBMLReader to validate SBML
        Level 1 version 1 documents.

        The filename should be either i) an absolute path or ii) relative to
        the directory contain the SBML file(s) to be read.


        """
        return _libsbml.SBMLReader_setSchemaFilenameL1v1(*args)

    def setSchemaFilenameL1v2(*args): 
        """
        setSchemaFilenameL1v2(self, string filename)

        Sets the schema filename used by this SBMLReader to validate SBML
        Level 1 version 2 documents.

        The filename should be either i) an absolute path or ii) relative to
        the directory contain the SBML file(s) to be read.


        """
        return _libsbml.SBMLReader_setSchemaFilenameL1v2(*args)

    def setSchemaFilenameL2v1(*args): 
        """
        setSchemaFilenameL2v1(self, string filename)

        Sets the schema filename used by this SBMLReader to validate SBML Level
        2 version 1 documents.

        The filename should be either i) an absolute path or ii) relative to the
        directory contain the SBML file(s) to be read.


        """
        return _libsbml.SBMLReader_setSchemaFilenameL2v1(*args)

    def setSchemaValidationLevel(*args): 
        """
        setSchemaValidationLevel(self, XMLSchemaValidation_t level)

        Sets the schema validation level used by this SBMLReader.

        The levels are:

          XML_SCHEMA_VALIDATION_NONE (0) turns schema validation off.

          XML_SCHEMA_VALIDATION_BASIC (1) validates an XML instance document
          against an XML Schema.  Those who wish to perform schema checking on
          SBML documents should use this option.

          XML_SCHEMA_VALIDATION_FULL (2) validates both the instance document
          itself and the XML Schema document.  The XML Schema document is
          checked for violation of particle unique attribution constraints and
          particle derivation restrictions, which is both time-consuming and
          memory intensive.


        """
        return _libsbml.SBMLReader_setSchemaValidationLevel(*args)


class SBMLReaderPtr(SBMLReader):
    def __init__(self, this):
        _swig_setattr(self, SBMLReader, 'this', this)
        if not hasattr(self,"thisown"): _swig_setattr(self, SBMLReader, 'thisown', 0)
        _swig_setattr(self, SBMLReader,self.__class__,SBMLReader)
_libsbml.SBMLReader_swigregister(SBMLReaderPtr)


def readSBMLFromString(*args):
    """
    readSBMLFromString(char xml) -> SBMLDocument_t

    Reads an SBML document from the given XML string.

    The XML string must be complete and legal XML document.  Among other
    things, it must start with an XML processing instruction.  For e.g.,:

      <?xml version='1.0' encoding='UTF-8'?>

    This method will log a fatal error if the XML string is not SBML.  See
    the function documentation for readSBML(filename) for example error
    checking code.

    Returns a pointer to the SBMLDocument read.


    """
    return _libsbml.readSBMLFromString(*args)
class SBMLWriter(_object):
    """Proxy of C++ SBMLWriter class"""
    __swig_setmethods__ = {}
    __setattr__ = lambda self, name, value: _swig_setattr(self, SBMLWriter, name, value)
    __swig_getmethods__ = {}
    __getattr__ = lambda self, name: _swig_getattr(self, SBMLWriter, name)
    def __repr__(self):
        return "<%s.%s; proxy of C++ SBMLWriter instance at %s>" % (self.__class__.__module__, self.__class__.__name__, self.this,)
    def __init__(self, *args):
        """
        __init__(self) -> SBMLWriter

        Creates a new SBMLWriter.


        """
        _swig_setattr(self, SBMLWriter, 'this', _libsbml.new_SBMLWriter(*args))
        _swig_setattr(self, SBMLWriter, 'thisown', 1)
    def __del__(self, destroy=_libsbml.delete_SBMLWriter):
        """__del__(self)"""
        try:
            if self.thisown: destroy(self)
        except: pass

    def setProgramName(*args): 
        """
        setProgramName(self, string name)

        Sets the name of this program, i.e. the one about to write out the
        SBMLDocument.  If the program name and version are set
        (setProgramVersion()), the following XML comment, intended for human
        consumption, will be written at the beginning of the document:

          <!-- Created by <program name> version <program version>
          on yyyy-MM-dd HH:mm with libsbml version <libsbml version>. -->


        """
        return _libsbml.SBMLWriter_setProgramName(*args)

    def setProgramVersion(*args): 
        """
        setProgramVersion(self, string version)

        Sets the version of this program, i.e. the one about to write out the
        SBMLDocument.  If the program version and name are set
        (setProgramName()), the following XML comment, intended for human
        consumption, will be written at the beginning of the document:

          <!-- Created by <program name> version <program version>
          on yyyy-MM-dd HH:mm with libsbml version <libsbml version>. -->


        """
        return _libsbml.SBMLWriter_setProgramVersion(*args)

    def write(*args): 
        """
        write(self, SBMLDocument d, string filename) -> bool

        Writes the given SBML document to the output stream.

        Returns true on success and false if one of the underlying Xerces or
        Expat components fail (rare).


        """
        return _libsbml.SBMLWriter_write(*args)

    def writeToString(*args): 
        """
        writeToString(self, SBMLDocument d) -> char

        Writes the given SBML document to an in-memory string and returns a
        pointer to it.  The string is owned by the caller and should be freed
        (with free()) when no longer needed.

        Returns the string on success and 0 if one of the underlying Xerces or
        Expat components fail (rare).


        """
        return _libsbml.SBMLWriter_writeToString(*args)


class SBMLWriterPtr(SBMLWriter):
    def __init__(self, this):
        _swig_setattr(self, SBMLWriter, 'this', this)
        if not hasattr(self,"thisown"): _swig_setattr(self, SBMLWriter, 'thisown', 0)
        _swig_setattr(self, SBMLWriter,self.__class__,SBMLWriter)
_libsbml.SBMLWriter_swigregister(SBMLWriterPtr)


def writeSBML(*args):
    """
    writeSBML(SBMLDocument_t d, char filename) -> int

    Writes the given SBML document to filename.  This convenience function
    is functionally equivalent to:

      SBMLWriter_writeSBML(SBMLWriter_create(), d, filename);

    Returns non-zero on success and zero if the filename could not be opened
    for writing.)


    """
    return _libsbml.writeSBML(*args)

def writeSBMLToString(*args):
    """
    writeSBMLToString(SBMLDocument_t d) -> char

    Writes the given SBML document to an in-memory string and returns a
    pointer to it.  The string is owned by the caller and should be freed
    (with free()) when no longer needed.  This convenience function is
    functionally equivalent to:

      SBMLWriter_writeSBMLToString(SBMLWriter_create(), d);

    Returns the string on success and NULL if one of the underlying Xerces
    or Expat components fail (rare).


    """
    return _libsbml.writeSBMLToString(*args)
SBML_UNKNOWN = _libsbml.SBML_UNKNOWN
SBML_COMPARTMENT = _libsbml.SBML_COMPARTMENT
SBML_DOCUMENT = _libsbml.SBML_DOCUMENT
SBML_EVENT = _libsbml.SBML_EVENT
SBML_EVENT_ASSIGNMENT = _libsbml.SBML_EVENT_ASSIGNMENT
SBML_FUNCTION_DEFINITION = _libsbml.SBML_FUNCTION_DEFINITION
SBML_KINETIC_LAW = _libsbml.SBML_KINETIC_LAW
SBML_LIST_OF = _libsbml.SBML_LIST_OF
SBML_MODEL = _libsbml.SBML_MODEL
SBML_PARAMETER = _libsbml.SBML_PARAMETER
SBML_REACTION = _libsbml.SBML_REACTION
SBML_SPECIES = _libsbml.SBML_SPECIES
SBML_SPECIES_REFERENCE = _libsbml.SBML_SPECIES_REFERENCE
SBML_MODIFIER_SPECIES_REFERENCE = _libsbml.SBML_MODIFIER_SPECIES_REFERENCE
SBML_UNIT_DEFINITION = _libsbml.SBML_UNIT_DEFINITION
SBML_UNIT = _libsbml.SBML_UNIT
SBML_ALGEBRAIC_RULE = _libsbml.SBML_ALGEBRAIC_RULE
SBML_ASSIGNMENT_RULE = _libsbml.SBML_ASSIGNMENT_RULE
SBML_RATE_RULE = _libsbml.SBML_RATE_RULE
SBML_SPECIES_CONCENTRATION_RULE = _libsbml.SBML_SPECIES_CONCENTRATION_RULE
SBML_COMPARTMENT_VOLUME_RULE = _libsbml.SBML_COMPARTMENT_VOLUME_RULE
SBML_PARAMETER_RULE = _libsbml.SBML_PARAMETER_RULE

def SBMLTypeCode_toString(*args):
    """
    SBMLTypeCode_toString(SBMLTypeCode_t tc) -> char

    Returns a human readable name for the given SBMLTypeCode_t.  The caller
    does not own the returned string and is therefore not allowed to modify
    it.


    """
    return _libsbml.SBMLTypeCode_toString(*args)
class SBase(_object):
    """Proxy of C++ SBase class"""
    __swig_setmethods__ = {}
    __setattr__ = lambda self, name, value: _swig_setattr(self, SBase, name, value)
    __swig_getmethods__ = {}
    __getattr__ = lambda self, name: _swig_getattr(self, SBase, name)
    def __init__(self): raise RuntimeError, "No constructor defined"
    def __repr__(self):
        return "<%s.%s; proxy of C++ SBase instance at %s>" % (self.__class__.__module__, self.__class__.__name__, self.this,)
    def __del__(self, destroy=_libsbml.delete_SBase):
        """__del__(self)"""
        try:
            if self.thisown: destroy(self)
        except: pass

    def init(*args): 
        """
        init(self, SBMLTypeCode_t tc)

        SBase 'objects' are abstract, i.e., they are not created.  Rather,
        specific 'subclasses' are created (e.g., Model) and their SBASE_FIELDS
        are initialized with this function.  The type of the specific
        'subclass' is indicated by the given SBMLTypeCode.


        """
        return _libsbml.SBase_init(*args)

    def getTypeCode(*args): 
        """
        getTypeCode(self) -> int

        """
        return _libsbml.SBase_getTypeCode(*args)

    def getColumn(*args): 
        """
        getColumn(self) -> unsigned int

        """
        return _libsbml.SBase_getColumn(*args)

    def getLine(*args): 
        """
        getLine(self) -> unsigned int

        """
        return _libsbml.SBase_getLine(*args)

    def getMetaId(*args): 
        """
        getMetaId(self) -> string

        """
        return _libsbml.SBase_getMetaId(*args)

    def getNotes(*args): 
        """
        getNotes(self) -> string

        """
        return _libsbml.SBase_getNotes(*args)

    def getAnnotation(*args): 
        """
        getAnnotation(self) -> string

        """
        return _libsbml.SBase_getAnnotation(*args)

    def getNamespaces(*args): 
        """
        getNamespaces(self) -> XMLNamespaceList

        Returns a list of XML namespaces defined on this SBML object.


        """
        return _libsbml.SBase_getNamespaces(*args)

    def hasNamespaces(*args): 
        """
        hasNamespaces(self) -> bool

        Returns true if this SBML object has any XML namespaces defined on it,
        false otherwise.


        """
        return _libsbml.SBase_hasNamespaces(*args)

    def isSetMetaId(*args): 
        """
        isSetMetaId(self) -> bool

        """
        return _libsbml.SBase_isSetMetaId(*args)

    def isSetNotes(*args): 
        """
        isSetNotes(self) -> bool

        """
        return _libsbml.SBase_isSetNotes(*args)

    def isSetAnnotation(*args): 
        """
        isSetAnnotation(self) -> bool

        """
        return _libsbml.SBase_isSetAnnotation(*args)

    def setMetaId(*args): 
        """
        setMetaId(self, string id)

        """
        return _libsbml.SBase_setMetaId(*args)

    def setNotes(*args): 
        """
        setNotes(self, string xhtml)

        """
        return _libsbml.SBase_setNotes(*args)

    def setAnnotation(*args): 
        """
        setAnnotation(self, string xml)

        """
        return _libsbml.SBase_setAnnotation(*args)

    def toSBML(*args): 
        """
        toSBML(self, unsigned int level=2, unsigned int version=1) -> char
        toSBML(self, unsigned int level=2) -> char
        toSBML(self) -> char

        Returns the partial SBML that describes this SBML object.


        """
        return _libsbml.SBase_toSBML(*args)

    def unsetMetaId(*args): 
        """
        unsetMetaId(self)

        """
        return _libsbml.SBase_unsetMetaId(*args)

    def unsetNotes(*args): 
        """
        unsetNotes(self)

        """
        return _libsbml.SBase_unsetNotes(*args)

    def unsetAnnotation(*args): 
        """
        unsetAnnotation(self)

        """
        return _libsbml.SBase_unsetAnnotation(*args)

    def __str__(self):
      return self.toSBML()

    def __cmp__(self, rhs):
      if hasattr(self, 'this') and hasattr(rhs, 'this'):
        if self.this == rhs.this: return 0
      return 1



class SBasePtr(SBase):
    def __init__(self, this):
        _swig_setattr(self, SBase, 'this', this)
        if not hasattr(self,"thisown"): _swig_setattr(self, SBase, 'thisown', 0)
        _swig_setattr(self, SBase,self.__class__,SBase)
_libsbml.SBase_swigregister(SBasePtr)

def SBaseTest_setup(*args):
    """SBaseTest_setup()"""
    return _libsbml.SBaseTest_setup(*args)

class ListOf(SBase):
    """Proxy of C++ ListOf class"""
    __swig_setmethods__ = {}
    for _s in [SBase]: __swig_setmethods__.update(_s.__swig_setmethods__)
    __setattr__ = lambda self, name, value: _swig_setattr(self, ListOf, name, value)
    __swig_getmethods__ = {}
    for _s in [SBase]: __swig_getmethods__.update(_s.__swig_getmethods__)
    __getattr__ = lambda self, name: _swig_getattr(self, ListOf, name)
    def __repr__(self):
        return "<%s.%s; proxy of C++ ListOf instance at %s>" % (self.__class__.__module__, self.__class__.__name__, self.this,)
    def __init__(self, *args):
        """
        __init__(self) -> ListOf

        Creates a new ListOf.


        """
        _swig_setattr(self, ListOf, 'this', _libsbml.new_ListOf(*args))
        _swig_setattr(self, ListOf, 'thisown', 1)
    def __del__(self, destroy=_libsbml.delete_ListOf):
        """__del__(self)"""
        try:
            if self.thisown: destroy(self)
        except: pass

    def append(*args):
      if args[1] is not None: args[1].thisown = 0
      return _libsbml.ListOf_append(*args)


    def get(*args): 
        """
        get(self, unsigned int n) -> SBase

        Returns the nth item in this List.  If n > ListOf.getNumItems()
        returns 0.


        """
        return _libsbml.ListOf_get(*args)

    def getNumItems(*args): 
        """
        getNumItems(self) -> unsigned int

        Returns the number of items in this List.


        """
        return _libsbml.ListOf_getNumItems(*args)

    def prepend(*args):
      if args[1] is not None: args[1].thisown = 0
      return _libsbml.ListOf_prepend(*args)


    def remove(*args):
      result = _libsbml.ListOf_remove(*args)
      if result is not None: result.thisown = 1
      return result


    def __len__(*args): 
        """__len__(self) -> int"""
        return _libsbml.ListOf___len__(*args)

    def __getitem__(self, key):

      try:
         keyIsSlice = isinstance(key, slice)
      except:
         keyIsSlice = 0

      if keyIsSlice:
        start = key.start
        if start is None:
          start = 0
        stop = key.stop
        if stop is None:
          stop = self.getNumItems()
        return [self[i] for i in range(
          self._fixNegativeIndex(start), self._fixNegativeIndex(stop)
        )]

      key = self._fixNegativeIndex(key)
      if key < 0 or key >= self.getNumItems():
        raise IndexError(key)
      return self.get(key)


    def _fixNegativeIndex(self, index):
      if index < 0:
        return index + self.getNumItems()
      else:
        return index


    def __iter__(self):
      for i in range(self.getNumItems()):
        yield self[i]


    def __repr__(self):
      return "[" + ", ".join([repr(self[i]) for i in range(len(self))]) + "]"


    def __str__(self):
      return repr(self)


class ListOfPtr(ListOf):
    def __init__(self, this):
        _swig_setattr(self, ListOf, 'this', this)
        if not hasattr(self,"thisown"): _swig_setattr(self, ListOf, 'thisown', 0)
        _swig_setattr(self, ListOf,self.__class__,ListOf)
_libsbml.ListOf_swigregister(ListOfPtr)

class Model(SBase):
    """Proxy of C++ Model class"""
    __swig_setmethods__ = {}
    for _s in [SBase]: __swig_setmethods__.update(_s.__swig_setmethods__)
    __setattr__ = lambda self, name, value: _swig_setattr(self, Model, name, value)
    __swig_getmethods__ = {}
    for _s in [SBase]: __swig_getmethods__.update(_s.__swig_getmethods__)
    __getattr__ = lambda self, name: _swig_getattr(self, Model, name)
    def __repr__(self):
        return "<%s.%s; proxy of C++ Model instance at %s>" % (self.__class__.__module__, self.__class__.__name__, self.this,)
    def __init__(self, *args):
        """
        __init__(self, string id="", string name="") -> Model
        __init__(self, string id="") -> Model
        __init__(self) -> Model

        Creates a new Model, optionally with its id and name attributes set.


        """
        _swig_setattr(self, Model, 'this', _libsbml.new_Model(*args))
        _swig_setattr(self, Model, 'thisown', 1)
    def __del__(self, destroy=_libsbml.delete_Model):
        """__del__(self)"""
        try:
            if self.thisown: destroy(self)
        except: pass

    def getId(*args): 
        """
        getId(self) -> string

        Returns the id of this Model.


        """
        return _libsbml.Model_getId(*args)

    def getName(*args): 
        """
        getName(self) -> string

        Returns the name of this Model.


        """
        return _libsbml.Model_getName(*args)

    def isSetId(*args): 
        """
        isSetId(self) -> bool

        Returns true if the id of this Model has been set, false otherwise.


        """
        return _libsbml.Model_isSetId(*args)

    def isSetName(*args): 
        """
        isSetName(self) -> bool

        Returns true if the name of this Model has been set, false otherwise.


        """
        return _libsbml.Model_isSetName(*args)

    def moveAllIdsToNames(*args): 
        """
        moveAllIdsToNames(self)

        Moves the id field to the name field for this Model and all of its
        contituent UnitDefinitions, Compartments, Species, Parameters, and
        Reactions.  This method is used for converting from L2 to L1.

        NOTE: Any object with its name field already set will be skipped.

        @see moveIdToName


        """
        return _libsbml.Model_moveAllIdsToNames(*args)

    def moveAllNamesToIds(*args): 
        """
        moveAllNamesToIds(self)

        Moves the name field to the id field for this Model and all of its
        contituent UnitDefinitions, Compartments, Species, Parameters, and
        Reactions.  This method is used for converting from L1 to L2.

        NOTE: Any object with its id field already set will be skipped.

        @see moveNameToId


        """
        return _libsbml.Model_moveAllNamesToIds(*args)

    def moveIdToName(*args): 
        """
        moveIdToName(self)

        Moves the id field of this Model to its name field (iff name is not
        already set).  This method is used for converting from L2 to L1.


        """
        return _libsbml.Model_moveIdToName(*args)

    def moveNameToId(*args): 
        """
        moveNameToId(self)

        Moves the name field of this Model to its id field (iff id is not
        already set).  This method is used for converting from L1 to L2.


        """
        return _libsbml.Model_moveNameToId(*args)

    def setId(*args): 
        """
        setId(self, string sid)

        Sets the id of this Model to a copy of sid.


        """
        return _libsbml.Model_setId(*args)

    def setName(*args): 
        """
        setName(self, string str)

        Sets the name of this Model to a copy of string (SName in L1).


        """
        return _libsbml.Model_setName(*args)

    def unsetId(*args): 
        """
        unsetId(self)

        Unsets the id of this Model.


        """
        return _libsbml.Model_unsetId(*args)

    def unsetName(*args): 
        """
        unsetName(self)

        Unsets the name of this Model.


        """
        return _libsbml.Model_unsetName(*args)

    def createFunctionDefinition(*args): 
        """
        createFunctionDefinition(self) -> FunctionDefinition

        Creates a new FunctionDefinition inside this Model and returns it.
        This covenience method is equivalent to:

          addFunctionDefinition( FunctionDefinition() );


        """
        return _libsbml.Model_createFunctionDefinition(*args)

    def createUnitDefinition(*args): 
        """
        createUnitDefinition(self) -> UnitDefinition

        Creates a new UnitDefinition inside this Model and returns it.  This
        covenience method is equivalent to:

          addUnitDefinition( UnitDefinition() );


        """
        return _libsbml.Model_createUnitDefinition(*args)

    def createUnit(*args): 
        """
        createUnit(self) -> Unit

        Creates a new Unit inside this Model and returns a pointer to it.  The
        Unit is added to the last UnitDefinition created.

        If a UnitDefinitions does not exist for this model, a new Unit is not
        created and NULL is returned.


        """
        return _libsbml.Model_createUnit(*args)

    def createCompartment(*args): 
        """
        createCompartment(self) -> Compartment

        Creates a new Compartment inside this Model and returns it.  This
        covenience method is equivalent to:

          addCompartment( Compartment() );


        """
        return _libsbml.Model_createCompartment(*args)

    def createSpecies(*args): 
        """
        createSpecies(self) -> Species

        Creates a new Species inside this Model and returns .  This covenience
        method is equivalent to:

          addSpecies( Species() );


        """
        return _libsbml.Model_createSpecies(*args)

    def createParameter(*args): 
        """
        createParameter(self) -> Parameter

        Creates a new Parameter inside this Model and returns.  This
        covenience method is equivalent to:

          addParameter( Parameter() );


        """
        return _libsbml.Model_createParameter(*args)

    def createAssignmentRule(*args): 
        """
        createAssignmentRule(self) -> AssignmentRule

        Creates a new AssignmentRule inside this Model and returns .  This
        covenience method is equivalent to:

          addRule( AssignmentRule() );

        (L2 only)


        """
        return _libsbml.Model_createAssignmentRule(*args)

    def createRateRule(*args): 
        """
        createRateRule(self) -> RateRule

        Creates a new RateRule inside this Model and returns it.  This
        covenience method is equivalent to:

          addRule( RateRule() );

        (L2 only)


        """
        return _libsbml.Model_createRateRule(*args)

    def createAlgebraicRule(*args): 
        """
        createAlgebraicRule(self) -> AlgebraicRule

        Creates a new AlgebraicRule inside this Model and returns it.  This
        covenience method is equivalent to:

          addRule( AlgebraicRule() );


        """
        return _libsbml.Model_createAlgebraicRule(*args)

    def createCompartmentVolumeRule(*args): 
        """
        createCompartmentVolumeRule(self) -> CompartmentVolumeRule

        Creates a new CompartmentVolumeRule inside this Model and returns.
        This covenience method is equivalent to:

          addRule( CompartmentVolumeRule() );


        """
        return _libsbml.Model_createCompartmentVolumeRule(*args)

    def createParameterRule(*args): 
        """
        createParameterRule(self) -> ParameterRule

        Creates a new ParameterRule inside this Model and returns it.  This
        covenience method is equivalent to:

          addRule( ParameterRule() );


        """
        return _libsbml.Model_createParameterRule(*args)

    def createSpeciesConcentrationRule(*args): 
        """
        createSpeciesConcentrationRule(self) -> SpeciesConcentrationRule

        Creates a new SpeciesConcentrationRule inside this Model and returns
        it.  This covenience method is equivalent to:

          addRule( SpeciesConcentrationRule() );


        """
        return _libsbml.Model_createSpeciesConcentrationRule(*args)

    def createReaction(*args): 
        """
        createReaction(self) -> Reaction

        Creates a new Reaction inside this Model and returns.  This covenience
        method is equivalent to:

          addReaction( Reaction() );


        """
        return _libsbml.Model_createReaction(*args)

    def createReactant(*args): 
        """
        createReactant(self) -> SpeciesReference

        Creates a new Reactant (i.e. SpeciesReference) inside this Model and
        returns a pointer to it.  The SpeciesReference is added to the
        reactants of the last Reaction created.

        If a Reaction does not exist for this model, a new SpeciesReference is
        not created and NULL is returned.


        """
        return _libsbml.Model_createReactant(*args)

    def createProduct(*args): 
        """
        createProduct(self) -> SpeciesReference

        Creates a new Product (i.e. SpeciesReference) inside this Model and
        returns a pointer to it.  The SpeciesReference is added to the
        products of the last Reaction created.

        If a Reaction does not exist for this model, a new SpeciesReference is
        not created and NULL is returned.


        """
        return _libsbml.Model_createProduct(*args)

    def createModifier(*args): 
        """
        createModifier(self) -> ModifierSpeciesReference

        Creates a new Modifer (i.e. ModifierSpeciesReference) inside this
        Model and returns a pointer to it.  The ModifierSpeciesReference is
        added to the modifiers of the last Reaction created.

        If a Reaction does not exist for this model, a new
        ModifierSpeciesReference is not created and NULL is returned.


        """
        return _libsbml.Model_createModifier(*args)

    def createKineticLaw(*args): 
        """
        createKineticLaw(self) -> KineticLaw

        Creates a new KineticLaw inside this Model and returns a pointer to
        it.  The KineticLaw is associated with the last Reaction created.

        If a Reaction does not exist for this model, or a Reaction does exist,
        but already has a KineticLaw, a new KineticLaw is not created and NULL
        is returned.


        """
        return _libsbml.Model_createKineticLaw(*args)

    def createKineticLawParameter(*args): 
        """
        createKineticLawParameter(self) -> Parameter

        Creates a new Parameter (of a KineticLaw) inside this Model and
        returns a pointer to it.  The Parameter is associated with the
        KineticLaw of the last Reaction created.

        If a Reaction does not exist for this model, or a KineticLaw for the
        Reaction, a new Parameter is not created and NULL is returned.


        """
        return _libsbml.Model_createKineticLawParameter(*args)

    def createEvent(*args): 
        """
        createEvent(self) -> Event

        Creates a new Event inside this Model and returns.  This covenience
        function is functionally equivalent to:

          addEvent( Event() );


        """
        return _libsbml.Model_createEvent(*args)

    def createEventAssignment(*args): 
        """
        createEventAssignment(self) -> EventAssignment

        Creates a new EventAssignment inside this Model and returns a pointer
        to it.  The EventAssignment is added to the the last Event created.

        If an Event does not exist for this model, a new EventAssignment is
        not created and NULL is returned.


        """
        return _libsbml.Model_createEventAssignment(*args)

    def addFunctionDefinition(*args):
      if args[1] is not None: args[1].thisown = 0
      return _libsbml.Model_addFunctionDefinition(*args)


    def addUnitDefinition(*args):
      if args[1] is not None: args[1].thisown = 0
      return _libsbml.Model_addUnitDefinition(*args)


    def addCompartment(*args):
      if args[1] is not None: args[1].thisown = 0
      return _libsbml.Model_addCompartment(*args)


    def addSpecies(*args):
      if args[1] is not None: args[1].thisown = 0
      return _libsbml.Model_addSpecies(*args)


    def addParameter(*args):
      if args[1] is not None: args[1].thisown = 0
      return _libsbml.Model_addParameter(*args)


    def addRule(*args):
      if args[1] is not None: args[1].thisown = 0
      return _libsbml.Model_addRule(*args)


    def addReaction(*args):
      if args[1] is not None: args[1].thisown = 0
      return _libsbml.Model_addReaction(*args)


    def addEvent(*args):
      if args[1] is not None: args[1].thisown = 0
      return _libsbml.Model_addEvent(*args)


    def getListOfFunctionDefinitions(*args): 
        """
        getListOfFunctionDefinitions(self) -> ListOf

        Returns the list of FunctionDefinitions for this Model.


        """
        return _libsbml.Model_getListOfFunctionDefinitions(*args)

    def getListOfUnitDefinitions(*args): 
        """
        getListOfUnitDefinitions(self) -> ListOf

        Returns the list of UnitDefinitions for this Model.


        """
        return _libsbml.Model_getListOfUnitDefinitions(*args)

    def getListOfCompartments(*args): 
        """
        getListOfCompartments(self) -> ListOf

        Returns the list of Compartments for this Model.


        """
        return _libsbml.Model_getListOfCompartments(*args)

    def getListOfSpecies(*args): 
        """
        getListOfSpecies(self) -> ListOf

        Returns the list of Species for this Model.


        """
        return _libsbml.Model_getListOfSpecies(*args)

    def getListOfParameters(*args): 
        """
        getListOfParameters(self) -> ListOf

        Returns the list of Parameters for this Model.


        """
        return _libsbml.Model_getListOfParameters(*args)

    def getListOfRules(*args): 
        """
        getListOfRules(self) -> ListOf

        Returns the list of Rules for this Model.


        """
        return _libsbml.Model_getListOfRules(*args)

    def getListOfReactions(*args): 
        """
        getListOfReactions(self) -> ListOf

        Returns the list of Rules for this Model.


        """
        return _libsbml.Model_getListOfReactions(*args)

    def getListOfEvents(*args): 
        """
        getListOfEvents(self) -> ListOf

        Returns the list of Rules for this Model.


        """
        return _libsbml.Model_getListOfEvents(*args)

    def getFunctionDefinition(*args): 
        """
        getFunctionDefinition(self, unsigned int n) -> FunctionDefinition
        getFunctionDefinition(self, string sid) -> FunctionDefinition

        Returns the FunctionDefinition in this Model with the given id or NULL
        if no such FunctionDefinition exists.


        """
        return _libsbml.Model_getFunctionDefinition(*args)

    def getUnitDefinition(*args): 
        """
        getUnitDefinition(self, unsigned int n) -> UnitDefinition
        getUnitDefinition(self, string sid) -> UnitDefinition

        Returns the UnitDefinition in this Model with the given id or NULL if
        no such UnitDefinition exists.


        """
        return _libsbml.Model_getUnitDefinition(*args)

    def getCompartment(*args): 
        """
        getCompartment(self, unsigned int n) -> Compartment
        getCompartment(self, string sid) -> Compartment

        Returns the Compartment in this Model with the given id or NULL if no
        such Compartment exists.


        """
        return _libsbml.Model_getCompartment(*args)

    def getSpecies(*args): 
        """
        getSpecies(self, unsigned int n) -> Species
        getSpecies(self, string sid) -> Species

        Returns the Species in this Model with the given id or NULL if no such
        Species exists.


        """
        return _libsbml.Model_getSpecies(*args)

    def getParameter(*args): 
        """
        getParameter(self, unsigned int n) -> Parameter
        getParameter(self, string sid) -> Parameter

        Returns the Parameter in this Model with the given id or NULL if no
        such Parameter exists.


        """
        return _libsbml.Model_getParameter(*args)

    def getRule(*args): 
        """
        getRule(self, unsigned int n) -> Rule

        Returns the nth Rule of this Model.


        """
        return _libsbml.Model_getRule(*args)

    def getReaction(*args): 
        """
        getReaction(self, unsigned int n) -> Reaction
        getReaction(self, string sid) -> Reaction

        Returns the Reaction in this Model with the given id or NULL if no
        such Reaction exists.


        """
        return _libsbml.Model_getReaction(*args)

    def getEvent(*args): 
        """
        getEvent(self, unsigned int n) -> Event
        getEvent(self, string sid) -> Event

        Returns the Event in this Model with the given id or NULL if no such
        Event exists.


        """
        return _libsbml.Model_getEvent(*args)

    def getNumFunctionDefinitions(*args): 
        """
        getNumFunctionDefinitions(self) -> unsigned int

        Returns the number of FunctionDefinitions in this Model.


        """
        return _libsbml.Model_getNumFunctionDefinitions(*args)

    def getNumUnitDefinitions(*args): 
        """
        getNumUnitDefinitions(self) -> unsigned int

        Returns the number of UnitDefinitions in this Model.


        """
        return _libsbml.Model_getNumUnitDefinitions(*args)

    def getNumCompartments(*args): 
        """
        getNumCompartments(self) -> unsigned int

        Returns the number of Compartments in this Model.


        """
        return _libsbml.Model_getNumCompartments(*args)

    def getNumSpecies(*args): 
        """
        getNumSpecies(self) -> unsigned int

        Returns the number of Species in this Model.


        """
        return _libsbml.Model_getNumSpecies(*args)

    def getNumSpeciesWithBoundaryCondition(*args): 
        """
        getNumSpeciesWithBoundaryCondition(self) -> unsigned int

        Returns the number of Species in this Model with boundaryCondition set
        to true.


        """
        return _libsbml.Model_getNumSpeciesWithBoundaryCondition(*args)

    def getNumParameters(*args): 
        """
        getNumParameters(self) -> unsigned int

        Returns the number of Parameters in this Model.  Parameters defined in
        KineticLaws are not included.


        """
        return _libsbml.Model_getNumParameters(*args)

    def getNumRules(*args): 
        """
        getNumRules(self) -> unsigned int

        Returns the number of Rules in this Model.


        """
        return _libsbml.Model_getNumRules(*args)

    def getNumReactions(*args): 
        """
        getNumReactions(self) -> unsigned int

        Returns the number of Reactions in this Model.


        """
        return _libsbml.Model_getNumReactions(*args)

    def getNumEvents(*args): 
        """
        getNumEvents(self) -> unsigned int

        Returns the number of Events in this Model.


        """
        return _libsbml.Model_getNumEvents(*args)

    def isBoolean(*args): 
        """isBoolean(self, ASTNode node) -> bool"""
        return _libsbml.Model_isBoolean(*args)


class ModelPtr(Model):
    def __init__(self, this):
        _swig_setattr(self, Model, 'this', this)
        if not hasattr(self,"thisown"): _swig_setattr(self, Model, 'thisown', 0)
        _swig_setattr(self, Model,self.__class__,Model)
_libsbml.Model_swigregister(ModelPtr)

class SBMLDocument(SBase):
    """Proxy of C++ SBMLDocument class"""
    __swig_setmethods__ = {}
    for _s in [SBase]: __swig_setmethods__.update(_s.__swig_setmethods__)
    __setattr__ = lambda self, name, value: _swig_setattr(self, SBMLDocument, name, value)
    __swig_getmethods__ = {}
    for _s in [SBase]: __swig_getmethods__.update(_s.__swig_getmethods__)
    __getattr__ = lambda self, name: _swig_getattr(self, SBMLDocument, name)
    def __repr__(self):
        return "<%s.%s; proxy of C++ SBMLDocument instance at %s>" % (self.__class__.__module__, self.__class__.__name__, self.this,)
    def __init__(self, *args):
        """
        __init__(self, unsigned int level=2, unsigned int version=1) -> SBMLDocument
        __init__(self, unsigned int level=2) -> SBMLDocument
        __init__(self) -> SBMLDocument

        Creates a new SBMLDocument.  The SBML level defaults to 2 and version
        defaults to 1.


        """
        _swig_setattr(self, SBMLDocument, 'this', _libsbml.new_SBMLDocument(*args))
        _swig_setattr(self, SBMLDocument, 'thisown', 1)
    def __del__(self, destroy=_libsbml.delete_SBMLDocument):
        """__del__(self)"""
        try:
            if self.thisown: destroy(self)
        except: pass

    def createModel(*args): 
        """
        createModel(self, string sid="") -> Model
        createModel(self) -> Model

        Creates a new Model (optionally with its Id attribute set) inside this
        SBMLDocument and returns it.  This covenience method is equivalent to:

          setModel( Model() );


        """
        return _libsbml.SBMLDocument_createModel(*args)

    def getLevel(*args): 
        """
        getLevel(self) -> unsigned int

        Returns the level of this SBMLDocument.


        """
        return _libsbml.SBMLDocument_getLevel(*args)

    def getVersion(*args): 
        """
        getVersion(self) -> unsigned int

        Returns the version of this SBMLDocument.


        """
        return _libsbml.SBMLDocument_getVersion(*args)

    def getModel(*args): 
        """
        getModel(self) -> Model

        Returns the Model associated with this SBMLDocument.


        """
        return _libsbml.SBMLDocument_getModel(*args)

    def getWarning(*args): 
        """
        getWarning(self, unsigned int n) -> ParseMessage

        Returns the nth warning encountered during the parse of this
        SBMLDocument or NULL if n > getNumWarnings() - 1.


        """
        return _libsbml.SBMLDocument_getWarning(*args)

    def getError(*args): 
        """
        getError(self, unsigned int n) -> ParseMessage

        Returns the nth error encountered during the parse of this
        SBMLDocument or NULL if n > getNumErrors() - 1.


        """
        return _libsbml.SBMLDocument_getError(*args)

    def getFatal(*args): 
        """
        getFatal(self, unsigned int n) -> ParseMessage

        Returns the nth fatal error encountered during the parse of this
        SBMLDocument or NULL if n > getNumFatals() - 1.


        """
        return _libsbml.SBMLDocument_getFatal(*args)

    def getNumWarnings(*args): 
        """
        getNumWarnings(self) -> unsigned int

        Returns the number of warnings encountered during the parse of this
        SBMLDocument.


        """
        return _libsbml.SBMLDocument_getNumWarnings(*args)

    def getNumErrors(*args): 
        """
        getNumErrors(self) -> unsigned int

        Returns the number of errors encountered during the parse of this
        SBMLDocument.


        """
        return _libsbml.SBMLDocument_getNumErrors(*args)

    def getNumFatals(*args): 
        """
        getNumFatals(self) -> unsigned int

        Returns the number of fatal errors encountered during the parse of
        this SBMLDocument.


        """
        return _libsbml.SBMLDocument_getNumFatals(*args)

    def setLevel(*args): 
        """
        setLevel(self, unsigned int level)

        Sets the level of this SBMLDocument to the given level number.  Valid
        levels are currently 1 and 2.


        """
        return _libsbml.SBMLDocument_setLevel(*args)

    def setVersion(*args): 
        """
        setVersion(self, unsigned int version)

        Sets the version of this SBMLDocument to the given version number.
        Valid versions are currently 1 and 2 for SBML L1 and 1 for SBML L2.


        """
        return _libsbml.SBMLDocument_setVersion(*args)

    def setModel(*args):
      if args[1] is not None: args[1].thisown = 0
      return _libsbml.SBMLDocument_setModel(*args)


    def checkConsistency(*args): 
        """
        checkConsistency(self) -> unsigned int

        Performs a set of semantic consistency checks on the document.  Query
        the results by calling getWarning(), getNumError(),and getNumFatal().

        Returns the number of failed checks (errors) encountered.


        """
        return _libsbml.SBMLDocument_checkConsistency(*args)

    def validate(*args): 
        """
        validate(self) -> unsigned int

        @deprecated use checkConsistency() instead.


        """
        return _libsbml.SBMLDocument_validate(*args)

    def checkL1Compatibility(*args): 
        """
        checkL1Compatibility(self) -> unsigned int

        Performs a set of semantic consistency checks on the document to establish
        whether it is compatible with L1 and can be converted.  Query
        the results by calling getWarning(), getNumError(),and getNumFatal().

        Returns the number of failed checks (errors) encountered.


        """
        return _libsbml.SBMLDocument_checkL1Compatibility(*args)


class SBMLDocumentPtr(SBMLDocument):
    def __init__(self, this):
        _swig_setattr(self, SBMLDocument, 'this', this)
        if not hasattr(self,"thisown"): _swig_setattr(self, SBMLDocument, 'thisown', 0)
        _swig_setattr(self, SBMLDocument,self.__class__,SBMLDocument)
_libsbml.SBMLDocument_swigregister(SBMLDocumentPtr)

class FunctionDefinition(SBase):
    """Proxy of C++ FunctionDefinition class"""
    __swig_setmethods__ = {}
    for _s in [SBase]: __swig_setmethods__.update(_s.__swig_setmethods__)
    __setattr__ = lambda self, name, value: _swig_setattr(self, FunctionDefinition, name, value)
    __swig_getmethods__ = {}
    for _s in [SBase]: __swig_getmethods__.update(_s.__swig_getmethods__)
    __getattr__ = lambda self, name: _swig_getattr(self, FunctionDefinition, name)
    def __repr__(self):
        return "<%s.%s; proxy of C++ FunctionDefinition instance at %s>" % (self.__class__.__module__, self.__class__.__name__, self.this,)
    def __init__(self, *args):
      _swig_setattr(self, FunctionDefinition, 'this', _libsbml.new_FunctionDefinition(*args))
      _swig_setattr(self, FunctionDefinition, 'thisown', 1)
      try:
        if args[1] is not None: args[1].thisown = 0
      except (IndexError, AttributeError):
        pass


    def __del__(self, destroy=_libsbml.delete_FunctionDefinition):
        """__del__(self)"""
        try:
            if self.thisown: destroy(self)
        except: pass

    def getId(*args): 
        """
        getId(self) -> string

        Returns the id of this FunctionDefinition.


        """
        return _libsbml.FunctionDefinition_getId(*args)

    def getName(*args): 
        """
        getName(self) -> string

        Returns the name of this FunctionDefinition.


        """
        return _libsbml.FunctionDefinition_getName(*args)

    def getMath(*args): 
        """
        getMath(self) -> ASTNode

        Returns the math of this FunctionDefinition.


        """
        return _libsbml.FunctionDefinition_getMath(*args)

    def isSetId(*args): 
        """
        isSetId(self) -> bool

        Returns true if the id of this FunctionDefinition has been set, false
        otherwise.


        """
        return _libsbml.FunctionDefinition_isSetId(*args)

    def isSetName(*args): 
        """
        isSetName(self) -> bool

        Returns true if the name of this FunctionDefinition has been set, false
        otherwise.


        """
        return _libsbml.FunctionDefinition_isSetName(*args)

    def isSetMath(*args): 
        """
        isSetMath(self) -> bool

        Returns true if the math of this FunctionDefinition has been set, false
        otherwise.


        """
        return _libsbml.FunctionDefinition_isSetMath(*args)

    def setId(*args): 
        """
        setId(self, string sid)

        Sets the id of this FunctionDefinition to a copy of sid.


        """
        return _libsbml.FunctionDefinition_setId(*args)

    def setName(*args): 
        """
        setName(self, string str)

        Sets the name of this FunctionDefinition to a copy of string.


        """
        return _libsbml.FunctionDefinition_setName(*args)

    def setMath(*args):
      if args[1] is not None: args[1].thisown = 0
      return _libsbml.FunctionDefinition_setMath(*args)


    def unsetName(*args): 
        """
        unsetName(self)

        Unsets the name of this FunctionDefinition.


        """
        return _libsbml.FunctionDefinition_unsetName(*args)


class FunctionDefinitionPtr(FunctionDefinition):
    def __init__(self, this):
        _swig_setattr(self, FunctionDefinition, 'this', this)
        if not hasattr(self,"thisown"): _swig_setattr(self, FunctionDefinition, 'thisown', 0)
        _swig_setattr(self, FunctionDefinition,self.__class__,FunctionDefinition)
_libsbml.FunctionDefinition_swigregister(FunctionDefinitionPtr)

class Unit(SBase):
    """Proxy of C++ Unit class"""
    __swig_setmethods__ = {}
    for _s in [SBase]: __swig_setmethods__.update(_s.__swig_setmethods__)
    __setattr__ = lambda self, name, value: _swig_setattr(self, Unit, name, value)
    __swig_getmethods__ = {}
    for _s in [SBase]: __swig_getmethods__.update(_s.__swig_getmethods__)
    __getattr__ = lambda self, name: _swig_getattr(self, Unit, name)
    def __repr__(self):
        return "<%s.%s; proxy of C++ Unit instance at %s>" % (self.__class__.__module__, self.__class__.__name__, self.this,)
    def __init__(self, *args):
        """
        __init__(self, UnitKind_t kind=UNIT_KIND_INVALID, int exponent=1, 
            int scale=0, double multiplier=1.0, double offset=0.0) -> Unit
        __init__(self, UnitKind_t kind=UNIT_KIND_INVALID, int exponent=1, 
            int scale=0, double multiplier=1.0) -> Unit
        __init__(self, UnitKind_t kind=UNIT_KIND_INVALID, int exponent=1, 
            int scale=0) -> Unit
        __init__(self, UnitKind_t kind=UNIT_KIND_INVALID, int exponent=1) -> Unit
        __init__(self, UnitKind_t kind=UNIT_KIND_INVALID) -> Unit
        __init__(self) -> Unit
        __init__(self, string kind, int exponent=1, int scale=0, double multiplier=1.0, 
            double offset=0.0) -> Unit
        __init__(self, string kind, int exponent=1, int scale=0, double multiplier=1.0) -> Unit
        __init__(self, string kind, int exponent=1, int scale=0) -> Unit
        __init__(self, string kind, int exponent=1) -> Unit
        __init__(self, string kind) -> Unit

        Creates a new Unit, optionally with its kind (via string), exponent,
        scale, multiplier, and offset attributes set.


        """
        _swig_setattr(self, Unit, 'this', _libsbml.new_Unit(*args))
        _swig_setattr(self, Unit, 'thisown', 1)
    def __del__(self, destroy=_libsbml.delete_Unit):
        """__del__(self)"""
        try:
            if self.thisown: destroy(self)
        except: pass

    def initDefaults(*args): 
        """
        initDefaults(self)

        Initializes the fields of this Unit to their defaults:

          - exponent   = 1
          - scale      = 0
          - multiplier = 1.0
          - offset     = 0.0


        """
        return _libsbml.Unit_initDefaults(*args)

    def getKind(*args): 
        """
        getKind(self) -> int

        Returns the kind of this Unit.


        """
        return _libsbml.Unit_getKind(*args)

    def getExponent(*args): 
        """
        getExponent(self) -> int

        Returns the exponent of this Unit.


        """
        return _libsbml.Unit_getExponent(*args)

    def getScale(*args): 
        """
        getScale(self) -> int

        Returns the scale of this Unit.


        """
        return _libsbml.Unit_getScale(*args)

    def getMultiplier(*args): 
        """
        getMultiplier(self) -> double

        Returns the multiplier of this Unit.


        """
        return _libsbml.Unit_getMultiplier(*args)

    def getOffset(*args): 
        """
        getOffset(self) -> double

        Returns the offset of this Unit.


        """
        return _libsbml.Unit_getOffset(*args)

    def isAmpere(*args): 
        """
        isAmpere(self) -> bool

        Returns true if the kind of this Unit is 'ampere', false otherwise.


        """
        return _libsbml.Unit_isAmpere(*args)

    def isBecquerel(*args): 
        """
        isBecquerel(self) -> bool

        Returns true if the kind of this Unit is 'becquerel', false otherwise.


        """
        return _libsbml.Unit_isBecquerel(*args)

    def isCandela(*args): 
        """
        isCandela(self) -> bool

        Returns true if the kind of this Unit is 'candela', false otherwise.


        """
        return _libsbml.Unit_isCandela(*args)

    def isCelsius(*args): 
        """
        isCelsius(self) -> bool

        Returns true if the kind of this Unit is 'Celsius', false otherwise.


        """
        return _libsbml.Unit_isCelsius(*args)

    def isCoulomb(*args): 
        """
        isCoulomb(self) -> bool

        Returns true if the kind of this Unit is 'coulomb', false otherwise.


        """
        return _libsbml.Unit_isCoulomb(*args)

    def isDimensionless(*args): 
        """
        isDimensionless(self) -> bool

        Returns true if the kind of this Unit is 'dimensionless', false
        otherwise.


        """
        return _libsbml.Unit_isDimensionless(*args)

    def isFarad(*args): 
        """
        isFarad(self) -> bool

        Returns true if the kind of this Unit is 'farad', false otherwise.


        """
        return _libsbml.Unit_isFarad(*args)

    def isGram(*args): 
        """
        isGram(self) -> bool

        Returns true if the kind of this Unit is 'gram', false otherwise.


        """
        return _libsbml.Unit_isGram(*args)

    def isGray(*args): 
        """
        isGray(self) -> bool

        Returns true if the kind of this Unit is 'gray', false otherwise.


        """
        return _libsbml.Unit_isGray(*args)

    def isHenry(*args): 
        """
        isHenry(self) -> bool

        Returns true if the kind of this Unit is 'henry', false otherwise.


        """
        return _libsbml.Unit_isHenry(*args)

    def isHertz(*args): 
        """
        isHertz(self) -> bool

        Returns true if the kind of this Unit is 'hertz', false otherwise.


        """
        return _libsbml.Unit_isHertz(*args)

    def isItem(*args): 
        """
        isItem(self) -> bool

        Returns true if the kind of this Unit is 'item', false otherwise.


        """
        return _libsbml.Unit_isItem(*args)

    def isJoule(*args): 
        """
        isJoule(self) -> bool

        Returns true if the kind of this Unit is 'joule', false otherwise.


        """
        return _libsbml.Unit_isJoule(*args)

    def isKatal(*args): 
        """
        isKatal(self) -> bool

        Returns true if the kind of this Unit is 'katal', false otherwise.


        """
        return _libsbml.Unit_isKatal(*args)

    def isKelvin(*args): 
        """
        isKelvin(self) -> bool

        Returns true if the kind of this Unit is 'kelvin', false otherwise.


        """
        return _libsbml.Unit_isKelvin(*args)

    def isKilogram(*args): 
        """
        isKilogram(self) -> bool

        Returns true if the kind of this Unit is 'kilogram', false otherwise.


        """
        return _libsbml.Unit_isKilogram(*args)

    def isLitre(*args): 
        """
        isLitre(self) -> bool

        Returns true if the kind of this Unit is 'litre' or 'liter', false
        otherwise.


        """
        return _libsbml.Unit_isLitre(*args)

    def isLumen(*args): 
        """
        isLumen(self) -> bool

        Returns true if the kind of this Unit is 'lumen', false otherwise.


        """
        return _libsbml.Unit_isLumen(*args)

    def isLux(*args): 
        """
        isLux(self) -> bool

        Returns true if the kind of this Unit is 'lux', false otherwise.


        """
        return _libsbml.Unit_isLux(*args)

    def isMetre(*args): 
        """
        isMetre(self) -> bool

        Returns true if the kind of this Unit is 'metre' or 'meter', false
        otherwise.


        """
        return _libsbml.Unit_isMetre(*args)

    def isMole(*args): 
        """
        isMole(self) -> bool

        Returns true if the kind of this Unit is 'mole', false otherwise.


        """
        return _libsbml.Unit_isMole(*args)

    def isNewton(*args): 
        """
        isNewton(self) -> bool

        Returns true if the kind of this Unit is 'newton', false otherwise.


        """
        return _libsbml.Unit_isNewton(*args)

    def isOhm(*args): 
        """
        isOhm(self) -> bool

        Returns true if the kind of this Unit is 'ohm', false otherwise.


        """
        return _libsbml.Unit_isOhm(*args)

    def isPascal(*args): 
        """
        isPascal(self) -> bool

        Returns true if the kind of this Unit is 'pascal', false otherwise.


        """
        return _libsbml.Unit_isPascal(*args)

    def isRadian(*args): 
        """
        isRadian(self) -> bool

        Returns true if the kind of this Unit is 'radian', false otherwise.


        """
        return _libsbml.Unit_isRadian(*args)

    def isSecond(*args): 
        """
        isSecond(self) -> bool

        Returns true if the kind of this Unit is 'second', false otherwise.


        """
        return _libsbml.Unit_isSecond(*args)

    def isSiemens(*args): 
        """
        isSiemens(self) -> bool

        Returns true if the kind of this Unit is 'siemens', false otherwise.


        """
        return _libsbml.Unit_isSiemens(*args)

    def isSievert(*args): 
        """
        isSievert(self) -> bool

        Returns true if the kind of this Unit is 'sievert', false otherwise.


        """
        return _libsbml.Unit_isSievert(*args)

    def isSteradian(*args): 
        """
        isSteradian(self) -> bool

        Returns true if the kind of this Unit is 'steradian', false otherwise.


        """
        return _libsbml.Unit_isSteradian(*args)

    def isTesla(*args): 
        """
        isTesla(self) -> bool

        Returns true if the kind of this Unit is 'tesla', false otherwise.


        """
        return _libsbml.Unit_isTesla(*args)

    def isVolt(*args): 
        """
        isVolt(self) -> bool

        Returns true if the kind of this Unit is 'volt', false otherwise.


        """
        return _libsbml.Unit_isVolt(*args)

    def isWatt(*args): 
        """
        isWatt(self) -> bool

        Returns true if the kind of this Unit is 'watt', false otherwise.


        """
        return _libsbml.Unit_isWatt(*args)

    def isWeber(*args): 
        """
        isWeber(self) -> bool

        Returns true if the kind of this Unit is 'weber', false otherwise.


        """
        return _libsbml.Unit_isWeber(*args)

    def isSetKind(*args): 
        """
        isSetKind(self) -> bool

        Returns true if the kind of this Unit has been set, false otherwise.


        """
        return _libsbml.Unit_isSetKind(*args)

    def setKind(*args): 
        """
        setKind(self, UnitKind_t kind)

        Sets the kind of this Unit to the given UnitKind.


        """
        return _libsbml.Unit_setKind(*args)

    def setExponent(*args): 
        """
        setExponent(self, int value)

        Sets the exponent of this Unit to the given value.


        """
        return _libsbml.Unit_setExponent(*args)

    def setScale(*args): 
        """
        setScale(self, int value)

        Sets the scale of this Unit to the given value.


        """
        return _libsbml.Unit_setScale(*args)

    def setMultiplier(*args): 
        """
        setMultiplier(self, double value)

        Sets the multiplier of this Unit to the given value.


        """
        return _libsbml.Unit_setMultiplier(*args)

    def setOffset(*args): 
        """
        setOffset(self, double value)

        Sets the offset of this Unit to the given value.


        """
        return _libsbml.Unit_setOffset(*args)

    __swig_getmethods__["isBuiltIn"] = lambda x: _libsbml.Unit_isBuiltIn
    if _newclass:isBuiltIn = staticmethod(_libsbml.Unit_isBuiltIn)
    __swig_getmethods__["isUnitKind"] = lambda x: _libsbml.Unit_isUnitKind
    if _newclass:isUnitKind = staticmethod(_libsbml.Unit_isUnitKind)

class UnitPtr(Unit):
    def __init__(self, this):
        _swig_setattr(self, Unit, 'this', this)
        if not hasattr(self,"thisown"): _swig_setattr(self, Unit, 'thisown', 0)
        _swig_setattr(self, Unit,self.__class__,Unit)
_libsbml.Unit_swigregister(UnitPtr)

def Unit_isBuiltIn(*args):
    """
    Unit_isBuiltIn(string name) -> bool

    Returns true if name is one of the five SBML builtin Unit names
    ('substance', 'volume', 'area', 'length' or 'time'), false otherwise.


    """
    return _libsbml.Unit_isBuiltIn(*args)

def Unit_isUnitKind(*args):
    """
    Unit_isUnitKind(string name) -> bool

    Returns true if name is a valid UnitKind.


    """
    return _libsbml.Unit_isUnitKind(*args)

class UnitDefinition(SBase):
    """Proxy of C++ UnitDefinition class"""
    __swig_setmethods__ = {}
    for _s in [SBase]: __swig_setmethods__.update(_s.__swig_setmethods__)
    __setattr__ = lambda self, name, value: _swig_setattr(self, UnitDefinition, name, value)
    __swig_getmethods__ = {}
    for _s in [SBase]: __swig_getmethods__.update(_s.__swig_getmethods__)
    __getattr__ = lambda self, name: _swig_getattr(self, UnitDefinition, name)
    def __repr__(self):
        return "<%s.%s; proxy of C++ UnitDefinition instance at %s>" % (self.__class__.__module__, self.__class__.__name__, self.this,)
    def __init__(self, *args):
        """
        __init__(self, string id="", string name="") -> UnitDefinition
        __init__(self, string id="") -> UnitDefinition
        __init__(self) -> UnitDefinition

        Creates a new UnitDefinition, optionally with its id and name
        attributes set.


        """
        _swig_setattr(self, UnitDefinition, 'this', _libsbml.new_UnitDefinition(*args))
        _swig_setattr(self, UnitDefinition, 'thisown', 1)
    def __del__(self, destroy=_libsbml.delete_UnitDefinition):
        """__del__(self)"""
        try:
            if self.thisown: destroy(self)
        except: pass

    def getId(*args): 
        """
        getId(self) -> string

        Returns the id of this UnitDefinition.


        """
        return _libsbml.UnitDefinition_getId(*args)

    def getName(*args): 
        """
        getName(self) -> string

        Returns the name of this UnitDefinition.


        """
        return _libsbml.UnitDefinition_getName(*args)

    def isSetId(*args): 
        """
        isSetId(self) -> bool

        Returns true if the id of this UnitDefinition has been set, false
        otherwise.


        """
        return _libsbml.UnitDefinition_isSetId(*args)

    def isSetName(*args): 
        """
        isSetName(self) -> bool

        Returns true if the name of this UnitDefinition has been set, false
        otherwise.

        In SBML L1, a UnitDefinition name is required and therefore <b>should
        always be set</b>.  In L2, name is optional and as such may or may not
        be set.


        """
        return _libsbml.UnitDefinition_isSetName(*args)

    def isVariantOfArea(*args): 
        """
        isVariantOfArea(self) -> bool

        Returns true if this UnitDefinition is a variant of the builtin type
        area, i.e. square metres with only abritrary variations in scale,
        multiplier, or offset values, false otherwise.


        """
        return _libsbml.UnitDefinition_isVariantOfArea(*args)

    def isVariantOfLength(*args): 
        """
        isVariantOfLength(self) -> bool

        Returns true if this UnitDefinition is a variant of the builtin type
        length, i.e. metres with only abritrary variations in scale,
        multiplier, or offset values, false otherwise.


        """
        return _libsbml.UnitDefinition_isVariantOfLength(*args)

    def isVariantOfSubstance(*args): 
        """
        isVariantOfSubstance(self) -> bool

        Returns true if this UnitDefinition is a variant of the builtin type
        substance, i.e. moles or items with only abritrary variations in
        scale, multiplier, or offset values, false otherwise.


        """
        return _libsbml.UnitDefinition_isVariantOfSubstance(*args)

    def isVariantOfTime(*args): 
        """
        isVariantOfTime(self) -> bool

        Returns true if this UnitDefinition is a variant of the builtin type
        time, i.e. seconds with only abritrary variations in scale,
        multiplier, or offset values, false otherwise.


        """
        return _libsbml.UnitDefinition_isVariantOfTime(*args)

    def isVariantOfVolume(*args): 
        """
        isVariantOfVolume(self) -> bool

        Returns true if this UnitDefinition is a variant of the builtin type
        volume, i.e. litre or cubic metre with only abritrary variations in
        scale, multiplier, or offset values, false otherwise.


        """
        return _libsbml.UnitDefinition_isVariantOfVolume(*args)

    def moveIdToName(*args): 
        """
        moveIdToName(self)

        Moves the id field of this UnitDefinition to its name field (iff name
        is not already set).  This method is used for converting from L2 to
        L1.


        """
        return _libsbml.UnitDefinition_moveIdToName(*args)

    def moveNameToId(*args): 
        """
        moveNameToId(self)

        Moves the name field of this UnitDefinition to its id field (iff id is
        not already set).  This method is used for converting from L1 to L2.


        """
        return _libsbml.UnitDefinition_moveNameToId(*args)

    def setId(*args): 
        """
        setId(self, string sid)

        Sets the id of this UnitDefinition to a copy of sid.


        """
        return _libsbml.UnitDefinition_setId(*args)

    def setName(*args): 
        """
        setName(self, string str)

        Sets the name of this UnitDefinition to a copy of string (SName in
        L1).


        """
        return _libsbml.UnitDefinition_setName(*args)

    def unsetName(*args): 
        """
        unsetName(self)

        Unsets the name of this UnitDefinition.

        In SBML L1, a UnitDefinition name is required and therefore <b>should
        always be set</b>.  In L2, name is optional and as such may or may not
        be set.


        """
        return _libsbml.UnitDefinition_unsetName(*args)

    def addUnit(*args):
      if args[1] is not None: args[1].thisown = 0
      return _libsbml.UnitDefinition_addUnit(*args)


    def getListOfUnits(*args): 
        """
        getListOfUnits(self) -> ListOf

        Returns the list of Units for this UnitDefinition.


        """
        return _libsbml.UnitDefinition_getListOfUnits(*args)

    def getUnit(*args): 
        """
        getUnit(self, unsigned int n) -> Unit

        Returns the nth Unit of this UnitDefinition


        """
        return _libsbml.UnitDefinition_getUnit(*args)

    def getNumUnits(*args): 
        """
        getNumUnits(self) -> unsigned int

        Returns the number of Units in this UnitDefinition.


        """
        return _libsbml.UnitDefinition_getNumUnits(*args)


class UnitDefinitionPtr(UnitDefinition):
    def __init__(self, this):
        _swig_setattr(self, UnitDefinition, 'this', this)
        if not hasattr(self,"thisown"): _swig_setattr(self, UnitDefinition, 'thisown', 0)
        _swig_setattr(self, UnitDefinition,self.__class__,UnitDefinition)
_libsbml.UnitDefinition_swigregister(UnitDefinitionPtr)

class Compartment(SBase):
    """Proxy of C++ Compartment class"""
    __swig_setmethods__ = {}
    for _s in [SBase]: __swig_setmethods__.update(_s.__swig_setmethods__)
    __setattr__ = lambda self, name, value: _swig_setattr(self, Compartment, name, value)
    __swig_getmethods__ = {}
    for _s in [SBase]: __swig_getmethods__.update(_s.__swig_getmethods__)
    __getattr__ = lambda self, name: _swig_getattr(self, Compartment, name)
    def __repr__(self):
        return "<%s.%s; proxy of C++ Compartment instance at %s>" % (self.__class__.__module__, self.__class__.__name__, self.this,)
    def __init__(self, *args):
        """
        __init__(self, string id="") -> Compartment
        __init__(self) -> Compartment

        Creates a new Compartment, optionally with its id attribute set.


        """
        _swig_setattr(self, Compartment, 'this', _libsbml.new_Compartment(*args))
        _swig_setattr(self, Compartment, 'thisown', 1)
    def __del__(self, destroy=_libsbml.delete_Compartment):
        """__del__(self)"""
        try:
            if self.thisown: destroy(self)
        except: pass

    def initDefaults(*args): 
        """
        initDefaults(self)

        Initializes the fields of this Compartment to their defaults:

          - volume            = 1.0          (L1 only)
          - spatialDimensions = 3            (L2 only)
          - constant          = 1    (true)  (L2 only)


        """
        return _libsbml.Compartment_initDefaults(*args)

    def getId(*args): 
        """
        getId(self) -> string

        Returns the id of this Compartment.


        """
        return _libsbml.Compartment_getId(*args)

    def getName(*args): 
        """
        getName(self) -> string

        Returns the name of this Compartment.


        """
        return _libsbml.Compartment_getName(*args)

    def getSpatialDimensions(*args): 
        """
        getSpatialDimensions(self) -> unsigned int

        Returns the spatialDimensions of this Compartment.


        """
        return _libsbml.Compartment_getSpatialDimensions(*args)

    def getSize(*args): 
        """
        getSize(self) -> double

        Returns the size (volume in L1) of this Compartment.


        """
        return _libsbml.Compartment_getSize(*args)

    def getVolume(*args): 
        """
        getVolume(self) -> double

        Returns the volume (size in L2) of this Compartment.


        """
        return _libsbml.Compartment_getVolume(*args)

    def getUnits(*args): 
        """
        getUnits(self) -> string

        Returns the units of this Compartment.


        """
        return _libsbml.Compartment_getUnits(*args)

    def getOutside(*args): 
        """
        getOutside(self) -> string

        Returns the outside of this Compartment.


        """
        return _libsbml.Compartment_getOutside(*args)

    def getConstant(*args): 
        """
        getConstant(self) -> bool

        Returns true if this Compartment is constant, false otherwise.


        """
        return _libsbml.Compartment_getConstant(*args)

    def isSetId(*args): 
        """
        isSetId(self) -> bool

        Returns true if the id of this Compartment has been set, false
        otherwise.


        """
        return _libsbml.Compartment_isSetId(*args)

    def isSetName(*args): 
        """
        isSetName(self) -> bool

        Returns true if the name of this Compartment has been set, false
        otherwise.

        In SBML L1, a Compartment name is required and therefore <b>should
        always be set</b>.  In L2, name is optional and as such may or may not
        be set.


        """
        return _libsbml.Compartment_isSetName(*args)

    def isSetSize(*args): 
        """
        isSetSize(self) -> bool

        Returns true if the size (volume in L1) of this Compartment has been
        set, false otherwise.


        """
        return _libsbml.Compartment_isSetSize(*args)

    def isSetVolume(*args): 
        """
        isSetVolume(self) -> bool

        Returns true if the volume (size in L2) of this Compartment has been
        set, false otherwise.

        In SBML L1, a Compartment volume has a default value (1.0) and
        therefore <b>should always be set</b>.  In L2, volume (size) is
        optional with no default value and as such may or may not be set.


        """
        return _libsbml.Compartment_isSetVolume(*args)

    def isSetUnits(*args): 
        """
        isSetUnits(self) -> bool

        Returns true if the units of this Compartment has been set, false
        otherwise.


        """
        return _libsbml.Compartment_isSetUnits(*args)

    def isSetOutside(*args): 
        """
        isSetOutside(self) -> bool

        Returns true if the outside of this Compartment has been set, false
        otherwise.


        """
        return _libsbml.Compartment_isSetOutside(*args)

    def moveIdToName(*args): 
        """
        moveIdToName(self)

        Moves the id field of this Compartment to its name field (iff name is
        not already set).  This method is used for converting from L2 to L1.


        """
        return _libsbml.Compartment_moveIdToName(*args)

    def moveNameToId(*args): 
        """
        moveNameToId(self)

        Moves the name field of this Compartment to its id field (iff id is
        not already set).  This method is used for converting from L1 to L2.


        """
        return _libsbml.Compartment_moveNameToId(*args)

    def setId(*args): 
        """
        setId(self, string sid)

        Sets the id of this Compartment to a copy of sid.


        """
        return _libsbml.Compartment_setId(*args)

    def setName(*args): 
        """
        setName(self, string str)

        Sets the name of this Compartment to a copy of string (SName in L1).


        """
        return _libsbml.Compartment_setName(*args)

    def setSpatialDimensions(*args): 
        """
        setSpatialDimensions(self, unsigned int value)

        Sets the spatialDimensions of this Compartment to value.

        If value is not one of [0, 1, 2, 3] the function will have no effect
        (i.e. spatialDimensions will not be set).


        """
        return _libsbml.Compartment_setSpatialDimensions(*args)

    def setSize(*args): 
        """
        setSize(self, double value)

        Sets the size (volume in L1) of this Compartment to value.


        """
        return _libsbml.Compartment_setSize(*args)

    def setVolume(*args): 
        """
        setVolume(self, double value)

        Sets the volume (size in L2) of this Compartment to value.


        """
        return _libsbml.Compartment_setVolume(*args)

    def setUnits(*args): 
        """
        setUnits(self, string sid)

        Sets the units of this Compartment to a copy of sid.


        """
        return _libsbml.Compartment_setUnits(*args)

    def setOutside(*args): 
        """
        setOutside(self, string sid)

        Sets the outside of this Compartment to a copy of sid.


        """
        return _libsbml.Compartment_setOutside(*args)

    def setConstant(*args): 
        """
        setConstant(self, bool value)

        Sets the constant field of this Compartment to value.


        """
        return _libsbml.Compartment_setConstant(*args)

    def unsetName(*args): 
        """
        unsetName(self)

        Unsets the name of this Compartment.


        """
        return _libsbml.Compartment_unsetName(*args)

    def unsetSize(*args): 
        """
        unsetSize(self)

        Unsets the size (volume in L1) of this Compartment.


        """
        return _libsbml.Compartment_unsetSize(*args)

    def unsetVolume(*args): 
        """
        unsetVolume(self)

        Unsets the volume (size in L2) of this Compartment.

        In SBML L1, a Compartment volume has a default value (1.0) and
        therefore <b>should always be set</b>.  In L2, volume is optional with
        no default value and as such may or may not be set.


        """
        return _libsbml.Compartment_unsetVolume(*args)

    def unsetUnits(*args): 
        """
        unsetUnits(self)

        Unsets the units of this Compartment.


        """
        return _libsbml.Compartment_unsetUnits(*args)

    def unsetOutside(*args): 
        """
        unsetOutside(self)

        Unsets the outside of this Compartment.


        """
        return _libsbml.Compartment_unsetOutside(*args)


class CompartmentPtr(Compartment):
    def __init__(self, this):
        _swig_setattr(self, Compartment, 'this', this)
        if not hasattr(self,"thisown"): _swig_setattr(self, Compartment, 'thisown', 0)
        _swig_setattr(self, Compartment,self.__class__,Compartment)
_libsbml.Compartment_swigregister(CompartmentPtr)

class Species(SBase):
    """Proxy of C++ Species class"""
    __swig_setmethods__ = {}
    for _s in [SBase]: __swig_setmethods__.update(_s.__swig_setmethods__)
    __setattr__ = lambda self, name, value: _swig_setattr(self, Species, name, value)
    __swig_getmethods__ = {}
    for _s in [SBase]: __swig_getmethods__.update(_s.__swig_getmethods__)
    __getattr__ = lambda self, name: _swig_getattr(self, Species, name)
    def __repr__(self):
        return "<%s.%s; proxy of C++ Species instance at %s>" % (self.__class__.__module__, self.__class__.__name__, self.this,)
    def __init__(self, *args):
        """
        __init__(self, string id="") -> Species
        __init__(self) -> Species

        Creates a new Species, optionally with its id attribute set.


        """
        _swig_setattr(self, Species, 'this', _libsbml.new_Species(*args))
        _swig_setattr(self, Species, 'thisown', 1)
    def __del__(self, destroy=_libsbml.delete_Species):
        """__del__(self)"""
        try:
            if self.thisown: destroy(self)
        except: pass

    def initDefaults(*args): 
        """
        initDefaults(self)

        Initializes the fields of this Species to their defaults:

          - boundaryCondition = false
          - constant          = false  (L2 only)


        """
        return _libsbml.Species_initDefaults(*args)

    def getId(*args): 
        """
        getId(self) -> string

        Returns the id of this Species


        """
        return _libsbml.Species_getId(*args)

    def getName(*args): 
        """
        getName(self) -> string

        Returns the name of this Species.


        """
        return _libsbml.Species_getName(*args)

    def getCompartment(*args): 
        """
        getCompartment(self) -> string

        Returns the compartment of this Species.


        """
        return _libsbml.Species_getCompartment(*args)

    def getInitialAmount(*args): 
        """
        getInitialAmount(self) -> double

        Returns the initialAmount of this Species.


        """
        return _libsbml.Species_getInitialAmount(*args)

    def getInitialConcentration(*args): 
        """
        getInitialConcentration(self) -> double

        Returns the initialConcentration of this Species.


        """
        return _libsbml.Species_getInitialConcentration(*args)

    def getSubstanceUnits(*args): 
        """
        getSubstanceUnits(self) -> string

        Returns the substanceUnits of this Species.


        """
        return _libsbml.Species_getSubstanceUnits(*args)

    def getSpatialSizeUnits(*args): 
        """
        getSpatialSizeUnits(self) -> string

        Returns the spatialSizeUnits of this Species.


        """
        return _libsbml.Species_getSpatialSizeUnits(*args)

    def getUnits(*args): 
        """
        getUnits(self) -> string

        Returns the units of this Species (L1 only).


        """
        return _libsbml.Species_getUnits(*args)

    def getHasOnlySubstanceUnits(*args): 
        """
        getHasOnlySubstanceUnits(self) -> bool

        Returns true if this Species hasOnlySubstanceUnits, false otherwise.


        """
        return _libsbml.Species_getHasOnlySubstanceUnits(*args)

    def getBoundaryCondition(*args): 
        """
        getBoundaryCondition(self) -> bool

        Returns the boundaryCondition of this Species.


        """
        return _libsbml.Species_getBoundaryCondition(*args)

    def getCharge(*args): 
        """
        getCharge(self) -> int

        Returns the charge of this Species.


        """
        return _libsbml.Species_getCharge(*args)

    def getConstant(*args): 
        """
        getConstant(self) -> bool

        Returns true if this Species is constant, false otherwise.


        """
        return _libsbml.Species_getConstant(*args)

    def isSetId(*args): 
        """
        isSetId(self) -> bool

        Returns true if the id of this Species has been set, false otherwise.


        """
        return _libsbml.Species_isSetId(*args)

    def isSetName(*args): 
        """
        isSetName(self) -> bool

        Returns true if the name of this Species has been set, false
        otherwise.

        In SBML L1, a Species name is required and therefore <b>should always
        be set</b>.  In L2, name is optional and as such may or may not be
        set.


        """
        return _libsbml.Species_isSetName(*args)

    def isSetCompartment(*args): 
        """
        isSetCompartment(self) -> bool

        Returns true if the compartment of this Species has been set, false
        otherwise.


        """
        return _libsbml.Species_isSetCompartment(*args)

    def isSetInitialAmount(*args): 
        """
        isSetInitialAmount(self) -> bool

        Returns true if the initialAmount of this Species has been set, false
        otherwise.

        In SBML L1, a Species initialAmount is required and therefore
        <b>should always be set</b>.  In L2, initialAmount is optional and as
        such may or may not be set.


        """
        return _libsbml.Species_isSetInitialAmount(*args)

    def isSetInitialConcentration(*args): 
        """
        isSetInitialConcentration(self) -> bool

        Returns true if the initialConcentration of this Species has been set,
        false otherwise.


        """
        return _libsbml.Species_isSetInitialConcentration(*args)

    def isSetSubstanceUnits(*args): 
        """
        isSetSubstanceUnits(self) -> bool

        Returns true if the substanceUnits of this Species has been set, false
        otherwise.


        """
        return _libsbml.Species_isSetSubstanceUnits(*args)

    def isSetSpatialSizeUnits(*args): 
        """
        isSetSpatialSizeUnits(self) -> bool

        Returns true if the spatialSizeUnits of this Species has been set,
        false otherwise.


        """
        return _libsbml.Species_isSetSpatialSizeUnits(*args)

    def isSetUnits(*args): 
        """
        isSetUnits(self) -> bool

        Returns true if the units of this Species has been set, false
        otherwise (L1 only).


        """
        return _libsbml.Species_isSetUnits(*args)

    def isSetCharge(*args): 
        """
        isSetCharge(self) -> bool

        Returns true if the charge of this Species has been set, false
        otherwise.


        """
        return _libsbml.Species_isSetCharge(*args)

    def moveIdToName(*args): 
        """
        moveIdToName(self)

        Moves the id field of this Species to its name field (iff name is not
        already set).  This method is used for converting from L2 to L1.


        """
        return _libsbml.Species_moveIdToName(*args)

    def moveNameToId(*args): 
        """
        moveNameToId(self)

        Moves the name field of this Species to its id field (iff id is not
        already set).  This method is used for converting from L1 to L2.


        """
        return _libsbml.Species_moveNameToId(*args)

    def setId(*args): 
        """
        setId(self, string sid)

        Sets the id of this Species to a copy of sid.


        """
        return _libsbml.Species_setId(*args)

    def setName(*args): 
        """
        setName(self, string str)

        Sets the name of this Species to a copy of string (SName in L1).


        """
        return _libsbml.Species_setName(*args)

    def setCompartment(*args): 
        """
        setCompartment(self, string sid)

        Sets the compartment of this Species to a copy of sid.


        """
        return _libsbml.Species_setCompartment(*args)

    def setInitialAmount(*args): 
        """
        setInitialAmount(self, double value)

        Sets the initialAmount of this Species to value and marks the field as
        set.  This method also unsets the initialConentration field.


        """
        return _libsbml.Species_setInitialAmount(*args)

    def setInitialConcentration(*args): 
        """
        setInitialConcentration(self, double value)

        Sets the initialConcentration of this Species to value and marks the
        field as set.  This method also unsets the initialAmount field.


        """
        return _libsbml.Species_setInitialConcentration(*args)

    def setSubstanceUnits(*args): 
        """
        setSubstanceUnits(self, string sid)

        Sets the substanceUnits of this Species to a copy of sid.


        """
        return _libsbml.Species_setSubstanceUnits(*args)

    def setSpatialSizeUnits(*args): 
        """
        setSpatialSizeUnits(self, string sid)

        Sets the spatialSizeUnits of this Species to a copy of sid.


        """
        return _libsbml.Species_setSpatialSizeUnits(*args)

    def setUnits(*args): 
        """
        setUnits(self, string sname)

        Sets the units of this Species to a copy of sname (L1 only).


        """
        return _libsbml.Species_setUnits(*args)

    def setHasOnlySubstanceUnits(*args): 
        """
        setHasOnlySubstanceUnits(self, bool value)

        Sets the hasOnlySubstanceUnits field of this Species to value.


        """
        return _libsbml.Species_setHasOnlySubstanceUnits(*args)

    def setBoundaryCondition(*args): 
        """
        setBoundaryCondition(self, bool value)

        Sets the boundaryCondition of this Species to value.


        """
        return _libsbml.Species_setBoundaryCondition(*args)

    def setCharge(*args): 
        """
        setCharge(self, int value)

        Sets the charge of this Species to value and marks the field as set.


        """
        return _libsbml.Species_setCharge(*args)

    def setConstant(*args): 
        """
        setConstant(self, bool value)

        Sets the constant field of this Species to value.


        """
        return _libsbml.Species_setConstant(*args)

    def unsetName(*args): 
        """
        unsetName(self)

        Unsets the name of this Species.

        In SBML L1, a Species name is required and therefore <b>should always
        be set</b>.  In L2, name is optional and as such may or may not be
        set.


        """
        return _libsbml.Species_unsetName(*args)

    def unsetInitialAmount(*args): 
        """
        unsetInitialAmount(self)

        Marks the initialAmount of this Species as unset.


        """
        return _libsbml.Species_unsetInitialAmount(*args)

    def unsetInitialConcentration(*args): 
        """
        unsetInitialConcentration(self)

        Unsets the initialConcentration of this Species.


        """
        return _libsbml.Species_unsetInitialConcentration(*args)

    def unsetSubstanceUnits(*args): 
        """
        unsetSubstanceUnits(self)

        Unsets the substanceUnits of this Species.


        """
        return _libsbml.Species_unsetSubstanceUnits(*args)

    def unsetSpatialSizeUnits(*args): 
        """
        unsetSpatialSizeUnits(self)

        Unsets the spatialSizeUnits of this Species.


        """
        return _libsbml.Species_unsetSpatialSizeUnits(*args)

    def unsetUnits(*args): 
        """
        unsetUnits(self)

        Unsets the units of this Species (L1 only).


        """
        return _libsbml.Species_unsetUnits(*args)

    def unsetCharge(*args): 
        """
        unsetCharge(self)

        Unsets the charge of this Species.


        """
        return _libsbml.Species_unsetCharge(*args)


class SpeciesPtr(Species):
    def __init__(self, this):
        _swig_setattr(self, Species, 'this', this)
        if not hasattr(self,"thisown"): _swig_setattr(self, Species, 'thisown', 0)
        _swig_setattr(self, Species,self.__class__,Species)
_libsbml.Species_swigregister(SpeciesPtr)

class Parameter(SBase):
    """Proxy of C++ Parameter class"""
    __swig_setmethods__ = {}
    for _s in [SBase]: __swig_setmethods__.update(_s.__swig_setmethods__)
    __setattr__ = lambda self, name, value: _swig_setattr(self, Parameter, name, value)
    __swig_getmethods__ = {}
    for _s in [SBase]: __swig_getmethods__.update(_s.__swig_getmethods__)
    __getattr__ = lambda self, name: _swig_getattr(self, Parameter, name)
    def __repr__(self):
        return "<%s.%s; proxy of C++ Parameter instance at %s>" % (self.__class__.__module__, self.__class__.__name__, self.this,)
    def __init__(self, *args):
        """
        __init__(self, string id="") -> Parameter
        __init__(self) -> Parameter
        __init__(self, string id, double value, string units="", bool constant=True) -> Parameter
        __init__(self, string id, double value, string units="") -> Parameter
        __init__(self, string id, double value) -> Parameter

        Creates a new Parameter, with its id and value attributes set and
        optionally its units and constant attributes.


        """
        _swig_setattr(self, Parameter, 'this', _libsbml.new_Parameter(*args))
        _swig_setattr(self, Parameter, 'thisown', 1)
    def __del__(self, destroy=_libsbml.delete_Parameter):
        """__del__(self)"""
        try:
            if self.thisown: destroy(self)
        except: pass

    def initDefaults(*args): 
        """
        initDefaults(self)

        Initializes the fields of this Parameter to their defaults:

          - constant = true  (L2 only)


        """
        return _libsbml.Parameter_initDefaults(*args)

    def getId(*args): 
        """
        getId(self) -> string

        Returns the id of this Parameter


        """
        return _libsbml.Parameter_getId(*args)

    def getName(*args): 
        """
        getName(self) -> string

        Returns the name of this Parameter.


        """
        return _libsbml.Parameter_getName(*args)

    def getValue(*args): 
        """
        getValue(self) -> double

        Returns the value of this Parameter.


        """
        return _libsbml.Parameter_getValue(*args)

    def getUnits(*args): 
        """
        getUnits(self) -> string

        Returns the units of this Parameter.


        """
        return _libsbml.Parameter_getUnits(*args)

    def getConstant(*args): 
        """
        getConstant(self) -> bool

        Returns true if this Parameter is constant, false otherwise.


        """
        return _libsbml.Parameter_getConstant(*args)

    def isSetId(*args): 
        """
        isSetId(self) -> bool

        Returns true if the id of this Parameter has been set, false otherwise.


        """
        return _libsbml.Parameter_isSetId(*args)

    def isSetName(*args): 
        """
        isSetName(self) -> bool

        Returns true if the name of this Parameter has been set, false
        otherwise.

        In SBML L1, a Parameter name is required and therefore <b>should always
        be set</b>.  In L2, name is optional and as such may or may not be
        set.


        """
        return _libsbml.Parameter_isSetName(*args)

    def isSetValue(*args): 
        """
        isSetValue(self) -> bool

        Returns true if the value of this Parameter has been set, false
        otherwise.

        In SBML L1v1, a Parameter value is required and therefore <b>should
        always be set</b>.  In L1v2 and beyond, a value is optional and as
        such may or may not be set.


        """
        return _libsbml.Parameter_isSetValue(*args)

    def isSetUnits(*args): 
        """
        isSetUnits(self) -> bool

        Returns true if the units of this Parameter has been set, false
        otherwise.


        """
        return _libsbml.Parameter_isSetUnits(*args)

    def moveIdToName(*args): 
        """
        moveIdToName(self)

        Moves the id field of this Parameter to its name field (iff name is
        not already set).  This method is used for converting from L2 to L1.


        """
        return _libsbml.Parameter_moveIdToName(*args)

    def moveNameToId(*args): 
        """
        moveNameToId(self)

        Moves the name field of this Parameter to its id field (iff id is
        not already set).  This method is used for converting from L1 to L2.


        """
        return _libsbml.Parameter_moveNameToId(*args)

    def setId(*args): 
        """
        setId(self, string sid)

        Sets the id of this Parameter to a copy of sid.


        """
        return _libsbml.Parameter_setId(*args)

    def setName(*args): 
        """
        setName(self, string str)

        Sets the name of this Parameter to a copy of string (SName in L1).


        """
        return _libsbml.Parameter_setName(*args)

    def setValue(*args): 
        """
        setValue(self, double value)

        Sets the initialAmount of this Parameter to value and marks the field
        as set.


        """
        return _libsbml.Parameter_setValue(*args)

    def setUnits(*args): 
        """
        setUnits(self, string sname)

        Sets the units of this Parameter to a copy of sid.


        """
        return _libsbml.Parameter_setUnits(*args)

    def setConstant(*args): 
        """
        setConstant(self, bool value)

        Sets the constant field of this Parameter to value.


        """
        return _libsbml.Parameter_setConstant(*args)

    def unsetName(*args): 
        """
        unsetName(self)

        Unsets the name of this Parameter.

        In SBML L1, a Parameter name is required and therefore <b>should
        always be set</b>.  In L2, name is optional and as such may or may not
        be set.


        """
        return _libsbml.Parameter_unsetName(*args)

    def unsetValue(*args): 
        """
        unsetValue(self)

        Unsets the value of this Parameter.

        In SBML L1v1, a Parameter value is required and therefore <b>should
        always be set</b>.  In L1v2 and beyond, a value is optional and as
        such may or may not be set.


        """
        return _libsbml.Parameter_unsetValue(*args)

    def unsetUnits(*args): 
        """
        unsetUnits(self)

        Unsets the units of this Parameter.


        """
        return _libsbml.Parameter_unsetUnits(*args)


class ParameterPtr(Parameter):
    def __init__(self, this):
        _swig_setattr(self, Parameter, 'this', this)
        if not hasattr(self,"thisown"): _swig_setattr(self, Parameter, 'thisown', 0)
        _swig_setattr(self, Parameter,self.__class__,Parameter)
_libsbml.Parameter_swigregister(ParameterPtr)

class Rule(SBase):
    """Proxy of C++ Rule class"""
    __swig_setmethods__ = {}
    for _s in [SBase]: __swig_setmethods__.update(_s.__swig_setmethods__)
    __setattr__ = lambda self, name, value: _swig_setattr(self, Rule, name, value)
    __swig_getmethods__ = {}
    for _s in [SBase]: __swig_getmethods__.update(_s.__swig_getmethods__)
    __getattr__ = lambda self, name: _swig_getattr(self, Rule, name)
    def __repr__(self):
        return "<%s.%s; proxy of C++ Rule instance at %s>" % (self.__class__.__module__, self.__class__.__name__, self.this,)
    def __init__(self, *args):
        """
        __init__(self, string formula="") -> Rule
        __init__(self) -> Rule
        __init__(self, ASTNode math) -> Rule

        Creates a new Rule with its math attribute set.


        """
        _swig_setattr(self, Rule, 'this', _libsbml.new_Rule(*args))
        _swig_setattr(self, Rule, 'thisown', 1)
    def __del__(self, destroy=_libsbml.delete_Rule):
        """__del__(self)"""
        try:
            if self.thisown: destroy(self)
        except: pass

    def getFormula(*args): 
        """
        getFormula(self) -> string

        Returns the formula for this Rule.


        """
        return _libsbml.Rule_getFormula(*args)

    def getMath(*args): 
        """
        getMath(self) -> ASTNode

        Returns the math for this Rule.


        """
        return _libsbml.Rule_getMath(*args)

    def isSetFormula(*args): 
        """
        isSetFormula(self) -> bool

        Returns true if the formula (or equivalently the math) for this Rule
        has been set, false otherwise.


        """
        return _libsbml.Rule_isSetFormula(*args)

    def isSetMath(*args): 
        """
        isSetMath(self) -> bool

        Returns true if the formula (or equivalently the math) for this Rule
        has been set, false otherwise.


        """
        return _libsbml.Rule_isSetMath(*args)

    def setFormula(*args): 
        """
        setFormula(self, string str)

        Sets the formula of this Rule to a copy of string.


        """
        return _libsbml.Rule_setFormula(*args)

    def setMath(*args):
      if args[1] is not None: args[1].thisown = 0
      return _libsbml.Rule_setMath(*args)


    def setFormulaFromMath(*args): 
        """
        setFormulaFromMath(self)

        This method is no longer necessary.  LibSBML now keeps formula strings
        and math ASTs synchronized automatically.  The method is kept around
        for backward compatibility (and is used internally).


        """
        return _libsbml.Rule_setFormulaFromMath(*args)

    def setMathFromFormula(*args): 
        """
        setMathFromFormula(self)

        This method is no longer necessary.  LibSBML now keeps formula strings
        and math ASTs synchronized automatically.  The method is kept around
        for backward compatibility (and is used internally).


        """
        return _libsbml.Rule_setMathFromFormula(*args)


class RulePtr(Rule):
    def __init__(self, this):
        _swig_setattr(self, Rule, 'this', this)
        if not hasattr(self,"thisown"): _swig_setattr(self, Rule, 'thisown', 0)
        _swig_setattr(self, Rule,self.__class__,Rule)
_libsbml.Rule_swigregister(RulePtr)

class AlgebraicRule(Rule):
    """Proxy of C++ AlgebraicRule class"""
    __swig_setmethods__ = {}
    for _s in [Rule]: __swig_setmethods__.update(_s.__swig_setmethods__)
    __setattr__ = lambda self, name, value: _swig_setattr(self, AlgebraicRule, name, value)
    __swig_getmethods__ = {}
    for _s in [Rule]: __swig_getmethods__.update(_s.__swig_getmethods__)
    __getattr__ = lambda self, name: _swig_getattr(self, AlgebraicRule, name)
    def __repr__(self):
        return "<%s.%s; proxy of C++ AlgebraicRule instance at %s>" % (self.__class__.__module__, self.__class__.__name__, self.this,)
    def __init__(self, *args):
      _swig_setattr(self, AlgebraicRule, 'this', _libsbml.new_AlgebraicRule(*args))
      _swig_setattr(self, AlgebraicRule, 'thisown', 1)
      try:
        if args[0] is not None: args[0].thisown = 0
      except (IndexError, AttributeError):
        pass


    def __del__(self, destroy=_libsbml.delete_AlgebraicRule):
        """__del__(self)"""
        try:
            if self.thisown: destroy(self)
        except: pass


class AlgebraicRulePtr(AlgebraicRule):
    def __init__(self, this):
        _swig_setattr(self, AlgebraicRule, 'this', this)
        if not hasattr(self,"thisown"): _swig_setattr(self, AlgebraicRule, 'thisown', 0)
        _swig_setattr(self, AlgebraicRule,self.__class__,AlgebraicRule)
_libsbml.AlgebraicRule_swigregister(AlgebraicRulePtr)

class AssignmentRule(Rule):
    """Proxy of C++ AssignmentRule class"""
    __swig_setmethods__ = {}
    for _s in [Rule]: __swig_setmethods__.update(_s.__swig_setmethods__)
    __setattr__ = lambda self, name, value: _swig_setattr(self, AssignmentRule, name, value)
    __swig_getmethods__ = {}
    for _s in [Rule]: __swig_getmethods__.update(_s.__swig_getmethods__)
    __getattr__ = lambda self, name: _swig_getattr(self, AssignmentRule, name)
    def __repr__(self):
        return "<%s.%s; proxy of C++ AssignmentRule instance at %s>" % (self.__class__.__module__, self.__class__.__name__, self.this,)
    def __init__(self, *args):
      _swig_setattr(self, AssignmentRule, 'this', _libsbml.new_AssignmentRule(*args))
      _swig_setattr(self, AssignmentRule, 'thisown', 1)
      try:
        if args[1] is not None: args[1].thisown = 0
      except (IndexError, AttributeError):
        pass


    def __del__(self, destroy=_libsbml.delete_AssignmentRule):
        """__del__(self)"""
        try:
            if self.thisown: destroy(self)
        except: pass

    def initDefaults(*args): 
        """
        initDefaults(self)

        The function is kept for backward compatibility with the SBML L1 API.

        Initializes the fields of this AssignmentRule to their defaults:

          - type = RULE_TYPE_SCALAR


        """
        return _libsbml.AssignmentRule_initDefaults(*args)

    def getType(*args): 
        """
        getType(self) -> int

        Returns the type for this AssignmentRule.


        """
        return _libsbml.AssignmentRule_getType(*args)

    def getVariable(*args): 
        """
        getVariable(self) -> string

        Returns the variable for this AssignmentRule.


        """
        return _libsbml.AssignmentRule_getVariable(*args)

    def isSetVariable(*args): 
        """
        isSetVariable(self) -> bool

        Returns true if the variable of this AssignmentRule has been set,
        false otherwise.


        """
        return _libsbml.AssignmentRule_isSetVariable(*args)

    def setType(*args): 
        """
        setType(self, RuleType_t rt)

        Sets the type of this Rule to the given RuleType.


        """
        return _libsbml.AssignmentRule_setType(*args)

    def setVariable(*args): 
        """
        setVariable(self, string sid)

        Sets the variable of this AssignmentRule to a copy of sid.


        """
        return _libsbml.AssignmentRule_setVariable(*args)


class AssignmentRulePtr(AssignmentRule):
    def __init__(self, this):
        _swig_setattr(self, AssignmentRule, 'this', this)
        if not hasattr(self,"thisown"): _swig_setattr(self, AssignmentRule, 'thisown', 0)
        _swig_setattr(self, AssignmentRule,self.__class__,AssignmentRule)
_libsbml.AssignmentRule_swigregister(AssignmentRulePtr)

class RateRule(Rule):
    """Proxy of C++ RateRule class"""
    __swig_setmethods__ = {}
    for _s in [Rule]: __swig_setmethods__.update(_s.__swig_setmethods__)
    __setattr__ = lambda self, name, value: _swig_setattr(self, RateRule, name, value)
    __swig_getmethods__ = {}
    for _s in [Rule]: __swig_getmethods__.update(_s.__swig_getmethods__)
    __getattr__ = lambda self, name: _swig_getattr(self, RateRule, name)
    def __repr__(self):
        return "<%s.%s; proxy of C++ RateRule instance at %s>" % (self.__class__.__module__, self.__class__.__name__, self.this,)
    def __init__(self, *args):
      _swig_setattr(self, RateRule, 'this', _libsbml.new_RateRule(*args))
      _swig_setattr(self, RateRule, 'thisown', 1)
      try:
        if args[1] is not None: args[1].thisown = 0
      except (IndexError, AttributeError):
        pass


    def __del__(self, destroy=_libsbml.delete_RateRule):
        """__del__(self)"""
        try:
            if self.thisown: destroy(self)
        except: pass

    def getVariable(*args): 
        """
        getVariable(self) -> string

        Returns the variable for this RateRule.


        """
        return _libsbml.RateRule_getVariable(*args)

    def isSetVariable(*args): 
        """
        isSetVariable(self) -> bool

        Returns true if the variable of this RateRule has been set, false
        otherwise.


        """
        return _libsbml.RateRule_isSetVariable(*args)

    def setVariable(*args): 
        """
        setVariable(self, string sid)

        Sets the variable of this RateRule to a copy of sid.


        """
        return _libsbml.RateRule_setVariable(*args)


class RateRulePtr(RateRule):
    def __init__(self, this):
        _swig_setattr(self, RateRule, 'this', this)
        if not hasattr(self,"thisown"): _swig_setattr(self, RateRule, 'thisown', 0)
        _swig_setattr(self, RateRule,self.__class__,RateRule)
_libsbml.RateRule_swigregister(RateRulePtr)

class SpeciesConcentrationRule(AssignmentRule):
    """Proxy of C++ SpeciesConcentrationRule class"""
    __swig_setmethods__ = {}
    for _s in [AssignmentRule]: __swig_setmethods__.update(_s.__swig_setmethods__)
    __setattr__ = lambda self, name, value: _swig_setattr(self, SpeciesConcentrationRule, name, value)
    __swig_getmethods__ = {}
    for _s in [AssignmentRule]: __swig_getmethods__.update(_s.__swig_getmethods__)
    __getattr__ = lambda self, name: _swig_getattr(self, SpeciesConcentrationRule, name)
    def __repr__(self):
        return "<%s.%s; proxy of C++ SpeciesConcentrationRule instance at %s>" % (self.__class__.__module__, self.__class__.__name__, self.this,)
    def __init__(self, *args):
        """
        __init__(self) -> SpeciesConcentrationRule
        __init__(self, string species, string formula, RuleType_t type=RULE_TYPE_SCALAR) -> SpeciesConcentrationRule
        __init__(self, string species, string formula) -> SpeciesConcentrationRule

        Creates a new SpeciesConcentrationRule with its species, formula and
        (optionally) type attributes set.


        """
        _swig_setattr(self, SpeciesConcentrationRule, 'this', _libsbml.new_SpeciesConcentrationRule(*args))
        _swig_setattr(self, SpeciesConcentrationRule, 'thisown', 1)
    def __del__(self, destroy=_libsbml.delete_SpeciesConcentrationRule):
        """__del__(self)"""
        try:
            if self.thisown: destroy(self)
        except: pass

    def getSpecies(*args): 
        """
        getSpecies(self) -> string

        Returns the species of this SpeciesConcentrationRule.


        """
        return _libsbml.SpeciesConcentrationRule_getSpecies(*args)

    def isSetSpecies(*args): 
        """
        isSetSpecies(self) -> bool

        Returns true if the species of this SpeciesConcentrationRule has been
        set, false otherwise.


        """
        return _libsbml.SpeciesConcentrationRule_isSetSpecies(*args)

    def setSpecies(*args): 
        """
        setSpecies(self, string sname)

        Sets the species of this SpeciesConcentrationRule to a copy of sname.


        """
        return _libsbml.SpeciesConcentrationRule_setSpecies(*args)


class SpeciesConcentrationRulePtr(SpeciesConcentrationRule):
    def __init__(self, this):
        _swig_setattr(self, SpeciesConcentrationRule, 'this', this)
        if not hasattr(self,"thisown"): _swig_setattr(self, SpeciesConcentrationRule, 'thisown', 0)
        _swig_setattr(self, SpeciesConcentrationRule,self.__class__,SpeciesConcentrationRule)
_libsbml.SpeciesConcentrationRule_swigregister(SpeciesConcentrationRulePtr)

class CompartmentVolumeRule(AssignmentRule):
    """Proxy of C++ CompartmentVolumeRule class"""
    __swig_setmethods__ = {}
    for _s in [AssignmentRule]: __swig_setmethods__.update(_s.__swig_setmethods__)
    __setattr__ = lambda self, name, value: _swig_setattr(self, CompartmentVolumeRule, name, value)
    __swig_getmethods__ = {}
    for _s in [AssignmentRule]: __swig_getmethods__.update(_s.__swig_getmethods__)
    __getattr__ = lambda self, name: _swig_getattr(self, CompartmentVolumeRule, name)
    def __repr__(self):
        return "<%s.%s; proxy of C++ CompartmentVolumeRule instance at %s>" % (self.__class__.__module__, self.__class__.__name__, self.this,)
    def __init__(self, *args):
        """
        __init__(self) -> CompartmentVolumeRule
        __init__(self, string compartment, string formula, RuleType_t type=RULE_TYPE_SCALAR) -> CompartmentVolumeRule
        __init__(self, string compartment, string formula) -> CompartmentVolumeRule

        Creates a new CompartmentVolumeRule with its compartment, formula and
        (optionally) type attributes set.


        """
        _swig_setattr(self, CompartmentVolumeRule, 'this', _libsbml.new_CompartmentVolumeRule(*args))
        _swig_setattr(self, CompartmentVolumeRule, 'thisown', 1)
    def __del__(self, destroy=_libsbml.delete_CompartmentVolumeRule):
        """__del__(self)"""
        try:
            if self.thisown: destroy(self)
        except: pass

    def getCompartment(*args): 
        """
        getCompartment(self) -> string

        Returns the compartment of this CompartmentVolumeRule.


        """
        return _libsbml.CompartmentVolumeRule_getCompartment(*args)

    def isSetCompartment(*args): 
        """
        isSetCompartment(self) -> bool

        Returns true if the compartment of this CompartmentVolumeRule has been
        set, false otherwise.


        """
        return _libsbml.CompartmentVolumeRule_isSetCompartment(*args)

    def setCompartment(*args): 
        """
        setCompartment(self, string sname)

        Sets the compartment of this CompartmentVolumeRule to a copy of sname.


        """
        return _libsbml.CompartmentVolumeRule_setCompartment(*args)


class CompartmentVolumeRulePtr(CompartmentVolumeRule):
    def __init__(self, this):
        _swig_setattr(self, CompartmentVolumeRule, 'this', this)
        if not hasattr(self,"thisown"): _swig_setattr(self, CompartmentVolumeRule, 'thisown', 0)
        _swig_setattr(self, CompartmentVolumeRule,self.__class__,CompartmentVolumeRule)
_libsbml.CompartmentVolumeRule_swigregister(CompartmentVolumeRulePtr)

class ParameterRule(AssignmentRule):
    """Proxy of C++ ParameterRule class"""
    __swig_setmethods__ = {}
    for _s in [AssignmentRule]: __swig_setmethods__.update(_s.__swig_setmethods__)
    __setattr__ = lambda self, name, value: _swig_setattr(self, ParameterRule, name, value)
    __swig_getmethods__ = {}
    for _s in [AssignmentRule]: __swig_getmethods__.update(_s.__swig_getmethods__)
    __getattr__ = lambda self, name: _swig_getattr(self, ParameterRule, name)
    def __repr__(self):
        return "<%s.%s; proxy of C++ ParameterRule instance at %s>" % (self.__class__.__module__, self.__class__.__name__, self.this,)
    def __init__(self, *args):
        """
        __init__(self) -> ParameterRule
        __init__(self, string name, string formula, RuleType_t type=RULE_TYPE_SCALAR) -> ParameterRule
        __init__(self, string name, string formula) -> ParameterRule

        Creates a new ParameterRule with its name, formula and (optionally)
        type attributes set.


        """
        _swig_setattr(self, ParameterRule, 'this', _libsbml.new_ParameterRule(*args))
        _swig_setattr(self, ParameterRule, 'thisown', 1)
    def __del__(self, destroy=_libsbml.delete_ParameterRule):
        """__del__(self)"""
        try:
            if self.thisown: destroy(self)
        except: pass

    def getName(*args): 
        """
        getName(self) -> string

        Returns the (Parameter) name for this ParameterRule.


        """
        return _libsbml.ParameterRule_getName(*args)

    def getUnits(*args): 
        """
        getUnits(self) -> string

        Returns the units for this ParameterRule.


        """
        return _libsbml.ParameterRule_getUnits(*args)

    def isSetName(*args): 
        """
        isSetName(self) -> bool

        Returns true if the (Parameter) name for this ParameterRule has been
        set, false otherwise.


        """
        return _libsbml.ParameterRule_isSetName(*args)

    def isSetUnits(*args): 
        """
        isSetUnits(self) -> bool

        Returns true if the units for this ParameterRule has been set, false
        otherwise.


        """
        return _libsbml.ParameterRule_isSetUnits(*args)

    def setName(*args): 
        """
        setName(self, string sname)

        Sets the (Parameter) name for this ParameterRule to a copy of sname.


        """
        return _libsbml.ParameterRule_setName(*args)

    def setUnits(*args): 
        """
        setUnits(self, string sname)

        Sets the units for this ParameterRule to a copy of sname.


        """
        return _libsbml.ParameterRule_setUnits(*args)

    def unsetUnits(*args): 
        """
        unsetUnits(self)

        Unsets the units for this ParameterRule.


        """
        return _libsbml.ParameterRule_unsetUnits(*args)


class ParameterRulePtr(ParameterRule):
    def __init__(self, this):
        _swig_setattr(self, ParameterRule, 'this', this)
        if not hasattr(self,"thisown"): _swig_setattr(self, ParameterRule, 'thisown', 0)
        _swig_setattr(self, ParameterRule,self.__class__,ParameterRule)
_libsbml.ParameterRule_swigregister(ParameterRulePtr)

class Reaction(SBase):
    """Proxy of C++ Reaction class"""
    __swig_setmethods__ = {}
    for _s in [SBase]: __swig_setmethods__.update(_s.__swig_setmethods__)
    __setattr__ = lambda self, name, value: _swig_setattr(self, Reaction, name, value)
    __swig_getmethods__ = {}
    for _s in [SBase]: __swig_getmethods__.update(_s.__swig_getmethods__)
    __getattr__ = lambda self, name: _swig_getattr(self, Reaction, name)
    def __repr__(self):
        return "<%s.%s; proxy of C++ Reaction instance at %s>" % (self.__class__.__module__, self.__class__.__name__, self.this,)
    def __init__(self, *args):
      _swig_setattr(self, Reaction, 'this', _libsbml.new_Reaction(*args))
      _swig_setattr(self, Reaction, 'thisown', 1)
      for index in [1, 2, 3]:
        try:
          if args[index] is not None: args[index].thisown = 0
        except (IndexError, AttributeError):
          pass


    def __del__(self, destroy=_libsbml.delete_Reaction):
        """__del__(self)"""
        try:
            if self.thisown: destroy(self)
        except: pass

    def initDefaults(*args): 
        """
        initDefaults(self)

        Initializes the fields of this Reaction to their defaults:

          - reversible = true
          - fast       = false  (L1 only)


        """
        return _libsbml.Reaction_initDefaults(*args)

    def getId(*args): 
        """
        getId(self) -> string

        Returns the id of this Reaction.


        """
        return _libsbml.Reaction_getId(*args)

    def getName(*args): 
        """
        getName(self) -> string

        Returns the name of this Reaction.


        """
        return _libsbml.Reaction_getName(*args)

    def getKineticLaw(*args): 
        """
        getKineticLaw(self) -> KineticLaw

        Returns the KineticLaw of this Reaction.


        """
        return _libsbml.Reaction_getKineticLaw(*args)

    def getReversible(*args): 
        """
        getReversible(self) -> bool

        Returns the reversible status of this Reaction.


        """
        return _libsbml.Reaction_getReversible(*args)

    def getFast(*args): 
        """
        getFast(self) -> bool

        Returns the fast status of this Reaction.


        """
        return _libsbml.Reaction_getFast(*args)

    def isSetId(*args): 
        """
        isSetId(self) -> bool

        Returns true if the id of this Reaction has been set, false otherwise.


        """
        return _libsbml.Reaction_isSetId(*args)

    def isSetName(*args): 
        """
        isSetName(self) -> bool

        Returns true if the name of this Reaction has been set, false
        otherwise.

        In SBML L1, a Reaction name is required and therefore <b>should always
        be set</b>.  In L2, name is optional and as such may or may not be
        set.


        """
        return _libsbml.Reaction_isSetName(*args)

    def isSetKineticLaw(*args): 
        """
        isSetKineticLaw(self) -> bool

        Returns true if the KineticLaw of this Reaction has been set, false
        otherwise.


        """
        return _libsbml.Reaction_isSetKineticLaw(*args)

    def isSetFast(*args): 
        """
        isSetFast(self) -> bool

        Returns true if the fast status of this Reation has been set, false
        otherwise.

        In L1, fast is optional with a default of false, which means it is
        effectively always set.  In L2, however, fast is optional with no
        default value, so it may or may not be set to a specific value.


        """
        return _libsbml.Reaction_isSetFast(*args)

    def moveIdToName(*args): 
        """
        moveIdToName(self)

        Moves the id field of this Reaction to its name field (iff name is not
        already set).  This method is used for converting from L2 to L1.


        """
        return _libsbml.Reaction_moveIdToName(*args)

    def moveNameToId(*args): 
        """
        moveNameToId(self)

        Moves the name field of this Reaction to its id field (iff id is not
        already set).  This method is used for converting from L1 to L2.


        """
        return _libsbml.Reaction_moveNameToId(*args)

    def setId(*args): 
        """
        setId(self, string sid)

        Sets the id of this Reaction to a copy of sid.


        """
        return _libsbml.Reaction_setId(*args)

    def setName(*args): 
        """
        setName(self, string str)

        Sets the name of this Reaction to a copy of string (SName in L1).


        """
        return _libsbml.Reaction_setName(*args)

    def setKineticLaw(*args):
      if args[1] is not None: args[1].thisown = 0
      return _libsbml.Reaction_setKineticLaw(*args)


    def setReversible(*args): 
        """
        setReversible(self, bool value)

        Sets the reversible status of this Reaction to value.


        """
        return _libsbml.Reaction_setReversible(*args)

    def setFast(*args): 
        """
        setFast(self, bool value)

        Sets the fast status of this Reaction to value.


        """
        return _libsbml.Reaction_setFast(*args)

    def addReactant(*args):
      if args[1] is not None: args[1].thisown = 0
      return _libsbml.Reaction_addReactant(*args)


    def addProduct(*args):
      if args[1] is not None:
        args[1].thisown = 0
      return _libsbml.Reaction_addProduct(*args)


    def addModifier(*args):
      if args[1] is not None:
        args[1].thisown = 0
      return _libsbml.Reaction_addModifier(*args)


    def getListOfReactants(*args): 
        """
        getListOfReactants(self) -> ListOf

        Returns the list of Reactants for this Reaction.


        """
        return _libsbml.Reaction_getListOfReactants(*args)

    def getListOfProducts(*args): 
        """
        getListOfProducts(self) -> ListOf

        Returns the list of Products for this Reaction.


        """
        return _libsbml.Reaction_getListOfProducts(*args)

    def getListOfModifiers(*args): 
        """
        getListOfModifiers(self) -> ListOf

        Returns the list of Modifiers for this Reaction.


        """
        return _libsbml.Reaction_getListOfModifiers(*args)

    def getReactant(*args): 
        """
        getReactant(self, unsigned int n) -> SpeciesReference
        getReactant(self, string sid) -> SpeciesReference

        Returns the reactant (SpeciesReference) in this Reaction with the
        given id or NULL if no such reactant exists.


        """
        return _libsbml.Reaction_getReactant(*args)

    def getProduct(*args): 
        """
        getProduct(self, unsigned int n) -> SpeciesReference
        getProduct(self, string sid) -> SpeciesReference

        Returns the product (SpeciesReference) in this Reaction with the given
        id or NULL if no such product exists.


        """
        return _libsbml.Reaction_getProduct(*args)

    def getModifier(*args): 
        """
        getModifier(self, unsigned int n) -> ModifierSpeciesReference
        getModifier(self, string sid) -> ModifierSpeciesReference

        Returns the modifier (ModifierSpeciesReference) in this Reaction with
        the given id or NULL if no such modifier exists.


        """
        return _libsbml.Reaction_getModifier(*args)

    def getNumReactants(*args): 
        """
        getNumReactants(self) -> unsigned int

        Returns the number of reactants (SpeciesReferences) in this Reaction.


        """
        return _libsbml.Reaction_getNumReactants(*args)

    def getNumProducts(*args): 
        """
        getNumProducts(self) -> unsigned int

        Returns the number of products (SpeciesReferences) in this Reaction.


        """
        return _libsbml.Reaction_getNumProducts(*args)

    def getNumModifiers(*args): 
        """
        getNumModifiers(self) -> unsigned int

        Returns the number of modifiers (ModifierSpeciesReferences) in this
        Reaction.


        """
        return _libsbml.Reaction_getNumModifiers(*args)

    def unsetName(*args): 
        """
        unsetName(self)

        Unsets the name of this Reaction.

        In SBML L1, a Reaction name is required and therefore <b>should always
        be set</b>.  In L2, name is optional and as such may or may not be
        set.


        """
        return _libsbml.Reaction_unsetName(*args)

    def unsetKineticLaw(*args): 
        """
        unsetKineticLaw(self)

        Unsets the KineticLaw of this Reaction.


        """
        return _libsbml.Reaction_unsetKineticLaw(*args)

    def unsetFast(*args): 
        """
        unsetFast(self)

        Unsets the fast status of this Reation.

        In L1, fast is optional with a default of false, which means it is
        effectively always set.  In L2, however, fast is optional with no
        default value, so it may or may not be set to a specific value.


        """
        return _libsbml.Reaction_unsetFast(*args)


class ReactionPtr(Reaction):
    def __init__(self, this):
        _swig_setattr(self, Reaction, 'this', this)
        if not hasattr(self,"thisown"): _swig_setattr(self, Reaction, 'thisown', 0)
        _swig_setattr(self, Reaction,self.__class__,Reaction)
_libsbml.Reaction_swigregister(ReactionPtr)

class KineticLaw(SBase):
    """Proxy of C++ KineticLaw class"""
    __swig_setmethods__ = {}
    for _s in [SBase]: __swig_setmethods__.update(_s.__swig_setmethods__)
    __setattr__ = lambda self, name, value: _swig_setattr(self, KineticLaw, name, value)
    __swig_getmethods__ = {}
    for _s in [SBase]: __swig_getmethods__.update(_s.__swig_getmethods__)
    __getattr__ = lambda self, name: _swig_getattr(self, KineticLaw, name)
    def __repr__(self):
        return "<%s.%s; proxy of C++ KineticLaw instance at %s>" % (self.__class__.__module__, self.__class__.__name__, self.this,)
    def __init__(self, *args):
        """
        __init__(self, string formula="", string timeUnits="", string substanceUnits="") -> KineticLaw
        __init__(self, string formula="", string timeUnits="") -> KineticLaw
        __init__(self, string formula="") -> KineticLaw
        __init__(self) -> KineticLaw

        Creates a new KineticLaw, optionally with its formula, timeUnits
        and/or substanceUnits set.


        """
        _swig_setattr(self, KineticLaw, 'this', _libsbml.new_KineticLaw(*args))
        _swig_setattr(self, KineticLaw, 'thisown', 1)
    def __del__(self, destroy=_libsbml.delete_KineticLaw):
        """__del__(self)"""
        try:
            if self.thisown: destroy(self)
        except: pass

    def getFormula(*args): 
        """
        getFormula(self) -> string

        Returns the formula of this KineticLaw.


        """
        return _libsbml.KineticLaw_getFormula(*args)

    def getMath(*args): 
        """
        getMath(self) -> ASTNode

        Returns the math of this KineticLaw.


        """
        return _libsbml.KineticLaw_getMath(*args)

    def getListOfParameters(*args): 
        """
        getListOfParameters(self) -> ListOf

        Returns the list of Parameters for this KineticLaw.


        """
        return _libsbml.KineticLaw_getListOfParameters(*args)

    def getTimeUnits(*args): 
        """
        getTimeUnits(self) -> string

        Returns the timeUnits of this KineticLaw.


        """
        return _libsbml.KineticLaw_getTimeUnits(*args)

    def getSubstanceUnits(*args): 
        """
        getSubstanceUnits(self) -> string

        Returns the substanceUnits of this KineticLaw.


        """
        return _libsbml.KineticLaw_getSubstanceUnits(*args)

    def isSetFormula(*args): 
        """
        isSetFormula(self) -> bool

        Returns true if the formula (or equivalently the math) of this
        KineticLaw has been set, false otherwise.


        """
        return _libsbml.KineticLaw_isSetFormula(*args)

    def isSetMath(*args): 
        """
        isSetMath(self) -> bool

        Returns true if the math (or equivalently the formula) of this
        KineticLaw has been set, false otherwise.


        """
        return _libsbml.KineticLaw_isSetMath(*args)

    def isSetTimeUnits(*args): 
        """
        isSetTimeUnits(self) -> bool

        Returns true if the timeUnits of this KineticLaw has been set, false
        otherwise.


        """
        return _libsbml.KineticLaw_isSetTimeUnits(*args)

    def isSetSubstanceUnits(*args): 
        """
        isSetSubstanceUnits(self) -> bool

        Returns true if the substanceUnits of this KineticLaw has been set,
        false otherwise.


        """
        return _libsbml.KineticLaw_isSetSubstanceUnits(*args)

    def setFormula(*args): 
        """
        setFormula(self, string formula)

        Sets the formula of this KineticLaw to a copy of string.


        """
        return _libsbml.KineticLaw_setFormula(*args)

    def setMath(*args):
      if args[1] is not None: args[1].thisown = 0
      return _libsbml.KineticLaw_setMath(*args)


    def setFormulaFromMath(*args): 
        """
        setFormulaFromMath(self)

        This method is no longer necessary.  LibSBML now keeps formula strings
        and math ASTs synchronized automatically.  The method is kept around
        for backward compatibility (and is used internally).


        """
        return _libsbml.KineticLaw_setFormulaFromMath(*args)

    def setMathFromFormula(*args): 
        """
        setMathFromFormula(self)

        This method is no longer necessary.  LibSBML now keeps formula strings
        and math ASTs synchronized automatically.  The method is kept around
        for backward compatibility (and is used internally).


        """
        return _libsbml.KineticLaw_setMathFromFormula(*args)

    def setTimeUnits(*args): 
        """
        setTimeUnits(self, string sname)

        Sets the timeUnits of this KineticLaw to a copy of sname.


        """
        return _libsbml.KineticLaw_setTimeUnits(*args)

    def setSubstanceUnits(*args): 
        """
        setSubstanceUnits(self, string sname)

        Sets the substanceUnits of this KineticLaw to a copy of sname.


        """
        return _libsbml.KineticLaw_setSubstanceUnits(*args)

    def addParameter(*args):
      if args[1] is not None: args[1].thisown = 0
      return _libsbml.KineticLaw_addParameter(*args)


    def getParameter(*args): 
        """
        getParameter(self, unsigned int n) -> Parameter

        Returns the nth Parameter of this KineticLaw.


        """
        return _libsbml.KineticLaw_getParameter(*args)

    def getNumParameters(*args): 
        """
        getNumParameters(self) -> unsigned int

        Returns the number of Parameters in this KineticLaw.


        """
        return _libsbml.KineticLaw_getNumParameters(*args)

    def unsetTimeUnits(*args): 
        """
        unsetTimeUnits(self)

        Unsets the timeUnits of this KineticLaw.


        """
        return _libsbml.KineticLaw_unsetTimeUnits(*args)

    def unsetSubstanceUnits(*args): 
        """
        unsetSubstanceUnits(self)

        Unsets the substanceUnits of this KineticLaw.


        """
        return _libsbml.KineticLaw_unsetSubstanceUnits(*args)


class KineticLawPtr(KineticLaw):
    def __init__(self, this):
        _swig_setattr(self, KineticLaw, 'this', this)
        if not hasattr(self,"thisown"): _swig_setattr(self, KineticLaw, 'thisown', 0)
        _swig_setattr(self, KineticLaw,self.__class__,KineticLaw)
_libsbml.KineticLaw_swigregister(KineticLawPtr)

class SimpleSpeciesReference(SBase):
    """Proxy of C++ SimpleSpeciesReference class"""
    __swig_setmethods__ = {}
    for _s in [SBase]: __swig_setmethods__.update(_s.__swig_setmethods__)
    __setattr__ = lambda self, name, value: _swig_setattr(self, SimpleSpeciesReference, name, value)
    __swig_getmethods__ = {}
    for _s in [SBase]: __swig_getmethods__.update(_s.__swig_getmethods__)
    __getattr__ = lambda self, name: _swig_getattr(self, SimpleSpeciesReference, name)
    def __repr__(self):
        return "<%s.%s; proxy of C++ SimpleSpeciesReference instance at %s>" % (self.__class__.__module__, self.__class__.__name__, self.this,)
    def __init__(self, *args):
        """
        __init__(self, string species="") -> SimpleSpeciesReference
        __init__(self) -> SimpleSpeciesReference

        Creates a new SimpleSpeciesReference, optionally with its species
        attribute set.


        """
        _swig_setattr(self, SimpleSpeciesReference, 'this', _libsbml.new_SimpleSpeciesReference(*args))
        _swig_setattr(self, SimpleSpeciesReference, 'thisown', 1)
    def __del__(self, destroy=_libsbml.delete_SimpleSpeciesReference):
        """__del__(self)"""
        try:
            if self.thisown: destroy(self)
        except: pass

    def getSpecies(*args): 
        """
        getSpecies(self) -> string

        Returns the species for this SimpleSpeciesReference.


        """
        return _libsbml.SimpleSpeciesReference_getSpecies(*args)

    def isSetSpecies(*args): 
        """
        isSetSpecies(self) -> bool

        Returns true if the species for this SimpleSpeciesReference has been
        set, false otherwise.


        """
        return _libsbml.SimpleSpeciesReference_isSetSpecies(*args)

    def setSpecies(*args): 
        """
        setSpecies(self, string sid)

        Sets the species of this SimpleSpeciesReference to a copy of sid.


        """
        return _libsbml.SimpleSpeciesReference_setSpecies(*args)


class SimpleSpeciesReferencePtr(SimpleSpeciesReference):
    def __init__(self, this):
        _swig_setattr(self, SimpleSpeciesReference, 'this', this)
        if not hasattr(self,"thisown"): _swig_setattr(self, SimpleSpeciesReference, 'thisown', 0)
        _swig_setattr(self, SimpleSpeciesReference,self.__class__,SimpleSpeciesReference)
_libsbml.SimpleSpeciesReference_swigregister(SimpleSpeciesReferencePtr)

class SpeciesReference(SimpleSpeciesReference):
    """Proxy of C++ SpeciesReference class"""
    __swig_setmethods__ = {}
    for _s in [SimpleSpeciesReference]: __swig_setmethods__.update(_s.__swig_setmethods__)
    __setattr__ = lambda self, name, value: _swig_setattr(self, SpeciesReference, name, value)
    __swig_getmethods__ = {}
    for _s in [SimpleSpeciesReference]: __swig_getmethods__.update(_s.__swig_getmethods__)
    __getattr__ = lambda self, name: _swig_getattr(self, SpeciesReference, name)
    def __repr__(self):
        return "<%s.%s; proxy of C++ SpeciesReference instance at %s>" % (self.__class__.__module__, self.__class__.__name__, self.this,)
    def __init__(self, *args):
        """
        __init__(self, string species="", double stoichiometry=1.0, int denominator=1) -> SpeciesReference
        __init__(self, string species="", double stoichiometry=1.0) -> SpeciesReference
        __init__(self, string species="") -> SpeciesReference
        __init__(self) -> SpeciesReference

        Creates a new SpeciesReference, optionally with its species,
        stoichiometry, and denominator attributes set.


        """
        _swig_setattr(self, SpeciesReference, 'this', _libsbml.new_SpeciesReference(*args))
        _swig_setattr(self, SpeciesReference, 'thisown', 1)
    def __del__(self, destroy=_libsbml.delete_SpeciesReference):
        """__del__(self)"""
        try:
            if self.thisown: destroy(self)
        except: pass

    def initDefaults(*args): 
        """
        initDefaults(self)

        Initializes the fields of this SpeciesReference to their defaults:

          - stoichiometry = 1
          - denominator   = 1


        """
        return _libsbml.SpeciesReference_initDefaults(*args)

    def getStoichiometry(*args): 
        """
        getStoichiometry(self) -> double

        Returns the stoichiometry of this SpeciesReference.


        """
        return _libsbml.SpeciesReference_getStoichiometry(*args)

    def getStoichiometryMath(*args): 
        """
        getStoichiometryMath(self) -> ASTNode

        Returns the stoichiometryMath of this SpeciesReference.


        """
        return _libsbml.SpeciesReference_getStoichiometryMath(*args)

    def getDenominator(*args): 
        """
        getDenominator(self) -> int

        Returns the denominator of this SpeciesReference.


        """
        return _libsbml.SpeciesReference_getDenominator(*args)

    def isSetStoichiometryMath(*args): 
        """
        isSetStoichiometryMath(self) -> bool

        Returns true if the stoichiometryMath of this SpeciesReference has
        been set, false otherwise.


        """
        return _libsbml.SpeciesReference_isSetStoichiometryMath(*args)

    def setStoichiometry(*args): 
        """
        setStoichiometry(self, double value)

        Sets the stoichiometry of this SpeciesReference to value.


        """
        return _libsbml.SpeciesReference_setStoichiometry(*args)

    def setStoichiometryMath(*args):
      if args[1] is not None: args[1].thisown = 0
      return _libsbml.SpeciesReference_setStoichiometryMath(*args)


    def setDenominator(*args): 
        """
        setDenominator(self, int value)

        Sets the denominator of this SpeciesReference to value.


        """
        return _libsbml.SpeciesReference_setDenominator(*args)


class SpeciesReferencePtr(SpeciesReference):
    def __init__(self, this):
        _swig_setattr(self, SpeciesReference, 'this', this)
        if not hasattr(self,"thisown"): _swig_setattr(self, SpeciesReference, 'thisown', 0)
        _swig_setattr(self, SpeciesReference,self.__class__,SpeciesReference)
_libsbml.SpeciesReference_swigregister(SpeciesReferencePtr)

class ModifierSpeciesReference(SimpleSpeciesReference):
    """Proxy of C++ ModifierSpeciesReference class"""
    __swig_setmethods__ = {}
    for _s in [SimpleSpeciesReference]: __swig_setmethods__.update(_s.__swig_setmethods__)
    __setattr__ = lambda self, name, value: _swig_setattr(self, ModifierSpeciesReference, name, value)
    __swig_getmethods__ = {}
    for _s in [SimpleSpeciesReference]: __swig_getmethods__.update(_s.__swig_getmethods__)
    __getattr__ = lambda self, name: _swig_getattr(self, ModifierSpeciesReference, name)
    def __repr__(self):
        return "<%s.%s; proxy of C++ ModifierSpeciesReference instance at %s>" % (self.__class__.__module__, self.__class__.__name__, self.this,)
    def __init__(self, *args):
        """
        __init__(self, string species="") -> ModifierSpeciesReference
        __init__(self) -> ModifierSpeciesReference

        Creates a new ModifierSpeciesReference, optionally with its species
        attribute set.


        """
        _swig_setattr(self, ModifierSpeciesReference, 'this', _libsbml.new_ModifierSpeciesReference(*args))
        _swig_setattr(self, ModifierSpeciesReference, 'thisown', 1)
    def __del__(self, destroy=_libsbml.delete_ModifierSpeciesReference):
        """__del__(self)"""
        try:
            if self.thisown: destroy(self)
        except: pass


class ModifierSpeciesReferencePtr(ModifierSpeciesReference):
    def __init__(self, this):
        _swig_setattr(self, ModifierSpeciesReference, 'this', this)
        if not hasattr(self,"thisown"): _swig_setattr(self, ModifierSpeciesReference, 'thisown', 0)
        _swig_setattr(self, ModifierSpeciesReference,self.__class__,ModifierSpeciesReference)
_libsbml.ModifierSpeciesReference_swigregister(ModifierSpeciesReferencePtr)

class Event(SBase):
    """Proxy of C++ Event class"""
    __swig_setmethods__ = {}
    for _s in [SBase]: __swig_setmethods__.update(_s.__swig_setmethods__)
    __setattr__ = lambda self, name, value: _swig_setattr(self, Event, name, value)
    __swig_getmethods__ = {}
    for _s in [SBase]: __swig_getmethods__.update(_s.__swig_getmethods__)
    __getattr__ = lambda self, name: _swig_getattr(self, Event, name)
    def __repr__(self):
        return "<%s.%s; proxy of C++ Event instance at %s>" % (self.__class__.__module__, self.__class__.__name__, self.this,)
    def __init__(self, *args):
      _swig_setattr(self, Event, 'this', _libsbml.new_Event(*args))
      _swig_setattr(self, Event, 'thisown', 1)
      for index in [1, 2]:
        try:
          if args[index] is not None: args[index].thisown = 0
        except (IndexError, AttributeError):
          pass


    def __del__(self, destroy=_libsbml.delete_Event):
        """__del__(self)"""
        try:
            if self.thisown: destroy(self)
        except: pass

    def getId(*args): 
        """
        getId(self) -> string

        Returns the id of this Event.


        """
        return _libsbml.Event_getId(*args)

    def getName(*args): 
        """
        getName(self) -> string

        Returns the name of this Event.


        """
        return _libsbml.Event_getName(*args)

    def getTrigger(*args): 
        """
        getTrigger(self) -> ASTNode

        Returns the trigger of this Event.


        """
        return _libsbml.Event_getTrigger(*args)

    def getDelay(*args): 
        """
        getDelay(self) -> ASTNode

        Returns the delay of this Event.


        """
        return _libsbml.Event_getDelay(*args)

    def getTimeUnits(*args): 
        """
        getTimeUnits(self) -> string

        Returns the timeUnits of this Event.


        """
        return _libsbml.Event_getTimeUnits(*args)

    def isSetId(*args): 
        """
        isSetId(self) -> bool

        Returns true if the id of this Event has been set, false otherwise.


        """
        return _libsbml.Event_isSetId(*args)

    def isSetName(*args): 
        """
        isSetName(self) -> bool

        Returns true if the name of this Event has been set, false otherwise.


        """
        return _libsbml.Event_isSetName(*args)

    def isSetTrigger(*args): 
        """
        isSetTrigger(self) -> bool

        Returns true if the trigger of this Event has been set, false
        otherwise.


        """
        return _libsbml.Event_isSetTrigger(*args)

    def isSetDelay(*args): 
        """
        isSetDelay(self) -> bool

        Returns true if the delay of this Event has been set, false otherwise.


        """
        return _libsbml.Event_isSetDelay(*args)

    def isSetTimeUnits(*args): 
        """
        isSetTimeUnits(self) -> bool

        Returns true if the timeUnits of this Event has been set, false
        otherwise.


        """
        return _libsbml.Event_isSetTimeUnits(*args)

    def setId(*args): 
        """
        setId(self, string sid)

        Sets the id of this Event to a copy of sid.


        """
        return _libsbml.Event_setId(*args)

    def setName(*args): 
        """
        setName(self, string str)

        Sets the name of this Event to a copy of string.


        """
        return _libsbml.Event_setName(*args)

    def setTrigger(*args):
      if args[1] is not None: args[1].thisown = 0
      return _libsbml.Event_setTrigger(*args)


    def setDelay(*args):
      if args[1] is not None: args[1].thisown = 0
      return _libsbml.Event_setDelay(*args)


    def setTimeUnits(*args): 
        """
        setTimeUnits(self, string sid)

        Sets the timeUnits of this Event to a copy of sid.


        """
        return _libsbml.Event_setTimeUnits(*args)

    def unsetId(*args): 
        """
        unsetId(self)

        Unsets the id of this Event.


        """
        return _libsbml.Event_unsetId(*args)

    def unsetName(*args): 
        """
        unsetName(self)

        Unsets the name of this Event.


        """
        return _libsbml.Event_unsetName(*args)

    def unsetDelay(*args): 
        """
        unsetDelay(self)

        Unsets the delay of this Event.


        """
        return _libsbml.Event_unsetDelay(*args)

    def unsetTimeUnits(*args): 
        """
        unsetTimeUnits(self)

        Unsets the timeUnits of this Event.


        """
        return _libsbml.Event_unsetTimeUnits(*args)

    def addEventAssignment(*args):
      if args[1] is not None: args[1].thisown = 0
      return _libsbml.Event_addEventAssignment(*args)


    def getListOfEventAssignments(*args): 
        """
        getListOfEventAssignments(self) -> ListOf

        Returns the list of EventAssignments for this Event.


        """
        return _libsbml.Event_getListOfEventAssignments(*args)

    def getEventAssignment(*args): 
        """
        getEventAssignment(self, unsigned int n) -> EventAssignment

        Returns the nth EventAssignment of this Event.


        """
        return _libsbml.Event_getEventAssignment(*args)

    def getNumEventAssignments(*args): 
        """
        getNumEventAssignments(self) -> unsigned int

        Returns the number of EventAssignments in this Event.


        """
        return _libsbml.Event_getNumEventAssignments(*args)


class EventPtr(Event):
    def __init__(self, this):
        _swig_setattr(self, Event, 'this', this)
        if not hasattr(self,"thisown"): _swig_setattr(self, Event, 'thisown', 0)
        _swig_setattr(self, Event,self.__class__,Event)
_libsbml.Event_swigregister(EventPtr)

class EventAssignment(SBase):
    """Proxy of C++ EventAssignment class"""
    __swig_setmethods__ = {}
    for _s in [SBase]: __swig_setmethods__.update(_s.__swig_setmethods__)
    __setattr__ = lambda self, name, value: _swig_setattr(self, EventAssignment, name, value)
    __swig_getmethods__ = {}
    for _s in [SBase]: __swig_getmethods__.update(_s.__swig_getmethods__)
    __getattr__ = lambda self, name: _swig_getattr(self, EventAssignment, name)
    def __repr__(self):
        return "<%s.%s; proxy of C++ EventAssignment instance at %s>" % (self.__class__.__module__, self.__class__.__name__, self.this,)
    def __init__(self, *args):
      _swig_setattr(self, EventAssignment, 'this', _libsbml.new_EventAssignment(*args))
      _swig_setattr(self, EventAssignment, 'thisown', 1)
      try:
        if args[1] is not None: args[1].thisown = 0
      except (IndexError, AttributeError):
        pass


    def __del__(self, destroy=_libsbml.delete_EventAssignment):
        """__del__(self)"""
        try:
            if self.thisown: destroy(self)
        except: pass

    def getVariable(*args): 
        """
        getVariable(self) -> string

        Returns the variable of this EventAssignment.


        """
        return _libsbml.EventAssignment_getVariable(*args)

    def getMath(*args): 
        """
        getMath(self) -> ASTNode

        Returns the math of this EventAssignment.


        """
        return _libsbml.EventAssignment_getMath(*args)

    def isSetVariable(*args): 
        """
        isSetVariable(self) -> bool

        Returns true if the variable of this EventAssignment has been set, false
        otherwise.


        """
        return _libsbml.EventAssignment_isSetVariable(*args)

    def isSetMath(*args): 
        """
        isSetMath(self) -> bool

        Returns true if the math of this EventAssignment has been set, false
        otherwise.


        """
        return _libsbml.EventAssignment_isSetMath(*args)

    def setVariable(*args): 
        """
        setVariable(self, string sid)

        Sets the variable of this EventAssignment to a copy of sid.


        """
        return _libsbml.EventAssignment_setVariable(*args)

    def setMath(*args):
      if args[1] is not None: args[1].thisown = 0
      return _libsbml.EventAssignment_setMath(*args)



class EventAssignmentPtr(EventAssignment):
    def __init__(self, this):
        _swig_setattr(self, EventAssignment, 'this', this)
        if not hasattr(self,"thisown"): _swig_setattr(self, EventAssignment, 'thisown', 0)
        _swig_setattr(self, EventAssignment,self.__class__,EventAssignment)
_libsbml.EventAssignment_swigregister(EventAssignmentPtr)

UNIT_KIND_AMPERE = _libsbml.UNIT_KIND_AMPERE
UNIT_KIND_BECQUEREL = _libsbml.UNIT_KIND_BECQUEREL
UNIT_KIND_CANDELA = _libsbml.UNIT_KIND_CANDELA
UNIT_KIND_CELSIUS = _libsbml.UNIT_KIND_CELSIUS
UNIT_KIND_COULOMB = _libsbml.UNIT_KIND_COULOMB
UNIT_KIND_DIMENSIONLESS = _libsbml.UNIT_KIND_DIMENSIONLESS
UNIT_KIND_FARAD = _libsbml.UNIT_KIND_FARAD
UNIT_KIND_GRAM = _libsbml.UNIT_KIND_GRAM
UNIT_KIND_GRAY = _libsbml.UNIT_KIND_GRAY
UNIT_KIND_HENRY = _libsbml.UNIT_KIND_HENRY
UNIT_KIND_HERTZ = _libsbml.UNIT_KIND_HERTZ
UNIT_KIND_ITEM = _libsbml.UNIT_KIND_ITEM
UNIT_KIND_JOULE = _libsbml.UNIT_KIND_JOULE
UNIT_KIND_KATAL = _libsbml.UNIT_KIND_KATAL
UNIT_KIND_KELVIN = _libsbml.UNIT_KIND_KELVIN
UNIT_KIND_KILOGRAM = _libsbml.UNIT_KIND_KILOGRAM
UNIT_KIND_LITER = _libsbml.UNIT_KIND_LITER
UNIT_KIND_LITRE = _libsbml.UNIT_KIND_LITRE
UNIT_KIND_LUMEN = _libsbml.UNIT_KIND_LUMEN
UNIT_KIND_LUX = _libsbml.UNIT_KIND_LUX
UNIT_KIND_METER = _libsbml.UNIT_KIND_METER
UNIT_KIND_METRE = _libsbml.UNIT_KIND_METRE
UNIT_KIND_MOLE = _libsbml.UNIT_KIND_MOLE
UNIT_KIND_NEWTON = _libsbml.UNIT_KIND_NEWTON
UNIT_KIND_OHM = _libsbml.UNIT_KIND_OHM
UNIT_KIND_PASCAL = _libsbml.UNIT_KIND_PASCAL
UNIT_KIND_RADIAN = _libsbml.UNIT_KIND_RADIAN
UNIT_KIND_SECOND = _libsbml.UNIT_KIND_SECOND
UNIT_KIND_SIEMENS = _libsbml.UNIT_KIND_SIEMENS
UNIT_KIND_SIEVERT = _libsbml.UNIT_KIND_SIEVERT
UNIT_KIND_STERADIAN = _libsbml.UNIT_KIND_STERADIAN
UNIT_KIND_TESLA = _libsbml.UNIT_KIND_TESLA
UNIT_KIND_VOLT = _libsbml.UNIT_KIND_VOLT
UNIT_KIND_WATT = _libsbml.UNIT_KIND_WATT
UNIT_KIND_WEBER = _libsbml.UNIT_KIND_WEBER
UNIT_KIND_INVALID = _libsbml.UNIT_KIND_INVALID

def UnitKind_equals(*args):
    """
    UnitKind_equals(UnitKind_t uk1, UnitKind_t uk2) -> int

    Tests for logical equality between two UnitKinds.  This function behaves
    exactly like C's == operator, except for the following two cases:

      - UNIT_KIND_LITER == UNIT_KIND_LITRE
      - UNIT_KIND_METER == UNIT_KIND_METRE

    where C would yield false (since each of the above is a distinct
    enumeration value), UnitKind_equals(...) yields true.

    Returns true (!0) if uk1 is logically equivalent to uk2, false (0) otherwise.


    """
    return _libsbml.UnitKind_equals(*args)

def UnitKind_forName(*args):
    """
    UnitKind_forName(char name) -> int

    Returns the UnitKind with the given name (case-insensitive).


    """
    return _libsbml.UnitKind_forName(*args)

def UnitKind_toString(*args):
    """
    UnitKind_toString(UnitKind_t uk) -> char

    Returns the name of the given UnitKind.  The caller does not own the
    returned string and is therefore not allowed to modify it.


    """
    return _libsbml.UnitKind_toString(*args)

def UnitKind_isValidUnitKindString(*args):
    """
    UnitKind_isValidUnitKindString(char string) -> int

    Returns nonzero if string is the name of a valid unitKind.


    """
    return _libsbml.UnitKind_isValidUnitKindString(*args)
RULE_TYPE_RATE = _libsbml.RULE_TYPE_RATE
RULE_TYPE_SCALAR = _libsbml.RULE_TYPE_SCALAR
RULE_TYPE_INVALID = _libsbml.RULE_TYPE_INVALID

def RuleType_forName(*args):
    """
    RuleType_forName(char name) -> int

    Returns the RuleType with the given name (case-insensitive).


    """
    return _libsbml.RuleType_forName(*args)

def RuleType_toString(*args):
    """
    RuleType_toString(RuleType_t rt) -> char

    Returns the name of the given RuleType.  The caller does not own the
    returned string and is therefore not allowed to modify it.


    """
    return _libsbml.RuleType_toString(*args)

def readMathMLFromString(*args):
    """
    readMathMLFromString(char xml) -> MathMLDocument_t

    Reads the MathML from the given XML string, constructs a corresponding
    abstract syntax tree and returns a pointer to the root of the tree.


    """
    return _libsbml.readMathMLFromString(*args)
class MathMLWriter(_object):
    """Proxy of C++ MathMLWriter class"""
    __swig_setmethods__ = {}
    __setattr__ = lambda self, name, value: _swig_setattr(self, MathMLWriter, name, value)
    __swig_getmethods__ = {}
    __getattr__ = lambda self, name: _swig_getattr(self, MathMLWriter, name)
    def __repr__(self):
        return "<%s.%s; proxy of C++ MathMLWriter instance at %s>" % (self.__class__.__module__, self.__class__.__name__, self.this,)
    def __init__(self, *args):
        """
        __init__(self) -> MathMLWriter

        Creates a new MathMLWriter.


        """
        _swig_setattr(self, MathMLWriter, 'this', _libsbml.new_MathMLWriter(*args))
        _swig_setattr(self, MathMLWriter, 'thisown', 1)
    def __del__(self, destroy=_libsbml.delete_MathMLWriter):
        """__del__(self)"""
        try:
            if self.thisown: destroy(self)
        except: pass

    def write(*args): 
        """
        write(self, MathMLDocument d, string filename) -> bool

        Writes the given MathML document to the output stream.

        Returns true on success and false if one of the underlying Xerces or
        Expat components fail (rare).


        """
        return _libsbml.MathMLWriter_write(*args)

    def writeToString(*args): 
        """
        writeToString(self, MathMLDocument d) -> char

        Writes the given MathML document to an in-memory string and returns a
        pointer to it.  The string is owned by the caller and should be freed
        (with free()) when no longer needed.

        Returns the string on success and 0 if one of the underlying Xerces or
        Expat components fail (rare).


        """
        return _libsbml.MathMLWriter_writeToString(*args)


class MathMLWriterPtr(MathMLWriter):
    def __init__(self, this):
        _swig_setattr(self, MathMLWriter, 'this', this)
        if not hasattr(self,"thisown"): _swig_setattr(self, MathMLWriter, 'thisown', 0)
        _swig_setattr(self, MathMLWriter,self.__class__,MathMLWriter)
_libsbml.MathMLWriter_swigregister(MathMLWriterPtr)


def writeMathML(*args):
    """
    writeMathML(MathMLDocument_t d, char filename) -> int

    Writes the given MathML document to filename.

    Returns true on success and false if the filename could not be opened
    for writing.


    """
    return _libsbml.writeMathML(*args)

def writeMathMLToString(*args):
    """
    writeMathMLToString(MathMLDocument_t d) -> char

    Writes the given MathML document to an in-memory string and returns a
    pointer to it.  The string is owned by the caller and should be freed
    (with free()) when no longer needed.

    Returns the string on success and 0 if one of the underlying Xerces or
    Expat components fail (rare).


    """
    return _libsbml.writeMathMLToString(*args)
class ASTNode(_object):
    """Proxy of C++ ASTNode class"""
    __swig_setmethods__ = {}
    __setattr__ = lambda self, name, value: _swig_setattr(self, ASTNode, name, value)
    __swig_getmethods__ = {}
    __getattr__ = lambda self, name: _swig_getattr(self, ASTNode, name)
    def __repr__(self):
        return "<%s.%s; proxy of C++ ASTNode instance at %s>" % (self.__class__.__module__, self.__class__.__name__, self.this,)
    def __init__(self, *args):
        """
        __init__(self, ASTNodeType_t type=AST_UNKNOWN) -> ASTNode
        __init__(self) -> ASTNode

        Creates a new ASTNode.

        By default, node will have a type of AST_UNKNOWN and should be set to
        something else as soon as possible.


        """
        _swig_setattr(self, ASTNode, 'this', _libsbml.new_ASTNode(*args))
        _swig_setattr(self, ASTNode, 'thisown', 1)
    def __del__(self, destroy=_libsbml.delete_ASTNode):
        """__del__(self)"""
        try:
            if self.thisown: destroy(self)
        except: pass

    def canonicalize(*args): 
        """
        canonicalize(self) -> bool

        Attempts to convert this ASTNode to a canonical form and returns true
        if the conversion succeeded, false otherwise.

        The rules determining the canonical form conversion are as follows:

          1. If the node type is AST_NAME and the node name matches
          'ExponentialE', 'Pi', 'True' or 'False' the node type is converted
          to the corresponding AST_CONSTANT type.

          2. If the node type is an AST_FUNCTION and the node name matches an
          L1 or L2 (MathML) function name, logical operator name, or
          relational operator name, the node is converted to the correspnding
          AST_FUNCTION, AST_LOGICAL or AST_CONSTANT type.

        L1 function names are searched first, so canonicalizing 'log' will
        result in a node type of AST_FUNCTION_LN (see L1 Specification,
        Appendix C).

        Some canonicalizations result in a structural converion of the nodes
        (by adding a child).  For example, a node with L1 function name 'sqr'
        and a single child node (the argument) will be transformed to a node
        of type AST_FUNCTION_POWER with two children.  The first child will
        remain unchanged, but the second child will be an ASTNode of type
        AST_INTEGER and a value of 2.  The function names that result in
        structural changes are: log10, sqr and sqrt.


        """
        return _libsbml.ASTNode_canonicalize(*args)

    def addChild(*args):
      if args[1] is not None: args[1].thisown = 0
      return _libsbml.ASTNode_addChild(*args)


    def prependChild(*args):
      if args[1] is not None: args[1].thisown = 0
      return _libsbml.ASTNode_prependChild(*args)


    def deepCopy(*args): 
        """deepCopy(self) -> ASTNode"""
        return _libsbml.ASTNode_deepCopy(*args)

    def getChild(*args): 
        """
        getChild(self, unsigned int n) -> ASTNode

        Returns the nth child of this ASTNode or NULL if this node has no nth
        child (n > ASTNode_getNumChildren() - 1).


        """
        return _libsbml.ASTNode_getChild(*args)

    def getLeftChild(*args): 
        """
        getLeftChild(self) -> ASTNode

        Returns the left child of this ASTNode.  This is equivalent to
        getChild(0);


        """
        return _libsbml.ASTNode_getLeftChild(*args)

    def getRightChild(*args): 
        """
        getRightChild(self) -> ASTNode

        Returns the right child of this ASTNode or NULL if this node has no
        right child.  If getNumChildren() > 1, then this is equivalent to:

          getChild( getNumChildren() - 1);


        """
        return _libsbml.ASTNode_getRightChild(*args)

    def getNumChildren(*args): 
        """
        getNumChildren(self) -> unsigned int

        Returns the number of children of this ASTNode or 0 is this node has
        no children.


        """
        return _libsbml.ASTNode_getNumChildren(*args)

    def getCharacter(*args): 
        """
        getCharacter(self) -> char

        Returns the value of this ASTNode as a single character.  This
        function should be called only when getType() is one of AST_PLUS,
        AST_MINUS, AST_TIMES, AST_DIVIDE or AST_POWER.


        """
        return _libsbml.ASTNode_getCharacter(*args)

    def getInteger(*args): 
        """
        getInteger(self) -> long

        Returns the value of this ASTNode as a (long) integer.  This function
        should be called only when getType() == AST_INTEGER.


        """
        return _libsbml.ASTNode_getInteger(*args)

    def getName(*args): 
        """
        getName(self) -> char

        Returns the value of this ASTNode as a string.  This function may be
        called on nodes that are not operators (isOperator() == false) or
        numbers (isNumber() == false).


        """
        return _libsbml.ASTNode_getName(*args)

    def getNumerator(*args): 
        """
        getNumerator(self) -> long

        Returns the value of the numerator of this ASTNode.  This function
        should be called only when getType() == AST_RATIONAL.


        """
        return _libsbml.ASTNode_getNumerator(*args)

    def getDenominator(*args): 
        """
        getDenominator(self) -> long

        Returns the value of the denominator of this ASTNode.  This function
        should be called only when getType() == AST_RATIONAL.


        """
        return _libsbml.ASTNode_getDenominator(*args)

    def getReal(*args): 
        """
        getReal(self) -> double

        Returns the value of this ASTNode as a real (double).  This function
        should be called only when isReal() == true.

        This function performs the necessary arithmetic if the node type is
        AST_REAL_E (mantissa  $10^exponent$) or AST_RATIONAL (numerator /
        denominator).


        """
        return _libsbml.ASTNode_getReal(*args)

    def getMantissa(*args): 
        """
        getMantissa(self) -> double

        Returns the value of the mantissa of this ASTNode.  This function
        should be called only when getType() is AST_REAL_E or AST_REAL.  If
        AST_REAL, this method is identical to getReal().


        """
        return _libsbml.ASTNode_getMantissa(*args)

    def getExponent(*args): 
        """
        getExponent(self) -> long

        Returns the value of the exponent of this ASTNode.  This function
        should be called only when getType() is AST_REAL_E or AST_REAL.


        """
        return _libsbml.ASTNode_getExponent(*args)

    def getPrecedence(*args): 
        """
        getPrecedence(self) -> int

        Returns the precedence of this ASTNode (as defined in the SBML L1
        specification).


        """
        return _libsbml.ASTNode_getPrecedence(*args)

    def getType(*args): 
        """
        getType(self) -> int

        Returns the type of this ASTNode.


        """
        return _libsbml.ASTNode_getType(*args)

    def isBoolean(*args): 
        """
        isBoolean(self) -> bool

        Returns true if this ASTNode is a boolean (a logical operator, a
        relational operator, or the constants true or false), false otherwise.


        """
        return _libsbml.ASTNode_isBoolean(*args)

    def isConstant(*args): 
        """
        isConstant(self) -> bool

        Returns true if this ASTNode is a MathML constant (true, false, pi,
        exponentiale), false otherwise.


        """
        return _libsbml.ASTNode_isConstant(*args)

    def isFunction(*args): 
        """
        isFunction(self) -> bool

        Returns true if this ASTNode is a function in SBML L1, L2 (MathML)
        (everything from abs() to tanh()) or user-defined, false otherwise.


        """
        return _libsbml.ASTNode_isFunction(*args)

    def isInteger(*args): 
        """
        isInteger(self) -> bool

        Returns true if this ASTNode is of type AST_INTEGER, false otherwise.


        """
        return _libsbml.ASTNode_isInteger(*args)

    def isLambda(*args): 
        """
        isLambda(self) -> bool

        Returns true if this ASTNode is of type AST_LAMBDA, false otherwise.


        """
        return _libsbml.ASTNode_isLambda(*args)

    def isLog10(*args): 
        """
        isLog10(self) -> bool

        Returns true if the given ASTNode represents a log10() function, false
        otherwise.

        More precisley, the node type is AST_FUNCTION_LOG with two children
        the first of which is an AST_INTEGER equal to 10.


        """
        return _libsbml.ASTNode_isLog10(*args)

    def isLogical(*args): 
        """
        isLogical(self) -> bool

        Returns true if this ASTNode is a MathML logical operator (and, or,
        not, xor), false otherwise.


        """
        return _libsbml.ASTNode_isLogical(*args)

    def isName(*args): 
        """
        isName(self) -> bool

        Returns true if this ASTNode is a user-defined variable name in SBML
        L1, L2 (MathML) or the special symbols delay or time, false otherwise.


        """
        return _libsbml.ASTNode_isName(*args)

    def isNumber(*args): 
        """
        isNumber(self) -> bool

        Returns true if this ASTNode is a number, false otherwise.

        This is functionally equivalent to:

          isInteger() || isReal().


        """
        return _libsbml.ASTNode_isNumber(*args)

    def isOperator(*args): 
        """
        isOperator(self) -> bool

        Returns true if this ASTNode is an operator, false otherwise.
        Operators are: +, -, , / and \^ (power).


        """
        return _libsbml.ASTNode_isOperator(*args)

    def isRational(*args): 
        """
        isRational(self) -> bool

        Returns true if this ASTNode is of type AST_RATIONAL, false otherwise.


        """
        return _libsbml.ASTNode_isRational(*args)

    def isReal(*args): 
        """
        isReal(self) -> bool

        Returns true if the value of this ASTNode can represented as a real
        number, false otherwise.

        To be a represented as a real number, this node must be of one of the
        following types: AST_REAL, AST_REAL_E or AST_RATIONAL.


        """
        return _libsbml.ASTNode_isReal(*args)

    def isRelational(*args): 
        """
        isRelational(self) -> bool

        Returns true if this ASTNode is a MathML relational operator (==, >=,
        >, <=, < !=), false otherwise.


        """
        return _libsbml.ASTNode_isRelational(*args)

    def isSqrt(*args): 
        """
        isSqrt(self) -> bool

        Returns true if the given ASTNode represents a sqrt() function, false
        otherwise.

        More precisley, the node type is AST_FUNCTION_ROOT with two children
        the first of which is an AST_INTEGER equal to 2.


        """
        return _libsbml.ASTNode_isSqrt(*args)

    def isUMinus(*args): 
        """
        isUMinus(self) -> bool

        Returns true if this ASTNode is a unary minus, false otherwise.

        For numbers, unary minus nodes can be 'collapsed' by negating the
        number.  In fact, SBML_parseFormula() does this during its parse.
        However, unary minus nodes for symbols (AST_NAMES) cannot be
        'collapsed', so this predicate function is necessary.

        A node is defined as a unary minus node if it is of type AST_MINUS and
        has exactly one child.


        """
        return _libsbml.ASTNode_isUMinus(*args)

    def isUnknown(*args): 
        """
        isUnknown(self) -> bool

        Returns true if this ASTNode is of type AST_UNKNOWN, false otherwise.


        """
        return _libsbml.ASTNode_isUnknown(*args)

    def setCharacter(*args): 
        """
        setCharacter(self, char value)

        Sets the value of this ASTNode to the given character.  If character
        is one of '+', '-', '', '/' or '\^', the node type will be set
        accordingly.  For all other characters, the node type will be set to
        AST_UNKNOWN.


        """
        return _libsbml.ASTNode_setCharacter(*args)

    def setName(*args): 
        """
        setName(self, char name)

        Sets the value of this ASTNode to the given name.

        The node type will be set (to AST_NAME) ONLY IF the ASTNode was
        previously an operator (isOperator(node) == true) or number
        (isNumber(node) == true).  This allows names to be set for
        AST_FUNCTIONs and the like.


        """
        return _libsbml.ASTNode_setName(*args)

    def setValue(*args): 
        """
        setValue(self, long value)
        setValue(self, long numerator, long denominator)
        setValue(self, double value)
        setValue(self, double mantissa, long exponent)

        Sets the value of this ASTNode to the given real (double) in two
        parts: the mantissa and the exponent.  The node type is set to
        AST_REAL_E.


        """
        return _libsbml.ASTNode_setValue(*args)

    def setType(*args): 
        """
        setType(self, ASTNodeType_t type)

        Sets the type of this ASTNode to the given ASTNodeType.


        """
        return _libsbml.ASTNode_setType(*args)


class ASTNodePtr(ASTNode):
    def __init__(self, this):
        _swig_setattr(self, ASTNode, 'this', this)
        if not hasattr(self,"thisown"): _swig_setattr(self, ASTNode, 'thisown', 0)
        _swig_setattr(self, ASTNode,self.__class__,ASTNode)
_libsbml.ASTNode_swigregister(ASTNodePtr)

AST_PLUS = _libsbml.AST_PLUS
AST_MINUS = _libsbml.AST_MINUS
AST_TIMES = _libsbml.AST_TIMES
AST_DIVIDE = _libsbml.AST_DIVIDE
AST_POWER = _libsbml.AST_POWER
AST_INTEGER = _libsbml.AST_INTEGER
AST_REAL = _libsbml.AST_REAL
AST_REAL_E = _libsbml.AST_REAL_E
AST_RATIONAL = _libsbml.AST_RATIONAL
AST_NAME = _libsbml.AST_NAME
AST_NAME_TIME = _libsbml.AST_NAME_TIME
AST_CONSTANT_E = _libsbml.AST_CONSTANT_E
AST_CONSTANT_FALSE = _libsbml.AST_CONSTANT_FALSE
AST_CONSTANT_PI = _libsbml.AST_CONSTANT_PI
AST_CONSTANT_TRUE = _libsbml.AST_CONSTANT_TRUE
AST_LAMBDA = _libsbml.AST_LAMBDA
AST_FUNCTION = _libsbml.AST_FUNCTION
AST_FUNCTION_ABS = _libsbml.AST_FUNCTION_ABS
AST_FUNCTION_ARCCOS = _libsbml.AST_FUNCTION_ARCCOS
AST_FUNCTION_ARCCOSH = _libsbml.AST_FUNCTION_ARCCOSH
AST_FUNCTION_ARCCOT = _libsbml.AST_FUNCTION_ARCCOT
AST_FUNCTION_ARCCOTH = _libsbml.AST_FUNCTION_ARCCOTH
AST_FUNCTION_ARCCSC = _libsbml.AST_FUNCTION_ARCCSC
AST_FUNCTION_ARCCSCH = _libsbml.AST_FUNCTION_ARCCSCH
AST_FUNCTION_ARCSEC = _libsbml.AST_FUNCTION_ARCSEC
AST_FUNCTION_ARCSECH = _libsbml.AST_FUNCTION_ARCSECH
AST_FUNCTION_ARCSIN = _libsbml.AST_FUNCTION_ARCSIN
AST_FUNCTION_ARCSINH = _libsbml.AST_FUNCTION_ARCSINH
AST_FUNCTION_ARCTAN = _libsbml.AST_FUNCTION_ARCTAN
AST_FUNCTION_ARCTANH = _libsbml.AST_FUNCTION_ARCTANH
AST_FUNCTION_CEILING = _libsbml.AST_FUNCTION_CEILING
AST_FUNCTION_COS = _libsbml.AST_FUNCTION_COS
AST_FUNCTION_COSH = _libsbml.AST_FUNCTION_COSH
AST_FUNCTION_COT = _libsbml.AST_FUNCTION_COT
AST_FUNCTION_COTH = _libsbml.AST_FUNCTION_COTH
AST_FUNCTION_CSC = _libsbml.AST_FUNCTION_CSC
AST_FUNCTION_CSCH = _libsbml.AST_FUNCTION_CSCH
AST_FUNCTION_DELAY = _libsbml.AST_FUNCTION_DELAY
AST_FUNCTION_EXP = _libsbml.AST_FUNCTION_EXP
AST_FUNCTION_FACTORIAL = _libsbml.AST_FUNCTION_FACTORIAL
AST_FUNCTION_FLOOR = _libsbml.AST_FUNCTION_FLOOR
AST_FUNCTION_LN = _libsbml.AST_FUNCTION_LN
AST_FUNCTION_LOG = _libsbml.AST_FUNCTION_LOG
AST_FUNCTION_PIECEWISE = _libsbml.AST_FUNCTION_PIECEWISE
AST_FUNCTION_POWER = _libsbml.AST_FUNCTION_POWER
AST_FUNCTION_ROOT = _libsbml.AST_FUNCTION_ROOT
AST_FUNCTION_SEC = _libsbml.AST_FUNCTION_SEC
AST_FUNCTION_SECH = _libsbml.AST_FUNCTION_SECH
AST_FUNCTION_SIN = _libsbml.AST_FUNCTION_SIN
AST_FUNCTION_SINH = _libsbml.AST_FUNCTION_SINH
AST_FUNCTION_TAN = _libsbml.AST_FUNCTION_TAN
AST_FUNCTION_TANH = _libsbml.AST_FUNCTION_TANH
AST_LOGICAL_AND = _libsbml.AST_LOGICAL_AND
AST_LOGICAL_NOT = _libsbml.AST_LOGICAL_NOT
AST_LOGICAL_OR = _libsbml.AST_LOGICAL_OR
AST_LOGICAL_XOR = _libsbml.AST_LOGICAL_XOR
AST_RELATIONAL_EQ = _libsbml.AST_RELATIONAL_EQ
AST_RELATIONAL_GEQ = _libsbml.AST_RELATIONAL_GEQ
AST_RELATIONAL_GT = _libsbml.AST_RELATIONAL_GT
AST_RELATIONAL_LEQ = _libsbml.AST_RELATIONAL_LEQ
AST_RELATIONAL_LT = _libsbml.AST_RELATIONAL_LT
AST_RELATIONAL_NEQ = _libsbml.AST_RELATIONAL_NEQ
AST_UNKNOWN = _libsbml.AST_UNKNOWN

def parseFormula(*args):
    """
    parseFormula(char formula) -> ASTNode_t

    Parses the given SBML formula and returns a representation of it as an
    Abstract Syntax Tree (AST).  The root node of the AST is returned.

    If the formula contains a grammatical error, NULL is returned.


    """
    return _libsbml.parseFormula(*args)
class MathMLDocument(_object):
    """Proxy of C++ MathMLDocument class"""
    __swig_setmethods__ = {}
    __setattr__ = lambda self, name, value: _swig_setattr(self, MathMLDocument, name, value)
    __swig_getmethods__ = {}
    __getattr__ = lambda self, name: _swig_getattr(self, MathMLDocument, name)
    def __repr__(self):
        return "<%s.%s; proxy of C++ MathMLDocument instance at %s>" % (self.__class__.__module__, self.__class__.__name__, self.this,)
    def __init__(self, *args):
        """
        __init__(self) -> MathMLDocument

        Creates a new MathMLDocument.


        """
        _swig_setattr(self, MathMLDocument, 'this', _libsbml.new_MathMLDocument(*args))
        _swig_setattr(self, MathMLDocument, 'thisown', 1)
    def __del__(self, destroy=_libsbml.delete_MathMLDocument):
        """__del__(self)"""
        try:
            if self.thisown: destroy(self)
        except: pass

    def getMath(*args): 
        """
        getMath(self) -> ASTNode

        Returns the an abstract syntax tree (AST) representation of the math
        in this MathMLDocument.


        """
        return _libsbml.MathMLDocument_getMath(*args)

    def isSetMath(*args): 
        """
        isSetMath(self) -> bool

        Returns true if the math of this MathMLDocument has been set, false
        otherwise.


        """
        return _libsbml.MathMLDocument_isSetMath(*args)

    def setMath(*args):
      if args[1] is not None: args[1].thisown = 0
      return _libsbml.MathMLDocument_setMath(*args)



class MathMLDocumentPtr(MathMLDocument):
    def __init__(self, this):
        _swig_setattr(self, MathMLDocument, 'this', this)
        if not hasattr(self,"thisown"): _swig_setattr(self, MathMLDocument, 'thisown', 0)
        _swig_setattr(self, MathMLDocument,self.__class__,MathMLDocument)
_libsbml.MathMLDocument_swigregister(MathMLDocumentPtr)

class ParseMessage(_object):
    """Proxy of C++ ParseMessage class"""
    __swig_setmethods__ = {}
    __setattr__ = lambda self, name, value: _swig_setattr(self, ParseMessage, name, value)
    __swig_getmethods__ = {}
    __getattr__ = lambda self, name: _swig_getattr(self, ParseMessage, name)
    def __repr__(self):
        return "<%s.%s; proxy of C++ ParseMessage instance at %s>" % (self.__class__.__module__, self.__class__.__name__, self.this,)
    def __init__(self, *args):
        """
        __init__(self, unsigned int id=0, string message="", unsigned int line=0, 
            unsigned int col=0) -> ParseMessage
        __init__(self, unsigned int id=0, string message="", unsigned int line=0) -> ParseMessage
        __init__(self, unsigned int id=0, string message="") -> ParseMessage
        __init__(self, unsigned int id=0) -> ParseMessage
        __init__(self) -> ParseMessage
        __init__(self, ParseMessage msg) -> ParseMessage

        Creates a new ParseMessage by copying an existing ParseMessage.


        """
        _swig_setattr(self, ParseMessage, 'this', _libsbml.new_ParseMessage(*args))
        _swig_setattr(self, ParseMessage, 'thisown', 1)
    def __del__(self, destroy=_libsbml.delete_ParseMessage):
        """__del__(self)"""
        try:
            if self.thisown: destroy(self)
        except: pass

    def getId(*args): 
        """
        getId(self) -> unsigned int

        Returns the id of this ParseMessage.


        """
        return _libsbml.ParseMessage_getId(*args)

    def getMessage(*args): 
        """
        getMessage(self) -> string

        Returns the message text of this ParseMessage.


        """
        return _libsbml.ParseMessage_getMessage(*args)

    def getLine(*args): 
        """
        getLine(self) -> unsigned int

        Returns the line number where this ParseMessage ocurred.


        """
        return _libsbml.ParseMessage_getLine(*args)

    def getColumn(*args): 
        """
        getColumn(self) -> unsigned int

        Returns the column number where this ParseMessage occurred.


        """
        return _libsbml.ParseMessage_getColumn(*args)


class ParseMessagePtr(ParseMessage):
    def __init__(self, this):
        _swig_setattr(self, ParseMessage, 'this', this)
        if not hasattr(self,"thisown"): _swig_setattr(self, ParseMessage, 'thisown', 0)
        _swig_setattr(self, ParseMessage,self.__class__,ParseMessage)
_libsbml.ParseMessage_swigregister(ParseMessagePtr)

class XMLNamespace(_object):
    """Proxy of C++ XMLNamespace class"""
    __swig_setmethods__ = {}
    __setattr__ = lambda self, name, value: _swig_setattr(self, XMLNamespace, name, value)
    __swig_getmethods__ = {}
    __getattr__ = lambda self, name: _swig_getattr(self, XMLNamespace, name)
    def __repr__(self):
        return "<%s.%s; proxy of C++ XMLNamespace instance at %s>" % (self.__class__.__module__, self.__class__.__name__, self.this,)
    def __init__(self, *args):
        """
        __init__(self, string prefix, string URI) -> XMLNamespace

        Creates a new XMLNamespace with prefix and URI.

        If prefix starts with 'xmlns:' (case-insensitive), it will be removed.


        """
        _swig_setattr(self, XMLNamespace, 'this', _libsbml.new_XMLNamespace(*args))
        _swig_setattr(self, XMLNamespace, 'thisown', 1)
    def getPrefix(*args): 
        """getPrefix(self) -> string"""
        return _libsbml.XMLNamespace_getPrefix(*args)

    def getURI(*args): 
        """getURI(self) -> string"""
        return _libsbml.XMLNamespace_getURI(*args)

    __swig_getmethods__["startsWithXMLNS"] = lambda x: _libsbml.XMLNamespace_startsWithXMLNS
    if _newclass:startsWithXMLNS = staticmethod(_libsbml.XMLNamespace_startsWithXMLNS)
    def __del__(self, destroy=_libsbml.delete_XMLNamespace):
        """__del__(self)"""
        try:
            if self.thisown: destroy(self)
        except: pass


class XMLNamespacePtr(XMLNamespace):
    def __init__(self, this):
        _swig_setattr(self, XMLNamespace, 'this', this)
        if not hasattr(self,"thisown"): _swig_setattr(self, XMLNamespace, 'thisown', 0)
        _swig_setattr(self, XMLNamespace,self.__class__,XMLNamespace)
_libsbml.XMLNamespace_swigregister(XMLNamespacePtr)

def XMLNamespace_startsWithXMLNS(*args):
    """
    XMLNamespace_startsWithXMLNS(string prefix) -> bool

    Returns true if prefix begins with 'xmlns:' (case-insensitive), false
    otherwise.


    """
    return _libsbml.XMLNamespace_startsWithXMLNS(*args)

class XMLNamespaceList(_object):
    """Proxy of C++ XMLNamespaceList class"""
    __swig_setmethods__ = {}
    __setattr__ = lambda self, name, value: _swig_setattr(self, XMLNamespaceList, name, value)
    __swig_getmethods__ = {}
    __getattr__ = lambda self, name: _swig_getattr(self, XMLNamespaceList, name)
    def __repr__(self):
        return "<%s.%s; proxy of C++ XMLNamespaceList instance at %s>" % (self.__class__.__module__, self.__class__.__name__, self.this,)
    def add(*args): 
        """
        add(self, XMLNamespace ns)
        add(self, string prefix, string URI)

        Adds (prefix, URI) to this list of XML namespaces.

        If prefix starts with 'xmlns:' (case-insensitive), it will be removed.


        """
        return _libsbml.XMLNamespaceList_add(*args)

    def getLength(*args): 
        """
        getLength(self) -> unsigned int

        Returns the number of XML namespaces in this list.


        """
        return _libsbml.XMLNamespaceList_getLength(*args)

    def getNamespace(*args): 
        """
        getNamespace(self, unsigned int n) -> XMLNamespace

        Returns the nth XMLNamespace in this list.


        """
        return _libsbml.XMLNamespaceList_getNamespace(*args)

    def getPrefix(*args): 
        """
        getPrefix(self, unsigned int n) -> string
        getPrefix(self, string URI) -> string

        Returns the prefix of the XML namespace with the given URI.  If URI is
        not in this list of namespaces, an empty string is returned.


        """
        return _libsbml.XMLNamespaceList_getPrefix(*args)

    def getURI(*args): 
        """
        getURI(self, unsigned int n) -> string
        getURI(self, string prefix) -> string

        Returns the URI of the XML namespace with the given prefix.  If prefix
        was not found, an empty string is returned.


        """
        return _libsbml.XMLNamespaceList_getURI(*args)

    def __init__(self, *args):
        """__init__(self) -> XMLNamespaceList"""
        _swig_setattr(self, XMLNamespaceList, 'this', _libsbml.new_XMLNamespaceList(*args))
        _swig_setattr(self, XMLNamespaceList, 'thisown', 1)
    def __del__(self, destroy=_libsbml.delete_XMLNamespaceList):
        """__del__(self)"""
        try:
            if self.thisown: destroy(self)
        except: pass


class XMLNamespaceListPtr(XMLNamespaceList):
    def __init__(self, this):
        _swig_setattr(self, XMLNamespaceList, 'this', this)
        if not hasattr(self,"thisown"): _swig_setattr(self, XMLNamespaceList, 'thisown', 0)
        _swig_setattr(self, XMLNamespaceList,self.__class__,XMLNamespaceList)
_libsbml.XMLNamespaceList_swigregister(XMLNamespaceListPtr)

XML_SCHEMA_VALIDATION_NONE = _libsbml.XML_SCHEMA_VALIDATION_NONE
XML_SCHEMA_VALIDATION_BASIC = _libsbml.XML_SCHEMA_VALIDATION_BASIC
XML_SCHEMA_VALIDATION_FULL = _libsbml.XML_SCHEMA_VALIDATION_FULL

def formulaToString(*args):
    """formulaToString(ASTNode_t tree) -> char"""
    return _libsbml.formulaToString(*args)

