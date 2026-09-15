package com.smartspend.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.smartspend.app.data.local.entity.IncomeEntity
import com.smartspend.app.domain.model.IncomeSource
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal

data class IncomeSourceSpendAggregate(
    val source: IncomeSource,
    val totalAmount: BigDecimal,
    val transactionCount: Int
)

@Dao
interface IncomeDao {

    @Query("SELECT * FROM incomes WHERE profileId = :profileId ORDER BY date DESC, createdAt DESC")
    fun getAllIncomesForProfile(profileId: String): Flow<List<IncomeEntity>>

    @Query("SELECT * FROM incomes WHERE profileId = :profileId AND id = :id LIMIT 1")
    suspend fun getIncomeById(profileId: String, id: String): IncomeEntity?

    @Query("""
        SELECT * FROM incomes 
        WHERE profileId = :profileId 
        AND date >= :startDate AND date <= :endDate
        ORDER BY date DESC, createdAt DESC
    """)
    fun getIncomesBetweenDates(profileId: String, startDate: Long, endDate: Long): Flow<List<IncomeEntity>>

    @Query("""
        SELECT COALESCE(SUM(amount), '0.00') 
        FROM incomes 
        WHERE profileId = :profileId AND date >= :startDate AND date <= :endDate
    """)
    fun getTotalIncomeBetween(profileId: String, startDate: Long, endDate: Long): Flow<BigDecimal?>

    @Query("""
        SELECT source, SUM(amount) AS totalAmount, COUNT(id) AS transactionCount
        FROM incomes
        WHERE profileId = :profileId AND date >= :startDate AND date <= :endDate
        GROUP BY source
        ORDER BY totalAmount DESC
    """)
    fun getIncomeSourceAggregatesBetween(profileId: String, startDate: Long, endDate: Long): Flow<List<IncomeSourceSpendAggregate>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncome(income: IncomeEntity)

    @Update
    suspend fun updateIncome(income: IncomeEntity)

    @Query("DELETE FROM incomes WHERE profileId = :profileId AND id = :id")
    suspend fun deleteIncomeById(profileId: String, id: String)
}
