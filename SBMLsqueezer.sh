#!/bin/bash
###############################################################################
#                                                                             #
# SBMLsqueezer.sh - SBMLsqueezer version 1.3 for Linux                        #
#                                                                             #
# Copyright (c) 2009 Center for Bioinformatics Tübingen. All rights reserved. #
#                                                                             #
###############################################################################

DIR=$(pwd)
cd ${0%/*}
ABS_PATH=$(pwd)
cd ${DIR}

export LD_LIBRARY_PATH="\
${LD_LIBRARY_PATH}:\
${ABS_PATH}/lib/linux/libSBML/lib:\
${ABS_PATH}/lib/linux/xerces/lib"

CLASSPATH="\
${ABS_PATH}/lib/linux/libSBML/share/java/libsbmlj.jar:\
${ABS_PATH}/dist/SBMLsqueezer1.3.jar"

MAIN_CLASS="org.sbml.squeezer.SBMLsqueezer"
VM_ARGS="-Xms32M -Xmx512M"

java ${VM_ARGS} -cp ${CLASSPATH} ${MAIN_CLASS} $@

exit 0
