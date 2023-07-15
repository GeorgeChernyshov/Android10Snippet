package com.example.android10snippet

import android.Manifest
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Bundle
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SimpleCursorAdapter
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import androidx.navigation.fragment.findNavController
import com.example.android10snippet.databinding.FragmentIdentifiersBinding
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

        binding.activateAdminButton.setOnClickListener {
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, deviceAdminSample)
            }

            val mainActivity = requireActivity() as MainActivity
            mainActivity.requestAdminLauncher.launch(intent)
        }

        binding.goToAdminInfoButton.isVisible = dpm.isAdminActive(deviceAdminSample)

        binding.getContactsButton.setOnClickListener {
            when {
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_CONTACTS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    LoaderManager.getInstance(this).initLoader(0, null, this)
                }

                else -> {
                    val mainActivity = requireActivity() as MainActivity
                    mainActivity.requestPermissionLauncher.launch(
                        Manifest.permission.READ_CONTACTS)
                }
            }
        }

        binding.goToAdminInfoButton.setOnClickListener {
            findNavController().navigate(R.id.action_IdentifiersFragment_to_AdminInfoFragment)
        }

        binding.connectionUidTextView.text = getConnectionOwnerUid()

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

    private fun getConnectionOwnerUid(): String {
        // Update to use ConnectivityManager and for old version to return correct values
        try {
            val file = File(PROC_FILE)
            return file.readLines().drop(1)
                .map { line -> line.trim().split("\\s+") }
                .filter { it.size > INDEX_UID_COL }
                .toString()
        }
        catch (ex: Exception) {
            return getString(R.string.proc_net_not_available)
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
        private const val INDEX_LOCAL_ADDRESS_COL = 1
    }
}