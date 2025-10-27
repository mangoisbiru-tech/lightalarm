@echo off
echo Building Light Alarm APK...
cd android
set JAVA_HOME=C:\Users\lau_w\AppData\Local\jdk-11.0.2
echo Using JAVA_HOME: %JAVA_HOME%
"%JAVA_HOME%\bin\java" -version
gradlew.bat assembleDebug
pause