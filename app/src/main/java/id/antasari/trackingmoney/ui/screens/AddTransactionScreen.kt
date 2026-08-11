package id.antasari.trackingmoney.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.AnnotatedString
import id.antasari.trackingmoney.utils.CurrencyUtils
import id.antasari.trackingmoney.data.model.Category
import id.antasari.trackingmoney.data.model.Frequency
import id.antasari.trackingmoney.data.model.TransactionType
import id.antasari.trackingmoney.ui.theme.ExpenseColor
import id.antasari.trackingmoney.ui.theme.ExpenseColor
import id.antasari.trackingmoney.ui.theme.IncomeColor
import id.antasari.trackingmoney.ui.viewmodel.AddTransactionViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    transactionId: Int? = null,
    onNavigateBack: () -> Unit,
    viewModel: AddTransactionViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(transactionId) {
        if (transactionId != null) {
            viewModel.loadTransaction(transactionId)
        }
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onNavigateBack()
        }
    }

    var showDatePicker by remember { mutableStateOf(false) }
    
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.dateMillis
        )
        
        LaunchedEffect(uiState.dateMillis) {
            datePickerState.selectedDateMillis = uiState.dateMillis
        }

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { viewModel.setDate(it) }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Batal")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (transactionId != null) "Edit Transaksi" else "Tambah Transaksi", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (transactionId != null) {
                        IconButton(onClick = { viewModel.deleteTransaction() }) {
                            Icon(Icons.Default.Delete, contentDescription = "Hapus Transaksi", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            
            // Transaction Type Tabs
            val types = listOf(TransactionType.EXPENSE to "Pengeluaran", TransactionType.INCOME to "Pemasukan")
            TabRow(
                selectedTabIndex = types.indexOfFirst { it.first == uiState.selectedType },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    if (uiState.selectedType == TransactionType.EXPENSE) {
                        TabRowDefaults.SecondaryIndicator(Modifier.tabIndicatorOffset(tabPositions[0]), color = ExpenseColor)
                    } else {
                        TabRowDefaults.SecondaryIndicator(Modifier.tabIndicatorOffset(tabPositions[1]), color = IncomeColor)
                    }
                }
            ) {
                types.forEach { (type, label) ->
                    val isSelected = uiState.selectedType == type
                    val color = if (isSelected) {
                        if (type == TransactionType.EXPENSE) ExpenseColor else IncomeColor
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    
                    Tab(
                        selected = isSelected,
                        onClick = { viewModel.setTransactionType(type) },
                        text = {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = color
                            )
                        }
                    )
                }
            }

            // Date Picker Field
            val formatter = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")).apply {
                timeZone = java.util.TimeZone.getTimeZone("UTC")
            }
            val dateStr = formatter.format(Date(uiState.dateMillis))
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = dateStr,
                    onValueChange = { },
                    label = { Text("Tanggal") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = true
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .semantics {
                            contentDescription = "Pilih tanggal, saat ini $dateStr"
                        }
                        .clickable(role = Role.Button) { showDatePicker = true }
                )
            }

            // Amount Input
            OutlinedTextField(
                value = uiState.amount,
                onValueChange = { viewModel.setAmount(it) },
                label = { Text("Nominal (Rp)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                visualTransformation = CurrencyVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )

            // Note Input
            OutlinedTextField(
                value = uiState.note,
                onValueChange = { viewModel.setNote(it) },
                label = { Text("Catatan") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )

            // Recurring Options
            if (transactionId == null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Jadikan Transaksi Berulang",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Switch(
                                checked = uiState.isRecurring,
                                onCheckedChange = { viewModel.setIsRecurring(it) }
                            )
                        }

                        if (uiState.isRecurring) {
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            var expanded by remember { mutableStateOf(false) }
                            
                            ExposedDropdownMenuBox(
                                expanded = expanded,
                                onExpandedChange = { expanded = it }
                            ) {
                                OutlinedTextField(
                                    value = when (uiState.recurringFrequency) {
                                        Frequency.DAILY -> "Harian"
                                        Frequency.WEEKLY -> "Mingguan"
                                        Frequency.MONTHLY -> "Bulanan"
                                        Frequency.YEARLY -> "Tahunan"
                                    },
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Frekuensi") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                    modifier = Modifier.menuAnchor().fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Harian") },
                                        onClick = { viewModel.setRecurringFrequency(Frequency.DAILY); expanded = false }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Mingguan") },
                                        onClick = { viewModel.setRecurringFrequency(Frequency.WEEKLY); expanded = false }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Bulanan") },
                                        onClick = { viewModel.setRecurringFrequency(Frequency.MONTHLY); expanded = false }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Tahunan") },
                                        onClick = { viewModel.setRecurringFrequency(Frequency.YEARLY); expanded = false }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Categories Selection
            Text(
                text = "Pilih Kategori",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.heightIn(max = 250.dp) // bounded height for scroll
            ) {
                items(uiState.categories) { category ->
                    CategoryItem(
                        category = category,
                        isSelected = uiState.selectedCategory?.id == category.id,
                        onClick = { viewModel.setCategory(category) }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Save Button
            val amountVal = uiState.amount.toLongOrNull() ?: 0L
            Button(
                onClick = { viewModel.saveTransaction() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (uiState.selectedType == TransactionType.EXPENSE) ExpenseColor else IncomeColor
                ),
                enabled = !uiState.isSaving && !uiState.isSaved && amountVal > 0 && uiState.selectedCategory != null
            ) {
                Text("Simpan", style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}

@Composable
fun CategoryItem(
    category: Category,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(bgColor, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = category.icon, fontSize = 28.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = category.name,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
    }
}

class CurrencyVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text
        if (originalText.isEmpty()) return TransformedText(text, OffsetMapping.Identity)

        val parsed = originalText.toLongOrNull() ?: 0L
        val formatted = CurrencyUtils.formatRupiah(parsed)

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                return formatted.length
            }

            override fun transformedToOriginal(offset: Int): Int {
                return originalText.length
            }
        }
        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}
