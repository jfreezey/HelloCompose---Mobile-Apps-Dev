data class Mahasiswa(
        val nim: String,
        val nama: String,
        var ipk: Double
    )

sealed class Hasil {
    data class Success(val data: List<Mahasiswa>) : Hasil()
    data class Error(val pesan: String) : Hasil()
}

fun main() {    
    val siswa = listOf (
        Mahasiswa("01082240001", "Charlie", 3.81),
        Mahasiswa("01082240005", "Bella", 3.75),
        Mahasiswa("01082240038", "Carmen", 3.37),
        Mahasiswa("01082240042", "Kazuha", 3.5),
        Mahasiswa("01082240012", "Giselle", 3.2)
    )

    val filter_ipk = siswa.filter {it.ipk >= 3.50}.sortedBy { it.nama }
    
    val hasil : Hasil = 
    if (filter_ipk.isNotEmpty()) {
        Hasil.Success(filter_ipk)
    } else {
        Hasil.Error("Tidak ada mahasiswa dengan IPK >= 3.5")
    }

    when (hasil){
        is Hasil.Success -> {
            hasil.data.forEach {
                println("${it.nama} - IPK: ${it.ipk}")
            }
        }
        is Hasil.Error -> println(hasil.pesan)
    }
}