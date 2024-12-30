package com.example.post29

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class PhotosRecyclerAdapter : RecyclerView.Adapter<PhotosRecyclerAdapter.ViewHolder>() {

    var items: List<Photo> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = PhotoItem(parent.context, null)

        view.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        return ViewHolder(view)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.photo = items[position]
    }

    inner class ViewHolder(val item: PhotoItem) : RecyclerView.ViewHolder(item) {
        var photo: Photo? = null
            set(value) {
                value?.let {
                    item.setData(value)
                    field = value
                }
            }
    }
}