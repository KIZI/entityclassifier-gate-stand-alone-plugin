#!/bin/bash

# This script was created by Milan Dojchinovski <http://dojchinovski.mk>
# Contact me by an email <dojcinovski.milan@gmail.com> or on Twitter at @mici
#
# Feel free to use and adapt it to your needs!

echo "== Linked Hypernyms Dataset version 2.3.9 =="
echo "The script is going to start downloading the latest partitions of the LHD v2.0 dataset"
echo "Downloading started ..."

############## Variables ################

##### DBpedia related variables ######
dbpediaDownloadServer="http://downloads.dbpedia.org/3.9/"

# DBpedia Ontology v3.9 - http://wiki.dbpedia.org/Downloads39#dbpedia-ontology
dbpediaOntology="dbpedia_3.9.owl.bz2"

##### Linked Hypernyms Dataset related variables #####
lhdDownloadServer="http://boa.lmcloud.vse.cz/LHD/"
bitbucketDownloadServer="https://bitbucket.org/entityclassifier/entityclassifier-gate-stand-alone-plugin/raw/014957fa3e44a81ebd58e801b13e9af204a01cde/datasets/"

enlhdCore="en.LHDv1.draft.nt.gz"
nllhdCore="nl.LHDv1.draft.nt.gz"
delhdCore="de.LHDv1.draft.nt.gz"

eninferred="en.inferredmappingstoDBpedia.nt"
deinferred="de.inferredmappingstoDBpedia.nt"
nlinferred="nl.inferredmappingstoDBpedia.nt"

#classifierJAR="nl.inferredmappingstoDBpedia.nt"

#####################################################

cd ..

# Creating download directory "resources".
if [ ! -d "resources" ]; then
  # Check if the partitions directory exist.
  mkdir "resources"
fi

cd resources

# Creating download directory "dbpedia-3.9".
if [ ! -d "dbpedia-3.9" ]; then
  # Check if the partitions directory exist.
  mkdir "dbpedia-3.9"
fi

cd dbpedia-3.9

################ Downloading DBpedia 3.9 files ################

#### DBpedia Ontology v3.9 ####
if [ ! -f "${dbpediaOntology:0:15}" ]; then
  echo "Started downloading DBpedia Ontology v3.9 ..."
  curl -# -O "$dbpediaDownloadServer$dbpediaOntology"
  echo "Finished downloading."
fi

if [ ! -f "${dbpediaOntology:0:15}" ]; then
  echo "Started decompressing DBpedia Ontology v3.9 file ..."
  bzip2 -d $dbpediaOntology
  echo "Finished decompressing."
fi

rm $dbpediaOntology

######################################

cd ..

# Creating download directory "lhd-2.3.9".
if [ ! -d "lhd-2.3.9" ]; then
  # Check if the partitions directory exist.
  mkdir "lhd-2.3.9"
fi


cd lhd-2.3.9


################ Downloading Linked Hypernyms Dataset ################

### Downloading and decompressing English LHD Core partitions ###
if [ ! -f "${enlhdCore:0:17}" ]; then
  echo "Started downloading English LHD Core files ..."
  curl -# -O "$lhdDownloadServer$enlhdCore"
  echo "Finished downloading."
fi

if [ ! -f "${enlhdCore:0:17}" ]; then
  echo "Started decompressing English LHD Core files ..."
  gunzip $enlhdCore
  echo "Finished decompressing."
fi
##############################################################

### Downloading and decompressing Dutch LHD Core partitions ###
if [ ! -f "${nllhdCore:0:17}" ]; then
  echo "Started downloading Dutch LHD Core files ..."
  curl -# -O "$lhdDownloadServer$nllhdCore"
  echo "Finished downloading."
fi

if [ ! -f "${nllhdCore:0:17}" ]; then
  echo "Started decompressing Dutch LHD Core files ..."
  gunzip $nllhdCore
  echo "Finished decompressing."
fi
##############################################################

### Downloading and decompressing German LHD Core partitions ###
if [ ! -f "${delhdCore:0:17}" ]; then
  echo "Started downloading German LHD Core files ..."
  curl -# -O "$lhdDownloadServer$delhdCore"
  echo "Finished downloading."
fi

if [ ! -f "${delhdCore:0:17}" ]; then
  echo "Started decompressing German LHD Core files ..."
  gunzip $delhdCore
  echo "Finished decompressing."
fi
##############################################################


### Downloading English LHD v2.0 inferred types partition ###
if [ ! -f "${eninferred}" ]; then
  echo "Started downloading the English LHD v2.0 inferred types partition ..."
  curl -# -O "$bitbucketDownloadServer$eninferred"
  echo "Finished downloading."
fi
##############################################################

### Downloading German LHD v2.0 inferred types partition ###
if [ ! -f "${deinferred}" ]; then
  echo "Started downloading the German LHD v2.0 inferred types partition ..."
  curl -# -O "$bitbucketDownloadServer$deinferred"
  echo "Finished downloading."
fi
##############################################################

### Downloading English LHD v2.0 inferred types partition ###
if [ ! -f "${nlinferred}" ]; then
  echo "Started downloading the Dutch LHD v2.0 inferred types partition ..."
  curl -# -O "$bitbucketDownloadServer$nlinferred"
  echo "Finished downloading."
fi
##############################################################

cd ..
cd ..

echo "Compiling the plugin ..."
mvn license:update-file-header
mvn clean package

echo "Copying compiled jar ..."
cp target/Entityclassifier.eu_NER-1.0-jar-with-dependencies.jar Entityclassifier.eu_NER-1.0.jar 

#echo "Creating creole.xml document."
#printf '%s\n' '<?xml version="1.0"?>' >> creole.xml
#printf '%s\n' '<CREOLE-DIRECTORY ID="org.vse.fis.keg.Entityclassifier NER Stand Alone" VERSION="1.0">' >> creole.xml
#printf '%s\n' '    <JAR scan="true">Entityclassifier.eu_NER-1.0.jar</JAR>' >> creole.xml
#printf '%s\n' '</CREOLE-DIRECTORY>' >> creole.xml

echo "Generating configuration file ..."
java -cp Entityclassifier.eu_NER-1.0.jar cz.ctu.fit.entityclassifier.gate.plugin.thd.standalone.Configurator

mvn clean

echo "Datasets download finished!"

