package com.smartspend.app.data.mapper

import com.smartspend.app.data.local.entity.BudgetEntity
import com.smartspend.app.data.local.entity.CategoryEntity
import com.smartspend.app.data.local.entity.ExpenseEntity
import com.smartspend.app.data.local.entity.PaymentMethodEntity
import com.smartspend.app.data.local.entity.ProfileEntity
import com.smartspend.app.domain.model.Budget
import com.smartspend.app.domain.model.Category
import com.smartspend.app.domain.model.Expense
import com.smartspend.app.domain.model.PaymentMethod
import com.smartspend.app.domain.model.Profile

fun ProfileEntity.toDomain(): Profile = Profile(
    id = id,
    name = name,
    primaryAuthType = primaryAuthType,
    credentialSalt = credentialSalt,
    credentialHash = credentialHash,
    biometricEnabled = biometricEnabled,
    createdAt = createdAt
)

fun Profile.toEntity(): ProfileEntity = ProfileEntity(
    id = id,
    name = name,
    primaryAuthType = primaryAuthType,
    credentialSalt = credentialSalt,
    credentialHash = credentialHash,
    biometricEnabled = biometricEnabled,
    createdAt = createdAt
)

fun CategoryEntity.toDomain(): Category = Category(
    id = id,
    profileId = profileId,
    name = name,
    iconName = iconName,
    colorHex = colorHex,
    isCustom = isCustom,
    parentDefaultId = parentDefaultId
)

fun Category.toEntity(): CategoryEntity = CategoryEntity(
    id = id,
    profileId = profileId,
    name = name,
    iconName = iconName,
    colorHex = colorHex,
    isCustom = isCustom,
    parentDefaultId = parentDefaultId
)

fun PaymentMethodEntity.toDomain(): PaymentMethod = PaymentMethod(
    id = id,
    profileId = profileId,
    type = type,
    label = label,
    isCustom = isCustom
)

fun PaymentMethod.toEntity(): PaymentMethodEntity = PaymentMethodEntity(
    id = id,
    profileId = profileId,
    type = type,
    label = label,
    isCustom = isCustom
)

