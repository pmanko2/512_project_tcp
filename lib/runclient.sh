#!/bin/bash

echo 'Establishing classpath...'
export CLASSPATH=~/cs512_tcp/client:~/cs512_tcp/java-json.jar

echo 'Compiling...'
javac ~/cs512_tcp/client/Client.java

echo 'Running client...'
java Client teaching
