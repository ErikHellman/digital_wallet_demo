package se.hellsoft.digitalwallet.digital_wallet

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

@Composable
actual fun CameraPermissions(modifier: Modifier, onPermissionsGranted: () -> Unit) {
    LaunchedEffect(true) {
        onPermissionsGranted()
    }
}

@Composable
actual fun isCameraPermissionGranted(): MutableState<Boolean> = remember { mutableStateOf(true) }