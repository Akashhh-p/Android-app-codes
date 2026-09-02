package com.example.phonebook

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

class ContactAdapter(
    private var contacts: List<Contact>,
    private val onEdit: (Contact) -> Unit,
    private val onDelete: (Contact) -> Unit,
    private val onSelectionChanged: () -> Unit
) : RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {

    class ContactViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvPhone: TextView = view.findViewById(R.id.tvPhone)
        val cbSelect: CheckBox = view.findViewById(R.id.cbSelect)
        val btnEdit: MaterialButton = view.findViewById(R.id.btnEdit)
        val btnDelete: MaterialButton = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_contact, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contacts[position]
        holder.tvName.text = contact.name
        holder.tvPhone.text = contact.phoneNumber
        
        holder.cbSelect.setOnCheckedChangeListener(null)
        holder.cbSelect.isChecked = contact.isSelected
        holder.cbSelect.setOnCheckedChangeListener { _, isChecked ->
            contact.isSelected = isChecked
            onSelectionChanged()
        }

        holder.btnEdit.setOnClickListener { onEdit(contact) }
        holder.btnDelete.setOnClickListener { onDelete(contact) }
    }

    override fun getItemCount() = contacts.size

    fun updateContacts(newContacts: List<Contact>) {
        this.contacts = newContacts
        notifyDataSetChanged()
    }

    fun getSelectedContacts(): List<Contact> {
        return contacts.filter { it.isSelected }
    }

    fun selectAll(select: Boolean) {
        contacts.forEach { it.isSelected = select }
        notifyDataSetChanged()
        onSelectionChanged()
    }
}
