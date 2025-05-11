$gradleVersion = "7.5"
$gradleUrl = "https://services.gradle.org/distributions/gradle-${gradleVersion}-bin.zip"
$gradleZip = "gradle-${gradleVersion}-bin.zip"
$gradleHome = "$env:USERPROFILE\.gradle"

# Create Gradle home directory
New-Item -ItemType Directory -Force -Path $gradleHome

# Download Gradle
Invoke-WebRequest -Uri $gradleUrl -OutFile $gradleZip

# Extract Gradle
Expand-Archive -Path $gradleZip -DestinationPath $gradleHome -Force

# Clean up
Remove-Item $gradleZip

Write-Host "Gradle $gradleVersion installed successfully." 