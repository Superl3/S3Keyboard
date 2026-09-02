param(
    [string] $Serial = "emulator-5558",
    [string] $OutputDir = "",
    [switch] $SkipBuild,
    [switch] $Resume,
    [string[]] $ThemeIds = @(),
    [string[]] $Modes = @("english", "hangul"),
    [int] $MaxNewCaptures = 0,
    [int] $BottomTolerancePx = 8
)

$ErrorActionPreference = "Stop"
$Root = Resolve-Path (Join-Path $PSScriptRoot "..")
$Adb = Join-Path $Root ".android-tools\android-sdk\platform-tools\adb.exe"
$Apk = Join-Path $Root "app\build\outputs\apk\debug\app-debug.apk"
$Package = "com.superl3.s3keyboard"
$Activity = "$Package/.MainActivity"
$Ime = "$Package/.S3KeyboardService"

if ([string]::IsNullOrWhiteSpace($OutputDir)) {
    $stamp = Get-Date -Format "yyyyMMdd-HHmmss"
    $OutputDir = Join-Path $Root "captures\runtime-theme-audit-geometry-$stamp"
}
New-Item -ItemType Directory -Force -Path $OutputDir | Out-Null
if (-not $Resume) {
    $existing = @(Get-ChildItem -LiteralPath $OutputDir -Force -ErrorAction SilentlyContinue)
    if ($existing.Count -gt 0) {
        throw "Refusing to mix a new audit with existing files: $OutputDir. Use a new folder or -Resume."
    }
}

if (-not (Test-Path -LiteralPath $Adb)) { throw "adb not found: $Adb" }
if (-not $SkipBuild) {
    & (Join-Path $Root "gradlew.bat") assembleDebug
    if ($LASTEXITCODE -ne 0) { throw "assembleDebug failed" }
}
if (-not (Test-Path -LiteralPath $Apk)) { throw "APK not found: $Apk" }

function Invoke-Adb {
    param([string[]] $Arguments, [switch] $AllowFailure)
    $previousPreference = $ErrorActionPreference
    try {
        $ErrorActionPreference = "Continue"
        $output = & $Adb -s $Serial @Arguments 2>&1
        $exit = $LASTEXITCODE
    } finally {
        $ErrorActionPreference = $previousPreference
    }
    if (-not $AllowFailure -and $exit -ne 0) {
        throw "adb failed ($exit): $($Arguments -join ' ')`n$($output -join "`n")"
    }
    return @($output)
}

function Wait-ForImeRegistration {
    for ($attempt = 0; $attempt -lt 30; $attempt++) {
        $listed = (Invoke-Adb -Arguments @("shell", "ime", "list", "-a") -AllowFailure) -join "`n"
        if ($listed -match "com\.superl3\.s3keyboard/\.S3KeyboardService") { return $true }
        Start-Sleep -Milliseconds 180
    }
    return $false
}

function Get-ImeState {
    return (Invoke-Adb -Arguments @("shell", "dumpsys", "input_method") -AllowFailure) -join "`n"
}

function Test-ImeSelectedAndShown {
    $state = Get-ImeState
    return $state -match "mCurMethodId=com\.superl3\.s3keyboard/\.S3KeyboardService" -and
            $state -match "mInputShown=true"
}

function Wait-ForIme {
    for ($attempt = 0; $attempt -lt 16; $attempt++) {
        Invoke-Adb -Arguments @("shell", "input", "tap", "420", "1580") -AllowFailure | Out-Null
        Start-Sleep -Milliseconds 180
        if (Test-ImeSelectedAndShown) { return $true }
    }
    return $false
}

function Get-AuditLogs {
    return (Invoke-Adb -Arguments @("logcat", "-d", "-s", "S3KeyboardAudit:D", "*:S") -AllowFailure) -join "`n"
}

