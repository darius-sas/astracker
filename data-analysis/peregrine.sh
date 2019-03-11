#!/bin/bash
#SBATCH --job-name=track_as_qualitas_corpus
#SBATCH --mail-type=ALL
#SBATCH --time=2-01:00:00
#SBATCH --mail-user=d.d.sas@rug.nl
#SBATCH --output=job-%j.log
#SBATCH --partition=regular
#SBATCH --nodes=1
#SBATCH --ntasks=3
#SBATCH --cpus-per-task=8
#SBATCH --mem=64000

module restore trackas

projects_task1="freemind,jmeter,jung,freecol"
projects_task2="jstock,junit,lucene,weka"
projects_task3="ant,antlr,argouml,hibernate"
projects_task4="jgraph,azureus"

srun ./analyse-system.sh -m /data/p284098/qualitas-corpus/output/arcanOutput -o /data/p284098/qualitas-corpus/output -mP ${projects_task1} -pC -rT

srun ./analyse-system.sh -m /data/p284098/qualitas-corpus/output/arcanOutput -o /data/p284098/qualitas-corpus/output -mP ${projects_task2} -pC -rT

srun ./analyse-system.sh -m /data/p284098/qualitas-corpus/output/arcanOutput -o /data/p284098/qualitas-corpus/output -mP ${projects_task3} -pC -rT

#srun ./analyse-system.sh -m /data/p284098/qualitas-corpus/output/arcanOutput -o /data/p284098/qualitas-corpus/output -mP ${projects_task4} -pC -rT
