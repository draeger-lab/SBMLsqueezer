
                            l i b S B M L

                           Example Programs

                            Ben Bornstein
	   with contributions from (in alphabetical order)
	    Christoph Flamm, Ralph Gauges, Michael Hucka,
		 Rainer Machne and Nicolas Rodriguez

			    The SBML Team
			 http://www.sbml.org/
		     mailto:sbml-team@caltech.edu

       Please join the libsbml-discuss mailing list by visiting
	    http://www.sbml.org/forums/index.php?t=pre_reg

    Date of last update to this file: $Date: 2005/09/30 21:05:52 $


There are separate subdirectories for different programming languages:
"c" for example programs in the C language, "c++" for examples in C++,
"java" for examples in Java, etc.  You will first need to change your
working directory to one of these subdirectories.

The Makefile in each subdirectory is kept simple for illustrative
purposes.  For this reason, you may need to modify some of the values
assigned to variables in the Makefiles in order that they correspond
to your particular environment.

Once this is done, at the Unix command prompt, you should be able to
type the following command to compile the example programs in a given
language subdirectory:

  % make

Next, you may wish to try running the example programs on some SBML
files.  There are a number of sample models used for testing the rest
of libSBML in src/sbml/test/test-data.  So for example, you could do
try the following:

  % printSBML     ../../src/sbml/test/test-data/l2v1-branch.xml
  % readSBML      ../../src/sbml/test/test-data/l2v1-delay.xml
  % convertSBML   ../../src/sbml/test/test-data/l1v1-rules.xml l2v1-rules.xml
  % validateSBML  ../../src/sbml/test/test-data/l2v1-branch.xml
  % validateSBML  ../../src/sbml/test/test-data/l1v1-branch.xml
  % validateSBML  ../../src/sbml/test/test-data/l1v1-branch-schema-error.xml

More models may be obtained from a number of other sources, including
the BioModels Database (http://www.ebi.ac.uk/biomodels/).









-------------------------------------------
File authors: B. Bornstein, M. Hucka
Last Modified: $Date: 2005/09/30 21:05:52 $
Last Modified By: $Author: mhucka $
-------------------------------------------

# The following is for [X]Emacs users.  Please leave in place.
# Local Variables:
# fill-column: 70
# End:
