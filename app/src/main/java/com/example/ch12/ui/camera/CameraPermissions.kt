
package com.example.ch12.ui.camera

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

// Fakta dari sistem Android
fun Context.hasCameraPermission(): Boolean =
    ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

fun Context.shouldShowCameraRationale(): Boolean =
    findActivity()?.let {
        ActivityCompat.shouldShowRequestPermissionRationale(
            it,
            Manifest.permission.CAMERA
        )
    } ?: false

fun Context.hasCameraHardware(): Boolean =
    packageManager.hasSystemFeature(
        PackageManager.FEATURE_CAMERA_ANY
    )

// Membuka halaman Pengaturan aplikasi
fun Context.openAppSettings() {
    startActivity(
        Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null)
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
}

private tailrec fun Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }

enum class PermissionAction {
    OpenCamera,
    ShowRationale,
    RequestPermission,
    ShowDenied,
    OpenSettings
}

fun cameraClickAction(
    hasPermission: Boolean,
    shouldShowRationale: Boolean
): PermissionAction =
    when {
        hasPermission ->
            PermissionAction.OpenCamera

        shouldShowRationale ->
            PermissionAction.ShowRationale

        else ->
            PermissionAction.RequestPermission
    }

fun permissionResultAction(
    isGranted: Boolean,
    shouldShowRationale: Boolean
): PermissionAction =
    when {
        isGranted ->
            PermissionAction.OpenCamera

        shouldShowRationale ->
            PermissionAction.ShowDenied

        else ->
            PermissionAction.OpenSettings
    }
