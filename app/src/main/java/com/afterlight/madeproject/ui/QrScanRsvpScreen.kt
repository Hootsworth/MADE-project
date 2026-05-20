package com.afterlight.madeproject.ui

// (Imports are identical to original QrScanRsvpScreen + SmoothButton instead of BrutalistButton)
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
import com.afterlight.madeproject.domain.model.Event
import com.afterlight.madeproject.domain.model.EventStatus
import com.afterlight.madeproject.ui.components.SmoothButton
import com.afterlight.madeproject.ui.theme.*
import com.afterlight.madeproject.utils.DateTimeUtils
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.Executors

@Composable
fun QrScanRsvpScreen(
    onBackClick: () -> Unit,
    onEventClick: (String) -> Unit,
    viewModel: QrScanRsvpViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val eventId by viewModel.eventId.collectAsStateWithLifecycle()
    val lookupState by viewModel.eventLookupState.collectAsStateWithLifecycle()
    val event = (lookupState as? QrScanLookupState.Ready)?.event
    val status by viewModel.status.collectAsStateWithLifecycle()
    val rsvpDone by viewModel.rsvpDone.collectAsStateWithLifecycle()
    val rsvpLoading by viewModel.rsvpLoading.collectAsStateWithLifecycle()
    var hasCameraPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

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
            Text(text = "Scan RSVP", style = GatherTypography.titleLarge, color = Coal)
        }

        Text(
            text = "Scan an event QR. Confirm the details, then RSVP to share your details with the host.",
            style = GatherTypography.bodyLarge,
            color = LightTextMuted
        )

        when {
            !hasCameraPermission -> PermissionPanel(onRequest = { permissionLauncher.launch(Manifest.permission.CAMERA) })
            eventId.isBlank() -> ScannerPanel(onCodeScanned = viewModel::onCodeScanned)
            else -> EventConfirmationPanel(
                event = event,
                eventId = eventId,
                lookupState = lookupState,
                status = status,
                rsvpDone = rsvpDone,
                rsvpLoading = rsvpLoading,
                onRsvp = viewModel::rsvp,
                onNo = viewModel::clearScan,
                onEventClick = onEventClick
            )
        }

        status?.takeIf { eventId.isBlank() }?.let {
            StatusPanel(text = it)
        }

        Spacer(modifier = Modifier.height(100.dp))
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
                text = "Camera permission is needed to scan event QR codes.",
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
                    text = "Point camera at event QR",
                    style = GatherTypography.labelMedium,
                    color = Coal,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }
        }
    }
}

@Composable
private fun EventConfirmationPanel(
    event: Event?,
    eventId: String,
    lookupState: QrScanLookupState,
    status: String?,
    rsvpDone: Boolean,
    rsvpLoading: Boolean,
    onRsvp: () -> Unit,
    onNo: () -> Unit,
    onEventClick: (String) -> Unit
) {
    val eventLoading = lookupState is QrScanLookupState.Loading
    val eventMissing = lookupState is QrScanLookupState.NotFound
    val eventOver = event?.let {
        it.status == EventStatus.PAST || it.dateTime <= System.currentTimeMillis()
    } == true

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Pearl),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Text(text = "Confirm Event", style = GatherTypography.titleLarge, color = Coal)

            if (event == null) {
                Text(
                    text = when {
                        eventLoading -> "Looking up scanned event..."
                        eventMissing -> "That QR code does not match a live event."
                        else -> "Unable to load event details."
                    },
                    style = GatherTypography.bodyLarge,
                    color = LightTextMuted
                )
                Text(text = "ID: ${eventId.take(10)}", style = GatherTypography.labelMedium, color = Coal)
            } else {
                Text(text = event.title.ifBlank { "Untitled Event" }, style = GatherTypography.displayLarge, color = Coal)
                EventDetailLine(label = "Date", value = DateTimeUtils.formatEventTime(event.dateTime))
                EventDetailLine(label = "Location", value = event.venue.ifBlank { "TBD" })
                EventDetailLine(label = "Host", value = event.hostName.ifBlank { "Event Host" })
                EventDetailLine(label = "Signed Up", value = "${event.rsvpCount} / ${event.capacity}")
            }

            status?.let { StatusPanel(text = it) }

            if (eventMissing && status == null) {
                StatusPanel(text = "This QR code could not be matched to an event.")
            }

            if (eventOver && status == null) {
                StatusPanel(text = "This event has ended. RSVP is closed.")
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SmoothButton(
                    text = when {
                        eventMissing -> "Scan Again"
                        eventOver -> "Event Ended"
                        rsvpDone -> "RSVP Confirmed"
                        eventLoading -> "Looking Up..."
                        rsvpLoading -> "Confirming..."
                        else -> "RSVP Now"
                    },
                    onClick = if (eventMissing) onNo else onRsvp,
                    enabled = when {
                        eventMissing -> true
                        eventLoading -> false
                        else -> event != null && !eventOver && !rsvpDone && !rsvpLoading
                    },
                    containerColor = if (eventOver || eventMissing) Sand else Moss,
                    contentColor = if (eventOver || eventMissing) Coal else Snow,
                    modifier = Modifier.weight(1f)
                )
                SmoothButton(
                    text = "Cancel",
                    onClick = onNo,
                    containerColor = Pearl.copy(alpha = 0.5f),
                    contentColor = Coal,
                    modifier = Modifier.weight(1f)
                )
            }

            if (event != null) {
                SmoothButton(
                    text = "View Details",
                    onClick = { onEventClick(eventId) },
                    containerColor = Copper,
                    contentColor = Snow,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun EventDetailLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = GatherTypography.labelMedium, color = LightTextMuted)
        Text(text = value, style = GatherTypography.bodyMedium, color = Coal)
    }
}

@Composable
private fun StatusPanel(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Sand.copy(alpha = 0.2f)
    ) {
        Text(
            text = text,
            style = GatherTypography.bodyMedium,
            color = Coal,
            modifier = Modifier.padding(16.dp)
        )
    }
}

// QrCameraPreview and processQrFrame from original
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