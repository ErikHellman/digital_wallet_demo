package se.hellsoft.digitalwallet.mrz

import kotlinx.datetime.LocalDate

/**
 * Represents parsed data from a TD3 Machine Readable Zone (MRZ).
 * TD3 format is used in passports and consists of two lines of 44 characters each.
 *
 * @property documentType Type of document (typically "P" for passport)
 * @property issuingCountry Three-letter country code of the issuing state
 * @property familyName Last name of the passport holder
 * @property givenNames Given names of the passport holder (space-separated if multiple)
 * @property documentNumber Passport document number
 * @property nationality Three-letter country code of nationality
 * @property dateOfBirth Date of birth of the passport holder
 * @property sex Sex indicator: "M" (male), "F" (female), or "<" (unspecified)
 * @property expirationDate Expiration date of the passport
 * @property personalNumber Optional personal number or other identifier
 * @property isValid Whether all check digits in the MRZ are valid
 * @property rawMrzLine1 The first line of the raw MRZ data
 * @property rawMrzLine2 The second line of the raw MRZ data
 */
data class TD3MrzData(
    val documentType: String,
    val issuingCountry: String,
    val familyName: String,
    val givenNames: String,
    val documentNumber: String,
    val nationality: String,
    val dateOfBirth: LocalDate,
    val sex: String,
    val expirationDate: LocalDate,
    val personalNumber: String,
    val isValid: Boolean,
    val rawMrzLine1: String,
    val rawMrzLine2: String
) {
    /**
     * Returns the MRZ as a single concatenated string (88 characters)
     */
    fun toMrzString(): String = rawMrzLine1 + rawMrzLine2
}
