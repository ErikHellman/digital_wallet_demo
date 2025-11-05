package se.hellsoft.digitalwallet.digital_wallet.data

import kotlinx.datetime.LocalDate
import kotlin.jvm.JvmInline

class PassportInfo(
    val id: String,
    val givenNames: String,
    val familyName: String,
    val birthDate: LocalDate,
    val photo: PassportPhoto? = null,
)


@JvmInline
value class PassportPhoto(val data: ByteArray)