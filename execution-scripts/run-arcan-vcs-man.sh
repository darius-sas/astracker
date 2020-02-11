#!/bin/bash

# Instructions:
# change checkout command (line #32), only part before `|| {...}`
# Ensure the -Xmx5G (line #9) option sets the memory to less than the 
# actual physical memory available to the machine.

arcanc(){
    java -jar -Xmx5G ./Arcan-c-1.0.2-RELEASE-jar-with-dependencies.jar $@
}

project=$1
outputDir=$2
commitsFile=$3

if [[ -z $project || -z $outputDir || -z $commitsFile ]] ; then
    echo "Usage run-arcan-vcs-man.sh <project-source-dir> <output-dir> <commits-file>"
    exit 1
fi
echo $project

mkdir -p $outputDir

i=0
wd=$(pwd)

while IFS= read -r commit
do
    echo "Analysing $commit"
    cd $project
    # CHECKOUT COMMAND
    git checkout $commit || { echo "Failed checking out commit. Skipping..."; exit 1; }
    cd $wd

    arcanc -p $project -out $outputDir -versionId $i-$commit &> $outputDir/log-$i-$commit.txt || { echo "Failed arcan analysis, skipping... "; }
    
    ((i = i + 1))

done < "$commitsFile"