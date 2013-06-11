@echo off

set binary=bin
set source=src/main/java/lwrt/*.java src/main/java/ui/*.java src/main/java/util/*.java src/main/java/vdm/*.java
set classes=lwrt/*.class ui/*.class util/*.class vdm/*.class
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
rmdir /S /Q %binary%
