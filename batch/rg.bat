@ECHO off

>> "%temp%\Launch_options.reg" ECHO Windows Registry Editor Version 5.00
>> "%temp%\Launch_options.reg" ECHO.
>> "%temp%\Launch_options.reg" ECHO [%1]
>> "%temp%\Launch_options.reg" ECHO "%2"=dword:%3
regedit.exe /s "%temp%\Launch_options.reg"
del %temp%\Launch_options.reg