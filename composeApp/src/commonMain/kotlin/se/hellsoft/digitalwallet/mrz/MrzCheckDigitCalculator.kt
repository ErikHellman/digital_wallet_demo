package se.hellsoft.digitalwallet.mrz

/**
 * Calculator for MRZ check digits according to ICAO Doc 9303 specification.
 *
 * The check digit is calculated using a weighted sum with weights [7, 3, 1] repeating.
 * Character values:
 * - Digits 0-9 have values 0-9
 * - Letters A-Z have values 10-35
 * - Filler character '<' has value 0
 *
 * The check digit is the remainder of the weighted sum divided by 10.
 */
class MrzCheckDigitCalculator {

    companion object {
        private val WEIGHTS = intArrayOf(7, 3, 1)
    }

    /**
     * Calculates the check digit for the given string.
     *
     * @param data The string to calculate the check digit for
     * @return The calculated check digit (0-9)
     */
    fun calculate(data: String): Int {
        var sum = 0

        data.forEachIndexed { index, char ->
            val value = charToValue(char)
            val weight = WEIGHTS[index % WEIGHTS.size]
            sum += value * weight
        }

        return sum % 10
    }

    /**
     * Validates that the given check digit is correct for the data.
     *
     * @param data The string to validate
     * @param checkDigit The check digit to validate
     * @return true if the check digit is correct, false otherwise
     */
    fun validate(data: String, checkDigit: Char): Boolean {
        val expected = calculate(data)
        return checkDigit.isDigit() && checkDigit.digitToInt() == expected
    }

    /**
     * Converts an MRZ character to its numeric value.
     *
     * @param char The character to convert
     * @return The numeric value (0-35)
     */
    private fun charToValue(char: Char): Int = when (char) {
        in '0'..'9' -> char - '0'
        in 'A'..'Z' -> char - 'A' + 10
        '<' -> 0
        else -> 0 // Treat any other character as filler
    }
}
