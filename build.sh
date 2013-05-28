#!/bin/bash

binary=bin
declare -a src=(src/main/java/config/*.java src/main/java/ui/*.java)
declare -a classes=(config/*.class ui/*.class)
classpath=.
mainclass=ui.LwrtGUI
manifest=manifest.txt
jaroutput=lawena.jar

mkdir $binary
javac -classpath $classpath -d $binary ${src[@]}
cd $binary
echo Main-Class: $mainclass > $manifest
jar -cfm ../$jaroutput $manifest ${classes[@]}
cd ..
rm -r -f $binary

