package com.smartspend.app.domain.usecase.demo

import com.smartspend.app.domain.model.Account
import com.smartspend.app.domain.model.AccountType
import com.smartspend.app.domain.model.Budget
import com.smartspend.app.domain.model.BudgetType
import com.smartspend.app.domain.model.Category
import com.smartspend.app.domain.model.ContributionType
import com.smartspend.app.domain.model.Expense
import com.smartspend.app.domain.model.ExpenseSource
import com.smartspend.app.domain.model.Income
import com.smartspend.app.domain.model.IncomeSource
import com.smartspend.app.domain.model.PaymentMethod
import com.smartspend.app.domain.model.RecurringExpense
import com.smartspend.app.domain.model.RecurringFrequency
import com.smartspend.app.domain.model.SavingsGoal
import com.smartspend.app.domain.model.SavingsGoalContribution
import com.smartspend.app.domain.repository.AccountRepository
import com.smartspend.app.domain.repository.BudgetRepository
import com.smartspend.app.domain.repository.CategoryRepository
import com.smartspend.app.domain.repository.ExpenseRepository
import com.smartspend.app.domain.repository.IncomeRepository
import com.smartspend.app.domain.repository.PaymentMethodRepository
import com.smartspend.app.domain.repository.RecurringExpenseRepository
import com.smartspend.app.domain.repository.SavingsGoalRepository
import java.math.BigDecimal
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject

