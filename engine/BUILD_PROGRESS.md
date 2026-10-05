# Engine build progress

This file exists to trigger the cross-platform CI gates after native engine changes.

## Current engine milestone
- HTML parser: entities, raw-text elements, implied closes
- DOM: tag/id/class/attribute selectors and descendant matching
- CSS: cascade ordering, shorthand margin/padding, inherited typography, common flex properties
- Layout: block/flex sizing and intrinsic text height
- Paint: background/border/direct text emission without ancestor text duplication

These are compatibility foundations, not a claim of Chrome-level compatibility. The release gate remains cross-platform build + tests + compatibility coverage.
