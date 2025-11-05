package se.hellsoft.digitalwallet.digital_wallet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
actual fun CameraPermissions(modifier: Modifier, onPermissionsGranted: () -> Unit) {
    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)

    if (!cameraPermissionState.status.isGranted) {
        Button(
            modifier = Modifier.fillMaxWidth(0.75f),
            onClick = { cameraPermissionState.launchPermissionRequest() },
        ) {
            Column(
                modifier = modifier,
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("Grant Camera permissions")
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
actual fun isCameraPermissionGranted(): MutableState<Boolean> {
    val permission = rememberPermissionState(android.Manifest.permission.CAMERA)
    val isGranted = remember { mutableStateOf(permission.status.isGranted) }

    return isGranted
}