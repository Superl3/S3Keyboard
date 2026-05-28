param(
    [string] $ThemeDir = "",
    [string] $OutputDir = ""
)

$ErrorActionPreference = "Stop"

$Root = Resolve-Path (Join-Path $PSScriptRoot "..")
if ([string]::IsNullOrWhiteSpace($ThemeDir)) {
    $ThemeDir = Join-Path $Root "themes"
}
if ([string]::IsNullOrWhiteSpace($OutputDir)) {
    $OutputDir = Join-Path $Root "captures\theme-previews"
}

$DefaultThemeDir = Join-Path $Root "themes"
if ([System.IO.Path]::GetFullPath($ThemeDir) -eq [System.IO.Path]::GetFullPath($DefaultThemeDir)) {
    & node (Join-Path $Root "tools\sync-themes.mjs") --check
}

Add-Type -AssemblyName System.Drawing
New-Item -ItemType Directory -Force -Path $OutputDir | Out-Null

function Convert-ThemeColor {
    param([object] $Value, [string] $Fallback)
    $text = if ($null -eq $Value -or [string]::IsNullOrWhiteSpace([string]$Value)) { $Fallback } else { [string]$Value }
    if (-not $text.StartsWith("#")) {
        $text = "#$text"
    }
    return [System.Drawing.ColorTranslator]::FromHtml($text)
}

function New-RoundRectPath {
    param([float] $X, [float] $Y, [float] $W, [float] $H, [float] $Radius)
    $path = [System.Drawing.Drawing2D.GraphicsPath]::new()
    $Radius = [Math]::Min($Radius, [Math]::Min($W, $H) / 2)
    if ($Radius -le 0) {
        $path.AddRectangle([System.Drawing.RectangleF]::new($X, $Y, $W, $H))
        return $path
    }

    $d = $Radius * 2
    $path.AddArc($X, $Y, $d, $d, 180, 90)
    $path.AddArc($X + $W - $d, $Y, $d, $d, 270, 90)
    $path.AddArc($X + $W - $d, $Y + $H - $d, $d, $d, 0, 90)
    $path.AddArc($X, $Y + $H - $d, $d, $d, 90, 90)
    $path.CloseFigure()
    return $path
}

function Draw-RoundRect {
    param(
        [System.Drawing.Graphics] $Graphics,
        [System.Drawing.Brush] $Brush,
        [System.Drawing.Pen] $Pen,
        [float] $X,
        [float] $Y,
        [float] $W,
        [float] $H,
        [float] $Radius
    )
    $path = New-RoundRectPath -X $X -Y $Y -W $W -H $H -Radius $Radius
    try {
        if ($null -ne $Brush) {
            $Graphics.FillPath($Brush, $path)
        }
        if ($null -ne $Pen) {
            $Graphics.DrawPath($Pen, $path)
        }
    } finally {
        $path.Dispose()
    }
}

function Draw-CenteredText {
    param(
        [System.Drawing.Graphics] $Graphics,
        [string] $Text,
        [System.Drawing.Font] $Font,
        [System.Drawing.Brush] $Brush,
        [float] $X,
        [float] $Y,
        [float] $W,
        [float] $H
    )
    $format = [System.Drawing.StringFormat]::new()
    try {
        $format.Alignment = [System.Drawing.StringAlignment]::Center
        $format.LineAlignment = [System.Drawing.StringAlignment]::Center
        $Graphics.DrawString($Text, $Font, $Brush, [System.Drawing.RectangleF]::new($X, $Y, $W, $H), $format)
    } finally {
        $format.Dispose()
    }
}

function Get-ThemeInt {
    param([object] $Value, [int] $Fallback)
    if ($null -eq $Value) {
        return $Fallback
    }
    return [int]$Value
}

function Get-ThemeBool {
    param([object] $Value, [bool] $Fallback)
    if ($null -eq $Value) {
        return $Fallback
    }
    return [System.Convert]::ToBoolean($Value)
}

function Blend-ThemeColor {
    param(
        [System.Drawing.Color] $Foreground,
        [System.Drawing.Color] $Background,
        [double] $ForegroundAmount
    )
    $amount = [Math]::Max(0.0, [Math]::Min(1.0, $ForegroundAmount))
    $inverse = 1.0 - $amount
    return [System.Drawing.Color]::FromArgb(
        255,
        [int][Math]::Round($Foreground.R * $amount + $Background.R * $inverse),
        [int][Math]::Round($Foreground.G * $amount + $Background.G * $inverse),
        [int][Math]::Round($Foreground.B * $amount + $Background.B * $inverse))
}

