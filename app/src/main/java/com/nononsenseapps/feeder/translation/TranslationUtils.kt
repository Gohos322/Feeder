package com.nononsenseapps.feeder.translation

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslateLanguage
import com.nononsenseapps.feeder.model.html.LinearArticle
import com.nononsenseapps.feeder.model.html.LinearText
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