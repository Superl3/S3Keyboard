param(
    [string]$CaptureDir = ""
)

$ErrorActionPreference = "Stop"

function Resolve-CaptureDir {
    param([string]$Requested)
    if ($Requested) {
        return (Resolve-Path $Requested).Path
    }
    $root = Join-Path (Resolve-Path (Join-Path $PSScriptRoot "..")) "captures\device-logs"
    $latest = Get-ChildItem $root -Directory -ErrorAction SilentlyContinue |
        Sort-Object LastWriteTime -Descending |
        Select-Object -First 1
    if (-not $latest) {
        throw "No capture directory found under $root"
    }
    return $latest.FullName
}

function Read-CaptureText {
    param([string]$Path)
    [byte[]]$bytes = [System.IO.File]::ReadAllBytes($Path)
    if ($bytes.Length -ge 2 -and $bytes[0] -eq 0xFF -and $bytes[1] -eq 0xFE) {
        $text = [System.Text.Encoding]::Unicode.GetString($bytes)
        return $text.TrimStart([char]0xFEFF)
    }
    if ($bytes.Length -ge 2 -and $bytes[0] -eq 0xFE -and $bytes[1] -eq 0xFF) {
        $text = [System.Text.Encoding]::BigEndianUnicode.GetString($bytes)
        return $text.TrimStart([char]0xFEFF)
    }
    if ($bytes.Length -ge 3 -and $bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF) {
        $text = [System.Text.Encoding]::UTF8.GetString($bytes, 3, $bytes.Length - 3)
        return $text.TrimStart([char]0xFEFF)
    }
    $text = [System.Text.Encoding]::UTF8.GetString($bytes)
    return $text.TrimStart([char]0xFEFF)
}

function Get-PrefString {
    param(
        [xml]$Prefs,
        [string]$Name
    )
    $node = $Prefs.map.string | Where-Object { $_.name -eq $Name } | Select-Object -First 1
    if (-not $node) {
        return ""
    }
    return [string]$node.InnerText
}

function Count-By {
    param(
        [object[]]$Items,
        [string]$Property
    )
    $Items |
        Where-Object { $_.$Property } |
        Group-Object -Property $Property |
        Sort-Object Count -Descending |
        ForEach-Object { "{0}={1}" -f $_.Name, $_.Count }
}

function Delete-Bursts {
    param([object[]]$Events)
    $bursts = New-Object System.Collections.Generic.List[int]
    $current = 0
    foreach ($event in $Events) {
        if ($event.type -eq "delete") {
            $current++
            continue
        }
        if ($current -gt 0) {
            $bursts.Add($current)
            $current = 0
        }
    }
    if ($current -gt 0) {
        $bursts.Add($current)
    }
    return $bursts.ToArray()
}

$dir = Resolve-CaptureDir $CaptureDir
$prefsPath = Join-Path $dir "keyboard_preferences.xml"
if (-not (Test-Path $prefsPath)) {
    throw "keyboard_preferences.xml not found in $dir"
}

[xml]$prefs = Read-CaptureText $prefsPath
$journalJson = Get-PrefString $prefs "typing_event_journal_v1"
$patternJson = Get-PrefString $prefs "typing_pattern_log"
$touchBias = Get-PrefString $prefs "touch_bias_stats"
$dingulProfile = Get-PrefString $prefs "dingul_touch_profile_v1"

$journal = if ($journalJson) { @($journalJson | ConvertFrom-Json) } else { @() }
$pattern = if ($patternJson) { @($patternJson | ConvertFrom-Json) } else { @() }
$labels = @($journal | Where-Object { $_.type -eq "label" })
$inputs = @($journal | Where-Object { $_.type -eq "input" })
$deletes = @($journal | Where-Object { $_.type -eq "delete" })
$bursts = Delete-Bursts $journal

Write-Host "Capture: $dir"
Write-Host "Journal events: input=$($inputs.Count) delete=$($deletes.Count) label=$($labels.Count)"
Write-Host "Label counts: $((Count-By $labels 'label') -join ', ')"
Write-Host "Input action counts: $((Count-By $inputs 'action') -join ', ')"
Write-Host "Delete bursts: $($bursts -join ', ')"
Write-Host "Pattern events: $($pattern.Count)"
Write-Host "Pattern types: $((Count-By $pattern 'type') -join ', ')"
Write-Host "Touch bias: $touchBias"
Write-Host "Dingul profile bytes: $($dingulProfile.Length)"
