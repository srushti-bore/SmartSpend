package com.smartspend.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.smartspend.app.data.local.entity.PaymentMethodEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentMethodDao {

    @Query("SELECT * FROM payment_methods WHERE profileId = :profileId ORDER BY label ASC")
    fun getPaymentMethodsForProfile(profileId: String): Flow<List<PaymentMethodEntity>>

    @Query("SELECT * FROM payment_methods WHERE profileId = :profileId AND id = :id LIMIT 1")
    suspend fun getPaymentMethodById(profileId: String, id: String): PaymentMethodEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaymentMethods(methods: List<PaymentMethodEntity>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPaymentMethod(method: PaymentMethodEntity)

    @Update
    suspend fun updatePaymentMethod(method: PaymentMethodEntity)

    @Query("DELETE FROM payment_methods WHERE profileId = :profileId AND id = :id")
    suspend fun deletePaymentMethodById(profileId: String, id: String)

    @Query("SELECT COUNT(*) FROM expenses WHERE profileId = :profileId AND paymentMethodId = :paymentMethodId")
    suspend fun countExpensesForPaymentMethod(profileId: String, paymentMethodId: String): Int
}
