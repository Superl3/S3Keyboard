param(
    [switch] $SkipBuild,
    [string] $Serial = "",
    [switch] $SkipEmulatorLaunch,
    [string] $OutDir = ""
)

$ErrorActionPreference = "Stop"

. (Join-Path $PSScriptRoot "env.ps1")

$Root = Resolve-Path (Join-Path $PSScriptRoot "..")
$Adb = Join-Path $env:ANDROID_SDK_ROOT "platform-tools\adb.exe"
$Package = "com.superl3.s3keyboard"
$Ime = "$Package/.S3KeyboardService"
$Apk = Join-Path $Root "app\build\outputs\apk\debug\app-debug.apk"
$CaptureDir = if ([string]::IsNullOrWhiteSpace($OutDir)) {
    Join-Path $Root "captures\smoke"
} else {
    $OutDir
}

if (-not $SkipBuild) {
    & (Join-Path $PSScriptRoot "build-debug.ps1")
}

if (-not $SkipEmulatorLaunch -and [string]::IsNullOrWhiteSpace($Serial)) {
    & (Join-Path $PSScriptRoot "setup-emulator.ps1")
    & (Join-Path $PSScriptRoot "launch-emulator.ps1")
}

New-Item -ItemType Directory -Force -Path $CaptureDir | Out-Null

function Resolve-DeviceSerial {
    if (-not [string]::IsNullOrWhiteSpace($Serial)) {
        return $Serial
    }
    $line = & $Adb devices | Select-String -Pattern "^\S+\s+device$" | Select-Object -First 1
    if ($null -eq $line) {
        throw "No connected ADB device. Pass -Serial or connect an emulator/device first."
    }
    return $line.ToString().Split()[0]
}

$Device = Resolve-DeviceSerial
$AdbTarget = @("-s", $Device)
$Results = New-Object System.Collections.Generic.List[object]

function Invoke-AdbTarget {
    param(
        [Parameter(ValueFromRemainingArguments = $true)]
        [string[]] $Arguments
    )
    & $Adb @AdbTarget @Arguments | Out-Host
    if ($LASTEXITCODE -ne 0) {
        throw "adb failed: $($Arguments -join ' ')"
    }
}

function Invoke-AdbTargetText {
    param(
        [Parameter(ValueFromRemainingArguments = $true)]
        [string[]] $Arguments
    )
    $result = & $Adb @AdbTarget @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "adb failed: $($Arguments -join ' ')"
    }
    return ($result | Out-String)
}

Invoke-AdbTarget wait-for-device
Invoke-AdbTarget install -r $Apk
Invoke-AdbTarget shell ime enable $Ime
Invoke-AdbTarget shell ime set $Ime
Invoke-AdbTarget shell settings put secure show_ime_with_hard_keyboard 1

function Test-PackageInstalled {
    param([string] $PackageName)
    $result = (& $Adb @AdbTarget shell pm path $PackageName | Out-String).Trim()
    if ($LASTEXITCODE -ne 0) {
        return $false
    }
    return $result.StartsWith("package:")
}

function Get-ImeDump {
    return Invoke-AdbTargetText shell dumpsys input_method
}

function Test-ImeVisible {
    param([string] $Dump)
    return $Dump.Contains("mInputShown=true") -and $Dump.Contains($Ime)
}

function Test-ImeSelected {
    $selected = Invoke-AdbTargetText shell settings get secure default_input_method
    return $selected.Trim() -eq $Ime
}

function New-RemoteEvidence {
    param(
        [string] $ProfileHint,
        [string] $PackageName
    )
    $isRemote = $ProfileHint.StartsWith("remote_")
    $exportCommand = ""
    if ($isRemote -and -not [string]::IsNullOrWhiteSpace($PackageName)) {
        $exportCommand = "rtk powershell -ExecutionPolicy Bypass -File .\scripts\export-remote-compatibility.ps1 -TargetPackage $PackageName"
    }
    return [ordered]@{
        evidenceLevel = if ($isRemote) { "requires_manual_windows_confirmation" } else { "android_ime_state_only" }
        localMatrixCommand = $exportCommand
        requiredCaseLabels = @(
            "Esc",
            "Tab",
            "Shift+Tab",
            "Ctrl+Tab",
            "Alt+Tab",
            "Ctrl+A",
            "F1",
            "F2",
            "F3",
            "F4",
            "F5",
            "F6",
            "F7",
            "F8",
            "F9",
            "F10",
            "F11",
            "F12",
            "Alt+Shift",
            "Ctrl+Space",
            "Win+Space",
            "Lang"
        )
        requiredAppFamilies = @(
            "parsec",
            "moonlight",
            "microsoft_rdp",
            "chrome_remote_desktop",
            "steam_link",
            "anydesk",
            "teamviewer"
        )
        manualResultRequired = $isRemote
        note = if ($isRemote) {
            "Run the local matrix in the target app, then mark pass/fail after confirming the remote Windows session received each shortcut."
        } else {
            "No remote Windows shortcut delivery proof is expected for this target."
        }
    }
}

