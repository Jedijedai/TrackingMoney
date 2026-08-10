package id.antasari.trackingmoney.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val type: TransactionType,
    val icon: String = "", // Stores emoji string
    val isDefault: Boolean = false // To differentiate default categories from user-created ones
)