fun ExpenseEntity.toDomain(): Expense = Expense(
    id = id,
    profileId = profileId,
    title = title,
    amount = amount,
    currency = currency,
    categoryId = categoryId,
    paymentMethodId = paymentMethodId,
    date = date,
    notes = notes,
    isRecurring = isRecurring,
    source = source,
    attachmentRef = attachmentRef,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Expense.toEntity(): ExpenseEntity = ExpenseEntity(
    id = id,
    profileId = profileId,
    title = title,
    amount = amount,
    currency = currency,
    categoryId = categoryId,
    paymentMethodId = paymentMethodId,
    date = date,
    notes = notes,
    isRecurring = isRecurring,
    source = source,
    attachmentRef = attachmentRef,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun BudgetEntity.toDomain(): Budget = Budget(
    id = id,
    profileId = profileId,
    type = type,
    amount = amount,
    categoryId = categoryId,
    thresholdPct = thresholdPct
)

fun Budget.toEntity(): BudgetEntity = BudgetEntity(
    id = id,
    profileId = profileId,
    type = type,
    amount = amount,
    categoryId = categoryId,
    thresholdPct = thresholdPct
)

fun com.smartspend.app.data.local.entity.IncomeEntity.toDomain(): com.smartspend.app.domain.model.Income = com.smartspend.app.domain.model.Income(
    id = id,
    profileId = profileId,
    source = source,
    title = title,
    amount = amount,
    currency = currency,
    date = date,
    paymentMethodId = paymentMethodId,
    notes = notes,
    isRecurring = isRecurring,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun com.smartspend.app.domain.model.Income.toEntity(): com.smartspend.app.data.local.entity.IncomeEntity = com.smartspend.app.data.local.entity.IncomeEntity(
    id = id,
    profileId = profileId,
    source = source,
    title = title,
    amount = amount,
    currency = currency,
    date = date,
    paymentMethodId = paymentMethodId,
    notes = notes,
    isRecurring = isRecurring,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun com.smartspend.app.data.local.entity.AccountEntity.toDomain(): com.smartspend.app.domain.model.Account = com.smartspend.app.domain.model.Account(
    id = id,
    profileId = profileId,
    name = name,
    type = type,
    currency = currency,
    initialBalance = initialBalance,
    currentBalance = initialBalance,
    color = color,
    icon = icon,
    isActive = isActive,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun com.smartspend.app.domain.model.Account.toEntity(): com.smartspend.app.data.local.entity.AccountEntity = com.smartspend.app.data.local.entity.AccountEntity(
    id = id,
    profileId = profileId,
    name = name,
    type = type,
    currency = currency,
    initialBalance = initialBalance,
    color = color,
    icon = icon,
    isActive = isActive,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun com.smartspend.app.data.local.entity.RecurringExpenseEntity.toDomain(): com.smartspend.app.domain.model.RecurringExpense = com.smartspend.app.domain.model.RecurringExpense(
    id = id,
    profileId = profileId,
    title = title,
    amount = amount,
    currency = currency,
    categoryId = categoryId,
    paymentMethodId = paymentMethodId,
    frequency = frequency,
    startDate = startDate,
    nextDueDate = nextDueDate,
    lastGeneratedDate = lastGeneratedDate,
    isActive = isActive,
    autoCreate = autoCreate,
    notifyBeforeDays = notifyBeforeDays,
    notes = notes,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun com.smartspend.app.domain.model.RecurringExpense.toEntity(): com.smartspend.app.data.local.entity.RecurringExpenseEntity = com.smartspend.app.data.local.entity.RecurringExpenseEntity(
    id = id,
    profileId = profileId,
    title = title,
    amount = amount,
    currency = currency,
    categoryId = categoryId,
    paymentMethodId = paymentMethodId,
    frequency = frequency,
    startDate = startDate,
    nextDueDate = nextDueDate,
    lastGeneratedDate = lastGeneratedDate,
    isActive = isActive,
    autoCreate = autoCreate,
    notifyBeforeDays = notifyBeforeDays,
    notes = notes,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun com.smartspend.app.data.local.entity.SavingsGoalEntity.toDomain(): com.smartspend.app.domain.model.SavingsGoal = com.smartspend.app.domain.model.SavingsGoal(
    id = id,
    profileId = profileId,
    name = name,
    targetAmount = targetAmount,
    currentAmount = currentAmount,
    currency = currency,
    targetDate = targetDate,
    color = color,
    icon = icon,
    isArchived = isArchived,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun com.smartspend.app.domain.model.SavingsGoal.toEntity(): com.smartspend.app.data.local.entity.SavingsGoalEntity = com.smartspend.app.data.local.entity.SavingsGoalEntity(
    id = id,
    profileId = profileId,
    name = name,
    targetAmount = targetAmount,
    currentAmount = currentAmount,
    currency = currency,
    targetDate = targetDate,
    color = color,
    icon = icon,
    isArchived = isArchived,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun com.smartspend.app.data.local.entity.SavingsGoalContributionEntity.toDomain(): com.smartspend.app.domain.model.SavingsGoalContribution = com.smartspend.app.domain.model.SavingsGoalContribution(
    id = id,
    goalId = goalId,
    profileId = profileId,
    amount = amount,
    type = type,
    date = date,
    notes = notes,
    createdAt = createdAt
)

fun com.smartspend.app.domain.model.SavingsGoalContribution.toEntity(): com.smartspend.app.data.local.entity.SavingsGoalContributionEntity = com.smartspend.app.data.local.entity.SavingsGoalContributionEntity(
    id = id,
    goalId = goalId,
    profileId = profileId,
    amount = amount,
    type = type,
    date = date,
    notes = notes,
    createdAt = createdAt
)