function Get-DimmedDepthColor {
    param([System.Drawing.Color] $Background)
    $luminance = ($Background.R * 299 + $Background.G * 587 + $Background.B * 114) / 1000.0
    if ($luminance -lt 42) {
        return Blend-ThemeColor `
                -Foreground ([System.Drawing.Color]::White) `
                -Background $Background `
                -ForegroundAmount 0.10
    }
    return Blend-ThemeColor `
            -Foreground ([System.Drawing.Color]::Black) `
            -Background $Background `
            -ForegroundAmount 0.16
}

function Get-ThemeDepthColor {
    param([object] $Theme, [System.Drawing.Color] $Fill)
    if ($null -ne $Theme.colors.depth -and -not [string]::IsNullOrWhiteSpace([string]$Theme.colors.depth)) {
        return Convert-ThemeColor $Theme.colors.depth "#696969"
    }
    return Get-DimmedDepthColor -Background $Fill
}

function Get-KeyFaceGradientEffect {
    param([object] $Theme)
    if ($null -eq $Theme.effects) {
        return $null
    }
    if ($null -ne $Theme.effects.keyFaceGradient) {
        return $Theme.effects.keyFaceGradient
    }
    return $Theme.effects.keyGradient
}

function Get-KeyFaceGradientEnabled {
    param([object] $Theme)
    $effect = Get-KeyFaceGradientEffect -Theme $Theme
    if ($null -eq $effect) {
        return $true
    }
    return Get-ThemeBool $effect.enabled $true
}

function Get-KeyFaceGradientStrength {
    param([object] $Theme)
    $effect = Get-KeyFaceGradientEffect -Theme $Theme
    if ($null -eq $effect) {
        return 22
    }
    return [Math]::Max(0, [Math]::Min(100, (Get-ThemeInt $effect.strengthPercent 22)))
}

function Get-KeyFaceGradientStartColor {
    param([object] $Theme)
    $effect = Get-KeyFaceGradientEffect -Theme $Theme
    if ($null -eq $effect) {
        return [System.Drawing.Color]::White
    }
    return Convert-ThemeColor $effect.startColor "#FFFFFF"
}

function Get-KeyFaceGradientEndColor {
    param([object] $Theme)
    $effect = Get-KeyFaceGradientEffect -Theme $Theme
    if ($null -eq $effect) {
        return [System.Drawing.Color]::Black
    }
    return Convert-ThemeColor $effect.endColor "#000000"
}

function Get-KeyFaceGradientCurve {
    param([object] $Theme)
    $effect = Get-KeyFaceGradientEffect -Theme $Theme
    if ($null -eq $effect -or [string]::IsNullOrWhiteSpace([string]$effect.curve)) {
        return "soft"
    }
    $curve = [string]$effect.curve
    if ($curve -eq "linear" -or $curve -eq "soft" -or $curve -eq "top_glow" -or $curve -eq "bottom_shade") {
        return $curve
    }
    return "soft"
}

function Get-KeyFaceGradientPositions {
    param([object] $Theme)
    switch (Get-KeyFaceGradientCurve -Theme $Theme) {
        "linear" { return [single[]](0.0, 0.50, 1.0) }
        "top_glow" { return [single[]](0.0, 0.30, 1.0) }
        "bottom_shade" { return [single[]](0.0, 0.62, 1.0) }
        default { return [single[]](0.0, 0.42, 1.0) }
    }
}

function Get-KeyFaceGradientColors {
    param(
        [System.Drawing.Color] $Background,
        [int] $StrengthPercent,
        [System.Drawing.Color] $StartColor,
        [System.Drawing.Color] $EndColor
    )
    $luminance = ($Background.R * 299 + $Background.G * 587 + $Background.B * 114) / 1000.0
    $strength = [Math]::Max(0.0, [Math]::Min(1.0, $StrengthPercent / 100.0))
    $topAmount = $(if ($luminance -lt 42) { 0.08 } else { 0.06 }) + 0.24 * $strength
    $bottomAmount = $(if ($luminance -lt 42) { 0.04 } else { 0.05 }) + 0.18 * $strength
    return @(
        (Blend-ThemeColor -Foreground $StartColor -Background $Background -ForegroundAmount $topAmount),
        $Background,
        (Blend-ThemeColor -Foreground $EndColor -Background $Background -ForegroundAmount $bottomAmount)
    )
}

function New-KeyFaceBrush {
    param(
        [object] $Theme,
        [System.Drawing.Color] $Fill,
        [float] $X,
        [float] $Y,
        [float] $W,
        [float] $H
    )
    $depthEnabled = Get-ThemeBool $Theme.shape.depthEnabled $false
    $depthDp = Get-ThemeInt $Theme.shape.depthDp 0
    $gradientEnabled = Get-KeyFaceGradientEnabled -Theme $Theme
    $strength = Get-KeyFaceGradientStrength -Theme $Theme
    if (-not $depthEnabled -or $depthDp -le 0 -or -not $gradientEnabled -or $strength -le 0) {
        return [System.Drawing.SolidBrush]::new($Fill)
    }

    $colors = Get-KeyFaceGradientColors `
            -Background $Fill `
            -StrengthPercent $strength `
            -StartColor (Get-KeyFaceGradientStartColor -Theme $Theme) `
            -EndColor (Get-KeyFaceGradientEndColor -Theme $Theme)
    $rect = [System.Drawing.RectangleF]::new($X, $Y, [Math]::Max(1, $W), [Math]::Max(1, $H))
    $brush = [System.Drawing.Drawing2D.LinearGradientBrush]::new(
            $rect,
            $colors[0],
            $colors[2],
            [System.Drawing.Drawing2D.LinearGradientMode]::Vertical)
    $blend = [System.Drawing.Drawing2D.ColorBlend]::new(3)
    $blend.Positions = Get-KeyFaceGradientPositions -Theme $Theme
    $blend.Colors = [System.Drawing.Color[]]($colors[0], $colors[1], $colors[2])
    $brush.InterpolationColors = $blend
    return $brush
}

function New-PanelBackgroundBrush {
    param(
        [object] $Theme,
        [float] $X,
        [float] $Y,
        [float] $W,
        [float] $H
    )
    $panelColor = if ($null -ne $Theme.colors.panelBackground) { $Theme.colors.panelBackground } else { $Theme.colors.keyboardBackground }
    $fallback = Convert-ThemeColor $panelColor "#EBEBEB"
    $gradient = if ($null -ne $Theme.effects) { $Theme.effects.panelGradient } else { $null }
    if ($null -eq $gradient -or -not (Get-ThemeBool $gradient.enabled $false)) {
        return [System.Drawing.SolidBrush]::new($fallback)
    }
    $start = Convert-ThemeColor $gradient.startColor $panelColor
    $end = Convert-ThemeColor $gradient.endColor $panelColor
    $rect = [System.Drawing.RectangleF]::new($X, $Y, [Math]::Max(1, $W), [Math]::Max(1, $H))
    return [System.Drawing.Drawing2D.LinearGradientBrush]::new(
            $rect,
            $start,
            $end,
            [System.Drawing.Drawing2D.LinearGradientMode]::Vertical)
}

function Get-FontFamilyName {
    param([object] $Theme)
    $fontFamily = if ($null -ne $Theme.typography) { [string]$Theme.typography.fontFamily } else { "default" }
    switch ($fontFamily) {
        "noto_serif_kr" { return "Georgia" }
        "d2coding" { return "Consolas" }
        default { return "Segoe UI" }
    }
}

function Get-FontStyle {
    param([bool] $Bold, [bool] $Italic)
    $style = [System.Drawing.FontStyle]::Regular
    if ($Bold) {
        $style = [System.Drawing.FontStyle]([int]$style -bor [int][System.Drawing.FontStyle]::Bold)
    }
    if ($Italic) {
        $style = [System.Drawing.FontStyle]([int]$style -bor [int][System.Drawing.FontStyle]::Italic)
    }
    return $style
}

function New-ThemeFont {
    param([object] $Theme, [float] $BaseSize, [bool] $Primary = $true)
    $typography = $Theme.typography
    if ($Primary) {
        $scale = (Get-ThemeInt $typography.primaryTextSizePercent 100) / 100.0
        $bold = Get-ThemeBool $typography.primaryTextBold $false
        $italic = Get-ThemeBool $typography.primaryTextItalic $false
    } else {
        $scale = (Get-ThemeInt $typography.secondaryTextSizePercent 100) / 100.0
        $bold = $true
        $italic = Get-ThemeBool $typography.secondaryTextItalic $false
    }
    return [System.Drawing.Font]::new(
        (Get-FontFamilyName $Theme),
        $BaseSize * $scale,
        (Get-FontStyle -Bold $bold -Italic $italic),
        [System.Drawing.GraphicsUnit]::Point)
}

function Get-RoleColor {
    param([object] $Theme, [string] $Role)
    switch ($Role) {
        "modifier" { return Convert-ThemeColor $Theme.colors.modifierKey "#E7EAF0" }
        "accent" { return Convert-ThemeColor $Theme.colors.accentKey "#EAF1FF" }
        "pressed" { return Convert-ThemeColor $Theme.colors.keyPressed "#B2B2B2" }
        default { return Convert-ThemeColor $Theme.colors.alphaKey "#F8F8F8" }
    }
}

function Get-RoleTextColor {
    param([object] $Theme, [string] $Role)
    switch ($Role) {
        "normal" { return Convert-ThemeColor $Theme.colors.accent "#232323" }
        "pressed" { return Convert-ThemeColor $Theme.colors.accent "#232323" }
        "accent" { return Get-AccentTextColor -Theme $Theme }
        "modifier" { return Convert-ThemeColor $Theme.colors.secondary "#5F6368" }
        default { return Convert-ThemeColor $Theme.colors.secondary "#5F6368" }
    }
}

function Get-AccentTextColor {
    param([object] $Theme)
    if ($null -ne $Theme.dingulColors -and $null -ne $Theme.dingulColors.modInv -and
            -not [string]::IsNullOrWhiteSpace([string]$Theme.dingulColors.modInv.foreground)) {
        return Convert-ThemeColor $Theme.dingulColors.modInv.foreground $Theme.colors.accent
    }
    return Convert-ThemeColor $Theme.colors.accent "#232323"
}

function Get-NumberRowRole {
    param([object] $Theme, [string] $Label)
    $mode = if ($null -ne $Theme.additionalNumberRow) { [string]$Theme.additionalNumberRow.colorMode } else { "full_mod" }
    $inner = $Label -ge "4" -and $Label -le "7"
    switch ($mode) {
        "full_alpha" { return "normal" }
        "half_mod_4567" { if ($inner) { return "modifier" } return "normal" }
        "alpha_accent" { if ($inner) { return "accent" } return "normal" }
        "mod_alpha" { if ($inner) { return "normal" } return "modifier" }
        "mod_accent" { if ($inner) { return "accent" } return "modifier" }
        "accent_alpha" { if ($inner) { return "normal" } return "accent" }
        "accent_mod" { if ($inner) { return "modifier" } return "accent" }
        "full_accent" { return "accent" }
        default { return "modifier" }
    }
}

function Get-AccentPolicyTargets {
    param([object] $Theme, [string] $Layout)
    if ($null -eq $Theme.accentPolicy) {
        return Get-ImplicitAccentPolicyTargets -Theme $Theme -Layout $Layout
    }
    $property = $Theme.accentPolicy.PSObject.Properties |
            Where-Object { $_.Name -eq $Layout } |
            Select-Object -First 1
    if ($null -eq $property -or $null -eq $property.Value) {
        return Get-ImplicitAccentPolicyTargets -Theme $Theme -Layout $Layout
    }
    if ($property.Value -is [System.Array]) {
        $targets = @($property.Value | ForEach-Object { [string]$_ })
        if ($targets.Count -eq 0) {
            return Get-ImplicitAccentPolicyTargets -Theme $Theme -Layout $Layout
        }
        return $targets
    }
    return @([string]$property.Value)
}

function Get-ImplicitAccentPolicyTargets {
    param([object] $Theme, [string] $Layout)
    if (-not (Test-ImplicitAccentPolicy -Theme $Theme)) {
        return @()
    }
    if ($Layout -eq "dingul") {
        return @("modEnter", "modShift")
    }
    return @("modCtrl")
}

function Test-ImplicitAccentPolicy {
    param([object] $Theme)
    if ($null -eq $Theme.colors) {
        return $false
    }
    $accent = Convert-ThemeColor $Theme.colors.accentKey "#000000"
    $baseColors = @(
        (Convert-ThemeColor $Theme.colors.alphaKey "#000000"),
        (Convert-ThemeColor $Theme.colors.modifierKey "#000000")
    )
    foreach ($base in $baseColors) {
        if ((Get-ColorDistance -Left $accent -Right $base) -lt 48) {
            return $false
        }
    }
    return $true
}

function Get-ColorDistance {
    param([System.Drawing.Color] $Left, [System.Drawing.Color] $Right)
    return [Math]::Sqrt(
        [Math]::Pow($Left.R - $Right.R, 2) +
        [Math]::Pow($Left.G - $Right.G, 2) +
        [Math]::Pow($Left.B - $Right.B, 2))
}

function Test-AccentPolicyTarget {
    param([object] $Theme, [string] $Layout, [string] $Target)
    if ([string]::IsNullOrWhiteSpace($Target)) {
        return $false
    }
    return (Get-AccentPolicyTargets -Theme $Theme -Layout $Layout) -contains $Target
}

function Get-SemanticTargetForPreviewKey {
    param([string] $Layout, [string] $Label)
    $normalized = $Label.ToLowerInvariant()
    switch ($normalized) {
        "1" { return "escPoint" }
        "settings" { return "settingsEnter" }
        "options" { return "settingsEnter" }
        "enter" { return "enter" }
        "reserved" { return "modMeta" }
        "res" { return "modMeta" }
        "language" { return "modMeta" }
        "lang" { return "modMeta" }
        "shift" { return "qwertyShift" }
        "bksp" { return "backspace" }
        "backspace" { return "backspace" }
    }
    if ($Layout -eq "dingul") {
        if ($Label -eq ".") { return "modEnter" }
        if ($Label -eq "/") { return "modShift" }
        if ($Label -eq "?") { return "question" }
    }
    return ""
}

function Get-AlternateAccentPolicyTargets {
    param([string] $Target)
    switch ($Target) {
        "settingsEnter" { return @("settingsEnter", "modCtrl") }
        "enter" { return @("enter", "settingsEnter", "modCtrl") }
        "qwertyShift" { return @("qwertyShift", "shift", "modCommand") }
        "backspace" { return @("backspace", "modCommand") }
        "modEnter" { return @("dingulDot", "modEnter") }
        "modShift" { return @("dingulSlash", "modShift") }
        default { return @($Target) }
    }
}

function Get-SpacebarPreviewRole {
    param([object] $Theme, [string] $BaseRole)
    if ($null -eq $Theme.accentPolicy) {
        return $BaseRole
    }
    $role = [string]$Theme.accentPolicy.spacebar
    if ([string]::IsNullOrWhiteSpace($role)) {
        $role = [string]$Theme.accentPolicy.space
    }
    switch ($role) {
        "accent" { return "accent" }
        "mod" { return "modifier" }
        "modifier" { return "modifier" }
        "modifiers" { return "modifier" }
        default { return $BaseRole }
    }
}

function Get-QuestionPreviewRole {
    param([object] $Theme, [string] $BaseRole)
    if ($null -eq $Theme.accentPolicy) {
        return $BaseRole
    }
    $role = [string]$Theme.accentPolicy.question
    if ([string]::IsNullOrWhiteSpace($role)) {
        $role = [string]$Theme.accentPolicy.questionMark
    }
    switch ($role) {
        "accent" { return "accent" }
        "mod" { return "modifier" }
        "modifier" { return "modifier" }
        "modifiers" { return "modifier" }
        "alpha" { return "normal" }
        default { return $BaseRole }
    }
}

function Resolve-PreviewRole {
    param([object] $Theme, [string] $Layout, [string] $Label, [string] $BaseRole)
    if ($BaseRole -eq "pressed") {
        return $BaseRole
    }
    if ($Label.ToLowerInvariant() -eq "space") {
        return Get-SpacebarPreviewRole -Theme $Theme -BaseRole $BaseRole
    }
    if ($Layout -eq "dingul" -and $Label -eq "?") {
        return Get-QuestionPreviewRole -Theme $Theme -BaseRole $BaseRole
    }
    $target = Get-SemanticTargetForPreviewKey -Layout $Layout -Label $Label
    foreach ($candidate in (Get-AlternateAccentPolicyTargets -Target $target)) {
        if ((Test-AccentPolicyTarget -Theme $Theme -Layout $Layout -Target $candidate) -or
                ($candidate -in @("modEnter", "modShift") -and (Test-AccentPolicyTarget -Theme $Theme -Layout $Layout -Target "punctuation"))) {
            return "accent"
        }
    }
    return $BaseRole
}

function Get-KeyOverrideColor {
    param([object] $Theme, [string] $Label)
    $overrides = $Theme.keyTextColorOverrides
    if ($null -eq $overrides) {
        $overrides = $Theme.keyColorOverrides
    }
    if ($null -eq $overrides -or [string]::IsNullOrWhiteSpace($Label)) {
        return $null
    }
    $candidates = Get-OverrideCandidatesForLabel -Label $Label
    foreach ($candidate in $candidates) {
        $property = $overrides.PSObject.Properties |
                Where-Object { ($_.Name.ToLowerInvariant() -replace "\s+", "") -eq $candidate } |
                Select-Object -First 1
        if ($null -ne $property) {
            return Convert-ThemeColor $property.Value "#F8F8F8"
        }
    }
    if ((Test-AlphaPreviewLabel -Label $Label) -and $null -ne $overrides.alpha) {
        return Convert-ThemeColor $overrides.alpha "#F8F8F8"
    }
    return $null
}

function Get-KeyBackgroundOverrideColor {
    param([object] $Theme, [string] $Label)
    $overrides = $Theme.keyBackgroundColorOverrides
    if ($null -eq $overrides -or [string]::IsNullOrWhiteSpace($Label)) {
        return $null
    }
    $candidates = Get-OverrideCandidatesForLabel -Label $Label
    foreach ($candidate in $candidates) {
        $property = $overrides.PSObject.Properties |
                Where-Object { ($_.Name.ToLowerInvariant() -replace "\s+", "") -eq $candidate } |
                Select-Object -First 1
        if ($null -ne $property) {
            return Convert-ThemeColor $property.Value "#F8F8F8"
        }
    }
    if ((Test-AlphaPreviewLabel -Label $Label) -and $null -ne $overrides.alpha) {
        return Convert-ThemeColor $overrides.alpha "#F8F8F8"
    }
    return $null
}

function Get-OverrideCandidatesForLabel {
    param([string] $Label)
    $normalizedLabel = $Label.ToLowerInvariant() -replace "\s+", ""
    $semantic = switch ($normalizedLabel) {
        "1" { "escpoint" }
        "bksp" { "backspace" }
        "opt" { "options" }
        "res" { "reserved" }
        "lang" { "language" }
        "enter" { "enter" }
        "." { "dinguldot" }
        "/" { "dingulslash" }
        default { $normalizedLabel }
    }
    return @(
        $normalizedLabel,
        $semantic,
        "label:$normalizedLabel",
        "label:$semantic",
        "tap:$normalizedLabel",
        "tap:$semantic"
    ) | Select-Object -Unique
}

function Get-PreviewIconName {
    param([string] $Label)
    switch ($Label.ToLowerInvariant()) {
        "shift" { return "shift" }
        "bksp" { return "backspace" }
        "backspace" { return "backspace" }
        "opt" { return "options" }
        "options" { return "options" }
        "res" { return "reserved" }
        "reserved" { return "reserved" }
        "space" { return "space" }
        "lang" { return "language" }
        "language" { return "language" }
        "settings" { return "settings" }
        "enter" { return "enter" }
        default { return "" }
    }
}

function Test-DotLegendLabel {
    param([object] $Theme, [string] $Label)
    $display = $Theme.keyDisplayOverrides
    if ($null -ne $display -and $null -ne $display.alpha) {
        return $display.alpha.type -eq "icon" `
                -and $display.alpha.value -eq "dot" `
                -and (Test-AlphaPreviewLabel -Label $Label)
    }
    return $null -ne $Theme.legendStyle `
            -and $Theme.legendStyle.preset -eq "dots" `
            -and [string]::IsNullOrWhiteSpace((Get-PreviewIconName $Label))
}

function Test-AlphaPreviewLabel {
    param([string] $Label)
    return $Label -match '^[A-Za-z]$' `
            -or $Label -match '^[0-9]$' `
            -or $Label -match '^[\u3131-\u318E\uAC00-\uD7A3]$' `
            -or $Label -eq "?" `
            -or $Label -eq "." `
            -or $Label -eq "/" `
            -or $Label -eq ".." `
            -or $Label -eq ". ." `
            -or $Label -eq ([string][char]0x3163 + ".") `
            -or $Label -eq ([string][char]0x3161 + [string][char]0x3150)
}

function Test-SimpleTextPack {
    param([string] $PackId)
    return $PackId -eq "simple-text" -or $PackId -eq "olivia-script-text"
}

function Test-GitCommandPack {
    param([string] $PackId)
    return $PackId -eq "git-commands"
}

function Test-PointDisplayPack {
    param([string] $PackId)
    return $PackId -eq "geo-points" `
            -or $PackId -eq "soft-symbols" `
            -or $PackId -eq "terminal-points" `
            -or $PackId -eq "punctuation-points" `
            -or $PackId -eq "full-decorative" `
            -or $PackId -eq "keyboard-symbols" `
            -or $PackId -eq "keyboard-navigation" `
            -or $PackId -eq "gmk-style-points" `
            -or $PackId -eq "gmk-style-novelties" `
            -or $PackId -eq "gmk-style-macros" `
            -or $PackId -eq "gmk-style-celestial" `
            -or $PackId -eq "gmk-style-nature" `
            -or $PackId -eq "gmk-style-spacebars" `
            -or $PackId -eq "font-symbols" `
            -or $PackId -eq "image-mask-marks" `
            -or $PackId -eq "tall-mod-glyphs" `
            -or $PackId -eq "mixed-source-novelties"
}

function Get-PointDisplayPackGlyph {
    param([string] $PackId, [string] $Name, [bool] $IsLabel)
    $normalized = if ([string]::IsNullOrWhiteSpace($PackId)) { "none" } else { $PackId }
    if ($IsLabel -and $normalized -eq "full-decorative" -and (Test-AlphaPreviewLabel -Label $Name)) {
        return "dot"
    }
    switch ($normalized) {
        "geo-points" {
            switch ($Name) {
                "enter" { return "spark" }
                "backspace" { return "chevron_left" }
                "shift" { return "chevron_up" }
                "space" { return "space_dots" }
                "language" { return "orbit" }
                "options" { return "grid_4" }
                "settings" { return "gear_dot" }
                "reserved" { return "bookmark_dot" }
                "." { return "two_dots" }
                "/" { return "slash_dot" }
                "?" { return "ring" }
            }
        }
        "soft-symbols" {
            switch ($Name) {
                "enter" { return "ring" }
                "backspace" { return "cross" }
                "shift" { return "plus" }
                "space" { return "space_dots" }
                "language" { return "ring" }
                "options" { return "grid_4" }
                "settings" { return "square" }
                "reserved" { return "diamond" }
                "." { return "two_dots" }
                "/" { return "slash_dot" }
                "?" { return "diamond" }
            }
        }
        "terminal-points" {
            switch ($Name) {
                "enter" { return "terminal" }
                "backspace" { return "cross" }
                "shift" { return "chevron_up" }
                "space" { return "cursor" }
                "language" { return "orbit" }
                "options" { return "terminal" }
                "settings" { return "grid_4" }
                "reserved" { return "square" }
                "." { return "two_dots" }
                "/" { return "slash_dot" }
                "?" { return "cursor" }
            }
        }
        "punctuation-points" {
            switch ($Name) {
                "enter" { return "spark" }
                "backspace" { return "chevron_left" }
                "." { return "two_dots" }
                "/" { return "slash_dot" }
                "?" { return "ring" }
            }
        }
        "full-decorative" {
            return Get-PointDisplayPackGlyph -PackId "geo-points" -Name $Name -IsLabel $IsLabel
        }
        "keyboard-symbols" {
            switch ($Name) {
                "enter" { return "keyboard_return" }
                "backspace" { return "keyboard_backspace" }
                "shift" { return "keyboard_capslock" }
                "space" { return "keyboard_space" }
                "language" { return "keyboard_language" }
                "options" { return "keyboard_option" }
                "settings" { return "keyboard_command" }
                "reserved" { return "keyboard_control" }
                "." { return "two_dots" }
                "/" { return "slash_dot" }
                "?" { return "keyboard_keys" }
            }
        }
        "keyboard-navigation" {
            switch ($Name) {
                "enter" { return "keyboard_return" }
                "backspace" { return "keyboard_arrow_left" }
                "shift" { return "keyboard_arrow_up" }
                "space" { return "keyboard_space" }
                "language" { return "keyboard_language" }
                "options" { return "keyboard_double_left" }
                "settings" { return "keyboard_double_right" }
                "reserved" { return "keyboard_control" }
                "." { return "keyboard_arrow_down" }
                "/" { return "keyboard_arrow_up" }
                "?" { return "keyboard_tab" }
            }
        }
        "gmk-style-points" {
            switch ($Name) {
                "enter" { return "gmk_accent_bar" }
                "backspace" { return "gmk_accent_corner" }
                "shift" { return "gmk_accent_stripe" }
                "space" { return "gmk_space_dash" }
                "language" { return "gmk_orbit_star" }
                "options" { return "gmk_macro_stack" }
                "settings" { return "gmk_target" }
                "reserved" { return "gmk_diamond_cluster" }
                "." { return "gmk_triple_dot" }
                "/" { return "gmk_twin_ticks" }
                "?" { return "gmk_target" }
            }
        }
        "gmk-style-novelties" {
            switch ($Name) {
                "enter" { return "gmk_sun" }
                "backspace" { return "gmk_moon" }
                "shift" { return "gmk_mountain" }
                "space" { return "gmk_space_dash" }
                "language" { return "gmk_leaf" }
                "options" { return "gmk_flower" }
                "settings" { return "gmk_orbit_star" }
                "reserved" { return "gmk_droplet" }
                "." { return "gmk_triple_dot" }
                "/" { return "gmk_wave" }
                "?" { return "gmk_flower" }
            }
        }
        "gmk-style-macros" {
            switch ($Name) {
                "enter" { return "gmk_macro_brackets" }
                "backspace" { return "gmk_pulse" }
                "shift" { return "gmk_macro_stack" }
                "space" { return "gmk_space_dash" }
                "language" { return "gmk_orbit_star" }
                "options" { return "gmk_macro_stack" }
                "settings" { return "gmk_target" }
                "reserved" { return "gmk_pixel_steps" }
                "." { return "gmk_triple_dot" }
                "/" { return "gmk_accent_stripe" }
                "?" { return "gmk_macro_brackets" }
            }
        }
        "gmk-style-celestial" {
            switch ($Name) {
                "enter" { return "gmk_planet_ring" }
                "backspace" { return "gmk_crescent_star" }
                "shift" { return "gmk_constellation" }
                "space" { return "gmk_space_dash" }
                "language" { return "gmk_orbit_star" }
                "options" { return "gmk_sparkle_pair" }
                "settings" { return "gmk_compass" }
                "reserved" { return "gmk_snow" }
                "." { return "gmk_triple_dot" }
                "/" { return "gmk_comet_tail" }
                "?" { return "gmk_planet_ring" }
            }
        }
        "gmk-style-nature" {
            switch ($Name) {
                "enter" { return "gmk_flower_alt" }
                "backspace" { return "gmk_cloud" }
                "shift" { return "gmk_sprout" }
                "space" { return "gmk_wave_double" }
                "language" { return "gmk_leaf" }
                "options" { return "gmk_petals" }
                "settings" { return "gmk_rain" }
                "reserved" { return "gmk_flame" }
                "." { return "gmk_droplet" }
                "/" { return "gmk_wave_double" }
                "?" { return "gmk_sprout" }
            }
        }
        "gmk-style-spacebars" {
            switch ($Name) {
                "enter" { return "gmk_iso_enter_mark" }
                "backspace" { return "gmk_side_stripes" }
                "shift" { return "gmk_stepped_bar" }
                "space" { return "gmk_split_bar" }
                "language" { return "gmk_corner_dots" }
                "options" { return "gmk_equalizer" }
                "settings" { return "gmk_rising_blocks" }
                "reserved" { return "gmk_arcade_diamond" }
                "." { return "gmk_dot_matrix" }
                "/" { return "gmk_center_cross" }
                "?" { return "gmk_lab_flask" }
            }
        }
        "font-symbols" {
            switch ($Name) {
                "enter" { return "font_return_arrow" }
                "backspace" { return "font_delete_left" }
                "shift" { return "font_shift_arrow" }
                "space" { return "font_keyboard" }
                "language" { return "font_command" }
                "options" { return "font_option" }
                "settings" { return "font_control" }
                "reserved" { return "font_escape" }
                "." { return "font_star_outline" }
                "/" { return "font_triangle_up" }
                "?" { return "font_power" }
            }
        }
        "image-mask-marks" {
            switch ($Name) {
                "enter" { return "img_arc_gate" }
                "backspace" { return "img_side_notch" }
                "shift" { return "img_flag_tab" }
                "space" { return "img_horizon_bars" }
                "language" { return "img_tall_orbit" }
                "options" { return "img_punch_card" }
                "settings" { return "img_ladder" }
                "reserved" { return "img_ticket" }
                "." { return "img_capsule_dots" }
                "/" { return "img_wave_tile" }
                "?" { return "img_blob_star" }
            }
        }
        "tall-mod-glyphs" {
            switch ($Name) {
                "enter" { return "img_arc_gate" }
                "backspace" { return "img_tall_bracket" }
                "shift" { return "img_vertical_ribbon" }
                "space" { return "img_horizon_bars" }
                "language" { return "img_dual_posts" }
                "options" { return "img_stacked_tiles" }
                "settings" { return "img_corner_frame" }
                "reserved" { return "img_tall_capsule" }
                "." { return "img_diamond_stack" }
                "/" { return "img_soft_cross" }
                "?" { return "img_pin_drop" }
            }
        }
        "mixed-source-novelties" {
            switch ($Name) {
                "enter" { return "font_return_arrow" }
                "backspace" { return "font_delete_left" }
                "shift" { return "font_shift_arrow" }
                "space" { return "img_horizon_bars" }
                "language" { return "img_tall_orbit" }
                "options" { return "img_punch_card" }
                "settings" { return "font_home" }
                "reserved" { return "img_blob_star" }
                "." { return "font_star_solid" }
                "/" { return "img_leaf_slab" }
                "?" { return "font_eject" }
            }
        }
    }
    return ""
}

function Get-ModifierPackId {
    param([object] $Theme)
    if ($null -ne $Theme.icons -and -not [string]::IsNullOrWhiteSpace([string]$Theme.icons.modifierPackId)) {
        if ((Test-SimpleTextPack -PackId ([string]$Theme.icons.modifierPackId))) {
            return "line-mono"
        }
        return [string]$Theme.icons.modifierPackId
    }
    return "line-mono"
}

function Get-KeyDisplayPackId {
    param([object] $Theme)
    if ($null -ne $Theme.icons -and -not [string]::IsNullOrWhiteSpace([string]$Theme.icons.keyDisplayPackId)) {
        return [string]$Theme.icons.keyDisplayPackId
    }
    if ($null -ne $Theme.icons -and (Test-SimpleTextPack -PackId ([string]$Theme.icons.modifierPackId))) {
        return "simple-text"
    }
    return "none"
}

function Get-MetropolisPreviewColor {
    param([string] $Icon)
    switch ($Icon) {
        "options" { return [System.Drawing.Color]::FromArgb(255, 255, 176, 0) }
        "shift" { return [System.Drawing.Color]::FromArgb(255, 255, 75, 62) }
        "reserved" { return [System.Drawing.Color]::FromArgb(255, 255, 75, 62) }
        "backspace" { return [System.Drawing.Color]::FromArgb(255, 255, 176, 0) }
        "enter" { return [System.Drawing.Color]::FromArgb(255, 102, 227, 196) }
        "language" { return [System.Drawing.Color]::FromArgb(255, 102, 227, 196) }
        "settings" { return [System.Drawing.Color]::FromArgb(255, 102, 227, 196) }
        default { return [System.Drawing.Color]::FromArgb(255, 112, 215, 232) }
    }
}

function Draw-KeyDisplayPackPreview {
    param(
        [System.Drawing.Graphics] $Graphics,
        [object] $Theme,
        [string] $Icon,
        [System.Drawing.Color] $Color,
        [float] $X,
        [float] $Y,
        [float] $W,
        [float] $H
    )
    $packId = Get-KeyDisplayPackId -Theme $Theme
    if (-not (Test-SimpleTextPack -PackId $packId) `
            -and -not (Test-GitCommandPack -PackId $packId) `
            -and -not (Test-PointDisplayPack -PackId $packId)) {
        return $false
    }
    if (Test-PointDisplayPack -PackId $packId) {
        $glyph = Get-PointDisplayPackGlyph -PackId $packId -Name $Icon -IsLabel $false
        return Draw-PointGlyphPreview -Graphics $Graphics -Glyph $glyph -Color $Color -X $X -Y $Y -W $W -H $H
    }
    if (Test-GitCommandPack -PackId $packId) {
        $text = switch ($Icon) {
            "enter" { "exec" }
            "backspace" { "reset" }
            "shift" { "rebase" }
            "space" { "pull" }
            "language" { "fetch" }
            "options" { "stash" }
            "settings" { "config" }
            "reserved" { "commit" }
            default { "" }
        }
    } else {
        $text = ""
    }
    if ([string]::IsNullOrWhiteSpace($text)) {
        return $false
    }
    $brush = [System.Drawing.SolidBrush]::new($Color)
    try {
        if ((Test-SimpleTextPack -PackId $packId) -and $text -eq "hihihi") {
            Draw-HihihiPreviewGlyph -Graphics $Graphics -Color $Color -X $X -Y $Y -W $W -H $H
        } else {
            $font = [System.Drawing.Font]::new("Segoe UI", [Math]::Max(8, $H * 0.28), [System.Drawing.FontStyle]::Bold, [System.Drawing.GraphicsUnit]::Pixel)
            try {
                Draw-CenteredText -Graphics $Graphics -Text $text -Font $font -Brush $brush -X $X -Y $Y -W $W -H $H
            } finally {
                $font.Dispose()
            }
        }
    } finally {
        $brush.Dispose()
    }
    return $true
}

function Draw-LabelDisplayPackPreview {
    param(
        [System.Drawing.Graphics] $Graphics,
        [object] $Theme,
        [string] $Label,
        [System.Drawing.Color] $Color,
        [float] $X,
        [float] $Y,
        [float] $W,
        [float] $H
    )
    $packId = Get-KeyDisplayPackId -Theme $Theme
    if (-not (Test-SimpleTextPack -PackId $packId) `
            -and -not (Test-GitCommandPack -PackId $packId) `
            -and -not (Test-PointDisplayPack -PackId $packId)) {
        return $false
    }
    if (Test-PointDisplayPack -PackId $packId) {
        $glyph = Get-PointDisplayPackGlyph -PackId $packId -Name $Label -IsLabel $true
        return Draw-PointGlyphPreview -Graphics $Graphics -Glyph $glyph -Color $Color -X $X -Y $Y -W $W -H $H
    }
    if (Test-GitCommandPack -PackId $packId) {
        $text = switch ($Label) {
            "." { "diff" }
            "/" { "log" }
            default { "" }
        }
        if ([string]::IsNullOrWhiteSpace($text)) {
            return $false
        }
        $brush = [System.Drawing.SolidBrush]::new($Color)
        try {
            $font = [System.Drawing.Font]::new("Segoe UI", [Math]::Max(8, $H * 0.28), [System.Drawing.FontStyle]::Bold, [System.Drawing.GraphicsUnit]::Pixel)
            try {
                Draw-CenteredText -Graphics $Graphics -Text $text -Font $font -Brush $brush -X $X -Y $Y -W $W -H $H
            } finally {
                $font.Dispose()
            }
        } finally {
            $brush.Dispose()
        }
        return $true
    }
    if ($Label -ne ".") {
        return $false
    }
    Draw-HihihiPreviewGlyph -Graphics $Graphics -Color $Color -X $X -Y $Y -W $W -H $H
    return $true
}

function Draw-HihihiPreviewGlyph {
    param(
        [System.Drawing.Graphics] $Graphics,
        [System.Drawing.Color] $Color,
        [float] $X,
        [float] $Y,
        [float] $W,
        [float] $H
    )
    $pen = [System.Drawing.Pen]::new($Color, [Math]::Max(1.5, $H * 0.06))
    try {
        $pen.StartCap = [System.Drawing.Drawing2D.LineCap]::Round
        $pen.EndCap = [System.Drawing.Drawing2D.LineCap]::Round
        $pen.LineJoin = [System.Drawing.Drawing2D.LineJoin]::Round
        $scale = [Math]::Min($W * 0.74 / 120.0, $H * 0.54 / 32.0)
        $left = $X + $W / 2.0 - 120.0 * $scale / 2.0
        $top = $Y + $H / 2.0 - 32.0 * $scale / 2.0
        $p = {
            param([float] $Value)
            return $left + $Value * $scale
        }
        $q = {
            param([float] $Value)
            return $top + $Value * $scale
        }
        $Graphics.DrawBezier($pen, (& $p 8), (& $q 22), (& $p 10), (& $q 10), (& $p 17), (& $q 10), (& $p 17), (& $q 22))
        $Graphics.DrawLine($pen, (& $p 8), (& $q 16), (& $p 18), (& $q 16))
        $Graphics.DrawBezier($pen, (& $p 25), (& $q 12), (& $p 30), (& $q 9), (& $p 33), (& $q 11), (& $p 31), (& $q 16))
        $Graphics.DrawBezier($pen, (& $p 31), (& $q 16), (& $p 29), (& $q 21), (& $p 24), (& $q 20), (& $p 26), (& $q 14))
        $Graphics.DrawBezier($pen, (& $p 41), (& $q 22), (& $p 43), (& $q 10), (& $p 50), (& $q 10), (& $p 50), (& $q 22))
        $Graphics.DrawLine($pen, (& $p 41), (& $q 16), (& $p 51), (& $q 16))
        $Graphics.DrawBezier($pen, (& $p 58), (& $q 12), (& $p 63), (& $q 9), (& $p 66), (& $q 11), (& $p 64), (& $q 16))
        $Graphics.DrawBezier($pen, (& $p 64), (& $q 16), (& $p 62), (& $q 21), (& $p 57), (& $q 20), (& $p 59), (& $q 14))
        $Graphics.DrawBezier($pen, (& $p 75), (& $q 22), (& $p 77), (& $q 10), (& $p 84), (& $q 10), (& $p 84), (& $q 22))
        $Graphics.DrawLine($pen, (& $p 75), (& $q 16), (& $p 85), (& $q 16))
        $Graphics.DrawBezier($pen, (& $p 92), (& $q 12), (& $p 97), (& $q 9), (& $p 100), (& $q 11), (& $p 98), (& $q 16))
        $Graphics.DrawBezier($pen, (& $p 98), (& $q 16), (& $p 96), (& $q 21), (& $p 91), (& $q 20), (& $p 93), (& $q 14))
        $Graphics.DrawBezier($pen, (& $p 109), (& $q 12), (& $p 114), (& $q 9), (& $p 117), (& $q 11), (& $p 115), (& $q 16))
        $Graphics.DrawBezier($pen, (& $p 115), (& $q 16), (& $p 113), (& $q 21), (& $p 108), (& $q 20), (& $p 110), (& $q 14))
    } finally {
        $pen.Dispose()
    }
}

function Draw-PackPreviewIcon {
    param(
        [System.Drawing.Graphics] $Graphics,
        [object] $Theme,
        [string] $Icon,
        [System.Drawing.Color] $Color,
        [float] $X,
        [float] $Y,
        [float] $W,
        [float] $H
    )
    $pack = Get-ModifierPackId -Theme $Theme
    if ($pack -eq "metropolis-points" -or $pack -eq "metropolis-graph") {
        Draw-PreviewIcon -Graphics $Graphics -Icon $Icon -Color $Color -X $X -Y $Y -W $W -H $H
        return $true
    }
    if ($pack -eq "dots-lines") {
        $weight = Get-DotsLineWeight -W $W -H $H
        $pen = [System.Drawing.Pen]::new($Color, $weight)
        $brush = [System.Drawing.SolidBrush]::new($Color)
        try {
            $pen.StartCap = [System.Drawing.Drawing2D.LineCap]::Round
            $pen.EndCap = [System.Drawing.Drawing2D.LineCap]::Round
            $y = $Y + $H / 2
            if ($Icon -eq "space") {
                $colors = @(
                    [System.Drawing.Color]::FromArgb(255, 239, 71, 111),
                    [System.Drawing.Color]::FromArgb(255, 255, 209, 102),
                    [System.Drawing.Color]::FromArgb(255, 6, 214, 160),
                    [System.Drawing.Color]::FromArgb(255, 76, 201, 240)
                )
                $diameter = Get-GlyphDotDiameter -H $H
                $gap = Get-SpaceDotGap -Diameter $diameter
                $total = $diameter * $colors.Count + $gap * ($colors.Count - 1)
                $dotX = $X + $W / 2 - $total / 2 + $diameter / 2
                foreach ($dotColor in $colors) {
                    $dotBrush = [System.Drawing.SolidBrush]::new($dotColor)
                    try {
                        Draw-DotGlyph -Graphics $Graphics -Brush $dotBrush -Cx $dotX -Cy $y -Diameter $diameter
                    } finally {
                        $dotBrush.Dispose()
                    }
                    $dotX += $diameter + $gap
                }
            } elseif ($Icon -eq "language" -or $Icon -eq "reserved") {
                $diameter = Get-GlyphDotDiameter -H $H
                Draw-DotGlyph -Graphics $Graphics -Brush $brush -Cx ($X + $W / 2) -Cy $y -Diameter $diameter
            } else {
                $side = if ($Icon -eq "options" -or $Icon -eq "settings" -or $Icon -eq "enter") { 0.39 } elseif ($Icon -eq "backspace" -or $Icon -eq "shift") { 0.30 } else { 0.34 }
                $left = $X + $W * $side
                $right = $X + $W * (1.0 - $side)
                $Graphics.DrawLine($pen, $left, $y, $right, $y)
            }
        } finally {
            $pen.Dispose()
            $brush.Dispose()
        }
        return $true
    }
    return $false
}

function Get-DotsLineWeight {
    param([float] $W, [float] $H)
    return (Get-GlyphDotDiameter -H $H) / 1.38
}

function Get-GlyphDotDiameter {
    param([float] $H)
    return [Math]::Max(4.2, $H * 0.16)
}

function Get-SpaceDotGap {
    param([float] $Diameter)
    return [Math]::Max(($Diameter / 2.0) * 1.35, 3.2)
}

function Get-TwoDotCenterGap {
    param([float] $Diameter)
    return [Math]::Max(($Diameter / 2.0) * 2.75, 5.4)
}

function Draw-DotGlyph {
    param(
        [System.Drawing.Graphics] $Graphics,
        [System.Drawing.Brush] $Brush,
        [float] $Cx,
        [float] $Cy,
        [float] $Diameter
    )
    $Graphics.FillEllipse($Brush, $Cx - $Diameter / 2.0, $Cy - $Diameter / 2.0, $Diameter, $Diameter)
}

function Draw-TwoDotGlyph {
    param(
        [System.Drawing.Graphics] $Graphics,
        [System.Drawing.Brush] $Brush,
        [float] $Cx,
        [float] $Cy,
        [float] $Diameter
    )
    $gap = Get-TwoDotCenterGap -Diameter $Diameter
    Draw-DotGlyph -Graphics $Graphics -Brush $Brush -Cx ($Cx - $gap / 2.0) -Cy $Cy -Diameter $Diameter
    Draw-DotGlyph -Graphics $Graphics -Brush $Brush -Cx ($Cx + $gap / 2.0) -Cy $Cy -Diameter $Diameter
}

function Draw-PointGlyphPreview {
    param(
        [System.Drawing.Graphics] $Graphics,
        [string] $Glyph,
        [System.Drawing.Color] $Color,
        [float] $X,
        [float] $Y,
        [float] $W,
        [float] $H
    )
    if ([string]::IsNullOrWhiteSpace($Glyph)) {
        return $false
    }
    $cx = $X + $W / 2.0
    $cy = $Y + $H / 2.0
    $diameter = Get-GlyphDotDiameter -H $H
    $r = $diameter / 2.0
    $brush = [System.Drawing.SolidBrush]::new($Color)
    $pen = [System.Drawing.Pen]::new($Color, [Math]::Max(1.4, $r / 0.69 * 0.78))
    try {
        $pen.StartCap = [System.Drawing.Drawing2D.LineCap]::Round
        $pen.EndCap = [System.Drawing.Drawing2D.LineCap]::Round
        $pen.LineJoin = [System.Drawing.Drawing2D.LineJoin]::Round
        if ($Glyph.StartsWith("font_")) {
            $fontText = switch ($Glyph) {
                "font_return_arrow" { [string][char]0x21B5 }
                "font_tab_arrow" { [string][char]0x21E5 }
                "font_back_tab" { [string][char]0x21E4 }
                "font_shift_arrow" { [string][char]0x21E7 }
                "font_delete_left" { [string][char]0x232B }
                "font_delete_right" { [string][char]0x2326 }
                "font_command" { [string][char]0x2318 }
                "font_option" { [string][char]0x2325 }
                "font_control" { [string][char]0x2303 }
                "font_escape" { [string][char]0x238B }
                "font_home" { [string][char]0x21F1 }
                "font_end" { [string][char]0x21F2 }
                "font_page_up" { [string][char]0x21DE }
                "font_page_down" { [string][char]0x21DF }
                "font_power" { [string][char]0x23FB }
                "font_eject" { [string][char]0x23CF }
                "font_play_pause" { [string][char]0x23EF }
                "font_record" { [string][char]0x23FA }
                "font_rewind" { [string][char]0x23EA }
                "font_fast_forward" { [string][char]0x23E9 }
                "font_triangle_up" { [string][char]0x25B2 }
                "font_triangle_down" { [string][char]0x25BC }
                "font_star_outline" { [string][char]0x2606 }
                "font_star_solid" { [string][char]0x2605 }
                "font_keyboard" { [string][char]0x2328 }
                default { "" }
            }
            if (-not [string]::IsNullOrWhiteSpace($fontText)) {
                $font = [System.Drawing.Font]::new("Segoe UI Symbol", [Math]::Min($W, $H) * 0.38, [System.Drawing.FontStyle]::Bold, [System.Drawing.GraphicsUnit]::Pixel)
                $format = [System.Drawing.StringFormat]::new()
                try {
                    $format.Alignment = [System.Drawing.StringAlignment]::Center
                    $format.LineAlignment = [System.Drawing.StringAlignment]::Center
                    $Graphics.DrawString($fontText, $font, $brush, [System.Drawing.RectangleF]::new($X, $Y, $W, $H), $format)
                } finally {
                    $format.Dispose()
                    $font.Dispose()
                }
                return $true
            }
        }
        if ($Glyph.StartsWith("img_")) {
            $maskPath = Join-Path (Split-Path -Parent $PSScriptRoot) ("app\src\main\res\drawable-nodpi\glyph_mask_" + $Glyph + ".png")
            if (Test-Path $maskPath) {
                $src = [System.Drawing.Bitmap]::new($maskPath)
                $tinted = [System.Drawing.Bitmap]::new($src.Width, $src.Height, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
                try {
                    for ($px = 0; $px -lt $src.Width; $px++) {
                        for ($py = 0; $py -lt $src.Height; $py++) {
                            $a = $src.GetPixel($px, $py).A
                            if ($a -gt 0) {
                                $tinted.SetPixel($px, $py, [System.Drawing.Color]::FromArgb($a, $Color.R, $Color.G, $Color.B))
                            }
                        }
                    }
                    $targetH = $H * 0.68
                    $targetW = [Math]::Min($W * 0.72, $targetH * 0.75)
                    $Graphics.DrawImage($tinted, [System.Drawing.RectangleF]::new($cx - $targetW / 2.0, $cy - $targetH / 2.0, $targetW, $targetH))
                } finally {
                    $tinted.Dispose()
                    $src.Dispose()
                }
                return $true
            }
        }
        switch ($Glyph) {
            "dot" {
                Draw-DotGlyph -Graphics $Graphics -Brush $brush -Cx $cx -Cy $cy -Diameter $diameter
                return $true
            }
            "ring" {
                $rr = $r * 1.45
                $Graphics.DrawEllipse($pen, $cx - $rr, $cy - $rr, $rr * 2.0, $rr * 2.0)
                return $true
            }
            "diamond" {
                $s = $r * 2.45
                $points = @(
                    [System.Drawing.PointF]::new($cx, $cy - $s),
                    [System.Drawing.PointF]::new($cx + $s, $cy),
                    [System.Drawing.PointF]::new($cx, $cy + $s),
                    [System.Drawing.PointF]::new($cx - $s, $cy)
                )
                $Graphics.FillPolygon($brush, $points)
                return $true
            }
            "square" {
                $s = $r * 2.15
                $Graphics.FillRectangle($brush, $cx - $s, $cy - $s, $s * 2.0, $s * 2.0)
                return $true
            }
            "plus" {
                $Graphics.DrawLine($pen, $cx - $r * 2.0, $cy, $cx + $r * 2.0, $cy)
                $Graphics.DrawLine($pen, $cx, $cy - $r * 2.0, $cx, $cy + $r * 2.0)
                return $true
            }
            "cross" {
                $Graphics.DrawLine($pen, $cx - $r * 1.45, $cy - $r * 1.45, $cx + $r * 1.45, $cy + $r * 1.45)
                $Graphics.DrawLine($pen, $cx + $r * 1.45, $cy - $r * 1.45, $cx - $r * 1.45, $cy + $r * 1.45)
                return $true
            }
            "star" {
                $Graphics.DrawLine($pen, $cx - $r * 2.1, $cy, $cx + $r * 2.1, $cy)
                $Graphics.DrawLine($pen, $cx, $cy - $r * 2.1, $cx, $cy + $r * 2.1)
                $Graphics.DrawLine($pen, $cx - $r * 1.5, $cy - $r * 1.5, $cx + $r * 1.5, $cy + $r * 1.5)
                $Graphics.DrawLine($pen, $cx + $r * 1.5, $cy - $r * 1.5, $cx - $r * 1.5, $cy + $r * 1.5)
                return $true
            }
            "spark" {
                $Graphics.DrawLine($pen, $cx, $cy - $r * 2.5, $cx, $cy + $r * 2.5)
                $Graphics.DrawLine($pen, $cx - $r * 1.7, $cy, $cx + $r * 1.7, $cy)
                return $true
            }
            "chevron_up" {
                $Graphics.DrawLines($pen, @(
                    [System.Drawing.PointF]::new($cx - $r * 2.1, $cy + $r * 1.15),
                    [System.Drawing.PointF]::new($cx, $cy - $r * 1.35),
                    [System.Drawing.PointF]::new($cx + $r * 2.1, $cy + $r * 1.15)
                ))
                return $true
            }
            "chevron_left" {
                $Graphics.DrawLines($pen, @(
                    [System.Drawing.PointF]::new($cx + $r * 1.35, $cy - $r * 2.1),
                    [System.Drawing.PointF]::new($cx - $r * 1.15, $cy),
                    [System.Drawing.PointF]::new($cx + $r * 1.35, $cy + $r * 2.1)
                ))
                return $true
            }
            "chevron_right" {
                $Graphics.DrawLines($pen, @(
                    [System.Drawing.PointF]::new($cx - $r * 1.35, $cy - $r * 2.1),
                    [System.Drawing.PointF]::new($cx + $r * 1.15, $cy),
                    [System.Drawing.PointF]::new($cx - $r * 1.35, $cy + $r * 2.1)
                ))
                return $true
            }
            "slash_dot" {
                $Graphics.DrawLine($pen, $cx - $r * 1.8, $cy + $r * 1.8, $cx + $r * 1.8, $cy - $r * 1.8)
                Draw-DotGlyph -Graphics $Graphics -Brush $brush -Cx ($cx + $r * 2.35) -Cy ($cy + $r * 1.85) -Diameter ($diameter * 0.72)
                return $true
            }
            "orbit" {
                $Graphics.DrawEllipse($pen, $cx - $r * 2.55, $cy - $r * 1.35, $r * 5.1, $r * 2.7)
                Draw-DotGlyph -Graphics $Graphics -Brush $brush -Cx ($cx + $r * 1.65) -Cy ($cy - $r * 0.75) -Diameter ($diameter * 0.75)
                return $true
            }
            "gear_dot" {
                $Graphics.DrawEllipse($pen, $cx - $r * 1.35, $cy - $r * 1.35, $r * 2.7, $r * 2.7)
                $Graphics.DrawLine($pen, $cx - $r * 2.3, $cy, $cx - $r * 1.75, $cy)
                $Graphics.DrawLine($pen, $cx + $r * 1.75, $cy, $cx + $r * 2.3, $cy)
                $Graphics.DrawLine($pen, $cx, $cy - $r * 2.3, $cx, $cy - $r * 1.75)
                $Graphics.DrawLine($pen, $cx, $cy + $r * 1.75, $cx, $cy + $r * 2.3)
                Draw-DotGlyph -Graphics $Graphics -Brush $brush -Cx $cx -Cy $cy -Diameter ($diameter * 0.55)
                return $true
            }
            "bookmark_dot" {
                $points = @(
                    [System.Drawing.PointF]::new($cx - $r * 1.65, $cy - $r * 2.2),
                    [System.Drawing.PointF]::new($cx + $r * 1.65, $cy - $r * 2.2),
                    [System.Drawing.PointF]::new($cx + $r * 1.65, $cy + $r * 2.15),
                    [System.Drawing.PointF]::new($cx, $cy + $r * 1.25),
                    [System.Drawing.PointF]::new($cx - $r * 1.65, $cy + $r * 2.15)
                )
                $Graphics.DrawPolygon($pen, $points)
                Draw-DotGlyph -Graphics $Graphics -Brush $brush -Cx $cx -Cy ($cy - $r * 0.3) -Diameter ($diameter * 0.48)
                return $true
            }
            "space_dots" {
                $gap = [Math]::Max($r * 1.35, 3.2)
                $total = $r * 8.0 + $gap * 3.0
                $start = $cx - $total / 2.0 + $r
                for ($i = 0; $i -lt 4; $i++) {
                    Draw-DotGlyph -Graphics $Graphics -Brush $brush -Cx ($start + $i * ($r * 2.0 + $gap)) -Cy $cy -Diameter $diameter
                }
                return $true
            }
            "two_dots" {
                Draw-TwoDotGlyph -Graphics $Graphics -Brush $brush -Cx $cx -Cy $cy -Diameter $diameter
                return $true
            }
            "grid_4" {
                $gap = $r * 1.45
                $d = $diameter * 0.78
                Draw-DotGlyph -Graphics $Graphics -Brush $brush -Cx ($cx - $gap) -Cy ($cy - $gap) -Diameter $d
                Draw-DotGlyph -Graphics $Graphics -Brush $brush -Cx ($cx + $gap) -Cy ($cy - $gap) -Diameter $d
                Draw-DotGlyph -Graphics $Graphics -Brush $brush -Cx ($cx - $gap) -Cy ($cy + $gap) -Diameter $d
                Draw-DotGlyph -Graphics $Graphics -Brush $brush -Cx ($cx + $gap) -Cy ($cy + $gap) -Diameter $d
                return $true
            }
            "terminal" {
                $Graphics.DrawLines($pen, @(
                    [System.Drawing.PointF]::new($cx - $r * 2.3, $cy - $r * 1.25),
                    [System.Drawing.PointF]::new($cx - $r * 0.65, $cy),
                    [System.Drawing.PointF]::new($cx - $r * 2.3, $cy + $r * 1.25)
                ))
                $Graphics.DrawLine($pen, $cx - $r * 0.1, $cy + $r * 1.45, $cx + $r * 2.2, $cy + $r * 1.45)
                return $true
            }
            "cursor" {
                $Graphics.DrawLine($pen, $cx, $cy - $r * 2.35, $cx, $cy + $r * 2.35)
                Draw-DotGlyph -Graphics $Graphics -Brush $brush -Cx ($cx + $r * 1.45) -Cy ($cy + $r * 1.75) -Diameter ($diameter * 0.58)
                return $true
            }
            "keyboard_return" {
                $Graphics.DrawLine($pen, $cx + $r * 2.4, $cy - $r * 2.2, $cx + $r * 2.4, $cy + $r * 0.7)
                $Graphics.DrawLine($pen, $cx + $r * 2.4, $cy + $r * 0.7, $cx - $r * 1.8, $cy + $r * 0.7)
                $Graphics.DrawLine($pen, $cx - $r * 1.8, $cy + $r * 0.7, $cx - $r * 0.45, $cy - $r * 0.6)
                $Graphics.DrawLine($pen, $cx - $r * 1.8, $cy + $r * 0.7, $cx - $r * 0.45, $cy + $r * 2.0)
                return $true
            }
            "keyboard_tab" {
                $Graphics.DrawLine($pen, $cx + $r * 2.4, $cy - $r * 2.2, $cx + $r * 2.4, $cy + $r * 2.2)
                $Graphics.DrawLine($pen, $cx - $r * 2.4, $cy, $cx + $r * 1.3, $cy)
                $Graphics.DrawLine($pen, $cx + $r * 1.3, $cy, $cx + $r * 0.15, $cy - $r * 1.15)
                $Graphics.DrawLine($pen, $cx + $r * 1.3, $cy, $cx + $r * 0.15, $cy + $r * 1.15)
                return $true
            }
            "keyboard_capslock" {
                $Graphics.DrawLines($pen, @(
                    [System.Drawing.PointF]::new($cx - $r * 2.2, $cy + $r * 0.25),
                    [System.Drawing.PointF]::new($cx, $cy - $r * 2.0),
                    [System.Drawing.PointF]::new($cx + $r * 2.2, $cy + $r * 0.25)
                ))
                $Graphics.DrawLine($pen, $cx - $r * 2.4, $cy + $r * 2.0, $cx + $r * 2.4, $cy + $r * 2.0)
                return $true
            }
            "keyboard_command" {
                $s = $r * 1.25
                $o = $r * 1.45
                foreach ($dx in @(-1, 1)) {
                    foreach ($dy in @(-1, 1)) {
                        $Graphics.DrawEllipse($pen, $cx + $dx * $o - $s, $cy + $dy * $o - $s, $s * 2, $s * 2)
                    }
                }
                $Graphics.DrawRectangle($pen, $cx - $o, $cy - $o, $o * 2, $o * 2)
                return $true
            }
            "keyboard_option" {
                $Graphics.DrawLine($pen, $cx - $r * 2.6, $cy - $r * 1.65, $cx - $r * 1.05, $cy - $r * 1.65)
                $Graphics.DrawLine($pen, $cx - $r * 0.95, $cy - $r * 1.65, $cx + $r * 1.25, $cy + $r * 1.65)
                $Graphics.DrawLine($pen, $cx + $r * 1.25, $cy + $r * 1.65, $cx + $r * 2.6, $cy + $r * 1.65)
                $Graphics.DrawLine($pen, $cx + $r * 0.9, $cy - $r * 1.65, $cx + $r * 2.6, $cy - $r * 1.65)
                return $true
            }
            "keyboard_control" {
                $Graphics.DrawLines($pen, @(
                    [System.Drawing.PointF]::new($cx - $r * 2.35, $cy + $r * 0.75),
                    [System.Drawing.PointF]::new($cx, $cy - $r * 1.75),
                    [System.Drawing.PointF]::new($cx + $r * 2.35, $cy + $r * 0.75)
                ))
                return $true
            }
            "keyboard_hide" {
                $Graphics.DrawRectangle($pen, $cx - $r * 4.0, $cy - $r * 2.7, $r * 8.0, $r * 4.8)
                $Graphics.DrawLine($pen, $cx - $r * 1.4, $cy + $r * 2.55, $cx, $cy + $r * 3.65)
                $Graphics.DrawLine($pen, $cx, $cy + $r * 3.65, $cx + $r * 1.4, $cy + $r * 2.55)
                return $true
            }
            "keyboard_full" {
                $Graphics.DrawRectangle($pen, $cx - $r * 4.4, $cy - $r * 2.75, $r * 8.8, $r * 5.1)
                for ($row = -1; $row -le 1; $row++) {
                    $count = if ($row -eq 1) { 3 } else { 5 }
                    $start = $cx - ($count - 1) * $r * 1.05
                    $yy = $cy + $row * $r * 1.35
                    for ($i = 0; $i -lt $count; $i++) {
                        Draw-DotGlyph -Graphics $Graphics -Brush $brush -Cx ($start + $i * $r * 2.1) -Cy $yy -Diameter ($diameter * 0.55)
                    }
                }
                return $true
            }
            "keyboard_keys" {
                for ($row = -1; $row -le 1; $row++) {
                    $count = if ($row -eq 1) { 3 } else { 5 }
                    $start = $cx - ($count - 1) * $r * 1.05
                    $yy = $cy + $row * $r * 1.35
                    for ($i = 0; $i -lt $count; $i++) {
                        Draw-DotGlyph -Graphics $Graphics -Brush $brush -Cx ($start + $i * $r * 2.1) -Cy $yy -Diameter ($diameter * 0.55)
                    }
                }
                return $true
            }
            "keyboard_language" {
                $Graphics.DrawEllipse($pen, $cx - $r * 2.6, $cy - $r * 2.6, $r * 5.2, $r * 5.2)
                $Graphics.DrawLine($pen, $cx - $r * 2.6, $cy, $cx + $r * 2.6, $cy)
                $Graphics.DrawLine($pen, $cx, $cy - $r * 2.6, $cx, $cy + $r * 2.6)
                $Graphics.DrawEllipse($pen, $cx - $r * 1.25, $cy - $r * 2.6, $r * 2.5, $r * 5.2)
                return $true
            }
            "keyboard_arrow_up" { return Draw-PointGlyphPreview -Graphics $Graphics -Glyph "chevron_up" -Color $Color -X $X -Y $Y -W $W -H $H }
            "keyboard_arrow_down" {
                $Graphics.DrawLines($pen, @(
                    [System.Drawing.PointF]::new($cx - $r * 2.1, $cy - $r * 1.15),
                    [System.Drawing.PointF]::new($cx, $cy + $r * 1.35),
                    [System.Drawing.PointF]::new($cx + $r * 2.1, $cy - $r * 1.15)
                ))
                return $true
            }
            "keyboard_arrow_left" { return Draw-PointGlyphPreview -Graphics $Graphics -Glyph "chevron_left" -Color $Color -X $X -Y $Y -W $W -H $H }
            "keyboard_arrow_right" { return Draw-PointGlyphPreview -Graphics $Graphics -Glyph "chevron_right" -Color $Color -X $X -Y $Y -W $W -H $H }
            "keyboard_double_left" {
                $Graphics.DrawLines($pen, @(
                    [System.Drawing.PointF]::new($cx - $r * 0.2, $cy - $r * 2.1),
                    [System.Drawing.PointF]::new($cx - $r * 2.0, $cy),
                    [System.Drawing.PointF]::new($cx - $r * 0.2, $cy + $r * 2.1)
                ))
                $Graphics.DrawLines($pen, @(
                    [System.Drawing.PointF]::new($cx + $r * 2.0, $cy - $r * 2.1),
                    [System.Drawing.PointF]::new($cx + $r * 0.2, $cy),
                    [System.Drawing.PointF]::new($cx + $r * 2.0, $cy + $r * 2.1)
                ))
                return $true
            }
            "keyboard_double_right" {
                $Graphics.DrawLines($pen, @(
                    [System.Drawing.PointF]::new($cx - $r * 2.0, $cy - $r * 2.1),
                    [System.Drawing.PointF]::new($cx - $r * 0.2, $cy),
                    [System.Drawing.PointF]::new($cx - $r * 2.0, $cy + $r * 2.1)
                ))
                $Graphics.DrawLines($pen, @(
                    [System.Drawing.PointF]::new($cx + $r * 0.2, $cy - $r * 2.1),
                    [System.Drawing.PointF]::new($cx + $r * 2.0, $cy),
                    [System.Drawing.PointF]::new($cx + $r * 0.2, $cy + $r * 2.1)
                ))
                return $true
            }
            "keyboard_backspace" {
                $points = @(
                    [System.Drawing.PointF]::new($cx - $r * 2.8, $cy),
                    [System.Drawing.PointF]::new($cx - $r * 1.25, $cy - $r * 1.6),
                    [System.Drawing.PointF]::new($cx + $r * 2.45, $cy - $r * 1.6),
                    [System.Drawing.PointF]::new($cx + $r * 2.45, $cy + $r * 1.6),
                    [System.Drawing.PointF]::new($cx - $r * 1.25, $cy + $r * 1.6)
                )
                $Graphics.DrawPolygon($pen, $points)
                $Graphics.DrawLine($pen, $cx - $r * 0.4, $cy - $r * 0.75, $cx + $r * 0.95, $cy + $r * 0.75)
                $Graphics.DrawLine($pen, $cx + $r * 0.95, $cy - $r * 0.75, $cx - $r * 0.4, $cy + $r * 0.75)
                return $true
            }
            "keyboard_space" {
                $Graphics.DrawLine($pen, $cx - $r * 3.1, $cy - $r * 0.8, $cx - $r * 3.1, $cy + $r * 0.95)
                $Graphics.DrawLine($pen, $cx - $r * 3.1, $cy + $r * 0.95, $cx + $r * 3.1, $cy + $r * 0.95)
                $Graphics.DrawLine($pen, $cx + $r * 3.1, $cy + $r * 0.95, $cx + $r * 3.1, $cy - $r * 0.8)
                return $true
            }
            "gmk_accent_bar" {
                $Graphics.DrawLine($pen, $cx - $r * 3.2, $cy, $cx + $r * 3.2, $cy)
                return $true
            }
            "gmk_accent_corner" {
                $Graphics.DrawLines($pen, @(
                    [System.Drawing.PointF]::new($cx - $r * 2.4, $cy - $r * 1.65),
                    [System.Drawing.PointF]::new($cx + $r * 1.9, $cy - $r * 1.65),
                    [System.Drawing.PointF]::new($cx + $r * 1.9, $cy + $r * 2.2)
                ))
                return $true
            }
            "gmk_accent_stripe" {
                $Graphics.DrawLine($pen, $cx - $r * 2.8, $cy - $r * 1.3, $cx + $r * 2.8, $cy - $r * 1.3)
                $Graphics.DrawLine($pen, $cx - $r * 2.8, $cy + $r * 1.3, $cx + $r * 2.8, $cy + $r * 1.3)
                return $true
            }
            "gmk_triple_dot" {
                Draw-DotGlyph -Graphics $Graphics -Brush $brush -Cx ($cx - $r * 2.0) -Cy $cy -Diameter ($diameter * 0.72)
                Draw-DotGlyph -Graphics $Graphics -Brush $brush -Cx $cx -Cy $cy -Diameter ($diameter * 0.72)
                Draw-DotGlyph -Graphics $Graphics -Brush $brush -Cx ($cx + $r * 2.0) -Cy $cy -Diameter ($diameter * 0.72)
                return $true
            }
            "gmk_twin_ticks" {
                $Graphics.DrawLine($pen, $cx - $r * 1.3, $cy - $r * 1.8, $cx - $r * 2.2, $cy + $r * 1.8)
                $Graphics.DrawLine($pen, $cx + $r * 2.2, $cy - $r * 1.8, $cx + $r * 1.3, $cy + $r * 1.8)
                return $true
            }
            "gmk_space_dash" {
                $Graphics.DrawLine($pen, $cx - $r * 3.8, $cy + $r * 0.7, $cx + $r * 3.8, $cy + $r * 0.7)
                $Graphics.DrawLine($pen, $cx - $r * 3.8, $cy - $r * 0.7, $cx - $r * 2.6, $cy - $r * 0.7)
                $Graphics.DrawLine($pen, $cx + $r * 2.6, $cy - $r * 0.7, $cx + $r * 3.8, $cy - $r * 0.7)
                return $true
            }
            "gmk_macro_stack" {
                $Graphics.DrawLine($pen, $cx - $r * 2.5, $cy - $r * 1.7, $cx + $r * 2.5, $cy - $r * 1.7)
                $Graphics.DrawLine($pen, $cx - $r * 1.7, $cy, $cx + $r * 1.7, $cy)
                $Graphics.DrawLine($pen, $cx - $r * 2.5, $cy + $r * 1.7, $cx + $r * 2.5, $cy + $r * 1.7)
                return $true
            }
            "gmk_macro_brackets" {
                $Graphics.DrawLines($pen, @([System.Drawing.PointF]::new($cx - $r * 1.1, $cy - $r * 2.2), [System.Drawing.PointF]::new($cx - $r * 2.5, $cy - $r * 2.2), [System.Drawing.PointF]::new($cx - $r * 2.5, $cy + $r * 2.2), [System.Drawing.PointF]::new($cx - $r * 1.1, $cy + $r * 2.2)))
                $Graphics.DrawLines($pen, @([System.Drawing.PointF]::new($cx + $r * 1.1, $cy - $r * 2.2), [System.Drawing.PointF]::new($cx + $r * 2.5, $cy - $r * 2.2), [System.Drawing.PointF]::new($cx + $r * 2.5, $cy + $r * 2.2), [System.Drawing.PointF]::new($cx + $r * 1.1, $cy + $r * 2.2)))
                return $true
            }
            "gmk_target" {
                $Graphics.DrawEllipse($pen, $cx - $r * 2.25, $cy - $r * 2.25, $r * 4.5, $r * 4.5)
                $Graphics.DrawEllipse($pen, $cx - $r * 0.9, $cy - $r * 0.9, $r * 1.8, $r * 1.8)
                $Graphics.DrawLine($pen, $cx - $r * 3.0, $cy, $cx - $r * 2.25, $cy)
                $Graphics.DrawLine($pen, $cx + $r * 2.25, $cy, $cx + $r * 3.0, $cy)
                return $true
            }
            "gmk_pulse" {
                $Graphics.DrawLines($pen, @([System.Drawing.PointF]::new($cx - $r * 3.2, $cy), [System.Drawing.PointF]::new($cx - $r * 1.6, $cy), [System.Drawing.PointF]::new($cx - $r * 0.8, $cy - $r * 1.7), [System.Drawing.PointF]::new($cx + $r * 0.25, $cy + $r * 1.8), [System.Drawing.PointF]::new($cx + $r * 1.1, $cy), [System.Drawing.PointF]::new($cx + $r * 3.2, $cy)))
                return $true
            }
            "gmk_wave" {
                $path = [System.Drawing.Drawing2D.GraphicsPath]::new()
                $path.AddBezier($cx - $r * 3.0, $cy + $r * 0.4, $cx - $r * 1.9, $cy - $r * 1.6, $cx - $r * 0.9, $cy + $r * 2.0, $cx, $cy + $r * 0.2)
                $path.AddBezier($cx, $cy + $r * 0.2, $cx + $r * 0.9, $cy - $r * 1.6, $cx + $r * 1.9, $cy + $r * 2.0, $cx + $r * 3.0, $cy)
                $Graphics.DrawPath($pen, $path)
                $path.Dispose()
                return $true
            }
            "gmk_moon" {
                $path = [System.Drawing.Drawing2D.GraphicsPath]::new()
                $path.AddBezier($cx + $r * 1.0, $cy - $r * 2.25, $cx - $r * 1.8, $cy - $r * 2.0, $cx - $r * 2.9, $cy + $r * 0.8, $cx - $r * 0.5, $cy + $r * 2.5)
                $path.AddBezier($cx - $r * 0.5, $cy + $r * 2.5, $cx + $r * 0.65, $cy + $r * 3.3, $cx + $r * 2.1, $cy + $r * 2.45, $cx + $r * 2.55, $cy + $r * 1.25)
                $path.AddBezier($cx + $r * 2.55, $cy + $r * 1.25, $cx + $r * 0.75, $cy + $r * 1.9, $cx - $r * 0.35, $cy + $r * 0.55, $cx, $cy - $r * 0.65)
                $path.AddBezier($cx, $cy - $r * 0.65, $cx + $r * 0.25, $cy - $r * 1.55, $cx + $r * 0.85, $cy - $r * 2.1, $cx + $r * 1.0, $cy - $r * 2.25)
                $Graphics.FillPath($brush, $path)
                $path.Dispose()
                return $true
            }
            "gmk_sun" {
                $Graphics.DrawEllipse($pen, $cx - $r * 1.3, $cy - $r * 1.3, $r * 2.6, $r * 2.6)
                for ($i = 0; $i -lt 8; $i++) {
                    $a = [Math]::PI * 2.0 * $i / 8.0
                    $Graphics.DrawLine($pen, $cx + [Math]::Cos($a) * $r * 2.05, $cy + [Math]::Sin($a) * $r * 2.05, $cx + [Math]::Cos($a) * $r * 3.0, $cy + [Math]::Sin($a) * $r * 3.0)
                }
                return $true
            }
            "gmk_leaf" {
                $path = [System.Drawing.Drawing2D.GraphicsPath]::new()
                $path.AddBezier($cx - $r * 1.9, $cy + $r * 1.8, $cx - $r * 1.7, $cy - $r * 1.8, $cx + $r * 1.9, $cy - $r * 2.1, $cx + $r * 2.1, $cy + $r * 1.4)
                $path.AddBezier($cx + $r * 2.1, $cy + $r * 1.4, $cx + $r * 0.2, $cy + $r * 2.0, $cx - $r * 1.0, $cy + $r * 2.1, $cx - $r * 1.9, $cy + $r * 1.8)
                $Graphics.DrawPath($pen, $path)
                $path.Dispose()
                $Graphics.DrawLine($pen, $cx - $r * 1.4, $cy + $r * 1.4, $cx + $r * 1.2, $cy - $r * 1.1)
                return $true
            }
            "gmk_flower" {
                Draw-DotGlyph -Graphics $Graphics -Brush $brush -Cx $cx -Cy ($cy - $r * 1.45) -Diameter ($diameter * 0.95)
                Draw-DotGlyph -Graphics $Graphics -Brush $brush -Cx ($cx + $r * 1.35) -Cy $cy -Diameter ($diameter * 0.95)
                Draw-DotGlyph -Graphics $Graphics -Brush $brush -Cx $cx -Cy ($cy + $r * 1.45) -Diameter ($diameter * 0.95)
                Draw-DotGlyph -Graphics $Graphics -Brush $brush -Cx ($cx - $r * 1.35) -Cy $cy -Diameter ($diameter * 0.95)
                Draw-DotGlyph -Graphics $Graphics -Brush $brush -Cx $cx -Cy $cy -Diameter ($diameter * 0.55)
                return $true
            }
            "gmk_mountain" {
                $Graphics.DrawLines($pen, @([System.Drawing.PointF]::new($cx - $r * 3.0, $cy + $r * 2.0), [System.Drawing.PointF]::new($cx - $r * 0.8, $cy - $r * 1.6), [System.Drawing.PointF]::new($cx + $r * 0.4, $cy + $r * 0.3), [System.Drawing.PointF]::new($cx + $r * 1.3, $cy - $r * 0.9), [System.Drawing.PointF]::new($cx + $r * 3.0, $cy + $r * 2.0)))
                return $true
            }
            "gmk_droplet" {
                $path = [System.Drawing.Drawing2D.GraphicsPath]::new()
                $path.AddBezier($cx, $cy - $r * 2.8, $cx + $r * 2.2, $cy - $r * 0.8, $cx + $r * 2.0, $cy + $r * 2.2, $cx, $cy + $r * 2.4)
                $path.AddBezier($cx, $cy + $r * 2.4, $cx - $r * 2.0, $cy + $r * 2.2, $cx - $r * 2.2, $cy - $r * 0.8, $cx, $cy - $r * 2.8)
                $Graphics.DrawPath($pen, $path)
                $path.Dispose()
                return $true
            }
            "gmk_orbit_star" {
                $Graphics.DrawEllipse($pen, $cx - $r * 2.8, $cy - $r * 1.3, $r * 5.6, $r * 2.6)
                $Graphics.DrawLine($pen, $cx + $r * 1.75, $cy - $r * 1.6, $cx + $r * 1.75, $cy + $r * 1.6)
                $Graphics.DrawLine($pen, $cx + $r * 0.55, $cy, $cx + $r * 2.95, $cy)
                return $true
            }
            "gmk_diamond_cluster" {
                Draw-PointGlyphPreview -Graphics $Graphics -Glyph "diamond" -Color $Color -X ($X) -Y ($Y - $r * 1.7) -W $W -H $H | Out-Null
                Draw-PointGlyphPreview -Graphics $Graphics -Glyph "diamond" -Color $Color -X ($X - $r * 1.55) -Y ($Y + $r * 0.9) -W $W -H $H | Out-Null
                Draw-PointGlyphPreview -Graphics $Graphics -Glyph "diamond" -Color $Color -X ($X + $r * 1.55) -Y ($Y + $r * 0.9) -W $W -H $H | Out-Null
                return $true
            }
            "gmk_pixel_steps" {
                $size = $r * 1.25
                for ($i = 0; $i -lt 4; $i++) {
                    $Graphics.FillRectangle($brush, $cx - $r * 2.4 + $i * $size, $cy + $r * 1.6 - $i * $size, $size, $size * 2.0)
                }
                return $true
            }
            "gmk_constellation" {
                $Graphics.DrawLines($pen, @([System.Drawing.PointF]::new($cx - $r * 2.1, $cy - $r), [System.Drawing.PointF]::new($cx - $r * 0.35, $cy + $r * 0.15), [System.Drawing.PointF]::new($cx + $r * 1.7, $cy - $r * 1.35), [System.Drawing.PointF]::new($cx + $r * 2.2, $cy + $r * 1.45)))
                foreach ($pt in @(@(-2.1, -1.0), @(-0.35, 0.15), @(1.7, -1.35), @(2.2, 1.45))) {
                    Draw-DotGlyph -Graphics $Graphics -Brush $brush -Cx ($cx + $r * $pt[0]) -Cy ($cy + $r * $pt[1]) -Diameter ($diameter * 0.45)
                }
                return $true
            }
            "gmk_planet_ring" {
                $Graphics.DrawEllipse($pen, $cx - $r * 1.55, $cy - $r * 1.55, $r * 3.1, $r * 3.1)
                $Graphics.DrawEllipse($pen, $cx - $r * 3.0, $cy - $r * 1.05, $r * 6.0, $r * 2.1)
                return $true
            }
            "gmk_comet_tail" {
                Draw-DotGlyph -Graphics $Graphics -Brush $brush -Cx ($cx + $r * 1.85) -Cy ($cy - $r * 1.2) -Diameter ($diameter * 0.85)
                $Graphics.DrawLine($pen, $cx + $r * 0.8, $cy - $r * 0.45, $cx - $r * 2.8, $cy + $r * 1.7)
                $Graphics.DrawLine($pen, $cx + $r * 0.55, $cy - $r * 1.25, $cx - $r * 2.45, $cy - $r * 0.35)
                return $true
            }
            "gmk_crescent_star" {
                Draw-PointGlyphPreview -Graphics $Graphics -Glyph "gmk_moon" -Color $Color -X $X -Y $Y -W $W -H $H | Out-Null
                $Graphics.DrawLine($pen, $cx + $r * 2.0, $cy - $r * 2.2, $cx + $r * 2.0, $cy - $r * 0.8)
                $Graphics.DrawLine($pen, $cx + $r * 1.3, $cy - $r * 1.5, $cx + $r * 2.7, $cy - $r * 1.5)
                return $true
            }
            "gmk_sparkle_pair" {
                $Graphics.DrawLine($pen, $cx - $r * 1.5, $cy - $r * 2.2, $cx - $r * 1.5, $cy - $r * 0.4)
                $Graphics.DrawLine($pen, $cx - $r * 2.4, $cy - $r * 1.3, $cx - $r * 0.6, $cy - $r * 1.3)
                $Graphics.DrawLine($pen, $cx + $r * 1.55, $cy + $r * 0.15, $cx + $r * 1.55, $cy + $r * 2.35)
                $Graphics.DrawLine($pen, $cx + $r * 0.45, $cy + $r * 1.25, $cx + $r * 2.65, $cy + $r * 1.25)
                return $true
            }
            "gmk_plus_cluster" {
                $Graphics.DrawLine($pen, $cx - $r * 2.0, $cy - $r * 0.7, $cx - $r * 0.6, $cy - $r * 0.7)
                $Graphics.DrawLine($pen, $cx - $r * 1.3, $cy - $r * 1.4, $cx - $r * 1.3, $cy)
                $Graphics.DrawLine($pen, $cx + $r * 0.7, $cy + $r * 1.0, $cx + $r * 2.3, $cy + $r * 1.0)
                $Graphics.DrawLine($pen, $cx + $r * 1.5, $cy + $r * 0.2, $cx + $r * 1.5, $cy + $r * 1.8)
                return $true
            }
            "gmk_dot_matrix" {
                for ($row = -1; $row -le 1; $row++) {
                    for ($col = -1; $col -le 1; $col++) {
                        Draw-DotGlyph -Graphics $Graphics -Brush $brush -Cx ($cx + $col * $r * 1.45) -Cy ($cy + $row * $r * 1.45) -Diameter ($diameter * 0.42)
                    }
                }
                return $true
            }
            "gmk_corner_dots" {
                foreach ($pt in @(@(-2.4, -1.8), @(-1.0, -1.8), @(-2.4, -0.4), @(2.4, 1.8))) {
                    Draw-DotGlyph -Graphics $Graphics -Brush $brush -Cx ($cx + $r * $pt[0]) -Cy ($cy + $r * $pt[1]) -Diameter ($diameter * 0.55)
                }
                return $true
            }
            "gmk_side_stripes" {
                $Graphics.DrawLine($pen, $cx - $r * 2.9, $cy - $r * 2.0, $cx - $r * 2.9, $cy + $r * 2.0)
                $Graphics.DrawLine($pen, $cx + $r * 2.9, $cy - $r * 2.0, $cx + $r * 2.9, $cy + $r * 2.0)
                return $true
            }
            "gmk_center_cross" {
                $Graphics.DrawEllipse($pen, $cx - $r * 2.5, $cy - $r * 2.5, $r * 5.0, $r * 5.0)
                $Graphics.DrawLine($pen, $cx - $r * 1.5, $cy, $cx + $r * 1.5, $cy)
                $Graphics.DrawLine($pen, $cx, $cy - $r * 1.5, $cx, $cy + $r * 1.5)
                return $true
            }
            "gmk_arcade_diamond" {
                Draw-PointGlyphPreview -Graphics $Graphics -Glyph "diamond" -Color $Color -X $X -Y $Y -W $W -H $H | Out-Null
                $Graphics.DrawLine($pen, $cx - $r * 2.6, $cy, $cx - $r * 1.7, $cy)
                $Graphics.DrawLine($pen, $cx + $r * 1.7, $cy, $cx + $r * 2.6, $cy)
                return $true
            }
            "gmk_iso_enter_mark" {
                $Graphics.DrawLines($pen, @([System.Drawing.PointF]::new($cx + $r * 2.0, $cy - $r * 2.1), [System.Drawing.PointF]::new($cx + $r * 2.0, $cy + $r * 0.8), [System.Drawing.PointF]::new($cx - $r * 2.0, $cy + $r * 0.8), [System.Drawing.PointF]::new($cx - $r * 0.8, $cy - $r * 0.4)))
                $Graphics.DrawLine($pen, $cx - $r * 2.0, $cy + $r * 0.8, $cx - $r * 0.8, $cy + $r * 2.0)
                return $true
            }
            "gmk_split_bar" {
                $Graphics.DrawLine($pen, $cx - $r * 3.4, $cy + $r * 0.75, $cx - $r * 0.45, $cy + $r * 0.75)
                $Graphics.DrawLine($pen, $cx + $r * 0.45, $cy + $r * 0.75, $cx + $r * 3.4, $cy + $r * 0.75)
                return $true
            }
            "gmk_long_bar_ticks" {
                $Graphics.DrawLine($pen, $cx - $r * 3.4, $cy, $cx + $r * 3.4, $cy)
                $Graphics.DrawLine($pen, $cx - $r * 1.7, $cy - $r * 0.9, $cx - $r * 1.7, $cy + $r * 0.9)
                $Graphics.DrawLine($pen, $cx + $r * 1.7, $cy - $r * 0.9, $cx + $r * 1.7, $cy + $r * 0.9)
                return $true
            }
            "gmk_stepped_bar" {
                $Graphics.DrawLines($pen, @([System.Drawing.PointF]::new($cx - $r * 3.0, $cy + $r * 1.4), [System.Drawing.PointF]::new($cx - $r * 0.8, $cy + $r * 1.4), [System.Drawing.PointF]::new($cx - $r * 0.8, $cy - $r * 0.2), [System.Drawing.PointF]::new($cx + $r * 2.7, $cy - $r * 0.2)))
                return $true
            }
            "gmk_rising_blocks" {
                for ($i = 0; $i -lt 4; $i++) {
                    $h = $r * (0.9 + $i * 0.55)
                    $x = $cx - $r * 2.4 + $i * $r * 1.5
                    $Graphics.FillRectangle($brush, $x, $cy + $r * 1.8 - $h, $r * 0.8, $h)
                }
                return $true
            }
            "gmk_equalizer" {
                $Graphics.DrawLine($pen, $cx - $r * 2.2, $cy + $r * 1.8, $cx - $r * 2.2, $cy - $r * 0.8)
                $Graphics.DrawLine($pen, $cx, $cy + $r * 1.8, $cx, $cy - $r * 1.8)
                $Graphics.DrawLine($pen, $cx + $r * 2.2, $cy + $r * 1.8, $cx + $r * 2.2, $cy - $r * 0.2)
                return $true
            }
            "gmk_wave_double" {
                Draw-PointGlyphPreview -Graphics $Graphics -Glyph "gmk_wave" -Color $Color -X $X -Y ($Y - $r * 0.65) -W $W -H $H | Out-Null
                Draw-PointGlyphPreview -Graphics $Graphics -Glyph "gmk_wave" -Color $Color -X $X -Y ($Y + $r * 0.65) -W $W -H $H | Out-Null
                return $true
            }
            "gmk_flower_alt" {
                $Graphics.DrawEllipse($pen, $cx - $r * 2.05, $cy - $r * 1.85, $r * 2.1, $r * 2.1)
                $Graphics.DrawEllipse($pen, $cx - $r * 0.05, $cy - $r * 1.85, $r * 2.1, $r * 2.1)
                $Graphics.DrawEllipse($pen, $cx - $r * 1.05, $cy - $r * 0.05, $r * 2.1, $r * 2.1)
                Draw-DotGlyph -Graphics $Graphics -Brush $brush -Cx $cx -Cy $cy -Diameter ($diameter * 0.55)
                return $true
            }
            "gmk_leaf_pair" {
                Draw-PointGlyphPreview -Graphics $Graphics -Glyph "gmk_leaf" -Color $Color -X ($X - $r * 1.25) -Y $Y -W $W -H $H | Out-Null
                Draw-PointGlyphPreview -Graphics $Graphics -Glyph "gmk_leaf" -Color $Color -X ($X + $r * 1.25) -Y $Y -W $W -H $H | Out-Null
                return $true
            }
            "gmk_sprout" {
                $Graphics.DrawLine($pen, $cx, $cy + $r * 2.2, $cx, $cy - $r * 0.7)
                $path = [System.Drawing.Drawing2D.GraphicsPath]::new()
                $path.AddBezier($cx, $cy - $r * 0.4, $cx - $r * 2.2, $cy - $r * 1.6, $cx - $r * 2.4, $cy + $r * 0.6, $cx, $cy + $r * 0.25)
                $path.AddBezier($cx, $cy - $r * 0.8, $cx + $r * 2.2, $cy - $r * 2.0, $cx + $r * 2.4, $cy + $r * 0.2, $cx, $cy - $r * 0.05)
                $Graphics.DrawPath($pen, $path)
                $path.Dispose()
                return $true
            }
            "gmk_petals" {
                for ($i = 0; $i -lt 5; $i++) {
                    $a = [Math]::PI * 2.0 * $i / 5.0 - [Math]::PI / 2.0
                    Draw-DotGlyph -Graphics $Graphics -Brush $brush -Cx ($cx + [Math]::Cos($a) * $r * 1.35) -Cy ($cy + [Math]::Sin($a) * $r * 1.35) -Diameter ($diameter * 0.72)
                }
                return $true
            }
            "gmk_rain" {
                $Graphics.DrawLine($pen, $cx - $r * 1.7, $cy - $r * 1.9, $cx - $r * 2.4, $cy - $r * 0.6)
                $Graphics.DrawLine($pen, $cx, $cy - $r * 0.9, $cx - $r * 0.7, $cy + $r * 0.4)
                $Graphics.DrawLine($pen, $cx + $r * 1.7, $cy + $r * 0.1, $cx + $r * 1.0, $cy + $r * 1.4)
                return $true
            }
            "gmk_snow" {
                $Graphics.DrawLine($pen, $cx - $r * 2.3, $cy, $cx + $r * 2.3, $cy)
                $Graphics.DrawLine($pen, $cx - $r * 1.15, $cy - $r * 2.0, $cx + $r * 1.15, $cy + $r * 2.0)
                $Graphics.DrawLine($pen, $cx + $r * 1.15, $cy - $r * 2.0, $cx - $r * 1.15, $cy + $r * 2.0)
                return $true
            }
            "gmk_cloud" {
                $path = [System.Drawing.Drawing2D.GraphicsPath]::new()
                $path.AddBezier($cx - $r * 2.7, $cy + $r, $cx - $r * 2.7, $cy - $r * 0.2, $cx - $r * 1.4, $cy - $r * 0.3, $cx - $r * 1.1, $cy - $r * 0.8)
                $path.AddBezier($cx - $r * 1.1, $cy - $r * 0.8, $cx - $r * 0.6, $cy - $r * 2.0, $cx + $r, $cy - $r * 1.8, $cx + $r * 1.2, $cy - $r * 0.6)
                $path.AddBezier($cx + $r * 1.2, $cy - $r * 0.6, $cx + $r * 2.8, $cy - $r * 0.8, $cx + $r * 3.0, $cy + $r, $cx + $r * 1.6, $cy + $r)
                $Graphics.DrawPath($pen, $path)
                $path.Dispose()
                return $true
            }
            "gmk_flame" {
                $points = @([System.Drawing.PointF]::new($cx, $cy - $r * 2.8), [System.Drawing.PointF]::new($cx + $r * 1.9, $cy + $r * 2.4), [System.Drawing.PointF]::new($cx, $cy + $r * 2.5), [System.Drawing.PointF]::new($cx - $r * 1.8, $cy + $r * 1.9))
                $Graphics.FillPolygon($brush, $points)
                return $true
            }
            "gmk_bolt" {
                $points = @([System.Drawing.PointF]::new($cx + $r * 0.6, $cy - $r * 3.0), [System.Drawing.PointF]::new($cx - $r * 1.4, $cy + $r * 0.4), [System.Drawing.PointF]::new($cx + $r * 0.2, $cy + $r * 0.4), [System.Drawing.PointF]::new($cx - $r * 0.6, $cy + $r * 3.0), [System.Drawing.PointF]::new($cx + $r * 1.8, $cy - $r * 0.7), [System.Drawing.PointF]::new($cx + $r * 0.3, $cy - $r * 0.7))
                $Graphics.FillPolygon($brush, $points)
                return $true
            }
            "gmk_crystal" {
                $points = @([System.Drawing.PointF]::new($cx, $cy - $r * 2.7), [System.Drawing.PointF]::new($cx + $r * 2.1, $cy - $r * 0.5), [System.Drawing.PointF]::new($cx + $r * 1.2, $cy + $r * 2.5), [System.Drawing.PointF]::new($cx - $r * 1.2, $cy + $r * 2.5), [System.Drawing.PointF]::new($cx - $r * 2.1, $cy - $r * 0.5))
                $Graphics.DrawPolygon($pen, $points)
                $Graphics.DrawLine($pen, $cx, $cy - $r * 2.7, $cx, $cy + $r * 2.5)
                return $true
            }
            "gmk_compass" {
                $Graphics.DrawEllipse($pen, $cx - $r * 2.6, $cy - $r * 2.6, $r * 5.2, $r * 5.2)
                $points = @([System.Drawing.PointF]::new($cx, $cy - $r * 2.0), [System.Drawing.PointF]::new($cx + $r * 0.75, $cy + $r * 0.75), [System.Drawing.PointF]::new($cx, $cy + $r * 0.25), [System.Drawing.PointF]::new($cx - $r * 0.75, $cy + $r * 0.75))
                $Graphics.FillPolygon($brush, $points)
                return $true
            }
            "gmk_lab_flask" {
                $points = @([System.Drawing.PointF]::new($cx - $r * 0.8, $cy - $r * 2.4), [System.Drawing.PointF]::new($cx + $r * 0.8, $cy - $r * 2.4), [System.Drawing.PointF]::new($cx + $r * 0.4, $cy - $r * 0.4), [System.Drawing.PointF]::new($cx + $r * 2.0, $cy + $r * 2.3), [System.Drawing.PointF]::new($cx - $r * 2.0, $cy + $r * 2.3), [System.Drawing.PointF]::new($cx - $r * 0.4, $cy - $r * 0.4))
                $Graphics.DrawPolygon($pen, $points)
                return $true
            }
        }
    } finally {
        $brush.Dispose()
        $pen.Dispose()
    }
    return $false
}

function Draw-PreviewIcon {
    param(
        [System.Drawing.Graphics] $Graphics,
        [string] $Icon,
        [System.Drawing.Color] $Color,
        [float] $X,
        [float] $Y,
        [float] $W,
        [float] $H
    )
    $size = [Math]::Min($W, $H) * 0.46
    $cx = $X + $W / 2
    $cy = $Y + $H / 2
    $left = $cx - $size / 2
    $top = $cy - $size / 2
    $right = $cx + $size / 2
    $bottom = $cy + $size / 2
    $pen = [System.Drawing.Pen]::new($Color, [Math]::Max(1.6, $size / 9))
    $brush = [System.Drawing.SolidBrush]::new($Color)
    try {
        $pen.StartCap = [System.Drawing.Drawing2D.LineCap]::Round
        $pen.EndCap = [System.Drawing.Drawing2D.LineCap]::Round
        $pen.LineJoin = [System.Drawing.Drawing2D.LineJoin]::Round
        switch ($Icon) {
            "shift" {
                $path = [System.Drawing.Drawing2D.GraphicsPath]::new()
                try {
                    $path.AddLines(@(
                        [System.Drawing.PointF]::new($cx, $top),
                        [System.Drawing.PointF]::new($right, $cy),
                        [System.Drawing.PointF]::new($cx + $size * 0.22, $cy),
                        [System.Drawing.PointF]::new($cx + $size * 0.22, $bottom),
                        [System.Drawing.PointF]::new($cx - $size * 0.22, $bottom),
                        [System.Drawing.PointF]::new($cx - $size * 0.22, $cy),
                        [System.Drawing.PointF]::new($left, $cy),
                        [System.Drawing.PointF]::new($cx, $top)
                    ))
                    $Graphics.DrawPath($pen, $path)
                } finally {
                    $path.Dispose()
                }
            }
            "backspace" {
                $path = [System.Drawing.Drawing2D.GraphicsPath]::new()
                try {
                    $path.AddLines(@(
                        [System.Drawing.PointF]::new($left + $size * 0.22, $cy),
                        [System.Drawing.PointF]::new($left + $size * 0.42, $top + $size * 0.15),
                        [System.Drawing.PointF]::new($right, $top + $size * 0.15),
                        [System.Drawing.PointF]::new($right, $bottom - $size * 0.15),
                        [System.Drawing.PointF]::new($left + $size * 0.42, $bottom - $size * 0.15),
                        [System.Drawing.PointF]::new($left + $size * 0.22, $cy)
                    ))
                    $Graphics.DrawPath($pen, $path)
                    $Graphics.DrawLine($pen, $cx - $size * 0.05, $cy - $size * 0.15, $cx + $size * 0.20, $cy + $size * 0.15)
                    $Graphics.DrawLine($pen, $cx + $size * 0.20, $cy - $size * 0.15, $cx - $size * 0.05, $cy + $size * 0.15)
                } finally {
                    $path.Dispose()
                }
            }
            "options" {
                for ($i = 0; $i -lt 3; $i++) {
                    $yy = $top + $size * (0.25 + $i * 0.25)
                    $Graphics.DrawLine($pen, $left, $yy, $right, $yy)
                    $dotX = if ($i -eq 1) { $cx + $size * 0.18 } else { $cx - $size * 0.18 }
                    $Graphics.FillEllipse($brush, $dotX - $size * 0.055, $yy - $size * 0.055, $size * 0.11, $size * 0.11)
                }
            }
            "reserved" {
                $path = [System.Drawing.Drawing2D.GraphicsPath]::new()
                try {
                    $path.AddLines(@(
                        [System.Drawing.PointF]::new($left + $size * 0.2, $top),
                        [System.Drawing.PointF]::new($right - $size * 0.2, $top),
                        [System.Drawing.PointF]::new($right - $size * 0.2, $bottom),
                        [System.Drawing.PointF]::new($cx, $bottom - $size * 0.22),
                        [System.Drawing.PointF]::new($left + $size * 0.2, $bottom),
                        [System.Drawing.PointF]::new($left + $size * 0.2, $top)
                    ))
                    $Graphics.DrawPath($pen, $path)
                } finally {
                    $path.Dispose()
                }
            }
            "space" {
                $barW = [Math]::Min($W * 0.26, $size * 1.8)
                $barH = $size * 0.22
                $Graphics.DrawLine($pen, $cx - $barW / 2, $cy + $barH, $cx + $barW / 2, $cy + $barH)
                $Graphics.DrawLine($pen, $cx - $barW / 2, $cy + $barH, $cx - $barW / 2, $cy)
                $Graphics.DrawLine($pen, $cx + $barW / 2, $cy + $barH, $cx + $barW / 2, $cy)
            }
            "language" {
                $Graphics.DrawEllipse($pen, $left, $top, $size, $size)
                $Graphics.DrawLine($pen, $left, $cy, $right, $cy)
                $Graphics.DrawEllipse($pen, $left + $size * 0.28, $top, $size * 0.44, $size)
            }
            "settings" {
                $Graphics.DrawEllipse($pen, $cx - $size * 0.18, $cy - $size * 0.18, $size * 0.36, $size * 0.36)
                for ($i = 0; $i -lt 6; $i++) {
                    $angle = [Math]::PI * 2 * $i / 6
                    $x1 = $cx + [Math]::Cos($angle) * $size * 0.34
                    $y1 = $cy + [Math]::Sin($angle) * $size * 0.34
                    $x2 = $cx + [Math]::Cos($angle) * $size * 0.48
                    $y2 = $cy + [Math]::Sin($angle) * $size * 0.48
                    $Graphics.DrawLine($pen, $x1, $y1, $x2, $y2)
                }
            }
            "enter" {
                $Graphics.DrawLine($pen, $right - $size * 0.12, $top + $size * 0.12, $right - $size * 0.12, $cy + $size * 0.18)
                $Graphics.DrawLine($pen, $right - $size * 0.12, $cy + $size * 0.18, $left + $size * 0.18, $cy + $size * 0.18)
                $Graphics.DrawLine($pen, $left + $size * 0.18, $cy + $size * 0.18, $left + $size * 0.38, $cy)
                $Graphics.DrawLine($pen, $left + $size * 0.18, $cy + $size * 0.18, $left + $size * 0.38, $cy + $size * 0.36)
            }
        }
    } finally {
        $pen.Dispose()
        $brush.Dispose()
    }
}

function Draw-Key {
    param(
        [System.Drawing.Graphics] $Graphics,
        [object] $Theme,
        [string] $Label,
        [string] $Role,
        [float] $X,
        [float] $Y,
        [float] $W,
        [float] $H,
        [float] $Radius,
        [System.Drawing.Font] $Font
    )

    $border = Convert-ThemeColor $Theme.colors.border "#696969"
    $overrideFill = Get-KeyBackgroundOverrideColor -Theme $Theme -Label $Label
    $fill = if ($null -ne $overrideFill) { $overrideFill } else { Get-RoleColor -Theme $Theme -Role $Role }
    $accent = Convert-ThemeColor $Theme.colors.accent "#232323"
    $overrideText = Get-KeyOverrideColor -Theme $Theme -Label $Label
    $textColor = if ($null -ne $overrideText) { $overrideText } else { Get-RoleTextColor -Theme $Theme -Role $Role }
    $depthColor = Get-ThemeDepthColor -Theme $Theme -Fill $fill

    $depthEnabled = [bool]$Theme.shape.depthEnabled
    $depthDp = [int]$Theme.shape.depthDp
    if ($depthEnabled -and $depthDp -gt 0) {
        $depthBrush = [System.Drawing.SolidBrush]::new($depthColor)
        try {
            Draw-RoundRect -Graphics $Graphics -Brush $depthBrush -Pen $null -X $X -Y ($Y + $depthDp) -W $W -H $H -Radius $Radius
        } finally {
            $depthBrush.Dispose()
        }
    }

    $fillBrush = New-KeyFaceBrush -Theme $Theme -Fill $fill -X $X -Y $Y -W $W -H $H
    $borderWidth = [Math]::Max(0, (Get-ThemeInt $Theme.shape.borderWidthDp 1)) * 1.2
    $borderPen = if ($borderWidth -gt 0) { [System.Drawing.Pen]::new($border, $borderWidth) } else { $null }
    $textBrush = [System.Drawing.SolidBrush]::new($textColor)
    try {
        Draw-RoundRect -Graphics $Graphics -Brush $fillBrush -Pen $null -X $X -Y $Y -W $W -H $H -Radius $Radius
        if ($null -ne $borderPen) {
            $inset = $borderWidth / 2.0
            Draw-RoundRect `
                    -Graphics $Graphics `
                    -Brush $null `
                    -Pen $borderPen `
                    -X ($X + $inset) `
                    -Y ($Y + $inset) `
                    -W ([Math]::Max(1, $W - $borderWidth)) `
                    -H ([Math]::Max(1, $H - $borderWidth)) `
                    -Radius ([Math]::Max(0, $Radius - $inset))
        }
        $icon = Get-PreviewIconName $Label
        if (Test-DotLegendLabel -Theme $Theme -Label $Label) {
            $diameter = Get-GlyphDotDiameter -H $H
            if ($Label -eq "." -or $Label -eq "/") {
                $cx = $X + $W / 2.0
                $cy = $Y + $H / 2.0
                Draw-TwoDotGlyph -Graphics $Graphics -Brush $textBrush -Cx $cx -Cy $cy -Diameter $diameter
            } else {
                Draw-DotGlyph `
                        -Graphics $Graphics `
                        -Brush $textBrush `
                        -Cx ($X + $W / 2.0) `
                        -Cy ($Y + $H / 2.0) `
                        -Diameter $diameter
            }
        } elseif (-not [string]::IsNullOrWhiteSpace($icon) -and (Draw-KeyDisplayPackPreview -Graphics $Graphics -Theme $Theme -Icon $icon -Color $textColor -X $X -Y $Y -W $W -H $H)) {
            # rendered by key display pack
        } elseif ([string]::IsNullOrWhiteSpace($icon) -and (Draw-LabelDisplayPackPreview -Graphics $Graphics -Theme $Theme -Label $Label -Color $textColor -X $X -Y $Y -W $W -H $H)) {
            # rendered by key display pack
        } elseif (-not [string]::IsNullOrWhiteSpace($icon) -and (Draw-PackPreviewIcon -Graphics $Graphics -Theme $Theme -Icon $icon -Color $textColor -X $X -Y $Y -W $W -H $H)) {
            # rendered by icon pack
        } elseif ([string]::IsNullOrWhiteSpace($icon)) {
            Draw-CenteredText -Graphics $Graphics -Text $Label -Font $Font -Brush $textBrush -X $X -Y $Y -W $W -H $H
        } else {
            Draw-PreviewIcon -Graphics $Graphics -Icon $icon -Color $textColor -X $X -Y $Y -W $W -H $H
        }
    } finally {
        $fillBrush.Dispose()
        if ($null -ne $borderPen) {
            $borderPen.Dispose()
        }
        $textBrush.Dispose()
    }
}

