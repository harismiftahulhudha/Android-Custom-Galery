package id.haris.galleryapplication

import android.annotation.SuppressLint
import android.app.Application
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class CustomGalleryRepository(private val application: Application) {
    private val TAG = "CustomGalleryRepository";

    private val files: MutableLiveData<MutableList<CustomGallery>> = MutableLiveData()
    private val folders: MutableLiveData<MutableList<CustomGalleryFolder>> = MutableLiveData()
    private val folderNames: MutableLiveData<MutableList<String>> = MutableLiveData()

    val folderTitles: MutableList<String> = mutableListOf()

    fun getFiles(): LiveData<MutableList<CustomGallery>> = files
    fun getFolders(): LiveData<MutableList<CustomGalleryFolder>> = folders
    fun getFolderNames(): LiveData<MutableList<String>> = folderNames

    @SuppressLint("Recycle")
    fun getFiles(type: Int, bucket: String, selectedFiles: MutableList<String> = mutableListOf()) {
        val file: MutableList<CustomGallery> = ArrayList()
        GlobalScope.launch {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val uri = MediaStore.Files.getContentUri("external")
                val projection = arrayOf(
                    MediaStore.Files.FileColumns._ID,
                    MediaStore.Files.FileColumns.DATE_ADDED,
                    MediaStore.Files.FileColumns.MEDIA_TYPE,
                    MediaStore.Files.FileColumns.MIME_TYPE,
                    MediaStore.Files.FileColumns.BUCKET_ID,
                    MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME,
                    MediaStore.Files.FileColumns.SIZE
                )

                val selectImage = "${MediaStore.Files.FileColumns.MEDIA_TYPE}=${MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE} AND ${MediaStore.Files.FileColumns.BUCKET_ID}='${bucket}'"
                val selectVideo = "${MediaStore.Files.FileColumns.MEDIA_TYPE}=${MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO} AND ${MediaStore.Files.FileColumns.MIME_TYPE}='video/mp4' AND ${MediaStore.Files.FileColumns.BUCKET_ID}='${bucket}'"
                var selection: String? = null
                if (type == 3) {
                    selection = "${selectImage} OR ${selectVideo}"
                }
                application.contentResolver.query(uri, projection, selection, null, "${MediaStore.Files.FileColumns.DATE_ADDED} DESC").run {
                    val columnIndexId = this?.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                    val columnIndexMimeType = this?.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)
                    val columnIndexSize = this?.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)

                    while (this?.moveToNext()!!) {
                        val id = this.getLong(columnIndexId!!)
                        val mimeType = this.getString(columnIndexMimeType!!)
                        val size = this.getInt(columnIndexSize!!)
                        val path = Uri.withAppendedPath(uri, "" + id)

                        var duration: String? = null
                        val type: String
                        when {
                            mimeType.equals("video/mp4") -> {
                                type = "video"
                                duration = getDuration(id.toString())
                            }
                            else -> {
                                type = "image"
                            }
                        }
                        val select = when {
                            selectedFiles.size > 0 && selectedFiles.indexOf(path.toString()) != -1 -> {
                                true
                            }
                            else -> {
                                false
                            }
                        }
                        val customGallery = CustomGallery(id.toString(), path.toString(), type, duration, select)
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
                    this.close()
                }

                files.postValue(file)
            } else {
                val uri = MediaStore.Files.getContentUri("external")
                val projection = arrayOf(
                    MediaStore.Files.FileColumns._ID,
                    MediaStore.Files.FileColumns.DATA,
                    MediaStore.Files.FileColumns.DATE_ADDED,
                    MediaStore.Files.FileColumns.MEDIA_TYPE,
                    MediaStore.Files.FileColumns.MIME_TYPE,
                    MediaStore.Files.FileColumns.TITLE,
                    MediaStore.Files.FileColumns.PARENT,
                    MediaStore.Files.FileColumns.DISPLAY_NAME
                )

                val selectImage = "${MediaStore.Files.FileColumns.MEDIA_TYPE}=${MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE} AND ${MediaStore.Files.FileColumns.DATA} LIKE '%${bucket}%'"
                val selectVideo = "${MediaStore.Files.FileColumns.MEDIA_TYPE}=${MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO} AND ${MediaStore.Files.FileColumns.MIME_TYPE}='video/mp4' AND ${MediaStore.Files.FileColumns.DATA} LIKE '%${bucket}%'"
                var selection: String? = null
                if (type == 3) {
                    selection = "${selectImage} OR ${selectVideo}"
                }
                application.contentResolver.query(uri, projection, selection, null, "${MediaStore.Files.FileColumns.DATE_ADDED} DESC").run {
                    val columnIndexId = this?.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                    val columnIndexData = this?.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
                    val columnIndexMimeType = this?.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)

                    while (this?.moveToNext()!!) {
                        val id = this.getLong(columnIndexId!!)
                        val data = this.getString(columnIndexData!!)
                        val mimeType = this.getString(columnIndexMimeType!!)
                        val path = "file://${data}"

                        var duration: String? = null
                        val type: String
                        when {
                            mimeType.equals("video/mp4") -> {
                                type = "video"
                                duration = getDuration(id.toString())
                            }
                            else -> {
                                type = "image"
                            }
                        }
                        val select = when {
                            selectedFiles.size > 0 && selectedFiles.indexOf(path) != -1 -> {
                                true
                            }
                            else -> {
                                false
                            }
                        }
                        val customGallery = CustomGallery(id.toString(), path, type, duration, select)
                        if (type.equals("video")) {
                            val f = File(data)
                            val fileSizeInBytes = f.length()
                            val fileSizeInKB = fileSizeInBytes / 1024
                            val fileSizeInMB = fileSizeInKB / 1024
                            if (fileSizeInMB <= 10) {
                                file.add(customGallery)
                            }
                        } else {
                            file.add(customGallery)
                        }
                    }
                    this.close()
                }

                files.postValue(file)
            }
        }
    }
    fun getFolders(type: Int) {

        // GET IMAGE AND VIDEO FOLDER
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
    @SuppressLint("Recycle")
    private fun getFolderImage(): MutableList<CustomGalleryFolder> {
        val fileFolders: MutableList<CustomGalleryFolder> = ArrayList()


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val uriExternal = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATE_TAKEN,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.BUCKET_ID,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME
            )
            Log.d(TAG, "getFolderImage: ANDROID Q")
            application.contentResolver.query(uriExternal, projection, null, null, MediaStore.Images.Media.DATE_TAKEN + " DESC").run {

                val indexId = this?.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val indexName = this?.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                val indexBucketId = this?.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID)
                val indexBucketName = this?.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
                while(this?.moveToNext()!!) {
                    val id = this.getLong(indexId!!)
                    val name = this.getString(indexName!!)
                    val bucketId = this.getLong(indexBucketId!!)
                    val bucketName = this.getString(indexBucketName!!)

                    if (folderTitles.indexOf(bucketName) == -1) {
                        val folder = CustomGalleryFolder(bucketId.toString(), bucketName, 0)
                        fileFolders.add(folder)
                        folderTitles.add(bucketName)
                    }
                }
                this.close()
            }
            return fileFolders
        } else {
            val uriExternal = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val projection = arrayOf(MediaStore.Images.Media.BUCKET_ID, MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.Images.Media.DATA)
            val orderBy = MediaStore.Images.Media.DATE_TAKEN

            application.contentResolver.query(uriExternal, projection, null, null, orderBy).run {
                val columnIndexBucketId = this?.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID)
                val columnIndexBucketName = this?.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
                val columnIndexData = this?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                while (this?.moveToNext()!!) {
                    val bucketId = this.getString(columnIndexBucketId!!)
                    val bucket = this.getString(columnIndexBucketName!!)
                    val data = this.getString(columnIndexData!!)
                    if (folderTitles.indexOf(bucket) == -1) {
                        val split = data.split("/")
                        val folder = CustomGalleryFolder(bucketId, bucket, (split.size - 1))
                        fileFolders.add(folder)
                        folderTitles.add(bucket)
                    }
                }
                this.close()
            }

            return fileFolders
        }
    }
    @SuppressLint("Recycle")
    private fun getFolderVideo(fileFolders: MutableList<CustomGalleryFolder> = ArrayList()): MutableList<CustomGalleryFolder> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val uriExternal = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            val projection = arrayOf(
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DATE_TAKEN,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.BUCKET_ID,
                MediaStore.Video.Media.BUCKET_DISPLAY_NAME
            )
            Log.d(TAG, "getFolderVideo: ANDROID Q")
            application.contentResolver.query(uriExternal, projection, null, null, MediaStore.Video.Media.DATE_TAKEN + " DESC").run {

                val indexId = this?.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val indexName = this?.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                val indexBucketId = this?.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID)
                val indexBucketName = this?.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
                while(this?.moveToNext()!!) {
                    val id = this.getLong(indexId!!)
                    val name = this.getString(indexName!!)
                    val bucketId = this.getLong(indexBucketId!!)
                    val bucketName = this.getString(indexBucketName!!)
                    val countFolders = fileFolders.size

                    if (countFolders > 0) {
                        val index = folderTitles.indexOf(bucketName)
                        if (index == -1) {
                            val folder = CustomGalleryFolder(bucketId.toString(), bucketName, 0)
                            fileFolders.add(folder)
                            folderTitles.add(bucketName)
                        }
                    } else {
                        val folder = CustomGalleryFolder(bucketId.toString(), bucketName, 0)
                        fileFolders.add(folder)
                        folderTitles.add(bucketName)
                    }
                }
                this.close()
            }

            return fileFolders
        } else {
            val uriExternal = MediaStore.Video.Media.EXTERNAL_CONTENT_URI

            val projection = arrayOf(MediaStore.Video.VideoColumns.BUCKET_ID, MediaStore.Video.VideoColumns.BUCKET_DISPLAY_NAME, MediaStore.Video.VideoColumns.DATA)
            val where = "${MediaStore.Video.VideoColumns.MIME_TYPE}='video/mp4'"
            val orderBy = "${MediaStore.Video.VideoColumns.DATE_TAKEN} DESC"

            application.contentResolver.query(uriExternal, projection, where, null, orderBy).run {
                val columnIndexBucketId = this?.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.BUCKET_ID)
                val columnIndexBucketName = this?.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.BUCKET_DISPLAY_NAME)
                val columnIndexData = this?.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DATA)
                while (this?.moveToNext()!!) {
                    val bucketId = this.getString(columnIndexBucketId!!)
                    val bucket = this.getString(columnIndexBucketName!!)
                    val data = this.getString(columnIndexData!!)
                    val split = data.split("/")
                    val countFolders = fileFolders.size


                    if (countFolders > 0) {
                        val index = folderTitles.indexOf(bucket)
                        if (index == -1) {
                            val folder = CustomGalleryFolder(bucketId, bucket, (split.size - 1))
                            fileFolders.add(folder)
                            folderTitles.add(bucket)
                        }
                    } else {
                        val folder = CustomGalleryFolder(bucketId, bucket, (split.size - 1))
                        fileFolders.add(folder)
                        folderTitles.add(bucket)
                    }

                }
                this.close()
            }

            return fileFolders
        }
    }

    @SuppressLint("InlinedApi", "Recycle")
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
            application.contentResolver.query(uri, projection, whereClause, null, "$orderBy DESC").run {
                this?.moveToFirst()
                result = formatMilliSeccond(Integer.valueOf(this?.getString(1)!!))
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