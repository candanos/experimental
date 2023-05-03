#!usr/bin/bash

COBC_PATH=$1
WORKINGDIR=$2
COBOLFILES=$3
PARMS=$4
STEPS=$5

WORKINGDIR="/c/cloud/github/experimental-repository/src/main/cobol"
COBOLFILES="SUBPGM01.cbl SUBPGM02.cbl"
PARMS="-std=ibm -fixed"  
COBC_PATH="/c/msys64/usr/bin"

export PATH=$COBC_PATH:$PATH

cd $WORKINGDIR
. cobenv.sh --setenv

# that worked
# cobc $PARMS -x -o MAINPGM MAINPGM.cbl SUBPGM01.cbl SUBPGM02.cbl
# ./MAINPGM

# that worked 
# compilation
# cobc $PARMS -c SUBPGM01.cbl SUBPGM02.cbl
# cobc -c -x "MAINPGM.cbl"
# link
cobc -x -o MAIN MAINPGM.o SUBPGM01.o SUBPGM02.o
#run
./MAIN


echo "bye bye cobol_compiler.sh"
read -p "Press [Enter] key to go on."