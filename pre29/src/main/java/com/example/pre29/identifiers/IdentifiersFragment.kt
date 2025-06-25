package com.example.pre29.identifiers

import android.Manifest
import android.app.admin.DevicePolicyManager
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SimpleCursorAdapter
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import androidx.navigation.fragment.findNavController
import com.example.pre29.MainActivity
import com.example.pre29.R
import com.example.pre29.databinding.FragmentIdentifiersBinding
import java.io.File
import java.lang.Exception

class IdentifiersFragment : Fragment(), LoaderManager.LoaderCallbacks<Cursor> {

    private lateinit var binding: FragmentIdentifiersBinding

    private val deviceAdminSample get() = (requireActivity() as MainActivity).deviceAdminSample
    private val dpm get() = (requireActivity() as MainActivity).dpm

    // An adapter that binds the result Cursor to the ListView
    private var cursorAdapter: SimpleCursorAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentIdentifiersBinding.inflate(inflater, container, false)

        requestPermission(Manifest.permission.READ_CONTACTS)
        requestPermission(Manifest.permission.READ_PHONE_STATE)

        with (binding) {
            activateAdminButton.setOnClickListener {
                val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                    putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, deviceAdminSample)
                }

                val mainActivity = requireActivity() as MainActivity
                mainActivity.requestAdminLauncher.launch(intent)
            }

            goToAdminInfoButton.isVisible = dpm.isAdminActive(deviceAdminSample)

            getContactsButton.setOnClickListener {
                when {
                    ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.READ_CONTACTS
                    ) == PackageManager.PERMISSION_GRANTED -> {
                        LoaderManager.getInstance(this@IdentifiersFragment)
                            .initLoader(0, null, this@IdentifiersFragment)
                    }

                    else -> {
                        val mainActivity = requireActivity() as MainActivity
                        mainActivity.requestPermissionLauncher.launch(
                            Manifest.permission.READ_CONTACTS
                        )
                    }
                }
            }

            goToAdminInfoButton.setOnClickListener {
                findNavController().navigate(R.id.action_IdentifiersFragment_to_AdminInfoFragment)
            }

            connectionUidTextView.text = getConnectionOwnerUid()
            serialTextView.text = getBuildSerial()

            getClipboardDataButton.setOnClickListener {
                clipboardDataTextView.text = (activity as? MainActivity)
                    ?.binder
                    ?.getClipboardData()
                    ?: context?.getString(R.string.error_not_available_common)
            }

            goToNextScreenButton.setOnClickListener {
                findNavController().navigate(R.id.action_IdentifiersFragment_to_CameraAndConnectivityFragment)
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.also {
            cursorAdapter = SimpleCursorAdapter(
                it,
                R.layout.item_contact,
                null,
                FROM_COLUMNS, TO_IDS,
                0
            )

            binding.contactsListView.adapter = cursorAdapter
        }
    }

    override fun onCreateLoader(loaderId: Int, args: Bundle?): Loader<Cursor> {
        selectionArgs[0] = searchString
        // Starts the query
        return activity?.let {
            return CursorLoader(
                it,
                ContactsContract.Contacts.CONTENT_URI,
                PROJECTION,
                null,
                null,
                null
            )
        } ?: throw IllegalStateException()
    }

    override fun onLoadFinished(loader: Loader<Cursor>, cursor: Cursor) {
        cursorAdapter?.swapCursor(cursor)
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        cursorAdapter?.swapCursor(null)
    }

    private fun requestPermission(permission: String) {
        if (
            ContextCompat.checkSelfPermission(
                requireContext(),
                permission
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            val mainActivity = requireActivity() as MainActivity
            mainActivity.requestPermissionLauncher.launch(permission)
        }
    }

    private fun getBuildSerial(): String {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                Build.getSerial()
            else getString(R.string.error_build_serial_not_implemented)
        }
        catch (ex: SecurityException) {
            ex.message.toString()
        }
    }

    private fun getConnectionOwnerUid(): String {
        return try {
            File(PROC_FILE).readLines()
                .drop(1)
                .map { line -> line.trim().split(" ") }
                .first()[INDEX_UID_COL]
        } catch (ex: Exception) {
            getString(R.string.error_proc_net_not_available)
        }
    }

    companion object {
        private val FROM_COLUMNS: Array<String> = arrayOf(
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
            ContactsContract.Contacts.TIMES_CONTACTED
        )

        private val TO_IDS: IntArray = intArrayOf(
            R.id.nameTextView,
            R.id.callsTextView
        )

        private val PROJECTION: Array<out String> = arrayOf(
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.LOOKUP_KEY,
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
            ContactsContract.Contacts.TIMES_CONTACTED
        )

        // Defines a variable for the search string
        private val searchString: String = ""

        private val selectionArgs = arrayOf(searchString)

        // Constants needed to fetch connection owner uid
        private const val PROC_FILE = "/proc/net/tcp"
        private const val INDEX_UID_COL = 7
    }
}