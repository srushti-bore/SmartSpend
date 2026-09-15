package com.smartspend.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.smartspend.app.data.local.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal

data class CategorySpendAggregate(
    val categoryId: String,
    val categoryName: String,
    val colorHex: String,
    val iconName: String,
    val totalAmount: BigDecimal,
    val transactionCount: Int
)

@Dao
interface ExpenseDao {

    @Query("SELECT * FROM expenses WHERE profileId = :profileId ORDER BY date DESC, createdAt DESC")
    fun getAllExpensesForProfile(profileId: String): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE profileId = :profileId AND id = :id LIMIT 1")
    suspend fun getExpenseById(profileId: String, id: String): ExpenseEntity?

    @Query("SELECT * FROM expenses WHERE profileId = :profileId ORDER BY date DESC, createdAt DESC LIMIT :limit")
    fun getRecentExpensesForProfile(profileId: String, limit: Int = 5): Flow<List<ExpenseEntity>>

    @Query("""
        SELECT * FROM expenses 
        WHERE profileId = :profileId 
        AND date >= :startDate AND date <= :endDate
        ORDER BY date DESC, createdAt DESC
    """)
    fun getExpensesBetweenDates(profileId: String, startDate: Long, endDate: Long): Flow<List<ExpenseEntity>>

    @Query("""
        SELECT * FROM expenses 
        WHERE profileId = :profileId 
        AND (:categoryId IS NULL OR categoryId = :categoryId)
        AND (:paymentMethodId IS NULL OR paymentMethodId = :paymentMethodId)
        AND (:startDate IS NULL OR date >= :startDate)
        AND (:endDate IS NULL OR date <= :endDate)
        AND (:query IS NULL OR title LIKE '%' || :query || '%' OR notes LIKE '%' || :query || '%')
        ORDER BY 
            CASE WHEN :sortOrder = 'DATE_DESC' THEN date END DESC,
            CASE WHEN :sortOrder = 'DATE_ASC' THEN date END ASC,
            CASE WHEN :sortOrder = 'AMOUNT_DESC' THEN amount END DESC,
            CASE WHEN :sortOrder = 'AMOUNT_ASC' THEN amount END ASC,
            createdAt DESC
    """)
    fun filterExpenses(
        profileId: String,
        query: String?,
        categoryId: String?,
        paymentMethodId: String?,
        startDate: Long?,
        endDate: Long?,
        sortOrder: String = "DATE_DESC"
    ): Flow<List<ExpenseEntity>>

    @Query("""
        SELECT e.categoryId, c.name AS categoryName, c.colorHex, c.iconName, SUM(e.amount) AS totalAmount, COUNT(e.id) AS transactionCount
        FROM expenses e
        INNER JOIN categories c ON e.categoryId = c.id
        WHERE e.profileId = :profileId AND e.date >= :startDate AND e.date <= :endDate
        GROUP BY e.categoryId, c.name, c.colorHex, c.iconName
        ORDER BY totalAmount DESC
    """)
    fun getCategorySpendingBetween(profileId: String, startDate: Long, endDate: Long): Flow<List<CategorySpendAggregate>>

    @Query("""
        SELECT COALESCE(SUM(amount), '0.00') 
        FROM expenses 
        WHERE profileId = :profileId AND date >= :startDate AND date <= :endDate
    """)
    fun getTotalSpendingBetween(profileId: String, startDate: Long, endDate: Long): Flow<BigDecimal?>

    @Query("""
        SELECT COALESCE(SUM(amount), '0.00') 
        FROM expenses 
        WHERE profileId = :profileId AND categoryId = :categoryId AND date >= :startDate AND date <= :endDate
    """)
    fun getCategoryTotalSpendingBetween(profileId: String, categoryId: String, startDate: Long, endDate: Long): Flow<BigDecimal?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity)

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Query("DELETE FROM expenses WHERE profileId = :profileId AND id = :id")
    suspend fun deleteExpenseById(profileId: String, id: String)
}
