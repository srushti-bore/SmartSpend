package com.smartspend.app.domain.repository

import com.smartspend.app.data.local.dao.IncomeSourceSpendAggregate
import com.smartspend.app.domain.model.Income
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal

interface IncomeRepository {
    fun getAllIncomes(profileId: String): Flow<List<Income>>
    suspend fun getIncomeById(profileId: String, id: String): Income?
    fun getIncomesBetweenDates(profileId: String, startDate: Long, endDate: Long): Flow<List<Income>>
    fun getTotalIncome(profileId: String, startDate: Long, endDate: Long): Flow<BigDecimal>
    fun getIncomeSourceAggregates(profileId: String, startDate: Long, endDate: Long): Flow<List<IncomeSourceSpendAggregate>>
    suspend fun addIncome(income: Income)
    suspend fun updateIncome(income: Income)
    suspend fun deleteIncome(profileId: String, id: String)
}
