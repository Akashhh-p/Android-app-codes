package com.example.phonebook

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Telephony
import android.view.*
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.*

class SmsFragment : Fragment() {

    private lateinit var storage: SmsStorage
    private lateinit var adapter: SmsAdapter
    private lateinit var viewModel: MainViewModel
    private val deviceMessages = mutableListOf<SmsMessage>()
    private val importedMessages = mutableListOf<SmsMessage>()

    private lateinit var permissionContainer: LinearLayout
    private lateinit var rvSms: RecyclerView
    private lateinit var tvEmptyState: TextView

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            permissionContainer.visibility = View.GONE
            loadDeviceSms()
        } else {
            permissionContainer.visibility = View.VISIBLE
            showToast("Permission denied. Cannot access SMS.")
        }
    }

    private val createDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            val smsToExport = deviceMessages + importedMessages
            if (smsToExport.isEmpty()) {
                showToast("No SMS to export")
                return@let
            }

            if (storage.exportToUri(it, smsToExport)) {
                showToast("SMS exported successfully")
            } else {
                showToast("Export failed")
            }
        }
    }

    private val openDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            val importedList = storage.importFromUri(it)
            if (importedList != null) {
                val currentImported = viewModel.importedSmsMessages.value ?: mutableListOf()
                val currentDevice = viewModel.smsMessages.value ?: mutableListOf()

                val existingMessages = currentImported + currentDevice
                val newUniqueMessages = SmsUtils.getUniqueNewMessages(importedList, existingMessages)

                if (newUniqueMessages.isNotEmpty()) {
                    val mergedList = currentImported.toMutableList()
                    mergedList.addAll(newUniqueMessages)
                    
                    viewModel.setImportedSmsMessages(mergedList)
                    showToast("${newUniqueMessages.size} unique SMS imported.")
                } else {
                    showToast("No new unique SMS found to import")
                }
            } else {
                showToast("Invalid file or empty data")
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_sms_list, container, false)
        setHasOptionsMenu(true)
        storage = SmsStorage(requireContext())
        viewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        
        permissionContainer = view.findViewById(R.id.permissionContainer)
        rvSms = view.findViewById(R.id.rvSms)
        tvEmptyState = view.findViewById(R.id.tvEmptyState)

        view.findViewById<Button>(R.id.btnAllowAccess).setOnClickListener {
            requestPermissionLauncher.launch(Manifest.permission.READ_SMS)
        }

        view.findViewById<Button>(R.id.btnImportSms).setOnClickListener {
            openDocumentLauncher.launch(arrayOf("application/json"))
        }

        view.findViewById<Button>(R.id.btnExportSms).setOnClickListener {
            createDocumentLauncher.launch("sms_export.json")
        }

        view.findViewById<Button>(R.id.btnDeleteAllSms).setOnClickListener {
            deleteAllSms()
        }

        adapter = SmsAdapter(mutableListOf(), ::onDeleteSms) {
            updateSelectionCount()
        }
        rvSms.layoutManager = LinearLayoutManager(requireContext())
        rvSms.adapter = adapter

        observeViewModel()
        checkPermissionAndLoad()
        return view
    }

    private fun observeViewModel() {
        viewModel.smsMessages.observe(viewLifecycleOwner) { list ->
            deviceMessages.clear()
            deviceMessages.addAll(list)
            refreshList()
        }
        viewModel.importedSmsMessages.observe(viewLifecycleOwner) { list ->
            importedMessages.clear()
            importedMessages.addAll(list)
            refreshList()
        }
    }

    private fun checkPermissionAndLoad() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED) {
            permissionContainer.visibility = View.GONE
            if (!viewModel.isSmsLoaded) {
                loadDeviceSms()
            }
        } else {
            permissionContainer.visibility = View.VISIBLE
        }
    }

    private fun loadDeviceSms() {
        val newList = mutableListOf<SmsMessage>()
        try {
            val cursor = requireContext().contentResolver.query(
                Telephony.Sms.CONTENT_URI,
                null, null, null, Telephony.Sms.DEFAULT_SORT_ORDER
            )
            cursor?.use {
                val idIndex = it.getColumnIndex(Telephony.Sms._ID)
                val addressIndex = it.getColumnIndex(Telephony.Sms.ADDRESS)
                val bodyIndex = it.getColumnIndex(Telephony.Sms.BODY)
                val dateIndex = it.getColumnIndex(Telephony.Sms.DATE)
                
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                
                while (it.moveToNext()) {
                    val id = it.getString(idIndex)
                    val address = it.getString(addressIndex) ?: "Unknown"
                    val body = it.getString(bodyIndex) ?: ""
                    val timestamp = it.getLong(dateIndex)
                    val date = sdf.format(Date(timestamp))
                    newList.add(SmsMessage(id, address, body, timestamp, date))
                }
            }
            viewModel.setSmsMessages(newList)
        } catch (e: SecurityException) {
            showToast("Permission required")
            permissionContainer.visibility = View.VISIBLE
        }
    }

    private fun onDeleteSms(message: SmsMessage) {
        val isImported = importedMessages.any { it.id == message.id }
        AlertDialog.Builder(requireContext())
            .setTitle("Delete SMS")
            .setMessage("Are you sure you want to delete this SMS?")
            .setPositiveButton(R.string.delete) { dialog, which ->
                if (isImported) {
                    val newList = importedMessages.toMutableList()
                    newList.removeAll { it.id == message.id }
                    viewModel.setImportedSmsMessages(newList)
                    showToast("Imported SMS removed")
                } else {
                    if (isDefaultSmsApp()) {
                        try {
                            val deleted = requireContext().contentResolver.delete(
                                Telephony.Sms.CONTENT_URI,
                                "${Telephony.Sms._ID}=?",
                                arrayOf(message.id)
                            )
                            if (deleted > 0) {
                                showToast("SMS deleted")
                                viewModel.isSmsLoaded = false
                                loadDeviceSms()
                            } else {
                                showToast("Failed to delete SMS")
                            }
                        } catch (e: Exception) {
                            showToast("Error: ${e.message}")
                        }
                    } else {
                        Snackbar.make(requireView(), R.string.sms_deletion_restricted, Snackbar.LENGTH_LONG).show()
                    }
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun isDefaultSmsApp(): Boolean {
        val defaultSmsPackage = Telephony.Sms.getDefaultSmsPackage(requireContext())
        return defaultSmsPackage != null && defaultSmsPackage == requireContext().packageName
    }

    private fun refreshList() {
        adapter.updateMessages(deviceMessages + importedMessages)
        updateEmptyState()
    }

    private fun updateSelectionCount() {
        val count = adapter.getSelectedMessages().size
        if (count > 0) {
            activity?.title = "SMS ($count selected)"
        } else {
            activity?.title = "SMS"
        }
    }

    private fun updateEmptyState() {
        val total = deviceMessages.size + importedMessages.size
        tvEmptyState.visibility = if (total == 0 && permissionContainer.visibility == View.GONE) View.VISIBLE else View.GONE
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.sms_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_select_all -> { adapter.selectAll(true); true }
            R.id.action_clear_selection -> { adapter.selectAll(false); true }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun deleteAllSms() {
        if (deviceMessages.isEmpty() && importedMessages.isEmpty()) {
            showToast("No SMS to delete")
            return
        }
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.delete_all_sms)
            .setMessage(R.string.delete_all_sms_confirmation)
            .setPositiveButton(R.string.delete) { dialog, which ->
                viewModel.setImportedSmsMessages(mutableListOf())
                
                if (deviceMessages.isNotEmpty()) {
                    if (isDefaultSmsApp()) {
                        try {
                            val deleted = requireContext().contentResolver.delete(
                                Telephony.Sms.CONTENT_URI,
                                null,
                                null
                            )
                            showToast("$deleted Device SMS deleted")
                            viewModel.isSmsLoaded = false
                            loadDeviceSms()
                        } catch (e: Exception) {
                            showToast("Error deleting Device SMS: ${e.message}")
                        }
                    } else {
                        Snackbar.make(requireView(), R.string.sms_deletion_restricted, Snackbar.LENGTH_LONG).show()
                    }
                } else {
                    showToast("Imported SMS cleared")
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        activity?.title = if (adapter.getSelectedMessages().isNotEmpty())
            "SMS (${adapter.getSelectedMessages().size} selected)"
            else "SMS"
    }
}
