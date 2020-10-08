# ASTracker
ASTracker is a Java tool that parses Arcan's output and tracks the architectural smells detected in each versionString analysed by Arcan.

# Requirements
The requirements to install and execute the application are:

* JDK 11 (or newer)
* Maven 3 (3.6.0 is the one used, but other minor versions should work fine too)


# Installation
The installation process is simple, but it requires `git` and `mvn` (Maven) to be installed on your system.
```shell script
git clone https://github.com/darius-sas/astracker
cd astracker
mvn clean compile assembly:single -DskipTests=true
```
and a JAR file will be created in the `target` directory.

Alternatively, ASTracker can be executed as a web service:
```shell script
mvn spring-boot:run
```
and even built as a Docker image (ensure you have Docker installed) using:
```shell script
mvn clean compile package -DskipTests
sudo docker build --tag astracker-web:latest .
```
then run the Docker image using:
```shell script
sudo docker run -itp 8080:8080 astracker-web:latest
```
**Note** that, in order for the image to be able to complete the requests you need to first [download](https://drive.google.com/file/d/1u8vYwAE9rrDosyoM33Nvg5YJuXRD_cA_/view?usp=sharing) Arcan and unzip the contents under a directory called `arcan` in the same directory as the Dockerfile.

## Deploying Docker container 
If you have access privileges to `webhosting.rug.nl`, then run:
```shell script
ns=astracker; tag=latest; img=astracker-web
docker login registry.webhosting.rug.nl
docker tag ${img} registry.webhosting.rug.nl/${ns}/${img}:${tag}
docker push registry.webhosting.rug.nl/${ns}/${img}:${tag}
```

# Usage
ASTracker can be run as any standard executable `.jar` file:
```bash
java -jar astracker.jar -help
```
This command will provide further information on the available commands.

As an example, try running the following command on the folder `sample-data`, which contains the `.grampml`, describing the Architectural smells affecting multiple versions of Antlr:
```shell script
java -jar target/astracker-0.9.0-jar-with-dependencies.jar -i sample-data -p antlr -o sample-data -pC
```

Optionally, you can execute tests by running
```shell script
./test-data/git-projects/clone-repos.sh       # Clone test repositories on locally
mvn clean compile test
```
though ensure you have `test-data` in the root directory of the project and that you clone all the necessary git repositories (using the handy script).
Test reports on coverage are available in `target/site` and output logs are available in `target/surefire`.

## Input formats
ASTracker is able to parse the `.graphML` files produced by Arcan representing the system dependency graph and the detected smells.
In order to be able to detect the versionString of the system analysed,these files must conform to the following naming pattern:
```
<index>-<date>-<commitId>.graphml
```

### Running Arcan
ASTracker can also execute Arcan by itself and avoid you the trouble. The only requirement is to provide the input JAR files that conform to the same name convention mentioned for the `.graphml` files.
The following input folder structures are supported by ASTracker in order to correctly provide the JAR files to Arcan:
#### FOLDER-BASED PROJECTS
```
input-folder
|-- <project-name>-version1
|    |-- jar-name1.jar
|    `- jar-nameN.jar
|-- <project-name>-version2
|    |-- jar-name1.jar
|    `-- jar-nameN.jar
...
```
#### SINGLE JAR-BASED PROJECTS
```
input-folder
|-- jar-version1.jar
|-- jar-version2.jar
...
`-- jar-versionN.jar
```

## Output files
By default two graph files will be output by ASTracker:
```
condensed-graph-consecOnly.xml
track-graph-consecOnly.xml
```
These files contain the same information, but in different formats, each suitable for different types of analyses. 
The information described are *the architectural smells tracked* starting from the first version to the final one. Additional information, such as architectural smells characteristics, are stored as node properties.

In order to print the properties as a single CSV file, the `-pC` option can be added to the initial command line.
A file named `smell-characteristics-consecOnly.csv` will also appear in the output directory.

# ASTracker architecture
This section briefly explains the general architecture of the AStracker and the responsibility of every component.

![Components](docs/astracker-architecture.png)

Explanation:
* The *Tracker* module has the responsibility to track architectural smells throughout versions. This component has the following interfaces:
    * *ASTracker* which is responsible of the actual logic for performing the tracking;
    * *SimilarityLinker* which is responsible for the logic of establishing when two smells from two versions match (i.e. can be linked);
  
* The *Runners* module contains all the code to perform an analysis as a whole on a given `Project`, for example to track the smells on that project or to also run Arcan on it;
* The *Persistence* module is responsible to write the relevant data to disk in order to save the results of the tracking and collect the necessary data;
  This module is designed with a "subscriber" design: data is sent to a central persistence writer (master), and if there are any specific writer subscribed to write that type of data (object), than such object is delivered to all subscribers.
  This allows for a flexible and lightweight management of output writing of data;
* The *Data* module is the biggest module of ASTracker. All the data handled by ASTracker is modeled by this module:
    * The *Architectural smells* sub-module contains all the models of the smells supported by ASTracker for tracking.
    * The *Characteristics* sub-module contains the smells characteristics and component characteristics that are computed by ASTracker as a complementary task to the tracking of smells itself.
    * The *Project* sub-module contains key components that abstract the project under analysis as versions and allow a client to retrieve the source code of a class/package/file of the analysed project starting from its `Vertex` in the dependency graph.
      Different project types require different implementations of the `Project` and `Version` interfaces, though most of the functionality is common and dedicated abstract classes handled those aspect.
      These interfaces are mostly responsible of representing in-memory the project on the file system.

# Notes
If you are here because you are looking for the dataset (and scripts) of our paper at ICSME'19 `Investigating instability architectural smells evolution: an exploratory case study`, you can find it [here](https://github.com/darius-sas/data-analysis-scripts/blob/master/data/smells.csv). Scripts to read and plot the data are instead [here](https://github.com/darius-sas/data-analysis-scripts).
