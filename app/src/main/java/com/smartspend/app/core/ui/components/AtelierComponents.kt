package com.smartspend.app.core.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

/**
 * 1px Hairline divider in accordance with Atelier Ledger design system.
 */
@Composable
fun HairlineDivider(
    modifier: Modifier = Modifier,
    color: Color = AtelierHairline,
    thickness: Dp = 1.dp
) {
    HorizontalDivider(
        modifier = modifier,
        thickness = thickness,
        color = color
    )
}

/**
 * Double Hairline Rule (classical ledger close marker: two 1px lines with 3px space).
 */
@Composable
fun DoubleHairlineRule(
    modifier: Modifier = Modifier,
    color: Color = AtelierHairline
) {
    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalDivider(thickness = 1.dp, color = color)
        Spacer(modifier = Modifier.height(3.dp))
        HorizontalDivider(thickness = 1.dp, color = color)
    }
}

/**
 * Editorial Section Label in tracked uppercase.
 */
@Composable
fun SectionLabel(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = AtelierInkMuted
) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = color,
        modifier = modifier
    )
}

/**
 * Pill status badge (e.g. Reconciled, Near Limit, On Track, Outflow).
 */
@Composable
fun AtelierPillBadge(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    backgroundColor: Color = AtelierSageSubtle,
    contentColor: Color = AtelierSage
) {
    Surface(
        shape = RoundedCornerShape(9999.dp),
        color = backgroundColor,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = text.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.08.sp
                ),
                color = contentColor,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

/**
 * Circular progress ring for Safe-to-Spend arc and Budget gauges.
 */
@Composable
fun CircularGauge(
    progress: Float, // 0.0f to 1.0f
    modifier: Modifier = Modifier,
    size: Dp = 200.dp,
    strokeWidth: Dp = 6.dp,
    trackColor: Color = AtelierSurfaceChalk,
    progressColor: Color = AtelierAmber,
    innerContent: @Composable () -> Unit = {}
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        label = "CircularGaugeProgress"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // Subtle background fill
        Box(
            modifier = Modifier
                .size(size - 16.dp)
                .clip(CircleShape)
                .background(AtelierSurfaceChalk.copy(alpha = 0.7f))
                .border(1.dp, AtelierHairline.copy(alpha = 0.5f), CircleShape)
        )

        Canvas(modifier = Modifier.size(size)) {
            val stroke = strokeWidth.toPx()
            val radius = (this.size.minDimension - stroke) / 2
            val center = this.center

            // Background track
            drawCircle(
                color = trackColor,
                radius = radius,
                center = center,
                style = Stroke(width = stroke)
            )

            // Progress arc (-90 deg = top start)
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = animatedProgress * 360f,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }

        // Centered Content
        innerContent()
    }
}

/**
 * Metric Shelf block (Open-air, hairline bordered metric).
 */
@Composable
fun MetricShelf(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    unit: String? = null,
    subtitle: String? = null,
    valueColor: Color = AtelierPrimaryInk,
    alignment: Alignment.Horizontal = Alignment.Start
) {
    Column(
        modifier = modifier,
        horizontalAlignment = alignment
    ) {
        SectionLabel(text = title)
        Spacer(modifier = Modifier.height(2.dp))
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = if (alignment == Alignment.End) Arrangement.End else Arrangement.Start
        ) {
            Text(
                text = value,
                fontFamily = NewsreaderFontFamily,
                fontSize = 24.sp,
                fontWeight = FontWeight.Normal,
                color = valueColor
            )
            if (unit != null) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = unit,
                    style = MaterialTheme.typography.labelSmall,
                    color = valueColor.copy(alpha = 0.8f)
                )
            }
        }
        if (subtitle != null) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = AtelierInkMuted
            )
        }
    }
}

/**
 * Ledger Seal / Reconciliation verification footer.
 */
@Composable
fun LedgerSealFooter(
    modifier: Modifier = Modifier,
    text: String = "Reconciled Today • Archival System",
    timestamp: String = "08:30 AM"
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        DoubleHairlineRule()
        Spacer(modifier = Modifier.height(14.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.VerifiedUser,
                contentDescription = null,
                tint = AtelierSage,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "$text ($timestamp)".uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.08.sp),
                color = AtelierInkMuted
            )
        }
    }
}

/**
 * Atelier Bottom Navigation Dock with floating Center Amber FAB.
 */
@Composable
fun AtelierBottomNavBar(
    selectedRoute: String,
    onNavigateToRoute: (String) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = AtelierCanvas.copy(alpha = 0.96f),
        shadowElevation = 0.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            HairlineDivider()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tab 1: Ledger (Dashboard)
                BottomNavItem(
                    label = "Ledger",
                    icon = Icons.Default.Book,
                    isSelected = selectedRoute == "dashboard",
                    onClick = { onNavigateToRoute("dashboard") }
                )

                // Tab 2: Budgets
                BottomNavItem(
                    label = "Budgets",
                    icon = Icons.Default.PieChart,
                    isSelected = selectedRoute == "budgets",
                    onClick = { onNavigateToRoute("budgets") }
                )

                // Central Amber FAB for Add Expense
                Box(
                    modifier = Modifier.offset(y = (-14).dp),
                    contentAlignment = Alignment.Center
                ) {
                    FloatingActionButton(
                        onClick = onAddClick,
                        shape = CircleShape,
                        containerColor = AtelierAmber,
                        contentColor = Color.White,
                        elevation = FloatingActionButtonDefaults.elevation(
                            defaultElevation = 3.dp,
                            pressedElevation = 6.dp
                        ),
                        modifier = Modifier
                            .size(52.dp)
                            .border(2.dp, AtelierCanvas, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Expense",
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                // Tab 3: Scan / OCR Receipt
                BottomNavItem(
                    label = "Scan",
                    icon = Icons.Default.QrCodeScanner,
                    isSelected = selectedRoute == "receipt_scan",
                    onClick = { onNavigateToRoute("receipt_scan") }
                )

                // Tab 4: Settings
                BottomNavItem(
                    label = "Settings",
                    icon = Icons.Default.Settings,
                    isSelected = selectedRoute == "settings",
                    onClick = { onNavigateToRoute("settings") }
                )
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) AtelierPrimaryInk else AtelierInkMuted,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                letterSpacing = 0.06.sp
            ),
            color = if (isSelected) AtelierPrimaryInk else AtelierInkMuted
        )
    }
}
