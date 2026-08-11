package id.antasari.trackingmoney.ui.viewmodel

import android.app.Application
import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import id.antasari.trackingmoney.data.db.AppDatabase
import id.antasari.trackingmoney.data.model.Category
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

data class CategoryManageUiState(
    val selectedType: TransactionType = TransactionType.EXPENSE,
    val categories: List<Category> = emptyList(),
    val errorMessage: String? = null
)

class CategoryManageViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val categoryDao = db.categoryDao()

    private val _uiState = MutableStateFlow(CategoryManageUiState())
    val uiState: StateFlow<CategoryManageUiState> = _uiState.asStateFlow()

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
                    _uiState.value = _uiState.value.copy(categories = cats)
                }
        }
    }

    fun setTransactionType(type: TransactionType) {
        _uiState.value = _uiState.value.copy(selectedType = type, errorMessage = null)
    }

    fun saveCategory(category: Category) {
        viewModelScope.launch {
            if (category.id == 0) {
                categoryDao.insertCategory(category)
            } else {
                categoryDao.updateCategory(category)
            }
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            try {
                categoryDao.deleteCategory(category)
                _uiState.value = _uiState.value.copy(errorMessage = null)
            } catch (e: SQLiteConstraintException) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Kategori '${category.name}' tidak bisa dihapus karena sedang digunakan dalam transaksi."
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Terjadi kesalahan saat menghapus kategori."
                )
            }
        }
    }

    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

class CategoryManageViewModelFactory(private val application: Application) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CategoryManageViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CategoryManageViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
