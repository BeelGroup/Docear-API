@echo off

CALL :reconnect

:LOOP

:: start with sleepTime of 500ms
java -jar googleQueryWorker.jar sleep=500 maxDocs=20

IF %ERRORLEVEL% == 9999 (
   ECHO "NOTHING TODO -> WAITING..."   
   :: wait 5 min if the worker got nothing to do, or got an exception
   CALL :wait 300   
   GOTO LOOP
)

CALL :reconnect

GOTO LOOP

:reconnect
SETLOCAL
ECHO "RECONNECTING..."
RouterReconnect.exe
CALL :wait 150
ENDLOCAL

:wait
SETLOCAL
"%SystemRoot%/System32/timeout.exe" /T %1 /NOBREAK
ENDLOCAL  
