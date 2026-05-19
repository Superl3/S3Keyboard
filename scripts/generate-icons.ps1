$ErrorActionPreference = "Stop"

$Root = Resolve-Path (Join-Path $PSScriptRoot "..")
$IconSource = Join-Path $Root "tools\icons\icons.json"
$DrawableDir = Join-Path $Root "app\src\main\res\drawable"

if (-not (Test-Path $IconSource)) {
    throw "Icon source not found: $IconSource"
}

New-Item -ItemType Directory -Force -Path $DrawableDir | Out-Null

$source = Get-Content -Raw -Encoding UTF8 $IconSource | ConvertFrom-Json
$viewport = [int] $source.viewport
$strokeWidth = [int] $source.defaultStrokeWidth
$generated = New-Object System.Collections.Generic.List[string]

foreach ($icon in $source.icons) {
    $name = [string] $icon.name
    if ($name -notmatch '^[a-z0-9_]+$') {
        throw "Invalid icon name: $name"
    }

    $pathTags = New-Object System.Collections.Generic.List[string]
    foreach ($pathData in $icon.paths) {
        $escapedPath = [System.Security.SecurityElement]::Escape([string] $pathData)
        $pathTags.Add("    <path android:pathData=""$escapedPath"" android:fillColor=""@android:color/transparent"" android:strokeColor=""#FF000000"" android:strokeWidth=""$strokeWidth"" android:strokeLineCap=""round"" android:strokeLineJoin=""round"" />")
    }

    $xml = @(
        "<?xml version=""1.0"" encoding=""utf-8""?>",
        "<vector xmlns:android=""http://schemas.android.com/apk/res/android""",
        "    android:width=""24dp""",
        "    android:height=""24dp""",
        "    android:viewportWidth=""$viewport""",
        "    android:viewportHeight=""$viewport"">",
        ($pathTags -join [Environment]::NewLine),
        "</vector>"
    ) -join [Environment]::NewLine

    $fileName = "ic_keyboard_$name.xml"
    $outputPath = Join-Path $DrawableDir $fileName
    Set-Content -Encoding UTF8 -Path $outputPath -Value $xml
    $generated.Add($fileName)
}

Write-Host "Generated $($generated.Count) icons:"
$generated | ForEach-Object { Write-Host " - $_" }
