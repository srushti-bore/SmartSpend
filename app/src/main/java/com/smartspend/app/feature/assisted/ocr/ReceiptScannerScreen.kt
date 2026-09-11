package com.smartspend.app.feature.assisted.ocr

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.smartspend.app.core.money.MoneyUtils
import com.smartspend.app.core.ui.components.AtelierPillBadge
import com.smartspend.app.core.ui.components.DoubleHairlineRule
import com.smartspend.app.core.ui.components.DuplicateWarningBanner
import com.smartspend.app.core.ui.components.HairlineDivider
import com.smartspend.app.core.ui.components.SectionLabel
import com.smartspend.app.core.ui.theme.AtelierAmber
import com.smartspend.app.core.ui.theme.AtelierAmberSubtle
import com.smartspend.app.core.ui.theme.AtelierCanvas
import com.smartspend.app.core.ui.theme.AtelierCoral
import com.smartspend.app.core.ui.theme.AtelierCoralSubtle
import com.smartspend.app.core.ui.theme.AtelierHairline
import com.smartspend.app.core.ui.theme.AtelierInkMuted
import com.smartspend.app.core.ui.theme.AtelierPeriwinkle
import com.smartspend.app.core.ui.theme.AtelierPeriwinkleSubtle
import com.smartspend.app.core.ui.theme.AtelierPrimaryInk
import com.smartspend.app.core.ui.theme.AtelierSage
import com.smartspend.app.core.ui.theme.AtelierSageSubtle
import com.smartspend.app.core.ui.theme.AtelierSurfaceChalk
import com.smartspend.app.core.ui.theme.NewsreaderFontFamily
import java.io.File
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ReceiptScannerScreen(
    onNavigateBack: () -> Unit,
    onExpenseSaved: () -> Unit,
    sharedImageUri: Uri? = null,
    viewModel: ReceiptScannerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsState()

    var hasCameraPermission by remember { mutableStateOf(false) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.processImageUri(context, uri)
        }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    LaunchedEffect(sharedImageUri) {
        if (sharedImageUri != null) {
            viewModel.processImageUri(context, sharedImageUri, isScreenshot = true)
        }
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onExpenseSaved()
        }
    }

    Scaffold(
        containerColor = AtelierCanvas,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Voucher Scanner OCR",
                        fontFamily = NewsreaderFontFamily,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = AtelierPrimaryInk
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = AtelierPrimaryInk
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AtelierCanvas
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.parsedDraft == null && !uiState.isScanning) {
                // Camera Viewfinder Mode
                if (hasCameraPermission) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AndroidView(
                            factory = { ctx ->
                                val previewView = PreviewView(ctx)
                                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                                cameraProviderFuture.addListener({
                                    val cameraProvider = cameraProviderFuture.get()
                                    val preview = Preview.Builder().build().also {
                                        it.setSurfaceProvider(previewView.surfaceProvider)
                                    }
                                    val capture = ImageCapture.Builder()
                                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                                        .build()
                                    imageCapture = capture

                                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                                    try {
                                        cameraProvider.unbindAll()
                                        cameraProvider.bindToLifecycle(
                                            lifecycleOwner,
                                            cameraSelector,
                                            preview,
                                            capture
                                        )
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }, ContextCompat.getMainExecutor(ctx))
                                previewView
                            },
                            modifier = Modifier.fillMaxSize()
                        )

                        // Scan Frame Overlay
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .height(380.dp)
                                .align(Alignment.Center)
                                .border(2.dp, AtelierAmber, RoundedCornerShape(12.dp))
                                .background(Color.Black.copy(alpha = 0.15f))
                        )

                        // Bottom Controls
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .background(Color.Black.copy(alpha = 0.7f))
                                .padding(vertical = 24.dp, horizontal = 32.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Gallery Picker
                            IconButton(
                                onClick = { galleryLauncher.launch("image/*") },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = "Pick from Gallery",
                                    tint = Color.White
                                )
                            }

                            // Capture Shutter Button
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(AtelierAmber)
                                    .clickable {
                                        val capture = imageCapture ?: return@clickable
                                        val photoFile = File(
                                            context.cacheDir,
                                            "receipt_${System.currentTimeMillis()}.jpg"
                                        )
                                        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

                                        capture.takePicture(
                                            outputOptions,
                                            cameraExecutor,
                                            object : ImageCapture.OnImageSavedCallback {
                                                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                                    val uri = Uri.fromFile(photoFile)
                                                    viewModel.processImageUri(context, uri)
                                                }

                                                override fun onError(exception: ImageCaptureException) {
                                                    exception.printStackTrace()
                                                }
                                            }
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Capture Receipt",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            Spacer(modifier = Modifier.size(48.dp))
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Camera Permission Required",
                            fontFamily = NewsreaderFontFamily,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            color = AtelierPrimaryInk
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Grant camera access to scan physical bills and receipts, or pick an image from your gallery.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AtelierInkMuted
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        Button(
                            onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AtelierPrimaryInk,
                                contentColor = AtelierCanvas
                            ),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("Grant Permission")
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = { galleryLauncher.launch("image/*") },
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Image, contentDescription = null, tint = AtelierPrimaryInk)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Select from Gallery", color = AtelierPrimaryInk)
                        }
                    }
                }
            } else if (uiState.isScanning) {
                // OCR Processing Loading State
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(52.dp),
                        color = AtelierAmber,
                        strokeWidth = 3.dp
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        text = "Analyzing Voucher with On-Device AI...",
                        fontFamily = NewsreaderFontFamily,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = AtelierPrimaryInk
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Extracting merchant, total debit, date, and allocations",
                        style = MaterialTheme.typography.bodySmall,
                        color = AtelierInkMuted
                    )
                }
            } else {
                // Review & Confirmation Screen
                val draft = uiState.parsedDraft
                if (draft != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(AtelierCanvas)
                            .padding(horizontal = 20.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Spacer(modifier = Modifier.height(12.dp))

                        // Receipt Image Thumbnail
                        if (uiState.capturedImageUri != null) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                                    .border(1.dp, AtelierHairline, RoundedCornerShape(6.dp)),
                                shape = RoundedCornerShape(6.dp),
                                colors = CardDefaults.cardColors(containerColor = AtelierSurfaceChalk)
                            ) {
                                AsyncImage(
                                    model = uiState.capturedImageUri,
                                    contentDescription = "Scanned Receipt",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // Duplicate Warning Banner
                        if (!uiState.duplicateWarning.isNullOrBlank()) {
                            DuplicateWarningBanner(warningMessage = uiState.duplicateWarning)
                            Spacer(modifier = Modifier.height(14.dp))
                        }

                        // Summary Header Card
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = AtelierSurfaceChalk,
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, AtelierHairline, RoundedCornerShape(4.dp))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    SectionLabel(text = "Extracted Payee")
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = draft.title,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = AtelierPrimaryInk
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    SectionLabel(text = "Total Debit")
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (draft.amount != null) MoneyUtils.format(draft.amount, draft.currency) else "₹0.00",
                                        fontFamily = NewsreaderFontFamily,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = AtelierCoral
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        DoubleHairlineRule()
                        Spacer(modifier = Modifier.height(16.dp))

                        // Extracted Details Form
                        SectionLabel(text = "Merchant / Payee Narrative")
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = draft.title,
                            onValueChange = { viewModel.onTitleChanged(it) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(4.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AtelierAmber,
                                unfocusedBorderColor = AtelierHairline
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        SectionLabel(text = "Debit Sum (₹)")
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = draft.amount?.toPlainString() ?: "",
                            onValueChange = { viewModel.onAmountChanged(it) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(4.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AtelierAmber,
                                unfocusedBorderColor = AtelierHairline
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Category Picker
                        SectionLabel(text = "Ledger Allocation")
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            uiState.availableCategories.forEach { category ->
                                val isSelected = category.id == uiState.selectedCategory?.id
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.onCategorySelected(category) },
                                    label = { Text(text = category.name, style = MaterialTheme.typography.labelSmall) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = AtelierAmberSubtle,
                                        selectedLabelColor = AtelierAmber,
                                        containerColor = AtelierCanvas
                                    ),
                                    shape = RoundedCornerShape(4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Payment Method Picker
                        SectionLabel(text = "Settlement Method")
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            uiState.availablePaymentMethods.forEach { pm ->
                                val isSelected = pm.id == uiState.selectedPaymentMethod?.id
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.onPaymentMethodSelected(pm) },
                                    label = { Text(text = pm.label, style = MaterialTheme.typography.labelSmall) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = AtelierPeriwinkleSubtle,
                                        selectedLabelColor = AtelierPeriwinkle,
                                        containerColor = AtelierCanvas
                                    ),
                                    shape = RoundedCornerShape(4.dp)
                                )
                            }
                        }

                        if (!uiState.errorMessage.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = uiState.errorMessage ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = AtelierCoral
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Actions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.resetScan() },
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp), tint = AtelierPrimaryInk)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Retake Scan", color = AtelierPrimaryInk)
                            }

                            Button(
                                onClick = { viewModel.saveExpense() },
                                enabled = !uiState.isSaving && draft.amount != null,
                                shape = RoundedCornerShape(4.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AtelierPrimaryInk,
                                    contentColor = AtelierCanvas
                                )
                            ) {
                                if (uiState.isSaving) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        color = AtelierCanvas,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Post to Ledger")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}
