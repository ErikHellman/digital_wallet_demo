package se.hellsoft.digitalwallet.mrz

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TD3ParserTest {

    // Real example from ICAO Doc 9303 specification
    private val validMrzLine1 = "P<UTOERIKSSON<<ANNA<MARIA<<<<<<<<<<<<<<<<<<<"
    private val validMrzLine2 = "L898902C36UTO7408122F1204159ZE184226B<<<<<10"

    @Test
    fun `parse valid TD3 MRZ returns correct data`() {
        val parser = TD3Parser()
        val result = parser.parse(validMrzLine1, validMrzLine2)

        assertTrue(result.isValid)
        assertEquals("P", result.documentType)
        assertEquals("UTO", result.issuingCountry)
        assertEquals("ERIKSSON", result.familyName)
        assertEquals("ANNA MARIA", result.givenNames)
        assertEquals("L898902C3", result.documentNumber)
        assertEquals("UTO", result.nationality)
        assertEquals(LocalDate(1974, 8, 12), result.dateOfBirth)
        assertEquals("F", result.sex)
        assertEquals(LocalDate(2012, 4, 15), result.expirationDate)
        assertEquals("ZE184226B", result.personalNumber)
    }

    @Test
    fun `parse TD3 MRZ with no given names`() {
        val parser = TD3Parser()
        val line1 = "P<UTOERIKSSON<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<"
        val line2 = "L898902C36UTO7408122F1204159ZE184226B<<<<<10"

        val result = parser.parse(line1, line2)

        assertEquals("ERIKSSON", result.familyName)
        assertEquals("", result.givenNames)
    }

    @Test
    fun `parse TD3 MRZ with multiple given names`() {
        val parser = TD3Parser()
        val line1 = "P<UTOERIKSSON<<ANNA<MARIA<LOUISE<<<<<<<<<<<<<<"
        val line2 = "L898902C36UTO7408122F1204159ZE184226B<<<<<10"

        val result = parser.parse(line1, line2)

        assertEquals("ERIKSSON", result.familyName)
        assertEquals("ANNA MARIA LOUISE", result.givenNames)
    }

    @Test
    fun `parse TD3 MRZ with invalid check digit fails validation`() {
        val parser = TD3Parser()
        // Changed last digit from 6 to 7 (invalid check digit for document number)
        val line2Invalid = "L898902C37UTO7408122F1204159ZE184226B<<<<<10"

        val result = parser.parse(validMrzLine1, line2Invalid)

        assertFalse(result.isValid)
    }

    @Test
    fun `parse TD3 MRZ with incorrect line length throws exception`() {
        val parser = TD3Parser()
        val shortLine = "P<UTOERIKSSON<<ANNA<MARIA"

        assertFailsWith<IllegalArgumentException> {
            parser.parse(shortLine, validMrzLine2)
        }
    }

    @Test
    fun `parse TD3 MRZ with male sex indicator`() {
        val parser = TD3Parser()
        val line2Male = "L898902C36UTO7408122M1204159ZE184226B<<<<<10"

        val result = parser.parse(validMrzLine1, line2Male)

        assertEquals("M", result.sex)
    }

    @Test
    fun `parse TD3 MRZ with unspecified sex indicator`() {
        val parser = TD3Parser()
        val line2Unspecified = "L898902C36UTO7408122<1204159ZE184226B<<<<<10"

        val result = parser.parse(validMrzLine1, line2Unspecified)

        assertEquals("<", result.sex)
    }

    @Test
    fun `check digit calculation for numeric string`() {
        val calculator = MrzCheckDigitCalculator()

        // Check digit for "520727" should be 3
        assertEquals(3, calculator.calculate("520727"))
    }

    @Test
    fun `check digit calculation for alphanumeric string`() {
        val calculator = MrzCheckDigitCalculator()

        // Check digit for "L898902C3" should be 6
        assertEquals(6, calculator.calculate("L898902C3"))
    }

    @Test
    fun `check digit calculation with filler characters`() {
        val calculator = MrzCheckDigitCalculator()

        // Check digit for "ZE184226B" should be 1
        assertEquals(1, calculator.calculate("ZE184226B"))
    }

    @Test
    fun `validate check digit returns true for correct digit`() {
        val calculator = MrzCheckDigitCalculator()

        assertTrue(calculator.validate("L898902C3", '6'))
    }

    @Test
    fun `validate check digit returns false for incorrect digit`() {
        val calculator = MrzCheckDigitCalculator()

        assertFalse(calculator.validate("L898902C3", '7'))
    }

    @Test
    fun `parse empty personal number field`() {
        val parser = TD3Parser()
        val line2 = "L898902C36UTO7408122F1204159<<<<<<<<<<<<<<14"

        val result = parser.parse(validMrzLine1, line2)

        assertEquals("", result.personalNumber)
    }

    @Test
    fun `format MRZ as single string`() {
        val parser = TD3Parser()
        val result = parser.parse(validMrzLine1, validMrzLine2)

        val formatted = result.toMrzString()

        assertEquals(validMrzLine1 + validMrzLine2, formatted)
    }
}
