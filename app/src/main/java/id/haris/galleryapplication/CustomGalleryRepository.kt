package id.haris.galleryapplication

import android.annotation.SuppressLint
import android.app.Application
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

class CustomGalleryRepository(private val application: Application) {
    private val TAG = "CustomGalleryRepository";

    private val updatedFile: MutableLiveData<CustomGallery> = MutableLiveData()
    private val files: MutableLiveData<List<CustomGallery>> = MutableLiveData()
    private val folders: MutableLiveData<List<CustomGalleryFolder>> = MutableLiveData()
    private val folderNames: MutableLiveData<List<String>> = MutableLiveData()

    val folderTitles: MutableList<String> = mutableListOf()

    fun updateFile(file: CustomGallery) {
        updatedFile.postValue(file)
    }

    fun getUpdatedFile(): LiveData<CustomGallery> = updatedFile
    fun getFiles(): LiveData<List<CustomGallery>> = files
    fun getFolders(): LiveData<List<CustomGalleryFolder>> = folders
    fun getFolderNames(): LiveData<List<String>> = folderNames

    fun getFiles(type: Int, bucketId: String, selectedFiles: List<String> = ArrayList()) {
        val file: MutableList<CustomGallery> = ArrayList()
        GlobalScope.launch {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Belum Tau
            } else {
                val uri = MediaStore.Files.getContentUri("external")
                val projection = arrayOf(
                    MediaStore.Files.FileColumns._ID,
                    MediaStore.Files.FileColumns.DATE_ADDED,
                    MediaStore.Files.FileColumns.MEDIA_TYPE,
                    MediaStore.Files.FileColumns.MIME_TYPE,
                    MediaStore.Files.FileColumns.TITLE,
                    MediaStore.Files.FileColumns.PARENT,
                    MediaStore.Files.FileColumns.DISPLAY_NAME,
                    OpenableColumns.SIZE
                )

                val selectImage = "${MediaStore.Files.FileColumns.MEDIA_TYPE}=${MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE} AND ${MediaStore.Files.FileColumns.BUCKET_ID}='$bucketId'"
                val selectVideo = "${MediaStore.Files.FileColumns.MEDIA_TYPE}=${MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO} AND ${MediaStore.Files.FileColumns.MIME_TYPE}='video/mp4' AND ${MediaStore.Files.FileColumns.BUCKET_ID}='$bucketId'"
                var selection: String? = null
                if (type == 3) {
                    selection = "${selectImage} OR ${selectVideo}"
                }
                val cursor = application.contentResolver.query(
                    uri,
                    projection,
                    selection,
                    null,
                    "${MediaStore.Files.FileColumns.DATE_ADDED} DESC"
                )
                if (cursor != null) {
                    val columnIndexID = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                    val columnIndexName = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
                    val columnIndexMediaType = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)
                    val columnIndexSize = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)

                    while (cursor.moveToNext()) {
                        val imageId = cursor.getLong(columnIndexID)
                        val name = cursor.getString(columnIndexName)
                        val mimeType = cursor.getString(columnIndexMediaType)
                        val size = cursor.getLong(columnIndexSize)
                        val uriImage = Uri.withAppendedPath(uri, "" + imageId)
                        var duration: String? = null
                        val type: String
                        when {
                            mimeType.equals("video/mp4") -> {
                                type = "video"
                                duration = getDuration(imageId.toString())
                            }
                            else -> {
                                type = "image"
                            }
                        }
                        val select = when {
                            selectedFiles.size > 0 && selectedFiles.indexOf(uriImage.toString()) != -1 -> {
                                true
                            }
                            else -> {
                                false
                            }
                        }
                        val customGallery = CustomGallery(imageId.toString(), uriImage.toString(), type, duration, select)
                        if (type.equals("video")) {
                            val fileSizeInBytes = size
                            val fileSizeInKB = fileSizeInBytes / 1024
                            val fileSizeInMB = fileSizeInKB / 1024
                            if (fileSizeInMB <= 10) {
                                file.add(customGallery)
                            }
                        } else {
                            file.add(customGallery)
                        }
                    }
                    cursor.close()
                }

