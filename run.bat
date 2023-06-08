@echo off

REM Check if build has already been executed by looking for the build directory
if not exist "build" (
    echo Building the project...
    gradlew build
)

REM Run the application
echo Running the application...
gradlew run