package com.example.pre29.camera

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class CameraSettingsRecyclerAdapter : RecyclerView.Adapter<CameraSettingsRecyclerAdapter.ViewHolder>() {

    var items: List<CameraSetting> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = CameraSettingItem(parent.context, null)

        view.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        return ViewHolder(view)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setting = items[position]
    }

    inner class ViewHolder(val item: CameraSettingItem) : RecyclerView.ViewHolder(item) {
        var setting: CameraSetting? = null
            set(value) {
                value?.let {
                    item.setData(value)
                    field = value
                }
            }
    }
}