@echo off

set binary=bin
set source=src/main/java/lwrt/*.java src/main/java/ui/*.java src/main/java/util/*.java src/main/java/vdm/*.java
set classes=lwrt/*.* ui/*.* util/*.* vdm/*.*
set resources=src\main\resources
set classpath=.
set mainclass=ui.LwrtGUI
set manifest=manifest.txt
set jaroutput=lawena.jar
set exe=lawena.bat

if exist %jaroutput% del %jaroutput%
mkdir %binary%
javac -cp %classpath% -d %binary% %source%
xcopy /s %resources% %binary%
cd %binary%
echo Main-Class: %mainclass% > %manifest%
jar -cfm ../%jaroutput% %manifest% %classes%
cd ..
rmdir /s /q %binary%

(
  echo @echo off
  echo if exist %jaroutput% java -jar %jaroutput% else echo Please run build.bat
) > %exe%