function New-ProfileExpectation {
    param(
        [string] $ProfileHint,
        [string] $PackageName
    )
    if ($ProfileHint.StartsWith("remote_")) {
        return [ordered]@{
            id = $ProfileHint
            expectedRemoteMode = $true
            expectedPreferAsciiLayout = $true
            expectedForceNumberRow = $true
            expectedAllowComposingText = $false
            expectedAllowTextConveniences = $false
            manualRemoteDeliveryRequired = $true
            remoteCompatibilityEvidence = New-RemoteEvidence $ProfileHint $PackageName
            note = "Android IME visibility is not proof that the remote Windows session received KeyEvent shortcuts."
        }
    }
    switch ($ProfileHint) {
        "browser_search" {
            return [ordered]@{
                id = "browser_search"
                expectedRemoteMode = $false
                expectedPreferAsciiLayout = $true
                expectedForceNumberRow = $false
                expectedAllowComposingText = $null
                expectedAllowTextConveniences = $true
                manualRemoteDeliveryRequired = $false
                remoteCompatibilityEvidence = New-RemoteEvidence $ProfileHint $PackageName
                note = "Search and URL fields should prefer ASCII without forcing remote mode."
            }
        }
        "webview" {
            return [ordered]@{
                id = "webview"
                expectedRemoteMode = $false
                expectedPreferAsciiLayout = $true
                expectedForceNumberRow = $false
                expectedAllowComposingText = $false
                expectedAllowTextConveniences = $true
                manualRemoteDeliveryRequired = $false
                remoteCompatibilityEvidence = New-RemoteEvidence $ProfileHint $PackageName
                note = "WebView-like editors should avoid composing spans when they are not reliable."
            }
        }
        "webview-provider" {
            return [ordered]@{
                id = "webview"
                expectedRemoteMode = $false
                expectedPreferAsciiLayout = $true
                expectedForceNumberRow = $false
                expectedAllowComposingText = $false
                expectedAllowTextConveniences = $true
                manualRemoteDeliveryRequired = $false
                remoteCompatibilityEvidence = New-RemoteEvidence $ProfileHint $PackageName
                note = "Provider packages are recorded for installation visibility; launch may be skipped."
            }
        }
        "messaging" {
            return [ordered]@{
                id = "messaging"
                expectedRemoteMode = $false
                expectedPreferAsciiLayout = $false
                expectedForceNumberRow = $false
                expectedAllowComposingText = $true
                expectedAllowTextConveniences = $true
                manualRemoteDeliveryRequired = $false
                remoteCompatibilityEvidence = New-RemoteEvidence $ProfileHint $PackageName
                note = "Messaging targets should keep Hangul composing available."
            }
        }
        "password" {
            return [ordered]@{
                id = "password"
                expectedRemoteMode = $false
                expectedPreferAsciiLayout = $true
                expectedForceNumberRow = $true
                expectedAllowComposingText = $false
                expectedAllowTextConveniences = $false
                manualRemoteDeliveryRequired = $false
                remoteCompatibilityEvidence = New-RemoteEvidence $ProfileHint $PackageName
                note = "Password fields should prefer ASCII and avoid composing/text conveniences."
            }
        }
        "number" {
            return [ordered]@{
                id = "number"
                expectedRemoteMode = $false
                expectedPreferAsciiLayout = $null
                expectedForceNumberRow = $false
                expectedAllowComposingText = $false
                expectedAllowTextConveniences = $false
                manualRemoteDeliveryRequired = $false
                remoteCompatibilityEvidence = New-RemoteEvidence $ProfileHint $PackageName
                note = "Number-like fields should use the editor's numeric surface and avoid composing/text conveniences."
            }
        }
        "url" {
            return [ordered]@{
                id = "url"
                expectedRemoteMode = $false
                expectedPreferAsciiLayout = $true
                expectedForceNumberRow = $false
                expectedAllowComposingText = $false
                expectedAllowTextConveniences = $false
                manualRemoteDeliveryRequired = $false
                remoteCompatibilityEvidence = New-RemoteEvidence $ProfileHint $PackageName
                note = "URL fields should prefer ASCII and avoid text conveniences."
            }
        }
        "email" {
            return [ordered]@{
                id = "email"
                expectedRemoteMode = $false
                expectedPreferAsciiLayout = $true
                expectedForceNumberRow = $false
                expectedAllowComposingText = $false
                expectedAllowTextConveniences = $false
                manualRemoteDeliveryRequired = $false
                remoteCompatibilityEvidence = New-RemoteEvidence $ProfileHint $PackageName
                note = "Email fields should prefer ASCII and avoid text conveniences."
            }
        }
        "web_edit" {
            return [ordered]@{
                id = "web_edit"
                expectedRemoteMode = $false
                expectedPreferAsciiLayout = $true
                expectedForceNumberRow = $false
                expectedAllowComposingText = $false
                expectedAllowTextConveniences = $false
                manualRemoteDeliveryRequired = $false
                remoteCompatibilityEvidence = New-RemoteEvidence $ProfileHint $PackageName
                note = "Web-edit fields should use the commit-only text path and avoid composing spans."
            }
        }
        default {
            return [ordered]@{
                id = if ([string]::IsNullOrWhiteSpace($ProfileHint)) { "standard" } else { $ProfileHint }
                expectedRemoteMode = $false
                expectedPreferAsciiLayout = $null
                expectedForceNumberRow = $null
                expectedAllowComposingText = $null
                expectedAllowTextConveniences = $null
                manualRemoteDeliveryRequired = $false
                remoteCompatibilityEvidence = New-RemoteEvidence $ProfileHint $PackageName
                note = "Standard profile keeps the field policy derived from EditorInfo."
            }
        }
    }
}

