package id.antasari.trackingmoney.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class Frequency {
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY
}

@Entity(
    tableName = "recurring_transactions",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("categoryId")]
)
data class RecurringTransaction(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val amount: Long,
    val categoryId: Int,
    val note: String,
    val type: TransactionType,
    val frequency: Frequency,
    val nextDueDateMillis: Long
)
