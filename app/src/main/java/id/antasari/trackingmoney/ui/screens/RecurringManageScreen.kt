package id.antasari.trackingmoney.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import id.antasari.trackingmoney.data.model.Frequency
import id.antasari.trackingmoney.data.model.TransactionType
import id.antasari.trackingmoney.ui.theme.ExpenseColor
import id.antasari.trackingmoney.ui.theme.IncomeColor
import id.antasari.trackingmoney.ui.viewmodel.RecurringManageViewModel
import id.antasari.trackingmoney.ui.viewmodel.RecurringTransactionItemUiState
import id.antasari.trackingmoney.utils.CurrencyUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringManageScreen(
    onNavigateBack: () -> Unit,
    viewModel: RecurringManageViewModel = viewModel()
) {
    val recurringList by viewModel.recurringList.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transaksi Berulang", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (recurringList.isEmpty()) {
                item {
                    Text(
                        text = "Belum ada transaksi berulang.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }

            items(recurringList) { item ->
                RecurringItemRow(item = item, onDelete = { viewModel.deleteRecurring(item.recurring) })
            }
        }
    }
}

@Composable
fun RecurringItemRow(item: RecurringTransactionItemUiState, onDelete: () -> Unit) {
    val formatter = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
    val nextDateString = formatter.format(Date(item.recurring.nextDueDateMillis))

    val freqString = when (item.recurring.frequency) {
        Frequency.DAILY -> "Harian"
        Frequency.WEEKLY -> "Mingguan"
        Frequency.MONTHLY -> "Bulanan"
        Frequency.YEARLY -> "Tahunan"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp)),
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
                    text = "${item.recurring.note.ifBlank { "Tidak ada catatan" }} • $freqString",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Selanjutnya: $nextDateString",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                val isExpense = item.recurring.type == TransactionType.EXPENSE
                val amountColor = if (isExpense) ExpenseColor else IncomeColor
                val sign = if (isExpense) "-" else "+"
                
                Text(
                    text = "$sign${CurrencyUtils.formatRupiah(item.recurring.amount)}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = amountColor
                )
                
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
