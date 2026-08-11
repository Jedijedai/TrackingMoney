package id.antasari.trackingmoney.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import id.antasari.trackingmoney.data.db.AppDatabase
import id.antasari.trackingmoney.data.model.Frequency
import id.antasari.trackingmoney.data.model.Transaction
import java.util.Calendar
import java.util.TimeZone

class RecurringWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val db = AppDatabase.getDatabase(applicationContext)
        val recurringDao = db.recurringTransactionDao()
        val transactionDao = db.transactionDao()
        
        val currentMillis = System.currentTimeMillis()
        val dueTransactions = recurringDao.getDueTransactions(currentMillis)

        for (recurring in dueTransactions) {
            // Insert standard transaction
            val transaction = Transaction(
                amount = recurring.amount,
                categoryId = recurring.categoryId,
                dateMillis = currentMillis,
                note = recurring.note,
                type = recurring.type
            )
            transactionDao.insertTransaction(transaction)

            // Update next due date
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            calendar.timeInMillis = recurring.nextDueDateMillis
            
            when (recurring.frequency) {
                Frequency.DAILY -> calendar.add(Calendar.DAY_OF_YEAR, 1)
                Frequency.WEEKLY -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
                Frequency.MONTHLY -> calendar.add(Calendar.MONTH, 1)
                Frequency.YEARLY -> calendar.add(Calendar.YEAR, 1)
            }
            
            val updatedRecurring = recurring.copy(nextDueDateMillis = calendar.timeInMillis)
            recurringDao.updateRecurringTransaction(updatedRecurring)
        }

        return Result.success()
    }
}
