#!/bin/bash

if [ "$1" == "root" ]
then

	if [ "($whoami)" = "root" ]
	then
		echo "Warning: It is not a good idea to run this script as root"
		echo "		Add 'root' as a launch option to bypass"
	
	fi
fi

jaroutput=lawena.jar

if [ -x $jaroutput ] 
then
	rm -f $jaroutput
fi
	
binary=bin
declare -a src=(src/main/java/config/*.java src/main/java/ui/*.java)
declare -a classes=(config/*.class ui/*.class)
classpath=.
mainclass=ui.LwrtGUI
manifest=manifest.txt
exe=Lawena.sh

mkdir $binary
javac -classpath $classpath -d $binary ${src[@]}
cd $binary
echo Main-Class: $mainclass > $manifest
jar -cfm ../$jaroutput $manifest ${classes[@]}
cd ..
rm -r -f $binary
chmod -x $jaroutput

if [ -x $exe ]
then
	rm -f $exe
fi

echo "jaroutput=\"lawena.jar\"
if [ -x $jaroutput ]
then
	jar -jar $jaroutput
else
	echo \"Please run build.sh\"
fi
" > $exe

chmod -x $exe
