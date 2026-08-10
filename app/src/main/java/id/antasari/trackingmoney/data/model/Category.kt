package id.antasari.trackingmoney.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val type: TransactionType,
    val iconId: Int = 0, // Reference to drawable resource if needed
    val isDefault: Boolean = false // To differentiate default categories from user-created ones
)
