package id.antasari.trackingmoney.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import id.antasari.trackingmoney.data.db.AppDatabase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import id.antasari.trackingmoney.ui.viewmodel.TransactionItemUiState

data class SearchUiState(
    val query: String = "",
    val searchResults: List<TransactionItemUiState> = emptyList(),
    val isLoading: Boolean = false
)

class SearchViewModel(application: Application) : AndroidViewModel(application) {
    private val transactionDao = AppDatabase.getDatabase(application).transactionDao()
    private val categoryDao = AppDatabase.getDatabase(application).categoryDao()

    private val _query = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val uiState: StateFlow<SearchUiState> = _query
        .debounce(300)
        .flatMapLatest { query ->
            if (query.isBlank()) {
                flowOf(SearchUiState(query = query, searchResults = emptyList()))
            } else {
                combine(
                    transactionDao.searchTransactions(query),
                    categoryDao.getAllCategories()
                ) { transactions, categories ->
                    val categoryMap = categories.associateBy { it.id }
                    val results = transactions.map { tx ->
                        val category = categoryMap[tx.categoryId]
                        TransactionItemUiState(
                            id = tx.id,
                            amount = tx.amount,
                            type = tx.type,
                            dateMillis = tx.dateMillis,
                            note = tx.note,
                            categoryName = category?.name ?: "Unknown",
                            icon = category?.icon ?: "❓"
                        )
                    }
                    SearchUiState(query = query, searchResults = results)
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SearchUiState()
        )

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
    }
}
