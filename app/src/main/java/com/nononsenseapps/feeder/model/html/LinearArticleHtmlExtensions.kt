package com.nononsenseapps.feeder.model.html

/**
 * Serialize a LinearArticle to HTML, preserving text annotations (bold, italic, links, etc.)
 * and parse back HTML to a LinearArticle to restore rich-text formatting after translation.
 */
fun LinearArticle.toHtml(): String = buildString {
    elements.forEach { element ->
        when (element) {
            is LinearText -> append(element.toHtml())
            else -> append(elementToPlainHtml(element))
        }
        append("\n")
    }
}

/**
 * Parse HTML markup into a LinearArticle, restoring annotations and block structure.
 */
fun LinearArticle.Companion.fromHtml(
    html: String,
    baseUrl: String = ""
): LinearArticle =
    HtmlLinearizer().linearize(html, baseUrl)

private fun elementToPlainHtml(element: LinearElement): String =
    when (element) {
        is LinearText -> element.text
        else -> ""
    }

/**
 * Serialize a LinearText to HTML by emitting start/end tags for each annotation range.
 */
fun LinearText.toHtml(): String = buildString {
    val opens = annotations.groupBy { it.start }
    val closes = annotations.groupBy { it.endExclusive }
    for (i in 0..text.length) {
        closes[i]?.sortedByDescending { it.start }?.forEach { ann ->
            append(endTagFor(ann.data))
        }
        opens[i]?.sortedBy { it.end }?.forEach { ann ->
            append(startTagFor(ann.data, ann))
        }
        if (i < text.length) {
            append(text[i])
        }
    }
}

private fun startTagFor(
    data: LinearTextAnnotationData,
    annotation: LinearTextAnnotation
): String = when (data) {
    LinearTextAnnotationH1 -> "<h1>"
    LinearTextAnnotationH2 -> "<h2>"
    LinearTextAnnotationH3 -> "<h3>"
    LinearTextAnnotationH4 -> "<h4>"
    LinearTextAnnotationH5 -> "<h5>"
    LinearTextAnnotationH6 -> "<h6>"
    LinearTextAnnotationBold -> "<b>"
    LinearTextAnnotationItalic -> "<i>"
    LinearTextAnnotationMonospace -> "<tt>"
    LinearTextAnnotationUnderline -> "<u>"
    LinearTextAnnotationStrikethrough -> "<s>"
    LinearTextAnnotationSuperscript -> "<sup>"
    LinearTextAnnotationSubscript -> "<sub>"
    is LinearTextAnnotationFont -> "<font face=\"${data.face}\">"
    LinearTextAnnotationCode -> "<code>"
    is LinearTextAnnotationLink -> "<a href=\"${data.href}\">"
    else -> ""
}

private fun endTagFor(data: LinearTextAnnotationData): String = when (data) {
    LinearTextAnnotationH1 -> "</h1>"
    LinearTextAnnotationH2 -> "</h2>"
    LinearTextAnnotationH3 -> "</h3>"
    LinearTextAnnotationH4 -> "</h4>"
    LinearTextAnnotationH5 -> "</h5>"
    LinearTextAnnotationH6 -> "</h6>"
    LinearTextAnnotationBold -> "</b>"
    LinearTextAnnotationItalic -> "</i>"
    LinearTextAnnotationMonospace -> "</tt>"
    LinearTextAnnotationUnderline -> "</u>"
    LinearTextAnnotationStrikethrough -> "</s>"
    LinearTextAnnotationSuperscript -> "</sup>"
    LinearTextAnnotationSubscript -> "</sub>"
    is LinearTextAnnotationFont -> "</font>"
    LinearTextAnnotationCode -> "</code>"
    is LinearTextAnnotationLink -> "</a>"
    else -> ""
}