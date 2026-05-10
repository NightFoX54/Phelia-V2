package com.example.myapplication.ui.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.example.myapplication.data.CardPanUtils

/**
 * Shows groups of 4 digits while the underlying text stays digits-only so the caret stays stable.
 */
object CreditCardVisualTransformation : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text.filter { it.isDigit() }.take(CardPanUtils.PAYMENT_FORM_MAX_DIGITS)
        val formatted = CardPanUtils.formatGrouped(digits)

        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val o = offset.coerceIn(0, digits.length)
                if (o == 0) return 0
                var digitSeen = 0
                var i = 0
                while (i < formatted.length) {
                    if (formatted[i].isDigit()) {
                        digitSeen++
                        if (digitSeen == o) return i + 1
                    }
                    i++
                }
                return formatted.length
            }

            override fun transformedToOriginal(offset: Int): Int {
                val safe = offset.coerceIn(0, formatted.length)
                var digitsBefore = 0
                for (i in 0 until safe) {
                    if (formatted[i].isDigit()) digitsBefore++
                }
                return digitsBefore
            }
        }

        return TransformedText(AnnotatedString(formatted), mapping)
    }
}
