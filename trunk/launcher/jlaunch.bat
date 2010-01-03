@ECHO OFF

REM JLibs: Common Utilities for Java
REM Copyright (C) 2009  Santhosh Kumar T
REM
REM This library is free software; you can redistribute it and/or
REM modify it under the terms of the GNU Lesser General Public
REM License as published by the Free Software Foundation; either
REM version 2.1 of the License, or (at your option) any later version.
REM
REM This library is distributed in the hope that it will be useful,
REM but WITHOUT ANY WARRANTY; without even the implied warranty of
REM MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
REM Lesser General Public License for more details.

SET CMD=java.exe
IF DEFINED JAVA_HOME SET CMD="%JAVA_HOME%\bin\%CMD%"
IF DEFINED JAVA_CMD SET CMD="%JAVA_CMD%"
SET JAVA_CMD=

SET SECTION=
SET RESULT=
SET FILE=%1
SHIFT

FOR /F "usebackq delims=" %%i IN ("%FILE%") DO CALL :processline "%%i"
REM append result to command
if DEFINED RESULT CALL :processresult

:loop.start
IF "%~1"=="" GOTO :loop.end
SET CMD=%CMD% "%~1"
SHIFT
GOTO :loop.start
:loop.end

%CMD%
GOTO :end

:processline
IF %1 == "<java.classpath>" GOTO option
IF %1 == "<java.endorsed.dirs>" GOTO option
IF %1 == "<java.ext.dirs>" GOTO option
IF %1 == "<java.library.path>" GOTO option
IF %1 == "<java.system.props>" GOTO option
IF %1 == "<java.bootclasspath>" GOTO option
IF %1 == "<java.bootclasspath.append>" GOTO option
IF %1 == "<java.bootclasspath.prepend>" GOTO option
IF %1 == "<jvm.args>" GOTO option

REM ignore if line is comment
SET LINE=%1
SET FIRST_CHAR=%LINE:~1,1%
IF "%FIRST_CHAR%" == "#" GOTO end

REM join the line to result
IF DEFINED RESULT SET RESULT=%RESULT%%SEPARATOR%
SET RESULT=%RESULT%"%PREFIX%%~1"
GOTO end

:option

REM append result to command
IF DEFINED RESULT CALL :processresult

SET OPTION=%1
SET SECTION=%OPTION:~2,-2%
GOTO %SECTION%

:java.classpath
SET RESULT=
SET SECTION_PREFIX=-classpath 
SET PREFIX=
SET SEPARATOR=;
GOTO end

:java.endorsed.dirs
SET RESULT=
SET SECTION_PREFIX=-Djava.endorsed.dirs=
SET PREFIX=
SET SEPARATOR=;
GOTO end

:java.ext.dirs
SET RESULT=
SET SECTION_PREFIX=-Djava.ext.dirs=
SET PREFIX=
SET SEPARATOR=;
GOTO end

:java.library.path
SET RESULT=
SET SECTION_PREFIX=-Djava.library.path=
SET PREFIX=
SET SEPARATOR=;
GOTO end

:java.system.props
SET RESULT=
SET SECTION_PREFIX=
SET PREFIX=-D
SET SEPARATOR= 
GOTO end

:java.bootclasspath
SET RESULT=
SET SECTION_PREFIX=-Xbootclasspath:
SET PREFIX=
SET SEPARATOR=;
GOTO end

:java.bootclasspath.prepend
SET RESULT=
SET SECTION_PREFIX=-Xbootclasspath/p:
SET PREFIX=
SET SEPARATOR=;
GOTO end

:java.bootclasspath.append
SET RESULT=
SET SECTION_PREFIX=-Xbootclasspath/a:
SET PREFIX=
SET SEPARATOR=;
GOTO end

:jvm.args
SET CMD=%CMD% "-DSCRIPT_FILE=%APP%.bat"
SET RESULT=
SET SECTION_PREFIX=
SET PREFIX=
SET SEPARATOR= 
GOTO end

:processresult
IF "%SECTION%" == "java.ext.dirs" SET RESULT="%JAVA_HOME%\lib\ext";"%JAVA_HOME%\jre\lib\ext";%RESULT%
SET CMD=%CMD% %SECTION_PREFIX%%RESULT%

:end
