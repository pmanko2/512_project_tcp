#!/bin/bash

echo 'Establishing classpath...'
export CLASSPATH=~/comp512_tcp/middleware:~/comp512_tcp/java-json.jar

echo 'Compiling...'
javac ~/comp512_tcp/middleware/MiddlewareServer.java

echo 'Running Middleware server...'
java MiddlewareServer