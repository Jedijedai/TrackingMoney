package id.antasari.trackingmoney.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import id.antasari.trackingmoney.data.dao.CategoryDao
import id.antasari.trackingmoney.data.dao.TransactionDao
import id.antasari.trackingmoney.data.model.Category
import id.antasari.trackingmoney.data.model.RecurringTransaction
import id.antasari.trackingmoney.data.model.Transaction
import id.antasari.trackingmoney.data.model.TransactionType
import id.antasari.trackingmoney.data.dao.RecurringTransactionDao
import id.antasari.trackingmoney.data.dao.ReportDao
import androidx.room.migration.Migration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Database(entities = [Category::class, Transaction::class, RecurringTransaction::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun recurringTransactionDao(): RecurringTransactionDao
    abstract fun reportDao(): ReportDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tracking_money_db"
                )
                .addMigrations(MIGRATION_2_3)
                .fallbackToDestructiveMigration()
                .addCallback(DatabaseCallback())
                .build().also { INSTANCE = it }
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `recurring_transactions` (" +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`amount` INTEGER NOT NULL, " +
                            "`categoryId` INTEGER NOT NULL, " +
                            "`note` TEXT NOT NULL, " +
                            "`type` TEXT NOT NULL, " +
                            "`frequency` TEXT NOT NULL, " +
                            "`nextDueDateMillis` INTEGER NOT NULL, " +
                            "FOREIGN KEY(`categoryId`) REFERENCES `categories`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT)"
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_recurring_transactions_categoryId` ON `recurring_transactions` (`categoryId`)")
            }
        }
    }

    private class DatabaseCallback : RoomDatabase.Callback() {
        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    val categoryDao = database.categoryDao()
                    if (categoryDao.getAllCategories().first().isEmpty()) {
                        populateDatabase(categoryDao)
                    }
                }
            }
        }

        suspend fun populateDatabase(categoryDao: CategoryDao) {
            val defaultCategories = listOf(
                Category(name = "Makan", type = TransactionType.EXPENSE, icon = "🍗", isDefault = true),
                Category(name = "Bensin", type = TransactionType.EXPENSE, icon = "⛽", isDefault = true),
                Category(name = "Kebutuhan", type = TransactionType.EXPENSE, icon = "🛍️", isDefault = true),
                Category(name = "Lainnya", type = TransactionType.EXPENSE, icon = "💸", isDefault = true),
                
                Category(name = "Gaji", type = TransactionType.INCOME, icon = "💰", isDefault = true),
                Category(name = "Pemberian", type = TransactionType.INCOME, icon = "🎁", isDefault = true),
                Category(name = "Lainnya", type = TransactionType.INCOME, icon = "💵", isDefault = true)
            )
            categoryDao.insertCategories(defaultCategories)
        }
    }
}
