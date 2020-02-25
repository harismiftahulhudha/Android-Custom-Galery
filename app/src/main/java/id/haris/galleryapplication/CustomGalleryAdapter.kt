package id.haris.galleryapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class CustomGalleryAdapter : RecyclerView.Adapter<CustomGalleryAdapter.ViewHolder>() {
    private var models: MutableList<CustomGallery>? = null
    private var onClickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.activity_custom_gallery_row, parent, false)
        return ViewHolder(view)
    }

    fun updateModel(position: Int, model: CustomGallery) {
        models?.set(position, model)
        notifyItemChanged(position)
    }

    fun setModel(models: MutableList<CustomGallery>) {
        this.models = models
        notifyDataSetChanged()
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (models != null) {
            val model = models!![position]
            val params: ConstraintLayout.LayoutParams = holder.image.getLayoutParams() as ConstraintLayout.LayoutParams
            val params1: ConstraintLayout.LayoutParams =
                holder.iconVideo.getLayoutParams() as ConstraintLayout.LayoutParams

            if (model.select) {
                holder.icon.setVisibility(View.VISIBLE)
                holder.frame.setVisibility(View.VISIBLE)
                params.setMargins(6, 6, 6, 6)
                params1.setMargins(12, 10, 0, 0)
                holder.iconVideo.setLayoutParams(params1)
                holder.image.setLayoutParams(params)
            } else {
                holder.icon.setVisibility(View.GONE)
                holder.frame.setVisibility(View.GONE)
                params.setMargins(0, 0, 0, 0)
                params1.setMargins(12, 10, 0, 0)
                holder.iconVideo.setLayoutParams(params1)
                holder.image.setLayoutParams(params)
            }

            if (model.type.equals("video")) {
                holder.iconPlay.setVisibility(View.VISIBLE)
                holder.duration.setVisibility(View.VISIBLE)
                holder.iconVideo.setVisibility(View.VISIBLE)
                holder.overlay.setVisibility(View.VISIBLE)
                holder.duration.setText(model.duration)
                Glide.with(holder.itemView.context).load(model.path)
                    .error(holder.itemView.context.getDrawable(R.drawable.broken_image))
                    .into(holder.image)
            } else {
                holder.iconPlay.setVisibility(View.GONE)
                holder.duration.setVisibility(View.GONE)
                holder.iconVideo.setVisibility(View.GONE)
                holder.overlay.setVisibility(View.GONE)
                Glide.with(holder.itemView.context).load(model.path)
                    .error(holder.itemView.context.getDrawable(R.drawable.broken_image))
                    .into(holder.image)
            }

            if (onClickListener != null) {
                holder.itemView.setOnClickListener {
                    onClickListener!!.onClick(position, model)
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int = position

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getItemCount(): Int = when {
        models == null -> 0
        else -> models!!.size
    }

    class ViewHolder (itemView: View): RecyclerView.ViewHolder(itemView) {
        val image = itemView.findViewById<ImageView>(R.id.galleryStorageImage)
        val icon = itemView.findViewById<ImageView>(R.id.galleryStorageSelect)
        val iconVideo = itemView.findViewById<ImageView>(R.id.galleryStorageIconVideo)
        val iconPlay = itemView.findViewById<ImageView>(R.id.galleryStorageIconPlay)
        val duration = itemView.findViewById<TextView>(R.id.galleryStorageDurationVideo)
        val frame = itemView.findViewById<View>(R.id.galleryStorageFrame)
        val overlay = itemView.findViewById<View>(R.id.galleryStorageOverlay)
    }

    interface OnClickListener {
        fun onClick(position: Int, model: CustomGallery)
    }
}