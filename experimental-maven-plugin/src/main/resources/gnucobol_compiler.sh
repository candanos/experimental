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
COBOL_FILE_GROUPS=$3 #COBOLFILES="SUBPGM01.cbl SUBPGM02.cbl"
SYSLIB=$4
STEPS=$5 #compile or link

# following line is single output file for all source files.
# cobc $PARMS -x -o MAINPGM MAINPGM.cbl SUBPGM01.cbl SUBPGM02.cbl
# ./MAINPGM

json_string=$COBOL_FILE_GROUPS
# Use sed to replace backslashes with double backslashes
#json_string=$(echo "$json_string" | sed 's/\\/\\\\/g')

# Parse JSON string and iterate over each key-value pair
while read key value; do
  # Print the key
#  key=$(echo "$key" | sed 's/\\/\\\\/g')
#  value=$(echo "$value" | sed 's/\\/\\\\/g')
  cd $TARGETDIR
  PACKAGE=$key
  mkdir -p $PACKAGE; cd $PACKAGE

  # Concatenate all items in the array with a space between them
  COBOLFILES=$(echo "$value" | jq -r '.[]' | sed 's/\\/\\\\/g' | tr '\n' ' ')
  # Trim leading and trailing spaces.
  COBOLFILES=${COBOLFILES#"${COBOLFILES%%[![:space:]]*}"}
  COBOLFILES=${COBOLFILES%"${COBOLFILES##*[![:space:]]}"}
  cobc $PARMS -c $COBOLFILES -I"$SYSLIB"
done < <(echo "$json_string" | jq -r 'to_entries | .[] | "\(.key) \(.value)"')


# that worked
# compilation


# link
# cobc -c -x "MAINPGM.cbl" && cobc -x -o MAIN MAINPGM.o SUBPGM01.o SUBPGM02.o