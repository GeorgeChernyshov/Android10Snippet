package com.example.post29.darktheme

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.post29.R
import com.example.post29.databinding.FragmentDarkThemeBinding

class DarkThemeFragment : Fragment() {

    private lateinit var binding: FragmentDarkThemeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDarkThemeBinding.inflate(inflater, container, false)

        with(binding) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                darkThemeHintTextView.text = getString(R.string.dark_theme_supported)
                chooseThemeLayout.visibility = View.VISIBLE
            } else {
                darkThemeHintTextView.text = getString(R.string.dark_theme_not_supported)
                chooseThemeLayout.visibility = View.GONE
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            lightThemeButton.setOnClickListener {
                setTheme(Theme.LIGHT)
            }

            darkThemeButton.setOnClickListener {
                setTheme(Theme.DARK)
            }

            systemThemeButton.setOnClickListener {
                setTheme(Theme.SYSTEM)
            }

            goToNextScreenButton.setOnClickListener {
                findNavController().navigate(
                    R.id.action_DarkThemeFragment_to_IdentifiersFragment
                )
            }
        }
    }

    private fun setTheme(theme: Theme) {
        when (theme) {
            Theme.LIGHT -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }

            Theme.DARK -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }

            Theme.SYSTEM -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }
    }

    enum class Theme {
        LIGHT,
        DARK,
        SYSTEM;
    }
}