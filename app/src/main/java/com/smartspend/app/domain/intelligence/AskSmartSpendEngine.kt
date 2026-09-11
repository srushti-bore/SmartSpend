package com.smartspend.app.domain.intelligence

import com.smartspend.app.domain.repository.AccountRepository
import com.smartspend.app.domain.repository.BudgetRepository
import com.smartspend.app.domain.repository.ExpenseRepository
import com.smartspend.app.domain.repository.IncomeRepository
import com.smartspend.app.domain.repository.RecurringExpenseRepository
import kotlinx.coroutines.flow.first
import java.math.BigDecimal
import java.util.Calendar
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

data class AiChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val isUser: Boolean,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val quickReplies: List<String> = emptyList()
)

@Singleton
class AskSmartSpendEngine @Inject constructor(
    private val safeToSpendEngine: SafeToSpendEngine,
    private val healthScoreUseCase: FinancialHealthScoreUseCase,
    private val leakHunterUseCase: LeakHunterUseCase,
    private val purchaseSimulator: PurchaseSimulatorUseCase,
    private val forecasterUseCase: SpendForecasterUseCase,
    private val expenseRepository: ExpenseRepository,
    private val incomeRepository: IncomeRepository,
    private val recurringRepository: RecurringExpenseRepository,
    private val budgetRepository: BudgetRepository
) {

    suspend fun answerQuestion(profileId: String, query: String): AiChatMessage {
        val q = query.trim().lowercase()

        // 1. Check for purchase simulation intent (e.g., "can i afford 2000", "can i buy phone for 15000", "1500 kharch karu ka")
        val amountRegex = Regex("""(?:(?:₹|Rs\.?|INR|\$)\s*)?([0-9]+(?:,[0-9]{2,3})*(?:\.[0-9]{1,2})?)""")
        if (q.contains("afford") || q.contains("buy") || q.contains("purchase") || q.contains("karu ka") || q.contains("kharidu") || q.contains("le sakta")) {
            val match = amountRegex.find(q)
            if (match != null) {
                val num = match.groups[1]?.value?.replace(",", "")
                try {
                    val amount = BigDecimal(num)
                    val simResult = purchaseSimulator(profileId, "Simulated Purchase", amount)
                    val response = "📊 **Purchase Simulation Result**\n\n" +
                            "**Decision:** ${simResult.verdictTitle}\n\n" +
                            "${simResult.explanation}\n\n" +
                            "• Current Safe Daily: ₹${simResult.currentSafeDaily}\n" +
                            "• New Safe Daily: ₹${simResult.newSafeDaily}\n" +
                            "• New Remaining Budget: ₹${simResult.newRemainingBudget}"
                    return AiChatMessage(isUser = false, text = response, quickReplies = listOf("Safe-to-Spend", "Health Score", "Top Categories"))
                } catch (_: Exception) {}
            }
        }

        // 2. Safe to Spend Intent
        if (q.contains("safe") || q.contains("run rate") || q.contains("daily limit") || q.contains("kiti kharch karu") || q.contains("kitna bacha")) {
            val safe = safeToSpendEngine.calculate(profileId)
            val response = "🛡️ **Safe-to-Spend Analysis**\n\n" +
                    "**${safe.headlineMessage}**\n\n" +
                    "• **Daily Allowance:** ₹${safe.safeDailySpend}/day\n" +
                    "• **Weekly Allowance:** ₹${safe.safeWeeklySpend}/week\n" +
                    "• **Remaining Budget:** ₹${safe.remainingBudget} (${safe.percentageUsed}% spent)\n" +
                    "• **Upcoming Fixed Bills:** ₹${safe.upcomingRecurringCommitments}\n" +
                    "• **Days Left in Month:** ${safe.daysRemainingInMonth} days\n\n" +
                    "💡 *${safe.pacingAdvice}*"
            return AiChatMessage(isUser = false, text = response, quickReplies = listOf("Check Leaks", "Month Forecast", "Can I afford ₹1000?"))
        }

        // 3. Health Score Intent
        if (q.contains("health") || q.contains("score") || q.contains("rating") || q.contains("aarogya") || q.contains("kasa aahe")) {
            val health = healthScoreUseCase(profileId)
            val pillarsText = health.pillars.joinToString("\n") { "• **${it.name}:** ${it.score}/20 (${it.status})" }
            val actionsText = health.actionRecommendations.joinToString("\n") { "👉 $it" }
            val response = "🏆 **Financial Health Score: ${health.overallScore}/100 (Grade ${health.grade})**\n\n" +
                    "**${health.summaryTitle}**\n\n" +
                    "**Pillar Breakdown:**\n$pillarsText\n\n" +
                    "**Action Steps:**\n$actionsText"
            return AiChatMessage(isUser = false, text = response, quickReplies = listOf("Safe-to-Spend", "Leak Hunter", "Forecast"))
        }

        // 4. Leak Hunter / Subscriptions Intent
        if (q.contains("leak") || q.contains("subscription") || q.contains("recurring") || q.contains("micro") || q.contains("fokat") || q.contains("bills")) {
            val leaks = leakHunterUseCase(profileId)
            val leakItemsText = if (leaks.leaksFound.isNotEmpty()) {
                leaks.leaksFound.joinToString("\n\n") { "• **${it.title}** (₹${it.totalAmount}): ${it.explanation}\n  *Action:* ${it.suggestedAction}" }
            } else "✨ Zero active leak patterns identified."
            val response = "🔍 **Leak Hunter Report**\n\n" +
                    "${leaks.headline}\n\n" +
                    "$leakItemsText"
            return AiChatMessage(isUser = false, text = response, quickReplies = listOf("Health Score", "Safe-to-Spend", "Income Summary"))
        }

        // 5. Forecast & 50/30/20 Intent
        if (q.contains("forecast") || q.contains("50/30/20") || q.contains("future") || q.contains("burn") || q.contains("pudhe") || q.contains("month end")) {
            val forecast = forecasterUseCase(profileId)
            val split = forecast.rule503020
            val response = "📈 **Month-End Spend Forecast**\n\n" +
                    "• **Daily Burn-Rate:** ₹${forecast.dailyBurnRate}/day\n" +
                    "• **Projected Month-End Spend:** ₹${forecast.projectedMonthEndSpend} (Budget: ₹${forecast.budgetAmount})\n\n" +
                    "**50/30/20 Split Analysis:**\n" +
                    "• Needs (Target 50%): ${String.format("%.1f", split.needsPct)}% (₹${split.needsAmount})\n" +
                    "• Wants (Target 30%): ${String.format("%.1f", split.wantsPct)}% (₹${split.wantsAmount})\n" +
                    "• Savings (Target 20%): ${String.format("%.1f", split.savingsPct)}% (₹${split.savingsAmount})\n\n" +
                    "💬 ${forecast.summaryMessage}"
            return AiChatMessage(isUser = false, text = response, quickReplies = listOf("Safe-to-Spend", "Check Leaks", "Top Categories"))
        }

        // 6. Total Spends / Category Spends
        if (q.contains("spend") || q.contains("kharch") || q.contains("kharcha") || q.contains("category") || q.contains("top") || q.contains("kiti")) {
            val calendar = Calendar.getInstance()
            val startCal = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val endCal = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }
            val total = expenseRepository.getTotalSpending(profileId, startCal.timeInMillis, endCal.timeInMillis).first()
            val categories = expenseRepository.getCategorySpending(profileId, startCal.timeInMillis, endCal.timeInMillis).first()
            val topCats = categories.take(3).joinToString("\n") { "• ${it.categoryName}: ₹${it.totalAmount}" }

            val response = "💳 **Monthly Spending Summary**\n\n" +
                    "• **Total Spent this Month:** ₹$total\n\n" +
                    "**Top Categories:**\n${if (topCats.isNotBlank()) topCats else "No category spends recorded yet."}"
            return AiChatMessage(isUser = false, text = response, quickReplies = listOf("Safe-to-Spend", "Health Score", "Forecast"))
        }

        // Default Grounded Help Response
        val helpText = "🤖 **Hello! I am your SmartSpend Financial AI.**\n\n" +
                "I am grounded 100% in your encrypted local financial ledger with zero hallucinations.\n\n" +
                "You can ask me:\n" +
                "• *\"How much can I safely spend today?\"*\n" +
                "• *\"Can I afford a smartwatch for ₹3,500?\"*\n" +
                "• *\"What is my financial health score?\"*\n" +
                "• *\"Find my spending leaks and subscriptions\"*\n" +
                "• *\"What is my 50/30/20 breakdown?\"*\n" +
                "• *\"Maza ya mahinyat kiti kharch jhala?\"* (मराठी)\n" +
                "• *\"Is mahine mera kitna kharcha hua?\"* (हिंदी)"
        return AiChatMessage(isUser = false, text = helpText, quickReplies = listOf("Safe-to-Spend", "Financial Health", "Leak Hunter", "Can I afford ₹1500?"))
    }
}
