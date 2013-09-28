#!/bin/bash

echo 'Establishing classpath...'
export CLASSPATH=~/cs512_tcp/client:~/cs512_tcp/java-json.jar

echo 'Compiling...'
javac -Xlint Client.java

echo 'Running client...'
java Client teaching
