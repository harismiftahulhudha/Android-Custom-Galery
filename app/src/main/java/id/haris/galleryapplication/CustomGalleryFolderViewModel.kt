package id.haris.galleryapplication

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData

class CustomGalleryFolderViewModel(application: Application) : AndroidViewModel(application) {
    val repository: CustomGalleryRepository = CustomGalleryRepository(application)

    fun getFolders(type: Int) { repository.getFolders(type) }
    fun getFiles(type: Int, bucketId: String, selectedFiles: MutableList<String> = ArrayList()) { repository.getFiles(type, bucketId, selectedFiles) }

    fun getFiles(): LiveData<MutableList<CustomGallery>> = repository.getFiles()
    fun getFolders(): LiveData<MutableList<CustomGalleryFolder>> = repository.getFolders()
    fun getFolderNames(): LiveData<MutableList<String>> = repository.getFolderNames()
}