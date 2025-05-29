package com.mindostech.quiznova.util

import androidx.core.text.HtmlCompat
import javax.inject.Inject

interface HtmlDecoder {
    fun decode(html: String): String
}

class AndroidHtmlDecoder @Inject constructor() : HtmlDecoder {
    override fun decode(html: String): String {
        return HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
    }

}