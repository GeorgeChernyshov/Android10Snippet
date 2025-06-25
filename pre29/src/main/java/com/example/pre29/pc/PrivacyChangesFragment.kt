package com.example.pre29.pc

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.pre29.R
import com.example.pre29.databinding.FragmentPrivacyChangesBinding
import java.io.File

class PrivacyChangesFragment : Fragment() {

    private lateinit var binding: FragmentPrivacyChangesBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPrivacyChangesBinding.inflate(inflater, container, false)
        binding.accessScopedStorageButton.setOnClickListener {
            accessScopedStorage()
        }

        binding.goToNextScreenButton.setOnClickListener {
            findNavController().navigate(
                R.id.action_PrivacyChangesFragment_to_WiFiP2PFragment
            )
        }

        return binding.root
    }

    private fun accessScopedStorage() {
        with (context?.externalCacheDir ?: return) {
            val scopedFile = File(this, SCOPED_FILE_NAME)

            if (!scopedFile.exists() || scopedFile.length() == 0L) {
                scopedFile.createNewFile()
                scopedFile.writeText(SCOPED_FILE_TEXT)
            }

            binding.accessScopedStorageTextView.text = scopedFile.readText()
        }
    }

    companion object {
        private const val SCOPED_FILE_NAME = "Scoped.txt"
        private const val SCOPED_FILE_TEXT = "This is a content of a file inside the scoped app storage"
    }
}