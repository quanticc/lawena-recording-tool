#!/bin/bash

jaroutput=lawena.jar

if [ -f $jaroutput ] 
then
	rm -f $jaroutput
fi
	
binary=bin
declare -a src=(src/main/java/lwrt/*.java src/main/java/ui/*.java src/main/java/util/*.java src/main/java/vdm/*.java)
declare -a classes=(lwrt/*.* ui/*.* util/*.* vdm/*.*)
resources=src/main/resources/*
classpath=.
mainclass=ui.LwrtGUI
manifest=manifest.txt
exe=lawena.sh

mkdir $binary
javac -classpath $classpath -d $binary ${src[@]}
cp -vR $resources $binary
cd $binary
echo Main-Class: $mainclass > $manifest
jar -cfm ../$jaroutput $manifest ${classes[@]}
cd ..
rm -rf $binary
chmod +x $jaroutput

if [ -f $exe ]
then
	rm -f $exe
fi

echo "if [ -f $jaroutput ]
then
	java -jar $jaroutput
else
	echo \"Please run build.sh\"
fi
" > $exe

chmod +x $exe