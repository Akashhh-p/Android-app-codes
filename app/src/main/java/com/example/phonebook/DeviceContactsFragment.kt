package com.example.phonebook

import android.Manifest
import android.content.ContentProviderOperation
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
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
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class DeviceContactsFragment : Fragment() {

    private lateinit var storage: ContactStorage
    private lateinit var adapter: DeviceContactAdapter
    private lateinit var viewModel: MainViewModel
    private val deviceContacts = mutableListOf<Contact>()
    private val importedContacts = mutableListOf<Contact>()
    
    private lateinit var permissionContainer: LinearLayout
    private lateinit var rvContacts: RecyclerView
    private lateinit var tvEmptyState: TextView

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            permissionContainer.visibility = View.GONE
            loadDeviceContacts()
        } else {
            permissionContainer.visibility = View.VISIBLE
            showToast("Permission denied. Cannot access device contacts.")
        }
    }

    private val createDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            val contactsToExport = deviceContacts + importedContacts
            if (contactsToExport.isEmpty()) {
                showToast("No contacts to export")
                return@let
            }

            if (storage.exportToUri(it, contactsToExport)) {
                showToast("Contacts exported successfully")
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
                val currentImported = viewModel.importedDeviceContacts.value ?: mutableListOf()
                val currentDevice = viewModel.deviceContacts.value ?: mutableListOf()

                val existingContacts = currentImported + currentDevice
                val newUniqueContacts = ContactUtils.getUniqueNewContacts(importedList, existingContacts)

                if (newUniqueContacts.isNotEmpty()) {
                    val mergedList = currentImported.toMutableList()
                    mergedList.addAll(newUniqueContacts)
                    
                    viewModel.setImportedDeviceContacts(mergedList)
                    showToast("${newUniqueContacts.size} unique contacts imported.")
                } else {
                    showToast("No new unique contacts found to import")
                }
            } else {
                showToast("Invalid file or empty data")
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_device_contacts, container, false)
        setHasOptionsMenu(true)
        storage = ContactStorage(requireContext())
        viewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        
        permissionContainer = view.findViewById(R.id.permissionContainer)
        rvContacts = view.findViewById(R.id.rvContacts)
        tvEmptyState = view.findViewById(R.id.tvEmptyState)
        
        view.findViewById<Button>(R.id.btnAllowAccess).setOnClickListener {
            requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }

        view.findViewById<Button>(R.id.btnImport).setOnClickListener {
            openDocumentLauncher.launch(arrayOf("application/json"))
        }

        view.findViewById<Button>(R.id.btnExport).setOnClickListener {
            createDocumentLauncher.launch("device_contacts.json")
        }

        adapter = DeviceContactAdapter(mutableListOf(), ::showContactDialog, ::onDeleteContact) {
            updateSelectionCount()
        }
        rvContacts.layoutManager = LinearLayoutManager(requireContext())
        rvContacts.adapter = adapter

        observeViewModel()
        checkPermissionAndLoad()
        return view
    }

    private fun observeViewModel() {
        viewModel.deviceContacts.observe(viewLifecycleOwner) { list ->
            deviceContacts.clear()
            deviceContacts.addAll(list)
            refreshList()
        }
        viewModel.importedDeviceContacts.observe(viewLifecycleOwner) { list ->
            importedContacts.clear()
            importedContacts.addAll(list)
            refreshList()
        }
    }

    private fun checkPermissionAndLoad() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            permissionContainer.visibility = View.GONE
            if (!viewModel.isDeviceContactsLoaded) {
                loadDeviceContacts()
            }
        } else {
            permissionContainer.visibility = View.VISIBLE
        }
    }

    private fun loadDeviceContacts() {
        val newList = mutableListOf<Contact>()
        try {
            val cursor = requireContext().contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
            )
            cursor?.use {
                val idIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
                val dataIdIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID)
                val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                while (it.moveToNext()) {
                    val id = it.getLong(idIndex)
                    val dataId = it.getLong(dataIdIndex)
                    val name = it.getString(nameIndex) ?: "No Name"
                    val number = it.getString(numberIndex) ?: ""
                    newList.add(Contact(id, name, number, dataId))
                }
            }
            viewModel.setDeviceContacts(newList)
        } catch (e: SecurityException) {
            showToast("Permission required")
            permissionContainer.visibility = View.VISIBLE
        }
    }

    private fun refreshList() {
        adapter.updateContacts(deviceContacts + importedContacts)
        updateEmptyState()
    }

    private fun updateSelectionCount() {
        val count = adapter.getSelectedContacts().size
        if (count > 0) {
            activity?.title = "Device Contacts ($count selected)"
        } else {
            activity?.title = "Device Contacts"
        }
    }

    private fun updateEmptyState() {
        val total = deviceContacts.size + importedContacts.size
        tvEmptyState.visibility = if (total == 0 && permissionContainer.visibility == View.GONE) View.VISIBLE else View.GONE
    }

    fun addContact() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            showContactDialog(null)
        } else {
            requestWritePermissionLauncher.launch(Manifest.permission.WRITE_CONTACTS)
        }
    }

    private val requestWritePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showContactDialog(null)
        } else {
            showToast("Permission denied. Cannot add contacts.")
        }
    }

    private fun showContactDialog(contact: Contact?) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_contact, null)
        val etName = dialogView.findViewById<TextInputEditText>(R.id.etName)
        val etPhone = dialogView.findViewById<TextInputEditText>(R.id.etPhone)
        val tilName = dialogView.findViewById<TextInputLayout>(R.id.tilName)
        val tilPhone = dialogView.findViewById<TextInputLayout>(R.id.tilPhone)

        contact?.let {
            etName.setText(it.name)
            etPhone.setText(it.phoneNumber)
        }

        AlertDialog.Builder(requireContext())
            .setTitle(if (contact == null) "Add Device Contact" else "Edit Device Contact")
            .setView(dialogView)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
            .create().apply {
                show()
                getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                    val name = etName.text.toString().trim()
                    val phone = etPhone.text.toString().trim()
                    if (name.isNotEmpty() && phone.length == 10) {
                        if (contact == null) insertDeviceContact(name, phone)
                        else updateDeviceContact(contact, name, phone)
                        dismiss()
                    } else {
                        if (name.isEmpty()) tilName.error = "Name required"
                        if (phone.length != 10) tilPhone.error = "10-digit phone required"
                    }
                }
            }
    }

    private fun insertDeviceContact(name: String, phone: String) {
        try {
            val ops = arrayListOf<ContentProviderOperation>()
            ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build())
            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
                .build())
            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phone)
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                .build())
            requireContext().contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)
            showToast("Contact inserted into device contacts")
            loadDeviceContacts()
        } catch (e: Exception) {
            showToast("Failed to insert contact: ${e.message}")
        }
    }

    private fun updateDeviceContact(contact: Contact, name: String, phone: String) {
        try {
            val ops = arrayListOf<ContentProviderOperation>()
            ops.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                .withSelection("${ContactsContract.Data.CONTACT_ID}=? AND ${ContactsContract.Data.MIMETYPE}=?", 
                    arrayOf(contact.id.toString(), ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE))
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
                .build())
            ops.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                .withSelection("${ContactsContract.Data._ID}=?", 
                    arrayOf(contact.dataId.toString()))
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phone)
                .build())
            
            requireContext().contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)
            showToast("Contact updated in device contacts")
            loadDeviceContacts()
        } catch (e: Exception) {
            showToast("Failed to update contact: ${e.message}")
        }
    }

    private fun onDeleteContact(contact: Contact) {
        val isImported = importedContacts.any { it.id == contact.id }
        AlertDialog.Builder(requireContext())
            .setTitle(if (isImported) R.string.delete_contact else R.string.delete_device_contact_title)
            .setMessage(if (isImported) R.string.delete_confirmation else R.string.delete_device_contact_message)
            .setPositiveButton(R.string.delete) { _, _ ->
                if (isImported) {
                    val newList = importedContacts.toMutableList()
                    newList.removeAll { it.id == contact.id }
                    viewModel.setImportedDeviceContacts(newList)
                    showToast("Imported contact removed from App Model")
                } else {
                    try {
                        val ops = arrayListOf<ContentProviderOperation>()
                        ops.add(ContentProviderOperation.newDelete(ContactsContract.RawContacts.CONTENT_URI)
                            .withSelection("${ContactsContract.RawContacts.CONTACT_ID}=?", arrayOf(contact.id.toString()))
                            .build())
                        requireContext().contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)
                        showToast("Contact deleted from device contacts")
                        viewModel.isDeviceContactsLoaded = false
                        loadDeviceContacts()
                    } catch (e: Exception) {
                        showToast("Failed to delete contact: ${e.message}")
                    }
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.device_contacts_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_select_all -> { adapter.selectAll(true); true }
            R.id.action_clear_selection -> { adapter.selectAll(false); true }
            R.id.action_delete_all_imported -> {
                deleteAllImported()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun deleteAllImported() {
        if (importedContacts.isEmpty()) {
            showToast("No imported contacts to delete")
            return
        }
        AlertDialog.Builder(requireContext())
            .setTitle("Delete All Imported")
            .setMessage("Permanently remove all imported contacts from the app model?")
            .setPositiveButton(R.string.delete) { _, _ ->
                viewModel.setImportedDeviceContacts(mutableListOf())
                showToast("All imported contacts deleted")
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        activity?.title = if (adapter.getSelectedContacts().isNotEmpty()) 
            "Device Contacts (${adapter.getSelectedContacts().size} selected)" 
            else "Device Contacts"
    }
}
