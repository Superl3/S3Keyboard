from pathlib import Path

from PIL import Image, ImageDraw


ROOT = Path(__file__).resolve().parents[1]

DENSITIES = {
    "mipmap-mdpi": 48,
    "mipmap-hdpi": 72,
    "mipmap-xhdpi": 96,
    "mipmap-xxhdpi": 144,
    "mipmap-xxxhdpi": 192,
}


def rounded_rectangle(draw, box, radius, fill, outline=None, width=1):
    draw.rounded_rectangle(box, radius=radius, fill=fill, outline=outline, width=width)


def line(draw, points, fill, width):
    draw.line(points, fill=fill, width=width, joint="curve")


def render(size, round_variant=False):
    scale = 4
    canvas = Image.new("RGBA", (size * scale, size * scale), (0, 0, 0, 0))
    draw = ImageDraw.Draw(canvas)

    def u(value):
        return int(round(value * size * scale / 192.0))

    bg = (35, 42, 44, 255)
    bg_outline = (51, 59, 61, 255)
    key = (231, 228, 219, 255)
    key_edge = (168, 165, 155, 255)
    legend = (45, 51, 53, 255)
    accent = (92, 164, 161, 255)
    shadow = (0, 0, 0, 60)

    if round_variant:
        draw.ellipse((u(16), u(16), u(176), u(176)), fill=bg, outline=bg_outline, width=u(3))
    else:
        rounded_rectangle(draw, (u(16), u(16), u(176), u(176)), u(30), bg, bg_outline, u(3))

    gap = u(10)
    key_size = u(54)
    left = u(38)
    top = u(38)
    positions = [
        (left, top),
        (left + key_size + gap, top),
        (left, top + key_size + gap),
        (left + key_size + gap, top + key_size + gap),
    ]

    for x, y in positions:
        rounded_rectangle(
            draw,
            (x + u(2), y + u(4), x + key_size + u(2), y + key_size + u(4)),
            u(11),
            shadow,
        )
        rounded_rectangle(
            draw,
            (x, y, x + key_size, y + key_size),
            u(11),
            key,
            key_edge,
            u(2),
        )

    stroke = u(7)
    cap = u(2)

    x, y = positions[0]
    line(draw, [(x + u(18), y + u(18)), (x + u(18), y + u(36)), (x + u(36), y + u(36))], legend, stroke)

    x, y = positions[1]
    rounded_rectangle(draw, (x + u(17), y + u(16), x + u(38), y + u(38)), u(4), legend)

    x, y = positions[2]
    line(draw, [(x + u(16), y + u(31)), (x + u(40), y + u(31))], legend, stroke)

    x, y = positions[3]
    line(draw, [(x + u(35), y + u(15)), (x + u(35), y + u(38)), (x + u(19), y + u(38))], legend, stroke)
    line(draw, [(x + u(19), y + u(38)), (x + u(27), y + u(30))], legend, max(cap, u(4)))
    line(draw, [(x + u(19), y + u(38)), (x + u(27), y + u(46))], legend, max(cap, u(4)))

    cx = u(96)
    cy = u(96)
    rounded_rectangle(draw, (cx - u(8), cy - u(8), cx + u(8), cy + u(8)), u(4), accent)

    return canvas.resize((size, size), Image.Resampling.LANCZOS)


def main():
    for density, size in DENSITIES.items():
        out_dir = ROOT / "app" / "src" / "main" / "res" / density
        out_dir.mkdir(parents=True, exist_ok=True)
        render(size, False).save(out_dir / "ic_launcher.png")
        render(size, True).save(out_dir / "ic_launcher_round.png")


if __name__ == "__main__":
    main()
