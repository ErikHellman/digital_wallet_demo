package se.hellsoft.digitalwallet.mrz

import kotlinx.datetime.LocalDate

/**
 * Parser for TD3 Machine Readable Zone (MRZ) data as used in passports.
 *
 * TD3 format consists of two lines of 44 characters each:
 *
 * Line 1: Document type (2 chars) + Issuing country (3 chars) + Name (39 chars)
 * Line 2: Document number (9 chars) + Check digit (1) + Nationality (3 chars) +
 *         Date of birth (6 chars) + Check digit (1) + Sex (1 char) +
 *         Expiration date (6 chars) + Check digit (1) + Personal number (14 chars) +
 *         Check digit (1) + Composite check digit (1)
 *
 * This parser validates all check digits and parses the data into a structured format.
 */
class TD3Parser {

    companion object {
        private const val LINE_LENGTH = 44
        private const val FILLER_CHAR = '<'
    }

    private val checkDigitCalculator = MrzCheckDigitCalculator()

    /**
     * Parses two lines of TD3 MRZ data.
     *
     * @param line1 The first line of the MRZ (44 characters)
     * @param line2 The second line of the MRZ (44 characters)
     * @return Parsed TD3MrzData
     * @throws IllegalArgumentException if either line is not exactly 44 characters
     */
    fun parse(line1: String, line2: String): TD3MrzData {
        require(line1.length == LINE_LENGTH) {
            "Line 1 must be exactly $LINE_LENGTH characters, got ${line1.length}"
        }
        require(line2.length == LINE_LENGTH) {
            "Line 2 must be exactly $LINE_LENGTH characters, got ${line2.length}"
        }

        // Parse Line 1
        val documentType = line1.substring(0, 1)
        val issuingCountry = line1.substring(2, 5)
        val (familyName, givenNames) = parseName(line1.substring(5, 44))

        // Parse Line 2
        val documentNumber = line2.substring(0, 9).trimFillers()
        val documentNumberCheck = line2[9]

        val nationality = line2.substring(10, 13)

        val dateOfBirthStr = line2.substring(13, 19)
        val dateOfBirthCheck = line2[19]

        val sex = line2.substring(20, 21)

        val expirationDateStr = line2.substring(21, 27)
        val expirationDateCheck = line2[27]

        val personalNumber = line2.substring(28, 42).trimFillers()
        val personalNumberCheck = line2[42]

        val compositeCheck = line2[43]

        // Validate check digits
        val isDocumentNumberValid = checkDigitCalculator.validate(line2.substring(0, 9), documentNumberCheck)
        val isDateOfBirthValid = checkDigitCalculator.validate(dateOfBirthStr, dateOfBirthCheck)
        val isExpirationDateValid = checkDigitCalculator.validate(expirationDateStr, expirationDateCheck)
        val isPersonalNumberValid = checkDigitCalculator.validate(line2.substring(28, 42), personalNumberCheck)

        // Composite check digit validates: document number + check + nationality + DOB + check + sex + expiry + check + personal number + check
        val compositeData = line2.substring(0, 10) + line2.substring(13, 20) + line2.substring(21, 43)
        val isCompositeValid = checkDigitCalculator.validate(compositeData, compositeCheck)

        val isValid = isDocumentNumberValid && isDateOfBirthValid && isExpirationDateValid &&
                isPersonalNumberValid && isCompositeValid

        // Parse dates
        val dateOfBirth = parseDate(dateOfBirthStr)
        val expirationDate = parseDate(expirationDateStr)

        return TD3MrzData(
            documentType = documentType,
            issuingCountry = issuingCountry,
            familyName = familyName,
            givenNames = givenNames,
            documentNumber = documentNumber,
            nationality = nationality,
            dateOfBirth = dateOfBirth,
            sex = sex,
            expirationDate = expirationDate,
            personalNumber = personalNumber,
            isValid = isValid,
            rawMrzLine1 = line1,
            rawMrzLine2 = line2
        )
    }

    /**
     * Parses the name field from Line 1.
     * Format: SURNAME<<GIVEN<NAMES (separated by <<, individual names by <)
     *
     * @param nameField The 39-character name field
     * @return Pair of (familyName, givenNames)
     */
    private fun parseName(nameField: String): Pair<String, String> {
        val parts = nameField.split("<<")

        val familyName = parts[0].replace(FILLER_CHAR, ' ').trim()

        val givenNames = if (parts.size > 1) {
            parts[1]
                .trimEnd(FILLER_CHAR)
                .replace(FILLER_CHAR, ' ')
                .trim()
        } else {
            ""
        }

        return Pair(familyName, givenNames)
    }

    /**
     * Parses a date in YYMMDD format.
     * Assumes dates 00-50 are 2000-2050, and 51-99 are 1951-1999.
     *
     * @param dateStr The date string in YYMMDD format
     * @return LocalDate
     */
    private fun parseDate(dateStr: String): LocalDate {
        val year = dateStr.substring(0, 2).toInt()
        val month = dateStr.substring(2, 4).toInt()
        val day = dateStr.substring(4, 6).toInt()

        // Determine century: 00-50 -> 2000-2050, 51-99 -> 1951-1999
        val fullYear = if (year <= 50) 2000 + year else 1900 + year

        return LocalDate(fullYear, month, day)
    }

    /**
     * Removes trailing filler characters from a string.
     */
    private fun String.trimFillers(): String = this.trimEnd(FILLER_CHAR)
}
