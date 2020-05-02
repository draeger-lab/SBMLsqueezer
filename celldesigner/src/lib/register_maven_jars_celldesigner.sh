#!/bin/bash
#################################################
# Script for registering maven artifacts
#
# If additional jars have to be added register
# these in the same manner like below in the
# repository.
#################################################

# lib directory
LIB_DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

# register
mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file -DgroupId=jp.sbi -DartifactId=celldesigner -Dversion=4.4 -Dfile=maven/celldesigner-4.4-GSoC.jar -DlocalRepositoryPath=${LIB_DIR}/maven -Dpackaging=jar -DgeneratePom=true -DcreateChecksum=true
mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file -DgroupId=org.sbml.jsbml -DartifactId=celldesigner -Dversion=1.0 -Dfile=maven/jsbml-celldesigner-1.0.jar -DlocalRepositoryPath=${LIB_DIR}/maven -Dpackaging=jar -DgeneratePom=true -DcreateChecksum=true
mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file -DgroupId=org.sbml.squeezer -DartifactId=SBMLsqueezer -Dversion=1.4 -Dfile=maven/SBMLsqueezer-1.4-with-dependencies.jar -DlocalRepositoryPath=${LIB_DIR}/maven -Dpackaging=jar -DgeneratePom=true -DcreateChecksum=true
