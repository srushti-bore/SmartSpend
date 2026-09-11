package com.smartspend.app.feature.export

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smartspend.app.domain.usecase.export.ExportTransactionsUseCase
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

enum class ExportDateRange(val label: String) {
    THIS_MONTH("Current Month"),
    LAST_3_MONTHS("Last 3 Months"),
    ALL_TIME("All Time")
}

@Composable
fun ExportDialog(
    profileId: String,
    exportTransactionsUseCase: ExportTransactionsUseCase,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedRange by remember { mutableStateOf(ExportDateRange.THIS_MONTH) }
    var isExporting by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Export Financial Data", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Select time range to export as CSV spreadsheet:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                ExportDateRange.values().forEach { range ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (selectedRange == range),
                            onClick = { selectedRange = range }
                        )
                        Text(range.label, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isExporting = true
                    scope.launch {
                        val now = LocalDate.now()
                        val (startMs, endMs) = when (selectedRange) {
                            ExportDateRange.THIS_MONTH -> {
                                val s = now.withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                                val e = now.plusMonths(1).withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                                Pair(s, e)
                            }
                            ExportDateRange.LAST_3_MONTHS -> {
                                val s = now.minusMonths(2).withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                                val e = now.plusMonths(1).withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                                Pair(s, e)
                            }
                            ExportDateRange.ALL_TIME -> Pair(0L, Long.MAX_VALUE)
                        }

                        val csvContent = exportTransactionsUseCase(profileId, startMs, endMs)
                        shareCsv(context, csvContent)
                        isExporting = false
                        onDismiss()
                    }
                },
                enabled = !isExporting
            ) {
                Text(if (isExporting) "Generating..." else "Export & Share CSV")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun shareCsv(context: Context, csvContent: String) {
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, csvContent)
        type = "text/csv"
    }
    val shareIntent = Intent.createChooser(sendIntent, "Export SmartSpend Ledger CSV")
    context.startActivity(shareIntent)
}
