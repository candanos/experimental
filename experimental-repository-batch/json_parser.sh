#!/usr/bin/bash

if [ -z "$MSYS2_HOME" ]
then
   MSYS2_HOME="/c/msys64"
fi

GNUCOBOL_PATH=$MSYS2_HOME"/mingw64/bin"
export MINGW_PREFIX=$MSYS2_HOME"/mingw64"
export PATH=$GNUCOBOL_PATH:$PATH

json_string='{"subs":["SUBPGM01.cbl","SUBPGM02.cbl"],"drivers/gunsonu":["EXPBATCH.cbl"]}'

# Use sed to replace backslashes with double backslashes
#json_string=$(echo "$json_string" | sed 's/\\/\\\\/g')

# Parse JSON string and iterate over each key-value pair
while read key value; do
  # Print the key
#  key=$(echo "$key" | sed 's/\\/\\\\/g')

  PACKAGE=$key

  # Concatenate all items in the array with a space between them
  # The -r option tells jq to output raw strings instead of JSON-encoded strings. This is useful when you want to extract specific values from the JSON data.
  # '.[]' is a filter expression used by jq to iterate over all elements in an array. It selects each element individually and outputs it. It effectively "flattens" the array, separating each element onto its own line.
  # the | after jq -r '.[]' passes each element to the right side." 
  
  COBOLFILES=$(echo "$value" | jq -r --arg var "$PACKAGE/" '.[] | "\($var)\(.)"' | sed 's/\\/\\\\/g' | tr '\n' ' ')
  # Trim leading and trailing spaces.
  COBOLFILES=${COBOLFILES#"${COBOLFILES%%[![:space:]]*}"}
  COBOLFILES=${COBOLFILES%"${COBOLFILES##*[![:space:]]}"}
  echo $COBOLFILES
done < <(echo "$json_string" | jq -r 'to_entries | .[] | "\(.key) \(.value)"')


# that worked
# compilation


# link
# cobc -c -x "MAINPGM.cbl" && cobc -x -o MAIN MAINPGM.o SUBPGM01.o SUBPGM02.o