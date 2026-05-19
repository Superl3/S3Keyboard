$ErrorActionPreference = "Stop"
$ProgressPreference = "SilentlyContinue"

$Root = Resolve-Path (Join-Path $PSScriptRoot "..")
$ToolsRoot = Join-Path $Root ".android-tools"
$Downloads = Join-Path $ToolsRoot "downloads"
$JdkRoot = Join-Path $ToolsRoot "jdk-17"
$AndroidSdk = Join-Path $ToolsRoot "android-sdk"
$CmdToolsRoot = Join-Path $AndroidSdk "cmdline-tools\latest"
$GradleVersion = "8.10.2"
$GradleRoot = Join-Path $ToolsRoot "gradle-$GradleVersion"

function Ensure-Directory($Path) {
    New-Item -ItemType Directory -Force -Path $Path | Out-Null
}

function Assert-UnderTools($Path) {
    $resolved = [System.IO.Path]::GetFullPath($Path)
    $tools = [System.IO.Path]::GetFullPath($ToolsRoot)
    if (-not $resolved.StartsWith($tools, [System.StringComparison]::OrdinalIgnoreCase)) {
        throw "Refusing to modify path outside tool root: $resolved"
    }
}

function Remove-ToolPath($Path) {
    if (Test-Path $Path) {
        Assert-UnderTools $Path
        Remove-Item -LiteralPath $Path -Recurse -Force
    }
}

function Download-File($Url, $OutFile) {
    if (Test-Path $OutFile) {
        Write-Host "Using cached $(Split-Path -Leaf $OutFile)"
        return
    }

    Write-Host "Downloading $Url"
    Invoke-WebRequest -UseBasicParsing -Uri $Url -OutFile $OutFile
}

function Expand-SingleRootZip($ZipPath, $Destination) {
    if (Test-Path $Destination) {
        return
    }

    $temp = Join-Path $ToolsRoot ("extract-" + [System.Guid]::NewGuid().ToString("N"))
    Ensure-Directory $temp
    try {
        Expand-Archive -LiteralPath $ZipPath -DestinationPath $temp -Force
        $inner = Get-ChildItem -LiteralPath $temp -Directory | Select-Object -First 1
        if ($null -eq $inner) {
            throw "Archive did not contain a root directory: $ZipPath"
        }
        Move-Item -LiteralPath $inner.FullName -Destination $Destination
    } finally {
        Remove-ToolPath $temp
    }
}

Ensure-Directory $ToolsRoot
Ensure-Directory $Downloads
Ensure-Directory $AndroidSdk

$jdkZip = Join-Path $Downloads "temurin-jdk17-windows-x64.zip"
$jdkUrl = "https://api.adoptium.net/v3/binary/latest/17/ga/windows/x64/jdk/hotspot/normal/eclipse?project=jdk"
Download-File $jdkUrl $jdkZip
Expand-SingleRootZip $jdkZip $JdkRoot

$env:JAVA_HOME = $JdkRoot
$env:Path = (Join-Path $JdkRoot "bin") + ";" + $env:Path

if (-not (Test-Path (Join-Path $CmdToolsRoot "bin\sdkmanager.bat"))) {
    $repoXml = [xml](Invoke-WebRequest -UseBasicParsing -Uri "https://dl.google.com/android/repository/repository2-1.xml").Content
    $cmdPackage = $repoXml."sdk-repository".remotePackage | Where-Object { $_.path -eq "cmdline-tools;latest" }
    $cmdArchive = $cmdPackage.archives.archive | Where-Object { $_."host-os" -eq "windows" }
    if ($null -eq $cmdArchive) {
        throw "Could not find Windows command-line tools archive in Android repository metadata."
    }
    $cmdZipName = Split-Path -Leaf $cmdArchive.complete.url
    $cmdZip = Join-Path $Downloads $cmdZipName
    $cmdUrl = "https://dl.google.com/android/repository/$($cmdArchive.complete.url)"

    Download-File $cmdUrl $cmdZip

    $temp = Join-Path $ToolsRoot ("cmdtools-" + [System.Guid]::NewGuid().ToString("N"))
    Ensure-Directory $temp
    try {
        Expand-Archive -LiteralPath $cmdZip -DestinationPath $temp -Force
        Ensure-Directory (Join-Path $AndroidSdk "cmdline-tools")
        Move-Item -LiteralPath (Join-Path $temp "cmdline-tools") -Destination $CmdToolsRoot
    } finally {
        Remove-ToolPath $temp
    }
}

$env:ANDROID_HOME = $AndroidSdk
$env:ANDROID_SDK_ROOT = $AndroidSdk
$env:Path = (Join-Path $CmdToolsRoot "bin") + ";" + (Join-Path $AndroidSdk "platform-tools") + ";" + $env:Path

$sdkManager = Join-Path $CmdToolsRoot "bin\sdkmanager.bat"
$sdkPackages = @(
    "platform-tools",
    "platforms;android-35",
    "build-tools;35.0.0"
)

Write-Host "Accepting Android SDK licenses"
(1..80 | ForEach-Object { "y" }) | & $sdkManager --sdk_root=$AndroidSdk --licenses | Out-Host

Write-Host "Installing Android SDK packages"
(1..80 | ForEach-Object { "y" }) | & $sdkManager --sdk_root=$AndroidSdk @sdkPackages | Out-Host

$gradleZip = Join-Path $Downloads "gradle-$GradleVersion-bin.zip"
$gradleUrl = "https://services.gradle.org/distributions/gradle-$GradleVersion-bin.zip"
Download-File $gradleUrl $gradleZip
Expand-SingleRootZip $gradleZip $GradleRoot

$sdkDir = $AndroidSdk.Replace("\", "/")
Set-Content -LiteralPath (Join-Path $Root "local.properties") -Encoding ASCII -Value "sdk.dir=$sdkDir"

if (-not (Test-Path (Join-Path $Root "gradlew.bat"))) {
    Write-Host "Generating Gradle wrapper"
    & (Join-Path $GradleRoot "bin\gradle.bat") -p $Root --no-daemon wrapper --gradle-version $GradleVersion --distribution-type bin
}

Write-Host "Android development environment is ready."
Write-Host "Run: .\scripts\build-debug.ps1"
