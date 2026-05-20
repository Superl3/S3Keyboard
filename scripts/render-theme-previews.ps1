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
        $bold = Get-ThemeBool $typography.secondaryTextBold $false
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
        "function" { return Convert-ThemeColor $Theme.colors.functionKey "#E7EAF0" }
        "primary" { return Convert-ThemeColor $Theme.colors.primaryFunctionKey "#DDE3EC" }
        "accent" { return Convert-ThemeColor $Theme.colors.accentKey "#EAF1FF" }
        "pressed" { return Convert-ThemeColor $Theme.colors.keyPressed "#B2B2B2" }
        default { return Convert-ThemeColor $Theme.colors.keyIdle "#F8F8F8" }
    }
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
    $normalizedLabel = $Label.ToLowerInvariant() -replace "\s+", ""
    $candidates = @(
        $normalizedLabel,
        "label:$normalizedLabel",
        "tap:$normalizedLabel"
    )
    foreach ($candidate in $candidates) {
        $property = $overrides.PSObject.Properties |
                Where-Object { ($_.Name.ToLowerInvariant() -replace "\s+", "") -eq $candidate } |
                Select-Object -First 1
        if ($null -ne $property) {
            return Convert-ThemeColor $property.Value "#F8F8F8"
        }
    }
    return $null
}

