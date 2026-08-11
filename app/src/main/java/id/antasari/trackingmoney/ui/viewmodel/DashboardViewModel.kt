package id.antasari.trackingmoney.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import id.antasari.trackingmoney.data.db.AppDatabase
import id.antasari.trackingmoney.data.model.TransactionType
import id.antasari.trackingmoney.ui.components.ChartData
import id.antasari.trackingmoney.ui.theme.ChartColors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Calendar

data class TransactionItemUiState(
    val id: Int,
    val icon: String,
    val categoryName: String,
    val note: String,
    val amount: Long,
    val dateMillis: Long,
    val type: TransactionType
)

data class DashboardUiState(
    val totalIncome: Long = 0L,
    val totalExpense: Long = 0L,
    val totalBalance: Long = 0L,
    val expenseChartData: List<ChartData> = emptyList(),
    val legendItems: List<Pair<String, androidx.compose.ui.graphics.Color>> = emptyList(),
    val recentTransactions: List<TransactionItemUiState> = emptyList()
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val transactionDao = db.transactionDao()
    private val categoryDao = db.categoryDao()

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            val startOfMonth = calendar.timeInMillis
            
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            val endOfMonth = calendar.timeInMillis

            // We launch coroutines to collect flows or just get snapshot.
            // Since DAO returns Flow, we can collect them.
            launch {
                transactionDao.getTotalAmountByTypeAndDateRange(TransactionType.INCOME.name, startOfMonth, endOfMonth).collect { income ->
                    _uiState.value = _uiState.value.copy(totalIncome = income ?: 0L, totalBalance = (income ?: 0L) - _uiState.value.totalExpense)
                }
            }
            
            launch {
                transactionDao.getTotalAmountByTypeAndDateRange(TransactionType.EXPENSE.name, startOfMonth, endOfMonth).collect { expense ->
                    _uiState.value = _uiState.value.copy(totalExpense = expense ?: 0L, totalBalance = _uiState.value.totalIncome - (expense ?: 0L))
                }
            }
            
            launch {
                combine(
                    transactionDao.getTransactionsByDateRange(startOfMonth, endOfMonth),
                    categoryDao.getAllCategories()
                ) { txs, allCategories ->
                    val expensesThisMonth = txs.filter { it.type == TransactionType.EXPENSE }
                    val groupedExpenses = expensesThisMonth.groupBy { it.categoryId }
                    
                    val chartDataList = mutableListOf<ChartData>()
                    val legendList = mutableListOf<Pair<String, androidx.compose.ui.graphics.Color>>()
                    
                    var colorIndex = 0
                    groupedExpenses.forEach { (categoryId, groupedTxs) ->
                        val category = allCategories.find { it.id == categoryId }
                        val name = category?.let { "${it.icon} ${it.name}" } ?: "Unknown"
                        val sum = groupedTxs.sumOf { it.amount }
                        val color = ChartColors[colorIndex % ChartColors.size]
                        
                        chartDataList.add(ChartData(value = sum.toFloat(), color = color))
                        legendList.add(name to color)
                        colorIndex++
                    }

                    val recentList = txs.sortedByDescending { it.dateMillis }.map { tx ->
                        val category = allCategories.find { it.id == tx.categoryId }
                        TransactionItemUiState(
                            id = tx.id,
                            icon = category?.icon ?: "❓",
                            categoryName = category?.name ?: "Unknown",
                            note = tx.note,
                            amount = tx.amount,
                            dateMillis = tx.dateMillis,
                            type = tx.type
                        )
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        expenseChartData = chartDataList,
                        legendItems = legendList,
                        recentTransactions = recentList
                    )
                }.collect { }
            }
        }
    }
}
