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
mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file -DgroupId=de -DartifactId=zbit -Dversion=1.0 -Dfile=${LIB_DIR}/maven/SysBio.jar -DlocalRepositoryPath=${LIB_DIR}/maven -Dpackaging=jar -DgeneratePom=true -DcreateChecksum=true
mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file -DgroupId=org.sbml.tolatex -DartifactId=SBML2LaTeX -Dversion=1.0 -Dfile=${LIB_DIR}/maven/SBML2LaTeX_v1.0_slim.jar -DlocalRepositoryPath=${LIB_DIR}/maven -Dpackaging=jar -DgeneratePom=true -DcreateChecksum=true
mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file -DgroupId=jp.sbi.garuda -DartifactId=platform -Dversion=1.0 -Dfile=${LIB_DIR}/maven/garuda-csr.jar -DlocalRepositoryPath=${LIB_DIR}/maven -Dpackaging=jar -DgeneratePom=true -DcreateChecksum=true
mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file -DgroupId=jp.sbi.garuda.client -DartifactId=backend -Dversion=1.0 -Dfile=${LIB_DIR}/maven/GarudaBackend.jar -DlocalRepositoryPath=${LIB_DIR}/maven -Dpackaging=jar -DgeneratePom=true -DcreateChecksum=true
mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file -DgroupId=org.sbml.jsbml -DartifactId=jsbml -Dversion=1.4a1 -Dfile=${LIB_DIR}/maven/jsbml-1.4-a1-incl-libs.jar -DlocalRepositoryPath=${LIB_DIR}/maven -Dpackaging=jar -DgeneratePom=true -DcreateChecksum=true
mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file -DgroupId=org.sbml.jsbml.modules -DartifactId=jsbml-libSBMLio -Dversion=1.0 -Dfile=${LIB_DIR}/maven/jsbml-libSBMLio-1.0.jar -DlocalRepositoryPath=${LIB_DIR}/maven -Dpackaging=jar -DgeneratePom=true -DcreateChecksum=true
