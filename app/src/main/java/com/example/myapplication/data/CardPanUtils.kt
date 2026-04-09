package com.example.myapplication.data

/**
 * Card number validation and masking. Full PAN must not be stored in production databases.
 */
object CardPanUtils {
    const val MIN_PAN_LENGTH = 13
    const val MAX_PAN_LENGTH = 19

    fun digitsOnly(s: String): String = s.filter { it.isDigit() }.take(MAX_PAN_LENGTH)

    fun formatGrouped(digits: String): String = digits.chunked(4).joinToString(" ")

    fun luhnCheck(digits: String): Boolean {
        if (digits.length < MIN_PAN_LENGTH) return false
        var sum = 0
        var alternate = false
        for (i in digits.length - 1 downTo 0) {
            var n = digits[i].digitToInt()
            if (alternate) {
                n *= 2
                if (n > 9) n -= 9
            }
            sum += n
            alternate = !alternate
        }
        return sum % 10 == 0
    }

    fun inferBrand(digits: String): String {
        val d = digits.filter { it.isDigit() }
        if (d.isEmpty()) return "Card"
        return when (d[0]) {
            '4' -> "Visa"
            '5', '2' -> "Mastercard"
            '3' -> when {
                d.startsWith("34") || d.startsWith("37") -> "Amex"
                else -> "Diners"
            }
            '6' -> "Discover"
            else -> "Card"
        }
    }

    /** Display: first 4 + masked middle + last 4 (typical 16-digit card UI). */
    fun maskedDisplay(digits: String): String {
        val d = digits.filter { it.isDigit() }
        if (d.length < MIN_PAN_LENGTH) return formatGrouped(d)
        val first4 = d.take(4)
        val last4 = d.takeLast(4)
        return "$first4 •••• •••• $last4"
    }

    fun last4(digits: String): String {
        val d = digits.filter { it.isDigit() }
        return if (d.length >= 4) d.takeLast(4) else d.padStart(4, '0').takeLast(4)
    }
}