function Draw-KeyboardBackground {
    param(
        [System.Drawing.Graphics] $Graphics,
        [object] $Theme,
        [float] $X,
        [float] $Y,
        [float] $W,
        [float] $H
    )
    $brush = New-PanelBackgroundBrush -Theme $Theme -X $X -Y $Y -W $W -H $H
    $pen = [System.Drawing.Pen]::new((Convert-ThemeColor $Theme.colors.border "#696969"), 1)
    try {
        Draw-RoundRect -Graphics $Graphics -Brush $brush -Pen $pen -X $X -Y $Y -W $W -H $H -Radius 18
    } finally {
        $brush.Dispose()
        $pen.Dispose()
    }
}

function Draw-QwertySample {
    param([System.Drawing.Graphics] $Graphics, [object] $Theme, [float] $X, [float] $Y, [float] $W, [float] $H)
    Draw-KeyboardBackground -Graphics $Graphics -Theme $Theme -X $X -Y $Y -W $W -H $H

    $radius = [Math]::Max(0, [int]$Theme.shape.roundnessDp) * 2.1
    $gap = [Math]::Max(0, [int]$Theme.shape.keyGapDp) * 1.55
    $leftPadding = 0
    $rightPadding = 0
    $bottomPadding = 0
    $bottomRowTopPadding = 0
    $font = New-ThemeFont -Theme $Theme -BaseSize 11 -Primary $true
    try {
        $rowH = ($H - 40 - $bottomPadding - $bottomRowTopPadding) / 5
        $unit = ($W - 36 - $leftPadding - $rightPadding) / 20
        $startX = $X + 18 + $leftPadding
        $rowY = $Y + 18
        $rowIndex = 0
        $rows = @(
            @(@("1",2,"number"), @("2",2,"number"), @("3",2,"number"), @("4",2,"number"), @("5",2,"number"), @("6",2,"number"), @("7",2,"number"), @("8",2,"number"), @("9",2,"number"), @("0",2,"number")),
            @(@("q",2,"normal"), @("w",2,"normal"), @("e",2,"pressed"), @("r",2,"normal"), @("t",2,"normal"), @("y",2,"normal"), @("u",2,"normal"), @("i",2,"normal"), @("o",2,"normal"), @("p",2,"normal")),
            @(@("a",2,"normal"), @("s",2,"normal"), @("d",2,"normal"), @("f",2,"normal"), @("g",2,"normal"), @("h",2,"normal"), @("j",2,"normal"), @("k",2,"normal"), @("l",2,"normal")),
            @(@("shift",3,"modifier"), @("z",2,"normal"), @("x",2,"normal"), @("c",2,"normal"), @("v",2,"normal"), @("b",2,"normal"), @("n",2,"normal"), @("m",2,"normal"), @("bksp",3,"modifier")),
            @(@("settings",3,"modifier"), @("reserved",2,"modifier"), @("space",10,"normal"), @("language",2,"modifier"), @("enter",3,"modifier"))
        )

        foreach ($row in $rows) {
            if ($rowIndex -eq ($rows.Count - 1)) {
                $rowY += $bottomRowTopPadding
            }
            $rowUnits = 0
            foreach ($key in $row) {
                $rowUnits += [int]$key[1]
            }
            $xOffset = $startX + ((20 - $rowUnits) * $unit / 2)
            $isBottomRow = $rowIndex -eq ($rows.Count - 1)
            $spaceIndex = -1
            for ($i = 0; $i -lt $row.Count; $i++) {
                if ([string]$row[$i][0] -eq "space") {
                    $spaceIndex = $i
                    break
                }
            }
            $keyIndex = 0
            foreach ($key in $row) {
                $label = [string]$key[0]
                $role = [string]$key[2]
                if ($role -eq "number") {
                    $role = Get-NumberRowRole -Theme $Theme -Label $label
                }
                $role = Resolve-PreviewRole -Theme $Theme -Layout "qwerty" -Label $label -BaseRole $role
                $keyX = $xOffset + $gap / 2
                $keyW = [int]$key[1] * $unit - $gap
                if ($isBottomRow) {
                    $keyX = $xOffset
                    $keyW = [int]$key[1] * $unit
                    if ($spaceIndex -ge 0 -and $keyIndex -ne $spaceIndex) {
                        if ($keyIndex -lt $spaceIndex) {
                            $keyW -= $gap
                        } else {
                            $keyX += $gap
                            $keyW -= $gap
                        }
                    }
                }
                Draw-Key -Graphics $Graphics -Theme $Theme -Label $label -Role $role `
                    -X $keyX -Y ($rowY + $gap / 2) `
                    -W $keyW -H ($rowH - $gap) -Radius $radius -Font $font
                $xOffset += [int]$key[1] * $unit
                $keyIndex++
            }
            $rowY += $rowH
            $rowIndex++
        }
    } finally {
        $font.Dispose()
    }
}

