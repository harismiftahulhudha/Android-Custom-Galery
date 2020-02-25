package id.haris.galleryapplication

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity";

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun openGallery(view: View) {
        val permissionHelper = PermissionHelper(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (permissionHelper.hasAccessMediaLocation()) {
                startGallery()
            } else {
                permissionHelper.requestAccessMediaLocation()
            }
        } else {
            if (permissionHelper.hasReadAndWriteStoragePermission()) {
                startGallery()
            } else {
                permissionHelper.requestReadAndWriteStoragePermission()
            }
        }
    }

    fun startGallery() {
        Log.d(TAG, "openGallery: open gallery")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            val chooserIntent = Intent.createChooser(intent, "getImage")
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivityForResult(chooserIntent, 1)
        } else {
            val intent = Intent(this, CustomGalleryActivity::class.java)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivityForResult(intent, 1)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 1) {
            if (grantResults.size == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "onRequestPermissionsResult: 2 permission granted")
                startGallery()
            } else if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "onRequestPermissionsResult: 1 permission granted")
                startGallery()
            } else {
                Log.d(TAG, "onRequestPermissionsResult: permission denied")
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {

            }
        }
    }
}
