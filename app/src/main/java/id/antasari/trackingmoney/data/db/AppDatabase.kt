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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Database(entities = [Category::class, Transaction::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao

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
                .fallbackToDestructiveMigration()
                .addCallback(DatabaseCallback())
                .build().also { INSTANCE = it }
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
