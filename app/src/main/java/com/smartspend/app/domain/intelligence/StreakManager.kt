package com.smartspend.app.domain.intelligence

import com.smartspend.app.domain.model.Expense
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

data class StreakBadge(
    val id: String,
    val title: String,
    val description: String,
    val emoji: String,
    val targetDays: Int,
    val isUnlocked: Boolean
)

data class StreakStatus(
    val currentStreakDays: Int,
    val longestStreakDays: Int,
    val isLoggedToday: Boolean,
    val motivationalTip: String,
    val badges: List<StreakBadge>
)

@Singleton
class StreakManager @Inject constructor() {

    private val zoneId = ZoneId.of("Asia/Kolkata")

    fun calculateStreak(expenses: List<Expense>, referenceDate: LocalDate = LocalDate.now(zoneId)): StreakStatus {
        if (expenses.isEmpty()) {
            return StreakStatus(
                currentStreakDays = 0,
                longestStreakDays = 0,
                isLoggedToday = false,
                motivationalTip = "Log your first expense today to start your SmartSpend streak! 🚀",
                badges = generateBadges(0)
            )
        }

        // Extract distinct local dates sorted ascending
        val logDates = expenses.map {
            Instant.ofEpochMilli(it.date).atZone(zoneId).toLocalDate()
        }.distinct().sorted()

        val isLoggedToday = logDates.contains(referenceDate)
        val isLoggedYesterday = logDates.contains(referenceDate.minusDays(1))

        // Calculate current streak backwards from today or yesterday
        var currentStreak = 0
        var checkDate: LocalDate? = if (isLoggedToday) referenceDate else if (isLoggedYesterday) referenceDate.minusDays(1) else null

        if (checkDate != null) {
            val dateSet = logDates.toSet()
            var curr = checkDate
            while (curr != null && dateSet.contains(curr)) {
                currentStreak++
                curr = curr.minusDays(1)
            }
        }

        // Calculate longest streak across all history
        var longestStreak = 0
        var tempStreak = 0
        var previousDate: LocalDate? = null

        for (date in logDates) {
            val prev = previousDate
            if (prev == null) {
                tempStreak = 1
            } else {
                val daysBetween = ChronoUnit.DAYS.between(prev, date)
                if (daysBetween == 1L) {
                    tempStreak++
                } else if (daysBetween > 1L) {
                    tempStreak = 1
                }
            }
            if (tempStreak > longestStreak) {
                longestStreak = tempStreak
            }
            previousDate = date
        }

        val motivationalTip = when {
            isLoggedToday -> "Awesome! Streak active ($currentStreak days). Keep recording to stay on track! 🔥"
            isLoggedYesterday -> "You logged yesterday! Add an entry today to keep your $currentStreak-day streak alive! ⚡"
            currentStreak > 0 -> "You're on a $currentStreak-day streak! Log today's spends to keep the momentum going! 💪"
            else -> "Start fresh today! Regular tracking builds long-term wealth habits. 🌟"
        }

        val badges = generateBadges(longestStreak.coerceAtLeast(currentStreak))

        return StreakStatus(
            currentStreakDays = currentStreak,
            longestStreakDays = longestStreak,
            isLoggedToday = isLoggedToday,
            motivationalTip = motivationalTip,
            badges = badges
        )
    }

    private fun generateBadges(maxStreak: Int): List<StreakBadge> {
        return listOf(
            StreakBadge(
                id = "badge_3",
                title = "3-Day Starter",
                description = "Logged expenses 3 days in a row",
                emoji = "🔥",
                targetDays = 3,
                isUnlocked = maxStreak >= 3
            ),
            StreakBadge(
                id = "badge_7",
                title = "7-Day Focused",
                description = "Consistent tracking for 1 full week",
                emoji = "⚡",
                targetDays = 7,
                isUnlocked = maxStreak >= 7
            ),
            StreakBadge(
                id = "badge_14",
                title = "14-Day Dedicated",
                description = "2 weeks of unbroken financial discipline",
                emoji = "🎯",
                targetDays = 14,
                isUnlocked = maxStreak >= 14
            ),
            StreakBadge(
                id = "badge_30",
                title = "30-Day Master",
                description = "1 month financial tracking mastery",
                emoji = "🏆",
                targetDays = 30,
                isUnlocked = maxStreak >= 30
            ),
            StreakBadge(
                id = "badge_100",
                title = "100-Day Legend",
                description = "Elite financial habit locked in",
                emoji = "👑",
                targetDays = 100,
                isUnlocked = maxStreak >= 100
            )
        )
    }
}
