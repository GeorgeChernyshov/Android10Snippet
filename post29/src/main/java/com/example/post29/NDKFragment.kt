package com.example.post29

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.post29.databinding.FragmentNdkBinding

class NDKFragment : Fragment() {

    private lateinit var binding: FragmentNdkBinding

    private external fun getTextRelocationText(): String

    init {
        System.loadLibrary("basic")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNdkBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with (binding) {
            textRelocationTextView.text = getTextRelocationText()

            bionicPathsHint.text = getString(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                    R.string.ndk_bionic_symbolic_hint
                else R.string.ndk_bionic_literal_hint
            )

            executableMemoryTypeHint.text = getString(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                    R.string.ndk_system_binaries_read
                else R.string.ndk_system_binaries_executable
            )
        }
    }
}