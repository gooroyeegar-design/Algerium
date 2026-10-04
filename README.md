# Algerium

Algerium is an independent Android browser project. It does not embed Chromium, Android WebView, Gecko, WebKit, or another browser engine.

## Everyday-browser features in 0.2
- Native Android browser shell
- Address/search bar
- Back/forward/reload
- Home/new tab page
- Browsing history
- Bookmarks UI foundation
- Find in page
- Share page
- Settings/privacy controls foundation
- Independent HTML/text parser and Canvas renderer
- HTTPS networking and redirect handling
- Scrollable pages
- Error pages
- GitHub APK CI

## Important architecture note
This is genuinely independent, but independence has a cost: this is **not** yet a standards-complete Chrome replacement. The engine is being built incrementally. JavaScript execution, full CSS layout, images, forms, cookies/storage, downloads, permissions, media, accessibility, service workers, extensions and robust site compatibility require substantial additional engine work and are next-stage targets.
