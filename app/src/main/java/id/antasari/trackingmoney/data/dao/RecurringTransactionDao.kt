package id.antasari.trackingmoney.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import id.antasari.trackingmoney.data.model.RecurringTransaction
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringTransactionDao {
    @Query("SELECT * FROM recurring_transactions")
    fun getAllRecurringTransactions(): Flow<List<RecurringTransaction>>

    @Query("SELECT * FROM recurring_transactions WHERE nextDueDateMillis <= :currentMillis")
    suspend fun getDueTransactions(currentMillis: Long): List<RecurringTransaction>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurringTransaction(transaction: RecurringTransaction)

    @Update
    suspend fun updateRecurringTransaction(transaction: RecurringTransaction)

    @Delete
    suspend fun deleteRecurringTransaction(transaction: RecurringTransaction)
}
