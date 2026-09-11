package com.smartspend.app.core.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartspend.app.core.money.MoneyUtils
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
import com.smartspend.app.domain.model.BudgetProgress
import com.smartspend.app.domain.model.BudgetStatus
import com.smartspend.app.domain.model.Expense
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String? = null,
    backgroundColor: Color = AtelierSurfaceChalk,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.border(1.dp, AtelierHairline, RoundedCornerShape(4.dp)),
        shape = RoundedCornerShape(4.dp),
        color = backgroundColor
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            SectionLabel(text = title)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontFamily = NewsreaderFontFamily,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = AtelierPrimaryInk
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = AtelierInkMuted
                )
            }
        }
    }
}

@Composable
fun BudgetProgressBar(
    progress: BudgetProgress,
    modifier: Modifier = Modifier
) {
    val statusColor = when (progress.status) {
        BudgetStatus.ON_TRACK -> AtelierSage
        BudgetStatus.NEAR_LIMIT -> AtelierAmber
        BudgetStatus.OVER_BUDGET -> AtelierCoral
    }

    val statusBgColor = when (progress.status) {
        BudgetStatus.ON_TRACK -> AtelierSageSubtle
        BudgetStatus.NEAR_LIMIT -> AtelierAmberSubtle
        BudgetStatus.OVER_BUDGET -> AtelierCoralSubtle
    }

    val statusLabel = when (progress.status) {
        BudgetStatus.ON_TRACK -> "On Track"
        BudgetStatus.NEAR_LIMIT -> "Near Limit"
        BudgetStatus.OVER_BUDGET -> "Over Budget"
    }

    val animatedProgress by animateFloatAsState(
        targetValue = (progress.percentageUsed / 100f).coerceIn(0f, 1f),
        label = "BudgetProgress"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, AtelierHairline, RoundedCornerShape(4.dp)),
        shape = RoundedCornerShape(4.dp),
        color = AtelierCanvas
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    SectionLabel(text = "Allocation Register")
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${progress.budget.type.name.lowercase().replaceFirstChar { it.uppercase() }} Budget",
                        style = MaterialTheme.typography.titleMedium,
                        color = AtelierPrimaryInk
                    )
                }

                AtelierPillBadge(
                    text = statusLabel,
                    backgroundColor = statusBgColor,
                    contentColor = statusColor
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            LinearProgressIndicator(
                progress = animatedProgress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = statusColor,
                trackColor = AtelierSurfaceChalk
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "Spent: ${MoneyUtils.format(progress.spentAmount)} (${progress.percentageUsed}%)",
                    style = MaterialTheme.typography.bodySmall,
                    color = AtelierInkMuted
                )
                Text(
                    text = "Limit: ${MoneyUtils.format(progress.budget.amount)}",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = AtelierPrimaryInk
                )
            }
        }
    }
}

@Composable
fun ExpenseItemCard(
    expense: Expense,
    categoryName: String = "Expense",
    categoryColorHex: String = "#5B7598",
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val dateStr = SimpleDateFormat("hh:mm a • dd MMM", Locale.getDefault()).format(Date(expense.date))

    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Monogram/Icon
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(AtelierPeriwinkleSubtle),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Category,
                    contentDescription = categoryName,
                    tint = AtelierPeriwinkle,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Merchant / Particulars & Date
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = expense.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = AtelierPrimaryInk
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$dateStr • $categoryName",
                    style = MaterialTheme.typography.bodySmall,
                    color = AtelierInkMuted
                )
            }

            // Amount in Newsreader Serif with DR indicator
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "↓ ${MoneyUtils.format(expense.amount, expense.currency)}",
                    fontFamily = NewsreaderFontFamily,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Normal,
                    color = AtelierPrimaryInk
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "DR",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = AtelierInkMuted
                )
            }
        }
        HairlineDivider()
    }
}
