# ecb-wd-stanford-coref
Extract Within-Document(WD) Coref from ECB+ corpus using Stanford coref


Pre-Requirements
--
- Java 1.8
- Gradle
- ECB+ corpus root folder named 'ECB+' (<a href="http://www.newsreader-project.eu/results/data/the-ecb-corpus/">Download ECB+</a>) should be put under `src/main/resources`

Build And Run
--

* From command line navigate to project root directory and run:
    
    
    ./gradlew clean build
    
*Should get a message saying: `BUILD SUCCESSFUL in 7s`*
* Extract the build zip file created at this location `build/distributions/ecb-wd-stanford-coref-1.0-SNAPSHOT.zip`
* Run the unzipped .jar file using command:

    
    java -jar build/distributions/ecb-wd-stanford-coref-1.0-SNAPSHOT/ecb-wd-stanford-coref-1.0-SNAPSHOT.jar
