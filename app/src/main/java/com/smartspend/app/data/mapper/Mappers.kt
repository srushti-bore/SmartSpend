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
