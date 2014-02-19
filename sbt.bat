set SCRIPT_DIR=%~dp0
java -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=256m -Xmx1024M -Xss10M -jar "%SCRIPT_DIR%\sbt-launch.jar" %*
