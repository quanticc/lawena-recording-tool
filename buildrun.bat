@echo off

set binary=bin
set source=src/config/*.java src/ui/*.java
set classes=config/*.class ui/*.class
set classpath=.
set mainclass=ui.LwrtGUI
set manifest=manifest.txt
set jaroutput=lawena.jar

mkdir %binary%
javac -cp %classpath% -d %binary% %source%
cd %binary%
echo Main-Class: %mainclass% > %manifest%
jar -cfm ../%jaroutput% %manifest% %classes%
del %manifest%
cd ..
java -jar %jaroutput%
