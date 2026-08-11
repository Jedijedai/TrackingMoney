package id.antasari.trackingmoney.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import id.antasari.trackingmoney.data.preferences.ThemeMode
import id.antasari.trackingmoney.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCategoryManage: () -> Unit,
    onNavigateToRecurringManage: () -> Unit,
    onNavigateToProfile: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val themeMode by viewModel.themeMode.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pengaturan", fontWeight = FontWeight.Bold) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            // Profile Section
            SettingsSectionHeader(title = "Profil & Akun")
            
            ListItem(
                headlineContent = { Text("Pengaturan Profil") },
                supportingContent = { Text("Ubah nama dan info pribadi") },
                leadingContent = {
                    Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
                trailingContent = {
                    Icon(Icons.Default.ArrowForward, contentDescription = null)
                },
                modifier = Modifier.clickable { onNavigateToProfile() }
            )
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Theme Setting Section
            SettingsSectionHeader(title = "Preferensi Tampilan")
            
            ThemeSelectionItem(
                currentMode = themeMode,
                onModeSelected = { viewModel.setThemeMode(it) }
            )
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Categories Setting Section
            SettingsSectionHeader(title = "Manajemen Data")
            
            ListItem(
                headlineContent = { Text("Kategori Transaksi") },
                supportingContent = { Text("Tambah, ubah, atau hapus kategori") },
                leadingContent = {
                    Icon(Icons.Default.List, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
                trailingContent = {
                    Icon(Icons.Default.ArrowForward, contentDescription = null)
                },
                modifier = Modifier.clickable { onNavigateToCategoryManage() }
            )
            
            ListItem(
                headlineContent = { Text("Transaksi Berulang (Langganan)") },
                supportingContent = { Text("Kelola tagihan yang berjalan otomatis") },
                leadingContent = {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
                trailingContent = {
                    Icon(Icons.Default.ArrowForward, contentDescription = null)
                },
                modifier = Modifier.clickable { onNavigateToRecurringManage() }
            )
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            // About Section
            SettingsSectionHeader(title = "Tentang Aplikasi")
            
            ListItem(
                headlineContent = { Text("Versi Aplikasi") },
                supportingContent = { Text("v1.0.0") },
                leadingContent = {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            )
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
    )
}

@Composable
fun ThemeSelectionItem(
    currentMode: ThemeMode,
    onModeSelected: (ThemeMode) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        ListItem(
            headlineContent = { Text("Tema Aplikasi") },
            leadingContent = {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ThemeOption(
                label = "Ikuti Sistem",
                isSelected = currentMode == ThemeMode.SYSTEM,
                onClick = { onModeSelected(ThemeMode.SYSTEM) }
            )
            ThemeOption(
                label = "Terang",
                isSelected = currentMode == ThemeMode.LIGHT,
                onClick = { onModeSelected(ThemeMode.LIGHT) }
            )
            ThemeOption(
                label = "Gelap",
                isSelected = currentMode == ThemeMode.DARK,
                onClick = { onModeSelected(ThemeMode.DARK) }
            )
        }
    }
}

@Composable
fun ThemeOption(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(label) }
    )
}
