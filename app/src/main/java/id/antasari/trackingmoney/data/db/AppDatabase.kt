package id.antasari.trackingmoney.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import id.antasari.trackingmoney.data.dao.CategoryDao
import id.antasari.trackingmoney.data.dao.TransactionDao
import id.antasari.trackingmoney.data.model.Category
import id.antasari.trackingmoney.data.model.Transaction
import id.antasari.trackingmoney.data.model.TransactionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Category::class, Transaction::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tracking_money_db"
                )
                .addCallback(DatabaseCallback())
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateDatabase(database.categoryDao())
                }
            }
        }

        suspend fun populateDatabase(categoryDao: CategoryDao) {
            val defaultCategories = listOf(
                Category(name = "Makan", type = TransactionType.EXPENSE, isDefault = true),
                Category(name = "Bensin", type = TransactionType.EXPENSE, isDefault = true),
                Category(name = "Kebutuhan", type = TransactionType.EXPENSE, isDefault = true),
                Category(name = "Lainnya", type = TransactionType.EXPENSE, isDefault = true),
                
                Category(name = "Gaji", type = TransactionType.INCOME, isDefault = true),
                Category(name = "Pemberian", type = TransactionType.INCOME, isDefault = true),
                Category(name = "Lainnya", type = TransactionType.INCOME, isDefault = true)
            )
            categoryDao.insertCategories(defaultCategories)
        }
    }
}
