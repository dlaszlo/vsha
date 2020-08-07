call setenv.bat
cd ..
call gradlew clean build --exclude-task test
IF ERRORLEVEL 1 GOTO FINISH
ECHO error level is %ERRORLEVEL%
cd docker
del vsha.jar
copy ..\config\build\libs\vsha.jar .\vsha.jar
IF ERRORLEVEL 1 GOTO FINISH
docker stop homeautomation
IF ERRORLEVEL 1 GOTO FINISH
docker rm homeautomation
IF ERRORLEVEL 1 GOTO FINISH
docker build -t homeautomation-img .
IF ERRORLEVEL 1 GOTO FINISH
docker run --name homeautomation --restart always  --hostname homeautomation -p8080:8080 -v /sys:/sys -v /storage/homeautomation/log:/var/log/homeautomation -e TZ=Europe/Budapest -d homeautomation-img
IF ERRORLEVEL 1 GOTO FINISH
del vsha.jar
echo ***************
echo *** sikeres ***
echo ***************
GOTO THEEND
:FINISH
echo ******************
echo *** sikertelen ***
echo ******************
:THEEND
