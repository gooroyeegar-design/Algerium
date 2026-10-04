# Algerium security policy

Algerium treats security as a release blocker. Web content is untrusted input.

- No Chromium, WebView, Gecko, WebKit, or privileged JavaScript bridge.
- Navigation accepts only supported schemes and rejects URL credentials.
- Android cleartext HTTP is disabled.
- Website JavaScript runs in an isolated Android service.
- Native tests cover origin, navigation, event cancellation, and rendering invariants.

A release is not production-ready until native, Android, iOS, compatibility, fuzzing, and crash/recovery gates pass. CI success is not proof of Chrome-level compatibility or absence of vulnerabilities.
