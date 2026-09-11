package com.smartspend.app.domain.usecase.income

import com.smartspend.app.domain.repository.IncomeRepository
import javax.inject.Inject

class DeleteIncomeUseCase @Inject constructor(
    private val incomeRepository: IncomeRepository
) {
    suspend operator fun invoke(profileId: String, incomeId: String): Result<Unit> {
        val existing = incomeRepository.getIncomeById(profileId, incomeId)
            ?: return Result.failure(IllegalArgumentException("Income record not found"))

        incomeRepository.deleteIncome(profileId, existing.id)
        return Result.success(Unit)
    }
}