class SeedDemoDataUseCase @Inject constructor(
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository,
    private val paymentMethodRepository: PaymentMethodRepository,
    private val incomeRepository: IncomeRepository,
    private val expenseRepository: ExpenseRepository,
    private val budgetRepository: BudgetRepository,
    private val recurringExpenseRepository: RecurringExpenseRepository,
    private val savingsGoalRepository: SavingsGoalRepository
) {
    suspend operator fun invoke(profileId: String): Result<Int> {
        return runCatching {
            var itemsAdded = 0
            val today = LocalDate.now()
            val zone = ZoneId.systemDefault()

            // 1. Ensure starter categories and payment methods exist
            categoryRepository.addCategories(Category.starterCategories(profileId))
            paymentMethodRepository.addPaymentMethods(PaymentMethod.starterPaymentMethods(profileId))

            val catFood = "cat_food_$profileId"
            val catTransport = "cat_transport_$profileId"
            val catHousing = "cat_housing_$profileId"
            val catBills = "cat_bills_$profileId"
            val catShopping = "cat_shopping_$profileId"
            val catEntertainment = "cat_entertainment_$profileId"
            val catHealthcare = "cat_healthcare_$profileId"

            val pmUpi = "pm_upi_$profileId"
            val pmCash = "pm_cash_$profileId"
            val pmDebit = "pm_debit_$profileId"
            val pmCredit = "pm_credit_$profileId"
            val pmBank = "pm_bank_$profileId"

            // 2. Add Accounts
            val accounts = listOf(
                Account(
                    id = "acc_hdfc_$profileId",
                    profileId = profileId,
                    name = "HDFC Salary Account",
                    type = AccountType.BANK,
                    currency = "INR",
                    initialBalance = BigDecimal("55000.00"),
                    currentBalance = BigDecimal("55000.00"),
                    color = "#1D4ED8",
                    icon = "account_balance"
                ),
                Account(
                    id = "acc_sbi_$profileId",
                    profileId = profileId,
                    name = "SBI Savings Account",
                    type = AccountType.BANK,
                    currency = "INR",
                    initialBalance = BigDecimal("24500.00"),
                    currentBalance = BigDecimal("24500.00"),
                    color = "#047857",
                    icon = "account_balance"
                ),
                Account(
                    id = "acc_gpay_$profileId",
                    profileId = profileId,
                    name = "Google Pay / UPI Wallet",
                    type = AccountType.WALLET,
                    currency = "INR",
                    initialBalance = BigDecimal("6200.00"),
                    currentBalance = BigDecimal("6200.00"),
                    color = "#7C3AED",
                    icon = "account_balance_wallet"
                ),
                Account(
                    id = "acc_cash_$profileId",
                    profileId = profileId,
                    name = "Cash in Wallet",
                    type = AccountType.CASH,
                    currency = "INR",
                    initialBalance = BigDecimal("3500.00"),
                    currentBalance = BigDecimal("3500.00"),
                    color = "#D97706",
                    icon = "payments"
                )
            )
            for (acc in accounts) {
                accountRepository.createAccount(acc)
                itemsAdded++
            }

            // 3. Add Monthly Incomes for current month
            val startOfMonthMs = today.withDayOfMonth(1).atStartOfDay(zone).toInstant().toEpochMilli()
            val midMonthMs = today.withDayOfMonth(minOf(10, today.lengthOfMonth())).atStartOfDay(zone).toInstant().toEpochMilli()
            val divDateMs = today.withDayOfMonth(minOf(15, today.lengthOfMonth())).atStartOfDay(zone).toInstant().toEpochMilli()

            val incomes = listOf(
                Income(
                    id = UUID.randomUUID().toString(),
                    profileId = profileId,
                    source = IncomeSource.SALARY,
                    title = "Monthly Software Engineering Salary",
                    amount = BigDecimal("75000.00"),
                    currency = "INR",
                    date = startOfMonthMs,
                    paymentMethodId = pmBank,
                    notes = "Monthly payroll direct deposit",
                    isRecurring = true
                ),
                Income(
                    id = UUID.randomUUID().toString(),
                    profileId = profileId,
                    source = IncomeSource.FREELANCE,
                    title = "Mobile UI Consultation Project",
                    amount = BigDecimal("18500.00"),
                    currency = "INR",
                    date = midMonthMs,
                    paymentMethodId = pmUpi,
                    notes = "Client milestone payment",
                    isRecurring = false
                ),
                Income(
                    id = UUID.randomUUID().toString(),
                    profileId = profileId,
                    source = IncomeSource.INVESTMENT,
                    title = "Mutual Fund Quarterly Dividend",
                    amount = BigDecimal("2400.00"),
                    currency = "INR",
                    date = divDateMs,
                    paymentMethodId = pmBank,
                    notes = "Nifty 50 Index Fund dividend",
                    isRecurring = false
                )
            )
            for (inc in incomes) {
                incomeRepository.addIncome(inc)
                itemsAdded++
            }

            // 4. Add Monthly Budgets
            val budgets = listOf(
                Budget(
                    id = "budget_overall_$profileId",
                    profileId = profileId,
                    type = BudgetType.MONTHLY,
                    amount = BigDecimal("45000.00"),
                    thresholdPct = 80
                ),
                Budget(
                    id = "budget_food_$profileId",
                    profileId = profileId,
                    type = BudgetType.CATEGORY,
                    categoryId = catFood,
                    amount = BigDecimal("12000.00"),
                    thresholdPct = 85
                ),
                Budget(
                    id = "budget_transport_$profileId",
                    profileId = profileId,
                    type = BudgetType.CATEGORY,
                    categoryId = catTransport,
                    amount = BigDecimal("4500.00"),
                    thresholdPct = 80
                ),
                Budget(
                    id = "budget_shopping_$profileId",
                    profileId = profileId,
                    type = BudgetType.CATEGORY,
                    categoryId = catShopping,
                    amount = BigDecimal("6000.00"),
                    thresholdPct = 75
                ),
                Budget(
                    id = "budget_bills_$profileId",
                    profileId = profileId,
                    type = BudgetType.CATEGORY,
                    categoryId = catBills,
                    amount = BigDecimal("4500.00"),
                    thresholdPct = 90
                ),
                Budget(
                    id = "budget_ent_$profileId",
                    profileId = profileId,
                    type = BudgetType.CATEGORY,
                    categoryId = catEntertainment,
                    amount = BigDecimal("3000.00"),
                    thresholdPct = 80
                )
            )
            for (b in budgets) {
                budgetRepository.upsertBudget(b)
                itemsAdded++
            }

            // 5. Add Realistic Expenses across recent days
            fun dayMs(daysAgo: Long, hour: Int = 14): Long {
                return today.minusDays(daysAgo).atTime(hour, 30).atZone(zone).toInstant().toEpochMilli()
            }

            val expenses = listOf(
                Expense(
                    id = UUID.randomUUID().toString(),
                    profileId = profileId,
                    title = "Swiggy Gourmet Dinner",
                    amount = BigDecimal("480.00"),
                    currency = "INR",
                    categoryId = catFood,
                    paymentMethodId = pmUpi,
                    date = dayMs(0, 20),
                    notes = "Dinner after work",
                    source = ExpenseSource.QUICK_ADD
                ),
                Expense(
                    id = UUID.randomUUID().toString(),
                    profileId = profileId,
                    title = "D-Mart Supermarket Grocery",
                    amount = BigDecimal("3450.00"),
                    currency = "INR",
                    categoryId = catHousing,
                    paymentMethodId = pmUpi,
                    date = dayMs(1, 17),
                    notes = "Monthly household grocery shopping",
                    source = ExpenseSource.OCR
                ),
                Expense(
                    id = UUID.randomUUID().toString(),
                    profileId = profileId,
                    title = "Shell Petrol Station Fuel",
                    amount = BigDecimal("1500.00"),
                    currency = "INR",
                    categoryId = catTransport,
                    paymentMethodId = pmDebit,
                    date = dayMs(2, 9),
                    notes = "Full tank petrol",
                    source = ExpenseSource.MANUAL
                ),
                Expense(
                    id = UUID.randomUUID().toString(),
                    profileId = profileId,
                    title = "Zara Weekend Fashion Shopping",
                    amount = BigDecimal("2890.00"),
                    currency = "INR",
                    categoryId = catShopping,
                    paymentMethodId = pmCredit,
                    date = dayMs(3, 16),
                    notes = "Casual shirts & jeans",
                    source = ExpenseSource.MANUAL
                ),
                Expense(
                    id = UUID.randomUUID().toString(),
                    profileId = profileId,
                    title = "Netflix 4K Premium Plan",
                    amount = BigDecimal("649.00"),
                    currency = "INR",
                    categoryId = catEntertainment,
                    paymentMethodId = pmCredit,
                    date = dayMs(4, 11),
                    notes = "Monthly recurring streaming subscription",
                    isRecurring = true,
                    source = ExpenseSource.MANUAL
                ),
                Expense(
                    id = UUID.randomUUID().toString(),
                    profileId = profileId,
                    title = "Electricity Bill (MSEDCL)",
                    amount = BigDecimal("1850.00"),
                    currency = "INR",
                    categoryId = catBills,
                    paymentMethodId = pmBank,
                    date = dayMs(5, 10),
                    notes = "Monthly electricity consumption",
                    source = ExpenseSource.MANUAL
                ),
                Expense(
                    id = UUID.randomUUID().toString(),
                    profileId = profileId,
                    title = "Starbucks Cold Brew & Bagel",
                    amount = BigDecimal("380.00"),
                    currency = "INR",
                    categoryId = catFood,
                    paymentMethodId = pmUpi,
                    date = dayMs(6, 15),
                    notes = "Coffee with team",
                    source = ExpenseSource.VOICE
                ),
                Expense(
                    id = UUID.randomUUID().toString(),
                    profileId = profileId,
                    title = "Zepto 10-Min Fast Groceries",
                    amount = BigDecimal("320.00"),
                    currency = "INR",
                    categoryId = catFood,
                    paymentMethodId = pmUpi,
                    date = dayMs(7, 19),
                    notes = "Fruits, curd & snacks",
                    source = ExpenseSource.QUICK_ADD
                ),
                Expense(
                    id = UUID.randomUUID().toString(),
                    profileId = profileId,
                    title = "Zomato Weekend Dinner Outing",
                    amount = BigDecimal("1450.00"),
                    currency = "INR",
                    categoryId = catFood,
                    paymentMethodId = pmUpi,
                    date = dayMs(8, 21),
                    notes = "Dinner with college friends",
                    source = ExpenseSource.MANUAL
                ),
                Expense(
                    id = UUID.randomUUID().toString(),
                    profileId = profileId,
                    title = "Amazon Electronics 65W GaN Charger",
                    amount = BigDecimal("1299.00"),
                    currency = "INR",
                    categoryId = catShopping,
                    paymentMethodId = pmCredit,
                    date = dayMs(9, 13),
                    notes = "Type-C fast charger for laptop & phone",
                    source = ExpenseSource.MANUAL
                ),
                Expense(
                    id = UUID.randomUUID().toString(),
                    profileId = profileId,
                    title = "Blinkit Daily Dairy & Veggies",
                    amount = BigDecimal("285.00"),
                    currency = "INR",
                    categoryId = catFood,
                    paymentMethodId = pmUpi,
                    date = dayMs(10, 8),
                    notes = "Fresh vegetables and milk",
                    source = ExpenseSource.QUICK_ADD
                ),
                Expense(
                    id = UUID.randomUUID().toString(),
                    profileId = profileId,
                    title = "Uber Premier Ride to Tech Park",
                    amount = BigDecimal("320.00"),
                    currency = "INR",
                    categoryId = catTransport,
                    paymentMethodId = pmUpi,
                    date = dayMs(11, 9),
                    notes = "Morning office commute",
                    source = ExpenseSource.VOICE
                ),
                Expense(
                    id = UUID.randomUUID().toString(),
                    profileId = profileId,
                    title = "JioFiber Broadband Ultra",
                    amount = BigDecimal("999.00"),
                    currency = "INR",
                    categoryId = catBills,
                    paymentMethodId = pmBank,
                    date = dayMs(12, 12),
                    notes = "300 Mbps unlimited fiber connection",
                    isRecurring = true,
                    source = ExpenseSource.MANUAL
                ),
                Expense(
                    id = UUID.randomUUID().toString(),
                    profileId = profileId,
                    title = "Cult.fit Monthly Gym Pass",
                    amount = BigDecimal("1500.00"),
                    currency = "INR",
                    categoryId = catHealthcare,
                    paymentMethodId = pmUpi,
                    date = dayMs(14, 7),
                    notes = "Fitness and gym membership",
                    isRecurring = true,
                    source = ExpenseSource.MANUAL
                ),
                Expense(
                    id = UUID.randomUUID().toString(),
                    profileId = profileId,
                    title = "Apollo Pharmacy Health Vitamins",
                    amount = BigDecimal("640.00"),
                    currency = "INR",
                    categoryId = catHealthcare,
                    paymentMethodId = pmDebit,
                    date = dayMs(16, 18),
                    notes = "Multivitamins and first aid",
                    source = ExpenseSource.OCR
                ),
                Expense(
                    id = UUID.randomUUID().toString(),
                    profileId = profileId,
                    title = "Spotify Family Plan",
                    amount = BigDecimal("179.00"),
                    currency = "INR",
                    categoryId = catEntertainment,
                    paymentMethodId = pmCredit,
                    date = dayMs(18, 14),
                    notes = "Music streaming plan",
                    isRecurring = true,
                    source = ExpenseSource.MANUAL
                ),
                Expense(
                    id = UUID.randomUUID().toString(),
                    profileId = profileId,
                    title = "BookMyShow IMAX Movie Tickets",
                    amount = BigDecimal("650.00"),
                    currency = "INR",
                    categoryId = catEntertainment,
                    paymentMethodId = pmUpi,
                    date = dayMs(22, 19),
                    notes = "Weekend movie with popcorn",
                    source = ExpenseSource.MANUAL
                ),
                Expense(
                    id = UUID.randomUUID().toString(),
                    profileId = profileId,
                    title = "Auto Rickshaw Metro Connect",
                    amount = BigDecimal("140.00"),
                    currency = "INR",
                    categoryId = catTransport,
                    paymentMethodId = pmCash,
                    date = dayMs(24, 18),
                    notes = "Metro station drop",
                    source = ExpenseSource.QUICK_ADD
                )
            )
            for (exp in expenses) {
                expenseRepository.addExpense(exp)
                itemsAdded++
            }

            // 6. Add Subscriptions
            val subscriptions = listOf(
                RecurringExpense(
                    id = UUID.randomUUID().toString(),
                    profileId = profileId,
                    title = "Netflix 4K Premium",
                    amount = BigDecimal("649.00"),
                    currency = "INR",
                    categoryId = catEntertainment,
                    paymentMethodId = pmCredit,
                    frequency = RecurringFrequency.MONTHLY,
                    startDate = dayMs(30),
                    nextDueDate = today.plusDays(10).atStartOfDay(zone).toInstant().toEpochMilli(),
                    isActive = true,
                    autoCreate = true,
                    notifyBeforeDays = 2,
                    notes = "Ultra HD streaming tier"
                ),
                RecurringExpense(
                    id = UUID.randomUUID().toString(),
                    profileId = profileId,
                    title = "Spotify Family Plan",
                    amount = BigDecimal("179.00"),
                    currency = "INR",
                    categoryId = catEntertainment,
                    paymentMethodId = pmCredit,
                    frequency = RecurringFrequency.MONTHLY,
                    startDate = dayMs(30),
                    nextDueDate = today.plusDays(14).atStartOfDay(zone).toInstant().toEpochMilli(),
                    isActive = true,
                    autoCreate = true,
                    notifyBeforeDays = 1,
                    notes = "Hi-Fi audio music"
                ),
                RecurringExpense(
                    id = UUID.randomUUID().toString(),
                    profileId = profileId,
                    title = "JioFiber Broadband Unlimited",
                    amount = BigDecimal("999.00"),
                    currency = "INR",
                    categoryId = catBills,
                    paymentMethodId = pmBank,
                    frequency = RecurringFrequency.MONTHLY,
                    startDate = dayMs(30),
                    nextDueDate = today.plusDays(18).atStartOfDay(zone).toInstant().toEpochMilli(),
                    isActive = true,
                    autoCreate = true,
                    notifyBeforeDays = 3,
                    notes = "300 Mbps unlimited fiber"
                ),
                RecurringExpense(
                    id = UUID.randomUUID().toString(),
                    profileId = profileId,
                    title = "Cult.fit Gym & Fitness Pass",
                    amount = BigDecimal("1500.00"),
                    currency = "INR",
                    categoryId = catHealthcare,
                    paymentMethodId = pmUpi,
                    frequency = RecurringFrequency.MONTHLY,
                    startDate = dayMs(30),
                    nextDueDate = today.plusDays(20).atStartOfDay(zone).toInstant().toEpochMilli(),
                    isActive = true,
                    autoCreate = true,
                    notifyBeforeDays = 2,
                    notes = "All-center gym access"
                ),
                RecurringExpense(
                    id = UUID.randomUUID().toString(),
                    profileId = profileId,
                    title = "Amazon Prime Membership",
                    amount = BigDecimal("1499.00"),
                    currency = "INR",
                    categoryId = catShopping,
                    paymentMethodId = pmCredit,
                    frequency = RecurringFrequency.YEARLY,
                    startDate = dayMs(100),
                    nextDueDate = today.plusMonths(8).atStartOfDay(zone).toInstant().toEpochMilli(),
                    isActive = true,
                    autoCreate = true,
                    notifyBeforeDays = 7,
                    notes = "Annual Prime delivery & Video"
                )
            )
            for (sub in subscriptions) {
                recurringExpenseRepository.createRecurringExpense(sub)
                itemsAdded++
            }

            // 7. Add Savings Goals & Contributions
            val goal1Id = UUID.randomUUID().toString()
            val goal2Id = UUID.randomUUID().toString()
            val goal3Id = UUID.randomUUID().toString()

            val goals = listOf(
                SavingsGoal(
                    id = goal1Id,
                    profileId = profileId,
                    name = "Emergency Fund (6 Months)",
                    targetAmount = BigDecimal("150000.00"),
                    currentAmount = BigDecimal("65000.00"),
                    currency = "INR",
                    targetDate = today.plusMonths(6).atStartOfDay(zone).toInstant().toEpochMilli(),
                    color = "#047857",
                    icon = "security"
                ),
                SavingsGoal(
                    id = goal2Id,
                    profileId = profileId,
                    name = "Goa Year-End Friends Trip",
                    targetAmount = BigDecimal("25000.00"),
                    currentAmount = BigDecimal("18000.00"),
                    currency = "INR",
                    targetDate = today.plusMonths(2).atStartOfDay(zone).toInstant().toEpochMilli(),
                    color = "#D97706",
                    icon = "flight"
                ),
                SavingsGoal(
                    id = goal3Id,
                    profileId = profileId,
                    name = "Apple MacBook M3 Pro",
                    targetAmount = BigDecimal("140000.00"),
                    currentAmount = BigDecimal("45000.00"),
                    currency = "INR",
                    targetDate = today.plusMonths(5).atStartOfDay(zone).toInstant().toEpochMilli(),
                    color = "#7C3AED",
                    icon = "laptop_mac"
                )
            )
            for (g in goals) {
                savingsGoalRepository.createSavingsGoal(g)
                itemsAdded++
            }

            // Contributions
            val contributions = listOf(
                SavingsGoalContribution(
                    id = UUID.randomUUID().toString(),
                    goalId = goal1Id,
                    profileId = profileId,
                    amount = BigDecimal("35000.00"),
                    type = ContributionType.DEPOSIT,
                    date = dayMs(25),
                    notes = "Initial emergency cushion deposit"
                ),
                SavingsGoalContribution(
                    id = UUID.randomUUID().toString(),
                    goalId = goal1Id,
                    profileId = profileId,
                    amount = BigDecimal("30000.00"),
                    type = ContributionType.DEPOSIT,
                    date = dayMs(5),
                    notes = "Monthly salary savings allocation"
                ),
                SavingsGoalContribution(
                    id = UUID.randomUUID().toString(),
                    goalId = goal2Id,
                    profileId = profileId,
                    amount = BigDecimal("10000.00"),
                    type = ContributionType.DEPOSIT,
                    date = dayMs(20),
                    notes = "Initial flight ticket booking fund"
                ),
                SavingsGoalContribution(
                    id = UUID.randomUUID().toString(),
                    goalId = goal2Id,
                    profileId = profileId,
                    amount = BigDecimal("8000.00"),
                    type = ContributionType.DEPOSIT,
                    date = dayMs(2),
                    notes = "Hotel booking contribution"
                ),
                SavingsGoalContribution(
                    id = UUID.randomUUID().toString(),
                    goalId = goal3Id,
                    profileId = profileId,
                    amount = BigDecimal("45000.00"),
                    type = ContributionType.DEPOSIT,
                    date = dayMs(10),
                    notes = "Freelance earnings transferred to laptop fund"
                )
            )
            for (c in contributions) {
                savingsGoalRepository.addContribution(c)
                itemsAdded++
            }

            itemsAdded
        }
    }
}