function Draw-DingulSample {
    param([System.Drawing.Graphics] $Graphics, [object] $Theme, [float] $X, [float] $Y, [float] $W, [float] $H)
    Draw-KeyboardBackground -Graphics $Graphics -Theme $Theme -X $X -Y $Y -W $W -H $H

    $radius = [Math]::Max(0, [int]$Theme.shape.roundnessDp) * 2.1
    $gap = [Math]::Max(0, [int]$Theme.shape.keyGapDp) * 1.55
    $leftPadding = 0
    $rightPadding = 0
    $mainSpecialGap = 8 * 1.55
    $bottomPadding = 0
    $bottomRowTopPadding = 0
    $font = New-ThemeFont -Theme $Theme -BaseSize 11 -Primary $true
    try {
        $rowH = ($H - 32 - $bottomPadding - $bottomRowTopPadding) / 6
        $unit = ($W - 36 - $leftPadding - $rightPadding - $mainSpecialGap) / 300
        $bottomUnit = ($W - 36 - $leftPadding - $rightPadding) / 300
        $startX = $X + 18 + $leftPadding
        $rowY = $Y + 16

        $g = [string][char]0x3131
        $n = [string][char]0x3134
        $i = [string][char]0x3163
        $r = [string][char]0x3139
        $m = [string][char]0x3141
        $s = [string][char]0x3145
        $o = [string][char]0x3147
        $eu = [string][char]0x3161
        $ui = [string][char]0x3162
        $ae = [string][char]0x3150
        $j = [string][char]0x3148
        $hieut = [string][char]0x314E

        $rows = @(
            @(@("1",30,"number"), @("2",30,"number"), @("3",30,"number"), @("4",30,"number"), @("5",30,"number"), @("6",30,"number"), @("7",30,"number"), @("8",30,"number"), @("9",30,"number"), @("0",30,"number")),
            @(@($g,83,"normal"), @($n,83,"normal"), @($ui,83,"normal"), @("bksp",51,"modifier")),
            @(@($r,83,"normal"), @($m,83,"normal"), @("$i.",83,"normal"), @("?",51,"normal")),
            @(@($s,83,"normal"), @($o,83,"pressed"), @("$eu$ae",83,"normal"), @(".",51,"modifier")),
            @(@($j,83,"normal"), @($hieut,83,"normal"), @("..",83,"normal"), @("/",51,"modifier")),
            @(@("settings",45,"modifier"), @("reserved",30,"modifier"), @("space",150,"normal"), @("language",30,"modifier"), @("enter",45,"modifier"))
        )

        $rowIndex = 0
        foreach ($row in $rows) {
            if ($rowIndex -eq ($rows.Count - 1)) {
                $rowY += $bottomRowTopPadding
            }
            $activeUnit = if ($rowIndex -eq ($rows.Count - 1)) { $bottomUnit } else { $unit }
            if ($rowIndex -eq 0) {
                $activeUnit = $bottomUnit
            }
            $xOffset = $startX
            $isBottomRow = $rowIndex -eq ($rows.Count - 1)
            $spaceIndex = -1
            for ($i = 0; $i -lt $row.Count; $i++) {
                if ([string]$row[$i][0] -eq "space") {
                    $spaceIndex = $i
                    break
                }
            }
            $keyIndex = 0
            foreach ($key in $row) {
                $label = [string]$key[0]
                $role = [string]$key[2]
                if ($role -eq "number") {
                    $role = Get-NumberRowRole -Theme $Theme -Label $label
                }
                $role = Resolve-PreviewRole -Theme $Theme -Layout "dingul" -Label $label -BaseRole $role
                $keyX = $xOffset + $gap / 2
                $keyW = [int]$key[1] * $activeUnit - $gap
                if ($isBottomRow) {
                    $keyX = $xOffset
                    $keyW = [int]$key[1] * $activeUnit
                    if ($spaceIndex -ge 0 -and $keyIndex -ne $spaceIndex) {
                        if ($keyIndex -lt $spaceIndex) {
                            $keyW -= $gap
                        } else {
                            $keyX += $gap
                            $keyW -= $gap
                        }
                    }
                }
                Draw-Key -Graphics $Graphics -Theme $Theme -Label $label -Role $role `
                    -X $keyX -Y ($rowY + $gap / 2) `
                    -W $keyW -H ($rowH - $gap) -Radius $radius -Font $font
                $xOffset += [int]$key[1] * $activeUnit
                if ($rowIndex -gt 0 -and $rowIndex -lt ($rows.Count - 1) -and $keyIndex -eq 2) {
                    $xOffset += $mainSpecialGap
                }
                $keyIndex++
            }
            $rowY += $rowH
            $rowIndex++
        }
    } finally {
        $font.Dispose()
    }
}

