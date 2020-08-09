call setenv.bat

cd ..
call gradlew clean build --exclude-task test
IF ERRORLEVEL 1 GOTO FINISH

cd docker
del .\homeautomation\vsha.jar
copy ..\config\build\libs\vsha.jar .\homeautomation\vsha.jar
IF ERRORLEVEL 1 GOTO FINISH

docker-compose down
IF ERRORLEVEL 1 GOTO FINISH

docker-compose up -d --build
IF ERRORLEVEL 1 GOTO FINISH

del .\homeautomation\vsha.jar

echo ***************
echo *** sikeres ***
echo ***************
GOTO THEEND

:FINISH
echo ******************
echo *** sikertelen ***
echo ******************

:THEEND
