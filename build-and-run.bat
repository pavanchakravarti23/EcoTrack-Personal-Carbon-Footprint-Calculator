@echo off
setlocal
echo Compiling EcoTrack...
if not exist out\main mkdir out\main
dir /s /B src\main\java\*.java > sources.txt
javac --release 17 -encoding UTF-8 -d out\main @sources.txt
if errorlevel 1 (
    del sources.txt
    echo Compilation failed.
    exit /b 1
)
del sources.txt
echo Launching EcoTrack...
java -cp out\main com.ecotrack.main.EcoTrackApp
endlocal
