package com.example.myapplication.ui.util

/** Client-side checks before saving a shipping address. Messages are user-facing. */
object ShippingAddressValidation {

    fun validateFullName(value: String): String? {
        val t = value.trim().replace(Regex("\\s+"), " ")
        if (t.length < 2) return "Enter your full name (at least 2 characters)."
        if (!t.any { it.isLetter() }) return "Name should include letters."
        return null
    }

    fun validatePhone(value: String): String? {
        val raw = value.trim()
        if (raw.isBlank()) return "Phone is required."
        if (!raw.matches(Regex("^[+]?[\\d\\s().\\-]+$"))) {
            return "Phone may only include digits, spaces, and + ( ) - ."
        }
        val digits = raw.filter { it.isDigit() }
        if (digits.length < 10) return "Enter a valid phone number (at least 10 digits)."
        if (digits.length > 15) return "Phone number is too long."
        return null
    }

    fun validateLine1(value: String): String? {
        val t = value.trim()
        if (t.length < 3) return "Address line 1 should be at least 3 characters."
        return null
    }

    fun validateDistrict(value: String): String? = validateCityLike(value, "District")

    fun validateCity(value: String): String? = validateCityLike(value, "City")

    private fun validateCityLike(value: String, label: String): String? {
        val t = value.trim().replace(Regex("\\s+"), " ")
        if (t.length < 2) return "$label must be at least 2 characters."
        if (t.all { it.isDigit() }) return "$label cannot be numbers only."
        if (!t.matches(Regex("^[\\p{L}\\d\\s'.-]+$"))) {
            return "$label contains invalid characters (use letters, spaces, numbers only where needed)."
        }
        if (!t.any { it.isLetter() }) return "$label must include at least one letter."
        return null
    }

    fun validatePostalCode(postal: String, country: String): String? {
        val t = postal.trim().uppercase()
        if (t.isBlank()) return "Postal code is required."
        val c = country.trim().uppercase()
        val isTurkey = c == "TR" || c == "TUR" || c == "TURKEY" || c == "TÜRKİYE"
        return if (isTurkey) {
            when {
                !t.matches(Regex("^\\d{5}$")) -> "Turkey postal codes must be exactly 5 digits."
                else -> null
            }
        } else {
            when {
                t.length < 3 || t.length > 12 -> "Enter a valid postal code (3–12 characters)."
                !t.matches(Regex("^[A-Z0-9\\s-]+$")) -> "Postal code contains invalid characters."
                else -> null
            }
        }
    }

    fun validateCountry(value: String): String? {
        val t = value.trim()
        if (t.isBlank()) return "Country is required."
        return when {
            t.length <= 3 -> {
                if (!t.matches(Regex("^[A-Za-z]{2,3}$"))) {
                    "Use a 2–3 letter country code (e.g. TR) or a longer country name."
                } else {
                    null
                }
            }
            t.length < 3 -> "Country name is too short."
            !t.any { it.isLetter() } -> "Enter a valid country."
            else -> null
        }
    }

    fun validateLabel(value: String): String? {
        val t = value.trim()
        if (t.isBlank()) return "Label is required (e.g. Home, Office)."
        if (t.length < 2) return "Label should be at least 2 characters."
        return null
    }
}
