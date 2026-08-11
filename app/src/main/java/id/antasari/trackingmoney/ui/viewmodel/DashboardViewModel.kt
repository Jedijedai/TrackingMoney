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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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
    val recentTransactions: List<TransactionItemUiState> = emptyList(),
    val selectedMonthName: String = ""
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val transactionDao = db.transactionDao()
    private val categoryDao = db.categoryDao()

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
    
    private val _selectedDate = MutableStateFlow(Calendar.getInstance())

    init {
        loadDashboardData()
    }
    
    fun previousMonth() {
        val cal = _selectedDate.value.clone() as Calendar
        cal.add(Calendar.MONTH, -1)
        _selectedDate.value = cal
    }
    
    fun nextMonth() {
        val cal = _selectedDate.value.clone() as Calendar
        cal.add(Calendar.MONTH, 1)
        _selectedDate.value = cal
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
            _selectedDate.flatMapLatest { calendar ->
                val cal = calendar.clone() as Calendar
                val formatter = SimpleDateFormat("MMMM yyyy", Locale("id", "ID"))
                val monthName = formatter.format(cal.time)
                
                val utcCal = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
                utcCal.clear()
                utcCal.set(Calendar.YEAR, cal.get(Calendar.YEAR))
                utcCal.set(Calendar.MONTH, cal.get(Calendar.MONTH))
                utcCal.set(Calendar.DAY_OF_MONTH, 1)
                val startOfMonth = utcCal.timeInMillis
                
                utcCal.set(Calendar.DAY_OF_MONTH, utcCal.getActualMaximum(Calendar.DAY_OF_MONTH))
                utcCal.set(Calendar.HOUR_OF_DAY, 23)
                utcCal.set(Calendar.MINUTE, 59)
                utcCal.set(Calendar.SECOND, 59)
                utcCal.set(Calendar.MILLISECOND, 999)
                val endOfMonth = utcCal.timeInMillis

                combine(
                    transactionDao.getTotalAmountByTypeAndDateRange(TransactionType.INCOME.name, startOfMonth, endOfMonth),
                    transactionDao.getTotalAmountByTypeAndDateRange(TransactionType.EXPENSE.name, startOfMonth, endOfMonth),
                    transactionDao.getTransactionsByDateRange(startOfMonth, endOfMonth),
                    categoryDao.getAllCategories()
                ) { income, expense, txs, allCategories ->
                    val totalIncome = income ?: 0L
                    val totalExpense = expense ?: 0L
                    val totalBalance = totalIncome - totalExpense
                    
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
                    
                    DashboardUiState(
                        totalIncome = totalIncome,
                        totalExpense = totalExpense,
                        totalBalance = totalBalance,
                        expenseChartData = chartDataList,
                        legendItems = legendList,
                        recentTransactions = recentList,
                        selectedMonthName = monthName
                    )
                }
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }
}
