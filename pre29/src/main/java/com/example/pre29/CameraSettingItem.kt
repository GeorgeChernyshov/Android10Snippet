package com.example.pre29

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.example.pre29.databinding.ItemCameraSettingBinding

class CameraSettingItem @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    private lateinit var binding: ItemCameraSettingBinding

    init {
        if (isInEditMode)
            inflate(context, R.layout.item_camera_setting, this)
        else
            binding = ItemCameraSettingBinding.inflate(LayoutInflater.from(context), this, true)
    }

    fun setData(data: CameraSetting) = with (binding) {
        settingTitleTextView.text = data.title
        settingValueTextView.text = data.value
    }
}