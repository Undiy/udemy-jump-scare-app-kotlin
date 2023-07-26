package com.example.countershock

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import java.io.File

class ImagePickerAdapter(
    var items: List<ImageModel>,
    var callback: Callback)
    : RecyclerView.Adapter<ImagePickerAdapter.ViewHolder>() {

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView

        init {
            imageView = itemView.findViewById(R.id.gridImageView)
        }

    }

    interface Callback {
        fun itemSelected(item: ImageModel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.grid_item, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.itemView.setOnClickListener {
            callback.itemSelected(item)
        }

        val imageUri = if (item.isAsset) {
            ShockUtils.getDrawableUri(holder.itemView.context, item.imgFilename)
        } else {
            Uri.fromFile(File(item.imgFilename))
        }

        Glide.with(holder.imageView.context)
            .load(imageUri)
            .apply(RequestOptions()
                .override(500, 700)
                .placeholder(R.drawable.ic_broken_image))
            .into(holder.imageView)

    }
}