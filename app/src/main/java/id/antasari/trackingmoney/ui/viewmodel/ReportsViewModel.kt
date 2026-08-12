package id.antasari.trackingmoney.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import id.antasari.trackingmoney.data.dao.CategorySum
import id.antasari.trackingmoney.data.dao.MonthlyTotal
import id.antasari.trackingmoney.data.db.AppDatabase
import id.antasari.trackingmoney.data.model.TransactionType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.map
import java.util.Calendar

enum class ReportViewMode {
    MONTHLY, YEARLY
}

data class ReportsUiState(
    val selectedDate: Calendar = Calendar.getInstance(),
    val viewMode: ReportViewMode = ReportViewMode.MONTHLY,
    val transactionType: TransactionType = TransactionType.EXPENSE,
    val categoryBreakdown: List<CategorySum> = emptyList(),
    val monthlyTrend: List<MonthlyTotal> = emptyList(),
    val isLoading: Boolean = false
)

class ReportsViewModel(application: Application) : AndroidViewModel(application) {
    private val reportDao = AppDatabase.getDatabase(application).reportDao()

    private val _selectedDate = MutableStateFlow(Calendar.getInstance())
    private val _viewMode = MutableStateFlow(ReportViewMode.MONTHLY)
    private val _transactionType = MutableStateFlow(TransactionType.EXPENSE)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<ReportsUiState> = combine(
        _selectedDate, _viewMode, _transactionType
    ) { date, mode, type ->
        Triple(date, mode, type)
    }.flatMapLatest { (date, mode, type) ->
        val (startMillis, endMillis) = getStartAndEndOfPeriod(date, mode)
        
        // We get trend for 6 months (if monthly mode) or 6 years (if yearly mode)
        // Wait, the trend chart spec says 6 months trend for bar chart. Let's stick to 6 months trend back from current date
        val (trendStartMillis, trendEndMillis) = getTrendPeriod(date, mode)

        val breakdownFlow = reportDao.getCategoryBreakdown(type, startMillis, endMillis)
        val trendFlow = reportDao.getMonthlyTrend(trendStartMillis, trendEndMillis)
        
        combine(breakdownFlow, trendFlow) { breakdown, trend ->
            ReportsUiState(
                selectedDate = date,
                viewMode = mode,
                transactionType = type,
                categoryBreakdown = breakdown,
                monthlyTrend = trend,
                isLoading = false
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ReportsUiState(isLoading = true)
    )

    private fun getStartAndEndOfPeriod(calendar: Calendar, mode: ReportViewMode): Pair<Long, Long> {
        val cal = calendar.clone() as Calendar
        if (mode == ReportViewMode.MONTHLY) {
            cal.set(Calendar.DAY_OF_MONTH, 1)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val start = cal.timeInMillis

            cal.add(Calendar.MONTH, 1)
            cal.add(Calendar.MILLISECOND, -1)
            val end = cal.timeInMillis
            return Pair(start, end)
        } else {
            cal.set(Calendar.DAY_OF_YEAR, 1)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val start = cal.timeInMillis

            cal.add(Calendar.YEAR, 1)
            cal.add(Calendar.MILLISECOND, -1)
            val end = cal.timeInMillis
            return Pair(start, end)
        }
    }

    private fun getTrendPeriod(calendar: Calendar, mode: ReportViewMode): Pair<Long, Long> {
        // We always show the last 6 months ending at the currently selected date's end
        val cal = calendar.clone() as Calendar
        if (mode == ReportViewMode.MONTHLY) {
            cal.set(Calendar.DAY_OF_MONTH, 1)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            cal.add(Calendar.MONTH, 1)
            cal.add(Calendar.MILLISECOND, -1)
            val end = cal.timeInMillis

            cal.add(Calendar.MILLISECOND, 1)
            cal.add(Calendar.MONTH, -6) // 6 months back
            val start = cal.timeInMillis
            return Pair(start, end)
        } else {
            // For yearly mode trend, we could show last 6 years, but our DAO query format is 'YYYY-MM'. 
            // The prompt says "Tren Bulanan". So even in yearly mode, the trend might show the 12 months of that year!
            // Let's do 12 months of the selected year for yearly mode.
            cal.set(Calendar.DAY_OF_YEAR, 1)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val start = cal.timeInMillis

            cal.add(Calendar.YEAR, 1)
            cal.add(Calendar.MILLISECOND, -1)
            val end = cal.timeInMillis
            return Pair(start, end)
        }
    }

    fun nextPeriod() {
        val current = _selectedDate.value.clone() as Calendar
        if (_viewMode.value == ReportViewMode.MONTHLY) {
            current.add(Calendar.MONTH, 1)
        } else {
            current.add(Calendar.YEAR, 1)
        }
        _selectedDate.value = current
    }

    fun previousPeriod() {
        val current = _selectedDate.value.clone() as Calendar
        if (_viewMode.value == ReportViewMode.MONTHLY) {
            current.add(Calendar.MONTH, -1)
        } else {
            current.add(Calendar.YEAR, -1)
        }
        _selectedDate.value = current
    }

    fun setViewMode(mode: ReportViewMode) {
        _viewMode.value = mode
    }

    fun setTransactionType(type: TransactionType) {
        _transactionType.value = type
    }
}
