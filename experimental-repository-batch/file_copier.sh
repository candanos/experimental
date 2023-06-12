#!/usr/bin/bash
target='/C/Users/Candan Yuksel/Desktop/fromdir'
mkdir -p "$target"
source="/C/Cloud/github/experimental-repository-batch/src/main/cobol"

if [ -z "$MSYS2_HOME" ]
then
   MSYS2_HOME="/c/msys64"
fi

BASH_PATH=$MSYS2_HOME"/usr/bin"
GNUCOBOL_PATH=$MSYS2_HOME"/mingw64/bin"
export MINGW_PREFIX=$MSYS2_HOME"/mingw64"
export PATH=$GNUCOBOL_PATH:$BASH_PATH:$PATH
find $source -type f \( -name "*.cbl" -or -name "*.cpy" \) -exec cp --preserve {} "$target" \; 