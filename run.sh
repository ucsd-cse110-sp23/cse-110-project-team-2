#!/bin/bash

# Change to the project directory
cd /path/to/your/project

# Check if build has already been executed by looking for the build directory
if [ ! -d "build" ]; then
    echo "Building the project..."
    ./gradlew build
fi

# Run the application
echo "Running the application..."
./gradlew run