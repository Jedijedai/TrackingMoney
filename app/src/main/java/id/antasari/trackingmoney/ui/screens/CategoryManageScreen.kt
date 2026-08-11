package id.antasari.trackingmoney.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import id.antasari.trackingmoney.data.model.Category
import id.antasari.trackingmoney.data.model.TransactionType
import id.antasari.trackingmoney.ui.theme.ExpenseColor
import id.antasari.trackingmoney.ui.theme.IncomeColor
import id.antasari.trackingmoney.ui.viewmodel.CategoryManageViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryManageScreen(
    onNavigateBack: () -> Unit,
    viewModel: CategoryManageViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<Category?>(null) }

    if (uiState.errorMessage != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearErrorMessage() },
            title = { Text("Peringatan") },
            text = { Text(uiState.errorMessage!!) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearErrorMessage() }) {
                    Text("OK")
                }
            }
        )
    }

    if (showDialog) {
        CategoryDialog(
            category = editingCategory,
            type = uiState.selectedType,
            onDismiss = {
                showDialog = false
                editingCategory = null
            },
            onSave = { savedCategory ->
                viewModel.saveCategory(savedCategory)
                showDialog = false
                editingCategory = null
            },
            onDelete = { categoryToDelete ->
                viewModel.deleteCategory(categoryToDelete)
                showDialog = false
                editingCategory = null
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kelola Kategori", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingCategory = null
                    showDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Kategori")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
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

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(uiState.categories) { category ->
                    ManageCategoryItem(
                        category = category,
                        onClick = {
                            editingCategory = category
                            showDialog = true
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ManageCategoryItem(
    category: Category,
    onClick: () -> Unit
) {
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
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = category.icon, fontSize = 28.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = category.name,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDialog(
    category: Category?,
    type: TransactionType,
    onDismiss: () -> Unit,
    onSave: (Category) -> Unit,
    onDelete: (Category) -> Unit
) {
    var name by remember { mutableStateOf(category?.name ?: "") }
    var icon by remember { mutableStateOf(category?.icon ?: "") }

    val isEdit = category != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(if (isEdit) "Edit Kategori" else "Tambah Kategori")
                if (isEdit) {
                    IconButton(onClick = { onDelete(category!!) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = icon,
                    onValueChange = { icon = it },
                    label = { Text("Ikon (Emoji)") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Kategori") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && icon.isNotBlank()) {
                        onSave(
                            Category(
                                id = category?.id ?: 0,
                                name = name,
                                icon = icon,
                                type = type
                            )
                        )
                    }
                },
                enabled = name.isNotBlank() && icon.isNotBlank()
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