function Focus-LocalPracticeField {
    for ($attempt = 0; $attempt -lt 12; $attempt++) {
        Invoke-AdbTarget shell input tap 540 565
        Start-Sleep -Milliseconds 500
        Invoke-AdbTarget shell ime set $Ime
        Start-Sleep -Milliseconds 700
        if (Test-ImeVisible (Get-ImeDump)) {
            return
        }
    }
    Get-ImeDump | Set-Content -LiteralPath (Join-Path $CaptureDir "local-practice-not-ready-input_method.txt") -Encoding UTF8
    throw "Local practice field did not show $Ime"
}

function Save-State {
    param(
        [string] $Name,
        [string] $PackageName,
        [string] $ProfileHint
    )
    $safeName = $Name -replace "[^A-Za-z0-9_-]", "-"
    $dumpFile = Join-Path $CaptureDir "$safeName-input_method.txt"
    $screenFile = Join-Path $CaptureDir "$safeName.png"
    $state = Get-ImeDump
    $state | Set-Content -LiteralPath $dumpFile -Encoding UTF8
    & $Adb @AdbTarget shell screencap -p "/sdcard/$safeName.png" | Out-Null
    if ($LASTEXITCODE -ne 0) {
        throw "adb failed: shell screencap -p /sdcard/$safeName.png"
    }
    Invoke-AdbTarget pull "/sdcard/$safeName.png" $screenFile
    $Results.Add([ordered]@{
        name = $Name
        packageName = $PackageName
        profileHint = $ProfileHint
        profileExpectation = New-ProfileExpectation $ProfileHint $PackageName
        installed = $true
        launched = $true
        imeSelected = Test-ImeSelected
        imeVisible = Test-ImeVisible $state
        inputMethodDump = $dumpFile
        screenshot = $screenFile
    }) | Out-Null
}

function Add-SkippedResult {
    param(
        [string] $Name,
        [string] $PackageName,
        [string] $ProfileHint,
        [string] $Reason
    )
    $Results.Add([ordered]@{
        name = $Name
        packageName = $PackageName
        profileHint = $ProfileHint
        profileExpectation = New-ProfileExpectation $ProfileHint $PackageName
        installed = $false
        launched = $false
        imeSelected = Test-ImeSelected
        imeVisible = $false
        reason = $Reason
    }) | Out-Null
}

Write-Host "Smoke: local settings/practice field"
Invoke-AdbTarget shell am start -n "$Package/.MainActivity" --ez demo_settings true --ez demo_show_keyboard true
Focus-LocalPracticeField
Save-State "local-practice" $Package "standard"

$SyntheticFields = @(
    @{ Name = "field-password"; Profile = "password"; DemoProfile = "password" },
    @{ Name = "field-number"; Profile = "number"; DemoProfile = "number" },
    @{ Name = "field-url"; Profile = "url"; DemoProfile = "url" },
    @{ Name = "field-email"; Profile = "email"; DemoProfile = "email" },
    @{ Name = "field-web-edit"; Profile = "web_edit"; DemoProfile = "web_edit" },
    @{ Name = "field-search"; Profile = "browser_search"; DemoProfile = "search" },
    @{ Name = "field-multiline"; Profile = "standard"; DemoProfile = "multiline" }
)

