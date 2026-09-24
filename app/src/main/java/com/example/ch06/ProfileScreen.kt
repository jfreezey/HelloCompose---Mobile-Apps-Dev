package com.example.ch06

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ch06.profile.ProfileUiState
import com.example.ch06.profile.ProfileViewModel
import com.example.ch06.profile.ProfileViewModelFactory

@Composable
fun ProfileRoute() {
    val app = LocalContext.current.applicationContext as MainApplication
    val viewModel: ProfileViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = ProfileViewModelFactory(app.container.profileRepository)
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ProfileScreen(
        uiState = uiState,
        onUsernameChange = viewModel::onUsernameChange,
        onToggleNotification = viewModel::onToggleNotification,
        onToggleEdit = viewModel::toggleEditMode
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    uiState: ProfileUiState,
    onUsernameChange: (String) -> Unit,
    onToggleNotification: (Boolean) -> Unit,
    onToggleEdit: () -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Profil") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.AccountCircle,
                contentDescription = null,
                modifier = Modifier.size(96.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(16.dp))

            // kondisi untuk tampilan saat edit atau tidak
            if (!uiState.isEditing) {
                // tampilan awal
                Text(
                    text = uiState.username,
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "mahasiswa@kampus.ac.id", // Atau NIM kamu
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Notifikasi: ${if (uiState.notificationsEnabled) "Aktif" else "Nonaktif"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                // tampilan saat editing
                OutlinedTextField(
                    value = uiState.username,
                    onValueChange = onUsernameChange,
                    label = { Text("Ubah Nama Pengguna") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Aktifkan Notifikasi", style = MaterialTheme.typography.bodyLarge)
                    Switch(
                        checked = uiState.notificationsEnabled,
                        onCheckedChange = onToggleNotification
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // tombol edit profil dan simpan perubahan
            Button(
                onClick = onToggleEdit,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (uiState.isEditing) "Simpan Perubahan" else "Edit Profil")
            }
        }
    }
}
