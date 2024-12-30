package com.example.post29

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.example.post29.databinding.ItemPhotoBinding

class PhotoItem @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    private lateinit var binding: ItemPhotoBinding

    init {
        if (isInEditMode)
            inflate(context, R.layout.item_photo, this)
        else
            binding = ItemPhotoBinding.inflate(LayoutInflater.from(context), this, true)
    }

    fun setData(data: Photo) {
        binding.photo.setImageURI(data.photoUri)
    }
}