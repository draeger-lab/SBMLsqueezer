#!/bin/bash

# Copyright © 2009 Andreas Dräger
# Center for Bioinformatics Tübingen (ZBIT).
# Thanks to Markus List.
#
# This program comes with ABSOLUTELY NO WARRANTY.
# This is free software, and you are welcome
# to redistribute it under certain conditions;
# see http://www.gnu.org/copyleft/gpl.html for details.
#
# 
# This script loads the necessary paths and starts
# SBMLsqueezer in stand-alone mode. You will probably
# have to modify this script for your specific system
# configuration.


# Don't change this part:
VM_ARGS="-Xms32M -Xmx512M -Djava.library.path="

##############################################################
# The following lines depend on your system's configuration:
VM_ARGS="${VM_ARGS}\
/usr/lib/jvm/java-6-sun/jre/lib/i386/client:\
/usr/lib/jvm/java-6-sun/jre/lib/i386:\
/usr/lib/jvm/java-6-sun/lib:\
/usr/local/lib"
#:[path to xerces]/xerces/lib"
CLASS_PATH="/usr/local/share/java/libsbmlj.jar:\
/opt/CellDesigner4.1/plugin/SBMLsqueezer1.3.jar"
LD_LIBRARY_PATH="/usr/local/lib"
##############################################################

# Start SBMLsqueezer using the command-line options passed
# to this script (gathered in ${@}). You may, for instance,
# want to start the graphical user interface by invoking this 
# script with the following command:
# ./run.sh --gui

MAIN_CLASS=org.sbml.squeezer.SBMLsqueezer 
java ${VM_ARGS} -cp ${CLASS_PATH} ${MAIN_CLASS} ${@}

# After finishing SBMLsqueezer, tell the operating system that
# no error occurred.

exit 0
