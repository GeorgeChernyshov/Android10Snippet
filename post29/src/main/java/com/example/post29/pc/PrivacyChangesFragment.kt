package com.example.post29.pc

import android.Manifest
import android.app.Activity
import android.content.ContentUris
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.post29.MainActivity
import com.example.post29.R
import com.example.post29.databinding.FragmentPrivacyChangesBinding
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

class PrivacyChangesFragment : Fragment() {

    private lateinit var binding: FragmentPrivacyChangesBinding
    private lateinit var currentPhotoPath: String

    private val recyclerAdapter = PhotosRecyclerAdapter()

    private val photoIntentLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val bitmap = result.data?.extras?.get("data") as Bitmap
            savePhoto(bitmap)
            loadPhotos()
        }
    }

    private val requestMediaPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) handleAccessToMedia()
    }

    private val photoCollection by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(
                MediaStore.VOLUME_EXTERNAL_PRIMARY
            )
        } else MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPrivacyChangesBinding.inflate(inflater, container, false)

        with(binding) {
            photosRecycler.adapter = recyclerAdapter
            accessScopedStorageButton.setOnClickListener {
                accessScopedStorage()
            }

            takePhotoButton.setOnClickListener {
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        requireContext(),
                        "com.example.android.fileprovider_post29",
                        it
                    )

                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    photoIntentLauncher.launch(intent)
                }
            }

            backgroundLocationTextView.text = getString(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                    R.string.location_permission_needed_hint
                    else R.string.location_permission_not_needed_hint
            )

            backgroundLocationButton.setOnClickListener {
                if (
                    ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED &&
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                ) {
                    (context as MainActivity).requestPermissionLauncher
                        .launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                } else {
                    val intent = Intent(context, LocationService::class.java)
                    context?.startService(intent)
                }
            }

            goToNextScreenButton.setOnClickListener {
                findNavController().navigate(
                    R.id.action_PrivacyChangesFragment_to_NDKFragment
                )
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                takePhotosLayout.isVisible = true
                mediaPermissionLayout.isVisible = false

                loadPhotos()
            } else {
                takePhotosLayout.isVisible = false
                mediaPermissionLayout.isVisible = true

                giveMediaPermissionButton.setOnClickListener {
                    tryAccessMedia()
                }
            }
        }
    }

    private fun accessScopedStorage() {
        with (context?.externalCacheDir ?: return){
            val scopedFile = File(this, SCOPED_FILE_NAME)

            if (!scopedFile.exists() || scopedFile.length() == 0L) {
                scopedFile.createNewFile()
                scopedFile.writeText(SCOPED_FILE_TEXT)
            }

            binding.accessScopedStorageTextView.text = scopedFile.readText()
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File = context?.externalCacheDir
            ?: throw IOException("Failed to create a file")

        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    private fun savePhoto(imageBitmap: Bitmap) {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())

        val newImageDetails = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "JPEG_${timeStamp}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }

        var uri: Uri? = null
        try {
            uri = requireContext().contentResolver
                .insert(photoCollection, newImageDetails)
                ?: throw IOException("Failed to create new MediaStore record.")

            requireContext().contentResolver
                .openOutputStream(uri)
                ?.use {
                    if (!imageBitmap.compress(
                            Bitmap.CompressFormat.JPEG,
                            95,
                            it
                    )) {
                        throw IOException("Failed to save bitmap.")
                    }
                } ?: throw IOException("Failed to open output stream.")
        }
        catch (e: IOException) {
            uri?.let { orphanUri ->
                // Don't leave an orphan entry in the MediaStore
                requireContext().contentResolver
                    .delete(orphanUri, null, null)
            }
        }
    }

    private fun loadPhotos() {
        val projection = arrayOf(MediaStore.Images.Media._ID)
        requireContext().contentResolver.query(
            photoCollection,
            projection,
            null,
            null,
            " LIMIT 20"
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndex(MediaStore.Images.Media._ID)

            val photos = ArrayList<Photo>()
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val uri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                photos.add(Photo(uri))
            }

            recyclerAdapter.items = photos
        }
    }

    private fun handleAccessToMedia() {
        binding.takePhotosLayout.isVisible = true
        loadPhotos()
    }

    private fun tryAccessMedia() {
        if (
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            handleAccessToMedia()
        } else {
            requestMediaPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    companion object {
        private const val SCOPED_FILE_NAME = "Scoped.txt"
        private const val SCOPED_FILE_TEXT = "This is a content of a file inside the scoped app storage"
    }
}