function Wait-ForRenderReady {
    param([string] $ThemeId, [string] $Mode, [string] $Material)
    $themePattern = [regex]::Escape($ThemeId)
    $modePattern = [regex]::Escape($Mode)
    $materialPattern = [regex]::Escape($Material)
    for ($attempt = 0; $attempt -lt 40; $attempt++) {
        $logs = Get-AuditLogs
        if ($logs -match "renderReady theme=$themePattern mode=$modePattern material=$materialPattern ") {
            return $true
        }
        Start-Sleep -Milliseconds 180
    }
    return $false
}

function Get-GeometrySamples {
    param([string] $ThemeId, [string] $Mode, [string] $Material)
    $samples = [System.Collections.Generic.List[object]]::new()
    $pattern = 'geometry theme=(\S+) mode=(\S+) material=(\S+) width=(\d+) height=(\d+) x=(-?\d+) y=(-?\d+) screenWidth=(\d+) screenHeight=(\d+) navBottomInset=(\d+) rawInputBottom=(-?\d+) keyboardBottom=(-?\d+) expectedBottom=(-?\d+) bottomDelta=(-?\d+) uptimeMs=(\d+)'
    foreach ($line in ((Get-AuditLogs) -split "`r?`n")) {
        if ($line -notmatch $pattern) { continue }
        if ($Matches[1] -ne $ThemeId -or $Matches[2] -ne $Mode -or $Matches[3] -ne $Material) { continue }
        $samples.Add([pscustomobject]@{
            Theme = $Matches[1]; Mode = $Matches[2]; Material = $Matches[3]
            Width = [int]$Matches[4]; Height = [int]$Matches[5]
            X = [int]$Matches[6]; Y = [int]$Matches[7]
            ScreenWidth = [int]$Matches[8]; ScreenHeight = [int]$Matches[9]
            NavBottomInset = [int]$Matches[10]; RawInputBottom = [int]$Matches[11]
            KeyboardBottom = [int]$Matches[12]; ExpectedBottom = [int]$Matches[13]
            BottomDelta = [int]$Matches[14]; UptimeMs = [long]$Matches[15]
        })
    }
    return @($samples)
}

function Test-GeometryBounds {
    param([object] $Sample)
    if ($Sample.Width -le 0 -or $Sample.Height -le 0) { return $false }
    if ($Sample.X -lt 0 -or $Sample.Y -lt 0) { return $false }
    if (($Sample.X + $Sample.Width) -gt $Sample.ScreenWidth) { return $false }
    if (($Sample.Y + $Sample.Height) -gt $Sample.ScreenHeight) { return $false }
    if ([Math]::Abs($Sample.BottomDelta) -gt $BottomTolerancePx) { return $false }
    return $true
}

function Test-SameGeometry {
    param([object] $A, [object] $B)
    return $A.Width -eq $B.Width -and $A.Height -eq $B.Height -and
            $A.X -eq $B.X -and $A.Y -eq $B.Y -and
            $A.ScreenWidth -eq $B.ScreenWidth -and $A.ScreenHeight -eq $B.ScreenHeight -and
            $A.NavBottomInset -eq $B.NavBottomInset -and
            $A.KeyboardBottom -eq $B.KeyboardBottom -and $A.ExpectedBottom -eq $B.ExpectedBottom
}

function Wait-ForStableGeometry {
    param([string] $ThemeId, [string] $Mode, [string] $Material)
    for ($attempt = 0; $attempt -lt 40; $attempt++) {
        $samples = @(Get-GeometrySamples -ThemeId $ThemeId -Mode $Mode -Material $Material)
        for ($i = $samples.Count - 1; $i -ge 1; $i--) {
            $newer = $samples[$i]
            $older = $samples[$i - 1]
            if (($newer.UptimeMs - $older.UptimeMs) -lt 250) { continue }
            if ((Test-GeometryBounds $older) -and (Test-GeometryBounds $newer) -and
                    (Test-SameGeometry $older $newer)) {
                return $newer
            }
        }
        Start-Sleep -Milliseconds 180
    }
    return $null
}

