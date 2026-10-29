@echo off
chcp 65001 >nul
cd /d "%~dp0"
call "%~dp0mvnw.cmd" -q -DskipTests package
if errorlevel 1 exit /b 1
java -Dfile.encoding=UTF-8 -cp "target\classes;target\dependency\*" edu.java.issues.Launcher %*
pause
