@ECHO OFF
SET APP_HOME=%~dp0
SET CLASSPATH=%APP_HOME%gradle\wrapper\gradle-wrapper.jar
IF NOT EXIST "%CLASSPATH%" (
  ECHO Missing gradle\wrapper\gradle-wrapper.jar. Open the project in Android Studio to regenerate the wrapper.
  EXIT /B 1
)
java %JAVA_OPTS% %GRADLE_OPTS% -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*
