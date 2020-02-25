package id.haris.galleryapplication

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionHelper(private val context: Context) {

    companion object {
        val REQUEST_READ_WRITE_EXTERNAL_STORAGE = 1
        val REQUEST_ACCESS_MEDIA_LOCATION = 2
    }

    fun isGreaterThenLollipop(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M

    fun hasReadAndWriteStoragePermission(): Boolean {
        if (!isGreaterThenLollipop()) return true
        val bol: Boolean = (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        return bol
    }

    fun requestReadAndWriteStoragePermission() {
        if (!isGreaterThenLollipop()) return
        if (hasReadAndWriteStoragePermission()) return
        ActivityCompat.requestPermissions((context as Activity), arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_READ_WRITE_EXTERNAL_STORAGE)
    }

    fun isGreaterThenQ(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

    fun hasAccessMediaLocation(): Boolean {
        if (isGreaterThenQ()) {
            return (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_MEDIA_LOCATION) == PackageManager.PERMISSION_GRANTED)
        } else {
            return true
        }
    }

    fun requestAccessMediaLocation() {
        if (!isGreaterThenQ()) return
        if (hasAccessMediaLocation()) return
        ActivityCompat.requestPermissions((context as Activity), arrayOf(Manifest.permission.ACCESS_MEDIA_LOCATION), REQUEST_ACCESS_MEDIA_LOCATION)
    }
}