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
import kotlinx.coroutines.launch

data class AddTransactionUiState(
    val amount: String = "",
    val note: String = "",
    val selectedType: TransactionType = TransactionType.EXPENSE,
    val selectedCategory: Category? = null,
    val categories: List<Category> = emptyList(),
    val isSaved: Boolean = false,
    val isSaving: Boolean = false
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

    fun setCategory(category: Category) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    fun saveTransaction() {
        val currentState = _uiState.value
        if (currentState.isSaving || currentState.isSaved) return

        val amountVal = currentState.amount.toLongOrNull() ?: 0L
        val categoryId = currentState.selectedCategory?.id
        
        if (amountVal > 0 && categoryId != null) {
            _uiState.value = currentState.copy(isSaving = true)
            viewModelScope.launch {
                val transaction = Transaction(
                    amount = amountVal,
                    categoryId = categoryId,
                    dateMillis = System.currentTimeMillis(),
                    note = currentState.note,
                    type = currentState.selectedType
                )
                transactionDao.insertTransaction(transaction)
                _uiState.value = _uiState.value.copy(isSaving = false, isSaved = true)
            }
        }
    }
}
