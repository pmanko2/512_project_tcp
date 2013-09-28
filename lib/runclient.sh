#!/bin/bash

echo 'Establishing classpath...'
export CLASSPATH=~/comp512_tcp/client:~/comp512_tcp/java-json.jar

echo 'Compiling...'
javac -Xlint Client.java

echo 'Running client...'
java Client teaching
