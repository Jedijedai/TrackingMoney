package id.antasari.trackingmoney.data.dao

import androidx.room.Dao
import androidx.room.Query
import id.antasari.trackingmoney.data.model.TransactionType
import kotlinx.coroutines.flow.Flow

data class CategorySum(
    val categoryId: Int,
    val categoryName: String,
    val categoryIcon: String,
    val totalAmount: Long
)

data class MonthlyTotal(
    val monthLabel: String, // format "2026-08"
    val totalExpense: Long,
    val totalIncome: Long
)

@Dao
interface ReportDao {
    @Query("""
        SELECT c.id AS categoryId, c.name AS categoryName, c.icon AS categoryIcon, SUM(t.amount) AS totalAmount
        FROM transactions t
        INNER JOIN categories c ON t.categoryId = c.id
        WHERE t.type = :type AND t.dateMillis BETWEEN :startDate AND :endDate
        GROUP BY c.id
        ORDER BY totalAmount DESC
    """)
    fun getCategoryBreakdown(
        type: TransactionType,
        startDate: Long,
        endDate: Long
    ): Flow<List<CategorySum>>

    @Query("""
        SELECT strftime('%Y-%m', datetime(dateMillis / 1000, 'unixepoch', 'localtime')) AS monthLabel,
               SUM(CASE WHEN type = 'EXPENSE' THEN amount ELSE 0 END) AS totalExpense,
               SUM(CASE WHEN type = 'INCOME' THEN amount ELSE 0 END) AS totalIncome
        FROM transactions
        WHERE dateMillis BETWEEN :startDate AND :endDate
        GROUP BY monthLabel
        ORDER BY monthLabel ASC
    """)
    fun getMonthlyTrend(
        startDate: Long,
        endDate: Long
    ): Flow<List<MonthlyTotal>>
}
