# Algerium

A from-scratch Android browser experiment for Algeria.

## Independence
Algerium does **not** embed Chromium, Android WebView, Gecko, WebKit, or another browser engine. Its networking, document parsing, layout and drawing are implemented in this project using Android platform primitives only.

## Current engine
The first engine supports a deliberately small HTML/text subset. It is an independent renderer, not a compatibility claim for the modern web. The project is designed to grow its own HTML, CSS, scripting, layout, image, storage and security subsystems over time.

## Build
Open in Android Studio or run `gradle assembleDebug`.
