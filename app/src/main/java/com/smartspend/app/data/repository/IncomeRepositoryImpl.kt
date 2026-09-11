package com.smartspend.app.data.repository

import com.smartspend.app.data.local.dao.IncomeDao
import com.smartspend.app.data.local.dao.IncomeSourceSpendAggregate
import com.smartspend.app.data.mapper.toDomain
import com.smartspend.app.data.mapper.toEntity
import com.smartspend.app.domain.model.Income
import com.smartspend.app.domain.repository.IncomeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IncomeRepositoryImpl @Inject constructor(
    private val incomeDao: IncomeDao
) : IncomeRepository {

    override fun getAllIncomes(profileId: String): Flow<List<Income>> {
        return incomeDao.getAllIncomesForProfile(profileId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getIncomeById(profileId: String, id: String): Income? {
        return incomeDao.getIncomeById(profileId, id)?.toDomain()
    }

    override fun getIncomesBetweenDates(profileId: String, startDate: Long, endDate: Long): Flow<List<Income>> {
        return incomeDao.getIncomesBetweenDates(profileId, startDate, endDate).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getTotalIncome(profileId: String, startDate: Long, endDate: Long): Flow<BigDecimal> {
        return incomeDao.getTotalIncomeBetween(profileId, startDate, endDate).map { it ?: BigDecimal.ZERO }
    }

    override fun getIncomeSourceAggregates(profileId: String, startDate: Long, endDate: Long): Flow<List<IncomeSourceSpendAggregate>> {
        return incomeDao.getIncomeSourceAggregatesBetween(profileId, startDate, endDate)
    }

    override suspend fun addIncome(income: Income) {
        incomeDao.insertIncome(income.toEntity())
    }

    override suspend fun updateIncome(income: Income) {
        incomeDao.updateIncome(income.toEntity())
    }

    override suspend fun deleteIncome(profileId: String, id: String) {
        incomeDao.deleteIncomeById(profileId, id)
    }
}