function Draw-ThemeCard {
    param([System.Drawing.Graphics] $Graphics, [object] $Theme, [float] $X, [float] $Y, [float] $W, [float] $H)
    $cardBrush = [System.Drawing.SolidBrush]::new([System.Drawing.ColorTranslator]::FromHtml("#FFFFFF"))
    $cardPen = [System.Drawing.Pen]::new([System.Drawing.ColorTranslator]::FromHtml("#E5E7EB"), 1)
    $titleBrush = [System.Drawing.SolidBrush]::new([System.Drawing.ColorTranslator]::FromHtml("#111827"))
    $metaBrush = [System.Drawing.SolidBrush]::new([System.Drawing.ColorTranslator]::FromHtml("#6B7280"))
    $titleFont = [System.Drawing.Font]::new("Segoe UI", 18, [System.Drawing.FontStyle]::Bold, [System.Drawing.GraphicsUnit]::Point)
    $labelFont = [System.Drawing.Font]::new("Segoe UI", 9, [System.Drawing.FontStyle]::Bold, [System.Drawing.GraphicsUnit]::Point)
    try {
        Draw-RoundRect -Graphics $Graphics -Brush $cardBrush -Pen $cardPen -X $X -Y $Y -W $W -H $H -Radius 18
        $Graphics.DrawString([string]$Theme.name, $titleFont, $titleBrush, $X + 24, $Y + 18)
        $hangulHeight = 260
        $englishHeight = 186
        $heightRatio = [Math]::Max(1.18, [Math]::Min(1.35, $hangulHeight / [Math]::Max(1, $englishHeight)))
        $qwertyPreviewH = 190
        $dingulPreviewH = [Math]::Round($qwertyPreviewH * $heightRatio)
        $Graphics.DrawString("QWERTY preview - e = pressed state", $labelFont, $metaBrush, $X + 24, $Y + 58)
        Draw-QwertySample -Graphics $Graphics -Theme $Theme -X ($X + 24) -Y ($Y + 82) -W ($W - 48) -H $qwertyPreviewH
        $dingulLabelY = $Y + 82 + $qwertyPreviewH + 20
        $Graphics.DrawString("Dingul preview - number row + semantic accent policy", $labelFont, $metaBrush, $X + 24, $dingulLabelY)
        Draw-DingulSample -Graphics $Graphics -Theme $Theme -X ($X + 24) -Y ($dingulLabelY + 24) -W ($W - 48) -H $dingulPreviewH
    } finally {
        $cardBrush.Dispose()
        $cardPen.Dispose()
        $titleBrush.Dispose()
        $metaBrush.Dispose()
        $titleFont.Dispose()
        $labelFont.Dispose()
    }
}

