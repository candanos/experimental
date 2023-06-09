#!/usr/bin/bash

if [ -z "$MSYS2_HOME" ]
then
   MSYS2_HOME="/c/msys64"
fi

BASH_PATH=$MSYS2_HOME"/usr/bin"
GNUCOBOL_PATH=$MSYS2_HOME"/mingw64/bin"
export MINGW_PREFIX=$MSYS2_HOME"/mingw64"
export PATH=$GNUCOBOL_PATH:$BASH_PATH:$PATH
#. cobenv.sh --setenv
. cobenv.sh --setenv 1> /dev/null
#. cobenv.sh --showenv

PROJECT_ROOT=$1 #"/c/cloud/github/experimental-repository'
COBOL_FILE_GROUPS=$2 #{"subs":["SUBPGM01.cbl","SUBPGM02.cbl"],"drivers/gunsonu":["EXPBATCH.cbl"]}'
OPERATION=$3 #compile or link

COMPILER_ARGS="-std=ibm"
TARGETDIR="$PROJECT_ROOT/target/objects"
SOURCEDIR="$PROJECT_ROOT/src/main/cobol"
SYSLIB="$PROJECT_ROOT/target/cobc-syslib"

#copy projects own copybooks and dcls into syslib
find $SOURCEDIR -type f \( -name "*.cbl" -or -name "*.cpy" \) -exec cp --preserve {} "$SYSLIB" \;
json_string=$COBOL_FILE_GROUPS

# Parse JSON string and iterate over each key-value pair
while read key value; do
  # Print the key
#  key=$(echo "$key" | sed 's/\\/\\\\/g')
#  value=$(echo "$value" | sed 's/\\/\\\\/g')
  cd $TARGETDIR
  PACKAGE=$key
  mkdir -p $PACKAGE; cd $PACKAGE

  # Concatenate all items in the array with a space between them,
  # and as converted to fullpath.
  COBOLFILES=$(echo "$value" | jq -r --arg var "$SOURCEDIR/$PACKAGE/" '.[] | "\($var)\(.)"' | sed 's/\\/\\\\/g' | tr '\n' ' ')
  # Trim leading and trailing spaces.
  COBOLFILES=${COBOLFILES#"${COBOLFILES%%[![:space:]]*}"}
  COBOLFILES=${COBOLFILES%"${COBOLFILES##*[![:space:]]}"}

  cobc $PARMS -c $COBOLFILES -I"$SYSLIB"
done < <(echo "$json_string" | jq -r 'to_entries | .[] | "\(.key) \(.value)"')


# that worked
# compilation


# link
# cobc -c -x "MAINPGM.cbl" && cobc -x -o MAIN MAINPGM.o SUBPGM01.o SUBPGM02.o