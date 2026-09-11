package com.smartspend.app.domain.usecase.split

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

enum class SplitMode {
    EQUAL,
    PERCENTAGE,
    EXACT_AMOUNT
}

data class SplitParticipant(
    val id: String,
    val name: String,
    val shareAmount: BigDecimal,
    val percentage: Double? = null,
    val isMe: Boolean = false,
    val hasSettled: Boolean = false
)

data class SplitResult(
    val totalAmount: BigDecimal,
    val myShare: BigDecimal,
    val othersShareTotal: BigDecimal,
    val participants: List<SplitParticipant>,
    val shareableText: String
)

@Singleton
class SplitExpenseUseCase @Inject constructor() {

    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    fun calculateEqualSplit(
        totalAmount: BigDecimal,
        participantNames: List<String>,
        expenseTitle: String = "Group Expense",
        upiId: String? = null
    ): SplitResult {
        if (participantNames.isEmpty() || totalAmount <= BigDecimal.ZERO) {
            return SplitResult(
                totalAmount = totalAmount,
                myShare = totalAmount,
                othersShareTotal = BigDecimal.ZERO,
                participants = emptyList(),
                shareableText = ""
            )
        }

        val count = participantNames.size
        val countBd = BigDecimal(count)
        val baseShare = totalAmount.divide(countBd, 2, RoundingMode.DOWN)
        val remainder = totalAmount.subtract(baseShare.multiply(countBd))

        // Give any cent remainder to the first person (usually 'You')
        val participants = participantNames.mapIndexed { index, name ->
            val isMe = index == 0 || name.equals("You", ignoreCase = true) || name.equals("Me", ignoreCase = true)
            val share = if (index == 0) baseShare.add(remainder) else baseShare
            val pct = share.divide(totalAmount, 4, RoundingMode.HALF_EVEN).multiply(BigDecimal(100)).toDouble()
            SplitParticipant(
                id = "part_${index + 1}",
                name = name,
                shareAmount = share,
                percentage = pct,
                isMe = isMe
            )
        }

        val myParticipant = participants.firstOrNull { it.isMe } ?: participants.first()
        val myShare = myParticipant.shareAmount
        val othersTotal = totalAmount.subtract(myShare)

        val shareText = buildShareableSummary(expenseTitle, totalAmount, participants, upiId)

        return SplitResult(
            totalAmount = totalAmount,
            myShare = myShare,
            othersShareTotal = othersTotal,
            participants = participants,
            shareableText = shareText
        )
    }

    fun calculatePercentageSplit(
        totalAmount: BigDecimal,
        participantPercentages: List<Pair<String, Double>>,
        expenseTitle: String = "Group Expense",
        upiId: String? = null
    ): SplitResult {
        if (participantPercentages.isEmpty() || totalAmount <= BigDecimal.ZERO) {
            return SplitResult(
                totalAmount = totalAmount,
                myShare = totalAmount,
                othersShareTotal = BigDecimal.ZERO,
                participants = emptyList(),
                shareableText = ""
            )
        }

        val participants = participantPercentages.mapIndexed { index, (name, pct) ->
            val isMe = index == 0 || name.equals("You", ignoreCase = true) || name.equals("Me", ignoreCase = true)
            val share = totalAmount.multiply(BigDecimal(pct))
                .divide(BigDecimal(100), 2, RoundingMode.HALF_EVEN)
            SplitParticipant(
                id = "part_${index + 1}",
                name = name,
                shareAmount = share,
                percentage = pct,
                isMe = isMe
            )
        }

        val myParticipant = participants.firstOrNull { it.isMe } ?: participants.first()
        val myShare = myParticipant.shareAmount
        val othersTotal = participants.filter { !it.isMe }.fold(BigDecimal.ZERO) { acc, p -> acc.add(p.shareAmount) }

        val shareText = buildShareableSummary(expenseTitle, totalAmount, participants, upiId)

        return SplitResult(
            totalAmount = totalAmount,
            myShare = myShare,
            othersShareTotal = othersTotal,
            participants = participants,
            shareableText = shareText
        )
    }

    fun calculateExactSplit(
        totalAmount: BigDecimal,
        participantAmounts: List<Pair<String, BigDecimal>>,
        expenseTitle: String = "Group Expense",
        upiId: String? = null
    ): SplitResult {
        if (participantAmounts.isEmpty() || totalAmount <= BigDecimal.ZERO) {
            return SplitResult(
                totalAmount = totalAmount,
                myShare = totalAmount,
                othersShareTotal = BigDecimal.ZERO,
                participants = emptyList(),
                shareableText = ""
            )
        }

        val participants = participantAmounts.mapIndexed { index, (name, share) ->
            val isMe = index == 0 || name.equals("You", ignoreCase = true) || name.equals("Me", ignoreCase = true)
            val pct = if (totalAmount > BigDecimal.ZERO) {
                share.divide(totalAmount, 4, RoundingMode.HALF_EVEN).multiply(BigDecimal(100)).toDouble()
            } else 0.0

            SplitParticipant(
                id = "part_${index + 1}",
                name = name,
                shareAmount = share,
                percentage = pct,
                isMe = isMe
            )
        }

        val myParticipant = participants.firstOrNull { it.isMe } ?: participants.first()
        val myShare = myParticipant.shareAmount
        val othersTotal = participants.filter { !it.isMe }.fold(BigDecimal.ZERO) { acc, p -> acc.add(p.shareAmount) }

        val shareText = buildShareableSummary(expenseTitle, totalAmount, participants, upiId)

        return SplitResult(
            totalAmount = totalAmount,
            myShare = myShare,
            othersShareTotal = othersTotal,
            participants = participants,
            shareableText = shareText
        )
    }

    private fun buildShareableSummary(
        title: String,
        totalAmount: BigDecimal,
        participants: List<SplitParticipant>,
        upiId: String?
    ): String {
        val sb = StringBuilder()
        sb.append("🧾 *SmartSpend Expense Split*\n")
        sb.append("📌 *Item:* $title\n")
        sb.append("💰 *Total:* ${currencyFormatter.format(totalAmount)}\n\n")
        sb.append("👥 *Breakdown:*\n")

        participants.forEach { p ->
            val tag = if (p.isMe) " (Paid by You)" else ""
            sb.append("• ${p.name}$tag: ${currencyFormatter.format(p.shareAmount)}\n")
        }

        if (!upiId.isNullOrBlank()) {
            sb.append("\n💳 *Pay via UPI:* `$upiId`\n")
            sb.append("⚡ Link: upi://pay?pa=$upiId&pn=SmartSpend&am=&cu=INR\n")
        }

        sb.append("\n_Calculated accurately via SmartSpend_ 🚀")
        return sb.toString()
    }
}
