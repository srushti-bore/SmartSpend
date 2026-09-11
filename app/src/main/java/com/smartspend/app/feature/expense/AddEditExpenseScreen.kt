package com.smartspend.app.feature.expense

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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditExpenseScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddEditExpenseViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showAccountMenu by remember { mutableStateOf(false) }

    LaunchedEffect(state.isSaved, state.isDeleted) {
        if (state.isSaved || state.isDeleted) {
            onNavigateBack()
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            containerColor = AtelierCanvas,
            title = {
                Text(
                    text = "Delete Ledger Entry",
                    fontFamily = NewsreaderFontFamily,
                    fontSize = 20.sp,
                    color = AtelierPrimaryInk
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to strike this entry from the general ledger?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AtelierInkMuted
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation = false
                        viewModel.delete()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = AtelierCoral)
                ) {
                    Text("Delete Entry", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel", color = AtelierInkMuted)
                }
            }
        )
    }

    Scaffold(
        containerColor = AtelierCanvas
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AtelierCanvas)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // 1. GRAB HANDLE & HEADER STRIP
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 40.dp, height = 4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(AtelierHairline)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(AtelierAmber)
                    )
                    Text(
                        text = if (state.isEditMode) "LEDGER ENTRY • EDIT RECORD" else "LEDGER ENTRY • OUTFLOW",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.08.sp
                        ),
                        color = AtelierInkMuted
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (state.isEditMode) {
                        IconButton(
                            onClick = { showDeleteConfirmation = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = AtelierCoral,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = AtelierPrimaryInk,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            HairlineDivider()

            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
                // Duplicate Warning Banner
                if (!state.duplicateWarning.isNullOrBlank()) {
                    DuplicateWarningBanner(warningMessage = state.duplicateWarning)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 2. HERO AMOUNT INPUT
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    SectionLabel(text = "Debit Sum")
                    Spacer(modifier = Modifier.height(6.dp))

                    val currencySymbol = when (state.currency.uppercase()) {
                        "INR" -> "₹"
                        "USD" -> "$"
                        "EUR" -> "€"
                        "GBP" -> "£"
                        else -> "₹"
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .border(
                                width = 0.dp,
                                color = Color.Transparent
                            )
                            .padding(bottom = 4.dp)
                    ) {
                        Text(
                            text = currencySymbol,
                            fontFamily = NewsreaderFontFamily,
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Light,
                            color = AtelierInkMuted.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        BasicTextField(
                            value = state.amountInput,
                            onValueChange = viewModel::onAmountChange,
                            textStyle = TextStyle(
                                fontFamily = NewsreaderFontFamily,
                                fontSize = 44.sp,
                                fontWeight = FontWeight.Normal,
                                color = AtelierPrimaryInk,
                                textAlign = TextAlign.Center
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            cursorBrush = SolidColor(AtelierAmber),
                            modifier = Modifier
                                .width(200.dp)
                                .border(0.dp, Color.Transparent)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(180.dp)
                            .height(2.dp)
                            .background(AtelierAmber)
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Entry currency: ${state.currency} ($currencySymbol)",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = AtelierInkMuted
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                DoubleHairlineRule(modifier = Modifier.padding(vertical = 10.dp))

                // 3. DRAWN FROM ACCOUNT & STAMP TIMESTAMP
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Account selector
                    Column(modifier = Modifier.weight(1f)) {
                        SectionLabel(text = "Drawn From Account")
                        Spacer(modifier = Modifier.height(6.dp))
                        Box {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, AtelierHairline, RoundedCornerShape(4.dp))
                                    .clickable { showAccountMenu = true },
                                shape = RoundedCornerShape(4.dp),
                                color = AtelierSurfaceChalk
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val selectedMethod = state.paymentMethods.find { it.id == state.selectedPaymentMethodId }
                                    Text(
                                        text = selectedMethod?.label ?: "Debit Passbook",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                                        color = AtelierPrimaryInk
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ExpandMore,
                                        contentDescription = null,
                                        tint = AtelierInkMuted,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            DropdownMenu(
                                expanded = showAccountMenu,
                                onDismissRequest = { showAccountMenu = false }
                            ) {
                                state.paymentMethods.forEach { method ->
                                    DropdownMenuItem(
                                        text = { Text(method.label) },
                                        onClick = {
                                            viewModel.onPaymentMethodSelect(method.id)
                                            showAccountMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Timestamp
                    Column(modifier = Modifier.weight(1f)) {
                        SectionLabel(text = "Stamp Timestamp")
                        Spacer(modifier = Modifier.height(6.dp))
                        val dateFormatted = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(state.date))
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, AtelierHairline, RoundedCornerShape(4.dp)),
                            shape = RoundedCornerShape(4.dp),
                            color = AtelierSurfaceChalk
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = dateFormatted,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                                    color = AtelierPrimaryInk
                                )
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = AtelierPeriwinkle,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // 4. MERCHANT / PAYEE NARRATIVE
                Column(modifier = Modifier.fillMaxWidth()) {
                    SectionLabel(text = "Merchant / Payee Narrative")
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, AtelierHairline, RoundedCornerShape(4.dp)),
                        shape = RoundedCornerShape(4.dp),
                        color = AtelierSurfaceChalk
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storefront,
                                contentDescription = null,
                                tint = AtelierInkMuted,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            BasicTextField(
                                value = state.title,
                                onValueChange = viewModel::onTitleChange,
                                textStyle = TextStyle(
                                    fontFamily = com.smartspend.app.core.ui.theme.IBMPlexSansFontFamily,
                                    fontSize = 14.sp,
                                    color = AtelierPrimaryInk
                                ),
                                singleLine = true,
                                cursorBrush = SolidColor(AtelierAmber),
                                modifier = Modifier.fillMaxWidth(),
                                decorationBox = { innerTextField ->
                                    if (state.title.isEmpty()) {
                                        Text(
                                            text = "e.g. Corner Stationer or Payee",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontSize = 14.sp,
                                                color = AtelierInkMuted.copy(alpha = 0.5f)
                                            )
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 5. CATEGORY SELECTION CHIPS
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SectionLabel(text = "Ledger Allocation")
                        Surface(
                            shape = RoundedCornerShape(9999.dp),
                            color = AtelierAmberSubtle
                        ) {
                            Text(
                                text = "1 Tag Selected",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    color = AtelierAmber,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        state.categories.forEach { category ->
                            val isSelected = state.selectedCategoryId == category.id
                            val chipBg = if (isSelected) AtelierPeriwinkle else AtelierSurfaceChalk
                            val chipText = if (isSelected) Color.White else AtelierPrimaryInk
                            val chipBorder = if (isSelected) AtelierPeriwinkle else AtelierHairline

                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = chipBg,
                                modifier = Modifier
                                    .border(1.dp, chipBorder, RoundedCornerShape(4.dp))
                                    .clickable { viewModel.onCategorySelect(category.id) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = category.name,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                        ),
                                        color = chipText
                                    )
                                    if (isSelected) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // 6. NOTES REGISTER INPUT
                Column(modifier = Modifier.fillMaxWidth()) {
                    SectionLabel(text = "Folio Memo / Notes")
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, AtelierHairline, RoundedCornerShape(4.dp)),
                        shape = RoundedCornerShape(4.dp),
                        color = AtelierSurfaceChalk
                    ) {
                        BasicTextField(
                            value = state.notes,
                            onValueChange = viewModel::onNotesChange,
                            textStyle = TextStyle(
                                fontFamily = com.smartspend.app.core.ui.theme.IBMPlexSansFontFamily,
                                fontSize = 13.sp,
                                color = AtelierPrimaryInk
                            ),
                            cursorBrush = SolidColor(AtelierAmber),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            decorationBox = { innerTextField ->
                                if (state.notes.isEmpty()) {
                                    Text(
                                        text = "Optional particulars or memo narrative...",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = AtelierInkMuted.copy(alpha = 0.5f)
                                        )
                                    )
                                }
                                innerTextField()
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 7. RECURRING TOGGLE
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Recurring Voucher",
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp),
                            color = AtelierPrimaryInk
                        )
                        Text(
                            text = "Auto-repeat this debit in next cycle",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = AtelierInkMuted
                        )
                    }
                    Switch(
                        checked = state.isRecurring,
                        onCheckedChange = viewModel::onRecurringToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = AtelierAmber,
                            uncheckedThumbColor = AtelierInkMuted,
                            uncheckedTrackColor = AtelierSurfaceChalk
                        )
                    )
                }

                if (state.errorMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = state.errorMessage ?: "",
                        color = AtelierCoral,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // 8. SUBMIT / POST TO LEDGER BUTTON
                Button(
                    onClick = viewModel::save,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AtelierPrimaryInk,
                        contentColor = AtelierCanvas
                    ),
                    enabled = !state.isLoading
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            color = AtelierCanvas,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text(
                            text = if (state.isEditMode) "Update Ledger Record" else "Post to General Ledger",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}
