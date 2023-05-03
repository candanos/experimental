#!/usr/bin/bash

if [ -z "$MSYS2_HOME" ]
then
   MSYS2_HOME="/c/msys64"
fi

GNUCOBOL_PATH=$MSYS2_HOME"/mingw64/bin"
echo $GNUCOBOL_PATH
export MINGW_PREFIX=$MSYS2_HOME"/mingw64"

TARGETDIR=$1
COMPILER_ARGS=$2
COBOLFILES=$3
STEPS=$4

export PATH=$GNUCOBOL_PATH:$PATH
. cobenv.sh --setenv
#. cobenv.sh --setenv 1> /dev/null

#WORKINGDIR="/c/cloud/github/experimental-repository/src/main/cobol"
#COBOLFILES="SUBPGM01.cbl SUBPGM02.cbl"
#PARMS="-std=ibm -fixed"
#COBC_PATH="/c/msys64/usr/bin"


cd $TARGETDIR
. cobenv.sh --showenv

#. cobenv.sh --showenv
#. cobenv.sh
#. cobenv.sh --showenv
# that worked
# cobc $PARMS -x -o MAINPGM MAINPGM.cbl SUBPGM01.cbl SUBPGM02.cbl
# ./MAINPGM

# that worked 
# compilation
cobc $PARMS -c $COBOLFILES
# cobc -c -x "MAINPGM.cbl"
# link
#cobc -x -o MAIN MAINPGM.o SUBPGM01.o SUBPGM02.o