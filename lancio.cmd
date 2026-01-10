@echo on

SET appname=Fattaass
set LUOGO=%~dp0
set LAST_DIR=no
set LUOGO1=%LUOGO%
if "%LUOGO:~-1%" == "\" set "LUOGO1=%LUOGO:~0,-1%"

for %%f in ("%LUOGO1%") do set "LAST_DIR=%%~nxf"
echo %LAST_DIR%
rem se sono in .\bin scendo di una
if /I "%LAST_DIR%" == "bin" cd /d "%LUOGO%\.."

set qta=0
set REMDEB=
if "%1" == "-d" (
	set REMDEB=-Xdebug -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=y 
	shift
)

set JARTEST=target\%appname%-jar-with-dependencies.jar
if exist "%JARTEST%" (
  set JAREXE=%JARTEST%
  call :info  "%JARTEST%"
  set /a qta=%qta%+1
  )

set JARTEST=%appname%-jar-with-dependencies.jar
if exist "%JARTEST%" (
  set JAREXE=%JARTEST%
  call :info  "%JARTEST%"
  set /a qta=%qta%+1
  )

set JARTEST=target\%appname%.jar
if exist "%JARTEST%" (
  set JAREXE=%JARTEST%
  call :info  "%JARTEST%"
  set /a qta=%qta%+1
  )

set JARTEST=%appname%.jar
if exist "%JARTEST%" (
  set JAREXE=%JARTEST%
  call :info  "%JARTEST%"
  set /a qta=%qta%+1
  )

set JARTEST=bin\%appname%.jar
if exist "%JARTEST%" (
  set JAREXE=%JARTEST%
  call :info  "%JARTEST%"
  set /a qta=%qta%+1
  )
  
if %qta% equ 0 (
  @echo.
  call :errore Non trovo il *.JAR del programma ?!?
  goto fine
) 
if %qta% gtr 1 (
  @echo.
  call :errore Troppi programmi *.JAR del programma !
  call :errore cancella le versioni piu vecchie
  call :errore e rilancia
  goto fine
) 
goto vai



:info
@echo off
echo [92m%*[0m
goto :eof

:errore
@echo off
echo [91m%*[0m
goto :eof

:vai
@echo on
if "%JAVAFX_HOME%" == "" (
	call :errore non hai settato la envir.var JAVAFX_HOME
	goto fine
	)
set MODS=javafx.controls
set MODS=%MODS%,javafx.base
set MODS=%MODS%,javafx.fxml
set MODS=%MODS%,javafx.graphics
set MODS=%MODS%,javafx.web
rem set MODS=%MODS%,javafx.media

rem java --module-path "%JAVAFX_HOME%\lib" --add-modules="%MODS%" -jar "%JAREXE%"

java %REMDEB%  --module-path "%JAVAFX_HOME%\lib" --add-modules="%MODS%" -jar "%JAREXE%"

:fine