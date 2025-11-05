package se.hellsoft.digitalwallet.digital_wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import com.kashif.cameraK.controller.CameraController
import com.kashif.cameraK.enums.*
import com.kashif.cameraK.ui.CameraPreview
import com.kashif.ocrPlugin.rememberOcrPlugin
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import se.hellsoft.digitalwallet.digital_wallet.data.PassportInfo

private val TD3_LINE1_REGEXP = Regex("^(P)<([A-Z]{3})([A-Z<]{39})$")
private val TD3_LINE2_REGEXP =
    Regex(
        "^([A-Z0-9<]{9})([0-9])([A-Z]{3})([0-9]{6})([0-9])([MF<])([0-9]{6})([0-9])([A-Z0-9<]{14})([0-9<])([0-9])$"
    )

@Composable
expect fun CameraPermissions(modifier: Modifier = Modifier, onPermissionsGranted: () -> Unit)

@Composable expect fun isCameraPermissionGranted(): MutableState<Boolean>

@Composable
fun PassportIdScanner(modifier: Modifier = Modifier, onPassportIdScanned: (PassportInfo) -> Unit) {
    val permissionsGranted by isCameraPermissionGranted()

    if (!permissionsGranted) {
        CameraPermissions(modifier = modifier) {}
    } else {
        var cameraController by remember { mutableStateOf<CameraController?>(null) }
        val ocrPlugin = rememberOcrPlugin()
        var line1 by remember { mutableStateOf<String?>(null) }
        var line2 by remember { mutableStateOf<String?>(null) }

        LaunchedEffect(line1) {
            val data = line1
            if (data != null) {
                TD3_LINE1_REGEXP.findAll(data).onEach {
                    Logger.d { "${it.range} : ${it.value}" }
                }
            }
        }

        LaunchedEffect(line2) {
            val data = line2
            if (data != null) {
                TD3_LINE2_REGEXP.findAll(data).onEach {
                    Logger.d { "${it.range} : ${it.value}" }
                }
            }
        }

        LaunchedEffect(Unit) {
            Logger.d { "Setup OCR flow..." }
            ocrPlugin.ocrFlow.consumeAsFlow().distinctUntilChanged().collect { data ->
                data
                    .split("\n")
                    .filter { line -> line.contains("<") }
                    .map { line -> line.replace(Regex("\\s+"), "").replace("«", "<<").trim() }
                    .filter { line -> line.length == 44 }
                    .forEach { line ->
                        Logger.d { line }
                        if (line.matches(TD3_LINE1_REGEXP)) {
                            Logger.d { "TD3_LINE1_REGEXP matches $line1" }
                            line1 = line
                        } else if (line.matches(TD3_LINE2_REGEXP)) {
                            Logger.d { "TD3_LINE2_REGEXP matches $line2" }
                            line2 = line
                        } else {
                            Logger.d { "Unknown line format: $line" }
                        }
                    }
            }
        }

        DisposableEffect(Unit) {
            onDispose {
                ocrPlugin.stopRecognition()
                cameraController?.stopSession()
            }
        }

        Column(
            modifier = modifier.border(8.dp, Color.Green),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start,
        ) {
            Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f).background(Color.Red)) {
                CameraPreview(
                    modifier = Modifier.fillMaxSize().aspectRatio(1f),
                    cameraConfiguration = {
                        setQualityPrioritization(QualityPrioritization.QUALITY)
                        setTorchMode(TorchMode.ON)
                        setCameraLens(CameraLens.BACK)
                        setFlashMode(FlashMode.AUTO)
                        setImageFormat(ImageFormat.PNG)
                        setDirectory(Directory.DCIM)
                        addPlugin(ocrPlugin)
                    },
                    onCameraControllerReady = {
                        Logger.d { "PassportIdScanner onCameraControllerReady" }
                        cameraController = it
                        it.startSession()
                        ocrPlugin.initialize(it)
                        ocrPlugin.startRecognition()
                    },
                )
            }
            Column(
                modifier = Modifier.fillMaxWidth().background(Color.White).padding(16.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Top,
            ) {
                Text("Line1:", fontWeight = FontWeight.Bold)
                Text(line1 ?: "<not found yet>")
                Text("Line2:", fontWeight = FontWeight.Bold)
                Text(line2 ?: "<not found yet>")
            }
        }
    }
}
