package id.haris.galleryapplication

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData

class CustomGalleryFolderViewModel(application: Application) : AndroidViewModel(application) {
    val repository: CustomGalleryRepository = CustomGalleryRepository(application)

    fun getFolders(type: Int) { repository.getFolders(type) }
    fun getFiles(type: Int, bucketId: String) { repository.getFiles(type, bucketId) }
    fun updateFile(file: CustomGallery) {
        repository.updateFile(file)
    }

    fun getFiles(): LiveData<List<CustomGallery>> = repository.getFiles()
    fun getFolders(): LiveData<List<CustomGalleryFolder>> = repository.getFolders()
    fun getFolderNames(): LiveData<List<String>> = repository.getFolderNames()
    fun getUpdatedFile(): LiveData<CustomGallery> = repository.getUpdatedFile()
}