package id.antasari.trackingmoney.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import id.antasari.trackingmoney.data.dao.CategorySum
import id.antasari.trackingmoney.data.model.TransactionType
import id.antasari.trackingmoney.ui.components.CategoryDonutChart
import id.antasari.trackingmoney.ui.components.TrendBarChart
import id.antasari.trackingmoney.ui.theme.ChartColors
import id.antasari.trackingmoney.ui.theme.ExpenseColor
import id.antasari.trackingmoney.ui.theme.IncomeColor
import id.antasari.trackingmoney.ui.viewmodel.ReportViewMode
import id.antasari.trackingmoney.ui.viewmodel.ReportsViewModel
import id.antasari.trackingmoney.utils.CurrencyUtils
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: ReportsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.Builder().setLanguage("id").setRegion("ID").build())
    val yearFormat = SimpleDateFormat("yyyy", Locale.Builder().setLanguage("id").setRegion("ID").build())
    val periodLabel = if (uiState.viewMode == ReportViewMode.MONTHLY) {
        monthFormat.format(uiState.selectedDate.time)
    } else {
        yearFormat.format(uiState.selectedDate.time)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Laporan Keuangan", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)
        ) {
            item {
                // Period Navigator
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.previousPeriod() }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Sebelumnya", tint = MaterialTheme.colorScheme.primary)
                    }
                    Text(
                        text = periodLabel,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    IconButton(onClick = { viewModel.nextPeriod() }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Selanjutnya", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            item {
                // View Mode Tabs (Bulan / Tahun)
                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                    TabButton(
                        text = "Bulanan",
                        isSelected = uiState.viewMode == ReportViewMode.MONTHLY,
                        onClick = { viewModel.setViewMode(ReportViewMode.MONTHLY) },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TabButton(
                        text = "Tahunan",
                        isSelected = uiState.viewMode == ReportViewMode.YEARLY,
                        onClick = { viewModel.setViewMode(ReportViewMode.YEARLY) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                // Type Tabs (Pengeluaran / Pemasukan)
                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
                    TabButton(
                        text = "Pengeluaran",
                        isSelected = uiState.transactionType == TransactionType.EXPENSE,
                        onClick = { viewModel.setTransactionType(TransactionType.EXPENSE) },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TabButton(
                        text = "Pemasukan",
                        isSelected = uiState.transactionType == TransactionType.INCOME,
                        onClick = { viewModel.setTransactionType(TransactionType.INCOME) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (uiState.isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            } else if (uiState.categoryBreakdown.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🤷‍♂️", fontSize = 64.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Belum ada transaksi di periode ini",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                item {
                    // Category Breakdown Section
                    Text(
                        text = "Ringkasan per Kategori",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CategoryDonutChart(
                            data = uiState.categoryBreakdown,
                            colors = ChartColors,
                            modifier = Modifier.size(200.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }

                items(uiState.categoryBreakdown.size) { index ->
                    val item = uiState.categoryBreakdown[index]
                    val color = ChartColors[index % ChartColors.size]
                    val totalForType = uiState.categoryBreakdown.sumOf { it.totalAmount }
                    val percentage = if (totalForType > 0) (item.totalAmount.toFloat() / totalForType) * 100 else 0f
                    
                    CategoryLegendItem(item = item, color = color, percentage = percentage)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
            
            if (!uiState.isLoading && uiState.monthlyTrend.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                    // Monthly Trend Section
                    Text(
                        text = "Tren Waktu",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    TrendBarChart(
                        data = uiState.monthlyTrend,
                        expenseColor = ExpenseColor,
                        incomeColor = IncomeColor,
                        modifier = Modifier.fillMaxWidth().height(200.dp)
                    )
                    
                    // Simple legend for Bar Chart
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(), 
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(4.dp)).background(ExpenseColor))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Pengeluaran", style = MaterialTheme.typography.labelMedium)
                        Spacer(modifier = Modifier.width(24.dp))
                        Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(4.dp)).background(IncomeColor))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Pemasukan", style = MaterialTheme.typography.labelMedium)
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun TabButton(text: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.buttonColors(containerColor = containerColor, contentColor = contentColor),
        shape = RoundedCornerShape(12.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = if (isSelected) 4.dp else 0.dp)
    ) {
        Text(text = text, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun CategoryLegendItem(item: CategorySum, color: Color, percentage: Float) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(color.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = item.categoryIcon, fontSize = 24.sp)
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.categoryName,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = String.format(Locale.US, "%.1f%%", percentage),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Text(
            text = CurrencyUtils.formatRupiah(item.totalAmount),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
