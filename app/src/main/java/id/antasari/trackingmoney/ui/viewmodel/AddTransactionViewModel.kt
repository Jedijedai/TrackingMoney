package id.antasari.trackingmoney.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import id.antasari.trackingmoney.data.db.AppDatabase
import id.antasari.trackingmoney.data.model.Category
import id.antasari.trackingmoney.data.model.Transaction
import id.antasari.trackingmoney.data.model.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AddTransactionUiState(
    val amount: String = "",
    val note: String = "",
    val selectedType: TransactionType = TransactionType.EXPENSE,
    val selectedCategory: Category? = null,
    val categories: List<Category> = emptyList(),
    val isSaved: Boolean = false
)

class AddTransactionViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val categoryDao = db.categoryDao()
    private val transactionDao = db.transactionDao()

    private val _uiState = MutableStateFlow(AddTransactionUiState())
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            categoryDao.getAllCategories().collect { cats ->
                _uiState.value = _uiState.value.copy(
                    categories = cats.filter { it.type == _uiState.value.selectedType },
                    selectedCategory = cats.firstOrNull { it.type == _uiState.value.selectedType }
                )
            }
        }
    }

    fun setTransactionType(type: TransactionType) {
        _uiState.value = _uiState.value.copy(selectedType = type)
        // Refresh categories based on new type
        viewModelScope.launch {
            categoryDao.getAllCategories().collect { cats ->
                val filtered = cats.filter { it.type == type }
                _uiState.value = _uiState.value.copy(
                    categories = filtered,
                    selectedCategory = filtered.firstOrNull()
                )
            }
        }
    }

    fun setAmount(amount: String) {
        // Only allow digits
        val cleanAmount = amount.filter { it.isDigit() }
        _uiState.value = _uiState.value.copy(amount = cleanAmount)
    }

    fun setNote(note: String) {
        _uiState.value = _uiState.value.copy(note = note)
    }

    fun setCategory(category: Category) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    fun saveTransaction() {
        val currentState = _uiState.value
        val amountVal = currentState.amount.toLongOrNull() ?: 0L
        val categoryId = currentState.selectedCategory?.id
        
        if (amountVal > 0 && categoryId != null) {
            viewModelScope.launch {
                val transaction = Transaction(
                    amount = amountVal,
                    categoryId = categoryId,
                    dateMillis = System.currentTimeMillis(),
                    note = currentState.note,
                    type = currentState.selectedType
                )
                transactionDao.insertTransaction(transaction)
                _uiState.value = currentState.copy(isSaved = true)
            }
        }
    }
}
