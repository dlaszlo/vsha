call setenv.bat

call gradlew clean build --exclude-task test
IF ERRORLEVEL 1 GOTO FINISH

cd vsha-frontend
call npm run build
IF ERRORLEVEL 1 GOTO FINISH

cd ..
docker-compose down
IF ERRORLEVEL 1 GOTO FINISH

docker-compose up -d --build
IF ERRORLEVEL 1 GOTO FINISH

echo ***************
echo *** sikeres ***
echo ***************
GOTO THEEND

:FINISH
echo ******************
echo *** sikertelen ***
echo ******************

:THEEND
