package com.example.phonebook

import android.os.Bundle
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider

class AppContactsFragment : Fragment() {

    private lateinit var storage: ContactStorage
    private lateinit var adapter: ContactAdapter
    private lateinit var viewModel: MainViewModel
    private val contacts = mutableListOf<Contact>()

    private val createDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            if (storage.exportToUri(it, contacts)) {
                showToast(getString(R.string.msg_export_success))
            } else {
                showToast(getString(R.string.msg_export_failed))
            }
        }
    }

    private val openDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            val importedList = storage.importFromUri(it)
            if (importedList != null) {
                val currentContacts = viewModel.appContacts.value ?: contacts
                val newUniqueContacts = ContactUtils.getUniqueNewContacts(importedList, currentContacts)

                if (newUniqueContacts.isNotEmpty()) {
                    val updatedList = currentContacts.toMutableList()
                    updatedList.addAll(newUniqueContacts)
                    
                    storage.saveContactsInternal(updatedList)
                    viewModel.setAppContacts(updatedList)
                    
                    showToast("${newUniqueContacts.size} new unique contacts imported.")
                } else {
                    showToast("No new unique contacts found in the selected file.")
                }
            } else {
                showToast(getString(R.string.msg_import_invalid))
            }
        }
    }

    private val sampleContacts = listOf(
        Contact(1, "Rahul", "9876543210"),
        Contact(2, "Priya", "9123456789"),
        Contact(3, "Kiran", "8765432109"),
        Contact(4, "Anjali", "7896541230"),
        Contact(5, "Rohan", "9654321870")
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_contact_list, container, false)
        setHasOptionsMenu(true)

        storage = ContactStorage(requireContext())
        viewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        
        val rvContacts = view.findViewById<RecyclerView>(R.id.rvContacts)
        adapter = ContactAdapter(contacts, ::onEditContact, ::onDeleteContact) {
            updateSelectionCount()
        }
        rvContacts.layoutManager = LinearLayoutManager(requireContext())
        rvContacts.adapter = adapter

        observeViewModel()
        if (!viewModel.isAppContactsLoaded) {
            initializeContacts()
        }
        return view
    }

    private fun observeViewModel() {
        viewModel.appContacts.observe(viewLifecycleOwner) { list ->
            contacts.clear()
            contacts.addAll(list)
            adapter.updateContacts(contacts)
            updateEmptyState()
        }
    }

    private fun initializeContacts() {
        val loaded = storage.loadContactsInternal()
        val initialList = mutableListOf<Contact>()
        if (loaded == null) {
            initialList.addAll(sampleContacts)
            storage.saveContactsInternal(initialList)
        } else {
            initialList.addAll(loaded)
        }
        viewModel.setAppContacts(initialList)
    }

    fun addContact() {
        showContactDialog(null)
    }

    private fun saveAndRefresh() {
        storage.saveContactsInternal(contacts)
        viewModel.setAppContacts(contacts)
    }

    private fun updateSelectionCount() {
        val count = adapter.getSelectedContacts().size
        if (count > 0) {
            activity?.title = "App Contacts ($count selected)"
        } else {
            activity?.title = "App Contacts"
        }
    }

    override fun onResume() {
        super.onResume()
        updateSelectionCount()
    }

    private fun updateEmptyState() {
        view?.findViewById<TextView>(R.id.tvEmptyState)?.visibility =
            if (contacts.isEmpty()) View.VISIBLE else View.GONE
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
            .setTitle(if (contact == null) R.string.add_contact else R.string.edit_contact)
            .setView(dialogView)
            .setPositiveButton(R.string.save, null)
            .setNegativeButton(R.string.cancel, null)
            .create().apply {
                show()
                getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                    val name = etName.text.toString().trim()
                    val phone = etPhone.text.toString().trim()

                    var isValid = true
                    if (name.isEmpty()) {
                        tilName.error = getString(R.string.error_name_empty)
                        isValid = false
                    } else {
                        tilName.error = null
                    }

                    if (phone.length != 10 || !phone.all { it.isDigit() }) {
                        tilPhone.error = getString(R.string.error_phone_invalid)
                        isValid = false
                    } else {
                        tilPhone.error = null
                    }

                    if (isValid) {
                        if (contact == null && ContactUtils.isDuplicate(name, phone, contacts)) {
                            tilPhone.error = "This contact already exists"
                        } else {
                            if (contact == null) {
                                val newContact = Contact(System.currentTimeMillis(), name, phone)
                                contacts.add(newContact)
                                showToast(getString(R.string.msg_added))
                            } else {
                                val updatedContact = contact.copy(name = name, phoneNumber = phone)
                                val index = contacts.indexOfFirst { it.id == contact.id }
                                if (index != -1) {
                                    contacts[index] = updatedContact
                                    showToast(getString(R.string.msg_updated))
                                }
                            }
                            saveAndRefresh()
                            dismiss()
                        }
                    }
                }
            }
    }

    private fun onEditContact(contact: Contact) {
        showContactDialog(contact)
    }

    private fun onDeleteContact(contact: Contact) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.delete_contact)
            .setMessage(R.string.delete_confirmation)
            .setPositiveButton(R.string.delete) { _, _ ->
                contacts.removeAll { it.id == contact.id }
                saveAndRefresh()
                showToast(getString(R.string.msg_deleted))
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_export_app -> {
                val path = storage.exportToAppSpecificExternal(contacts)
                if (path != null) {
                    Toast.makeText(requireContext(), "${getString(R.string.msg_export_success)} to: $path", Toast.LENGTH_LONG).show()
                } else {
                    showToast(getString(R.string.msg_export_failed))
                }
                true
            }
            R.id.action_import_app -> {
                val importedList = storage.readFromAppSpecificExternal()
                if (importedList != null) {
                    val currentContacts = viewModel.appContacts.value ?: contacts
                    val newUniqueContacts = ContactUtils.getUniqueNewContacts(importedList, currentContacts)

                    if (newUniqueContacts.isNotEmpty()) {
                        val updatedList = currentContacts.toMutableList()
                        updatedList.addAll(newUniqueContacts)
                        storage.saveContactsInternal(updatedList)
                        viewModel.setAppContacts(updatedList)
                        showToast("${newUniqueContacts.size} unique contacts imported from backup")
                    } else {
                        showToast("No new unique contacts found in backup")
                    }
                } else {
                    showToast(getString(R.string.msg_external_backup_not_found))
                }
                true
            }
            R.id.action_export_saf -> {
                createDocumentLauncher.launch("contacts_export.json")
                true
            }
            R.id.action_import_saf -> {
                openDocumentLauncher.launch(arrayOf("application/json"))
                true
            }
            R.id.action_delete_selected -> {
                val selected = adapter.getSelectedContacts()
                if (selected.isNotEmpty()) {
                    AlertDialog.Builder(requireContext())
                        .setTitle(R.string.delete_selected)
                        .setMessage(getString(R.string.msg_delete_selected_confirmation, selected.size))
                        .setPositiveButton(R.string.delete) { _, _ ->
                            contacts.removeAll(selected)
                            saveAndRefresh()
                            showToast(getString(R.string.msg_selected_deleted))
                        }
                        .setNegativeButton(R.string.cancel, null)
                        .show()
                } else {
                    showToast(getString(R.string.msg_no_selection))
                }
                true
            }
            R.id.action_clear_all -> {
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.clear_all_menu)
                    .setMessage(R.string.clear_all_confirmation)
                    .setPositiveButton(R.string.delete) { _, _ ->
                        contacts.clear()
                        saveAndRefresh()
                        showToast(getString(R.string.msg_cleared))
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