$themeOrder = @(
    "ios-clean-light.json",
    "ios-clean-dark.json",
    "macos-frost-light.json",
    "macos-graphite-dark.json",
    "android-material-light.json",
    "android-material-dark.json",
    "paper-mono-flat.json",
    "amoled-black.json",
    "nord-snow.json",
    "nord-night.json",
    "slate-glass.json",
    "mint-air.json",
    "lavender-focus.json",
    "graphite-mono.json",
    "high-contrast-light.json",
    "gmk-bento.json",
    "gmk-metropolis.json",
    "gmk-oblivion.json",
    "gmk-oblivion-hagoromo.json",
    "gmk-8008.json",
    "gmk-hammerhead.json",
    "gmk-dracula.json",
    "gmk-modern-dolch.json",
    "gmk-olivia-light.json",
    "gmk-olivia-dark.json",
    "gmk-dots-light.json",
    "gmk-dots-dark.json",
    "marigold-fiesta-dark.json",
    "marigold-fiesta-light.json"
)

$allThemeFiles = Get-ChildItem -LiteralPath $ThemeDir -Filter "*.json"
$themeFiles = @()
foreach ($name in $themeOrder) {
    $match = $allThemeFiles | Where-Object { $_.Name -eq $name } | Select-Object -First 1
    if ($null -ne $match) {
        $themeFiles += $match
    }
}
$themeFiles += $allThemeFiles | Where-Object { $themeOrder -notcontains $_.Name } | Sort-Object Name
if ($themeFiles.Count -eq 0) {
    throw "No theme JSON files found under $ThemeDir."
}

