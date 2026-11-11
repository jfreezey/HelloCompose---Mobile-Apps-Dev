package com.example.ch12.data.photo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import com.example.ch12.util.PROFILE_PHOTO_QUALITY
import com.example.ch12.util.PROFILE_PHOTO_SIZE
import com.example.ch12.util.calculateInSampleSize
import com.example.ch12.util.centerSquare
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream
import javax.inject.Inject

// Foto 4000x3000 -> JPEG persegi 400x400, sekitar 30-60 KB
class AndroidImageCompressor @Inject constructor(
    @ApplicationContext private val context: Context
) : ImageCompressor {

    // Decode dan kompres JPEG adalah kerja CPU + I/O: jangan di main thread
    override suspend fun compress(uri: String): CompressedImage = withContext(Dispatchers.Default) {
        val source = Uri.parse(uri)

        // 1. Dekode Tahap 1: Ambil hanya ukuran gambar (bounds) tanpa memuat piksel ke RAM
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        open(source).use { stream ->
            BitmapFactory.decodeStream(stream, null, bounds)
        }

        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            throw ImageDecodeException("Berkas yang dipilih bukan gambar.")
        }

        // 2. Dekode Tahap 2: Sub-sampling menggunakan inSampleSize (Stream baru)
        val sampleOptions = BitmapFactory.Options().apply {
            inSampleSize = calculateInSampleSize(
                width = bounds.outWidth,
                height = bounds.outHeight,
                reqSize = PROFILE_PHOTO_SIZE
            )
        }

        val sampledBitmap = open(source).use { stream ->
            BitmapFactory.decodeStream(stream, null, sampleOptions)
        } ?: throw ImageDecodeException("Gagal mendekode gambar.")

        // 3. Potong Persegi di Tengah (Center Square Crop) & Terapkan Transformasi Matrix
        val crop = centerSquare(sampledBitmap.width, sampledBitmap.height)
        val scale = minOf(1f, PROFILE_PHOTO_SIZE.toFloat() / crop.size)
        val rotation = readRotation(source).toFloat()

        val matrix = Matrix().apply {
            postScale(scale, scale)
            postRotate(rotation)
        }

        val finalBitmap = Bitmap.createBitmap(
            sampledBitmap,
            crop.left,
            crop.top,
            crop.size,
            crop.size,
            matrix,
            true
        )

        // Lepas memory bitmap awal jika bitmap baru dibuat hasil transformasi
        if (sampledBitmap != finalBitmap) {
            sampledBitmap.recycle()
        }

        // 4. Kompresi ke format JPEG (Output ByteArray)
        val out = ByteArrayOutputStream()
        finalBitmap.compress(Bitmap.CompressFormat.JPEG, PROFILE_PHOTO_QUALITY, out)
        val bytes = out.toByteArray()

        val resultWidth = finalBitmap.width
        val resultHeight = finalBitmap.height

        // 5. Bersihkan bitmap hasil dan kembalikan CompressedImage
        finalBitmap.recycle()

        CompressedImage(
            bytes = bytes,
            originalWidth = bounds.outWidth,
            originalHeight = bounds.outHeight,
            width = resultWidth,
            height = resultHeight
        )
    }

    // content:// (Photo Picker) maupun file:// (CameraX) dibuka lewat ContentResolver
    private fun open(uri: Uri): InputStream =
        context.contentResolver.openInputStream(uri)
            ?: throw ImageDecodeException("Gambar tidak dapat dibuka.")

    // Kamera menyimpan piksel apa adanya + tag orientasi. BitmapFactory
    // mengabaikan tag itu, sehingga tanpa ini foto potret bisa tampil miring.
    private fun readRotation(uri: Uri): Int = runCatching {
        open(uri).use { ExifInterface(it).rotationDegrees }
    }.getOrDefault(0)
}