import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class HtmlTranslator() {

    suspend fun translateHtmlFromUrl(textToTranslate: String, targetLanguage: String): String {
        return withContext(Dispatchers.IO) {
            try {
                // Effettua la traduzione
                val translatedText = translateText(textToTranslate, targetLanguage)
                var newtranslatedText = translatedText.replace("</ ", "</")
                newtranslatedText = newtranslatedText.replace("& nbsp;".toRegex(RegexOption.IGNORE_CASE), "&nbsp;")
                newtranslatedText = newtranslatedText.replace("& nbsp".toRegex(RegexOption.IGNORE_CASE), "&nbsp;")
                newtranslatedText
            } catch (e: Exception) {
                e.printStackTrace()
                "Failed to Translate"
            }
        }
    }

    private suspend fun translateText(text: String, targetLanguage: String): String {
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH) // Lingua sorgente (da modificare se necessario)
            .setTargetLanguage(TranslateLanguage.ITALIAN)
            .build()
        val translator = Translation.getClient(options)

        // Assicurati che il modello di traduzione sia scaricato
        val downloadConditions = DownloadConditions.Builder()
            .requireWifi()
            .build()
        translator.downloadModelIfNeeded(downloadConditions).await()

        // Esegui la traduzione
        return translator.translate(text).await()
    }
}