function Save-ScreenshotAtomically {
    param([string] $Destination)
    $remote = "/sdcard/s3-theme-runtime.png"
    $staging = "$Destination.pending"
    Remove-Item -LiteralPath $staging -Force -ErrorAction SilentlyContinue
    try {
        Invoke-Adb -Arguments @("shell", "screencap", "-p", $remote) | Out-Null
        Invoke-Adb -Arguments @("pull", $remote, $staging) | Out-Null
        $info = Get-Item -LiteralPath $staging -ErrorAction SilentlyContinue
        if ($null -eq $info -or $info.Length -le 0) { throw "Captured PNG is missing or empty" }
        if (-not (Test-ImeSelectedAndShown)) { throw "IME state changed while staging screenshot" }
        Move-Item -LiteralPath $staging -Destination $Destination -Force
    } finally {
        Invoke-Adb -Arguments @("shell", "rm", "-f", $remote) -AllowFailure | Out-Null
        Remove-Item -LiteralPath $staging -Force -ErrorAction SilentlyContinue
    }
}

$deviceState = (Invoke-Adb -Arguments @("get-state") -AllowFailure) -join ""
if ($deviceState -notmatch "device") { throw "Device $Serial is not ready: $deviceState" }
Invoke-Adb -Arguments @("uninstall", $Package) -AllowFailure | Out-Null
Invoke-Adb -Arguments @("install", $Apk) | Out-Null
if (-not (Wait-ForImeRegistration)) { throw "IME component did not register after install" }
Invoke-Adb -Arguments @("shell", "ime", "enable", $Ime) | Out-Null
Invoke-Adb -Arguments @("shell", "ime", "set", $Ime) | Out-Null

$themeFiles = @(Get-ChildItem -LiteralPath (Join-Path $Root "themes") -Filter "*.json" | Sort-Object Name)
if ($ThemeIds.Count -gt 0) {
    $wanted = [System.Collections.Generic.HashSet[string]]::new([string[]]$ThemeIds)
    $themeFiles = @($themeFiles | Where-Object { $wanted.Contains($_.BaseName) })
}
if ($themeFiles.Count -eq 0) { throw "No themes found for the requested selection" }
if ($Modes.Count -eq 0) { throw "No keyboard modes requested" }
$results = [System.Collections.Generic.List[object]]::new()
$index = 0
$newCaptureCount = 0

