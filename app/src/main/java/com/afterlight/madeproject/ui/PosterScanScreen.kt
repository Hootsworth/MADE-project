package com.afterlight.madeproject.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.afterlight.madeproject.domain.model.Event
import com.afterlight.madeproject.ui.components.SmoothButton
import com.afterlight.madeproject.ui.theme.*
import com.afterlight.madeproject.utils.DateTimeUtils

@Composable
fun PosterScanScreen(
    onBackClick: () -> Unit,
    onEventClick: (String) -> Unit,
    viewModel: PosterScanViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val analyzing by viewModel.analyzing.collectAsStateWithLifecycle()
    val matchedEvents by viewModel.matchedEvents.collectAsStateWithLifecycle()
    val status by viewModel.status.collectAsStateWithLifecycle()

    var hasCameraPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { hasCameraPermission = it }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Snow)
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = Pearl.copy(alpha = 0.5f),
                modifier = Modifier.size(48.dp).clickable(onClick = onBackClick)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.ChevronLeft, contentDescription = "Back", tint = Coal)
                }
            }
            Text(text = "Poster AI", style = GatherTypography.labelLarge, color = Coal)
        }

        if (!hasCameraPermission) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = Pearl.copy(alpha = 0.3f)
            ) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Camera Required", style = GatherTypography.titleLarge, color = Coal)
                    Text("Permission is needed to analyze posters.", style = GatherTypography.bodyLarge, color = LightTextMuted)
                    SmoothButton("Allow Camera", onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) })
                }
            }
            return
        }

        if (matchedEvents.isEmpty()) {
            Text(
                text = "Snap a picture of a physical poster. AI will extract the details and find the RSVP link.",
                style = GatherTypography.bodyLarge,
                color = LightTextMuted
            )

            PosterCameraCapture(
                analyzing = analyzing,
                onCapture = { proxy -> viewModel.analyzePoster(proxy) }
            )

            status?.let {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Sand.copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (analyzing) CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Coal, strokeWidth = 2.dp)
                        Text(text = it, style = GatherTypography.bodyMedium, color = Coal)
                    }
                }
            }
        } else {
            // Results Mode
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(text = "Matches Found", style = GatherTypography.titleLarge, color = Coal)
                Text(text = "Scan again", style = GatherTypography.labelMedium, color = Copper, modifier = Modifier.clickable { viewModel.reset() })
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(matchedEvents) { event ->
                    MatchedEventCard(event = event, onClick = { onEventClick(event.eventId) })
                }
            }
        }
    }
}

@Composable
private fun PosterCameraCapture(analyzing: Boolean, onCapture: (ImageProxy) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val imageCapture = remember { ImageCapture.Builder().build() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(Coal)
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { viewContext ->
                val previewView = PreviewView(viewContext)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(viewContext)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture)
                }, ContextCompat.getMainExecutor(context))
                previewView
            }
        )

        // Capture Button Overlay
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(24.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = Paper,
                modifier = Modifier
                    .size(64.dp)
                    .clickable(enabled = !analyzing) {
                        imageCapture.takePicture(
                            ContextCompat.getMainExecutor(context),
                            object : ImageCapture.OnImageCapturedCallback() {
                                override fun onCaptureSuccess(image: ImageProxy) {
                                    onCapture(image)
                                }
                                override fun onError(exception: ImageCaptureException) {}
                            }
                        )
                    }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.PhotoCamera, contentDescription = "Capture", tint = Coal, modifier = Modifier.size(28.dp))
                }
            }
        }
    }
}

@Composable
private fun MatchedEventCard(event: Event, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Paper),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = event.coverImageUrl.ifBlank { "https://source.unsplash.com/800x600/?campus" },
                contentDescription = null,
                modifier = Modifier.size(80.dp).clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = event.title, style = GatherTypography.titleLarge, color = Coal)
                Text(text = DateTimeUtils.formatEventTime(event.dateTime), style = GatherTypography.bodyMedium, color = LightTextMuted)
                Text(text = event.hostName, style = GatherTypography.labelSmall, color = Copper)
            }
        }
    }
}