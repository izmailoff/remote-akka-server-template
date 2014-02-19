Build Status
====================
[![Build Status](https://travis-ci.org/izmailoff/remote-akka-server-template.png?branch=master)](https://travis-ci.org/izmailoff/remote-akka-server-template)

remote-akka-server-template
====================

Abstract
====================
Example of an Akka server with remoting that services clients and broadcasts messages to connected clients

How To Build
====================
SBT
-------------
Go to project directory and type 'sbt' or './sbt' (sbt executable is provided).
After this you can issue commands in SBT prompt:

    ; clean; compile; test; run

Alternatively you can type in shell:

    sbt update clean compile test run

The easiest way to run apps is to type 'run' in SBT and select app number from the list.

JAR
-------------
Generate a jar that contains all dependencies with SBT command:

    one-jar
    
This will generate both client and server jars.

How To Run
====================
Once you've built jars run them like a regular jar (default main class will be set for you):

    java -jar <jar-file.jar> <cmd args ...>


IDE support
====================
Eclipse
-------------
Eclipse projects can be generated with:

    sbt eclipse
    
Use 'Import New Projects' in eclipse to open them afterwards.

IntelliJ IDEA
-------------
You can generate projects by running:

    sbt gen-idea
    
Some available options are:

    no-classifiers
    no-sbt-classifiers
    
