package com.smartspend.app.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.smartspend.app.core.ui.theme.AtelierAmber
import com.smartspend.app.core.ui.theme.AtelierCoral
import com.smartspend.app.core.ui.theme.AtelierHairlineDark
import kotlin.math.sqrt

/**
 * Interactive 3x3 Pattern Lock View.
 * Renders a 3x3 matrix of 9 tactile dots with real-time gesture tracking,
 * continuous connecting line segments, active node halo glows, and haptic feedback.
 */
@Composable
fun PatternLockView(
    modifier: Modifier = Modifier,
    size: Dp = 280.dp,
    minDots: Int = 4,
    isError: Boolean = false,
    enabled: Boolean = true,
    activeColor: Color = AtelierAmber,
    errorColor: Color = AtelierCoral,
    dotInactiveColor: Color = AtelierHairlineDark,
    onPatternStarted: () -> Unit = {},
    onPatternCompleted: (pattern: String) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val selectedDots = remember { mutableStateListOf<Int>() }
    var currentDragPosition by remember { mutableStateOf<Offset?>(null) }

    val currentColor by animateColorAsState(
        targetValue = if (isError) errorColor else activeColor,
        animationSpec = tween(durationMillis = 200),
        label = "pattern_color"
    )

    Box(
        modifier = modifier
            .size(size)
            .aspectRatio(1f)
            .padding(16.dp)
            .pointerInput(enabled, isError) {
                if (!enabled) return@pointerInput

                detectDragGestures(
                    onDragStart = { startOffset ->
                        selectedDots.clear()
                        currentDragPosition = startOffset
                        onPatternStarted()

                        val dotIndex = getDotIndexAtPosition(startOffset, this.size.width.toFloat(), this.size.height.toFloat())
                        if (dotIndex != null && dotIndex !in selectedDots) {
                            selectedDots.add(dotIndex)
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        currentDragPosition = change.position

                        val dotIndex = getDotIndexAtPosition(change.position, this.size.width.toFloat(), this.size.height.toFloat())
                        if (dotIndex != null && dotIndex !in selectedDots) {
                            // Check if jumping over middle dot (e.g. 0 to 2 passes through 1, 0 to 8 passes through 4)
                            if (selectedDots.isNotEmpty()) {
                                val lastDot = selectedDots.last()
                                val midDot = getIntermediateDot(lastDot, dotIndex)
                                if (midDot != null && midDot !in selectedDots) {
                                    selectedDots.add(midDot)
                                }
                            }
                            selectedDots.add(dotIndex)
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                    },
                    onDragEnd = {
                        currentDragPosition = null
                        if (selectedDots.size >= minDots) {
                            val patternString = selectedDots.joinToString("-")
                            onPatternCompleted(patternString)
                        } else if (selectedDots.isNotEmpty()) {
                            // Too short pattern
                            onPatternCompleted("")
                        }
                    },
                    onDragCancel = {
                        selectedDots.clear()
                        currentDragPosition = null
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = this.size.width
            val canvasHeight = this.size.height
            val cellWidth = canvasWidth / 3f
            val cellHeight = canvasHeight / 3f

            val dotCenters = Array(9) { index ->
                val row = index / 3
                val col = index % 3
                Offset(
                    x = col * cellWidth + cellWidth / 2f,
                    y = row * cellHeight + cellHeight / 2f
                )
            }

            val strokeWidthPx = 4.5.dp.toPx()
            val outerRadiusPx = 22.dp.toPx()
            val innerRadiusPx = 5.5.dp.toPx()

            // 1. Draw connecting lines between selected dots
            if (selectedDots.size > 1) {
                for (i in 0 until selectedDots.size - 1) {
                    val p1 = dotCenters[selectedDots[i]]
                    val p2 = dotCenters[selectedDots[i + 1]]
                    drawLine(
                        color = currentColor,
                        start = p1,
                        end = p2,
                        strokeWidth = strokeWidthPx,
                        cap = StrokeCap.Round
                    )
                }
            }

            // 2. Draw trailing line to finger drag point
            if (currentDragPosition != null && selectedDots.isNotEmpty()) {
                val lastCenter = dotCenters[selectedDots.last()]
                drawLine(
                    color = currentColor.copy(alpha = 0.7f),
                    start = lastCenter,
                    end = currentDragPosition!!,
                    strokeWidth = strokeWidthPx,
                    cap = StrokeCap.Round
                )
            }

            // 3. Draw 9 dots (outer rings + inner center cores)
            for (i in 0..8) {
                val center = dotCenters[i]
                val isSelected = selectedDots.contains(i)

                if (isSelected) {
                    // Outer glow halo
                    drawCircle(
                        color = currentColor.copy(alpha = 0.18f),
                        radius = outerRadiusPx,
                        center = center
                    )
                    // Outer accent ring
                    drawCircle(
                        color = currentColor,
                        radius = outerRadiusPx,
                        center = center,
                        style = Stroke(width = 2.dp.toPx())
                    )
                    // Inner solid dot
                    drawCircle(
                        color = currentColor,
                        radius = innerRadiusPx * 1.3f,
                        center = center
                    )
                } else {
                    // Unselected subtle node
                    drawCircle(
                        color = dotInactiveColor,
                        radius = innerRadiusPx,
                        center = center
                    )
                }
            }
        }
    }
}

/**
 * Hit-test helper to determine which dot index (0..8) contains the touch position.
 */
private fun getDotIndexAtPosition(position: Offset, width: Float, height: Float): Int? {
    val cellWidth = width / 3f
    val cellHeight = height / 3f
    val hitRadius = (cellWidth * 0.42f)

    for (i in 0..8) {
        val row = i / 3
        val col = i % 3
        val centerX = col * cellWidth + cellWidth / 2f
        val centerY = row * cellHeight + cellHeight / 2f

        val dx = position.x - centerX
        val dy = position.y - centerY
        val distance = sqrt(dx * dx + dy * dy)

        if (distance <= hitRadius) {
            return i
        }
    }
    return null
}

/**
 * Returns the intermediate dot if connecting between two dots crosses directly over a center dot.
 */
private fun getIntermediateDot(dotA: Int, dotB: Int): Int? {
    val rowA = dotA / 3
    val colA = dotA % 3
    val rowB = dotB / 3
    val colB = dotB % 3

    val dRow = rowB - rowA
    val dCol = colB - colA

    // If step is exactly 2 in row/col or diagonal
    if ((dRow == 0 && (dCol == 2 || dCol == -2)) ||
        (dCol == 0 && (dRow == 2 || dRow == -2)) ||
        ((dRow == 2 || dRow == -2) && (dCol == 2 || dCol == -2))
    ) {
        val midRow = (rowA + rowB) / 2
        val midCol = (colA + colB) / 2
        return midRow * 3 + midCol
    }
    return null
}
