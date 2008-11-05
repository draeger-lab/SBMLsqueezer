#!/bin/bash

echo "checking out all java files to be compiled"
find ./src/ -name '*.java' > classes
rm -rf ./bin/*
echo "compiling"
/usr/java/jdk1.5.0_11/bin/javac @options @classes
echo "generating javadoc"
rm -rf ./doc/*
javadoc -private -author -quiet -version -d doc @classes
rm classes

