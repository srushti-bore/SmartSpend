package com.smartspend.app.core.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smartspend.app.core.ui.theme.BrandPrimary
import com.smartspend.app.core.ui.theme.StatusDanger
import com.smartspend.app.core.ui.theme.StatusSuccess
import com.smartspend.app.core.ui.theme.StatusWarning
import com.smartspend.app.domain.intelligence.SafeSpendTier

@Composable
fun VisualMoodMascot(
    tier: SafeSpendTier,
    percentageUsed: Int,
    safeDaily: String,
    onMascotClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mascotAnim")
    val bounceScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (tier == SafeSpendTier.DANGER) 1.15f else 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (tier == SafeSpendTier.DANGER) 400 else 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounceScale"
    )

    val (mascotColor, emoji, moodTitle, moodMessage) = when (tier) {
        SafeSpendTier.HEALTHY -> Quad(
            BrandPrimary,
            "🥳",
            "Thriving & Savvy",
            "High savings velocity! Safe daily spend is ₹$safeDaily."
        )
        SafeSpendTier.MODERATE -> Quad(
            Color(0xFF4CD7F6),
            "🧘",
            "Mindful & Balanced",
            "Pacing smoothly! Keep daily spend near ₹$safeDaily."
        )
        SafeSpendTier.CAUTION -> Quad(
            StatusWarning,
            "⚡",
            "Cautious & Watchful",
            "Budget at $percentageUsed%. Pace discretionary buys."
        )
        SafeSpendTier.DANGER -> Quad(
            StatusDanger,
            "😱",
            "Budget Alert / Deficit",
            "100%+ consumed. Spending freeze strongly recommended."
        )
    }

    var quipIndex by remember { mutableIntStateOf(0) }
    val quips = listOf(
        "\"A penny saved is a penny earned!\"",
        "\"Your future self will thank you for budgeting today.\"",
        "\"Discipline is choosing between what you want now and what you want most.\"",
        "\"Stay consistent, build wealth!\""
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                quipIndex = (quipIndex + 1) % quips.size
                onMascotClick()
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Animated Mascot Aura
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(54.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .scale(bounceScale)
                        .clip(CircleShape)
                        .background(mascotColor.copy(alpha = 0.2f))
                )
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(mascotColor.copy(alpha = 0.35f))
                        .border(1.5.dp, mascotColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = emoji, style = MaterialTheme.typography.titleMedium)
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Brainy • $moodTitle",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = mascotColor
                    )
                    Text(
                        text = "$percentageUsed% Used",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = mascotColor
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (quipIndex == 0) moodMessage else quips[quipIndex],
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
