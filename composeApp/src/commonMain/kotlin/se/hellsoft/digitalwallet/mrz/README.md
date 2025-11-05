# TD3 Machine Readable Zone (MRZ) Parser

This module provides a parser for TD3 Machine Readable Zone data as used in passports, compliant with ICAO Doc 9303 specification.

## Overview

TD3 format consists of two lines of 44 characters each:

### Line 1 Format
```
Position  Length  Field
0-1       2       Document type (e.g., "P<" for passport)
2-4       3       Issuing country code (ISO 3166-1 alpha-3)
5-43      39      Name (Format: SURNAME<<GIVEN<NAMES)
```

### Line 2 Format
```
Position  Length  Field
0-8       9       Document number
9         1       Check digit for document number
10-12     3       Nationality (ISO 3166-1 alpha-3)
13-18     6       Date of birth (YYMMDD)
19        1       Check digit for date of birth
20        1       Sex (M/F/<)
21-26     6       Expiration date (YYMMDD)
27        1       Check digit for expiration date
28-41     14      Personal number (optional)
42        1       Check digit for personal number
43        1       Composite check digit
```

## Usage

```kotlin
val parser = TD3Parser()
val line1 = "P<UTOERIKSSON<<ANNA<MARIA<<<<<<<<<<<<<<<<<<<"
val line2 = "L898902C36UTO7408122F1204159ZE184226B<<<<<10"

val result = parser.parse(line1, line2)

if (result.isValid) {
    println("Name: ${result.givenNames} ${result.familyName}")
    println("Document: ${result.documentNumber}")
    println("DOB: ${result.dateOfBirth}")
    println("Expiry: ${result.expirationDate}")
}
```

## Features

- ✅ Full TD3 MRZ parsing
- ✅ Check digit validation for all fields
- ✅ Composite check digit validation
- ✅ Name parsing with support for multiple given names
- ✅ Date parsing with century inference
- ✅ Kotlin Multiplatform compatible
- ✅ Comprehensive unit tests

## Check Digit Algorithm

The MRZ uses a weighted modulo-10 check digit algorithm:
- Weights: [7, 3, 1] (repeating)
- Character values: 0-9 = 0-9, A-Z = 10-35, < (filler) = 0
- Check digit = (weighted sum) mod 10

## Classes

### `TD3Parser`
Main parser class that processes TD3 MRZ data.

### `TD3MrzData`
Data class containing all parsed fields and validation status.

### `MrzCheckDigitCalculator`
Utility class for calculating and validating MRZ check digits.

## Test Coverage

Comprehensive unit tests cover:
- Valid MRZ parsing
- Name variations (no given names, multiple given names)
- Check digit validation (valid and invalid)
- Line length validation
- Sex indicator variations
- Empty optional fields
- MRZ string formatting

## References

- ICAO Doc 9303: Machine Readable Travel Documents
- ISO/IEC 7501-1: Identification cards — Machine readable travel documents
