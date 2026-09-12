package com.smartspend.app.feature.settings

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.smartspend.app.core.money.MoneyUtils
import com.smartspend.app.domain.model.Category
import com.smartspend.app.domain.model.Expense
import com.smartspend.app.domain.model.Income
import com.smartspend.app.domain.model.PaymentMethod
import com.smartspend.app.domain.model.Profile
import java.io.File
import java.io.FileOutputStream
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfExporter {

    fun exportMonthlyStatement(
        context: Context,
        profile: Profile?,
        monthTitle: String,
        expenses: List<Expense>,
        incomes: List<Income>,
        categoriesMap: Map<String, Category>,
        paymentMethodsMap: Map<String, PaymentMethod>,
        currency: String
    ): Result<File> {
        return runCatching {
            val pdfDocument = PdfDocument()
            val pageWidth = 595 // A4 standard width (pt)
            val pageHeight = 842 // A4 standard height (pt)

            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

            // Prepare unified item list sorted by date descending
            data class StatementItem(
                val date: Long,
                val type: String,
                val categoryOrSource: String,
                val title: String,
                val notes: String?,
                val mode: String,
                val amount: BigDecimal,
                val isExpense: Boolean
            )

            val items = mutableListOf<StatementItem>()
            expenses.forEach { exp ->
                items.add(
                    StatementItem(
                        date = exp.date,
                        type = "EXPENSE",
                        categoryOrSource = categoriesMap[exp.categoryId]?.name ?: "Uncategorized",
                        title = exp.title,
                        notes = exp.notes,
                        mode = paymentMethodsMap[exp.paymentMethodId]?.label ?: "Cash",
                        amount = exp.amount,
                        isExpense = true
                    )
                )
            }
            incomes.forEach { inc ->
                items.add(
                    StatementItem(
                        date = inc.date,
                        type = "INCOME",
                        categoryOrSource = inc.source.displayName,
                        title = inc.title,
                        notes = inc.notes,
                        mode = inc.paymentMethodId?.let { paymentMethodsMap[it]?.label } ?: "Bank / UPI",
                        amount = inc.amount,
                        isExpense = false
                    )
                )
            }
            items.sortByDescending { it.date }

            val totalExpense = expenses.fold(BigDecimal.ZERO) { acc, e -> acc.add(e.amount) }
            val totalIncome = incomes.fold(BigDecimal.ZERO) { acc, i -> acc.add(i.amount) }
            val netSavings = totalIncome.subtract(totalExpense)

            var pageNumber = 1
            var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            var page = pdfDocument.startPage(pageInfo)
            var canvas = page.canvas

            var y = 40f

            // 1. Header
            paint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            paint.textSize = 20f
            paint.color = Color.rgb(30, 35, 42) // AtelierPrimaryInk
            canvas.drawText("SmartSpend", 36f, y, paint)

            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            paint.textSize = 10f
            paint.color = Color.rgb(104, 110, 120) // AtelierInkMuted
            canvas.drawText("PRIVATE DIGITAL FINANCIAL LEDGER", 160f, y - 2, paint)

            y += 24f
            paint.textSize = 14f
            paint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            paint.color = Color.rgb(30, 35, 42)
            canvas.drawText("Monthly Financial Statement — $monthTitle", 36f, y, paint)

            y += 16f
            paint.textSize = 9f
            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            val profileName = profile?.name ?: "Primary Account"
            val generatedDate = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
            canvas.drawText("Account Holder: $profileName  |  Generated: $generatedDate  |  Security: AES-256 On-Device", 36f, y, paint)

            y += 12f
            paint.color = Color.rgb(226, 224, 216) // AtelierHairline
            paint.strokeWidth = 1f
            canvas.drawLine(36f, y, (pageWidth - 36).toFloat(), y, paint)

            // 2. Summary Box
            y += 14f
            paint.color = Color.rgb(244, 243, 239) // AtelierSurfaceChalk
            canvas.drawRect(36f, y, (pageWidth - 36).toFloat(), y + 42f, paint)

            paint.color = Color.rgb(30, 35, 42)
            paint.textSize = 8.5f
            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            canvas.drawText("TOTAL DEBITS (SPENT)", 48f, y + 15f, paint)
            canvas.drawText("TOTAL CREDITS (INCOME)", 210f, y + 15f, paint)
            canvas.drawText("NET SAVINGS BALANCE", 380f, y + 15f, paint)

            paint.textSize = 12f
            paint.color = Color.rgb(220, 60, 60) // Coral
            canvas.drawText(MoneyUtils.format(totalExpense, currency), 48f, y + 33f, paint)

            paint.color = Color.rgb(46, 125, 50) // Sage
            canvas.drawText("+" + MoneyUtils.format(totalIncome, currency), 210f, y + 33f, paint)

            paint.color = if (netSavings >= BigDecimal.ZERO) Color.rgb(46, 125, 50) else Color.rgb(220, 60, 60)
            canvas.drawText(MoneyUtils.format(netSavings, currency), 380f, y + 33f, paint)

            y += 56f

            // 3. Table Header
            paint.color = Color.rgb(30, 35, 42)
            canvas.drawRect(36f, y, (pageWidth - 36).toFloat(), y + 20f, paint)

            paint.color = Color.WHITE
            paint.textSize = 8.5f
            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)

            canvas.drawText("DATE", 42f, y + 13f, paint)
            canvas.drawText("TYPE", 95f, y + 13f, paint)
            canvas.drawText("CATEGORY / SOURCE", 145f, y + 13f, paint)
            canvas.drawText("MEMO / DESCRIPTION", 260f, y + 13f, paint)
            canvas.drawText("MODE", 420f, y + 13f, paint)
            canvas.drawText("AMOUNT", 500f, y + 13f, paint)

            y += 24f

            if (items.isEmpty()) {
                paint.color = Color.rgb(104, 110, 120)
                paint.textSize = 10f
                paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.ITALIC)
                canvas.drawText("No vouchers or transactions recorded for this billing cycle.", 42f, y + 18f, paint)
            } else {
                for ((index, item) in items.withIndex()) {
                    if (y > pageHeight - 50) {
                        // Draw footer on current page
                        paint.color = Color.rgb(150, 150, 150)
                        paint.textSize = 8f
                        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                        canvas.drawText("Page $pageNumber  •  SmartSpend Encrypted Ledger Statement", 36f, pageHeight - 20f, paint)

                        pdfDocument.finishPage(page)
                        pageNumber++
                        pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                        page = pdfDocument.startPage(pageInfo)
                        canvas = page.canvas
                        y = 40f

                        // Table header on subsequent pages
                        paint.color = Color.rgb(30, 35, 42)
                        canvas.drawRect(36f, y, (pageWidth - 36).toFloat(), y + 18f, paint)
                        paint.color = Color.WHITE
                        paint.textSize = 8f
                        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                        canvas.drawText("DATE", 42f, y + 12f, paint)
                        canvas.drawText("TYPE", 95f, y + 12f, paint)
                        canvas.drawText("CATEGORY / SOURCE", 145f, y + 12f, paint)
                        canvas.drawText("MEMO / DESCRIPTION", 260f, y + 12f, paint)
                        canvas.drawText("MODE", 420f, y + 12f, paint)
                        canvas.drawText("AMOUNT", 500f, y + 12f, paint)
                        y += 22f
                    }

                    // Row background
                    if (index % 2 == 1) {
                        paint.color = Color.rgb(250, 249, 246)
                        canvas.drawRect(36f, y - 4f, (pageWidth - 36).toFloat(), y + 16f, paint)
                    }

                    paint.textSize = 8.5f
                    paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                    paint.color = Color.rgb(60, 65, 75)

                    val dateStr = dateFormat.format(Date(item.date))
                    canvas.drawText(dateStr, 42f, y + 9f, paint)

                    // Type
                    paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                    paint.color = if (item.isExpense) Color.rgb(180, 50, 50) else Color.rgb(40, 130, 60)
                    canvas.drawText(if (item.isExpense) "DR" else "CR", 95f, y + 9f, paint)

                    paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                    paint.color = Color.rgb(30, 35, 42)
                    val catText = if (item.categoryOrSource.length > 20) item.categoryOrSource.take(18) + "…" else item.categoryOrSource
                    canvas.drawText(catText, 145f, y + 9f, paint)

                    val titleText = if (item.title.length > 28) item.title.take(26) + "…" else item.title
                    canvas.drawText(titleText, 260f, y + 9f, paint)

                    val modeText = if (item.mode.length > 12) item.mode.take(10) + "…" else item.mode
                    paint.color = Color.rgb(100, 105, 115)
                    canvas.drawText(modeText, 420f, y + 9f, paint)

                    // Amount
                    paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                    paint.color = if (item.isExpense) Color.rgb(180, 50, 50) else Color.rgb(40, 130, 60)
                    val prefix = if (item.isExpense) "-" else "+"
                    val amtStr = prefix + MoneyUtils.format(item.amount, currency)
                    canvas.drawText(amtStr, 500f, y + 9f, paint)

                    y += 18f
                }
            }

            // Draw final page footer
            paint.color = Color.rgb(150, 150, 150)
            paint.textSize = 8f
            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            canvas.drawText("Page $pageNumber  •  SmartSpend Encrypted Ledger Statement  •  Generated On-Device", 36f, pageHeight - 20f, paint)

            pdfDocument.finishPage(page)

            // Save PDF
            val cleanMonth = monthTitle.replace(" ", "_").replace("/", "-")
            val outputFile = File(context.cacheDir, "smartspend_statement_${cleanMonth}.pdf")
            val outputStream = FileOutputStream(outputFile)
            pdfDocument.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            pdfDocument.close()

            outputFile
        }
    }

    fun sharePdfFile(context: Context, file: File, title: String) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(sendIntent, "Share Monthly Statement PDF")
        context.startActivity(chooser)
    }
}
