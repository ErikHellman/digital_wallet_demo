package se.hellsoft.digitalwallet.digital_wallet

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import se.hellsoft.digitalwallet.digital_wallet.data.PassportInfo

@Composable
expect fun PassportIdScanner(
    modifier: Modifier = Modifier,
    onPassportIdScanned: (PassportInfo) -> Unit
)
