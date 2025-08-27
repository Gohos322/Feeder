## Changelog

### 2025-08-27
- Configured git locally: `user.name=Gohos322`, `user.email=lorenzo.guenci@libero.it`, `core.autocrlf=true`.
- Implemented structure-preserving translation to avoid sending raw HTML to ML Kit:
  - Added `translateLinearArticleSuspend` in `java/com/nononsenseapps/feeder/translation/TranslationUtils.kt` to translate `LinearArticle` by translating `LinearText` elements and preserving non-text elements and formatting.
  - Updated `LaunchedEffect(showingTranslated)` in `java/com/nononsenseapps/feeder/ui/compose/feedarticle/ArticleScreen.kt` to call `translateLinearArticleSuspend` instead of translating HTML or plain text, and to render the translated `LinearArticle` via `overrideArticleContent`.
- Ensured no raw HTML is sent to ML Kit, addressing incomplete translations and HTML tag alteration issues.
- Improved annotation remapping: map both start and end proportionally using inclusive indices, preserving order and clamping within translated text length.