function Get-PreviewIconName {
    param([string] $Label)
    switch ($Label.ToLowerInvariant()) {
        "shift" { return "shift" }
        "bksp" { return "backspace" }
        "backspace" { return "backspace" }
        "opt" { return "options" }
        "res" { return "reserved" }
        "space" { return "space" }
        "lang" { return "language" }
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
    return $Label -match '^[A-Za-z]$' -or $Label -match '^[\u3131-\u318E\uAC00-\uD7A3]$'
}

function Test-SimpleTextPack {
    param([string] $PackId)
    return $PackId -eq "simple-text" -or $PackId -eq "olivia-script-text"
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
        "shift" { return [System.Drawing.Color]::FromArgb(255, 255, 75, 62) }
        "backspace" { return [System.Drawing.Color]::FromArgb(255, 255, 176, 0) }
        "enter" { return [System.Drawing.Color]::FromArgb(255, 102, 227, 196) }
        "language" { return [System.Drawing.Color]::FromArgb(255, 102, 227, 196) }
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
    if (-not (Test-SimpleTextPack -PackId (Get-KeyDisplayPackId -Theme $Theme))) {
        return $false
    }
    $text = switch ($Icon) {
        "enter" { "hihihi" }
        "backspace" { "del" }
        "shift" { "shift" }
        "space" { "space" }
        "language" { "lang" }
        default { "" }
    }
    if ([string]::IsNullOrWhiteSpace($text)) {
        return $false
    }
    $font = [System.Drawing.Font]::new("Segoe UI", [Math]::Max(8, $H * 0.28), [System.Drawing.FontStyle]::Bold, [System.Drawing.GraphicsUnit]::Pixel)
    $brush = [System.Drawing.SolidBrush]::new($Color)
    try {
        Draw-CenteredText -Graphics $Graphics -Text $text -Font $font -Brush $brush -X $X -Y $Y -W $W -H $H
    } finally {
        $brush.Dispose()
        $font.Dispose()
    }
    return $true
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
    if ($pack -eq "dots-lines" -or $pack -eq "metropolis-points") {
        $drawColor = if ($pack -eq "metropolis-points") { Get-MetropolisPreviewColor -Icon $Icon } else { $Color }
        $pen = [System.Drawing.Pen]::new($drawColor, [Math]::Max(2, $H * 0.08))
        $brush = [System.Drawing.SolidBrush]::new($drawColor)
        try {
            $pen.StartCap = [System.Drawing.Drawing2D.LineCap]::Round
            $pen.EndCap = [System.Drawing.Drawing2D.LineCap]::Round
            $y = $Y + $H / 2
            $left = $X + $W * 0.28
            $right = $X + $W * 0.72
            if ($pack -eq "dots-lines" -and $Icon -ne "space") {
                for ($i = 0; $i -lt 4; $i++) {
                    $dotX = $left + ($right - $left) * $i / 3.0
                    $Graphics.FillEllipse($brush, $dotX - $H * 0.035, $y - $H * 0.035, $H * 0.07, $H * 0.07)
                }
            } else {
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
                $font = [System.Drawing.Font]::new("Segoe UI", [Math]::Max(8, $size * 0.33), [System.Drawing.FontStyle]::Bold, [System.Drawing.GraphicsUnit]::Pixel)
                try {
                    Draw-CenteredText -Graphics $Graphics -Text "A/KR" -Font $font -Brush $brush -X $X -Y $Y -W $W -H $H
                } finally {
                    $font.Dispose()
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
    $fill = Get-RoleColor -Theme $Theme -Role $Role
    $accent = Convert-ThemeColor $Theme.colors.accent "#232323"
    $overrideText = Get-KeyOverrideColor -Theme $Theme -Label $Label
    $textColor = if ($null -ne $overrideText) { $overrideText } else { $accent }
    $depthColor = $border
    if ($null -ne $Theme.colors.depth -and -not [string]::IsNullOrWhiteSpace([string]$Theme.colors.depth)) {
        $depthColor = Convert-ThemeColor $Theme.colors.depth "#696969"
    }

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

    $fillBrush = [System.Drawing.SolidBrush]::new($fill)
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
            $diameter = [Math]::Min($W, $H) * 0.20
            $Graphics.FillEllipse(
                    $textBrush,
                    $X + ($W - $diameter) / 2.0,
                    $Y + ($H - $diameter) / 2.0,
                    $diameter,
                    $diameter)
        } elseif (-not [string]::IsNullOrWhiteSpace($icon) -and (Draw-KeyDisplayPackPreview -Graphics $Graphics -Theme $Theme -Icon $icon -Color $textColor -X $X -Y $Y -W $W -H $H)) {
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
    $brush = [System.Drawing.SolidBrush]::new((Convert-ThemeColor $Theme.colors.keyboardBackground "#EBEBEB"))
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
        $rowH = ($H - 40 - $bottomPadding - $bottomRowTopPadding) / 4
        $unit = ($W - 36 - $leftPadding - $rightPadding) / 20
        $startX = $X + 18 + $leftPadding
        $rowY = $Y + 18
        $rowIndex = 0
        $rows = @(
            @(@("q",2,"normal"), @("w",2,"normal"), @("e",2,"pressed"), @("r",2,"normal"), @("t",2,"normal"), @("y",2,"normal"), @("u",2,"normal"), @("i",2,"normal"), @("o",2,"normal"), @("p",2,"normal")),
            @(@("a",2,"normal"), @("s",2,"normal"), @("d",2,"normal"), @("f",2,"normal"), @("g",2,"normal"), @("h",2,"normal"), @("j",2,"normal"), @("k",2,"normal"), @("l",2,"normal")),
            @(@("shift",3,"primary"), @("z",2,"normal"), @("x",2,"normal"), @("c",2,"normal"), @("v",2,"normal"), @("b",2,"normal"), @("n",2,"normal"), @("m",2,"normal"), @("bksp",3,"primary")),
            @(@("opt",3,"function"), @("res",2,"function"), @("space",10,"normal"), @("lang",2,"function"), @("enter",3,"primary"))
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
            foreach ($key in $row) {
                Draw-Key -Graphics $Graphics -Theme $Theme -Label ([string]$key[0]) -Role ([string]$key[2]) `
                    -X ($xOffset + $gap / 2) -Y ($rowY + $gap / 2) `
                    -W ([int]$key[1] * $unit - $gap) -H ($rowH - $gap) -Radius $radius -Font $font
                $xOffset += [int]$key[1] * $unit
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
        $rowH = ($H - 32 - $bottomPadding - $bottomRowTopPadding) / 5
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
            @(@($g,83,"normal"), @($n,83,"normal"), @($ui,83,"normal"), @("bksp",51,"primary")),
            @(@($r,83,"normal"), @($m,83,"normal"), @("$i.",83,"normal"), @("?",51,"normal")),
            @(@($s,83,"normal"), @($o,83,"pressed"), @("$eu$ae",83,"normal"), @(".",51,"accent")),
            @(@($j,83,"normal"), @($hieut,83,"normal"), @("..",83,"normal"), @("/",51,"accent")),
            @(@("opt",45,"function"), @("res",30,"function"), @("space",150,"normal"), @("lang",30,"function"), @("enter",45,"primary"))
        )

        $rowIndex = 0
        foreach ($row in $rows) {
            if ($rowIndex -eq ($rows.Count - 1)) {
                $rowY += $bottomRowTopPadding
            }
            $activeUnit = if ($rowIndex -eq ($rows.Count - 1)) { $bottomUnit } else { $unit }
            $xOffset = $startX
            $keyIndex = 0
            foreach ($key in $row) {
                Draw-Key -Graphics $Graphics -Theme $Theme -Label ([string]$key[0]) -Role ([string]$key[2]) `
                    -X ($xOffset + $gap / 2) -Y ($rowY + $gap / 2) `
                    -W ([int]$key[1] * $activeUnit - $gap) -H ($rowH - $gap) -Radius $radius -Font $font
                $xOffset += [int]$key[1] * $activeUnit
                if ($rowIndex -lt ($rows.Count - 1) -and $keyIndex -eq 2) {
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
        $Graphics.DrawString("Dingul preview - . and / = accent special keys", $labelFont, $metaBrush, $X + 24, $dingulLabelY)
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
