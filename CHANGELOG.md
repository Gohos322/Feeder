# Changelog

## [Unreleased]

- Introduced per-feed translation settings:
  - UI in Edit Feed screen (`EditFeedScreen.kt`) to enable/disable article translation and select source/target languages.
  - Persisted settings in Room DB (`Feed` entity, `AppDatabase` migration 37→38, `Constants.kt`).
 - Updated `EditFeedScreenViewModel` to load and save translation preferences.
 - Updated `CreateFeedScreenViewModel` to initialize and persist translation preferences (translateEnabled, translationSourceLanguage, translationTargetLanguage).
- In Article Screen (`ArticleScreen.kt`):
  - Added translation toggle button in the top app bar when translation is enabled for the feed.
  - Implemented translation workflow using Google ML Kit on-device translation:
    - Downloads model if needed (`downloadModelIfNeededSuspend`).
    - Converts article content to plain text (`toPlainText`) and translates (`translateTextSuspend`).
    - Displays translated text with a loading indicator and allows switching back to original text.
- Updated imports and UI components to support scrollable translated text view.

### Fixed
- Preserve rich-text formatting (bold, italic, underline, links, code, headings, etc.) when translating articles by round-tripping through HTML tags instead of stripping to plain text.
- Fix compilation error "Unresolved reference 'Companion'" in LinearArticle.Companion.fromHtml by adding empty companion object body to LinearArticle (LinearStuff.kt).

### Alternatives to ML Kit
- TensorFlow Lite translation models bundled in-app.
- ONNX Runtime mobile with pre-trained translation models.
- Custom local inference using OpenNMT or Marian NMT models.