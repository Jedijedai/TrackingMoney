package id.antasari.trackingmoney.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import id.antasari.trackingmoney.data.db.AppDatabase
import id.antasari.trackingmoney.data.model.Category
import id.antasari.trackingmoney.data.model.Transaction
import id.antasari.trackingmoney.data.model.TransactionType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

data class AddTransactionUiState(
    val amount: String = "",
    val note: String = "",
    val selectedType: TransactionType = TransactionType.EXPENSE,
    val selectedCategory: Category? = null,
    val categories: List<Category> = emptyList(),
    val isSaved: Boolean = false,
    val isSaving: Boolean = false,
    val transactionId: Int? = null,
    val dateMillis: Long = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
        set(java.util.Calendar.HOUR_OF_DAY, 0)
        set(java.util.Calendar.MINUTE, 0)
        set(java.util.Calendar.SECOND, 0)
        set(java.util.Calendar.MILLISECOND, 0)
    }.timeInMillis
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

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun loadCategories() {
        viewModelScope.launch {
            _uiState.map { it.selectedType }
                .distinctUntilChanged()
                .flatMapLatest { type ->
                    categoryDao.getCategoriesByType(type.name)
                }
                .collectLatest { cats ->
                    // Make sure the selected category is valid for the current type
                    val currentSelected = _uiState.value.selectedCategory
                    val newSelected = if (currentSelected != null && cats.any { it.id == currentSelected.id }) {
                        currentSelected
                    } else {
                        cats.firstOrNull()
                    }

                    _uiState.value = _uiState.value.copy(
                        categories = cats,
                        selectedCategory = newSelected
                    )
                }
        }
    }

    fun setTransactionType(type: TransactionType) {
        if (_uiState.value.selectedType != type) {
            _uiState.value = _uiState.value.copy(selectedType = type, selectedCategory = null)
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

    fun setDate(millis: Long) {
        _uiState.value = _uiState.value.copy(dateMillis = millis)
    }

    fun setCategory(category: Category) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    fun loadTransaction(id: Int) {
        viewModelScope.launch {
            val transaction = transactionDao.getTransactionById(id)
            if (transaction != null) {
                _uiState.value = _uiState.value.copy(
                    amount = transaction.amount.toString(),
                    note = transaction.note,
                    selectedType = transaction.type,
                    transactionId = transaction.id,
                    dateMillis = transaction.dateMillis
                )
                // The category selection will be updated automatically via the flatMapLatest 
                // in loadCategories() because selectedType is set. However, we also need to set 
                // selectedCategory once categories are loaded. We can do this by setting a temporary 
                // selectedCategory with just the id, or handling it inside loadCategories.
                // Let's just wait for the category flow to emit. We'll set a placeholder category.
                val category = categoryDao.getCategoriesByType(transaction.type.name).firstOrNull()?.find { it.id == transaction.categoryId }
                if (category != null) {
                    _uiState.value = _uiState.value.copy(selectedCategory = category)
                }
            }
        }
    }

    fun saveTransaction() {
        val currentState = _uiState.value
        if (currentState.isSaving || currentState.isSaved) return

        val amountVal = currentState.amount.replace(".", "").toLongOrNull() ?: 0L
        val categoryId = currentState.selectedCategory?.id
        
        if (amountVal > 0 && categoryId != null) {
            _uiState.value = currentState.copy(isSaving = true)
            viewModelScope.launch {
                val transaction = Transaction(
                    id = currentState.transactionId ?: 0,
                    amount = amountVal,
                    categoryId = categoryId,
                    dateMillis = currentState.dateMillis ?: System.currentTimeMillis(),
                    note = currentState.note,
                    type = currentState.selectedType
                )
                if (currentState.transactionId != null) {
                    transactionDao.updateTransaction(transaction)
                } else {
                    transactionDao.insertTransaction(transaction)
                }
                _uiState.value = _uiState.value.copy(isSaving = false, isSaved = true)
            }
        }
    }

    fun deleteTransaction() {
        val currentState = _uiState.value
        val txId = currentState.transactionId
        if (txId != null && !currentState.isSaving) {
            _uiState.value = currentState.copy(isSaving = true)
            viewModelScope.launch {
                val transaction = transactionDao.getTransactionById(txId)
                if (transaction != null) {
                    transactionDao.deleteTransaction(transaction)
                }
                _uiState.value = _uiState.value.copy(isSaving = false, isSaved = true)
            }
        }
    }
}

class AddTransactionViewModelFactory(private val application: Application) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddTransactionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddTransactionViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
