param(
    [switch]$BuildAar,
    [switch]$DownloadModels
)

$ErrorActionPreference = "Stop"

. (Join-Path $PSScriptRoot "env.ps1")

$Root = Resolve-Path (Join-Path $PSScriptRoot "..")
$ExternalRoot = Join-Path $Root "external"
$LibraryDir = Join-Path $ExternalRoot "futo-swipe-library"
$ModelDir = Join-Path $ExternalRoot "futo-swipe-models"
$AarTarget = Join-Path $Root "app\libs\futo-swipe.aar"

New-Item -ItemType Directory -Force -Path $ExternalRoot | Out-Null
New-Item -ItemType Directory -Force -Path (Split-Path $AarTarget -Parent) | Out-Null

if (-not (Test-Path (Join-Path $LibraryDir ".git"))) {
    git clone --recursive https://gitlab.futo.org/keyboard/swipe-library.git $LibraryDir
} else {
    git -C $LibraryDir fetch --all --prune
    git -C $LibraryDir checkout master
    git -C $LibraryDir pull --ff-only
    git -C $LibraryDir submodule update --init --recursive
}

if ($BuildAar) {
    if (-not (Get-Command make -ErrorAction SilentlyContinue)) {
        throw "make was not found. Install a POSIX make environment, then rerun with -BuildAar."
    }
    Push-Location $LibraryDir
    try {
        make android
    } finally {
        Pop-Location
    }
    $Aar = Get-ChildItem -Path (Join-Path $LibraryDir "android\build\outputs\aar") -Filter "*.aar" |
        Sort-Object LastWriteTime -Descending |
        Select-Object -First 1
    if (-not $Aar) {
        throw "FUTO Android AAR was not found after build."
    }
    Copy-Item -Force $Aar.FullName $AarTarget
    Write-Host "Copied AAR to $AarTarget"
} else {
    Write-Host "Library checkout is ready at $LibraryDir"
    Write-Host "Run with -BuildAar after installing CMake 3.29+, Android NDK r27+, and make."
}

if ($DownloadModels) {
    if (-not (Test-Path (Join-Path $ModelDir ".git"))) {
        git clone https://huggingface.co/futo-org/futo-swipe $ModelDir
    } else {
        git -C $ModelDir pull --ff-only
    }
    Write-Host "Model files are available at $ModelDir"
}

Write-Host ""
Write-Host "Runtime model placement:"
Write-Host "  adb shell run-as com.superl3.s3keyboard mkdir -p files/futo-swipe"
Write-Host "  adb push/copy these directories under the app files/futo-swipe directory:"
Write-Host "    honorable_sturgeon\model_fp32.pte + metadata.json"
Write-Host "    magic_macaw\model_fp32.pte + metadata.json"
Write-Host "    hungry_jellyfish\context_lm.pte + metadata.json + vocab.txt"
Write-Host ""
Write-Host "The app falls back to the built-in heuristic swipe decoder when the AAR or model files are missing."