:themeLoop foreach ($themeFile in $themeFiles) {
    $index++
    $themeId = $themeFile.BaseName
    $theme = Get-Content -Raw -Encoding UTF8 -LiteralPath $themeFile.FullName | ConvertFrom-Json
    $material = if ($null -ne $theme.effects -and -not [string]::IsNullOrWhiteSpace([string]$theme.effects.materialStyle)) {
        [string]$theme.effects.materialStyle
    } else { "solid" }
    foreach ($mode in $Modes) {
        $prefix = "{0:D2}" -f $index
        $fileName = "$prefix-$themeId-$mode.png"
        $destination = Join-Path $OutputDir $fileName
        if ($Resume -and (Test-Path -LiteralPath $destination) -and (Get-Item $destination).Length -gt 0) {
            $results.Add([pscustomobject]@{ Theme=$themeId; Mode=$mode; Material=$material; Stable=$true; File=$fileName; Error="" })
            continue
        }
        try {
            Invoke-Adb -Arguments @("logcat", "-c") -AllowFailure | Out-Null
            Invoke-Adb -Arguments @("shell", "am", "force-stop", $Package) | Out-Null
            Invoke-Adb -Arguments @(
                "shell", "am", "start", "-W", "-n", $Activity,
                "--ez", "demo_settings", "true",
                "--ez", "demo_show_keyboard", "true",
                "--es", "theme_preset_id", $themeId,
                "--es", "keyboard_mode", $mode,
                "--ez", "transparent_overlay_input", "false"
            ) | Out-Null
            Invoke-Adb -Arguments @("shell", "ime", "enable", $Ime) | Out-Null
            Invoke-Adb -Arguments @("shell", "ime", "set", $Ime) | Out-Null
            if (-not (Wait-ForIme)) { throw "IME did not become visible for $themeId / $mode" }
            if (-not (Wait-ForRenderReady -ThemeId $themeId -Mode $mode -Material $material)) {
                throw "renderReady did not match $themeId / $mode / $material"
            }
            Start-Sleep -Milliseconds 850
            $geometry = Wait-ForStableGeometry -ThemeId $themeId -Mode $mode -Material $material
            if ($null -eq $geometry) { throw "stable bottom-aligned geometry was not observed" }
            if (-not (Test-ImeSelectedAndShown)) { throw "IME state changed before capture" }
            Save-ScreenshotAtomically -Destination $destination
            $results.Add([pscustomobject]@{
                Theme=$themeId; Mode=$mode; Material=$material; Stable=$true; File=$fileName; Error=""
                Width=$geometry.Width; Height=$geometry.Height; X=$geometry.X; Y=$geometry.Y
                ScreenHeight=$geometry.ScreenHeight; NavBottomInset=$geometry.NavBottomInset
                KeyboardBottom=$geometry.KeyboardBottom; ExpectedBottom=$geometry.ExpectedBottom
                BottomDelta=$geometry.BottomDelta
            })
            Write-Host "[$index/$($themeFiles.Count)] $themeId / $mode -> $fileName delta=$($geometry.BottomDelta)"
            $newCaptureCount++
            if ($MaxNewCaptures -gt 0 -and $newCaptureCount -ge $MaxNewCaptures) { break themeLoop }
        } catch {
            $message = $_.Exception.Message
            $results.Add([pscustomobject]@{ Theme=$themeId; Mode=$mode; Material=$material; Stable=$false; File=$fileName; Error=$message })
            Write-Warning "$themeId / $mode failed: $message"
            if ($message -match "device offline|device '.*' not found|no devices/emulators found") { break themeLoop }
        }
    }
}

$summaryPath = Join-Path $OutputDir "capture-summary.csv"
$results | Export-Csv -NoTypeInformation -Encoding UTF8 -LiteralPath $summaryPath
$manifest = [System.Collections.Generic.List[object]]::new()
$index = 0
foreach ($themeFile in $themeFiles) {
    $index++
    foreach ($mode in $Modes) {
        $fileName = ("{0:D2}-{1}-{2}.png" -f $index, $themeFile.BaseName, $mode)
        $path = Join-Path $OutputDir $fileName
        $info = Get-Item -LiteralPath $path -ErrorAction SilentlyContinue
        $manifest.Add([pscustomobject]@{
            Theme=$themeFile.BaseName; Mode=$mode; File=$fileName
            Exists=$null -ne $info; Bytes=if ($null -ne $info) { $info.Length } else { 0 }
        })
    }
}
$manifestPath = Join-Path $OutputDir "capture-manifest.csv"
$manifest | Export-Csv -NoTypeInformation -Encoding UTF8 -LiteralPath $manifestPath
$duplicates = @($manifest | Group-Object Theme,Mode | Where-Object Count -ne 1)
$missing = @($manifest | Where-Object { -not $_.Exists -or $_.Bytes -le 0 })
$failures = @($results | Where-Object { -not [string]::IsNullOrWhiteSpace($_.Error) -or -not $_.Stable })
Write-Host "Runtime theme captures: $OutputDir"
Write-Host "PNG count: $(@(Get-ChildItem -LiteralPath $OutputDir -Filter '*.png' -File).Count) / expected $($themeFiles.Count * $Modes.Count)"
Write-Host "Summary: $summaryPath"
Write-Host "Manifest: $manifestPath"
if ($duplicates.Count -gt 0 -or $missing.Count -gt 0 -or $failures.Count -gt 0) {
    Write-Warning "Audit incomplete: duplicates=$($duplicates.Count) missing=$($missing.Count) failures=$($failures.Count)"
    exit 2
}