foreach ($field in $SyntheticFields) {
    Write-Host "Smoke: synthetic $($field.Name)"
    Invoke-AdbTarget shell am start -n "$Package/.MainActivity" --ez demo_settings true --ez demo_show_keyboard true --es demo_field_profile $field.DemoProfile
    Focus-LocalPracticeField
    Save-State $field.Name $Package $field.Profile
}

$Targets = @(
    @{ Name = "chrome-url"; PackageName = "com.android.chrome"; Profile = "browser_search"; Command = @("shell", "am", "start", "-a", "android.intent.action.VIEW", "-d", "https://example.com") },
    @{ Name = "chrome-beta"; PackageName = "com.chrome.beta"; Profile = "browser_search"; Command = @("shell", "monkey", "-p", "com.chrome.beta", "1") },
    @{ Name = "chrome-dev"; PackageName = "com.chrome.dev"; Profile = "browser_search"; Command = @("shell", "monkey", "-p", "com.chrome.dev", "1") },
    @{ Name = "chrome-canary"; PackageName = "com.chrome.canary"; Profile = "browser_search"; Command = @("shell", "monkey", "-p", "com.chrome.canary", "1") },
    @{ Name = "samsung-browser"; PackageName = "com.sec.android.app.sbrowser"; Profile = "browser_search"; Command = @("shell", "monkey", "-p", "com.sec.android.app.sbrowser", "1") },
    @{ Name = "edge"; PackageName = "com.microsoft.emmx"; Profile = "browser_search"; Command = @("shell", "monkey", "-p", "com.microsoft.emmx", "1") },
    @{ Name = "brave"; PackageName = "com.brave.browser"; Profile = "browser_search"; Command = @("shell", "monkey", "-p", "com.brave.browser", "1") },
    @{ Name = "firefox"; PackageName = "org.mozilla.firefox"; Profile = "browser_search"; Command = @("shell", "monkey", "-p", "org.mozilla.firefox", "1") },
    @{ Name = "firefox-beta"; PackageName = "org.mozilla.firefox_beta"; Profile = "browser_search"; Command = @("shell", "monkey", "-p", "org.mozilla.firefox_beta", "1") },
    @{ Name = "opera"; PackageName = "com.opera.browser"; Profile = "browser_search"; Command = @("shell", "monkey", "-p", "com.opera.browser", "1") },
    @{ Name = "system-webview"; PackageName = "com.google.android.webview"; Profile = "webview-provider"; Command = $null },
    @{ Name = "android-webview"; PackageName = "com.android.webview"; Profile = "webview-provider"; Command = $null },
    @{ Name = "system-webview-beta"; PackageName = "com.google.android.webview.beta"; Profile = "webview-provider"; Command = $null },
    @{ Name = "system-webview-dev"; PackageName = "com.google.android.webview.dev"; Profile = "webview-provider"; Command = $null },
    @{ Name = "chromium-webview-shell"; PackageName = "org.chromium.webview_shell"; Profile = "webview-provider"; Command = $null },
    @{ Name = "messages-google"; PackageName = "com.google.android.apps.messaging"; Profile = "messaging"; Command = @("shell", "monkey", "-p", "com.google.android.apps.messaging", "1") },
    @{ Name = "messages-samsung"; PackageName = "com.samsung.android.messaging"; Profile = "messaging"; Command = @("shell", "monkey", "-p", "com.samsung.android.messaging", "1") },
    @{ Name = "kakaotalk"; PackageName = "com.kakao.talk"; Profile = "messaging"; Command = @("shell", "monkey", "-p", "com.kakao.talk", "1") },
    @{ Name = "telegram"; PackageName = "org.telegram.messenger"; Profile = "messaging"; Command = @("shell", "monkey", "-p", "org.telegram.messenger", "1") },
    @{ Name = "telegram-x"; PackageName = "org.telegram.messenger.web"; Profile = "messaging"; Command = @("shell", "monkey", "-p", "org.telegram.messenger.web", "1") },
    @{ Name = "whatsapp"; PackageName = "com.whatsapp"; Profile = "messaging"; Command = @("shell", "monkey", "-p", "com.whatsapp", "1") },
    @{ Name = "line"; PackageName = "jp.naver.line.android"; Profile = "messaging"; Command = @("shell", "monkey", "-p", "jp.naver.line.android", "1") },
    @{ Name = "signal"; PackageName = "org.thoughtcrime.securesms"; Profile = "messaging"; Command = @("shell", "monkey", "-p", "org.thoughtcrime.securesms", "1") },
    @{ Name = "discord"; PackageName = "com.discord"; Profile = "messaging"; Command = @("shell", "monkey", "-p", "com.discord", "1") },
    @{ Name = "facebook-messenger"; PackageName = "com.facebook.orca"; Profile = "messaging"; Command = @("shell", "monkey", "-p", "com.facebook.orca", "1") },
    @{ Name = "notes-keep"; PackageName = "com.google.android.keep"; Profile = "standard"; Command = @("shell", "monkey", "-p", "com.google.android.keep", "1") },
    @{ Name = "parsec"; PackageName = "tv.parsec.client"; Profile = "remote_parsec"; Command = @("shell", "monkey", "-p", "tv.parsec.client", "1") },
    @{ Name = "moonlight"; PackageName = "com.limelight"; Profile = "remote_moonlight"; Command = @("shell", "monkey", "-p", "com.limelight", "1") },
    @{ Name = "microsoft-rdp"; PackageName = "com.microsoft.rdc.androidx"; Profile = "remote_microsoft_rdp"; Command = @("shell", "monkey", "-p", "com.microsoft.rdc.androidx", "1") },
    @{ Name = "microsoft-rdp-legacy"; PackageName = "com.microsoft.rdc.android"; Profile = "remote_microsoft_rdp"; Command = @("shell", "monkey", "-p", "com.microsoft.rdc.android", "1") },
    @{ Name = "chrome-remote-desktop"; PackageName = "com.google.chromeremotedesktop"; Profile = "remote_chrome_remote_desktop"; Command = @("shell", "monkey", "-p", "com.google.chromeremotedesktop", "1") },
    @{ Name = "steam-link"; PackageName = "com.valvesoftware.steamlink"; Profile = "remote_steam_link"; Command = @("shell", "monkey", "-p", "com.valvesoftware.steamlink", "1") },
    @{ Name = "anydesk"; PackageName = "com.anydesk.anydeskandroid"; Profile = "remote_anydesk"; Command = @("shell", "monkey", "-p", "com.anydesk.anydeskandroid", "1") },
    @{ Name = "teamviewer"; PackageName = "com.teamviewer.teamviewer.market.mobile"; Profile = "remote_teamviewer"; Command = @("shell", "monkey", "-p", "com.teamviewer.teamviewer.market.mobile", "1") },
    @{ Name = "teamviewer-quicksupport"; PackageName = "com.teamviewer.quicksupport.market"; Profile = "remote_teamviewer"; Command = @("shell", "monkey", "-p", "com.teamviewer.quicksupport.market", "1") }
)

