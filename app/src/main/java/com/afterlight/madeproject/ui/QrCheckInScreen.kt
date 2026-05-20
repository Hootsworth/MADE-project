package com.afterlight.madeproject.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.afterlight.madeproject.ui.components.SmoothButton
import com.afterlight.madeproject.ui.components.SmoothTextField
import com.afterlight.madeproject.ui.theme.*
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

@Composable
fun QrCheckInScreen(
    onBackClick: () -> Unit,
    viewModel: QrCheckInViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val status by viewModel.status.collectAsStateWithLifecycle()
    val loading by viewModel.loading.collectAsStateWithLifecycle()
    val scanned by viewModel.scanned.collectAsStateWithLifecycle()
    var hasCameraPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    var manualInput by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Snow)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SmoothButton(
                text = "Back",
                onClick = onBackClick,
                containerColor = Pearl.copy(alpha = 0.5f),
                contentColor = Coal,
                modifier = Modifier.fillMaxWidth(0.3f)
            )
            Text(text = "QR Check-In", style = GatherTypography.titleLarge, color = Coal)
        }

        Text(
            text = "Scan a ticket QR code to admit the guest.",
            style = GatherTypography.bodyLarge,
            color = LightTextMuted
        )

        when {
            !hasCameraPermission -> PermissionPanel(onRequest = { permissionLauncher.launch(Manifest.permission.CAMERA) })
            else -> ScannerPanel(
                onCodeScanned = { code ->
                    val accepted = viewModel.onScanned(code)
                    if (accepted) {
                        viewModel.checkInScanned()
                    }
                    accepted
                }
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Pearl),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(text = "Manual Entry", style = GatherTypography.titleLarge, color = Coal)
                SmoothTextField(
                    value = manualInput,
                    onValueChange = { manualInput = it },
                    label = "Paste ticket or QR link"
                )
                SmoothButton(
                    text = if (loading) "Checking In..." else "Check In",
                    onClick = {
                        if (viewModel.onScanned(manualInput)) {
                            viewModel.checkInScanned()
                        }
                    },
                    enabled = !loading,
                    containerColor = Moss,
                    contentColor = Snow,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Pearl.copy(alpha = 0.3f)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = "Scanned Value", style = GatherTypography.labelMedium, color = Coal)
                Text(
                    text = scanned.ifBlank { "Waiting for a QR code..." },
                    style = GatherTypography.bodyMedium,
                    color = LightTextMuted
                )
            }
        }

        status?.let {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Sand.copy(alpha = 0.2f)
            ) {
                Text(
                    text = it,
                    style = GatherTypography.bodyMedium,
                    color = Coal,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(60.dp))
    }
}

@Composable
private fun PermissionPanel(onRequest: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Pearl.copy(alpha = 0.3f)
    ) {
        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(text = "Camera Access Required", style = GatherTypography.titleLarge, color = Coal)
            Text(
                text = "Camera permission is needed to scan attendee ticket QRs.",
                style = GatherTypography.bodyLarge,
                color = LightTextMuted
            )
            SmoothButton(text = "Allow Camera", onClick = onRequest, containerColor = Coal, contentColor = Snow, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun ScannerPanel(onCodeScanned: (String) -> Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(420.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(Coal)
    ) {
        QrCameraPreview(
            modifier = Modifier.fillMaxSize(),
            onCodeScanned = onCodeScanned
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(24.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = Pearl.copy(alpha = 0.8f)
            ) {
                Text(
                    text = "Point camera at ticket QR",
                    style = GatherTypography.labelMedium,
                    color = Coal,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }
        }
    }
}

// QrCameraPreview and processQrFrame remain unchanged, just visually wrapped differently.
@Composable
private fun QrCameraPreview(
    modifier: Modifier = Modifier,
    onCodeScanned: (String) -> Boolean
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scanner = remember { BarcodeScanning.getClient() }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val scanLock = remember { AtomicBoolean(false) }

    DisposableEffect(Unit) {
        onDispose {
            scanner.close()
            cameraExecutor.shutdown()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { viewContext ->
            val previewView = PreviewView(viewContext).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }
            val cameraProviderFuture = ProcessCameraProvider.getInstance(viewContext)
            cameraProviderFuture.addListener(
                {
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    val analysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also {
                            it.setAnalyzer(cameraExecutor) { imageProxy ->
                                processQrFrame(
                                    imageProxy = imageProxy,
                                    scanner = scanner,
                                    scanLock = scanLock,
                                    onCode = onCodeScanned
                                )
                            }
                        }

                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        analysis
                    )
                },
                ContextCompat.getMainExecutor(context)
            )
            previewView
        }
    )
}

private fun processQrFrame(
    imageProxy: ImageProxy,
    scanner: BarcodeScanner,
    scanLock: AtomicBoolean,
    onCode: (String) -> Boolean
) {
    val mediaImage = imageProxy.image
    if (scanLock.get() || mediaImage == null) {
        imageProxy.close()
        return
    }

    val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
    scanner.process(image)
        .addOnSuccessListener { barcodes ->
            val qrValue = barcodes.firstOrNull { it.format == Barcode.FORMAT_QR_CODE }
                ?.rawValue
                .orEmpty()
            if (qrValue.isNotBlank() && onCode(qrValue)) {
                scanLock.compareAndSet(false, true)
            }
        }
        .addOnCompleteListener {
            imageProxy.close()
        }
}