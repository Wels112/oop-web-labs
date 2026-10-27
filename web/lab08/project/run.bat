@echo off
chcp 65001 >nul
call mvnw.cmd -q -DskipTests package
if errorlevel 1 exit /b 1
java -Dfile.encoding=UTF-8 -cp "target\classes;target\dependency\*" edu.java.web.DevServer
