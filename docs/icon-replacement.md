# Icon Replacement Inventory

This prototype uses generated clean-room Android vector drawables for compact command controls.

## Replaced With Icons

- Keyboard command keys: options, reserved phrases, space, language toggle, enter/search/done/next, shift, caps lock, backspace/delete, hide keyboard, settings, cursor left, cursor right.
- Settings screen action buttons: reset touch correction, open Android keyboard settings, show keyboard picker.

## Kept As Text

- Actual input symbols: Hangul jamo, English letters, numbers, punctuation, and long-press symbol hints.
- Settings labels and values where text is needed for clarity.

## Regeneration

```powershell
rtk powershell -ExecutionPolicy Bypass -File .\scripts\generate-icons.ps1
```
