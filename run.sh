#!/bin/bash

# clean before compiling and running
#find bin/ -name "*.class" -type f -delete

# compile
javac -cp lib/json-20230227.jar:. -d bin src/main/*.java

# run
cd bin
java -cp lib/json-20230227.jar:. App
cd ..

# clean after compiling and running
find bin/ -name "*.class" -type f -delete