#! /bin/sh

# JLibs: Common Utilities for Java
# Copyright (C) 2009  Santhosh Kumar T
# 
# This library is free software; you can redistribute it and/or
# modify it under the terms of the GNU Lesser General Public
# License as published by the Free Software Foundation; either
# version 2.1 of the License, or (at your option) any later version.
# 
# This library is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
# Lesser General Public License for more details.

join() {
    if [ -n "$line" ] # skip lines of zero length
    then
        if [ `echo $line | cut -c 1` = '#' ]; then
            return # ignore lines starting with '#'
        fi
        line=`echo $line | sed 's/\(%\)\([a-zA-Z0-9_]*\)\(\%\)/\$\2/g'`
        line=`eval echo "$line"` # evaluate environment variables used, if any
        if [ -n "$RESULT" ]; then
            RESULT=$RESULT$SEPARATOR
        fi
        RESULT=$RESULT$PREFIX$line
    fi
}

processresult() {
    if [ -n "$RESULT" ]; then
        if [ "$SECTION" = "<java.ext.dirs>" ]; then
            if [ -n "$JAVA_HOME" ]; then
                RESULT="$JAVA_HOME/lib/ext":"$JAVA_HOME/jre/lib/ext":$RESULT
            fi
        fi
        CMD=$CMD' '$SECTION_PREFIX$RESULT
    fi
    SECTION=$line
}

processline() {
    case "$line" in
        '<java.classpath>')
            processresult
            RESULT="";
            SECTION_PREFIX="-classpath "
            PREFIX=""
            SEPARATOR=":"
            ;;
        '<java.endorsed.dirs>')
            processresult
            RESULT="";
            SECTION_PREFIX="-Djava.endorsed.dirs="
            PREFIX=""
            SEPARATOR=":"
            ;;
        '<java.ext.dirs>')
            processresult
            RESULT="";
            SECTION_PREFIX="-Djava.ext.dirs="
            PREFIX=""
            SEPARATOR=":"
            ;;
        '<java.library.path>')
            processresult
            RESULT="";
            SECTION_PREFIX="-Djava.library.path="
            PREFIX=""
            SEPARATOR=":"
            ;;
        '<java.system.props>')
            processresult
            RESULT="";
            SECTION_PREFIX=""
            PREFIX="-D"
            SEPARATOR=" "
            ;;
        '<java.bootclasspath>')
            processresult
            RESULT="";
            SECTION_PREFIX="-Xbootclasspath:"
            PREFIX=""
            SEPARATOR=":"
            ;;
        '<java.bootclasspath.prepend>')
            processresult
            RESULT="";
            SECTION_PREFIX="-Xbootclasspath/p:"
            PREFIX=""
            SEPARATOR=":"
            ;;
        '<java.bootclasspath.append>')
            processresult
            RESULT="";
            SECTION_PREFIX="-Xbootclasspath/a:"
            PREFIX=""
            SEPARATOR=":"
            ;;
        '<jvm.args>')
            processresult
            CMD=$CMD' '-DSCRIPT_FILE=`basename $FILE`
            RESULT="";
            SECTION_PREFIX=""
            PREFIX=""
            SEPARATOR=" "
            ;;
        *)
            if [ -n "$SEPARATOR" ]; then
                join
            fi
    esac         
}

readFile() {
    SECTION=""
    RESULT=""
    line=""

    # if file doesn't end with empty line, append empty line to file
    if [ -n "`tail -1 $FILE`" ]; then 
        echo>>$FILE
    fi
    
    temp_dir=`date "+%d%m%y%H%M%S%N"`
    mkdir $temp_dir
    # in solaris bash shell, while loop spawns a new subshell
    # http://www.kilala.nl/Sysadmin/script-variablescope.php
    # so using temporary file read/write to gain access to
    # changes in variables from while loop
    while read line; do
        processline

        echo "$RESULT">$temp_dir/RESULT
        echo "$SEPARATOR">$temp_dir/SEPARATOR
        echo "$line">$temp_dir/line
        echo "$SECTION">$temp_dir/SECTION
        echo "$SECTION_PREFIX">$temp_dir/SECTION_PREFIX
        echo "$PREFIX">$temp_dir/PREFIX
        echo "$CMD">$temp_dir/CMD 
    done < $FILE 

    RESULT=`cat $temp_dir/RESULT`
    SEPARATOR=`cat $temp_dir/SEPARATOR`
    line=`cat $temp_dir/line`
    SECTION=`cat $temp_dir/SECTION`
    PREFIX=`cat $temp_dir/PREFIX`
    SECTION_PREFIX=`cat $temp_dir/SECTION_PREFIX`
    CMD=`cat $temp_dir/CMD`
    rm -rf $temp_dir

    processresult
}

if [ -z "$JAVA_CMD" ] ; then
    if [ -n "$JAVA_HOME"  ] ; then
        if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
            # IBM's JDK on AIX uses strange locations for the executables
            JAVA_CMD="$JAVA_HOME/jre/sh/java"
        else
            JAVA_CMD="$JAVA_HOME/bin/java"
        fi
    else
        JAVA_CMD=`which java 2> /dev/null`
        if [ -z "$JAVA_CMD" ] ; then
            JAVA_CMD=java
        fi
    fi
fi

if [ ! -x "$JAVA_CMD" ] ; then
  echo "Error: JAVA_HOME is not defined correctly."
  echo "  We cannot execute $JAVA_CMD"
  exit 1
fi

FILE=$1
cd `dirname $FILE`
if [ -r "$FILE" ]; then
    readFile
    shift;
    exec $JAVA_CMD -Dpid=$$ $CMD $*
else
    echo $FILE not found
fi
