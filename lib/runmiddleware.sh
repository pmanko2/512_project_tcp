#!/bin/bash

echo 'Establishing classpath...'
export CLASSPATH=~/cs512_tcp/middleware:~/cs512_tcp/java-json.jar

echo 'Compiling...'
javac ~/cs512_tcp/middleware/ClientHandler.java
javac ~/cs512_tcp/middleware/MiddlewareServer.java

echo 'Running Middleware server...'
java MiddlewareServer