#!/bin/bash

echo 'Establishing classpath...'
export CLASSPATH=~/cs512_tcp/servercode/:~/cs512_tcp/java-json.jar

echo 'Compiling...'
javac ~/cs512_tcp/servercode/ResInterface/ResourceManager.java
javac -Xlint ~/cs512_tcp/servercode/ResImpl/ResourceManagerImpl.java

echo 'Generating jar file(s)...'
jar cvf ~/cs512_tcp/ResInterface.jar ~/cs512_tcp/servercode/ResInterface/*.class

echo 'Running Flight RM server...' 
java ResImpl.ResourceManagerImpl Flight
