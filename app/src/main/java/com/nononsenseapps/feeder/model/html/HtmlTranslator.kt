import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class HtmlTranslator() {

    suspend fun translateHtml(textToTranslate: String, sourceLanguage: String?, targetLanguage: String?): String {
        return withContext(Dispatchers.IO) {
            try {
                // Effettua la traduzione
                val translatedText = translateText(textToTranslate, sourceLanguage, targetLanguage)
                var newtranslatedText = translatedText.replace("</ ", "</")
                                                      .replace("& nbsp;".toRegex(RegexOption.IGNORE_CASE), "&nbsp;")
                                                      .replace("& nbsp".toRegex(RegexOption.IGNORE_CASE), "&nbsp;")
                newtranslatedText
            } catch (e: Exception) {
                e.printStackTrace()
                "Failed to Translate"
            }
        }
    }

    private suspend fun translateText(text: String, sourceLanguage: String?, targetLanguage: String?): String {
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(sourceLanguage.toString()) // Lingua sorgente (da modificare se necessario)
            .setTargetLanguage(targetLanguage.toString())
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
