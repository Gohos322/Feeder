package com.nononsenseapps.feeder.translation

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslateLanguage
import com.nononsenseapps.feeder.model.html.LinearArticle
import com.nononsenseapps.feeder.model.html.LinearText
import com.nononsenseapps.feeder.model.html.LinearElement
import com.nononsenseapps.feeder.model.html.LinearTextAnnotation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Manager for ML Kit on-device translation.
 */
object TranslationManager {
    /**
     * Returns a Translator for the given source and target language tags.
     */
    fun getTranslator(sourceLang: String?, targetLang: String?): Translator {
        val src = TranslateLanguage.fromLanguageTag(sourceLang ?: TranslateLanguage.ENGLISH)
            ?: TranslateLanguage.ENGLISH
        val tgt = TranslateLanguage.fromLanguageTag(targetLang ?: TranslateLanguage.ENGLISH)
            ?: TranslateLanguage.ENGLISH
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(src)
            .setTargetLanguage(tgt)
            .build()
        return Translation.getClient(options)
    }
}

/**
 * Downloads the translation model if not already downloaded.
 */
suspend fun Translator.downloadModelIfNeededSuspend() = suspendCancellableCoroutine<Unit> { cont ->
    val conditions = DownloadConditions.Builder().build()
    this.downloadModelIfNeeded(conditions)
        .addOnSuccessListener { cont.resume(Unit) }
        .addOnFailureListener { cont.resumeWithException(it) }
}

/**
 * Translates the input text to the target language.
 */
suspend fun Translator.translateTextSuspend(text: String): String = suspendCancellableCoroutine { cont ->
    this.translate(text)
        .addOnSuccessListener { cont.resume(it) }
        .addOnFailureListener { cont.resumeWithException(it) }
}

/**
 * Converts a LinearArticle to a plain text string for translation.
 */
fun LinearArticle.toPlainText(): String = elements
    .filterIsInstance<LinearText>()
    .joinToString(separator = "\n") { it.text }

/**
 * Translates a LinearArticle without ever sending raw HTML to ML Kit.
 * Preserves the element structure and non-textual elements.
 * Note: This simplified version translates each LinearText independently and
 * keeps annotations unchanged. Further refinement may be required if indices
 * must shift based on translation lengths.
 */
suspend fun Translator.translateLinearArticleSuspend(article: LinearArticle): LinearArticle {
    val newElements = mutableListOf<LinearElement>()

    // LinearArticle has no title field in this model; only translate elements

    for (element in article.elements) {
        when (element) {
            is LinearText -> {
                if (element.text.isNotBlank()) {
                    val translated = translateTextSuspend(element.text)
                    // Remap annotations: indices are local to the LinearText
                    val origLen = element.text.length
                    val newLen = translated.length
                    val newAnnotations: List<LinearTextAnnotation> = element.annotations.mapNotNull { ann ->
                        if (ann.start < 0 || ann.end < ann.start || origLen <= 0 || newLen <= 0) {
                            null
                        } else {
                            // Map both start and end proportionally using inclusive indices.
                            val denomOld = (origLen - 1).coerceAtLeast(1)
                            val denomNew = (newLen - 1).coerceAtLeast(1)

                            val startRatio = ann.start.toDouble() / denomOld.toDouble()
                            val endRatio = ann.end.toDouble() / denomOld.toDouble()

                            val mappedStart = kotlin.math.round(startRatio * denomNew).toInt()
                            val mappedEnd = kotlin.math.round(endRatio * denomNew).toInt()

                            val clampedStart = mappedStart.coerceIn(0, newLen - 1)
                            val clampedEnd = mappedEnd.coerceIn(0, newLen - 1)

                            val normalizedStart = minOf(clampedStart, clampedEnd)
                            val normalizedEnd = maxOf(clampedStart, clampedEnd)

                            if (normalizedStart > normalizedEnd) null else LinearTextAnnotation(ann.data, normalizedStart, normalizedEnd)
                        }
                    }
                    newElements.add(
                        element.copy(
                            text = translated,
                            annotations = newAnnotations,
                        ),
                    )
                } else {
                    newElements.add(element)
                }
            }
            else -> {
                newElements.add(element)
            }
        }
    }

    return LinearArticle(
        elements = newElements,
    )
}
