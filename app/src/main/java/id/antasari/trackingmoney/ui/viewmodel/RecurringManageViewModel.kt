package id.antasari.trackingmoney.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import id.antasari.trackingmoney.data.db.AppDatabase
import id.antasari.trackingmoney.data.model.RecurringTransaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class RecurringTransactionItemUiState(
    val recurring: RecurringTransaction,
    val categoryName: String,
    val categoryIcon: String
)

class RecurringManageViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val recurringDao = db.recurringTransactionDao()
    private val categoryDao = db.categoryDao()

    private val _recurringList = MutableStateFlow<List<RecurringTransactionItemUiState>>(emptyList())
    val recurringList: StateFlow<List<RecurringTransactionItemUiState>> = _recurringList.asStateFlow()

    init {
        loadRecurringTransactions()
    }

    private fun loadRecurringTransactions() {
        viewModelScope.launch {
            combine(
                recurringDao.getAllRecurringTransactions(),
                categoryDao.getAllCategories()
            ) { list, categories ->
                list.map { recurring ->
                    val cat = categories.find { it.id == recurring.categoryId }
                    RecurringTransactionItemUiState(
                        recurring = recurring,
                        categoryName = cat?.name ?: "Unknown",
                        categoryIcon = cat?.icon ?: "❓"
                    )
                }
            }.collect { uiList ->
                _recurringList.value = uiList
            }
        }
    }

    fun deleteRecurring(recurring: RecurringTransaction) {
        viewModelScope.launch {
            recurringDao.deleteRecurringTransaction(recurring)
        }
    }
}

class RecurringManageViewModelFactory(private val application: Application) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecurringManageViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RecurringManageViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
