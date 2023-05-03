#!/bin/bash

mvn archetype:generate -DgroupId=com.candanos -DartifactId=experimental-maven-plugin -Dversion=0.0.1-SNAPSHOT -DarchetypeGroupId=org.apache.maven.archetypes -DarchetypeArtifactId=maven-archetype-mojo

read -p "Press [Enter] key to go on."  