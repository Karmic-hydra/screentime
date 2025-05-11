$wrapperUrl = "https://raw.githubusercontent.com/gradle/gradle/v7.5.0/gradle/wrapper/gradle-wrapper.jar"
$wrapperPath = "gradle/wrapper/gradle-wrapper.jar"

# Create directory if it doesn't exist
New-Item -ItemType Directory -Force -Path "gradle/wrapper"

# Download the wrapper JAR
Invoke-WebRequest -Uri $wrapperUrl -OutFile $wrapperPath

Write-Host "Gradle wrapper JAR downloaded successfully." 