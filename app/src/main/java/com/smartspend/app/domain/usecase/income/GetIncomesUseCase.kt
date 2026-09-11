package com.smartspend.app.domain.usecase.income

import com.smartspend.app.domain.model.Income
import com.smartspend.app.domain.repository.IncomeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetIncomesUseCase @Inject constructor(
    private val incomeRepository: IncomeRepository
) {
    operator fun invoke(profileId: String, startDate: Long? = null, endDate: Long? = null): Flow<List<Income>> {
        return if (startDate != null && endDate != null) {
            incomeRepository.getIncomesBetweenDates(profileId, startDate, endDate)
        } else {
            incomeRepository.getAllIncomes(profileId)
        }
    }
}
