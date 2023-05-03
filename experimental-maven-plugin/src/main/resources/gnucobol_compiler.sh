#!/usr/bin/bash

if [ -z "$MSYS2_HOME" ]
then
   MSYS2_HOME="/c/msys64"
fi

GNUCOBOL_PATH=$MSYS2_HOME"/mingw64/bin"
export MINGW_PREFIX=$MSYS2_HOME"/mingw64"
export PATH=$GNUCOBOL_PATH:$PATH
#. cobenv.sh --setenv
. cobenv.sh --setenv 1> /dev/null
#. cobenv.sh --showenv

TARGETDIR=$1 #TARGETDIR="/c/cloud/github/experimental-repository/src/main/cobol"
COMPILER_ARGS=$2 #PARMS="-std=ibm -fixed"
COBOLFILES=$3 #COBOLFILES="SUBPGM01.cbl SUBPGM02.cbl"
STEPS=$4 #compile or link

cd $TARGETDIR

# that worked
# following line is single output file for all source files.
# cobc $PARMS -x -o MAINPGM MAINPGM.cbl SUBPGM01.cbl SUBPGM02.cbl
# ./MAINPGM

# that worked 
# compilation
cobc $PARMS -c $COBOLFILES

# link
# cobc -c -x "MAINPGM.cbl" && cobc -x -o MAIN MAINPGM.o SUBPGM01.o SUBPGM02.o