package com.example.ch03

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ch03.ui.theme.Ch03Theme
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.material3.FilterChip

data class Mahasiswa(val nama: String, val nim: String, val ipk: Double)

// daftar list mahasiswa
val dummyMahasiswa = listOf<Mahasiswa>(
    Mahasiswa("Ali Rahman", "22001", 3.85),
    Mahasiswa("Budi Santoso", "22002", 3.40),
    Mahasiswa("Cici Wulandari", "22003", 3.92),
    Mahasiswa("Dian Pratama", "22004", 2.95),
    Mahasiswa("Eka Fitriani", "22005", 3.75),
    Mahasiswa("Fandi Ahmad", "22006", 3.50),
    Mahasiswa("Gita Permata", "22007", 3.88),
    Mahasiswa("Hendra Kusuma", "22008", 2.80),
    Mahasiswa("Indah Lestari", "22009", 3.65),
    Mahasiswa("Joko Pratama", "22010", 3.20),

    Mahasiswa("Bunga Lestari", "22011", 3.47),
    Mahasiswa("Mary Santoso", "22012", 3.69),
    Mahasiswa("Budi Utomo", "22013", 3.93),
    Mahasiswa("Dilan Milea", "22014", 2.64),
    Mahasiswa("Jasmine", "22015", 3.15),
    Mahasiswa("Muhammad Ramdan", "22016", 2.32),
    Mahasiswa("Jared Kim", "22017", 3.99),
    Mahasiswa("Indri Ratih", "22018", 2.74),
    Mahasiswa("Putri Cantika", "22019", 3.55),
    Mahasiswa("Judika Pratama", "22020", 3.58)
)

@Composable
fun StudentListScreen() {
    // TODO 2: Panggil DaftarMahasiswa dengan dummyMahasiswa.
    DaftarMahasiswa(mahasiswaList = dummyMahasiswa)
}

@Composable
fun DaftarMahasiswa(
    mahasiswaList: List<Mahasiswa>,
    modifier: Modifier = Modifier
) {
    // daftar button filtering
    val kategori = listOf("Semua", "IPK ≥ 3.5", "IPK < 3.5")

    // tombol aktif
    var kategori_terseleksi by remember { mutableStateOf("Semua") }

    // logika filtering list
    val filteredList = when (kategori_terseleksi) {
        "IPK ≥ 3.5" -> mahasiswaList.filter { it.ipk >= 3.5 }
        "IPK < 3.5" -> mahasiswaList.filter { it.ipk < 3.5 }
        else -> mahasiswaList
    }

    // lazyColumn
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        //LazyRow (filtering komponen kanan, kiri)
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                items(kategori.size) { index ->
                    val klik_kategori = kategori[index]
                    FilterChip(
                        selected = (klik_kategori == kategori_terseleksi),
                        onClick = { kategori_terseleksi = klik_kategori},
                        label = { Text(klik_kategori) }
                    )
                }
            }
        }
        //header
        item {
            Text(
                text = "Angkatan 2024",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        // menampilkan daftar mahasiswa terfilter
        items(
            items = filteredList,
            key = { it.nim }
        ) { mahasiswa ->
            MahasiswaCard(mahasiswa)
        }

        item {
            Text(
                text = "Total Mahasiswa: ${filteredList.size}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

    }
}

@Composable
fun MahasiswaCard(mahasiswa: Mahasiswa) {
    // Buat Card berisi nama, NIM, dan IPK.
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column (
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = mahasiswa.nama,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = mahasiswa.nim,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "IPK ${mahasiswa.ipk}",
                style = MaterialTheme.typography.labelLarge,
                color = if (mahasiswa.ipk >= 3.5) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun StudentListScreenPreview() {
    Ch03Theme {
        StudentListScreen()
    }
}