$themes = foreach ($file in $themeFiles) {
    Get-Content -Raw -Encoding UTF8 -LiteralPath $file.FullName | ConvertFrom-Json
}

$cols = 2
$cardW = 820
$cardH = 640
$gap = 28
$margin = 32
$rows = [int][Math]::Ceiling($themes.Count / $cols)
$canvasW = $margin * 2 + $cols * $cardW + ($cols - 1) * $gap
$canvasH = $margin * 2 + $rows * $cardH + ($rows - 1) * $gap

$bitmap = [System.Drawing.Bitmap]::new($canvasW, $canvasH)
$graphics = [System.Drawing.Graphics]::FromImage($bitmap)
try {
    $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $graphics.TextRenderingHint = [System.Drawing.Text.TextRenderingHint]::ClearTypeGridFit
    $graphics.Clear([System.Drawing.ColorTranslator]::FromHtml("#F6F7F9"))

    for ($n = 0; $n -lt $themes.Count; $n++) {
        $col = $n % $cols
        $row = [int][Math]::Floor($n / $cols)
        Draw-ThemeCard -Graphics $graphics -Theme $themes[$n] `
            -X ($margin + $col * ($cardW + $gap)) `
            -Y ($margin + $row * ($cardH + $gap)) `
            -W $cardW -H $cardH
    }

    $gridPath = Join-Path $OutputDir "theme-preview-grid.png"
    $bitmap.Save($gridPath, [System.Drawing.Imaging.ImageFormat]::Png)
} finally {
    $graphics.Dispose()
    $bitmap.Dispose()
}

foreach ($theme in $themes) {
    $safeName = ([string]$theme.name).ToLowerInvariant() -replace "[^a-z0-9]+", "-"
    $safeName = $safeName.Trim("-")
    $singleBitmap = [System.Drawing.Bitmap]::new($cardW, $cardH)
    $singleGraphics = [System.Drawing.Graphics]::FromImage($singleBitmap)
    try {
        $singleGraphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
        $singleGraphics.TextRenderingHint = [System.Drawing.Text.TextRenderingHint]::ClearTypeGridFit
        $singleGraphics.Clear([System.Drawing.ColorTranslator]::FromHtml("#F6F7F9"))
        Draw-ThemeCard -Graphics $singleGraphics -Theme $theme -X 0 -Y 0 -W $cardW -H $cardH
        $singleBitmap.Save((Join-Path $OutputDir "$safeName.png"), [System.Drawing.Imaging.ImageFormat]::Png)
    } finally {
        $singleGraphics.Dispose()
        $singleBitmap.Dispose()
    }
}

Write-Host "Theme preview grid: $(Join-Path $OutputDir 'theme-preview-grid.png')"