                files.postValue(file)
            }
        }
    }
    fun getFolders(type: Int) {
        Log.d(TAG, "getFolders: type: " + type)

        /**
         *
         * GET IMAGE FOLDER
         */
        if (type == 3) {
            GlobalScope.launch {
                folderTitles.clear()

                val fileFolders: MutableList<CustomGalleryFolder> = ArrayList()
                fileFolders.addAll(getFolderVideo(getFolderImage()))

                Collections.sort(folderTitles)

                folders.postValue(fileFolders)
                folderNames.postValue(folderTitles)
            }
        }
    }
    private fun getFolderImage(): MutableList<CustomGalleryFolder> {
        val fileFolders: MutableList<CustomGalleryFolder> = ArrayList()


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Belum Tau

            return fileFolders
        } else {
            val uriExternal = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val projection = arrayOf(
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Images.Media.BUCKET_ID,
                "COUNT(${MediaStore.Images.Media._ID}) as total"
            )
            val selection = " 1=1 ) GROUP BY (${MediaStore.Images.Media.BUCKET_ID}"

            val cursor: Cursor?

            cursor = application.contentResolver.query(uriExternal, projection, selection, null, null)
            if (cursor != null) {
                val columnIndexBucket =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
                val columnIndexBucketId =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID)
                val columnIndexTotal = cursor.getColumnIndexOrThrow("total")
                while (cursor.moveToNext()) {
                    val bucket = cursor.getString(columnIndexBucket)
                    val bucketId = cursor.getString(columnIndexBucketId)
                    val total = cursor.getInt(columnIndexTotal)
                    val folder = CustomGalleryFolder(bucketId, bucket, total)
                    fileFolders.add(folder)
                    folderTitles.add(bucket)
                }
                cursor.close()
            }

            return fileFolders
        }
    }
    private fun getFolderVideo(fileFolders: MutableList<CustomGalleryFolder> = ArrayList()): MutableList<CustomGalleryFolder> {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Belum Tau

            return fileFolders
        } else {
            val uriExternal = MediaStore.Video.Media.EXTERNAL_CONTENT_URI

            val projection = arrayOf(
                MediaStore.Video.VideoColumns.BUCKET_DISPLAY_NAME,
                MediaStore.Video.VideoColumns.BUCKET_ID,
                "COUNT(${MediaStore.Video.VideoColumns._ID}) as total"
            )
            val where = "${MediaStore.Video.VideoColumns.MIME_TYPE}='video/mp4'"
            val orderBy = "${MediaStore.Video.VideoColumns.DATE_TAKEN} DESC"

            val cursor: Cursor?
            cursor = application.contentResolver.query(uriExternal, projection, where, null, orderBy)
            if (cursor != null) {
                val columnIndexBucket =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
                val columnIndexBucketId =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID)
                val columnIndexTotal = cursor.getColumnIndexOrThrow("total")
                val countFolders = fileFolders.size
                while (cursor.moveToNext()) {
                    val bucket = cursor.getString(columnIndexBucket)
                    val bucketId = cursor.getString(columnIndexBucketId)
                    val total = cursor.getInt(columnIndexTotal)
                    val folder = CustomGalleryFolder(bucketId, bucket, total)
                    if (countFolders > 0) {
                        if (fileFolders.indexOf(folder) == -1) {
                            fileFolders.add(folder)
                            folderTitles.add(bucket)
                        }
                    } else {
                        fileFolders.add(folder)
                        folderTitles.add(bucket)
                    }

                }
                cursor.close()
            }

            return fileFolders
        }
    }

    @SuppressLint("InlinedApi")
    private fun getDuration(id: String): String? {
        var result: String? = null
        try {
            val uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            val projection = arrayOf(
                MediaStore.Video.VideoColumns._ID,
                MediaStore.Video.VideoColumns.DURATION,
                MediaStore.Video.VideoColumns.SIZE,
                MediaStore.Video.VideoColumns.MIME_TYPE
            )
            val whereClause = "${MediaStore.Video.VideoColumns._ID}='${id}'"
            val orderBy = MediaStore.Video.Media.DATE_TAKEN
            val cursor = application.contentResolver.query(
                uri,
                projection,
                whereClause,
                null,
                "$orderBy DESC"
            )
            if (cursor != null) {
                cursor.moveToFirst()
                result = formatMilliSeccond(Integer.valueOf(cursor.getString(1)))
            }
        } catch (e: NumberFormatException) {
            e.printStackTrace()
            result = "00:00"
        }
        return result
    }
    private fun formatMilliSeccond(milliseconds: Int): String {
        var finalTimerString = ""
        val minut: String
        val secondsString: String
        val hours = milliseconds / (1000 * 60 * 60)
        val minutes = milliseconds % (1000 * 60 * 60) / (1000 * 60)
        val seconds = milliseconds % (1000 * 60 * 60) % (1000 * 60) / 1000
        if (hours > 0) {
            finalTimerString = "$hours:"
        }
        minut = if (minutes < 10) {
            "0$minutes"
        } else {
            minutes.toString()
        }
        secondsString = if (seconds < 10) {
            "0$seconds"
        } else {
            "" + seconds
        }
        finalTimerString = "$finalTimerString$minut:$secondsString"
        return finalTimerString
    }
}