foreach ($target in $Targets) {
    if (-not (Test-PackageInstalled $target.PackageName)) {
        Write-Host "Smoke: $($target.Name) skipped; package $($target.PackageName) is not installed"
        Add-SkippedResult $target.Name $target.PackageName $target.Profile "package_not_installed"
        continue
    }
    if ($null -eq $target.Command) {
        Write-Host "Smoke: $($target.Name) installed; provider package has no launch command"
        Add-SkippedResult $target.Name $target.PackageName $target.Profile "provider_package_no_launch"
        continue
    }
    Write-Host "Smoke: $($target.Name)"
    Invoke-AdbTarget @($target.Command)
    Start-Sleep -Seconds 2
    Invoke-AdbTarget shell ime set $Ime
    Save-State $target.Name $target.PackageName $target.Profile
}

$Report = [ordered]@{
    schemaVersion = 2
    generatedAt = (Get-Date).ToUniversalTime().ToString("o")
    device = $Device
    packageName = $Package
    ime = $Ime
    apk = $Apk
    evidenceLimits = @(
        "imeSelected and imeVisible only prove Android-side IME state.",
        "remote_* profileExpectation requires manual Windows delivery confirmation for shortcuts and IME toggle.",
        "remoteCompatibilityEvidence.localMatrixCommand exports local Android acceptance; pass/fail must be marked after checking the remote Windows session."
    )
    results = $Results
}
$ReportPath = Join-Path $CaptureDir "smoke-ime-apps-report.json"
$Report | ConvertTo-Json -Depth 6 | Set-Content -LiteralPath $ReportPath -Encoding UTF8

Write-Host "Smoke artifacts: $CaptureDir"
Write-Host "Smoke report: $ReportPath"
