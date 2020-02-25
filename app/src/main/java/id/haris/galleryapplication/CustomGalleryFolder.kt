package id.haris.galleryapplication

data class CustomGalleryFolder(val id: String, val title: String, val total: Int) {
    override fun equals(other: Any?): Boolean {
        if (other !is CustomGalleryFolder) {
            return super.equals(other)
        } else {
            val folder = other
            return folder.id == this.id || folder.title == this.title
        }
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + total
        return result
    }
}